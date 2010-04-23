package org.synyx.hades.roo.addon;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;
import org.synyx.hades.roo.addon.support.AbstractRooIntegrationTests;


/**
 * Intergration test for {@link HadesOperations}.
 * 
 * @author Oliver Gierke
 */
public class HadesOperationsIntegrationTest extends AbstractRooIntegrationTests {

    private static final String PACKAGE = "org.synyx.hades.roo.addon";
    private static final JavaType ID_TYPE = new JavaType("java.lang.Long");
    private static final JavaType DOMAIN_CLASS =
            new JavaType(PACKAGE + ".User");
    private static final JavaType REPOSITORY_INTERFACE =
            new JavaType(PACKAGE + ".UserRepository");

    @Autowired
    private HadesOperations hades;


    @Test
    public void discoveresIdCorrectly() throws Exception {

        assertThat(hades.getIdType(DOMAIN_CLASS), is(ID_TYPE));
    }


    @Test
    public void createsRepositorySuprtInterfaceCorrectly() throws Exception {

        JavaType repositorySupterType =
                hades.getGenericDaoSuperType(DOMAIN_CLASS);

        assertThat(repositorySupterType.getParameters().get(0),
                is(DOMAIN_CLASS));
        assertThat(repositorySupterType.getParameters().get(1), is(ID_TYPE));
    }


    @Test
    public void determinesRepositoryInterfaceCorrectly() throws Exception {

        ClassOrInterfaceTypeDetails repositoryInterface =
                hades.determineRepositoryInterface(DOMAIN_CLASS, null);

        assertThat(repositoryInterface.getName(), is(REPOSITORY_INTERFACE));
    }
}
