#!/bin/bash

# Find the directory of this script, so it works anywhere
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Go to the project's root directory (assuming this script is in dir1)
CODEGEN_DIR="$SCRIPT_DIR/codegen"

# Ensure the target/classes folder exists
if [ ! -d "$CODEGEN_DIR/target/classes" ]; then
    echo "Error: Compiled classes not found! Run 'mvn clean compile' first."
    exit 1
fi

# Run the Java program using the dynamically found path
java -cp "$CODEGEN_DIR/target/classes" com.generated_progs.ArrayList_add_java_lang_Object_10
