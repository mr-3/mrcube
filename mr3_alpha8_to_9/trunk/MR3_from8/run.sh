#!/bin/sh

JAVA_HOME=/usr/java/j2sdk1.4.2
MR3_HOME=.

$JAVA_HOME/bin/java -classpath "\
$MR3_HOME/mr3.jar:\
$MR3_HOME/lib/antlr.debug.jar:\
$MR3_HOME/lib/antlr.jar:\
$MR3_HOME/lib/concurrent.jar:\
$MR3_HOME/lib/icu4j.jar:\
$MR3_HOME/lib/jakarta-oro-2.0.5.jar:\
$MR3_HOME/lib/jena.jar:\
$MR3_HOME/lib/jgraph.jar:\
$MR3_HOME/lib/junit.jar:\
$MR3_HOME/lib/log4j-1.2.7.jar:\
$MR3_HOME/lib/rdf-api-2001-01-19.jar:\
$MR3_HOME/lib/xercesImpl.jar:\
$MR3_HOME/lib/xmlParserAPIs.jar:" org.semanticweb.mmm.mr3.MR3
