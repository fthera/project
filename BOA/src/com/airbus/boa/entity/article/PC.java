/*
 * ------------------------------------------------------------------------
 * Class : PC
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.Contains_PC_Board;
import com.airbus.boa.entity.memory.MemorySlot;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Entity implementation class for Entity: PC
 */
@Entity
@Table(name = PC.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = PC.BY_NAME_QUERY,
                query = "SELECT p FROM PC p WHERE p.name = :name"),
        @NamedQuery(name = PC.COUNT_BY_NAME_QUERY,
                query = "SELECT COUNT(p) FROM PC p WHERE p.name = :name"),
        @NamedQuery(name = PC.BY_SN_QUERY,
                query = "SELECT p FROM PC p WHERE p.airbusSN = :sn"),
        @NamedQuery(
                name = PC.BY_INCHARGE_QUERY,
                query = "SELECT COUNT(p) FROM PC p WHERE p.inCharge = :inCharge"),
        @NamedQuery(name = PC.EXIST_PC_BY_SN_QUERY,
                query = "SELECT COUNT(p) FROM PC p WHERE p.airbusSN = :sn"),
        @NamedQuery(name = PC.PC_AUTO_UPDATE_QUERY,
                query = "SELECT p FROM PC p WHERE p.autoUpdate = true"),
        @NamedQuery(name = PC.PC_ALL_QUERY,
                query = "SELECT p FROM PC p") })
public class PC extends Article implements Serializable {
    
    /**
     * Availability status of a PC
     */
    public static enum AvailabilityStatus {
        /**
         * The PC is not yet or just received from the supplier (purchase) and
         * is available
         */
        New,
        /** The PC has been replaced and is available */
        Replaced,
        /** The PC is in use and is not available */
        InUse,
        /** The PC is free */
        Free;
        
        /**
         * @return the Message Bundle key of this state
         */
        private String getMessageBundleKey() {
            return this.getClass().getSimpleName() + name();
        }
        
        @Override
        public String toString() {
            return MessageBundle.getMessage(getMessageBundleKey());
        }
        
        /**
         * Return an array of all values but the InUse one
         * 
         * @return the array of all not in use values
         */
        public static AvailabilityStatus[] valuesNotInUse() {
            ArrayList<AvailabilityStatus> lValues =
                    new ArrayList<AvailabilityStatus>();
            for (AvailabilityStatus lStatus : AvailabilityStatus.values()) {
                if (lStatus != AvailabilityStatus.InUse) {
                    lValues.add(lStatus);
                }
            }
            return lValues.toArray(new AvailabilityStatus[lValues.size()]);
        }

        
        /**
         * Return the list of SelectItem objects corresponding to all possible
         * values for the enumeration
         * 
         * @return the list of all SelectItem objects
         */
        public static List<SelectItem> getSelectItems() {
            List<SelectItem> lResult = new ArrayList<SelectItem>();
            for (AvailabilityStatus lStatus : AvailabilityStatus.values()) {
                lResult.add(new SelectItem(lStatus, lStatus.toString()));
            }
            return lResult;
        }
        
        /**
         * @param pString
         *            the availability status string returned by the
         *            <b>getStringValue</b>
         *            method
         * @return the availability status if the provided string is available,
         *         else null
         */
        public static AvailabilityStatus getEnumValue(String pString) {
            
            AvailabilityStatus[] lValues = AvailabilityStatus.values();
            List<String> lBundleKeys = new ArrayList<String>();
            
            for (AvailabilityStatus lStatus : lValues) {
                lBundleKeys.add(lStatus.getMessageBundleKey());
            }
            
            Map<String, String> lMap =
                    MessageBundle.getAllDefaultMessages(lBundleKeys);
            for (AvailabilityStatus lStatus : AvailabilityStatus.values()) {
                String lMessage = lMap.get(lStatus.getMessageBundleKey());
                if (lMessage != null && lMessage.equals(pString)) {
                    return lStatus;
                }
            }
            
            lMap = MessageBundle.getAllFrenchMessages(lBundleKeys);
            for (AvailabilityStatus lStatus : AvailabilityStatus.values()) {
                String lMessage = lMap.get(lStatus.getMessageBundleKey());
                if (lMessage != null && lMessage.equals(pString)) {
                    return lStatus;
                }
            }
            return null;
        }
    }
    
    /**
     * Type of automatic update of a PC
     */
    public static enum AutoUpdateType {
        /** The PC is automatically updated executing Sysmon on it */
        Sysmon,
        /** The PC is automatically updated importing a file in Sysmon format */
        File;
        
        @Override
        public String toString() {
            String lKey = getClass().getSimpleName() + super.toString();
            String lValue = MessageBundle.getMessage(lKey);
            if (lValue.equals(lKey)) {
                return super.toString();
            }
            else {
                return lValue;
            }
        }
    }
    
    private static final long serialVersionUID = -1L;
    
    /** The table name */
    public static final String TABLE_NAME = "pc";
    /** The admin column name */
    public static final String ADMIN_COLUMN_NAME = "ADMIN";
    /** The assignment column name */
    public static final String ASSIGNMENT_COLUMN_NAME = "ASSIGNMENT";
    /** The comment column name */
    public static final String COMMENT_COLUMN_NAME = "COMMENT";
    /** The function column name */
    public static final String FUNCTION_COLUMN_NAME = "FUNCTION";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    /** The owner column name */
    public static final String OWNER_COLUMN_NAME = "OWNER";
    /** The owner siglum column name */
    public static final String OWNERSIGLUM_COLUMN_NAME = "OWNERSIGLUM";
    /** The platform column name */
    public static final String PLATFORM_COLUMN_NAME = "PLATFORM";
    
    /**
     * Name of the named query counting the number of PCs with the provided
     * Airbus SN. <br>
     * This query is parameterized: <br>
     * <b>sn</b> the Airbus SN to search
     */
    public static final String EXIST_PC_BY_SN_QUERY = "ExistPCBySN";
    
    /**
     * Name of the named query selecting PCs with the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "PCByName";
    
    /**
     * Name of the named query counting PCs with the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String COUNT_BY_NAME_QUERY = "PCCountByName";
    
    /**
     * Name of the named query counting the number of PCs with the provided
     * user in charge. <br>
     * This query is parameterized: <br>
     * <b>inCharge</b> the User in charge to search
     */
    public static final String BY_INCHARGE_QUERY = "PCByIncharge";
    
    /**
     * Name of the named query selecting PCs with the provided Airbus SN. <br>
     * This query is parameterized: <br>
     * <b>sn</b> the Airbus SN to search
     */
    public static final String BY_SN_QUERY = "PCBySN";
    
    /**
     * Name of the named query selecting PCs having automatic update activated
     */
    public static final String PC_AUTO_UPDATE_QUERY = "PCAutoUpdate";
    
    /**
     * Name of the named query selecting LINUX PCs
     */
    public static final String PC_ALL_QUERY = "AllPC";
    
    @Column(name = NAME_COLUMN_NAME)
    private String name;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTINCHARGE_ID")
    private DepartmentInCharge departmentInCharge;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCTTYPEPC_ID")
    private ProductTypePC productType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFAULTOS_SOFTWARE_ID")
    private Software defaultOS = null;
    
    @Column(name = FUNCTION_COLUMN_NAME)
    private String function;
    
    @Column
    private Integer nbScreens;
    
    @Column(name = COMMENT_COLUMN_NAME)
    private String comment;
    
    @Column
    private Boolean autoUpdate;
    
    @Column(name = "LASTAUTOUPDATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAutoUpdate;
    
    @Column
    @Enumerated(EnumType.STRING)
    private AutoUpdateType lastAutoUpdateType;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date autoDataValidityDate;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date availabilityDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;
    
    @Column(name = ADMIN_COLUMN_NAME)
    private String admin;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESSALLOCATIONPC_ID")
    private BusinessAllocationPC allocation;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User inCharge;
    
    @Column(name = OWNER_COLUMN_NAME)
    private String owner;
    
    @Column(name = OWNERSIGLUM_COLUMN_NAME)
    private String ownerSiglum;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESSUSAGEPC_ID")
    private BusinessUsagePC usage;
    
    @Column(name = ASSIGNMENT_COLUMN_NAME)
    private String assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_ID")
    private Domain domain;
    
    @Column(name = PLATFORM_COLUMN_NAME)
    private String platform;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL },
            orphanRemoval = true)
    @JoinTable(name = "pc_communicationport", joinColumns = @JoinColumn(
            name = "PC_ID"), inverseJoinColumns = @JoinColumn(
            name = "COMMUNICATIONPORT_ID"))
    private List<CommunicationPort> ports = new ArrayList<CommunicationPort>();
    
    @OneToMany(mappedBy = "pc")
    private List<Contains_PC_Board> containedOrmBoards =
            new ArrayList<Contains_PC_Board>();
    
    @OneToOne(mappedBy = "PCToReplace", fetch = FetchType.LAZY)
    private Demand replacementDemand;
    
    @OneToOne(mappedBy = "allocatedPC", fetch = FetchType.LAZY)
    private Demand allocatedToDemand;
    
    @Column(name = "CPUTYPE")
    private String cpuType;
    
    @Column(name = "CPUMAXSPEED")
    private String cpuMaxSpeed;
    
    @Column(name = "CPUSOCKETS")
    private Integer cpuSockets;
    
    @Column(name = "CPUCOREPERSOCKET")
    private Integer cpuCorePerSocket;
    
    @Column(name = "CPULOGICALCPUPERSOCKET")
    private Integer cpuLogicalCpuPerSocket;
    
    @Column(name = "CPUTOTALLOGICALCPUS")
    private Integer cpuTotalLogicalCpus;
    
    @Column(name = "CPUHYPERTHREADING")
    private String cpuHyperthreading;
    
    @Column(name = "TOTALMEMORYSIZE")
    private String totalMemorySize;
    
    @OneToMany(mappedBy = "pc", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    private List<MemorySlot> memorySlots = new ArrayList<MemorySlot>();
    
    @OneToMany(mappedBy = "pc", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    private List<PCSpecificity> specificities = new ArrayList<PCSpecificity>();
    
    /**
     * Constructor
     */
    public PC() {
        super();
    }
    
    /**
     * Constructor with mandatory attributes. <br>
     * Set the provided values and clear the ports.
     * 
     * @param airbusSN
     *            the Airbus SN
     * @param pProductType
     *            the product type
     * @param name
     *            the name
     * @param pTypeArticle
     *            the article type
     * @param allocation
     *            the business allocation
     * @param usage
     *            the business usage
     * @param pInCharge
     *            the person in charge
     * @param pOwner
     *            the owner
     * @param pOwnerSiglum
     *            the owner siglum
     * @param availabilityStatus
     *            the availability status.
     */
    public PC(String airbusSN, ProductTypePC pProductType, String name,
            DepartmentInCharge pDepartmentInCharge,
            TypeArticle pTypeArticle, BusinessAllocationPC allocation,
            BusinessUsagePC usage, User pInCharge, String pOwner,
            String pOwnerSiglum, AvailabilityStatus availabilityStatus) {
        
        super();
        
        this.airbusSN = airbusSN;
        productType = pProductType;
        this.name = name;
        this.departmentInCharge = pDepartmentInCharge;
        typeArticle = pTypeArticle;
        this.allocation = allocation;
        this.usage = usage;
        inCharge = pInCharge;
        owner = pOwner;
        ownerSiglum = pOwnerSiglum;
        this.availabilityStatus = availabilityStatus;
        
        ports.clear();
    }
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypePC();
    }
    
    @Override
    public void validate() {
        
        if (StringUtil.isEmptyOrNull(airbusSN) || productType == null
                || StringUtil.isEmptyOrNull(name) || allocation == null
                || usage == null || inCharge == null
                || StringUtil.isEmptyOrNull(owner)
                || StringUtil.isEmptyOrNull(ownerSiglum)) {
            
            throw new ValidationException(
                    MessageBundle
                            .getMessage(Constants.PC_MANDATORY_FIELD_EMPTY));
        }
    }
    
    /**
     * @return a boolean indicating if the PC is available (according to its
     *         availability status)
     */
    public boolean isAvailable() {
        switch (availabilityStatus) {
        case Free:
        case New:
        case Replaced:
            return true;
        case InUse:
        default:
            return false;
        }
    }
    
    @Override
    public List<? extends Article> getChildren() {
        
        List<Article> result = new ArrayList<Article>();
        
        // Retrieve the common children
        result.addAll(super.getChildren());
        
        // Add the Boards from specific relation with PC
        for (Contains_PC_Board relation : getContainedOrmBoards()) {
            
            result.add(relation.getBoard());
        }
        
        Collections.sort(result);
        return result;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = super.getContainedItems();
        
        for (Contains_PC_Board lContainerOrm : containedOrmBoards) {
            lContainedItems.add(lContainerOrm.getBoard());
        }
        
        return lContainedItems;
    }
    
    /**
     * @return the list of Software being Operating System installed on PC
     */
    public List<Software> getOperatingSystems() {
        
        List<Software> lSoftwareList = getSoftwares();
        List<Software> lOperatingSystems = new ArrayList<Software>();
        
        for (Software lSoftware : lSoftwareList) {
            if (lSoftware.getOperatingSystem()) {
                lOperatingSystems.add(lSoftware);
            }
        }
        return lOperatingSystems;
    }
    
    /**
     * @return the list of Software being Operating System installed on PC in
     *         string form
     */
    public String getOperatingSystemsNames() {
        
        return getSoftwareListNames(getOperatingSystems());
    }
    
    /**
     * @param pSoftwareList
     *            the list of Software
     * @return the list of complete name values of the provided software list in
     *         string form
     */
    private String getSoftwareListNames(List<Software> pSoftwareList) {
        
        StringBuffer lSB = new StringBuffer();
        Iterator<Software> lIterator = pSoftwareList.iterator();
        Software lSoftware;
        
        while (lIterator.hasNext()) {
            
            lSoftware = lIterator.next();
            lSB.append(lSoftware.getCompleteName());
            
            if (lIterator.hasNext()) {
                lSB.append(", ");
            }
        }
        
        return lSB.toString();
    }
    
    /**
     * @return the containedOrmBoards
     */
    public List<Contains_PC_Board> getContainedOrmBoards() {
        return containedOrmBoards;
    }
    
    /**
     * @param contains
     *            the containedOrmBoards to set
     */
    public void setContainedOrmBoard(List<Contains_PC_Board> contains) {
        containedOrmBoards = contains;
    }
    
    /**
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }
    
    /**
     * @param pDomain
     *            the domain to set
     */
    public void setDomain(Domain pDomain) {
        domain = pDomain;
    }
    
    @Override
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
     * @return the departmentInCharge
     */
    public DepartmentInCharge getDepartmentInCharge() {
        return departmentInCharge;
    }
    
    /**
     * @param pDepartmentInCharge
     *            the departmentInCharge to set
     */
    public void setDepartmentInCharge(DepartmentInCharge pDepartmentInCharge) {
        departmentInCharge = pDepartmentInCharge;
    }
    
    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * @param pPlatform
     *            the platform to set
     */
    public void setPlatform(String pPlatform) {
        platform = pPlatform;
    }
    
    /**
     * @return the productType
     */
    public ProductTypePC getProductType() {
        return productType;
    }
    
    /**
     * @param pProductType
     *            the productType to set
     */
    public void setProductType(ProductTypePC pProductType) {
        productType = pProductType;
    }
    
    /**
     * @return the function
     */
    public String getFunction() {
        return function;
    }
    
    /**
     * @param function
     *            the function to set
     */
    public void setFunction(String function) {
        this.function = function;
    }
    
    /**
     * @return the defaultOS
     */
    public Software getDefaultOS() {
        
        return defaultOS;
    }
    
    /**
     * @param pDefaultOS
     *            the defaultOS to set
     */
    public void setDefaultOS(Software pDefaultOS) {
        
        defaultOS = pDefaultOS;
    }
    
    /**
     * @return the nbScreens
     */
    public Integer getNbScreens() {
        return nbScreens;
    }
    
    /**
     * @param nbScreens
     *            the nbScreens to set
     */
    public void setNbScreens(Integer nbScreens) {
        this.nbScreens = nbScreens;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return the availabilityDate
     */
    public Date getAvailabilityDate() {
        return availabilityDate;
    }
    
    /**
     * @param availabilityDate
     *            the availabilityDate to set
     */
    public void setAvailabilityDate(Date availabilityDate) {
        this.availabilityDate = availabilityDate;
    }
    
    /**
     * @return the availabilityStatus
     */
    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }
    
    /**
     * @param pAvailabilityStatus
     *            the availabilityStatus to set
     */
    public void setAvailabilityStatus(AvailabilityStatus pAvailabilityStatus) {
        availabilityStatus = pAvailabilityStatus;
    }
    
    /**
     * @return the admin
     */
    public String getAdmin() {
        return admin;
    }
    
    /**
     * @param admin
     *            the admin to set
     */
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    
    /**
     * @return the allocation
     */
    public BusinessAllocationPC getAllocation() {
        return allocation;
    }
    
    /**
     * @param allocation
     *            the allocation to set
     */
    public void setAllocation(BusinessAllocationPC allocation) {
        this.allocation = allocation;
    }
    
    /**
     * @return the inCharge
     */
    public User getInCharge() {
        return inCharge;
    }
    
    /**
     * @param pInCharge
     *            the inCharge to set
     */
    public void setInCharge(User pInCharge) {
        inCharge = pInCharge;
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
     * @return the usage
     */
    public BusinessUsagePC getUsage() {
        return usage;
    }
    
    /**
     * @param usage
     *            the usage to set
     */
    public void setUsage(BusinessUsagePC usage) {
        this.usage = usage;
    }
    
    /**
     * @return the assignment
     */
    public String getAssignment() {
        return assignment;
    }
    
    /**
     * @param assignment
     *            the assignment to set
     */
    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }
    
    /**
     * @return the ports
     */
    public List<CommunicationPort> getPorts() {
        return ports;
    }
    
    /**
     * @param ports
     *            the ports to set
     */
    public void setPorts(List<CommunicationPort> ports) {
        this.ports = ports;
    }
    
    /**
     * @return the replacementDemand
     */
    public Demand getReplacementDemand() {
        return replacementDemand;
    }
    
    /**
     * @param pReplacementDemand
     *            the replacementDemand to set
     */
    public void setReplacementDemand(Demand pReplacementDemand) {
        replacementDemand = pReplacementDemand;
    }
    
    /**
     * @return the allocatedToDemand
     */
    public Demand getAllocatedToDemand() {
        return allocatedToDemand;
    }
    
    /**
     * @param pAllocatedToDemand
     *            the allocatedToDemand to set
     */
    public void setAllocatedToDemand(Demand pAllocatedToDemand) {
        allocatedToDemand = pAllocatedToDemand;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.PC;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.PC;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.PC;
    }
    
    /**
     * @return the autoDataValidityDate
     */
    public Date getAutoDataValidityDate() {
        return autoDataValidityDate;
    }
    
    /**
     * @param pAutoDataValidityDate
     *            the autoDataValidityDate to set
     */
    public void setAutoDataValidityDate(Date pAutoDataValidityDate) {
        autoDataValidityDate = pAutoDataValidityDate;
    }
    
    /**
     * @return the autoUpdate
     */
    public Boolean getAutoUpdate() {
        return autoUpdate;
    }
    
    /**
     * @param pAutoUpdate
     *            the autoUpdate to set
     */
    public void setAutoUpdate(Boolean pAutoUpdate) {
        autoUpdate = pAutoUpdate;
    }
    
    /**
     * @return the lastAutoUpdate
     */
    public Date getLastAutoUpdate() {
        return lastAutoUpdate;
    }
    
    /**
     * @param pLastAutoUpdate
     *            the lastAutoUpdate to set
     */
    public void setLastAutoUpdate(Date pLastAutoUpdate) {
        lastAutoUpdate = pLastAutoUpdate;
    }
    
    /**
     * @return the lastAutoUpdateType
     */
    public AutoUpdateType getLastAutoUpdateType() {
        return lastAutoUpdateType;
    }
    
    /**
     * @param pLastAutoUpdateType
     *            the lastAutoUpdateType to set
     */
    public void setLastAutoUpdateType(AutoUpdateType pLastAutoUpdateType) {
        lastAutoUpdateType = pLastAutoUpdateType;
    }
    
    /**
     * @param pCpuType
     *            the cpuType to set
     */
    public void setCpuType(String pCpuType) {
        cpuType = pCpuType;
    }
    
    /**
     * @param pCpuMaxSpeed
     *            the cpuMaxSpeed to set
     */
    public void setCpuMaxSpeed(String pCpuMaxSpeed) {
        cpuMaxSpeed = pCpuMaxSpeed;
    }
    
    /**
     * @param pCpuSockets
     *            the cpuSockets to set
     */
    public void setCpuSockets(Integer pCpuSockets) {
        cpuSockets = pCpuSockets;
    }
    
    /**
     * @param pCpuCorePerSocket
     *            the cpuCorePerSocket to set
     */
    public void setCpuCorePerSocket(Integer pCpuCorePerSocket) {
        cpuCorePerSocket = pCpuCorePerSocket;
    }
    
    /**
     * @param pCpuLogicalCpuPerSocket
     *            the cpuLogicalCpuPerSocket to set
     */
    public void setCpuLogicalCpuPerSocket(Integer pCpuLogicalCpuPerSocket) {
        cpuLogicalCpuPerSocket = pCpuLogicalCpuPerSocket;
    }
    
    /**
     * @param pCpuTotalLogicalCpus
     *            the cpuTotalLogicalCpus to set
     */
    public void setCpuTotalLogicalCpus(Integer pCpuTotalLogicalCpus) {
        cpuTotalLogicalCpus = pCpuTotalLogicalCpus;
    }
    
    /**
     * @param pCpuHyperthreading
     *            the cpuHyperthreading to set
     */
    public void setCpuHyperthreading(String pCpuHyperthreading) {
        cpuHyperthreading = pCpuHyperthreading;
    }
    
    /**
     * @param pTotalMemorySize
     *            the totalMemorySize to set
     */
    public void setTotalMemorySize(String pTotalMemorySize) {
        totalMemorySize = pTotalMemorySize;
    }
    
    /**
     * @return the memorySlots
     */
    public List<MemorySlot> getMemorySlots() {
        return memorySlots;
    }
    
    public List<PCSpecificity> getSpecificities() {
        return specificities;
    }
    
    public String getHasSpecificities() {
        return specificities.isEmpty() ? "" : MessageBundle.getMessage("yes");
    }
}
