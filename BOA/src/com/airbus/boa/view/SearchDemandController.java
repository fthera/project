/*
 * ------------------------------------------------------------------------
 * Class : SearchDemandController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.richfaces.model.Filter;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.demand.DemandStatus;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.io.demand.IOExcelPurchase;
import com.airbus.boa.io.demand.IOExcelPurchase.PurchaseMode;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.filter.DemandFilterRegex;

/**
 * Controller managing the search of Demands
 */
@ManagedBean(name = SearchDemandController.BEAN_NAME)
@SessionScoped
public class SearchDemandController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchDemandController";

    @EJB
    private DemandBean demandBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private ValueListBean valueListBean;

    private List<Demand> searchDemand = new ArrayList<Demand>();
    
    private DemandFilterRegex filterRegex = new DemandFilterRegex();
    
    private boolean oneUserDemands = false;
    
    private Long selectedPCId;
    
    private List<IOExcelPurchase> ioExcelPurchases =
            new ArrayList<IOExcelPurchase>();
    
    private IOExcelPurchase selectedIOExcelPurchase;
    
    /**
     * map of the demand Id associated with a boolean, true if the demand is
     * selected false otherwise.
     */
    private Map<Long, Boolean> selections = new HashMap<Long, Boolean>();
    
    /**
     * Post-construct method to initialize the filter values
     */
    @PostConstruct
    private void init() {
        List<SelectItem> lList =
                valueListBean.generateSelectItems(ProductTypePC.class);
        List<Long> lProductTypes = new ArrayList<Long>();
        for (SelectItem lItem : lList) {
            lProductTypes.add((Long) lItem.getValue());
        }
        setFilterProductType(lProductTypes);
        setFilterStatus(Arrays.asList(DemandStatus.values()));
    }
    
    /**
     * Created the PC for all the selected demands. Will create PCs only if
     * creation is possible for all demands.
     * 
     * @param pUseState
     *            UseState to which PCs will be created.
     */
    private void createPCs(UseState pUseState) {
        if (isPCCreationPossible()) {
            List<Demand> lDemands = new ArrayList<Demand>();
            for (Map.Entry<Long, Boolean> lEntry : selections.entrySet()) {
                if (lEntry.getValue()) {
                    Demand lDemand = demandBean.findDemandById(lEntry.getKey());
                    try {
                        pcBean.create(lDemand, pUseState);
                    }
                    catch (ValidationException e) {
                        Utils.addFacesMessage(
                                NavigationConstants.DEMAND_LIST_ERROR_ID,
                                "Demand n°: " + lDemand.getDemandNumber() + " "
                                        + e.getMessage());
                        continue;
                    }
                    PC lPC =
                            pcBean.findPCbySN(Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX
                                    + lDemand.getDemandNumber());
                    lDemand.setAllocatedPC(lPC);
                    demandBean.merge(lDemand);
                    lDemands.add(lDemand);
                }
            }
            try {
                Workbook lWorkbook = new SXSSFWorkbook(100);
                IOExcelPurchase lExport = null;
                switch (pUseState) {
                case OnExternalPurchase:
                    lExport =
                            new IOExcelPurchase(lWorkbook,
                                    PurchaseMode.EXTERNAL_PURCHASE);
                    break;
                case OnInternalPurchase:
                    lExport =
                            new IOExcelPurchase(lWorkbook,
                                    PurchaseMode.INTERNAL_PURCHASE);
                    break;
                default:
                    break;
                }
                if (lExport != null) {
                    lExport.writeSheetContent(lDemands);
                    lExport.applyStyles();
                    ioExcelPurchases.add(lExport);
                }
            }
            catch (ExportException e) {
                Utils.addFacesMessage(NavigationConstants.DEMAND_LIST_ERROR_ID,
                        e.getMessage());
            }
        }
    }
    
    /**
     * Created the PC for all the selected demands. Will create PCs only if
     * creation is possible for all demands. Will create the PCs with state on
     * External Purchase.
     */
    public void doCreatePCsExternalPurchase() {
        createPCs(UseState.OnExternalPurchase);
        if (oneUserDemands) {
            doListUserDemands();
        }
        else {
            doListAllDemands();
        }
    }
    
    /**
     * Created the PC for all the selected demands. Will create PCs only if
     * creation is possible for all demands. Will create the PCs with state on
     * Internal Purchase.
     */
    public void doCreatePCsInternalPurchase() {
        createPCs(UseState.OnInternalPurchase);
        if (oneUserDemands) {
            doListUserDemands();
        }
        else {
            doListAllDemands();
        }
    }
    
    /**
     * Download the selected Excel purchase file
     */
    public void doDownloadExcelPurchaseFile() {
        
        if (selectedIOExcelPurchase != null) {
            
            download(selectedIOExcelPurchase,
                    selectedIOExcelPurchase.getFileName() + ".xlsx",
                    ExportController.MIMETYPE_XLSX);
        }
    }
    
    /**
     * Prepare the user's demands list page
     */
    public void doListUserDemands() {
        
        LogInController lLogInController = findBean(LogInController.class);
        User lUser = lLogInController.getUserLogged();
        
        if (lUser != null) {
            searchDemand = demandBean.findDemandsByIssuer(lUser);
        }
        else {
            searchDemand = new ArrayList<Demand>();
        }
        // Initialize map with all user's demands not selected.
        selections.clear();
        for (Demand lDemand : searchDemand) {
            selections.put(lDemand.getId(), false);
        }
        oneUserDemands = true;
    }
    
    /**
     * Prepare the demands list page for their management
     */
    public void doListAllDemands() {
        searchDemand = demandBean.findAllDemands();
        oneUserDemands = false;
        // Initialize map with all demands not selected.
        selections.clear();
        for (Demand lDemand : searchDemand) {
            selections.put(lDemand.getId(), false);
        }
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        List<SelectItem> lList =
                valueListBean.generateSelectItems(ProductTypePC.class);
        List<Long> lProductTypes = new ArrayList<Long>();
        for (SelectItem lItem : lList) {
            lProductTypes.add((Long) lItem.getValue());
        }
        setFilterProductType(lProductTypes);
        setFilterStatus(Arrays.asList(DemandStatus.values()));
        filterRegex.resetFilters();
    }
    
    /**
     * Return the filter for demand product types
     * 
     * @return the requested filter object
     */    
    public Filter<?> getProductTypeFilter() {
        return new Filter<Demand>() {
            public boolean accept(Demand item) {
            	return filterRegex.filterMethodProductType(item);
            }
        };
    }
    
    /**
     * Apply the filter on provided object
     * 
     * @param pCurrent
     *            the object on which to apply filter
     * @return a boolean indicating if the object satisfies the filter
     */
    public Boolean filterMethodRegex(Object pCurrent) {
        return filterRegex.filterMethodRegex((Demand) pCurrent);
    }
    
    /**
     * Return the filter for demands
     * 
     * @return the requested filter object
     */    
    public Filter<?> getDemandFilter() {
        return new Filter<Demand>() {
            public boolean accept(Demand item) {
            	return filterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * Return the filter for demand statuses
     * 
     * @return the requested filter object
     */    
    public Filter<?> getStatusFilter() {
        return new Filter<Demand>() {
            public boolean accept(Demand item) {
            	return filterRegex.filterMethodStatus(item);
            }
        };
    }
    
    /**
     * Remove the selected Excel purchase file from the list
     */
    public void removeExcelPurchaseFile() {
        if (selectedIOExcelPurchase != null) {
            ioExcelPurchases.remove(selectedIOExcelPurchase);
        }
    }
    
    /**
     * Builds content of the title of the fieldset containing actions on
     * selected demands.
     * 
     * @return String of the generated title.
     */
    public String getActionTitle() {
        return MessageBundle.getMessageResourceDefault(
                Constants.DEMAND_ACTION_TITLE,
                new Object[] { getNbOfSelectedDemands() });
    }
    
    /**
     * @return the containerTypes select items
     */
    public SelectItem[] getContainerTypes() {
        
        int lSize = ContainerType.values().length;
        SelectItem[] lContainerTypes = new SelectItem[lSize];
        ContainerType[] lValues = ContainerType.values();
        for (int i = 0; i < lSize; i++) {
            lContainerTypes[i] =
                    new SelectItem(lValues[i], lValues[i].toString());
        }
        return lContainerTypes;
    }
    
    /**
     * Update the countFilteredDemand with the number of demands filtered
     * 
     * @return the countFilteredDemand
     */
    public Integer getCountFilteredDemand() {
        return filterRegex.countFiltered(searchDemand);
    }
    
    /**
     * @return the filterProductType
     */
    public List<Long> getFilterProductType() {
        return filterRegex.getFilterProductType();
    }
    
    /**
     * @param pFilterProductType
     *            the filterProductType to set
     */
    public void setFilterProductType(List<Long> pFilterProductType) {
        filterRegex.setFilterProductType(pFilterProductType);
    }
    
    /**
     * @return the filterStatus
     */
    public List<DemandStatus> getFilterStatus() {
        return filterRegex.getFilterStatus();
    }
    
    /**
     * @param filterStatus
     *            the filterStatus to set
     */
    public void setFilterStatus(List<DemandStatus> filterStatus) {
        filterRegex.setFilterStatus(filterStatus);
    }
    
    /**
     * @return the demandFilterValues
     */
    public Map<String, String> getFilterValues() {
        return filterRegex.getFilterValues();
    }
    
    /**
     * @param pDemandFilterValues
     *            the demandFilterValues to set
     */
    public void setFilterValues(Map<String, String> pDemandFilterValues) {
        filterRegex.setFilterValues(pDemandFilterValues);
    }
    
    /**
     * @return the ioExcelPurchases
     */
    public List<IOExcelPurchase> getIoExcelPurchases() {
        return ioExcelPurchases;
    }
    
    /**
     * @return the number of selected demands
     */
    public int getNbOfSelectedDemands() {
        int lNumber = 0;
        for (Map.Entry<Long, Boolean> lEntry : selections.entrySet()) {
            if (lEntry.getValue()) {
                lNumber = lNumber + 1;
            }
        }
        return lNumber;
    }
    
    /**
     * Return a boolean indicating if the page lists only one user's demands
     * 
     * @return the oneUserDemands
     */
    public boolean isOneUserDemands() {
        return oneUserDemands;
    }
    
    /**
     * Computes the messages displayed on the confirmation message of the create
     * PC from demands action.
     * 
     * @return The message to be displayed in the pop-up
     */
    public String getPCCreationMsg() {
        String lMsg = "";
        for (Map.Entry<Long, Boolean> lEntry : selections.entrySet()) {
            if (lEntry.getValue()) {
                Demand lDemand = demandBean.findDemandById(lEntry.getKey());
                String lNewLine =
                        MessageBundle.getMessage(Constants.DEMAND_NUMBER)
                                + lDemand.getDemandNumber() + "; ";
                lMsg = lMsg + lNewLine;
            }
        }
        return lMsg;
    }
    
    /**
     * Checks if PC creation from all selected demands is possible: Impossible
     * if demand is
     * null or unconfirmed or a PC is already allocated to it. Will also display
     * encountered errors in the /error/errorMessages.xhtml include of the page.
     * 
     * @return true if creation for all demands is possible. false otherwise.
     */
    private boolean isPCCreationPossible() {
        
        for (Map.Entry<Long, Boolean> lEntry : selections.entrySet()) {
            if (lEntry.getValue()) {
                Demand lDemand = demandBean.findDemandById(lEntry.getKey());
                if (lDemand != null) {
                    if (lDemand.getStatus() != DemandStatus.Confirmed) {
                        Utils.addFacesMessage(
                                NavigationConstants.DEMAND_LIST_ERROR_ID,
                                "Demand n°: "
                                        + lDemand.getDemandNumber()
                                        + " "
                                        + MessageBundle
                                                .getMessage(Constants.DEMAND_NOT_CONFIRMED));
                        return false;
                    }
                    if (lDemand.getAllocatedPC() != null) {
                        Utils.addFacesMessage(
                                NavigationConstants.DEMAND_LIST_ERROR_ID,
                                "Demand n°: "
                                        + lDemand.getDemandNumber()
                                        + " "
                                        + MessageBundle
                                                .getMessage(Constants.ALREADY_A_PC_ALLOCATED_TO_DEMAND));
                        return false;
                    }
                }
                else {
                    Utils.addFacesMessage(
                            NavigationConstants.DEMAND_LIST_ERROR_ID,
                            MessageBundle
                            .getMessage(Constants.DEMAND_DOES_NOT_EXIST));
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * @return the product types
     */
    public List<SelectItem> getProductTypes() {
        return valueListBean.generateSelectItems(ProductTypePC.class);
    }
    
    /**
     * @return the searchDemand
     */
    public List<Demand> getSearchDemand() {
        return searchDemand;
    }
    
    /**
     * @return the selectedIOExcelPurchase
     */
    public IOExcelPurchase getSelectedIOExcelPurchase() {
        return selectedIOExcelPurchase;
    }
    
    /**
     * @param pSelectedIOExcelPurchase
     *            the selectedIOExcelPurchase to set
     */
    public void setSelectedIOExcelPurchase(
            IOExcelPurchase pSelectedIOExcelPurchase) {
        selectedIOExcelPurchase = pSelectedIOExcelPurchase;
    }
    
    /**
     * @return the selectedPCId
     */
    public Long getSelectedPCId() {
        return selectedPCId;
    }
    
    /**
     * @param selectedPCId
     *            the selectedPCId to set
     */
    public void setSelectedPCId(Long selectedPCId) {
        this.selectedPCId = selectedPCId;
    }
    
    /**
     * Returns a map of the demand Id associated with a boolean, true if the
     * demand is selected false otherwise.
     * 
     * @return the selections
     */
    public Map<Long, Boolean> getSelections() {
        return selections;
    }
    
    /**
     * Sets a map of the demand Id associated with a boolean, true is the
     * demand is selected false otherwise.
     * 
     * @param pSelections
     *            the selections to set
     */
    public void setSelections(Map<Long, Boolean> pSelections) {
        selections = pSelections;
    }
    
    /**
     * Update the status values list
     * 
     * @return the status values list
     */
    public SelectItem[] getStatusValues() {
        int lLength = DemandStatus.values().length;
        SelectItem[] lStatusValues = new SelectItem[lLength];
        DemandStatus[] lValues = DemandStatus.values();
        for (int i = 0; i < lLength; i++) {
            
            lStatusValues[i] =
                    new SelectItem(lValues[i], lValues[i].toString());
        }
        
        return lStatusValues;
    }
    
    /**
     * Check if the Demand Status column is filtered
     * 
     * @return true if the column is filtered, false otherwise
     */
    public boolean getDemandStatusFiltered() {
        if (filterRegex.getFilterStatus()
                .size() < DemandStatus.values().length) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if the Product Type column is filtered
     * 
     * @return true if the column is filtered, false otherwise
     */
    public boolean getProductTypeFiltered() {
        if (filterRegex.getFilterProductType().size() < valueListBean
                .generateSelectItems(ProductTypePC.class).size()) {
            return true;
        }
        return false;
    }
    
}
