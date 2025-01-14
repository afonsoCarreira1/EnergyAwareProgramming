#!/bin/bash

sudo rm -r java_progs/progs/*
javac java_progs/CreateTemplates.java
java java_progs/CreateTemplates
javac java_progs/aux/ArrayListAux.java
mv java_progs/aux/ArrayListAux.class java_progs/out/java_progs/aux
rm java_progs/out/java_progs/progs/* #remove compiled classes


echo Compiling all Java programs...
search_dir=java_progs/progs
aux_file=java_progs/aux/WritePid.java
output_dir=java_progs/out

# Create the output directory if it doesn't exist
mkdir -p "$output_dir"

# Collect all .java files into a single list and compile together
find "$search_dir" -name "*.java" > sources.txt
echo "$aux_file" >> sources.txt  # Include the auxiliary file
javac -d "$output_dir" @sources.txt
echo Compiled!

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

    #if [ -s "$error_files" ]; then
    #    #echo "The following files had compilation errors and will be deleted:"
    #    #cat "$error_files"
    #    while read -r error_file; do
    #        rm "$error_file"
    #    done < "$error_files"
    #fi

    # Clean up
    #rm sources.txt compile_errors.log "$error_files"
    echo "Removed incorrect programs and compiled all the others."
    echo "Compilation process complete!"
}

delete_uncompiled_java_files() {
    local java_dir="java_progs/progs"
    local class_dir="java_progs/out/java_progs/progs"

    # Find all .java files in the java directory
    for java_file in "$java_dir"/*.java; do
        
        # Get the base name of the .java file (without extension)
        class_file="$class_dir/$(basename "$java_file" .java).class"
        sleep 5

        # Check if the corresponding .class file exists
        if [ ! -f "$class_file" ]; then
            echo $java_file
            # If .class file doesn't exist, delete the .java file
            #echo "Deleting $java_file (no corresponding .class file)"
            #rm "$java_file"
        fi
    done
}


#compile_java_programs
#delete_uncompiled_java_files