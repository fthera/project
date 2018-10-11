/*
 * ------------------------------------------------------------------------
 * Class : ValueListController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.entity.valuelist.I18nAttributeValueList;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.application.DBConstants;

/**
 * Controller managing the values lists update
 */
@ManagedBean(name = ValueListController.BEAN_NAME)
@SessionScoped
public class ValueListController extends AbstractController {
    
    /**
     * Enumeration to indicate the current mode : update or creation
     */
    private enum Mode {
        CREATE,
        UPDATE
    }
    
    private Mode mode = Mode.CREATE;
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "valueListController";

    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private DemandBean demandBean;
    
    private String valueListNameSelected;
    
    private SelectItem[] valueListNames;
    
    private AttributeValueList selection;
    
    private DataModel<AttributeValueList> dataValueLists =
            new ListDataModel<AttributeValueList>();
    
    private String defaultValue;
    
    private String frenchValue;
    
    private String colorHexValue;
    
    private Integer monthNumber;
    
    private Integer level;
    
    private String description;
    
    private boolean renderFrenchItem;
    
    private boolean renderColorItem;
    
    private boolean renderMonthNumberItem;
    
    private boolean renderLevelItem;
    
    private boolean renderDescriptionItem;
    
    /**
     * Constructor
     */
    @PostConstruct
    public void init() {
        initGenerateValueListNames();
        valueListNameSelected = valueListNames[0].getLabel();
        updateDataValueLists();
        updateRenderAttrs();
    }
    
    /**
     * Add the new value to its value list
     */
    public void doAdd() {
        
        if (StringUtil.isEmptyOrNull(defaultValue)
                || (StringUtil.isEmptyOrNull(frenchValue)
                        && renderFrenchItem)) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_MODAL_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.VALUE_LIST_MANDATORY_FIELDS));
        }
        else {
            try {
                selection = (AttributeValueList) Class
                        .forName(valueListNameSelected).newInstance();
                if (valueListBean.findAttributeValueListByName(
                        selection.getClass(), defaultValue) != null) {
                    Utils.addFacesMessage(
                            NavigationConstants.VALUE_LIST_MODAL_ERROR_ID,
                            MessageBundle.getMessage(
                                    Constants.ALREADY_EXISTED_VALUE_LIST));
                }
                else {
                    setAttributes();
                    valueListBean.create(selection);
                    updateDataValueLists();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Utils.addFacesMessage(
                        NavigationConstants.VALUE_LIST_MODAL_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }
    
    /**
     * Delete the selected value from its value list
     */
    public void doDelete() {
        
        AttributeValueList lVLA = dataValueLists.getRowData();
        
        try {
            if (lVLA != null) {
                
                // Check that no demand references the selected product type PC
                if (lVLA instanceof ProductTypePC
                        && !demandBean.findDemandsUsingProductType(
                                (ProductTypePC) lVLA).isEmpty()) {
                    Utils.addFacesMessage(
                            NavigationConstants.VALUE_LIST_ERROR_ID,
                            MessageBundle
                                    .getMessage(Constants.PRODUCTTYPEPC_DELETION_USED_DEMAND));
                }
                valueListBean.remove(lVLA);
                updateDataValueLists();
            }
            
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
        
    }
    
    /**
     * Reset the input fields
     */
    public void doReset() {
        defaultValue = null;
        frenchValue = null;
        colorHexValue = null;
        monthNumber = null;
        level = null;
        description = null;
    }
    
    /**
     * Update the attribute value
     */
    public void doUpdate() {
        
        if (StringUtil.isEmptyOrNull(defaultValue)
                || (StringUtil.isEmptyOrNull(frenchValue)
                        && selection instanceof I18nAttributeValueList)) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_MODAL_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.VALUE_LIST_MANDATORY_FIELDS));
        }
        else if (valueListBean.findAttributeValueListByName(
                selection.getClass(), defaultValue) != null) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_MODAL_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.ALREADY_EXISTED_VALUE_LIST));
        }
        else {
            try {
                setAttributes();
                valueListBean.merge(selection);
                updateDataValueLists();
                
            } catch (ValidationException e) {
                Utils.addFacesMessage(NavigationConstants.UPDATE_VALUE_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }
    
    /**
     * @return a boolean indicating if the specificity is in creation mode
     */
    public boolean isCreateMode() {
        switch (mode) {
        case CREATE:
            return true;
        case UPDATE:
        default:
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the specificity is in update mode
     */
    public boolean isUpdateMode() {
        switch (mode) {
        case UPDATE:
            return true;
        case CREATE:
        default:
            return false;
        }
    }
    
    /**
     * @return the colorHexValue
     */
    public String getColorHexValue() {
        String lColorHexValue = colorHexValue;
        if (lColorHexValue != null) {
            lColorHexValue = lColorHexValue.replace("#", "");
        }
        return lColorHexValue;
    }
    
    /**
     * @return the dataValueLists
     */
    public DataModel<AttributeValueList> getDataValueLists() {
        return dataValueLists;
    }
    
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * @return the frenchValue
     */
    public String getFrenchValue() {
        return frenchValue;
    }
    
    /**
     * @return the monthNumber
     */
    public Integer getMonthNumber() {
        return monthNumber;
    }
    
    /**
     * @return the valueListNames
     */
    public SelectItem[] getValueListNames() {
        
        for (SelectItem s : valueListNames) {
            s.setLabel(MessageBundle.getMessage(s.getValue().toString()));
        }
        return valueListNames;
    }
    
    /**
     * @return the valueListNameSelected
     */
    public String getValueListNameSelected() {
        return valueListNameSelected;
    }
    
    private void initGenerateValueListNames() {
        
        valueListNames =
                new SelectItem[ValueListBean.VALUE_LIST_CLASSES.length];
        
        for (int i = 0; i < valueListNames.length; i++) {
            valueListNames[i] =
                    new SelectItem(
                            ValueListBean.VALUE_LIST_CLASSES[i].getName());
        }
        
    }
    
    /**
     * @return the renderFrenchItem
     */
    public boolean isRenderFrenchItem() {
        return renderFrenchItem;
    }
    
    /**
     * @return the renderColorItem
     */
    public boolean isRenderColorItem() {
        return renderColorItem;
    }
    
    /**
     * @return the renderMonthNumberItem
     */
    public boolean isRenderMonthNumberItem() {
        return renderMonthNumberItem;
    }
    
    /**
     * @return the renderLevelItem
     */
    public boolean isRenderLevelItem() {
        return renderLevelItem;
    }
    
    /**
     * @return the renderDescriptionItem
     */
    public boolean isRenderDescriptionItem() {
        return renderDescriptionItem;
    }
    
    /**
     * Update the attribute values list according to the selected list name
     * 
     * @param pEvent
     *            the event sent while the selected name in the list is changed
     */
    public void listNameChanged(ValueChangeEvent pEvent) {
        
        if (pEvent.getNewValue() != null) {
            valueListNameSelected = (String) pEvent.getNewValue();
            updateDataValueLists();
            updateRenderAttrs();
        }
    }
    
    /**
     * Update the render attributes for the creation/modification pop-up and the
     * table
     */
    public void updateRenderAttrs() {
        try {
            AttributeValueList lAVL = (AttributeValueList) Class
                    .forName(valueListNameSelected).newInstance();
            
            renderFrenchItem = lAVL instanceof I18nAttributeValueList;
            renderColorItem = lAVL instanceof AirbusStatus
                    || lAVL instanceof ManufacturerStatus;
            renderMonthNumberItem = lAVL instanceof ConsultPeriod;
            renderLevelItem = lAVL instanceof Role;
            renderDescriptionItem = lAVL instanceof DepartmentInCharge;
        } catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_ERROR_ID,
                    MessageBundle.getMessage(Constants.VALUELIST_ERROR));
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_ERROR_ID,
                    valueListNameSelected);
        }
        
    }
    
    /**
     * Update the modal for updating the selected attribute value
     * 
     * @param pEvent
     *            event sent when clicking the edit icon of a line
     */
    public void prepareUpdate(ActionEvent pEvent) {
        mode = Mode.UPDATE;
        AttributeValueList lAVL =
                (AttributeValueList) dataValueLists.getRowData();
        initAttributes(lAVL);
        
    }
    
    /**
     * Update the modal for creating the an attribute value
     * 
     * @param event
     *            event sent when clicking the add icon of the table
     */
    public void prepareAdd(ActionEvent event) {
        mode = Mode.CREATE;
        initAttributes(null);
    }
    
    /**
     * Initialize all the attributes for the HMI from the given
     * AttributeValueList object.
     * 
     * @param pAVL
     *            the AttributeValueList object being created or updated
     */
    private void initAttributes(AttributeValueList pAVL) {
        defaultValue = null;
        frenchValue = null;
        colorHexValue = null;
        monthNumber = null;
        level = null;
        description = null;
        
        if (pAVL != null) {
            defaultValue = pAVL.getDefaultValue();
            
            if (pAVL instanceof I18nAttributeValueList) {
                frenchValue = ((I18nAttributeValueList) pAVL).getFrenchValue();
            }
            if (pAVL instanceof AirbusStatus) {
                colorHexValue = ((AirbusStatus) pAVL).getColorHex();
            }
            if (pAVL instanceof ManufacturerStatus) {
                colorHexValue = ((ManufacturerStatus) pAVL).getColorHex();
            }
            else if (pAVL instanceof ConsultPeriod) {
                monthNumber = ((ConsultPeriod) pAVL).getMonthNumber();
            }
            else if (pAVL instanceof Role) {
                level = ((Role) pAVL).getLevel();
            }
            if (pAVL instanceof DepartmentInCharge) {
                description = ((DepartmentInCharge) pAVL).getDescription();
            }
        }
    }
    
    /**
     * Set the selected AttributeValueList (selection) attributes from the HMI
     * values.
     */
    private void setAttributes() {
        selection.setDefaultValue(defaultValue);
        
        if (selection instanceof I18nAttributeValueList) {
            ((I18nAttributeValueList) selection).setFrenchValue(frenchValue);
        }
        if (selection instanceof AirbusStatus) {
            ((AirbusStatus) selection).setColorHex(colorHexValue);
        }
        if (selection instanceof ManufacturerStatus) {
            ((ManufacturerStatus) selection).setColorHex(colorHexValue);
        }
        if (selection instanceof ConsultPeriod) {
            ((ConsultPeriod) selection).setMonthNumber(monthNumber);
        }
        if (selection instanceof Role) {
            ((Role) selection).setLevel(level);
        }
        if (selection instanceof DepartmentInCharge) {
            ((DepartmentInCharge) selection).setDescription(description);
        }
    }
    
    /**
     * @param colorHexValue
     *            the colorHexValue to set
     */
    public void setColorHexValue(String pColorHexValue) {
        if (pColorHexValue != null) {
            pColorHexValue = "#" + pColorHexValue;
        }
        this.colorHexValue = pColorHexValue;
    }
    
    /**
     * @param pDataValueLists
     *            the dataValueLists to set
     */
    public void
            setDataValueLists(DataModel<AttributeValueList> pDataValueLists) {
        this.dataValueLists = pDataValueLists;
    }
    
    /**
     * @param pDefaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String pDefaultValue) {
        defaultValue = pDefaultValue;
    }
    
    /**
     * @param pFrenchValue
     *            the frenchValue to set
     */
    public void setFrenchValue(String pFrenchValue) {
        frenchValue = pFrenchValue;
    }
    
    /**
     * @param monthNumber
     *            the monthNumber to set
     */
    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }

	/**
     * @param selection
     *            the selection to set
     */
    public void setSelection(AttributeValueList selection) {
        this.selection = selection;
    }
    
    /**
     * @param valueListNameSelected
     *            the valueListNameSelected to set
     */
    public void setValueListNameSelected(String valueListNameSelected) {
        this.valueListNameSelected = valueListNameSelected;
    }
    
    /**
     * Update the dataValueLists for the selected AttributeValueList class in
     * the interface (valueListNameSelected).
     */
    private <T extends AttributeValueList> void updateDataValueLists() {
        try {
            @SuppressWarnings("unchecked")
            Class<AttributeValueList> lClass =
                    (Class<AttributeValueList>) Class
                            .forName(valueListNameSelected);
            dataValueLists
                    .setWrappedData(valueListBean.findAllValueLists(lClass));
        } catch (ClassNotFoundException e) {
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_ERROR_ID,
                    MessageBundle.getMessage(Constants.VALUELIST_ERROR));
            Utils.addFacesMessage(NavigationConstants.VALUE_LIST_ERROR_ID,
                    valueListNameSelected);
        }
    }
    
    /**
     * @return the level
     */
    public Integer getLevel() {
        return level;
    }
    
    /**
     * @param pLevel
     *            the level to set
     */
    public void setLevel(Integer pLevel) {
        level = pLevel;
    }
    
    /**
     * @return the minimum authorized level for Role
     */
    public Integer getLevelMin() {
        return Role.MIN_LEVEL;
    }
    
    /**
     * @return the maximum authorized level for Role
     */
    public Integer getLevelMax() {
        return Role.MAX_LEVEL;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }
    
    /**
     * Retrieve the length limitation for the default value for the selected
     * AttributeValueList class in the HMI (valueListNameSelected).
     * 
     * @return the length limitation for the default value
     */
    public int getDefaultValueLength() {
        
        DBConstants dbConstants = findBean(DBConstants.class);
        
        if (AircraftProgram.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getAircraftprogramDefaultvalueLength();
        }
        else if (DepartmentInCharge.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getDepartmentinchargeDefaultvalueLength();
        }
        else if (Domain.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getDomainDefaultvalueLength();
        }
        else if (ExternalEntityType.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getExternalentitytypeDefaultvalueLength();
        }
        else if (BusinessAllocationPC.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getBusinessallocationpcDefaultvalueLength();
        }
        else if (BusinessUsagePC.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getBusinessusagepcDefaultvalueLength();
        }
        else if (Network.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getNetworkDefaultvalueLength();
        }
        else if (ProductTypePC.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getProducttypepcDefaultvalueLength();
        }
        else if (ActionObso.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getActionobsoDefaultvalueLength();
        }
        else if (AirbusStatus.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getAirbusstatusDefaultvalueLength();
        }
        else if (ConsultPeriod.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getConsultperiodDefaultvalueLength();
        }
        else if (ManufacturerStatus.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getManufacturerstatusDefaultvalueLength();
        }
        else if (Strategy.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getStrategyDefaultvalueLength();
        }
        else if (Role.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getRoleDefaultvalueLength();
        }
        else {
            return 0;
        }
    }
    
    /**
     * Retrieve the length limitation for the French value for the selected
     * AttributeValueList class in the HMI (valueListNameSelected).
     * 
     * @return the length limitation for the French value
     */
    public int getFrenchValueLength() {
        
        DBConstants dbConstants = findBean(DBConstants.class);
        
        if (ExternalEntityType.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getExternalentitytypeFrenchvalueLength();
        }
        else if (BusinessAllocationPC.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getBusinessallocationpcFrenchvalueLength();
        }
        else if (BusinessUsagePC.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getBusinessusagepcFrenchvalueLength();
        }
        else if (Network.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getNetworkFrenchvalueLength();
        }
        else if (ProductTypePC.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getProducttypepcFrenchvalueLength();
        }
        else if (ActionObso.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getActionobsoFrenchvalueLength();
        }
        else if (AirbusStatus.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getAirbusstatusFrenchvalueLength();
        }
        else if (ConsultPeriod.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getConsultperiodFrenchvalueLength();
        }
        else if (ManufacturerStatus.class.getName()
                .equals(valueListNameSelected)) {
            return dbConstants.getManufacturerstatusFrenchvalueLength();
        }
        else if (Strategy.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getStrategyFrenchvalueLength();
        }
        else if (Role.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getRoleFrenchvalueLength();
        }
        else {
            return 0;
        }
    }
    
    /**
     * Retrieve the length limitation for the description for the selected
     * AttributeValueList class in the HMI (valueListNameSelected).
     * 
     * @return the length limitation for the description
     */
    public int getDescriptionLength() {
        
        DBConstants dbConstants = findBean(DBConstants.class);
        
        if (DepartmentInCharge.class.getName().equals(valueListNameSelected)) {
            return dbConstants.getDepartmentinchargeDescriptionLength();
        }
        else {
            return 0;
        }
    }
    
    /**
     * Returns a ValueListBean object.
     * 
     * @return a ValueListBean object
     */
    public ValueListBean getValueListBean() {
        return valueListBean;
    }
}
