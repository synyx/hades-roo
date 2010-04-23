package org.synyx.hades.roo.addon;

import org.springframework.roo.addon.jpa.JpaOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.synyx.hades.roo.addon.support.Spring;
import org.synyx.hades.roo.addon.support.SpringConfigFile;
import org.synyx.hades.roo.addon.support.SpringManager;
import org.synyx.hades.roo.addon.support.SpringManager.XmlTemplateProcessor;
import org.w3c.dom.Element;


/**
 * All operations to setup a project to use Hades.
 * 
 * @author Oliver Gierke
 */
@ScopeDevelopment
class HadesInstallationOperations {

    private static final String HADES_CONFIG_FILE =
            "applicationContext-hades.xml";
    private static final String HADES_CONFIG_TEMPLATE =
            "applicationContext-hades-template.xml";

    private final JpaOperations jpaOperations;
    private final HadesProjectOperations projectOperations;
    private final SpringManager springManager;
    private final MetadataService metadataService;


    /**
     * @param jpaOperations
     * @param projectOperations
     * @param springManager
     * @param metadataService
     */
    public HadesInstallationOperations(JpaOperations jpaOperations,
            HadesProjectOperations projectOperations,
            SpringManager springManager, MetadataService metadataService) {

        Assert.notNull(projectOperations);
        Assert.notNull(jpaOperations);
        Assert.notNull(springManager);
        Assert.notNull(metadataService);

        this.jpaOperations = jpaOperations;
        this.projectOperations = projectOperations;
        this.springManager = springManager;
        this.metadataService = metadataService;
    }


    /**
     * Returns whether Hades is already installed for the current project.
     * Checks that general persistence setup has been made, that Hades
     * dependency was added to the project and the Hades config file exists.
     * 
     * @return
     */
    public boolean isInstalled() {

        boolean hadesDependencyDeclared =
                getProjectMetadata().getDependenciesExcludingVersion(
                        HadesProjectOperations.HADES).size() == 1;
        boolean hadesConfigFileExists =
                springManager.configFileExists(HADES_CONFIG_FILE);

        return canBeInstalled() && hadesDependencyDeclared
                && hadesConfigFileExists;
    }


    private ProjectMetadata getProjectMetadata() {

        return (ProjectMetadata) metadataService.get(ProjectMetadata
                .getProjectIdentifier());
    }


    /**
     * Returns whether Hades functionality can be installed for the current
     * project.
     * 
     * @return
     */
    public boolean canBeInstalled() {

        ProjectMetadata metadata = getProjectMetadata();

        if (metadata == null) {
            return false;
        }

        return jpaOperations.isJpaInstalled();
    }


    /**
     * Installs Hades support for the Roo project:
     * <ol>
     * <li>Adds Hades as dependency</li>
     * <li>Creates config file with Hades namespace</li>
     * <li>Adds import of Hades config file from application config file</li>
     * </ol>
     */
    public void installHades() {

        projectOperations.addSynyxRepository();
        projectOperations.addHadesDependency();

        springManager.createConfigFileFromTemplate(getClass(),
                HADES_CONFIG_TEMPLATE, HADES_CONFIG_FILE,
                new BasePackageConfigurer());

        SpringConfigFile config =
                springManager.getConfigFile(Spring.APPLICATION_CONTEXT);
        config.addImport(HADES_CONFIG_FILE);
        config.apply();
    }

    /**
     * Template processor that sets the Hades namespace {@literal base-package}
     * attribute to the project's top level package.
     * 
     * @author Oliver Gierke
     */
    private class BasePackageConfigurer implements XmlTemplateProcessor {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.synyx.hades.roo.addon.SpringManager.XmlTemplateProcessor#postProcess
         * (org.w3c.dom.Element)
         */
        public void postProcess(Element documentElement) {

            Element element =
                    XmlUtils.findFirstElementByName("hades:dao-config",
                            documentElement);
            element.setAttribute("base-package", getProjectMetadata()
                    .getTopLevelPackage().getFullyQualifiedPackageName());
        }
    }
}
