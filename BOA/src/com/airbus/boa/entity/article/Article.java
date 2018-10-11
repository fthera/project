/*
 * ------------------------------------------------------------------------
 * Class : Article
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Contains_Tool_Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.LocationForArticle;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerItem;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Entity implementation class for entity: Article
 */
@Entity
@Table(name = Article.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(name = "AllArticle",
                query = "SELECT DISTINCT a FROM Article a"
                        + " LEFT JOIN FETCH a.airbusPN"
                        + " LEFT JOIN FETCH a.manufacturerPN"
                        + " LEFT JOIN FETCH a.typeArticle"
                        + " LEFT JOIN FETCH a.locationOrm ORDER BY a.airbusSN"),
        @NamedQuery(
                name = "ExistAirbusSN",
                query = "SELECT a.airbusSN FROM Article a WHERE a.airbusSN = :sn"),
        @NamedQuery(
                name = "ExistManufacturerSN",
                query = "SELECT a.manufacturerSN FROM Article a WHERE a.manufacturerSN =:sn"),
        @NamedQuery(
                name = "ArticleBySN",
                query = "SELECT a FROM Article a WHERE a.airbusSN =:sn OR a.manufacturerSN = :sn"),
        @NamedQuery(name = Article.BY_ID_QUERY,
                query = "SELECT a FROM Article a WHERE a.id = :primaryKey"),
        @NamedQuery(name = Article.BY_AIRBUSPN_ID_QUERY,
                query = "SELECT a FROM Article a WHERE a.airbusPN.id = :pn"),
        @NamedQuery(
                name = Article.BY_MANUFACTURERPN_ID_QUERY,
                query = "SELECT a FROM Article a WHERE a.manufacturerPN.id = :pn"),
        @NamedQuery(
                name = Article.COUNT_BY_TYPEARTICLE_ID_QUERY,
                query = "SELECT COUNT(a) FROM Article a WHERE a.typeArticle.id = :id"),
        @NamedQuery(
                name = Article.CMSCODE_BY_TYPEARTICLE_ID_QUERY,
                query = "SELECT a.cmsCode FROM Article a"
                        + " WHERE a.typeArticle.id = :typeArtId AND a.cmsCode IS NOT NULL"
                        + " ORDER BY a.cmsCode ASC"),
        @NamedQuery(name = Article.TYPEARTICLE_BY_MANUFACTURERPN_QUERY,
                query = "SELECT DISTINCT a.typeArticle FROM Article a"
                        + " WHERE a.manufacturerPN = :pn"),
        @NamedQuery(
                name = Article.MANUFACTURERPN_BY_TYPEARTICLE_ID_QUERY,
                query = "SELECT DISTINCT a.manufacturerPN FROM Article a"
                        + " WHERE a.typeArticle.id = :id AND a.manufacturerPN IS NOT NULL") })
public abstract class Article implements Item, ContainerItem, ContainedItem,
        LocatedItem, Serializable, Comparable<Article> {
    
    private static final long serialVersionUID = -2152781114784397203L;
    
    /** The table name */
    public static final String TABLE_NAME = "article";
    /** The Airbus SN column name */
    public static final String AIRBUSSN_COLUMN_NAME = "AIRBUSSN";
    /** The CMS Code column name */
    public static final String CMSCODE_COLUMN_NAME = "CMSCODE";
    /** The Manufacturer SN column name */
    public static final String MANUFACTURERSN_COLUMN_NAME = "MANUFACTURERSN";
    
    /**
     * Name of the named query selecting the Article having the provided id.<br>
     * This query is parameterized:<br>
     * <b>primaryKey</b> the Article id
     */
    public static final String BY_ID_QUERY = "ArticleById";
    
    /**
     * Name of the named query selecting the Article having the Airbus PN
     * corresponding to the provided id.<br>
     * This query is parameterized:<br>
     * <b>pn</b> the Airbus PN id
     */
    public static final String BY_AIRBUSPN_ID_QUERY = "ArticleByAirbusPNId";
    
    /**
     * Name of the named query selecting the Article having the Manufacturer PN
     * corresponding to the provided id.<br>
     * This query is parameterized:<br>
     * <b>pn</b> the Manufacturer PN id
     */
    public static final String BY_MANUFACTURERPN_ID_QUERY =
            "ArticleByManufacturerPNId";
    
    /**
     * Name of the named query counting the Article having the Type Article
     * corresponding to the provided id.<br>
     * This query is parameterized:<br>
     * <b>id</b> the TypeArticle id
     */
    public static final String COUNT_BY_TYPEARTICLE_ID_QUERY =
            "ArticleCountByTypeArticleId";
    
    /**
     * Name of the named query selecting the CMS Code of the Article having the
     * provided TypeArticle.<br>
     * This query is parameterized:<br>
     * <b>typeArtId</b> the TypeArticle id
     */
    public static final String CMSCODE_BY_TYPEARTICLE_ID_QUERY =
            "CmsCodeByTypeArticleId";
    
    /**
     * Name of the named query selecting the TypeArticle of the Articles having
     * the Manufacturer PN similar to the provided one.<br>
     * This query is parameterized:<br>
     * <b>pn</b> the Manufacturer PN
     */
    public static final String TYPEARTICLE_BY_MANUFACTURERPN_QUERY =
            "TypeArticleByManufacturerPN";
    
    /**
     * Name of the named query selecting the Manufacturer PN of the Articles
     * having the TypeArticle corresponding to the provided id.<br>
     * This query is parameterized:<br>
     * <b>id</b> the TypeArticle id
     */
    public static final String MANUFACTURERPN_BY_TYPEARTICLE_ID_QUERY =
            "ManufacturerPNByTypeArticleId";
    
    /*
     * Static attributes and methods for displaying information
     */
    
    public static final String SLOT_PREFIX = "Slot ";
    public static final String SEPARATOR_FIELD = " | ";
    
    /**
     * Compute the detailed name of the article for displaying it in one line
     * 
     * @param pArticle
     *            the article
     * @param pWithContainerDetails
     *            boolean indicating if the details on container have to be
     *            added or not (e.g.: slot number, letter of cabinet into an
     *            installation, details...)
     * @return the detailed name of the article
     */
    public static String computeDetailedName(Article pArticle,
            boolean pWithContainerDetails) {
        
        StringBuffer lDetailedName = new StringBuffer();
        
        if (pWithContainerDetails) {
            lDetailedName.append(computeContainerDetails(pArticle));
            lDetailedName.append(SEPARATOR_FIELD);
        }
        
        lDetailedName.append(computeDetailedName(pArticle));
        
        return lDetailedName.toString();
    }
    
    /**
     * Compute the details on container (e.g.: slot number, letter of cabinet
     * into an installation, details...)
     * 
     * @param pArticle
     *            the article
     * @return the details on container of the article
     */
    public static String computeContainerDetails(Article pArticle) {
        
        Container lContainer = pArticle.getContainer();
        StringBuilder lName = new StringBuilder();
        if (lContainer != null && lContainer.getType() != null) {
            String lPrecision = lContainer.getPrecision();
            
            if (!StringUtil.isEmptyOrNull(lPrecision)) {
                
                switch (lContainer.getType()) {
                
                case Rack:
                case PC:
                    // Add the slot prefix when a board is located into a Rack
                    // or a PC
                    lName.append(SLOT_PREFIX);
                    break;
                
                case Board:
                case Cabinet:
                case Installation:
                case Switch:
                case Tool:
                default:
                    // No prefix to add
                }
                lName.append(lPrecision);
            }
        }
        return lName.toString();
    }
    
    /**
     * Compute the part of the detailed name of the article concerning the
     * article characteristics
     * 
     * @param pArticle
     *            the article
     * @return the part of the name containing the article characteristics
     */
    private static String computeDetailedName(Article pArticle) {
        
        StringBuffer lDetailedName = new StringBuffer();
        
        // Type
        lDetailedName.append(pArticle.getTypeArticle()).append(SEPARATOR_FIELD);
        
        // Designation (Cabinet or Rack)
        if (pArticle instanceof Cabinet) {
            Cabinet lCabinet = (Cabinet) pArticle;
            if (lCabinet.getDesignation() != null) {
                lDetailedName.append(lCabinet.getDesignation());
            }
            else {
                lDetailedName.append("");
            }
            lDetailedName.append(SEPARATOR_FIELD);
        }
        if (pArticle instanceof Rack && (!(pArticle instanceof Switch))) {
            Rack lRack = (Rack) pArticle;
            if (lRack.getDesignation() != null) {
                lDetailedName.append(lRack.getDesignation());
            }
            else {
                lDetailedName.append("");
            }
            lDetailedName.append(SEPARATOR_FIELD);
        }
        
        // Name (PC)
        if (pArticle instanceof PC) {
            PC lPC = (PC) pArticle;
            lDetailedName.append(lPC.getName()).append(SEPARATOR_FIELD);
        }
        
        // Airbus PN (except PC)
        if (!(pArticle instanceof PC)) {
            if (pArticle.getAirbusPN() != null) {
                lDetailedName.append(pArticle.getAirbusPN().getIdentifier());
            }
            else {
                lDetailedName.append("");
            }
            lDetailedName.append(SEPARATOR_FIELD);
        }
        
        // Airbus SN
        if (pArticle.getAirbusSN() != null) {
            lDetailedName.append(pArticle.getAirbusSN());
        }
        else {
            lDetailedName.append("");
        }
        lDetailedName.append(SEPARATOR_FIELD);
        
        // Manufacturer PN (except PC)
        if (!(pArticle instanceof PC)) {
            if (pArticle.getManufacturerPN() != null) {
                lDetailedName.append(pArticle.getManufacturerPN()
                        .getIdentifier());
            }
            else {
                lDetailedName.append("");
            }
            lDetailedName.append(SEPARATOR_FIELD);
        }
        
        // Manufacturer SN
        if (pArticle.getManufacturerSN() != null) {
            lDetailedName.append(pArticle.getManufacturerSN());
        }
        else {
            lDetailedName.append("");
        }
        
        return lDetailedName.toString();
    }
    
    /**
     * Compute the tool tip of the article for giving filled information about
     * it
     * (class, Airbus SN, Airbus PN, Manufacturer SN, Manufacturer PN, Type,
     * Designation for Cabinet and Rack, name for PC)
     * 
     * @param pArticle
     *            the article
     * @return the tool tip of the article
     */
    public static String computeToolTip(Article pArticle) {
        
        StringBuffer lToolTip = new StringBuffer();
        lToolTip.append(MessageBundle.getMessage("ClassTitle"))
                .append(": ")
                .append(MessageBundle.getMessage(pArticle.getClass()
                        .getSimpleName()));
        lToolTip.append("<br/>\n");
        
        if (pArticle.getAirbusSN() != null) {
            lToolTip.append(MessageBundle.getMessage("airbusSN")).append(": ")
                    .append(pArticle.getAirbusSN());
            lToolTip.append("<br/>\n");
        }
        if (pArticle.getAirbusPN() != null) {
            lToolTip.append(MessageBundle.getMessage("airbusPN")).append(": ")
                    .append(pArticle.getAirbusPN());
            lToolTip.append("<br/>\n");
        }
        if (pArticle.getManufacturerSN() != null) {
            lToolTip.append(MessageBundle.getMessage("manufacturerSN"))
                    .append(": ").append(pArticle.getManufacturerSN());
            lToolTip.append("<br/>\n");
        }
        if (pArticle.getManufacturerPN() != null) {
            lToolTip.append(MessageBundle.getMessage("manufacturerPN"))
                    .append(": ").append(pArticle.getManufacturerPN());
            lToolTip.append("<br/>\n");
        }
        if (pArticle.getTypeArticle() != null) {
            lToolTip.append(MessageBundle.getMessage("TypeArticle"))
                    .append(": ").append(pArticle.getTypeArticle());
            lToolTip.append("<br/>\n");
        }
        
        if (pArticle instanceof Rack) {
            Rack lRack = (Rack) pArticle;
            
            if (lRack.getDesignation() != null) {
                lToolTip.append(MessageBundle.getMessage("designation"))
                        .append(": ").append(lRack.getDesignation());
                lToolTip.append("<br/>\n");
            }
        }
        
        if (pArticle instanceof Cabinet) {
            Cabinet lCabinet = (Cabinet) pArticle;
            
            if (lCabinet.getDesignation() != null) {
                lToolTip.append(MessageBundle.getMessage("designation"))
                        .append(": ").append(lCabinet.getDesignation());
                lToolTip.append("<br/>\n");
            }
        }
        
        if (pArticle instanceof PC) {
            PC lPC = (PC) pArticle;
            
            if (lPC.getName() != null) {
                lToolTip.append(MessageBundle.getMessage("name")).append(": ")
                        .append(lPC.getName());
                lToolTip.append("<br/>\n");
            }
        }
        
        return lToolTip.toString();
    }
    
    /** Article id */
    @Id
    @GeneratedValue
    protected Long id;
    
    /** Set of articles contained by this */
    @OneToMany(mappedBy = "containerArticle")
    protected List<Article> containedArticles = new ArrayList<Article>();
    
    /** The article containing this */
    @ManyToOne
    @JoinColumn(name = "USEDBY_ARTICLE_ID")
    protected Article containerArticle;
    
    /** The installation containing this */
    @ManyToOne
    @JoinColumn(name = "USEDBY_INSTALLATION_ID")
    protected Installation containerInstallation;
    
    /** Article state */
    @Column
    @Enumerated(EnumType.STRING)
    protected ArticleState state = ArticleState.Operational;
    
    /** Article state */
    @Column
    @Enumerated(EnumType.STRING)
    protected UseState useState = UseState.InStock;
    
    /** Article CMS Code */
    @Column(name = CMSCODE_COLUMN_NAME)
    protected String cmsCode;
    
    /** Article Airbus PN */
    @ManyToOne(fetch = FetchType.LAZY)
    protected AirbusPN airbusPN;
    
    /** Article manufacturer PN */
    @ManyToOne(fetch = FetchType.LAZY)
    protected ManufacturerPN manufacturerPN;
    
    /** Article history */
    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.ALL },
            orphanRemoval = true)
    protected History history = new History();
    
    /** Article type */
    @ManyToOne(fetch = FetchType.LAZY)
    protected TypeArticle typeArticle;
    
    /** Article acquisition date */
    @Temporal(TemporalType.TIMESTAMP)
    protected Date acquisitionDate = new Date();
    
    /** Article Airbus SN */
    @Column(name = AIRBUSSN_COLUMN_NAME, unique = true)
    protected String airbusSN;
    
    /** Article manufacturer SN */
    @Column(name = MANUFACTURERSN_COLUMN_NAME)
    protected String manufacturerSN;
    
    @Version
    private Long version;
    
    /** The location of this */
    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY)
    protected LocationForArticle locationOrm;
    
    /** The tool link relation */
    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY)
    protected Contains_Tool_Article containerOrmTool;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    /** The list of Software products installed on the article */
    @ManyToMany(mappedBy = "equipments", fetch = FetchType.EAGER)
    @OrderBy("operatingSystem DESC, name ASC, distribution ASC, kernel ASC")
    protected List<Software> softwares = new ArrayList<Software>();
    
    @OrderBy("date DESC")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DatedComment> datedComments = new ArrayList<DatedComment>();
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Document> documents = new ArrayList<Document>();
    
    @Override
    public int compareTo(Article arg0) {
        
        String first =
                (getAirbusSN() != null) ? getAirbusSN() : getManufacturerSN();
        String second =
                (arg0.getAirbusSN() != null) ? arg0.getAirbusSN() : arg0
                        .getManufacturerSN();
        
        return first.compareTo(second);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Article other = (Article) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "["
                + getClass().getSimpleName()
                + " "
                + (airbusSN != null ? "airbusSN=" + airbusSN + ", " : "")
                + (manufacturerSN != null ? "manufacturerSN=" + manufacturerSN
                        + ", " : "") + (id != null ? "id=" + id : "") + "]";
    }
    
    /**
     * Initialize an object of a class inheriting from TypeArticle class
     * according to the current implementation of Article class
     * 
     * @return an empty TypeArticle implementation object
     */
    public abstract TypeArticle createTypeArticle();
    
    /**
     * Method automatically called when an Article is to be stored in database
     */
    public void validate() {
        
        // P/N AIRBUS Manually filled – Empty if CMS code field is filled
        // CMS Code Manually filled – Empty if P/N Airbus field is filled
        // 02/03/2011 Les deux champs peuvent être vide !
        if (!StringUtil.isEmptyOrNull(cmsCode) && airbusPN != null) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(Constants.PNAIRBUS1_CMSCODE_ONE_FIELD_MUST_BE_FILLED));
        }
        // Comportement par défaut des articles pour leur identification
        
        // Un SN Airbus ou Fabricant est obligatoire.
        if (StringUtil.isEmptyOrNull(airbusSN)
                && StringUtil.isEmptyOrNull(manufacturerSN)) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.ASN_OR_MSN_MANDATORY));
        }
    }
    
    /**
     * Method automatically called before updating an Article in database
     */
    @PreUpdate
    public void onUpdate() {
        lastUpdate = new Date();
        validate();
    }
    
    /**
     * Method automatically called before creating an Article in database
     */
    @PrePersist
    public void onCreate() {
        creationDate = lastUpdate = new Date();
        validate();
    }
    
    /**
     * @return the id
     */
    @Override
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
     * @return the computed name
     */
    @Override
    public String getName() {
        return (airbusSN == null) ? manufacturerSN : airbusSN;
    }
    
    @Override
    public Location getLocation() {
        
        LocationManager lLocationManager = new LocationManager(this);
        return lLocationManager.getLocation();
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
    
    /**
     * @return the list of contained articles
     */
    public List<? extends Article> getChildren() {
        List<Article> result = new ArrayList<Article>();
        if (getContainedArticles() != null) {
            result.addAll(getContainedArticles());
        }
        
        return result;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = new ArrayList<ContainedItem>();
        
        lContainedItems.addAll(containedArticles);
        
        return lContainedItems;
    }
    
    @Override
    public List<ContainedItem> getContainedItemsInheriting() {
        
        return ContainerManager.getInheritingContainedItem(getContainedItems());
    }
    
    /**
     * @return the airbusSN
     */
    public String getAirbusSN() {
        return airbusSN;
    }
    
    /**
     * @param airbusSN
     *            the airbusSN to set
     */
    public void setAirbusSN(String airbusSN) {
        
        if (airbusSN != null && (!"".equals(airbusSN.trim()))) {
            this.airbusSN = airbusSN.trim();
        }
        else {
            this.airbusSN = null;
        }
    }
    
    /**
     * @return the manufacturerPN
     */
    public ManufacturerPN getManufacturerPN() {
        return manufacturerPN;
    }
    
    /**
     * @param manufacturerPN
     *            the manufacturerPN to set
     */
    public void setManufacturerPN(ManufacturerPN manufacturerPN) {
        
        this.manufacturerPN = manufacturerPN;
    }
    
    /**
     * @return the manufacturerSN
     */
    public String getManufacturerSN() {
        return manufacturerSN;
    }
    
    /**
     * @param manufacturerSN
     *            the manufacturerSN to set
     */
    public void setManufacturerSN(String manufacturerSN) {
        
        if (manufacturerSN != null && !"".equals(manufacturerSN.trim())) {
            this.manufacturerSN = manufacturerSN.trim();
        }
        else {
            this.manufacturerSN = null;
        }
    }
    
    /**
     * @return the acquisition date
     */
    public Date getAcquisitionDate() {
        return acquisitionDate;
    }
    
    /**
     * @param acquisitionDate
     *            the acquisition date
     */
    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
    
    /**
     * @return the state
     */
    public ArticleState getState() {
        return state;
    }
    
    /**
     * @return true if the use state is archived, false otherwise
     */
    public boolean isArchived() {
        return useState == UseState.Archived;
    }
    
    /**
     * @param state
     *            the state to set
     */
    public void setState(ArticleState state) {
        this.state = state;
    }
    
    /**
     * @return the useState
     */
    public UseState getUseState() {
        return useState;
    }
    
    /**
     * @param pUseState
     *            the useState to set
     */
    public void setUseState(UseState pUseState) {
        useState = pUseState;
    }
    
    /**
     * @return the cmsCode
     */
    public String getCmsCode() {
        return cmsCode;
    }
    
    /**
     * @param cmsCode
     *            the cmsCode to set
     */
    public void setCmsCode(String cmsCode) {
        this.cmsCode = cmsCode;
    }
    
    /**
     * @return the history
     */
    public History getHistory() {
        return history;
    }
    
    /**
     * @param history
     *            the history to set
     */
    public void setHistory(History history) {
        this.history = history;
    }
    
    /**
     * @return the typeArticle
     */
    public TypeArticle getTypeArticle() {
        return typeArticle;
    }
    
    /**
     * @param typeArticle
     *            the typeArticle to set
     */
    public void setTypeArticle(TypeArticle typeArticle) {
        this.typeArticle = typeArticle;
    }
    
    /**
     * @return the airbusPN
     */
    public AirbusPN getAirbusPN() {
        return airbusPN;
    }
    
    /**
     * @param airbusPn
     *            the airbusPN to set
     */
    public void setAirbusPN(AirbusPN airbusPn) {
        
        airbusPN = airbusPn;
    }
    
    /**
     * @return the locationOrm
     */
    public LocationForArticle getLocationOrm() {
        return locationOrm;
    }
    
    /**
     * @param pLocationOrm
     *            the locationOrm to set
     */
    public void setLocationOrm(LocationForArticle pLocationOrm) {
        locationOrm = pLocationOrm;
    }
    
    /**
     * @return the containerInstallation
     */
    public Installation getContainerInstallation() {
        return containerInstallation;
    }
    
    /**
     * @param usedByInstallation
     *            the containerInstallation to set
     */
    public void setContainerInstallation(Installation usedByInstallation) {
        containerInstallation = usedByInstallation;
    }
    
    /**
     * @return the containedArticles
     */
    public List<Article> getContainedArticles() {
        return containedArticles;
    }
    
    /**
     * @param uses
     *            the containedArticles to set
     */
    public void setContainedArticles(List<Article> uses) {
        containedArticles = uses;
    }
    
    /**
     * @return the containerArticle
     */
    public Article getContainerArticle() {
        return containerArticle;
    }
    
    /**
     * @param usedBy
     *            the containerArticle to set
     */
    public void setContainerArticle(Article usedBy) {
        containerArticle = usedBy;
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
     * @return the containerOrmTool
     */
    public Contains_Tool_Article getContainerOrmTool() {
        return containerOrmTool;
    }
    
    /**
     * @param pContainerTool
     *            the containerOrmTool to set
     */
    public void setContainerOrmTool(Contains_Tool_Article pContainerTool) {
        containerOrmTool = pContainerTool;
    }
    
    public Installation getMasterInstallation(){
    	Container lMasterContainer = getMasterContainer();
    	if(lMasterContainer != null && 
    			lMasterContainer.getType().equals(ContainerType.Installation)){
    		return (Installation) lMasterContainer.getContainerItem();
    	}
    	else{
    		return null;
    	}
    }
    
    /**
     * @return the datedComments
     */
    public List<DatedComment> getDatedComments() {
        return datedComments;
    }
    
    /**
     * @return the documents list
     */
    public List<Document> getDocuments() {
        return documents;
    }
}
