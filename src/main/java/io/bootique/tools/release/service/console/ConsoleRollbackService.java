package io.bootique.tools.release.service.console;

public interface ConsoleRollbackService {

    boolean checkReadyForRollback();

    void startRollbackFromConsole();

}
