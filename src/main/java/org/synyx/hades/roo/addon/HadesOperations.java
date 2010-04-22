package org.synyx.hades.roo.addon;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;


/**
 * Implementation of Hades commands that are available via the Roo shell.
 * 
 * @author Oliver Gierke
 */
@ScopeDevelopment
public class HadesOperations {

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


    public void createDaoClass(JavaType entity, JavaPackage daoPackage) {

        EntityType entityType =
                new EntityType(entity, daoPackage, classpathOperations);
        ClassOrInterfaceTypeDetails daoInterface =
                entityType.createDao(pathResolver);
        classpathOperations.generateClassFile(daoInterface);
    }

    static class EntityType {

        private static final String DAO_POSTFIX = "Repository";

        private ClasspathOperations operations;
        private JavaType entityType;
        private JavaPackage daoPackage;


        public EntityType(JavaType type, JavaPackage daoPackage,
                ClasspathOperations operations) {

            this.entityType = type;
            this.operations = operations;

            this.daoPackage =
                    daoPackage != null ? daoPackage : entityType.getPackage();
        }


        public JavaType getGenericDaoInterface() {

            JavaType entityIdType = getIdType(entityType);

            return new JavaType("org.synyx.hades.dao.GenericDao", 0,
                    DataType.TYPE, null, Arrays
                            .asList(entityType, entityIdType));
        }


        public JavaType getDaoInterfaceName() {

            return new JavaType(daoPackage + "."
                    + entityType.getSimpleTypeName() + DAO_POSTFIX);
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