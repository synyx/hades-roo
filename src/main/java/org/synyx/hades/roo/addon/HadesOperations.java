package org.synyx.hades.roo.addon;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;


/**
 * @author Oliver Gierke
 */
public interface HadesOperations {

    /**
     * Creates the actual repository interface.
     * 
     * @param entity
     * @param daoPackage
     */
    void createRepositoryInterface(JavaType entity, JavaPackage daoPackage);
}