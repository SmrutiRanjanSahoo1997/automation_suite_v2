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
echo Installing Windows Service: automation-suite-frontend
echo ========================================================



"%~dp0automation-suite-frontend.exe" install
if %errorlevel% equ 0 (
    echo [SUCCESS] Service installed successfully.
    echo Starting service...
    "%~dp0automation-suite-frontend.exe" start
) else (
    echo [ERROR] Failed to install service.
)

echo.
pause