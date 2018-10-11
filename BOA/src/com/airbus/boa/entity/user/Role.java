/*
 * ------------------------------------------------------------------------
 * Class : Role
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.airbus.boa.entity.valuelist.I18nAttributeValueList;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation class for Entity: Role
 */
@Entity
@Table(name = Role.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Role.ALL_QUERY,
                query = "SELECT r FROM Role r ORDER BY r.level"),
        @NamedQuery(
                name = Role.BY_NAME_QUERY,
                query = "SELECT r FROM Role r WHERE r.defaultValue = :defaultValue"),
        @NamedQuery(name = Role.BY_LEVEL_QUERY,
                query = "SELECT r FROM Role r WHERE r.level = :level") })
public class Role extends I18nAttributeValueList implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "role";
    
    /**
     * Name of the named query retrieving all roles
     */
    public static final String ALL_QUERY = "AllRoles";
    
    /**
     * Name of the named query retrieving the role having the provided default
     * value. <br>
     * This query is parameterized:<br>
     * <b>defaultValue</b> the default value to search
     */
    public static final String BY_NAME_QUERY = "RoleByName";
    
    /**
     * Name of the named query retrieving the role having the provided level. <br>
     * This query is parameterized:<br>
     * <b>level</b> the level to search
     */
    public static final String BY_LEVEL_QUERY = "RoleByLevel";
    
    /** The level of the virtual Anonymous role */
    private static final Integer ANONYMOUS_LEVEL = 0;
    
    /** The level of the Super Administrator role */
    public static final Integer SUPER_ADMINISTRATOR_LEVEL = 100;
    
    /** Minimum level number authorized for a role */
    public static final Integer MIN_LEVEL = ANONYMOUS_LEVEL + 1;
    
    /** Maximum level number authorized for a role */
    public static final Integer MAX_LEVEL = SUPER_ADMINISTRATOR_LEVEL - 1;
    
    /**
     * Role to use when no user is logged (anonymous session)
     */
    public static final Role ANONYMOUS = new Role(
            MessageBundle.getMessageDefault("RoleAnonymous"),
            MessageBundle.getMessageFrench("RoleAnonymous"), ANONYMOUS_LEVEL);
    
    @Column
    private Integer level;
    
    /**
     * Constructor
     */
    public Role() {
        super();
    }
    
    private Role(String pDefaultValue, String pFrenchValue, Integer pLevel) {
        super();
        defaultValue = pDefaultValue;
        frenchValue = pFrenchValue;
        level = pLevel;
    }
    
    @Override
    public String getAllQuery() {
        return ALL_QUERY;
    }
    
    @Override
    public String getByNameQuery() {
        return BY_NAME_QUERY;
    }
    
    /**
     * @return the level
     */
    public Integer getLevel() {
        return level;
    }
    
    /**
     * @param pLevel
     *            the level to set
     */
    public void setLevel(Integer pLevel) {
        level = pLevel;
    }
    
}
