#!/bin/bash

#pythonOrJava="${1:-"j"}"
filename="${2:-"Fib"}"
runCProgram="${3:-"f"}"
numberOfRuns="${4:-"1"}"

sudo rm powerjoular.*
    sudo gcc c_progs/Fib.c -o c_progs/Fib
    
    # Compile Java program (name provided as second argument, e.g., MyRunner.java)
    sudo javac java_progs/"$2".java #2>/dev/null
    sudo javac Runner.java #2>/dev/null
    if [ "$1" == "j" ]; then
        sudo java Runner $filename $runCProgram $numberOfRuns
    elif [ "$1" == "p" ]; then
        sudo python3 Pyrun.py $filename $runCProgram $numberOfRuns
    elif [ "$1" == "b" ]; then
        sudo ./c_runner.sh $filename $runCProgram $numberOfRuns
    else
    # If neither "j" nor "p" is provided, display a usage message
    echo "Usage: $0 [j|p] [file name] [optional number]"
    echo "j - Run Java program (file without extension)"
    echo "p - Run Python program (file without extension)"
    echo "Optional number is passed to the program as an argument"
fi