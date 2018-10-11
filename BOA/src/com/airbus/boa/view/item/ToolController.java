/*
 * ------------------------------------------------------------------------
 * Class : ToolController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import com.airbus.boa.control.SearchBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleClass;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.location.Contains_Tool_Article;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.explorer.view.ExplorerController;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.ComparatorNameArticle;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.ContainerController;
import com.airbus.boa.view.DocumentController;
import com.airbus.boa.view.LocationController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.ReminderController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.application.DBConstants;

/**
 * Controller managing the tool creation, consultation and modification
 */
@ManagedBean(name = ToolController.BEAN_NAME)
@ViewScoped
public class ToolController extends AbstractItemController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "toolController";
    
    private long toolId;
    
    private Tool tool = null;
    /** The displayed tool articles list */
    private List<Article> toolComponents = new ArrayList<Article>();
    /** The displayed comments map classified by article id */
    private Map<Long, String> toolComponentsComments =
            new HashMap<Long, String>();
    /**
     * A map indicating if the component is currently located in tool (in
     * database)
     */
    private Map<Long, Boolean> toolComponentsInDatabase;
    
    private final int COMMENT_LENGTH;
    
    private Article selectedArticle;
    
    private Map<String, Object> findCriteria = new HashMap<String, Object>();
    private List<Article> componentsFound = new ArrayList<Article>();
    private List<Article> componentsSelected = new ArrayList<Article>();
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private SearchBean searchBean;
    
    /**
     * Default constructor
     */
    public ToolController() {
        
        DBConstants lDBConstants = findBean(DBConstants.class);
        
        COMMENT_LENGTH = lDBConstants.getCommentMessageLength();
        itemPage = NavigationConstants.TOOL_MANAGEMENT_PAGE;
        listPage = NavigationConstants.TOOL_LIST_PAGE;
        resultPage = listPage;
        errorId = NavigationConstants.TOOL_MGMT_ERROR_ID;
    }
    
    @Override
    protected void setItemId(Long pId) {
        toolId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return toolId;
    }
    
    @Override
    protected void deleteItem() throws Exception {
        return;
    }
    
    @Override
    protected void createItem() throws Exception {
        
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();
        LocationManager.validateLocation(lLocation);
        
        ContainerController lContainerCtrl =
                findBean(ContainerController.class);
        Container lContainer = lContainerCtrl.getContainer();
        
        // retrieve current user
        User lUser = null;
        LogInController lLogInController = findBean(LogInController.class);
        
        if (lLogInController != null) {
            lUser = lLogInController.getUserLogged();
        }
        
        String lLogin = null;
        if (lUser != null) {
            lLogin = lUser.getLogin();
        }
        
        // create the tool in database
        tool =
                toolBean.create(tool, toolComponents, toolComponentsComments,
                        lLocation, lContainer, lLogin);
        toolId = tool.getId();
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(tool);
        
        // Reset Container and Location to new value
        lLocationCtrl.setLocation(lLocation);
        lContainerCtrl.setContainer(lContainer);
    }

    /**
     * Update the componentsFound by searching articles satisfying the
     * findCriteria
     */
    public void doFind() {
        
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
            componentsFound.clear();
            String msg =
                    MessageBundle
                            .getMessage(Constants.QUERY_CRITERIA_MUST_BE_ENTERED);
            Utils.addFacesMessage(NavigationConstants.CHOOSE_COMPONENT_ERROR_ID,
                    msg);
        }
        else {
        
            List<Article> lResults = new ArrayList<Article>();
            
            lResults.addAll(searchBean.searchArticle(findCriteria));
            
            lResults = extractAuthorizedAsComponent(lResults);
            
            // sort the found article by name
            Collections.sort(lResults, new ComparatorNameArticle());
            
            int lMaxLimit = getQUERY_MAX_RESULTS();
            int lNbFound = lResults.size();
            
            if ((lResults.size() > lMaxLimit) && (lMaxLimit > 0)) {
                
                componentsFound = lResults.subList(0, lMaxLimit);
                
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.QUERY_MAX_MODAL_EXCEEDED,
                                new Object[] { lNbFound, lMaxLimit });
                Utils.addWarningMessage(
                        NavigationConstants.CHOOSE_COMPONENT_ERROR_ID, lMsg);
            }
            else {
                componentsFound = lResults;
            }
        }
    }
    
    /**
     * Add the selected components
     */
    public void doModalChoose() {
        
        if (componentsSelected != null && !componentsSelected.isEmpty()) {
            for (Article lComponent : componentsSelected) {
                
                if (!toolComponents.contains(lComponent)) {
                    
                    toolComponents.add(lComponent);
                    
                    if (tool.getChildren().contains(lComponent)) {
                        // The component was already in the tool
                        // Retrieve the component comment in database
                        if (lComponent.getContainerOrmTool() != null) {
                            toolComponentsComments.put(lComponent.getId(),
                                    lComponent.getContainerOrmTool()
                                            .getComment());
                        }
                        else {
                            toolComponentsComments.put(lComponent.getId(), "");
                        }
                        
                        toolComponentsInDatabase.put(lComponent.getId(), true);
                    }
                    else {
                        toolComponentsComments.put(lComponent.getId(), "");
                        
                        toolComponentsInDatabase.put(lComponent.getId(), false);
                    }
                }
            }
        }
    }
    
    @Override
    protected void initItemWithNew() {
        tool = new Tool();
    }
    
    @Override
    protected void initItemFromDatabase() {
        tool = toolBean.findToolById(toolId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        if (tool != null) {
            initToolComponentsLists();
            
            // used by locationController and containerController
            getSession().setAttribute("item", tool);
            
            // location reset
            LocationController lLocationController =
                    findBean(LocationController.class);
            lLocationController.setLocation(tool.getLocation());
            lLocationController.setLocatedItem(tool);
            
            // container reset
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            lContainerController.setContainer(tool.getContainer());
            lContainerController.setContainedItem(tool);
            
            // Document reset
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(tool);

            // Reminder reset
            ReminderController lReminderController =
                    findBean(ReminderController.class);
            lReminderController.setMode(tool);
            
            // Explorer reset
            ExplorerController controller = findBean(ExplorerController.class);
            controller.setRootNode(tool);
        }
    }
    
    /**
     * Remove the component from the tool. <br>
     * <b><i>selectedArticle</i></b> must be initialized before calling
     */
    public void doRemoveArticle() {
        toolComponents.remove(selectedArticle);
        toolComponentsComments.remove(selectedArticle.getId());
        toolComponentsInDatabase.remove(selectedArticle.getId());
    }
    
    /**
     * Reset attributes for search of components
     */
    public void doResetFindField() {
        findCriteria.clear();
        componentsSelected.clear();
        componentsFound.clear();
    }
    
    @Override
    public void updateItem() throws Exception {
        
        LocationController lLocationController =
                findBean(LocationController.class);
        Location lLocation = lLocationController.getLocation();
        LocationManager.validateLocation(lLocation);
        
        ContainerController lContainerController =
                findBean(ContainerController.class);
        Container lContainer = lContainerController.getContainer();
        
        LogInController lLogInController = findBean(LogInController.class);
        User lUser = lLogInController.getUserLogged();
        String lLogin = null;
        if (lUser != null) {
            lLogin = lUser.getLogin();
        }
        
        tool =
                toolBean.merge(tool, toolComponents, toolComponentsComments,
                        lLocation, lContainer, lLogin);
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(tool);
        
        // Reset Container and Location to new value
        lLocationController.setLocation(lLocation);
        lContainerController.setContainer(lContainer);
    }
    
    /**
     * Remove from the provided articles list the articles which class is not
     * authorized as a tool component
     * 
     * @param pArticles
     *            the articles list
     * @return an articles list containing only authorized articles
     */
    private List<Article> extractAuthorizedAsComponent(List<Article> pArticles) {
        
        List<Article> lComponents = new ArrayList<Article>();
        for (Article lArticle : pArticles) {
            if (isAuthorizedAsComponent(lArticle)) {
                lComponents.add(lArticle);
            }
        }
        return lComponents;
    }
    
    /**
     * Initialize the toolComponents and toolComponentsComments lists using the
     * tool children
     */
    private void initToolComponentsLists() {
        
        toolComponents = new ArrayList<Article>();
        toolComponentsComments = new HashMap<Long, String>();
        toolComponentsInDatabase = new HashMap<Long, Boolean>();
        
        for (Article lArticle : tool.getChildren()) {
            
            toolComponents.add(lArticle);
            
            Contains_Tool_Article lRelation = lArticle.getContainerOrmTool();
            if (lRelation != null && lRelation.getComment() != null) {
                toolComponentsComments.put(lArticle.getId(),
                        lRelation.getComment());
            }
            else {
                toolComponentsComments.put(lArticle.getId(), "");
            }
            
            toolComponentsInDatabase.put(lArticle.getId(), true);
        }
    }
    
    /**
     * Check if the provided tool name is valid for updating the selected tool.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the tool name to valid
     * @throws ValidatorException
     *             when the provided tool name is not valid
     */
    public void validateToolName(FacesContext pContext, UIComponent pComponent,
            Object pValue)
            throws ValidatorException {
        
        String lValue = (String) pValue;
        
        if (lValue == null) {
            lValue = "";
        }
        else {
            lValue = lValue.trim();
        }
        
        // Check that entered tool name is not empty
        if (lValue.isEmpty()) {
            
            String lMsg =
                    MessageBundle
                            .getMessage(Constants.TOOL_NAME_MUST_BE_FILLED);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        /*
         * If controller is in creation mode or
         * if controller is in update mode and tool name has changed,
         * check that entered tool name does not already exist
         */
        if (isCreateMode()
                || (isUpdateMode() && (!tool.getName().equals(lValue)))) {
            
            if (toolBean.existToolName((String) pValue)) {
                
                String lMsg =
                        MessageBundle
                                .getMessage(Constants.TOOL_NAME_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    /**
     * Determine if the provided article is of an authorized class for tool
     * components
     * 
     * @param pArticle
     *            the article to valid as component
     * @return a boolean indicating if the article is authorized as component
     */
    private boolean isAuthorizedAsComponent(Article pArticle) {
        
        if (pArticle == null) {
            return false;
        }
        for (ArticleClass lClass : Tool.COMPONENTS_CLASSES) {
            
            if (pArticle.getClass().getSimpleName().equals(lClass.name())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return the number of available characters into the comment field
     */
    public int getAvailableChars() {
        if (tool.getGeneralComment() != null
                && tool.getGeneralComment().getMessage() != null) {
            return COMMENT_LENGTH
                    - tool.getGeneralComment().getMessage().length();
        }
        return COMMENT_LENGTH;
    }
    
    /**
     * @return the list of available tool components classes
     */
    public SelectItem[] getClasses() {
        
        ArticleClass[] lValues = Tool.COMPONENTS_CLASSES;
        int lLength = lValues.length;
        
        SelectItem[] lFamilies = new SelectItem[lLength];
        
        for (int i = 0; i < lLength; i++) {
            lFamilies[i] =
                    new SelectItem(lValues[i],
                            MessageBundle.getMessage(lValues[i].toString()));
        }
        return lFamilies;
    }
    
    /**
     * @return the componentsFound
     */
    public List<Article> getComponentsFound() {
        return componentsFound;
    }
    
    /**
     * @param pComponentsFound
     *            the componentsFound to set
     */
    public void setComponentsFound(List<Article> pComponentsFound) {
        componentsFound = pComponentsFound;
    }
    
    /**
     * @return the componentsSelected
     */
    public List<Article> getComponentsSelected() {
        return componentsSelected;
    }
    
    /**
     * @param pComponentsSelected
     *            the componentsSelected to set
     */
    public void setComponentsSelected(List<Article> pComponentsSelected) {
        componentsSelected = pComponentsSelected;
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
     * @return the selectedArticle
     */
    public Article getSelectedArticle() {
        return selectedArticle;
    }
    
    /**
     * @param pSelectedArticle
     *            the selectedArticle to set
     */
    public void setSelectedArticle(Article pSelectedArticle) {
        selectedArticle = pSelectedArticle;
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createToolTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoToolTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateToolTitle");
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }
    
    /**
     * @return the toolComponents
     */
    public List<Article> getToolComponents() {
        return toolComponents;
    }
    
    /**
     * @return the toolComponentsComments
     */
    public Map<Long, String> getToolComponentsComments() {
        return toolComponentsComments;
    }
    
    /**
     * @param pToolComponentsComments
     *            the toolComponentsComments to set
     */
    public void setToolComponentsComments(
            Map<Long, String> pToolComponentsComments) {
        toolComponentsComments = pToolComponentsComments;
    }
    
    /**
     * @return the toolComponentsInDatabase
     */
    public Map<Long, Boolean> getToolComponentsInDatabase() {
        return toolComponentsInDatabase;
    }
    
    /**
     * @return the toolGeneralComment
     */
    public String getToolGeneralComment() {
        
        if (tool.getGeneralComment() != null) {
            return tool.getGeneralComment().getMessage();
        }
        return null;
    }
    
    /**
     * @param pToolGeneralComment
     *            the toolGeneralComment to set
     */
    public void setToolGeneralComment(String pToolGeneralComment) {
        
        if (tool.getGeneralComment() == null) {
            tool.setGeneralComment(new Comment(pToolGeneralComment));
        }
        else {
            tool.getGeneralComment().setMessage(pToolGeneralComment);
        }
    }
    
}
