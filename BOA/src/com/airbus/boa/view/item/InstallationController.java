/*
 * ------------------------------------------------------------------------
 * Class : InstallationController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.LocationManagerBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.explorer.view.ExplorerController;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.DocumentController;
import com.airbus.boa.view.LocationController;
import com.airbus.boa.view.ReminderController;

/**
 * Controller managing the installation creation, consultation and modification
 */
@ManagedBean(name = InstallationController.BEAN_NAME)
@ViewScoped
public class InstallationController extends AbstractItemController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "installationController";
    
    private long installationId;
    
    private Installation installation = null;
    
    private Long personInChargeId;
    
    private String personInCharge;
    
    private Long aircraftProgramId;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    public InstallationController() {
        itemPage = NavigationConstants.INSTALLATION_MANAGEMENT_PAGE;
        listPage = NavigationConstants.INSTALLATION_LIST_PAGE;
        resultPage = listPage;
        errorId = NavigationConstants.INST_MGMT_ERROR_ID;
    }

    @Override
    protected void setItemId(Long pId) {
        installationId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return installationId;
    }
    
    @Override
    protected void deleteItem() throws Exception {
        return;
    }
    
    @Override
    protected void createItem() throws Exception {
        
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();
        LocationManager.validateLocation(lLocation);
        
        User lUserInCharge = findUser(personInChargeId);
        installation.setPersonInCharge(lUserInCharge);
        
        AircraftProgram lAircraftProgram =
                findAircraftProgram(aircraftProgramId);
        installation.setAircraftProgram(lAircraftProgram);
        
        installation = locationBean.createInstallation(installation, lLocation);
        installationId = installation.getId();
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(installation);
        
        // Reset Container and Location to new value
        lLocationCtrl.setLocation(lLocation);
    }
    
    @Override
    protected void initItemWithNew() {
        installation = new Installation();
    }
    
    @Override
    protected void initItemFromDatabase() {
        installation = locationBean.findInstallationById(installationId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        if (installation != null) {
            if (installation.getPersonInCharge() != null) {
                setPersonInChargeId(installation.getPersonInCharge().getId());
            }
            else {
                personInChargeId = null;
                personInCharge = null;
            }
            
            if (installation.getAircraftProgram() != null) {
                aircraftProgramId = installation.getAircraftProgram().getId();
            }
            else {
                aircraftProgramId = null;
            }
            
            // location reset
            LocationController lLocationControl =
                    findBean(LocationController.class);
            lLocationControl.setLocation(installation.getLocation());
            lLocationControl.setLocatedItem(installation);
            
            // Document reset
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(installation);

            // Reminder reset
            ReminderController lReminderController =
                    findBean(ReminderController.class);
            lReminderController.setMode(installation);

            // Explorer reset
            ExplorerController controller = findBean(ExplorerController.class);
            controller.setRootNode(installation);
        }
    }
    
    @Override
    public void updateItem() throws Exception {
        LocationController lLocationControl =
                findBean(LocationController.class);
        Location lLocation = lLocationControl.getLocation();
        LocationManager.validateLocation(lLocation);
        
        User lUserInCharge = findUser(personInChargeId);
        installation.setPersonInCharge(lUserInCharge);
        
        AircraftProgram lAircraftProgram =
                findAircraftProgram(aircraftProgramId);
        installation.setAircraftProgram(lAircraftProgram);
        
        installation = locationBean.merge(installation);
        
        LocationManager lManager = new LocationManager(installation);
        lManager.moveTo(lLocation, locationManagerBean);
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(installation);
        
        // Reset Location to new value
        lLocationControl.setLocation(lLocation);
    }
    
    /**
     * @param pAircraftProgramId
     *            the user id
     * @return the found user or null
     */
    private AircraftProgram findAircraftProgram(Long pAircraftProgramId) {
        
        AircraftProgram lResult = null;
        if (pAircraftProgramId != null) {
            lResult = valueListBean.findAttributeValueListById(
                    AircraftProgram.class, pAircraftProgramId);
        }
        return lResult;
    }
    
    /**
     * @param pUserId
     *            the user id
     * @return the found user or null
     */
    private User findUser(Long pUserId) {
        
        User lResult = null;
        if (pUserId != null) {
            lResult = userBean.findUser(pUserId);
        }
        return lResult;
    }
    
    /**
     * Check if the provided installation name is valid for updating
     * the selected installation.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the installation name to valid
     * @throws ValidatorException
     *             when the provided installation name is not valid
     */
    public void validateInstallationName(FacesContext pContext,
            UIComponent pComponent, Object pValue)
            throws ValidatorException {
        
        String lValue = (String) pValue;
        
        if (lValue == null) {
            lValue = "";
        }
        else {
            lValue = lValue.trim();
        }
        
        // Check that entered installation name is not empty
        if (lValue.isEmpty()) {
            
            String lMsg =
                    MessageBundle
                            .getMessage(Constants.INSTALLATION_NAME_MUST_BE_FILLED);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        /*
         * If controller is in creation mode or
         * if controller is in update mode and installation name has changed,
         * check that entered installation name does not already exist
         */
        if (isCreateMode()
                || (isUpdateMode() && (!installation.getName().equals(lValue)))) {
            
            if (locationBean.isInstallationExisting((String) pValue)) {
                
                String lMsg =
                        MessageBundle
                                .getMessage(Constants.INSTALLATION_NAME_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    public void validatePersonInCharge(FacesContext pContext,
            UIComponent pComponent, Object pValue) throws ValidatorException {
        boolean result = false;
        String pPersonInCharge = (String) pValue;
        String[] userDetails = pPersonInCharge.split("\\(|\\)");
        if (userDetails.length > 1) {
            User user = userBean.findUser(userDetails[1]);
            if (user != null) {
                result = true;
            }
        }
        
        if (!result) {
            String lMsg =
                    MessageBundle.getMessage(MessageConstants.UNKNOWN_INCHARGE);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }

    /**
     * @return the aircraftProgramId
     */
    public Long getAircraftProgramId() {
        return aircraftProgramId;
    }
    
    /**
     * @param pAircraftProgramId
     *            the aircraftProgramId to set
     */
    public void setAircraftProgramId(Long pAircraftProgramId) {
        aircraftProgramId = pAircraftProgramId;
    }
    
    /**
     * @return the list of available Aircraft Programs
     */
    public List<SelectItem> getAircraftPrograms() {
        return valueListBean.generateSelectItems(AircraftProgram.class);
    }
    
    /**
     * @return the installation
     */
    public Installation getInstallation() {
        return installation;
    }
    
    /**
     * @return the personInChargeId
     */
    public Long getPersonInChargeId() {
        return personInChargeId;
    }
    
    /**
     * @param pPersonInChargeId
     *            the personInChargeId to set
     */
    public void setPersonInChargeId(Long pPersonInChargeId) {
        
        personInChargeId = pPersonInChargeId;
        personInCharge = userBean.findUser(pPersonInChargeId).getLoginDetails();
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
        String login = pPersonInCharge.split("\\(|\\)")[1];
        personInChargeId = userBean.findUser(login).getId();
        personInCharge = pPersonInCharge;
    }
    
    /**
     * Build a list of values for the Person in charge
     * 
     * @return a list of users
     */
    public List<String> getPersonsInCharge() {
        
        List<User> lUsers = userBean.findUsers();
        
        List<String> lInCharge = new ArrayList<String>();
        for (User lUser : lUsers) {
            lInCharge.add(lUser.getLoginDetails());
        }
        return lInCharge;
    }
    
    /**
     * @return the list of existing Business siglums
     */
    public List<String> getSuggestionBusinessSiglums() {
        
        return locationBean.findAllInstallationBusinessSiglums();
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createInstallationTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoInstallationTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateInstallationTitle");
    }
    
}
