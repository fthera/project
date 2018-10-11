/*
 * ------------------------------------------------------------------------
 * Class : Tool
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleClass;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerItem;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;

/**
 * Entity implementation class for Entity: Tool
 */
@Entity
@Table(name = Tool.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Tool.ALL_TOOLS_QUERY, query = "SELECT t FROM Tool t"
                + " LEFT JOIN FETCH t.locationOrm"
                + " LEFT JOIN FETCH t.containerInstallation"
                + " ORDER BY t.name"),
        @NamedQuery(
                name = Tool.TOOLS_BY_ANY_FIELD_QUERY,
                query = "SELECT t FROM Tool t WHERE t.name LIKE :criterion OR t.designation LIKE :criterion OR t.personInCharge LIKE :criterion OR t.generalComment.message LIKE :criterion ORDER BY t.name"),
        @NamedQuery(name = Tool.TOOL_BY_NAME_QUERY,
                query = "SELECT t FROM Tool t WHERE t.name = :name"),
        @NamedQuery(
                name = Tool.TOOLS_BY_NAME_QUERY,
                query = "SELECT t FROM Tool t WHERE t.name LIKE :name ORDER BY t.name"),
        @NamedQuery(name = Tool.EXIST_TOOL_NAME_QUERY,
                query = "SELECT t.name FROM Tool t WHERE t.name = :name") })
public class Tool implements Item, ContainerItem, ContainedItem, LocatedItem,
        Serializable, Comparable<Tool> {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "tool";
    /** The designation column name */
    public static final String DESIGNATION_COLUMN_NAME = "DESIGNATION";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    /** The person in charge column name */
    public static final String PERSONINCHARGE_COLUMN_NAME = "PERSONINCHARGE";
    
    /**
     * Name of the named query selecting all Tools
     */
    public static final String ALL_TOOLS_QUERY = "allTools";
    
    /**
     * Name of the named query selecting tools with at least one of the
     * following fields satisfying (use of "LIKE") the provided criterion: <br>
     * - name, - designation, - person in charge, - general comment
     * This query is parameterized:<br>
     * <b>criterion</b> the criterion to search
     */
    public static final String TOOLS_BY_ANY_FIELD_QUERY = "toolsByAnyField";
    
    /**
     * Name of the named query selecting tools having a name like the provided
     * name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String TOOLS_BY_NAME_QUERY = "toolsByName";
    
    /**
     * Name of the named query selecting tool with the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String TOOL_BY_NAME_QUERY = "toolByName";
    
    /**
     * Name of the named query selecting the tool name of tools with the
     * provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String EXIST_TOOL_NAME_QUERY = "existToolName";
    
    /**
     * List of articles classes able to be a tool component
     */
    public static final ArticleClass[] COMPONENTS_CLASSES = {
            ArticleClass.Board, ArticleClass.PC, ArticleClass.Various,
            ArticleClass.Rack, ArticleClass.Switch };
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private String designation;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Temporal(TemporalType.DATE)
    private Date loanDate;
    
    @Temporal(TemporalType.DATE)
    private Date loanDueDate;
    
    @Column
    private String personInCharge;
    
    @OneToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "COMMENT_ID")
    private Comment generalComment;
    
    @OneToMany(mappedBy = "tool", fetch = FetchType.EAGER,
            cascade = CascadeType.PERSIST)
    private List<Contains_Tool_Article> containedOrmArticles =
            new ArrayList<Contains_Tool_Article>();
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST,
            CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "INSTALLATION_ID")
    private Installation containerInstallation;
    
    @OneToOne(mappedBy = "tool", fetch = FetchType.LAZY)
    private LocationForTool locationOrm;
    
    @Version
    private Long version;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
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
        creationDate = lastUpdate = new Date();
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
        Tool other = (Tool) obj;
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
        return name;
    }
    
    @Override
    public int compareTo(Tool arg0) {
        
        return name.compareTo(arg0.name);
    }
    
    @Override
    public int hashCode() {
        final int lPrime = 31;
        int lResult = 1;
        lResult = lPrime * lResult + ((id == null) ? 0 : id.hashCode());
        return lResult;
    }
    
    /**
     * Return the articles contained by this tool
     * 
     * @return a list of articles
     */
    public List<Article> getChildren() {
        
        List<Article> lResult = new ArrayList<Article>();
        
        for (Contains_Tool_Article lRelation : getContainedOrmArticles()) {
            lResult.add(lRelation.getArticle());
        }
        Collections.sort(lResult);
        
        return lResult;
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
    
    /**
     * @return the name
     */
    @Override
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
     * @return the designation
     */
    public String getDesignation() {
        return designation;
    }
    
    /**
     * @param pDesignation
     *            the designation to set
     */
    public void setDesignation(String pDesignation) {
        designation = pDesignation;
    }
    
    /**
     * @return the personInCharge
     */
    public String getPersonInCharge() {
        return personInCharge;
    }
    
    /**
     * @param pPersonInCharge
     *            the personInCharge to set
     */
    public void setPersonInCharge(String pPersonInCharge) {
        personInCharge = pPersonInCharge;
    }
    
    /**
     * @return the id
     */
    @Override
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
     * @return the containedOrmArticles
     */
    public List<Contains_Tool_Article> getContainedOrmArticles() {
        return containedOrmArticles;
    }
    
    /**
     * @param pContains
     *            the containedOrmArticles to set
     */
    public void setContainedOrmArticles(List<Contains_Tool_Article> pContains) {
        containedOrmArticles = pContains;
    }
    
    /**
     * @return the containerInstallation
     */
    public Installation getContainerInstallation() {
        return containerInstallation;
    }
    
    /**
     * @param pInstallation
     *            the containerInstallation to set
     */
    public void setContainerInstallation(Installation pInstallation) {
        containerInstallation = pInstallation;
    }
    
    /**
     * @return the locationOrm
     */
    public LocationForTool getLocationOrm() {
        return locationOrm;
    }
    
    /**
     * @param pLocationOrm
     *            the locationOrm to set
     */
    public void setLocationOrm(LocationForTool pLocationOrm) {
        locationOrm = pLocationOrm;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = new ArrayList<ContainedItem>();
        
        for (Contains_Tool_Article lContainedOrm : containedOrmArticles) {
            lContainedItems.add(lContainedOrm.getArticle());
        }
        
        return lContainedItems;
    }
    
    @Override
    public List<ContainedItem> getContainedItemsInheriting() {
        
        return ContainerManager.getInheritingContainedItem(getContainedItems());
    }
    
    /**
     * @return the loanDate
     */
    public Date getLoanDate() {
        return loanDate;
    }
    
    /**
     * @param pLoanDate
     *            the loanDate to set
     */
    public void setLoanDate(Date pLoanDate) {
        loanDate = pLoanDate;
    }
    
    /**
     * @return the loanDueDate
     */
    public Date getLoanDueDate() {
        return loanDueDate;
    }
    
    /**
     * @param pLoanDueDate
     *            the loanDueDate to set
     */
    public void setLoanDueDate(Date pLoanDueDate) {
        loanDueDate = pLoanDueDate;
    }
    
    /**
     * @return the generalComment
     */
    public Comment getGeneralComment() {
        return generalComment;
    }
    
    /**
     * @param pGeneralComment
     *            the generalComment to set
     */
    public void setGeneralComment(Comment pGeneralComment) {
        generalComment = pGeneralComment;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Tool;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Tool;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Tool;
    }
    
}
