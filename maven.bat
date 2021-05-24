SET args=%*
ECHO "mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
cmd.ext /C mvn -f $args -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\"