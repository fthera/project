/*
 * ------------------------------------------------------------------------
 * Class : ContainerController
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.SearchBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.ContainerItem;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.service.ComparatorNameArticle;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.application.DBConstants;

/**
 * Controller managing the container
 */
@ManagedBean(name = ContainerController.BEAN_NAME)
@ViewScoped
public class ContainerController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Controller name */
    public static final String BEAN_NAME =
            "containerController";
    
    private static final String ARTICLE_LABEL = "Article";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ContainerFactory containerFactory;
    
    @EJB
    private SearchBean searchBean;
    
    private ContainedType containedType;
    
    /** The article to be contained (if any) for filtering results */
    private Article containedArticle;
    
    /** Current container of the item */
    private Container container;
    
    /** Temporary container used in modal */
    private Container tempContainer;
    
    /**
     * Boolean to select the action to do on location after parent change
     * 0: Inherit (default), 1: Copy, 2: Do nothing
     */
    private int updateLocationAction = 0;
    
    /*
     * attributes used in modal for changing container
     */
    
    private List<ContainerType> possibleContainerTypes =
            new ArrayList<ContainerType>();
    private List<SelectItem> availableClasses = new ArrayList<SelectItem>();
    private List<SelectItem> availableArticleClasses =
            new ArrayList<SelectItem>();
    
    private String selectedClass = null;
    
    private Map<String, Object> findCriteria = new HashMap<String, Object>();
    private String inputSearchField;
    
    private List<Article> foundParentArticles = new ArrayList<Article>();
    
    private List<Installation> foundParentInstallations =
            new ArrayList<Installation>();
    
    private List<Tool> foundParentTools = new ArrayList<Tool>();
    
    /**
     * Constructor
     */
    public ContainerController() {
        
        super();
    }
    
    /**
     * Reset the temporary container using the current one (on main page)
     */
    public void doCancel() {
        resetSearchAttributes();
        if (container != null) {
            tempContainer = new Container(container);
        }
        else {
            tempContainer = null;
        }
    }
    
    /**
     * Update the found elements (depending on the selectedClass) by searching
     * articles, installations or tools satisfying the findCriteria
     */
    public void doFind() {
        updateSearchLists(true);
    }
    
    /**
     * Reset attributes for search
     */
    public void doResetSearchField() {
        resetSearchAttributes();
    }
    
    /**
     * Reset the temporary container to no container (null)
     */
    public void doResetTempContainer() {
        tempContainer = null;
    }
    
    /**
     * Prepare display of the container item
     */
    public void doShowContainer() {
        
        if (container != null) {
            
            ContainerItem lContainerItem = container.getContainerItem();
            
            if (lContainerItem != null) {
                
                Long lContainerId = null;
                switch (lContainerItem.getContainerType()) {
                case Board:
                case Cabinet:
                case PC:
                case Rack:
                case Switch:
                    lContainerId = ((Article) lContainerItem).getId();
                    break;
                case Installation:
                    lContainerId = ((Installation) lContainerItem).getId();
                    break;
                case Tool:
                    lContainerId = ((Tool) lContainerItem).getId();
                    break;
                default:
                }
                
                NavigationUtil.doShowItem(lContainerId, articleBean,
                        locationBean, toolBean);
            }
        }
    }
    
    /**
     * Store the temporary container as the current one (on main page).<br>
     * <b><i>updateLocationAction</i></b> must be initialized before calling
     */
    public void doValidate() {
        
        resetSearchAttributes();
        if (tempContainer != null) {
            container = new Container(tempContainer);
        }
        else {
            container = null;
        }
        
        LocationController lLocationController =
                findBean(LocationController.class);
        lLocationController.updateContainer(container, updateLocationAction);
    }
    
    public void setContainedItem(ContainedItem pContainedItem) {
        if (pContainedItem != null) {
            containedType = pContainedItem.getContainedType();
            
            if (pContainedItem instanceof Article) {
                containedArticle = (Article) pContainedItem;
            }
            else {
                containedArticle = null;
            }
            
            // Initialize current container and temporary container for modal
            
            ContainerManager lContainerManager =
                    new ContainerManager(pContainedItem);
            
            container = lContainerManager.getContainer();
            if (container != null) {
                tempContainer = new Container(container);
            }
            else {
                tempContainer = null;
            }
            possibleContainerTypes =
                    Arrays.asList(lContainerManager.getPossibleContainers());
            updateClasses();
        }
    }
    
    /**
     * Update the container according to the selected parent article
     * 
     * @param event
     *            the event sent while the selected name in the list is changed
     */
    public void parentArticleChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            
            Article lParentArticle = (Article) event.getNewValue();
            
            try {
                tempContainer =
                        containerFactory.generateContainer(
                                lParentArticle.getContainerType(),
                                lParentArticle.getId(), null, containedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    /**
     * Update the container according to the selected parent installation
     * 
     * @param event
     *            the event sent while the selected name in the list is changed
     */
    public void parentInstallationChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            
            Installation lParentInstallation =
                    (Installation) event.getNewValue();
            
            try {
                tempContainer =
                        containerFactory.generateContainer(ContainerType
                                .valueOf(lParentInstallation.getClass()
                                        .getSimpleName()), lParentInstallation
                                .getId(), null, containedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    /**
     * Update the container according to the selected parent tool
     * 
     * @param event
     *            the event sent while the selected name in the list is changed
     */
    public void parentToolChanged(ValueChangeEvent event) {
        
        if (event.getNewValue() != null) {
            
            Tool lParentTool = (Tool) event.getNewValue();
            
            try {
                tempContainer =
                        containerFactory.generateContainer(
                                ContainerType.valueOf(lParentTool.getClass()
                                        .getSimpleName()), lParentTool.getId(),
                                null, containedType);
            }
            catch (LocalizationException e) {
                Utils.addFacesMessage(
                        NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.LOCALIZATION_UNKNOWN_ERROR));
            }
        }
    }
    
    private void resetSearchAttributes() {
        
        findCriteria.clear();
        inputSearchField = null;
        
        updateSearchLists(false);
    }
    
    /**
     * Update the modal according to the selected container class
     * 
     * @param event
     *            the event sent while the selected container class is changed
     */
    public void selectedClassChanged(ValueChangeEvent event) {
        resetSearchAttributes();
    }
    
    /**
     * Update the lists of classes using the possibleContainerTypes
     */
    private void updateClasses() {
        
        availableClasses.clear();
        availableArticleClasses.clear();
        
        boolean lIsArticle = false;
        boolean lIsInstallation = false;
        boolean lIsTool = false;
        
        for (ContainerType lContainerType : possibleContainerTypes) {
            switch (lContainerType) {
            case Board:
            case Cabinet:
            case PC:
            case Rack:
            case Switch:
                availableArticleClasses.add(new SelectItem(lContainerType,
                        MessageBundle.getMessage(lContainerType.name())));
                lIsArticle = true;
                break;
            case Installation:
                lIsInstallation = true;
                break;
            case Tool:
                lIsTool = true;
                break;
            default:
                break;
            }
        }
        
        // Add the possible container types in the following order:
        // - Installation, - Article, - Tool
        
        if (lIsInstallation) {
            availableClasses
                    .add(new SelectItem(ContainerType.Installation,
                            MessageBundle.getMessage(ContainerType.Installation
                                    .name())));
        }
        
        if (lIsArticle) {
            availableClasses.add(new SelectItem(ARTICLE_LABEL, MessageBundle
                    .getMessage(ARTICLE_LABEL)));
        }
        
        if (lIsTool) {
            availableClasses.add(new SelectItem(ContainerType.Tool,
                    MessageBundle.getMessage(ContainerType.Tool.name())));
        }
        
        // Select the default value for selected class
        if (lIsArticle) {
            selectedClass = ARTICLE_LABEL;
        }
        else if (lIsInstallation) {
            selectedClass = ContainerType.Installation.name();
        }
        else if (lIsTool) {
            selectedClass = ContainerType.Tool.name();
        }
        updateSearchLists(false);
    }
    
    private void updateSearchListArticles() {
        
        // Remove empty parameters
        for (String lKey : new ArrayList<String>(findCriteria.keySet())) {
            
            Object lObject = findCriteria.get(lKey);
            if (lObject == null
                    || (lObject instanceof String && StringUtil
                            .isEmptyOrNull((String) lObject))) {
                findCriteria.remove(lKey);
            }
        }
        
        if (findCriteria.isEmpty()) {
            // no criteria is provided
            foundParentArticles.clear();
            String lMsg =
                    MessageBundle
                            .getMessage(Constants.QUERY_CRITERIA_MUST_BE_ENTERED);
            Utils.addFacesMessage(NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                    lMsg);
            return;
        }
        
        List<Article> lResults = new ArrayList<Article>();
        
        lResults.addAll(searchBean.searchArticle(findCriteria));
        
        // Remove not available articles for being the container of item
        List<Article> lResultsCopy = new ArrayList<Article>(lResults);
        lResults.clear();
        for (Article lArticle : lResultsCopy) {
            if (ContainerManager.isContainerAvailable(lArticle, containedType)
                    && !lArticle.equals(containedArticle)) {
                lResults.add(lArticle);
            }
        }
        
        // sort the found article by name
        Collections.sort(lResults, new ComparatorNameArticle());
        
        int lMaxLimit = getQUERY_MAX_RESULTS();
        int lNbFound = lResults.size();
        
        if ((lNbFound > lMaxLimit) && (lMaxLimit > 0)) {
            
            foundParentArticles = lResults.subList(0, lMaxLimit);
            
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    lNbFound, lMaxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHANGE_CONTAINER_ERROR_ID, lMsg);
        }
        else if (lNbFound == 0) {
            foundParentArticles.clear();
            Utils.addFacesMessage(NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else {
            foundParentArticles = lResults;
        }
    }
    
    private void updateSearchListInstallations() {
        
        List<Installation> lResults = new ArrayList<Installation>();
        
        if (StringUtil.isEmptyOrNull(inputSearchField)) {
            // no criteria is provided
            lResults.addAll(locationBean.findAllInstallation());
        }
        else {
            lResults.addAll(locationBean
                    .findAllInstallationsByName(inputSearchField));
        }
        
        int lMaxLimit = getQUERY_MAX_RESULTS();
        int lNbFound = lResults.size();
        
        if ((lNbFound > lMaxLimit) && (lMaxLimit > 0)) {
            
            foundParentInstallations = lResults.subList(0, lMaxLimit);
            
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    lNbFound, lMaxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHANGE_CONTAINER_ERROR_ID, lMsg);
        }
        else if (lNbFound == 0) {
            foundParentInstallations.clear();
            Utils.addFacesMessage(NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else {
            foundParentInstallations = lResults;
        }
    }
    
    /**
     * Update the lists of found elements which do not need any criteria
     * 
     * @param pForceSearch
     *            true if the search must be performed, false if search should
     *            not be performed if criteria are mandatory for it
     */
    private void updateSearchLists(boolean pForceSearch) {
        
        if (isArticleMode() && pForceSearch) {
            // search for articles needs some criteria so it is performed only
            // if forced
            updateSearchListArticles();
        }
        else if (isInstallationMode()) {
            updateSearchListInstallations();
        }
        else if (isToolMode()) {
            updateSearchListTools();
        }
    }
    
    private void updateSearchListTools() {
        
        List<Tool> lResults = new ArrayList<Tool>();
        
        if (StringUtil.isEmptyOrNull(inputSearchField)) {
            // no criteria is provided
            lResults.addAll(toolBean.findAllTools());
        }
        else {
            lResults.addAll(toolBean.findToolsByName(inputSearchField));
        }
        
        int lMaxLimit = getQUERY_MAX_RESULTS();
        int lNbFound = lResults.size();
        
        if ((lNbFound > lMaxLimit) && (lMaxLimit > 0)) {
            
            foundParentTools = lResults.subList(0, lMaxLimit);
            
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    lNbFound, lMaxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHANGE_CONTAINER_ERROR_ID, lMsg);
        }
        else if (lNbFound == 0) {
            foundParentTools.clear();
            Utils.addFacesMessage(NavigationConstants.CHANGE_CONTAINER_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else {
            foundParentTools = lResults;
        }
    }
    
    /**
     * Check if the temporary precision value is correct
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the temporary precision to valid
     * @throws ValidatorException
     *             when the temporary precision is not valid
     */
    public void validateTempPrecision(FacesContext context,
            UIComponent component, Object value)
            throws ValidatorException {
        
        String lValueStr = (String) value;
        
        if (tempContainer != null) {
            
            String lErrorMsg =
                    tempContainer.getPrecisionValidationError(lValueStr);
            if (!lErrorMsg.isEmpty()) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lErrorMsg, lErrorMsg));
            }
        }
    }
    
    /**
     * @return a boolean indicating if the graphical elements for searching an
     *         article shall be displayed
     */
    public boolean isArticleMode() {
        
        return selectedClass != null && selectedClass.equals(ARTICLE_LABEL);
    }
    
    /**
     * @return a boolean indicating if the graphical elements for searching an
     *         installation shall be displayed
     */
    public boolean isInstallationMode() {
        
        return selectedClass != null
                && selectedClass.equals(ContainerType.Installation.name());
    }
    
    /**
     * @return a boolean indicating if the graphical elements for searching a
     *         tool shall be displayed
     */
    public boolean isToolMode() {
        
        return selectedClass != null
                && selectedClass.equals(ContainerType.Tool.name());
    }
    
    /**
     * @return the list of available article parent families classes
     */
    public List<SelectItem> getArticleClasses() {
        
        return availableArticleClasses;
    }
    
    /**
     * @return the list of available parent families classes
     */
    public List<SelectItem> getClasses() {
        
        return availableClasses;
    }
    
    /**
     * @return the container
     */
    public Container getContainer() {
        return container;
    }
    
    /**
     * @param pContainer
     *            the container to set
     */
    public void setContainer(Container pContainer) {
        container = pContainer;
        if (container != null) {
            tempContainer = new Container(container);
        }
        else {
            tempContainer = null;
        }
    }
    
    /**
     * @return the findCriteria
     */
    public Map<String, Object> getFindCriteria() {
        return findCriteria;
    }
    
    /**
     * @param pFindCriteria
     *            the findCriteria to set
     */
    public void setFindCriteria(Map<String, Object> pFindCriteria) {
        findCriteria = pFindCriteria;
    }
    
    /**
     * @return the foundParentArticles
     */
    public List<Article> getFoundParentArticles() {
        return foundParentArticles;
    }
    
    /**
     * @return the foundParentInstallations
     */
    public List<Installation> getFoundParentInstallations() {
        return foundParentInstallations;
    }
    
    /**
     * @return the foundParentTools
     */
    public List<Tool> getFoundParentTools() {
        return foundParentTools;
    }
    
    /**
     * @return the inputSearchField
     */
    public String getInputSearchField() {
        return inputSearchField;
    }
    
    /**
     * @param pInputSearchField
     *            the inputSearchField to set
     */
    public void setInputSearchField(String pInputSearchField) {
        inputSearchField = pInputSearchField;
    }
    
    /**
     * Compute the master container
     * 
     * @return the computed master container name
     */
    public String getMasterContainerName() {
        
        if (container == null) {
            return MessageBundle.getMessage(Constants.NOT_CONTAINED);
        }
        
        Container lMasterContainer =
                ContainerManager.getMasterContainer(container);
        
        if (lMasterContainer == null) {
            return MessageBundle.getMessage(Constants.NOT_CONTAINED);
        }
        return lMasterContainer.getContainerName();
    }
    
    /**
     * 
     * @return the master container or null if not exists.
     */
    public Container getMasterContainer(){
    	if (container == null) {
            return null;
        }
    	return ContainerManager.getMasterContainer(container);
    }
    
    /**
     * @return the precision label
     */
    public String getPrecisionLabel() {
        
        if (container != null) {
            return container.getPrecisionLabel();
        }
        else {
            return "";
        }
    }
    
    /**
     * @return the QUERY_MAX_RESULTS init parameter, else 0
     */
    private int getQUERY_MAX_RESULTS() {
        
        String lLimit = getInitParameter("QUERY_MAX_RESULTS");
        if (lLimit == null) {
            return 0;
        }
        try {
            return Integer.valueOf(lLimit);
        }
        catch (NumberFormatException e) {
            log.warning("Context variable not correctly defined : QUERY_MAX_RESULTS must be a positive integer");
            return 0;
        }
    }
    
    /**
     * @return the selectedClass
     */
    public String getSelectedClass() {
        return selectedClass;
    }
    
    /**
     * @param pSelectedClass
     *            the selectedClass to set
     */
    public void setSelectedClass(String pSelectedClass) {
        selectedClass = pSelectedClass;
        // update the list of containers
        updateSearchLists(false);
    }
    
    /**
     * @return a boolean indicating if the precision should be displayed
     */
    public boolean isShowPrecision() {
        
        if (container != null) {
            return container.isPrecisionAvailable();
        }
        else {
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the temporary precision should be
     *         displayed
     */
    public boolean isShowTempPrecision() {
        
        if (tempContainer != null) {
            return tempContainer.isPrecisionAvailable();
        }
        else {
            return false;
        }
    }
    
    /**
     * @return the tempContainer
     */
    public Container getTempContainer() {
        return tempContainer;
    }
    
    /**
     * @return the temporary precision label
     */
    public String getTempPrecisionLabel() {
        
        if (tempContainer != null) {
            return tempContainer.getPrecisionLabel();
        }
        else {
            return "";
        }
    }
    
    /**
     * @return the maximum length of the temporary precision
     */
    public int getTempPrecisionMaxLength() {
        if (tempContainer != null) {
            DBConstants lDBConstants = findBean(DBConstants.class);
            return tempContainer.getPrecisionMaxLength(lDBConstants);
        }
        else {
            return 100;
        }
    }
    
    /**
     * @return the tool tip for the temporary precision
     */
    public String getTempPrecisionToolTip() {
        if (tempContainer != null) {
            return tempContainer.getPrecisionToolTip();
        }
        else {
            return "";
        }
    }
    
    /**
     * @return true if the master container is an installation
     */
    public boolean isMasterParentInstallation(){
    	Container lMasterContainer = getMasterContainer();
    	if(lMasterContainer == null){
    		return false;
    	}
    	else{
    		return lMasterContainer.getType().equals(ContainerType.Installation); 
    	}
    	
    }
    
    /**
     * @return the updateLocationAction
     */
    public int getUpdateLocationAction() {
        return updateLocationAction;
    }
    
    /**
     * @param pUpdateLocationAction
     *            the updateLocationAction to set
     */
    public void setUpdateLocationAction(int pUpdateLocationAction) {
        updateLocationAction = pUpdateLocationAction;
    }
    
}
