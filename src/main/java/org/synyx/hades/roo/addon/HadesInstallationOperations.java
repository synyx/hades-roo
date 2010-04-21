package org.synyx.hades.roo.addon;

import org.springframework.roo.addon.jpa.JpaOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
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
public class HadesInstallationOperations {

    private static final String HADES_ID = "org.synyx.hades";
    private static final String HADES_VERSION = "2.0.0.BUILD-SNAPSHOT";

    private static final Dependency HADES =
            new Dependency(HADES_ID, HADES_ID, HADES_VERSION);

    private JpaOperations jpaOperations;
    private ProjectOperations projectOperations;
    private SpringManager springManager;
    private MetadataService metadataService;


    /**
     * @param jpaOperations
     * @param projectOperations
     * @param springManager
     * @param metadataService
     */
    public HadesInstallationOperations(JpaOperations jpaOperations,
            ProjectOperations projectOperations, SpringManager springManager,
            MetadataService metadataService) {

        this.jpaOperations = jpaOperations;
        this.projectOperations = projectOperations;
        this.springManager = springManager;

        this.metadataService = metadataService;
    }


    public boolean isInstalled() {

        ProjectMetadata metadata = getProjectMetadata();

        if (metadata == null) {
            return false;
        }

        boolean hadesDeclared =
                getProjectMetadata().getDependenciesExcludingVersion(HADES)
                        .size() == 1;
        boolean jpaInstalled = jpaOperations.isJpaInstalled();

        return jpaInstalled && hadesDeclared;
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

        return jpaOperations.isJpaInstalled();
    }


    public void installHades() {

        projectOperations.dependencyUpdate(HADES);

        springManager.createConfigFileFromTemplate(getClass(),
                "applicationContext-hades-template.xml",
                "applicationContext-hades.xml", new BasePackageConfigurer());

        SpringConfigFile config =
                springManager.getConfigFile(Spring.APPLICATION_CONTEXT);
        config.addImport("applicationContext-hades.xml");
        config.apply();
    }

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
