##!/bin/bash
#
## Define classpath variables
#SPOON_JAR="./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar"
#SLF4J_JAR="./parser/lib/slf4j-simple-2.0.16.jar"
#JACKSON_JAR="./parser/jackson-core-2.18.2.jar"
#
#find generated_progs/ -type f -delete #remove java programs
#find generated_templates/ -type f -delete #remove java programs
#
#javac -cp "$JACKSON_JAR" java_progs/aux/DeepCopyUtil.java
#javac java_progs/aux/ArrayListAux.java java_progs/aux/WritePid.java java_progs/aux/TemplatesAux.java 
#mv java_progs/aux/DeepCopyUtil.class java_progs/out/java_progs/aux
#mv java_progs/aux/ArrayListAux.class java_progs/out/java_progs/aux
#mv java_progs/aux/TemplatesAux.class java_progs/out/java_progs/aux
#mv java_progs/aux/WritePid.class java_progs/out/java_progs/aux
#
#output_dir=java_progs/out/
#javac -d "$output_dir" java_progs/templates/Template.java
#
## Compile Java files
#javac -cp "$SPOON_JAR" java_progs/templates/TemplateCreator.java java_progs/templates/SpoonInjector.java
#
## Run Java program
#java -cp ".:$SPOON_JAR:$SLF4J_JAR" java_progs/templates/TemplateCreator TestClass

#!/bin/bash

# Navigate to the correct directory (if needed)
#cd "$(dirname "$0")"
#cd codegen/
#mvn dependency:resolve
#mvn exec:java -Dexec.mainClass=com.template.TemplateCreator
#mvn exec:java -Dexec.mainClass="com.template.codegen.TemplateCreator"


#java -jar target/dataprocessor-1.0-SNAPSHOT-jar-with-dependencies.jar


cd codegen/
mvn install
mvn clean compile assembly:single
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar

#java -cp "$(cat classpath.txt):target/classes" com.template.TemplateCreator 2>&1 | tee error.log




