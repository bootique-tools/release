package io.bootique.tools.release.model.release;

public enum RollbackStage {
    NO_ROLLBACK,
    ROLLBACK_BINTRAY,
    ROLLBACK_MVN;

    public String getText() {
        switch(this) {
            case ROLLBACK_BINTRAY:
                return "Rollback bintray";
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
