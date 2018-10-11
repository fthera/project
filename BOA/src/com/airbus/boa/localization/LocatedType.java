/*
 * ------------------------------------------------------------------------
 * Class : LocatedType
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate listing the BOA existing located entities
 */
public enum LocatedType {
    
    /** When the entity is a cabinet */
    Cabinet,
    /** When the entity is a rack */
    Rack,
    /** When the entity is a switch */
    Switch,
    /** When the entity is a board */
    Board,
    /** When the entity is a PC */
    PC,
    /** When the entity is a various article */
    Various,
    /** When the entity is a tool */
    Tool,
    /** When the entity is a demand */
    Demand,
    /** When the entity is an installation */
    Installation;
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(name());
    }
    
}
