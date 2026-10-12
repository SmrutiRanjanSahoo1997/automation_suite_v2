// Content Script for Copilot AI Agent Extension (v2.0.0)
// Runs inside copilot.com web page

console.log("[Copilot Extension] Content script loaded on page:", window.location.href);

const ALLOWED_EXTENSIONS = new Set([
  ".txt", ".log", ".pdf", ".doc", ".docx", ".xlsx", ".xls", ".csv",
  ".ppt", ".pptx", ".xml", ".html", ".htm", ".json", ".md",
  ".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp", ".svg",
  ".py", ".java"
]);

// Listen for chat automation requests from background.js
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "RUN_CHAT") {
    handleChatTurn(request)
      .then((result) => sendResponse(result))
      .catch((err) => sendResponse({
        status: EventConstants.STATUS_FAILED,
        conversationId: request.conversationId || request.conversation_id || null,
        response: null,
        error: err.message || "Automation failed.",
        responseTimeMs: 0
      }));
    return true; // Asynchronous response
  }

  if (request.action === "DELETE_CHAT") {
    handleDeleteTurn(request)
      .then((result) => sendResponse(result))
      .catch((err) => sendResponse({
        status: EventConstants.STATUS_FAILED,
        taskId: request.taskId || null,
        conversationId: request.conversationId || request.conversation_id || null,
        message: null,
        error: err.message || "Delete automation failed."
      }));
    return true; // Asynchronous response
  }
});

let isResolvingVerification = false;

async function autoCheckVerificationBox() {
  if (isResolvingVerification) return false;
  try {
    const candidates = [];
    findVerificationCandidatesRecursive(document, candidates);

    for (const el of candidates) {
      if (isElementVisible(el)) {
        console.log("[Copilot Extension] Verification candidate detected:", el);
        return await requestPyAutoGuiVerificationClick(el);
      }
    }

    if (isVerificationRequiredPresent()) {
      console.log("[Copilot Extension] Verification required text detected on page without explicit input element.");
      return await requestPyAutoGuiVerificationClick(null);
    }
  } catch (err) {
    // Silent catch
  }
  return false;
}

async function requestPyAutoGuiVerificationClick(el) {
  if (isResolvingVerification) return false;
  isResolvingVerification = true;

  try {
    let screenX = 0;
    let screenY = 0;

    if (el && typeof el.getBoundingClientRect === 'function') {
      const rect = el.getBoundingClientRect();
      const headerOffset = Math.max(0, window.outerHeight - window.innerHeight);
      // Target checkbox square: left + 20px, middle of height
      screenX = Math.round(window.screenX + rect.left + Math.min(25, rect.width / 2));
      screenY = Math.round(window.screenY + headerOffset + rect.top + rect.height / 2);
    } else {
      // Fallback screen coordinates for centered modal
      const headerOffset = Math.max(0, window.outerHeight - window.innerHeight);
      screenX = Math.round(window.screenX + (window.innerWidth * 0.42));
      screenY = Math.round(window.screenY + headerOffset + (window.innerHeight * 0.53));
    }

    console.log(`[Copilot Extension] Requesting PyAutoGUI OS click for verification checkbox at screen (${screenX}, ${screenY})...`);

    // Also attempt synthetic click as instant fallback
    if (el) clickVerificationElement(el);

    // Dispatch OS hardware mouse click via background.js
    chrome.runtime.sendMessage({
      action: "VERIFICATION_REQUIRED",
      screenX: screenX,
      screenY: screenY
    });

    // Wait up to 3 seconds for modal to clear
    for (let i = 0; i < 15; i++) {
      await sleep(200);
      if (!isVerificationRequiredPresent()) {
        console.log("[Copilot Extension] Verification box successfully cleared!");
        isResolvingVerification = false;
        return true;
      }
    }
  } catch (e) {
    console.warn("[Copilot Extension] Error requesting PyAutoGUI click:", e);
  } finally {
    isResolvingVerification = false;
  }
  return false;
}

function findVerificationCandidatesRecursive(root, results) {
  if (!root) return;

  const allElements = root.querySelectorAll ? root.querySelectorAll('*') : [];
  for (const el of allElements) {
    const tag = el.tagName ? el.tagName.toLowerCase() : '';
    const role = el.getAttribute ? (el.getAttribute('role') || '').toLowerCase() : '';
    const text = (el.innerText || el.textContent || '').trim().toLowerCase();
    const ariaLabel = el.getAttribute ? (el.getAttribute('aria-label') || '').toLowerCase() : '';
    const title = el.getAttribute ? (el.getAttribute('title') || '').toLowerCase() : '';

    const isMatch = text.includes('verify you are human') || 
                    text.includes('verification required') || 
                    ariaLabel.includes('verify') || 
                    title.includes('verify');

    if (isMatch) {
      if ((tag === 'input' && el.type === 'checkbox') || role === 'checkbox' || tag === 'fluent-checkbox') {
        results.push(el);
      } else if (text === 'verify you are human' || text.includes('verify you are human')) {
        const innerCb = el.querySelector ? el.querySelector('input[type="checkbox"], [role="checkbox"], fluent-checkbox') : null;
        results.push(innerCb || el);
      }
    }

    // Deep traverse Shadow Root
    if (el.shadowRoot) {
      findVerificationCandidatesRecursive(el.shadowRoot, results);
    }
  }

  // Deep traverse IFrames
  const iframes = root.querySelectorAll ? root.querySelectorAll('iframe, frame') : [];
  for (const iframe of iframes) {
    try {
      const doc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
      if (doc) {
        findVerificationCandidatesRecursive(doc, results);
      }
    } catch (e) {
      // Cross-origin iframe
    }
  }
}

function clickVerificationElement(el) {
  if (!el) return;
  try {
    if (typeof el.focus === 'function') el.focus();
    if (el.tagName === 'INPUT' && el.type === 'checkbox') {
      el.click();
      el.checked = true;
      el.dispatchEvent(new Event('change', { bubbles: true }));
      el.dispatchEvent(new Event('input', { bubbles: true }));
    } else {
      el.click();
      if (el.parentElement) {
        el.parentElement.click();
      }
    }
  } catch (e) {
    try { el.click(); } catch (err) {}
  }
}

function triggerFullClick(el) {
  clickVerificationElement(el);
}

async function handleChatTurn(req) {
  const started = performance.now();
  const convId = req.conversationId || req.conversation_id || null;
  const prompt = (req.prompt || req.message || "").trim();
  const filesList = req.files_path || req.files || [];

  // 1. Mandatory Prompt Validation
  if (!prompt) {
    throw new Error("Prompt is mandatory and cannot be empty.");
  }
  
  // 2. Max 3 Files Limit Validation
  if (filesList.length > 3) {
    throw new Error("Max 3 file paths allowed per turn.");
  }

  // 3. Extension Validation
  for (const f of filesList) {
    const dotIdx = f.lastIndexOf('.');
    const ext = dotIdx >= 0 ? f.substring(dotIdx).toLowerCase() : '';
    if (!ext || !ALLOWED_EXTENSIONS.has(ext)) {
      throw new Error(`File extension not allowed: ${ext}`);
    }
  }

  // Check invalid conversation popup
  if (checkInvalidConversation()) {
    throw new Error("The conversation no longer exists.");
  }

  // 5. Attach files if present via background WebSocket proxy
  if (filesList.length > 0) {
    console.log("[Copilot Extension] Attaching local files via AI Agent:", filesList);
    await attachFilesFromAgent(filesList);
  }

  // 6. Find input box where "Message Copilot"
  const editor = await findInputEditor();
  if (!editor) {
    throw new Error("Could not find 'Message Copilot' input box on page.");
  }

  // 7. Paste prompt into editor with verification check
  console.log("[Copilot Extension] Pasting prompt into editor...");
  await pastePromptIntoEditor(editor, prompt);
  await sleep(400);

  // 8. Click send button with 1-sec retry loop and pre-send verification
  console.log("[Copilot Extension] Clicking send button (1-sec retry loop)...");
  const submitted = await clickSendWith1SecRetry(editor, prompt);
  if (!submitted) {
    if (isVerificationRequiredPresent()) {
      throw new Error("Bot detected: Verification required dialog appeared.");
    }
    throw new Error("Could not submit prompt to Copilot.");
  }

  // 1-second buffer for Copilot React app to mount turn before re-minimizing
  await sleep(1000);

  // Notify background script that prompt was submitted successfully (triggers re-minimization)
  try {
    chrome.runtime.sendMessage({ action: "SENT", taskId: req.taskId || null, conversationId: convId });
  } catch (e) {}

  // 9. Wait until generation complete (stop button disappears)
  console.log("[Copilot Extension] Waiting for Copilot generation to complete...");
  await waitUntilStopButtonInvisible();

  // 10. Extract exact conversationId from URL bar and read response
  let extractedConvId = extractConversationIdFromUrl(window.location.href);
  for (let i = 0; i < 10 && !extractedConvId; i++) {
    await sleep(200);
    extractedConvId = extractConversationIdFromUrl(window.location.href);
  }
  if (!extractedConvId) {
    extractedConvId = convId;
  }

  const responseText = readLatestResponse();
  const elapsedMs = Math.round(performance.now() - started);

  if (!responseText || responseText.trim().length === 0) {
    if (isVerificationRequiredPresent()) {
      throw new Error("Bot detected: Verification required dialog appeared.");
    }
    throw new Error("No response generated by Copilot.");
  }

  return {
    status: EventConstants.STATUS_RESPONSE,
    taskId: req.taskId || null,
    conversationId: extractedConvId,
    response: responseText,
    error: null,
    responseTimeMs: elapsedMs
  };
}

function isVerificationRequiredPresent() {
  try {
    const text = (document.body ? document.body.innerText : '').toLowerCase();
    const dialogs = document.querySelectorAll('div, dialog, [role="dialog"]');
    for (const d of dialogs) {
      const txt = (d.innerText || '').toLowerCase();
      if (txt.includes('verify you are human') || txt.includes('verification required')) {
        return true;
      }
    }
    return text.includes('verify you are human') || text.includes('verification required');
  } catch (e) {
    return false;
  }
}

function checkInvalidConversation() {
  const text = (document.body ? document.body.innerText : '').toLowerCase();
  const dialog = document.querySelector('[role="dialog"]');
  const dialogText = (dialog ? dialog.innerText : '').toLowerCase();
  const full = text + ' ' + dialogText;
  const keywords = [
    'this chat is no longer available',
    'this chat was recently deleted',
    'no longer available',
    'chat no longer available',
    'conversation no longer available',
    'conversation not found'
  ];
  return keywords.some(k => full.includes(k));
}

async function findInputEditor() {
  const selectors = [
    "#m365-chat-editor-target-element",
    ".fai-EditorInput__input",
    "[data-lexical-editor='true']",
    "[aria-label='Message Copilot']",
    "[aria-label*='Message Copilot']",
    "p[data-placeholder*='Message Copilot']",
    "textarea#userInput",
    "textarea[data-testid='composer-input']",
    "textarea[placeholder*='Message Copilot']",
    "[contenteditable='true']",
    "textarea"
  ];

  for (let i = 0; i < 15; i++) {
    autoCheckVerificationBox();
    for (const sel of selectors) {
      const el = document.querySelector(sel);
      if (el && isElementVisible(el)) {
        return el;
      }
    }
    await sleep(300);
  }
  return null;
}

function getEditorText(editor) {
  if (!editor) return "";
  if (editor.tagName === 'TEXTAREA' || editor.tagName === 'INPUT') {
    return (editor.value || "").trim();
  }
  return (editor.innerText || editor.textContent || "").trim();
}

function dispatchInputEvents(editor, text) {
  try {
    editor.dispatchEvent(new InputEvent('beforeinput', { inputType: 'insertText', data: text, bubbles: true }));
  } catch (e) {}
  editor.dispatchEvent(new Event('input', { bubbles: true }));
  editor.dispatchEvent(new Event('change', { bubbles: true }));
}

async function pastePromptIntoEditor(editor, text) {
  autoCheckVerificationBox();
  for (let attempt = 1; attempt <= 3; attempt++) {
    autoCheckVerificationBox();
    console.log(`[Copilot Extension] Pasting prompt attempt ${attempt}/3...`);

    editor.focus();
    let target = editor;
    if (editor.isContentEditable) {
      let p = editor.querySelector('p');
      if (!p) {
        p = document.createElement('p');
        editor.appendChild(p);
      }
      target = p;
      target.focus();
    }

    // Clear existing content to prevent concatenation
    if (editor.tagName === 'TEXTAREA' || editor.tagName === 'INPUT') {
      editor.value = '';
    } else {
      editor.innerText = '';
    }
    await sleep(100);

    // Strategy 1: execCommand insertText
    try {
      document.execCommand('insertText', false, text);
      dispatchInputEvents(editor, text);
    } catch (e) {}

    await sleep(150);
    if (getEditorText(editor).length > 0) {
      return;
    }

    // Strategy 2: Synthetic ClipboardEvent paste
    try {
      const dt = new DataTransfer();
      dt.setData('text/plain', text);
      const pasteEvt = new ClipboardEvent('paste', { clipboardData: dt, bubbles: true, cancelable: true });
      editor.dispatchEvent(pasteEvt);
      dispatchInputEvents(editor, text);
    } catch (e) {}

    await sleep(150);
    if (getEditorText(editor).length > 0) {
      return;
    }

    // Strategy 3: DOM textContent fallback
    if (editor.tagName === 'TEXTAREA' || editor.tagName === 'INPUT') {
      editor.value = text;
    } else {
      let p = editor.querySelector('p') || editor;
      p.textContent = text;
    }
    dispatchInputEvents(editor, text);

    await sleep(200);
    if (getEditorText(editor).length > 0) {
      return;
    }
  }

  if (getEditorText(editor).length === 0) {
    throw new Error("Could not paste prompt into Copilot editor after 3 attempts.");
  }
}

async function findFileInput() {
  const inputSelectors = [
    "input[type='file']",
    "input[data-testid*='file']",
    "input[data-testid*='upload']",
    "input[data-testid*='attach']",
    "input[accept]",
    "input[aria-label*='file' i]",
    "input[aria-label*='upload' i]"
  ];

  // Pass 1: Scan existing DOM for any file input element
  for (let i = 0; i < 8; i++) {
    for (const sel of inputSelectors) {
      const el = document.querySelector(sel);
      if (el) return el;
    }
    const allInputs = document.querySelectorAll('input');
    for (const inp of allInputs) {
      if (inp.type === 'file') return inp;
    }
    await sleep(250);
  }

  // Pass 2: Click attachment (+) button if input isn't mounted yet
  const buttonCandidates = Array.from(document.querySelectorAll('button, div[role="button"], span[role="button"]'));
  for (const btn of buttonCandidates) {
    const txt = (btn.getAttribute('aria-label') || btn.getAttribute('title') || btn.innerText || '').toLowerCase();
    if (txt.includes('add') || txt.includes('attach') || txt.includes('file') || txt.includes('upload') || txt.includes('content') || txt.trim() === '+') {
      try {
        btn.click();
        await sleep(400);
        for (const sel of inputSelectors) {
          const el = document.querySelector(sel);
          if (el) return el;
        }
      } catch (e) {}
    }
  }

  return document.querySelector("input[type='file']");
}

async function attachFilesFromAgent(filePaths) {
  const fileObjects = [];

  for (const pathStr of filePaths) {
    try {
      const fileData = await new Promise((resolve, reject) => {
        chrome.runtime.sendMessage({ action: "GET_FILE", path: pathStr }, (resp) => {
          if (chrome.runtime.lastError) {
            reject(new Error(chrome.runtime.lastError.message));
          } else if (resp && resp.success) {
            resolve(resp);
          } else {
            reject(new Error((resp && resp.error) || "Failed to fetch file data from AI Agent"));
          }
        });
      });

      if (fileData.base64) {
        const fileBlob = base64ToFile(fileData.base64, fileData.fileName, fileData.mimeType);
        fileObjects.push(fileBlob);
        console.log(`[Copilot Extension] Loaded file from AI Agent: ${fileData.fileName} (${fileData.size || 0} bytes)`);
      }
    } catch (err) {
      throw new Error(`AI Agent File Fetch Error ('${pathStr}'): ${err.message}`);
    }
  }

  if (fileObjects.length === 0) return;

  // Try finding input element
  let fileInput = await findFileInput();

  if (fileInput) {
    console.log("[Copilot Extension] Found file input element:", fileInput);
    const dataTransfer = new DataTransfer();
    fileObjects.forEach(f => dataTransfer.items.add(f));
    fileInput.files = dataTransfer.files;
    fileInput.dispatchEvent(new Event('change', { bubbles: true }));
    fileInput.dispatchEvent(new Event('input', { bubbles: true }));
    await sleep(2000);
    return;
  }

  // Fallback: Synthetic Drag and Drop on Copilot Editor Container
  console.log("[Copilot Extension] File input element not found directly. Attempting Drag & Drop attachment on editor...");
  const editor = await findInputEditor();
  if (editor) {
    const dt = new DataTransfer();
    fileObjects.forEach(f => dt.items.add(f));

    editor.dispatchEvent(new DragEvent('dragenter', { dataTransfer: dt, bubbles: true, cancelable: true }));
    editor.dispatchEvent(new DragEvent('dragover', { dataTransfer: dt, bubbles: true, cancelable: true }));
    await sleep(100);
    editor.dispatchEvent(new DragEvent('drop', { dataTransfer: dt, bubbles: true, cancelable: true }));
    await sleep(2000);
    return;
  }

  console.warn("[Copilot Extension] Could not locate file input or editor element for attachment.");
}

function base64ToFile(base64, fileName, mimeType) {
  const byteChars = atob(base64);
  const byteNumbers = new Array(byteChars.length);
  for (let i = 0; i < byteChars.length; i++) {
    byteNumbers[i] = byteChars.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  return new File([byteArray], fileName, { type: mimeType });
}

function countBotTurns() {
  const messageSelectors = [
    "div[data-content='ai-message']",
    "[data-content='ai-message']",
    "[data-testid='response-message']",
    "[data-message-author-role='assistant']",
    ".bot-turn",
    "[data-content='assistant']",
    "[role='article']"
  ];
  let count = 0;
  for (const sel of messageSelectors) {
    const els = document.querySelectorAll(sel);
    if (els && els.length > count) {
      count = els.length;
    }
  }
  return count;
}

function countUserTurns() {
  const userSelectors = [
    "[data-message-author-role='user']",
    ".user-turn",
    "[data-content='user']"
  ];
  let count = 0;
  for (const sel of userSelectors) {
    const els = document.querySelectorAll(sel);
    if (els && els.length > count) {
      count = els.length;
    }
  }
  return count;
}

function hasNewTurnAppeared(initialBotTurns, initialResponse) {
  const currentBotTurns = countBotTurns();
  if (currentBotTurns > initialBotTurns) {
    return true;
  }
  const currentResp = readLatestResponse();
  if (currentResp && currentResp.trim().length > 0 && currentResp !== initialResponse) {
    return true;
  }
  return false;
}

async function clickSendWith1SecRetry(editor, promptText) {
  const deadline = Date.now() + 15000;

  // Record baseline state before clicking send
  const initialResponse = readLatestResponse();
  const initialBotTurns = countBotTurns();
  let sendAttempted = false;

  while (Date.now() < deadline) {
    // Check if generation is actively occurring or already completed
    if (isBusy() || hasNewTurnAppeared(initialBotTurns, initialResponse)) {
      return true;
    }

    // Only paste if editor is empty AND we haven't already clicked send
    if (getEditorText(editor).length === 0 && promptText && !sendAttempted) {
      console.warn("[Copilot Extension] Editor text was empty before send click. Pasting prompt...");
      await pastePromptIntoEditor(editor, promptText);
      await sleep(200);
    }

    const sendSelectors = [
      "button[aria-label='Send']",
      "button[aria-label*='Submit' i]",
      "button[aria-label*='Send' i]",
      "button.fai-SendButton",
      "#composer-create-button",
      "button[title*='Send' i]",
      "button[title*='Submit' i]",
      "button[data-testid='submit-button']",
      "button[data-testid*='send']"
    ];

    let clicked = false;
    for (const sel of sendSelectors) {
      try {
        const btn = document.querySelector(sel);
        if (btn && isElementVisible(btn) && !btn.disabled && btn.getAttribute('aria-disabled') !== 'true') {
          console.log("[Copilot Extension] Clicking Send button via selector:", sel);
          btn.click();
          clicked = true;
          sendAttempted = true;
          break;
        }
      } catch (e) {}
    }

    if (!clicked && editor) {
      editor.focus();
      editor.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }));
      sendAttempted = true;
    }

    await sleep(1000); // 1-sec gap per requirement

    // Check if generation started or turn appeared
    if (isBusy() || hasNewTurnAppeared(initialBotTurns, initialResponse)) {
      return true;
    }

    // If editor text was cleared by Copilot after send click, Copilot accepted the prompt!
    if (sendAttempted && getEditorText(editor).length === 0) {
      for (let w = 0; w < 6; w++) {
        await sleep(500);
        if (isBusy() || hasNewTurnAppeared(initialBotTurns, initialResponse)) {
          return true;
        }
      }
    }
  }

  // Final check: did busy state trigger or did a response already generate?
  if (isBusy() || hasNewTurnAppeared(initialBotTurns, initialResponse)) {
    return true;
  }

  // Pre-throw fallback: If latest response is non-empty and changed from initial, it was submitted!
  const finalResp = readLatestResponse();
  if (finalResp && finalResp.trim().length > 0 && finalResp !== initialResponse) {
    console.log("[Copilot Extension] Verified: New response is already present in DOM. Treating submission as successful.");
    return true;
  }

  return false;
}

async function waitUntilStopButtonInvisible() {
  const deadline = Date.now() + 120000;
  await sleep(1000);

  while (Date.now() < deadline) {
    if (!isBusy()) {
      await sleep(1000);
      if (!isBusy()) {
        return;
      }
    }
    await sleep(500);
  }
}

function isBusy() {
  const busySelectors = [
    "button[aria-label*='Stop generating']",
    "button[aria-label*='Stop generating response']",
    "button[aria-label*='Stop']",
    "[data-testid='stop-generating-button']",
    "[aria-busy='true']",
    "button[title*='Stop']"
  ];
  return busySelectors.some(sel => {
    const el = document.querySelector(sel);
    if (!el || !isElementVisible(el)) return false;
    const label = (el.getAttribute('aria-label') || el.getAttribute('title') || el.innerText || '').toLowerCase();
    if (label.includes('stopped generating')) {
      return false;
    }
    return true;
  });
}

function readLatestResponse() {
  const messageSelectors = [
    "div[data-content='ai-message']",
    "[data-content='ai-message']",
    "[data-testid='response-message']",
    "[data-message-author-role='assistant']",
    ".bot-turn",
    "[data-content='assistant']",
    "[role='article']"
  ];

  for (const sel of messageSelectors) {
    const els = document.querySelectorAll(sel);
    if (els && els.length > 0) {
      const last = els[els.length - 1];
      const rawText = (last.innerText || "").trim();
      if (rawText) {
        return cleanResponseText(rawText);
      }
    }
  }

  // Strictly return empty string if no assistant turn element was found.
  // Never fall back to bodyText scanning to avoid scraping the editor or unsubmitted prompts!
  return "";
}

function cleanResponseText(text) {
  if (!text) return "";
  let cleaned = text.replace(/^(?:Copilot|Microsoft Copilot)\s+(?:said|says)?:?\s*/i, "").trim();
  const match = cleaned.match(/(\{[\s\S]*?"type"\s*:\s*"(?:EXECUTE|FINAL_ANSWER)"[\s\S]*?\})/);
  if (match) {
    return match[1].trim();
  }
  return cleaned;
}

function extractConversationIdFromUrl(url) {
  if (!url) return null;
  const match = url.match(/\/conversation\/([a-zA-Z0-9_-]+)/);
  return match ? match[1] : null;
}

function isElementVisible(el) {
  return !!(el.offsetWidth || el.offsetHeight || el.getClientRects().length);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function handleDeleteTurn(req) {
  const convId = req.conversationId || req.conversation_id || null;
  if (!convId || convId === "null") {
    throw new Error("conversationId is mandatory for delete operation.");
  }

  console.log(`[Copilot Extension] Executing handleDeleteTurn for convId '${convId}'...`);

  // Check invalid / already deleted conversation popup first
  if (checkInvalidConversation()) {
    throw new Error("The conversation no longer exists.");
  }

  // Step A: Find the highlighted/selected chat tile or link matching conversationId in left sidebar
  let tileElement = null;
  const selectors = [
    `a[href*='${convId}']`,
    `div:has(> a[href*='${convId}'])`,
    `li:has(a[href*='${convId}'])`,
    `[role='listitem']:has(a[href*='${convId}'])`,
    `div[aria-selected='true']`,
    `div[aria-current='page']`
  ];

  for (let i = 0; i < 15; i++) {
    autoCheckVerificationBox();
    if (checkInvalidConversation()) {
      throw new Error("The conversation no longer exists.");
    }
    for (const sel of selectors) {
      try {
        const el = document.querySelector(sel);
        if (el && isElementVisible(el)) {
          tileElement = el.closest('div, li, [role="listitem"]') || el;
          break;
        }
      } catch (e) {}
    }
    if (tileElement) break;
    await sleep(300);
  }

  if (!tileElement) {
    const allLinks = Array.from(document.querySelectorAll('a'));
    for (const a of allLinks) {
      if (a.href && a.href.includes(convId)) {
        tileElement = a.closest('div, li, [role="listitem"]') || a;
        break;
      }
    }
  }

  if (checkInvalidConversation()) {
    throw new Error("The conversation no longer exists.");
  }

  if (tileElement) {
    try {
      tileElement.focus && tileElement.focus();
      tileElement.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
      tileElement.dispatchEvent(new MouseEvent('mouseenter', { bubbles: true }));
    } catch(e) {}
    await sleep(200);
  }

  // Step B: Click three-dot menu (...) on tile
  let optionsBtn = null;
  const optionsSelectors = [
    "button[aria-label*='options' i]",
    "button[aria-label*='more' i]",
    "button[title*='More' i]",
    "button[title*='Options' i]",
    "button[aria-haspopup='menu']",
    "button:has(svg)"
  ];

  const searchRoot = tileElement || document;
  for (const sel of optionsSelectors) {
    try {
      const btn = searchRoot.querySelector(sel);
      if (btn && isElementVisible(btn)) {
        optionsBtn = btn;
        break;
      }
    } catch (e) {}
  }

  if (!optionsBtn) {
    const allBtns = Array.from(document.querySelectorAll('button'));
    for (const btn of allBtns) {
      if (!isElementVisible(btn)) continue;
      const label = (btn.getAttribute('aria-label') || btn.getAttribute('title') || '').toLowerCase();
      if (label.includes('options') || label.includes('more') || btn.getAttribute('aria-haspopup') === 'menu') {
        optionsBtn = btn;
        break;
      }
    }
  }

  if (!optionsBtn) {
    if (checkInvalidConversation()) {
      throw new Error("The conversation no longer exists.");
    }
    throw new Error("Could not locate three-dot options (...) button for conversation.");
  }

  console.log("[Copilot Extension] Clicking three-dot options (...) button:", optionsBtn);
  triggerFullClick(optionsBtn);
  await sleep(400);

  // Step C: Click 'Delete' item in dropdown menu (with re-click retries & portal/shadow DOM handling)
  let deleteMenuItem = null;
  for (let i = 0; i < 20; i++) {
    if (checkInvalidConversation()) {
      throw new Error("The conversation no longer exists.");
    }

    // Re-click optionsBtn every 5 attempts (~1.2s) if menu hasn't appeared yet
    if (i > 0 && i % 5 === 0 && optionsBtn) {
      console.log(`[Copilot Extension] Retrying click on three-dot options button (Attempt ${i})...`);
      triggerFullClick(optionsBtn);
      await sleep(300);
    }

    const itemSelectors = [
      "[role='menuitem']",
      "fluent-menu-item",
      "div[role='menu'] *",
      "ul[role='menu'] *",
      "button",
      "div",
      "span"
    ];

    for (const sel of itemSelectors) {
      try {
        const items = Array.from(document.querySelectorAll(sel));
        for (const item of items) {
          if (!isElementVisible(item)) continue;
          const txt = (item.innerText || item.textContent || '').trim().toLowerCase();
          if (txt === 'delete' || txt.startsWith('delete')) {
            deleteMenuItem = item;
            break;
          }
        }
      } catch (e) {}
      if (deleteMenuItem) break;
    }

    if (deleteMenuItem) break;
    await sleep(250);
  }

  if (!deleteMenuItem) {
    if (checkInvalidConversation()) {
      throw new Error("The conversation no longer exists.");
    }
    throw new Error("Could not locate 'Delete' item in context dropdown menu.");
  }

  console.log("[Copilot Extension] Clicking 'Delete' item in dropdown menu:", deleteMenuItem);
  triggerFullClick(deleteMenuItem);
  await sleep(500);

  // Step D: Detect 'Delete chat?' confirmation popup and click white 'Delete' button
  let modalConfirmBtn = null;
  for (let i = 0; i < 20; i++) {
    autoCheckVerificationBox();
    if (checkInvalidConversation()) {
      throw new Error("The conversation no longer exists.");
    }

    // Re-click deleteMenuItem every 6 attempts (~1.8s) if modal dialog hasn't appeared
    if (i > 0 && i % 6 === 0 && deleteMenuItem) {
      console.log(`[Copilot Extension] Retrying click on 'Delete' menu item (Attempt ${i})...`);
      triggerFullClick(deleteMenuItem);
      await sleep(300);
    }

    const dialogs = document.querySelectorAll("div[role='dialog'], dialog, [role='dialog'], .fluent-dialog");
    for (const d of dialogs) {
      if (!isElementVisible(d)) continue;
      const dText = (d.innerText || d.textContent || '').toLowerCase();
      if (dText.includes('delete chat') || dText.includes('permanently delete') || dText.includes('delete')) {
        const btns = d.querySelectorAll("button, div[role='button']");
        for (const btn of btns) {
          if (!isElementVisible(btn)) continue;
          const btnTxt = (btn.innerText || btn.textContent || '').trim().toLowerCase();
          if (btnTxt === 'delete') {
            modalConfirmBtn = btn;
            break;
          }
        }
      }
      if (modalConfirmBtn) break;
    }

    if (modalConfirmBtn) break;
    await sleep(300);
  }

  if (!modalConfirmBtn) {
    const allBtns = Array.from(document.querySelectorAll("button"));
    for (const btn of allBtns) {
      if (!isElementVisible(btn)) continue;
      const txt = (btn.innerText || btn.textContent || '').trim().toLowerCase();
      if (txt === 'delete') {
        modalConfirmBtn = btn;
        break;
      }
    }
  }

  if (!modalConfirmBtn) {
    throw new Error("Could not locate 'Delete' confirmation button in modal dialog.");
  }

  console.log("[Copilot Extension] Clicking white 'Delete' confirmation button in modal dialog:", modalConfirmBtn);
  triggerFullClick(modalConfirmBtn);

  // Step E: Wait for popup to close & Step F: Verify current URL is redirected to .../chat
  console.log("[Copilot Extension] Waiting for deletion popup to close and URL redirect to https://copilot.com/chat...");
  const deadline = Date.now() + 12000;
  let redirected = false;

  while (Date.now() < deadline) {
    await sleep(300);
    const currUrl = window.location.href;
    if (currUrl === "https://copilot.com/chat" || currUrl === "https://copilot.com/chat/" || !currUrl.includes(convId)) {
      redirected = true;
      break;
    }
  }

  if (!redirected) {
    console.warn("[Copilot Extension] Deletion finished, URL verification timeout reached.");
  }

  console.log("[Copilot Extension] Delete turn completed successfully for convId:", convId);

  return {
    status: EventConstants.STATUS_RESPONSE,
    taskId: req.taskId || null,
    conversationId: convId,
    message: "The conversation deleted",
    error: null
  };
}
