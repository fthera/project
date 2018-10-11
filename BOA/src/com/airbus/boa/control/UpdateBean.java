/*
 * ------------------------------------------------------------------------
 * Class : UpdateBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remove;
import javax.ejb.Stateful;

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
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.history.ModificationRecorder;
import com.airbus.boa.entity.history.command.ArticleCommand;
import com.airbus.boa.entity.history.command.Command;
import com.airbus.boa.entity.history.command.ContainerCommand;
import com.airbus.boa.entity.history.command.DefaultCommand;
import com.airbus.boa.entity.history.command.LocationCommand;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.HistoryConstants;
import com.airbus.boa.util.CollectionUtil;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.application.DBConstants;

/**
 * Bean managing the article update with history of the modifications
 */
// Stateful bean in order to execute the methods in only one transaction in
// database
@Stateful
@LocalBean
public class UpdateBean implements Serializable {
    
    private static final long serialVersionUID = 5289876732513048L;
    
    private static Logger log = Logger.getLogger(UpdateBean.class.getName());
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    @EJB
    private DocumentBean documentBean;
    
    private Article article;
    private User currentUser;
    private ModificationRecorder recorder = new ModificationRecorder();
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Map<String, Command> commands = new HashMap<String, Command>();
    
    private String airbusSN;
    private String manufacturerSN;
    private String manufacturerPNName;
    private Long airbusPNId;
    private Long typeArticleId;
    private String typeArticleLabel;
    
    private String cmsCode;
    
    private ArticleState state;
    private UseState useState;
    private Date acquisitionDate;
    private String comment;
    private String bootloader;
    private Boolean calibration;
    private String ipaddress;
    private String designation;
    private Date activeStockControlDate;
    
    private String revH;
    private String revS;
    
    private Location location;
    
    private Container container;
    
    private boolean generalCommentModified;
    
    private List<CommunicationPort> ports = new ArrayList<CommunicationPort>();
    
    private List<Software> softwares = new ArrayList<Software>();
    private List<Software> addedSoftwares = new ArrayList<Software>();
    private List<Software> removedSoftwares = new ArrayList<Software>();
    
    private List<Document> documents = new ArrayList<Document>();
    private List<Document> addedDocuments = new ArrayList<Document>();
    private List<Document> removedDocuments = new ArrayList<Document>();
    
    /**
     * Constructor
     */
    public UpdateBean() {
        super();
        try {
            log.warning("UpdateBean Creation DEBUT");
            
            pcs.addPropertyChangeListener(recorder);
            commands = new HashMap<String, Command>();
            log.warning("UpdateBean Creation FIN");
        }
        catch (Exception e) {
            log.warning(e.getMessage());
        }
    }
    
    /**
     * Cancel changes (remove the listener)
     */
    public void cancelChange() {
        pcs.removePropertyChangeListener(recorder);
    }
    
    /**
     * Execute all stored commands to update the article
     * 
     * @param pHistoryComment
     *            the user history comment to add to each action
     */
    public void saveChange(String pHistoryComment) {
        
        // List of actions about software
        List<Action> actionsSoft = new ArrayList<Action>();
        
        if (recorder.containsProperty(HistoryConstants.MODIFY_SOFTWARE)) {
            
            String login =
                    ((Action) recorder
                            .getProperty(HistoryConstants.MODIFY_SOFTWARE))
                                    .getLogin();
            
            // Update software equipment list
            // (To do at the beginning to avoid integrity violation)
            // Update general action of software modification by details about
            // performed modifications
            Software software;
            for (Software newSoft : addedSoftwares) {
                // Added software
                software = softwareBean.findById(newSoft.getId());
                software.addEquipement(article);
                softwareBean.merge(software);
                Action newModif =
                        new FieldModification(login, null,
                                HistoryConstants.INSTALL_SOFTWARE, null, null,
                                null, software.getCompleteName());
                actionsSoft.add(newModif);
            }
            
            for (Software removedSoft : removedSoftwares) {
                // Removed software
                software = softwareBean.findById(removedSoft.getId());
                software.removeEquipement(article);
                softwareBean.merge(software);
                
                Action newModif =
                        new FieldModification(login, null,
                                HistoryConstants.REMOVE_SOFTWARE, null, null,
                                software.getCompleteName(), null);
                actionsSoft.add(newModif);
            }
            // Remove general action of software modification
            recorder.removeChange(HistoryConstants.MODIFY_SOFTWARE);
        }
        
        // Update the article documents
        List<Action> lDocumentActions = new ArrayList<Action>();
        
        if (recorder.containsProperty(HistoryConstants.MODIFY_DOCUMENTS)) {
            
            String lLogin =
                    ((Action) recorder
                            .getProperty(HistoryConstants.MODIFY_DOCUMENTS))
                            .getLogin();
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            // Add new documents
            for (Document lDocument : addedDocuments) {
                
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
                lDocumentActions.add(lAction);
            }
            
            // Remove old documents
            for (Document lDocument : removedDocuments) {
                
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
                lDocumentActions.add(lAction);
            }
            
            recorder.removeChange(HistoryConstants.MODIFY_DOCUMENTS);
        }
        
        History history = article.getHistory();
        
        DBConstants lDBConstants =
                AbstractController.findBean(DBConstants.class);
        
        final int FIELDMODIFICATION_AFTERVALUE_LENGTH =
                lDBConstants.getFieldmodificationAftervalueLength();
        final int FIELDMODIFICATION_BEFOREVALUE_LENGTH =
                lDBConstants.getFieldmodificationBeforevalueLength();
        
        // Check before/after fields size in order to not exceed the maximum
        // size in database
        List<Action> actions = recorder.getActions();
        for (Action act : actions) {
            if (act instanceof FieldModification) {
                FieldModification modif = (FieldModification) act;
                if (modif.getBeforeValue().length() > FIELDMODIFICATION_BEFOREVALUE_LENGTH) {
                    modif.setBeforeValue(modif
                            .getBeforeValue()
                            .substring(0,
                                    FIELDMODIFICATION_BEFOREVALUE_LENGTH - 10)
                            .concat("..."));
                }
                if (modif.getAfterValue().length() > FIELDMODIFICATION_AFTERVALUE_LENGTH) {
                    modif.setAfterValue(modif
                            .getAfterValue()
                            .substring(0,
                                    FIELDMODIFICATION_AFTERVALUE_LENGTH - 10)
                            .concat("..."));
                }
            }
        }
        
        List<Action> lActions = new ArrayList<Action>();
        lActions.addAll(actionsSoft);
        lActions.addAll(lDocumentActions);
        lActions.addAll(recorder.getActions());
        
        // set history comment if given
        if (!StringUtil.isEmptyOrNull(pHistoryComment)) {
            for (Action lAction : lActions) {
                Comment lAutoComment = lAction.getComment();
                if (lAutoComment != null
                        && !StringUtil.isEmptyOrNull(lAutoComment.getMessage())) {
                    lAction.getComment()
                            .setMessage(
                                    pHistoryComment + " - "
                                            + lAutoComment.getMessage());
                }
                else {
                    Comment lComment = new Comment(pHistoryComment);
                    lAction.setComment(lComment);
                }
            }
        }
        
        // Update the history
        history.getActions().addAll(lActions);
        history = historyBean.merge(history);
        
        // Execute all update actions
        for (String field : recorder.getFields()) {
            
            Command command = commands.get(field);
            if (command != null) {
                try {
                    
                    command.execute();
                    log.warning("Commande exécutée :" + field);
                }
                catch (RuntimeException e) {
                    
                    throw new ValidationException(ExceptionUtil.getRootCause(e)
                            .getMessage());
                }
                catch (Exception e) {
                    throw new ValidationException(ExceptionUtil.getRootCause(e)
                            .getMessage());
                    
                }
            }
        }
        
        // Update the article
        article = articleBean.merge(article);
        
        // Update the article location
        if (recorder.containsProperty(Constants.Location)) {
            
            Location lLocation =
                    (Location) commands.get(Constants.Location).getValue();
            LocationManager lLocationManager = new LocationManager(article);
            lLocationManager.moveTo(lLocation, locationManagerBean);
        }
        
        // Update the article container
        if (recorder.containsProperty(Constants.Container)) {
            
            Container lContainer =
                    (Container) commands.get(Constants.Container).getValue();
            ContainerManager lContainerManager = new ContainerManager(article);
            lContainerManager.linkTo(lContainer, containerManagerBean);
        }
        
        if (recorder.containsProperty(HistoryConstants.ARTICLE_USE_STATE)) {
            String lLogin = ((Action) recorder
                    .getProperty(HistoryConstants.ARTICLE_USE_STATE))
                            .getLogin();
            articleBean.updateChildrenUsestate(article, lLogin);
        }
        
        // Clean the bean attributes
        pcs.removePropertyChangeListener(recorder);
        recorder.clearChanges();
        commands.clear();
    }
    
    @PreDestroy
    public void destroy() {
        log.warning("destruction");
    }
    
    @Remove
    public void remove() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Remove du Bean UpdateBean");
        }
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
        Object oldValue = this.acquisitionDate;
        this.acquisitionDate = acquisitionDate;
        pcs.firePropertyChange(HistoryConstants.ACQUISITION_DATE, oldValue,
                acquisitionDate);
        commands.put(HistoryConstants.ACQUISITION_DATE, new ArticleCommand(
                article, acquisitionDate) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                article.setAcquisitionDate((Date) value);
            }
        });
    }
    
    /**
     * @return the actions
     */
    public List<Action> getActions() {
        log.warning("getActions - recorder : "
                + String.valueOf(recorder.getActions()));
        return recorder.getActions();
    }
    
    /**
     * @return the activeStockControlDate
     */
    public Date getActiveStockControlDate() {
        return activeStockControlDate;
    }
    
    /**
     * @param pActiveStockControlDate
     *            the activeStockControlDate to set
     */
    public void setActiveStockControlDate(Date pActiveStockControlDate) {
        Object oldValue = activeStockControlDate;
        activeStockControlDate = pActiveStockControlDate;
        pcs.firePropertyChange(HistoryConstants.ACTIVE_STOCK_CONTROL_DATE,
                oldValue, activeStockControlDate);
        commands.put(HistoryConstants.ACTIVE_STOCK_CONTROL_DATE,
                new ArticleCommand(article, activeStockControlDate) {
                    
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void execute() throws IllegalArgumentException,
                            IllegalAccessException, InvocationTargetException {
                        
                        ((Board) article)
                                .setActiveStockControlDate((Date) value);
                    }
                });
    }
    
    /**
     * @return the airbusPNId
     */
    public Long getAirbusPNId() {
        return airbusPNId;
    }
    
    /**
     * @param airbusPNId
     *            the airbusPNId to set
     */
    public void setAirbusPNId(Long airbusPNId) {
        Object oldValue = articleBean.findAirbusPNById(this.airbusPNId);
        this.airbusPNId = airbusPNId;
        Object newValue = articleBean.findAirbusPNById(this.airbusPNId);
        pcs.firePropertyChange(HistoryConstants.AIRBUS_PN, oldValue, newValue);
        commands.put(HistoryConstants.AIRBUS_PN, new ArticleCommand(article,
                newValue) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                article.setAirbusPN((AirbusPN) value);
            }
        });
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
        
        String old = this.airbusSN;
        this.airbusSN = StringUtil.notEmpty(airbusSN);
        pcs.firePropertyChange(HistoryConstants.AIRBUS_SN, old, airbusSN);
        
        commands.put(HistoryConstants.AIRBUS_SN, new ArticleCommand(article,
                airbusSN) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                article.setAirbusSN((String) value);
            }
        });
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
    /**
     * @param article
     *            the article to set
     */
    public void setArticle(Article article) {
        
        this.article = article;
        if (this.article != null) {
            setArticleId(this.article.getId());
        }
    }
    
    /**
     * Initialize the bean with the article attributes
     * 
     * @param articleId
     *            the articleId to set
     */
    private void setArticleId(Long articleId) {
        if (articleId != null) {
            
            // TODO voir ici pour loquer le BEAN SI BESOIN
            article = articleBean.findArticleById(articleId);
            
            airbusSN = article.getAirbusSN();
            manufacturerSN = article.getManufacturerSN();
            manufacturerPNName =
                    (article.getManufacturerPN() == null) ? null : article
                            .getManufacturerPN().getIdentifier();
            airbusPNId =
                    (article.getAirbusPN() == null) ? null : article
                            .getAirbusPN().getId();
            typeArticleId = article.getTypeArticle().getId();
            typeArticleLabel = article.getTypeArticle().getLabel();
            cmsCode = article.getCmsCode();
            state = article.getState();
            useState = article.getUseState();
            acquisitionDate = article.getAcquisitionDate();
            
            Comment generalComment = article.getHistory().getGeneralComment();
            if (generalComment != null) {
                comment = generalComment.getMessage();
            }
            generalCommentModified = false;
            
            if (article instanceof Board) {
                Board board = (Board) article;
                bootloader = board.getBootLoader();
                calibration = board.getCalibrated();
                
                // Liste des ports
                ports = new ArrayList<CommunicationPort>(board.getPorts());
                
                if (manufacturerPNName != null) {
                    revH = board.getRevH();
                    revS = board.getRevS();
                }
                
                activeStockControlDate = board.getActiveStockControlDate();
            }
            
            if (article instanceof Switch) {
                Switch swch = (Switch) article;
                ipaddress = swch.getIpAddress();
            }
            
            // Liste des logiciels installés
            softwares = new ArrayList<Software>(article.getSoftwares());
            
            // DESIGNATION RACK ET CABINET
            if (article instanceof Rack) {
                designation = ((Rack) article).getDesignation();
            }
            else if (article instanceof Cabinet) {
                designation = ((Cabinet) article).getDesignation();
            }
            
            location = article.getLocation();
            
            container = article.getContainer();
            
            documents =
                    new ArrayList<Document>(
                            documentBean.findDocumentsByArticle(article));
            
            pcs.removePropertyChangeListener(recorder);
            recorder = new ModificationRecorder();
            recorder.setUser(getCurrentUser());
            pcs.addPropertyChangeListener(recorder);
        }
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
        Object oldValue = this.bootloader;
        this.bootloader = StringUtil.notEmpty(bootloader);
        pcs.firePropertyChange(HistoryConstants.BOOT_LOADER, oldValue,
                bootloader);
        commands.put(HistoryConstants.BOOT_LOADER, new ArticleCommand(article,
                bootloader) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                ((Board) article).setBootLoader((String) value);
            }
        });
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
        Object oldValue = this.calibration;
        this.calibration = calibration;
        pcs.firePropertyChange(HistoryConstants.CALIBRATION, oldValue,
                calibration);
        commands.put(HistoryConstants.CALIBRATION, new ArticleCommand(article,
                calibration) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                ((Board) article).setCalibrated((Boolean) value);
            }
        });
    }
    
    /**
     * @return the cmsCode
     */
    public String getCmsCode() {
        
        return cmsCode;
    }
    
    /**
     * @param cmsCode
     *            the cmsCode to set
     */
    public void setCmsCode(String cmsCode) {
        Object oldValue = this.cmsCode;
        this.cmsCode = StringUtil.notEmpty(cmsCode);
        pcs.firePropertyChange(HistoryConstants.CMS_CODE, oldValue, cmsCode);
        commands.put(HistoryConstants.CMS_CODE, new ArticleCommand(article,
                cmsCode) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                article.setCmsCode((String) value);
            }
        });
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param message
     *            the comment to set
     */
    public void setComment(String message) {
        comment = message;
        Comment generalComment = article.getHistory().getGeneralComment();
        
        // a message already existed
        if (generalComment != null) {
            
            // case of empty message
            if (message == null || message.isEmpty()) {
                // remove general comment
                article.getHistory().setGeneralComment(null);
                
                generalCommentModified = true;
                return;
            }
            
            // case of not empty message
            if (message != null && (!message.isEmpty())
                    && generalComment.getMessage() != null) {
                // check that previous and new messages are different
                if (!message.equals(generalComment.getMessage())) {
                    generalCommentModified = true;
                    article.getHistory().getGeneralComment()
                            .setMessage(message);
                    return;
                }
            }
            
        }
        else {
            
            if (message != null && (!message.isEmpty())) {
                article.getHistory().setGeneralComment(new Comment(message));
                
                generalCommentModified = true;
            }
            
        }
    }
    
    /**
     * @param pContainer
     *            the container to set
     */
    public void setContainer(Container pContainer) {
        Object lOldValue = container;
        container = pContainer;
        Object lNewValue = pContainer;
        pcs.firePropertyChange(Constants.Container, lOldValue, lNewValue);
        
        ContainerCommand lCommand = new ContainerCommand(article, lNewValue);
        
        commands.put(Constants.Container, lCommand);
    }
    
    /**
     * @return the currentUser
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * @param currentUser
     *            the currentUser to set
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        recorder.setUser(currentUser);
    }
    
    /**
     * @return the designation
     */
    public String getDesignation() {
        return designation;
    }
    
    /**
     * @param designation
     *            the designation to set
     */
    public void setDesignation(String designation) {
        Object oldValue = this.designation;
        
        designation = StringUtil.notEmpty(designation);
        // (designation == null || designation.isEmpty())?null:designation ;
        this.designation = designation;
        pcs.firePropertyChange(HistoryConstants.DESIGNATION, oldValue,
                designation);
        Method method = null;
        Class<?> paramtypes[] = { String.class };
        
        if (article instanceof Cabinet) {
            Cabinet cabinet = (Cabinet) article;
            
            try {
                
                method =
                        cabinet.getClass().getMethod("setDesignation",
                                paramtypes);
                commands.put(HistoryConstants.DESIGNATION, new DefaultCommand(
                        article, method, new Object[] { designation }));
            }
            catch (NoSuchMethodException e) {
                log.warning("setDesignation impossible : " + e.getMessage());
            }
        }
        else if (article instanceof Rack) {
            try {
                
                Rack rack = (Rack) article;
                
                method =
                        rack.getClass().getMethod("setDesignation", paramtypes);
                commands.put(HistoryConstants.DESIGNATION, new DefaultCommand(
                        article, method, new Object[] { designation }));
                
            }
            catch (NoSuchMethodException e) {
                log.warning("setDesignation impossible : " + e.getMessage());
            }
        }
    }
    
    /**
     * @return true if the general comment has been modified, else false
     */
    public boolean isGeneralCommentModified() {
        return generalCommentModified;
    }
    
    /**
     * @return the ipaddress
     */
    public String getIpaddress() {
        return ipaddress;
    }
    
    /**
     * @param ipaddress
     *            the ipaddress to set
     */
    public void setIpaddress(String ipaddress) {
        String oldValue = this.ipaddress;
        this.ipaddress = ipaddress;
        pcs.firePropertyChange(Constants.IPAddress, oldValue, ipaddress);
        commands.put(Constants.IPAddress,
                new ArticleCommand(article, ipaddress) {
                    
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void execute() throws IllegalArgumentException,
                            IllegalAccessException, InvocationTargetException {
                        
                        ((Switch) article).setIpAddress((String) value);
                    }
                });
    }
    
    /**
     * @param pDocuments
     *            the list of documents to set
     */
    public void setDocuments(List<Document> pDocuments) {
        
        List<Document> lOldDocuments = documents;
        List<Document> lNewDocuments = pDocuments;
        
        addedDocuments.clear();
        removedDocuments.clear();
        CollectionUtil.retrieveAddedAndRemoved(documents, lNewDocuments,
                addedDocuments, removedDocuments);
        
        documents = pDocuments;
        
        String lOldValueStr = "";
        String lNewValueStr = "";
        for (Document lDocument : lOldDocuments) {
            lOldValueStr = lOldValueStr + lDocument;
        }
        for (Document lDocument : lNewDocuments) {
            lNewValueStr = lNewValueStr + lDocument;
        }
        
        pcs.firePropertyChange(HistoryConstants.MODIFY_DOCUMENTS, lOldValueStr,
                lNewValueStr);
        commands.put(HistoryConstants.MODIFY_DOCUMENTS,
                new ArticleCommand(article, pDocuments));
    }
    
    /**
     * @param pLocation
     *            the location to set
     */
    public void setLocation(Location pLocation) {
        Object oldValue = location;
        location = pLocation;
        Object newValue = pLocation;
        pcs.firePropertyChange(Constants.Location, oldValue, newValue);
        
        LocationCommand command = new LocationCommand(article, newValue);
        
        commands.put(Constants.Location, command);
    }
    
    /**
     * @return the manufacturerPNName
     */
    public String getManufacturerPNName() {
        return manufacturerPNName;
    }
    
    /**
     * @param manufacturerPNName
     *            the manufacturerPNName to set
     */
    public void setManufacturerPNName(String manufacturerPNName) {
        if (!(manufacturerPNName.equals(this.manufacturerPNName))) {
            Object oldValue =
                    articleBean
                            .findManufacturerPNByName(this.manufacturerPNName);
            Object newValue = null;
            // on ajoute un nouveau PN s'il n'existe pas encor
            if (manufacturerPNName != null
                    && !manufacturerPNName.trim().isEmpty()) {
                manufacturerPNName = manufacturerPNName.trim();
                newValue =
                        articleBean
                                .findManufacturerPNByName(manufacturerPNName);
                if (newValue == null) { // il faut creer un nouveau
                                        // ManufacturerPN.
                    newValue =
                            articleBean.createPN(new ManufacturerPN(
                                    manufacturerPNName));
                }
            }
            // sinon on enregistre un retrait de manufacturer PN avec une valeur
            // "null"
            
            this.manufacturerPNName = manufacturerPNName;
            pcs.firePropertyChange(HistoryConstants.MANUFACTURER_PN, oldValue,
                    newValue);
            
            commands.put(HistoryConstants.MANUFACTURER_PN, new ArticleCommand(
                    article, newValue) {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public void execute() throws IllegalArgumentException,
                        IllegalAccessException, InvocationTargetException {
                    article.setManufacturerPN((ManufacturerPN) value);
                }
            });
            
        }
    }
    
    /**
     * @return the manufacturerSN
     */
    public String getManufacturerSN() {
        return manufacturerSN;
    }
    
    /**
     * @param manufacturerSN
     *            the manufacturerSN to set
     */
    public void setManufacturerSN(String manufacturerSN) {
        String oldValue = this.manufacturerSN;
        this.manufacturerSN = StringUtil.notEmpty(manufacturerSN);
        pcs.firePropertyChange(HistoryConstants.MANUFACTURER_SN, oldValue,
                manufacturerSN);
        
        commands.put(HistoryConstants.MANUFACTURER_SN, new ArticleCommand(
                article, manufacturerSN) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                article.setManufacturerSN((String) value);
            }
        });
        
    }
    
    /**
     * @param ports
     *            the list of ports to set
     */
    public void setPorts(List<CommunicationPort> ports) {
        List<CommunicationPort> oldValue = this.ports;
        List<CommunicationPort> newValue = ports;
        this.ports = ports;
        
        // Formatage de l'affichage des infos des ports comm
        String oldDataPorts = "";
        String newDataPorts = "";
        for (CommunicationPort port : oldValue) {
            oldDataPorts = oldDataPorts + port;
        }
        for (CommunicationPort port : newValue) {
            newDataPorts = newDataPorts + port;
        }
        
        pcs.firePropertyChange(Constants.ModifyPort, oldDataPorts, newDataPorts);
        commands.put(Constants.ModifyPort, new ArticleCommand(article, ports) {
            
            private static final long serialVersionUID = 1L;
            
            @SuppressWarnings("unchecked")
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                ((Board) article).setPorts((List<CommunicationPort>) value);
            }
        });
        
    }
    
    /**
     * @return the revH
     */
    public String getRevH() {
        return revH;
    }
    
    /**
     * @param revH
     *            the revH to set
     */
    public void setRevH(String revH) {
        String oldValue = this.revH;
        this.revH = revH;
        if (article instanceof Board) {
            pcs.firePropertyChange(HistoryConstants.REV_H, oldValue, revH);
            commands.put(HistoryConstants.REV_H, new ArticleCommand(article,
                    revH) {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public void execute() throws IllegalArgumentException,
                        IllegalAccessException, InvocationTargetException {
                    
                    ((Board) article).setRevH((String) value);
                }
            });
        }
    }
    
    /**
     * @return the revS
     */
    public String getRevS() {
        return revS;
    }
    
    /**
     * @param revS
     *            the revS to set
     */
    public void setRevS(String revS) {
        String oldValue = this.revS;
        this.revS = revS;
        if (article instanceof Board) {
            pcs.firePropertyChange(HistoryConstants.REV_S, oldValue, revS);
            commands.put(HistoryConstants.REV_S, new ArticleCommand(article,
                    revS) {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public void execute() throws IllegalArgumentException,
                        IllegalAccessException, InvocationTargetException {
                    
                    ((Board) article).setRevS((String) value);
                }
            });
        }
    }
    
    /**
     * @param newSoftwares
     *            the list of software to set
     */
    public void setSoftwares(List<Software> newSoftwares) {
        List<Software> oldValue = softwares;
        List<Software> newValue = newSoftwares;
        
        // Mémorisation des différences entre les 2 listes,
        // elles seront utilisées au moment du merge de l'article,
        // pour réaliser le merge au niveau software
        addedSoftwares.clear();
        removedSoftwares.clear();
        CollectionUtil.retrieveAddedAndRemoved(softwares, newSoftwares,
                addedSoftwares, removedSoftwares);
        
        softwares = newSoftwares;
        
        // Formatage de l'affichage des infos des logiciels
        String oldData = "";
        String newData = "";
        for (Software soft : oldValue) {
            oldData = oldData + soft;
        }
        
        for (Software soft : newValue) {
            newData = newData + soft;
        }
        
        pcs.firePropertyChange(HistoryConstants.MODIFY_SOFTWARE, oldData,
                newData);
        commands.put(HistoryConstants.MODIFY_SOFTWARE,
                new ArticleCommand(article, softwares) {
            
            private static final long serialVersionUID = 1L;
            
            @SuppressWarnings("unchecked")
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                ((Board) article).setSoftwares((List<Software>) value);
            }
        });
        
    }
    
    /**
     * @return the state
     */
    public ArticleState getState() {
        return state;
    }
    
    /**
     * @param state
     *            the state to set
     */
    public void setState(ArticleState state) {
        Object oldValue = this.state;
        this.state = state;
        pcs.firePropertyChange(HistoryConstants.ARTICLE_STATE, oldValue, state);
        commands.put(HistoryConstants.ARTICLE_STATE, new ArticleCommand(
                article, state) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                
                article.setState((ArticleState) value);
            }
        });
    }
    
    /**
     * @param state
     *            the state to set
     */
    public void setUseState(UseState useState) {
        Object oldValue = this.useState;
        this.useState = useState;
        pcs.firePropertyChange(HistoryConstants.ARTICLE_USE_STATE, oldValue,
                useState);
        commands.put(HistoryConstants.ARTICLE_USE_STATE,
                new ArticleCommand(article, useState) {
                    
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void execute() throws IllegalArgumentException,
                            IllegalAccessException, InvocationTargetException {
                        
                        article.setUseState((UseState) value);
                    }
                });
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
        TypeArticle oldValue = articleBean.findTypeArticleById(this.typeArticleId);
        
        this.typeArticleId = typeArticleId;
        TypeArticle newValue = articleBean.findTypeArticleById(this.typeArticleId);
        this.typeArticleLabel = newValue.getLabel();
        pcs.firePropertyChange(HistoryConstants.TYPE_ARTICLE, oldValue,
                newValue);
        commands.put(HistoryConstants.TYPE_ARTICLE, new ArticleCommand(article,
                newValue) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void execute() throws IllegalArgumentException,
                    IllegalAccessException, InvocationTargetException {
                article.setTypeArticle((TypeArticle) value);
            }
        });
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
        setTypeArticleId(articleBean.findTypeArticleByName(typeArticleLabel)
                .getId());
    }
    
}
