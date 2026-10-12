@echo off
title Copilot File Bridge Server (Port 9020)
cd /d "%~dp0"
echo ========================================================
echo  Compiling & Running Standalone Copilot Java File Bridge
echo ========================================================
javac -d . CopilotFileBridge.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compilation failed! Make sure JDK is installed and in PATH.
    pause
    exit /b %ERRORLEVEL%
)
java com.automation.bridge.CopilotFileBridge
pause
