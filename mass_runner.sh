#!/bin/bash

folderName="${1:-"files_default"}"
programName="${2:-"Fib"}"

start_time=$(($(date +%s%N) / 1000000))

sudo mkdir $folderName

# Define the array
arr=("j" "p" "c" "b")

# Loop through each element in the array
for orchestrator in "${arr[@]}"
do
    #sudo mkdir ${folderName}/${orchestrator}_files
    sudo mkdir ${folderName}/${orchestrator}_files_java_${programName}
    sudo mkdir ${folderName}/${orchestrator}_files_c_${programName}
    ./run.sh $orchestrator $programName f 100
    sleep .2
    sudo rm powerjoular.csv
    sudo mv powerjoular.csv* $folderName/${orchestrator}_files_java_${programName}
    sleep 5
    ./run.sh $orchestrator $programName t 100
    sleep .2
    sudo rm powerjoular.csv
    sudo mv powerjoular.csv* $folderName/${orchestrator}_files_c_${programName}
done


end_time=$(($(date +%s%N) / 1000000))
time_difference=$(echo "scale=3; ($end_time - $start_time) / 1000" | bc)
echo Mass runner elapsed time ${time_difference}s
