@echo off
cd /d "%~dp0"
echo =========================================================
echo              CHESS GAME OOP (Debug / Log Mode)
echo =========================================================
echo.
java --enable-native-access=ALL-UNNAMED -jar ChessGame.jar
if %errorlevel% neq 0 (
    echo.
    echo Game exited with error code: %errorlevel%
    pause
)
