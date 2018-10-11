/*
 * ------------------------------------------------------------------------
 * Class : AircraftProgram
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for entity: AircraftProgram
 */
@Entity
@Table(name = AircraftProgram.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = AircraftProgram.ALL_QUERY,
                query = "SELECT ap FROM AircraftProgram ap ORDER BY ap.defaultValue"),
        @NamedQuery(
                name = AircraftProgram.BY_NAME_QUERY,
                query = "SELECT ap FROM AircraftProgram ap WHERE ap.defaultValue = :defaultValue") })
public class AircraftProgram extends AttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "aircraftprogram";
    
    /**
     * Name of the named query selecting all aircraft programs
     */
    public static final String ALL_QUERY =
            "allAircraftProgramsQuery";
    
    /**
     * Name of the named query selecting the aircraft program having the
     * provided value. <br>
     * This query is parameterized: <br>
     * <b>value</b> the value to search
     */
    public static final String BY_NAME_QUERY =
            "aircraftProgramByName";
    
    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    @Override
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }
    
}
