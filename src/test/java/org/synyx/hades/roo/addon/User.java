package org.synyx.hades.roo.addon;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * @author Oliver Gierke - gierke@synyx.de
 */
@Entity
public class User {

    @Id
    private Long id;


    /**
     * @return the id
     */
    public Long getId() {

        return id;
    }
}
