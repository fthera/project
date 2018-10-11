/*
 * ------------------------------------------------------------------------
 * Class : ActionObso
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist.obso;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.airbus.boa.entity.valuelist.I18nAttributeValueList;

/**
 * Entity implementation class for Entity: ActionObso
 */
@Entity
@Table(name = ActionObso.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = ActionObso.ALL_QUERY,
                query = "SELECT s FROM ActionObso s ORDER BY s.defaultValue"),
        @NamedQuery(
                name = ActionObso.BY_NAME_QUERY,
                query = "SELECT s FROM ActionObso s WHERE s.defaultValue = :defaultValue") })
public class ActionObso extends I18nAttributeValueList implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "actionobso";
    
    /**
     * Name of the named query selecting all the attributes values of ActionObso
     */
    public static final String ALL_QUERY = "AllActionObso";
    
    /**
     * Name of the named query retrieving the attribute value of ActionObso
     * according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "ActionObsoByName";
    
    /**
     * Default constructor
     */
    public ActionObso() {
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
