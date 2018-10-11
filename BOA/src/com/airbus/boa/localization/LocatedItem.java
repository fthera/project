/*
 * ------------------------------------------------------------------------
 * Class : LocatedItem
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

/**
 * Interface defining a located item behavior
 */
public interface LocatedItem {
    
    /**
     * @return the locatedType
     */
    public LocatedType getLocatedType();
    
    /**
     * @return the location, through the container if inherited from it
     */
    public Location getLocation();
    
}
