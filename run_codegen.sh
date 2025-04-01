##!/bin/bash

targetProgram="${1:-"lists"}"
targetMethod="${2:-}"

cd codegen/
sudo find src/main/java/com/generated_progs/ -type d -exec rm -r {} +
sudo find src/main/java/com/generated_templates/ -type f -delete #remove java templates
mvn install
mvn clean compile assembly:single
mvn install:install-file -Dfile=target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.template -DartifactId=codegen -Dversion=1.0-SNAPSHOT -Dpackaging=jar
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar $targetProgram $targetMethod






