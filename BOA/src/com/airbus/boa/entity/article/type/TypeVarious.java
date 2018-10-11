/*
 * ------------------------------------------------------------------------
 * Class : TypeVarious
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity implementation class for entity: TypeVarious
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypeVarious.ALL_QUERY,
                query = "SELECT t FROM TypeVarious t ORDER BY t.label"),
        @NamedQuery(name = TypeVarious.BY_NAME_QUERY,
                query = "SELECT t FROM TypeVarious t WHERE t.label = :name") })
public class TypeVarious extends TypeArticle implements Serializable {
    
    private static final long serialVersionUID = 6996308551247569030L;
    
    /**
     * Name of the named query selecting all TypeVarious
     */
    public static final String ALL_QUERY = "AllTypeVarious";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypeVariousByName";
    
    /**
     * Default constructor
     */
    public TypeVarious() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param label
     *            the label
     */
    public TypeVarious(String label) {
        super(label);
    }
    
    @Override
    public String getAllQuery() {
        return TypeVarious.ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return TypeVarious.BY_NAME_QUERY;
    }
    
}
