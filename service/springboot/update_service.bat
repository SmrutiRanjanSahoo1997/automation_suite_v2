@echo off
setlocal
cd /d "%~dp0"

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Administrative permissions required. Requesting elevation...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

echo ======================================================================
echo  Updating Spring Boot Orchestrator Windows Service
echo ======================================================================

echo [1/3] Stopping service to release JAR lock...
"%~dp0automation-suite-springboot.exe" stop >nul 2>&1

echo [2/3] Rebuilding JAR with latest code...
call "%~dp0build.bat"

echo [3/3] Starting updated service...
"%~dp0automation-suite-springboot.exe" start
echo.
echo Service Status:
"%~dp0automation-suite-springboot.exe" status
echo.
pause
