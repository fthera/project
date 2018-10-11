/*
 * ------------------------------------------------------------------------
 * Class : Contains_Cabinet_Rack
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.Rack;

/**
 * Entity implementation class for relation: Rack linked into Cabinet
 */
@Entity
public class Contains_Cabinet_Rack extends ContainerOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The rack position column name */
    public static final String RACKPOSITION_COLUMN_NAME = "RACKPOSITION";
    
    @ManyToOne
    private Cabinet cabinet;
    
    @OneToOne
    private Rack rack;
    
    @Column
    private String rackPosition;
    
    @Override
    public String toString() {
        return "Contains_Cabinet_Rack [cabinet=" + cabinet.getId() + ", id="
                + getId() + ", rack=" + rack.getId() + ", rackPosition="
                + rackPosition + "]";
    }
    
    /**
     * @return the rackPosition
     */
    public String getRackPosition() {
        return rackPosition;
    }
    
    /**
     * @param rackPosition
     *            the rackPosition to set
     */
    public void setRackPosition(String rackPosition) {
        this.rackPosition = rackPosition;
    }
    
    /**
     * @return the cabinet
     */
    public Cabinet getCabinet() {
        return cabinet;
    }
    
    /**
     * @param cabinet
     *            the cabinet to set
     */
    public void setCabinet(Cabinet cabinet) {
        this.cabinet = cabinet;
    }
    
    /**
     * @return the rack
     */
    public Rack getRack() {
        return rack;
    }
    
    /**
     * @param rack
     *            the rack to set
     */
    public void setRack(Rack rack) {
        this.rack = rack;
    }
    
}
