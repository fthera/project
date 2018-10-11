/*
 * ------------------------------------------------------------------------
 * Class : ExternalEntityType
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ExternalEntityType
 */
@Entity
@Table(name = ExternalEntityType.TABLE_NAME)
@NamedQueries({
        @NamedQuery(
                name = ExternalEntityType.ALL_QUERY,
                query = "SELECT t FROM ExternalEntityType t ORDER BY t.defaultValue"),
        @NamedQuery(
                name = ExternalEntityType.BY_NAME_QUERY,
                query = "SELECT t FROM ExternalEntityType t WHERE t.defaultValue = :defaultValue") })
public class ExternalEntityType extends I18nAttributeValueList
        implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "externalentitytype";
    
    /**
     * Name of the named query retrieving all external entity types
     */
    public static final String ALL_QUERY = "AllExternalEntityTypes";
    
    /**
     * Name of the named query retrieving the external entity type having the
     * provided default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "ExternalEntityTypeByName";
    
    /**
     * Constructor
     */
    public ExternalEntityType() {
        super();
    }
    
    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    @Override
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }
    
}
