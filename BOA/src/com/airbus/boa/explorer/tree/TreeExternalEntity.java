/*
 * ------------------------------------------------------------------------
 * Class : TreeExternalEntity
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;

/**
 * Manage an external entity tree node
 */
public class TreeExternalEntity extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private ExternalEntity externalEntity;
    
    /**
     * Constructor
     * 
     * @param pExternalEntity
     *            the external entity
     * @param parent
     *            the parent tree node
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeExternalEntity(ExternalEntity pExternalEntity,
            TreeNode parent, ChildrenCriterion pChildrenCriterion) {
        super(parent, pChildrenCriterion);
        externalEntity = pExternalEntity;
        
        setIcon(ICON_PATH + "/locations/suppliers.png");
        setLeafIcon(getIcon());
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case LOCATION:
            
            addLocatedItems(externalEntity.getLocatedItems());
            break;
        
        case CONTAINER:
        default:// No element is contained by an external entity
            break;
        }
    }
    
    @Override
    public String getName() {
        return externalEntity.getName();
    }
    
    @Override
    public String getStyleClass() {
        return "";
    }
    
    @Override
    public String getItemClassName() {
        return externalEntity.getClass().getSimpleName();
    }
    
    @Override
    public boolean isLeaf() {
        
        switch (childrenCriterion) {
        case LOCATION:
            return externalEntity.getLocatedItems().isEmpty();
            
        case CONTAINER:
        default:
            return true;
        }
    }
    
    @Override
    public Long getId() {
        return null;
    }
    
}
