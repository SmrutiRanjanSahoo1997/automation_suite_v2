# Automation Suite V2 — Individual Services Guide (WinSW)

Every folder inside `service/` is **100% self-contained and independent**. You can copy any individual folder to its target server host and run it without needing any other folders!

---

## 1. Directory Structure

```
service/
├── README_SERVICES.md           # Documentation (this file)
│
├── springboot/                  # Host B: Spring Boot Orchestrator (Port 9011)
│   ├── automation-suite-springboot.exe
│   ├── automation-suite-springboot.xml
│   ├── automation_suite_springboot-2.0.0.jar
│   ├── build.bat                # Rebuilds the Spring Boot JAR
│   ├── install_service.bat      # Attaches & starts as Windows Service (Elevated)
│   ├── uninstall_service.bat    # Stops & detaches from Windows Service (Elevated)
│   ├── update_service.bat       # 1-Click: Stop -> Rebuild -> Start (Elevated)
│   ├── start_service.bat        # Start service (Elevated)
│   ├── stop_service.bat         # Stop service (Elevated)
│   ├── restart_service.bat      # Restart service (Elevated)
│   └── status_service.bat       # Check status
│
├── agent/                       # Hosts C..N: Terminal Execution Agent (Port 9010)
│   ├── automation-suite-agent.exe
│   ├── automation-suite-agent.xml
│   ├── automation_suite_agent-2.0.0.jar
│   ├── build.bat
│   ├── install_service.bat
│   ├── uninstall_service.bat
│   ├── update_service.bat
│   ├── start_service.bat
│   ├── stop_service.bat
│   ├── restart_service.bat
│   └── status_service.bat
│
├── mssql_agent/                 # Database Hosts: MSSQL Streaming Agent (Port 9012)
│   ├── automation-suite-mssql-agent.exe
│   ├── automation-suite-mssql-agent.xml
│   ├── automation_suite_mssql_agent-2.0.0.jar
│   ├── build.bat
│   ├── install_service.bat
│   ├── uninstall_service.bat
│   ├── update_service.bat
│   ├── start_service.bat
│   ├── stop_service.bat
│   ├── restart_service.bat
│   └── status_service.bat
│
├── ai_agent/                    # Host X: AI Inference & Runbook Server (Port 9020)
│   ├── automation-suite-ai-agent.exe
│   ├── automation-suite-ai-agent.xml
│   ├── automation_suite_ai_agent-1.0.0.jar
│   ├── build.bat
│   ├── install_service.bat
│   ├── uninstall_service.bat
│   ├── update_service.bat
│   ├── start_service.bat
│   ├── stop_service.bat
│   ├── restart_service.bat
│   └── status_service.bat
│
└── frontend/                    # Host A: Standalone Frontend Web Server (Port 3000)
    ├── automation-suite-frontend.exe
    ├── automation-suite-frontend.xml
    ├── dist/                    # Compiled SPA distribution files
    ├── build.bat
    ├── install_service.bat
    ├── uninstall_service.bat
    ├── update_service.bat
    ├── start_service.bat
    ├── stop_service.bat
    ├── restart_service.bat
    └── status_service.bat
```

---

## 2. In Future: How to Rebuild & Update After Modifying Code

Whenever you modify code for any component, just go to that project's folder inside `service\` and follow these steps:

### Scenario 1: The 1-Click Update (Recommended)
Double-click `update_service.bat` inside that component's folder:
- For Spring Boot: double-click `service\springboot\update_service.bat`
- For Terminal Agent: double-click `service\agent\update_service.bat`
- For MSSQL Agent: double-click `service\mssql_agent\update_service.bat`
- For AI Agent: double-click `service\ai_agent\update_service.bat`
- For Frontend: double-click `service\frontend\update_service.bat`

**What `update_service.bat` does automatically:**
1. Requests Administrator elevation (UAC prompt).
2. Stops the running Windows service (so Windows unlocks the JAR).
3. Calls `build.bat` to recompile the project and replace the JAR with the latest version.
4. Starts the updated Windows service and prints the current status.

---

### Scenario 2: Step-by-Step Manual Update

If you want to do it manually step by step:

1. **Stop the running service**:
   - Double-click `stop_service.bat` in that folder.
2. **Rebuild the JAR**:
   - Double-click `build.bat` in that folder (compiles the code and puts the updated JAR in place).
3. **Start the service**:
   - Double-click `start_service.bat` in that folder.

*(Note: You do **not** need to uninstall or reinstall the Windows service. WinSW automatically picks up the updated JAR!)*

---

## 3. First-Time Setup on a New Host

To deploy any component to a separate host machine:

1. **Copy the specific folder** (e.g. copy the whole `agent` folder to Host C, or `mssql_agent` folder to database host).
2. On that host, double-click **`install_service.bat`**.
   - It will prompt for Administrator elevation.
   - It registers the Windows Service and starts it automatically.
3. To verify: double-click **`status_service.bat`** (will show `Started`).
4. To remove/detach later: double-click **`uninstall_service.bat`**.
