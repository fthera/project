/*
 * ------------------------------------------------------------------------
 * Class : Domain
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for entity: Domain
 */
@Entity
@Table(name = Domain.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Domain.ALL_QUERY,
                query = "SELECT d FROM Domain d ORDER BY d.defaultValue"),
        @NamedQuery(name = Domain.BY_NAME_QUERY,
                query = "SELECT d FROM Domain d WHERE d.defaultValue = :defaultValue") })
public class Domain extends AttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "domain";

    /**
     * Name of the named query selecting all domains
     */
    public static final String ALL_QUERY = "allDomainsQuery";
    
    /**
     * Name of the named query selecting the domain having the provided value. <br>
     * This query is parameterized: <br>
     * <b>value</b> the value to search
     */
    public static final String BY_NAME_QUERY = "domainByName";

    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }

    @Override
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }
    
}
