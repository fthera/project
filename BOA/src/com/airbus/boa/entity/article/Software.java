/*
 * ------------------------------------------------------------------------
 * Class : Software
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Entity implementation class for Entity: Software
 */
@Entity
@Table(name = Software.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {
                Software.NAME_COLUMN_NAME, Software.DISTRIBUTION_COLUMN_NAME,
                Software.KERNEL_COLUMN_NAME }))
@NamedQueries({
        @NamedQuery(name = Software.ALL_QUERY,
                query = "SELECT s FROM Software s"
                        + " ORDER BY s.name, s.distribution, s.kernel"),
        @NamedQuery(
                name = Software.ALL_OS,
                query = "SELECT s FROM Software s WHERE s.operatingSystem = true"
                        + " ORDER BY s.name, s.distribution, s.kernel"),
        @NamedQuery(
                name = Software.EXIST_SOFTWARE_BY_NAME_DISTRIBUTION_KERNEL_QUERY,
                query = "SELECT COUNT(s) FROM Software s"
                        + " WHERE s.name = :name"
                        + " AND s.distribution = :distribution"
                        + " AND s.kernel = :kernel"),
        @NamedQuery(name = Software.SUGGESTION_BY_SUBS_COMPLETE_NAME_QUERY,
                query = "SELECT s FROM Software s WHERE TRIM(TRAILING '"
                        + Software.ATTRIBUTES_SEPARATOR
                        + "' FROM CONCAT(s.name,'"
                        + Software.ATTRIBUTES_SEPARATOR + "',s.distribution,'"
                        + Software.ATTRIBUTES_SEPARATOR
                        + "',s.kernel)) LIKE :completeName"),
        @NamedQuery(name = Software.FIND_BY_COMPLETE_NAME_QUERY,
                query = "SELECT s FROM Software s WHERE TRIM(TRAILING '"
                        + Software.ATTRIBUTES_SEPARATOR
                        + "' FROM CONCAT(s.name,'"
                        + Software.ATTRIBUTES_SEPARATOR + "',s.distribution,'"
                        + Software.ATTRIBUTES_SEPARATOR
                        + "',s.kernel)) = :completeName"),
        @NamedQuery(
                name = Software.FIND_NAMES_QUERY,
                query = "SELECT DISTINCT s.name FROM Software s ORDER BY s.name"),
        @NamedQuery(name = Software.FIND_NAMES_BY_OS_QUERY,
                query = "SELECT DISTINCT s.name FROM Software s"
                        + " WHERE s.operatingSystem = :operatingSystem"
                        + " ORDER BY s.name"),
        @NamedQuery(name = Software.FIND_DISTRIBUTIONS_BY_NAME_QUERY,
                query = "SELECT DISTINCT s.distribution FROM Software s"
                        + " WHERE s.name = :name ORDER BY s.distribution"),
        @NamedQuery(name = Software.FIND_DISTRIBUTIONS_BY_OS_NAME_QUERY,
                query = "SELECT DISTINCT s.distribution FROM Software s"
                        + " WHERE s.operatingSystem = :operatingSystem"
                        + " AND s.name = :name ORDER BY s.distribution"),
        @NamedQuery(
                name = Software.FIND_KERNELS_BY_NAME_DISTRI_QUERY,
                query = "SELECT DISTINCT s.kernel FROM Software s"
                        + " WHERE s.name = :name AND s.distribution = :distribution"
                        + " ORDER BY s.kernel") })
public class Software implements Serializable {
    
    private static final long serialVersionUID = -1L;
    
    /** The table name */
    public static final String TABLE_NAME = "software";
    /** The description column name */
    public static final String DESCRIPTION_COLUMN_NAME = "DESCRIPTION";
    /** The distribution column name */
    public static final String DISTRIBUTION_COLUMN_NAME = "DISTRIBUTION";
    /** The kernel column name */
    public static final String KERNEL_COLUMN_NAME = "KERNEL";
    /** The licence column name */
    public static final String LICENCE_COLUMN_NAME = "LICENCE";
    /** The manufacturer column name */
    public static final String MANUFACTURER_COLUMN_NAME = "MANUFACTURER";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    
    /**
     * Name of the named query selecting all Software
     */
    public static final String ALL_QUERY = "AllSoftware";
    
    /**
     * Name of the named query returning all Software being Operating Systems
     */
    public static final String ALL_OS = "AllOperatingSystems";
    
    /**
     * Name of the named query counting the number of Software having the
     * provided name, distribution and kernel. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search<br>
     * <b>distribution</b> the distribution to search<br>
     * <b>kernel</b> the kernel to search
     */
    public static final String EXIST_SOFTWARE_BY_NAME_DISTRIBUTION_KERNEL_QUERY =
            "existSoftwareByNameDistrAndKernel";
    
    /**
     * Name of the named query returning all Software having a complete name
     * similar to the provided one. <br>
     * This query is parameterized: <br>
     * <b>completeName</b> the complete name to search
     */
    public static final String SUGGESTION_BY_SUBS_COMPLETE_NAME_QUERY =
            "suggestionListByCompleteName";
    
    /**
     * Name of the named query returning the Software having the provided
     * complete name. <br>
     * This query is parameterized: <br>
     * <b>completeName</b> the complete name to search
     */
    public static final String FIND_BY_COMPLETE_NAME_QUERY =
            "findUniqueByCompleteName";
    
    /**
     * Name of the named query returning the names of all Softwares
     */
    public static final String FIND_NAMES_QUERY = "findNames";
    
    /**
     * Name of the named query returning the names of Software being an
     * operating system or not. <br>
     * This query is parameterized: <br>
     * <b>operatingSystem</b> true if the software is an operating system, else
     * false
     */
    public static final String FIND_NAMES_BY_OS_QUERY = "findNamesByOS";
    
    /**
     * Name of the named query returning the distributions of Software having
     * the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String FIND_DISTRIBUTIONS_BY_NAME_QUERY =
            "findDistributionsByName";
    
    /**
     * Name of the named query returning the distributions of Software being an
     * operating system or not and having the provided name. <br>
     * This query is parameterized: <br>
     * <b>operatingSystem</b> true if the software is an operating system, else
     * false<br>
     * <b>name</b> the name to search
     */
    public static final String FIND_DISTRIBUTIONS_BY_OS_NAME_QUERY =
            "findDistributionsByOSName";
    
    /**
     * Name of the named query returning the kernels of Software having the
     * provided name and distribution. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search<br>
     * <b>distribution</b> the distribution to search
     */
    public static final String FIND_KERNELS_BY_NAME_DISTRI_QUERY =
            "findKernelsByNameDistribution";
    
    /**
     * The separator for separating the software name, distribution and kernel
     * in complete name
     */
    public static final String ATTRIBUTES_SEPARATOR = " ";
    
    /**
     * List of Articles able to have software installed on
     */
    public static final Class<?>[] DEPLOYABLE_EQUIPMENT_CLASS = { PC.class,
            Board.class };
    
    /**
     * Compute the software complete name
     * 
     * @param pName
     *            the software name
     * @param pDistribution
     *            the software distribution
     * @param pKernel
     *            the software kernel
     * @return the software complete name
     */
    public static String getCompleteName(String pName, String pDistribution,
            String pKernel) {
        
        if (pName == null || pDistribution == null) {
            return null;
        }
        
        StringBuilder lCompleteName = new StringBuilder(pName);
        
        lCompleteName.append(ATTRIBUTES_SEPARATOR).append(pDistribution);
        
        if (!StringUtil.isEmptyOrNull(pKernel)) {
            lCompleteName.append(ATTRIBUTES_SEPARATOR).append(pKernel);
        }
        
        return lCompleteName.toString();
    }
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Version
    private Long version;
    
    @Column(name = NAME_COLUMN_NAME, nullable = false)
    private String name;
    
    @Column(name = DISTRIBUTION_COLUMN_NAME, nullable = false)
    private String distribution;
    
    @Column(name = KERNEL_COLUMN_NAME, nullable = false)
    private String kernel;
    
    @Column(name = MANUFACTURER_COLUMN_NAME)
    private String manufacturer;
    
    @Column(name = LICENCE_COLUMN_NAME)
    private String licence;
    
    @Column(name = DESCRIPTION_COLUMN_NAME)
    private String description;
    
    @Column
    private Boolean operatingSystem;
    
    @ManyToMany
    @JoinTable(name = "software_article", joinColumns = @JoinColumn(
            name = "SOFTWARE_ID"), inverseJoinColumns = @JoinColumn(
            name = "ARTICLE_ID"))
    private List<Article> equipments = new ArrayList<Article>();
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    /**
     * Default constructor
     */
    public Software() {
        super();
        name = "";
        distribution = "";
        kernel = "";
        operatingSystem = false;
    }
    
    /**
     * Constructor
     * 
     * @param name
     *            the software name
     * @param pDistribution
     *            the software distribution
     * @param pKernel
     *            the software kernel
     */
    public Software(String name, String pDistribution, String pKernel) {
        super();
        this.name = name;
        distribution = pDistribution;
        setKernel(pKernel);
    }
    
    /**
     * Method automatically called before updating in database
     */
    @PreUpdate
    public void onUpdate() {
        lastUpdate = new Date();
        validate();
    }
    
    /**
     * Method automatically called before persisting in database
     */
    @PrePersist
    public void onCreate() {
        creationDate = lastUpdate = new Date();
        validate();
    }
    
    private void validate() {
        if (StringUtil.isEmptyOrNull(name)
                || StringUtil.isEmptyOrNull(distribution) || kernel == null) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY));
        }
    }
    
    /**
     * Add the provided article to the list of equipment
     * 
     * @param equipment
     *            the article to add
     */
    public void addEquipement(Article equipment) {
        if (!equipments.contains(equipment)) {
            equipments.add(equipment);
        }
    }
    
    /**
     * Remove the provided article from the list of equipment
     * 
     * @param equipment
     *            the article to remove
     */
    public void removeEquipement(Article equipment) {
        equipments.remove(equipment);
    }
    
    @Override
    public String toString() {
        return getCompleteName();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result =
                prime
                        * result
                        + ((creationDate == null) ? 0 : creationDate.hashCode());
        result =
                prime * result
                        + ((description == null) ? 0 : description.hashCode());
        result =
                prime * result
                        + ((equipments == null) ? 0 : equipments.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result =
                prime * result
                        + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
        result = prime * result + ((licence == null) ? 0 : licence.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        
        if (distribution == null) {
            result = prime * result;
        }
        else {
            result = prime * result + distribution.hashCode();
        }
        
        if (kernel == null) {
            result = prime * result;
        }
        else {
            result = prime * result + kernel.hashCode();
        }
        
        if (operatingSystem == null) {
            result = prime * result;
        }
        else {
            result = prime * result + operatingSystem.hashCode();
        }
        
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
        Software other = (Software) obj;
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
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        }
        else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (kernel == null) {
            if (other.kernel != null) {
                return false;
            }
        }
        else if (!kernel.equals(other.kernel)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the complete name
     */
    public String getCompleteName() {
        return getCompleteName(name, distribution, kernel);
    }
    
    /**
     * @return the distribution
     */
    public String getDistribution() {
        return distribution;
    }
    
    /**
     * @param pDistribution
     *            the distribution to set
     */
    public void setDistribution(String pDistribution) {
        distribution = pDistribution;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @return the kernel
     */
    public String getKernel() {
        return kernel;
    }
    
    /**
     * @param pKernel
     *            the kernel to set
     */
    public void setKernel(String pKernel) {
        kernel = pKernel;
        if (kernel == null) {
            kernel = "";
        }
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
     * @return the licence
     */
    public String getLicence() {
        return licence;
    }
    
    /**
     * @param licence
     *            the licence to set
     */
    public void setLicence(String licence) {
        this.licence = licence;
    }
    
    /**
     * @return the operatingSystem
     */
    public Boolean getOperatingSystem() {
        return operatingSystem;
    }
    
    /**
     * @param operatingSystem
     *            the operatingSystem to set
     */
    public void setOperatingSystem(Boolean operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return the equipments
     */
    public List<Article> getEquipments() {
        return equipments;
    }
    
    /**
     * @param equipments
     *            the equipments to set
     */
    public void setEquipments(List<Article> equipments) {
        this.equipments = equipments;
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
    
}
