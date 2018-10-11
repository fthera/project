/*
 * ------------------------------------------------------------------------
 * Class : TreeNodeBase
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.richfaces.model.TreeNode;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.LocatedItem;
import com.airbus.boa.util.MessageBundle;

/**
 * Tree Node used for hierarchical display
 */
public abstract class TreeNodeBase implements TreeNode, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int REGROUPMENT_SIZE = 2;
    
    /** Path containing the icons */
    protected static final String ICON_PATH = "/resources/images/icons/";
    
    /** The way to retrieve children */
    protected ChildrenCriterion childrenCriterion;
    
    /**
     * The map containing the children nodes:<br>
     * the key is the element id in database and the value is the node
     * containing the element
     */
    private Map<Object, TreeNode> childrenTreeNodesMap;
    
    private boolean isChildrenMapInitialized = false;
    
    private String icon = ICON_PATH + "/iconFolder.gif";
    
    private String leafIcon = ICON_PATH + "/iconFolder.gif";
    
    private String description;
    
    private boolean expanded = false;
    
    /**
     * Constructor
     * 
     * @param pParent
     *            the parent tree node (can be null)
     * @param pChildrenCriterion
     *            the criterion to retrieve children
     */
    public TreeNodeBase(TreeNode pParent,
            ChildrenCriterion pChildrenCriterion) {
        childrenTreeNodesMap = new LinkedHashMap<Object, TreeNode>();
        childrenCriterion = pChildrenCriterion;
    }
    
    @Override
    public void addChild(Object key, TreeNode child) {
        childrenTreeNodesMap.put(key, child);
    }
    
    /**
     * Add the children of the node, according to the childrenCriterion value
     */
    protected abstract void addChildren();
    
    @Override
    public void removeChild(Object arg0) {
        childrenTreeNodesMap.remove(arg0);
    }
    
    /**
     * Add the article as a child of this node, if not already added
     * 
     * @param article
     *            the article
     */
    protected void addArticle(Article article) {
        if (getChild(article.getId()) != null) {
            return;
        }
        
        TreeNode child =
                new TreeArticle(article, this, childrenCriterion);
        addChild(article.getId(), child);
    }
    
    /**
     * Add the installation as a child of this node, if not already added
     * 
     * @param installation
     *            the installation
     */
    protected void addInstallation(Installation installation) {
        if (getChild(installation.getId()) != null) {
            return;
        }
        
        TreeNode child =
                new TreeInstallation(installation, this, childrenCriterion);
        addChild(installation.getId(), child);
    }
    
    /**
     * Add the tool as a child of this node, if not already added
     * 
     * @param pTool
     *            the tool
     */
    protected void addTool(Tool pTool) {
        if (getChild(pTool.getId()) != null) {
            return;
        }
        
        TreeNode lChild = new TreeTool(pTool, this, childrenCriterion);
        addChild(pTool.getId(), lChild);
    }
    
    /**
     * Add the place as a child of this node, if not already added
     * 
     * @param place
     *            the place
     */
    protected void addPlace(Place place) {
        
        if (getChild(place.getId()) != null) {
            return;
        }
        
        TreeNode child = new TreePlace(place, this, childrenCriterion);
        addChild(place.getId(), child);
    }
    
    /**
     * Add the external entity as a child of this node, if not already added
     * 
     * @param pExternalEntity
     *            the external entity
     */
    protected void addExternalEntity(ExternalEntity pExternalEntity) {
        
        if (getChild(pExternalEntity.getId()) != null) {
            return;
        }
        
        TreeNode child =
                new TreeExternalEntity(pExternalEntity, this, childrenCriterion);
        addChild(pExternalEntity.getId(), child);
    }
    
    /**
     * Add the articles as children of this node, if not already added.<br>
     * For each article family, if the articles list of this family is too long,
     * the category for this article family is added (only if not already added)
     * with the articles of this family.<br>
     * Else it is added one by one.
     * 
     * @param pArticles
     *            the articles
     */
    private void addArticles(List<Article> pArticles) {
        if (pArticles != null && !pArticles.isEmpty()) {
            Collections.sort(pArticles, new Comparator<Article>() {
                
                @Override
                public int compare(Article pArg0, Article pArg1) {
                    String lName0 = Article.computeDetailedName(pArg0, true);
                    String lName1 = Article.computeDetailedName(pArg1, true);
                    
                    // If the two articles have slots precision
                    // order by slot number
                    if (lName0.startsWith(Article.SLOT_PREFIX)
                            && lName1.startsWith(Article.SLOT_PREFIX)) {
                        String lPrecision0 = lName0.substring(0,
                                lName0.indexOf(Article.SEPARATOR_FIELD));
                        String lSlotNumber0 =
                                lPrecision0.replace(Article.SLOT_PREFIX, "")
                                        .replaceAll("f", "").replace("r", "");
                        boolean lIsFront0 = lPrecision0.contains("f");
                        boolean lIsRear0 = lPrecision0.contains("r");
                        
                        String lPrecision1 = lName1.substring(0,
                                lName1.indexOf(Article.SEPARATOR_FIELD));
                        String lSlotNumber1 =
                                lPrecision1.replace(Article.SLOT_PREFIX, "")
                                        .replaceAll("f", "").replace("r", "");
                        boolean lIsFront1 = lPrecision1.contains("f");
                        boolean lIsRear1 = lPrecision1.contains("r");
                        
                        // Append 0 prefixes to ensure a correct comparison
                        while (lSlotNumber0.length() < 10) {
                            lSlotNumber0 = "0" + lSlotNumber0;
                        }
                        while (lSlotNumber1.length() < 10) {
                            lSlotNumber1 = "0" + lSlotNumber1;
                        }
                        
                        int lSlotComparison =
                                lSlotNumber0.compareToIgnoreCase(lSlotNumber1);
                        // The slot number comparison is sufficient
                        if (lSlotComparison != 0) {
                            return lSlotComparison;
                        }
                        // Compare the faces, ex: Slot 12> Slot 12f> Slot 12r
                        else {
                            if (lIsFront0) {
                                if (lIsFront1) {
                                    return 0;
                                }
                                else if (lIsRear1) {
                                    return 1;
                                }
                                else {
                                    return -1;
                                }
                            }
                            else if (lIsRear0) {
                                if (lIsFront1 || !lIsRear1) {
                                    return -1;
                                }
                                else {
                                    return 0;
                                }
                            }
                            else {
                                if (lIsFront1 || lIsRear1) {
                                    return 1;
                                }
                                else {
                                    return 0;
                                }
                            }
                        }
                    }
                    
                    return lName0.compareToIgnoreCase(lName1);
                }
                
            });
            
            Map<String, List<Article>> lMapFamilies =
                    new LinkedHashMap<String, List<Article>>();
            
            // Build the map containing for each family the list of
            // corresponding articles
            for (Article lArticle : pArticles) {
                List<Article> lCurrent =
                        lMapFamilies.get(lArticle.getClass().getSimpleName());
                if (lCurrent == null) {
                    lCurrent = new ArrayList<Article>();
                }
                
                lCurrent.add(lArticle);
                lMapFamilies.put(lArticle.getClass().getSimpleName(), lCurrent);
            }
            
            String[] lOrder =
                    { Cabinet.class.getSimpleName(),
                            Rack.class.getSimpleName(),
                            Switch.class.getSimpleName(),
                            PC.class.getSimpleName(),
                            Board.class.getSimpleName(),
                            Various.class.getSimpleName() };
            
            if (lMapFamilies.size() > 1) {
                // There is more than one family
                for (int i = 0; i < lOrder.length; i++) {
                    
                    String lKey = lOrder[i];
                    
                    List<Article> lCurrent = lMapFamilies.get(lKey);
                    if (lCurrent != null) {
                        
                        if (lCurrent.size() > REGROUPMENT_SIZE) {
                            String lNameCategory =
                                    MessageBundle.getMessage(lKey);
                            if (!lNameCategory.endsWith("s")) {
                                lNameCategory += "s";
                            }
                            
                            if (getChild(lNameCategory) == null) {
                                TreeNode lChild =
                                        new TreeCategory(lNameCategory, this,
                                                lCurrent, childrenCriterion);
                                addChild(lNameCategory, lChild);
                            }
                            
                        }
                        else {
                            
                            for (Article article : lCurrent) {
                                addArticle(article);
                            }
                        }
                        
                    }
                }
                
            }
            else {
                // All articles are of the same family, no category is added
                for (Article lArticle : pArticles) {
                    addArticle(lArticle);
                }
                
            }
        }
    }
    
    /**
     * Add the installations as children of this node, if not already added.<br>
     * If the installations list is too long, the category "Installations" is
     * added (only if not already added) with the installations.<br>
     * Else it is added one by one.
     * 
     * @param installations
     *            the installations
     */
    private void addInstallations(List<Installation> installations) {
        
        if (installations != null) {
            
            Collections.sort(installations);
            
            if (installations.size() > REGROUPMENT_SIZE) {
                
                if (getChild("Installations") != null) {
                    return;
                }
                
                TreeNode child =
                        new TreeCategory("Installations", this, installations,
                                childrenCriterion);
                addChild("Installations", child);
                
            }
            else {
                
                for (Installation installation : installations) {
                    addInstallation(installation);
                }
            }
        }
    }
    
    /**
     * Add the tools as children of this node, if not already added.<br>
     * If the tools list is too long, the category "Tools" is
     * added (only if not already added) with the tools.<br>
     * Else it is added one by one.
     * 
     * @param pTools
     *            the tools
     */
    private void addTools(List<Tool> pTools) {
        
        if (pTools != null) {
            
            Collections.sort(pTools);
            
            if (pTools.size() > REGROUPMENT_SIZE) {
                
                if (getChild("Tools") != null) {
                    return;
                }
                
                TreeNode lChild =
                        new TreeCategory("Tools", this, pTools,
                                childrenCriterion);
                addChild("Tools", lChild);
                
            }
            else {
                
                for (Tool lTool : pTools) {
                    addTool(lTool);
                }
            }
        }
    }
    
    /**
     * Add the places as children of this node, if not already added.<br>
     * For each place type, if the places list of this type is too long, the
     * category for this place type is added (only if not already added) with
     * the places of this type.<br>
     * Else it is added one by one.
     * 
     * @param places
     *            the places
     */
    protected void addPlaces(List<Place> places) {
        
        if (places != null) {
            Collections.sort(places);
            
            Map<String, List<Place>> mapPlaceType =
                    new LinkedHashMap<String, List<Place>>();
            
            // Build the map containing for each place type the corresponding
            // places
            for (Place place : places) {
                List<Place> current =
                        mapPlaceType.get(place.getType().toString());
                if (current == null) {
                    current = new ArrayList<Place>();
                }
                
                current.add(place);
                mapPlaceType.put(place.getType().toString(), current);
            }
            
            PlaceType[] order =
                    { PlaceType.Storeroom, PlaceType.Laboratory, PlaceType.Room };
            
            for (int i = 0; i < order.length; i++) {
                
                String key = order[i].toString();
                
                List<Place> current = mapPlaceType.get(key);
                if (current != null) {
                    
                    if (current.size() > REGROUPMENT_SIZE) {
                        String nameCategory =
                                MessageBundle.getMessage("Category" + key);
                        if (getChild(nameCategory) != null) {
                            return;
                        }
                        TreeNode child =
                                new TreeCategory(nameCategory, this, current,
                                        childrenCriterion);
                        addChild(nameCategory, child);
                        
                    }
                    else {
                        for (Place place : current) {
                            addPlace(place);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add the provided items as children of this node.
     * 
     * @param pContainedItems
     *            the list of contained items
     */
    protected void addContainedItems(List<ContainedItem> pContainedItems) {
        
        List<Article> lArticles = new ArrayList<Article>();
        List<Tool> lTools = new ArrayList<Tool>();
        
        for (ContainedItem lContainedItem : pContainedItems) {
            switch (lContainedItem.getContainedType()) {
            case Board:
            case Cabinet:
            case PC:
            case Rack:
            case Switch:
            case Various:
                lArticles.add((Article) lContainedItem);
                break;
            case Tool:
                lTools.add((Tool) lContainedItem);
                break;
            case Demand:
                // Demands are not displayed in tree view
                break;
            default:
                break;
            }
        }
        
        addTools(lTools);
        addArticles(lArticles);
    }
    
    /**
     * Add the provided items as children of this node.
     * 
     * @param pLocatedItems
     *            the list of located items
     */
    protected void addLocatedItems(List<LocatedItem> pLocatedItems) {
        
        List<Installation> lInstallations = new ArrayList<Installation>();
        List<Tool> lTools = new ArrayList<Tool>();
        List<Article> lArticles = new ArrayList<Article>();
        
        for (LocatedItem lLocatedItem : pLocatedItems) {
            switch (lLocatedItem.getLocatedType()) {
            case Board:
            case Cabinet:
            case PC:
            case Rack:
            case Switch:
            case Various:
                lArticles.add((Article) lLocatedItem);
                break;
            case Installation:
                lInstallations.add((Installation) lLocatedItem);
                break;
            case Tool:
                lTools.add((Tool) lLocatedItem);
                break;
            case Demand:
                // Demands are not displayed in tree view
                break;
            default:
                break;
            }
        }
        
        addInstallations(lInstallations);
        addTools(lTools);
        addArticles(lArticles);
    }
    
    @Override
    public TreeNode getChild(Object key) {
        return childrenTreeNodesMap.get(key);
    }

	@Override
	public Iterator<Object> getChildrenKeysIterator() {
        if (!isChildrenMapInitialized) {
            // Lazy loading of the children
            addChildren();
            isChildrenMapInitialized = true;
        }
        return childrenTreeNodesMap.keySet().iterator();
	}

	@Override
	public int indexOf(Object key) {
		return 0;
	}

	@Override
	public void insertChild(int index, Object key, TreeNode child) {
        childrenTreeNodesMap.put(key, child);
	}
    
    /**
     * @return the name of the element corresponding to the node
     */
    public abstract String getName();
    
    /**
     * @return the style class to display the node name
     */
    public abstract String getStyleClass();
    
    /**
     * @return the style class of the represented item
     */
    public abstract String getItemClassName();
    
    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * @param icon
     *            the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * @return the leafIcon
     */
    public String getLeafIcon() {
        return leafIcon;
    }
    
    /**
     * @param leafIcon
     *            the leafIcon to set
     */
    public void setLeafIcon(String leafIcon) {
        this.leafIcon = leafIcon;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * The returned id is used to determine if the element can be consulted.<br>
     * If null is returned, the element cannot be consulted.
     * 
     * @return the id of the element corresponding to the node, or null
     */
    public abstract Long getId();

    /**
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param pExpanded
     *            the expanded to set
     */
    public void setExpanded(boolean pExpanded) {
        this.expanded = pExpanded;
    }
    
    /**
     * Expand all nodes
     */
    public void expandAll(){
        setExpanded(true);
        Iterator<Object> iterator = this.getChildrenKeysIterator();
        while (iterator.hasNext()) {
            TreeNodeBase node = (TreeNodeBase) getChild(iterator.next());
            node.expandAll();
        }
    }
    
    /**
     * Collapse all nodes
     */
    public void collapseAll() {
        setExpanded(false);
        Iterator<Object> iterator = this.getChildrenKeysIterator();
        while (iterator.hasNext()) {
            TreeNodeBase node = (TreeNodeBase) getChild(iterator.next());
            node.collapseAll();
        }
    }
    
    /**
     * @return this node depth
     */
    public int getDepth() {
        int lDepth = 1;
        if (!isLeaf()) {
            Iterator<Object> iterator = this.getChildrenKeysIterator();
            while (iterator.hasNext()) {
                TreeNodeBase node = (TreeNodeBase) getChild(iterator.next());
                int lTmpDepth = 1 + node.getDepth();
                if (lTmpDepth > lDepth) {
                    lDepth = lTmpDepth;
                }
            }
        }
        return lDepth;
    }
}
