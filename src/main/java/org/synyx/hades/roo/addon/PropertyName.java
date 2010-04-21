package org.synyx.hades.roo.addon;

import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;


/**
 * Sample of an enum-like, tab-completion-aware property name. You are free to
 * add extra methods or othe members to this class. Just ensure the "key" field
 * remains a unique key (which also ensures the equals and hashCode methods
 * remain correct).
 */
public class PropertyName implements Comparable<PropertyName> {

    /**
     * You can change this field name, but ensure getKey() returns a unique
     * value
     */
    private String propertyName;

    public static final PropertyName USERNAME = new PropertyName("Username");
    public static final PropertyName HOME_DIRECTORY =
            new PropertyName("Home Directory");

    public static final PropertyName ENTITY = new PropertyName("Entity");


    private PropertyName(String propertyName) {

        Assert.hasText(propertyName, "Property name required");
        this.propertyName = propertyName;
    }


    public String getPropertyName() {

        return propertyName;
    }


    @Override
    public final boolean equals(Object obj) {

        return obj != null && obj instanceof PropertyName
                && this.compareTo((PropertyName) obj) == 0;
    }


    public final int compareTo(PropertyName o) {

        if (o == null) {
            return -1;
        }
        int result = this.propertyName.compareTo(o.propertyName);

        return result;
    }


    @Override
    public String toString() {

        ToStringCreator tsc = new ToStringCreator(this);
        tsc.append("propertyName", propertyName);
        return tsc.toString();
    }


    public final String getKey() {

        return this.propertyName;
    }
}