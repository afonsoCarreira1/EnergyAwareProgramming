#!/bin/bash

folderName="${1:-"Fib"}"

javac -d out src/TestObject.java src/Test.java

javac -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/OperatorExtractor.java src/ASTFeatureExtractor.java


java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out ASTFeatureExtractor $folderName
