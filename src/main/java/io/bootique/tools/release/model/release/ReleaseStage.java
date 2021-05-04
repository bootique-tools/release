package io.bootique.tools.release.model.release;

public enum ReleaseStage {
    NO_RELEASE,
    RELEASE_PULL,
    RELEASE_INSTALL,
    RELEASE_PREPARE_PERFORM,
    RELEASE_SYNC;

    public String getText() {
        switch(this) {
            case RELEASE_PULL:
                return "Release pull";
            case RELEASE_INSTALL:
                return "Release install";
            case RELEASE_PREPARE_PERFORM:
                return "Release prepare, perform";
            case RELEASE_SYNC:
                return "Release sync";
        }
        return "No release";
    }

    public static ReleaseStage getNext(ReleaseStage releaseStage) {
        if(releaseStage == ReleaseStage.RELEASE_SYNC) {
            return ReleaseStage.RELEASE_SYNC;
        }
        return ReleaseStage.values()[releaseStage.ordinal() + 1];
    }
}
