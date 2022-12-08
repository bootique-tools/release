#!/usr/bin/env bash
#export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
echo "mvn -f $0 release:$1 -B -Pgpg -Dgoals=\"source:jar-no-fork javadoc:jar deploy\" -Dgpg.pinentry-mode=default -DskipTests -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
mvn -f $0 release:$1 -B -Pgpg -Dgoals="source:jar-no-fork javadoc:jar deploy" -Dgpg.pinentry-mode=default -DskipTests -Darguments="-Dgpg.pinentry-mode=default -DskipTests"