@echo off
title Automation Suite AI Agent V2 (Port 9020)

cd /d "%~dp0..\automation_suite_ai_agent"

echo ========================================================
echo       Automation Suite AI Agent (Port 9020)
echo ========================================================
echo.

:: Stop any existing process running on Port 9020
echo [INFO] Checking for stale process on Port 9020...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr /R /C:":9020 " ^| findstr LISTENING') do (
    echo [INFO] Killing process PID %%a on Port 9020...
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 1 /nobreak >nul

:: Auto-detect Java if JAVA_HOME is set or JDK 23 exists
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-23" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-23"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

java -cp "target/classes;lib/*" com.automation.aiagent.Main

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] AI Agent server stopped with error code %errorlevel%.
)

pause
