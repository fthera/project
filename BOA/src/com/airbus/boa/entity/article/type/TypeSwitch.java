/*
 * ------------------------------------------------------------------------
 * Class : TypeSwitch
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity implementation class for entity: TypeSwitch
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypeSwitch.ALL_QUERY,
                query = "SELECT t FROM TypeSwitch t ORDER BY t.label"),
        @NamedQuery(name = TypeSwitch.BY_NAME_QUERY,
                query = "SELECT t FROM TypeSwitch t WHERE t.label = :name") })
public class TypeSwitch extends TypeArticle {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the named query selecting all TypeSwitch
     */
    public static final String ALL_QUERY = "AllTypeSwitch";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypeSwitchByName";
    
    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return BY_NAME_QUERY;
    }
    
}
