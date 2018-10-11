/*
 * ------------------------------------------------------------------------
 * Class : Switch
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeSwitch;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;

/**
 * Entity implementation class for Entity: Switch
 */
@Entity
@Table(name = Switch.TABLE_NAME)
@NamedQueries({ @NamedQuery(
        name = "SwitchByName",
        query = "SELECT s FROM Switch s WHERE s.airbusSN = :name OR s.manufacturerSN = :name ") })
public class Switch extends Rack {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "switch";
    /** The IP address column name */
    public static final String IPADDRESS_COLUMN_NAME = "IPADDRESS";
    
    @Column
    private String ipAddress;
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypeSwitch();
    }
    
    @Override
    public void validate() {
        super.validate();
    }
    
    @Override
    public boolean isWithDesignation() {
        return false;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Switch;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Switch;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Switch;
    }
    
    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * @param ipAddress
     *            the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
}
