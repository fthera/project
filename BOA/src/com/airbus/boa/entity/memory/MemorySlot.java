/*
 * ------------------------------------------------------------------------
 * Class : MemorySlot
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.memory;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.airbus.boa.entity.article.PC;

/**
 * Entity implementation class for Entity: PC
 */
@Entity
@Table(name = "memoryslot", uniqueConstraints = @UniqueConstraint(
        columnNames = { "PC_ID", "NAME" }))
public class MemorySlot implements Serializable {
    
    private static final long serialVersionUID = 0L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private Boolean used;
    
    @Column(name = "MEMORYSIZE")
    private String memorySize;
    
    @Column(name = "MEMORYTYPE")
    private String memoryType;
    
    @ManyToOne
    @JoinColumn(name = "PC_ID")
    private PC pc;
    
    /**
     * Default constructor
     */
    public MemorySlot() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pPC
     *            the PC
     * @param pName
     *            the name
     * @param pUsed
     *            the used
     * @param pMemorySize
     *            the memorySize
     * @param pMemoryType
     *            the memoryType
     */
    public MemorySlot(PC pPC, String pName, Boolean pUsed, String pMemorySize,
            String pMemoryType) {
        
        pc = pPC;
        name = pName;
        used = pUsed;
        memorySize = pMemorySize;
        memoryType = pMemoryType;
    }
    
    @Override
    public int hashCode() {
        final int lPrime = 31;
        int lResult = 1;
        if (id == null) {
            lResult = lPrime * lResult;
        }
        else {
            lResult = lPrime * lResult + id.hashCode();
        }
        return lResult;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        MemorySlot lOther = (MemorySlot) pObj;
        if (id == null) {
            if (lOther.id != null) {
                return false;
            }
        }
        else if (!id.equals(lOther.id)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }
    
    /**
     * @return the memorySize
     */
    public String getMemorySize() {
        return memorySize;
    }
    
    /**
     * @param pMemorySize
     *            the memorySize to set
     */
    public void setMemorySize(String pMemorySize) {
        memorySize = pMemorySize;
    }
    
    /**
     * @return the memoryType
     */
    public String getMemoryType() {
        return memoryType;
    }
    
    /**
     * @param pMemoryType
     *            the memoryType to set
     */
    public void setMemoryType(String pMemoryType) {
        memoryType = pMemoryType;
    }
    
    /**
     * @return the used
     */
    public Boolean getUsed() {
        return used;
    }
    
    /**
     * @param pUsed
     *            the used to set
     */
    public void setUsed(Boolean pUsed) {
        used = pUsed;
    }
    
}
