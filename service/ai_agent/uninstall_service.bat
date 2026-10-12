@echo off
setlocal
cd /d "%~dp0"

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Administrative permissions required. Requesting elevation...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

echo ========================================================
echo Detaching / Uninstalling Windows Service: automation-suite-ai-agent
echo ========================================================

echo Stopping service if running...
"%~dp0automation-suite-ai-agent.exe" stop >nul 2>&1

echo Uninstalling service...
"%~dp0automation-suite-ai-agent.exe" uninstall
if %errorlevel% equ 0 (
    echo [SUCCESS] Service uninstalled successfully.
) else (
    echo [ERROR] Failed to uninstall service.
)

echo.
pause