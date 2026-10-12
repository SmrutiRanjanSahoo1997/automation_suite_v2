@echo off
title Automation Suite Frontend V2 (Port 3000)

cd /d "%~dp0"

echo ========================================================
echo       Automation Suite React Frontend V2 (Dev Mode)
echo ========================================================
echo.

echo Opening default web browser to http://localhost:3000/...
start http://localhost:3000/

call npm run dev

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Frontend dev server failed to start.
)

pause
