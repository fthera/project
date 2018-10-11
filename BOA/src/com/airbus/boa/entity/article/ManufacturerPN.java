/*
 * ------------------------------------------------------------------------
 * Class : ManufacturerPN
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ManufacturerPN
 */
@Entity
@Table(name = ManufacturerPN.TABLE_NAME)
@NamedQueries({
        @NamedQuery(
                name = "ManufacturerPNByName",
                query = "SELECT m FROM ManufacturerPN m WHERE m.identifier = :name"),
        @NamedQuery(
                name = "AllManufacturerPNIdentifers",
                query = "SELECT DISTINCT m.identifier FROM ManufacturerPN m"
                        + " WHERE m.identifier IS NOT NULL ORDER BY m.identifier ASC") })
public class ManufacturerPN extends PN implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "manufacturerpn";
    
    /**
     * Default constructor
     */
    public ManufacturerPN() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param identifier
     *            the identifier
     */
    public ManufacturerPN(String identifier) {
        super(identifier);
    }
    
    @PrePersist
    @PreUpdate
    @Override
    public void validate() {
        super.validate();
    }
}
