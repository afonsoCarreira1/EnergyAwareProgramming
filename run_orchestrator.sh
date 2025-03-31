##!/bin/bash


cd orchestrator/
#create classpath file for later usage
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single
sudo java -jar target/orchestrator-1.0-SNAPSHOT-jar-with-dependencies.jar