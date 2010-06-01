package org.synyx.hades.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;


/**
 * {@link CommandMarker} to declare all shell commands available for Hades Roo
 * integration.
 * 
 * @author Oliver Gierke
 */
@Service
@Component
public class HadesCommands implements CommandMarker {

    @Reference
    private HadesOperations operations;
    @Reference
    private HadesInstallationOperations installationOperations;


    @CliCommand(value = "hades repository", help = "Creates a Hades repository interface")
    public void createDaoInterface(
            @CliOption(key = "entity", mandatory = true, specifiedDefaultValue = "*", optionContext = "update,project", help = "The entity you want to create the repository for") JavaType typeName,
            @CliOption(key = "package") JavaPackage daoPackage) {

        operations.createRepositoryInterface(typeName, daoPackage);
    }


    @CliAvailabilityIndicator("hades repository")
    public boolean isCreateDaoInterfaceAvailable() {

        return installationOperations.isInstalled();
    }


    @CliCommand(value = "hades install", help = "Installs Hades for the project")
    public void installHades() {

        installationOperations.installHades();
    }


    @CliAvailabilityIndicator("hades install")
    public boolean canInstallHades() {

        return installationOperations.canBeInstalled()
                && !installationOperations.isInstalled();
    }
}