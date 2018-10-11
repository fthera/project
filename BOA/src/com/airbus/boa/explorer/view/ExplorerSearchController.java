/*
 * ----------------------------------------------------------------------------
 * Class : ExplorerSearchController
 * Copyright 2014 by AIRBUS France
 * ----------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.view;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.entity.TreeNodeFamily;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Controller managing the hierarchical search page
 */
@ManagedBean(name = ExplorerSearchController.BEAN_NAME)
@ViewScoped
public class ExplorerSearchController extends ExplorerController {
    
    private static final long serialVersionUID = 1L;
    
    /** Controller name */
    public static final String BEAN_NAME =
            "explorerSearchController";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private ToolBean toolBean;
    
    private TreeNodeFamily selectedFamily;
    
    private String suggestionValue;
    
    /**
     * Return a list of suggestions depending on the provided expression and the
     * selected family. <br>
     * The returned values are of the type defined by the selected family. <br>
     * If the provided expression is empty or null, it is considered as the
     * generic character '*'.<br>
     * If no family is selected, return an empty list.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the expression from which to build the suggestions lis
     * @return a list of suggestion objects satisfying the provided expression
     */
    public List<? extends Object> suggestions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        String lSuggest = pPrefix;
        
        if (StringUtil.isEmptyOrNull(lSuggest)) {
            lSuggest = "*";
        }
        
        if (selectedFamily == null) {
            return Collections.emptyList();
        }
        
        switch (selectedFamily) {
        
        case BUILDING:
            return locationBean.suggestionListLocation(Building.class, null,
                    lSuggest);
            
        case EXTERNAL_ENTITY:
            return locationBean.suggestionListLocation(ExternalEntity.class,
                    null, lSuggest);
            
        case STOREROOM:
            return locationBean.suggestionListLocation(Place.class,
                    Place.PlaceType.Storeroom, lSuggest);
            
        case LABORATORY:
            return locationBean.suggestionListLocation(Place.class,
                    Place.PlaceType.Laboratory, lSuggest);
            
        case ROOM:
            return locationBean.suggestionListLocation(Place.class,
                    Place.PlaceType.Room, lSuggest);
            
        case INSTALLATION:
            return locationBean.suggestionListLocation(Installation.class,
                    null, lSuggest);
            
        case CABINET:
            return articleBean.suggestionListArticle(Cabinet.class, lSuggest);
            
        case SWITCH:
            return articleBean.suggestionListArticle(Switch.class, lSuggest);
            
        case RACK:
            return articleBean.suggestionListArticle(Rack.class, lSuggest);
            
        case PC:
            return articleBean.suggestionListArticle(PC.class, lSuggest);
            
        case TOOL:
            return toolBean.suggestionListTool(lSuggest);
            
        default:
            break;
        }
        return Collections.emptyList();
        
    }
    
    /**
     * Initialize the page root node with the selected object
     */
    public void doShowNode() {
        Object lSelectedObject = null;
        
        if (selectedFamily == null) {
            return;
        }
        
        switch (selectedFamily) {
        
        case BUILDING:
            lSelectedObject = locationBean.findBuildingByName(suggestionValue);
            break;
            
        case EXTERNAL_ENTITY:
            lSelectedObject =
                    locationBean.findExternalEntityByName(suggestionValue);
            break;
            
        case STOREROOM:
        case LABORATORY:
        case ROOM:
            lSelectedObject =
                    locationBean.findPlaceByCompleteName(suggestionValue);
            break;
            
        case INSTALLATION:
            lSelectedObject =
                    locationBean.findInstallationByName(suggestionValue);
            break;
            
        case CABINET:
            lSelectedObject =
                    articleBean.findCabinetByDesignationOrSN(suggestionValue);
            break;
        
        case SWITCH:
            lSelectedObject = articleBean.findSwitchByASNOrMSN(suggestionValue);
            break;
        
        case RACK:
            lSelectedObject =
                    articleBean.findRackByDesignationOrSN(suggestionValue);
            break;
        
        case PC:
            lSelectedObject = pcBean.findPCbyName(suggestionValue);
            break;
        
        case TOOL:
            lSelectedObject = toolBean.findToolByName(suggestionValue);
            break;
        
        default:
            break;
        }
        
        if (lSelectedObject != null) {
            initRootNode();
            addTreeNode(lSelectedObject);
        }
    }
    
    /**
     * Initialize the page root node with the objects satisfying the suggestion
     * value.
     */
    public void doSearch() {
        
        initRootNode();
        
        for (Object lObject : suggestions(null, null, suggestionValue)) {
            addTreeNode(lObject);
        }
    }
    
    /**
     * @return the selectedFamily
     */
    public TreeNodeFamily getSelectedFamily() {
        return selectedFamily;
    }
    
    /**
     * @param pSelectedFamily
     *            the selectedFamily to set
     */
    public void setSelectedFamily(TreeNodeFamily pSelectedFamily) {
        selectedFamily = pSelectedFamily;
        suggestionValue = null;
    }
    
    /**
     * @return the families
     */
    public SelectItem[] getFamilies() {
        
        int length = TreeNodeFamily.values().length;
        SelectItem[] lFamilies = new SelectItem[TreeNodeFamily.values().length];
        for (int i = 0; i < length; i++) {
            TreeNodeFamily cfamily = TreeNodeFamily.values()[i];
            lFamilies[i] = new SelectItem(cfamily, cfamily.toString());
        }
        return lFamilies;
    }
    
    /**
     * @return true if the selected family is a room (and has a completeName
     *         attribute), else false
     */
    public boolean isSelectedFamilyPlace() {
        
        switch (selectedFamily) {
        
        case BUILDING:
        case EXTERNAL_ENTITY:
            return false;
            
        case STOREROOM:
        case LABORATORY:
        case ROOM:
            return true;
            
        case INSTALLATION:
            return false;
            
        case CABINET:
        case SWITCH:
        case RACK:
        case PC:
            return false;
            
        case TOOL:
            return false;
            
        default:
            return false;
        }
    }
    
    /**
     * @return the suggestionValue
     */
    public String getSuggestionValue() {
        return suggestionValue;
    }
    
    /**
     * @param suggestionValue
     *            the suggestionValue to set
     */
    public void setSuggestionValue(String suggestionValue) {
        this.suggestionValue = suggestionValue;
    }
    
    /**
     * @return the way the children are retrieved in string form for display
     */
    public String getSearchInfoText() {
        
        if (searchMode != null) {
            return MessageBundle.getMessage("hierarchicalSearchInfoText_"
                    + searchMode.name());
        }
        else {
            return "";
        }
    }
    
}
