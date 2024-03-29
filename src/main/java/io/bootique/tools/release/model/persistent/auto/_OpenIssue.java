package io.bootique.tools.release.model.persistent.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.exp.property.EntityProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;

import io.bootique.tools.release.model.persistent.Author;
import io.bootique.tools.release.model.persistent.Label;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.persistent.RepositoryNode;

/**
 * Class _OpenIssue was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _OpenIssue extends RepositoryNode {

    private static final long serialVersionUID = 1L;

    public static final String ID_PK_COLUMN = "ID";

    public static final EntityProperty<Author> AUTHOR = PropertyFactory.createEntity("author", Author.class);
    public static final ListProperty<Label> LABELS = PropertyFactory.createList("labels", Label.class);
    public static final EntityProperty<Milestone> MILESTONE = PropertyFactory.createEntity("milestone", Milestone.class);
    public static final EntityProperty<Repository> REPOSITORY = PropertyFactory.createEntity("repository", Repository.class);


    protected Object author;
    protected Object labels;
    protected Object milestone;
    protected Object repository;

    public void setAuthor(Author author) {
        setToOneTarget("author", author, true);
    }

    public Author getAuthor() {
        return (Author)readProperty("author");
    }

    public void addToLabels(Label obj) {
        addToManyTarget("labels", obj, true);
    }

    public void removeFromLabels(Label obj) {
        removeToManyTarget("labels", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Label> getLabels() {
        return (List<Label>)readProperty("labels");
    }

    public void setMilestone(Milestone milestone) {
        setToOneTarget("milestone", milestone, true);
    }

    public Milestone getMilestone() {
        return (Milestone)readProperty("milestone");
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
            case "author":
                return this.author;
            case "labels":
                return this.labels;
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
            case "author":
                this.author = val;
                break;
            case "labels":
                this.labels = val;
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
        out.writeObject(this.author);
        out.writeObject(this.labels);
        out.writeObject(this.milestone);
        out.writeObject(this.repository);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.author = in.readObject();
        this.labels = in.readObject();
        this.milestone = in.readObject();
        this.repository = in.readObject();
    }

}
