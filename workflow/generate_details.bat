@echo off
setlocal enabledelayedexpansion

echo =======================================================================
echo              AUTOMATION SUITE - CODEBASE DETAILS GENERATOR
echo =======================================================================
echo.
echo Analyzing projects and generating comprehensive code details...
echo.

python "%~dp0generate_details.py"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Detailed code generation failed with exit code %ERRORLEVEL%.
    if /i "%~1" neq "/nopause" pause
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================================
echo [SUCCESS] Detailed code markdown files generated in doc/!
echo.
echo Generated Files in doc Folder:
echo   1. doc/agent_detailed_code.md
echo   2. doc/springboot_detailed_code.md
echo   3. doc/frontend_detailed_code.md
echo =======================================================================
echo.

if /i "%~1" neq "/nopause" pause
