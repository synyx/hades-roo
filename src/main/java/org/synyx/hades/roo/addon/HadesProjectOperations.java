package org.synyx.hades.roo.addon;

import org.springframework.roo.project.Dependency;


/**
 * @author Oliver Gierke
 */
public interface HadesProjectOperations {

    static final Dependency HADES =
            new Dependency("org.synyx.hades", "org.synyx.hades",
                    "2.0.0.BUILD-SNAPSHOT");


    /**
     * Adds Hades as dependency if not already declared.
     */
    void addHadesDependency();


    /**
     * Adds the Synyx Maven repository if not already declared.
     */
    void addSynyxRepository();

}