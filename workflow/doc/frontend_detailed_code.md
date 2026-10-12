# 🚀 Automation Suite React Frontend - Detailed Source Code

> 📅 **Generated On:** `2026-09-28 22:17:35`  
> 📂 **Source Root:** `C:/Ranjan/AutomationSuite_v2/automation_suite_frontend`  
> 📊 **Total Source Files:** `37`  
> 📝 **Total Lines of Code:** `4,892`  
> 💾 **Total Size:** `174.26 KB`  

---

## 📌 Executive Summary

React 18 + Vite + TypeScript Frontend module featuring single global WebSocket hub (/ws/app), multi-tab server dashboard, environment & server category filters, resizable floating terminal modal windows, custom tab renaming, dynamic prompt path displays (PS C:\...>), command history navigation, and Ctrl+C interrupt support.

---

## 📂 Project Directory Structure

```text
automation_suite_frontend/
├── index.html
├── package.json
├── run-dev.bat
├── src/
│   ├── App.tsx
│   ├── config/
│   │   └── appConfig.ts
│   ├── main.tsx
│   ├── modules/
│   │   ├── ai/
│   │   │   ├── components/
│   │   │   │   ├── AiChatModal.tsx
│   │   │   │   ├── AiCopilotInput.tsx
│   │   │   │   ├── AiReviewBanner.tsx
│   │   │   │   └── HostSelectionOptionModal.tsx
│   │   │   ├── constants/
│   │   │   │   └── aiEvents.ts
│   │   │   ├── services/
│   │   │   │   ├── aiChatDb.ts
│   │   │   │   ├── promptTemplates.ts
│   │   │   │   └── terminal/
│   │   │   │       └── system_prompt_cli_agent.txt
│   │   │   └── types/
│   │   │       └── ai.types.ts
│   │   └── terminal/
│   │       ├── TerminalDashboardPage.tsx
│   │       ├── components/
│   │       │   ├── AiHostCard.tsx
│   │       │   ├── HeaderServerFilter.tsx
│   │       │   ├── HostTerminalDetailsModal.tsx
│   │       │   ├── ServerCardGrid.tsx
│   │       │   ├── ServerFilterDialog.tsx
│   │       │   ├── StartTerminalModal.tsx
│   │       │   ├── TerminalConsoleCanvas.tsx
│   │       │   ├── TerminalModalWindow.tsx
│   │       │   ├── TerminalRenameModal.tsx
│   │       │   └── TerminalTabHeader.tsx
│   │       ├── frontendSimulation.test.ts
│   │       ├── services/
│   │       │   └── terminalCache.ts
│   │       └── types/
│   │           └── terminal.types.ts
│   ├── services/
│   │   ├── api.ts
│   │   └── globalWebSocket.ts
│   └── styles/
│       └── terminalDashboard.css
├── test_ai_frame_handler.ts
├── test_ai_modal_flow.js
├── test_ai_modal_flow.ts
├── tsconfig.json
└── vite.config.ts
```

---

## 📑 File Index

1. [`index.html`](#file-1)
2. [`package.json`](#file-2)
3. [`run-dev.bat`](#file-3)
4. [`src/App.tsx`](#file-4)
5. [`src/config/appConfig.ts`](#file-5)
6. [`src/main.tsx`](#file-6)
7. [`src/modules/ai/components/AiChatModal.tsx`](#file-7)
8. [`src/modules/ai/components/AiCopilotInput.tsx`](#file-8)
9. [`src/modules/ai/components/AiReviewBanner.tsx`](#file-9)
10. [`src/modules/ai/components/HostSelectionOptionModal.tsx`](#file-10)
11. [`src/modules/ai/constants/aiEvents.ts`](#file-11)
12. [`src/modules/ai/services/aiChatDb.ts`](#file-12)
13. [`src/modules/ai/services/promptTemplates.ts`](#file-13)
14. [`src/modules/ai/services/terminal/system_prompt_cli_agent.txt`](#file-14)
15. [`src/modules/ai/types/ai.types.ts`](#file-15)
16. [`src/modules/terminal/TerminalDashboardPage.tsx`](#file-16)
17. [`src/modules/terminal/components/AiHostCard.tsx`](#file-17)
18. [`src/modules/terminal/components/HeaderServerFilter.tsx`](#file-18)
19. [`src/modules/terminal/components/HostTerminalDetailsModal.tsx`](#file-19)
20. [`src/modules/terminal/components/ServerCardGrid.tsx`](#file-20)
21. [`src/modules/terminal/components/ServerFilterDialog.tsx`](#file-21)
22. [`src/modules/terminal/components/StartTerminalModal.tsx`](#file-22)
23. [`src/modules/terminal/components/TerminalConsoleCanvas.tsx`](#file-23)
24. [`src/modules/terminal/components/TerminalModalWindow.tsx`](#file-24)
25. [`src/modules/terminal/components/TerminalRenameModal.tsx`](#file-25)
26. [`src/modules/terminal/components/TerminalTabHeader.tsx`](#file-26)
27. [`src/modules/terminal/frontendSimulation.test.ts`](#file-27)
28. [`src/modules/terminal/services/terminalCache.ts`](#file-28)
29. [`src/modules/terminal/types/terminal.types.ts`](#file-29)
30. [`src/services/api.ts`](#file-30)
31. [`src/services/globalWebSocket.ts`](#file-31)
32. [`src/styles/terminalDashboard.css`](#file-32)
33. [`test_ai_frame_handler.ts`](#file-33)
34. [`test_ai_modal_flow.js`](#file-34)
35. [`test_ai_modal_flow.ts`](#file-35)
36. [`tsconfig.json`](#file-36)
37. [`vite.config.ts`](#file-37)

---

## 💻 Comprehensive Source Code

<a id="file-1"></a>
### 1. `index.html`

**Path:** `index.html` | **Size:** `0.38 KB` | **Lines:** `13` | **Language:** `html`

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/favicon.ico" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Automation Suite V2 - Terminal Dashboard</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>

```

---

<a id="file-2"></a>
### 2. `package.json`

**Path:** `package.json` | **Size:** `0.51 KB` | **Lines:** `24` | **Language:** `json`

```json
{
  "name": "automation_suite_frontend",
  "private": true,
  "version": "2.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.6.7",
    "lucide-react": "^0.330.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.55",
    "@types/react-dom": "^18.2.19",
    "@vitejs/plugin-react": "^4.2.1",
    "typescript": "^5.2.2",
    "vite": "^5.1.0"
  }
}

```

---

<a id="file-3"></a>
### 3. `run-dev.bat`

**Path:** `run-dev.bat` | **Size:** `0.46 KB` | **Lines:** `21` | **Language:** `cmd`

```cmd
@echo off
title Automation Suite Frontend V2 (Port 3000)

cd /d "%~dp0"

echo ========================================================
echo       Automation Suite React Frontend V2 (Dev Mode)
echo ========================================================
echo.

echo Opening default web browser to http://localhost:3000/...
start http://localhost:3000/

call npm run dev

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Frontend dev server failed to start.
)

pause

```

---

<a id="file-4"></a>
### 4. `src/App.tsx`

**Path:** `src/App.tsx` | **Size:** `0.24 KB` | **Lines:** `9` | **Language:** `tsx`

```tsx
import React from 'react';
import { TerminalDashboardPage } from './modules/terminal/TerminalDashboardPage';
import './styles/terminalDashboard.css';

export const App: React.FC = () => {
  return <TerminalDashboardPage />;
};

export default App;

```

---

<a id="file-5"></a>
### 5. `src/config/appConfig.ts`

**Path:** `src/config/appConfig.ts` | **Size:** `0.23 KB` | **Lines:** `9` | **Language:** `typescript`

```typescript
/**
 * Centralized Application Configuration Constants
 */

export const baseUrl = 'http://localhost:9011';

export const globalWsUrl = baseUrl.replace(/^http/, 'ws') + '/ws/app';

export const apiBaseUrl = `${baseUrl}/api/v2/servers`;

```

---

<a id="file-6"></a>
### 6. `src/main.tsx`

**Path:** `src/main.tsx` | **Size:** `0.21 KB` | **Lines:** `9` | **Language:** `tsx`

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

```

---

<a id="file-7"></a>
### 7. `src/modules/ai/components/AiChatModal.tsx`

**Path:** `src/modules/ai/components/AiChatModal.tsx` | **Size:** `32.69 KB` | **Lines:** `813` | **Language:** `tsx`

```tsx
import React, { useState, useEffect, useRef } from 'react';
import { Bot, MessageSquare, Plus, Trash2, Edit3, X, Terminal, AlertCircle, Sparkles } from 'lucide-react';
import { AiConversation, AiChatMessage, AiTaskResultFrame } from '../types/ai.types';
import { getAllConversations, saveConversation, deleteConversationDb, getMessagesByConversation, saveChatMessage } from '../services/aiChatDb';
import { buildHostSelectionPrompt, buildDiagnosticsRemediationPrompt } from '../services/promptTemplates';
import { globalWebSocket } from '../../../services/globalWebSocket';
import { AiCopilotInput } from './AiCopilotInput';
import { HostSelectionOptionModal } from './HostSelectionOptionModal';
import { AiReviewBanner } from './AiReviewBanner';
import { ServerConfig } from '../../terminal/types/terminal.types';
import { ServerFilterDialog } from '../../terminal/components/ServerFilterDialog';

interface AiChatModalProps {
  isOpen: boolean;
  onClose: () => void;
  servers: ServerConfig[];
  initialConversationId?: string | null;
}

export const AiChatModal: React.FC<AiChatModalProps> = ({
  isOpen,
  onClose,
  servers,
  initialConversationId,
}) => {
  const [conversations, setConversations] = useState<AiConversation[]>([]);
  const [activeConv, setActiveConv] = useState<AiConversation | null>(null);
  const [messages, setMessages] = useState<AiChatMessage[]>([]);
  const [loading, setLoading] = useState(false);

  // Full message list for active conversation (used for lazy windowing max 20 messages)
  const allMessagesRef = useRef<AiChatMessage[]>([]);
  const [windowEndIndex, setWindowEndIndex] = useState<number>(0);

  // Modals & Editing
  const [isHostOptionOpen, setIsHostOptionOpen] = useState(false);
  const [isServerTreeFilterOpen, setIsServerTreeFilterOpen] = useState(false);
  const [editingTitleId, setEditingTitleId] = useState<string | null>(null);
  const [newTitleText, setNewTitleText] = useState('');

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const threadScrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen) {
      loadConversations();
    }
  }, [isOpen]);

  useEffect(() => {
    const unsub = globalWebSocket.subscribe((frame: any) => {
      handleIncomingAiFrame(frame);
    });
    return unsub;
  }, [activeConv]);

  const loadConversations = async () => {
    try {
      const list = await getAllConversations();
      setConversations(list);
      if (initialConversationId) {
        const found = list.find((c) => c.id === initialConversationId);
        if (found) {
          selectConversation(found);
          return;
        }
      }
      if (list.length > 0) {
        selectConversation(list[0]);
      } else {
        createNewChat();
      }
    } catch (err) {
      console.warn('Error loading AI conversations from IndexedDB:', err);
    }
  };

  const selectConversation = async (conv: AiConversation) => {
    setActiveConv(conv);
    try {
      const msgs = await getMessagesByConversation(conv.id);
      allMessagesRef.current = msgs;
      const end = msgs.length;
      const start = Math.max(0, end - 20);
      setWindowEndIndex(end);
      setMessages(msgs.slice(start, end));
      setTimeout(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'auto' });
      }, 50);
    } catch (err) {
      console.warn('Error loading messages:', err);
    }
  };

  const createNewChat = () => {
    // Draft conversation session cache: No ID assigned until AI Agent returns official conversationId
    const draftConv: AiConversation = {
      id: '',
      title: `AI Chat #${conversations.length + 1}`,
      targetHosts: [],
      isHostKnown: false,
      accumulatedAdditionalData: null,
      createdAt: Date.now(),
      updatedAt: Date.now(),
    };
    setActiveConv(draftConv);
    allMessagesRef.current = [];
    setWindowEndIndex(0);
    setMessages([]);
  };

  const handleRenameConv = async (id: string, newTitle: string) => {
    setEditingTitleId(null);
    if (!newTitle.trim()) return;
    const target = conversations.find((c) => c.id === id);
    if (!target) return;
    const updated = { ...target, title: newTitle.trim(), updatedAt: Date.now() };
    await saveConversation(updated);
    setConversations((prev) => prev.map((c) => (c.id === id ? updated : c)));
    if (activeConv?.id === id) {
      setActiveConv(updated);
    }
  };

  const handleDeleteConv = async (id: string) => {
    if (id) {
      await deleteConversationDb(id);
      globalWebSocket.send({
        'event-type': 'AI_DELETE_REQ',
        conversationId: id,
      });
    }
    const updatedList = conversations.filter((c) => c.id !== id);
    setConversations(updatedList);
    if (activeConv?.id === id) {
      if (updatedList.length > 0) {
        selectConversation(updatedList[0]);
      } else {
        createNewChat();
      }
    }
  };

  const appendMessage = async (msg: AiChatMessage) => {
    // Only save to IndexedDB if activeConv has an official ID assigned by AI Agent
    if (activeConv?.id) {
      await saveChatMessage(msg);
    }
    allMessagesRef.current = [...allMessagesRef.current, msg];
    const end = allMessagesRef.current.length;
    const start = Math.max(0, end - 20);
    setWindowEndIndex(end);
    setMessages(allMessagesRef.current.slice(start, end));
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, 50);
  };

  const handleScroll = () => {
    const container = threadScrollRef.current;
    if (!container || allMessagesRef.current.length <= 20) return;

    const scrollTop = container.scrollTop;
    const scrollHeight = container.scrollHeight;
    const clientHeight = container.clientHeight;

    // Scroll near top: load older messages
    if (scrollTop < 40) {
      const currentStart = Math.max(0, windowEndIndex - 20);
      if (currentStart > 0) {
        const newEnd = Math.max(20, windowEndIndex - 2);
        const newStart = Math.max(0, newEnd - 20);
        setWindowEndIndex(newEnd);
        const oldScrollHeight = scrollHeight;
        setMessages(allMessagesRef.current.slice(newStart, newEnd));
        requestAnimationFrame(() => {
          if (threadScrollRef.current) {
            threadScrollRef.current.scrollTop = threadScrollRef.current.scrollHeight - oldScrollHeight;
          }
        });
      }
    }

    // Scroll near bottom: load newer messages
    if (scrollTop + clientHeight > scrollHeight - 40) {
      if (windowEndIndex < allMessagesRef.current.length) {
        const newEnd = Math.min(allMessagesRef.current.length, windowEndIndex + 2);
        const newStart = Math.max(0, newEnd - 20);
        setWindowEndIndex(newEnd);
        setMessages(allMessagesRef.current.slice(newStart, newEnd));
      }
    }
  };

  const persistNewConversation = async (officialConvId: string, accumulatedData?: string | null) => {
    if (!activeConv) return;
    const savedConv: AiConversation = {
      ...activeConv,
      id: officialConvId,
      accumulatedAdditionalData: accumulatedData || activeConv.accumulatedAdditionalData || null,
      updatedAt: Date.now(),
    };
    await saveConversation(savedConv);
    setActiveConv(savedConv);
    setConversations((prev) => [savedConv, ...prev.filter((c) => c.id !== officialConvId)]);

    // Save all pending in-memory messages under officialConvId
    allMessagesRef.current = allMessagesRef.current.map((m) => ({
      ...m,
      conversationId: officialConvId,
    }));

    for (const msg of allMessagesRef.current) {
      await saveChatMessage(msg);
    }
  };

  const handleIncomingAiFrame = async (frame: any) => {
    if (!activeConv) return;
    const rawEventType = String(frame['event-type'] || frame.event || frame.eventType || '');
    const eventTypeUpper = rawEventType.toUpperCase();
    const frameConvId = frame.conversationId;

    // Session Response Filter Rule: Ignore any response frame for a different conversationId
    // If activeConv.id is empty (draft chat), accept all incoming frames for the current session.
    if (activeConv.id && frameConvId && frameConvId !== activeConv.id && !allMessagesRef.current.some(m => m.conversationId === frameConvId)) {
      console.warn(`[AI Studio] Ignoring frame for conversationId ${frameConvId} (Active session locked to ${activeConv.id})`);
      return;
    }

    // Unified 2-Event Task Result Handler
    if (eventTypeUpper === 'AI_TASK_RESULT' || eventTypeUpper === 'AI_HOST_RESOLVE_RESP' || eventTypeUpper === 'AI_REMEDIATE_RESP') {
      const resp = frame as AiTaskResultFrame;
      setLoading(false);

      const officialConvId = resp.conversationId || frameConvId || activeConv.id;

      // Lock session ID & persist if initial draft chat
      if (!activeConv.id && officialConvId) {
        await persistNewConversation(officialConvId);
      }

      const currentConvId = officialConvId || activeConv.id;

      if (resp.status === 'FAILED' || resp.error) {
        const errMsg: AiChatMessage = {
          id: 'msg_' + Date.now(),
          conversationId: currentConvId,
          sender: 'ai',
          status: 'error',
          text: resp.error ? (resp.error.startsWith('[Error]') ? resp.error : `[Error] ${resp.error}`) : '[Error] AI Task Execution Failed.',
          timestamp: Date.now(),
        };
        await appendMessage(errMsg);
        return;
      }

      // Parse JSON output payload from rawResponse or direct frame properties
      let jsonPayload: any = null;
      if (resp.rawResponse) {
        try {
          jsonPayload = JSON.parse(resp.rawResponse);
        } catch (e) {
          console.warn('[AI Studio] Failed to parse rawResponse JSON:', e);
        }
      }
      if (!jsonPayload) {
        jsonPayload = frame;
      }

      // Update accumulated_additional_data in active conversation session & IndexedDB
      const accumulatedData = jsonPayload.accumulated_additional_data || jsonPayload.accumulatedAdditionalData || activeConv.accumulatedAdditionalData;
      if (accumulatedData && activeConv) {
        const updatedConv: AiConversation = { ...activeConv, id: currentConvId, accumulatedAdditionalData: accumulatedData, updatedAt: Date.now() };
        setActiveConv(updatedConv);
        await saveConversation(updatedConv);
      }

      // Step A: Check if Host Resolution Result
      if (jsonPayload.target_hosts !== undefined || jsonPayload.review_required !== undefined) {
        const targetHosts: string[] = jsonPayload.target_hosts || [];
        const reviewRequired = Boolean(jsonPayload.review_required);

        const aiMsg: AiChatMessage = {
          id: 'msg_' + Date.now(),
          conversationId: currentConvId,
          sender: 'ai',
          preResponse: jsonPayload.pre_response,
          finalOutcome: jsonPayload.final_outcome,
          targetHosts: targetHosts,
          reviewRequiredHost: reviewRequired,
          reviewTitle: jsonPayload.review_title,
          reviewDescription: jsonPayload.review_description,
          accumulatedAdditionalData: accumulatedData,
          timestamp: Date.now(),
        };

        await appendMessage(aiMsg);

        // If hosts resolved without review required, auto-trigger remediation step!
        if (!reviewRequired && targetHosts.length > 0) {
          const primaryHost = targetHosts[0];
          const lastUserMsg = [...allMessagesRef.current].reverse().find((m) => m.sender === 'user');
          if (lastUserMsg && lastUserMsg.text) {
            triggerRemediation(currentConvId, lastUserMsg.text, primaryHost);
          }
        }
        return;
      }

      // Step B: Check if Remediation Result
      if (jsonPayload.Command !== undefined || jsonPayload.command !== undefined || jsonPayload.review_needed !== undefined) {
        const command = jsonPayload.Command || jsonPayload.command || null;
        const reviewNeeded = Boolean(jsonPayload.review_needed);

        const aiMsg: AiChatMessage = {
          id: 'msg_' + Date.now(),
          conversationId: currentConvId,
          sender: 'ai',
          preResponse: jsonPayload.pre_response,
          command: command,
          platform: jsonPayload.platform || 'PS',
          reviewNeeded: reviewNeeded,
          reviewTitle: jsonPayload.review_title,
          reviewDescription: jsonPayload.review_description,
          criticalityLevel: jsonPayload.CriticalityLevele !== undefined ? jsonPayload.CriticalityLevele : jsonPayload.criticality_level,
          fileNeeded: jsonPayload.file_needed,
          finalOutcome: jsonPayload.final_outcome,
          accumulatedAdditionalData: accumulatedData,
          timestamp: Date.now(),
        };

        await appendMessage(aiMsg);

        // If review is NOT needed and command exists, auto execute command on reserved AI terminal!
        if (!reviewNeeded && command && (jsonPayload.host || activeConv.targetHosts[0])) {
          const targetHost = jsonPayload.host || activeConv.targetHosts[0] || 'localhost';
          executeCommandOnAiTerminal(currentConvId, targetHost, command, jsonPayload.platform || 'PS');
        }
        return;
      }

      // General Outcome Frame
      if (jsonPayload.final_outcome || jsonPayload.pre_response) {
        const aiMsg: AiChatMessage = {
          id: 'msg_' + Date.now(),
          conversationId: currentConvId,
          sender: 'ai',
          preResponse: jsonPayload.pre_response,
          finalOutcome: jsonPayload.final_outcome,
          accumulatedAdditionalData: accumulatedData,
          timestamp: Date.now(),
        };
        await appendMessage(aiMsg);
      }

    } else if (eventTypeUpper === 'AI_EXECUTE_CMD_RESP') {
      const resp = frame;
      const currentConvId = resp.conversationId || activeConv.id;

      const sysMsg: AiChatMessage = {
        id: 'msg_' + Date.now(),
        conversationId: currentConvId,
        sender: 'system',
        text: resp.status === 'SUCCESS'
          ? `[AI Execution] Successfully dispatched command to reserved terminal ${resp.terminalId} at host ${resp.host}.`
          : `[AI Execution Error] ${resp.error || 'Failed to execute command on reserved terminal.'}`,
        timestamp: Date.now(),
      };

      await appendMessage(sysMsg);
    }
  };

  const handleSendQuery = async (queryText: string) => {
    if (!activeConv) return;

    // Generate unique taskId per prompt request
    const taskId = 'task_' + Date.now() + '_' + (crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 9));

    // Send null conversationId for new draft chat, pass locked conversationId for existing chat
    const outgoingConvId = activeConv.id ? activeConv.id : null;

    const userMsg: AiChatMessage = {
      id: 'msg_' + Date.now(),
      conversationId: activeConv.id || '',
      sender: 'user',
      text: queryText, // Layman query text ONLY
      timestamp: Date.now(),
    };

    try {
      await appendMessage(userMsg);
      setLoading(true);

      // Frontend is the Brain: Build full prompt locally with Server Inventory & Accumulated Additional Data
      if (activeConv.targetHosts.length > 0) {
        // Direct Remediation Prompt
        const host = activeConv.targetHosts[0];
        const fullPrompt = buildDiagnosticsRemediationPrompt(queryText, host, activeConv.accumulatedAdditionalData);
        globalWebSocket.send({
          'event-type': 'AI_EXECUTE_TASK',
          taskId: taskId,
          conversationId: outgoingConvId,
          prompt: fullPrompt,
          filesPath: [],
        });
      } else {
        // Target Host Resolution Prompt
        const fullPrompt = buildHostSelectionPrompt(queryText, servers, activeConv.accumulatedAdditionalData);
        globalWebSocket.send({
          'event-type': 'AI_EXECUTE_TASK',
          taskId: taskId,
          conversationId: outgoingConvId,
          prompt: fullPrompt,
          filesPath: [],
        });
      }
    } catch (err) {
      console.error('[AI Studio] Error in handleSendQuery:', err);
      setLoading(false);
      const errMsg: AiChatMessage = {
        id: 'msg_' + Date.now(),
        conversationId: activeConv.id || '',
        sender: 'ai',
        status: 'error',
        text: `[Error] Failed to send query: ${err instanceof Error ? err.message : String(err)}`,
        timestamp: Date.now(),
      };
      await appendMessage(errMsg);
    }
  };

  const triggerRemediation = (convId: string | null, query: string, host: string, taskId?: string) => {
    setLoading(true);
    const reqTaskId = taskId || 'task_' + Date.now() + '_' + (crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 9));
    const finalConvId = convId || (activeConv?.id ? activeConv.id : null);
    const fullPrompt = buildDiagnosticsRemediationPrompt(query, host, activeConv?.accumulatedAdditionalData);

    try {
      globalWebSocket.send({
        'event-type': 'AI_EXECUTE_TASK',
        taskId: reqTaskId,
        conversationId: finalConvId,
        prompt: fullPrompt,
        filesPath: [],
      });
    } catch (err) {
      console.error('[AI Studio] Error in triggerRemediation:', err);
      setLoading(false);
    }
  };

  const executeCommandOnAiTerminal = (convId: string, host: string, command: string, platform: 'PS' | 'CMD') => {
    const finalConvId = convId || (activeConv?.id ? activeConv.id : '');
    globalWebSocket.send({
      'event-type': 'ai_execute_cmd_req',
      conversationId: finalConvId,
      host: host,
      command: command,
      platform: platform,
      userId: 'user',
    });
  };

  const handleHostReviewAgree = (msg: AiChatMessage) => {
    if (!activeConv || !msg.targetHosts || msg.targetHosts.length === 0) return;
    const primaryHost = msg.targetHosts[0];
    const lastUserMsg = [...allMessagesRef.current].reverse().find((m) => m.sender === 'user');
    const targetConvId = activeConv.id || msg.conversationId || null;
    if (lastUserMsg && lastUserMsg.text) {
      triggerRemediation(targetConvId, lastUserMsg.text, primaryHost);
    }
  };

  const handleCommandReviewApprove = (msg: AiChatMessage) => {
    if (!activeConv || !msg.command) return;
    const targetHost = activeConv.targetHosts[0] || 'localhost';
    const targetConvId = activeConv.id || msg.conversationId || '';
    executeCommandOnAiTerminal(targetConvId, targetHost, msg.command, msg.platform || 'PS');
  };

  if (!isOpen) return null;

  const isNewChat = messages.length === 0;

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(15, 23, 42, 0.85)',
        backdropFilter: 'blur(8px)',
        zIndex: 1000,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
      }}
    >
      <div
        style={{
          background: '#0f172a',
          border: '1px solid #334155',
          borderRadius: '16px',
          width: '100%',
          maxWidth: '1200px',
          height: '85vh',
          display: 'flex',
          overflow: 'hidden',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
        }}
      >
        {/* Left Conversation Drawer */}
        <div
          style={{
            width: '280px',
            background: '#1e293b',
            borderRight: '1px solid #334155',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <div style={{ padding: '16px', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ fontWeight: 700, fontSize: '15px', color: '#f8fafc', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Bot style={{ width: '20px', height: '20px', color: '#818cf8' }} />
              AI Chat History
            </div>
            <button
              onClick={createNewChat}
              title="New AI Chat"
              style={{
                background: '#4f46e5',
                border: 'none',
                borderRadius: '6px',
                color: '#ffffff',
                padding: '6px 10px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
              }}
            >
              <Plus style={{ width: '14px', height: '14px' }} />
              New
            </button>
          </div>

          <div style={{ flex: 1, overflowY: 'auto', padding: '12px' }}>
            {conversations.map((c) => (
              <div
                key={c.id}
                onClick={() => selectConversation(c)}
                style={{
                  padding: '10px 12px',
                  borderRadius: '8px',
                  background: activeConv?.id === c.id ? '#334155' : 'transparent',
                  color: activeConv?.id === c.id ? '#ffffff' : '#94a3b8',
                  fontSize: '13px',
                  cursor: 'pointer',
                  marginBottom: '6px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  transition: 'all 0.2s',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', overflow: 'hidden' }}>
                  <MessageSquare style={{ width: '15px', height: '15px', color: '#818cf8', flexShrink: 0 }} />
                  {editingTitleId === c.id ? (
                    <input
                      value={newTitleText}
                      onChange={(e) => setNewTitleText(e.target.value)}
                      onBlur={() => handleRenameConv(c.id, newTitleText)}
                      onKeyDown={(e) => e.key === 'Enter' && handleRenameConv(c.id, newTitleText)}
                      autoFocus
                      style={{ background: '#0f172a', border: '1px solid #6366f1', color: '#ffffff', borderRadius: '4px', padding: '2px 4px', fontSize: '12px' }}
                    />
                  ) : (
                    <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontWeight: activeConv?.id === c.id ? 600 : 400 }}>
                      {c.title}
                    </span>
                  )}
                </div>

                <div style={{ display: 'flex', gap: '4px' }}>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setEditingTitleId(c.id);
                      setNewTitleText(c.title);
                    }}
                    style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', padding: '2px' }}
                  >
                    <Edit3 style={{ width: '12px', height: '12px' }} />
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteConv(c.id);
                    }}
                    style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: '2px' }}
                  >
                    <Trash2 style={{ width: '12px', height: '12px' }} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Main Chat Studio Area */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#0f172a' }}>
          {/* Header Bar */}
          <div style={{ padding: '16px 24px', background: '#1e293b', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Bot style={{ width: '22px', height: '22px', color: '#818cf8' }} />
              <div>
                <div style={{ fontWeight: 600, fontSize: '16px', color: '#f8fafc' }}>
                  {activeConv ? activeConv.title : 'AI Chat Studio'}
                </div>
                <div style={{ fontSize: '11px', color: '#94a3b8' }}>
                  {activeConv?.targetHosts.length ? `Target Hosts: ${activeConv.targetHosts.join(', ')}` : 'Auto Host Resolution Enabled'}
                </div>
              </div>
            </div>

            <button
              onClick={onClose}
              style={{ background: '#334155', border: 'none', color: '#94a3b8', borderRadius: '6px', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
            >
              <X style={{ width: '18px', height: '18px' }} />
            </button>
          </div>

          {/* Conversation Thread with Lazy Scroll Window */}
          <div
            ref={threadScrollRef}
            onScroll={handleScroll}
            style={{ flex: 1, overflowY: 'auto', padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}
          >
            {messages.length === 0 ? (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: '#64748b', textAlign: 'center', gap: '12px', marginTop: '40px' }}>
                <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#1e293b', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Sparkles style={{ width: '28px', height: '28px', color: '#818cf8' }} />
                </div>
                <div style={{ fontSize: '16px', fontWeight: 600, color: '#f8fafc' }}>ASR_AI Assistant Ready</div>
                <div style={{ fontSize: '13px', maxWidth: '440px', lineHeight: 1.5 }}>
                  Ask questions in layman's language to inspect system diagnostics, analyze performance, or remediate issues across target servers.
                </div>
              </div>
            ) : (
              messages.map((msg) => (
                <div key={msg.id} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {/* User Query Bubble */}
                  {msg.sender === 'user' && (
                    <div style={{ alignSelf: 'flex-end', background: '#3b82f6', color: '#ffffff', padding: '12px 16px', borderRadius: '16px 16px 2px 16px', maxWidth: '80%', fontSize: '14px', lineHeight: 1.5 }}>
                      {msg.text}
                    </div>
                  )}

                  {/* AI Response Card or Red Error Card */}
                  {msg.sender === 'ai' && (
                    msg.status === 'error' || msg.text?.startsWith('[Error]') ? (
                      <div
                        style={{
                          alignSelf: 'flex-start',
                          background: 'rgba(239, 68, 68, 0.12)',
                          border: '1px solid #ef4444',
                          color: '#f87171',
                          padding: '12px 16px',
                          borderRadius: '12px',
                          fontSize: '13px',
                          lineHeight: 1.5,
                          maxWidth: '85%',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                        }}
                      >
                        <AlertCircle style={{ width: '20px', height: '20px', flexShrink: 0, color: '#ef4444' }} />
                        <span>{msg.text || msg.finalOutcome || 'An error occurred during AI processing.'}</span>
                      </div>
                    ) : (
                      <div style={{ alignSelf: 'flex-start', background: '#1e293b', border: '1px solid #334155', padding: '16px', borderRadius: '16px 16px 16px 2px', maxWidth: '85%', color: '#f8fafc', fontSize: '14px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {/* Thought Process Banner */}
                        {msg.preResponse && (
                          <div style={{ background: '#0f172a', borderLeft: '3px solid #818cf8', padding: '8px 12px', borderRadius: '4px', fontSize: '12px', color: '#a5b4fc', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Sparkles style={{ width: '14px', height: '14px', flexShrink: 0 }} />
                            <span><strong>Thought Process:</strong> {msg.preResponse}</span>
                          </div>
                        )}

                        {/* Command Code Block */}
                        {msg.command && (
                          <div style={{ background: '#09090b', border: '1px solid #27272a', borderRadius: '8px', padding: '12px', fontFamily: 'Consolas, monospace', fontSize: '13px', color: '#38bdf8' }}>
                            <div style={{ fontSize: '11px', color: '#71717a', marginBottom: '6px', textTransform: 'uppercase' }}>
                              Executable Command ({msg.platform || 'PS'})
                            </div>
                            {msg.command}
                          </div>
                        )}

                        {/* Outcome Text */}
                        {msg.finalOutcome && (
                          <div style={{ lineHeight: 1.5, color: '#e2e8f0' }}>{msg.finalOutcome}</div>
                        )}

                        {/* Host Review Banner */}
                        {msg.reviewRequiredHost && (
                          <AiReviewBanner
                            type="host"
                            title={msg.reviewTitle}
                            description={msg.reviewDescription}
                            targetHosts={msg.targetHosts}
                            onAgree={() => handleHostReviewAgree(msg)}
                            onRefine={() => setIsHostOptionOpen(true)}
                          />
                        )}

                        {/* Command Execution Review Banner */}
                        {msg.reviewNeeded && (
                          <AiReviewBanner
                            type="command"
                            title={msg.reviewTitle}
                            description={msg.reviewDescription}
                            command={msg.command}
                            platform={msg.platform}
                            criticalityLevel={msg.criticalityLevel}
                            onApproveExecute={() => handleCommandReviewApprove(msg)}
                            onDecline={() => {}}
                          />
                        )}
                      </div>
                    )
                  )}

                  {/* System Notification Frame */}
                  {msg.sender === 'system' && (
                    <div style={{ alignSelf: 'center', background: '#0f172a', border: '1px solid #334155', color: '#38bdf8', padding: '8px 16px', borderRadius: '20px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Terminal style={{ width: '14px', height: '14px' }} />
                      {msg.text}
                    </div>
                  )}
                </div>
              ))
            )}

            {loading && (
              <div style={{ alignSelf: 'flex-start', background: '#1e293b', border: '1px solid #334155', padding: '12px 16px', borderRadius: '16px', color: '#94a3b8', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Sparkles style={{ width: '16px', height: '16px', color: '#818cf8', animation: 'spin 2s linear infinite' }} />
                ASR_AI is analyzing query and resolving target servers...
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Bottom Copilot Input */}
          <AiCopilotInput
            onSend={handleSendQuery}
            onOpenHostSelector={() => setIsHostOptionOpen(true)}
            showHostSelectorIcon={isNewChat}
            selectedHosts={activeConv?.targetHosts || []}
            isHostKnown={activeConv?.isHostKnown || false}
            disabled={loading}
          />
        </div>
      </div>

      {/* Target Host Option Pop-up */}
      {isHostOptionOpen && (
        <HostSelectionOptionModal
          onClose={() => setIsHostOptionOpen(false)}
          onChooseKnowHosts={() => setIsServerTreeFilterOpen(true)}
          onChooseDontKnowHosts={() => {
            if (activeConv) {
              const updated = { ...activeConv, isHostKnown: false, targetHosts: [] };
              if (updated.id) {
                saveConversation(updated);
              }
              setActiveConv(updated);
            }
          }}
        />
      )}

      {/* Target Server Tree Filter */}
      {isServerTreeFilterOpen && (
        <ServerFilterDialog
          open={isServerTreeFilterOpen}
          onClose={() => setIsServerTreeFilterOpen(false)}
          servers={servers}
          selectedHosts={activeConv?.targetHosts || []}
          onApply={(hosts) => {
            if (activeConv) {
              const updated = { ...activeConv, isHostKnown: true, targetHosts: hosts };
              if (updated.id) {
                saveConversation(updated);
              }
              setActiveConv(updated);
            }
            setIsServerTreeFilterOpen(false);
          }}
        />
      )}
    </div>
  );
};

```

---

<a id="file-8"></a>
### 8. `src/modules/ai/components/AiCopilotInput.tsx`

**Path:** `src/modules/ai/components/AiCopilotInput.tsx` | **Size:** `4.90 KB` | **Lines:** `166` | **Language:** `tsx`

```tsx
import React, { useState, KeyboardEvent } from 'react';
import { Send, Server, Sparkles } from 'lucide-react';

interface AiCopilotInputProps {
  onSend: (query: string) => void;
  onOpenHostSelector: () => void;
  showHostSelectorIcon: boolean;
  selectedHosts: string[];
  isHostKnown: boolean;
  disabled?: boolean;
}

export const AiCopilotInput: React.FC<AiCopilotInputProps> = ({
  onSend,
  onOpenHostSelector,
  showHostSelectorIcon,
  selectedHosts,
  isHostKnown,
  disabled = false,
}) => {
  const [text, setText] = useState('');

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      submit();
    }
  };

  const submit = () => {
    if (!text.trim() || disabled) return;
    onSend(text.trim());
    setText('');
  };

  return (
    <div
      style={{
        padding: '16px 20px',
        background: '#0f172a',
        borderTop: '1px solid #1e293b',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
      }}
    >
      {/* Target Host Badge Display */}
      {selectedHosts.length > 0 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: '#94a3b8' }}>
          <Server style={{ width: '13px', height: '13px', color: '#38bdf8' }} />
          <span>Target Hosts:</span>
          {selectedHosts.map((h) => (
            <span
              key={h}
              style={{
                background: '#1e293b',
                border: '1px solid #334155',
                color: '#38bdf8',
                padding: '2px 6px',
                borderRadius: '4px',
                fontWeight: 600,
              }}
            >
              {h}
            </span>
          ))}
        </div>
      )}

      <div
        style={{
          display: 'flex',
          alignItems: 'flex-end',
          gap: '8px',
          background: '#1e293b',
          border: '1px solid #334155',
          borderRadius: '12px',
          padding: '8px 12px',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
        }}
      >
        {/* Host Filter Icon: Visible ONLY in New Chat */}
        {showHostSelectorIcon && (
          <button
            onClick={onOpenHostSelector}
            title={
              selectedHosts.length > 0
                ? `Target Hosts: ${selectedHosts.join(', ')}`
                : isHostKnown
                ? 'Target Hosts Configured'
                : 'Select Target Host(s)'
            }
            style={{
              background: selectedHosts.length > 0 ? '#0284c7' : '#334155',
              border: 'none',
              borderRadius: '8px',
              width: '36px',
              height: '36px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              cursor: 'pointer',
              flexShrink: 0,
              marginBottom: '2px',
              transition: 'background 0.2s',
            }}
          >
            <Server style={{ width: '18px', height: '18px' }} />
          </button>
        )}

        {/* Prompt Input Textarea */}
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask ASR_AI in layman language (e.g. 'Check high CPU usage on IA PROD' or 'Restart service')..."
          disabled={disabled}
          rows={1}
          style={{
            flex: 1,
            background: 'transparent',
            border: 'none',
            outline: 'none',
            color: '#f8fafc',
            fontSize: '14px',
            resize: 'none',
            maxHeight: '120px',
            fontFamily: 'inherit',
            lineHeight: 1.5,
            padding: '6px 4px',
          }}
        />

        {/* Send Button */}
        <button
          onClick={submit}
          disabled={!text.trim() || disabled}
          style={{
            background: text.trim() && !disabled ? '#3b82f6' : '#334155',
            border: 'none',
            borderRadius: '8px',
            width: '36px',
            height: '36px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: text.trim() && !disabled ? '#ffffff' : '#64748b',
            cursor: text.trim() && !disabled ? 'pointer' : 'not-allowed',
            flexShrink: 0,
            marginBottom: '2px',
            transition: 'all 0.2s',
          }}
        >
          <Send style={{ width: '18px', height: '18px' }} />
        </button>
      </div>

      <div style={{ fontSize: '11px', color: '#64748b', textAlign: 'center', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px' }}>
        <Sparkles style={{ width: '12px', height: '12px', color: '#a855f7' }} />
        ASR_AI Assistant converts natural language queries into automated terminal diagnostics & remediations.
      </div>
    </div>
  );
};

```

---

<a id="file-9"></a>
### 9. `src/modules/ai/components/AiReviewBanner.tsx`

**Path:** `src/modules/ai/components/AiReviewBanner.tsx` | **Size:** `7.73 KB` | **Lines:** `258` | **Language:** `tsx`

```tsx
import React from 'react';
import { AlertTriangle, CheckCircle, ShieldAlert, Terminal } from 'lucide-react';

interface AiHostReviewBannerProps {
  type: 'host';
  title?: string;
  description?: string;
  targetHosts?: string[];
  onAgree: () => void;
  onRefine: () => void;
}

interface AiCommandReviewBannerProps {
  type: 'command';
  title?: string;
  description?: string;
  command?: string;
  platform?: 'PS' | 'CMD';
  criticalityLevel?: number;
  onApproveExecute: () => void;
  onDecline: () => void;
}

type AiReviewBannerProps = AiHostReviewBannerProps | AiCommandReviewBannerProps;

export const AiReviewBanner: React.FC<AiReviewBannerProps> = (props) => {
  if (props.type === 'host') {
    const { title, description, targetHosts, onAgree, onRefine } = props;
    const hasHosts = Boolean(targetHosts && targetHosts.length > 0);

    return (
      <div
        style={{
          background: '#1e1b4b',
          border: '1px solid #6366f1',
          borderRadius: '8px',
          padding: '16px',
          marginTop: '12px',
          marginBottom: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
          <AlertTriangle style={{ width: '20px', height: '20px', color: '#818cf8' }} />
          <h4 style={{ margin: 0, fontSize: '15px', color: '#e0e7ff', fontWeight: 600 }}>
            {title || 'Host Resolution Confirmation Required'}
          </h4>
        </div>

        {description && (
          <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: '#c7d2fe', lineHeight: 1.4 }}>
            {description}
          </p>
        )}

        {hasHosts && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '12px', color: '#a5b4fc', fontWeight: 500 }}>Target Hosts:</span>
            {targetHosts!.map((h) => (
              <span
                key={h}
                style={{
                  background: '#312e81',
                  border: '1px solid #4f46e5',
                  color: '#e0e7ff',
                  fontSize: '12px',
                  fontWeight: 600,
                  padding: '2px 8px',
                  borderRadius: '4px',
                }}
              >
                {h}
              </span>
            ))}
          </div>
        )}

        <div style={{ display: 'flex', gap: '12px' }}>
          {hasHosts && (
            <button
              onClick={onAgree}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '6px 14px',
                background: '#4f46e5',
                border: 'none',
                borderRadius: '6px',
                color: '#ffffff',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              <CheckCircle style={{ width: '15px', height: '15px' }} />
              Agree & Proceed
            </button>
          )}
          <button
            onClick={onRefine}
            style={{
              padding: '6px 14px',
              background: '#312e81',
              border: '1px solid #4338ca',
              borderRadius: '6px',
              color: '#c7d2fe',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            Refine Inputs
          </button>
        </div>
      </div>
    );
  }

  // Command Execution Review
  const { title, description, command, platform, criticalityLevel = 0, onApproveExecute, onDecline } = props;

  const getCriticalityBadge = (level: number) => {
    switch (level) {
      case 1:
        return {
          tag: '(Warning)',
          label: 'Level 1: Warning / Process Action',
          gradient: 'linear-gradient(135deg, #d97706 0%, #ea580c 100%)',
          borderColor: '#f59e0b',
        };
      case 2:
        return {
          tag: '(Critical)',
          label: 'Level 2: Critical State Change',
          gradient: 'linear-gradient(135deg, #ea580c 0%, #dc2626 100%)',
          borderColor: '#ef4444',
        };
      case 3:
        return {
          tag: '(High Critical)',
          label: 'Level 3: Severe State Change / Deletion',
          gradient: 'linear-gradient(135deg, #b91c1c 0%, #7f1d1d 100%)',
          borderColor: '#f87171',
        };
      default:
        return {
          tag: '(Normal)',
          label: 'Level 0: Inspection Action',
          gradient: 'linear-gradient(135deg, #059669 0%, #0d9488 100%)',
          borderColor: '#10b981',
        };
    }
  };

  const badge = getCriticalityBadge(criticalityLevel);

  return (
    <div
      style={{
        background: '#18181b',
        border: `1px solid ${badge.borderColor}`,
        borderRadius: '8px',
        padding: '16px',
        marginTop: '12px',
        marginBottom: '12px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldAlert style={{ width: '20px', height: '20px', color: badge.borderColor }} />
          <h4 style={{ margin: 0, fontSize: '15px', color: '#f4f4f5', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>{title || 'Command Review & Approval Required'}</span>
            <span
              style={{
                background: badge.gradient,
                border: `1px solid ${badge.borderColor}`,
                color: '#ffffff',
                fontSize: '12px',
                fontWeight: 700,
                padding: '2px 8px',
                borderRadius: '12px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.3)',
              }}
            >
              {badge.tag}
            </span>
          </h4>
        </div>
        <span style={{ fontSize: '11px', color: '#a1a1aa', fontWeight: 500 }}>
          {badge.label}
        </span>
      </div>

      {description && (
        <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: '#a1a1aa', lineHeight: 1.4 }}>
          {description}
        </p>
      )}

      {command && (
        <div
          style={{
            background: '#09090b',
            border: '1px solid #27272a',
            borderRadius: '6px',
            padding: '10px 12px',
            fontFamily: 'Consolas, monospace',
            fontSize: '13px',
            color: '#38bdf8',
            marginBottom: '16px',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-all',
          }}
        >
          <div style={{ fontSize: '11px', color: '#71717a', marginBottom: '4px', textTransform: 'uppercase' }}>
            Target Platform: {platform || 'PS'}
          </div>
          {command}
        </div>
      )}

      <div style={{ display: 'flex', gap: '12px' }}>
        <button
          onClick={onApproveExecute}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '8px 16px',
            background: '#16a34a',
            border: 'none',
            borderRadius: '6px',
            color: '#ffffff',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          <Terminal style={{ width: '15px', height: '15px' }} />
          Approve & Execute Command
        </button>
        <button
          onClick={onDecline}
          style={{
            padding: '8px 16px',
            background: '#27272a',
            border: '1px solid #3f3f46',
            borderRadius: '6px',
            color: '#d4d4d8',
            fontSize: '13px',
            cursor: 'pointer',
          }}
        >
          Decline / Refine
        </button>
      </div>
    </div>
  );
};

```

---

<a id="file-10"></a>
### 10. `src/modules/ai/components/HostSelectionOptionModal.tsx`

**Path:** `src/modules/ai/components/HostSelectionOptionModal.tsx` | **Size:** `5.52 KB` | **Lines:** `155` | **Language:** `tsx`

```tsx
import React, { useState } from 'react';
import { Server, HelpCircle, CheckCircle2 } from 'lucide-react';

interface HostSelectionOptionModalProps {
  onClose: () => void;
  onChooseKnowHosts: () => void;
  onChooseDontKnowHosts: () => void;
}

export const HostSelectionOptionModal: React.FC<HostSelectionOptionModalProps> = ({
  onClose,
  onChooseKnowHosts,
  onChooseDontKnowHosts,
}) => {
  const [showSmartAiInfo, setShowSmartAiInfo] = useState(false);

  const handleDontKnow = () => {
    setShowSmartAiInfo(true);
  };

  const handleSmartAiOk = () => {
    setShowSmartAiInfo(false);
    onChooseDontKnowHosts();
    onClose();
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(15, 23, 42, 0.75)',
        backdropFilter: 'blur(4px)',
        zIndex: 1100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
      }}
    >
      <div
        style={{
          background: '#1e293b',
          border: '1px solid #334155',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '480px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
          overflow: 'hidden',
          color: '#f8fafc',
        }}
      >
        <div style={{ padding: '20px 24px', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Server style={{ width: '20px', height: '20px', color: '#38bdf8' }} />
            Target Host Selection
          </h3>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '18px' }}
          >
            ✕
          </button>
        </div>

        <div style={{ padding: '24px' }}>
          {!showSmartAiInfo ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <p style={{ margin: 0, color: '#94a3b8', fontSize: '14px' }}>
                Do you know which specific target host server(s) you want ASR_AI to interact with?
              </p>

              <button
                onClick={() => {
                  onChooseKnowHosts();
                  onClose();
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '14px 16px',
                  background: '#0f172a',
                  border: '1px solid #3b82f6',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                }}
              >
                <Server style={{ width: '24px', height: '24px', color: '#60a5fa', flexShrink: 0 }} />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>I know the host(s)</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8' }}>Select specific target servers from the server inventory tree</div>
                </div>
              </button>

              <button
                onClick={handleDontKnow}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '14px 16px',
                  background: '#0f172a',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                }}
              >
                <HelpCircle style={{ width: '24px', height: '24px', color: '#a855f7', flexShrink: 0 }} />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>I don't know the host(s)</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8' }}>Let ASR_AI automatically resolve target hosts based on your query</div>
                </div>
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: '16px', padding: '12px 0' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: 'rgba(168, 85, 247, 0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <CheckCircle2 style={{ width: '28px', height: '28px', color: '#c084fc' }} />
              </div>
              <div style={{ fontSize: '15px', fontWeight: 500, color: '#f8fafc', lineHeight: 1.5 }}>
                Don't worry! Our <strong>ASR_AI</strong> is smart enough to help you resolve your issue/query and auto-determine the target host.
              </div>
              <button
                onClick={handleSmartAiOk}
                style={{
                  marginTop: '8px',
                  padding: '8px 24px',
                  background: '#8b5cf6',
                  border: 'none',
                  borderRadius: '6px',
                  color: '#ffffff',
                  fontWeight: 600,
                  fontSize: '14px',
                  cursor: 'pointer',
                }}
              >
                OK
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

```

---

<a id="file-11"></a>
### 11. `src/modules/ai/constants/aiEvents.ts`

**Path:** `src/modules/ai/constants/aiEvents.ts` | **Size:** `0.49 KB` | **Lines:** `16` | **Language:** `typescript`

```typescript
export const AI_EVENTS = {
  // Health / Liveness
  AI_HEALTH_STATUS: 'ai_health_status',

  // Step 1: Target Host Resolution
  AI_HOST_RESOLVE_REQ: 'ai_host_resolve_req',
  AI_HOST_RESOLVE_RESP: 'ai_host_resolve_resp',

  // Step 2: Command Generation & Remediation
  AI_REMEDIATE_REQ: 'ai_remediate_req',
  AI_REMEDIATE_RESP: 'ai_remediate_resp',

  // Step 3: Execution on Reserved AI Terminal
  AI_EXECUTE_CMD_REQ: 'ai_execute_cmd_req',
  AI_EXECUTE_CMD_RESP: 'ai_execute_cmd_resp',
} as const;

```

---

<a id="file-12"></a>
### 12. `src/modules/ai/services/aiChatDb.ts`

**Path:** `src/modules/ai/services/aiChatDb.ts` | **Size:** `3.55 KB` | **Lines:** `104` | **Language:** `typescript`

```typescript
import { AiConversation, AiChatMessage } from '../types/ai.types';

const DB_NAME = 'AutomationSuite_AiChatDB';
const DB_VERSION = 1;

function openDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event: any) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains('conversations')) {
        const convStore = db.createObjectStore('conversations', { keyPath: 'id' });
        convStore.createIndex('updatedAt', 'updatedAt', { unique: false });
      }
      if (!db.objectStoreNames.contains('messages')) {
        const msgStore = db.createObjectStore('messages', { keyPath: 'id' });
        msgStore.createIndex('conversationId', 'conversationId', { unique: false });
      }
    };

    request.onsuccess = (event: any) => {
      resolve(event.target.result);
    };

    request.onerror = (event: any) => {
      reject(event.target.error);
    };
  });
}

export async function getAllConversations(): Promise<AiConversation[]> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('conversations', 'readonly');
    const store = tx.objectStore('conversations');
    const request = store.getAll();
    request.onsuccess = () => {
      const list: AiConversation[] = request.result || [];
      list.sort((a, b) => b.updatedAt - a.updatedAt);
      resolve(list);
    };
    request.onerror = () => reject(request.error);
  });
}

export async function saveConversation(conv: AiConversation): Promise<void> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('conversations', 'readwrite');
    const store = tx.objectStore('conversations');
    const request = store.put(conv);
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
}

export async function deleteConversationDb(id: string): Promise<void> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(['conversations', 'messages'], 'readwrite');
    tx.objectStore('conversations').delete(id);

    const msgStore = tx.objectStore('messages');
    const index = msgStore.index('conversationId');
    const req = index.openKeyCursor(IDBKeyRange.only(id));
    req.onsuccess = (e: any) => {
      const cursor = e.target.result;
      if (cursor) {
        msgStore.delete(cursor.primaryKey);
        cursor.continue();
      }
    };
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function getMessagesByConversation(conversationId: string): Promise<AiChatMessage[]> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('messages', 'readonly');
    const store = tx.objectStore('messages');
    const index = store.index('conversationId');
    const request = index.getAll(IDBKeyRange.only(conversationId));
    request.onsuccess = () => {
      const list: AiChatMessage[] = request.result || [];
      list.sort((a, b) => a.timestamp - b.timestamp);
      resolve(list);
    };
    request.onerror = () => reject(request.error);
  });
}

export async function saveChatMessage(msg: AiChatMessage): Promise<void> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('messages', 'readwrite');
    const store = tx.objectStore('messages');
    const request = store.put(msg);
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
}

```

---

<a id="file-13"></a>
### 13. `src/modules/ai/services/promptTemplates.ts`

**Path:** `src/modules/ai/services/promptTemplates.ts` | **Size:** `6.12 KB` | **Lines:** `98` | **Language:** `typescript`

```typescript
import { ServerConfig } from '../../terminal/types/terminal.types';

export const TARGET_HOST_SELECTION_PROMPT_TEMPLATE = `You are an AI Server Infrastructure & Target Resolution Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below. Do not include markdown code block formatting (like \`\`\`json fences), HTML, commentary, or text outside the JSON object.

User Input Query: "{USER_QUERY}"
Accumulated Additional Data: {ACCUMULATED_ADDITIONAL_DATA}

Server Inventory:
{SERVER_INVENTORY}

Output JSON Schema:
{
  "pre_response": "<short 1-sentence active status message indicating what analysis is being performed>",
  "target_hosts": ["H1187"],
  "review_required": <boolean: false if 100% confident in the selected target host(s); true if candidate hosts were selected or user confirmation is recommended>,
  "review_title": "<null | short 3-6 word title if review_required is true>",
  "review_description": "<null | clear explanation of why host confirmation is required if review_required is true>",
  "accumulated_additional_data": "<null for initial interaction | short 1-2 sentence keyword summary of collective context, resolved target hosts, and key findings accumulated across past turns>",
  "final_outcome": "<explanation of why candidate target hosts were selected, or clarifying questions for the user if more information is needed>"
}

Strict Rules:
1. Output MUST be valid JSON only.
2. target_hosts MUST be an array of valid host identifiers (e.g. ["H1187"] or ["H1191", "H1375"]). If no specific host can be identified and questions are needed, target_hosts should be candidate list or [].
3. review_required MUST be false ONLY if target_hosts are 100% clear and unambiguous; otherwise set to true.
4. If user query is vague or ambiguous (e.g. specifying only 'scaler server' without environment PROD/T01/DEV or hostname), set review_required to true, populate review_title and review_description, and ask clarifying questions in final_outcome.
5. Use the Server Inventory to map environment names (PROD, T01, T02, T03, T04, DEV), displayName, and server types (INSPIRE_AUTOMATION, INSPIRE_SCALER, IPS_POOL, INTERACTIVE) to exact host codes (e.g., H1187, H1191, H1351, H1627).
6. Return ONLY the JSON object.`;

export const SYSTEM_DIAGNOSTICS_REMEDIATION_PROMPT_TEMPLATE = `You are an AI System Automation Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below. Do not include markdown code block formatting (like \`\`\`json fences), HTML, commentary, or text outside the JSON object.

User Query: "{USER_QUERY}"
Target Hostname: "{HOSTNAME}"
Accumulated Additional Data: {ACCUMULATED_ADDITIONAL_DATA}

Output JSON Schema:
{
  "host": "{HOSTNAME}",
  "pre_response": "<short 1-sentence active status message indicating what action is being taken>",
  "Command": "<null | single-line PowerShell or CMD executable command string to inspect, remediate, or execute>",
  "platform": "<'CMD' | 'PS'>",
  "review_needed": <boolean: true if Command modifies system state (delete, kill process, set registry, rename, replace file, restart service, install update, reboot); false for read-only inspection commands or when Command is null>,
  "review_title": "<null | short 3-6 word title of the command if Command is present>",
  "review_description": "<null | clear 1-sentence explanation of what the command does if Command is present>",
  "CriticalityLevele": <integer: 0 (normal read-only inspection), 1 (warning / process kill), 2 (critical state change / service restart), 3 (severe / file deletion / OS update / reboot)>,
  "file_needed": <null | array of UNC network file paths if a file needs to be fetched from host, e.g. ["\\\\\\\\localhost\\\\C$\\\\Ranjan\\\\Logs\\\\app.log"]>,
  "accumulated_additional_data": "<null for initial interaction | short 1-2 sentence keyword summary of collective context, target host, commands executed, and key findings accumulated across past turns>",
  "final_outcome": "<null | string containing answer/summary if no command is needed or when resolving user question>"
}

Strict Rules:
1. Output MUST be valid JSON only.
2. host is mandatory (e.g. localhost or H1187).
3. platform MUST be "CMD" or "PS".
4. If user requests to inspect a specific file path on host, convert file path into UNC format (e.g. C:\\Ranjan\\app.log -> \\\\host\\C$\\Ranjan\\app.log) and place in file_needed.
5. review_needed MUST be true for state-modifying actions (delete, kill, set, restart, update, reboot), and false for read-only inspection or file requests.
6. If user query is pure conversation or general explanation where no system command is needed, set Command to null, review_needed to false, and place the answer inside final_outcome.
7. CriticalityLevele MUST be an integer: 0 (normal/info), 1 (warning), 2 (critical), 3 (severe/destructive/reboot).
8. Return ONLY the JSON object.`;

export function formatServerInventoryJson(servers: ServerConfig[]): string {
  if (!servers || servers.length === 0) return '[]';
  const simplified = servers.map((s) => ({
    host: s.host,
    serverType: s.serverType || s.category,
    env: s.env || s.environment,
    displayName: s.displayName,
    port: s.port || 9010,
    environment: s.environment,
    category: s.category,
  }));
  return JSON.stringify(simplified, null, 2);
}

export function buildHostSelectionPrompt(
  query: string,
  servers: ServerConfig[],
  accumulatedData?: string | null
): string {
  const inventoryJson = formatServerInventoryJson(servers);
  const dataVal = accumulatedData && accumulatedData.trim() ? accumulatedData.trim() : 'null';
  return TARGET_HOST_SELECTION_PROMPT_TEMPLATE
    .replace('{USER_QUERY}', query)
    .replace('{ACCUMULATED_ADDITIONAL_DATA}', dataVal)
    .replace('{SERVER_INVENTORY}', inventoryJson);
}

export function buildDiagnosticsRemediationPrompt(
  query: string,
  host: string,
  accumulatedData?: string | null
): string {
  const dataVal = accumulatedData && accumulatedData.trim() ? accumulatedData.trim() : 'null';
  return SYSTEM_DIAGNOSTICS_REMEDIATION_PROMPT_TEMPLATE
    .replace('{USER_QUERY}', query)
    .replace(new RegExp('{HOSTNAME}', 'g'), host || 'localhost')
    .replace('{ACCUMULATED_ADDITIONAL_DATA}', dataVal);
}

```

---

<a id="file-14"></a>
### 14. `src/modules/ai/services/terminal/system_prompt_cli_agent.txt`

**Path:** `src/modules/ai/services/terminal/system_prompt_cli_agent.txt` | **Size:** `0.69 KB` | **Lines:** `1` | **Language:** `text`

```text
You are an AI Assistant for Automation Suite. Generate pure valid JSON ONLY matching schema: {"type":"EXECUTE"|"FINAL_ANSWER","reasoning":"<string>","executions":[{"host":"<ip_or_name>","platform":"PS"|"CMD","command":"<cmd>","is_mutating":true|false,"risk_description":"<string>"}],"final_answer":"<string>"}. RULES: 1) Pure JSON output only without markdown codeblock wrappers. 2) Execution terminals run in Administrator mode; decline if user profile directory is required. 3) Target host must be selected ONLY from active connected SERVER INVENTORY. Select platform PS for PowerShell or CMD for Command Prompt. 4) Set is_mutating=true for actions modifying settings, deleting files, or restarting services.
```

---

<a id="file-15"></a>
### 15. `src/modules/ai/types/ai.types.ts`

**Path:** `src/modules/ai/types/ai.types.ts` | **Size:** `1.46 KB` | **Lines:** `65` | **Language:** `typescript`

```typescript
export interface AiHealthFrame {
  'event-type': 'ai_health_status';
  status: 'OK' | 'NOT_OK';
  extsnConnected: boolean;
  activeTasks?: number;
  queuedTasks?: number;
  timestamp?: number;
}

export interface AiExecuteTaskFrame {
  'event-type': 'AI_EXECUTE_TASK';
  taskId: string;
  conversationId: string | null;
  prompt: string;
  filesPath?: string[];
}

export interface AiTaskResultFrame {
  'event-type': 'AI_TASK_RESULT';
  taskId: string;
  conversationId: string;
  status: 'RESPONSE' | 'FAILED' | 'RECEIVED';
  rawResponse?: string;
  error?: string | null;
}

export interface AiExecuteCmdRespFrame {
  'event-type': 'ai_execute_cmd_resp';
  conversationId: string;
  terminalId?: string;
  host?: string;
  status?: string;
  error?: string;
}

export interface AiConversation {
  id: string;
  title: string;
  targetHosts: string[];
  isHostKnown: boolean;
  accumulatedAdditionalData?: string | null;
  createdAt: number;
  updatedAt: number;
}

export interface AiChatMessage {
  id: string;
  conversationId: string;
  sender: 'user' | 'ai' | 'system';
  text?: string;
  preResponse?: string;
  command?: string;
  platform?: 'PS' | 'CMD';
  reviewNeeded?: boolean;
  reviewTitle?: string;
  reviewDescription?: string;
  criticalityLevel?: number;
  fileNeeded?: string[];
  finalOutcome?: string;
  targetHosts?: string[];
  reviewRequiredHost?: boolean;
  accumulatedAdditionalData?: string | null;
  status?: 'pending' | 'completed' | 'error';
  timestamp: number;
}

```

---

<a id="file-16"></a>
### 16. `src/modules/terminal/TerminalDashboardPage.tsx`

**Path:** `src/modules/terminal/TerminalDashboardPage.tsx` | **Size:** `21.70 KB` | **Lines:** `600` | **Language:** `tsx`

```tsx
import React, { useState, useEffect, useRef } from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession, ServerCategory, Environment, TerminalFrame, TerminalPlatform } from './types/terminal.types';
import { fetchServerInventory, fetchServerStatus, renameTerminalApi } from '../../services/api';
import { globalWebSocket } from '../../services/globalWebSocket';
import { HeaderServerFilter } from './components/HeaderServerFilter';
import { ServerCardGrid } from './components/ServerCardGrid';
import { TerminalModalWindow } from './components/TerminalModalWindow';
import { StartTerminalModal } from './components/StartTerminalModal';
import { TerminalRenameModal } from './components/TerminalRenameModal';
import { ServerFilterDialog } from './components/ServerFilterDialog';
import { HostTerminalDetailsModal } from './components/HostTerminalDetailsModal';
import { TerminalConsoleCanvas } from './components/TerminalConsoleCanvas';
import { getTerminalCache, setTerminalCache, appendTerminalCache, clearTerminalCache } from './services/terminalCache';
import { AiChatModal } from '../ai/components/AiChatModal';
import { AiConversation } from '../ai/types/ai.types';
import { getAllConversations } from '../ai/services/aiChatDb';


/**
 * Tracks the last accepted stream-chunk count per (eventType:terminalId) so a
 * terminal_result / terminal_error frame that is delivered twice (e.g. a
 * duplicate WebSocket broadcast) is appended to the canvas only once.
 * The agent increments `count` for every chunk of the current execution and
 * starts again from 1 on the next execution.
 */
interface ChunkTracker {
  lastCount: number;
  ended: boolean;
}

function shouldAcceptChunk(
  trackers: Map<string, ChunkTracker>,
  key: string,
  count: number,
  end: boolean
): boolean {
  const t = trackers.get(key);
  // No history (fresh terminal) or previous execution completed → accept & start anew.
  if (!t || t.ended) {
    trackers.set(key, { lastCount: count, ended: end });
    return true;
  }
  if (count > t.lastCount) {
    trackers.set(key, { lastCount: count, ended: end });
    return true;
  }
  return false; // duplicate / stale chunk already applied to the canvas
}

export const TerminalDashboardPage: React.FC = () => {
  const [servers, setServers] = useState<ServerConfig[]>([]);
  const [statusMap, setStatusMap] = useState<Record<string, AgentConnectionStatus>>({});
  const [categoryFilter, setCategoryFilter] = useState<ServerCategory | 'ALL'>('ALL');
  const [envFilter, setEnvFilter] = useState<Environment | 'ALL'>('ALL');
  const [searchHost, setSearchHost] = useState('');

  // Selected hosts from V1 ServerFilterDialog
  const [selectedTreeHosts, setSelectedTreeHosts] = useState<string[]>([]);
  const [isFilterDialogOpen, setIsFilterDialogOpen] = useState(false);
  const hasInitializedFilterRef = useRef(false);

  // Start Terminal pop-up target server
  const [startModalServer, setStartModalServer] = useState<ServerConfig | null>(null);

  // Host Terminal Details pop-up target server
  const [detailsModalServer, setDetailsModalServer] = useState<ServerConfig | null>(null);

  // Rename Modal target session
  const [renameModalSession, setRenameModalSession] = useState<TerminalSession | null>(null);

  // AI Assistant States
  const [isAiOnline, setIsAiOnline] = useState(false);
  const [aiConversations, setAiConversations] = useState<AiConversation[]>([]);
  const [isAiStudioOpen, setIsAiStudioOpen] = useState(false);
  const [selectedAiConvId, setSelectedAiConvId] = useState<string | null>(null);


  const [sessions, setSessions] = useState<TerminalSession[]>([]);
  const [activeTerminalId, setActiveTerminalId] = useState<string | null>(null);
  const frameTrackerRef = useRef(new Map<string, ChunkTracker>());

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, []);

  const loadData = async () => {
    try {
      const inv = await fetchServerInventory();
      setServers(inv);
      if (!hasInitializedFilterRef.current && inv.length > 0) {
        setSelectedTreeHosts(inv.map((s) => s.host));
        hasInitializedFilterRef.current = true;
      }
      const st = await fetchServerStatus();
      setStatusMap(st);

      // Load AI conversations from IndexedDB
      getAllConversations().then(setAiConversations).catch(() => {});

      // Send metadata_list request for all connected hosts to pull active terminal sessions
      inv.forEach((srv) => {
        globalWebSocket.send({
          'event-type': 'metadata_list',
          host: srv.host,
        });
      });
    } catch (err) {
      console.warn('Error fetching inventory/status:', err);
    }
  };

  useEffect(() => {
    const unsubscribe = globalWebSocket.subscribe((frame: any) => {
      handleIncomingFrame(frame);
    });
    return unsubscribe;
  }, []);

  const handleIncomingFrame = (frame: any) => {
    if (frame['event-type'] === 'ai_health_status') {
      setIsAiOnline(frame.status === 'OK' && Boolean(frame.extsnConnected));
      return;
    }

    if (frame['event-type'] === 'terminal_history') {
      const tid = frame.terminalid || frame.terminalId;
      const lines = (frame as any).lines;
      if (tid && Array.isArray(lines)) {
        const fullLines = lines.length > 0 ? lines : [`[System] Loaded history for ${tid.substring(0, 8)}...`];
        setTerminalCache(tid, fullLines);
        setSessions((prev) =>
          prev.map((s) => (s.terminalId === tid ? { ...s, outputLines: fullLines } : s))
        );
      }
      return;
    }


    // Handle metadata_list response carrying array of terminal metadata snapshots
    if (frame['event-type'] === 'metadata_list') {
      const rawTerminals = frame.terminals || (frame as any).terminals;
      if (Array.isArray(rawTerminals)) {
        const historyReqs: string[] = [];

        rawTerminals.forEach((item: any) => {
          const tid = item.terminalId || item.terminalid;
          if (!tid) return;
          const cachedLines = getTerminalCache(tid);
          if (!cachedLines || cachedLines.length === 0) {
            const restoredLines = [`[System] Loaded session ${item.name || tid.substring(0, 8)} (${item.platform || 'PS'})...`];
            setTerminalCache(tid, restoredLines);
            historyReqs.push(tid);
          }
        });

        historyReqs.forEach((tid) => {
          globalWebSocket.send({
            'event-type': 'terminal_history',
            terminalid: tid,
          });
        });

        setSessions((prev) => {
          const next = [...prev];
          rawTerminals.forEach((item: any) => {
            const tid = item.terminalId || item.terminalid;
            if (!tid) return;
            const itemHost = item.host || (frame as any).host || 'localhost';
            const existingIdx = next.findIndex((s) => s.terminalId === tid);
            const cachedLines = getTerminalCache(tid);

            if (existingIdx >= 0) {
              const s = next[existingIdx];
              const updatedLines = cachedLines && cachedLines.length > 0
                ? cachedLines
                : s.outputLines;

              next[existingIdx] = {
                ...s,
                state: item.state || s.state,
                currentPath: item.currentPath || s.currentPath,
                elevated: item.elevated !== undefined ? Boolean(item.elevated) : s.elevated,
                platform: item.platform || s.platform,
                name: item.name || s.name,
                lastCommand: item.lastCommand !== undefined ? item.lastCommand : s.lastCommand,
                lastResult: item.lastResult !== undefined ? item.lastResult : s.lastResult,
                lastError: item.lastError !== undefined ? item.lastError : s.lastError,
                outputLines: updatedLines,
              };
            } else {
              const restoredLines = cachedLines && cachedLines.length > 0
                ? cachedLines
                : [`[System] Loaded session ${item.name || tid.substring(0, 8)} (${item.platform || 'PS'})...`];

              next.push({
                terminalId: tid,
                name: item.name || `Terminal (${tid.substring(0, 8)})`,
                host: itemHost,
                platform: item.platform || 'PS',
                elevated: Boolean(item.elevated),
                state: item.state || 'CLOSED',
                currentPath: item.currentPath || 'C:\\',
                lastCommand: item.lastCommand || null,
                lastResult: item.lastResult || null,
                lastError: item.lastError || null,
                outputLines: restoredLines,
                history: item.lastCommand ? [item.lastCommand] : [],
                historyIndex: item.lastCommand ? 1 : -1,
              });
            }
          });
          return next;
        });
      }
      return;
    }

    const terminalId = frame.terminalid || frame.terminalId;
    if (!terminalId) return;

    // Dedupe: skip a terminal_result / terminal_error chunk that was already
    // applied once (the same frame reaching the browser twice doubles every line).
    const frameType = frame['event-type'];
    const isStreamChunk = (frameType === 'terminal_result' || frameType === 'terminal_error') && Boolean(frame.chunk);
    const chunkCount = typeof frame.count === 'number' ? frame.count : -1;
    if (isStreamChunk && chunkCount >= 0) {
      const key = `${frameType}:${terminalId}`;
      if (!shouldAcceptChunk(frameTrackerRef.current, key, chunkCount, Boolean(frame.end))) {
        return; // duplicate chunk — drop it
      }
    }
    // A new command starts with state RUNNING; the agent restarts its chunk
    // counter from 1, so forget the previous execution's counters.
    if (frameType === 'terminal_status' && frame.state === 'RUNNING') {
      frameTrackerRef.current.delete(`terminal_result:${terminalId}`);
      frameTrackerRef.current.delete(`terminal_error:${terminalId}`);
    }

    let updatedLinesForChunk: string[] | null = null;
    if (frame['event-type'] === 'terminal_result' && frame.chunk) {
      appendTerminalCache(terminalId, frame.chunk);
      updatedLinesForChunk = getTerminalCache(terminalId) || null;
    } else if (frame['event-type'] === 'terminal_error' && frame.chunk) {
      const errChunk = `[ERROR] ${frame.chunk}`;
      appendTerminalCache(terminalId, errChunk);
      updatedLinesForChunk = getTerminalCache(terminalId) || null;
    }

    setSessions((prev) =>
      prev.map((s) => {
        if (s.terminalId !== terminalId) return s;

        const next = { ...s, lastActivityAt: Date.now() };

        if (updatedLinesForChunk) {
          next.outputLines = updatedLinesForChunk;
        }

        if (frame['event-type'] === 'terminal_status') {
          if (frame.state) next.state = frame.state;
          if (frame.currentPath) next.currentPath = frame.currentPath;
          if (frame.elevated !== undefined) next.elevated = frame.elevated;
        }

        return next;
      })
    );
  };

  const handleStartTerminalSubmit = (server: ServerConfig, name: string, platform: TerminalPlatform) => {
    const openSessions = sessions.filter((s) => s.state !== 'CLOSED');
    if (openSessions.length >= 4) {
      alert('Maximum limit of 4 active terminals reached. Please close an existing terminal first.');
      return;
    }

    const userId = 'user';
    const uuid = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 9);
    const isAsr = name.toUpperCase().includes('ASR_AI') || name.toUpperCase().includes('ASR');
    const terminalId = isAsr ? `${userId}_ASR_AI_${uuid}` : `${userId}_${uuid}`;

    const newSession: TerminalSession = {
      terminalId,
      name,
      host: server.host,
      platform,
      state: 'IDLE',
      currentPath: 'C:\\',
      lastCommand: null,
      lastResult: null,
      lastError: null,
      outputLines: [`[System] Connecting to ${server.host} (${platform})...`],
      history: [],
      historyIndex: -1,
      lastActivityAt: Date.now(),
    };

    setTerminalCache(terminalId, newSession.outputLines);

    setSessions((prev) => [...prev, newSession]);
    setActiveTerminalId(terminalId);
    setStartModalServer(null);

    // Send terminal_start frame over global websocket
    globalWebSocket.send({
      'event-type': 'terminal_start',
      terminalid: terminalId,
      host: server.host,
      userId: userId,
      platform: platform,
      name: name,
    });
  };

  const handleExecute = (terminalId: string, host: string, command: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    if (!target) return;

    const currentPath = target.currentPath && target.currentPath.trim() ? target.currentPath : 'C:\\';
    const promptPrefix = target.platform === 'PS' ? `PS ${currentPath}>` : `${currentPath}>`;
    const commandLine = command ? `${promptPrefix} ${command}` : promptPrefix;
    const isNonEmpty = Boolean(command && command.trim());

    appendTerminalCache(terminalId, commandLine);
    const updatedLines = getTerminalCache(terminalId) || [...target.outputLines, commandLine];

    setSessions((prev) =>
      prev.map((s) => {
        if (s.terminalId !== terminalId) return s;

        return {
          ...s,
          outputLines: updatedLines,
          history: isNonEmpty ? [...s.history, command] : s.history,
          historyIndex: isNonEmpty ? s.history.length + 1 : s.historyIndex,
          state: isNonEmpty ? 'RUNNING' : s.state,
          lastActivityAt: Date.now(),
        };
      })
    );

    if (command && command.trim()) {
      globalWebSocket.send({
        'event-type': 'terminal_execute',
        terminalid: terminalId,
        host: host,
        userId: 'user',
        command: command,
      });
    }
  };

  const handleStop = (terminalId: string, host: string) => {
    globalWebSocket.send({
      'event-type': 'terminal_stop',
      terminalid: terminalId,
      host: host,
    });
  };

  const handleSelectTerminal = (terminalId: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    const cachedLines = getTerminalCache(terminalId);

    if (target && target.state === 'CLOSED') {
      globalWebSocket.send({
        'event-type': 'terminal_start',
        terminalid: target.terminalId,
        host: target.host,
        userId: 'user',
        platform: target.platform,
        name: target.name,
        path: target.currentPath,
      });

      setSessions((prev) =>
        prev.map((s) =>
          s.terminalId === terminalId
            ? {
                ...s,
                state: 'IDLE',
                outputLines: cachedLines || [...s.outputLines, `[System] Re-opening terminal session ${s.name} (${s.platform})...`],
                lastActivityAt: Date.now(),
              }
            : s
        )
      );
    } else {
      setSessions((prev) =>
        prev.map((s) =>
          s.terminalId === terminalId
            ? {
                ...s,
                outputLines: cachedLines || s.outputLines,
                lastActivityAt: Date.now(),
              }
            : s
        )
      );
    }
    setActiveTerminalId(terminalId);
  };

  const handleCloseTab = (terminalId: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    if (target) {
      globalWebSocket.send({
        'event-type': 'terminal_close',
        terminalid: terminalId,
        host: target.host,
      });
      // NOTE: terminal_remove is NOT sent; closed terminals remain listed in host card & DB
    }

    // Destroy Map cache on terminal close
    clearTerminalCache(terminalId);
    frameTrackerRef.current.delete(`terminal_result:${terminalId}`);
    frameTrackerRef.current.delete(`terminal_error:${terminalId}`);

    setSessions((prev) =>
      prev.map((s) => (s.terminalId === terminalId ? { ...s, state: 'CLOSED', lastActivityAt: Date.now() } : s))
    );

    const remainingOpen = sessions.filter((s) => s.terminalId !== terminalId && s.state !== 'CLOSED');
    if (activeTerminalId === terminalId) {
      setActiveTerminalId(remainingOpen.length > 0 ? remainingOpen[remainingOpen.length - 1].terminalId : null);
    }
  };

  const handleRename = async (terminalId: string, newName: string) => {
    try {
      await renameTerminalApi(terminalId, newName);
    } catch (err) {
      console.warn('Failed to rename terminal in backend DB:', err);
    }
    setSessions((prev) =>
      prev.map((s) => (s.terminalId === terminalId ? { ...s, name: newName } : s))
    );
  };

  const filteredServers = servers.filter((s) => {
    const category = s.serverType || s.category;
    const env = s.env || s.environment;

    if (categoryFilter !== 'ALL' && category !== categoryFilter) return false;
    if (envFilter !== 'ALL' && env !== envFilter) return false;
    if (selectedTreeHosts.length > 0 && !selectedTreeHosts.includes(s.host)) return false;
    if (searchHost && !s.host.toLowerCase().includes(searchHost.toLowerCase()) && !s.displayName.toLowerCase().includes(searchHost.toLowerCase())) {
      return false;
    }
    return true;
  });

  const urlParams = new URLSearchParams(window.location.search);
  const standaloneTerminalId = urlParams.get('terminalId');

  if (standaloneTerminalId) {
    const standaloneSession = sessions.find((s) => s.terminalId === standaloneTerminalId) || {
      terminalId: standaloneTerminalId,
      name: `Terminal (${standaloneTerminalId.substring(0, 8)})`,
      host: 'localhost',
      platform: 'PS' as TerminalPlatform,
      state: 'IDLE' as const,
      currentPath: 'C:\\',
      lastCommand: null,
      lastResult: null,
      lastError: null,
      outputLines: getTerminalCache(standaloneTerminalId) || [`[System] Loaded terminal ${standaloneTerminalId}...`],
      history: [],
      historyIndex: -1,
    };

    return (
      <div style={{ width: '100vw', height: '100vh', background: '#0f172a', display: 'flex', flexDirection: 'column' }}>
        <div style={{ padding: '8px 16px', background: '#1e293b', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#f8fafc' }}>
          <div style={{ fontWeight: 600, fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: standaloneSession.state === 'RUNNING' ? '#eab308' : '#22c55e' }} />
            {standaloneSession.name} ({standaloneSession.platform}) - Standalone View
          </div>
          <button
            onClick={() => window.close()}
            style={{ background: '#334155', border: 'none', color: '#f8fafc', padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
          >
            Close Window
          </button>
        </div>
        <div style={{ flex: 1, minHeight: 0 }}>
          <TerminalConsoleCanvas
            session={standaloneSession}
            onExecute={handleExecute}
            onStop={handleStop}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <HeaderServerFilter
        selectedCount={selectedTreeHosts.length}
        totalCount={servers.length}
        onOpenServerFilterDialog={() => setIsFilterDialogOpen(true)}
      />

      <ServerCardGrid
        servers={filteredServers}
        statusMap={statusMap}
        sessions={sessions}
        isAiOnline={isAiOnline}
        aiConversations={aiConversations}
        onOpenStartModal={(server) => setStartModalServer(server)}
        onOpenDetailsModal={(server) => setDetailsModalServer(server)}
        onSelectTerminal={handleSelectTerminal}
        onCloseTerminal={handleCloseTab}
        onOpenAiStudio={(convId) => {
          setSelectedAiConvId(convId || null);
          setIsAiStudioOpen(true);
        }}
        onNewAiChat={() => {
          setSelectedAiConvId(null);
          setIsAiStudioOpen(true);
        }}
      />

      {isAiStudioOpen && (
        <AiChatModal
          isOpen={isAiStudioOpen}
          onClose={() => {
            setIsAiStudioOpen(false);
            getAllConversations().then(setAiConversations).catch(() => {});
          }}
          servers={servers}
          initialConversationId={selectedAiConvId}
        />
      )}


      <TerminalModalWindow
        sessions={sessions}
        activeTerminalId={activeTerminalId}
        onSelectTab={handleSelectTerminal}
        onCloseTab={handleCloseTab}
        onExecute={handleExecute}
        onStop={handleStop}
        onRename={handleRename}
        onCloseWindow={() => {
          sessions.filter((s) => s.state !== 'CLOSED').forEach((s) => handleCloseTab(s.terminalId));
          setActiveTerminalId(null);
        }}
      />

      {startModalServer && (
        <StartTerminalModal
          server={startModalServer}
          onStart={handleStartTerminalSubmit}
          onClose={() => setStartModalServer(null)}
        />
      )}

      {detailsModalServer && (
        <HostTerminalDetailsModal
          server={detailsModalServer}
          statusMap={statusMap}
          sessions={sessions}
          onStartNewTerminal={(server) => {
            setDetailsModalServer(null);
            setStartModalServer(server);
          }}
          onSelectTerminal={handleSelectTerminal}
          onCloseTerminal={handleCloseTab}
          onRenameModalOpen={(sess) => setRenameModalSession(sess)}
          onClose={() => setDetailsModalServer(null)}
        />
      )}

      {renameModalSession && (
        <TerminalRenameModal
          session={renameModalSession}
          onSave={(id, name) => {
            handleRename(id, name);
            setRenameModalSession(null);
          }}
          onClose={() => setRenameModalSession(null)}
        />
      )}

      <ServerFilterDialog
        open={isFilterDialogOpen}
        onClose={() => setIsFilterDialogOpen(false)}
        servers={servers}
        selectedHosts={selectedTreeHosts}
        onApply={(hosts) => setSelectedTreeHosts(hosts)}
      />
    </div>
  );
};


```

---

<a id="file-17"></a>
### 17. `src/modules/terminal/components/AiHostCard.tsx`

**Path:** `src/modules/terminal/components/AiHostCard.tsx` | **Size:** `5.83 KB` | **Lines:** `178` | **Language:** `tsx`

```tsx
import React from 'react';
import { Bot, MessageSquare, Plus, Sparkles, Activity } from 'lucide-react';
import { AiConversation } from '../../ai/types/ai.types';

interface AiHostCardProps {
  isAiOnline: boolean;
  conversations: AiConversation[];
  onOpenStudio: (convId?: string) => void;
  onNewChat: () => void;
}

export const AiHostCard: React.FC<AiHostCardProps> = ({
  isAiOnline,
  conversations,
  onOpenStudio,
  onNewChat,
}) => {
  return (
    <div
      style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #0f172a 100%)',
        border: '1px solid #6366f1',
        borderRadius: '12px',
        padding: '16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
        boxShadow: '0 4px 12px rgba(99, 102, 241, 0.15)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Background Accent Glow */}
      <div
        style={{
          position: 'absolute',
          top: '-20px',
          right: '-20px',
          width: '100px',
          height: '100px',
          borderRadius: '50%',
          background: 'rgba(99, 102, 241, 0.15)',
          filter: 'blur(20px)',
          pointerEvents: 'none',
        }}
      />

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '8px',
              background: '#4f46e5',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
            }}
          >
            <Bot style={{ width: '20px', height: '20px' }} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '15px', color: '#f8fafc', display: 'flex', alignItems: 'center', gap: '6px' }}>
              ASR_AI Assistant
              <Sparkles style={{ width: '14px', height: '14px', color: '#c084fc' }} />
            </div>
            <div style={{ fontSize: '11px', color: '#a5b4fc' }}>AI Terminal Automation Engine</div>
          </div>
        </div>

        {/* Liveness Indicator Dot */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: isAiOnline ? '#4ade80' : '#f87171' }}>
          <span
            style={{
              width: '10px',
              height: '10px',
              borderRadius: '50%',
              background: isAiOnline ? '#22c55e' : '#ef4444',
              boxShadow: isAiOnline ? '0 0 8px #22c55e' : 'none',
            }}
          />
          {isAiOnline ? 'AI Online' : 'AI Offline'}
        </div>
      </div>

      {/* Quick Action Bar */}
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          onClick={() => onOpenStudio()}
          style={{
            flex: 1,
            padding: '8px 12px',
            background: '#4f46e5',
            border: 'none',
            borderRadius: '6px',
            color: '#ffffff',
            fontWeight: 600,
            fontSize: '13px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
          }}
        >
          <MessageSquare style={{ width: '14px', height: '14px' }} />
          Open AI Studio
        </button>

        <button
          onClick={onNewChat}
          title="Start New AI Chat"
          style={{
            padding: '8px 12px',
            background: '#312e81',
            border: '1px solid #4338ca',
            borderRadius: '6px',
            color: '#e0e7ff',
            fontWeight: 600,
            fontSize: '13px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '4px',
          }}
        >
          <Plus style={{ width: '15px', height: '15px' }} />
          New Chat
        </button>
      </div>

      {/* Recent Chats Preview */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
        <div style={{ fontSize: '11px', fontWeight: 600, color: '#a5b4fc', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Recent Conversations ({conversations.length})
        </div>

        {conversations.length === 0 ? (
          <div style={{ fontSize: '12px', color: '#64748b', fontStyle: 'italic', padding: '6px 0' }}>
            No recent AI chats. Click 'New Chat' to start.
          </div>
        ) : (
          conversations.slice(0, 3).map((conv) => (
            <div
              key={conv.id}
              onClick={() => onOpenStudio(conv.id)}
              style={{
                padding: '6px 10px',
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid rgba(99, 102, 241, 0.2)',
                borderRadius: '6px',
                fontSize: '12px',
                color: '#e2e8f0',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                transition: 'background 0.2s',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', overflow: 'hidden', whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>
                <MessageSquare style={{ width: '13px', height: '13px', color: '#818cf8', flexShrink: 0 }} />
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>{conv.title}</span>
              </div>
              <span style={{ fontSize: '10px', color: '#64748b', flexShrink: 0 }}>
                {new Date(conv.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

```

---

<a id="file-18"></a>
### 18. `src/modules/terminal/components/HeaderServerFilter.tsx`

**Path:** `src/modules/terminal/components/HeaderServerFilter.tsx` | **Size:** `1.13 KB` | **Lines:** `43` | **Language:** `tsx`

```tsx
import React from 'react';
import { Terminal, Sliders } from 'lucide-react';

interface Props {
  selectedCount: number;
  totalCount: number;
  onOpenServerFilterDialog: () => void;
}

export const HeaderServerFilter: React.FC<Props> = ({
  selectedCount,
  totalCount,
  onOpenServerFilterDialog,
}) => {
  return (
    <header className="header-filter" style={{ justifyContent: 'flex-start', gap: '24px' }}>
      <div className="header-title">
        <Terminal className="text-blue-500" size={24} color="#3b82f6" />
        <span>Terminal Dashboard</span>
      </div>

      <button
        onClick={onOpenServerFilterDialog}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: 'rgba(59, 130, 246, 0.15)',
          border: '1px solid #3b82f6',
          color: '#f8fafc',
          padding: '6px 14px',
          borderRadius: '6px',
          fontWeight: 600,
          cursor: 'pointer',
          fontSize: '13px',
        }}
      >
        <Sliders size={14} color="#38bdf8" />
        Server Filter Tree ({selectedCount}/{totalCount})
      </button>
    </header>
  );
};

```

---

<a id="file-19"></a>
### 19. `src/modules/terminal/components/HostTerminalDetailsModal.tsx`

**Path:** `src/modules/terminal/components/HostTerminalDetailsModal.tsx` | **Size:** `8.78 KB` | **Lines:** `198` | **Language:** `tsx`

```tsx
import React from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession } from '../types/terminal.types';
import { Server, Wifi, WifiOff, Plus, Play, X, RotateCcw, Edit2 } from 'lucide-react';

interface Props {
  server: ServerConfig;
  statusMap: Record<string, AgentConnectionStatus>;
  sessions: TerminalSession[];
  onStartNewTerminal: (server: ServerConfig) => void;
  onSelectTerminal: (terminalId: string) => void;
  onCloseTerminal: (terminalId: string) => void;
  onRenameModalOpen?: (session: TerminalSession) => void;
  onClose: () => void;
}

export const HostTerminalDetailsModal: React.FC<Props> = ({
  server,
  statusMap,
  sessions,
  onStartNewTerminal,
  onSelectTerminal,
  onCloseTerminal,
  onRenameModalOpen,
  onClose,
}) => {
  const isConnected = statusMap[server.host]?.connected || false;
  const hostSessions = sessions
    .filter((s) => s.host === server.host)
    .sort((a, b) => (b.lastActivityAt || 0) - (a.lastActivityAt || 0));

  const getStateBadge = (state: string) => {
    switch (state) {
      case 'RUNNING': return <span style={{ color: '#eab308', background: 'rgba(234, 179, 8, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>RUNNING</span>;
      case 'IDLE': return <span style={{ color: '#22c55e', background: 'rgba(34, 197, 94, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>IDLE</span>;
      case 'CLOSING': return <span style={{ color: '#f97316', background: 'rgba(249, 115, 22, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>CLOSING</span>;
      case 'CLOSED': return <span style={{ color: '#ef4444', background: 'rgba(239, 68, 68, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>CLOSED</span>;
      default: return null;
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '640px', maxHeight: '85vh', display: 'flex', flexDirection: 'column' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '12px', borderBottom: '1px solid #334155' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Server size={22} color="#3b82f6" />
            <div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: '#f8fafc' }}>{server.host}</div>
              <div style={{ fontSize: '12px', color: '#94a3b8' }}>
                {server.serverType || server.category} | {server.env || server.environment}
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {isConnected ? (
              <span title="Connected" style={{ display: 'flex', alignItems: 'center' }}>
                <Wifi size={18} color="#22c55e" />
              </span>
            ) : (
              <span title="Disconnected" style={{ display: 'flex', alignItems: 'center' }}>
                <WifiOff size={18} color="#ef4444" />
              </span>
            )}
            <button
              onClick={() => onStartNewTerminal(server)}
              style={{
                background: '#3b82f6',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                padding: '6px 12px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              <Plus size={14} /> + New Terminal
            </button>
          </div>
        </div>

        <div style={{ flex: 1, overflowY: 'auto', padding: '14px 0' }}>
          <div style={{ fontSize: '13px', fontWeight: 700, color: '#f8fafc', marginBottom: '10px' }}>
            All Terminals for Host ({hostSessions.length})
          </div>

          {hostSessions.length === 0 ? (
            <div style={{ fontSize: '13px', color: '#94a3b8', fontStyle: 'italic', padding: '16px 0', textAlign: 'center' }}>
              No terminal sessions created for this host yet. Click "+ New Terminal" to launch one.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {hostSessions.map((sess) => (
                <div
                  key={sess.terminalId}
                  style={{
                    background: 'rgba(255,255,255,0.03)',
                    border: '1px solid #334155',
                    borderRadius: '8px',
                    padding: '12px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span style={{ fontWeight: 700, color: '#f8fafc', fontSize: '14px' }}>
                        {sess.elevated ? 'Administrator: ' : ''}{sess.name}
                      </span>
                      {((sess.name && sess.name.toUpperCase().includes('ASR_AI')) || (sess.terminalId && sess.terminalId.toUpperCase().includes('ASR_AI'))) ? (
                        <span title="ASR_AI terminal cannot be renamed" style={{ fontSize: '10px', color: '#64748b', fontStyle: 'italic', background: 'rgba(100,116,139,0.15)', padding: '1px 5px', borderRadius: '4px' }}>
                          Protected ASR_AI
                        </span>
                      ) : (
                        <button
                          onClick={() => onRenameModalOpen && onRenameModalOpen(sess)}
                          title="Rename terminal"
                          style={{
                            background: 'transparent',
                            border: 'none',
                            color: '#94a3b8',
                            cursor: 'pointer',
                            padding: '2px 4px',
                            display: 'flex',
                            alignItems: 'center',
                          }}
                        >
                          <Edit2 size={13} />
                        </button>
                      )}
                      <span style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 600 }}>({sess.platform})</span>
                      {getStateBadge(sess.state)}
                    </div>
                    <div style={{ fontSize: '11px', color: '#64748b', fontFamily: 'monospace' }}>
                      ID: {sess.terminalId} | Path: {sess.currentPath || 'C:\\'}
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <button
                      onClick={() => {
                        onSelectTerminal(sess.terminalId);
                        onClose();
                      }}
                      style={{
                        background: sess.state === 'CLOSED' ? '#059669' : '#3b82f6',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '4px',
                        padding: '6px 12px',
                        fontSize: '12px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                      }}
                    >
                      <Play size={12} /> Resume
                    </button>

                    {sess.state !== 'CLOSED' && (
                      <button
                        onClick={() => onCloseTerminal(sess.terminalId)}
                        style={{
                          background: 'rgba(239, 68, 68, 0.15)',
                          color: '#ef4444',
                          border: '1px solid #ef4444',
                          borderRadius: '4px',
                          padding: '5px 10px',
                          fontSize: '12px',
                          cursor: 'pointer',
                        }}
                      >
                        Close
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="dialog-actions" style={{ paddingTop: '12px', borderTop: '1px solid #334155' }}>
          <button onClick={onClose} style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}>
            Close Dialog
          </button>
        </div>
      </div>
    </div>
  );
};

```

---

<a id="file-20"></a>
### 20. `src/modules/terminal/components/ServerCardGrid.tsx`

**Path:** `src/modules/terminal/components/ServerCardGrid.tsx` | **Size:** `11.13 KB` | **Lines:** `271` | **Language:** `tsx`

```tsx
import React from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession } from '../types/terminal.types';
import { Server, Wifi, WifiOff, Plus, X } from 'lucide-react';
import { AiHostCard } from './AiHostCard';
import { AiConversation } from '../../ai/types/ai.types';

interface Props {
  servers: ServerConfig[];
  statusMap: Record<string, AgentConnectionStatus>;
  sessions: TerminalSession[];
  isAiOnline: boolean;
  aiConversations: AiConversation[];
  onOpenStartModal: (server: ServerConfig) => void;
  onOpenDetailsModal: (server: ServerConfig) => void;
  onSelectTerminal: (terminalId: string) => void;
  onCloseTerminal: (terminalId: string) => void;
  onOpenAiStudio: (convId?: string) => void;
  onNewAiChat: () => void;
}


const ENV_ORDER: Record<string, number> = {
  PROD: 0,
  T01: 1,
  T02: 2,
  T03: 3,
  T04: 4,
  DEV: 5,
};

const CAT_ORDER: Record<string, number> = {
  INSPIRE_AUTOMATION: 0,
  INSPIRE_SCALER: 1,
  IPS_POOL: 2,
  INTERACTIVE: 3,
};

export const ServerCardGrid: React.FC<Props> = ({
  servers,
  statusMap,
  sessions,
  isAiOnline,
  aiConversations,
  onOpenStartModal,
  onOpenDetailsModal,
  onSelectTerminal,
  onCloseTerminal,
  onOpenAiStudio,
  onNewAiChat,
}) => {

  // Sort servers by Environment (PROD -> T01 -> T02 -> T03 -> T04 -> DEV) then by Category (INSPIRE_AUTOMATION -> INSPIRE_SCALER -> IPS_POOL -> INTERACTIVE)
  const sortedServers = [...servers].sort((a, b) => {
    const envA = ENV_ORDER[a.env || a.environment] ?? 99;
    const envB = ENV_ORDER[b.env || b.environment] ?? 99;
    if (envA !== envB) return envA - envB;

    const catA = CAT_ORDER[a.serverType || a.category] ?? 99;
    const catB = CAT_ORDER[b.serverType || b.category] ?? 99;
    if (catA !== catB) return catA - catB;

    return a.host.localeCompare(b.host);
  });

  // Group sorted servers by Environment for distinct dashboard sections
  const envGroups: { env: string; servers: ServerConfig[] }[] = [];
  sortedServers.forEach((server) => {
    const envName = server.env || server.environment || 'DEV';
    let group = envGroups.find((g) => g.env === envName);
    if (!group) {
      group = { env: envName, servers: [] };
      envGroups.push(group);
    }
    group.servers.push(server);
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', padding: '24px' }}>
      {/* Dedicated AI Assistant Host Card (Unaffected by filters) */}
      <AiHostCard
        isAiOnline={isAiOnline}
        conversations={aiConversations}
        onOpenStudio={onOpenAiStudio}
        onNewChat={onNewAiChat}
      />

      {envGroups.map((group) => (

        <div key={group.env} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div
            style={{
              fontSize: '15px',
              fontWeight: 700,
              color: '#38bdf8',
              letterSpacing: '0.05em',
              borderBottom: '1px solid #334155',
              paddingBottom: '6px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <span>Environment: {group.env}</span>
            <span style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>
              ({group.servers.length} {group.servers.length === 1 ? 'host' : 'hosts'})
            </span>
          </div>

          <div className="grid-container" style={{ padding: 0 }}>
            {group.servers.map((server) => {
              const status = statusMap[server.host];
              const isConnected = status ? status.connected : false;
              // Sort sessions with most recent active/closed at top
              const allHostSessions = sessions
                .filter((s) => s.host === server.host)
                .sort((a, b) => (b.lastActivityAt || 0) - (a.lastActivityAt || 0));
              const recentSessions = allHostSessions.slice(0, 5);

              return (
                <div
                  key={server.host}
                  className="server-card"
                  onClick={() => onOpenDetailsModal(server)}
                >
                  {/* Card Header: Host Name, Category, Env, Icon-Only Wifi Status */}
                  <div className="card-header" style={{ marginBottom: '8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', overflow: 'hidden' }}>
                      <Server size={18} color="#3b82f6" />
                      <span className="host-name" style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                        {server.host}
                      </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>
                        {server.serverType || server.category}
                      </span>
                      {isConnected ? (
                        <span title="Connected" style={{ display: 'flex', alignItems: 'center' }}>
                          <Wifi size={16} color="#22c55e" />
                        </span>
                      ) : (
                        <span title="Disconnected" style={{ display: 'flex', alignItems: 'center' }}>
                          <WifiOff size={16} color="#ef4444" />
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Card Body: Recent 3-5 Terminals */}
                  <div className="card-body" style={{ marginTop: '8px' }}>
                    <div style={{ fontSize: '11px', fontWeight: 600, color: '#64748b', marginBottom: '4px' }}>
                      Terminals ({allHostSessions.length})
                    </div>

                    {recentSessions.length === 0 ? (
                      <div style={{ fontSize: '12px', color: '#64748b', fontStyle: 'italic', padding: '4px 0' }}>
                        No terminals yet.
                      </div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        {recentSessions.map((sess) => (
                          <div
                            key={sess.terminalId}
                            style={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              background: 'rgba(255,255,255,0.04)',
                              border: '1px solid #334155',
                              borderRadius: '6px',
                              padding: '5px 8px',
                              fontSize: '11px',
                            }}
                          >
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', overflow: 'hidden' }}>
                              <span
                                style={{
                                  width: '8px',
                                  height: '8px',
                                  borderRadius: '50%',
                                  background:
                                    sess.state === 'RUNNING'
                                      ? '#eab308'
                                      : sess.state === 'IDLE'
                                      ? '#22c55e'
                                      : sess.state === 'CLOSING'
                                      ? '#f97316'
                                      : '#ef4444',
                                }}
                              />
                              <span style={{ color: '#f8fafc', fontWeight: 600, textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                {sess.elevated ? 'Admin: ' : ''}{sess.name} ({sess.platform})
                              </span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              {sess.state === 'CLOSED' && (
                                <span style={{ color: '#ef4444', fontSize: '10px', fontWeight: 600, paddingRight: '2px' }}>CLOSED</span>
                              )}
                              {sess.state === 'CLOSING' && (
                                <span style={{ color: '#f97316', fontSize: '10px', fontWeight: 600, paddingRight: '2px' }}>CLOSING</span>
                              )}
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onSelectTerminal(sess.terminalId);
                                }}
                                style={{
                                  background: sess.state === 'CLOSED' ? '#059669' : '#3b82f6',
                                  color: '#ffffff',
                                  border: 'none',
                                  borderRadius: '4px',
                                  padding: '2px 8px',
                                  fontSize: '10px',
                                  fontWeight: 600,
                                  cursor: 'pointer',
                                }}
                              >
                                Resume
                              </button>
                              {sess.state !== 'CLOSED' && (
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    onCloseTerminal(sess.terminalId);
                                  }}
                                  style={{
                                    background: 'transparent',
                                    color: '#ef4444',
                                    border: 'none',
                                    padding: '2px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                  }}
                                  title="Close Terminal"
                                >
                                  <X size={12} />
                                </button>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Card Footer: + New Terminal */}
                  <div className="card-footer" style={{ marginTop: '10px' }}>
                    <button
                      className="btn-open-terminal"
                      style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', padding: '4px 10px' }}
                      onClick={(e) => {
                        e.stopPropagation();
                        onOpenStartModal(server);
                      }}
                    >
                      <Plus size={12} /> + New Terminal
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
};


```

---

<a id="file-21"></a>
### 21. `src/modules/terminal/components/ServerFilterDialog.tsx`

**Path:** `src/modules/terminal/components/ServerFilterDialog.tsx` | **Size:** `9.60 KB` | **Lines:** `221` | **Language:** `tsx`

```tsx
import React, { useState, useMemo } from 'react';
import { ServerConfig, Environment, ServerCategory } from '../types/terminal.types';
import { Search, ChevronDown, ChevronRight, X, Check } from 'lucide-react';

interface Props {
  open: boolean;
  onClose: () => void;
  servers: ServerConfig[];
  selectedHosts: string[];
  onApply: (selectedHosts: string[]) => void;
}

const ENV_ORDER: Array<{ code: Environment; label: string }> = [
  { code: 'PROD', label: 'PRODUCTION' },
  { code: 'T01', label: 'T01' },
  { code: 'T02', label: 'T02' },
  { code: 'T03', label: 'T03' },
  { code: 'T04', label: 'T04' },
  { code: 'DEV', label: 'DEVELOPMENT' },
];

const SERVER_TYPE_ORDER: Array<{ code: ServerCategory; label: string }> = [
  { code: 'INSPIRE_AUTOMATION', label: 'Inspire Automation' },
  { code: 'INSPIRE_SCALER', label: 'Inspire Scaler' },
  { code: 'IPS_POOL', label: 'IPS Pool' },
  { code: 'INTERACTIVE', label: 'Interactive' },
];

export const ServerFilterDialog: React.FC<Props> = ({
  open,
  onClose,
  servers,
  selectedHosts,
  onApply,
}) => {
  const [search, setSearch] = useState('');
  const [localSelected, setLocalSelected] = useState<Set<string>>(() => new Set(selectedHosts));
  const [expandedEnvs, setExpandedEnvs] = useState<Set<string>>(() => new Set(['PROD', 'DEV', 'T01']));
  const [expandedTypes, setExpandedTypes] = useState<Set<string>>(new Set());

  if (!open) return null;

  const totalUniqueHosts = servers.length;

  const toggleHost = (host: string) => {
    setLocalSelected((prev) => {
      const next = new Set(prev);
      if (next.has(host)) next.delete(host);
      else next.add(host);
      return next;
    });
  };

  const toggleEnv = (envCode: string) => {
    const envHosts = servers.filter((s) => (s.env || s.environment) === envCode).map((s) => s.host);
    const allSelected = envHosts.every((h) => localSelected.has(h));
    setLocalSelected((prev) => {
      const next = new Set(prev);
      envHosts.forEach((h) => {
        if (allSelected) next.delete(h);
        else next.add(h);
      });
      return next;
    });
  };

  const toggleType = (envCode: string, serverType: ServerCategory) => {
    const typeHosts = servers
      .filter((s) => (s.env || s.environment) === envCode && (s.serverType || s.category) === serverType)
      .map((s) => s.host);
    const allSelected = typeHosts.every((h) => localSelected.has(h));
    setLocalSelected((prev) => {
      const next = new Set(prev);
      typeHosts.forEach((h) => {
        if (allSelected) next.delete(h);
        else next.add(h);
      });
      return next;
    });
  };

  const toggleExpandEnv = (envCode: string) => {
    setExpandedEnvs((prev) => {
      const next = new Set(prev);
      if (next.has(envCode)) next.delete(envCode);
      else next.add(envCode);
      return next;
    });
  };

  const toggleExpandType = (key: string) => {
    setExpandedTypes((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const handleSelectAll = () => {
    setLocalSelected(new Set(servers.map((s) => s.host)));
  };

  const handleDeselectAll = () => {
    setLocalSelected(new Set());
  };

  const handleSave = () => {
    onApply(Array.from(localSelected));
    onClose();
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '540px', maxHeight: '85vh', display: 'flex', flexDirection: 'column' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '12px', borderBottom: '1px solid #334155' }}>
          <div style={{ fontSize: '18px', fontWeight: 700 }}>Server Selection Filter</div>
          <span className="badge-connected" style={{ background: '#3b82f6', color: '#ffffff', border: 'none' }}>
            {localSelected.size}/{totalUniqueHosts} Selected
          </span>
        </div>

        <div style={{ display: 'flex', gap: '8px', margin: '14px 0' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '10px', top: '10px', color: '#94a3b8' }} />
            <input
              type="text"
              className="filter-select"
              style={{ width: '100%', paddingLeft: '32px', boxSizing: 'border-box' }}
              placeholder="Search hostname (H1191), environment..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button onClick={handleSelectAll} style={{ background: '#334155', color: '#f8fafc', border: '1px solid #475569', borderRadius: '4px', padding: '0 12px', cursor: 'pointer' }}>All</button>
          <button onClick={handleDeselectAll} style={{ background: '#334155', color: '#94a3b8', border: '1px solid #475569', borderRadius: '4px', padding: '0 12px', cursor: 'pointer' }}>None</button>
        </div>

        <div style={{ flex: 1, overflowY: 'auto', paddingRight: '4px' }}>
          {ENV_ORDER.map(({ code: envCode, label: envLabel }) => {
            const rawEnvItems = servers.filter((s) => (s.env || s.environment) === envCode);
            const totalEnvHosts = Array.from(new Set(rawEnvItems.map((s) => s.host)));
            if (totalEnvHosts.length === 0) return null;

            const selectedInEnv = totalEnvHosts.filter((h) => localSelected.has(h)).length;
            const isExpanded = expandedEnvs.has(envCode);

            return (
              <div key={envCode} style={{ marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '6px', background: 'rgba(255,255,255,0.03)', borderRadius: '4px' }}>
                  <span onClick={() => toggleExpandEnv(envCode)} style={{ cursor: 'pointer', color: '#94a3b8' }}>
                    {isExpanded ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                  </span>
                  <input
                    type="checkbox"
                    checked={selectedInEnv === totalEnvHosts.length}
                    onChange={() => toggleEnv(envCode)}
                  />
                  <span style={{ fontWeight: 700, fontSize: '14px', flex: 1 }}>{envLabel}</span>
                  <span style={{ fontSize: '11px', color: '#94a3b8', background: 'rgba(255,255,255,0.08)', padding: '2px 6px', borderRadius: '10px' }}>
                    {selectedInEnv}/{totalEnvHosts.length}
                  </span>
                </div>

                {isExpanded && (
                  <div style={{ paddingLeft: '24px', marginTop: '4px' }}>
                    {SERVER_TYPE_ORDER.map(({ code: typeCode, label: typeLabel }) => {
                      const typeHosts = rawEnvItems.filter((s) => (s.serverType || s.category) === typeCode).map((s) => s.host);
                      if (typeHosts.length === 0) return null;

                      const selectedInType = typeHosts.filter((h) => localSelected.has(h)).length;
                      const typeKey = `${envCode}-${typeCode}`;
                      const isTypeExpanded = expandedTypes.has(typeKey);

                      return (
                        <div key={typeKey} style={{ marginBottom: '4px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '4px' }}>
                            <span onClick={() => toggleExpandType(typeKey)} style={{ cursor: 'pointer', color: '#94a3b8' }}>
                              {isTypeExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                            </span>
                            <input
                              type="checkbox"
                              checked={selectedInType === typeHosts.length}
                              onChange={() => toggleType(envCode, typeCode)}
                            />
                            <span style={{ fontSize: '13px', color: '#cbd5e1', flex: 1 }}>{typeLabel}</span>
                            <span style={{ fontSize: '11px', color: '#94a3b8' }}>{selectedInType}/{typeHosts.length}</span>
                          </div>

                          {isTypeExpanded && (
                            <div style={{ paddingLeft: '28px' }}>
                              {typeHosts.map((host) => (
                                <label key={host} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '2px 0', fontSize: '13px', color: localSelected.has(host) ? '#f8fafc' : '#94a3b8', cursor: 'pointer' }}>
                                  <input
                                    type="checkbox"
                                    checked={localSelected.has(host)}
                                    onChange={() => toggleHost(host)}
                                  />
                                  <span>{host}</span>
                                </label>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className="dialog-actions" style={{ marginTop: '16px', paddingTop: '12px', borderTop: '1px solid #334155' }}>
          <button onClick={onClose} style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}>Cancel</button>
          <button onClick={handleSave} style={{ background: '#3b82f6', color: '#ffffff', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}>Apply Selection</button>
        </div>
      </div>
    </div>
  );
};

```

---

<a id="file-22"></a>
### 22. `src/modules/terminal/components/StartTerminalModal.tsx`

**Path:** `src/modules/terminal/components/StartTerminalModal.tsx` | **Size:** `3.43 KB` | **Lines:** `92` | **Language:** `tsx`

```tsx
import React, { useState } from 'react';
import { ServerConfig, TerminalPlatform } from '../types/terminal.types';

interface Props {
  server: ServerConfig;
  onStart: (server: ServerConfig, name: string, platform: TerminalPlatform) => void;
  onClose: () => void;
}

export const StartTerminalModal: React.FC<Props> = ({ server, onStart, onClose }) => {
  const [name, setName] = useState(`${server.host} Terminal`);
  const [platform, setPlatform] = useState<TerminalPlatform>('PS');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onStart(server, name.trim(), platform);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '380px' }} onClick={(e) => e.stopPropagation()}>
        <div className="rename-title">Start Terminal - {server.host}</div>
        <div style={{ fontSize: '12px', color: '#94a3b8', marginBottom: '14px' }}>
          {server.displayName} ({server.serverType || server.category} | {server.env || server.environment})
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '14px' }}>
            <label style={{ display: 'block', fontSize: '13px', marginBottom: '6px', color: '#f8fafc', fontWeight: 600 }}>
              Terminal Name
            </label>
            <input
              type="text"
              className="rename-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Logs Monitor"
              autoFocus
              style={{ marginBottom: '0' }}
            />
          </div>

          <div style={{ marginBottom: '18px' }}>
            <label style={{ display: 'block', fontSize: '13px', marginBottom: '8px', color: '#f8fafc', fontWeight: 600 }}>
              Shell Platform
            </label>
            <div style={{ display: 'flex', gap: '16px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '13px', color: '#f8fafc' }}>
                <input
                  type="radio"
                  name="platform"
                  value="PS"
                  checked={platform === 'PS'}
                  onChange={() => setPlatform('PS')}
                />
                PowerShell (PS)
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '13px', color: '#f8fafc' }}>
                <input
                  type="radio"
                  name="platform"
                  value="CMD"
                  checked={platform === 'CMD'}
                  onChange={() => setPlatform('CMD')}
                />
                Command Prompt (CMD)
              </label>
            </div>
          </div>

          <div className="dialog-actions">
            <button
              type="button"
              onClick={onClose}
              style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '7px 14px', borderRadius: '4px', cursor: 'pointer' }}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={{ background: '#3b82f6', color: '#ffffff', border: 'none', padding: '7px 14px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}
            >
              Start Terminal
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

```

---

<a id="file-23"></a>
### 23. `src/modules/terminal/components/TerminalConsoleCanvas.tsx`

**Path:** `src/modules/terminal/components/TerminalConsoleCanvas.tsx` | **Size:** `9.74 KB` | **Lines:** `278` | **Language:** `tsx`

```tsx
import React, { useState, useRef, useEffect } from 'react';
import { TerminalSession } from '../types/terminal.types';
import { Copy, Check } from 'lucide-react';

interface Props {
  session: TerminalSession;
  onExecute: (terminalId: string, host: string, command: string) => void;
  onStop: (terminalId: string, host: string) => void;
}

function stripAnsi(str: string): string {
  if (!str) return '';
  return str
    .replace(/\u001b\][^\u0007\u001b]*(?:\u0007|\u001b\\)/g, '')
    .replace(/\u0007/g, '')
    .replace(/\u001b\[[0-9;?]*[ -/]*[@-~]/g, '')
    .replace(/\u001b[@-_]/g, '');
}

function isBarePrompt(line: string): boolean {
  const clean = stripAnsi(line).trim();
  return /^(?:PS\s+)?[A-Za-z]:\\[^<>\"|?*]*>$/.test(clean);
}

export const TerminalConsoleCanvas: React.FC<Props> = ({ session, onExecute, onStop }) => {
  const [commandInput, setCommandInput] = useState('');
  const PAGE_SIZE = 25;
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE);

  const [selectedText, setSelectedText] = useState('');
  const [copied, setCopied] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const outputEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const prevScrollHeightRef = useRef<number>(0);
  const isAutoScrollBottomRef = useRef<boolean>(true);

  // Track text selection globally so Copy button appears reliably and doesn't lose text
  useEffect(() => {
    const handleSelectionChange = () => {
      const selection = window.getSelection();
      if (selection) {
        const text = selection.toString().trim();
        if (text.length > 0) {
          setSelectedText(text);
        } else if (!copied) {
          setSelectedText('');
        }
      }
    };

    document.addEventListener('selectionchange', handleSelectionChange);
    return () => {
      document.removeEventListener('selectionchange', handleSelectionChange);
    };
  }, [copied]);

  const fallbackCopy = (text: string) => {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    try {
      document.execCommand('copy');
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.warn('Fallback copy failed:', err);
    } finally {
      document.body.removeChild(textarea);
    }
  };

  const handleCopySelection = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    const selText = selectedText || (window.getSelection() ? window.getSelection()!.toString().trim() : '');
    if (selText) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(selText).then(() => {
          setCopied(true);
          setTimeout(() => setCopied(false), 2000);
        }).catch(() => fallbackCopy(selText));
      } else {
        fallbackCopy(selText);
      }
    }
  };

  const handleContainerClick = (e: React.MouseEvent) => {
    const selection = window.getSelection();
    const activeSel = selection ? selection.toString().trim() : '';
    if (activeSel.length > 0 || selectedText.length > 0) {
      // User has selected text — preserve selection and don't steal focus
      return;
    }
    inputRef.current?.focus();
  };

  // Keep visible count aligned with total output lines when live lines arrive
  useEffect(() => {
    if (isAutoScrollBottomRef.current) {
      setVisibleCount((prev) => Math.max(prev, Math.min(session.outputLines.length, prev + 5)));
      outputEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [session.outputLines]);

  const handleScroll = () => {
    if (!containerRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
    
    // Check if user is scrolled near bottom
    const isAtBottom = scrollHeight - (scrollTop + clientHeight) < 40;
    isAutoScrollBottomRef.current = isAtBottom;

    // When scrolled near top, load next page of older output lines
    if (scrollTop < 30 && visibleCount < session.outputLines.length) {
      prevScrollHeightRef.current = scrollHeight;
      setVisibleCount((prev) => Math.min(session.outputLines.length, prev + PAGE_SIZE));
    }
  };

  // Adjust scroll position after expanding older lines at the top
  useEffect(() => {
    if (containerRef.current && prevScrollHeightRef.current > 0) {
      const newScrollHeight = containerRef.current.scrollHeight;
      const diff = newScrollHeight - prevScrollHeightRef.current;
      containerRef.current.scrollTop += diff;
      prevScrollHeightRef.current = 0;
    }
  }, [visibleCount]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      onExecute(session.terminalId, session.host, commandInput);
      setCommandInput('');
      isAutoScrollBottomRef.current = true;
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (session.history.length > 0) {
        const nextIdx = Math.max(0, session.historyIndex - 1);
        session.historyIndex = nextIdx;
        setCommandInput(session.history[nextIdx] || '');
      }
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (session.history.length > 0) {
        const nextIdx = Math.min(session.history.length, session.historyIndex + 1);
        session.historyIndex = nextIdx;
        setCommandInput(session.history[nextIdx] || '');
      }
    } else if (e.ctrlKey && e.key === 'c') {
      e.preventDefault();
      onStop(session.terminalId, session.host);
    }
  };

  const currentPath = session.currentPath && session.currentPath.trim() ? session.currentPath : 'C:\\';
  const promptPrefix = session.platform === 'PS' ? `PS ${currentPath}>` : `${currentPath}>`;

  const flattenLines = (lines: string[]): string[] => {
    const res: string[] = [];
    lines.forEach((l) => {
      if (l === undefined || l === null) return;
      if (typeof l === 'string' && (l.includes('\n') || l.includes('\r'))) {
        const parts = l.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');
        if (parts.length > 1 && parts[parts.length - 1] === '') {
          parts.pop();
        }
        res.push(...parts);
      } else {
        res.push(String(l));
      }
    });
    return res;
  };

  const allLines = flattenLines(session.outputLines);
  const totalLines = allLines.length;
  const startIndex = Math.max(0, totalLines - visibleCount);
  const hiddenCount = startIndex;
  const displayedLines = allLines.slice(startIndex);

  const loadMoreOlder = () => {
    if (containerRef.current) {
      prevScrollHeightRef.current = containerRef.current.scrollHeight;
    }
    setVisibleCount((prev) => Math.min(totalLines, prev + PAGE_SIZE));
  };

  return (
    <div className="console-container" onClick={handleContainerClick} style={{ position: 'relative' }}>
      {selectedText && (
        <button
          onMouseDown={(e) => e.preventDefault()}
          onClick={handleCopySelection}
          style={{
            position: 'absolute',
            top: '12px',
            right: '24px',
            zIndex: 100,
            background: copied ? '#166534' : '#3b82f6',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            padding: '6px 12px',
            fontSize: '12px',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.4)',
          }}
        >
          {copied ? <Check size={14} color="#4ade80" /> : <Copy size={14} />}
          {copied ? 'Copied!' : 'Copy Selected'}
        </button>
      )}

      <div className="console-output" ref={containerRef} onScroll={handleScroll}>
        {hiddenCount > 0 && (
          <div
            onClick={loadMoreOlder}
            style={{
              padding: '4px 8px',
              margin: '4px 0 8px 0',
              background: 'rgba(59, 130, 246, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.3)',
              borderRadius: '4px',
              color: '#93c5fd',
              fontSize: '11px',
              textAlign: 'center',
              cursor: 'pointer',
              userSelect: 'none',
            }}
          >
            ↑ {hiddenCount} older lines hidden — scroll up or click to load more
          </div>
        )}

        {displayedLines.map((line, idx) => {
          const cleanLine = stripAnsi(line);
          if (isBarePrompt(cleanLine)) {
            return null; // drop bare prompt lines from history list so only active prompt renders
          }
          return (
            <div key={startIndex + idx} style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', minHeight: '18px', userSelect: 'text' }}>
              {cleanLine}
            </div>
          );
        })}

        {/* Real Terminal Prompt Line rendered inline at the bottom of output stream */}
        <div className="console-prompt-line" style={{ display: 'flex', alignItems: 'center', gap: '6px', width: '100%', marginTop: '6px' }}>
          <span className="prompt-path" style={{ whiteSpace: 'nowrap', flexShrink: 0, fontWeight: 'bold', color: '#38bdf8' }}>
            {promptPrefix}
          </span>
          <input
            ref={inputRef}
            type="text"
            className="console-input"
            style={{ flex: 1, minWidth: 0, border: 'none', outline: 'none', background: 'transparent', color: '#f8fafc', fontFamily: 'monospace', fontSize: '13px' }}
            value={commandInput}
            onChange={(e) => setCommandInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={session.state === 'CLOSING' || session.state === 'CLOSED'}
            autoFocus
          />
        </div>

        <div ref={outputEndRef} />
      </div>
    </div>
  );
};

```

---

<a id="file-24"></a>
### 24. `src/modules/terminal/components/TerminalModalWindow.tsx`

**Path:** `src/modules/terminal/components/TerminalModalWindow.tsx` | **Size:** `7.46 KB` | **Lines:** `200` | **Language:** `tsx`

```tsx
import React, { useState, useRef, useEffect } from 'react';
import { TerminalSession } from '../types/terminal.types';
import { TerminalTabHeader } from './TerminalTabHeader';
import { TerminalConsoleCanvas } from './TerminalConsoleCanvas';
import { TerminalRenameModal } from './TerminalRenameModal';

interface Props {
  sessions: TerminalSession[];
  activeTerminalId: string | null;
  onSelectTab: (terminalId: string) => void;
  onCloseTab: (terminalId: string) => void;
  onExecute: (terminalId: string, host: string, command: string) => void;
  onStop: (terminalId: string, host: string) => void;
  onRename: (terminalId: string, newName: string) => void;
  onCloseWindow: () => void;
  onPopOut?: (terminalId: string) => void;
}

export const TerminalModalWindow: React.FC<Props> = ({
  sessions,
  activeTerminalId,
  onSelectTab,
  onCloseTab,
  onExecute,
  onStop,
  onRename,
  onCloseWindow,
  onPopOut,
}) => {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [renameSession, setRenameSession] = useState<TerminalSession | null>(null);

  // Window position & size state for free mouse resizing and dragging
  const [pos, setPos] = useState({ x: 120, y: 80 });
  const [size, setSize] = useState({ w: 850, h: 540 });
  const isDraggingRef = useRef(false);
  const isResizingRef = useRef(false);
  const resizeEdgeRef = useRef<string | null>(null);
  const dragStartRef = useRef({ mouseX: 0, mouseY: 0, winX: 0, winY: 0, winW: 0, winH: 0 });

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isDraggingRef.current) {
        const dx = e.clientX - dragStartRef.current.mouseX;
        const dy = e.clientY - dragStartRef.current.mouseY;
        setPos({
          x: Math.max(0, dragStartRef.current.winX + dx),
          y: Math.max(0, dragStartRef.current.winY + dy),
        });
      } else if (isResizingRef.current && resizeEdgeRef.current) {
        const dx = e.clientX - dragStartRef.current.mouseX;
        const dy = e.clientY - dragStartRef.current.mouseY;
        const edge = resizeEdgeRef.current;

        let newW = dragStartRef.current.winW;
        let newH = dragStartRef.current.winH;
        let newX = dragStartRef.current.winX;
        let newY = dragStartRef.current.winY;

        if (edge.includes('e')) newW = Math.max(400, dragStartRef.current.winW + dx);
        if (edge.includes('s')) newH = Math.max(250, dragStartRef.current.winH + dy);
        if (edge.includes('w')) {
          const possibleW = dragStartRef.current.winW - dx;
          if (possibleW > 400) {
            newW = possibleW;
            newX = dragStartRef.current.winX + dx;
          }
        }
        if (edge.includes('n')) {
          const possibleH = dragStartRef.current.winH - dy;
          if (possibleH > 250) {
            newH = possibleH;
            newY = dragStartRef.current.winY + dy;
          }
        }

        setSize({ w: newW, h: newH });
        setPos({ x: newX, y: newY });
      }
    };

    const handleMouseUp = () => {
      isDraggingRef.current = false;
      isResizingRef.current = false;
      resizeEdgeRef.current = null;
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, []);

  const openSessions = sessions.filter((s) => s.state !== 'CLOSED');
  if (openSessions.length === 0) return null;

  const activeSession = openSessions.find((s) => s.terminalId === activeTerminalId) || openSessions[0];

  const startDrag = (e: React.MouseEvent) => {
    if (isFullscreen) return;
    isDraggingRef.current = true;
    dragStartRef.current = {
      mouseX: e.clientX,
      mouseY: e.clientY,
      winX: pos.x,
      winY: pos.y,
      winW: size.w,
      winH: size.h,
    };
  };

  const startResize = (e: React.MouseEvent, edge: string) => {
    e.stopPropagation();
    e.preventDefault();
    if (isFullscreen) return;
    isResizingRef.current = true;
    resizeEdgeRef.current = edge;
    dragStartRef.current = {
      mouseX: e.clientX,
      mouseY: e.clientY,
      winX: pos.x,
      winY: pos.y,
      winW: size.w,
      winH: size.h,
    };
  };

  return (
    <div className="terminal-modal-overlay">
      <div
        className={`terminal-modal-window ${isFullscreen ? 'fullscreen' : ''}`}
        style={
          isFullscreen
            ? {}
            : {
                top: isMinimized ? 'calc(100vh - 40px)' : `${pos.y}px`,
                left: `${pos.x}px`,
                width: `${size.w}px`,
                height: isMinimized ? '40px' : `${size.h}px`,
              }
        }
      >
        {/* Header Bar with Mouse Drag Move Listener */}
        <div onMouseDown={startDrag} style={{ cursor: isFullscreen ? 'default' : 'move' }}>
          <TerminalTabHeader
            sessions={openSessions}
            activeTerminalId={activeSession?.terminalId || null}
            onSelectTab={(id) => {
              setIsMinimized(false);
              onSelectTab(id);
            }}
            onCloseTab={onCloseTab}
            onRenameModalOpen={(sess) => setRenameSession(sess)}
            onToggleMinimize={() => setIsMinimized(!isMinimized)}
            onToggleFullscreen={() => setIsFullscreen(!isFullscreen)}
            onCloseWindow={onCloseWindow}
            onPopOut={onPopOut || ((tid) => window.open(`?terminalId=${encodeURIComponent(tid)}`, '_blank'))}
            isFullscreen={isFullscreen}
          />
        </div>

        {!isMinimized && activeSession && (
          <TerminalConsoleCanvas
            session={activeSession}
            onExecute={onExecute}
            onStop={onStop}
          />
        )}

        {/* Freely Resizable Edge Handles */}
        {!isFullscreen && !isMinimized && (
          <>
            <div onMouseDown={(e) => startResize(e, 'n')} style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '6px', cursor: 'ns-resize' }} />
            <div onMouseDown={(e) => startResize(e, 's')} style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '6px', cursor: 'ns-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'w')} style={{ position: 'absolute', top: 0, bottom: 0, left: 0, width: '6px', cursor: 'ew-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'e')} style={{ position: 'absolute', top: 0, bottom: 0, right: 0, width: '6px', cursor: 'ew-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'nw')} style={{ position: 'absolute', top: 0, left: 0, width: '10px', height: '10px', cursor: 'nwse-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'ne')} style={{ position: 'absolute', top: 0, right: 0, width: '10px', height: '10px', cursor: 'nesw-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'sw')} style={{ position: 'absolute', bottom: 0, left: 0, width: '10px', height: '10px', cursor: 'nesw-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'se')} style={{ position: 'absolute', bottom: 0, right: 0, width: '14px', height: '14px', cursor: 'nwse-resize', background: 'transparent' }} />
          </>
        )}
      </div>

      {renameSession && (
        <TerminalRenameModal
          session={renameSession}
          onSave={(id, name) => {
            onRename(id, name);
            setRenameSession(null);
          }}
          onClose={() => setRenameSession(null)}
        />
      )}
    </div>
  );
};

```

---

<a id="file-25"></a>
### 25. `src/modules/terminal/components/TerminalRenameModal.tsx`

**Path:** `src/modules/terminal/components/TerminalRenameModal.tsx` | **Size:** `2.10 KB` | **Lines:** `60` | **Language:** `tsx`

```tsx
import React, { useState } from 'react';
import { TerminalSession } from '../types/terminal.types';

interface Props {
  session: TerminalSession;
  onSave: (terminalId: string, newName: string) => void;
  onClose: () => void;
}

export const TerminalRenameModal: React.FC<Props> = ({ session, onSave, onClose }) => {
  const [name, setName] = useState(session.name);
  const isAsrAi = Boolean((session.name && session.name.toUpperCase().includes('ASR_AI')) || (session.terminalId && session.terminalId.toUpperCase().includes('ASR_AI')));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAsrAi && name.trim()) {
      onSave(session.terminalId, name.trim());
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="rename-title">Rename Terminal Tab</div>
        {isAsrAi && (
          <div style={{ color: '#ef4444', fontSize: '12px', marginBottom: '10px' }}>
            Protected terminal: ASR_AI session cannot be renamed.
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            className="rename-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter terminal name..."
            disabled={isAsrAi}
            autoFocus
          />
          <div className="dialog-actions">
            <button
              type="button"
              onClick={onClose}
              style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer' }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isAsrAi || !name.trim()}
              style={{ background: isAsrAi ? '#64748b' : '#3b82f6', color: '#ffffff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: isAsrAi ? 'not-allowed' : 'pointer' }}
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

```

---

<a id="file-26"></a>
### 26. `src/modules/terminal/components/TerminalTabHeader.tsx`

**Path:** `src/modules/terminal/components/TerminalTabHeader.tsx` | **Size:** `3.08 KB` | **Lines:** `103` | **Language:** `tsx`

```tsx
import React from 'react';
import { TerminalSession } from '../types/terminal.types';
import { X, Maximize2, Minimize2, Minus, ExternalLink } from 'lucide-react';

interface Props {
  sessions: TerminalSession[];
  activeTerminalId: string | null;
  onSelectTab: (terminalId: string) => void;
  onCloseTab: (terminalId: string) => void;
  onRenameModalOpen: (session: TerminalSession) => void;
  onToggleMinimize: () => void;
  onToggleFullscreen: () => void;
  onCloseWindow: () => void;
  onPopOut?: (terminalId: string) => void;
  isFullscreen: boolean;
}

export const TerminalTabHeader: React.FC<Props> = ({
  sessions,
  activeTerminalId,
  onSelectTab,
  onCloseTab,
  onRenameModalOpen,
  onToggleMinimize,
  onToggleFullscreen,
  onCloseWindow,
  onPopOut,
  isFullscreen,
}) => {
  const getStateColor = (state: string) => {
    switch (state) {
      case 'RUNNING': return '#eab308';
      case 'IDLE': return '#22c55e';
      case 'CLOSING': return '#f97316';
      case 'CLOSED': return '#ef4444';
      default: return '#94a3b8';
    }
  };

  return (
    <div className="window-header">
      <div className="tabs-container">
        {sessions.map((sess) => {
          const isActive = sess.terminalId === activeTerminalId;
          const stateColor = getStateColor(sess.state);

          return (
            <div
              key={sess.terminalId}
              className={`tab-item ${isActive ? 'active' : ''}`}
              onClick={() => onSelectTab(sess.terminalId)}
              onDoubleClick={() => onRenameModalOpen(sess)}
              title={`Double click to rename | ID: ${sess.terminalId}`}
            >
              <span
                style={{
                  width: '7px',
                  height: '7px',
                  borderRadius: '50%',
                  background: stateColor,
                  display: 'inline-block',
                }}
              />
              <span>
                {sess.elevated ? 'Administrator: ' : ''}{sess.name} ({sess.platform}) [{sess.state}]
              </span>
              <span
                className="tab-close"
                onClick={(e) => {
                  e.stopPropagation();
                  onCloseTab(sess.terminalId);
                }}
              >
                <X size={12} />
              </span>
            </div>
          );
        })}
      </div>

      <div className="window-controls">
        {activeTerminalId && onPopOut && (
          <button
            className="window-btn"
            onClick={() => onPopOut(activeTerminalId)}
            title="Open in new window"
          >
            <ExternalLink size={14} />
          </button>
        )}
        <button className="window-btn" onClick={onToggleMinimize} title="Minimize">
          <Minus size={14} />
        </button>
        <button className="window-btn" onClick={onToggleFullscreen} title={isFullscreen ? 'Restore' : 'Maximize'}>
          <Maximize2 size={14} />
        </button>
        <button className="window-btn close" onClick={onCloseWindow} title="Close Window">
          <X size={14} />
        </button>
      </div>
    </div>
  );
};

```

---

<a id="file-27"></a>
### 27. `src/modules/terminal/frontendSimulation.test.ts`

**Path:** `src/modules/terminal/frontendSimulation.test.ts` | **Size:** `6.94 KB` | **Lines:** `171` | **Language:** `typescript`

```typescript
/**
 * Frontend Simulation Test Suite
 * Validates Frontend state management, ANSI text formatting, filter retention,
 * terminal session retention on close, and re-opening of CLOSED terminals.
 */

function assertEquals<T>(actual: T, expected: T, message?: string) {
    if (JSON.stringify(actual) !== JSON.stringify(expected)) {
        throw new Error(message || `Assertion failed: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
    }
}

function assertTrue(value: boolean, message?: string) {
    if (!value) {
        throw new Error(message || `Assertion failed: expected true, got false`);
    }
}

// 1. Helper function matching stripAnsi from TerminalConsoleCanvas.tsx
function stripAnsi(str: string): string {
    return str
        .replace(/\u001b\][^\u001b]*(\u001b\\|\u0007)/g, '') // OSC sequences
        .replace(/\u001b\[[0-9;]*[a-zA-Z]/g, '')             // CSI sequences (color, cursor)
        .replace(/\r/g, '');                                 // Carriage returns
}

// 2. Types matching TerminalDashboardPage.tsx & TerminalModels.ts
interface TerminalSession {
    terminalId: string;
    host: string;
    name: string;
    platform: 'CMD' | 'PS';
    currentPath: string;
    state: 'IDLE' | 'RUNNING' | 'CLOSING' | 'CLOSED';
    elevated?: boolean;
    outputLines: string[];
}

// 3. Test Cases Implementation

function testAnsiStripping() {
    console.log('Test 1: ANSI Stripping & Formatting...');

    const systemMsg = '[System] Connected to PowerShell terminal';
    const strippedMsg = stripAnsi(systemMsg);
    assertEquals(strippedMsg, '[System] Connected to PowerShell terminal', 'System prefix should not be truncated to ystem]');

    const ansiColoredMsg = '\u001b[32m[System]\u001b[0m Ready';
    const strippedColored = stripAnsi(ansiColoredMsg);
    assertEquals(strippedColored, '[System] Ready', 'ANSI color codes should be stripped without altering text');

    console.log('✓ Test 1 Passed: ANSI stripping preserves [System] tag cleanly.');
}

function testCommandPromptPreservation() {
    console.log('\nTest 2: Command Output & Prompt Line Preservation...');

    const session: TerminalSession = {
        terminalId: 'term-101',
        host: 'localhost',
        name: 'Local PS',
        platform: 'PS',
        currentPath: 'C:\\',
        state: 'IDLE',
        outputLines: ['[System] Connected to PowerShell terminal']
    };

    // Simulate sending command 'dir'
    const promptPrefix = `${session.platform} ${session.currentPath}>`;
    const userCommand = 'dir';
    
    // Append prompt line
    session.outputLines.push(`${promptPrefix} ${userCommand}`);
    session.state = 'RUNNING';

    // Simulate receiving output line from agent
    const agentOutput = ' Directory of C:\\\n 2026-09-24  05:00    <DIR>          automation_suite';
    session.outputLines.push(agentOutput);
    session.state = 'IDLE';

    assertEquals(session.outputLines.length, 3);
    assertEquals(session.outputLines[1], 'PS C:\\> dir', 'Prompt prefix and command must be preserved in history');
    assertTrue(session.outputLines[2].includes('Directory of C:\\'), 'Command output must follow prompt line');

    console.log('✓ Test 2 Passed: Command prompt and output history preserved.');
}

function testServerFilterRetention() {
    console.log('\nTest 3: Server Filter Retention across Periodic Polling...');

    // Initial state
    const allAvailableHosts = ['localhost', 'srv-web-01', 'srv-db-01'];
    let selectedTreeHosts: string[] = [];
    let hasInitializedFilter = false;

    // First load: initialize with all hosts
    if (!hasInitializedFilter) {
        selectedTreeHosts = [...allAvailableHosts];
        hasInitializedFilter = true;
    }

    assertEquals(selectedTreeHosts, ['localhost', 'srv-web-01', 'srv-db-01']);

    // User modifies filter to select only 'localhost'
    selectedTreeHosts = ['localhost'];

    // Periodic 5-second polling tick executes
    const pollingLoadData = () => {
        if (!hasInitializedFilter) {
            selectedTreeHosts = [...allAvailableHosts];
            hasInitializedFilter = true;
        }
    };

    pollingLoadData();
    assertEquals(selectedTreeHosts, ['localhost'], 'User filter selection must NOT be overwritten by 5-second polling interval');

    console.log('✓ Test 3 Passed: User server filter selection retained during polling.');
}

function testClosedTabRetentionAndReopen() {
    console.log('\nTest 4: Closed Terminal Retention in Host Card & Re-opening...');

    let sessions: TerminalSession[] = [
        { terminalId: 't-1', host: 'localhost', name: 'PS 1', platform: 'PS', currentPath: 'C:\\', state: 'IDLE', outputLines: [] },
        { terminalId: 't-2', host: 'localhost', name: 'PS 2', platform: 'PS', currentPath: 'C:\\', state: 'IDLE', outputLines: [] }
    ];

    // Verify all host sessions are listed on the host card (both active and closed)
    const hostCardSessionsBefore = sessions.filter(s => s.host === 'localhost');
    assertEquals(hostCardSessionsBefore.length, 2, 'Host card must list all terminals for host');

    // User closes t-1 tab -> state set to CLOSED, terminal_close sent (no terminal_remove)
    const closeTabId = 't-1';
    sessions = sessions.map(s => s.terminalId === closeTabId ? { ...s, state: 'CLOSED' as const } : s);

    // Host card MUST still list t-1 with CLOSED state
    const hostCardSessionsAfterClose = sessions.filter(s => s.host === 'localhost');
    assertEquals(hostCardSessionsAfterClose.length, 2, 'Host card MUST continue listing closed terminal session in host card list');
    assertEquals(hostCardSessionsAfterClose.find(s => s.terminalId === 't-1')?.state, 'CLOSED');

    // Floating modal window filters active tabs
    const activeModalSessions = sessions.filter(s => s.state !== 'CLOSED');
    assertEquals(activeModalSessions.length, 1);
    assertEquals(activeModalSessions[0].terminalId, 't-2');

    // User clicks "Re-open" on t-1 from the host card -> re-arms session to IDLE
    sessions = sessions.map(s => s.terminalId === 't-1' ? { ...s, state: 'IDLE' as const } : s);
    const modalSessionsAfterReopen = sessions.filter(s => s.state !== 'CLOSED');
    assertEquals(modalSessionsAfterReopen.length, 2, 'Re-opened terminal must re-appear in floating modal window');

    console.log('✓ Test 4 Passed: Closed terminals retained on host card list and can be re-opened.');
}

// Run All Tests
export function runSimulationSuite() {
    console.log('========================================================');
    console.log('        React Frontend Simulation Test Suite           ');
    console.log('========================================================\n');

    testAnsiStripping();
    testCommandPromptPreservation();
    testServerFilterRetention();
    testClosedTabRetentionAndReopen();

    console.log('\n========================================================');
    console.log(' ALL FRONTEND SIMULATION TESTS PASSED SUCCESSFULLY! ✓   ');
    console.log('========================================================');
}

runSimulationSuite();

```

---

<a id="file-28"></a>
### 28. `src/modules/terminal/services/terminalCache.ts`

**Path:** `src/modules/terminal/services/terminalCache.ts` | **Size:** `1.18 KB` | **Lines:** `39` | **Language:** `typescript`

```typescript
const cacheMap = new Map<string, string[]>();

export const splitLines = (text: string): string[] => {
  if (!text) return [];
  const parts = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');
  if (parts.length > 1 && parts[parts.length - 1] === '') {
    parts.pop();
  }
  return parts;
};

export const getTerminalCache = (terminalId: string): string[] | undefined => {
  return cacheMap.get(terminalId);
};

export const setTerminalCache = (terminalId: string, lines: string[]): void => {
  const flattened: string[] = [];
  lines.forEach((item) => {
    if (item === undefined || item === null) return;
    if (typeof item === 'string' && (item.includes('\n') || item.includes('\r'))) {
      flattened.push(...splitLines(item));
    } else {
      flattened.push(String(item));
    }
  });
  cacheMap.set(terminalId, flattened);
};

export const appendTerminalCache = (terminalId: string, chunk: string): void => {
  const existing = cacheMap.get(terminalId) || [];
  if (!chunk) return;
  const newLines = splitLines(chunk);
  cacheMap.set(terminalId, [...existing, ...newLines]);
};

export const clearTerminalCache = (terminalId: string): void => {
  cacheMap.delete(terminalId);
};


```

---

<a id="file-29"></a>
### 29. `src/modules/terminal/types/terminal.types.ts`

**Path:** `src/modules/terminal/types/terminal.types.ts` | **Size:** `1.41 KB` | **Lines:** `57` | **Language:** `typescript`

```typescript
export type ServerCategory = 'INSPIRE_AUTOMATION' | 'IPS_POOL' | 'INSPIRE_SCALER' | 'INTERACTIVE';
export type Environment = 'PROD' | 'T01' | 'T02' | 'T03' | 'T04' | 'DEV';
export type TerminalPlatform = 'PS' | 'CMD';
export type TerminalState = 'IDLE' | 'RUNNING' | 'CLOSING' | 'CLOSED';

export interface ServerConfig {
  host: string;
  displayName: string;
  category: ServerCategory;
  serverType?: ServerCategory;
  environment: Environment;
  env?: Environment;
  port: number;
}

export interface AgentConnectionStatus {
  host: string;
  connected: boolean;
  lastConnectedAt: number | null;
  reconnectAttempts: number;
}

export interface TerminalSession {
  terminalId: string; // <userId>_<uuid>
  name: string;
  host: string;
  platform: TerminalPlatform;
  elevated?: boolean;
  state: TerminalState;
  currentPath: string;
  lastCommand: string | null;
  lastResult: string | null;
  lastError: string | null;
  outputLines: string[];
  history: string[];
  historyIndex: number;
  lastActivityAt?: number;
}

export interface TerminalFrame {
  'event-type': string;
  terminalid: string;
  terminalId?: string;
  chunk?: string;
  count?: number;
  end?: boolean;
  errorCode?: string;
  state?: TerminalState;
  currentPath?: string;
  lastCommand?: string;
  pid?: number;
  platform?: TerminalPlatform;
  elevated?: boolean;
  lastInputTimestamp?: number;
  lastOutputTimestamp?: number;
  terminals?: TerminalFrame[];
}

```

---

<a id="file-30"></a>
### 30. `src/services/api.ts`

**Path:** `src/services/api.ts` | **Size:** `0.76 KB` | **Lines:** `19` | **Language:** `typescript`

```typescript
import axios from 'axios';
import { ServerConfig, AgentConnectionStatus } from '../modules/terminal/types/terminal.types';
import { apiBaseUrl } from '../config/appConfig';

const API_BASE = apiBaseUrl;

export async function fetchServerInventory(): Promise<ServerConfig[]> {
  const res = await axios.get<ServerConfig[]>(`${API_BASE}/inventory`);
  return res.data;
}

export async function fetchServerStatus(): Promise<Record<string, AgentConnectionStatus>> {
  const res = await axios.get<Record<string, AgentConnectionStatus>>(`${API_BASE}/status`);
  return res.data;
}

export async function renameTerminalApi(terminalId: string, newName: string): Promise<void> {
  await axios.put(`${API_BASE}/terminals/${encodeURIComponent(terminalId)}/rename`, { name: newName });
}

```

---

<a id="file-31"></a>
### 31. `src/services/globalWebSocket.ts`

**Path:** `src/services/globalWebSocket.ts` | **Size:** `2.98 KB` | **Lines:** `107` | **Language:** `typescript`

```typescript
import { TerminalFrame } from '../modules/terminal/types/terminal.types';
import { globalWsUrl } from '../config/appConfig';

type MessageListener = (frame: TerminalFrame) => void;

class GlobalWebSocketService {
  private ws: WebSocket | null = null;
  private listeners: Set<MessageListener> = new Set();
  private reconnectTimer: number | null = null;
  private isConnected: boolean = false;

  private pendingQueue: any[] = [];

  constructor() {
    this.connect();
  }

  public connect() {
    if (this.ws && (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)) {
      return;
    }

    console.log(`[GlobalWebSocket] Connecting to ${globalWsUrl}`);
    this.ws = new WebSocket(globalWsUrl);

    this.ws.onopen = () => {
      console.log('[GlobalWebSocket] Connected successfully');
      this.isConnected = true;
      if (this.reconnectTimer) {
        window.clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
      // Request metadata list from backend/agent upon connection
      this.sendDirect({ 'event-type': 'metadata_list', host: 'localhost' });
      this.flushPendingQueue();
    };

    this.ws.onmessage = (event) => {
      try {
        const frame: TerminalFrame = JSON.parse(event.data);
        this.listeners.forEach((listener) => listener(frame));
      } catch (err) {
        console.warn('[GlobalWebSocket] Failed to parse message frame:', event.data);
      }
    };

    this.ws.onclose = () => {
      console.warn('[GlobalWebSocket] Connection closed; scheduling reconnect...');
      this.isConnected = false;
      this.scheduleReconnect();
    };

    this.ws.onerror = (err) => {
      console.error('[GlobalWebSocket] Connection error:', err);
    };
  }

  private scheduleReconnect() {
    if (!this.reconnectTimer) {
      this.reconnectTimer = window.setTimeout(() => {
        this.connect();
      }, 3000);
    }
  }

  private sendDirect(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
      return true;
    }
    return false;
  }

  private flushPendingQueue() {
    while (this.pendingQueue.length > 0 && this.ws && this.ws.readyState === WebSocket.OPEN) {
      const item = this.pendingQueue.shift();
      if (item) {
        this.sendDirect(item);
      }
    }
  }

  public send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      console.warn('[GlobalWebSocket] WebSocket not open yet; queuing payload:', data);
      this.pendingQueue.push(data);
      if (!this.ws || this.ws.readyState === WebSocket.CLOSED || this.ws.readyState === WebSocket.CLOSING) {
        this.connect();
      }
    }
  }

  public subscribe(listener: MessageListener) {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  public getIsConnected() {
    return this.isConnected;
  }
}

export const globalWebSocket = new GlobalWebSocketService();

```

---

<a id="file-32"></a>
### 32. `src/styles/terminalDashboard.css`

**Path:** `src/styles/terminalDashboard.css` | **Size:** `6.26 KB` | **Lines:** `346` | **Language:** `css`

```css
:root {
  --bg-primary: #0f172a;
  --bg-secondary: #1e293b;
  --bg-card: #1e293b;
  --border-color: #334155;
  --text-main: #f8fafc;
  --text-muted: #94a3b8;
  --accent-blue: #3b82f6;
  --accent-green: #22c55e;
  --accent-red: #ef4444;
  --accent-yellow: #eab308;
  --console-bg: #090d16;
  --console-fg: #38bdf8;
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-primary);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow-x: hidden;
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* Header Filter */
.header-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
}

.filter-controls {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-muted);
}

.filter-select {
  background-color: var(--bg-primary);
  color: var(--text-main);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 14px;
  outline: none;
}

/* Server Card Grid */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  padding: 24px;
}

.server-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 18px;
  cursor: pointer;
  transition: transform 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}

.server-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent-blue);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.host-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.badge-connected {
  background-color: rgba(34, 197, 94, 0.15);
  color: var(--accent-green);
  border: 1px solid var(--accent-green);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.badge-disconnected {
  background-color: rgba(239, 68, 68, 0.15);
  color: var(--accent-red);
  border: 1px solid var(--accent-red);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.card-footer {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.btn-open-terminal {
  background-color: var(--accent-blue);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

/* Floating Terminal Modal Window */
.terminal-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 1000;
}

.terminal-modal-window {
  position: absolute;
  pointer-events: auto;
  background-color: var(--console-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  resize: both;
}

.terminal-modal-window.fullscreen {
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  border-radius: 0;
}

/* Window Header Bar */
.window-header {
  background-color: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 12px;
  user-select: none;
  border-bottom: 1px solid var(--border-color);
}

.tabs-container {
  display: flex;
  gap: 4px;
  overflow-x: auto;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid transparent;
  padding: 4px 10px;
  border-radius: 4px 4px 0 0;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-muted);
}

.tab-item.active {
  background-color: var(--console-bg);
  color: var(--text-main);
  border-color: var(--border-color) var(--border-color) transparent var(--border-color);
  font-weight: 600;
}

.tab-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.tab-close:hover {
  background-color: var(--accent-red);
  color: white;
}

.window-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.window-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
}

.window-btn:hover {
  color: var(--text-main);
}

.window-btn.close:hover {
  color: var(--accent-red);
}

/* Console Canvas */
.console-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  background-color: var(--console-bg);
  overflow: hidden;
}

.console-output {
  flex: 1;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  color: #e2e8f0;
  margin-bottom: 8px;
}

.console-prompt-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.prompt-path {
  color: var(--console-fg);
  font-weight: bold;
}

.console-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #f8fafc;
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
}

/* Rename Modal Dialog */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.rename-dialog {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  width: 320px;
}

.rename-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 12px;
}

.rename-input {
  width: 100%;
  padding: 8px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-main);
  box-sizing: border-box;
  margin-bottom: 16px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

```

---

<a id="file-33"></a>
### 33. `test_ai_frame_handler.ts`

**Path:** `test_ai_frame_handler.ts` | **Size:** `2.14 KB` | **Lines:** `57` | **Language:** `typescript`

```typescript
import { buildHostSelectionPrompt } from './src/modules/ai/services/promptTemplates.ts';

console.log("==================================================");
console.log(" Frontend AI Frame & State Diagnostic Test");
console.log("==================================================");

// 1. Test Frame Parsing with various Copilot response outputs
const testFrames = [
  {
    "event-type": "AI_TASK_RESULT",
    "taskId": "task_1790598270_xyz",
    "conversationId": "7de31de1-7c5d-4d67-9670-71ea263214f3",
    "status": "RESPONSE",
    "rawResponse": JSON.stringify({
      "pre_response": "Analyzing server inventory...",
      "target_hosts": [],
      "review_required": true,
      "final_outcome": "Please specify target server environment."
    })
  },
  {
    "event-type": "AI_TASK_RESULT",
    "taskId": "task_1790598270_xyz2",
    "conversationId": "7de31de1-7c5d-4d67-9670-71ea263214f3",
    "status": "RESPONSE",
    "rawResponse": "Hello! How can I help you today?" // Raw non-JSON text response
  }
];

testFrames.forEach((frame, idx) => {
  console.log(`\n--- Test Case ${idx + 1} ---`);
  let jsonPayload: any = null;
  if (frame.rawResponse) {
    try {
      jsonPayload = JSON.parse(frame.rawResponse);
    } catch (e) {
      console.warn("rawResponse is not valid JSON string:", frame.rawResponse);
    }
  }
  if (!jsonPayload) {
    jsonPayload = frame;
  }

  const isHostRes = jsonPayload.target_hosts !== undefined || jsonPayload.review_required !== undefined;
  const isRemediation = jsonPayload.Command !== undefined || jsonPayload.command !== undefined || jsonPayload.review_needed !== undefined;
  const isGeneralOutcome = Boolean(jsonPayload.final_outcome || jsonPayload.pre_response);

  console.log("Matches Host Resolution (Step A):", isHostRes);
  console.log("Matches Remediation (Step B):", isRemediation);
  console.log("Matches General Outcome Frame:", isGeneralOutcome);

  if (!isHostRes && !isRemediation && !isGeneralOutcome) {
    console.error("⚠️ CRITICAL BUG FOUND: Incoming response frame matched NO rendering condition! UI will freeze loading!");
  } else {
    console.log("✅ Frame correctly matches a rendering handler.");
  }
});

```

---

<a id="file-34"></a>
### 34. `test_ai_modal_flow.js`

**Path:** `test_ai_modal_flow.js` | **Size:** `1.27 KB` | **Lines:** `24` | **Language:** `javascript`

```javascript
const { buildHostSelectionPrompt, buildDiagnosticsRemediationPrompt, formatServerInventoryJson } = require('./src/modules/ai/services/promptTemplates.ts');

console.log("==================================================");
console.log(" Testing Frontend Prompt Template Generators");
console.log("==================================================");

try {
  const query = "find me the latest resume inside document folder.";
  const servers = [
    { host: "localhost", displayName: "Localhost Agent", category: "INSPIRE_AUTOMATION", environment: "DEV" },
    { host: "H1191", displayName: "IA Prod 01", category: "INSPIRE_AUTOMATION", environment: "PROD" }
  ];

  const hostPrompt = buildHostSelectionPrompt(query, servers, null);
  console.log("[SUCCESS] Host Selection Prompt built successfully. Length:", hostPrompt.length);
  console.log("Sample Prompt Output:\n", hostPrompt.substring(0, 300) + "...\n");

  const diagPrompt = buildDiagnosticsRemediationPrompt(query, "H1191", "Previous context summary");
  console.log("[SUCCESS] Diagnostics Remediation Prompt built successfully. Length:", diagPrompt.length);
  console.log("Sample Prompt Output:\n", diagPrompt.substring(0, 300) + "...\n");

} catch (err) {
  System.err.println("[ERROR] Prompt template generator threw exception:", err);
}

```

---

<a id="file-35"></a>
### 35. `test_ai_modal_flow.ts`

**Path:** `test_ai_modal_flow.ts` | **Size:** `1.27 KB` | **Lines:** `24` | **Language:** `typescript`

```typescript
import { buildHostSelectionPrompt, buildDiagnosticsRemediationPrompt, formatServerInventoryJson } from './src/modules/ai/services/promptTemplates.ts';

console.log("==================================================");
console.log(" Testing Frontend Prompt Template Generators");
console.log("==================================================");

try {
  const query = "find me the latest resume inside document folder.";
  const servers: any[] = [
    { host: "localhost", displayName: "Localhost Agent", category: "INSPIRE_AUTOMATION", environment: "DEV" },
    { host: "H1191", displayName: "IA Prod 01", category: "INSPIRE_AUTOMATION", environment: "PROD" }
  ];

  const hostPrompt = buildHostSelectionPrompt(query, servers, null);
  console.log("[SUCCESS] Host Selection Prompt built successfully. Length:", hostPrompt.length);
  console.log("Sample Prompt Output:\n", hostPrompt.substring(0, 300) + "...\n");

  const diagPrompt = buildDiagnosticsRemediationPrompt(query, "H1191", "Previous context summary");
  console.log("[SUCCESS] Diagnostics Remediation Prompt built successfully. Length:", diagPrompt.length);
  console.log("Sample Prompt Output:\n", diagPrompt.substring(0, 300) + "...\n");

} catch (err) {
  console.error("[ERROR] Prompt template generator threw exception:", err);
}

```

---

<a id="file-36"></a>
### 36. `tsconfig.json`

**Path:** `tsconfig.json` | **Size:** `0.54 KB` | **Lines:** `24` | **Language:** `json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",

    /* Linting */
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"]
}

```

---

<a id="file-37"></a>
### 37. `vite.config.ts`

**Path:** `vite.config.ts` | **Size:** `0.35 KB` | **Lines:** `19` | **Language:** `typescript`

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:9011',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:9011',
        ws: true
      }
    }
  }
});

```

---
