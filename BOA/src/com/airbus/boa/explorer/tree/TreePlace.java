/*
 * ------------------------------------------------------------------------
 * Class : TreePlace
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.location.Place;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;

/**
 * Manage a place tree node
 */
public class TreePlace extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Place place;
    
    /**
     * Constructor
     * 
     * @param pPlace
     *            the place
     * @param pParent
     *            the parent tree node
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreePlace(Place pPlace, TreeNode pParent,
            ChildrenCriterion pChildrenCriterion) {
        super(pParent, pChildrenCriterion);
        place = pPlace;
        
        switch (place.getType()) {
        case Laboratory:
            setIcon(ICON_PATH + "/locations/labo.png");
            setLeafIcon(getIcon());
            break;
        
        case Storeroom:
            setIcon(ICON_PATH + "/locations/Warehouse.png");
            setLeafIcon(getIcon());
            break;
        
        case Room:
            setIcon(ICON_PATH + "/locations/room32x32.png");
            setLeafIcon(getIcon());
            break;
        
        default:
            break;
        }
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case LOCATION:
            
            addLocatedItems(place.getLocatedItems());
            break;
        
        case CONTAINER:
        default:
            // No element is contained by a place
            break;
        }
    }
    
    @Override
    public String getName() {
        
        return place.getCompleteName();
    }
    
    @Override
    public String getStyleClass() {
        return "";
    }
    
    @Override
    public boolean isLeaf() {
        
        switch (childrenCriterion) {
        case LOCATION:
            return place.getLocatedItems().isEmpty();
            
        case CONTAINER:
        default:
            return true;
        }
    }
    
    @Override
    public String getItemClassName() {
        return place.getClass().getSimpleName();
    }

    /**
     * @return the place
     */
    public Place getPlace() {
        return place;
    }
    
    @Override
    public Long getId() {
        return null;
    }
    
}
