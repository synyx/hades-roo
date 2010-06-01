package org.synyx.hades.roo.addon.support;

import static org.springframework.roo.support.util.FileCopyUtils.*;
import static org.springframework.roo.support.util.TemplateUtils.*;
import static org.springframework.roo.support.util.XmlUtils.*;

import java.io.IOException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.w3c.dom.Document;


/**
 * @author Oliver Gierke
 */
@Service
@Component
public class DefaultSpringManager implements SpringManager {

    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;


    /*
     * (non-Javadoc)
     * 
     * @see org.synyx.hades.roo.addon.SpringManager#getConfigFile(java.lang
     * .String)
     */
    public SpringConfigFile getConfigFile(String configFileName) {

        String filePath = getConfigFileLocation(configFileName);

        if (fileManager.exists(filePath)) {
            return new SpringConfigFile(fileManager.updateFile(filePath));
        } else {
            throw new IllegalStateException(String.format(
                    "Could not acquire %s in %s", configFileName, filePath));
        }
    }


    /**
     * Returns the full qualified configuration file name for the given relative
     * one.
     * 
     * @param filename
     * @return
     */
    private String getConfigFileLocation(String filename) {

        return pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT, filename);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.synyx.hades.roo.addon.support.SpringManager#configFileExists(java
     * .lang.String)
     */
    public boolean configFileExists(String configFileName) {

        return fileManager.exists(getConfigFileLocation(configFileName));
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
