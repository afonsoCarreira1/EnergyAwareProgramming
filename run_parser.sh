##!/bin/bash


cd parser/
mvn clean compile assembly:single
java -jar target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar