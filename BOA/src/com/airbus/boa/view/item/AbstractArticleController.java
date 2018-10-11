/*
 * ------------------------------------------------------------------------
 * Class : AbstractArticleController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.UpdateBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.explorer.view.ExplorerController;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.ComPortController;
import com.airbus.boa.view.ContainerController;
import com.airbus.boa.view.DocumentController;
import com.airbus.boa.view.LocationController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.ReminderController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.application.DBConstants;
import com.airbus.boa.view.obso.ReportingObsoController;

/**
 * Abstract controller to be extended by controllers managing an article
 * creation, consultation and modification
 */
public abstract class AbstractArticleController extends AbstractItemController
        implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    protected Long articleId;
    protected Article article = null;
    
    private Long typeArticleId;
    private String typeArticleLabel;
    private String designation;
    private String airbusSN;
    private String manufacturerSN;
    private Long airbusPnId;
    private String airbusPNIdentifier;
    private String cmsCode;
    private String manufacturerPNIdentifier;
    
    private ArticleState stateSelected;
    private UseState useStateSelected;
    private Date acquisitionDate = new Date();
    private String ipAddress;
    private Boolean calibration;
    private String bootloader;
    private Date activeStockControlDate;
    private List<Software> softwares = new ArrayList<Software>();
    
    private final int COMMENT_LENGTH;
    private int availableChars;
    private String historyComment = null;
    private String comment = new String();
    
    /**
     * Software to remove from the installed software products list
     */
    private Software software;
    
    private UpdateBean updateBean;

    @EJB
    protected ArticleBean articleBean;
    
    @EJB
    protected HistoryBean historyBean;
    
    @EJB
    private ObsolescenceBean obsolescenceBean;
    
    @EJB
    private UserBean userBean;
    
    List<String> selectTypeArticle = new ArrayList<String>();
    
    public AbstractArticleController() {
        DBConstants lDBConstants = findBean(DBConstants.class);
        
        COMMENT_LENGTH = lDBConstants.getCommentMessageLength();
        availableChars = COMMENT_LENGTH;
        
        resultPage = NavigationConstants.ARTICLE_SEARCH_RESULT_PAGE;
        errorId = NavigationConstants.ART_MGMT_ERROR_ID;
    }
    
    /**
     * Post-construct method initializing the selectTypeArticle list
     */
    @Override
    protected void init() {
        super.init();
        if (article != null) {
            List<TypeArticle> typeArts =
                    articleBean.findAllTypeArticle(article.createTypeArticle());
            for (TypeArticle typeArticle : typeArts) {
                selectTypeArticle.add(typeArticle.getLabel());
            }
        }
    }

    @Override
    protected void setItemId(Long pId) {
        articleId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return articleId;
    }
    @Override
    protected void createItem() throws Exception {
        if (typeArticleId == null)
            throw new ValidationException("formCreateArticle:type",
                    MessageBundle.getMessage(Constants.TYPE_MANDATORY));
            
        TypeArticle tArticle = articleBean.findTypeArticleById(typeArticleId);
        article.setTypeArticle(tArticle);
        if (isShowDesignation()) {
            if (article instanceof Cabinet) {
                ((Cabinet) article).setDesignation(designation);
            }
            else if (article instanceof Rack) {
                ((Rack) article).setDesignation(designation);
            }
        }
        
        article.setAirbusSN(airbusSN);
        article.setManufacturerSN(manufacturerSN);

        // TODO Management of CMS_CODE linked to Airbus PN to do
        AirbusPN airbusPn = null;
        if (getAirbusPnId() != null) {
            airbusPn = articleBean.findAirbusPNById(getAirbusPnId());
        }
        article.setAirbusPN(airbusPn);
        article.setCmsCode(cmsCode);
        if (manufacturerPNIdentifier != null
                && !manufacturerPNIdentifier.trim().isEmpty()) {
            ManufacturerPN manufacturerPN =
                    articleBean
                            .findManufacturerPNByName(manufacturerPNIdentifier);
            if (manufacturerPN == null) { // creation necessary
                manufacturerPN = new ManufacturerPN();
                manufacturerPN.setIdentifier(manufacturerPNIdentifier);
                try {
                    manufacturerPN =
                            articleBean.createPN(manufacturerPN);
                } catch (Exception e) {
                    
                }
            }
            article.setManufacturerPN(manufacturerPN);
        }
        
        article.setState(stateSelected);
        article.setUseState(useStateSelected);
        article.setAcquisitionDate(acquisitionDate);
        
        if (!StringUtil.isEmptyOrNull(comment)) {
            article.getHistory().setGeneralComment(new Comment(comment.trim()));
        }

        // retrieve current user
        User user = null;
        LogInController lLogInController = findBean(LogInController.class);
        
        if (lLogInController != null
                && lLogInController.getUserLogged() != null) {
            user =
                    userBean.findUser(lLogInController.getUserLogged()
                            .getId());
        }
        
        // add the creation action
        Action.createCreationAction(user, article,
                Constants.CREATION_BY_INTERFACE);
        
        if (isEthernetBoard()) {
            Board board = (Board) article;
            
            ComPortController comPortController =
                    findBean(ComPortController.class);
            List<CommunicationPort> ports =
                    comPortController.getPorts();
            if (ports != null) {
                board.setPorts(ports);
            }
        }
        
        if (hasSoftware()) {
            article.setSoftwares(softwares);
        }
        else {
            article.setSoftwares(null);
        }
        
        if (isShowActiveStockControlDate() && activeStockControlDate != null) {
            ((Board) article).setActiveStockControlDate(activeStockControlDate);
        }

        if (isShowBootloader() && bootloader != null
                && !bootloader.isEmpty()) {
            ((Board) article).setBootLoader(bootloader);
        }
        
        if (isShowCalibration() && calibration != null) {
            ((Board) article).setCalibrated(calibration);
        }
        
        if (hasIPAddress()) {
            Switch swch = (Switch) article;
            if (ipAddress != null && !ipAddress.isEmpty()) {
                swch.setIpAddress(ipAddress);
            }
        }
        
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();
        if (!useStateSelected.equals(UseState.Archived)) {
            LocationManager.validateLocation(lLocation);
        }
        
        ContainerController lContainerCtrl =
                findBean(ContainerController.class);
        Container lContainer = lContainerCtrl.getContainer();
        
        article =
                articleBean.createArticle(article, lLocation,
                        lContainer);
        articleId = article.getId();
        
        // Persist documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        
        lDocumentController.doPersistChanges(article);
    }
    
    @Override
    public void updateItem() throws Exception {
        User lUser = findBean(LogInController.class).getUserLogged();
        
        initialize();
        updateBean.setArticle(article);
        updateBean.setCurrentUser(lUser);
        
        updateBean.setTypeArticleId(typeArticleId);
        updateBean.setDesignation(designation);
        
        updateBean.setAirbusSN(airbusSN);
        updateBean.setManufacturerSN(manufacturerSN);
        
        updateBean.setAirbusPNId(airbusPnId);
        updateBean.setCmsCode(cmsCode);
        updateBean.setManufacturerPNName(manufacturerPNIdentifier);
        
        updateBean.setState(stateSelected);
        updateBean.setUseState(useStateSelected);
        updateBean.setAcquisitionDate(acquisitionDate);
        updateBean.setIpaddress(ipAddress);
        updateBean.setActiveStockControlDate(activeStockControlDate);
        updateBean.setBootloader(bootloader);
        updateBean.setComment(comment);

        LocationController lLocationController =
                findBean(LocationController.class);
        Location lLocation = lLocationController.getLocation();
        if (!useStateSelected.equals(UseState.Archived)) {
            LocationManager.validateLocation(lLocation);
        }
        updateBean.setLocation(lLocation);
        
        ContainerController lContainerController =
                findBean(ContainerController.class);
        Container lContainer = lContainerController.getContainer();
        updateBean.setContainer(lContainer);
        
        if (updateBean.getTypeArticleId() == null) {
            throw new ValidationException("updateArticle:type",
                    MessageBundle.getMessage(Constants.TYPE_MANDATORY));
        }
        
        if (isDesignationMandatory() && updateBean.getDesignation() == null) {
            throw new ValidationException("updateArticle:updateDesignation",
                    MessageBundle.getMessage(Constants.DESIGNATION_MANDATORY));
        }
        
        if (updateBean.getAirbusSN() == null
                && updateBean.getManufacturerSN() == null) {
            throw new ValidationException(MessageBundle.getMessage(Constants.ASN_OR_MSN_MANDATORY));
        }

        if (isPNMandatory()
                && (updateBean.getAirbusPNId() == null
                        && StringUtil.isEmptyOrNull(updateBean.getCmsCode()) && StringUtil
                            .isEmptyOrNull(updateBean.getManufacturerPNName()))) {
            throw new ValidationException(MessageBundle.getMessage(Constants.PN_OR_CMSCODE_MUST_BE_FILLED));
        }

        if (updateBean.getAirbusPNId() != null
                && updateBean.getCmsCode() != null) {
            throw new ValidationException(MessageBundle.getMessage(Constants.PNAIRBUS1_CMSCODE_ONE_FIELD_MUST_BE_FILLED));
        }

        if (isEthernetBoard()) {
            // retrieving of the communication ports modifications
            ComPortController comPortController =
                    findBean(ComPortController.class);
            List<CommunicationPort> ports = comPortController.getPorts();
            
            updateBean.setPorts(ports);
            
        }
        
        if (hasSoftware()) {
            // Mise à jour de la liste des logiciels installés,
            // pour vérification des modifications effectuées par
            // l'utilisateur
            updateBean.setSoftwares(softwares);
        }
        
        // Retrieve modifications on documents
        DocumentController lDocumentController =
                findBean(DocumentController.class);
        List<Document> lDocuments = lDocumentController.getDocuments();
        updateBean.setDocuments(lDocuments);
        
        List<Action> lActions = updateBean.getActions();
        
        if (lActions.size() > 0) {
            updateBean.saveChange(historyComment);
            updateBean.remove();
        }
        else if (updateBean.isGeneralCommentModified()) {
            articleBean.merge(updateBean.getArticle());
        }
        initItemFromDatabase();
    }
    
    @Override
    protected void deleteItem() throws Exception {
        return;
    }
    
    @Override
    protected void initAttributesFromItem() {
        if (article != null) {
            if (article.getTypeArticle() != null) {
                setTypeArticleId(article.getTypeArticle().getId());
            }
            else {
                typeArticleId = null;
                typeArticleLabel = null;
            }
            if (article instanceof Cabinet) {
                designation = ((Cabinet) article).getDesignation();
            }
            else if (article instanceof Rack) {
                designation = ((Rack) article).getDesignation();
            }
            else {
                designation = null;
            }
            
            airbusSN = article.getAirbusSN();
            manufacturerSN = article.getManufacturerSN();

            if (article.getAirbusPN() != null) {
                airbusPnId = article.getAirbusPN().getId();
                airbusPNIdentifier = article.getAirbusPN().getIdentifier();
            }
            else {
                airbusPnId = null;
                airbusPNIdentifier = null;
            }
            
            cmsCode = article.getCmsCode();

            if (article.getManufacturerPN() != null) {
                manufacturerPNIdentifier =
                        article.getManufacturerPN().getIdentifier();
            }
            else {
                manufacturerPNIdentifier = "";
            }

            stateSelected = article.getState();
            useStateSelected = article.getUseState();
            acquisitionDate = article.getAcquisitionDate();
            if (article instanceof Switch) {
                ipAddress = ((Switch) article).getIpAddress();
            }
            if (article instanceof Board) {
                bootloader = ((Board) article).getBootLoader();
                activeStockControlDate =
                        ((Board) article).getActiveStockControlDate();
            }

            Comment generalComment = article.getHistory().getGeneralComment();
            if (generalComment != null) {
                comment = generalComment.getMessage();
            }
            else {
                comment = null;
            }
            
            historyComment = null;

            softwares = new ArrayList<Software>(article.getSoftwares());
            
            if (isEthernetBoard()) {
                // Update ports list
                ComPortController lComPortController =
                        findBean(ComPortController.class);
                lComPortController.setPorts(((Board) article).getPorts());
            }
            
            // Location reset
            LocationController lLocationCtrl =
                    findBean(LocationController.class);
            lLocationCtrl.setLocation(article.getLocation());
            lLocationCtrl.setLocatedItem(article);
            
            // Container reset
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            lContainerController.setContainer(article.getContainer());
            lContainerController.setContainedItem(article);
            
            // Reminder reset
            ReminderController lReminderController =
                    findBean(ReminderController.class);
            lReminderController.setMode(article);
            
            // Document reset
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(article);

            // Explorer reset
            ExplorerController controller = findBean(ExplorerController.class);
            controller.setRootNode(article);
        }
    }

    private void initialize() {
        
        log.warning("Récupération d'un nouveau stateful bean");
        try {
            Context ctx = new InitialContext();
            updateBean = (UpdateBean) ctx.lookup("java:module/UpdateBean");
        } catch (NamingException e) {
            log.throwing(AbstractArticleController.class.getName(),
                    "initialize", e);
            throw new ValidationException(e);
        }
    }
    
    /**
     * Initialize the reporting obsolescence controller for displaying
     * obsolescence data
     */
    public void doViewObsolescence() {
        
        if (article != null) {
            
            ReportingObsoController lReportingObsoController =
                    findBean(ReportingObsoController.class);
            
            ObsolescenceData lObsolescenceData =
                    lReportingObsoController.getObsoBean().findByReference(
                            article.getAirbusPN(), article.getTypeArticle());
            
            if (lObsolescenceData == null) {
                lObsolescenceData =
                        lReportingObsoController.getObsoBean().findByReference(
                                article.getManufacturerPN(),
                                article.getTypeArticle());
            }
            
            if (lObsolescenceData != null) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("id", lObsolescenceData.getId().toString());
                params.put("mode", "READ");
                NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                        params);
            }
        }
    }
    
    /**
     * Change the article container and update the current use state accordingly
     */
    public void doChangeContainer() {
        ContainerController lContainerController =
                findBean(ContainerController.class);
        lContainerController.doValidate();
        
        if (!(article instanceof Various)) {
            if (lContainerController.getMasterContainer() != null
                    && lContainerController.getMasterContainer().getType()
                            .equals(ContainerType.Installation)) {
                useStateSelected = UseState.InUse;
            }
            else {
                useStateSelected = UseState.InStock;
            }
        }
    }
    
    /**
     * @return the authorizeUpdateAcquisitionDate
     */
    public boolean isAuthorizeUpdateAcquisitionDate() {
        LogInController lLogInController = findBean(LogInController.class);
        
        Date lAcquisitionDate = article.getAcquisitionDate();
        UseState lUseState = article.getUseState();
        boolean lStateIsOnPurchase = false;
        if (lUseState != null) {
            switch (lUseState) {
            case OnExternalPurchase:
            case OnInternalPurchase:
                lStateIsOnPurchase = true;
                break;
            default:
                lStateIsOnPurchase = false;
            }
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
        
        String ASN = article.getAirbusSN();

        if (ASN == null || ASN.isEmpty())
            return lLogInController.isAuthorized(RightCategoryCRUD.ArticleCRUD,
                    RightMaskCRUD.CRUD_Update);
        else
            return lLogInController
                    .isAuthorized(RightMaskAction.ArticleUpdateAirbusSN);
    }
    
    /**
     * Check if the provided AirbusSN is not empty and is unique.
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the AirbusSN to valid
     * @throws ValidatorException
     *             when the provided AirbusSN is not valid
     */
    public void validateASN(FacesContext context, UIComponent component,
            Object value)
            throws ValidatorException {
        
        String valeur = (String) value;
        valeur = (valeur == null) ? "" : valeur.trim();

        if (isCreateMode()) {
            if (articleBean.existASN(valeur)) {
                String msg =
                        MessageBundle.getMessage(Constants.SN_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, msg, msg));
            }
        }
        
        if (!valeur.equals(article.getAirbusSN()) && isUpdateMode()) {
            if ((articleBean.existASN(valeur))) {
                String lMsg =
                        MessageBundle.getMessage(Constants.SN_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    /**
     * Check if the provided Cabinet designation is not empty and is unique.
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the Cabinet designation to valid
     * @throws ValidatorException
     *             when the provided Cabinet designation is not valid
     */
    public void validateDesignationName(FacesContext context,
            UIComponent component, Object value)
            throws ValidatorException {
        String valeur = (String) value;
        valeur = (valeur == null) ? "" : valeur.trim();
        
        if (isCreateMode()) {
            if ("".equals(valeur)) {
                String msg =
                        MessageBundle.getMessage(Constants.DESIGNATION_INVALID);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, msg, msg));
            }
            if (articleBean.existCabinetDesignation((String) value)) {
                String msg =
                        MessageBundle
                                .getMessage(Constants.DESIGNATION_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, msg, msg));
            }
        }
        
        String lDesignation = "";
        if (article instanceof Rack) {
            lDesignation = ((Rack) article).getDesignation();
        }
        else if (article instanceof Cabinet) {
            lDesignation = ((Cabinet) article).getDesignation();
        }
        
        // check if the designation has changed
        if (!valeur.equals(lDesignation) && isUpdateMode()) {
            
            if (isDesignationMandatory() && "".equals(valeur)) {
                String lMsg =
                        MessageBundle.getMessage(Constants.DESIGNATION_INVALID);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
            if ((article instanceof Cabinet)
                    && articleBean.existCabinetDesignation((String) valeur)) {
                
                String lMsg =
                        MessageBundle
                                .getMessage(Constants.DESIGNATION_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }

    public Article getArticle() {
        return article;
    }

    /**
     * @return the airbusSN
     */
    public String getAirbusSN() {
        return airbusSN;
    }

    /**
     * @return the designation
     */
    public String getDesignation() {
        return designation;
    }

    /**
     * @param designation the designation to set
     */
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    /**
     * @param airbusSN the airbusSN to set
     */
    public void setAirbusSN(String airbusSN) {
        this.airbusSN = airbusSN;
    }

    /**
     * @return the manufacturerSN
     */
    public String getManufacturerSN() {
        return manufacturerSN;
    }

    /**
     * @param manufacturerSN the manufacturerSN to set
     */
    public void setManufacturerSN(String manufacturerSN) {
        this.manufacturerSN = manufacturerSN;
    }

    /**
     * @return the airbusPnId
     */
    public Long getAirbusPnId() {
        return airbusPnId;
    }
    
    /**
     * @param airbusPnId
     *            the airbusPnId to set
     */
    public void setAirbusPnId(Long airbusPnId) {
        this.airbusPnId = airbusPnId;
    }
    
    /**
     * @return the airbusPNIdentifier
     */
    public String getAirbusPNIdentifier() {
        return airbusPNIdentifier;
    }
    
    /**
     * @param airbusPNIdentifier
     *            the airbusPNIdentifier to set
     */
    public void setAirbusPNIdentifier(String airbusPNIdentifier) {
        this.airbusPNIdentifier = airbusPNIdentifier;
    }
    
    /**
     * @return the cmsCode
     */
    public String getCmsCode() {
        return cmsCode;
    }

    /**
     * @param cmsCode the cmsCode to set
     */
    public void setCmsCode(String cmsCode) {
        this.cmsCode = cmsCode;
    }

    /**
     * @return the number of available characters into the comment field
     */
    public int getAvailableChars() {
        if (comment != null) {
            availableChars = COMMENT_LENGTH - comment.length();
        }
        return availableChars;
    }
    
    /**
     * @return the bootloader
     */
    public String getBootloader() {
        return bootloader;
    }
    
    /**
     * @param bootloader
     *            the bootloader to set
     */
    public void setBootloader(String bootloader) {
        this.bootloader = bootloader;
    }
    
    public Date getActiveStockControlDate() {
        return activeStockControlDate;
    }
    
    public void setActiveStockControlDate(Date activeStockControlDate) {
        this.activeStockControlDate = activeStockControlDate;
    }

    /**
     * @return the calibration
     */
    public Boolean getCalibration() {
        return calibration;
    }
    
    /**
     * @param calibration
     *            the calibration to set
     */
    public void setCalibration(Boolean calibration) {
        this.calibration = calibration;
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
     * @return a boolean indicating if software has to be displayed
     *         (Board)
     */
    public Boolean hasActiveStockControl() {
        return article instanceof Board;
    }
    
    /**
     * @return a boolean indicating if the designation is mandatory
     */
    public boolean isDesignationMandatory() {
        return (article instanceof Rack && !(article instanceof Switch));
    }
    
    /**
     * @return the ethernetBoard
     */
    public boolean isEthernetBoard() {
        if (article instanceof Board && typeArticleLabel != null)
            return typeArticleLabel.matches(Constants.REGEX_ETHERNETBOARD_TYPE);
        else
            return false;
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
     * @return a boolean indicating the selected PC (if not null) has
     *         obsolescence data
     */
    public Boolean hasObsoData() {
        
        if (article == null)
            return false;
        else
            return (obsolescenceBean.findByReference(article.getAirbusPN(),
                    article.getTypeArticle()) != null || obsolescenceBean
                    .findByReference(article.getManufacturerPN(),
                            article.getTypeArticle()) != null);
    }
    
    /**
     * @return a boolean indicating if IPAddress is to be displayed
     *         (Switch)
     */
    public Boolean hasIPAddress() {
        // Only Switches have IP Address
        return article instanceof Switch;
    }
    
    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * @param ipAddress
     *            the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * @return the manufacturerPNIdentifier
     */
    public String getManufacturerPNIdentifier() {
        return manufacturerPNIdentifier;
    }
    
    /**
     * @param manufacturerPNIdentifier
     *            the manufacturerPNIdentifier to set
     */
    public void setManufacturerPNIdentifier(String manufacturerPNIdentifier) {
        this.manufacturerPNIdentifier = manufacturerPNIdentifier;
    }
    
    /**
     * @return a boolean indicating if the PN is mandatory
     */
    public boolean isPNMandatory() {
        
        return (article instanceof Board);
    }
    
    /**
     * @return the selectAirbusPN
     */
    public List<SelectItem> getSelectAirbusPN() {
        ArrayList<SelectItem> selectAirbusPN = new ArrayList<SelectItem>();
        if (typeArticleId != null) {
            TypeArticle managedTypeArticle =
                    articleBean.findTypeArticleById(typeArticleId);
            for (AirbusPN airbusPN : managedTypeArticle.getListAirbusPN()) {
                selectAirbusPN.add(new SelectItem(airbusPN.getId(), airbusPN
                        .getIdentifier()));
            }
        }
        return selectAirbusPN;
    }
    
    /**
     * @return the selectArticleState
     */
    public List<SelectItem> getSelectArticleState() {
        List<SelectItem> lSelectArticleState = new ArrayList<SelectItem>();
        for (ArticleState lState : ArticleState.values()) {
            lSelectArticleState.add(new SelectItem(lState, lState.toString()));
        }
        lSelectArticleState.remove(ArticleState.ToBeTested);
        return lSelectArticleState;
    }
    
    /**
     * @return the selectUseState
     */
    public List<SelectItem> getSelectUseState() {
        List<SelectItem> lSelectUseState = new ArrayList<SelectItem>();
        
        LogInController lLoginController = findBean(LogInController.class);
        
        for (UseState lState : UseState.values()) {
            if (UseState.isApplicable(lState, article)) {
                if ((lState == UseState.Archived && lLoginController
                        .isAuthorized(
                                RightMaskAction.ArchivedCRUDAuthorization))
                        || lState != UseState.Archived) {
                lSelectUseState.add(new SelectItem(lState, lState
                        .toString()));
                }
            }
        }
        return lSelectUseState;
    }
    
    /**
     * @return the acquisitionDate message depending on the selected state
     */
    public String getAcquisitionDateMsg() {
        
        if (article != null && article.getUseState() != null
                && UseState.isOnPurchase(article.getUseState())) {
            return MessageBundle.getMessage("dueAcquisitionDate");
        }
        return MessageBundle.getMessage("acquisitionDate");
    }
    
    /**
     * @return the selectedDate
     */
    public Date getAcquisitionDate() {
        return acquisitionDate;
    }
    
    /**
     * @param selectedDate
     *            the selectedDate to set
     */
    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
    
    /**
     * @return the selectTypeArticle list
     */
    public List<String> getSelectTypeArticle() {
        return selectTypeArticle;
    }
    
    /**
     * @return the showActiveStockControlDate
     */
    public boolean isShowActiveStockControlDate() {
        return (article instanceof Board);
    }
    
    /**
     * @return the showBootloader
     */
    public boolean isShowBootloader() {
        if (article instanceof Board && typeArticleLabel != null)
            return typeArticleLabel.matches(Constants.REGEX_BOOTLOADER_TYPE);
        else
            return false;
    }
    
    /**
     * @return the showCalibration
     */
    public boolean isShowCalibration() {
        if (article instanceof Board && typeArticleLabel != null)
            return typeArticleLabel.matches(Constants.REGEX_CALIBRATION_TYPE);
        else
            return false;
    }
    
    /**
     * @return the showDesignation
     */
    public boolean isShowDesignation() {
        return ((article instanceof Rack) && !(article instanceof Switch))
                || (article instanceof Cabinet);
    }
    
    /**
     * @return the showRevision
     */
    public boolean isShowRevision() {
        return (article instanceof Board && article.getManufacturerPN() != null);
    }
    
    /**
     * @return a boolean indicating if software has to be displayed
     *         (Board)
     */
    public Boolean hasSoftware() {
        return article instanceof Board;
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
     * Add the selected software to the softwares list if it does not already
     * contain it
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
            softwares.add(lSoftware);
        }
    }
    
    /**
     * Remove the software from the softwares list
     */
    public void removeSoftware() {
        softwares.remove(software);
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
     * @return the stateSelected
     */
    public ArticleState getStateSelected() {
        return stateSelected;
    }
    
    /**
     * @param pStateSelected
     *            the stateSelected to set
     */
    public void setStateSelected(ArticleState pStateSelected) {
        this.stateSelected = pStateSelected;
    }
    
    /**
     * @return the useStateSelected
     */
    public UseState getUseStateSelected() {
        return useStateSelected;
    }

    /**
     * @param pUseStateSelected
     *            the useStateSelected to set
     */
    public void setUseStateSelected(UseState pUseStateSelected) {
        if (pUseStateSelected.equals(UseState.Archived)
                && !pUseStateSelected.equals(useStateSelected)) {
            setStateSelected(ArticleState.Unusable);
            
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
        useStateSelected = pUseStateSelected;
    }

    /**
     * @return a suggestions list for CMSCode
     */
    public List<String> getSuggestionCMSCode() {
        if (typeArticleId == null) {
            return Collections.emptyList();
        }
        return articleBean.findCMSCodeByTypeArticleId(typeArticleId);
    }
    
    /**
     * @return a suggestions list for ManufacturerPN
     */
    public List<String> getSuggestionManufacturerPN() {
        return articleBean.findAllManufacturerPNIdentifiers();
    }
    
    /**
     * @return the typeArticleId
     */
    public Long getTypeArticleId() {
        return typeArticleId;
    }
    
    /**
     * @param typeArticleId
     *            the typeArticleId to set
     */
    public void setTypeArticleId(Long typeArticleId) {
        this.typeArticleId = typeArticleId;
        TypeArticle lTypeArticle =
                articleBean.findTypeArticleById(typeArticleId);
        if (lTypeArticle != null) {
            this.typeArticleLabel = lTypeArticle.getLabel();
        }
        else {
            this.typeArticleId = null;
            this.typeArticleLabel = null;
        }
    }

    /**
     * @return the typeArticleLabel
     */
    public String getTypeArticleLabel() {
        return typeArticleLabel;
    }

    /**
     * @param typeArticleLabel
     *            the typeArticleLabel to set
     */
    public void setTypeArticleLabel(String typeArticleLabel) {
        this.typeArticleLabel = typeArticleLabel;
        TypeArticle lTypeArticle =
                articleBean.findTypeArticleByName(typeArticleLabel);
        if (lTypeArticle != null) {
            this.typeArticleId = lTypeArticle.getId();
        }
        else {
            this.typeArticleId = null;
            this.typeArticleLabel = null;
        }
    }
}
