@echo off
SET arg1=%~1
SET arg2=%~2
SET arg3=%~3
echo mvn -f "%arg1%" -B release:%arg2% %arg3% -Pgpg -DskipTests -DignoreSnapshots=true -Dgpg.pinentry-mode=loopback -Darguments="-Dgpg.pinentry-mode=loopback -DskipTests" -Dgoals="source:jar-no-fork javadoc:jar deploy"
cmd.exe /C mvn -f "%arg1%" -B release:%arg2% %arg3% -Pgpg -DskipTests -DignoreSnapshots=true -Dgpg.pinentry-mode=loopback -Darguments="-Dgpg.pinentry-mode=loopback -DskipTests" -Dgoals="source:jar-no-fork javadoc:jar deploy"
