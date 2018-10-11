/*
 * ------------------------------------------------------------------------
 * Class : LocationController
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerItem;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Controller managing the location
 */
@ManagedBean(name = LocationController.BEAN_NAME)
@ViewScoped
public class LocationController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 4966542663322976893L;
    
    /** Controller name */
    public static final String BEAN_NAME = "locationController";
    
    private static final String PLACE_LABEL = "Place";
    private static final String EXTERNAL_ENTITY_LABEL = "ExternalEntity";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    private LocatedItem locatedItem;
    private LocatedType locatedType;
    
    /** Location before any modification */
    private Location firstLocation;
    
    /** Current location of the item */
    private Location location;
    
    /** Temporary location used in modal */
    private Location tempLocation;
    
    /*
     * attributes used in modal for changing location
     */
    
    private String selectedClass = null;
    
    private Long selectedBuildingId;
    private String inputSearchField;
    private Long selectedExternalEntityTypeId;
    
    private List<Place> foundPlaces = new ArrayList<Place>();
    
    private List<ExternalEntity> foundExternalEntities =
            new ArrayList<ExternalEntity>();
    
    /**
     * Constructor
     */
    public LocationController() {
        
        super();
    }
    
    /**
     * Reset the temporary location using the current one (on main page)
     */
    public void doCancel() {
        resetSearchAttributes();
        if (location != null) {
            tempLocation = new Location(location);
        }
        else {
            tempLocation = null;
        }
    }
    
    /**
     * Update the found elements (depending on the selectedClass) by searching
     * articles, installations or tools satisfying the findCriteria
     */
    public void doFind() {
        updateSearchLists();
    }
    
    /**
     * Reset attributes for search
     */
    public void doResetSearchField() {
        resetSearchAttributes();
    }
    
    /**
     * Reset the temporary location to no location (null)
     */
    public void doResetTempExternalLocation() {
        if (tempLocation != null) {
            try {
                tempLocation =
                        LocationManager.updateExternalLocationItem(
                                tempLocation, null, locatedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    /**
     * Store the temporary location as the current one (on main page)
     */
    public void doValidate() {
        resetSearchAttributes();
        if (tempLocation != null) {
            location = new Location(tempLocation);
        }
        else {
            location = null;
        }
    }
    
    /**
     * Update the location according to the selected external entity
     * 
     * @param event
     *            the event sent while the selected name in the list is changed
     */
    public void externalEntityChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            ExternalEntity lExternalEntity =
                    (ExternalEntity) event.getNewValue();
            
            try {
                tempLocation =
                        LocationManager.updateExternalLocationItem(
                                tempLocation, lExternalEntity, locatedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    /**
     * Update the location according to the inherited value
     * 
     * @param event
     *            the event sent while the inherited value is changed
     */
    public void inheritedChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            
            Boolean lInherited = (Boolean) event.getNewValue();
            
            if (lInherited != null) {
                ContainerController lContainerController =
                        findBean(ContainerController.class);
                Container lContainer = lContainerController.getContainer();
                
                try {
                    tempLocation =
                            LocationManager.updateInherited(tempLocation,
                                    lInherited, lContainer, locatedType);
                }
                catch (LocalizationException e) {
                    Utils.addFacesMessage(
                            NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                            MessageBundle.getMessage(
                                    Constants.LOCALIZATION_UNKNOWN_ERROR));
                }
            }
        }
    }
    
    public void setLocatedItem(LocatedItem pLocatedItem) {
        locatedItem = pLocatedItem;
        locatedType = locatedItem.getLocatedType();
        
        // Initialize current location and temporary location for modal
        LocationManager lLocationManager = new LocationManager(locatedItem);
        firstLocation = lLocationManager.getLocation();
        if (firstLocation != null) {
            location = new Location(firstLocation);
            tempLocation = new Location(firstLocation);
        }
        else {
            location = null;
            tempLocation = null;
        }
        updateClasses();

    }
    
    /**
     * Update the location according to the selected place
     * 
     * @param event
     *            the event sent while the selected name in the list is changed
     */
    public void placeChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            
            Place lPlace = (Place) event.getNewValue();
            
            try {
                tempLocation =
                        LocationManager.updateLocationItem(tempLocation,
                                lPlace, locatedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    private void resetSearchAttributes() {
        
        selectedBuildingId = null;
        inputSearchField = null;
        selectedExternalEntityTypeId = null;
        
        updateSearchLists();
    }
    
    /**
     * Update the lists of classes
     */
    private void updateClasses() {
        // Select the default value for selected class
        selectedClass = PLACE_LABEL;
        updateSearchLists();
    }
    
    /**
     * Update the location according to the provided container, if needed
     * 
     * @param pContainer
     *            the new container of the item
     * @param pUpdateLocationAction
     *            integer indicating the action to do on the location
     */
    public void updateContainer(Container pContainer,
            int pUpdateLocationAction) {
        
        try {
            location =
                    LocationManager.updateLocationWithContainer(location,
                            pContainer, pUpdateLocationAction, locatedType);
            if (location != null) {
                tempLocation = new Location(location);
            }
            else {
                tempLocation = null;
            }
        }
        catch (LocalizationException e) {
            // do not update location in case of error
        }
    }
    
    private void updateSearchListExternalEntities() {
        
        List<ExternalEntity> lResults = new ArrayList<ExternalEntity>();
        
        if (selectedExternalEntityTypeId == null) {
            if (StringUtil.isEmptyOrNull(inputSearchField)) {
                // no criteria is provided
                lResults.addAll(locationBean.findAllExternalEntities());
            }
            else {
                lResults.addAll(locationBean
                        .findAllExternalEntitiesByName(inputSearchField));
            }
        }
        else {
            ExternalEntityType lExternalEntityType =
                    valueListBean.findAttributeValueListById(
                            ExternalEntityType.class,
                            selectedExternalEntityTypeId);
            if (StringUtil.isEmptyOrNull(inputSearchField)) {
                lResults.addAll(locationBean
                        .findAllExternalEntitiesByType(lExternalEntityType));
            }
            else {
                lResults.addAll(locationBean
                        .findAllExternalEntitiesByNameAndType(inputSearchField,
                                lExternalEntityType));
            }
        }
        
        int lMaxLimit = getQUERY_MAX_RESULTS();
        int lNbFound = lResults.size();
        
        if ((lNbFound > lMaxLimit) && (lMaxLimit > 0)) {
            
            foundExternalEntities = lResults.subList(0, lMaxLimit);
            
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    lNbFound, lMaxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHANGE_LOCATION_ERROR_ID, lMsg);
        }
        else if (lNbFound == 0) {
            foundExternalEntities.clear();
            Utils.addFacesMessage(NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else {
            foundExternalEntities = lResults;
        }
    }
    
    private void updateSearchListPlaces() {
        
        List<Place> lResults = new ArrayList<Place>();
        
        if (StringUtil.isEmptyOrNull(inputSearchField)) {
            if (selectedBuildingId == null) {
                // no criteria is provided
                lResults.addAll(locationBean.findAllPlace());
            }
            else {
                // only a building is selected
                lResults.addAll(locationBean
                        .findPlacesByBuildingFk(selectedBuildingId));
            }
        }
        else {
            if (selectedBuildingId == null) {
                // only a text criteria is provided
                lResults.addAll(locationBean
                        .findAllPlacesByName(inputSearchField));
            }
            else {
                // a building is selected and a text criteria is provided
                lResults.addAll(locationBean.findPlacesByBuildingIdAndName(
                        selectedBuildingId, inputSearchField));
            }
        }
        
        // Remove not available places
        List<Place> lResultsCopy = new ArrayList<Place>(lResults);
        lResults.clear();
        for (Place lPlace : lResultsCopy) {
            if (LocationManager.isLocationAvailable(lPlace, locatedType)) {
                lResults.add(lPlace);
            }
        }
        
        int lMaxLimit = getQUERY_MAX_RESULTS();
        int lNbFound = lResults.size();
        
        if ((lNbFound > lMaxLimit) && (lMaxLimit > 0)) {
            
            foundPlaces = lResults.subList(0, lMaxLimit);
            
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    lNbFound, lMaxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHANGE_LOCATION_ERROR_ID, lMsg);
        }
        else if (lNbFound == 0) {
            foundPlaces.clear();
            Utils.addFacesMessage(NavigationConstants.CHANGE_LOCATION_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else {
            foundPlaces = lResults;
        }
    }
    
    private void updateSearchLists() {
        
        if (isPlaceMode()) {
            updateSearchListPlaces();
        }
        else if (isExternalEntityMode()) {
            updateSearchListExternalEntities();
        }
    }
    
    /**
     * @return a boolean indicating if the graphical elements for searching an
     *         external entity shall be displayed
     */
    public boolean isExternalEntityMode() {
        
        return selectedClass != null
                && selectedClass.equals(EXTERNAL_ENTITY_LABEL);
    }
    
    /**
     * @return a boolean indicating if the graphical elements for searching a
     *         place shall be displayed
     */
    public boolean isPlaceMode() {
        
        return selectedClass != null && selectedClass.equals(PLACE_LABEL);
    }
    
    /**
     * @return true if the external location can be defined, else false
     */
    public boolean isExternalLocationPossible() {
        if (locatedType != null) {
            return LocationManager.isExternalLocationAvailable(locatedType);
        }
        else {
            return false;
        }
    }
    
    /**
     * @return true if the inherited location is available, else false
     */
    public boolean isInheritedLocationAvailable() {
        
        if (locatedType != null
                && LocationManager.isInheritedLocationAvailable(locatedType)) {
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            return lContainerController.getContainer() != null;
        }
        else {
            return false;
        }
    }
    
    /**
     * @return the list of buildings
     */
    public List<SelectItem> getBuildings() {
        
        List<Building> lBuildings = locationBean.findAllBuilding();
        List<SelectItem> lSelectBuildings = new ArrayList<SelectItem>();
        
        for (Building lBuilding : lBuildings) {
            lSelectBuildings.add(new SelectItem(lBuilding.getId(), lBuilding
                    .getName()));
        }
        return lSelectBuildings;
    }
    
    /**
     * @return the list of available external types
     */
    public List<SelectItem> getExternalTypes() {
        return valueListBean.generateSelectItems(ExternalEntityType.class);
    }
    
    /**
     * @return the foundExternalEntities
     */
    public List<ExternalEntity> getFoundExternalEntities() {
        return foundExternalEntities;
    }
    
    /**
     * @return the foundPlaces
     */
    public List<Place> getFoundPlaces() {
        return foundPlaces;
    }
    
    /**
     * @return the inputSearchField
     */
    public String getInputSearchField() {
        return inputSearchField;
    }
    
    /**
     * @param pInputSearchField
     *            the inputSearchField to set
     */
    public void setInputSearchField(String pInputSearchField) {
        inputSearchField = pInputSearchField;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }
    
    /**
     * @param pLocation
     *            the location to set
     */
    public void setLocation(Location pLocation) {
        location = pLocation;
        if (location != null) {
            firstLocation = new Location(location);
            tempLocation = new Location(location);
        }
        else {
            firstLocation = null;
            tempLocation = null;
        }
    }
    
    /**
     * @return the style class for displaying the location name
     */
    public String getLocationStyleClass() {
        
        if (location == null) {
            return "";
        }
        
        String lStyleClass = "";
        
        if (location.isExternalLocated()) {
            lStyleClass = "mask";
        }
        
        if (location.isInherited()) {
            if (lStyleClass.isEmpty()) {
                return "inherited";
            }
            else {
                lStyleClass = lStyleClass + " inherited";
            }
        }
        return lStyleClass;
    }
    
    /**
     * @return the QUERY_MAX_RESULTS init parameter, else 0
     */
    private int getQUERY_MAX_RESULTS() {
        
        String lLimit = getInitParameter("QUERY_MAX_RESULTS");
        if (lLimit == null) {
            return 0;
        }
        try {
            return Integer.valueOf(lLimit);
        }
        catch (NumberFormatException e) {
            log.warning("Context variable not correctly defined : QUERY_MAX_RESULTS must be a positive integer");
            return 0;
        }
    }
    
    /**
     * @return a boolean indicating if the question should be asked to user to
     *         know if the location should be replaced by the parent location.
     */
    public boolean isReplaceLocationQuestionToBeAsked() {
        return LocationManager.isLocationReplaceAvailable(location);
    }
    
    /**
     * @return the selectedBuildingId
     */
    public Long getSelectedBuildingId() {
        return selectedBuildingId;
    }
    
    /**
     * @param pSelectedBuildingId
     *            the selectedBuildingId to set
     */
    public void setSelectedBuildingId(Long pSelectedBuildingId) {
        selectedBuildingId = pSelectedBuildingId;
        // Update list of locations or external locations
        updateSearchLists();
    }
    
    /**
     * @return the selectedClass
     */
    public String getSelectedClass() {
        return selectedClass;
    }
    
    /**
     * @param pSelectedClass
     *            the selectedClass to set
     */
    public void setSelectedClass(String pSelectedClass) {
        selectedClass = pSelectedClass;
        // update the list of locations or external locations
        updateSearchLists();
    }
    
    /**
     * @return the selectedExternalEntityTypeId
     */
    public Long getSelectedExternalEntityTypeId() {
        return selectedExternalEntityTypeId;
    }
    
    /**
     * @param pSelectedExternalEntityTypeId
     *            the selectedExternalEntityTypeId to set
     */
    public void setSelectedExternalEntityTypeId(
            Long pSelectedExternalEntityTypeId) {
        selectedExternalEntityTypeId = pSelectedExternalEntityTypeId;
    }
    
    /**
     * @return a boolean indicating if the precision should be displayed
     */
    public boolean isShowPrecision() {
        
        if (location != null) {
            return location.isPrecisionAvailable();
        }
        else {
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the temporary precision should be
     *         displayed
     */
    public boolean isShowTempPrecision() {
        
        if (tempLocation != null) {
            return tempLocation.isPrecisionAvailable();
        }
        else {
            return false;
        }
    }
    
    /**
     * @return the tempLocation
     */
    public Location getTempLocation() {
        return tempLocation;
    }
    
    /**
     * @return the tempLocation inherited value, or false
     */
    public boolean isTempLocationInherited() {
        if (tempLocation != null) {
            return tempLocation.isInherited();
        }
        else {
            return false;
        }
    }
    
    /**
     * @param pTempLocationInherited
     *            the tempLocation inherited to set
     */
    public void setTempLocationInherited(boolean pTempLocationInherited) {
        // The temporary location shall not be modified directly
    }
    
    /**
     * Check the change of location in front of the contained items inheriting
     * their location from parent.<br>
     * Compute a warning message to display to user.
     * 
     * @return the warning message, or an empty string
     */
    public String getWarningMessage() {
        
        if (firstLocation == null) {
            if (location == null) {
                // Location is identical
                return "";
            }
        }
        else {
            if (location != null) {
                Place lFirstPlace = firstLocation.getPlace();
                Place lNewPlace = location.getPlace();
                if ((lFirstPlace == null && lNewPlace == null)
                        || (lFirstPlace != null && lFirstPlace
                                .equals(lNewPlace))) {
                    // Location is identical
                    return "";
                }
            }
        }
        
        List<ContainedItem> lInheritedContainedItems =
                new ArrayList<ContainedItem>();
        if (locatedItem instanceof ContainerItem) {
            lInheritedContainedItems.addAll(((ContainerItem) locatedItem)
                    .getContainedItemsInheriting());
        }
        
        if (!lInheritedContainedItems.isEmpty()) {
            return MessageBundle.getMessage("InheritedLocationChangeWarning");
        }
        return "";
    }
    
}
