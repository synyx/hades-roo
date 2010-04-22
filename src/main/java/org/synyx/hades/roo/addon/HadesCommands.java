package org.synyx.hades.roo.addon;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.lifecycle.ScopeDevelopmentShell;
import org.springframework.roo.support.util.Assert;


/**
 * {@link CommandMarker} to declare all shell commands available for Hades Roo
 * integration.
 * 
 * @author Oliver Gierke
 */
@ScopeDevelopmentShell
public class HadesCommands implements CommandMarker {

    private final HadesOperations operations;
    private final HadesInstallationOperations installationOperations;


    public HadesCommands(HadesOperations operations,
            HadesInstallationOperations installationOperations) {

        Assert.notNull(operations);
        Assert.notNull(installationOperations);

        this.operations = operations;
        this.installationOperations = installationOperations;
    }


    @CliCommand(value = "hades repository", help = "Creates a Hades repository interface")
    public void createDaoInterface(
            @CliOption(key = "entity", mandatory = true, specifiedDefaultValue = "*", optionContext = "update,project", help = "The entity you want to create the repository for") JavaType typeName,
            @CliOption(key = "package") JavaPackage daoPackage) {

        operations.createDaoClass(typeName, daoPackage);
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