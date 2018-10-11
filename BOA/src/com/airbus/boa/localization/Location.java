/*
 * ------------------------------------------------------------------------
 * Class : Location
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;

import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Place;

/**
 * Object representing the geographical location of an element
 */
public class Location implements Serializable {
    
    private static final long serialVersionUID = 233862985288594546L;
    
    /** REBUT (SCRAP) location name */
    public static final String REBUT_SCRAP = "REBUT (SCRAP)";
    
    private LocatedType locatedType;
    
    private boolean inherited;
    
    private Place place;
    private String locationName;
    private String locationDetailedName;
    
    private String precision;
    
    private ExternalEntity externalEntity;
    private String externalLocationName;
    
    /**
     * Constructor
     * 
     * @param pInherited
     *            boolean indicating if the location is inherited from parent
     * @param pLocation
     *            the location
     * @param pExternalLocation
     *            the external location
     * @param pLocatedType
     *            the located type of item
     */
    // This constructor shall not be used out of this package
    Location(boolean pInherited, Place pLocation,
            ExternalEntity pExternalLocation, LocatedType pLocatedType) {
        
        locatedType = pLocatedType;
        
        inherited = pInherited;
        
        setPlace(pLocation);
        
        setExternalEntity(pExternalLocation);
    }
    
    /**
     * Constructor by copy
     * 
     * @param pLocation
     *            the location to copy into the new one (must be not null)
     */
    public Location(Location pLocation) {
        
        locatedType = pLocation.locatedType;
        
        inherited = pLocation.inherited;
        
        place = pLocation.place;
        externalEntity = pLocation.externalEntity;
        
        locationName = pLocation.locationName;
        locationDetailedName = pLocation.locationDetailedName;
        externalLocationName = pLocation.externalLocationName;
        
        precision = pLocation.precision;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int lResult = 1;
        if (inherited) {
            lResult = prime * lResult + 1;
        }
        else {
            lResult = prime * lResult;
        }
        if (place == null) {
            lResult = prime * lResult;
        }
        else {
            lResult = prime * lResult + place.hashCode();
        }
        if (externalEntity == null) {
            lResult = prime * lResult;
        }
        else {
            lResult = prime * lResult + externalEntity.hashCode();
        }
        if (precision == null) {
            lResult = prime * lResult;
        }
        else {
            lResult = prime * lResult + precision.hashCode();
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
        Location lOther = (Location) obj;
        if (inherited != lOther.inherited) {
            return false;
        }
        if (place == null) {
            if (lOther.place != null) {
                return false;
            }
        }
        else if (!place.equals(lOther.place)) {
            return false;
        }
        if (precision == null) {
            if (lOther.precision != null) {
                return false;
            }
        }
        else if (!precision.equals(lOther.precision)) {
            return false;
        }
        if (externalEntity == null) {
            if (lOther.externalEntity != null) {
                return false;
            }
        }
        else if (!externalEntity.equals(lOther.externalEntity)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (inherited) {
            builder.append("Inherited");
        }
        else {
            builder.append(locationName);
        }
        if (precision != null && !precision.isEmpty()) {
            builder.append("\n");
            builder.append("Details: ");
            builder.append(precision);
        }
        if (externalEntity != null) {
            builder.append("\n");
            builder.append("  (Ext.: ");
            builder.append(externalLocationName).append(")");
        }
        return builder.toString();
    }
    
    /**
     * Compare only location attributes
     * 
     * @param pLocation
     *            the location to compare with
     * @return true if the location is equal, else false
     */
    public boolean equalsLocation(Location pLocation) {
        
        if (pLocation == null) {
            return false;
        }
        
        if (inherited != pLocation.inherited) {
            return false;
        }
        if (place == null) {
            if (pLocation.place != null) {
                return false;
            }
        }
        else if (!place.equals(pLocation.place)) {
            return false;
        }
        if (precision == null) {
            if (pLocation.precision != null) {
                return false;
            }
        }
        else if (!precision.equals(pLocation.precision)) {
            return false;
        }
        return true;
    }
    
    /**
     * Compare only external location attributes
     * 
     * @param pLocation
     *            the location to compare with
     * @return true if the external location is equal, else false
     */
    public boolean equalsExternalLocation(Location pLocation) {
        
        if (pLocation == null) {
            return false;
        }
        
        if (externalEntity == null) {
            if (pLocation.externalEntity != null) {
                return false;
            }
        }
        else if (!externalEntity.equals(pLocation.externalEntity)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the tool tip corresponding to the location (can be empty)
     */
    public String getToolTip() {
        
        if (place != null) {
            
            return place.getToolTip();
        }
        else {
            return "";
        }
    }
    
    /**
     * Determine if the precision field is available for locating the provided
     * item into the place
     * 
     * @return a boolean indicating if the precision field is available
     */
    public boolean isPrecisionAvailable() {
        
        switch (locatedType) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
        case Demand:
        case Tool:
            return true;
            
        case Installation:
        default:
            return false;
        }
    }
    
    /**
     * @return true if the external location is defined, else false
     */
    public boolean isExternalLocated() {
        return externalEntity != null;
    }
    
    /**
     * @return true if the location is defined, else false
     */
    public boolean isLocated() {
        return place != null;
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
    // This method shall not be called out of this package
    void setInherited(boolean pInherited) {
        inherited = pInherited;
    }
    
    /**
     * @return the locationName
     */
    public String getLocationName() {
        return locationName;
    }
    
    /**
     * @return the locationDetailedName
     */
    public String getLocationDetailedName() {
        return locationDetailedName;
    }
    
    /**
     * @return the place
     */
    public Place getPlace() {
        return place;
    }
    
    /**
     * @param pPlace
     *            the location to set
     */
    // This method shall not be called out of this package
    void setPlace(Place pPlace) {
        
        place = pPlace;
        
        if (place != null) {
            locationName = place.getCompleteName();
            locationDetailedName = place.getDetailedName();
        }
        else {
            locationName = "";
            locationDetailedName = "";
        }
    }
    
    /**
     * @return the precision
     */
    public String getPrecision() {
        return precision;
    }
    
    /**
     * @param pPrecision
     *            the precision to set
     */
    public void setPrecision(String pPrecision) {
        precision = pPrecision;
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
    // This method shall not be called out of this package
    void setExternalEntity(ExternalEntity pExternalEntity) {
        
        externalEntity = pExternalEntity;
        
        if (externalEntity != null) {
            externalLocationName = externalEntity.getName();
        }
        else {
            externalLocationName = "";
        }
    }
    
    /**
     * @return the externalLocationName
     */
    public String getExternalLocationName() {
        return externalLocationName;
    }
    
}
