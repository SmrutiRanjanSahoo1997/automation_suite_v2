# Automation Suite V2

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2-blue.svg)](https://www.typescriptlang.org/)
[![Java](https://img.shields.io/badge/Java-17%2F21-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

**Automation Suite V2** is an enterprise-grade automation, system diagnostics, multi-terminal session orchestrator, MSSQL database query studio, and autonomous AI assistant platform designed for large-scale distributed server fleets.

---

## 1. System Architecture Overview

```
   ┌─────────────────────────────────────────────────────────────┐
   │                     Client Workstations                     │
   │               (Browser: Chrome / Edge / etc.)               │
   └──────────────────────────────┬──────────────────────────────┘
                                  │ HTTP(S) & WebSocket (Port 9011)
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │            HOST B: Spring Boot Backend (The Brain)          │
   │              Port 9011: REST & WebSocket (/ws/app)          │
   │  - SQLite Database (automation.db)                          │
   │  - Lowest-Queue MSSQL Load Balancer                         │
   │  - Terminal Session Orchestrator                            │
   │  - Auth & Security Engine (JWT / BCrypt / CORS)             │
   └────────────┬──────────────────┬──────────────────┬──────────┘
                │                  │                  │
    Port 9020   │      Port 9010   │      Port 9012   │
    WebSocket   │      WebSocket   │      WebSocket   │
                ▼                  ▼                  ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │  HOST X: AI Agent│  │ HOSTS C..N:      │  │ HOSTS M1..Mn:    │
   │  (Port 9020)     │  │ Terminal Agents  │  │ MSSQL Agents     │
   │ - Copilot Bridge │  │ (Port 9010)      │  │ (Port 9012)      │
   │ - Intent Routing │  │ - PowerShell/CMD │  │ - PAG0035L (Prod)│
   │ - Runbook Sync   │  │   ConPTY Bridge  │  │ - TAG0026L (Test)│
   └──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 2. Modules & Core Components

| Module | Technology | Port | Description |
| :--- | :--- | :---: | :--- |
| **`automation_suite_springboot`** | Spring Boot 3.2, Java 17 | `9011` | Core backend orchestrator, SQLite database, global WebSocket broker, JWT authentication. |
| **`automation_suite_frontend`** | React 18, TypeScript, Vite | `3000` / `9011` | High-performance SPA with Terminal Manager, Database Studio, and ASR AI chat interface. |
| **`automation_suite_agent`** | Plain Java 21, ConPTY, JNA | `9010` | Native Windows pseudo-console agent executing PowerShell and CMD with live streaming. |
| **`automation_suite_mssql_agent`** | Plain Java 17, Java-WebSocket | `9012` | Standalone streaming MSSQL execution agent with lowest-queue reporting. |
| **`automation_suite_ai_agent`** | Plain Java 17, Jackson | `9020` | Autonomous AI inference bridge, intent routing, and runbook catalog synchronizer. |
| **`ai_chrome_extension`** | Chrome Manifest V3 | — | Browser extension bridge for Copilot communication. |
| **`service/`** | WinSW x64, Windows Services | — | Production Windows Service wrappers and elevated management scripts for each component. |
| **`dev launcher/`** | Windows Batch | — | Quick-launch scripts for local multi-process development. |
| **`Runbooks/`** | Markdown & SQL | — | Repository of operational procedures, catalogs, and reusable queries. |

---

## 3. Key Features

### 3.1 Terminal Orchestration
- **Native ConPTY Integration**: Real pseudo-console execution on Windows without terminal emulation glitches.
- **Session Isolation & Multi-Window Tabs**: Run isolated PowerShell and CMD sessions across different target hosts simultaneously.
- **Auto-Reconnect & State Recovery**: Persistent agent connections with automatic backoff reconnection loops.

### 3.2 MSSQL Database Studio
- **Excel-Style Virtualized Grid**: Handles thousands of rows with `A1` select all, `Shift + Arrow` cell range selection, and context menus.
- **Lowest-Queue Load Balancing**: Pure in-memory queue depth monitoring across connected MSSQL agents; queries automatically route to the idle/least-busy agent.
- **Syntax Highlighting & Typo Intelligence**: Live non-blocking compile-time SQL linting, keyword coloring, and string literal recognition.
- **Dual-Axis Resizing**: Draggable splitters with independent vertical and horizontal overflow controls.

### 3.3 ASR AI Autonomous Assistant (`sahoos_ASR_AI`)
- **Intent Auto-Routing**: Dynamically distinguishes between general Q&A, server diagnostics (Terminal), and SQL queries (Database).
- **Safe Execution Policy**: Safe read-only `SELECT` queries execute automatically; mutating statements (`INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`) trigger an interactive `AiReviewBanner` for approval.
- **Result Synthesis**: Transforms raw database rows into formatted Markdown tables with domain insights.
- **Rolling Context Accumulator**: Maintains ground-truth multi-turn context across consecutive prompts.

---

## 4. Quick Start

### 4.1 Development Mode (Local Testing)
Use the turnkey batch scripts in `dev launcher/`:
1. Start Spring Boot: `dev launcher\SpringBoot.bat`
2. Start Terminal Agent: `dev launcher\Agent.bat`
3. Start Frontend: `dev launcher\Frontend.bat`
4. Or launch all at once: `dev launcher\run-all-dev.bat`

Access the application in your browser at:
`http://localhost:3000` (or `http://localhost:9011` when served from Spring Boot).

---

### 4.2 Production Mode (Windows Services)
Every folder inside `service/` is **100% self-contained**:

```
service/
├── springboot/     # Spring Boot Windows Service (Port 9011)
├── agent/          # Terminal Agent Windows Service (Port 9010)
├── mssql_agent/    # MSSQL Agent Windows Service (Port 9012)
├── ai_agent/       # AI Agent Windows Service (Port 9020)
└── frontend/       # Standalone Frontend Server (Port 3000, optional)
```

#### Installing a Service
1. Copy the target folder to your server (e.g., `service\agent` to a target server).
2. Double-click **`install_service.bat`** (elevates to Administrator automatically).
3. Check status with **`status_service.bat`**.

#### Updating a Service After Code Changes
Double-click **`update_service.bat`** in that component's folder. It automatically:
- Stops the running Windows service.
- Recompiles the project (`build.bat`) and replaces the JAR.
- Restarts the service with the new build.

For detailed distributed deployment instructions, see [`INSTALLATION_GUIDE.md`](INSTALLATION_GUIDE.md) and [`service/README_SERVICES.md`](service/README_SERVICES.md).

---

## 5. Configuration Reference

### Spring Boot (`application.properties`)
```properties
server.port=9011

# SQLite Database Location
DATABASE_PATH=C:/Ranjan/Automation Suite/Database
spring.datasource.url=jdbc:sqlite:${DATABASE_PATH}/automation.db

# Agent Ports
agent.default-port=9010
agent.secret-key=zGQ3aJB3IluC5eeU
mssql.agent.port=9012

# AI Agent & Runbooks
ai.agent.ws-url=ws://<AI_HOST>:9020/ws/ai_agent
AI_RUNBOOKS_ROOT_PATH=\\\\network-share\\ASR_Runbooks\\Database_Queries
```

### Network Ports
| Port | Direction | Usage |
| :---: | :---: | :--- |
| **9011** | Inbound | Spring Boot REST API & Global WebSocket (`/ws/app`) |
| **9010** | Inbound | Terminal Agent WebSocket on target hosts |
| **9012** | Inbound | MSSQL Agent WebSocket on database hosts |
| **9020** | Inbound | AI Agent WebSocket Server |
| **3000** | Inbound | Vite Dev Server (development only) |

---

## 6. Author

- **Author**: Smruti Ranjan Sahoo
- **Email**: happysahoo45@gmail.com
- **GitHub**: [@SmrutiRanjanSahoo1997](https://github.com/SmrutiRanjanSahoo1997)
