#!/bin/bash

filename="${2:-"Fib"}"
runCProgram="${3:-"f"}"
numberOfRuns="${4:-"1"}"

sudo rm powerjoular.*
sudo gcc c_progs/"$2".c -o c_progs/"$2"
sudo gcc OrchestratorC.c -o OrchestratorC

javac -XDignore.symbol.file -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out parser/src/OperatorExtractor.java parser/src/ASTFeatureExtractor.java java_progs/aux/WritePid.java Runner.java
if [ "$1" == "j" ]; then
    sudo java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar:out Runner $filename $runCProgram $numberOfRuns
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
        sudo java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar:out Runner $removed_suffix $runCProgram $numberOfRuns
    done
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
fi

filename_with_date=$(date +%Y-%m-%d_%H:%M:%S)
mkdir logs/run_$filename_with_date
mkdir logs/run_$filename_with_date/powerjoular_files
mkdir logs/run_$filename_with_date/tmp_files
mkdir logs/run_$filename_with_date/error_files
mv powerjoular.* logs/run_$filename_with_date/powerjoular_files
mv tmp/* logs/run_$filename_with_date/tmp_files
mv errorFiles/* logs/run_$filename_with_date/error_files
mv logs/runner_logs/* logs/run_$filename_with_date
cp features.csv logs/run_$filename_with_date
#sudo rm powerjoular.*
#sudo rm tmp/*
