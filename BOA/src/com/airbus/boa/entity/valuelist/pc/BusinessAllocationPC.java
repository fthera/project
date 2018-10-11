/*
 * ------------------------------------------------------------------------
 * Class : BusinessAllocationPC
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist.pc;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.airbus.boa.entity.valuelist.I18nAttributeValueList;

/**
 * Entity implementation class for Entity: BusinesssAllocationPC
 */
@Entity
@Table(name = BusinessAllocationPC.TABLE_NAME)
@NamedQueries({
        @NamedQuery(
                name = BusinessAllocationPC.ALL_QUERY,
                query = "SELECT p FROM BusinessAllocationPC p ORDER BY p.defaultValue"),
        @NamedQuery(
                name = BusinessAllocationPC.BY_NAME_QUERY,
                query = "SELECT p FROM BusinessAllocationPC p WHERE p.defaultValue = :defaultValue") })
public class BusinessAllocationPC extends I18nAttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "businessallocationpc";
    
    /**
     * Name of the named query selecting all the attributes values of
     * BusinessAllocationPC
     */
    public static final String ALL_QUERY = "AllBusinessAllocationPC";
    
    /**
     * Name of the named query retrieving the attribute value of
     * BusinessAllocationPC according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "BusinessAllocationPCByName";
    
    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    @Override
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }
    
}