/*
 * ------------------------------------------------------------------------
 * Class : BusinessUsagePC
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
 * Entity implementation class for Entity: BusinessUsagePC
 */
@Entity
@Table(name = BusinessUsagePC.TABLE_NAME)
@NamedQueries({
        @NamedQuery(
                name = BusinessUsagePC.ALL_QUERY,
                query = "SELECT b FROM BusinessUsagePC b ORDER BY b.defaultValue"),
        @NamedQuery(
                name = BusinessUsagePC.BY_NAME_QUERY,
                query = "SELECT b FROM BusinessUsagePC b WHERE b.defaultValue = :defaultValue") })
public class BusinessUsagePC extends I18nAttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "businessusagepc";
    
    /**
     * Name of the named query selecting all the attributes values of
     * BusinessUsagePC
     */
    public static final String ALL_QUERY = "AllBusinessUsagePC";
    
    /**
     * Name of the named query retrieving the attribute value of BusinessUsagePC
     * according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "BusinessUsagePCByName";
    
    @Override
    public String toString() {
        return "BusinessUsagePC [getDefaultValue()=" + getDefaultValue() + "]";
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
