@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ======================================================================
echo  Building MSSQL Database Agent JAR
echo ======================================================================

:: Locate Maven
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
) else (
    for /f "delims=" %%i in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\*mvn.cmd" 2^>nul') do (
        if exist "%%i" set "MVN_CMD=%%i"
    )
    if not defined MVN_CMD (
        if exist "..\..\automation_suite_springboot\mvnw.cmd" (
            set "MVN_CMD=call "..\..\automation_suite_springboot\mvnw.cmd""
        ) else (
            echo [ERROR] Maven not found. Please install Maven or add to PATH.
            pause
            exit /b 1
        )
    )
)

echo [1/2] Compiling and packaging automation_suite_mssql_agent...
cd /d "%~dp0..\..\automation_suite_mssql_agent"
set "OFFLINE_OPTS="
if exist "%~dp0..\..\.m2_repo" (
    echo [INFO] Using bundled offline repository: %~dp0..\..\.m2_repo
    set "OFFLINE_OPTS=-o -Dmaven.repo.local="%~dp0..\..\.m2_repo""
)
call !MVN_CMD! clean package -DskipTests !OFFLINE_OPTS!
if %errorlevel% neq 0 (
    echo [ERROR] MSSQL Agent packaging failed!
    cd /d "%~dp0"
    pause
    exit /b 1
)

echo [2/2] Copying JAR to service folder...
copy /y "%~dp0..\..\automation_suite_mssql_agent\target\automation_suite_mssql_agent-2.0.0.jar" "%~dp0" >nul
cd /d "%~dp0"

echo.
echo [SUCCESS] Build complete: %~dp0automation_suite_mssql_agent-2.0.0.jar
echo.
pause
