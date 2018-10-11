/*
 * ------------------------------------------------------------------------
 * Class : Place
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocationItem;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation class for Entity: Place
 */
@Entity
@Table(name = Place.TABLE_NAME, uniqueConstraints = @UniqueConstraint(
        columnNames = { Place.NAME_COLUMN_NAME, "BUILDING_ID" }))
@NamedQueries({
        @NamedQuery(
                name = Place.PLACES_BY_NAME_QUERY,
                query = "SELECT p FROM Place p WHERE p.name LIKE :name ORDER BY p.name"),
        @NamedQuery(name = "AllPlace",
                query = "SELECT p FROM Place p ORDER BY p.name"),
        @NamedQuery(
                name = "PlacesByBuildingFk",
                query = "SELECT p FROM Place p WHERE p.building.id = :buildingFk ORDER BY p.name"),
        @NamedQuery(
                name = Place.PLACES_BY_BUILDING_ID_AND_NAME_QUERY,
                query = "SELECT p FROM Place p WHERE p.building.id = :buildingId AND p.name LIKE :placeName ORDER BY p.name"),
        @NamedQuery(
                name = Place.PLACE_BY_NAME_AND_BUILDING_QUERY,
                query = "SELECT p FROM Place p WHERE p.name LIKE :name AND p.building = :building"),
        @NamedQuery(name = Place.PLACE_BY_COMPLETE_NAME_QUERY,
                query = "SELECT p FROM Place p WHERE CONCAT(p.building.name, '"
                        + Place.BUILDING_PLACE_SEPARATOR
                        + "', p.name) = :completeName") })
public class Place implements LocationItem, Serializable, Comparable<Place> {
    
    /**
     * Building place type
     */
    public enum PlaceType {
        /** The place is a laboratory */
        Laboratory,
        /** The place is a storeroom */
        Storeroom,
        /** The place is a simple room */
        Room
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "place";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    /** The type column name */
    public static final String TYPE_COLUMN_NAME = "TYPE";
    
    /**
     * Name of the named query selecting places with the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String PLACES_BY_NAME_QUERY = "placesByName";
    
    /**
     * Name of the named query selecting the place with the provided name and
     * the provided building.<br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     * <b>building</b> the building containing the place
     */
    public static final String PLACE_BY_NAME_AND_BUILDING_QUERY =
            "placeByNameAndBuilding";
    
    /**
     * Name of the named query selecting the places having a name like the
     * provided one and
     * the provided building id.<br>
     * This query is parameterized:<br>
     * <b>placeName</b> the name to search
     * <b>buildingId</b> the id of the building containing the place
     */
    public static final String PLACES_BY_BUILDING_ID_AND_NAME_QUERY =
            "placesByBuildingIdAndName";
    
    /**
     * Name of the named query selecting the place having the provided complete
     * name "<Building_name>-<Place_name>".<br>
     * This query is parameterized:<br>
     * <b>completeName</b> the complete name to search
     */
    public static final String PLACE_BY_COMPLETE_NAME_QUERY =
            "placeByCompleteName";
    
    /** The separator for separating the building name from the place name */
    public static final String BUILDING_PLACE_SEPARATOR = "-";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(optional = false)
    private Building building;
    
    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST)
    private List<LocationForTool> locatedOrmTools =
            new ArrayList<LocationForTool>();
    
    @Column
    @Enumerated(EnumType.STRING)
    private PlaceType type;
    
    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    private List<LocationForArticle> locatedOrmArticles =
            new ArrayList<LocationForArticle>();
    
    @OneToMany(mappedBy = "locationPlace")
    private List<Installation> locatedInstallations =
            new ArrayList<Installation>();
    
    @Version
    private Long version;
    
    /**
     * Default constructor
     */
    public Place() {
    }
    
    /**
     * Constructor
     * 
     * @param name
     *            the place name
     * @param type
     *            the place type
     */
    public Place(String name, PlaceType type) {
        super();
        this.name = name;
        this.type = type;
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
        Place other = (Place) obj;
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
    
    @Override
    public int compareTo(Place arg0) {
        return getName().compareTo(arg0.getName());
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
     * @return the building
     */
    public Building getBuilding() {
        return building;
    }
    
    /**
     * @param building
     *            the building to set
     */
    public void setBuilding(Building building) {
        this.building = building;
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
     * @return the type
     */
    public PlaceType getType() {
        return type;
    }
    
    /**
     * @param type
     *            the type to set
     */
    public void setType(PlaceType type) {
        this.type = type;
    }
    
    /**
     * @return the list of LocationForArticle (containing the located articles)
     */
    public List<LocationForArticle> getLocatedOrmArticles() {
        return locatedOrmArticles;
    }
    
    /**
     * @param pLocationForArticles
     *            the list of LocationForArticle to set (containing the located
     *            articles)
     */
    public void setLocatedOrmArticles(
            List<LocationForArticle> pLocationForArticles) {
        locatedOrmArticles = pLocationForArticles;
    }
    
    /**
     * @return the located installations
     */
    public List<Installation> getLocatedInstallations() {
        return locatedInstallations;
    }
    
    /**
     * @param testBenches
     *            the located installations to set
     */
    public void setLocatedInstallations(List<Installation> testBenches) {
        locatedInstallations = testBenches;
    }
    
    @Override
    public List<LocatedItem> getLocatedItems() {
        
        List<LocatedItem> lLocatedItems = getLocatedItemsDirectly();
        
        lLocatedItems.addAll(getLocatedItemsInherited());
        
        return lLocatedItems;
    }
    
    @Override
    public List<LocatedItem> getLocatedItemsDirectly() {
        
        List<LocatedItem> lLocatedItems = new ArrayList<LocatedItem>();
        
        lLocatedItems.addAll(locatedInstallations);
        
        for (LocationForArticle lLocatedOrmArticle : locatedOrmArticles) {
            lLocatedItems.add(lLocatedOrmArticle.getArticle());
        }
        
        for (LocationForTool lLocatedOrmTool : locatedOrmTools) {
            lLocatedItems.add(lLocatedOrmTool.getTool());
        }
        
        return lLocatedItems;
    }
    
    @Override
    public List<LocatedItem> getLocatedItemsInherited() {
        
        return LocationManager
                .getInheritedLocatedItems(getLocatedItemsDirectly());
    }
    
    /**
     * @return the detailed name of the place
     *         "<Building_name>-<Place_name> (<Place_type>)"
     */
    public String getDetailedName() {
        
        StringBuffer lDetailedName = new StringBuffer();
        
        lDetailedName.append(building.getName()).append(
                BUILDING_PLACE_SEPARATOR);
        
        lDetailedName.append(name);
        
        lDetailedName.append(" (");
        lDetailedName.append(MessageBundle.getMessage("placeType" + type));
        lDetailedName.append(")");
        
        return lDetailedName.toString();
    }
    
    /**
     * @return the name of the building and name of the place
     *         "<Building_name>-<Place_name>"
     */
    public String getCompleteName() {
        
        StringBuffer lCompleteName = new StringBuffer();
        
        lCompleteName.append(building.getName()).append(
                BUILDING_PLACE_SEPARATOR);
        
        lCompleteName.append(name);
        
        return lCompleteName.toString();
    }
    
    /**
     * @return the tool tip of the place
     */
    public String getToolTip() {
        
        StringBuffer lToolTip = new StringBuffer();
        
        lToolTip.append(MessageBundle.getMessage("Building: ")).append(
                building.getName());
        lToolTip.append("<br/>\n");
        
        lToolTip.append(MessageBundle.getMessage("Type: ")).append(
                MessageBundle.getMessage("placeType" + type));
        lToolTip.append("<br/>\n");
        
        lToolTip.append(MessageBundle.getMessage("Name: ")).append(name);
        
        return lToolTip.toString();
    }
    
    /**
     * @return the locatedOrmTools
     */
    public List<LocationForTool> getLocationForTools() {
        return locatedOrmTools;
    }
    
    /**
     * @param pLocationForTools
     *            the locatedOrmTools to set
     */
    public void setLocationForTools(List<LocationForTool> pLocationForTools) {
        locatedOrmTools = pLocationForTools;
    }
    
}
