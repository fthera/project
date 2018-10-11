/*
 * ------------------------------------------------------------------------
 * Class : LocationForTool
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Entity implementation class for relation: location for Tools
 */
@Entity
public class LocationForTool extends LocationOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @OneToOne
    private Tool tool;
    
    /**
     * Constructor
     */
    public LocationForTool() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pTool
     *            the tool
     * @param pInherited
     *            the inherited value
     * @param pPlace
     *            the location
     * @param pExternalEntity
     *            the external location
     * @param pPreciseLocation
     *            the details on location
     */
    public LocationForTool(Tool pTool, boolean pInherited, Place pPlace,
            ExternalEntity pExternalEntity, String pPreciseLocation) {
        
        super(pInherited, pPlace, pExternalEntity, pPreciseLocation);
        tool = pTool;
    }
    
    @Override
    protected String getToStringDetails() {
        return "tool=" + tool;
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }
    
}
