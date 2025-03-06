#!/bin/bash

javac -cp "/home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar" *.java
java -cp ".:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar" SpoonTemplateExample



#javac -XDignore.symbol.file -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out ListAddTemplate.java


#java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar:out ListAddTemplate

