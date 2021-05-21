package io.bootique.tools.release.job;

import java.util.ArrayList;
import java.util.List;

import io.bootique.tools.release.model.persistent.GitHubEntity;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.reflect.ArcProperty;
import org.apache.cayenne.reflect.AttributeProperty;
import org.apache.cayenne.reflect.PropertyVisitor;
import org.apache.cayenne.reflect.ToManyProperty;
import org.apache.cayenne.reflect.ToOneProperty;

class MergingAttributeVisitor implements PropertyVisitor {
    private final ObjectContext context;
    private final GitHubEntity entityFrom;
    private final GitHubEntity entityTo;
    private final boolean fromDb;

    public MergingAttributeVisitor(ObjectContext context, GitHubEntity entityFrom, GitHubEntity entityTo, boolean fromDb) {
        this.context = context;
        this.entityFrom = entityFrom;
        this.entityTo = entityTo;
        this.fromDb = fromDb;
    }

    @Override
    public boolean visitAttribute(AttributeProperty property) {
        if (!fromDb) {
            return true;
        }
        Object oldValue = property.readPropertyDirectly(entityTo);
        Object newValue = property.readPropertyDirectly(entityFrom);
        if(oldValue != newValue) {
            property.writeProperty(entityTo, oldValue, newValue);
        }
        return true;
    }

    @Override
    public boolean visitToOne(ToOneProperty property) {
        Class<GitHubEntity> targetEntityType = getGitHubEntityClass(property);
        if (targetEntityType == null) {
            return true;
        }

        Object oldValue = property.readProperty(entityTo);
        Object newValue = property.readProperty(entityFrom);

        if(property.getRelationship().isMandatory() && newValue == null) {
            return true;
        }

        if (newValue == null) {
            if(fromDb) {
                property.writeProperty(entityTo, oldValue, null);
            } else {
                property.writePropertyDirectly(entityTo, oldValue, null);
            }
        } else {
            GitHubEntity newEntity = GitHubDataImportJob
                    .syncEntity(context, targetEntityType, (GitHubEntity) newValue);
            if(fromDb) {
                property.writeProperty(entityTo, oldValue, newEntity);
            } else {
                property.writePropertyDirectly(entityTo, oldValue, newEntity);
            }
        }
        return true;
    }

    @Override
    public boolean visitToMany(ToManyProperty property) {
        Class<GitHubEntity> targetEntityType = getGitHubEntityClass(property);
        if (targetEntityType == null) {
            return true;
        }

        @SuppressWarnings("unchecked")
        List<GitHubEntity> newValue = (List<GitHubEntity>) property.readProperty(entityFrom);
        if(newValue == null) {
            // skip uninitialized relationships
            return true;
        }

        // remove old values
        @SuppressWarnings("unchecked")
        List<GitHubEntity> oldValue = (List<GitHubEntity>) property.readProperty(entityTo);
        if (oldValue != null) {
            oldValue = new ArrayList<>(oldValue);
            if (entityTo.getObjectContext() != null) {
                for (GitHubEntity next : oldValue) {
                    property.removeTarget(entityTo, next, false);
                }
            } else {
                property.writePropertyDirectly(entityTo, null, new ArrayList<>());
            }
        }

        // set new data
        newValue = new ArrayList<>(newValue);
        List<GitHubEntity> processed = new ArrayList<>(newValue.size());
        for (GitHubEntity next : newValue) {
            processed.add(GitHubDataImportJob.syncEntity(context, targetEntityType, next));
        }

        if (entityTo.getObjectContext() == null) {
            property.writePropertyDirectly(entityTo, null, processed);
        } else {
            for (GitHubEntity next : processed) {
                property.addTarget(entityTo, next, false);
            }
        }

        return true;
    }

    private Class<GitHubEntity> getGitHubEntityClass(ArcProperty property) {
        String javaClassName = property.getTargetDescriptor().getEntity().getJavaClassName();
        Class<?> targetClass;
        try {
            targetClass = Class.forName(javaClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to resolve java class " + javaClassName, e);
        }

        if (!GitHubEntity.class.isAssignableFrom(targetClass)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<GitHubEntity> targetEntityType = (Class<GitHubEntity>) targetClass;
        return targetEntityType;
    }
}
