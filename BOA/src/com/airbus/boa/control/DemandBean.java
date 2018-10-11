/*
 * ------------------------------------------------------------------------
 * Class : DemandBean
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;

/**
 * Bean used to managing Demands
 */
@Stateless
@LocalBean
public class DemandBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(DemandBean.class.getName());
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    @PersistenceContext(name = "DemandService")
    private EntityManager em;
    
    /**
     * Persist the provided demand in database.
     * 
     * @param pDemand
     *            the demand to create
     * @param pLocation
     *            the location of the PC of the demand
     * @param pContainer
     *            the container of the PC of the demand
     * @return the created demand
     */
    public Demand create(Demand pDemand, Location pLocation,
            Container pContainer) {
        
        em.persist(pDemand);
        
        LocationManager lLocationManager = new LocationManager(pDemand);
        
        lLocationManager.moveTo(pLocation, locationManagerBean);
        
        ContainerManager lContainerManager = new ContainerManager(pDemand);
        
        lContainerManager.linkTo(pContainer, containerManagerBean);
        
        return pDemand;
    }
    
    /**
     * Merge the provided demand with the one in database.<br>
     * The location is not updated.
     * 
     * @param pDemand
     *            the demand to merge
     * @return the merged demand
     */
    public Demand merge(Demand pDemand) {
        try {
            
            return em.merge(pDemand);
            
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
     * Merge the provided demand with the one in database.
     * 
     * @param pDemand
     *            the demand to merge
     * @param pLocation
     *            the location of the PC of the demand
     * @param pContainer
     *            the container of the PC of the demand
     * @return the merged demand
     */
    public Demand
            merge(Demand pDemand, Location pLocation, Container pContainer) {
        try {
            
            Demand lMergedDemand = em.merge(pDemand);
            
            LocationManager lManager = new LocationManager(lMergedDemand);
            lManager.moveTo(pLocation, locationManagerBean);
            
            ContainerManager lContainerManager =
                    new ContainerManager(lMergedDemand);
            lContainerManager.linkTo(pContainer, containerManagerBean);
            
            return em.merge(lMergedDemand);
            
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
     * Remove the provided demand from database
     * 
     * @param pDemand
     *            the demand to delete
     */
    public void remove(Demand pDemand) {
        
        // Remove the demand from its location
        if (pDemand.getLocation() != null) {
            LocationManager lManager = new LocationManager(pDemand);
            lManager.removeFrom(locationManagerBean);
        }
        
        // Remove the demand from its container
        if (pDemand.getContainer() != null) {
            ContainerManager lContainerManager = new ContainerManager(pDemand);
            lContainerManager.unlink(containerManagerBean);
        }
        
        em.remove(pDemand);
    }
    
    /**
     * Return all demands from database
     * 
     * @return a list of demands from database
     */
    public List<Demand> findAllDemands() {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.ALL_DEMANDS_QUERY, Demand.class);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search the demand by its id
     * 
     * @param pId
     *            the demand id to search
     * @return the found demand, else null
     */
    public Demand findDemandById(Long pId) {
        if (pId == null) {
            return null;
        }
        return em.find(Demand.class, pId);
    }
    
    /**
     * Search demands by their issuer
     * 
     * @param pDemandIssuer
     *            the demand issuer to search
     * @return the found demands list
     */
    public List<Demand> findDemandsByIssuer(User pDemandIssuer) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_BY_ISSUER_QUERY,
                        Demand.class);
        lQuery.setParameter("issuer", pDemandIssuer);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands located in the article
     * 
     * @param pArticle
     *            the article to search
     * @return the found demands list
     */
    public List<Demand> findDemandsLocatedIntoArticle(Article pArticle) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_LOCATED_ARTICLE_QUERY,
                        Demand.class);
        lQuery.setParameter("article", pArticle);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands located in the external entity
     * 
     * @param pExternalEntity
     *            the external entity to search
     * @return the found demands list
     */
    public List<Demand> findDemandsLocatedIntoExternalEntity(
            ExternalEntity pExternalEntity) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(
                        Demand.DEMANDS_LOCATED_EXTERNAL_ENTITY_QUERY,
                        Demand.class);
        lQuery.setParameter("externalEntity", pExternalEntity);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands located in the installation
     * 
     * @param pInstallation
     *            the installation to search
     * @return the found demands list
     */
    public List<Demand> findDemandsLocatedIntoInstallation(
            Installation pInstallation) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_LOCATED_INSTALLATION_QUERY,
                        Demand.class);
        lQuery.setParameter("installation", pInstallation);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands located in the place
     * 
     * @param pPlace
     *            the place to search
     * @return the found demands list
     */
    public List<Demand> findDemandsLocatedIntoPlace(Place pPlace) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_LOCATED_PLACE_QUERY,
                        Demand.class);
        lQuery.setParameter("place", pPlace);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands located in the tool
     * 
     * @param pTool
     *            the tool to search
     * @return the found demands list
     */
    public List<Demand> findDemandsLocatedIntoTool(Tool pTool) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_LOCATED_TOOL_QUERY,
                        Demand.class);
        lQuery.setParameter("tool", pTool);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands using the PC (to replace or allocated)
     * 
     * @param pPC
     *            the PC to search
     * @return the found demands list
     */
    public List<Demand> findDemandsUsingPC(PC pPC) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_USING_PC_QUERY, Demand.class);
        lQuery.setParameter("pc", pPC);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands using the product type
     * 
     * @param pProductType
     *            the product type to search
     * @return the found demands list
     */
    public List<Demand> findDemandsUsingProductType(ProductTypePC pProductType) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_USING_PRODUCTTYPE_QUERY,
                        Demand.class);
        lQuery.setParameter("productType", pProductType);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands using the software
     * 
     * @param pSoftware
     *            the software to search
     * @return the found demands list
     */
    public List<Demand> findDemandsUsingSoftware(Software pSoftware) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_USING_SOFTWARE_QUERY,
                        Demand.class);
        lQuery.setParameter("software", pSoftware);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search demands using the type
     * 
     * @param pType
     *            the type to search
     * @return the found demands list
     */
    public List<Demand> findDemandsUsingType(TypePC pType) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMANDS_USING_TYPE_QUERY,
                        Demand.class);
        lQuery.setParameter("type", pType);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search the demand having the provided PC as allocated PC
     * 
     * @param pPC
     *            the PC to search
     * @return the found demand, or null
     */
    public Demand findDemandWithAllocatedPC(PC pPC) {
        
        TypedQuery<Demand> lQuery =
                em.createNamedQuery(Demand.DEMAND_ALLOCATED_PC_QUERY,
                        Demand.class);
        lQuery.setParameter("pc", pPC);
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            return null;
        }
    }
    
}
