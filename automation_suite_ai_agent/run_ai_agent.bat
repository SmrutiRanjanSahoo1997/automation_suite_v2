@echo off
title Automation Suite AI Agent Server (Port 9020)
echo ===================================================
echo  Starting Automation Suite AI Agent Server...
echo ===================================================

cd /d "%~dp0"

echo [INFO] Checking for stale process on Port 9020...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr /R /C:":9020 " ^| findstr LISTENING') do (
    echo [INFO] Killing process PID %%a on Port 9020...
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 1 /nobreak >nul

java -cp "target/classes;lib/*" com.automation.aiagent.Main
pause
