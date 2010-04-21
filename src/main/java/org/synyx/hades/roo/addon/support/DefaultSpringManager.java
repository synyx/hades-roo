package org.synyx.hades.roo.addon.support;

import static org.springframework.roo.support.util.FileCopyUtils.*;
import static org.springframework.roo.support.util.TemplateUtils.*;
import static org.springframework.roo.support.util.XmlUtils.*;

import java.io.IOException;

import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.lifecycle.ScopeDevelopment;
import org.w3c.dom.Document;


/**
 * @author Oliver Gierke
 */
@ScopeDevelopment
class DefaultSpringManager implements SpringManager {

    private final PathResolver pathResolver;
    private final FileManager fileManager;


    /**
     * Creates a new {@link DefaultSpringManager}.
     * 
     * @param pathResolver
     * @param fileManager
     */
    public DefaultSpringManager(PathResolver pathResolver,
            FileManager fileManager) {

        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.synyx.hades.roo.addon.SpringManager#getConfigFile(java.lang
     * .String)
     */
    public SpringConfigFile getConfigFile(String configFileName) {

        String contextPath =
                pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT,
                        configFileName);

        if (fileManager.exists(contextPath)) {
            return new SpringConfigFile(fileManager.updateFile(contextPath));
        } else {
            throw new IllegalStateException(String.format(
                    "Could not acquire %s in %s", configFileName, contextPath));
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.synyx.hades.roo.addon.support.SpringManager#createConfigFileFromTemplate
     * (java.lang.Class, java.lang.String, java.lang.String,
     * org.synyx.hades.roo.addon.support.SpringManager.XmlTemplateProcessor)
     */
    public void createConfigFileFromTemplate(Class<?> owningClass,
            String templateFile, String destination,
            XmlTemplateProcessor processor) {

        String destinationFile =
                pathResolver
                        .getIdentifier(Path.SPRING_CONFIG_ROOT, destination);

        if (fileManager.exists(destinationFile)) {
            return;
        }

        try {

            MutableFile file = fileManager.createFile(destinationFile);
            copy(getTemplate(owningClass, templateFile), file.getOutputStream());

            applyTemplateProcessor(file, processor);

        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }


    private void applyTemplateProcessor(MutableFile file,
            XmlTemplateProcessor callback) {

        if (callback == null) {
            return;
        }

        Document document;
        try {

            document = getDocumentBuilder().parse(file.getInputStream());

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        callback.postProcess(document.getDocumentElement());
        writeXml(createIndentingTransformer(), file.getOutputStream(), document);
    }
}
