/*
 * ------------------------------------------------------------------------
 * Class : TypeCabinet
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity implementation class for entity: TypeCabinet
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypeCabinet.ALL_QUERY,
                query = "SELECT t FROM TypeCabinet t ORDER BY t.label"),
        @NamedQuery(name = TypeCabinet.BY_NAME_QUERY,
                query = "SELECT t FROM TypeCabinet t WHERE t.label = :name") })
public class TypeCabinet extends TypeArticle {
    
    private static final long serialVersionUID = 6728752810792175034L;
    
    /**
     * Name of the named query selecting all TypeCabinet
     */
    public static final String ALL_QUERY = "AllTypeCabinet";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypeCabinetByName";
    
    /**
     * Default constructor
     */
    public TypeCabinet() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param label
     *            the label
     */
    public TypeCabinet(String label) {
        super(label);
    }
    
    @Override
    public String getAllQuery() {
        return TypeCabinet.ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return BY_NAME_QUERY;
    }
    
}
