/*
 * ------------------------------------------------------------------------
 * Class : PermissionCRUD
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Entity implementation class for CRUD Permission
 */
@Entity
public class PermissionCRUD extends PermissionOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = PermissionOrm.CATEGORY_COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private RightCategoryCRUD category;
    
    /**
     * Constructor
     */
    public PermissionCRUD() {
        super();
    }
    
    /**
     * Constructor with fields
     * 
     * @param pCategory
     *            the right category
     * @param pRole
     *            the user role
     * @param pBitwiseCode
     *            the bitwise code representing the permission
     */
    public PermissionCRUD(RightCategoryCRUD pCategory, Role pRole,
            byte pBitwiseCode) {
        
        super(pRole, pBitwiseCode);
        
        category = pCategory;
    }
    
    /**
     * @return the category
     */
    public RightCategoryCRUD getCategory() {
        return category;
    }
    
}
