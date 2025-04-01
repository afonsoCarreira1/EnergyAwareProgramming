##!/bin/bash


cd orchestrator/

## in case the parser or codegen project are updated, uncomment these 2 lines
#rm -rf ~/.m2/repository/com/parse/parser  # Delete old parser from Maven repo
#rm -rf ~/.m2/repository/com/template/codegen  # Delete old codegen from Maven repo
#mvn clean compile dependency:purge-local-repository -U  # Force fresh dependencies
## in case the parser or codegen project are updated, uncomment these 2 lines

mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single -U
sudo java -jar target/orchestrator-1.0-SNAPSHOT-jar-with-dependencies.jar


#move files to a log dir of last run
formatted_time=$(date +%Y-%m-%d_%H:%M:%S)
dirName="logs/run_${formatted_time}"
powerjoularDir="${dirName}/powerjoular_files"
tmpDir="${dirName}/tmp_files"
errorDir="${dirName}/error_files"
mkdir -p "$dirName" "$powerjoularDir" "$tmpDir" "$errorDir"
sudo mv powerjoular.* "$powerjoularDir"
sudo mv tmp/* "$tmpDir"
sudo mv src/main/java/com/aux_runtime/error_files/* "$errorDir"
sudo mv logs/runner_logs/* "$dirName"
sudo mv features.csv "$dirName"