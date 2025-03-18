#!/bin/bash

sudo find generated_progs/ -type f -delete #remove java programs
sudo find generated_templates/ -type f -delete #remove java programs

javac java_progs/aux/ArrayListAux.java java_progs/aux/WritePid.java java_progs/aux/TemplatesAux.java
mv java_progs/aux/ArrayListAux.class java_progs/out/java_progs/aux
mv java_progs/aux/TemplatesAux.class java_progs/out/java_progs/aux
mv java_progs/aux/WritePid.class java_progs/out/java_progs/aux

output_dir=java_progs/out/
javac -d "$output_dir" java_progs/templates/Template.java

#javac -cp "./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar" java_progs/templates/TemplateCreator.java java_progs/templates/SpoonInjector.java
#java -cp ".:./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:./parser/lib/slf4j-simple-2.0.16.jar" java_progs/templates/TemplateCreator.java


# Define classpath variables
SPOON_JAR="./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar"
SLF4J_JAR="./parser/lib/slf4j-simple-2.0.16.jar"

# Compile Java files
javac -cp "$SPOON_JAR" java_progs/templates/TemplateCreator.java java_progs/templates/SpoonInjector.java

# Run Java program
java -cp ".:$SPOON_JAR:$SLF4J_JAR" java_progs/templates/TemplateCreator