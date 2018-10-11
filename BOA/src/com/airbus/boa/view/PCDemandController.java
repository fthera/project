/*
 * ------------------------------------------------------------------------
 * Class : PCDemandController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.query.ArticleQueryBuilder;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AvailabilityStatus;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.user.User;

/**
 * Controller managing the search of available PC and the allocation of a PC to
 * a demand
 */
@ManagedBean(name = PCDemandController.BEAN_NAME)
@SessionScoped
public class PCDemandController extends AbstractController implements
        Serializable {
    
    private enum Mode {
        CONSULTATION_AVAILABLE_PC,
        ALLOCATION_OF_PC_TO_DEMAND,
        MANAGEMENT_OF_PC_IN_CHARGE
    }
    
    private static final long serialVersionUID = 8019442362483040785L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "PCDemandController";

    @EJB
    private PCBean pcBean;
    
    private Mode mode = Mode.CONSULTATION_AVAILABLE_PC;
    
    private Long pcToFreeId;
    
    private Long pcToEditId;
    
    private Date pcToFreeAvailabilityDate = new Date();
    
    private Date pcToEditAvailabilityDate;
    
    /**
     * The availability date to the provided one. <br>
     * <b><i>pcToEditId</i></b> must be initialized before calling.
     */
    public void doEditPC() {
        
        // Update the PC in database
        PC lPCToEdit = pcBean.findById(pcToEditId);
        lPCToEdit.setAvailabilityDate(pcToEditAvailabilityDate);
        pcBean.merge(lPCToEdit, new ArrayList<Action>());
        
        SearchController lSearchController = findBean(SearchController.class);
        lSearchController.refreshDataModel();
    }
    
    /**
     * Set the PC availability status to Free and the availability date to the
     * provided one. <br>
     * <b><i>pcToFreeId</i></b> must be initialized before calling.
     */
    public void doFreePC() {
        
        // Update the PC in database
        PC lPCToFree = pcBean.findById(pcToFreeId);
        
        lPCToFree.setAvailabilityStatus(AvailabilityStatus.Free);
        lPCToFree.setAvailabilityDate(pcToFreeAvailabilityDate);
        
        pcBean.merge(lPCToFree, new ArrayList<Action>());
        
        pcToFreeAvailabilityDate = new Date();
        
        SearchController lSearchController = findBean(SearchController.class);
        lSearchController.refreshDataModel();
    }
    
    /**
     * Retrieve the available PC list
     */
    public void doListAvailablePC() {
        mode = Mode.CONSULTATION_AVAILABLE_PC;
        init();
    }
    
    /**
     * Retrieve the list of PC the logged user is in charge of
     */
    public void doListPCManagement() {
        mode = Mode.MANAGEMENT_OF_PC_IN_CHARGE;
        init();
    }
    
    /**
     * Retrieve the list of PC the logged user is in charge of
     */
    public void doListPCForAllocation() {
        mode = Mode.ALLOCATION_OF_PC_TO_DEMAND;
        init();
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        
        SearchController lSearchController = findBean(SearchController.class);
        lSearchController.doResetFilters();
    }
    
    /**
     * Set the PC availability status to InUse and the clears the availability
     * date
     * <b><i>pcToFreeId</i></b> must be initialized before calling.
     */
    public void doUnFreePC() {
        // Update the PC in database
        PC lPCToUnFree = pcBean.findById(pcToFreeId);
        
        lPCToUnFree.setAvailabilityStatus(AvailabilityStatus.InUse);
        lPCToUnFree.setAvailabilityDate(null);
        
        pcBean.merge(lPCToUnFree, new ArrayList<Action>());
        
        pcToFreeAvailabilityDate = new Date();
        
        SearchController lSearchController = findBean(SearchController.class);
        lSearchController.refreshDataModel();
    }
    
    private void init() {
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, PC.class.getSimpleName());
        
        List<String> lAutoConditions = new ArrayList<String>();
        Map<String, Object> lAutoFilter = new HashMap<String, Object>();
        
        switch (mode) {
        case CONSULTATION_AVAILABLE_PC:
        case ALLOCATION_OF_PC_TO_DEMAND:
            lFilter.put(ArticleQueryBuilder.AVAILABILITY_STATUS,
                    PC.AvailabilityStatus.valuesNotInUse());
            break;
        
        case MANAGEMENT_OF_PC_IN_CHARGE:
            LogInController lLogInController = findBean(LogInController.class);
            User lUser = lLogInController.getUserLogged();
            
            lFilter.put(ArticleQueryBuilder.IN_CHARGE_USER, lUser);
            break;
        
        default:
            // This case should not append
        }
        findBean(SearchController.class).doDisplayPCQuery(lFilter,
                lAutoConditions, lAutoFilter);
    }
    
    /**
     * @return a boolean indicating if the Actions column is to be displayed
     */
    public boolean isActionAvailable() {
        switch (mode) {
        case ALLOCATION_OF_PC_TO_DEMAND:
        case MANAGEMENT_OF_PC_IN_CHARGE:
            return true;
        case CONSULTATION_AVAILABLE_PC:
        default:
            return false;
        }
    }
    
    /**
     * @return true if the page is the page for allocating a PC to a demand
     */
    public boolean isAllocationToDemandMode() {
        return mode == Mode.ALLOCATION_OF_PC_TO_DEMAND;
    }
    
    /**
     * @return a boolean indicating if the End of support column is to be
     *         displayed
     */
    public boolean isEndOfSupportToDisplay() {
        switch (mode) {
        case MANAGEMENT_OF_PC_IN_CHARGE:
            return true;
        case ALLOCATION_OF_PC_TO_DEMAND:
        case CONSULTATION_AVAILABLE_PC:
        default:
            return false;
        }
    }
    
    /**
     * @return true if the page is the page for managing the list of PC the user
     *         is in charge of
     */
    public boolean isPCManagementMode() {
        return mode == Mode.MANAGEMENT_OF_PC_IN_CHARGE;
    }
    
    /**
     * @return the pcToEditAvailabilityDate
     */
    public Date getPcToEditAvailabilityDate() {
        pcToEditAvailabilityDate = null;
        if (pcToEditId != null) {
            PC lPCToEdit = pcBean.findById(pcToEditId);
            pcToEditAvailabilityDate = lPCToEdit.getAvailabilityDate();
        }
        return pcToEditAvailabilityDate;
    }
    
    /**
     * @param pPcToEditAvailabilityDate
     *            the pcToEditAvailabilityDate to set
     */
    public void setPcToEditAvailabilityDate(Date pPcToEditAvailabilityDate) {
        pcToEditAvailabilityDate = pPcToEditAvailabilityDate;
    }
    
    /**
     * @param pPcToEditId
     *            the pcToEditId to set
     */
    public void setPcToEditId(Long pPcToEditId) {
        pcToEditId = pPcToEditId;
    }
    
    /**
     * @return the pcToFreeAvailabilityDate
     */
    public Date getPcToFreeAvailabilityDate() {
        return pcToFreeAvailabilityDate;
    }
    
    /**
     * @param pPcToFreeAvailabilityDate
     *            the pcToFreeAvailabilityDate to set
     */
    public void setPcToFreeAvailabilityDate(Date pPcToFreeAvailabilityDate) {
        pcToFreeAvailabilityDate = pPcToFreeAvailabilityDate;
    }
    
    /**
     * @param pPcToFreeId
     *            the pcToFreeId to set
     */
    public void setPcToFreeId(Long pPcToFreeId) {
        pcToFreeId = pPcToFreeId;
    }
    
    /**
     * @return a boolean indicating if the Person in charge column is to be
     *         displayed
     */
    public boolean isPersonInChargeToDisplay() {
        switch (mode) {
        case MANAGEMENT_OF_PC_IN_CHARGE:
            return false;
        case ALLOCATION_OF_PC_TO_DEMAND:
        case CONSULTATION_AVAILABLE_PC:
        default:
            return true;
        }
    }
    
}
