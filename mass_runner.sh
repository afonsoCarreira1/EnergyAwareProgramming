#!/bin/bash

folderName="${1:-"files_default"}"

sudo mkdir $folderName

sudo mkdir $folderName/python_files_java_fib
sudo mkdir $folderName/java_files_java_fib

sudo mkdir $folderName/python_files_c_fib
sudo mkdir $folderName/java_files_c_fib


./run.sh j Fib f 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/java_files

./run.sh p Fib f 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/python_files

./run.sh j Fib t 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/java_files_c_fib

./run.sh p Fib t 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/python_files_c_fib