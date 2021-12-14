package io.bootique.tools.release.model.release;

public enum ReleaseStageStatus {
    Not_Start("Stage not start"),
    In_Progress("Stage in progress"),
    Success("Stage successfully finished"),
    Fail("Stage failed"),
    Reload("Stage reloaded"),
    Skip("Stage skipped"),

    Rollback("Stage rollback"),
    Fail_Rollback("Stage rollback failed");


    private final String message;

    ReleaseStageStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
