/*
 * ------------------------------------------------------------------------
 * Class : ConsultPeriod
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
 * Entity implementation class for Entity: ConsultPeriod
 */
@Entity
@Table(name = ConsultPeriod.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = ConsultPeriod.ALL_QUERY,
                query = "SELECT s FROM ConsultPeriod s ORDER BY s.defaultValue"),
        @NamedQuery(
                name = ConsultPeriod.BY_NAME_QUERY,
                query = "SELECT s FROM ConsultPeriod s WHERE s.defaultValue = :defaultValue") })
public class ConsultPeriod extends I18nAttributeValueList
        implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "consultperiod";
    
    /**
     * Name of the named query selecting all the attributes values of
     * ConsultPeriod
     */
    public static final String ALL_QUERY = "AllConsultPeriod";
    
    /**
     * Name of the named query retrieving the attribute value of ConsultPeriod
     * according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "ConsultPeriodByName";
    
    private Integer monthNumber;
    
    /**
     * Default constructor
     */
    public ConsultPeriod() {
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
    
    /**
     * @return the monthNumber
     */
    public Integer getMonthNumber() {
        return monthNumber;
    }
    
    /**
     * @param monthNumber
     *            the monthNumber to set
     */
    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }
    
}