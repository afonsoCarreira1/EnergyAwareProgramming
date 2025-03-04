#!/bin/bash

calculate_total_power() {
    # Initialize the total sum to zero
    local total_sum=0
    sudo rm powerjoular.csv
    # Loop through all files matching the pattern 'powerjoular.csv-*.csv'
    for file in powerjoular.csv-*.csv; do
        # Ensure that file exists before processing
        if [[ -f "$file" ]]; then
            # Sum the third column in the current file and add it to total_sum
            local sum=$(awk -F',' -v frequency="$frequency" '{if (NR>1) sum+=$3 * frequency} END {print sum}' "$file")
            #total_sum=$((total_sum + sum))
            total_sum=$(echo "$total_sum + $sum" | bc)
        fi
    done

    # Output the total sum
    total_sum=$(echo "scale=3; $total_sum / $numberOfRuns" | bc)
    total_time=$(echo "scale=3; $total_time / $numberOfRuns" | bc)

    echo "Average power was ${total_sum}j"
    echo "Average time was ${total_time}s"
}

show_power_for_last_run() {
    sum=$(awk -F',' -v frequency="$frequency" '{if (NR>1) sum+=$3 * frequency} END {print sum}' powerjoular.csv-$child_pid.csv)
    echo $sum
}

#fun to capture start signal
trap_start() {
    start_powerjoular=1
}

#fun to capture stop signal
trap_stop() {
    echo "Stopping powerjoular..."
    #sudo pkill -f "powerjoular"
    sudo kill $powerjoular_pid
    sudo kill $child_pid
    end_time=$(($(date +%s%N) / 1000000)) #$(date +%s)
    time_difference=$(echo "scale=3; ($end_time - $start_time) / 1000" | bc)
    power=$(show_power_for_last_run)
    total_power=$(echo "$total_power + $power" | bc)
    total_time=$(echo "$total_time + $time_difference" | bc)
    echo "CPU Power: $power j"
    echo "Time was ${time_difference}s" 
}

#"java", 
#                "-cp", 
#                "java_progs/out", 
#                "java_progs.progs." + file, 
#                Long.toString(ProcessHandle.current().pid())

get_program_to_run(){
    if [ "$runCProgram" == "f" ]; then
        sudo java -cp java_progs/out java_progs.progs.$filename $myPID &
    else
        sudo ./c_progs/$filename $myPID &
    fi
}

trap 'trap_start' USR1
trap 'trap_stop' USR2

#get parent pid
myPID=$$
total_time=0
total_power=0
filename="${1:-"Fib"}"
runCProgram="${2:-"f"}"
numberOfRuns="${3:-"1"}"
frequency=.1

for (( run=1; run<=numberOfRuns; run++ )); do
    echo "Starting run $run of $numberOfRuns"

    # Reset variable for start signal
    start_powerjoular=0

    # Run the C program in the background
    get_program_to_run
    
    # Wait for USR1 signal to start powerjoular
    echo "Waiting for USR1 signal to start powerjoular..."
    while [ $start_powerjoular -eq 0 ]; do
        sleep 0.1
    done

    # Get the start time for this run
    start_time=$(($(date +%s%N) / 1000000))  #$(date +%s)

    # Get child PID from the file generated by the targeted program
    if [ "$runCProgram" == "f" ]; then
    child_pid=$(cat java_progs/pid.txt | tr -d '\n')
    else
    child_pid=$(cat c_progs/pidfile.txt | tr -d '\n')
    fi
    echo "Child PID: $child_pid"

    # Start powerjoular for the child process
    sudo powerjoular -D "$frequency" -p "$child_pid" -f "powerjoular.csv" &
    powerjoular_pid=$!

    # Wait for USR2 signal to stop powerjoular and calculate the total power
    echo "Monitoring started for run $run. Waiting for USR2 signal to stop..."
    
    # Wait for the C program and powerjoular to complete before starting the next run
    wait $powerjoular_pid

    echo "Completed run $run of $numberOfRuns"
done

final_values=$(calculate_total_power)
echo $final_values