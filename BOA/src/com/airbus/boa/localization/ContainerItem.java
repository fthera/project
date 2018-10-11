/*
 * ------------------------------------------------------------------------
 * Class : ContainerItem
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.util.List;

/**
 * Interface defining a container item behavior
 */
public interface ContainerItem {
    
    /**
     * @return the containerType
     */
    public ContainerType getContainerType();
    
    /**
     * @return the contained items, directly into the container
     */
    public List<ContainedItem> getContainedItems();
    
    /**
     * @return the contained items inheriting location from this, recursively
     *         from child to child
     */
    public List<ContainedItem> getContainedItemsInheriting();
    
}
