#!/bin/bash

sudo rm powerjoular.*

sudo gcc c_progs/Fib.c -o c_progs/Fib

sudo ./c_progs/Fib &
sleep .5
pid=$(cat c_progs/pidfile.txt | tr -d '\n')
echo $pid

sudo powerjoular -D 0.1 -p "$pid" -f "powerjoular.csv"

sum=$(awk -F',' '{if (NR>1) sum+=$3} END {print sum}' powerjoular.csv-$pid.csv)

echo "Total CPU Power: $sum j"

