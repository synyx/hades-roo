package org.synyx.hades.roo.addon.support;

import org.w3c.dom.Element;


/**
 * Interface for operations to manage Spring configuration files.
 * 
 * @author Oliver Gierke
 */
public interface SpringManager {

    /**
     * Returns the root element of a Spring configuration file.
     * 
     * @see Spring
     * @param configFileName
     * @return
     */
    SpringConfigFile getConfigFile(String configFileName);


    /**
     * Returns whether the configuration file with the given name exists.
     * 
     * @param configFileName
     * @return
     */
    boolean configFileExists(String configFileName);


    /**
     * Creates a Spring configuration file out of the given template. Allows
     * post processing by providing an {@link XmlTemplateProcessor}.
     * 
     * @param owningClass the class the template has to be located relative to
     * @param templateFile
     * @param destination
     * @param processor
     */
    void createConfigFileFromTemplate(Class<?> owningClass,
            String templateFile, String destination,
            XmlTemplateProcessor processor);

    /**
     * Callback interface to manipulate an XML file right after it was created
     * from a template file.
     * 
     * @author Oliver Gierke
     */
    public interface XmlTemplateProcessor {

        /**
         * Do the actual post processing.
         * 
         * @param documentElement
         */
        void postProcess(Element documentElement);
    }
}
