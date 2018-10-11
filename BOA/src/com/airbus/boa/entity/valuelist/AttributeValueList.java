/*
 * ------------------------------------------------------------------------
 * Class : AttributeValueList
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

/**
 * Entity implementation super class for entity: AttributeValueList
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AttributeValueList implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The default value column name */
    public static final String DEFAULTVALUE_COLUMN_NAME = "DEFAULTVALUE";
    
    @Id
    @GeneratedValue
    private Long id;
    
    /** Default value of the attribute (in default language) */
    @Column(unique = true, nullable = false)
    protected String defaultValue;
    
    /** Boolean indicating if the value is modifiable */
    @Column
    protected boolean modifiable = true;
    
    /** Boolean indicating if the value is authorized to be deleted */
    @Column
    protected boolean deletable = true;
    
    @Override
    public String toString() {
        return "AttributeValueList [id=" + id + ", defaultValue="
                + defaultValue + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AttributeValueList other = (AttributeValueList) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null) {
                return false;
            }
        }
        else if (!defaultValue.equals(other.defaultValue)) {
            return false;
        }
        return true;
    }
    
    /**
     * Get the value of the attribute in the current language
     * 
     * @return the default value
     */
    public String getLocaleValue() {
        return defaultValue;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * @return the modifiable
     */
    public boolean isModifiable() {
        return modifiable;
    }
    
    /**
     * @param modifiable
     *            the modifiable to set
     */
    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }
    
    /**
     * @return the deletable
     */
    public boolean isDeletable() {
        return deletable;
    }
    
    /**
     * @param pDeletable
     *            the deletable to set
     */
    public void setDeletable(boolean pDeletable) {
        deletable = pDeletable;
    }
    
    /**
     * @return the name of the named query selecting all the attributes values
     *         of a list
     */
    public abstract String getAllQuery();
    
    /**
     * @return the name of the named query retrieving the attribute value of a
     *         list according to its default value. <br>
     *         This query is parameterized:<br>
     *         <b>defaultValue</b> the default value to search
     */
    public abstract String getByNameQuery();
    
}
