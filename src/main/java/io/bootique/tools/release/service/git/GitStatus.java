package io.bootique.tools.release.service.git;

import org.apache.cayenne.ExtendedEnumeration;

public enum GitStatus implements ExtendedEnumeration {
    OK,
    NEED_UPDATE,
    MISSING;

    @Override
    public Object getDatabaseValue() {
        return toString();
    }
}
