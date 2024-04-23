#!/usr/bin/env bash
export JAVA_HOME=$($1)
shift
args="$*"
echo "mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
mvn -f $args -Darguments="-Dgpg.pinentry-mode=default -DskipTests"