/*
 * ------------------------------------------------------------------------
 * Class : TypeRack
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity implementation class for Entity: TypeRack
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypeRack.ALL_QUERY,
                query = "SELECT t FROM TypeRack t ORDER BY t.label"),
        @NamedQuery(name = TypeRack.BY_NAME_QUERY,
                query = "SELECT t FROM TypeRack t WHERE t.label = :name") })
public class TypeRack extends TypeArticle implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the named query selecting all TypeRack
     */
    public static final String ALL_QUERY = "AllTypeRack";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypeRackByName";
    
    /**
     * Default constructor
     */
    public TypeRack() {
        super();
    }
    
    @Override
    public String getAllQuery() {
        return TypeRack.ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return TypeRack.BY_NAME_QUERY;
    }
    
}
