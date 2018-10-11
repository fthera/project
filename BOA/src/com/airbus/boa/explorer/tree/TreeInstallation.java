/*
 * ------------------------------------------------------------------------
 * Class : TreeInstallation
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;

/**
 * Manage an Installation tree node
 */
public class TreeInstallation extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Installation installation;
    
    /**
     * Constructor of the Installation tree node
     * 
     * @param installation
     *            the installation holden by the tree node
     * @param parent
     *            the parent tree node
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeInstallation(Installation installation, TreeNode parent,
            ChildrenCriterion pChildrenCriterion) {
        
        super(parent, pChildrenCriterion);
        this.installation = installation;
        
        setIcon(ICON_PATH + "/locations/installation32x32.png");
        setLeafIcon(getIcon());
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case CONTAINER:
            
            addContainedItems(installation.getContainedItems());
            break;
        
        case LOCATION:
        default:
            // No element is located into an installation
            break;
        }
    }
    
    @Override
    public boolean isLeaf() {
        switch (childrenCriterion) {
        case CONTAINER:
            return installation.getContainedItems().isEmpty();
        case LOCATION:
        default:
            return true;
        }
    }
    
    @Override
    public String getName() {
        return installation.getName();
    }
    
    @Override
    public String getItemClassName() {
        return installation.getClass().getSimpleName();
    }
    
    @Override
    public String getStyleClass() {
        return "";
    }
    
    @Override
    public Long getId() {
        return installation.getId();
    }
    
    /**
     * @return the holden Installation
     */
    public Installation getInstallation() {
        
        return installation;
    }
    
}
