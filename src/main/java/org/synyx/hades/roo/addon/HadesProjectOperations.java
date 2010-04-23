package org.synyx.hades.roo.addon;

import static org.springframework.roo.support.util.XmlUtils.*;

import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Facade for operations that deal with project infrastructure setup for Hades.
 * 
 * @author Oliver Gierke
 */
@ScopeDevelopment
class HadesProjectOperations {

    private static final String REPO_XPATH =
            "/repositories/repository/url[.='http://repo.synyx.org']";

    private static final String HADES_ID = "org.synyx.hades";
    private static final String HADES_VERSION = "2.0.0.BUILD-SNAPSHOT";

    static final Dependency HADES =
            new Dependency(HADES_ID, HADES_ID, HADES_VERSION);

    private final ProjectOperations projectOperations;
    private final PathResolver pathResolver;
    private final FileManager fileManager;


    /**
     * Creates a new {@link HadesProjectOperations}.
     * 
     * @param projectOperations
     * @param pathResolver
     * @param fileManager
     */
    public HadesProjectOperations(ProjectOperations projectOperations,
            PathResolver pathResolver, FileManager fileManager) {

        this.projectOperations = projectOperations;
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }


    /**
     * Adds Hades as dependency if not already declared.
     */
    public void addHadesDependency() {

        projectOperations.dependencyUpdate(HADES);
    }


    /**
     * Adds the Synyx Maven repository if not already declared.
     */
    public void addSynyxRepository() {

        String identifier = pathResolver.getIdentifier(Path.ROOT, "pom.xml");
        MutableFile file = fileManager.updateFile(identifier);

        Document document;
        try {
            document =
                    XmlUtils.getDocumentBuilder().parse(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException("pom.xml not found!", e);
        }

        appendRepositoryElement(document);
        XmlUtils.writeXml(file.getOutputStream(), document);
    }


    private void appendRepositoryElement(Document document) {

        Assert.notNull(document);

        Element element =
                findFirstElement(REPO_XPATH, document.getDocumentElement());

        if (element == null) {
            return;
        }

        Element repository = document.createElement("repository");
        repository.appendChild(createElement("id", "synyx", document));
        repository.appendChild(createElement("name", "Synyx Repository",
                document));
        repository.appendChild(createElement("url", "http://repo.synyx.org",
                document));

        Element repositories =
                findFirstElementByName("repositories", document
                        .getDocumentElement());
        repositories.appendChild(repository);
    }


    private Element createElement(String name, String value, Document document) {

        Element element = document.createElement(name);
        element.setTextContent(value);
        return element;
    }
}
