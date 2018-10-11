/*
 * ------------------------------------------------------------------------
 * Class : Item
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity;

/**
 * Interface defining the behavior of an item
 */
public interface Item extends EntityBase {
    
    /**
     * @return the name of the item (stored attribute or computed)
     */
    public String getName();
    
}
