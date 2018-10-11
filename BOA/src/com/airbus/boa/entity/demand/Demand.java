/*
 * ------------------------------------------------------------------------
 * Class : Demand
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.demand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.LocationForDemand;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation class for Entity: Demand
 */
@Entity
@Table(name = Demand.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Demand.ALL_DEMANDS_QUERY,
                query = "SELECT d FROM Demand d"),
        @NamedQuery(name = Demand.DEMANDS_BY_ISSUER_QUERY,
                query = "SELECT d FROM Demand d WHERE d.issuer = :issuer"),
        @NamedQuery(
                name = Demand.DEMANDS_USING_SOFTWARE_QUERY,
                query = "SELECT d FROM Demand d WHERE :software MEMBER OF d.softwares"),
        @NamedQuery(
                name = Demand.DEMANDS_USING_PC_QUERY,
                query = "SELECT d FROM Demand d WHERE d.PCToReplace = :pc OR d.allocatedPC = :pc"),
        @NamedQuery(name = Demand.DEMAND_ALLOCATED_PC_QUERY,
                query = "SELECT d FROM Demand d WHERE d.allocatedPC = :pc"),
        @NamedQuery(name = Demand.DEMANDS_USING_TYPE_QUERY,
                query = "SELECT d FROM Demand d WHERE d.typePC = :type"),
        @NamedQuery(
                name = Demand.DEMANDS_USING_PRODUCTTYPE_QUERY,
                query = "SELECT d FROM Demand d WHERE d.productTypePC = :productType"),
        @NamedQuery(
                name = Demand.DEMANDS_LOCATED_INSTALLATION_QUERY,
                query = "SELECT d FROM Demand d WHERE d.containerInstallation = :installation"),
        @NamedQuery(
                name = Demand.DEMANDS_LOCATED_EXTERNAL_ENTITY_QUERY,
                query = "SELECT d FROM Demand d WHERE d.locationOrm.externalEntity = :externalEntity"),
        @NamedQuery(
                name = Demand.DEMANDS_LOCATED_PLACE_QUERY,
                query = "SELECT d FROM Demand d WHERE d.locationOrm.place = :place"),
        @NamedQuery(
                name = Demand.DEMANDS_LOCATED_ARTICLE_QUERY,
                query = "SELECT d FROM Demand d WHERE d.containerArticle = :article"),
        @NamedQuery(name = Demand.DEMANDS_LOCATED_TOOL_QUERY,
                query = "SELECT d FROM Demand d WHERE d.containerTool = :tool") })
public class Demand implements ContainedItem, LocatedItem, Serializable,
        Comparable<Demand> {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "demand";
    /** The requestNumber column name */
    public static final String REQUESTNUMBER_COLUMN_NAME = "REQUESTNUMBER";
    /** The department column name */
    public static final String DEPARTMENT_COLUMN_NAME = "DEPARTMENT";
    /** The program column name */
    public static final String PROGRAM_COLUMN_NAME = "PROGRAM";
    /** The project column name */
    public static final String PROJECT_COLUMN_NAME = "PROJECT";
    /** The budget column name */
    public static final String BUDGET_COLUMN_NAME = "BUDGET";
    /** The justification column name */
    public static final String JUSTIFICATION_COLUMN_NAME = "JUSTIFICATION";
    /** The contact column name */
    public static final String CONTACT_COLUMN_NAME = "CONTACT";
    /** The comments column name */
    public static final String COMMENTS_COLUMN_NAME = "COMMENTS";
    /** The owner column name */
    public static final String OWNER_COLUMN_NAME = "OWNER";
    /** The owner siglum column name */
    public static final String OWNERSIGLUM_COLUMN_NAME = "OWNERSIGLUM";
    /** The plugNumber column name */
    public static final String PLUGNUMBER_COLUMN_NAME = "PLUGNUMBER";
    /** The features column name */
    public static final String FEATURES_COLUMN_NAME = "FEATURES";
    /** The additional information column name */
    public static final String ADDITIONALINFORMATION_COLUMN_NAME =
            "ADDITIONALINFORMATION";
    
    /**
     * Name of the named query selecting all Demands
     */
    public static final String ALL_DEMANDS_QUERY = "allDemands";
    
    /**
     * Name of the named query selecting demands with the provided issuer. <br>
     * This query is parameterized:<br>
     * <b>issuer</b> the issuer to search
     */
    public static final String DEMANDS_BY_ISSUER_QUERY = "demandsByIssuer";
    
    /**
     * Name of the named query selecting demands using the provided software. <br>
     * This query is parameterized:<br>
     * <b>software</b> the software to search
     */
    public static final String DEMANDS_USING_SOFTWARE_QUERY =
            "demandsUsingSoftware";
    
    /**
     * Name of the named query selecting demands using the provided PC (to
     * replace or allocated). <br>
     * This query is parameterized:<br>
     * <b>pc</b> the pc to search
     */
    public static final String DEMANDS_USING_PC_QUERY = "demandsUsingPC";
    
    /**
     * Name of the named query selecting the demand having the provided PC as
     * allocated PC. <br>
     * This query is parameterized:<br>
     * <b>pc</b> the pc to search
     */
    public static final String DEMAND_ALLOCATED_PC_QUERY =
            "demandWithAllocatedPC";
    
    /**
     * Name of the named query selecting demands using the provided type. <br>
     * This query is parameterized:<br>
     * <b>type</b> the type to search
     */
    public static final String DEMANDS_USING_TYPE_QUERY = "demandsUsingType";
    
    /**
     * Name of the named query selecting demands using the provided product
     * type. <br>
     * This query is parameterized:<br>
     * <b>productType</b> the product type to search
     */
    public static final String DEMANDS_USING_PRODUCTTYPE_QUERY =
            "demandsUsingProductType";
    
    /**
     * Name of the named query selecting demands located in the provided
     * installation. <br>
     * This query is parameterized:<br>
     * <b>installation</b> the installation to search
     */
    public static final String DEMANDS_LOCATED_INSTALLATION_QUERY =
            "demandsLocatedInInstallation";
    
    /**
     * Name of the named query selecting demands located in the provided
     * external entity. <br>
     * This query is parameterized:<br>
     * <b>externalEntity</b> the external entity to search
     */
    public static final String DEMANDS_LOCATED_EXTERNAL_ENTITY_QUERY =
            "demandsLocatedInExternalEntity";
    
    /**
     * Name of the named query selecting demands located in the provided place. <br>
     * This query is parameterized:<br>
     * <b>place</b> the place to search
     */
    public static final String DEMANDS_LOCATED_PLACE_QUERY =
            "demandsLocatedInPlace";
    
    /**
     * Name of the named query selecting demands located in the provided
     * article. <br>
     * This query is parameterized:<br>
     * <b>article</b> the article to search
     */
    public static final String DEMANDS_LOCATED_ARTICLE_QUERY =
            "demandsLocatedInArticle";
    
    /**
     * Name of the named query selecting demands located in the provided tool. <br>
     * This query is parameterized:<br>
     * <b>tool</b> the tool to search
     */
    public static final String DEMANDS_LOCATED_TOOL_QUERY =
            "demandsLocatedInTool";
    
    @TableGenerator(name = "demandGen", table = "SEQUENCE",
            pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "DEMAND_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "demandGen")
    private Long id;
    
    @Version
    private Long version;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    @Column
    private String requestNumber;
    
    @OneToOne
    @JoinColumn(name = "TOREPLACE_PC_ID")
    private PC PCToReplace = null;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User issuer;
    
    @Column(name = OWNER_COLUMN_NAME)
    private String owner;
    
    @Column(name = OWNERSIGLUM_COLUMN_NAME)
    private String ownerSiglum;
    
    @Temporal(TemporalType.DATE)
    private Date needDate = new Date();
    
    @Column
    @Enumerated(EnumType.STRING)
    private DemandStatus status = DemandStatus.New;
    
    @OneToOne
    @JoinColumn(name = "ALLOCATED_PC_ID")
    private PC allocatedPC = null;
    
    @Temporal(TemporalType.DATE)
    private Date closureDate;
    
    @Column
    private String department;
    
    @Column
    private String program;
    
    @Column
    private String project;
    
    @Column
    private String budget;
    
    @Column
    private String justification;
    
    @Column
    private String contact;
    
    @Column
    private String comments;
    
    @ManyToOne
    @JoinColumn(name = "TYPEPC_TYPEARTICLE_ID")
    private TypePC typePC;
    
    @ManyToOne
    @JoinColumn(name = "PRODUCTTYPEPC_ID")
    private ProductTypePC productTypePC;
    
    @Column
    private String plugNumber;
    
    @Column
    private String features;
    
    @Column
    private String additionalInformation;
    
    @ManyToOne
    @JoinColumn(name = "USEDBY_INSTALLATION_ID")
    private Installation containerInstallation;
    
    @ManyToOne
    @JoinColumn(name = "USEDBY_ARTICLE_ID")
    private Article containerArticle;
    
    @ManyToOne
    @JoinColumn(name = "USEDBY_TOOL_ID")
    private Tool containerTool;
    
    @OneToOne(mappedBy = "demand", cascade = CascadeType.ALL)
    private LocationForDemand locationOrm;
    
    @ManyToMany
    @JoinTable(name = "software_demand", joinColumns = @JoinColumn(
            name = "DEMAND_ID"), inverseJoinColumns = @JoinColumn(
            name = "SOFTWARE_ID"))
    @OrderBy("operatingSystem DESC, name ASC, distribution ASC, kernel ASC")
    private List<Software> softwares = new ArrayList<Software>();
    
    @OneToOne
    @JoinColumn(name = "BUSINESSALLOCATIONPC_ID")
    private BusinessAllocationPC allocation;
    
    @OneToOne
    @JoinColumn(name = "BUSINESSUSAGEPC_ID")
    private BusinessUsagePC usage;
    
    /**
     * Default constructor
     */
    public Demand() {
        
    }
    
    /**
     * Constructor with mandatory attributes
     * 
     * @param pIssuer
     *            the demand issuer
     */
    public Demand(User pIssuer) {
        
        issuer = pIssuer;
    }
    
    /**
     * Constructor with mandatory attributes
     * 
     * @param pIssuer
     *            the demand issuer
     * @param pPCToReplace
     *            the PC to replace of the demand
     */
    public Demand(User pIssuer, PC pPCToReplace) {
        
        issuer = pIssuer;
        PCToReplace = pPCToReplace;
        
        if (PCToReplace != null) {
            productTypePC = PCToReplace.getProductType();
            typePC = (TypePC) PCToReplace.getTypeArticle();
            
            softwares = PCToReplace.getOperatingSystems();
            
            allocation = PCToReplace.getAllocation();
            usage = PCToReplace.getUsage();
            
            owner = PCToReplace.getOwner();
            ownerSiglum = PCToReplace.getOwnerSiglum();
            
            // initialize the location
            LocationManager lLocationManagerPC =
                    new LocationManager(PCToReplace);
            Location lLocation = lLocationManagerPC.getLocation();
            
            if (lLocation != null) {
                locationOrm =
                        new LocationForDemand(this, lLocation.isInherited(),
                                lLocation.getPlace(),
                                lLocation.getExternalEntity(),
                                lLocation.getPrecision());
            }
            
            // initialize the container
            ContainerManager lContainerManagerPC =
                    new ContainerManager(PCToReplace);
            Container lContainer = lContainerManagerPC.getContainer();
            ContainerManager lContainerManagerDemand =
                    new ContainerManager(this);
            
            if (lContainer != null) {
                ContainerType lContType = lContainer.getType();
                // loop on possible container types only to ensure that the
                // container is correct
                for (ContainerType lPossibleType : lContainerManagerDemand
                        .getPossibleContainers()) {
                    if (lPossibleType == lContType) {
                        switch (lContType) {
                        case Cabinet:
                            containerArticle =
                                    (Article) lContainer.getContainerItem();
                            break;
                        case Installation:
                            containerInstallation =
                                    (Installation) lContainer
                                            .getContainerItem();
                            break;
                        case Tool:
                            containerTool =
                                    (Tool) lContainer.getContainerItem();
                            break;
                        case Board:
                        case PC:
                        case Rack:
                        case Switch:
                        default:
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        Demand lOther = (Demand) pObj;
        if (id == null) {
            if (lOther.id != null) {
                return false;
            }
        }
        else {
            if (!id.equals(lOther.id)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return getDemandNumber();
    }
    
    @Override
    public int compareTo(Demand arg0) {
        
        return id.compareTo(arg0.id);
    }
    
    @Override
    public int hashCode() {
        final int lPrime = 31;
        int lResult = 1;
        lResult = lPrime * lResult;
        if (id != null) {
            lResult = lResult + id.hashCode();
        }
        return lResult;
    }
    
    /**
     * Update the lastUpdate field before update entity in database
     */
    @PreUpdate
    public void onUpdate() {
        lastUpdate = new Date();
    }
    
    /**
     * Update the creationDate field before create entity in database
     */
    @PrePersist
    public void onCreate() {
        creationDate = new Date();
        lastUpdate = creationDate;
    }
    
    /**
     * Add a software to demand's list of softwares.
     * 
     * @param soft
     *            Software to be added.
     */
    public void addSoftware(Software soft) {
        softwares.add(soft);
    }
    
    /**
     * @return a boolean indicating if the demand is ended (Closed or Cancelled)
     */
    public boolean isEnded() {
        
        switch (status) {
        
        case New:
        case Confirmed:
        case Available:
            return false;
            
        case Cancelled:
        case Closed:
        default:
            return true;
        }
    }
    
    /**
     * @return details about the demand for display in tool tip
     */
    public String getToolTipDetails() {
        
        String lNumberTitle = MessageBundle.getMessage(Constants.DEMAND_NUMBER);
        String lStatusTitle = MessageBundle.getMessage(Constants.DEMAND_STATUS);
        
        StringBuffer lSB = new StringBuffer();
        
        lSB.append(lNumberTitle).append(": ").append(getDemandNumber());
        lSB.append("<br/>\n");
        lSB.append(lStatusTitle).append(": ").append(status);
        
        return lSB.toString();
    }
    
    @Override
    public Location getLocation() {
        
        LocationManager lManager = new LocationManager(this);
        return lManager.getLocation();
    }
    
    @Override
    public Container getContainer() {
        
        ContainerManager lContainerManager = new ContainerManager(this);
        return lContainerManager.getContainer();
    }
    
    /**
     * @return the master container
     */
    public Container getMasterContainer() {
        
        ContainerManager lContainerManager = new ContainerManager(this);
        return lContainerManager.getMasterContainer();
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Demand;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Demand;
    }
    
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
     * Compute the demand number from the id in String form on 5 digits
     * 
     * @return the demandNumber
     */
    public String getDemandNumber() {
        if (id != null) {
            return String.format("%05d", id);
        }
        else {
            return MessageBundle.getMessage(Constants.AUTO_FILLED);
        }
    }
    
    /**
     * @return the requestNumber
     */
    public String getRequestNumber() {
        return requestNumber;
    }
    
    /**
     * @param pRequestNumber
     *            the requestNumber to set
     */
    public void setRequestNumber(String pRequestNumber) {
        requestNumber = pRequestNumber;
    }
    
    /**
     * @return the pCToReplace
     */
    public PC getPCToReplace() {
        return PCToReplace;
    }
    
    /**
     * @param pPCToReplace
     *            the pCToReplace to set
     */
    public void setPCToReplace(PC pPCToReplace) {
        PCToReplace = pPCToReplace;
    }
    
    /**
     * @return the issuer
     */
    public User getIssuer() {
        return issuer;
    }
    
    /**
     * @param pIssuer
     *            the issuer to set
     */
    public void setIssuer(User pIssuer) {
        issuer = pIssuer;
    }
    
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * @param pOwner
     *            the owner to set
     */
    public void setOwner(String pOwner) {
        owner = pOwner;
    }
    
    /**
     * @return the ownerSiglum
     */
    public String getOwnerSiglum() {
        return ownerSiglum;
    }
    
    /**
     * @param pOwnerSiglum
     *            the ownerSiglum to set
     */
    public void setOwnerSiglum(String pOwnerSiglum) {
        ownerSiglum = pOwnerSiglum;
    }
    
    /**
     * @return the needDate
     */
    public Date getNeedDate() {
        return needDate;
    }
    
    /**
     * @param pNeedDate
     *            the needDate to set
     */
    public void setNeedDate(Date pNeedDate) {
        needDate = pNeedDate;
    }
    
    /**
     * @return the status
     */
    public DemandStatus getStatus() {
        return status;
    }
    
    /**
     * @param pStatus
     *            the status to set
     */
    public void setStatus(DemandStatus pStatus) {
        status = pStatus;
    }
    
    /**
     * @return the allocatedPC
     */
    public PC getAllocatedPC() {
        return allocatedPC;
    }
    
    /**
     * @param pAllocatedPC
     *            the allocatedPC to set
     */
    public void setAllocatedPC(PC pAllocatedPC) {
        allocatedPC = pAllocatedPC;
    }
    
    /**
     * @return the closureDate
     */
    public Date getClosureDate() {
        return closureDate;
    }
    
    /**
     * @param pClosureDate
     *            the closureDate to set
     */
    public void setClosureDate(Date pClosureDate) {
        closureDate = pClosureDate;
    }
    
    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }
    
    /**
     * @param pDepartment
     *            the department to set
     */
    public void setDepartment(String pDepartment) {
        department = pDepartment;
    }
    
    /**
     * @return the program
     */
    public String getProgram() {
        return program;
    }
    
    /**
     * @param pProgram
     *            the program to set
     */
    public void setProgram(String pProgram) {
        program = pProgram;
    }
    
    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }
    
    /**
     * @param pProject
     *            the project to set
     */
    public void setProject(String pProject) {
        project = pProject;
    }
    
    /**
     * @return the budget
     */
    public String getBudget() {
        return budget;
    }
    
    /**
     * @param pBudget
     *            the budget to set
     */
    public void setBudget(String pBudget) {
        budget = pBudget;
    }
    
    /**
     * @return the justification
     */
    public String getJustification() {
        return justification;
    }
    
    /**
     * @param pJustification
     *            the justification to set
     */
    public void setJustification(String pJustification) {
        justification = pJustification;
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
    
    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }
    
    /**
     * @param pComments
     *            the comments to set
     */
    public void setComments(String pComments) {
        comments = pComments;
    }
    
    /**
     * @return the typePC
     */
    public TypePC getTypePC() {
        return typePC;
    }
    
    /**
     * @param pTypePC
     *            the typePC to set
     */
    public void setTypePC(TypePC pTypePC) {
        typePC = pTypePC;
    }
    
    /**
     * @return the productTypePC
     */
    public ProductTypePC getProductTypePC() {
        return productTypePC;
    }
    
    /**
     * @param pProductTypePC
     *            the productTypePC to set
     */
    public void setProductTypePC(ProductTypePC pProductTypePC) {
        productTypePC = pProductTypePC;
    }
    
    /**
     * @return the plugNumber
     */
    public String getPlugNumber() {
        return plugNumber;
    }
    
    /**
     * @param pPlugNumber
     *            the plugNumber to set
     */
    public void setPlugNumber(String pPlugNumber) {
        plugNumber = pPlugNumber;
    }
    
    /**
     * @return the features
     */
    public String getFeatures() {
        return features;
    }
    
    /**
     * @param pFeatures
     *            the features to set
     */
    public void setFeatures(String pFeatures) {
        features = pFeatures;
    }
    
    /**
     * @return the additionalInformation
     */
    public String getAdditionalInformation() {
        return additionalInformation;
    }
    
    /**
     * @param pAdditionalInformation
     *            the additionalInformation to set
     */
    public void setAdditionalInformation(String pAdditionalInformation) {
        additionalInformation = pAdditionalInformation;
    }
    
    /**
     * @return the locationOrm
     */
    public LocationForDemand getLocationOrm() {
        return locationOrm;
    }
    
    /**
     * @param pLocationOrm
     *            the locationOrm to set
     */
    public void setLocationOrm(LocationForDemand pLocationOrm) {
        locationOrm = pLocationOrm;
    }
    
    /**
     * @return the containerInstallation
     */
    public Installation getContainerInstallation() {
        return containerInstallation;
    }
    
    /**
     * @param pContainerInstallation
     *            the containerInstallation to set
     */
    public void setContainerInstallation(Installation pContainerInstallation) {
        containerInstallation = pContainerInstallation;
    }
    
    /**
     * @return the containerArticle
     */
    public Article getContainerArticle() {
        return containerArticle;
    }
    
    /**
     * @param pContainerArticle
     *            the containerArticle to set
     */
    public void setContainerArticle(Article pContainerArticle) {
        containerArticle = pContainerArticle;
    }
    
    /**
     * @return the containerTool
     */
    public Tool getContainerTool() {
        return containerTool;
    }
    
    /**
     * @param pContainerTool
     *            the containerTool to set
     */
    public void setContainerTool(Tool pContainerTool) {
        containerTool = pContainerTool;
    }
    
    /**
     * @return the softwares
     */
    public List<Software> getSoftwares() {
        return softwares;
    }
    
    /**
     * @param softwares
     *            the softwares to set
     */
    public void setSoftwares(List<Software> softwares) {
        this.softwares = softwares;
    }
    
    /**
     * @return the list of software in string form
     */
    public String getSoftwaresString() {
        
        StringBuffer lSB = new StringBuffer();
        boolean lIsFirstAdded = false;
        for (Software lSoftware : softwares) {
            if (lIsFirstAdded) {
                lSB.append(", ");
            }
            else {
                lIsFirstAdded = true;
            }
            lSB.append(lSoftware.getCompleteName());
        }
        return lSB.toString();
    }
    
    /**
     * @return the allocation
     */
    public BusinessAllocationPC getAllocation() {
        return allocation;
    }
    
    /**
     * @param pAllocation
     *            the allocation to set
     */
    public void setAllocation(BusinessAllocationPC pAllocation) {
        allocation = pAllocation;
    }
    
    /**
     * @return the usage
     */
    public BusinessUsagePC getUsage() {
        return usage;
    }
    
    /**
     * @param pUsage
     *            the usage to set
     */
    public void setUsage(BusinessUsagePC pUsage) {
        usage = pUsage;
    }
}
