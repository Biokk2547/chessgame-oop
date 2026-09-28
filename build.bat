@echo off
if not exist out mkdir out
javac -encoding UTF-8 -d out src\com\chessegame\*.java src\com\chessegame\model\*.java src\com\chessegame\logic\*.java src\com\chessegame\character\*.java src\com\chessegame\window\*.java src\com\chessegame\ui\swing\*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed
    exit /b %ERRORLEVEL%
)
java -cp out com.chessegame.Main console
