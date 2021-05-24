#!/usr/bin/env bash

args="$*"
echo "mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\"