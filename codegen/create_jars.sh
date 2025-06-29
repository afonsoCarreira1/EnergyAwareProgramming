#!/bin/bash

# Clean old build
rm -rf bin
mkdir -p bin

# Compile Java source to bin directory with package folders
javac -d bin src/main/java/com/template/programsToBenchmark/BinaryTrees.java

# Check if compilation succeeded
if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi

# Go into bin directory
cd bin

# Create jar with proper entry point and package structure
jar cfe BinaryTrees.jar com.template.programsToBenchmark.BinaryTrees com/template/programsToBenchmark/*.class

echo "Build complete. JAR created at bin/BinaryTrees.jar"
