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
    private final GitHubEntity entityFromDb;
    private final GitHubEntity entity;
    private final boolean syncAttributes;

    public MergingAttributeVisitor(ObjectContext context, GitHubEntity entityFromDb, GitHubEntity entity, boolean syncAttributes) {
        this.context = context;
        this.entityFromDb = entityFromDb;
        this.entity = entity;
        this.syncAttributes = syncAttributes;
    }

    @Override
    public boolean visitAttribute(AttributeProperty property) {
        if (!syncAttributes) {
            return true;
        }
        Object oldValue = property.readPropertyDirectly(entityFromDb);
        Object newValue = property.readPropertyDirectly(entity);
        property.writeProperty(entityFromDb, oldValue, newValue);
        return true;
    }

    @Override
    public boolean visitToOne(ToOneProperty property) {
        Class<GitHubEntity> targetEntityType = getGitHubEntityClass(property);
        if (targetEntityType == null) {
            return true;
        }

        Object oldValue = property.readProperty(entityFromDb);
        Object newValue = property.readProperty(entity);

        if (newValue == null) {
            property.writeProperty(entityFromDb, oldValue, null);
        } else {
            GitHubEntity newEntity = GitHubDataImportJob
                    .syncEntity(context, targetEntityType, (GitHubEntity) newValue);
            property.writeProperty(entityFromDb, oldValue, newEntity);
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
        List<GitHubEntity> oldValue = (List<GitHubEntity>) property.readProperty(entityFromDb);
        @SuppressWarnings("unchecked")
        List<GitHubEntity> newValue = (List<GitHubEntity>) property.readProperty(entity);

        if (oldValue != null) {
            oldValue = new ArrayList<>(oldValue);
            if (entityFromDb.getObjectContext() != null) {
                for (GitHubEntity next : oldValue) {
                    property.removeTarget(entityFromDb, next, false);
                }
            } else {
                property.writePropertyDirectly(entityFromDb, null, new ArrayList<>());
            }
        }

        if (newValue != null) {
            newValue = new ArrayList<>(newValue);
            List<GitHubEntity> processed = new ArrayList<>(newValue.size());
            for (GitHubEntity next : newValue) {
                processed.add(GitHubDataImportJob.syncEntity(context, targetEntityType, next));
            }

            if (entityFromDb.getObjectContext() == null) {
                property.writePropertyDirectly(entityFromDb, null, processed);
            } else {
                for (GitHubEntity next : processed) {
                    property.addTarget(entityFromDb, next, false);
                }
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
