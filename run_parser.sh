##!/bin/bash


cd parser/
mvn clean install -U
mvn clean compile assembly:single 
mvn install:install-file -Dfile=target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.parse -DartifactId=parser -Dversion=1.0-SNAPSHOT -Dpackaging=jar
java -jar target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar