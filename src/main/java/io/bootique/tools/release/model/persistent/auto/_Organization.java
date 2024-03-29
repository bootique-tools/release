package io.bootique.tools.release.model.persistent.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import io.bootique.tools.release.model.persistent.GitHubEntity;
import io.bootique.tools.release.model.persistent.Repository;

/**
 * Class _Organization was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Organization extends GitHubEntity {

    private static final long serialVersionUID = 1L;

    public static final String ID_PK_COLUMN = "ID";

    public static final StringProperty<String> LOGIN = PropertyFactory.createString("login", String.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<Repository> REPOSITORIES = PropertyFactory.createList("repositories", Repository.class);

    protected String login;
    protected String name;

    protected Object repositories;

    public void setLogin(String login) {
        beforePropertyWrite("login", this.login, login);
        this.login = login;
    }

    public String getLogin() {
        beforePropertyRead("login");
        return this.login;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToRepositories(Repository obj) {
        addToManyTarget("repositories", obj, true);
    }

    public void removeFromRepositories(Repository obj) {
        removeToManyTarget("repositories", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Repository> getRepositories() {
        return (List<Repository>)readProperty("repositories");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "login":
                return this.login;
            case "name":
                return this.name;
            case "repositories":
                return this.repositories;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "login":
                this.login = (String)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "repositories":
                this.repositories = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.login);
        out.writeObject(this.name);
        out.writeObject(this.repositories);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.login = (String)in.readObject();
        this.name = (String)in.readObject();
        this.repositories = in.readObject();
    }

}
