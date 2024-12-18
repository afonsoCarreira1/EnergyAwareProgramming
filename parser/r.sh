#!/bin/bash

#javac -cp /home/afonso/Documents/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/App.java

#java -cp /home/afonso/Documents/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out App

javac -d out src/Test.java

#javac -cp /home/afonso/Documents/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/OperatorExtractor.java

javac -cp /home/afonso/Documents/tese/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar -d out src/OperatorExtractor.java src/ASTFeatureExtractor.java


java -cp /home/afonso/Documents/tese/parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:out ASTFeatureExtractor