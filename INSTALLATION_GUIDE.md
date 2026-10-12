# Automation Suite V2 — Distributed Installation & Multi-Host Deployment Guide

This document provides a comprehensive, step-by-step installation and configuration guide for deploying **Automation Suite V2** across a distributed, multi-host enterprise architecture.

---

## 1. Architecture & Component Topology

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

| Component | Default Port | Protocol | Primary Function |
| :--- | :--- | :--- | :--- |
| **Frontend UI** | `3000` (Dev) / `9011` (Prod) | HTTP(S) + WS | React SPA (Terminal, Database Studio, ASR AI) |
| **Spring Boot Backend** | `9011` | HTTP + WS | Core orchestrator, DB logs, WebSocket broker |
| **Terminal Agent** | `9010` | WS Server | Executes CMD / PowerShell commands via ConPTY |
| **MSSQL Agent** | `9012` | WS Server | Dedicated streaming T-SQL execution agent |
| **AI Agent** | `9020` | WS Server | Copilot AI inference and runbook sync |
| **SQLite DB** | Local / UNC | File | System data, audit logs (`automation.db`) |
| **Runbooks** | Local / UNC | File / SMB | Markdown workflows & `.sql` query files |

---

## 2. Server Inventory Configuration

The target server fleet is defined in:
[`ServerInventory.java`](file:///c:/Ranjan/AutomationSuite_v2/automation_suite_springboot/src/main/java/com/automation/config/ServerInventory.java)

- **Localhost Removed**: `localhost` has been removed from the server list so only real infrastructure hosts (`H1191`, `H1375`, `H1190`, `H1189`, etc.) are recognized.
- **Port Allocation**: Each host is contacted on `Port 9010` for Terminal and `Port 9012` for MSSQL.

---

## 3. Spring Boot Backend Deployment (Host B)

### 3.1 `application.properties` Reference
Located at: `src/main/resources/application.properties` (or passed via command line).

```properties
# Server Port
server.port=9011
spring.main.allow-circular-references=true

# Database Path Configuration
# You can change BASE_PATH to any local path or Windows network share (UNC)
BASE_PATH=C:/Ranjan/Automation Suite
DATABASE_PATH=${BASE_PATH}/Database
spring.datasource.url=jdbc:sqlite:${DATABASE_PATH}/automation.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# Flyway Database Migrations
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Terminal Agents Configuration
agent.default-port=9010
agent.secret-key=zGQ3aJB3IluC5eeU
agent.reconnect-interval-ms=5000

# MSSQL Agents Configuration
mssql.agent.port=9012

# AI Agent Configuration (Host X)
ai.agent.ws-url=ws://<AI_AGENT_HOST_IP_OR_HOSTNAME>:9020/ws/ai_agent
ai.prompt.path=C:/Ranjan/AutomationSuite_v2/PromptTesting/final_prompts

# Runbooks Repository Path (Local directory or Windows UNC Share)
AI_RUNBOOKS_ROOT_PATH=\\\\network-share\\ASR_Runbooks\\Database_Queries

# Auth & Security Keys
auth.salt-key=AutomationSuiteV2SecretSaltKey2026!
auth.jwt-secret=AutomationSuiteV2JwtSecretKey99887766554433221100!
auth.admin-password=happy@123
```

### 3.2 Running Spring Boot in Production
Build the executable jar:
```bash
cd automation_suite_springboot
mvn clean package -DskipTests
```
Launch Spring Boot with external overrides (no recompilation needed):
```bash
java -jar target/automation_suite_springboot-2.0.0.jar \
  --DATABASE_PATH="D:/AutomationData/Database" \
  --AI_RUNBOOKS_ROOT_PATH="\\\\fileserver\\Automation\\Runbooks" \
  --ai.agent.ws-url="ws://10.10.20.50:9020/ws/ai_agent"
```

---

## 4. Frontend Deployment (Host A or Embedded)

### 4.1 Deployment Options

#### Option A: Embedded Inside Spring Boot (Recommended — Zero Config)
During `mvn package`, Maven automatically builds the React SPA and copies `dist/*` into Spring Boot's `target/classes/static`.
- Spring Boot serves the frontend directly at `http://<SPRINGBOOT_HOST>:9011/`.
- **Zero URL Configuration**: The frontend dynamically detects `window.location.hostname` and `window.location.origin`, automatically binding REST to `http://<SPRINGBOOT_HOST>:9011` and WebSocket to `ws://<SPRINGBOOT_HOST>:9011/ws/app`.

#### Option B: Standalone Web Server (Nginx / Node / S3)
If the frontend is hosted on a separate host (e.g. `http://frontend-server:3000` or Nginx):
1. Create or update `.env.production` inside `automation_suite_frontend`:
   ```properties
   # Point to Host B (Spring Boot)
   VITE_BACKEND_URL=http://<SPRINGBOOT_HOST_IP_OR_NAME>:9011
   VITE_WS_URL=ws://<SPRINGBOOT_HOST_IP_OR_NAME>:9011/ws/app
   ```
2. Build the distribution:
   ```bash
   cd automation_suite_frontend
   npm run build
   ```
3. Copy the resulting `dist/` directory to your Nginx or web server document root.

### 4.2 Controlling Logs (Dev Mode vs. Production Mode)

1. **Production Mode (`npm run build`)**:
   - Diagnostic logging in [`logger.ts`](file:///c:/Ranjan/AutomationSuite_v2/automation_suite_frontend/src/utils/logger.ts) is **automatically turned OFF**.
   - Vite sets `import.meta.env.DEV = false`, silencing all `logger.debug`, `logger.info`, `logger.warn`, and `logger.error` output in the browser console.
2. **Development Mode (`npm run dev`)**:
   - If running in dev mode and you want to **stop debug console logs**, add this to `.env`:
     ```properties
     VITE_DISABLE_LOGS=true
     ```
   - Restart `npm run dev`. All console logs will be suppressed immediately.

---

## 5. Terminal Agent Deployment (Hosts C .. N)

The Terminal Agent must run on each managed Windows/Linux server listed in `ServerInventory.java`.

1. **Deploy Binary**: Copy `automation_suite_agent-2.0.0.jar` to the target server (e.g., `C:\AutomationAgent\`).
2. **Configure `application.properties`**:
   ```properties
   agent.key=zGQ3aJB3IluC5eeU
   terminal.max-count=10
   terminal.stop-timeout-ms=10000
   ```
3. **Start Agent**:
   ```bash
   java -jar automation_suite_agent-2.0.0.jar
   ```
4. **Firewall**: Ensure **Inbound Port 9010 (TCP)** is allowed on the target server.

---

## 6. MSSQL Agent Deployment (Database Server Hosts)

The MSSQL Agent executes T-SQL queries against Microsoft SQL Server and streams results back to Spring Boot.

1. **Deploy Binary**: Copy `automation_suite_mssql_agent-2.0.0.jar` to each database machine or server with SQL access.
2. **Run Agent**:
   ```bash
   java -jar automation_suite_mssql_agent-2.0.0.jar
   ```
3. **Behavior**:
   - The MSSQL Agent binds to `0.0.0.0:9012`.
   - Spring Boot's [`MssqlAgentClientManager`](file:///c:/Ranjan/AutomationSuite_v2/automation_suite_springboot/src/main/java/com/automation/mssql/client/MssqlAgentClientManager.java) automatically initiates WebSocket connections to each host on Port 9012.
   - Spring Boot polls queue depths every 3 seconds and routes incoming queries using in-memory **lowest active queue** load balancing.
4. **Firewall**: Ensure **Inbound Port 9012 (TCP)** is allowed.

---

## 7. AI Agent Deployment (Host X)

1. **Port & Endpoint**: Runs on `Port 9020`, exposing `ws://0.0.0.0:9020/ws/ai_agent`.
2. **Integration with Spring Boot**:
   - Spring Boot connects as a WebSocket client to `ai.agent.ws-url=ws://<AI_HOST>:9020/ws/ai_agent`.
   - AI Agent handles feature routing, SQL generation, and synchronizes the runbook catalog path with the frontend via the `AI_RUNBOOKS_ROOT_PATH` event.
3. **Firewall**: Ensure **Inbound Port 9020 (TCP)** is allowed on Host X.

---

## 8. Path Configuration Summary

| Path Type | Configuration Property / Location | Supports Network Share / UNC? | Notes |
| :--- | :--- | :---: | :--- |
| **SQLite DB** | `DATABASE_PATH` or `spring.datasource.url` | Yes | Store on fast local SSD or shared SMB volume |
| **Runbooks** | `AI_RUNBOOKS_ROOT_PATH` in `application.properties` | Yes (`\\\\server\\share`) | Accessible by Spring Boot and AI Agent |
| **Prompt Dir** | `ai.prompt.path` in `application.properties` | Yes | Local folder containing template `.txt` files |

---

## 9. Network & Firewall Port Checklist

Before starting, verify the following TCP port openings across your network:

```
[Client Browser]   ──(TCP 9011)─────────────► [Spring Boot (Host B)]
[Spring Boot]      ──(TCP 9010 Outbound)────► [Terminal Agents (Port 9010)]
[Spring Boot]      ──(TCP 9012 Outbound)────► [MSSQL Agents (Port 9012)]
[Spring Boot]      ──(TCP 9020 Outbound)────► [AI Agent (Port 9020)]
```
