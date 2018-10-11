/*
 * ------------------------------------------------------------------------
 * Class : PermissionOrm
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Entity implementation class for Entity: Permission
 */
@Entity
@Table(name = PermissionOrm.TABLE_NAME, uniqueConstraints = @UniqueConstraint(
        columnNames = { PermissionOrm.CATEGORY_COLUMN_NAME, "ROLE_ID" }))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries(value = {
        @NamedQuery(name = PermissionOrm.ALL_PERMISSIONS_QUERY,
                query = "SELECT p FROM PermissionOrm p"),
        @NamedQuery(name = PermissionOrm.PERMISSIONS_FOR_ROLE_QUERY,
                query = "SELECT p FROM PermissionOrm p WHERE p.role = :role"),
        @NamedQuery(name = PermissionOrm.PERMISSIONS_FOR_NO_ROLE_QUERY,
                query = "SELECT p FROM PermissionOrm p WHERE p.role IS NULL") })
public abstract class PermissionOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "permission";
    /** The category column name */
    public static final String CATEGORY_COLUMN_NAME = "CATEGORY";
    /** The bitwise code column name */
    public static final String BITWISECODE_COLUMN_NAME = "BITWISECODE";
    
    /**
     * Name of the named query retrieving all permissions
     */
    public static final String ALL_PERMISSIONS_QUERY = "allPermissions";
    
    /**
     * Name of the named query retrieving the permissions for the role.<br>
     * This query is parameterized:<br>
     * <b>role</b> the role
     */
    public static final String PERMISSIONS_FOR_ROLE_QUERY =
            "permissionsForRole";
    
    /**
     * Name of the named query retrieving the permissions without role.
     */
    public static final String PERMISSIONS_FOR_NO_ROLE_QUERY =
            "permissionsForNoRole";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ROLE_ID")
    private Role role;
    
    @Column(name = BITWISECODE_COLUMN_NAME)
    private Byte bitwiseCode;
    
    @Version
    private Long version;
    
    /**
     * Constructor
     */
    public PermissionOrm() {
        
    }
    
    /**
     * Constructor with fields
     * 
     * @param pRole
     *            the user role
     * @param pBitwiseCode
     *            the bitwise code representing the permission
     */
    protected PermissionOrm(Role pRole, byte pBitwiseCode) {
        
        role = pRole;
        bitwiseCode = pBitwiseCode;
    }
    
    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * @return the bitwiseCode
     */
    public Byte getBitwiseCode() {
        return bitwiseCode;
    }
    
    /**
     * @param pBitwiseCode
     *            the bitwiseCode to set
     */
    public void setBitwiseCode(Byte pBitwiseCode) {
        bitwiseCode = pBitwiseCode;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
}
