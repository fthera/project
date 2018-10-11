/*
 * ------------------------------------------------------------------------
 * Class : Installation
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainerItem;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;

/**
 * Entity implementation class for Entity: Installation
 */
@Entity
@Table(name = Installation.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = "allInstallation",
                query = "SELECT i FROM Installation i"
                        + " LEFT JOIN FETCH i.aircraftProgram"
                        + " LEFT JOIN FETCH i.personInCharge"
                        + " LEFT JOIN FETCH i.locationPlace ORDER BY i.name"),
        @NamedQuery(
                name = Installation.INSTALLATIONS_BY_ANY_FIELD_QUERY,
                query = "SELECT i FROM Installation i LEFT JOIN i.personInCharge u"
                        + " WHERE i.name LIKE :criterion OR i.comments LIKE :criterion"
                        + " OR i.aircraftProgram.defaultValue LIKE :criterion"
                        + " OR i.businessSiglum LIKE :criterion"
                        + " OR (u.lastname LIKE :criterion OR u.firstname LIKE :criterion"
                        + " OR u.login LIKE :criterion) ORDER BY i.name"),
        @NamedQuery(
                name = Installation.BY_PERSONINCHARGE_QUERY,
                query = "SELECT i FROM Installation i WHERE i.personInCharge = :personInCharge"),
        @NamedQuery(name = "InstallationByName",
                query = "SELECT i FROM Installation i WHERE i.name = :name"),
        @NamedQuery(name = Installation.INSTALLATIONS_BY_NAME_QUERY,
                query = "SELECT i FROM Installation i WHERE i.name LIKE :name"),
        @NamedQuery(
                name = "ExistInstallationName",
                query = "SELECT i.name FROM Installation i WHERE i.name = :name"),
        @NamedQuery(
                name = Installation.ALL_INSTALLATION_BUSINESS_SIGLUMS_QUERY,
                query = "SELECT DISTINCT i.businessSiglum FROM Installation i WHERE NOT i.businessSiglum = '' ORDER BY i.businessSiglum") })
public class Installation implements Item, ContainerItem, LocatedItem,
        Serializable, Comparable<Installation> {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "installation";
    /** The comments column name */
    public static final String COMMENTS_COLUMN_NAME = "COMMENTS";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    /** The business siglum column name */
    public static final String BUSINESSSIGLUM_COLUMN_NAME = "BUSINESSSIGLUM";
    /** The user column name */    
    public static final String USER_COLUMN_NAME = "USER";
    
    /**
     * Name of the named query selecting installations with at least one of the
     * following fields satisfying (use of "LIKE") the provided criterion: <br>
     * - name, - comments, - aircraft program, - person in charge
     * This query is parameterized:<br>
     * <b>criterion</b> the criterion to search
     */
    public static final String INSTALLATIONS_BY_ANY_FIELD_QUERY =
            "installationsByAnyField";
    
    /**
     * Name of the named query selecting installations with the provided name. <br>
     * This query is parameterized: <br>
     * <b>name</b> the name to search
     */
    public static final String INSTALLATIONS_BY_NAME_QUERY =
            "installationsByName";
    
    /**
     * Name of the named query counting the number of installations with the
     * provided user in charge. <br>
     * This query is parameterized: <br>
     * <b>personInCharge</b> the User in charge to search
     */
    public static final String BY_PERSONINCHARGE_QUERY =
            "installationByPersonIncharge";
    
    /**
     * Name of the named query selecting all business siglums from installations
     */
    public static final String ALL_INSTALLATION_BUSINESS_SIGLUMS_QUERY =
            "installationsBusinessSiglums";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private String comments;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AIRCRAFTPROGRAM_ID")
    private AircraftProgram aircraftProgram;
    
    @Column
    private String businessSiglum;
    
    @Column
    private String user;
    
    @Temporal(TemporalType.DATE)
    private Date startingDate;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User personInCharge;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(nullable = false)
    private History history = new History();
    
    @OneToMany(mappedBy = "installation", fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST)
    private List<Contains_Inst_Cabinet> containedOrmCabinets =
            new ArrayList<Contains_Inst_Cabinet>();
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST,
            CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "LABO_PLACE_ID")
    private Place locationPlace;
    
    @OneToMany(mappedBy = "containerInstallation")
    private List<Article> containedArticles = new ArrayList<Article>();
    
    @OneToMany(mappedBy = "containerInstallation")
    private List<Tool> containedTools = new ArrayList<Tool>();
    
    @Version
    private Long version;
    
    /**
     * Default constructor
     */
    public Installation() {
        super();
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
        Installation other = (Installation) obj;
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
    public int compareTo(Installation arg0) {
        
        return name.compareTo(arg0.getName());
    }
    
    /**
     * @return the aircraftProgram
     */
    public AircraftProgram getAircraftProgram() {
        return aircraftProgram;
    }
    
    /**
     * @param aircraftProgram
     *            the aircraftProgram to set
     */
    public void setAircraftProgram(AircraftProgram aircraftProgram) {
        this.aircraftProgram = aircraftProgram;
    }
    
    /**
     * @return the startingDate
     */
    public Date getStartingDate() {
        return startingDate;
    }
    
    /**
     * @param startingDate
     *            the startingDate to set
     */
    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }
    
    /**
     * @return the personInCharge
     */
    public User getPersonInCharge() {
        return personInCharge;
    }
    
    /**
     * @param personInCharge
     *            the personInCharge to set
     */
    public void setPersonInCharge(User personInCharge) {
        this.personInCharge = personInCharge;
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
     * @return the name
     */
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
     * @return the containedOrmCabinets
     */
    public List<Contains_Inst_Cabinet> getContainedOrmCabinets() {
        return containedOrmCabinets;
    }
    
    /**
     * @param contains
     *            the containedOrmCabinets to set
     */
    public void setContainedOrmCabinets(List<Contains_Inst_Cabinet> contains) {
        containedOrmCabinets = contains;
    }
    
    /**
     * @return the locationPlace
     */
    public Place getLocationPlace() {
        return locationPlace;
    }
    
    /**
     * @param integrationPlace
     *            the locationPlace to set
     */
    public void setLocationPlace(Place integrationPlace) {
        locationPlace = integrationPlace;
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
     * @return the list of articles directly contained by the installation
     */
    public List<Article> getChildren() {
        List<Article> result = new ArrayList<Article>();
        if (getContainedArticles() != null) {
            result.addAll(getContainedArticles());
        }
        for (Contains_Inst_Cabinet relation : getContainedOrmCabinets()) {
            result.add(relation.getCabinet());
        }
        Collections.sort(result);
        return result;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = new ArrayList<ContainedItem>();
        
        lContainedItems.addAll(containedArticles);
        
        lContainedItems.addAll(containedTools);
        
        for (Contains_Inst_Cabinet lContainerOrm : containedOrmCabinets) {
            lContainedItems.add(lContainerOrm.getCabinet());
        }
        
        return lContainedItems;
    }
    
    @Override
    public List<ContainedItem> getContainedItemsInheriting() {
        
        return ContainerManager.getInheritingContainedItem(getContainedItems());
    }
    
    @Override
    public Location getLocation() {
        LocationManager manager = new LocationManager(this);
        return manager.getLocation();
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Installation;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Installation;
    }
    
    /**
     * @return the containedTools
     */
    public List<Tool> getContainedTools() {
        return containedTools;
    }
    
    /**
     * @param pTools
     *            the containedTools to set
     */
    public void setContainedTools(List<Tool> pTools) {
        containedTools = pTools;
    }
    
    /**
     * @return the businessSiglum
     */
    public String getBusinessSiglum() {
        return businessSiglum;
    }
    
    /**
     * @param pBusinessSiglum
     *            the businessSiglum to set
     */
    public void setBusinessSiglum(String pBusinessSiglum) {
        businessSiglum = pBusinessSiglum;
    }
    
    /**
     * 
     * @return the user
     */
	public String getUser() {
		return user;
	}
	/**
	 * 
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
    
    
}
