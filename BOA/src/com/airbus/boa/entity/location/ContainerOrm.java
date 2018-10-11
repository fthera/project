/*
 * ------------------------------------------------------------------------
 * Class : ContainerOrm
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Entity implementation mother class for relation: link of entities into other
 * entities
 */
@Entity
@Table(name = ContainerOrm.TABLE_NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ContainerOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "container";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Override
    public int hashCode() {
        final int lPrime = 31;
        int lResult = 1;
        lResult = lPrime * lResult;
        if (id != null) {
            lResult = lResult + id.hashCode();
        }
        return lResult;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContainerOrm other = (ContainerOrm) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the id to set
     */
    public void setId(Long pId) {
        id = pId;
    }
    
}
