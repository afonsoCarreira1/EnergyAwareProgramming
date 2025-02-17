#!/bin/bash

sudo rm -r java_progs/progs/* #remove java programs
rm java_progs/out/java_progs/progs/* #remove compiled classes
javac java_progs/CreateTemplates.java
java java_progs/CreateTemplates
#javac -cp ".:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar" java_progs/CreateTemplates.java
#java -cp ".:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar" java_progs.CreateTemplates
echo Deleting old programs...
sleep 5
echo Deleted all old programs!

#compile aux files
javac java_progs/aux/ArrayListAux.java
javac java_progs/aux/WritePid.java java_progs/aux/TemplatesAux.java
mv java_progs/aux/ArrayListAux.class java_progs/out/java_progs/aux
mv java_progs/aux/TemplatesAux.class java_progs/out/java_progs/aux
mv java_progs/aux/WritePid.class java_progs/out/java_progs/aux



echo Compiling all Java programs...
search_dir=java_progs/progs
aux_file=java_progs/aux/TemplatesAux.java
output_dir=java_progs/out

# Create the output directory if it doesn't exist
mkdir -p "$output_dir"

# Collect all .java files into a single list and compile together
find "$search_dir" -name "*.java" > sources.txt
echo "$aux_file" >> sources.txt  # Include the auxiliary file
javac -d "$output_dir" @sources.txt
rm sources.txt
echo Compiled!
