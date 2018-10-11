/*
 * ------------------------------------------------------------------------
 * Class : StockSelection
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.airbus.boa.entity.article.type.TypeArticle;

/**
 * Entity implementation class for Entity: StockSelection
 */
@Entity
@Table(name = StockSelection.TABLE_NAME)
public class StockSelection implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "stock_selection";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = StockSelection.NAME_COLUMN_NAME)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "stock_selection_types",
            joinColumns = @JoinColumn(name = "STOCKSELECTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "TYPEARTICLE_ID"))
    private List<TypeArticle> selectedTypes;
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the id to set
     */
    public void setId(Long pId) {
        id = pId;
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
     * @return the user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * @param pUser
     *            the user to set
     */
    public void setUser(User pUser) {
        user = pUser;
    }
    
    /**
     * @return the selectedTypes
     */
    public List<TypeArticle> getSelectedTypes() {
        return selectedTypes;
    }
    
    /**
     * @param pSelectedTypes
     *            the selectedTypes to set
     */
    public void setSelectedTypes(List<TypeArticle> pSelectedTypes) {
        selectedTypes = pSelectedTypes;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StockSelection other = (StockSelection) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        }
        else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }
}
