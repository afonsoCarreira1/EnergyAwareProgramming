##!/bin/bash

targetProgram="${1:-"lists"}"
targetMethods="${2:-}"

# get all the methods like this -> add,get,size
targetMethods=$(echo "$targetMethods" | sed -E 's/\s*,\s*/,/g' | tr -d ' ')

cd codegen/
sudo find src/main/java/com/generated_progs/ -type d -exec rm -r {} +
sudo find src/main/java/com/generated_templates/ -type d -exec rm -r {} +
mvn install
mvn clean compile assembly:single
mvn install:install-file -Dfile=target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.template -DartifactId=codegen -Dversion=1.0-SNAPSHOT -Dpackaging=jar
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar $targetProgram $targetMethods






