#!/bin/bash

# Make the script executable
if [ ! -x "$0" ]; then
    chmod +x "$0"
    echo "Script is now executable. Please run it again."
    exit 0
fi

echo "Building the application..."

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows
    GO_EXECUTABLE="./main.exe"
else
    # Assume non-Windows (Linux, macOS, etc.)
    GO_EXECUTABLE="./main"
fi

# Build the Go program
go build lib/main.go

if [ $? -eq 0 ]; then
    echo "Build successful."
    echo "Running the executable..."
    $GO_EXECUTABLE backend
else
    echo "Build failed."
fi
