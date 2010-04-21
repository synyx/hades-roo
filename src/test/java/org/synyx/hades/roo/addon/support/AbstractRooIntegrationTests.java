package org.synyx.hades.roo.addon.support;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;


/**
 * Base class to extend from that reduced the need to configure config file
 * location and custom JUnit runner class.
 * 
 * @author Oliver Gierke
 */
@RunWith(RooJUnit4Runner.class)
@ContextConfiguration("classpath:roo-bootstrap.xml")
public abstract class AbstractRooIntegrationTests {

}
