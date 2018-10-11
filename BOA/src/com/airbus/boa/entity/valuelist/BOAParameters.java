/*
 * ------------------------------------------------------------------------
 * Class : BOAParameters
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: BOAParameters
 */
@Entity
@Table(name = BOAParameters.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = BOAParameters.ALL_QUERY,
                query = "SELECT s FROM BOAParameters s ORDER BY s.name"),
        @NamedQuery(
                name = BOAParameters.BY_NAME_QUERY,
                query = "SELECT s FROM BOAParameters s WHERE s.name = :name") })
public class BOAParameters implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "boa_parameters";
    
    /**
     * Name of the named query selecting all the attributes values of
     * BOAParameters
     */
    public static final String ALL_QUERY = "AllBOAParameters";
    
    /**
     * Name of the named query retrieving the attribute value of BOAParameters
     * according to its default value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "BOAParameterByName";

    /** The value column name */
    public static final String VALUE_COLUMN_NAME = "VALUE";
    public static final String NAME_COLUMN_NAME = "NAME";
    
    @Id
    private Long id;
    
    @Column(name = "VALUE", unique = false, nullable = false)
    private String value;
    
    @Column(name = "NAME", unique = false, nullable = false)
    private String name;
        

	/**
     * Default constructor
     */
    public BOAParameters() {
        super();
    }
    
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }

    /**
     * @return the value
     */
    public String getValue() {
		return value;
	}

    /**
     * @param value the value to set 
     */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
    
}