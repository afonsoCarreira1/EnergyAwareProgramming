#!/bin/bash

javac java_progs/aux/ArrayListAux.java java_progs/aux/WritePid.java java_progs/aux/TemplatesAux.java
mv java_progs/aux/ArrayListAux.class java_progs/out/java_progs/aux
mv java_progs/aux/TemplatesAux.class java_progs/out/java_progs/aux
mv java_progs/aux/WritePid.class java_progs/out/java_progs/aux

output_dir=java_progs/out/
javac -d "$output_dir" java_progs/templates/Template.java

javac -cp "./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar" java_progs/templates/TemplateCreator.java java_progs/templates/SpoonInjector.java
java -cp ".:./parser/lib/spoon-core-11.1.1-beta-18-jar-with-dependencies.jar:./parser/lib/slf4j-simple-2.0.16.jar" java_progs/templates/TemplateCreator.java