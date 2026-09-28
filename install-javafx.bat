@echo off
REM =====================================================
REM OpenJFX SDK Downloader and Installer for Chess Game
REM =====================================================

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  OpenJFX 21 Installation Script
echo ========================================
echo.

REM Create directories
if not exist "C:\javafx-sdk-21.0.0" mkdir C:\javafx-sdk-21.0.0

REM Try different download URLs
set "URL1=https://download2.gluonhq.com/openjfx/21.0.0/openjfx-21.0.0-windows-x64-sdk.zip"
set "URL2=https://gluonhq.com/download/javafx-21.0.0-sdk-windows.zip"
set "ZIPFILE=%TEMP%\javafx-sdk.zip"

echo [1/3] Downloading OpenJFX 21...
echo.
echo Trying URL: !URL1!

REM Try download with powershell (more reliable)
powershell -Command ^
  "$ProgressPreference = 'SilentlyContinue'; ^
  try { ^
    Invoke-WebRequest -Uri '!URL1!' -OutFile '!ZIPFILE!' -TimeoutSec 120; ^
    Write-Host 'Download successful'; ^
  } catch { ^
    Write-Host 'Failed'; ^
  }"

REM Check if download succeeded
if exist "%ZIPFILE%" (
    for /f %%A in ('powershell -Command "[math]::Round((Get-Item '%ZIPFILE%').Length / 1MB, 2)"') do set "SIZE=%%A"
    
    if "!SIZE!" gtr "50" (
        echo ✓ Downloaded: !SIZE! MB
        echo.
        echo [2/3] Extracting to C:\javafx-sdk-21.0.0...
        
        REM Extract using PowerShell
        powershell -Command "Expand-Archive -Path '%ZIPFILE%' -DestinationPath 'C:\' -Force"
        
        echo ✓ Extracted!
        echo.
        echo [3/3] Setting Environment Variable...
        
        REM Set environment variable
        setx JAVAFX_LIB "C:\javafx-sdk-21.0.0\lib"
        
        echo.
        echo ========================================
        echo  ✓ Installation Complete!
        echo ========================================
        echo.
        echo JAVAFX_LIB has been set to:
        echo   C:\javafx-sdk-21.0.0\lib
        echo.
        echo IMPORTANT: Please restart PowerShell/Command Prompt
        echo before running build-javafx.bat again!
        echo.
        pause
        exit /b 0
    )
)

echo.
echo ❌ Download failed or file too small
echo.
echo Please download manually:
echo   URL: https://gluonhq.com/products/javafx/
echo   Download: OpenJFX 21 SDK for Windows
echo   Extract to: C:\javafx-sdk-21.0.0
echo.
echo Then run:
echo   setx JAVAFX_LIB "C:\javafx-sdk-21.0.0\lib"
echo.
echo And RESTART PowerShell/Command Prompt!
echo.
pause
exit /b 1
