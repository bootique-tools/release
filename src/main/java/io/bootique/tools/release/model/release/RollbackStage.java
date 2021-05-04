package io.bootique.tools.release.model.release;

public enum RollbackStage {
    NO_ROLLBACK,
    ROLLBACK_SONATYPE,
    ROLLBACK_MVN;

    public String getText() {
        switch(this) {
            case ROLLBACK_SONATYPE:
                return "Rollback sonatype";
            case ROLLBACK_MVN:
                return "Rollback git";
        }
        return "No rollback";
    }

    public static RollbackStage getNext(RollbackStage releaseStage) {
        if(releaseStage == ROLLBACK_MVN) {
            return ROLLBACK_MVN;
        }
        return RollbackStage.values()[releaseStage.ordinal() + 1];
    }
}
