#!/usr/bin/env bash
#export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
args="$*"
echo "mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
mvn -f $args -Darguments="-Dgpg.pinentry-mode=default -DskipTests"