@echo off
setlocal enabledelayedexpansion

title Automation Suite V2 - Agent Dev Launcher

echo ========================================================
echo         Automation Suite V2 - Agent Dev Launcher
echo ========================================================
echo.

:: Auto-detect Java & Maven paths if not in global PATH
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-23" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-23"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

:: Auto-detect IntelliJ bundled Maven or .m2 Maven wrapper dists
for /d %%D in ("C:\Program Files\JetBrains\*") do (
    if exist "%%D\plugins\maven\lib\maven3\bin" (
        set "PATH=%%D\plugins\maven\lib\maven3\bin;!PATH!"
    )
)
for /d %%D in ("%LOCALAPPDATA%\Programs\IntelliJ*") do (
    if exist "%%D\plugins\maven\lib\maven3\bin" (
        set "PATH=%%D\plugins\maven\lib\maven3\bin;!PATH!"
    )
)
for /r "%USERPROFILE%\.m2\wrapper\dists" %%F in (mvn.cmd) do (
    if exist "%%~dpF" (
        set "PATH=%%~dpF;!PATH!"
    )
)

echo [1/4] Stopping stale processes on ports 9010, 9011, and 3000...
for %%P in (9010 9011 3000) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr /R /C:":%%P " ^| findstr LISTENING') do (
        echo [INFO] Killing process PID %%a on port %%P...
        taskkill /F /PID %%a >nul 2>&1
    )
)
timeout /t 2 /nobreak >nul

echo [2/4] Launching Java Agent V2 (Port 9010)...
start "Agent V2 (9010)" /d "%~dp0" cmd /k "Agent.bat"

timeout /t 3 /nobreak >nul

echo [3/4] Launching Spring Boot Orchestrator V2 (Port 9011)...
start "SpringBoot V2 (9011)" /d "%~dp0" cmd /k "SpringBoot.bat"

timeout /t 3 /nobreak >nul

echo [4/4] Launching React Frontend V2 (Port 3000)...
start "Frontend V2 (3000)" /d "%~dp0" cmd /k "Frontend.bat"

echo.
echo Opening default web browser to http://localhost:3000/...
start http://localhost:3000/

echo.
echo ========================================================
echo [SUCCESS] Agent, SpringBoot, and Frontend launched!
echo - Agent: ws://localhost:9010
echo - Spring Boot Orchestrator: http://localhost:9011
echo - React Frontend Dashboard: http://localhost:3000
echo ========================================================
echo.

pause
