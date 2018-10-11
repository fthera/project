/*
 * ------------------------------------------------------------------------
 * Class : ExternalEntity
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocationItem;

/**
 * Entity implementation class for Entity: ExternalEntity
 */
@Entity
@Table(name = ExternalEntity.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = ExternalEntity.ALL_EXTERNAL_ENTITIES_QUERY,
                query = "SELECT e FROM ExternalEntity e ORDER BY e.name asc"),
        @NamedQuery(name = ExternalEntity.EXTERNAL_ENTITY_BY_NAME_QUERY,
                query = "SELECT e FROM ExternalEntity e WHERE e.name=:name"),
        @NamedQuery(
                name = ExternalEntity.EXTERNAL_ENTITIES_BY_NAME_QUERY,
                query = "SELECT e FROM ExternalEntity e WHERE e.name LIKE :name ORDER BY e.name"),
        @NamedQuery(
                name = ExternalEntity.EXTERNAL_ENTITIES_BY_TYPE_QUERY,
                query = "SELECT e FROM ExternalEntity e WHERE e.externalEntityType = :type ORDER BY e.name"),
        @NamedQuery(
                name = ExternalEntity.EXTERNAL_ENTITIES_BY_NAME_AND_TYPE_QUERY,
                query = "SELECT e FROM ExternalEntity e "
                        + "WHERE e.name LIKE :name AND e.externalEntityType = :type ORDER BY e.name") })
public class ExternalEntity implements LocationItem, Serializable,
        Comparable<ExternalEntity> {
    
    private static final long serialVersionUID = 7929652848677540070L;
    
    /** The table name */
    public static final String TABLE_NAME = "externalentity";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    
    /**
     * Name of the named query selecting all external entities.
     */
    public static final String ALL_EXTERNAL_ENTITIES_QUERY =
            "allExternalEntities";
    
    /**
     * Name of the named query selecting the external entity with the provided
     * name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String EXTERNAL_ENTITY_BY_NAME_QUERY =
            "externalEntityByName";
    
    /**
     * Name of the named query selecting external entities with the provided
     * name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String EXTERNAL_ENTITIES_BY_NAME_QUERY =
            "externalEntitiesByName";
    
    /**
     * Name of the named query selecting external entities with the provided
     * type. <br>
     * This query is parameterized: <br>
     * <b>type</b> the type to search
     */
    public static final String EXTERNAL_ENTITIES_BY_TYPE_QUERY =
            "externalEntitiesByType";
    
    /**
     * Name of the named query selecting external entities with the provided
     * name and type. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     * <b>type</b> the type to search
     */
    public static final String EXTERNAL_ENTITIES_BY_NAME_AND_TYPE_QUERY =
            "externalEntitiesByNameAndType";
    
    @Version
    private Long version;
    
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToMany(mappedBy = "externalEntity", fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    private List<LocationForArticle> locatedOrmArticles =
            new ArrayList<LocationForArticle>();
    
    @OneToMany(mappedBy = "externalEntity", fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    private List<LocationForTool> locatedOrmTools =
            new ArrayList<LocationForTool>();
    
    @ManyToOne
    @JoinColumn(name = "EXTERNALENTITYTYPE_ID")
    private ExternalEntityType externalEntityType;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    /**
     * Constructor
     */
    public ExternalEntity() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param pName
     *            the external entity name
     * @param pExternalEntityType
     *            the external entity type
     */
    public ExternalEntity(String pName, ExternalEntityType pExternalEntityType) {
        name = pName;
        externalEntityType = pExternalEntityType;
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
        ExternalEntity other = (ExternalEntity) obj;
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
    public int compareTo(ExternalEntity arg0) {
        
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
    
    @Override
    public List<LocatedItem> getLocatedItems() {
        
        List<LocatedItem> lLocatedItems = getLocatedItemsDirectly();
        
        lLocatedItems.addAll(getLocatedItemsInherited());
        
        return lLocatedItems;
    }
    
    @Override
    public List<LocatedItem> getLocatedItemsDirectly() {
        
        List<LocatedItem> lLocatedItems = new ArrayList<LocatedItem>();
        
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
        
        // Items cannot inherit the external location from parent
        return new ArrayList<LocatedItem>();
    }
    
    /**
     * @return the externalEntityType
     */
    public ExternalEntityType getExternalEntityType() {
        return externalEntityType;
    }
    
    /**
     * @param pExternalEntityType
     *            the externalEntityType to set
     */
    public void setExternalEntityType(ExternalEntityType pExternalEntityType) {
        externalEntityType = pExternalEntityType;
    }
    
    /**
     * @return the locatedOrmArticles
     */
    public List<LocationForArticle> getLocatedOrmArticles() {
        return locatedOrmArticles;
    }
    
    /**
     * @param pLocationForArticles
     *            the locatedOrmArticles to set
     */
    public void setLocatedOrmArticles(
            List<LocationForArticle> pLocationForArticles) {
        locatedOrmArticles = pLocationForArticles;
    }
    
    /**
     * @return the locatedOrmTools
     */
    public List<LocationForTool> getLocatedOrmTools() {
        return locatedOrmTools;
    }
    
    /**
     * @param pLocationForTools
     *            the locatedOrmTools to set
     */
    public void setLocatedOrmTools(List<LocationForTool> pLocationForTools) {
        locatedOrmTools = pLocationForTools;
    }
    
}
