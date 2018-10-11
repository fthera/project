/*
 * ------------------------------------------------------------------------
 * Class : AirbusPN
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: AirbusPN
 */
@Entity
@Table(name = AirbusPN.TABLE_NAME)
@NamedQueries({ @NamedQuery(name = "AirbusPNByName",
        query = "SELECT a FROM AirbusPN a WHERE a.identifier = :name") })
public class AirbusPN extends PN {
    
    private static final long serialVersionUID = -4821379232609447201L;
    
    /** The table name */
    public static final String TABLE_NAME = "airbuspn";
    
    @PrePersist
    @PreUpdate
    @Override
    public void validate() {
        super.validate();
    }
}
