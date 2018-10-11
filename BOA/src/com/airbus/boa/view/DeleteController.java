/*
 * ------------------------------------------------------------------------
 * Class : DeleteController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ContainerManagerBean;
import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.DocumentBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.LocationManagerBean;
import com.airbus.boa.control.ReminderBean;
import com.airbus.boa.control.RemoveBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.UpdateBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.application.DBConstants;

/**
 * Controller managing the entities deletion
 */
@ManagedBean(name = DeleteController.BEAN_NAME)
@ViewScoped
public class DeleteController extends AbstractController {
    
    private static final long serialVersionUID = -4307572173798802117L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "deleteController";
    
    @EJB
    private RemoveBean removeBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private DemandBean demandBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    @EJB
    private ReminderBean reminderBean;
    
    @EJB
    private DocumentBean documentBean;
    
    /**
     * Airbus or Manufacturer SN of Article to delete
     */
    private String serialNumber;
    /**
     * Name of Installation or Tool to delete
     */
    private String name;
    
    private Article article;
    private Installation installation;
    private Tool tool;
    
    /**
     * Set to none the location of articles located in the article to
     * delete and then remove the article from database.
     * Also remove all related reminders and documents.
     */
    public void doRemove() {
        
        Article lArticle = articleBean.findArticleBySN(serialNumber);
        
        // Check that no demand is located into the selected article
        if (lArticle != null
                && !demandBean.findDemandsLocatedIntoArticle(lArticle)
                        .isEmpty()) {
            Utils.addFacesMessage(NavigationConstants.DEL_ART_ERROR_ID,
                    MessageBundle.getMessage(
                            Constants.ARTICLE_DELETION_CONTAINS_DEMAND));
        }
        else {
            User lCurrentUser = findBean(LogInController.class).getUserLogged();
            
            try {
                removeChildren(lCurrentUser, getChildren(), serialNumber);
                removeReminders(getArticleReminders());
                removeDocuments(getArticleDocuments());
                removeBean.removeArticle(serialNumber);
                Utils.addInfoMessage(NavigationConstants.DEL_ART_ERROR_ID,
                        MessageBundle.getMessageResource(
                        Constants.SUCCESSFUL_DELETE,
                        new Object[] { serialNumber }));
                serialNumber = null;
                article = null;
                
            } catch (ValidationException e) {
                Utils.addFacesMessage(NavigationConstants.DEL_ART_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }
    
    /**
     * Set to none the container of articles and tools linked in the
     * installation to delete and then remove the installation from database.
     * Also remove all related reminders and documents.
     */
    public void doRemoveInstallation() {
        try {
            
            // Check that no demand is located into the selected installation
            if (!demandBean.findDemandsLocatedIntoInstallation(
                    getInstallation()).isEmpty()) {
                Utils.addFacesMessage(
                        NavigationConstants.DEL_INST_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.INSTALLATION_DELETION_CONTAINS_DEMAND));
            }
            else {            
                User lCurrentUser =
                        findBean(LogInController.class).getUserLogged();
                
                removeChildren(lCurrentUser, getChildrenInstallation(),
                        installation.getName());
                // Retrieve the installation from the database again, to avoid
                // synchronization problems
                installation = locationBean
                        .findInstallationByName(installation.getName());
                removeTools(getInstallationTools());
                removeReminders(getInstallationReminders());
                removeDocuments(getInstallationDocuments());
                removeBean.removeInstallation(installation);
                
                Utils.addInfoMessage(NavigationConstants.DEL_INST_ERROR_ID,
                        MessageBundle.getMessageResource(
                        Constants.SUCCESSFUL_DELETE,
                        new Object[] { installation.getName() }));
                name = null;
                installation = null;
                
            }
        }
        catch (ValidationException e) {
            Utils.addFacesMessage(NavigationConstants.DEL_INST_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Set to none the location of articles contained by the tool to delete
     * and then remove the tool from database.
     * Also remove all related reminders and documents.
     */
    public void doRemoveTool() {
        
        try {
            // Check that no demand is located into the selected tool
            if (!demandBean.findDemandsLocatedIntoTool(tool).isEmpty()) {
                Utils.addFacesMessage(NavigationConstants.DEL_TOOL_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.TOOL_DELETION_CONTAINS_DEMAND));
            }
            else {
                User lCurrentUser =
                        findBean(LogInController.class).getUserLogged();
                
                removeChildren(lCurrentUser, getChildrenTool(), tool.getName());
                removeReminders(getToolReminders());
                removeDocuments(getToolDocuments());
                toolBean.remove(tool);
                
                Utils.addInfoMessage(NavigationConstants.DEL_TOOL_ERROR_ID,
                        MessageBundle.getMessageResource(
                        Constants.SUCCESSFUL_DELETE,
                        new Object[] { tool.getName() }));
                name = null;
                tool = null;
            }
        }
        catch (ValidationException e) {
            Utils.addFacesMessage(NavigationConstants.DEL_TOOL_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Remove the documents.
     * 
     * @param pDocuments
     *            the documents linked to the entity to delete
     */
    private void removeDocuments(List<Document> pDocuments) {
        
        for (Document lDocument : pDocuments) {
            documentBean.remove(lDocument);
        }
    }
    
    /**
     * Set container of provided children to none in order to remove them
     * from the entity to delete. Moreover, if the child location is inherited
     * from parent, the child location is set to the parent location and to not
     * inherited.
     * 
     * @param pCurrentUser
     *            the user performing the entity deletion
     * @param pChildren
     *            the entities contained by the entity to delete
     * @param pParentName
     *            the entity name
     */
    public void removeChildren(User pCurrentUser,
            List<? extends Article> pChildren, String pParentName) {
        
        // Patch to initialize the managed bean before calling the saveChange
        // method of the update bean
        AbstractController.findBean(DBConstants.class);
        
        UpdateBean lUpdateBean;
        try {
            Context lCtx = new InitialContext();
            lUpdateBean = (UpdateBean) lCtx.lookup("java:module/UpdateBean");
        }
        catch (NamingException e) {
            lUpdateBean = null;
            throw new ValidationException(e);
        }
        
        for (Article lArticle : pChildren) {
            
            lUpdateBean.setArticle(lArticle);
            lUpdateBean.setCurrentUser(pCurrentUser);
            lUpdateBean.setContainer(null);
            
            Container lContainer = lArticle.getContainer();
            Location lLocation = lArticle.getLocation();
            if (lLocation != null && lLocation.isInherited()) {
                try {
                    lLocation =
                            LocationManager.updateInherited(lLocation, false,
                                    lContainer, lArticle.getLocatedType());
                }
                catch (LocalizationException e) {
                    // Location is not updated in case of error
                }
                lUpdateBean.setLocation(lLocation);
            }
            
            for (Action lAction : lUpdateBean.getActions()) {
                Comment lComment =
                        new Comment(MessageBundle.getMessageResourceDefault(
                                "deletionOf", new Object[] { pParentName }));
                lAction.setComment(lComment);
            }
            lUpdateBean.saveChange(null);
        }
        lUpdateBean.remove();
        lUpdateBean = null;
    }
    
    /**
     * Remove the reminders.
     * 
     * @param pReminders
     *            the reminders related to the entity to delete
     */
    private void removeReminders(List<Reminder> pReminders) {
        
        for (Reminder lReminder : pReminders) {
            reminderBean.remove(lReminder);
        }
    }
    
    /**
     * Set parent of provided tools to none in order to remove them from the
     * installation to delete. Moreover, if the child location is inherited from
     * parent, the child location is set to the parent location and to not
     * inherited.
     * 
     * @param pTools
     *            the tools contained by the installation to delete
     */
    private void removeTools(List<Tool> pTools) {
        
        for (Tool lTool : pTools) {
            
            Container lContainer = lTool.getContainer();
            Location lLocation = lTool.getLocation();
            
            ContainerManager lContainerManager = new ContainerManager(lTool);
            lContainerManager.unlink(containerManagerBean);
            
            if (lLocation != null && lLocation.isInherited()) {
                try {
                    lLocation =
                            LocationManager.updateInherited(lLocation, false,
                                    lContainer, lTool.getLocatedType());
                    LocationManager lLocationManager =
                            new LocationManager(lTool);
                    lLocationManager.moveTo(lLocation, locationManagerBean);
                }
                catch (LocalizationException e) {
                    // Location is not updated in case of error
                }
            }
        }
    }
    
    /**
     * Check if the provided installation name exists.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the installation name to valid
     * @throws ValidatorException
     *             when the provided installation name does not exist
     */
    public void validateExistInstallation(FacesContext pContext,
            UIComponent pComponent, Object pValue)
            throws ValidatorException {
        
        String lName = (String) pValue;
        if (lName == null || lName.trim().equals("")
                || !locationBean.isInstallationExisting(lName)) {
            
            installation = null;
            String lMsg =
                    MessageBundle.getMessage(Constants.INSTALLATION_UNKNOWN);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        else {
            installation = locationBean.findInstallationByName(lName);
        }
    }
    
    /**
     * Check if the provided article SN exists and is unique. <br>
     * Update the article attribute.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the article SN to valid
     * @throws ValidatorException
     *             when the provided article SN does not exist or is not unique
     */
    public void validateExistSN(FacesContext pContext, UIComponent pComponent,
            Object pValue)
            throws ValidatorException {
        
        String lSerialNumber = (String) pValue;
        if (lSerialNumber == null
                || lSerialNumber.trim().equals("")
                || !(articleBean.existASN(lSerialNumber) || articleBean
                        .existMSN(lSerialNumber))) {
            
            article = null;
            String lMsg = MessageBundle.getMessage(Constants.SN_UNKNOWN);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        try {
            Article lArticle = articleBean.findArticleBySN(lSerialNumber);
            if (!(lArticle instanceof PC)) {
                article = lArticle;
            }
            else {
                // PC deletion must be performed by the PCController
                article = null;
            }
            
        }
        catch (ValidationException e) {
            article = null;
            
            String msg = MessageBundle.getMessage(Constants.SN_NON_UNIQUE);
            
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }
    
    /**
     * Check if the provided tool name exists.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the tool name to valid
     * @throws ValidatorException
     *             when the provided tool name does not exist
     */
    public void validateExistTool(FacesContext pContext,
            UIComponent pComponent, Object pValue)
            throws ValidatorException {
        
        String lToolName = (String) pValue;
        if (lToolName == null || lToolName.trim().equals("")
                || !toolBean.existToolName(lToolName)) {
            
            tool = null;
            String lMsg = MessageBundle.getMessage(Constants.TOOL_UNKNOWN);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        else {
            tool = toolBean.findToolByName(lToolName);
        }
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
        
    }
    
    /**
     * Return, for the current article, the list of documents linked to it or an
     * empty list
     * 
     * @return the articleDocuments
     */
    public List<Document> getArticleDocuments() {
        if (article != null) {
            return documentBean.findDocumentsByArticle(article);
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the current article, the list of reminders related to it or
     * an empty list
     * 
     * @return the articleReminders
     */
    public List<Reminder> getArticleReminders() {
        if (article != null) {
            return reminderBean.findRemindersByArticle(article);
        }
        return Collections.emptyList();
    }
    
    /**
     * Return the current article children or an empty list
     * 
     * @return the children
     */
    public List<? extends Article> getChildren() {
        if (article != null) {
            return article.getChildren();
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the installation corresponding to the name, the list of
     * childrenUpdate the children or an empty list
     * 
     * @return the childrenInstallation
     */
    public List<? extends Article> getChildrenInstallation() {
        if (installation != null) {
            return installation.getChildren();
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the tool corresponding to the name, the list of children or
     * an empty list
     * 
     * @return the childrenTool
     */
    public List<? extends Article> getChildrenTool() {
        if (tool != null) {
            return tool.getChildren();
        }
        return Collections.emptyList();
    }
    
    /**
     * @return the installation
     */
    public Installation getInstallation() {
        return installation;
    }
    
    /**
     * Return, for the installation corresponding to the name, the list of
     * documents linked to it or an empty list
     * 
     * @return the installationDocuments
     */
    public List<Document> getInstallationDocuments() {
        if (installation != null) {
            return documentBean.findDocumentsByInstallation(installation);
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the installation corresponding to the name, the list of
     * reminders related to it or an empty list
     * 
     * @return the installationReminders
     */
    public List<Reminder> getInstallationReminders() {
        if (installation != null) {
            return reminderBean.findRemindersByInstallation(installation);
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the installation corresponding to the name, the list of tools
     * used by it or an empty list
     * 
     * @return the installationTools
     */
    public List<Tool> getInstallationTools() {
        if (installation != null) {
            return installation.getContainedTools();
        }
        return Collections.emptyList();
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }
    
    /**
     * @param serialNumber
     *            the serialNumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }
    
    /**
     * Return, for the tool corresponding to the name, the list of documents
     * linked to it or an empty list
     * 
     * @return the toolDocuments
     */
    public List<Document> getToolDocuments() {
        if (tool != null) {
            return documentBean.findDocumentsByTool(tool);
        }
        return Collections.emptyList();
    }
    
    /**
     * Return, for the tool corresponding to the name, the list of reminders
     * related to it or an empty list
     * 
     * @return the toolReminders
     */
    public List<Reminder> getToolReminders() {
        if (tool != null) {
            return reminderBean.findRemindersByTool(tool);
        }
        return Collections.emptyList();
    }
    
}
