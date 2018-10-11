/*
 * ------------------------------------------------------------------------
 * Class : TreeArticle
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;
import com.airbus.boa.localization.Location;

/**
 * Manage an Article tree node
 */
public class TreeArticle extends TreeNodeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Article article;
    
    /**
     * Constructor
     * 
     * @param article
     *            the article
     * @param parent
     *            the parent node
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeArticle(Article article, TreeNode parent,
            ChildrenCriterion pChildrenCriterion) {
        super(parent, pChildrenCriterion);
        this.article = article;
        
        setIcons();
    }
    
    @Override
    protected void addChildren() {
        
        switch (childrenCriterion) {
        case CONTAINER:
            
            addContainedItems(article.getContainedItems());
            break;
        
        case LOCATION:
        default:
            // No element is located into an article
            break;
        }
    }
    
    @Override
    public String getName() {
        return Article.computeDetailedName(article, true);
    }
    
    @Override
    public String getStyleClass() {
        
        switch (childrenCriterion) {
        case LOCATION:
            Location lLocation = article.getLocation();
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
        return article.getClass().getSimpleName();
    }
    
    @Override
    public String getDescription() {
        return Article.computeToolTip(article);
    }
    
    @Override
    public Long getId() {
        return article.getId();
    }
    
    private void setIcons() {
        
        if (article instanceof Cabinet) {
            setIcon(ICON_PATH + "/articles/32x32/cabinet32x32.png");
            
        }
        else if (article instanceof Switch) {
            setIcon(ICON_PATH + "/articles/16x16/switch.png");
            
        }
        else if (article instanceof Rack) {
            setIcon(ICON_PATH + "/articles/16x16/rack.png");
            
        }
        else if (article instanceof Board) {
            setIcon(ICON_PATH + "/articles/16x16/board16x16.png");
            
        }
        else if (article instanceof Various) {
            setIcon(ICON_PATH + "/articles/16x16/divers16x16.png");
            
        }
        else if (article instanceof PC) {
            setIcon(ICON_PATH + "/articles/16x16/PC16x16.png");
            
        }
        else {
            setIcon(ICON_PATH + "/articles/article.png");
        }
        
        setLeafIcon(getIcon());
    }
    
    @Override
    public boolean isLeaf() {
        switch (childrenCriterion) {
        case CONTAINER:
            return article.getContainedItems().isEmpty();
        case LOCATION:
        default:
            return true;
        }
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
}
