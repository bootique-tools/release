package io.bootique.tools.release.model.persistent.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.cayenne.exp.Property;

import io.bootique.tools.release.model.persistent.GitHubEntity;
import io.bootique.tools.release.model.persistent.Issue;
import io.bootique.tools.release.model.persistent.Repository;

/**
 * Class _Milestone was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Milestone extends GitHubEntity {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "ID";

    public static final Property<Integer> NUMBER = Property.create("number", Integer.class);
    public static final Property<String> STATE = Property.create("state", String.class);
    public static final Property<String> TITLE = Property.create("title", String.class);
    public static final Property<List<Issue>> ISSUES = Property.create("issues", List.class);
    public static final Property<List<Issue>> ISSUES_LIST = Property.create("issuesList", List.class);
    public static final Property<List<Repository>> MILESTONE = Property.create("milestone", List.class);
    public static final Property<Repository> REPOSITORY = Property.create("repository", Repository.class);

    protected Integer number;
    protected String state;
    protected String title;

    protected Object issues;
    @JsonProperty("issuesList")
    protected Object issuesList;
    protected Object milestone;
    @JsonIgnore
    protected Object repository;

    public void setNumber(int number) {
        beforePropertyWrite("number", this.number, number);
        this.number = number;
    }

    public int getNumber() {
        beforePropertyRead("number");
        if(this.number == null) {
            return 0;
        }
        return this.number;
    }

    public void setState(String state) {
        beforePropertyWrite("state", this.state, state);
        this.state = state;
    }

    public String getState() {
        beforePropertyRead("state");
        return this.state;
    }

    public void setTitle(String title) {
        beforePropertyWrite("title", this.title, title);
        this.title = title;
    }

    public String getTitle() {
        beforePropertyRead("title");
        return this.title;
    }

    public void addToIssues(Issue obj) {
        addToManyTarget("issues", obj, true);
    }

    public void removeFromIssues(Issue obj) {
        removeToManyTarget("issues", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Issue> getIssues() {
        return (List<Issue>)readProperty("issues");
    }

    public void addToIssuesList(Issue obj) {
        addToManyTarget("issuesList", obj, true);
    }

    public void removeFromIssuesList(Issue obj) {
        removeToManyTarget("issuesList", obj, true);
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("issuesList")
    public List<Issue> getIssuesList() {
        return (List<Issue>)readProperty("issuesList");
    }

    public void addToMilestone(Repository obj) {
        addToManyTarget("milestone", obj, true);
    }

    public void removeFromMilestone(Repository obj) {
        removeToManyTarget("milestone", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Repository> getMilestone() {
        return (List<Repository>)readProperty("milestone");
    }

    public void setRepository(Repository repository) {
        setToOneTarget("repository", repository, true);
    }

    public Repository getRepository() {
        return (Repository)readProperty("repository");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "number":
                return this.number;
            case "state":
                return this.state;
            case "title":
                return this.title;
            case "issues":
                return this.issues;
            case "issuesList":
                return this.issuesList;
            case "milestone":
                return this.milestone;
            case "repository":
                return this.repository;
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
            case "number":
                this.number = (Integer)val;
                break;
            case "state":
                this.state = (String)val;
                break;
            case "title":
                this.title = (String)val;
                break;
            case "issues":
                this.issues = val;
                break;
            case "issuesList":
                this.issuesList = val;
                break;
            case "milestone":
                this.milestone = val;
                break;
            case "repository":
                this.repository = val;
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
        out.writeObject(this.number);
        out.writeObject(this.state);
        out.writeObject(this.title);
        out.writeObject(this.issues);
        out.writeObject(this.issuesList);
        out.writeObject(this.milestone);
        out.writeObject(this.repository);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.number = (Integer)in.readObject();
        this.state = (String)in.readObject();
        this.title = (String)in.readObject();
        this.issues = in.readObject();
        this.issuesList = in.readObject();
        this.milestone = in.readObject();
        this.repository = in.readObject();
    }

}
