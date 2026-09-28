@echo off
rem Edit JAVAFX_LIB to point to your JavaFX SDK lib folder, for example:
rem set JAVAFX_LIB=C:\javafx-sdk-20.0.2\lib
if "%JAVAFX_LIB%"=="" (
  echo Please set the JAVAFX_LIB environment variable to your JavaFX SDK lib path.
  echo Example: set JAVAFX_LIB=C:\javafx-sdk-20.0.2\lib
  exit /b 1
)
if not exist out mkdir out
javac --module-path %JAVAFX_LIB% --add-modules javafx.controls -d out src\com\chessegame\*.java src\com\chessegame\model\*.java src\com\chessegame\logic\*.java src\com\chessegame\window\*.java src\com\chessegame\window\fx\*.java src\com\chessegame\ui\fx\*.java
if %ERRORLEVEL% neq 0 (
  echo Compilation failed
  exit /b %ERRORLEVEL%
)
java --module-path %JAVAFX_LIB% --add-modules javafx.controls -cp out com.chessegame.window.fx.JavaFXWindow


