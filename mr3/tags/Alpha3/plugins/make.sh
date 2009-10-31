#!/bin/sh
javac -classpath ../build/lib/mr3.jar:../build/lib/jena.jar *.java
jar cvfm sample.jar MANIFEST.MF *.class
