/*
 * ------------------------------------------------------------------------
 * Class : TreeCategory
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.util.List;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;

/**
 * Manage a category tree node
 */
public class TreeCategory extends TreeNodeBase {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    /**
     * Constructor
     * 
     * @param name
     *            the category name
     * @param parent
     *            the parent tree node
     * @param children
     *            the children elements
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeCategory(String name, TreeNode parent,
            List<? extends Object> children,
            ChildrenCriterion pChildrenCriterion) {
        
        super(parent, pChildrenCriterion);
        this.name = name;
        
        if (children != null) {
            
            for (Object object : children) {
                
                if (object instanceof Article) {
                    
                    addArticle((Article) object);
                }
                else if (object instanceof Installation) {
                    
                    addInstallation((Installation) object);
                }
                else if (object instanceof ExternalEntity) {
                    
                    addExternalEntity((ExternalEntity) object);
                }
                else if (object instanceof Place) {
                    
                    addPlace((Place) object);
                }
                else if (object instanceof Tool) {
                    
                    addTool((Tool) object);
                }
            }
        }
    }
    
    @Override
    protected void addChildren() {
        // No child to add
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getStyleClass() {
        return "";
    }
    
    @Override
    public String getItemClassName() {
        return name.getClass().getSimpleName();
    }
    
    @Override
    public Long getId() {
        return null;
    }
    
    @Override
    public boolean isLeaf() {
        return false;
    }
    
}
