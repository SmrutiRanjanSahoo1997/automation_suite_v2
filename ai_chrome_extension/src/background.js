// Background Service Worker for Copilot AI Agent Extension (v2.0.0)
// Connects to Java AI Agent WebSocket Server at ws://127.0.0.1:9020/ws/extension
// Manages distinct multi-tab creation per conversation link and 15-min tab cleanup

importScripts('event-constants.js');

console.log("[Copilot Extension] Background service worker initialized.");

let ws = null;
let pingInterval = null;
let reconnectTimer = null;
let isConnecting = false;

// Cache of open conversation tabs: conversationId -> { tabId: number, lastAccessedMs: number }
const activeConversationTabs = new Map();
let activeChatTabId = null;

// Pending file requests map: path -> Array<{ resolve: Function, reject: Function }>
const pendingFileRequests = new Map();

// Initialize WebSocket connection to Java AI Agent Server
function connectWebSocket() {
  if (isConnecting || (ws && (ws.readyState === WebSocket.CONNECTING || ws.readyState === WebSocket.OPEN))) {
    return;
  }

  isConnecting = true;
  const wsUrl = (typeof EventConstants !== "undefined" && EventConstants.EXTSN_WS_URL) ? EventConstants.EXTSN_WS_URL : "ws://127.0.0.1:9020/ws/extension";
  console.log(`[Copilot Extension] Connecting to WebSocket ${wsUrl}...`);

  try {
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      isConnecting = false;
      console.log("[Copilot Extension] WebSocket connection established successfully!");

      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
      
      // Start 10-second ping heartbeat to keep connection and MV3 Service Worker active
      if (pingInterval) clearInterval(pingInterval);
      pingInterval = setInterval(sendPing, 10000);
      
      // Send immediate initial ping
      sendPing();
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        handleServerMessage(msg);
      } catch (err) {
        console.error("[Copilot Extension] Failed to parse WebSocket message:", err, event.data);
      }
    };

    ws.onerror = (err) => {
      isConnecting = false;
      console.warn("[Copilot Extension] WebSocket error detected.");
      scheduleReconnect();
    };

    ws.onclose = () => {
      isConnecting = false;
      console.warn("[Copilot Extension] WebSocket disconnected.");
      if (pingInterval) {
        clearInterval(pingInterval);
        pingInterval = null;
      }
      scheduleReconnect();
    };

  } catch (err) {
    isConnecting = false;
    console.error("[Copilot Extension] Connection initialization error:", err);
    scheduleReconnect();
  }
}

// Schedule automatic reconnect with 1-second retry
function scheduleReconnect() {
  if (!reconnectTimer) {
    console.log("[Copilot Extension] Scheduling WebSocket reconnect in 1 second...");
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null;
      connectWebSocket();
    }, 1000);
  }
}

// Send 10s Heartbeat EXTSN_PING & reset Chrome MV3 idle countdown
function sendPing() {
  chrome.runtime.getPlatformInfo(() => {});

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ event: EventConstants.EXTSN_PING }));
  }
}

// Keep-Alive Alarm to wake up service worker and ensure active connection
chrome.alarms.create("wsKeepAliveAlarm", { periodInMinutes: 0.5 });
chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === "wsKeepAliveAlarm") {
    keepAliveCheck();
  }
});

function keepAliveCheck() {
  chrome.runtime.getPlatformInfo(() => {});

  if (!ws || ws.readyState !== WebSocket.CLOSED || ws.readyState === WebSocket.CLOSING) {
    console.log("[Copilot Extension] Keep-alive alarm triggered reconnect.");
    connectWebSocket();
  } else if (ws.readyState === WebSocket.OPEN) {
    sendPing();
  }
}

// Handle Incoming Messages from Java AI Agent Server
function handleServerMessage(msg) {
  const event = msg.event || msg.action;

  if (event === EventConstants.EXTSN_PONG) {
    console.log("[Copilot Extension] Heartbeat EXTSN_PONG received from AI Agent Server.");
    return;
  }

  if (event === EventConstants.EXTSN_CHAT) {
    console.log("[Copilot Extension] Received EXTSN_CHAT task:", msg);
    executeChatTurn(msg);
    return;
  }

  if (event === EventConstants.EXTSN_DELETE) {
    console.log("[Copilot Extension] Received EXTSN_DELETE task:", msg);
    executeDeleteTurn(msg);
    return;
  }

  if (event === EventConstants.EXTSN_VERIFICATION_RESOLVED) {
    console.log("[Copilot Extension] Received EXTSN_VERIFICATION_RESOLVED from AI Agent Server.");
    if (activeChatTabId) {
      chrome.tabs.sendMessage(activeChatTabId, { action: "VERIFICATION_RESOLVED", status: msg.status || "OK" }).catch(() => {});
    }
    return;
  }

  if (event === EventConstants.EXTSN_FILE_RESPONSE) {
    console.log("[Copilot Extension] Received EXTSN_FILE_RESPONSE:", msg.path || msg.fileName);
    let pathKey = msg.path || msg.filePath;

    // Fallback search by fileName if path is not provided
    if (!pathKey && msg.fileName) {
      for (const k of pendingFileRequests.keys()) {
        if (k.endsWith(msg.fileName)) {
          pathKey = k;
          break;
        }
      }
    }

    if (pathKey && pendingFileRequests.has(pathKey)) {
      const callbacks = pendingFileRequests.get(pathKey);
      callbacks.forEach(cb => cb.resolve(msg));
      pendingFileRequests.delete(pathKey);
    } else {
      console.warn("[Copilot Extension] Received EXTSN_FILE_RESPONSE but no matching pending request found for:", pathKey || msg.fileName);
    }
    return;
  }

  console.warn("[Copilot Extension] Unrecognized server message event:", event);
}

// Execute Chat Turn in Copilot Tab
async function executeChatTurn(req) {
  const taskId = req.taskId || null;
  const convId = req.conversationId || req.conversation_id || null;
  const isConvIdValid = convId && convId !== "null" && convId.toLowerCase() !== "null" && convId.trim() !== "";
  
  let targetUrl = "https://copilot.com/chat";
  if (isConvIdValid) {
    targetUrl = `https://copilot.com/chat/conversation/${convId}`;
  }

  console.log(`[Copilot Extension] Target URL computed for taskId '${taskId}', convId '${convId}': ${targetUrl}`);

  try {
    const targetTab = await resolveCopilotTab(convId, targetUrl);

    if (!targetTab || !targetTab.id) {
      sendChatResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Could not locate or open Copilot tab.", 0);
      return;
    }

    // Update tab access cache timestamp
    activeChatTabId = targetTab.id;
    if (targetTab.windowId) {
      chrome.windows.update(targetTab.windowId, { focused: true, state: 'normal' }).catch(() => {});
    }

    if (isConvIdValid) {
      activeConversationTabs.set(convId, { tabId: targetTab.id, lastAccessedMs: Date.now() });
    }

    // Give content script time to settle after navigation/mount
    await new Promise(r => setTimeout(r, 1500));

    // Send chat command to content script inside target tab with retry loop for script readiness
    let responseSent = false;
    const maxAttempts = 15;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      const result = await new Promise((resolve) => {
        chrome.tabs.sendMessage(targetTab.id, {
          action: "RUN_CHAT",
          taskId: taskId,
          conversationId: convId,
          prompt: req.prompt,
          files_path: req.files_path || req.files || []
        }, (response) => {
          if (chrome.runtime.lastError) {
            resolve({ error: chrome.runtime.lastError.message });
          } else {
            resolve({ response: response });
          }
        });
      });

      if (result.error) {
        if ((result.error.includes("Could not establish connection") || result.error.includes("Receiving end does not exist")) && attempt < maxAttempts) {
          console.warn(`[Copilot Extension] Chat tab script not ready yet (Attempt ${attempt}/${maxAttempts}). Retrying in 500ms...`);
          await new Promise(r => setTimeout(r, 500));
          continue;
        }

        console.warn("[Copilot Extension] Error communicating with tab:", result.error);
        sendChatResponseToAgent(
          EventConstants.STATUS_FAILED,
          taskId,
          convId,
          null,
          "Could not reach Copilot tab script: " + result.error + ". Please refresh copilot.com/chat tab.",
          0
        );
        responseSent = true;
        break;
      }

      if (result.response) {
        const status = (result.response.status === "RESPONSE" || result.response.status === "RECEIVED") ? EventConstants.STATUS_RESPONSE : EventConstants.STATUS_FAILED;
        const resConvId = result.response.conversationId || convId;
        const resTaskId = result.response.taskId || taskId;
        const elapsedMs = result.response.responseTimeMs || result.response.response_time_ms || 0;

        if (resConvId && resConvId !== "null" && targetTab && targetTab.id) {
          activeConversationTabs.set(resConvId, { tabId: targetTab.id, lastAccessedMs: Date.now() });
        }
        
        sendChatResponseToAgent(status, resTaskId, resConvId, result.response.response, result.response.error, elapsedMs);
        responseSent = true;
        break;
      } else {
        sendChatResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Empty response received from content script.", 0);
        responseSent = true;
        break;
      }
    }

    if (!responseSent) {
      sendChatResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Could not reach Copilot tab script: Connection timed out.", 0);
    }

  } catch (err) {
    console.error("[Copilot Extension] Error executing chat turn:", err);
    sendChatResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Extension tab execution error: " + err.message, 0);
  }
}

// Locate or Open Copilot Tab based on conversationId
// Real-time resolution: Always queries Chrome tabs real-time first. Cache is updated for idle cleanup only.
async function resolveCopilotTab(convId, targetUrl) {
  const isConvIdValid = convId && convId !== "null" && convId.toLowerCase() !== "null" && convId.trim() !== "";

  // 1. Real-time query of all open Copilot tabs in Chrome
  const allCopilotTabs = await chrome.tabs.query({ url: ["https://copilot.com/*", "https://copilot.microsoft.com/*"] });

  if (isConvIdValid && allCopilotTabs && allCopilotTabs.length > 0) {
    for (const tab of allCopilotTabs) {
      if (tab.url && tab.url.includes(`/conversation/${convId}`)) {
        console.log(`[Copilot Extension] Real-time match: found open tab ${tab.id} for convId '${convId}'`);
        await chrome.tabs.update(tab.id, { active: true });
        // Update cache timestamp for 15-min idle tab cleanup scheduler
        activeConversationTabs.set(convId, { tabId: tab.id, lastAccessedMs: Date.now() });
        return tab;
      }
    }
  }

  // 2. Link/conversationId is NOT open in any browser tab (or New Chat) -> CREATE A NEW TAB!
  console.log(`[Copilot Extension] Real-time resolution: link not open in Chrome. Creating NEW TAB for targetUrl: ${targetUrl}`);
  const newTab = await chrome.tabs.create({ url: targetUrl, active: true });
  await waitForTabComplete(newTab.id);

  if (isConvIdValid) {
    activeConversationTabs.set(convId, { tabId: newTab.id, lastAccessedMs: Date.now() });
  }

  return newTab;
}

// Send EXTSN_CHAT_RESPONSE to Java AI Agent Server via WebSocket
function sendChatResponseToAgent(status, taskId, conversationId, response, error, responseTimeMs) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    const payload = {
      event: EventConstants.EXTSN_CHAT_RESPONSE,
      taskId: taskId,
      status: status,
      conversationId: conversationId,
      response: response,
      error: error,
      responseTimeMs: responseTimeMs,
      responseAt: Date.now()
    };
    ws.send(JSON.stringify(payload));
    console.log("[Copilot Extension] Sent EXTSN_CHAT_RESPONSE to AI Agent Server [TaskId: " + taskId + ", Status: " + status + "]");
  } else {
    console.error("[Copilot Extension] Cannot send EXTSN_CHAT_RESPONSE: WebSocket is not open.");
  }
}

// Execute Delete Turn in Copilot Tab
async function executeDeleteTurn(req) {
  const taskId = req.taskId || null;
  const convId = req.conversationId || req.conversation_id || null;
  const isConvIdValid = convId && convId !== "null" && convId.toLowerCase() !== "null" && convId.trim() !== "";

  if (!isConvIdValid) {
    sendDeleteResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "conversationId is mandatory for delete operation.");
    return;
  }

  const targetUrl = `https://copilot.com/chat/conversation/${convId}`;
  console.log(`[Copilot Extension] Delete target URL for taskId '${taskId}', convId '${convId}': ${targetUrl}`);

  try {
    const targetTab = await resolveCopilotTab(convId, targetUrl);

    if (!targetTab || !targetTab.id) {
      sendDeleteResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Could not locate or open Copilot conversation tab.");
      return;
    }

    // Update tab access cache timestamp
    if (isConvIdValid) {
      activeConversationTabs.set(convId, { tabId: targetTab.id, lastAccessedMs: Date.now() });
    }

    // Give content script time to settle after navigation/mount
    await new Promise(r => setTimeout(r, 1500));

    // Send delete command to content script inside target tab with retry loop for script readiness
    let responseSent = false;
    const maxAttempts = 15;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      const result = await new Promise((resolve) => {
        chrome.tabs.sendMessage(targetTab.id, {
          action: "DELETE_CHAT",
          taskId: taskId,
          conversationId: convId
        }, (response) => {
          if (chrome.runtime.lastError) {
            resolve({ error: chrome.runtime.lastError.message });
          } else {
            resolve({ response: response });
          }
        });
      });

      if (result.error) {
        if ((result.error.includes("Could not establish connection") || result.error.includes("Receiving end does not exist")) && attempt < maxAttempts) {
          console.warn(`[Copilot Extension] Delete tab script not ready yet (Attempt ${attempt}/${maxAttempts}). Retrying in 500ms...`);
          await new Promise(r => setTimeout(r, 500));
          continue;
        }

        console.warn("[Copilot Extension] Error communicating with tab for delete:", result.error);
        sendDeleteResponseToAgent(
          EventConstants.STATUS_FAILED,
          taskId,
          convId,
          null,
          "Could not reach Copilot tab script: " + result.error
        );
        responseSent = true;
        break;
      }

      if (result.response) {
        const status = (result.response.status === "RESPONSE" || result.response.status === "RECEIVED") ? EventConstants.STATUS_RESPONSE : EventConstants.STATUS_FAILED;
        const resConvId = result.response.conversationId || convId;
        const resTaskId = result.response.taskId || taskId;

        if (resConvId) {
          activeConversationTabs.delete(resConvId);
        }

        sendDeleteResponseToAgent(status, resTaskId, resConvId, result.response.message || "The conversation deleted", result.response.error);
        responseSent = true;
        break;
      } else {
        sendDeleteResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Empty response received from content script during delete.");
        responseSent = true;
        break;
      }
    }

    if (!responseSent) {
      sendDeleteResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Could not reach Copilot tab script: Connection timed out.");
    }

  } catch (err) {
    console.error("[Copilot Extension] Error executing delete turn:", err);
    sendDeleteResponseToAgent(EventConstants.STATUS_FAILED, taskId, convId, null, "Extension tab delete execution error: " + err.message);
  }
}

// Send EXTSN_DELETE_RESPONSE to Java AI Agent Server via WebSocket
function sendDeleteResponseToAgent(status, taskId, conversationId, message, error) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    const payload = {
      event: EventConstants.EXTSN_DELETE_RESPONSE,
      taskId: taskId,
      status: status,
      conversationId: conversationId,
      message: message,
      error: error,
      responseAt: Date.now()
    };
    ws.send(JSON.stringify(payload));
    console.log("[Copilot Extension] Sent EXTSN_DELETE_RESPONSE to AI Agent Server [TaskId: " + taskId + ", Status: " + status + "]");
  } else {
    console.error("[Copilot Extension] Cannot send EXTSN_DELETE_RESPONSE: WebSocket is not open.");
  }
}

// 15-Minute Inactive Tab Cleanup Scheduler
function cleanupInactiveTabs() {
  const FIFTEEN_MINUTES = 15 * 60 * 1000;
  const now = Date.now();

  activeConversationTabs.forEach((tabInfo, convId) => {
    if (now - tabInfo.lastAccessedMs > FIFTEEN_MINUTES) {
      console.log(`[Copilot Extension] Closing inactive tab ${tabInfo.tabId} for convId '${convId}' (Idle > 15 mins)`);
      chrome.tabs.remove(tabInfo.tabId, () => {
        if (chrome.runtime.lastError) {
          // Tab already closed by user
        }
      });
      activeConversationTabs.delete(convId);
    }
  });
}

// Run inactive tab cleanup every 2 minutes
setInterval(cleanupInactiveTabs, 120000);

function waitForTabComplete(tabId) {
  return new Promise((resolve) => {
    const listener = (updatedTabId, changeInfo) => {
      if (updatedTabId === tabId && changeInfo.status === 'complete') {
        chrome.tabs.onUpdated.removeListener(listener);
        resolve();
      }
    };
    chrome.tabs.onUpdated.addListener(listener);

    setTimeout(() => {
      chrome.tabs.onUpdated.removeListener(listener);
      resolve();
    }, 8000);
  });
}

// Listen for messages from content.js (e.g. GET_FILE request)
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "GET_FILE" && request.path) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      sendResponse({ success: false, error: "Extension WebSocket not connected to AI Agent Server." });
      return true;
    }

    const filePath = request.path;
    if (!pendingFileRequests.has(filePath)) {
      pendingFileRequests.set(filePath, []);
    }

    pendingFileRequests.get(filePath).push({
      resolve: (fileResponse) => sendResponse(fileResponse),
      reject: (err) => sendResponse({ success: false, error: err.message })
    });

    // Send EXTSN_FILE_REQUEST to Java AI Agent Server
    ws.send(JSON.stringify({
      event: EventConstants.EXTSN_FILE_REQUEST,
      path: filePath
    }));

    return true; // Keep channel open for async response
  }

  if (request.action === "VERIFICATION_REQUIRED") {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      sendResponse({ success: false, error: "Extension WebSocket not connected to AI Agent Server." });
      return true;
    }
    console.log(`[Copilot Extension] Forwarding VERIFICATION_REQUIRED to AI Agent Server at (${request.screenX}, ${request.screenY})...`);
    ws.send(JSON.stringify({
      event: EventConstants.EXTSN_VERIFICATION_REQUIRED,
      taskId: request.taskId || null,
      screenX: request.screenX,
      screenY: request.screenY
    }));
    sendResponse({ success: true, forwarded: true });
    return true;
  }

  if (request.action === "SENT") {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        event: EventConstants.EXTSN_SENT,
        taskId: request.taskId || null,
        conversationId: request.conversationId || null,
        timestamp: Date.now()
      }));
      console.log("[Copilot Extension] Sent EXTSN_SENT acknowledgment to AI Agent Server [TaskId: " + request.taskId + "]");
    }
    return true;
  }

  if (request.action === "PING") {
    sendResponse({ status: "alive", extension: "Copilot AI Agent Extension v2.0.0", wsConnected: ws && ws.readyState === WebSocket.OPEN });
    return true;
  }
});

// Start WebSocket connection immediately on service worker startup
connectWebSocket();
