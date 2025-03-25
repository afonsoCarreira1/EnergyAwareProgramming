##!/bin/bash


cd orchestrator/
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single
java -jar target/orchestrator-1.0-SNAPSHOT-jar-with-dependencies.jar