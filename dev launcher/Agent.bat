@echo off
title Automation Suite Agent V2 (Port 9010)

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

cd /d "%~dp0..\automation_suite_agent"

echo ========================================================
echo         Automation Suite Agent V2 - Dev Mode
echo ========================================================
echo.

call mvn compile exec:java

pause
