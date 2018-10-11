/*
 * ------------------------------------------------------------------------
 * Class : PCBean
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AvailabilityStatus;
import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.CollectionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Bean managing PC entities
 */
@Stateless
@LocalBean
public class PCBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private CommunicationPortBean comPortBean;
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    @PersistenceContext(name = "PCService")
    private EntityManager em;
    
    /**
     * Create PC in database from a demand.
     * Airbus SN will be Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX +
     * demand number.
     * PC will be allocated to demand.
     * Action of creation will be created in PC history.
     * PC Availability will be new.
     * PC Name will be: Constants.PC_CREATED_FROM_DEMAND_NAME_PREFIX + demand
     * number.
     * 
     * @param pDemand
     *            the demand for which the PC will be created.
     * @param pUseState
     *            the PC use state
     * @throws ValidationException
     *             when the PC cannot be created
     */
    public void create(Demand pDemand, UseState pUseState)
            throws ValidationException {
        // Create PC
        String lAirbusSN =
                Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX
                        + pDemand.getDemandNumber();
        if (articleBean.existASN(lAirbusSN)) {
            String lMsg =
                    MessageBundle.getMessage(Constants.SN_ALREADY_USED) + ": "
                            + lAirbusSN;
            throw new ValidationException(lMsg);
        }
        ProductTypePC lType = pDemand.getProductTypePC();
        String lName =
                Constants.PC_CREATED_FROM_DEMAND_NAME_PREFIX
                        + pDemand.getDemandNumber();
        TypeArticle lArticleType = pDemand.getTypePC();
        // TODO : to implement once the Demand Management process has been
        // redefined
        DepartmentInCharge lDepartmentInCharge = null;
        BusinessAllocationPC lPCAllocation = pDemand.getAllocation();
        BusinessUsagePC lPCUsage = pDemand.getUsage();
        User lUser = pDemand.getIssuer();
        String lOwner = pDemand.getOwner();
        String lOwnerSiglum = pDemand.getOwnerSiglum();
        AvailabilityStatus lAvailabilityStatus = AvailabilityStatus.New;
        PC lPC = new PC(lAirbusSN, lType, lName, lDepartmentInCharge,
                lArticleType, lPCAllocation, lPCUsage, lUser, lOwner,
                lOwnerSiglum, lAvailabilityStatus);
        // Get Software from demand, add them to PC.
        lPC.setSoftwares(pDemand.getSoftwares());
        lPC.setAllocatedToDemand(pDemand);
        // Set State of the PC.
        lPC.setUseState(pUseState);
        // Create article in database.
        articleBean.createArticle(lPC, pDemand.getLocation(),
                pDemand.getContainer());
        // Update PC history.
        History lHistory = lPC.getHistory();
        lHistory.getActions().add(
                new Action(lUser.getLogin(), Action.formatAuthor(lUser),
                        Constants.CREATION, new Comment(
                                Constants.CREATION_FROM_DEMAND)));
        lHistory = historyBean.merge(lHistory);
    }
    
    /**
     * Create the PC in database
     * 
     * @param pc
     *            the PC to create
     * @param pLocation
     *            the PC location
     * @param pContainer
     *            the PC container
     */
    public PC create(PC pc, Location pLocation, Container pContainer) {
        return articleBean.createArticle(pc, pLocation, pContainer);
    }
    
    /**
     * Update the PC in database with the provided values
     * 
     * @param pPC
     *            the PC to update
     * @param pModifications
     *            the actions performed to store in the history
     * @return the merged PC
     */
    public PC merge(PC pPC, List<Action> pModifications) {
        
        History lHistory = pPC.getHistory();
        lHistory.getActions().addAll(pModifications);
        lHistory = historyBean.merge(lHistory);
        
        return em.merge(pPC);
    }
    
    /**
     * Update the PC in database with the provided values
     * 
     * @param pc
     *            the PC to update
     * @param ports
     *            the PC communication ports list to set
     * @param modifications
     *            the actions performed to store in the history
     * @return the merged PC
     */
    public PC merge(PC pc, List<CommunicationPort> ports,
            List<Action> modifications) {
        
        History history = pc.getHistory();
        history.getActions().addAll(modifications);
        history = historyBean.merge(history);
        
        // Update the new ports list
        // No call to the merge method of comPortBean, treatment performed
        // automatically thanks to the "cascade.all"
        pc.setPorts(ports);
        
        pc = em.merge(pc);
        
        return pc;
    }
    
    /**
     * Update the PC in database with the provided values
     * 
     * @param pPC
     *            the modified PC to update
     * @param pPortsAdded
     *            the communication ports to add to the PC
     * @param pPortsDeleted
     *            the communication ports to remove from the PC
     * @param pSpecificitiesAdded
     *            the specificities to add to the PC
     * @param pSpecificitiesDeleted
     *            the specificities to remove from the PC
     * @param pDatedCommentsAdded
     *            the dated comments to add to the PC
     * @param pDatedCommentsDeleted
     *            the dated comments to remove from the PC
     * @param pSoftwareChanges
     *            the software changes (a map with following structure<br>
     *            Key: the software complete name<br>
     *            Value: True if the software is newly installed, False if the
     *            software is newly removed
     * @param pLocation
     *            the PC location
     * @param pContainer
     *            the PC container
     * @param pModifications
     *            the actions performed to store in the PC history
     * @param pLogin
     *            login of the user at the origin of the merge
     * @return the merged PC
     */
    public PC merge(PC pPC, List<CommunicationPort> pPortsAdded,
            List<CommunicationPort> pPortsDeleted,
            List<PCSpecificity> pSpecificitiesAdded,
            List<PCSpecificity> pSpecificitiesDeleted,
            List<DatedComment> pDatedCommentsAdded,
            List<DatedComment> pDatedCommentsDeleted,
            Map<String, Boolean> pSoftwareChanges, Location pLocation,
            Container pContainer, List<Action> pModifications, String pLogin) {
        
        // Update the new software list
        // Browse the modifications list
        Set<Entry<String, Boolean>> lSet = pSoftwareChanges.entrySet();
        Iterator<Entry<String, Boolean>> i = lSet.iterator();
        Software lSoftware;
        while (i.hasNext()) {
            // Key: Complete name of the software
            // Value: True if the software is installed on the PC, False if
            // removed from the PC
            Map.Entry<String, Boolean> lChange = i.next();
            if (lChange.getValue()) {
                // Software installed on the PC
                lSoftware = softwareBean.findByCompleteName(lChange.getKey());
                
                if (lSoftware != null) {
                    lSoftware.addEquipement(pPC);
                    softwareBean.merge(lSoftware);
                }
            }
            else {
                // Software removed from the PC
                lSoftware = softwareBean.findByCompleteName(lChange.getKey());
                if (lSoftware != null) {
                    lSoftware.removeEquipement(pPC);
                    softwareBean.merge(lSoftware);
                }
            }
        }
        
        // Update history
        History lHistory = pPC.getHistory();
        lHistory.getActions().addAll(pModifications);
        lHistory = historyBean.merge(lHistory);
        
        // Update the new ports list
        // No call to the merge method of comPortBean, treatment performed
        // automatically thanks to the "cascade.all"
        
        // List containing the previous instances of modified objects
        List<CommunicationPort> lPortsOldies =
                new ArrayList<CommunicationPort>();
        // List containing the new instances of modified objects
        List<CommunicationPort> lPortsUpdated =
                CollectionUtil.manageUpdated(pPortsAdded, pPortsDeleted,
                        lPortsOldies);
        
        // Remove from the PC the previous ports which have been modified
        pPC.getPorts().removeAll(lPortsOldies);
        // Merge the entity into the right transaction
        for (CommunicationPort lCP : lPortsUpdated) {
            comPortBean.merge(lCP);
        }
        // Add the modified ports
        pPC.getPorts().addAll(lPortsUpdated);
        
        // Remove from the database the deleted port (merge+delete)
        for (CommunicationPort lCP : pPortsDeleted) {
            comPortBean.remove(lCP);
        }
        // Remove the link between deleted ports and PC
        pPC.getPorts().removeAll(pPortsDeleted);
        
        // Add in database the new ports (with retrieving of the generated ID)
        for (CommunicationPort lCP : pPortsAdded) {
            comPortBean.create(lCP);
        }
        // Link the new ports to the PC
        pPC.getPorts().addAll(pPortsAdded);
        

        // Update the new specificity list
        // List containing the previous instances of modified objects
        List<PCSpecificity> lSpecificitiesOldies =
                new ArrayList<PCSpecificity>();
        // List containing the new instances of modified objects
        List<PCSpecificity> lSpecificitiesUpdated =
                CollectionUtil.manageUpdated(pSpecificitiesAdded,
                        pSpecificitiesDeleted, lSpecificitiesOldies);
        
        // Remove from the PC the previous specificities which have been
        // modified
        pPC.getSpecificities().removeAll(lSpecificitiesOldies);
        // Merge the entity into the right transaction
        for (PCSpecificity lPCS : lSpecificitiesUpdated) {
            em.merge(lPCS);
        }
        // Add the modified specificities
        pPC.getSpecificities().addAll(lSpecificitiesUpdated);
        
        // Remove from the database the deleted specificities (merge+delete)
        for (PCSpecificity lPCS : pSpecificitiesDeleted) {
            lPCS = em.merge(lPCS);
            em.remove(lPCS);
        }
        // Remove the link between deleted specificities and PC
        pPC.getSpecificities().removeAll(pSpecificitiesDeleted);
        
        // Add in database the new specificities (with retrieving of the
        // generated ID)
        for (PCSpecificity lPCS : pSpecificitiesAdded) {
            em.persist(lPCS);
        }
        // Link the new specificities to the PC
        pPC.getSpecificities().addAll(pSpecificitiesAdded);
        
        // Update the new dated comments list
        // List containing the previous instances of modified objects
        List<DatedComment> lDatedCommentsOldies = new ArrayList<DatedComment>();
        // List containing the new instances of modified objects
        List<DatedComment> lDatedCommentsUpdated =
                CollectionUtil.manageUpdated(pDatedCommentsAdded,
                        pDatedCommentsDeleted, lDatedCommentsOldies);
        
        // Remove from the PC the previous dated comments which have been
        // modified
        pPC.getDatedComments().removeAll(lDatedCommentsOldies);
        // Merge the entity into the right transaction
        for (DatedComment lDC : lDatedCommentsUpdated) {
            em.merge(lDC);
        }
        // Add the modified dated comments
        pPC.getDatedComments().addAll(lDatedCommentsUpdated);
        
        // Remove from the database the deleted dated comments (merge+delete)
        for (DatedComment lDC : pDatedCommentsDeleted) {
            lDC = em.merge(lDC);
            em.remove(lDC);
        }
        // Remove the link between deleted dated comments and PC
        pPC.getDatedComments().removeAll(pDatedCommentsDeleted);
        
        // Add in database the new dated comments (with retrieving of the
        // generated ID)
        for (DatedComment lDC : pDatedCommentsAdded) {
            em.persist(lDC);
        }
        // Link the new dated comments to the PC
        pPC.getDatedComments().addAll(pDatedCommentsAdded);
        
        // Merge the PC object into the database in order to validate the
        // modifications
        pPC = em.merge(pPC);
        
        if ((pLocation == null && pPC.getLocation() != null)
                || (pLocation != null
                        && !pLocation.equals(pPC.getLocation()))) {
            // Update the location, if modified
            LocationManager lLocationManager = new LocationManager(pPC);
            lLocationManager.moveTo(pLocation, locationManagerBean);
        }
        
        if ((pContainer == null && pPC.getContainer() != null)
                || (pContainer != null
                        && !pContainer.equals(pPC.getContainer()))) {
            // Update the container, if modified
            ContainerManager lContainerManager = new ContainerManager(pPC);
            lContainerManager.linkTo(pContainer, containerManagerBean);
        }
        
        articleBean.updateChildrenUsestate(pPC, pLogin);
        return pPC;
    }
    
    /**
     * Remove the provided PC from the database
     * 
     * @param selected
     *            the PC to delete
     */
    public void remove(PC selected) {
        
        selected = em.merge(selected);
        
        // Remove the PC from the installed software equipment list
        for (Software soft : selected.getSoftwares()) {
            Software softwareToUpdate = softwareBean.findById(soft.getId());
            softwareToUpdate.removeEquipement(selected);
            softwareBean.merge(softwareToUpdate);
        }
        
        if (selected.getLocation() != null) {
            LocationManager lManager = new LocationManager(selected);
            lManager.removeFrom(locationManagerBean);
        }
        
        if (selected.getContainer() != null) {
            ContainerManager lContainerManager = new ContainerManager(selected);
            lContainerManager.unlink(containerManagerBean);
        }
        
        // Remove the PC from database
        articleBean.remove(selected);
    }
    
    /**
     * Check if the provided SN exists
     * 
     * @param sn
     *            the SN for which to check the existence
     * @return true if the provided SN exists, else false
     */
    public boolean exist(String sn) {
        
        if (sn == null) {
            throw new IllegalArgumentException();
        }
        
        long result = 0;
        Query query = em.createNamedQuery(PC.EXIST_PC_BY_SN_QUERY);
        
        query.setParameter("sn", sn);
        
        result = (Long) query.getSingleResult();
        return (result > 0);
    }
    
    /**
     * Check the provided pInCharge existence in database
     * 
     * @param pInCharge
     *            the pInCharge to check
     * @return true if the pInCharge is found, else false
     */
    public boolean existByList(User pInCharge) {
        
        if (pInCharge == null) {
            return false;
        }
        Query query = em.createNamedQuery(PC.BY_INCHARGE_QUERY);
        query.setParameter("inCharge", pInCharge);
        Long lResult = (Long) query.getSingleResult();
        return (lResult > 0);
    }
    
    /**
     * Check the provided name existence in database
     * 
     * @param name
     *            the name to check
     * @return true if the name is found, else false
     */
    public boolean existByName(String name) {
        if (name == null) {
            return false;
        }
        Query query = em.createNamedQuery(PC.COUNT_BY_NAME_QUERY);
        query.setParameter("name", name);
        Long result = (Long) query.getSingleResult();
        
        return (result > 0);
    }
    
    /**
     * Search the PC having the provided id in database
     * 
     * @param id
     *            the PC id to search
     * @return the found PC
     */
    public PC findById(Long id) {
        
        PC pc = em.find(PC.class, id);
        
        // Refresh the entity manager since modifications have been done through
        // other entity managers (locationBean, ...)
        em.refresh(pc);
        
        return pc;
    }
    
    /**
     * Search PC which automatic update is activated
     * 
     * @return the list of found PC
     */
    public List<PC> findPCAutoUpdate() {
        
        TypedQuery<PC> lQuery =
                em.createNamedQuery(PC.PC_AUTO_UPDATE_QUERY, PC.class);
        try {
            List<PC> lFoundPCList = lQuery.getResultList();
            return lFoundPCList;
        }
        catch (NoResultException e) {
            return new ArrayList<PC>();
        }
    }
    
    /**
     * Retrieve the list of all the PC
     * 
     * @return the list of found PC
     */
    public List<PC> findAllPC() {
        
        TypedQuery<PC> lQuery =
                em.createNamedQuery(PC.PC_ALL_QUERY, PC.class);
        try {
            List<PC> lFoundPCList = lQuery.getResultList();
            return lFoundPCList;
        }
        catch (NoResultException e) {
            return new ArrayList<PC>();
        }
    }
    
    /**
     * Search the PC having the provided name
     * 
     * @param name
     *            the name of the searched PC
     * @return the found PC if any, or null
     */
    public PC findPCbyName(String name) {
        if (name == null) {
            return null;
        }
        TypedQuery<PC> query = em.createNamedQuery(PC.BY_NAME_QUERY, PC.class);
        try {
            query.setParameter("name", name);
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            return null;
        }
    }
    
    /**
     * Search the PC having the provided SN
     * 
     * @param sn
     *            the SN to search
     * @return the found PC if any, else null
     */
    public PC findPCbySN(String sn) {
        if (sn == null) {
            return null;
        }
        TypedQuery<PC> query = em.createNamedQuery(PC.BY_SN_QUERY, PC.class);
        try {
            query.setParameter("sn", sn);
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            return null;
        }
    }
    
}
