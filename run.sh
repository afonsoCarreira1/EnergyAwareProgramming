#!/bin/bash

#pythonOrJava="${1:-"j"}"
filename="${2:-"Fib"}"
runCProgram="${3:-"f"}"
numberOfRuns="${4:-"1"}"

sudo rm powerjoular.*
    sudo gcc c_progs/"$2".c -o c_progs/"$2"
    sudo gcc OrchestratorC.c -o OrchestratorC
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
    # Compile Java program (name provided as second argument, e.g., MyRunner.java)zd}
    #javac -d java_progs/out java_progs/progs/$filename.java java_progs/aux/WritePid.java
    javac -XDignore.symbol.file -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out parser/src/OperatorExtractor.java parser/src/ASTFeatureExtractor.java java_progs/aux/WritePid.java Runner.java
    if [ "$1" == "j" ]; then
        sudo java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out Runner $filename $runCProgram $numberOfRuns
    elif [ "$1" == "p" ]; then
        sudo python3 Pyrun.py $filename $runCProgram $numberOfRuns
    elif [ "$1" == "b" ]; then
        sudo ./OrchestratorBash.sh $filename $runCProgram $numberOfRuns
    elif [ "$1" == "t" ]; then
        search_dir=java_progs/progs
        for entry in "$search_dir"/*
        do
            removed_prefix=${entry#*java_progs/progs/}
            removed_suffix=${removed_prefix%.java*}
          #echo "$removed_suffix"
            sudo java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out Runner $removed_suffix $runCProgram $numberOfRuns
        done


        #sudo java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out Runner $filename $runCProgram $numberOfRuns
    elif [ "$1" == "c" ]; then
        if [ "$3" == "f" ]; then
            runCProgram=0
        elif [ "$3" == "t" ]; then
            runCProgram=1
        fi
        sudo ./OrchestratorC $filename $runCProgram $numberOfRuns
    else
    # If neither "j" nor "p" is provided, display a usage message
    echo "Usage: $0 [j|p|c|b] [file name] [optional t to run c program or f to run Java program] [optional run n times]"
    echo "j - Use Java orchestrator"
    echo "p - Use Python orchestrator"
    echo "c - Use C orchestrator"
    echo "b - Use Bash orchestrator"
    #echo "Optional number is passed to the program as an argument"
fi
sudo rm powerjoular.*
sudo rm tmp/*


compile_java_programs() {
    local search_dir="java_progs/progs"  # Directory containing Java programs
    local aux_file="java_progs/aux/WritePid.java"  # Auxiliary file
    local output_dir="java_progs/out"  # Output directory for .class files

    echo "Compiling all Java programs..."

    # Ensure output directory exists
    mkdir -p "$output_dir"

    # Find all Java source files and compile them together
    find "$search_dir" -name "*.java" > sources.txt
    echo "$aux_file" >> sources.txt  # Include auxiliary file
    javac -d "$output_dir" @sources.txt

    # Clean up temporary sources.txt file
    rm sources.txt

    echo "Compiled!"
}