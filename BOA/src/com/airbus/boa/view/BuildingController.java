/*
 * ------------------------------------------------------------------------
 * Class : BuildingController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.poi.ss.SpreadsheetVersion;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.io.ExportExcel;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Controller managing the Building and Place creation, modification and
 * deletion
 */
@ManagedBean(name = BuildingController.BEAN_NAME)
@ViewScoped
public class BuildingController extends AbstractController {
    
    private static final long serialVersionUID = 4114328496090102280L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "buildingController";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private DemandBean demandBean;
    
    private String newBuilding;
    private Place newPlace = new Place();
    
    private Building selectedBuilding;
    
    private List<SelectItem> listBuildings;
    
    private Long selectedPlaceId;
    
    // nom et type pour la modification d'un emplacement
    private String placeName;
    private PlaceType placeType;
    
    private List<Place> selectedPlaces;
    
    private Set<Integer> rowsToUpdate = new HashSet<Integer>();
    
    private List<Building> dynamicColumns = new ArrayList<Building>();
    
    private List<Building> selectedBuildings;
    
    public void changeCurrentBuilding(ValueChangeEvent e) {
        setSelectedBuilding((Building) e.getNewValue());
        // log.warning("changeCurrentBuilding "+String.valueOf(selectedBuilding));
    }
    
    public void doAddBuilding() {
        
        if ((newBuilding == null) || (newBuilding.trim().length() == 0)) {
            Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                    MessageBundle.getMessage(Constants.INVALID_BULDING_NAME));
        }
        else if (locationBean.isBuildingExisting(newBuilding)) {
            Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                    MessageBundle
                    .getMessageResource(Constants.ALREADY_EXISTED_BUILDING,
                            new Object[] { newBuilding }));
        }
        else {
            locationBean.createBuilding(new Building(newBuilding));
        }
    }
    
    /**
     * Add a place in the selected building
     */
    public void doAddPlace() {
        
        if (newPlace != null && !newPlace.getName().isEmpty()) {
            Place lFoundPlace =
                    locationBean.findPlaceByNameAndBuilding(newPlace.getName(),
                            selectedBuilding);
            if (lFoundPlace == null) {
                
                try {
                    newPlace =
                            locationBean
                                    .createPlace(newPlace, selectedBuilding);
                    
                }
                catch (Exception ve) {
                    Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                            ve.getMessage());
                }
                
            }
            else {
                String msg =
                        MessageBundle
                                .getMessage(Constants.ALREADY_EXISTED_PLACE);
                Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                        msg);
            }
            newPlace = new Place();
        }
    }
    
    /**
     * Remove the selected Place from the database
     */
    public void doDeletePlace() {
        try {
            Place place = locationBean.findPlaceById(selectedPlaceId);
            
            // Check that no item is located into the selected place
            if (!place.getLocatedItemsDirectly().isEmpty()) {
                Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.PLACE_DELETION_CONTAINS_ITEMS));
            }
            
            // Check that no demand is located into the selected place
            else if (!demandBean.findDemandsLocatedIntoPlace(place).isEmpty()) {
                Utils.addFacesMessage(NavigationConstants.BUILDING_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.PLACE_DELETION_CONTAINS_DEMAND));
            }
            
            else {
                locationBean.removePlace(place, selectedBuilding);
            }
            
        }
        catch (ValidationException ve) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(
                    NavigationConstants.BUILDING_ERROR_ID,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ve
                            .getMessage(), ve.getMessage()));
        }
    }
    
    /**
     * exporte les batiments et les salles dans un fichier excel
     */
    public void doExport() {
        
        List<Place> listPlaces = new ArrayList<Place>();
        
        if (!selectedBuildings.isEmpty()) {
            
            ExportExcel formExcel =
                    new ExportExcel(SpreadsheetVersion.EXCEL2007);
            
            // export des batiments en premier
            formExcel.writeListResult(selectedBuildings);
            
            for (Building building : selectedBuildings) {
                listPlaces.addAll(building.getPlaces());
            }
            
            if (!listPlaces.isEmpty()) {
                // export des emplacements
                formExcel.writeListResult(listPlaces);
            }
            
            // Mise en form
            formExcel.applyStyles();
            download(formExcel, "buildings.xlsx",
                    ExportController.MIMETYPE_XLSX);
            
        }
    }
    
    public void doRemoveBuilding() {
        try {
            locationBean.removeBuilding(selectedBuilding);
            selectedBuilding = null;
        }
        catch (ValidationException ve) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(NavigationConstants.BUILDING_ERROR_ID,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ve
                            .getMessage(), ve.getMessage()));
        }
    }
    
    /*
     * Effectue la modification de l'emplacement selectionné
     */
    public void doUpdatePlace() {
        try {
            Place place = locationBean.findPlaceById(selectedPlaceId);
            if (placeName != null && placeName.trim().length() > 0) {
                
                rowsToUpdate.clear();
                rowsToUpdate.add(selectedPlaces.indexOf(place));
                
                place.setName(placeName);
                place.setType(placeType);
                place = locationBean.merge(place);
                
            }
            
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.UPDATE_PLACE_ERROR_ID,
                    ExceptionUtil.getMessage(e));
            log.warning("erreur sur doUpdatePlace :"
                    + ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Check if the provided name value is correct
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the name to valid
     * @throws ValidatorException
     *             when the name is not valid
     */
    public void validatePlaceName(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        
        String valeur = (String) value;
        valeur = (valeur == null) ? "" : valeur.trim();
        if ("".equals(valeur)) {
            String msg =
                    MessageBundle
                            .getMessage("javax.faces.component.UIInput.REQUIRED");
            
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
        
        try {
            Place placeFound =
                    locationBean.findPlaceByNameAndBuilding(valeur,
                            selectedBuilding);
            
            // a place different from the one being modified has the same name
            // in the same building
            if (placeFound != null && placeFound.getId() != selectedPlaceId) {
                
                String msg =
                        MessageBundle
                                .getMessage(Constants.PLACENAME_ALREADY_USED);
                
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, msg, msg));
                
            }
        }
        catch (ValidationException e) {
            Throwable t = ExceptionUtil.getRootCause(e);
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            t.getMessage(), t.getMessage()));
        }
    }
    
    public List<Building> getDynamicColumns() {
        
        // Tous les bâtiments dans la liste des bâtiments existants
        dynamicColumns = locationBean.findAllBuilding();
        if (dynamicColumns != null && selectedBuildings != null) {
            // Il y a des bâtiments déjà sélectionnés,
            // on les supprime de la liste des bâtiments existants
            dynamicColumns.removeAll(selectedBuildings);
        }
        return dynamicColumns;
    }
    
    public void setDynamicColumns(List<Building> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }
    
    public List<SelectItem> getListBuildings() {
        List<Building> buildings = locationBean.findAllBuilding();
        listBuildings = new ArrayList<SelectItem>();
        for (Building batiment : buildings) {
            SelectItem item = new SelectItem(batiment, batiment.getName());
            listBuildings.add(item);
        }
        return listBuildings;
    }
    
    public String getNewBuilding() {
        return newBuilding;
    }
    
    public void setNewBuilding(String newBuilding) {
        this.newBuilding = newBuilding;
    }
    
    public Place getNewPlace() {
        return newPlace;
    }
    
    public void setNewPlace(Place newPlace) {
        this.newPlace = newPlace;
    }
    
    public String getPlaceName() {
        return placeName;
    }
    
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
    
    /**
     * @return the placeType
     */
    public PlaceType getPlaceType() {
        return placeType;
    }

    /**
     * @param placeType the placeType to set
     */
    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public List<SelectItem> getPlaceTypeList() {
        
        List<SelectItem> result = new ArrayList<SelectItem>();
        for (PlaceType placeType : PlaceType.values()) {
            result.add(new SelectItem(placeType, MessageBundle
                    .getMessage("placeType" + placeType)));
        }
        return result;
    }
    
    public Set<Integer> getRowsToUpdate() {
        return rowsToUpdate;
    }
    
    public void setRowsToUpdate(Set<Integer> rowsToUpdate) {
        this.rowsToUpdate = rowsToUpdate;
    }
    
    public Building getSelectedBuilding() {
        return selectedBuilding;
    }
    
    public void setSelectedBuilding(Building selectedBuilding) {
        this.selectedBuilding = selectedBuilding;
    }
    
    public List<Building> getSelectedBuildings() {
        return selectedBuildings;
    }
    
    public void setSelectedBuildings(List<Building> selectedBuildings) {
        this.selectedBuildings = selectedBuildings;
    }
    
    public Long getSelectedPlaceId() {
        return selectedPlaceId;
    }
    
    public void setSelectedPlaceId(Long selectedPlaceId) {
        this.selectedPlaceId = selectedPlaceId;
    }
    
    public List<Place> getSelectedPlaces() {
        if (selectedBuilding != null) {
            selectedPlaces =
                    locationBean.findPlacesByBuildingFk(selectedBuilding
                            .getId());
        }
        else {
            selectedPlaces = Collections.emptyList();
        }
        return selectedPlaces;
    }
    
}
