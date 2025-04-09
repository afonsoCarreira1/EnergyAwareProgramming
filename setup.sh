#!/bin/bash

sudo apt update
sudo apt upgrade

echo installing java
sudo apt install openjdk-17-jdk
echo installed java

echo installing maven
sudo apt install maven
echo installed maven


echo installing python3 and its dependencies
sudo apt install python3 python3-pip
echo "alias py='python3'" >> ~/.bashrc
source ~/.bashrc
sudo apt install python3-venv
echo installed python3 alias as py



#install powerjoular
TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT
echo "Cloning PowerJoular repository..."
git clone https://github.com/joular/powerjoular.git "$TMP_DIR/powerjoular"
cd "$TMP_DIR/powerjoular"
chmod +x install.sh
echo "Running installer..."
./install.sh
echo "Installation complete."



#compile codegen
echo compiling codegen
cd codegen/
sudo find src/main/java/com/generated_progs/ -type d -exec rm -r {} +
sudo find src/main/java/com/generated_templates/ -type d -exec rm -r {} +
mvn install
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single
mvn install:install-file -Dfile=target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.template -DartifactId=codegen -Dversion=1.0-SNAPSHOT -Dpackaging=jar


#compile parser
echo compiling parser
cd ..
cd parser/
mvn clean install -U
mvn clean compile assembly:single 
mvn install:install-file -Dfile=target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.parse -DartifactId=parser -Dversion=1.0-SNAPSHOT -Dpackaging=jar


#compile orchestrator
echo compiling orchestrator
cd ..
export MAVEN_OPTS="-Xmx512m -Xms128m -Xss2m"
cd orchestrator/
mvn versions:update-parent versions:update-properties versions:use-latest-releases -DgenerateBackupPoms=false #update pom
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single -U #-Dorg.slf4j.simpleLogger.defaultLogLevel=ERROR
cd ..
echo all projects compiled

