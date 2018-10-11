/*
 * ------------------------------------------------------------------------
 * Class : LocationManagerBean
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.LocationForArticle;
import com.airbus.boa.entity.location.LocationForDemand;
import com.airbus.boa.entity.location.LocationForTool;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;

/**
 * Bean used to manage the location of entities
 */
@Stateless
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class LocationManagerBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext(name = "LocationManagerService")
    private EntityManager em;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ToolBean toolBean;
    
    /**
     * Move the article to the external entity
     * 
     * @param pArticle
     *            the article
     * @param pExternalEntity
     *            the external entity
     * @return the merged article
     */
    public Article moveToExternalEntity(Article pArticle,
            ExternalEntity pExternalEntity) {
        
        Article lMergedArticle = em.merge(pArticle);
        ExternalEntity lMergedExternalEntity = em.merge(pExternalEntity);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null) {
            // Create a new LocationForArticle
            lLocationForArticle =
                    new LocationForArticle(lMergedArticle, false, null,
                            lMergedExternalEntity, null);
            
            em.persist(lLocationForArticle);
        }
        else {
            // Update the LocationForArticle
            lLocationForArticle.setExternalEntity(lMergedExternalEntity);
            
            lLocationForArticle = em.merge(lLocationForArticle);
        }
        
        lMergedArticle.setLocationOrm(lLocationForArticle);
        lMergedExternalEntity.getLocatedOrmArticles().add(lLocationForArticle);
        
        lMergedArticle = em.merge(lMergedArticle);
        lMergedExternalEntity = em.merge(lMergedExternalEntity);
        return lMergedArticle;
    }
    
    /**
     * Remove the article from the external entity
     * 
     * @param pArticle
     *            the article
     * @param pExternalEntity
     *            the external entity
     * @return the merged article
     */
    public Article removeFromExternalEntity(Article pArticle,
            ExternalEntity pExternalEntity) {
        
        Article lMergedArticle = em.merge(pArticle);
        ExternalEntity lMergedExternalEntity = em.merge(pExternalEntity);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null || lLocationForArticle.isEmpty()) {
            return lMergedArticle;
        }
        
        lLocationForArticle.setExternalEntity(null);
        
        // update the location
        lMergedArticle.setLocationOrm(lLocationForArticle);
        
        lMergedExternalEntity.getLocatedOrmArticles().remove(
                lLocationForArticle);
        
        lMergedArticle = em.merge(lMergedArticle);
        lMergedExternalEntity = em.merge(lMergedExternalEntity);
        
        lLocationForArticle = em.merge(lLocationForArticle);
        
        return lMergedArticle;
    }
    
    /**
     * Move the PC of the demand to the external entity
     * 
     * @param pDemand
     *            the demand
     * @param pExternalEntity
     *            the external entity
     * @return the merged demand
     */
    public Demand moveToExternalEntity(Demand pDemand,
            ExternalEntity pExternalEntity) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null) {
            // Create a new LocationForDemand
            lLocationForDemand =
                    new LocationForDemand(lMergedDemand, false, null,
                            pExternalEntity, null);
            
            em.persist(lLocationForDemand);
        }
        else {
            // Update the LocationForDemand
            lLocationForDemand.setExternalEntity(pExternalEntity);
            
            lLocationForDemand = em.merge(lLocationForDemand);
        }
        
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        return em.merge(lMergedDemand);
    }
    
    /**
     * Remove the PC of the demand from its external entity
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand removeFromExternalEntity(Demand pDemand) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null || lLocationForDemand.isEmpty()) {
            return lMergedDemand;
        }
        
        lLocationForDemand.setExternalEntity(null);
        
        // update the location
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        lMergedDemand = em.merge(lMergedDemand);
        
        lLocationForDemand = em.merge(lLocationForDemand);
        
        return lMergedDemand;
    }
    
    /**
     * Move the tool to the external entity
     * 
     * @param pTool
     *            the tool
     * @param pExternalEntity
     *            the external entity
     * @return the merged tool
     */
    public Tool
            moveToExternalEntity(Tool pTool, ExternalEntity pExternalEntity) {
        
        Tool lMergedTool = em.merge(pTool);
        ExternalEntity lMergedExternalEntity = em.merge(pExternalEntity);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null) {
            // Create a new LocationForTool
            lLocationForTool =
                    new LocationForTool(lMergedTool, false, null,
                            lMergedExternalEntity, null);
            
            em.persist(lLocationForTool);
        }
        else {
            // Update the LocationForTool
            lLocationForTool.setExternalEntity(lMergedExternalEntity);
            
            lLocationForTool = em.merge(lLocationForTool);
        }
        
        lMergedTool.setLocationOrm(lLocationForTool);
        lMergedExternalEntity.getLocatedOrmTools().add(lLocationForTool);
        
        lMergedTool = em.merge(lMergedTool);
        lMergedExternalEntity = em.merge(lMergedExternalEntity);
        return lMergedTool;
    }
    
    /**
     * Remove the tool from the external entity
     * 
     * @param pTool
     *            the tool
     * @param pExternalEntity
     *            the external entity
     * @return the merged tool
     */
    public Tool removeFromExternalEntity(Tool pTool,
            ExternalEntity pExternalEntity) {
        
        Tool lMergedTool = em.merge(pTool);
        ExternalEntity lMergedExternalEntity = em.merge(pExternalEntity);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null || lLocationForTool.isEmpty()) {
            return lMergedTool;
        }
        
        lLocationForTool.setExternalEntity(null);
        
        // update the location
        lMergedTool.setLocationOrm(lLocationForTool);
        
        lMergedExternalEntity.getLocatedOrmTools().remove(lLocationForTool);
        
        lMergedTool = em.merge(lMergedTool);
        lMergedExternalEntity = em.merge(lMergedExternalEntity);
        
        lLocationForTool = em.merge(lLocationForTool);
        
        return lMergedTool;
    }
    
    /**
     * Locate the article into the location inherited from parent
     * 
     * @param pArticle
     *            the article to locate
     * @param pPreciseLocation
     *            details on location
     * @return the merged article
     */
    public Article moveToInheritedLocation(Article pArticle,
            String pPreciseLocation) {
        
        Article lMergedArticle = em.merge(pArticle);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null) {
            // Create a new LocationForArticle
            lLocationForArticle =
                    new LocationForArticle(lMergedArticle, true, null, null,
                            pPreciseLocation);
            
            em.persist(lLocationForArticle);
        }
        else {
            // Update the LocationForArticle
            lLocationForArticle.setInherited(true);
            lLocationForArticle.setPreciseLocation(pPreciseLocation);
            
            lLocationForArticle = em.merge(lLocationForArticle);
        }
        
        lMergedArticle.setLocationOrm(lLocationForArticle);
        
        lMergedArticle = em.merge(lMergedArticle);
        return lMergedArticle;
    }
    
    /**
     * Remove the article from the location inherited from the parent
     * 
     * @param pArticle
     *            the article to remove
     * @return the merged article
     */
    public Article removeFromInheritedLocation(Article pArticle) {
        
        Article lMergedArticle = em.merge(pArticle);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null || lLocationForArticle.isEmpty()) {
            return lMergedArticle;
        }
        
        lLocationForArticle.setInherited(false);
        
        // update the location
        lMergedArticle.setLocationOrm(lLocationForArticle);
        
        lMergedArticle = em.merge(lMergedArticle);
        
        lLocationForArticle = em.merge(lLocationForArticle);
        
        return lMergedArticle;
    }
    
    /**
     * Move the PC of the demand to the location inherited from the parent
     * 
     * @param pDemand
     *            the demand
     * @param pPreciseLocation
     *            details on location
     * @return the merged demand
     */
    public Demand moveToInheritedLocation(Demand pDemand,
            String pPreciseLocation) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null) {
            // Create a new LocationForDemand
            lLocationForDemand =
                    new LocationForDemand(lMergedDemand, true, null, null,
                            pPreciseLocation);
            
            em.persist(lLocationForDemand);
        }
        else {
            // Update the LocationForDemand
            lLocationForDemand.setInherited(true);
            lLocationForDemand.setPreciseLocation(pPreciseLocation);
            
            lLocationForDemand = em.merge(lLocationForDemand);
        }
        
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        return em.merge(lMergedDemand);
    }
    
    /**
     * Remove the PC of the demand from the location inherited from parent
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand removeFromInheritedLocation(Demand pDemand) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null || lLocationForDemand.isEmpty()) {
            return lMergedDemand;
        }
        
        lLocationForDemand.setInherited(false);
        
        // update the location
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        lMergedDemand = em.merge(lMergedDemand);
        
        lLocationForDemand = em.merge(lLocationForDemand);
        
        return lMergedDemand;
    }
    
    /**
     * Locate the provided tool in the location inherited from parent
     * 
     * @param pTool
     *            the tool
     * @param pPreciseLocation
     *            details on location
     * @return the merged tool
     */
    public Tool moveToInheritedLocation(Tool pTool, String pPreciseLocation) {
        
        Tool lMergedTool = em.merge(pTool);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null) {
            // Create a new LocationForTool
            lLocationForTool =
                    new LocationForTool(lMergedTool, true, null, null,
                            pPreciseLocation);
            
            em.persist(lLocationForTool);
        }
        else {
            // Update the LocationForTool
            lLocationForTool.setInherited(true);
            lLocationForTool.setPreciseLocation(pPreciseLocation);
            
            lLocationForTool = em.merge(lLocationForTool);
        }
        
        lMergedTool.setLocationOrm(lLocationForTool);
        
        lMergedTool = em.merge(lMergedTool);
        return lMergedTool;
    }
    
    /**
     * Remove the provided tool from the location inherited from parent
     * 
     * @param pTool
     *            the tool to remove
     * @return the removed tool
     */
    public Tool removeFromInheritedLocation(Tool pTool) {
        
        Tool lMergedTool = toolBean.findToolById(pTool.getId());
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null || lLocationForTool.isEmpty()) {
            return lMergedTool;
        }
        
        lLocationForTool.setInherited(false);
        
        // update the location
        lMergedTool.setLocationOrm(lLocationForTool);
        
        lMergedTool = em.merge(lMergedTool);
        
        lLocationForTool = em.merge(lLocationForTool);
        
        return lMergedTool;
    }
    
    /**
     * Locate the article into the place
     * 
     * @param pArticle
     *            the article to locate
     * @param pPlace
     *            the place
     * @param pPreciseLocation
     *            details on location
     * @return the merged article
     */
    public Article moveToPlace(Article pArticle, Place pPlace,
            String pPreciseLocation) {
        
        Place lMergedPlace = em.merge(pPlace);
        Article lMergedArticle = em.merge(pArticle);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null) {
            // Create a new LocationForArticle
            lLocationForArticle =
                    new LocationForArticle(lMergedArticle, false, lMergedPlace,
                            null, pPreciseLocation);
            
            em.persist(lLocationForArticle);
        }
        else {
            // Update the LocationForArticle
            lLocationForArticle.setPlace(lMergedPlace);
            lLocationForArticle.setPreciseLocation(pPreciseLocation);
            
            lLocationForArticle = em.merge(lLocationForArticle);
        }
        
        lMergedPlace.getLocatedOrmArticles().add(lLocationForArticle);
        lMergedArticle.setLocationOrm(lLocationForArticle);
        
        lMergedPlace = em.merge(lMergedPlace);
        lMergedArticle = em.merge(lMergedArticle);
        return lMergedArticle;
    }
    
    /**
     * Remove the article from the place
     * 
     * @param pArticle
     *            the article to remove
     * @param pPlace
     *            the place
     * @return the merged article
     */
    public Article removeFromPlace(Article pArticle, Place pPlace) {
        
        Place lMergedPlace = locationBean.findPlaceById(pPlace.getId());
        Article lMergedArticle = em.merge(pArticle);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        if (lLocationForArticle == null || lLocationForArticle.isEmpty()) {
            return lMergedArticle;
        }
        
        lLocationForArticle.setPlace(null);
        
        // update the location
        lMergedArticle.setLocationOrm(lLocationForArticle);
        
        lMergedPlace.getLocatedOrmArticles().remove(lLocationForArticle);
        
        lMergedArticle = em.merge(lMergedArticle);
        lMergedPlace = em.merge(lMergedPlace);
        
        lLocationForArticle = em.merge(lLocationForArticle);
        
        return lMergedArticle;
    }
    
    /**
     * Move the PC of the demand to the place
     * 
     * @param pDemand
     *            the demand
     * @param pPlace
     *            the place
     * @param pPreciseLocation
     *            details on location
     * @return the merged demand
     */
    public Demand moveToPlace(Demand pDemand, Place pPlace,
            String pPreciseLocation) {
        
        Place lMergedPlace = em.merge(pPlace);
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null) {
            // Create a new LocationForDemand
            lLocationForDemand =
                    new LocationForDemand(lMergedDemand, false, lMergedPlace,
                            null, pPreciseLocation);
            
            em.persist(lLocationForDemand);
        }
        else {
            // Update the LocationForDemand
            lLocationForDemand.setPlace(lMergedPlace);
            lLocationForDemand.setPreciseLocation(pPreciseLocation);
            
            lLocationForDemand = em.merge(lLocationForDemand);
        }
        
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        return em.merge(lMergedDemand);
    }
    
    /**
     * Remove the PC of the demand from its place
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand removeFromPlace(Demand pDemand) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        if (lLocationForDemand == null || lLocationForDemand.isEmpty()) {
            return lMergedDemand;
        }
        
        lLocationForDemand.setPlace(null);
        
        // update the location
        lMergedDemand.setLocationOrm(lLocationForDemand);
        
        lMergedDemand = em.merge(lMergedDemand);
        
        lLocationForDemand = em.merge(lLocationForDemand);
        
        return lMergedDemand;
    }
    
    /**
     * Move the installation into a place (of Laboratory or Room type)
     * 
     * @param pInstallation
     *            the installation
     * @param pPlace
     *            the place
     */
    public void moveToPlace(Installation pInstallation, Place pPlace) {
        
        Place lPlace = locationBean.findPlaceById(pPlace.getId());
        Installation lInstallation =
                locationBean.findInstallationById(pInstallation.getId());
        
        lPlace.getLocatedInstallations().add(lInstallation);
        lInstallation.setLocationPlace(lPlace);
        
        em.merge(lPlace);
        em.merge(lInstallation);
    }
    
    /**
     * Remove the installation from the place
     * 
     * @param pInstallation
     *            the installation
     * @param pPlace
     *            the place
     */
    public void removeFromPlace(Installation pInstallation, Place pPlace) {
        
        Installation lInstallation =
                locationBean.findInstallationById(pInstallation.getId());
        Place lPlace = locationBean.findPlaceById(pPlace.getId());
        
        lPlace.getLocatedInstallations().remove(lInstallation);
        lInstallation.setLocationPlace(null);
        
        em.merge(lPlace);
        em.merge(lInstallation);
    }
    
    /**
     * Locate the provided tool in the provided place
     * 
     * @param pTool
     *            the tool
     * @param pPlace
     *            the place
     * @param pPreciseLocation
     *            details on location
     * @return the merged tool
     */
    public Tool moveToPlace(Tool pTool, Place pPlace, String pPreciseLocation) {
        
        Place lMergedPlace = em.merge(pPlace);
        Tool lMergedTool = em.merge(pTool);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null) {
            // Create a new LocationForTool
            lLocationForTool =
                    new LocationForTool(lMergedTool, false, lMergedPlace, null,
                            pPreciseLocation);
            
            em.persist(lLocationForTool);
        }
        else {
            // Update the LocationForTool
            lLocationForTool.setPlace(lMergedPlace);
            lLocationForTool.setPreciseLocation(pPreciseLocation);
            
            lLocationForTool = em.merge(lLocationForTool);
        }
        
        lMergedPlace.getLocationForTools().add(lLocationForTool);
        lMergedTool.setLocationOrm(lLocationForTool);
        
        lMergedPlace = em.merge(lMergedPlace);
        lMergedTool = em.merge(lMergedTool);
        return lMergedTool;
    }
    
    /**
     * Remove the provided tool from the provided place
     * 
     * @param pTool
     *            the tool to remove
     * @param pPlace
     *            the place containing the tool
     * @return the removed tool
     */
    public Tool removeFromPlace(Tool pTool, Place pPlace) {
        
        Place lMergedPlace = locationBean.findPlaceById(pPlace.getId());
        Tool lMergedTool = em.merge(pTool);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        if (lLocationForTool == null || lLocationForTool.isEmpty()) {
            return lMergedTool;
        }
        
        lLocationForTool.setPlace(null);
        
        // update the location
        lMergedTool.setLocationOrm(lLocationForTool);
        
        lMergedPlace.getLocationForTools().remove(lLocationForTool);
        
        lMergedTool = em.merge(lMergedTool);
        lMergedPlace = em.merge(lMergedPlace);
        
        lLocationForTool = em.merge(lLocationForTool);
        
        return lMergedTool;
    }
    
    /**
     * Remove the remaining location entry line in database.<br>
     * <b><i>The location entry shall represent an empty location (not
     * inherited, not located and not sent to external location).</i></b>
     * 
     * @param pArticle
     *            the article
     * @return the merged article
     */
    public Article removeFromAll(Article pArticle) {
        
        Article lMergedArticle = em.merge(pArticle);
        
        LocationForArticle lLocationForArticle =
                lMergedArticle.getLocationOrm();
        
        lMergedArticle.setLocationOrm(null);
        lMergedArticle = em.merge(lMergedArticle);
        
        em.remove(em.merge(lLocationForArticle));
        
        return lMergedArticle;
    }
    
    /**
     * Remove the remaining location entry line in database.<br>
     * <b><i>The location entry shall represent an empty location (not
     * inherited, not located and not sent to external location).</i></b>
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand removeFromAll(Demand pDemand) {
        
        Demand lMergedDemand = em.merge(pDemand);
        
        LocationForDemand lLocationForDemand = lMergedDemand.getLocationOrm();
        
        lMergedDemand.setLocationOrm(null);
        lMergedDemand = em.merge(lMergedDemand);
        
        em.remove(em.merge(lLocationForDemand));
        
        return lMergedDemand;
    }
    
    /**
     * Remove the remaining location entry line in database.<br>
     * <b><i>The location entry shall represent an empty location (not
     * inherited, not located and not sent to external location).</i></b>
     * 
     * @param pTool
     *            the tool
     * @return the merged tool
     */
    public Tool removeFromAll(Tool pTool) {
        
        Tool lMergedTool = em.merge(pTool);
        
        LocationForTool lLocationForTool = lMergedTool.getLocationOrm();
        
        lMergedTool.setLocationOrm(null);
        lMergedTool = em.merge(lMergedTool);
        
        em.remove(em.merge(lLocationForTool));
        
        return lMergedTool;
    }
    
}
