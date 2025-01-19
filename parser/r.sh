#!/bin/bash

folderName="${1:-"Fib"}"

#javac -d out src/TestObject.java src/TestFile.java src/Test.java

#javac -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/OperatorExtractor.java src/ASTFeatureExtractor.java

javac -XDignore.symbol.file -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/OperatorExtractor.java src/ASTFeatureExtractor.java src/TestObject.java src/TestObject2.java example_dir/ExampleExternalFile.java example_dir/more_depth/MoreDepthFile.java src/TestFile.java src/Test.java
#java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out ASTFeatureExtractor $folderName
java -cp /home/afonso/Documents/EnergyAwareProgramming/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:/home/afonso/Documents/EnergyAwareProgramming/parser/lib/slf4j-simple-2.0.16.jar:out Test