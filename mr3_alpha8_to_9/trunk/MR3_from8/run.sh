#!/bin/sh

JAVA_HOME=/usr/java/j2sdk1.4.2
MR3_HOME=.
cd $MR3_HOME

$JAVA_HOME/bin/java -classpath "\
$MR3_HOME/build/mr3.jar:\
$MR3_HOME/build/lib/antlr.debug.jar:\
$MR3_HOME/build/lib/antlr.jar:\
$MR3_HOME/build/lib/concurrent.jar:\
$MR3_HOME/build/lib/icu4j.jar:\
$MR3_HOME/build/lib/jakarta-oro-2.0.5.jar:\
$MR3_HOME/build/lib/jena.jar:\
$MR3_HOME/build/lib/jgraph.jar:\
$MR3_HOME/build/lib/junit.jar:\
$MR3_HOME/build/lib/log4j-1.2.7.jar:\
$MR3_HOME/build/lib/rdf-api-2001-01-19.jar:\
$MR3_HOME/build/lib/xercesImpl.jar:\
$MR3_HOME/build/lib/xmlParserAPIs.jar:" org.semanticweb.mmm.mr3.MR3
