#!/bin/sh
javac -classpath "../build/lib/mr3.jar;../build/lib/jena.jar" *.java
jar cvfm sample.jar META-INF/MANIFEST.MF *.class
