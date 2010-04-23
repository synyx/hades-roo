package org.synyx.hades.roo.addon;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;
import org.synyx.hades.dao.GenericDao;


/**
 * Implementation of Hades commands that are available via the Roo shell.
 * 
 * @author Oliver Gierke
 */
@ScopeDevelopment
class HadesOperations {

    private static final JavaType idType = new JavaType(Id.class.getName());

    private static final String GENERIC_DAO_INTERFACE =
            GenericDao.class.getName();

    private final MetadataService metadataService;
    private final ClasspathOperations classpathOperations;
    private final PathResolver pathResolver;


    public HadesOperations(MetadataService metadataService,
            ClasspathOperations classpathOperations, PathResolver pathResolver) {

        Assert.notNull(metadataService, "Metadata service required");
        Assert.notNull(classpathOperations);
        Assert.notNull(pathResolver);

        this.metadataService = metadataService;
        this.classpathOperations = classpathOperations;
        this.pathResolver = pathResolver;

    }


    /**
     * Looks up the id type of the given entity type. The lookup process favours
     * {@link EntityMetadata} field or method but also checks for a plain
     * {@link Id} annotated property, too.
     * 
     * @param entityType
     * @return
     */
    JavaType getIdType(JavaType entityType) {

        EntityMetadata entityMetadata =
                (EntityMetadata) metadataService.get(EntityMetadata
                        .createIdentifier(entityType, Path.SRC_MAIN_JAVA));

        if (entityMetadata != null) {

            FieldMetadata field = entityMetadata.getIdentifierField();

            if (field != null) {
                return field.getFieldType();
            }

            MethodMetadata method = entityMetadata.getIdentifierAccessor();

            if (method != null) {
                return method.getReturnType();
            }
        }

        ClassOrInterfaceTypeDetails details =
                classpathOperations.getClassOrInterface(entityType);

        List<FieldMetadata> fieldsWithAnnotation =
                MemberFindingUtils.getFieldsWithAnnotation(details, idType);

        if (!fieldsWithAnnotation.isEmpty()) {
            return fieldsWithAnnotation.get(0).getFieldType();
        }

        throw new IllegalStateException(
                "Could not find an @Id property or method in your entity class! Be sure it has one.");
    }


    public void createDaoClass(JavaType entity, JavaPackage daoPackage) {

        ClassOrInterfaceTypeDetails daoInterface =
                determineRepositoryInterface(entity, daoPackage);

        classpathOperations.generateClassFile(daoInterface);
    }


    /**
     * @param entity
     * @param daoPackage
     * @return
     */
    ClassOrInterfaceTypeDetails determineRepositoryInterface(JavaType entity,
            JavaPackage daoPackage) {

        EntityType entityType = new EntityType(entity, daoPackage);

        JavaType interfaceName = entityType.getDaoInterfaceName();
        JavaType superType = getGenericDaoSuperType(entity);

        String ressourceIdentifier =
                classpathOperations.getPhysicalLocationCanonicalPath(
                        interfaceName, Path.SRC_MAIN_JAVA);

        String metadataId =
                PhysicalTypeIdentifier.createIdentifier(interfaceName,
                        pathResolver.getPath(ressourceIdentifier));

        ClassOrInterfaceTypeDetails daoInterface =
                new DefaultClassOrInterfaceTypeDetails(metadataId,
                        interfaceName, Modifier.PUBLIC,
                        PhysicalTypeCategory.INTERFACE, null, null, null, null,
                        Arrays.asList(superType), null, null, null);

        return daoInterface;
    }


    /**
     * Returns the generic supertype typed to the given entity type.
     * 
     * @param entityType
     * @return
     */
    JavaType getGenericDaoSuperType(JavaType entityType) {

        JavaType entityIdType = getIdType(entityType);

        return new JavaType(GENERIC_DAO_INTERFACE, 0, DataType.TYPE, null,
                Arrays.asList(entityType, entityIdType));
    }

    static class EntityType {

        private static final String DAO_POSTFIX = "Repository";

        private final JavaType entityType;
        private final JavaPackage daoPackage;


        /**
         * Creates a new {@link EntityType}.
         * 
         * @param type
         * @param daoPackage
         */
        public EntityType(JavaType type, JavaPackage daoPackage) {

            this.entityType = type;
            this.daoPackage =
                    daoPackage != null ? daoPackage : entityType.getPackage();
        }


        public JavaType getDaoInterfaceName() {

            return new JavaType(daoPackage + "."
                    + entityType.getSimpleTypeName() + DAO_POSTFIX);
        }
    }
}