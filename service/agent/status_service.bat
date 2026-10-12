@echo off
cd /d "%~dp0"
echo Status for automation-suite-agent:
"%~dp0automation-suite-agent.exe" status

echo.
pause