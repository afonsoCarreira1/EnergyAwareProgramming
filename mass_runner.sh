#!/bin/bash

folderName="${1:-"files_default"}"

sudo mkdir $folderName

sudo mkdir $folderName/bash_files
sudo mkdir $folderName/c_files

sudo mkdir $folderName/bash_files_c_fib
sudo mkdir $folderName/c_files_c_fib

sleep 1

./run.sh b Fib f 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/bash_files

sleep 5

./run.sh c Fib f 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/c_files

sleep 5

./run.sh b Fib t 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/bash_files_c_fib

sleep 5

./run.sh c Fib t 100
sudo rm powerjoular.csv
sudo mv powerjoular.csv* $folderName/c_files_c_fib