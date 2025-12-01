@echo off
echo.
echo ================================
echo   AutoShield Application
echo ================================
echo.
echo Starting applications...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0start-app.ps1"

pause
