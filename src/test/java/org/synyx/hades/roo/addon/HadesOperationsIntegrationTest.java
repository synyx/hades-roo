package org.synyx.hades.roo.addon;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.PathResolver;
import org.synyx.hades.roo.addon.HadesOperations.EntityType;
import org.synyx.hades.roo.addon.support.AbstractRooIntegrationTests;


/**
 * @author Oliver Gierke - gierke@synyx.de
 */
public class HadesOperationsIntegrationTest extends AbstractRooIntegrationTests {

    private static final String DOMAIN_CLASS = "org.synyx.hades.roo.addon.User";
    private static final String REPOSITORY_INTERFACE = "UserRepository";

    @Autowired
    private PathResolver pathResolver;

    @Autowired
    private ClasspathOperations operations;


    @Test
    public void createsDaoClassCorrectly() throws Exception {

        EntityType type =
                new EntityType(new JavaType(DOMAIN_CLASS), operations);
        ClassOrInterfaceTypeDetails dao = type.createDao(pathResolver);

        assertThat(dao.getName().getSimpleTypeName(), is(REPOSITORY_INTERFACE));

        List<JavaType> parameters =
                dao.getExtendsTypes().get(0).getParameters();

        assertThat(parameters.get(0).getFullyQualifiedTypeName(), is(User.class
                .getName()));
        assertThat(parameters.get(1).getFullyQualifiedTypeName(), is(Long.class
                .getName()));
    }
}
