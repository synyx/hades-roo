package org.synyx.hades.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.jpa.JpaOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.ProjectMetadata;
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
@Service
@Component
public class HadesInstallationOperationsImpl implements HadesInstallationOperations {

    private static final String HADES_CONFIG_FILE =
            "applicationContext-hades.xml";
    private static final String HADES_CONFIG_TEMPLATE =
            "applicationContext-hades-template.xml";

    @Reference
    private JpaOperations jpaOperations;
    @Reference
    private HadesProjectOperations projectOperations;
    @Reference
    private SpringManager springManager;
    @Reference
    private MetadataService metadataService;


    /* (non-Javadoc)
     * @see org.synyx.hades.roo.addon.HadesInstallationOptions#isInstalled()
     */
    public boolean isInstalled() {

        boolean hadesDependencyDeclared =
                getProjectMetadata().getDependenciesExcludingVersion(
                        HadesProjectOperationsImpl.HADES).size() == 1;
        boolean hadesConfigFileExists =
                springManager.configFileExists(HADES_CONFIG_FILE);

        return canBeInstalled() && hadesDependencyDeclared
                && hadesConfigFileExists;
    }


    private ProjectMetadata getProjectMetadata() {

        return (ProjectMetadata) metadataService.get(ProjectMetadata
                .getProjectIdentifier());
    }


    /* (non-Javadoc)
     * @see org.synyx.hades.roo.addon.HadesInstallationOptions#canBeInstalled()
     */
    public boolean canBeInstalled() {

        ProjectMetadata metadata = getProjectMetadata();

        if (metadata == null) {
            return false;
        }

        return jpaOperations.isJpaInstalled();
    }


    /* (non-Javadoc)
     * @see org.synyx.hades.roo.addon.HadesInstallationOptions#installHades()
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
