package org.synyx.hades.roo.addon;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;
import org.synyx.hades.domain.AbstractPersistable;
import org.synyx.hades.domain.Persistable;
import org.synyx.hades.domain.auditing.AbstractAuditable;
import org.synyx.hades.domain.auditing.Auditable;


/**
 * Implementation of commands that are available via the Roo shell.
 * 
 * @author Ben Alex
 * @since 1.0
 */
@ScopeDevelopment
public class HadesOperations {

    private static final Map<Class<?>, Integer> classesMap =
            new HashMap<Class<?>, Integer>();
    private static final Map<Class<?>, Integer> interfacesMap =
            new HashMap<Class<?>, Integer>();

    static {
        classesMap.put(AbstractAuditable.class, 1);
        classesMap.put(AbstractPersistable.class, 0);
        interfacesMap.put(Persistable.class, 0);
        interfacesMap.put(Auditable.class, 1);
    }

    // private static Logger logger =
    // Logger.getLogger(HadesOperations.class.getName());

    private static final JavaType idType = new JavaType("javax.persistence.Id");

    private MetadataService metadataService;
    private ClasspathOperations classpathOperations;
    private PathResolver pathResolver;


    public HadesOperations(MetadataService metadataService,
            ClasspathOperations classpathOperations, PathResolver pathResolver) {

        Assert.notNull(metadataService, "Metadata service required");
        Assert.notNull(classpathOperations);
        Assert.notNull(pathResolver);

        this.metadataService = metadataService;
        this.classpathOperations = classpathOperations;
        this.pathResolver = pathResolver;

    }


    public boolean isProjectAvailable() {

        return getPathResolver() != null;
    }


    /**
     * @param propertyName to obtain (required)
     * @return a message that will ultimately be displayed on the shell
     */
    public String getProperty(PropertyName propertyName) {

        Assert.notNull(propertyName, "Property name required");
        String internalName = "user.name";
        if (PropertyName.HOME_DIRECTORY.equals(propertyName)) {
            internalName = "user.home";
        }
        return propertyName.getPropertyName() + " : "
                + System.getProperty(internalName);
    }


    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {

        ProjectMetadata projectMetadata =
                (ProjectMetadata) metadataService.get(ProjectMetadata
                        .getProjectIdentifier());
        if (projectMetadata == null) {
            return null;
        }
        return projectMetadata.getPathResolver();
    }


    public void createDaoClass(JavaType entity) {

        EntityType entityType = new EntityType(entity, classpathOperations);
        ClassOrInterfaceTypeDetails daoInterface =
                entityType.createDao(pathResolver);
        classpathOperations.generateClassFile(daoInterface);
    }

    public static class EntityType {

        private static final String DAO_POSTFIX = "Repository";

        private ClasspathOperations operations;
        private JavaType entityType;


        public EntityType(JavaType type, ClasspathOperations operations) {

            this.entityType = type;
            this.operations = operations;
        }


        public JavaType getGenericDaoInterface() {

            JavaType entityIdType = getIdType(entityType);

            return new JavaType("org.synyx.hades.dao.GenericDao", 0,
                    DataType.TYPE, null, Arrays
                            .asList(entityType, entityIdType));
        }


        public JavaType getDaoInterfaceName() {

            return new JavaType(entityType.getFullyQualifiedTypeName()
                    + DAO_POSTFIX);
        }


        public ClassOrInterfaceTypeDetails createDao(PathResolver pathResolver) {

            JavaType interfaceName = getDaoInterfaceName();

            String ressourceIdentifier =
                    operations.getPhysicalLocationCanonicalPath(interfaceName,
                            Path.SRC_MAIN_JAVA);

            String metadataId =
                    PhysicalTypeIdentifier.createIdentifier(interfaceName,
                            pathResolver.getPath(ressourceIdentifier));

            return new DefaultClassOrInterfaceTypeDetails(metadataId,
                    interfaceName, Modifier.PUBLIC,
                    PhysicalTypeCategory.INTERFACE, null, null, null, null,
                    Arrays.asList(getGenericDaoInterface()), null, null, null);
        }


        /**
         * Recursively resolves the ID type for the given type expecting it
         * either {@code Persistable} or {@code Auditable}.
         * 
         * @param type
         * @return
         */
        private JavaType getIdType(JavaType javaType) {

            ClassOrInterfaceTypeDetails details =
                    operations.getClassOrInterface(javaType);
            FieldMetadata metadata =
                    MemberFindingUtils.getFieldsWithAnnotation(details, idType)
                            .get(0);

            return metadata.getFieldType();
        }
    }
}