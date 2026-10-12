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
echo Installing Windows Service: automation-suite-agent
echo ========================================================

if not exist "%~dp0automation_suite_agent-2.0.0.jar" (
    echo [WARNING] automation_suite_agent-2.0.0.jar not found in this directory!
    echo Please run build.bat first to build and place the jar here.
    echo.
)

"%~dp0automation-suite-agent.exe" install
if %errorlevel% equ 0 (
    echo [SUCCESS] Service installed successfully.
    echo Starting service...
    "%~dp0automation-suite-agent.exe" start
) else (
    echo [ERROR] Failed to install service.
)

echo.
pause