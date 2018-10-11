/*
 * ------------------------------------------------------------------------
 * Class : TreeNodeFamily
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate listing the BOA families used as tree nodes by explorer
 */
public enum TreeNodeFamily {
    
    /*
     * ********************** LOCATIONS *************************
     */
    
    /** Buildings family */
    BUILDING,
    /** External entities family */
    EXTERNAL_ENTITY,
    /** Store rooms family */
    STOREROOM,
    /** Laboratories family */
    LABORATORY,
    /** Rooms family */
    ROOM,
    /** Installations family */
    INSTALLATION,
    
    /*
     * ********************** ARTICLES *************************
     */
    
    /** Cabinets family */
    CABINET,
    /** Switches family */
    SWITCH,
    /** Racks family */
    RACK,
    /** PCs family */
    PC,
    
    /*
     * ********************** OTHERS *************************
     */
    
    /** Tools family */
    TOOL;
    
    @Override
    public String toString() {
        // This string is used to find the current locale name of the family
        return MessageBundle.getMessage("FAMILY_" + super.toString());
    }
    
}
