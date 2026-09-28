@echo off
cd /d "%~dp0"
echo =========================================================
echo             CHESS GAME OOP (JavaFX Mode)
echo =========================================================
echo.
java --enable-native-access=ALL-UNNAMED -jar ChessGame.jar JAVAFX
pause
