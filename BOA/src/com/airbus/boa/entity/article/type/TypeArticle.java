/*
 * ------------------------------------------------------------------------
 * Class : TypeArticle
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;

/**
 * Entity implementation class for entity: TypeArticle
 */
@Entity
@Table(name = TypeArticle.TABLE_NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "AllTypeArticle",
                query = "SELECT t FROM TypeArticle t ORDER BY t.label"),
        @NamedQuery(name = "TypeArticleByLabel",
                query = "SELECT t FROM TypeArticle t WHERE t.label = :label"),
        @NamedQuery(name = "AllTypeArticleByLabel",
                query = "SELECT t FROM TypeArticle t WHERE t.label LIKE :label"),
        @NamedQuery(name = TypeArticle.BY_ID_QUERY,
                query = "SELECT t FROM TypeArticle t WHERE t.id= :id"),
        @NamedQuery(
                name = TypeArticle.COUNT_BY_NAME_QUERY,
                query = "SELECT COUNT(t.label) from TypeArticle t WHERE t.label = :name"),
        @NamedQuery(
                name = TypeArticle.BY_AIRBUS_PN_QUERY,
                query = "SELECT t FROM TypeArticle t WHERE :pn MEMBER OF t.listAirbusPN"),
        @NamedQuery(name = TypeArticle.BY_SUGGEST_NAME_QUERY,
                query = "SELECT t FROM TypeArticle t"
                        + " WHERE t.id NOT IN (SELECT tPC.id FROM TypePC tPC)"
                        + " AND t.label LIKE :suggest ORDER BY t.label") })
public abstract class TypeArticle implements Serializable {
    
    private static final long serialVersionUID = -1486743032288019506L;
    
    /** The table name */
    public static final String TABLE_NAME = "typearticle";
    /** The label column name */
    public static final String LABEL_COLUMN_NAME = "LABEL";
    /** The manufacturer column name */
    public static final String MANUFACTURER_COLUMN_NAME = "MANUFACTURER";
    
    /**
     * Name of the named query selecting the TypeArticle having the provided id.<br>
     * This query is parameterized:<br>
     * <b>id</b> the TypeArticle id
     */
    public static final String BY_ID_QUERY = "TypeArticleById";
    
    /**
     * Name of the named query counting the TypeArticle having the provided
     * name.<br>
     * This query is parameterized:<br>
     * <b>name</b> the TypeArticle name
     */
    public static final String COUNT_BY_NAME_QUERY = "CountTypeArticleByName";
    
    /**
     * Name of the named query selecting the TypeArticle having the provided
     * Airbus PN in its list.<br>
     * This query is parameterized:<br>
     * <b>pn</b> the Airbus PN
     */
    public static final String BY_AIRBUS_PN_QUERY = "TypeArticleByAirbusPN";
    
    /**
     * Name of the named query selecting the TypeArticle not being a TypePC and
     * having the name similar to the provided one.<br>
     * This query is parameterized:<br>
     * <b>suggest</b> the name suggestion
     */
    public static final String BY_SUGGEST_NAME_QUERY =
            "TypeArticleBySuggestName";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Column(unique = true, nullable = false, name = LABEL_COLUMN_NAME)
    private String label;
    
    @Column(name = MANUFACTURER_COLUMN_NAME)
    private String manufacturer;
    
    @OneToMany
    @JoinTable(name = "typearticle_airbuspn", joinColumns = @JoinColumn(
            name = "TYPEARTICLE_ID"), inverseJoinColumns = @JoinColumn(
            name = "AIRBUSPN_ID"))
    private List<AirbusPN> listAirbusPN;
    
    @Transient
    private List<ManufacturerPN> listManufacturerPN =
            new ArrayList<ManufacturerPN>();
    
    /**
     * Default constructor
     */
    public TypeArticle() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param label
     *            the label
     */
    public TypeArticle(String label) {
        this.label = label;
    }
    
    /**
     * Add the provided AirbusPN
     * 
     * @param airbusPN
     *            the AirbusPN to add
     */
    public void add(AirbusPN airbusPN) {
        if (airbusPN == null) {
            return;
        }
        
        if (listAirbusPN == null) {
            listAirbusPN = new ArrayList<AirbusPN>();
        }
        listAirbusPN.add(airbusPN);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (getId() == null) {
            return false;
        }
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        
        TypeArticle other = (TypeArticle) obj;
        return (other.getId() != null && getId().equals(other.getId()));
    }
    
    @Override
    public int hashCode() {
        
        return (label != null ? label.hashCode() : 0)
                + (id != null ? id.hashCode() : 0);
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    /**
     * @return the listAirbusPN
     */
    public List<AirbusPN> getListAirbusPN() {
        return listAirbusPN;
    }
    
    /**
     * @param listAirbusPN
     *            the listAirbusPN to set
     */
    public void setListAirbusPN(List<AirbusPN> listAirbusPN) {
        this.listAirbusPN = listAirbusPN;
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
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }
    
    /**
     * @param pManufacturer
     *            the manufacturer to set
     */
    public void setManufacturer(String pManufacturer) {
        manufacturer = pManufacturer;
    }
    
    /**
     * @return the name of the named query selecting all the types of the class
     */
    public abstract String getAllQuery();
    
    /**
     * @return the name of the named query retrieving the type having the
     *         provided name. <br>
     *         This query is parameterized:<br>
     *         <b>name</b> the name to search
     */
    public abstract String getByName();
    
    /**
     * @return the listManufacturerPN
     */
    public List<ManufacturerPN> getListManufacturerPN() {
        return listManufacturerPN;
    }
    
    /**
     * @param listMPN
     *            the listManufacturerPN to set
     */
    public void setListManufacturerPN(List<ManufacturerPN> listMPN) {
        listManufacturerPN = listMPN;
    }
    
}
