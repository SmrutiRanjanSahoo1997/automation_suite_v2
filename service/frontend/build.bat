@echo off
setlocal
cd /d "%~dp0"

echo ======================================================================
echo  Building Frontend SPA Distribution (Vite / React)
echo ======================================================================

cd /d "%~dp0..\..\automation_suite_frontend"
call npm run build
if %errorlevel% neq 0 (
    echo [ERROR] Frontend build failed!
    cd /d "%~dp0"
    pause
    exit /b 1
)

echo.
echo Copying dist/ to service/frontend folder...
if exist "%~dp0dist" rmdir /s /q "%~dp0dist"
xcopy /e /i /y "%~dp0..\..\automation_suite_frontend\dist" "%~dp0dist" >nul
cd /d "%~dp0"

echo.
echo [SUCCESS] Frontend built and copied to: %~dp0dist
echo.
pause
