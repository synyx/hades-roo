package org.synyx.hades.roo.addon;

import static org.springframework.roo.support.util.XmlUtils.*;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Facade for operations that deal with project infrastructure setup for Hades.
 * 
 * @author Oliver Gierke
 */
@Service
@Component
public class HadesProjectOperationsImpl implements HadesProjectOperations {

    private static final String REPO_XPATH =
            "/repositories/repository/url[.='http://repo.synyx.org']";

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.synyx.hades.roo.addon.HadesProjectOperations#addHadesDependency()
     */
    public void addHadesDependency() {

        projectOperations.dependencyUpdate(HADES);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.synyx.hades.roo.addon.HadesProjectOperations#addSynyxRepository()
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
