@echo off
title Automation Suite SpringBoot V2 (Port 9011)

:: Auto-detect Java & Maven paths if not in global PATH
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-23" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-23"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
for /d %%D in ("C:\Program Files\JetBrains\*") do (
    if exist "%%D\plugins\maven\lib\maven3\bin" (
        set "PATH=%%D\plugins\maven\lib\maven3\bin;!PATH!"
    )
)

cd /d "%~dp0..\automation_suite_springboot"

echo ========================================================
echo    Automation Suite SpringBoot Orchestrator V2 (Dev Mode)
echo ========================================================
echo.

call mvn spring-boot:run

pause
