/*
 * ------------------------------------------------------------------------
 * Class : LocationItem
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.util.List;

/**
 * Interface defining a location item behavior
 */
public interface LocationItem {
    
    /**
     * @return all the located items
     */
    public List<LocatedItem> getLocatedItems();
    
    /**
     * @return the located items directly located into this
     */
    public List<LocatedItem> getLocatedItemsDirectly();
    
    /**
     * @return the located items through inheritance of container
     */
    public List<LocatedItem> getLocatedItemsInherited();
    
}
