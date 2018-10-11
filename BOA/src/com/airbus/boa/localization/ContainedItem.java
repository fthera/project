/*
 * ------------------------------------------------------------------------
 * Class : ContainedItem
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

/**
 * Interface defining a contained item behavior
 */
public interface ContainedItem {
    
    /**
     * @return the containedType
     */
    public ContainedType getContainedType();
    
    /**
     * @return the container
     */
    public Container getContainer();
    
}
