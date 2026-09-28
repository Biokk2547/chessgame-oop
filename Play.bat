@echo off
cd /d "%~dp0"

echo =========================================================
echo              CHESS GAME OOP (LibGDX)
echo =========================================================
echo Starting Chess Game...
echo.

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java 11 or higher was not detected on this system!
    echo Please install Java 11 or higher from https://adoptium.net/
    echo.
    pause
    exit /b 1
)

start "" javaw --enable-native-access=ALL-UNNAMED -jar ChessGame.jar
