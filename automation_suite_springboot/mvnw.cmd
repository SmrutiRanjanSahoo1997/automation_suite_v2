@echo off
REM Minimal Maven wrapper for Windows. Uses system mvn when available.
where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
  mvn %*
  exit /b %ERRORLEVEL%
)
echo System mvn not found. Install Maven 3.9+ and re-run mvnw.cmd, or run: winget install Apache.Maven >&2
exit /b 1
