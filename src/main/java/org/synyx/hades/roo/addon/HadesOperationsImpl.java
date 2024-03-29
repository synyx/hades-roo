package org.synyx.hades.roo.addon;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
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


/**
 * Implementation of Hades commands that are available via the Roo shell.
 * 
 * @author Oliver Gierke
 */
@Service
@Component
public class HadesOperationsImpl implements HadesOperations {

    private static final JavaType idType = new JavaType("javax.persistence.Id");
    private static final String GENERIC_DAO_INTERFACE =
            "org.synyx.hades.dao.GenericDao";

    @Reference
    private MetadataService metadataService;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private PathResolver pathResolver;


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


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.synyx.hades.roo.addon.HadesOperations#createRepositoryInterface(org
     * .springframework.roo.model.JavaType,
     * org.springframework.roo.model.JavaPackage)
     */
    public void createRepositoryInterface(JavaType entity,
            JavaPackage daoPackage) {

        ClassOrInterfaceTypeDetails repositoryInterface =
                determineRepositoryInterface(entity, daoPackage);

        classpathOperations.generateClassFile(repositoryInterface);
    }


    /**
     * Returns the complete basic repository interface for the given entity and
     * package. If the given package is {@literal null}, the repository will be
     * created in the entity's package.
     * 
     * @param entity
     * @param repositoryPackage
     * @return
     */
    ClassOrInterfaceTypeDetails determineRepositoryInterface(JavaType entity,
            JavaPackage repositoryPackage) {

        JavaType interfaceType =
                getInterfaceTypeName(entity, repositoryPackage);
        JavaType superType = getGenericDaoSuperType(entity);

        String ressourceIdentifier =
                classpathOperations.getPhysicalLocationCanonicalPath(
                        interfaceType, Path.SRC_MAIN_JAVA);

        String metadataId =
                PhysicalTypeIdentifier.createIdentifier(interfaceType,
                        pathResolver.getPath(ressourceIdentifier));

        ClassOrInterfaceTypeDetailsBuilder builder =
                new ClassOrInterfaceTypeDetailsBuilder(metadataId);
        builder.setPhysicalTypeCategory(PhysicalTypeCategory.INTERFACE);
        builder.setName(interfaceType);
        builder.setModifier(Modifier.PUBLIC);
        builder.addExtendsTypes(superType);

        return builder.build();
    }


    private JavaType getInterfaceTypeName(JavaType entityType,
            JavaPackage repositoryPackage) {

        String interfaceName =
                repositoryPackage == null ? entityType
                        .getFullyQualifiedTypeName() : repositoryPackage + "."
                        + entityType.getSimpleTypeName();

        return new JavaType(interfaceName + "Repository");
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
}