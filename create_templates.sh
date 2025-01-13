#!/bin/bash

compile_java_programs() {
    local search_dir="java_progs/progs"  # Directory containing Java programs
    local aux_file="java_progs/aux/WritePid.java"  # Auxiliary file
    local output_dir="java_progs/out"  # Output directory for .class files
    local error_files="error_files.txt"  # File to store files with errors

    echo "Programs created."
    echo "Compiling all Java programs..."

    # Ensure output directory exists
    mkdir -p "$output_dir"

    # Find all source files and include auxiliary file
    find "$search_dir" -name "*.java" > sources.txt
    echo "$aux_file" >> sources.txt

    # Try compiling all at once and capture errors
    javac -d "$output_dir" @sources.txt 2> compile_errors.log

    # Parse error messages to find problematic files
    grep -oE '^[^:]*\.java' compile_errors.log | sort | uniq > "$error_files"

    if [ -s "$error_files" ]; then
        #echo "The following files had compilation errors and will be deleted:"
        #cat "$error_files"
        while read -r error_file; do
            rm "$error_file"
        done < "$error_files"
    fi

    # Clean up
    rm sources.txt compile_errors.log "$error_files"
    echo "Removed incorrect programs and compiled all the others."
    echo "Compilation process complete!"
}

sudo rm -r java_progs/progs/*
javac java_progs/CreateTemplates.java
java java_progs/CreateTemplates
compile_java_programs