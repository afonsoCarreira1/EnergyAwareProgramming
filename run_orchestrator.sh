#!/bin/bash

#compile generated progs
cd codegen
mvn install
cd ..


export MAVEN_OPTS="-Xmx512m -Xms128m -Xss2m"
cd orchestrator/
mvn versions:update-parent versions:update-properties versions:use-latest-releases -DgenerateBackupPoms=false #update pom
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single -U #-Dorg.slf4j.simpleLogger.defaultLogLevel=ERROR
sudo java -jar target/orchestrator-1.0-SNAPSHOT-jar-with-dependencies.jar


#move files to a log dir of last run
formatted_time=$(date +%Y-%m-%d_%H:%M:%S)
dirName="logs/run_${formatted_time}"
powerjoularDir="${dirName}/powerjoular_files"
tmpDir="${dirName}/tmp_files"
errorDir="${dirName}/error_files"
progsDir="${dirName}/prog_files"
sudo mkdir -p "$dirName" "$powerjoularDir" "$tmpDir" "$errorDir" "$progsDir"
sudo mv powerjoular.* "$powerjoularDir"
sudo mv tmp/* "$tmpDir"
sudo mv src/main/java/com/aux_runtime/error_files/* "$errorDir"
sudo mv logs/runner_logs/* "$dirName"
sudo mv features.csv "$dirName"

cd ..
sudo cp -r codegen/src/main/java/com/generated_progs/* orchestrator/"$progsDir" #copy programs ran to log

#set MAVEN_OPTS=-Xss10M
