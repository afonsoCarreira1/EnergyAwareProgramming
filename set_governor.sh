#!/bin/bash

# Function to set the CPU governor
set_governor() {
    local governor=$1
    for cpu in /sys/devices/system/cpu/cpu[0-9]*; do
        echo $governor | sudo tee $cpu/cpufreq/scaling_governor > /dev/null
    done
    echo "CPU governor set to $governor"
}

# Check the argument passed to the script
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 {performance|normal}"
    exit 1
fi

case "$1" in
    performance)
        set_governor "performance"
        ;;
    normal)
        set_governor "schedutil"
        ;;
    *)
        echo "Invalid option: $1"
        echo "Usage: $0 {performance|normal}"
        exit 1
        ;;
esac
