/*
 * ------------------------------------------------------------------------
 * Class : DepartmentInCharge
 * Copyright 2017 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for entity: DepartmentInCharge
 */
@Entity
@Table(name = DepartmentInCharge.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = DepartmentInCharge.ALL_QUERY,
                query = "SELECT d FROM DepartmentInCharge d ORDER BY d.defaultValue"),
        @NamedQuery(name = DepartmentInCharge.BY_NAME_QUERY,
                query = "SELECT d FROM DepartmentInCharge d WHERE d.defaultValue = :defaultValue") })
public class DepartmentInCharge extends AttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "departmentincharge";
    
    /** The Description column name */
    public static final String DESCRIPTION_COLUMN_NAME = "DESCRIPTION";
    
    /**
     * Name of the named query selecting all departments
     */
    public static final String ALL_QUERY = "allDepartmentsInChargeQuery";
    
    /**
     * Name of the named query selecting the department having the provided
     * siglum value. <br>
     * This query is parameterized: <br>
     * <b>value</b> the value to search
     */
    public static final String BY_NAME_QUERY = "departmentInChargeByName";
    
    @Column
    private String description;
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }
    
    /**
     * @return the concatenation of the value and the description
     */
    public String getFullDescription() {
        return defaultValue + " - " + description;
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
