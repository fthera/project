/*
 * ------------------------------------------------------------------------
 * Class : LocationOrm
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Entity implementation mother class for relation: location of entities in a
 * Place
 */
@Entity
@Table(name = LocationOrm.TABLE_NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class LocationOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "location";
    /** The details on location column name */
    public static final String PRECISELOCATION_COLUMN_NAME = "PRECISELOCATION";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Column
    private boolean inherited = false;
    
    @ManyToOne
    private Place place;
    
    @ManyToOne
    private ExternalEntity externalEntity;
    
    @Column
    private String preciseLocation;
    
    /**
     * Constructor
     */
    public LocationOrm() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pInherited
     *            the inherited value
     * @param pPlace
     *            the location
     * @param pExternalEntity
     *            the external location
     * @param pPreciseLocation
     *            the details on location
     */
    protected LocationOrm(boolean pInherited, Place pPlace,
            ExternalEntity pExternalEntity, String pPreciseLocation) {
        
        setPlace(pPlace);
        setExternalEntity(pExternalEntity);
        
        setInherited(pInherited);
        
        preciseLocation = pPreciseLocation;
    }
    
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
        LocationOrm other = (LocationOrm) obj;
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
     * Check the inherited attribute, the location and the external location
     * 
     * @return true if the location is empty, else false
     */
    public boolean isEmpty() {
        return (!inherited) && place == null && externalEntity == null;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", "
                + getToStringDetails() + ", place=" + place
                + ", preciseLocation=" + preciseLocation + ", externalEntity="
                + externalEntity + ", inherited=" + inherited + "]";
    }
    
    /**
     * @return the located entity in string form
     */
    protected abstract String getToStringDetails();
    
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
    
    /**
     * @return the inherited
     */
    public boolean isInherited() {
        return inherited;
    }
    
    /**
     * @param pInherited
     *            the inherited to set
     */
    public void setInherited(boolean pInherited) {
        inherited = pInherited;
        if (inherited && place != null) {
            // Reset the location since it is inherited
            place = null;
        }
    }
    
    /**
     * @return the place
     */
    public Place getPlace() {
        return place;
    }
    
    /**
     * @param pPlace
     *            the place to set
     */
    public void setPlace(Place pPlace) {
        place = pPlace;
        if (place != null && inherited) {
            // Reset inherited since a location is defined
            inherited = false;
        }
    }
    
    /**
     * @return the preciseLocation
     */
    public String getPreciseLocation() {
        return preciseLocation;
    }
    
    /**
     * @param pPreciseLocation
     *            the preciseLocation to set
     */
    public void setPreciseLocation(String pPreciseLocation) {
        preciseLocation = pPreciseLocation;
    }
    
    /**
     * @return the externalEntity
     */
    public ExternalEntity getExternalEntity() {
        return externalEntity;
    }
    
    /**
     * @param pExternalEntity
     *            the externalEntity to set
     */
    public void setExternalEntity(ExternalEntity pExternalEntity) {
        externalEntity = pExternalEntity;
    }
    
}
