/*
 * ------------------------------------------------------------------------
 * Class : TypeBoard
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity implementation class for Entity: TypeBoard
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypeBoard.ALL_QUERY,
                query = "SELECT t FROM TypeBoard t ORDER BY t.label"),
        @NamedQuery(name = TypeBoard.BY_NAME_QUERY,
                query = "SELECT t FROM TypeBoard t WHERE t.label = :name") })
public class TypeBoard extends TypeArticle implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the named query selecting all TypeBoard
     */
    public static final String ALL_QUERY = "AllTypeBoard";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypeBoardByName";
    
    /**
     * Default constructor
     */
    public TypeBoard() {
        super();
    }
    
    @Override
    public String getAllQuery() {
        return TypeBoard.ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return TypeBoard.BY_NAME_QUERY;
    }
    
}
