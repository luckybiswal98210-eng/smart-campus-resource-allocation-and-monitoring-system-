#!/bin/bash
# ==============================================================================
# Run Script for Smart Campus Resource Allocation & Monitoring System
# ==============================================================================

if [ ! -d "bin/com/smartcampus" ]; then
    echo "🔨 Binary files not found. Compiling first..."
    ./compile.sh
fi

echo "🚀 Launching Smart Campus System..."
java -cp "bin:lib/*:." com.smartcampus.Main
