/*
 * ------------------------------------------------------------------------
 * Class : LocationBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Bean used to manage location and container entities
 */
@Stateless
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class LocationBean implements Serializable {
    
    private static final long serialVersionUID = -6816228922929703989L;
    
    private static Logger log = Logger.getLogger(LocationBean.class.getName());
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @PersistenceContext(name = "LocationService")
    private EntityManager em;
    
    /**
     * Create the building in database
     * 
     * @param batiment
     *            the building to create
     * @return the building
     */
    public Building createBuilding(Building batiment) {
        em.persist(batiment);
        return batiment;
    }
    
    /**
     * Create the external entity in database
     * 
     * @param pExternalEntity
     *            the external entity
     * @return the external entity
     */
    public ExternalEntity createExternalEntity(ExternalEntity pExternalEntity) {
        em.persist(pExternalEntity);
        return pExternalEntity;
    }
    
    /**
     * Create the installation in database
     * 
     * @param installation
     *            the installation
     * @param pLocation
     *            the installation location
     * @return the installation
     */
    public Installation createInstallation(Installation installation,
            Location pLocation) {
        em.persist(installation);
        
        LocationManager lManager = new LocationManager(installation);
        lManager.moveTo(pLocation, locationManagerBean);
        return installation;
    }
    
    /**
     * Create the place into database
     * 
     * @param place
     *            the place to create
     * @param building
     *            the building containing the place
     * @return the place
     */
    public Place createPlace(Place place, Building building) {
        em.persist(place);
        addPlace(place, building);
        return place;
    }
    
    /**
     * Merge the provided entity in database
     * 
     * @param entity
     *            the entity to merge
     * @return the merged entity
     */
    public <T> T merge(T entity) {
        try {
            return em.merge(entity);
        }
        catch (OptimisticLockException e) {
            log.throwing(this.getClass().getName(), "merge", e);
            throw new ChangeCollisionException(e);
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            
            throw new ValidationException(e);
        }
    }
    
    /**
     * Remove the building from database
     * 
     * @param selectedBuilding
     *            the building
     */
    public void removeBuilding(Building selectedBuilding) {
        selectedBuilding = em.merge(selectedBuilding);
        if (selectedBuilding.getPlaces().size() > 0) {
            throw new ValidationException(MessageBundle.getMessageResource(
                    Constants.BUILDING_HAS_PLACES,
                    new Object[] { selectedBuilding.getName() }));
        }
        
        em.remove(selectedBuilding);
    }
    
    /**
     * Remove the external entity from the database
     * 
     * @param pExternalEntity
     *            the external entity
     */
    public void removeExternalEntity(ExternalEntity pExternalEntity) {
        
        pExternalEntity = merge(pExternalEntity);
        
        int numberArticle = pExternalEntity.getLocatedOrmArticles().size();
        if (numberArticle > 0) {
            Object[] args = { pExternalEntity.getName(), numberArticle };
            String msg =
                    MessageBundle.getMessageResource(
                            Constants.EXTERNAL_ENTITY_HAS_ARTICLES, args);
            throw new ValidationException(msg);
        }
        
        int lNbTools = pExternalEntity.getLocatedOrmTools().size();
        if (lNbTools > 0) {
            Object[] lArgs = { pExternalEntity.getName(), lNbTools };
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.EXTERNAL_ENTITY_HAS_TOOLS, lArgs);
            throw new ValidationException(lMsg);
        }
        
        em.remove(pExternalEntity);
    }
    
    /**
     * Remove the installation from database
     * 
     * @param id
     *            the installation id
     */
    public void removeInstallation(Long id) {
        Installation installation = findInstallationById(id);
        em.remove(installation);
    }
    
    /**
     * Remove the place from the building and from database
     * 
     * @param place
     *            the place to remove
     * @param building
     *            the building containing the place
     */
    public void removePlace(Place place, Building building) {
        
        if (place == null) {
            throw new IllegalArgumentException(
                    MessageBundle.getMessage(Constants.PLACE_IS_NULL));
        }
        if (building == null) {
            throw new IllegalArgumentException(
                    MessageBundle.getMessage(Constants.BUILDING_IS_NULL));
        }
        
        place = em.merge(place);
        if (place.getLocatedOrmArticles().size() > 0) {
            throw new ValidationException(MessageBundle.getMessageResource(
                    Constants.PLACE_HAS_ARTICLES,
                    new Object[] { place.getName() }));
        }
        
        if (place.getLocationForTools().size() > 0) {
            throw new ValidationException(
                    MessageBundle.getMessageResource(Constants.PLACE_HAS_TOOLS,
                            new Object[] { place.getName() }));
        }
        
        if (place.getLocatedInstallations().size() > 0) {
            throw new ValidationException(MessageBundle.getMessageResource(
                    Constants.LABO_HAS_TESTBENCHES,
                    new Object[] { place.getName() }));
        }
        
        if (place.getBuilding() == null) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.PLACE_ERROR));
        }
        
        if (!place.getBuilding().equals(building)) {
            throw new ValidationException(MessageBundle.getMessageResource(
                    Constants.BUILDING_MISTMATCH,
                    new Object[] { building.getName(), place.getBuilding() }));
        }
        
        building.getPlaces().remove(place);
        building = em.merge(building);
        
        place.setBuilding(null);
        em.remove(place);
    }
    
    /**
     * Add the place into the building
     * 
     * @param place
     *            the place
     * @param building
     *            the building containing the place
     * @return the merged place
     */
    private Place addPlace(Place place, Building building) {
        if (place == null || place.getBuilding() != null) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.PLACE_ERROR));
        }
        
        if (building == null) {
            throw new IllegalArgumentException(
                    MessageBundle.getMessage(Constants.BUILDING_IS_NULL));
        }
        
        try {
            place.setBuilding(building);
            building.getPlaces().add(place);
            
            place = em.merge(place);
            building = em.merge(building);
        }
        catch (OptimisticLockException e) {
            throw new ChangeCollisionException(e);
        }
        // lazy loading
        building.getPlaces().size();
        return place;
    }
    
    /**
     * @return the list of all buildings in database
     */
    public List<Building> findAllBuilding() {
        TypedQuery<Building> query =
                em.createNamedQuery("allBuilding", Building.class);
        return query.getResultList();
    }
    
    /**
     * @return the list of all external entities in database
     */
    public List<ExternalEntity> findAllExternalEntities() {
        
        TypedQuery<ExternalEntity> query =
                em.createNamedQuery(ExternalEntity.ALL_EXTERNAL_ENTITIES_QUERY,
                        ExternalEntity.class);
        List<ExternalEntity> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException e) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    /**
     * Search the external entities having the provided name
     * 
     * @param pName
     *            the name to search
     * @return the list of found external entities
     */
    public List<ExternalEntity> findAllExternalEntitiesByName(String pName) {
        
        List<ExternalEntity> lExternalEntities =
                new ArrayList<ExternalEntity>();
        
        if (pName == null) {
            return lExternalEntities;
        }
        
        String lNameSql = StringUtil.parseToSQLRegex(pName);
        
        TypedQuery<ExternalEntity> lQuery =
                em.createNamedQuery(
                        ExternalEntity.EXTERNAL_ENTITIES_BY_NAME_QUERY,
                        ExternalEntity.class);
        lQuery.setParameter("name", lNameSql);
        return lQuery.getResultList();
    }
    
    /**
     * Search the external entities having the provided name and type
     * 
     * @param pName
     *            the name to search
     * @param pType
     *            the type to search
     * @return the list of found external entities
     */
    public List<ExternalEntity> findAllExternalEntitiesByNameAndType(
            String pName, ExternalEntityType pType) {
        
        List<ExternalEntity> lExternalEntities =
                new ArrayList<ExternalEntity>();
        
        if (pName == null || pType == null) {
            return lExternalEntities;
        }
        
        String lNameSql = StringUtil.parseToSQLRegex(pName);
        
        TypedQuery<ExternalEntity> lQuery =
                em.createNamedQuery(
                        ExternalEntity.EXTERNAL_ENTITIES_BY_NAME_AND_TYPE_QUERY,
                        ExternalEntity.class);
        lQuery.setParameter("name", lNameSql);
        lQuery.setParameter("type", pType);
        return lQuery.getResultList();
    }
    
    /**
     * Search the external entities having the provided type
     * 
     * @param pType
     *            the type to search
     * @return the list of found external entities
     */
    public List<ExternalEntity> findAllExternalEntitiesByType(
            ExternalEntityType pType) {
        
        List<ExternalEntity> lExternalEntities =
                new ArrayList<ExternalEntity>();
        
        if (pType == null) {
            return lExternalEntities;
        }
        
        TypedQuery<ExternalEntity> lQuery =
                em.createNamedQuery(
                        ExternalEntity.EXTERNAL_ENTITIES_BY_TYPE_QUERY,
                        ExternalEntity.class);
        lQuery.setParameter("type", pType);
        return lQuery.getResultList();
    }
    
    /**
     * @return the list of all installations from database
     */
    public List<Installation> findAllInstallation() {
        TypedQuery<Installation> query =
                em.createNamedQuery("allInstallation", Installation.class);
        List<Installation> installations = query.getResultList();
        return installations;
    }
    
    /**
     * @return the list of existing Business siglums in database
     */
    public List<String> findAllInstallationBusinessSiglums() {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(
                        Installation.ALL_INSTALLATION_BUSINESS_SIGLUMS_QUERY,
                        String.class);
        return lQuery.getResultList();
    }
    
    /**
     * Find installations with at least one text field corresponding to the
     * provided criterion
     * 
     * @param pCriterion
     *            the criterion to search (can contain '*' as wildcard)
     * @return the list of found installations
     */
    public List<Installation> findAllInstallationsByAnyField(String pCriterion) {
        
        TypedQuery<Installation> lQuery =
                em.createNamedQuery(
                        Installation.INSTALLATIONS_BY_ANY_FIELD_QUERY,
                        Installation.class);
        
        String lCriterionSql = StringUtil.parseToSQLRegex(pCriterion);
        
        lQuery.setParameter("criterion", lCriterionSql);
        
        List<Installation> lInstallations = lQuery.getResultList();
        return lInstallations;
    }
    
    /**
     * Search the installations having the provided name
     * 
     * @param pName
     *            the name to search
     * @return the list of found installations
     */
    public List<Installation> findAllInstallationsByName(String pName) {
        
        List<Installation> lInstallations = new ArrayList<Installation>();
        
        if (pName == null) {
            return lInstallations;
        }
        
        String lNameSql = StringUtil.parseToSQLRegex(pName);
        
        TypedQuery<Installation> lQuery =
                em.createNamedQuery(Installation.INSTALLATIONS_BY_NAME_QUERY,
                        Installation.class);
        lQuery.setParameter("name", lNameSql);
        return lQuery.getResultList();
    }
    
    /**
     * @return a list of all places in database
     */
    public List<Place> findAllPlace() {
        TypedQuery<Place> query = em.createNamedQuery("AllPlace", Place.class);
        List<Place> places;
        try {
            places = query.getResultList();
        }
        catch (NoResultException e) {
            
            places = Collections.emptyList();
        }
        return places;
    }
    
    /**
     * Search the places having the provided name
     * 
     * @param pName
     *            the name to search
     * @return the list of found places
     */
    public List<Place> findAllPlacesByName(String pName) {
        
        List<Place> lPlaces = new ArrayList<Place>();
        
        if (pName == null) {
            return lPlaces;
        }
        
        String lNameSql = StringUtil.parseToSQLRegex(pName);
        
        TypedQuery<Place> lQuery =
                em.createNamedQuery(Place.PLACES_BY_NAME_QUERY, Place.class);
        lQuery.setParameter("name", lNameSql);
        return lQuery.getResultList();
    }
    
    /**
     * Search the building by its name
     * 
     * @param name
     *            the building name
     * @return the found building, else null
     */
    public Building findBuildingByName(String name) {
        Query query = em.createNamedQuery("BuildingByName");
        query.setParameter("name", name);
        Building building;
        try {
            building = (Building) query.getSingleResult();
        }
        catch (NoResultException e) {
            building = null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_WARN, e.getCause().getMessage(),
                    e.getMessage()));
        }
        return building;
    }
    
    /**
     * Search the external entity id in database
     * 
     * @param id
     *            the external entity id
     * @return the found external entity, else null
     */
    public ExternalEntity findExternalEntityById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(ExternalEntity.class, id);
    }
    
    /**
     * Search an external entity by its name
     * 
     * @param name
     *            the searched name
     * @return the found external entity, else null
     */
    public ExternalEntity findExternalEntityByName(String name) {
        Query query =
                em.createNamedQuery(ExternalEntity.EXTERNAL_ENTITY_BY_NAME_QUERY);
        query.setParameter("name", name);
        ExternalEntity result;
        try {
            result = (ExternalEntity) query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.NON_UNIQUE_RESULT));
        }
        return result;
    }
    
    /**
     * Search the installation id in database
     * 
     * @param id
     *            the installation id
     * @return the found installation, else null
     */
    public Installation findInstallationById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Installation.class, id);
    }
    
    /**
     * Search an installation by its name
     * 
     * @param name
     *            the searched installation name
     * @return the found installation, else null
     */
    public Installation findInstallationByName(String name) {
        Query query = em.createNamedQuery("InstallationByName");
        query.setParameter("name", name);
        Installation result;
        try {
            result = (Installation) query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.NON_UNIQUE_RESULT));
        }
        return result;
    }
    
    /**
     * Search the installations having the provided user as person in charge
     * 
     * @param pInCharge
     *            the user to search
     * @return the list of found installations
     */
    public List<Installation> findInstallationsByUser(User pInCharge) {
        
        List<Installation> lInstallations = new ArrayList<Installation>();
        
        if (pInCharge == null) {
            return lInstallations;
        }
        TypedQuery<Installation> lQuery =
                em.createNamedQuery(Installation.BY_PERSONINCHARGE_QUERY,
                        Installation.class);
        lQuery.setParameter("personInCharge", pInCharge);
        return lQuery.getResultList();
    }
    
    /**
     * Search a place by its complete name "<Building_name>-<PLace_name>"
     * 
     * @param pName
     *            the searched place complete name
     * @return the found place, else null
     */
    public Place findPlaceByCompleteName(String pName) {
        
        Query lQuery = em.createNamedQuery(Place.PLACE_BY_COMPLETE_NAME_QUERY);
        lQuery.setParameter("completeName", pName);
        Place lPlace;
        try {
            lPlace = (Place) lQuery.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.NON_UNIQUE_RESULT));
        }
        return lPlace;
    }
    
    /**
     * Search the place id in database
     * 
     * @param id
     *            the place id
     * @return the found place, else null
     */
    public Place findPlaceById(Long id) {
        if (id == null) {
            return null;
        }
        Place place = em.find(Place.class, id);
        return place;
    }
    
    /**
     * Search a place by its name and its building
     * 
     * @param pName
     *            the searched place name
     * @param pBuilding
     *            the building
     * @return the found place, else null
     */
    public Place findPlaceByNameAndBuilding(String pName, Building pBuilding) {
        
        Query lQuery =
                em.createNamedQuery(Place.PLACE_BY_NAME_AND_BUILDING_QUERY);
        lQuery.setParameter("name", pName);
        lQuery.setParameter("building", pBuilding);
        Place result;
        try {
            result = (Place) lQuery.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.NON_UNIQUE_RESULT));
        }
        return result;
    }
    
    /**
     * Search all places by the id of the building containing them
     * 
     * @param buildingFk
     *            the building id
     * @return the list of found places
     */
    public List<Place> findPlacesByBuildingFk(Long buildingFk) {
        List<Place> result = Collections.emptyList();
        TypedQuery<Place> query =
                em.createNamedQuery("PlacesByBuildingFk", Place.class);
        query.setParameter("buildingFk", buildingFk);
        try {
            result = query.getResultList();
            
        }
        catch (NoResultException e) {
        }
        return result;
    }
    
    /**
     * Search all places by the id of the building containing them and their
     * name
     * 
     * @param pBuildingId
     *            the building id
     * @param pPlaceName
     *            the place name to search
     * @return the list of found places
     */
    public List<Place> findPlacesByBuildingIdAndName(Long pBuildingId,
            String pPlaceName) {
        
        String lNameSql = StringUtil.parseToSQLRegex(pPlaceName);
        
        List<Place> lResult = Collections.emptyList();
        TypedQuery<Place> lQuery =
                em.createNamedQuery(Place.PLACES_BY_BUILDING_ID_AND_NAME_QUERY,
                        Place.class);
        lQuery.setParameter("buildingId", pBuildingId);
        lQuery.setParameter("placeName", lNameSql);
        try {
            lResult = lQuery.getResultList();
            
        }
        catch (NoResultException e) {
        }
        return lResult;
    }
    
    /**
     * Search the Tool corresponding to the provided id
     * 
     * @param id
     *            the searched tool id
     * @return the found tool, else null
     */
    public Tool findToolById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Tool.class, id);
    }
    
    /**
     * @param name
     *            the building name
     * @return true if the building name already exists, else false
     */
    public Boolean isBuildingExisting(String name) {
        return (findBuildingByName(name) != null);
    }
    
    /**
     * @param value
     *            the installation name
     * @return true if the installation name is already used, else false
     */
    public boolean isInstallationExisting(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        Query query = em.createNamedQuery("ExistInstallationName");
        query.setParameter("name", value);
        try {
            query.getSingleResult();
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
        catch (NonUniqueResultException e) {
            return true;
        }
    }
    
    /**
     * Return the list of location entities corresponding to the suggest value
     * 
     * @param clazz
     *            the class of the returned location entities
     * @param placeType
     *            the place type, if the entities class is Place
     * @param suggest
     *            the suggest value
     * @return the list of found entities of the provided class
     */
    public <T> List<T> suggestionListLocation(Class<T> clazz,
            PlaceType placeType, String suggest) {
        
        if (StringUtil.isEmptyOrNull(suggest)) {
            throw new IllegalArgumentException(
                    "suggestionListLocation suggest invalide");
        }
        
        suggest = StringUtil.parseToSQLRegex(suggest);
        if (suggest.indexOf("%") == -1) {
            // surround the suggest value with '%'
            suggest = "%" + suggest + "%";
        }
        
        StringBuffer qs =
                new StringBuffer("SELECT location FROM ").append(
                        clazz.getSimpleName()).append(" location WHERE ");
        
        if (clazz.equals(Place.class) && placeType != null) {
            
            qs.append(" CONCAT(location.building.name, '");
            qs.append(Place.BUILDING_PLACE_SEPARATOR);
            qs.append("', location.name) LIKE :suggest");
            qs.append(" AND location.type = :placeType");
        }
        else {
            qs.append(" location.name like :suggest");
        }
        
        qs.append(" order by location.name asc");
        
        TypedQuery<T> query = em.createQuery(qs.toString(), clazz);
        query.setParameter("suggest", suggest);
        if (clazz.equals(Place.class) && placeType != null) {
            query.setParameter("placeType", placeType);
        }
        
        log.warning(qs.toString());
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
}