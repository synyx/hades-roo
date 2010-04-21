package org.synyx.hades.roo.addon.support;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runners.model.InitializationError;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Oliver Gierke - gierke@synyx.de
 */
public class RooJUnit4Runner extends SpringJUnit4ClassRunner {

    /**
     * @param clazz
     * @throws InitializationError
     */
    public RooJUnit4Runner(Class<?> clazz) throws InitializationError {

        super(clazz);
    }


    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.test.context.junit4.SpringJUnit4ClassRunner#
     * createTestContextManager(java.lang.Class)
     */
    @Override
    protected TestContextManager createTestContextManager(Class<?> clazz) {

        // Ensure all JDK log messages are deferred until a target is registered
        Logger rootLogger = Logger.getLogger("");
        HandlerUtils.wrapWithDeferredLogHandler(rootLogger, Level.SEVERE);

        return super.createTestContextManager(clazz);
    }

}
