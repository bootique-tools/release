package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._User;

public class User extends _User {

    private static final long serialVersionUID = 1L;

    @JsonProperty("__typename")
    @Override
    public String getType() {
        return super.getType();
    }
}
