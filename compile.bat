@echo off
REM ==============================================================================
REM Compilation Script for Windows (Command Prompt / PowerShell)
REM ==============================================================================

echo ----------------------------------------------------
echo  Compiling Smart Campus Java Microproject (Windows)
echo ----------------------------------------------------

if not exist bin (
    mkdir bin
)

javac -cp "lib/*;." -d bin src/com/smartcampus/*.java src/com/smartcampus/*/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful! Files compiled into bin/
) else (
    echo [ERROR] Compilation failed. Please check if JDK is installed and in PATH.
    pause
)
