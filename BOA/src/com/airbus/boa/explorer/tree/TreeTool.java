/*
 * ------------------------------------------------------------------------
 * Class : TreeTool
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;
import com.airbus.boa.localization.Location;

/**
 * Manage a Tool tree node
 */
public class TreeTool extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Tool tool;
    
    /**
     * Constructor of the Tool tree node
     * 
     * @param pTool
     *            the tool holden by the tree node
     * @param pParent
     *            the parent tree node
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeTool(Tool pTool, TreeNode pParent,
            ChildrenCriterion pChildrenCriterion) {
        
        super(pParent, pChildrenCriterion);
        
        tool = pTool;
        setIcon(ICON_PATH + "/locations/Tool32x32.png");
        setLeafIcon(getIcon());
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case CONTAINER:
            
            addContainedItems(tool.getContainedItems());
            break;
        
        case LOCATION:
        default:
            // No element is located into a tool
            break;
        }
    }
    
    @Override
    public boolean isLeaf() {
        switch (childrenCriterion) {
        case CONTAINER:
            return tool.getContainedItems().isEmpty();
            
        case LOCATION:
        default:
            return true;
        }
    }
    
    @Override
    public String getName() {
        return tool.getName();
    }
    
    @Override
    public String getStyleClass() {
        
        switch (childrenCriterion) {
        case LOCATION:
            Location lLocation = tool.getLocation();
            if (lLocation != null && lLocation.isInherited()) {
                return "inherited";
            }
            else {
                return "";
            }
            
        case CONTAINER:
        default:
            return "";
        }
    }
    
    @Override
    public String getItemClassName() {
        return tool.getClass().getSimpleName();
    }
    
    @Override
    public Long getId() {
        return tool.getId();
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        
        return tool;
    }
    
}
