#!/bin/bash


cd energy_prediction/
mvn clean install -U
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single
mvn install:install-file -Dfile=target/energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.parse -DartifactId=energy_prediction -Dversion=1.0-SNAPSHOT -Dpackaging=jar
#java -jar target/energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar
cp target/energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar ../ext/server/