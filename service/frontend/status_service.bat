@echo off
cd /d "%~dp0"
echo Status for automation-suite-frontend:
"%~dp0automation-suite-frontend.exe" status

echo.
pause