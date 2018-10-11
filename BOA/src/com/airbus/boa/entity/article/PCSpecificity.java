/*
 * ------------------------------------------------------------------------
 * Class : PCSpecificity
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.airbus.boa.entity.EntityBase;

/**
 * Entity implementation class for Entity: PCSpecificity
 */
@Entity
@Table(name = PCSpecificity.TABLE_NAME)
public class PCSpecificity implements Serializable, EntityBase {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "pc_specificity";
    /** The description column name */
    public static final String DESC_COLUMN_NAME = "DESCRIPTION";
    /** The description column name */
    public static final String CONTACT_COLUMN_NAME = "CONTACT";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "PC_ID")
    private PC pc;
    
    @Column(name = PCSpecificity.DESC_COLUMN_NAME)
    private String description;
    
    @Column(name = PCSpecificity.CONTACT_COLUMN_NAME)
    private String contact;
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the function to id
     */
    public void setId(Long pId) {
        id = pId;
    }
    
    /**
     * @return the pc
     */
    public PC getPc() {
        return pc;
    }
    
    /**
     * @param pPc
     *            the pc to set
     */
    public void setPc(PC pPc) {
        pc = pPc;
    }
    
    /**
     * @return the function
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }
    
    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }
    
    /**
     * @param pContact
     *            the contact to set
     */
    public void setContact(String pContact) {
        contact = pContact;
    }
}
