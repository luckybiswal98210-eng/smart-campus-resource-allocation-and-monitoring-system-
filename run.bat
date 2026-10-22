@echo off
REM ==============================================================================
REM Run Script for Windows (Command Prompt / PowerShell)
REM ==============================================================================

echo ----------------------------------------------------
echo  Launching Smart Campus System on Windows...
echo ----------------------------------------------------

if not exist bin\com\smartcampus (
    echo [INFO] Bin directory not found. Compiling first...
    call compile.bat
)

java -cp "bin;lib/*;." com.smartcampus.Main

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application exited with an error.
    pause
)
