#!/usr/bin/env bash
#export JAVA_HOME=`/usr/libexec/java_home -v 11`
echo "mvn -f $1 -B release:$2 $3 -P gpg -DskipTests -Dgpg.pinentry-mode=default -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\" -Dgoals=\"source:jar-no-fork javadoc:jar deploy\""
mvn -f $1 -B release:$2 $3 -P gpg -DskipTests -Dgpg.pinentry-mode=default -Darguments="-Dgpg.pinentry-mode=default -DskipTests" -Dgoals="source:jar-no-fork javadoc:jar deploy"