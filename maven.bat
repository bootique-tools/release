SET args=%*
cmd.exe /C mvn -f %args% -Darguments="-Dgpg.pinentry-mode=default -DskipTests"