package org.synyx.hades.roo.addon.support;

import static org.springframework.roo.support.util.XmlUtils.*;

import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Abstraction of an XML Spring configuration file. Can be used to alter it and
 * write changes back to the original file.
 * 
 * @author Oliver Gierke
 */
public class SpringConfigFile {

    private Document document;
    private MutableFile file;


    /**
     * Creates a new {@link SpringConfigFile} by reading XML data from the given
     * {@link MutableFile}.
     * 
     * @param file
     */
    SpringConfigFile(MutableFile file) {

        this.file = file;

        try {
            this.document =
                    XmlUtils.getDocumentBuilder().parse(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Add an import of the given resource location to the config file.
     * 
     * @param resourceLocation
     */
    public void addImport(String resourceLocation) {

        Element element = document.createElement("import");
        element.setAttribute("resource", resourceLocation);

        document.getDocumentElement().appendChild(element);
    }


    /**
     * Apply the changes to the underlying file.
     */
    public void apply() {

        writeXml(createIndentingTransformer(), file.getOutputStream(), document);
    }
}
