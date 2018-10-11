/*
 * ------------------------------------------------------------------------
 * Class : LocationForDemand
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.demand.Demand;

/**
 * Entity implementation class for relation: location for PC of Demands
 */
@Entity
public class LocationForDemand extends LocationOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @OneToOne
    private Demand demand;
    
    /**
     * Constructor
     */
    public LocationForDemand() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pDemand
     *            the demand
     * @param pInherited
     *            the inherited value
     * @param pPlace
     *            the location
     * @param pExternalEntity
     *            the external location
     * @param pPreciseLocation
     *            the details on location
     */
    public LocationForDemand(Demand pDemand, boolean pInherited, Place pPlace,
            ExternalEntity pExternalEntity, String pPreciseLocation) {
        
        super(pInherited, pPlace, pExternalEntity, pPreciseLocation);
        demand = pDemand;
    }
    
    @Override
    protected String getToStringDetails() {
        return "demand=" + demand;
    }
    
    /**
     * @return the demand
     */
    public Demand getDemand() {
        return demand;
    }
    
}
