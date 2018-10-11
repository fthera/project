/*
 * ------------------------------------------------------------------------
 * Class : AirbusStatus
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist.obso;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.airbus.boa.entity.valuelist.I18nAttributeValueList;

/**
 * Entity implementation class for Entity: AirbusStatus
 */
@Entity
@Table(name = AirbusStatus.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = AirbusStatus.ALL_QUERY,
                query = "SELECT s FROM AirbusStatus s ORDER BY s.defaultValue"),
        @NamedQuery(
                name = AirbusStatus.BY_NAME_QUERY,
                query = "SELECT s FROM AirbusStatus s WHERE s.defaultValue = :defaultValue") })
public class AirbusStatus extends I18nAttributeValueList
        implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "airbusstatus";
    /** The color Hex column name */
    public static final String COLORHEX_COLUMN_NAME = "COLORHEX";
    
    /**
     * Name of the named query selecting all the attributes values of
     * AirbusStatus
     */
    public static final String ALL_QUERY = "AllAirbusStatus";
    
    /**
     * Name of the named query retrieving the attribute value of AirbusStatus
     * according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "AirbusStatusByName";
    
    @Column(name = COLORHEX_COLUMN_NAME)
    private String colorHex;
    
    /**
     * Default constructor
     */
    public AirbusStatus() {
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
     * @return the colorHex
     */
    public String getColorHex() {
        return colorHex;
    }
    
    /**
     * @param colorHex
     *            the colorHex to set
     */
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    
}
