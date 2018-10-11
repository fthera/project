/*
 * ------------------------------------------------------------------------
 * Class : Building
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Entity implementation class for Entity: Building
 */
@Entity
@Table(name = Building.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = "allBuilding",
                query = "SELECT b FROM Building b ORDER BY b.name ASC"),
        @NamedQuery(name = "BuildingByName",
                query = "SELECT b FROM Building b WHERE b.name =:name") })
public class Building implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "building";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "building", fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST, CascadeType.REFRESH })
    private List<Place> places = new ArrayList<Place>();
    
    @Version
    private Long version;
    
    /**
     * Constructor
     */
    public Building() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param name
     *            the name
     */
    public Building(String name) {
        super();
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
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
        Building other = (Building) obj;
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
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the places
     */
    public List<Place> getPlaces() {
        return places;
    }
    
    /**
     * @param places
     *            the places to set
     */
    public void setPlaces(List<Place> places) {
        this.places = places;
    }
    
}
