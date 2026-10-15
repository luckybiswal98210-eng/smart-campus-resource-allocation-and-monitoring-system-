#!/bin/bash
# ==============================================================================
# Compilation Script for Smart Campus Resource Allocation & Monitoring System
# ==============================================================================

echo "🔨 Compiling Smart Campus Java Microproject..."
mkdir -p bin

javac -cp "lib/*:." -d bin src/com/smartcampus/*.java src/com/smartcampus/*/*.java

if [ $? -eq 0 ]; then
    echo " Compilation successful! Output directory: bin/"
else
    echo "❌ Compilation failed. Please check errors above."
    exit 1
fi
