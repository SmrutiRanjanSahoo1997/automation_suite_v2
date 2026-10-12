@echo off
cd /d "%~dp0"
echo Status for automation-suite-mssql-agent:
"%~dp0automation-suite-mssql-agent.exe" status

echo.
pause