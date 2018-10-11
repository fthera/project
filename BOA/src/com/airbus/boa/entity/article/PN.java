/*
 * ------------------------------------------------------------------------
 * Class : PN
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Entity implementation super class for Entity: PN
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class PN implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The identifier column name */
    public static final String IDENTIFIER_COLUMN_NAME = "IDENTIFIER";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Column(unique = true, nullable = false)
    private String identifier;
    
    /**
     * Validate the provided identifier: it must be not null and not empty
     * 
     * @param identifier
     *            the identifier to validate
     * @return true if the identifier is valid, else false
     */
    public static boolean isValidIdentifier(String identifier) {
        if (StringUtil.isEmptyOrNull(identifier)) {
            return false;
        }
        return !(StringUtil.containsForbiddenChar(identifier));
    }
    
    /**
     * Default constructor
     */
    public PN() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param identifier
     *            the identifier
     */
    public PN(String identifier) {
        super();
        this.identifier = identifier;
        
    }
    
    @Override
    public String toString() {
        return getIdentifier();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result =
                prime * result
                        + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
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
        PN other = (PN) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        }
        else if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }
    
    /**
     * Method automatically called before persisting or updating the PN in
     * database
     */
    @PrePersist
    @PreUpdate
    public void validate() {
        if (!isValidIdentifier()) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.PN_IDENTIFIER_INVALID));
        }
    }
    
    private boolean isValidIdentifier() {
        return isValidIdentifier(identifier);
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
