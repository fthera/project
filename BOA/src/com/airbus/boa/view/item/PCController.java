/*
 * ------------------------------------------------------------------------
 * Class : PCController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.DocumentBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ReminderBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Contains_PC_Board;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.network.NetworkInterface;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.AutoUpdateException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.explorer.view.ExplorerController;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.HistoryConstants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.CollectionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AutoUpdateController;
import com.airbus.boa.view.ComPortController;
import com.airbus.boa.view.ContainerController;
import com.airbus.boa.view.DatedCommentController;
import com.airbus.boa.view.DeleteController;
import com.airbus.boa.view.DocumentController;
import com.airbus.boa.view.LocationController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.PCSpecificityController;
import com.airbus.boa.view.ReminderController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.obso.ReportingObsoController;

/**
 * Controller managing the PC creation, consultation, modification and deletion
 */
@ManagedBean(name = PCController.BEAN_NAME)
@ViewScoped
public class PCController extends AbstractItemController {
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "PCController";
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    protected HistoryBean historyBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private DemandBean demandBean;
    
    @EJB
    private ReminderBean reminderBean;
    
    @EJB
    private DocumentBean documentBean;
    
    private PC pc;

    private Long PCId;
    
    private InputStream sysmonInputStream = null;
    
    private String airbusSN;
    
    private String manufacturerSN;
    
    private String name;
    
    private Long departmentInChargeId;
    
    private String function;
    
    private Integer nbScreens;
    
    private Long domainId;
    
    private String platform;
    
    private Long productTypeId;
    
    private Long typePCId;
    
    private String typePCLabel;

    private String admin;
    
    private ArticleState state;
    
    private UseState useState;
    
    private Date acquisitionDate = new Date();
    
    private String comment;
    
    private Long defaultOSId;
    
    private Long allocationId;
    
    private Long inChargeId;
    
    private String personInCharge;

    private String owner;
    
    private String ownerSiglum;
    
    private Long usageId;
    
    private String assignment;
    
    private List<Software> softwares = new ArrayList<Software>();
    
    private Software software;
    
    private PC.AvailabilityStatus availabilityStatus;
    
    private Date availabilityDate;
    
    private String historyComment = null;
    
    private boolean needToSetDefaultOS = false;

    /**
     * List of user changes to valid
     */
    private Map<String, Action> changes = new HashMap<String, Action>();
    
    /**
     * List of network interfaces (PC ports and network boards located in PC
     * ports)
     */
    private List<NetworkInterface> networkInterfaces =
            new ArrayList<NetworkInterface>();

    public PCController() {
        itemPage = NavigationConstants.PC_MANAGEMENT_PAGE;
        listPage = NavigationConstants.PC_LIST_PAGE;
        resultPage = NavigationConstants.ARTICLE_SEARCH_RESULT_PAGE;
        errorId = NavigationConstants.PC_MGMT_ERROR_ID;
    }

    /**
     * Create an action for the property modification if necessary
     * and add it to the list of user changes to valid
     * 
     * @param pLogin
     *            the login of the author of the modification
     * @param pProperty
     *            the modified property name
     * @param pNewValue
     *            the property new value
     * @param pOldValue
     *            the property old value
     */
    private void addChangeInList(String pLogin, String pProperty,
            String pNewValue, String pOldValue) {
        
        // When old value is null and new value is null or empty
        if (pOldValue == null && (pNewValue == null || pNewValue.isEmpty())) {
            return;
        }
        
        // When new is null or different from old value
        if (pNewValue == null || !pNewValue.equals(pOldValue)) {
            
            // Create an action corresponding to the property modification
            FieldModification newModif =
                    new FieldModification(pLogin, null, Constants.MODIFICATION,
                            null, pProperty, pOldValue, pNewValue);
            
            // Add the action to the user changes to valid
            changes.put(pProperty, newModif);
        }
    }
    
    @Override
    protected void createItem() throws Exception {
        
        LogInController lLogInController = findBean(LogInController.class);
        User lUser = lLogInController.getUserLogged();
        
        /*
         * Check of required fields is performed by the web page
         */
        
        DepartmentInCharge lDepartmentInCharge =
                getDepartmentInChargeValue(departmentInChargeId);
        ProductTypePC lProductType = getProductTypeValue(productTypeId);
        
        BusinessAllocationPC lAllocation =
                valueListBean.findAttributeValueListById(
                        BusinessAllocationPC.class, allocationId);
        
        BusinessUsagePC lUsage = valueListBean
                .findAttributeValueListById(BusinessUsagePC.class, usageId);
        
        User lInCharge = getUserInCharge(inChargeId);
        
        TypeArticle lType = articleBean.findTypeArticleById(typePCId);
        
        PC.AvailabilityStatus lAvailabilityStatus = null;
        if (UseState.isOnPurchase(useState)) {
            lAvailabilityStatus = PC.AvailabilityStatus.New;
        }
        else {
            lAvailabilityStatus = PC.AvailabilityStatus.InUse;
        }
        
        /*
         * Check that PC name is valid
         */
        validatePCName(name);
        
        /*
         * Set the mandatory fields
         */
        pc.setAirbusSN(airbusSN);
        pc.setDepartmentInCharge(lDepartmentInCharge);
        pc.setProductType(lProductType);
        pc.setName(name);
        pc.setTypeArticle(lType);
        pc.setAllocation(lAllocation);
        pc.setUsage(lUsage);
        pc.setInCharge(lInCharge);
        pc.setOwner(owner);
        pc.setOwnerSiglum(ownerSiglum);
        pc.setAvailabilityStatus(lAvailabilityStatus);
        
        /*
         * Update optional fields
         */
        
        Domain lDomain = null;
        if (domainId != null) {
            lDomain = valueListBean.findAttributeValueListById(Domain.class,
                    domainId);
        }
        
        pc.setDomain(lDomain);
        pc.setAdmin(admin);
        pc.setAcquisitionDate(acquisitionDate);
        pc.setFunction(function);
        pc.setComment(comment);
        pc.setAssignment(assignment);
        pc.setNbScreens(nbScreens);
        pc.setState(state);
        pc.setUseState(useState);
        pc.setManufacturerSN(manufacturerSN);
        pc.setPlatform(platform);
        
        if (defaultOSId != null) {
            pc.setDefaultOS(softwareBean.findById(defaultOSId));
        }
        else {
            pc.setDefaultOS(null);
        }
        
        // Initialize the communication ports list
        ComPortController lComPortController =
                findBean(ComPortController.class);
        List<CommunicationPort> lPorts = lComPortController.getPorts();
        pc.setPorts(lPorts);
        
        // Update installed software products
        pc.setSoftwares(softwares);
        
        // Location
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();

        
        if (!useState.equals(UseState.Archived)) {
            LocationManager.validateLocation(lLocation);
        }
        
        // Container
        ContainerController lContainerCtrl =
                findBean(ContainerController.class);
        Container lContainer = lContainerCtrl.getContainer();
        
        /*
         * Check that no PC already exists with same Airbus SN
         */
        if (pcBean.exist(airbusSN)) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.SN_ALREADY_USED));
        }
        
        // Add the acquisition action
        Action.createCreationAction(lUser, pc, Constants.CREATION_BY_INTERFACE);
        
        // Initialize the specificity list
        PCSpecificityController lPCSpecificityController =
                findBean(PCSpecificityController.class);
        List<PCSpecificity> lSpecificities =
                lPCSpecificityController.getSpecificities();
        
        for (PCSpecificity lSpecificity : lSpecificities) {
            pc.getSpecificities().add(lSpecificity);
        }
        
        // Initialize the dated comment list
        DatedCommentController lDatedCommentController =
                findBean(DatedCommentController.class);
        List<DatedComment> lDatedComments =
                lDatedCommentController.getDatedComments();
        
        for (DatedComment lDatedComment : lDatedComments) {
            pc.getDatedComments().add(lDatedComment);
        }
        
        // Create the PC in database
        pc = pcBean.create(pc, lLocation, lContainer);
        PCId = pc.getId();
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(pc);

    }
    
    @Override
    public void updateItem() throws Exception {
        
        LogInController lLogInController = findBean(LogInController.class);
        User lUser = lLogInController.getUserLogged();
        String lLogin;
        if (lUser != null) {
            lLogin = lUser.getLogin();
        }
        else {
            lLogin = null;
        }
        
        /*
         * Check of required fields is performed by the web page
         */
        
        changes.clear();
        
        // Retrieve PC from database
        PC lModifiedPC = pcBean.findById(PCId);
        
        /*
         * Update PC with mandatory fields
         */
        if (departmentInChargeId != null && !departmentInChargeId
                .equals(pc.getDepartmentInCharge().getId())) {
            
            DepartmentInCharge lDepartmentInCharge =
                    getDepartmentInChargeValue(departmentInChargeId);
            lModifiedPC.setDepartmentInCharge(lDepartmentInCharge);
            
            addChangeInList(lLogin, HistoryConstants.DEPARTMENT_IN_CHARGE,
                    lDepartmentInCharge.getDefaultValue(),
                    pc.getDepartmentInCharge().getDefaultValue());
        }
        
        if (productTypeId != null
                && !productTypeId.equals(pc.getProductType().getId())) {
            
            ProductTypePC lProductType = getProductTypeValue(productTypeId);
            lModifiedPC.setProductType(lProductType);
            
            addChangeInList(lLogin, HistoryConstants.PRODUCT_TYPE,
                    lProductType.getDefaultValue(), pc.getProductType()
                            .getDefaultValue());
        }
        
        if (allocationId != null
                && !allocationId.equals(pc.getAllocation().getId())) {

            
            BusinessAllocationPC lAllocation =
                    valueListBean.findAttributeValueListById(
                            BusinessAllocationPC.class, allocationId);
            lModifiedPC.setAllocation(lAllocation);
            
            addChangeInList(lLogin, HistoryConstants.BUSINESS_ALLOCATION,
                    lAllocation.getDefaultValue(), pc.getAllocation()
                            .getDefaultValue());
        }
        
        if (inChargeId != null && !inChargeId.equals(pc.getInCharge().getId())) {
            
            User lInCharge = getUserInCharge(inChargeId);
            lModifiedPC.setInCharge(lInCharge);
            
            addChangeInList(lLogin, HistoryConstants.IN_CHARGE,
                    lInCharge.getLoginDetails(), pc.getInCharge()
                            .getLoginDetails());
        }
        
        if (usageId != null && !usageId.equals(pc.getUsage().getId())) {

            
            BusinessUsagePC lUsage = valueListBean
                    .findAttributeValueListById(BusinessUsagePC.class, usageId);
            lModifiedPC.setUsage(lUsage);
            
            addChangeInList(lLogin, HistoryConstants.BUSINESS_USAGE,
                    lUsage.getDefaultValue(), pc.getUsage().getDefaultValue());
        }
        
        if (typePCId != null && !typePCId.equals(pc.getTypeArticle().getId())) {
            
            TypeArticle lTypePC = articleBean.findTypeArticleById(typePCId);
            lModifiedPC.setTypeArticle(lTypePC);
            
            addChangeInList(lLogin, HistoryConstants.TYPE_ARTICLE,
                    lTypePC.getLabel(),
                    pc.getTypeArticle().getLabel());
        }
        
        lModifiedPC.setAirbusSN(airbusSN);
        addChangeInList(lLogin, HistoryConstants.AIRBUS_SN, airbusSN,
                pc.getAirbusSN());
        
        lModifiedPC.setManufacturerSN(manufacturerSN);
        addChangeInList(lLogin, HistoryConstants.MANUFACTURER_SN,
                manufacturerSN,
                pc.getManufacturerSN());
        
        lModifiedPC.setOwner(owner);
        addChangeInList(lLogin, HistoryConstants.OWNER, owner, pc.getOwner());
        
        lModifiedPC.setOwnerSiglum(ownerSiglum);
        addChangeInList(lLogin, HistoryConstants.OWNER_SIGLUM, ownerSiglum,
                pc.getOwnerSiglum());
        
        /*
         * Check that PC name is valid
         */
        validatePCName(name);
        
        lModifiedPC.setName(name);
        addChangeInList(lLogin, Constants.Name, name, pc.getName());
        
        /*
         * Update PC with optional fields
         */
        
        lModifiedPC.setAdmin(admin);
        addChangeInList(lLogin, HistoryConstants.ADMIN, admin, pc.getAdmin());
        
        lModifiedPC.setState(state);
        addChangeInList(lLogin, HistoryConstants.ARTICLE_STATE,
                state.getStringValue(),
                pc.getState().getStringValue());
        
        lModifiedPC.setUseState(useState);
        addChangeInList(lLogin, HistoryConstants.ARTICLE_USE_STATE,
                useState.getStringValue(), pc.getUseState().getStringValue());
        
        lModifiedPC.setAcquisitionDate(acquisitionDate);
        
        SimpleDateFormat lDateFormat =
                new SimpleDateFormat(Constants.DATE_FORMAT);
        lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        String lNewDate = null;
        if (acquisitionDate != null) {
            lNewDate = lDateFormat.format(acquisitionDate);
        }
        String lOldDate = null;
        if (pc.getAcquisitionDate() != null) {
            lOldDate = lDateFormat.format(pc.getAcquisitionDate());
        }
        addChangeInList(lLogin, HistoryConstants.ACQUISITION_DATE, lNewDate,
                lOldDate);
        
        lModifiedPC.setAvailabilityStatus(availabilityStatus);
        // Do not update history for Availability Status
        
        if (isAvailabilityDateMandatory()) {
            lModifiedPC.setAvailabilityDate(availabilityDate);
            // Do not update history for Availability Date
        }
        
        lModifiedPC.setFunction(function);
        addChangeInList(lLogin, HistoryConstants.FUNCTION, function,
                pc.getFunction());
        
        lModifiedPC.setPlatform(platform);
        addChangeInList(lLogin, HistoryConstants.PLATFORM, platform,
                pc.getPlatform());
        
        Software lDefaultOS = null;
        if (defaultOSId != null) {
            lDefaultOS = softwareBean.findById(defaultOSId);
        }
        lModifiedPC.setDefaultOS(lDefaultOS);
        String lOldDefaultOS = null;
        if (pc.getDefaultOS() != null) {
            lOldDefaultOS = pc.getDefaultOS().getCompleteName();
        }
        String lNewDefaultOS = null;
        if (lDefaultOS != null) {
            lNewDefaultOS = lDefaultOS.getCompleteName();
        }
        addChangeInList(lLogin, HistoryConstants.DEFAULT_OS, lNewDefaultOS,
                lOldDefaultOS);
        
        lModifiedPC.setNbScreens(nbScreens);
        String lNewNbScreensStr = null;
        if (nbScreens != null) {
            lNewNbScreensStr = nbScreens.toString();
        }
        String lOldNbScreensStr = null;
        if (pc.getNbScreens() != null) {
            lOldNbScreensStr = pc.getNbScreens().toString();
        }
        addChangeInList(lLogin, HistoryConstants.NB_SCREENS, lNewNbScreensStr,
                lOldNbScreensStr);
        
        Domain lDomain = null;
        if (domainId != null) {
            lDomain = valueListBean.findAttributeValueListById(Domain.class,
                    domainId);
        }
        lModifiedPC.setDomain(lDomain);
        String lOldDomain = null;
        if (pc.getDomain() != null) {
            lOldDomain = pc.getDomain().getDefaultValue();
        }
        String lNewDomain = null;
        if (lDomain != null) {
            lNewDomain = lDomain.getDefaultValue();
        }
        addChangeInList(lLogin, HistoryConstants.DOMAIN, lNewDomain,
                lOldDomain);
        
        // Comments are not stored in history
        lModifiedPC.setComment(comment);
        
        lModifiedPC.setAssignment(assignment);
        addChangeInList(lLogin, HistoryConstants.ASSIGNMENT, assignment,
                pc.getAssignment());
        
        /*
         * Manage communication ports
         */
        List<CommunicationPort> lOldPorts = lModifiedPC.getPorts();
        
        ComPortController lComPortController =
                findBean(ComPortController.class);
        List<CommunicationPort> lPorts = lComPortController.getPorts();
        
        Map<String, Action> lPortsChanges =
                lComPortController.GetChangesList(lOldPorts, lLogin);
        changes.putAll(lPortsChanges);
        
        List<CommunicationPort> lAddedPorts =
                new ArrayList<CommunicationPort>();
        
        List<CommunicationPort> lRemovedPorts =
                new ArrayList<CommunicationPort>();
        
        System.out.println(lOldPorts);
        System.out.println(lPorts);
        CollectionUtil.retrieveAddedAndRemoved(lOldPorts, lPorts,
                lAddedPorts, lRemovedPorts);
        System.out.println("-added-");
        System.out.println(lAddedPorts);
        System.out.println("-removed-");
        System.out.println(lRemovedPorts);
        /*
         * Manage installed software products
         */
        Map<String, Boolean> lSoftwareChanges =
                getChanges(lModifiedPC.getSoftwares(), softwares, lLogin);
        
        lModifiedPC.getSoftwares().clear();
        lModifiedPC.getSoftwares().addAll(softwares);
        
        /*
         * Manage documents
         */
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        List<Document> lNewDocuments = lDocumentController.getDocuments();
        
        List<Document> lOldDocuments = documentBean.findDocumentsByArticle(pc);
        
        // add new documents
        for (Document lDocument : lNewDocuments) {
            if (!lOldDocuments.contains(lDocument)) {
                
                documentBean.create(lDocument);
                String lDocumentLabel = null;
                if (lDocument.getUploadDate() != null) {
                    lDocumentLabel =
                            lDocument.getName()
                                    + " ("
                                    + lDateFormat.format(lDocument
                                            .getUploadDate()) + ")";
                }
                else {
                    lDocumentLabel = lDocument.getName();
                }
                
                Action lAction =
                        new FieldModification(lLogin, null,
                                HistoryConstants.LINK_DOCUMENT, null, null,
                                null, lDocumentLabel);
                changes.put(lDocument.getName() + lDocument.getUploadDate(),
                        lAction);
            }
        }
        
        // delete removed documents
        for (Document lDocument : lOldDocuments) {
            if (!lNewDocuments.contains(lDocument)) {
                
                String lDocumentLabel = null;
                if (lDocument.getUploadDate() != null) {
                    lDocumentLabel =
                            lDocument.getName()
                                    + " ("
                                    + lDateFormat.format(lDocument
                                            .getUploadDate()) + ")";
                }
                else {
                    lDocumentLabel = lDocument.getName();
                }
                documentBean.remove(lDocument);
                
                Action lAction =
                        new FieldModification(lLogin, null,
                                HistoryConstants.UNLINK_DOCUMENT, null, null,
                                lDocumentLabel, null);
                changes.put(lDocument.getName() + lDocument.getUploadDate(),
                        lAction);
            }
        }
        
        /*
         * Manage location
         */
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();
        
        if (!useState.equals(UseState.Archived)) {
            LocationManager.validateLocation(lLocation);
        }
        
        String lStrNewLocation;
        if (lLocation == null) {
            lStrNewLocation = null;
        }
        else {
            lStrNewLocation = lLocation.toString();
        }
        
        String lStrOldLocation;
        if (pc.getLocation() == null) {
            lStrOldLocation = null;
        }
        else {
            lStrOldLocation = pc.getLocation().toString();
        }
        
        addChangeInList(lLogin, Constants.Location, lStrNewLocation,
                lStrOldLocation);
        
        /*
         * Manage container
         */
        ContainerController lContainerCtrl =
                findBean(ContainerController.class);
        Container lContainer = lContainerCtrl.getContainer();
        
        String lStrNewContainer;
        if (lContainer == null) {
            lStrNewContainer = null;
        }
        else {
            lStrNewContainer = lContainer.toString();
        }
        
        String lStrOldContainer;
        if (pc.getContainer() == null) {
            lStrOldContainer = null;
        }
        else {
            lStrOldContainer = pc.getContainer().toString();
        }
        
        addChangeInList(lLogin, Constants.Container, lStrNewContainer,
                lStrOldContainer);

        /*
         * Manage specificities
         */
        List<PCSpecificity> lOldSpecificities = lModifiedPC.getSpecificities();
        PCSpecificityController lPCSpecificityController =
                findBean(PCSpecificityController.class);
        List<PCSpecificity> lSpecificities =
                lPCSpecificityController.getSpecificities();
        List<Action> specificitiesChanges = lPCSpecificityController
                .GetChangesList(lModifiedPC.getSpecificities(), lLogin);
        List<PCSpecificity> lAddedSpecificities =
                new ArrayList<PCSpecificity>();
        List<PCSpecificity> lRemovedSpecificities =
                new ArrayList<PCSpecificity>();
        CollectionUtil.retrieveAddedAndRemoved(lOldSpecificities,
                lSpecificities, lAddedSpecificities, lRemovedSpecificities);
        
        /*
         * Manage dated comments
         */
        List<DatedComment> lOldDatedComments = lModifiedPC.getDatedComments();
        DatedCommentController lDatedCommentController =
                findBean(DatedCommentController.class);
        List<DatedComment> lDatedComments =
                lDatedCommentController.getDatedComments();
        List<Action> datedCommentsChanges = lDatedCommentController
                .GetChangesList(lModifiedPC.getDatedComments(), lLogin);
        List<DatedComment> lAddedDatedComments = new ArrayList<DatedComment>();
        List<DatedComment> lRemovedDatedComments =
                new ArrayList<DatedComment>();
        CollectionUtil.retrieveAddedAndRemoved(lOldDatedComments,
                lDatedComments, lAddedDatedComments, lRemovedDatedComments);
        
        /*
         * Update PC in database
         */
        List<Action> lModifications = new ArrayList<Action>();
        lModifications.addAll(changes.values());
        lModifications.addAll(specificitiesChanges);
        lModifications.addAll(datedCommentsChanges);
        
        // set the history comment if defined
        if (!StringUtil.isEmptyOrNull(historyComment)) {
            for (Action lAction : lModifications) {
                Comment lAutoComment = lAction.getComment();
                if (lAutoComment != null
                        && !StringUtil.isEmptyOrNull(lAutoComment.getMessage())) {
                    lAction.getComment().setMessage(
                            historyComment + " - " + lAutoComment.getMessage());
                }
                else {
                    Comment lComment = new Comment(historyComment);
                    lAction.setComment(lComment);
                }
            }
        }
        
        lModifiedPC.validate();
        
        pc = pcBean.merge(lModifiedPC, lAddedPorts, lRemovedPorts,
                lAddedSpecificities, lRemovedSpecificities,
                lAddedDatedComments, lRemovedDatedComments, lSoftwareChanges,
                lLocation, lContainer, lModifications, lLogin);
    }
    
    @Override
    protected void deleteItem() throws Exception {
        // Check that no demand references the selected PC
        if (!demandBean.findDemandsUsingPC(pc).isEmpty()) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(Constants.PC_DELETION_USED_BY_DEMAND));
        }
        
        LogInController lLogInController = findBean(LogInController.class);
        
        // Manage remove of PC for its children
        DeleteController lDeleteController = findBean(DeleteController.class);
        lDeleteController.removeChildren(lLogInController.getUserLogged(),
                pc.getChildren(), pc.getName());
        
        // Remove reminders from database
        List<Reminder> lReminders = reminderBean.findRemindersByArticle(pc);
        for (Reminder lReminder : lReminders) {
            reminderBean.remove(lReminder);
        }
        
        // Remove documents from database
        List<Document> lDocuments = documentBean.findDocumentsByArticle(pc);
        for (Document lDocument : lDocuments) {
            documentBean.remove(lDocument);
        }
        
        // Delete PC from database
        pcBean.remove(pc);
    }
    
    @Override
    protected void initItemWithNew() {
        pc = new PC();
    }
    
    @Override
    protected void initItemFromDatabase() {
        pc = pcBean.findById(PCId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        
        if (pc != null) {
            airbusSN = pc.getAirbusSN();
            manufacturerSN = pc.getManufacturerSN();
            name = pc.getName();
            function = pc.getFunction();
            
            if (pc.getDepartmentInCharge() != null) {
                departmentInChargeId = pc.getDepartmentInCharge().getId();
            }
            else {
                departmentInChargeId = null;
            }
            
            if (pc.getProductType() != null) {
                productTypeId = pc.getProductType().getId();
            }
            else {
                productTypeId = null;
            }
            
            if (pc.getTypeArticle() != null) {
                typePCId = pc.getTypeArticle().getId();
                typePCLabel = pc.getTypeArticle().getLabel();
            }
            else {
                typePCId = null;
                typePCLabel = null;
            }
            
            if (pc.getInCharge() != null) {
                inChargeId = pc.getInCharge().getId();
                personInCharge = pc.getInCharge().getLoginDetails();
            }
            else {
                inChargeId = null;
                personInCharge = null;
            }
            
            owner = pc.getOwner();
            ownerSiglum = pc.getOwnerSiglum();
            admin = pc.getAdmin();
            state = pc.getState();
            useState = pc.getUseState();
            acquisitionDate = pc.getAcquisitionDate();
            comment = pc.getComment();
            
            if (pc.getAllocation() != null) {
                allocationId = pc.getAllocation().getId();
            }
            else {
                allocationId = null;
            }
            
            if (pc.getUsage() != null) {
                usageId = pc.getUsage().getId();
            }
            else {
                usageId = null;
            }
            
            assignment = pc.getAssignment();
            nbScreens = pc.getNbScreens();
            platform = pc.getPlatform();
            availabilityStatus = pc.getAvailabilityStatus();
            availabilityDate = pc.getAvailabilityDate();
            
            if (pc.getDefaultOS() != null) {
                defaultOSId = pc.getDefaultOS().getId();
            }
            else {
                defaultOSId = null;
            }
            
            if (pc.getDomain() != null) {
                domainId = pc.getDomain().getId();
            }
            else {
                domainId = null;
            }
            
            historyComment = null;
            
            List<CommunicationPort> lPorts =
                    new ArrayList<CommunicationPort>(pc.getPorts());
            
            // Initialize the comPortController with the PC ports list
            ComPortController lComPortController =
                    findBean(ComPortController.class);
            lComPortController.setPorts(lPorts);
            
            generateNetworkInterfaces();
            
            softwares = pc.getSoftwares();
            
            // Initialize the PCSpecificityController with the PC specificities
            PCSpecificityController lPCSpecificityController =
                    findBean(PCSpecificityController.class);
            lPCSpecificityController.setPc(pc);
            lPCSpecificityController.setSpecificities(
                    new ArrayList<PCSpecificity>(pc.getSpecificities()));
            
            // Initialize the DatedCommentController with the PC dated comments
            DatedCommentController lDatedCommentController =
                    findBean(DatedCommentController.class);
            lDatedCommentController.setArticle(pc);
            lDatedCommentController.setDatedComments(
                    new ArrayList<DatedComment>(pc.getDatedComments()));
            
            // Location reset
            LocationController lLocationCtrl =
                    findBean(LocationController.class);
            lLocationCtrl.setLocation(pc.getLocation());
            lLocationCtrl.setLocatedItem(pc);
            
            // Container reset
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            lContainerController.setContainer(pc.getContainer());
            lContainerController.setContainedItem(pc);
            
            // Reminder reset
            ReminderController lReminderController =
                    findBean(ReminderController.class);
            lReminderController.setMode(pc);
            
            // Document reset
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(pc);
            
            // Explorer reset
            ExplorerController controller = findBean(ExplorerController.class);
            controller.setRootNode(pc);
        }
    }
    
    /**
     * Merge the current PC
     */
    public void doMergePC() {
        pc = pcBean.merge(pc, new ArrayList<Action>());
    }
    
    /**
     * Initialize the reporting obsolescence controller for displaying
     * obsolescence data
     */
    public void doViewObsolescence() {
        
        if (pc != null) {
            
            ReportingObsoController lReportingObsoController =
                    findBean(ReportingObsoController.class);
            
            ObsolescenceData lObsolescenceData =
                    lReportingObsoController.getObsoBean().findByReference(
                            (TypePC) pc.getTypeArticle());
            
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", lObsolescenceData.getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }
    
    /**
     * Listener allowing uploading a file
     * 
     * @param pEvent
     *            the upload event
     * @throws Exception
     *             when an error occurs
     */
    public void fileUploadListener(FileUploadEvent pEvent) throws Exception {
        UploadedFile lItem = pEvent.getUploadedFile();
        sysmonInputStream = lItem.getInputStream();
    }
    
    /**
     * Update the network interfaces list with internal and external (from
     * installed network boards) communication ports
     */
    private void generateNetworkInterfaces() {
        
        networkInterfaces.clear();
        
        // Internal communication ports
        for (CommunicationPort lPort : pc.getPorts()) {
            
            networkInterfaces.add(new NetworkInterface(lPort));
        }
        
        // External communication ports
        for (Article lChild : pc.getChildren()) {
            
            if (lChild instanceof Board) {
                
                Board lBoard = (Board) lChild;
                
                List<CommunicationPort> lBoardPorts = lBoard.getPorts();
                
                if (lBoardPorts.size() != 0) {
                    
                    // Retrieve the PC slot on which the board is located
                    Contains_PC_Board lContainer = lBoard.getContainerOrmPC();
                    Integer lSlotNumber = lContainer.getSlotNumber();
                    
                    for (CommunicationPort lPort : lBoardPorts) {
                        
                        networkInterfaces.add(new NetworkInterface(lPort,
                                lSlotNumber));
                    }
                }
            }
        }
    }
    
    /**
     * Update the selected PC and open its consultation page.<br>
     * The update is done using a file in SYSMON format.
     */
    public void doAutoUpdateFromFile() {
        
        if (sysmonInputStream == null) {
            
            Utils.addFacesMessage(NavigationConstants.UPLOAD_SYSMON_ERROR_ID,
                    MessageBundle.getMessage(Constants.NO_SELECTED_FILE));
        }
        
        try {
            
            AutoUpdateController lAutoUpdateController =
                    findBean(AutoUpdateController.class);
            
            lAutoUpdateController.doUpdatePCFromFile(pc, sysmonInputStream);
            
            // The PC automatic update information have been updated in database
            // through the previous treatment which is at this point successful
            
        }
        catch (AutoUpdateException e) {
            Utils.addFacesMessage(NavigationConstants.UPLOAD_SYSMON_ERROR_ID,
                    e.getMessage());
        }
        doRefresh();
    }
    
    /**
     * Update the selected PC and open its consultation page.<br>
     * The update is done using SYSMON.
     */
    public void doAutoUpdateWithSysmon() {
        
        try {
            AutoUpdateController lAutoUpdateController =
                    findBean(AutoUpdateController.class);
            
            LogInController lLogInController = findBean(LogInController.class);
            
            lAutoUpdateController.doUpdatePCWithSysmon(pc,
                    lLogInController.getUserLogged());
            
            // The PC automatic update information have been updated in database
            // through the previous treatment which is at this point successful
            
        }
        catch (AutoUpdateException e) {
            Utils.addFacesMessage(NavigationConstants.PC_MGMT_ERROR_ID,
                    e.getMessage());
        }
        doRefresh();
        
    }
    
    /**
     * When mode is creation or when mode is modification and Airbus SN has been
     * modified, check that the provided Airbus SN does not already exist.
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the Airbus SN to validate
     * @throws ValidatorException
     *             when the provided Airbus SN is not valid
     */
    public void validateAirbusSN(FacesContext context, UIComponent component,
            Object value)
            throws ValidatorException {
        
        String lValue = (String) value;
        if (lValue == null) {
            lValue = "";
        }
        else {
            lValue = lValue.trim();
        }
        
        if (!isCreateMode() && !UseState.isOnPurchase(useState)) {
            // If in modification mode and PC state is different from any on
            // purchase state.
            if (lValue
                    .contains(Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX)) {
                String lMsg =
                        MessageBundle
                                .getMessageResource(
                                        Constants.AUTOGENERATED_SN_NEED_MODIFICATION,
                                        new Object[] { Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX });
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
        
        if ((!isCreateMode())
                && ((airbusSN == null && lValue.equals("")) || (airbusSN != null && airbusSN
                        .equalsIgnoreCase(lValue)))) {
            // Mode is modification and AirbusSN has not been modified:
            // nothing to validate
            return;
        }
        
        if (articleBean.existASN(lValue)) {
            String lMsg = MessageBundle.getMessage(Constants.SN_ALREADY_USED);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }
    
    /**
     * Validates the PC name entered by the user, using the following rules:<br>
     * When in creation mode, or in modification mode and PC name has changed,
     * the PC Name shall not already exist.<br>
     * When in modification mode and if selected PC state is different from any
     * on purchase one the PC Name cannot contain the prefix used to generate
     * name from demand.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            The PC name to validate
     * @throws ValidatorException
     *             when the PC name is not valid.
     */
    public void validatePCName(FacesContext pContext, UIComponent pComponent,
            Object pValue)
            throws ValidatorException {
        
        String lValue = (String) pValue;
        
        name = lValue;
        
        validatePCName(lValue);
    }
    
    /**
     * Validates the PC name entered by the user, using the following rules:<br>
     * When in creation mode, or in modification mode and PC name has changed,
     * the PC Name shall not already exist.<br>
     * When in modification mode and if selected PC state is different from any
     * on purchase one the PC Name cannot contain the prefix used to generate
     * name from demand.
     * 
     * @param pName
     *            The PC name to validate
     * @throws ValidatorException
     *             when the PC name is not valid.
     */
    private void validatePCName(String pName) throws ValidatorException {
        
        // If in creation mode
        // or in modification mode and new PC name is different from the
        // previous one,
        // check if PC name does not already exist
        if ((isCreateMode() || (!isCreateMode() && (!StringUtil
.isEmptyOrNull(pc
                .getName()) && !pc.getName()
                .equals(pName))))
                && pcBean.existByName(pName)) {
            String lMsg =
                    MessageBundle
                            .getMessage(Constants.PC_NAME_ALREADY_EXISTING);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        if (!isCreateMode() && !UseState.isOnPurchase(useState)) {
            // If in modification mode and PC state is different from any on
            // purchase state.
            if (pName.contains(Constants.PC_CREATED_FROM_DEMAND_NAME_PREFIX)) {
                String lMsg =
                        MessageBundle
                                .getMessageResource(
                                        Constants.AUTOGENERATED_NAME_NEED_MODIFICATION,
                                        new Object[] { Constants.PC_CREATED_FROM_DEMAND_NAME_PREFIX });
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    @Override
    protected void setItemId(Long pId) {
        PCId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return PCId;
    }
    
    /**
     * @return the selected
     */
    public PC getPc() {
        return pc;
    }
    
    /**
     * @return the selected
     */
    public void initPc(Long pId) {
        PCId = pId;
        initItemFromDatabase();
        initAttributesFromItem();
    }

    /**
     * @return the acquisitionDate
     */
    public Date getAcquisitionDate() {
        return acquisitionDate;
    }
    
    /**
     * @param acquisitionDate
     *            the acquisitionDate to set
     */
    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
    
    /**
     * @return the acquisitionDate message depending on the selected state
     */
    public String getAcquisitionDateMsg() {
        
        if (useState != null && UseState.isOnPurchase(useState)) {
            return MessageBundle.getMessage("dueAcquisitionDate");
        }
        return MessageBundle.getMessage("acquisitionDate");
    }
    
    /**
     * @return the admin
     */
    public String getAdmin() {
        return admin;
    }
    
    /**
     * @param admin
     *            the admin to set
     */
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    
    /**
     * @return the airbusSN
     */
    public String getAirbusSN() {
        return airbusSN;
    }
    
    /**
     * @param airbusSN
     *            the airbusSN to set
     */
    public void setAirbusSN(String airbusSN) {
        this.airbusSN = airbusSN;
    }
    
    /**
     * Return possible availability status according to the current PC:<br>
     * - New: in all cases<br>
     * - In Use: if the PC is not allocated to a demand
     * 
     * @return all possible availabilityStatus
     */
    public List<SelectItem> getAllAvailabilityStatus() {
        
        List<SelectItem> lSelectArticleState = new ArrayList<SelectItem>();
        lSelectArticleState.add(new SelectItem(PC.AvailabilityStatus.New,
                PC.AvailabilityStatus.New.toString()));
        if (pc.getAllocatedToDemand() == null) {
            // Skipped if selected PC is allocated to a demand
            lSelectArticleState.add(new SelectItem(PC.AvailabilityStatus.InUse,
                    PC.AvailabilityStatus.InUse.toString()));
        }
        
        return lSelectArticleState;
    }
    
    /**
     * @return the allocationId
     */
    public Long getAllocationId() {
        return allocationId;
    }
    
    /**
     * @param allocationId
     *            the allocationId to set
     */
    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }
    
    /**
     * @return List of possible allocations as items for combobox.
     */
    public List<SelectItem> getAllocations() {
        return valueListBean.generateSelectItems(BusinessAllocationPC.class);
    }
    
    /**
     * @return the assignment
     */
    public String getAssignment() {
        return assignment;
    }
    
    /**
     * @param assignment
     *            the assignment to set
     */
    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }
    
    /**
     * @return the authorizeUpdateAcquisitionDate
     */
    public boolean isAuthorizeUpdateAcquisitionDate() {
        LogInController lLogInController = findBean(LogInController.class);
        
        Date lAcquisitionDate = pc.getAcquisitionDate();
        UseState lUseState = pc.getUseState();
        boolean lStateIsOnPurchase = false;
        if (lUseState != null && UseState.isOnPurchase(lUseState)) {
                lStateIsOnPurchase = true;
        }
        else {
                lStateIsOnPurchase = false;
            }
        
        if (lAcquisitionDate == null || lStateIsOnPurchase)
            return lLogInController.isAuthorized(RightCategoryCRUD.ArticleCRUD,
                    RightMaskCRUD.CRUD_Update);
        else
            return lLogInController
                    .isAuthorized(RightMaskAction.ArticleUpdateAcquisitionDate);
    }
    
    /**
     * @return the authorizeUpdateAirbusSN
     */
    public boolean isAuthorizeUpdateAirbusSN() {
        LogInController lLogInController = findBean(LogInController.class);
        
        String ASN = pc.getAirbusSN();
        
        if (ASN == null
                || ASN.isEmpty()
                || ASN.contains(Constants.PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX))
            return lLogInController.isAuthorized(RightCategoryCRUD.ArticleCRUD,
                    RightMaskCRUD.CRUD_Update);
        else
            return lLogInController
                    .isAuthorized(RightMaskAction.ArticleUpdateAirbusSN);
    }
    
    /**
     * @return the availabilityDate
     */
    public Date getAvailabilityDate() {
        return availabilityDate;
    }
    
    /**
     * @param availabilityDate
     *            the availabilityDate to set
     */
    public void setAvailabilityDate(Date availabilityDate) {
        this.availabilityDate = availabilityDate;
    }
    
    /**
     * @return true if the availability date has to be filled
     */
    public boolean isAvailabilityDateMandatory() {
        if (isAvailabilityMandatory()
                && availabilityStatus == PC.AvailabilityStatus.New) {
            if (availabilityDate == null) {
                // Initialize with a default value
                availabilityDate = new Date();
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * @return true if the availability status and the delivery date
     *         has to be filled on the PC update page.
     */
    public boolean isAvailabilityMandatory() {
        if (isCreateMode()) {
            return false;
        }
        else {
            if (UseState.isOnPurchase(pc.getUseState())
                    && !UseState.isOnPurchase(useState)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return the availabilityStatus
     */
    public PC.AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }
    
    /**
     * @param availabilityStatus
     *            the availabilityStatus to set
     */
    public void setAvailabilityStatus(PC.AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
    
    /**
     * Build a map containing the added and removed installed software
     * products.<br>
     * The map contains as key the complete name (in String form) of the
     * Software product and as value a boolean with the following meaning: true
     * if software has been added and false if it has been removed.<br>
     * Detected modifications are added to the user changes to valid.
     * 
     * @param pOldList
     *            the old installed software list
     * @param pNewList
     *            the new installed software list
     * @param pLogin
     *            the login of the author of the modification
     * @return map of software products to update, with the boolean true if
     *         installed on PC or false if removed
     */
    private Map<String, Boolean> getChanges(List<Software> pOldList,
            List<Software> pNewList, String pLogin) {
        
        Map<String, Boolean> lListChanges = new HashMap<String, Boolean>();
        
        // Browse the new list
        for (Software lSoft : pNewList) {
            
            if (!pOldList.contains(lSoft)) {
                
                String lCompleteName = lSoft.getCompleteName();
                // Software is not found into the old list: it has been added
                lListChanges.put(lCompleteName, true);
                // Add the action into the user changes to valid
                Action lNewModif =
                        new FieldModification(pLogin, null,
                                HistoryConstants.INSTALL_SOFTWARE, null, null,
                                null, lCompleteName);
                changes.put(lCompleteName, lNewModif);
            }
        }
        
        // Browse the old list
        for (Software lSoft : pOldList) {
            
            if (!pNewList.contains(lSoft)) {
                
                String lCompleteName = lSoft.getCompleteName();
                // Software is not found into the new list: is has been removed
                lListChanges.put(lCompleteName, false);
                // Add the action into the user changes to valid
                Action lNewModif =
                        new FieldModification(pLogin, null,
                                HistoryConstants.REMOVE_SOFTWARE, null, null,
                                lCompleteName, null);
                changes.put(lCompleteName, lNewModif);
            }
        }
        
        return lListChanges;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return the defaultOS
     */
    public Software getDefaultOS() {
        
        if (defaultOSId == null) {
            return new Software();
        }
        return softwareBean.findById(defaultOSId);
    }
    
    /**
     * @return the defaultOSId
     */
    public Long getDefaultOSId() {
        return defaultOSId;
    }
    
    /**
     * @param pDefaultOSId
     *            the defaultOSId to set
     */
    public void setDefaultOSId(Long pDefaultOSId) {
        defaultOSId = pDefaultOSId;
        needToSetDefaultOS = false;
    }
    
    /**
     * Update the needToSetDefaultOS attribute
     */
    private void updateDefaultOSId() {
        needToSetDefaultOS = false;
        if (defaultOSId == null) {
            List<SelectItem> lOSList = getSelectOperatingSystems();
            if (lOSList.size() == 1) {
                defaultOSId = (Long) (lOSList.get(0).getValue());
            }
            else if (lOSList.size() > 1) {
                needToSetDefaultOS = true;
            }
        }
    }
    
    /**
     * @return the domainId
     */
    public Long getDomainId() {
        return domainId;
    }
    
    /**
     * @param pDomainId
     *            the domainId to set
     */
    public void setDomainId(Long pDomainId) {
        domainId = pDomainId;
    }
    
    /**
     * @return the domains
     */
    public List<SelectItem> getDomains() {
        return valueListBean.generateSelectItems(Domain.class);
    }
    
    /**
     * @return the function
     */
    public String getFunction() {
        return function;
    }
    
    /**
     * @param function
     *            the function to set
     */
    public void setFunction(String function) {
        this.function = function;
    }
    
    /**
     * @return the historyComment
     */
    public String getHistoryComment() {
        return historyComment;
    }
    
    /**
     * @param pHistoryComment
     *            the historyComment to set
     */
    public void setHistoryComment(String pHistoryComment) {
        historyComment = pHistoryComment;
    }
    
    /**
     * @return the personsInCharges
     */
    public List<String> getPersonsInCharge() {
        List<User> lResult = userBean.findUsers();
        
        List<String> lInCharge = new ArrayList<String>();
        for (User incharge : lResult) {
            lInCharge.add(incharge.getLoginDetails());
        }

        return lInCharge;
    }
    
    /**
     * @return the InChargeId
     */
    public Long getInChargeId() {
        return inChargeId;
    }
    
    /**
     * @param pInChargeId
     *            the InChargeId to set
     */
    public void setInChargeId(Long pInChargeId) {
        personInCharge = userBean.findUser(pInChargeId).getLoginDetails();
        inChargeId = pInChargeId;
    }
    
    /**
     * @return the personInCharge
     */
    public String getPersonInCharge() {
        return personInCharge;
    }
    
    /**
     * @param personInCharge
     *            the personInCharge to set
     */
    public void setPersonInCharge(String pPersonInCharge) {
        personInCharge = pPersonInCharge;
        String login = pPersonInCharge.split("\\(|\\)")[1];
        inChargeId = userBean.findUser(login).getId();
    }
    
    public void validatePersonInCharge(FacesContext pContext,
            UIComponent pComponent, Object pValue) throws ValidatorException {
        boolean result = false;
        String pPersonInCharge = (String) pValue;
        String[] userDetails = pPersonInCharge.split("\\(|\\)");
        if (userDetails.length > 1) {
            User user = userBean.findUser(userDetails[1]);
            if (user != null) {
                result = true;
            }
        }
        
        if (!result) {
            String lMsg =
                    MessageBundle.getMessage(MessageConstants.UNKNOWN_INCHARGE);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }

    /**
     * @return the acquisitionDate message depending on the selected PC state
     */
    public String getInfoAcquisitionDateMsg() {
        
        if (pc != null && pc.getUseState() != null
                && UseState.isOnPurchase(pc.getUseState())) {
                return MessageBundle.getMessage("dueAcquisitionDate");
        }
        return MessageBundle.getMessage("acquisitionDate");
    }
    
    /**
     * @return the manufacturerSN
     */
    public String getManufacturerSN() {
        return manufacturerSN;
    }
    
    /**
     * @param pManufacturerSN
     *            the manufacturerSN to set
     */
    public void setManufacturerSN(String pManufacturerSN) {
        manufacturerSN = pManufacturerSN;
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
     * @return the departmentInChargeId
     */
    public Long getDepartmentInChargeId() {
        return departmentInChargeId;
    }
    
    /**
     * @param pDepartmentInChargeId
     *            the departmentInChargeId to set
     */
    public void setDepartmentInChargeId(Long pDepartmentInChargeId) {
        departmentInChargeId = pDepartmentInChargeId;
    }
    
    /**
     * @return the departments in charge
     */
    public List<SelectItem> getDepartmentsInCharge() {
        List<DepartmentInCharge> lDepartmentsInCharge =
                valueListBean.findAllValueLists(DepartmentInCharge.class);
        
        List<SelectItem> lSelectItems = new ArrayList<SelectItem>();
        for (DepartmentInCharge lDepartmentInCharge : lDepartmentsInCharge) {
            lSelectItems.add(new SelectItem(lDepartmentInCharge.getId(),
                    lDepartmentInCharge.getFullDescription()));
        }
        return lSelectItems;
    }
    
    /**
     * @param pDepartmentInChargeId
     *            the department in charge id
     * @return the found department in charge or null
     */
    private DepartmentInCharge
            getDepartmentInChargeValue(Long pDepartmentInChargeId) {
        
        DepartmentInCharge lResult = null;
        if (pDepartmentInChargeId != null) {
            lResult = valueListBean.findAttributeValueListById(
                    DepartmentInCharge.class, pDepartmentInChargeId);
        }
        return lResult;
    }
    
    /**
     * @return the nbScreens
     */
    public Integer getNbScreens() {
        return nbScreens;
    }
    
    /**
     * @param pNbScreens
     *            the nbScreens to set
     */
    public void setNbScreens(Integer pNbScreens) {
        nbScreens = pNbScreens;
    }
    
    /**
     * @return the networkInterfaces
     */
    public List<NetworkInterface> getNetworkInterfaces() {
        return networkInterfaces;
    }
    
    /**
     * @return a boolean indicating the selected PC (if not null) has
     *         obsolescence data
     */
    public Boolean hasObsoData() {
        
        if (pc == null) {
            return false;
        }
        
        return (obsoBean.findByReference((TypePC) pc.getTypeArticle()) != null);
    }
    
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * @param pOwner
     *            the owner to set
     */
    public void setOwner(String pOwner) {
        owner = pOwner;
    }
    
    /**
     * @return the ownerSiglum
     */
    public String getOwnerSiglum() {
        return ownerSiglum;
    }
    
    /**
     * @param pOwnerSiglum
     *            the ownerSiglum to set
     */
    public void setOwnerSiglum(String pOwnerSiglum) {
        ownerSiglum = pOwnerSiglum;
    }
    
    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * @param pPlatform
     *            the platform to set
     */
    public void setPlatform(String pPlatform) {
        platform = pPlatform;
    }
    
    /**
     * @return the productTypeId
     */
    public Long getProductTypeId() {
        return productTypeId;
    }
    
    /**
     * @param pProductTypeId
     *            the productTypeId to set
     */
    public void setProductTypeId(Long pProductTypeId) {
        productTypeId = pProductTypeId;
    }
    
    /**
     * @return the product types
     */
    public List<SelectItem> getProductTypes() {
        return valueListBean.generateSelectItems(ProductTypePC.class);
    }
    
    /**
     * @param pProductTypeId
     *            the product type id
     * @return the found product type PC or null
     */
    private ProductTypePC getProductTypeValue(Long pProductTypeId) {
        
        ProductTypePC lResult = null;
        if (pProductTypeId != null) {
            lResult =
                    valueListBean.findAttributeValueListById(
                            ProductTypePC.class, pProductTypeId);
        }
        return lResult;
    }
    
    /**
     * @return the list of installed Operating Systems
     */
    public List<SelectItem> getSelectOperatingSystems() {
        
        List<SelectItem> lSelectItems = new ArrayList<SelectItem>();
        if (softwares != null) {
            
            for (Software lSoftware : softwares) {
                if (lSoftware.getOperatingSystem()) {
                    lSelectItems.add(new SelectItem(lSoftware.getId(),
                            lSoftware.getCompleteName()));
                }
            }
        }
        return lSelectItems;
    }
    
    /**
     * @return the software
     */
    public Software getSoftware() {
        return software;
    }
    
    /**
     * @param software
     *            the software to set
     */
    public void setSoftware(Software software) {
        this.software = software;
    }
    
    /**
     * Add the selected software product to the software products list
     */
    public void addSoftware() {
        
        SoftwareController lSoftwareController =
                findBean(SoftwareController.class);
        Software lSoftware = lSoftwareController.getSoftwareToInstall();
        
        if (lSoftware == null) {
            Utils.addFacesMessage(NavigationConstants.INSTALL_SOFT_ERROR_ID,
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NOT_FOUND));
        }
        else if (softwares.contains(lSoftware)) {
            Utils.addFacesMessage(NavigationConstants.INSTALL_SOFT_ERROR_ID,
                    MessageBundle.getMessage(
                            MessageConstants.SOFTWARE_ALREADY_INSTALLED));
        }
        else {
            // Add software product to the list
            softwares.add(lSoftware);
            updateDefaultOSId();
        }
    }
    
    /**
     * Remove the software product from the software products list
     */
    public void removeSoftware() {
        
        softwares.remove(software);
        
        // also remove software as default OS
        if (software.getId().equals(defaultOSId)) {
            defaultOSId = null;
        }

        updateDefaultOSId();
    }
    
    /**
     * @return the needToSetDefaultOS
     */
    public boolean getNeedToSetDefaultOS() {
        return needToSetDefaultOS;
    }

    /**
     * @param needToSetDefaultOS the needToSetDefaultOS to set
     */
    public void setNeedToSetDefaultOS(boolean needToSetDefaultOS) {
        this.needToSetDefaultOS = needToSetDefaultOS;
    }

    /**
     * @return the softwares
     */
    public List<Software> getSoftwares() {
        return softwares;
    }
    
    /**
     * @param softwares
     *            the softwares to set
     */
    public void setSoftwares(List<Software> softwares) {
        this.softwares = softwares;
    }
    
    /**
     * @return the state
     */
    public ArticleState getState() {
        return state;
    }
    
    /**
     * @param pState
     *            the state to set
     */
    public void setState(ArticleState pState) {
        state = pState;
    }
    
    /**
     * @return the use state
     */
    public UseState getUseState() {
        return useState;
    }

    /**
     * @param pUseState
     *            the use state to set
     */
    public void setUseState(UseState pUseState) {
        if (pUseState.equals(UseState.Archived)
                && !pUseState.equals(useState)) {
            setState(ArticleState.Unusable);
            
            // Erase location
            LocationController lLocationCtrl =
                    findBean(LocationController.class);
            lLocationCtrl.setLocation(null);
            
            // Erase Parent
            ContainerController lContainerCtrl =
                    findBean(ContainerController.class);
            lContainerCtrl.setContainer(null);
            
            Utils.addInfoMessage(errorId,
                    MessageBundle
                            .getMessage(Constants.UPDATE_TO_ARCHIVED_INFO));
            Utils.addWarningMessage(errorId, MessageBundle
                    .getMessage(Constants.UPDATE_TO_ARCHIVED_WARN));
        }
        useState = pUseState;
    }
    
    /**
     * @return the states
     */
    public List<SelectItem> getStates() {
        List<SelectItem> lSelectArticleState = new ArrayList<SelectItem>();
        for (ArticleState lState : ArticleState.values()) {
            lSelectArticleState.add(new SelectItem(lState, lState.toString()));
        }
        lSelectArticleState.remove(ArticleState.ToBeTested);
        return lSelectArticleState;
    }
    
    /**
     * @return the use states
     */
    public List<SelectItem> getUseStates() {
        
        boolean lArticleWasOnPurchase = false;
        
        if (mode == Mode.UPDATE && pc.getUseState() != null
                && UseState.isOnPurchase(pc.getUseState())) {
                lArticleWasOnPurchase = true;
        }
        
        List<SelectItem> lSelectUseState = new ArrayList<SelectItem>();
        
        LogInController lLoginController = findBean(LogInController.class);
        
        for (UseState lState : UseState.values()) {
            if (UseState.isOnPurchase(lState) && mode == Mode.UPDATE
                    && !lArticleWasOnPurchase) {
                    break;
            }
            else if (UseState.isApplicable(lState, pc)) {
                if ((lState == UseState.Archived && lLoginController
                        .isAuthorized(
                                RightMaskAction.ArchivedCRUDAuthorization))
                        || lState != UseState.Archived) {
                lSelectUseState.add(new SelectItem(lState, lState.toString()));
                }
            }
        }
        return lSelectUseState;
    }
    
    /**
     * @return the typePCId
     */
    public Long getTypePCId() {
        return typePCId;
    }
    
    /**
     * @param pTypePCId
     *            the typePCId to set
     */
    public void setTypePCId(Long pTypePCId) {
        typePCId = pTypePCId;
        typePCLabel = articleBean.findTypeArticleById(pTypePCId).getLabel();
    }
    
    /**
     * @return the typePCLabel
     */
    public String getTypePCLabel() {
        return typePCLabel;
    }

    /**
     * @param typePC
     *            the typePCLabel to set
     */
    public void setTypePCLabel(String pTypePC) {
        typePCLabel = pTypePC;
        typePCId = articleBean.findTypeArticleByName(pTypePC).getId();
    }
    
    public void validateTypePCLabel(FacesContext pContext,
            UIComponent pComponent, Object pValue) throws ValidatorException {
        boolean result = false;
        String pTypePC = (String) pValue;
        TypeArticle type = articleBean.findTypeArticleByName(pTypePC);
        if (type != null) {
            result = true;
        }
        
        if (!result) {
            String lMsg = MessageBundle.getMessage(Constants.PC_TYPE_NOT_FOUND);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        
        List<TypeArticle> lResult =
                articleBean.findAllTypeArticle(new TypePC());
        
        ArrayList<String> types = new ArrayList<String>();
        for (TypeArticle lType : lResult) {
            types.add(lType.getLabel());
        }
        return types;
    }
    
    /**
     * @return the usageId
     */
    public Long getUsageId() {
        return usageId;
    }
    
    /**
     * @param usageId
     *            the usageId to set
     */
    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }
    
    /**
     * @return List of possible usages as items for combobox.
     */
    public List<SelectItem> getUsages() {
        return valueListBean.generateSelectItems(BusinessUsagePC.class);
    }
    
    /**
     * @param pInChargeId
     *            the user in charge id
     * @return the found user in charge or null
     */
    private User getUserInCharge(Long pInChargeId) {
        
        User lResult = null;
        if (inChargeId != null) {
            lResult = userBean.findUser(pInChargeId);
            
        }
        return lResult;
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createPCTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoPCTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updatePCTitle");
    }
    
}
