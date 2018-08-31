package io.bootique.tools.release.service.bintray.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.bintray.DefaultBintrayApi;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.preferences.PreferenceService;

@BQConfig
public class BintrayCredentialFactory {

    private String username;
    private String password;
    private Integer close;

    @BQConfigProperty("Maven central username.")
    public void setUsername(String username) {
        this.username = username;
    }

    @BQConfigProperty("Maven central password.")
    public void setPassword(String password) {
        this.password = password;
    }

    @BQConfigProperty("Parameter to close maven central repo.")
    public void setClose(Integer close) {
        this.close = close;
    }

    public BintrayApi createBintrayApi(HttpTargets httpTargets, ObjectMapper objectMapper, PreferenceService preferenceService){
        if(username == null) {
            throw new DesktopException("Can't find maven central username.");
        }
        if(password == null) {
            throw new DesktopException("Can't find maven central password.");
        }
        if(close == null) {
            throw new DesktopException("Can't find close parameter for maven central.");
        }

        return new DefaultBintrayApi(httpTargets, objectMapper, preferenceService, username, password, close);
    }
}
