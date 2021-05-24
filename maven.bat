SET args=%*
ECHO "mvn -f %args% -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\""
cmd.exe /C mvn -f %args% -Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\"