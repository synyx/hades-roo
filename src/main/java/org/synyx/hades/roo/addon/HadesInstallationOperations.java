package org.synyx.hades.roo.addon;

/**
 *
 * @author Oliver Gierke
 */
public interface HadesInstallationOperations {

    /**
     * Returns whether Hades is already installed for the current project.
     * Checks that general persistence setup has been made, that Hades
     * dependency was added to the project and the Hades config file exists.
     * 
     * @return
     */
    boolean isInstalled();


    /**
     * Returns whether Hades functionality can be installed for the current
     * project.
     * 
     * @return
     */
    boolean canBeInstalled();


    /**
     * Installs Hades support for the Roo project:
     * <ol>
     * <li>Adds Hades as dependency</li>
     * <li>Creates config file with Hades namespace</li>
     * <li>Adds import of Hades config file from application config file</li>
     * </ol>
     */
    void installHades();

}