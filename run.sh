#!/bin/bash

pythonOrJava="${1:-"j"}"
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
    else
    # If neither "j" nor "p" is provided, display a usage message
    echo "Usage: $0 [j|p] [file name] [optional number]"
    echo "j - Run Java program (file without extension)"
    echo "p - Run Python program (file without extension)"
    echo "Optional number is passed to the program as an argument"
fi

: '
# Check if the first argument is provided (j for Java or p for Python)
if [ "$1" == "j" ]; then
    # Java case
    sudo rm powerjoular.*
    sudo gcc c_progs/Fib.c -o c_progs/Fib
    
    # Compile Java program (name provided as second argument, e.g., MyRunner.java)
    sudo javac java_progs/"$2".java #2>/dev/null
    sudo javac Runner.java #2>/dev/null
    
    # Run the Java program with optional argument (3rd argument)
    if [ -z "$3" ]; then
        # No argument provided, default to 1
        sudo java Runner "$2" f
    else
        # Pass the argument provided
        sudo java Runner "$2" "$3" 
    fi

elif [ "$1" == "p" ]; then
    # Python case
    sudo rm powerjoular.*
    sudo javac java_progs/"$2".java
    sudo gcc c_progs/Fib.c -o c_progs/Fib
    
    # Run Python program (name provided as second argument, e.g., MyRunner.py)
    if [ -z "$3" ]; then
        # No argument provided, default to f
        #sudo python3 "$2".py 1
        sudo python3 Pyrun.py "$2" f
    else
        # Pass the argument provided
        #sudo python3 "$2".py "$3"
        sudo python3 Pyrun.py "$2" "$3"
    fi

else
    # If neither "j" nor "p" is provided, display a usage message
    echo "Usage: $0 [j|p] [file name] [optional number]"
    echo "j - Run Java program (file without extension)"
    echo "p - Run Python program (file without extension)"
    echo "Optional number is passed to the program as an argument"
fi
'