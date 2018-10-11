/*
 * ------------------------------------------------------------------------
 * Class : TreeBuilding
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;
import java.util.List;

import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;

/**
 * Manage a building tree node
 */
public class TreeBuilding extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = -1L;
    
    private Building building;
    
    /**
     * Constructor
     * 
     * @param building
     *            the building
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeBuilding(Building building, ChildrenCriterion pChildrenCriterion) {
        super(null, pChildrenCriterion);
        this.building = building;
        setIcon(ICON_PATH + "/locations/Building.png");
        setLeafIcon(getIcon());
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case LOCATION:
            
            // places
            List<Place> places = building.getPlaces();
            if (places != null && !places.isEmpty()) {
                addPlaces(places);
            }
            
            break;
        
        case CONTAINER:
        default:
            // No element is contained by a building
            break;
        }
    }
    
    @Override
    public String getName() {
        return building.getName();
    }
    
    @Override
    public String getStyleClass() {
        return "";
    }
    
    @Override
    public String getItemClassName() {
        return building.getClass().getSimpleName();
    }
    
    @Override
    public Long getId() {
        return null;
    }
    
    @Override
    public boolean isLeaf() {
        
        switch (childrenCriterion) {
        case LOCATION:
            
            if (building.getPlaces() != null && !building.getPlaces().isEmpty()) {
                return false;
            }
            return true;
            
        case CONTAINER:
        default:
            return true;
        }
    }
    
}
