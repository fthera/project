/*
 * ------------------------------------------------------------------------
 * Class : ObsolescenceController
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.obso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.obso.ObsolescenceReference;
import com.airbus.boa.entity.obso.ObsolescenceReference.ReferenceType;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.item.AbstractItemController;

/**
 * Controller managing the display of unit obsolescence data for update and
 * creation.
 */
@ManagedBean(name = ObsolescenceController.BEAN_NAME)
@ViewScoped
public class ObsolescenceController extends AbstractItemController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "obsolescenceController";

    private ObsolescenceData obsoData;
    private Long obsoDataId;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private ValueListBean avlBean;
    
    @EJB
    private SoftwareBean softBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private HistoryBean historyBean;
    
    private String supplier;
    
    private String personInCharge;
    private String comments;
    
    private Date continuityDate;
    private Date endOfOrderDate;
    private Date endOfProductionDate;
    private Date endOfSupportDate;
    private Date obsolescenceDate;
    private Date lastObsolescenceUpdate;
    
    private Long mtbf;
    
    private Long actionId;
    private Long strategyId;
    private Long manufacturerStatusId;
    private Long airbusStatusId;
    private Long consultPeriodId;
    
    private List<SelectItem> selectAction = new ArrayList<SelectItem>();
    private List<SelectItem> selectStrategy = new ArrayList<SelectItem>();
    private List<SelectItem> selectManufacturerStatus =
            new ArrayList<SelectItem>();
    private List<SelectItem> selectAirbusStatus = new ArrayList<SelectItem>();
    private List<SelectItem> selectConsultPeriod = new ArrayList<SelectItem>();
    
    private ReferenceType referenceTypeChosen;
    
    private SelectItem[] optionsRadioReferenceType;
    
    private String selectedReferenceName = "";
    
    private TypeArticle selectedTypeArticle = null;
    
    public ObsolescenceController() {
        itemPage = NavigationConstants.OBSO_MANAGEMENT_PAGE;
        listPage = NavigationConstants.OBSO_LIST_PAGE;
        errorId = NavigationConstants.OBSO_MGMT_ERROR_ID;
        
        generateOptionRadios();
        // give a default value
        referenceTypeChosen =
                (ReferenceType) optionsRadioReferenceType[0].getValue();
    }

    @Override
    protected void createItem() throws Exception {     
        if (checkValidity()) {
            ObsolescenceReference lReference = retrieveReference();
            
            if (lReference == null) {
                throw new ValidationException(
                        MessageBundle
                                .getMessage(Constants.OBSO_INVALID_REFERENCE));
            }
            
            obsoData.setReference(lReference);
            setAllAttributesFromController();
            obsoData = obsoBean.create(obsoData);
            obsoDataId = obsoData.getId();
        }
    }
    
    @Override
    public void updateItem() throws Exception {
        setAllAttributesFromController();
        obsoBean.merge(obsoData);
    }
    
    @Override
    protected void deleteItem() throws Exception {
        obsoBean.delete(obsoData);
    }
    
    @Override
    protected void initItemWithNew() {
        obsoData = new ObsolescenceData();
    }
    
    @Override
    protected void initItemFromDatabase() {
        obsoData = obsoBean.findById(obsoDataId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        referenceTypeChosen =
                (ReferenceType) optionsRadioReferenceType[0].getValue();
        selectedReferenceName = null;
        selectedTypeArticle = null;

        if (obsoData != null) {
            setSupplier(obsoData.getSupplier());
            
            setEndOfOrderDate(obsoData.getEndOfOrderDate());
            setObsolescenceDate(obsoData.getObsolescenceDate());

            setEndOfSupportDate(obsoData.getEndOfSupportDate());
            setEndOfProductionDate(obsoData.getEndOfProductionDate());

            setActionId((obsoData.getCurrentAction() != null) ? obsoData
                    .getCurrentAction().getId() : null);
            setMtbf(obsoData.getMtbf());

            setStrategyId((obsoData.getStrategyKept() != null) ? obsoData
                    .getStrategyKept().getId() : null);
            setContinuityDate(obsoData.getContinuityDate());
            
            setManufacturerStatusId((obsoData.getManufacturerStatus() != null) ? obsoData
                    .getManufacturerStatus().getId() : null);
            setAirbusStatusId((obsoData.getAirbusStatus() != null) ? obsoData
                    .getAirbusStatus().getId() : null);
            
            setLastObsolescenceUpdate(obsoData.getLastObsolescenceUpdate());
            setConsultPeriodId((obsoData.getConsultPeriod() != null) ? obsoData
                    .getConsultPeriod().getId() : null);
            
            setPersonInCharge(obsoData.getPersonInCharge());
            setComments(obsoData.getCommentOnStrategy());
            
            obsoBean.computeStock(obsoData);
        }
    }

    /**
     * @return the computed next consulting date depending on the last
     *         obsolescence update
     *         and on the selected consult period
     */
    public Date getComputedNextConsultingDate() {
        Date nextConsultingDate =
                ObsolescenceData.calculateNextConsultingDate(
                        lastObsolescenceUpdate, avlBean
                                .findAttributeValueListById(
                                        ConsultPeriod.class, consultPeriodId));
        return nextConsultingDate;
    }
    
    /**
     * Set the chosen reference type to the new value retrieved from the
     * provided event.
     * Check validity of the selected reference name depending on the chosen
     * reference type.
     * Initialize attribute indicating if a new value has to be created.
     * 
     * @param pEvent
     *            the event sent while reference type has been changed
     */
    public void changeReferenceTypeListener(ValueChangeEvent pEvent) {
        setReferenceTypeChosen(((ReferenceType) pEvent.getNewValue()));
        selectedReferenceName = null;
        selectedTypeArticle = null;
        checkValidity();
    }
    
    /**
     * Set the selected reference name to the new value retrieved from the
     * provided event.
     * Check validity of the selected reference name depending on the chosen
     * reference type.
     * Initialize attribute indicating if a new value has to be created.
     * 
     * @param pEvent
     *            the event sent while reference type has been changed
     */
    public void changeSelectedReferenceNameListener(AjaxBehaviorEvent pEvent) {
        if (StringUtil.isEmptyOrNull(selectedReferenceName)) {
            selectedTypeArticle = null;
        }
        checkValidity();
    }
    
    /**
     * Set the selected Type Article reference to the new value retrieved from
     * the provided event.
     * Check validity of the selected reference elements depending on the chosen
     * reference type.
     * 
     * @param pEvent
     *            the event sent while reference type has been changed
     */
    public void changeSelectedTypeArticleListener(ValueChangeEvent pEvent) {
        
        setSelectedTypeArticleId((Long) pEvent.getNewValue());
        checkValidity();
    }
    
    /**
     * Check validity of the selected reference name depending on the chosen
     * reference type.
     * Initialize attribute indicating if a new value has to be created.
     * 
     * @return a boolean indicating if the selected reference name is valid
     */
    private boolean checkValidity() {
        
        switch (referenceTypeChosen) {
        
        case AIRBUSPN_TYPEARTICLE:
            
            AirbusPN lAirbusPN = null;
            if (!StringUtil.isEmptyOrNull(selectedReferenceName)) {
                lAirbusPN =
                        articleBean.findAirbusPNByName(selectedReferenceName);
                // check the Airbus PN existence
                if (lAirbusPN == null) {
                    
                    // reset the Article Type since Airbus PN is not found
                    selectedTypeArticle = null;
                    
                    Utils.addFacesMessage(
                            NavigationConstants.OBSO_MGMT_ERROR_ID,
                            MessageBundle
                                    .getMessage(Constants.AIRBUS_PN_NOT_FOUND));
                    return false;
                }
            }
            
            // search corresponding Article Types...
            if (lAirbusPN != null) {
                List<TypeArticle> lAirbusPNTypeArticles =
                        articleBean.findAllTypeArticleForPN(lAirbusPN);
                if (lAirbusPNTypeArticles != null) {
                    if (lAirbusPNTypeArticles.size() == 1) {
                        // if only one Article Type is found, select it
                        selectedTypeArticle = lAirbusPNTypeArticles.get(0);
                    }
                    else if (lAirbusPNTypeArticles.size() == 0) {
                        // reset the Article Type since no one has been found
                        selectedTypeArticle = null;
                    }
                    else {
                        if (selectedTypeArticle != null
                                && !lAirbusPNTypeArticles
                                        .contains(selectedTypeArticle)) {
                            // reset the Article Type since it is no more
                            // contained by the available Article Types for the
                            // Airbus PN
                            selectedTypeArticle = null;
                        }
                    }
                }
                else {
                    // reset the Article Type since no one has been found
                    selectedTypeArticle = null;
                }
            }
            
            // if at least one reference is empty, no check can be performed
            if (lAirbusPN == null || selectedTypeArticle == null) {
                return true;
            }
            
            // check that the Airbus PN and the Type Article are not already
            // obsolescence managed
            ObsolescenceReference lAirbusObsolescenceReference =
                    new ObsolescenceReference(lAirbusPN, selectedTypeArticle);
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lAirbusObsolescenceReference)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE));
                return false;
            }
            
            // check that no article is already obsolescence managed with its
            // manufacturer PN
            if (!obsoBean.isObsoCreationValidForExistingPN(lAirbusPN,
                    selectedTypeArticle)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_ALREADY_AIRBUSPN_MANAGED_CONFLICT_ERROR));
                return false;
            }
            
            return true;
            
        case MANUFACTURERPN_TYPEARTICLE:
            
            ManufacturerPN lManufacturerPN = null;
            if (!StringUtil.isEmptyOrNull(selectedReferenceName)) {
                lManufacturerPN =
                        articleBean
                                .findManufacturerPNByName(selectedReferenceName);
                // check the Manufacturer PN existence
                if (lManufacturerPN == null) {
                    
                    // reset the Article Type since Manufacturer PN is not found
                    selectedTypeArticle = null;
                    
                    Utils.addFacesMessage(
                            NavigationConstants.OBSO_MGMT_ERROR_ID,
                            MessageBundle
                            .getMessage(Constants.MANUFACTURER_PN_NOT_FOUND));
                    return false;
                }
            }
            
            // search corresponding Article Types...
            if (lManufacturerPN != null) {
                List<TypeArticle> lManufacturerPNTypeArticles =
                        articleBean.findAllTypeArticleForPN(lManufacturerPN);
                if (lManufacturerPNTypeArticles != null) {
                    if (lManufacturerPNTypeArticles.size() == 1) {
                        // if only one Type Article is found, select it
                        selectedTypeArticle =
                                lManufacturerPNTypeArticles.get(0);
                    }
                    else if (lManufacturerPNTypeArticles.size() == 0) {
                        // reset the Article Type since no one has been found
                        selectedTypeArticle = null;
                    }
                    else {
                        if (selectedTypeArticle != null
                                && !lManufacturerPNTypeArticles
                                        .contains(selectedTypeArticle)) {
                            // reset the Article Type since it is no more
                            // contained by the available Article Types for the
                            // Manufacturer PN
                            selectedTypeArticle = null;
                        }
                    }
                }
                else {
                    // reset the Article Type since no one has been found
                    selectedTypeArticle = null;
                }
            }
            
            // if at least one reference is empty, no check can be performed
            if (lManufacturerPN == null || selectedTypeArticle == null) {
                return true;
            }
            
            ObsolescenceReference lManufacturerObsolescenceReference =
                    new ObsolescenceReference(lManufacturerPN,
                            selectedTypeArticle);
            // check that the Manufacturer PN and the Type Article are not
            // already obsolescence managed
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lManufacturerObsolescenceReference)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE));
                return false;
            }
            
            // check that no article is already obsolescence managed with its
            // Airbus PN
            if (!obsoBean.isObsoCreationValidForExistingPN(lManufacturerPN,
                    selectedTypeArticle)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_ALREADY_MANUFACTURERPN_MANAGED_CONFLICT_ERROR));
                return false;
            }
            
            return true;
            
        case SOFTWARE:
            
            Software lSoftware = null;
            if (!StringUtil.isEmptyOrNull(selectedReferenceName)) {
                lSoftware = softBean.findByCompleteName(selectedReferenceName);
                // check the software existence
                if (lSoftware == null) {
                    
                    Utils.addFacesMessage(
                            NavigationConstants.OBSO_MGMT_ERROR_ID,
                            MessageBundle
                                    .getMessage(MessageConstants.SOFTWARE_COMPLETENAME_INCORRECT));
                    return false;
                }
            }
            
            // if the reference is empty, no check can be performed
            if (lSoftware == null) {
                return true;
            }
            
            ObsolescenceReference lSoftwareObsolescenceReference =
                    new ObsolescenceReference(lSoftware);
            // check that the software is not already obsolescence managed
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lSoftwareObsolescenceReference)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE));
                return false;
            }
            
            return true;
            
        case TYPEPC:
            TypePC lTypePC = null;
            if (!StringUtil.isEmptyOrNull(selectedReferenceName)) {
                lTypePC = articleBean.findTypePCByName(selectedReferenceName);
                // check the PC Type existence
                if (lTypePC == null) {
                    
                    Utils.addFacesMessage(
                            NavigationConstants.OBSO_MGMT_ERROR_ID,
                            MessageBundle
                                    .getMessage(Constants.PC_TYPE_NOT_FOUND));
                    return false;
                }
            }
            
            // if the reference is empty, no check can be performed
            if (lTypePC == null) {
                return true;
            }
            
            ObsolescenceReference lTypePCObsolescenceReference =
                    new ObsolescenceReference(lTypePC);
            // check that the PC Type is not already obsolescence managed
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lTypePCObsolescenceReference)) {
                
                Utils.addFacesMessage(
                        NavigationConstants.OBSO_MGMT_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE));
                
                return false;
            }
            
            return true;
            
        default:
            return false;
        }
    }
    
    public void doGenerateConstituantForm() {
        ReportingObsoController controller =
                findBean(ReportingObsoController.class);
        controller.setSelectedObsoData(obsoData);
        controller.doGenerateConstituantForm();
    }
    
    private void generateOptionRadios() {
        int taille = ReferenceType.values().length;
        optionsRadioReferenceType = new SelectItem[taille];
        
        ReferenceType[] referenceTypeTable = ReferenceType.values();
        for (int i = 0; i < taille; i++) {
            SelectItem option =
                    new SelectItem(referenceTypeTable[i],
                            MessageBundle.getMessage(referenceTypeTable[i]
                                    .toString()));
            optionsRadioReferenceType[i] = option;
            
        }
    }
    
    /**
     * Retrieve the obsolescence reference corresponding to the selected
     * reference name and depending on the chosen reference type
     * 
     * @return the retrieved obsolescence reference if available, else null
     */
    private ObsolescenceReference retrieveReference() {
        
        if (StringUtil.isEmptyOrNull(selectedReferenceName)) {
            return null;
        }
        
        switch (referenceTypeChosen) {
        case MANUFACTURERPN_TYPEARTICLE:
            ManufacturerPN lManufacturerPN =
                    articleBean.findManufacturerPNByName(selectedReferenceName);
            if (lManufacturerPN != null && selectedTypeArticle != null) {
                return new ObsolescenceReference(lManufacturerPN,
                        selectedTypeArticle);
            }
            else {
                return null;
            }
            
        case AIRBUSPN_TYPEARTICLE:
            AirbusPN lAirbusPN =
                    articleBean.findAirbusPNByName(selectedReferenceName);
            if (lAirbusPN != null && selectedTypeArticle != null) {
                return new ObsolescenceReference(lAirbusPN, selectedTypeArticle);
            }
            else {
                return null;
            }
            
        case SOFTWARE:
            return new ObsolescenceReference(
                    softBean.findByCompleteName(selectedReferenceName));
            
        case TYPEPC:
            return new ObsolescenceReference(
                    articleBean.findTypePCByName(selectedReferenceName));
        default:
        }
        return null;
    }
    
    /**
     * Initialize the provided obsolescence data with this controller attributes
     * 
     * @param obso
     *            the obsolescence data to update
     */
    private void setAllAttributesFromController() {
        obsoData.setSupplier(supplier);
        
        obsoData.setEndOfOrderDate(endOfOrderDate);
        obsoData.setObsolescenceDate(obsolescenceDate);
        
        obsoData.setEndOfSupportDate(endOfSupportDate);
        obsoData.setEndOfProductionDate(endOfProductionDate);
        
        obsoData.setCurrentAction(avlBean.findAttributeValueListById(
                ActionObso.class, actionId));
        obsoData.setMtbf(mtbf);
        
        obsoData.setStrategyKept(avlBean.findAttributeValueListById(
                Strategy.class,
                strategyId));
        obsoData.setContinuityDate(continuityDate);
        
        obsoData.setManufacturerStatus(avlBean.findAttributeValueListById(
                ManufacturerStatus.class, manufacturerStatusId));
        obsoData.setAirbusStatus(avlBean.findAttributeValueListById(
                AirbusStatus.class, airbusStatusId));
        
        obsoData.setLastObsolescenceUpdate(lastObsolescenceUpdate);
        obsoData.setConsultPeriod(avlBean.findAttributeValueListById(
                ConsultPeriod.class, consultPeriodId));
        
        obsoData.setPersonInCharge(personInCharge);
        obsoData.setCommentOnStrategy(comments);
    }
    
    /**
     * Return a suggestions list depending on the provided suggest and
     * on the reference type chosen by the user
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string from which obtain the suggestions list
     * @return the sorted suggestions list
     */
    public List<String> suggestions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        if (StringUtil.isEmptyOrNull(pPrefix)) {
            pPrefix = "*";
        }
        
        List<String> suggestionList = new ArrayList<String>();
        List<Software> softResults = Collections.emptyList();
        List<PN> PNResults = Collections.emptyList();
        List<TypePC> lTypePCResults = Collections.emptyList();
        
        if (!StringUtil.isEmptyOrNull(pPrefix)) {
            
            switch (getReferenceTypeChosen()) {
            
            case SOFTWARE:
                softResults = softBean.suggestionListSoftware(pPrefix);
                for (Software s : softResults) {
                    suggestionList.add(s.getCompleteName());
                }
                break;
            
            case MANUFACTURERPN_TYPEARTICLE:
                PNResults = articleBean.suggestionListManufacturerPN(pPrefix);
                for (PN pn : PNResults) {
                    suggestionList.add(pn.getIdentifier());
                }
                break;
            
            case AIRBUSPN_TYPEARTICLE:
                PNResults = articleBean.suggestionListAirbusPN(pPrefix);
                for (PN pn : PNResults) {
                    suggestionList.add(pn.getIdentifier());
                }
                break;
            
            case TYPEPC:
                lTypePCResults = articleBean.suggestionListTypePC(pPrefix);
                for (TypePC lType : lTypePCResults) {
                    suggestionList.add(lType.getLabel());
                }
                break;
            
            default:
            
            }
        }
        
        Collections.sort(suggestionList);
        return suggestionList;
        
    }
    
    @Override
    protected void setItemId(Long pId) {
        obsoDataId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return obsoDataId;
    }
    
    public ObsolescenceData getObsoData() {
        return obsoData;
    }

    /**
     * @return the actionId
     */
    public Long getActionId() {
        return actionId;
    }
    
    /**
     * @param actionId
     *            the actionId to set
     */
    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }
    
    /**
     * @return the airbusStatusId
     */
    public Long getAirbusStatusId() {
        return airbusStatusId;
    }
    
    /**
     * @param airbusStatusId
     *            the airbusStatusId to set
     */
    public void setAirbusStatusId(Long airbusStatusId) {
        this.airbusStatusId = airbusStatusId;
    }
    
    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }
    
    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    /**
     * @return the constituantName
     */
    public String getConstituantName() {
        String lConstituantName = "";
        if (isCreateMode()) {
            try {
                ObsolescenceReference lObsoRef = retrieveReference();
                if (lObsoRef != null) {
                    lConstituantName = lObsoRef.getName();
                }
            } catch (Exception e) {
                log.warning(Constants.OBSO_CONSTITUANT_ERROR + ":"
                        + ExceptionUtil.getMessage(e));
            }
        }
        else {
            lConstituantName = obsoData.getConstituantName();
        }
        return lConstituantName;
    }
    
    /**
     * @return the consultPeriodId
     */
    public Long getConsultPeriodId() {
        return consultPeriodId;
    }
    
    /**
     * @param consultPeriodId
     *            the consultPeriodId to set
     */
    public void setConsultPeriodId(Long consultPeriodId) {
        this.consultPeriodId = consultPeriodId;
    }
    
    /**
     * @return the continuityDate
     */
    public Date getContinuityDate() {
        return continuityDate;
    }
    
    /**
     * @param continuityDate
     *            the continuityDate to set
     */
    public void setContinuityDate(Date continuityDate) {
        this.continuityDate = continuityDate;
    }
    
    /**
     * @return the endOfOrderDate
     */
    public Date getEndOfOrderDate() {
        return endOfOrderDate;
    }
    
    /**
     * @param endOfOrderDate
     *            the endOfOrderDate to set
     */
    public void setEndOfOrderDate(Date endOfOrderDate) {
        this.endOfOrderDate = endOfOrderDate;
    }
    
    /**
     * @return the endOfProductionDate
     */
    public Date getEndOfProductionDate() {
        return endOfProductionDate;
    }
    
    /**
     * @param endOfProductionDate
     *            the endOfProductionDate to set
     */
    public void setEndOfProductionDate(Date endOfProductionDate) {
        this.endOfProductionDate = endOfProductionDate;
    }
    
    /**
     * @return the endOfSupportDate
     */
    public Date getEndOfSupportDate() {
        return endOfSupportDate;
    }
    
    /**
     * @param endOfSupportDate
     *            the endOfSupportDate to set
     */
    public void setEndOfSupportDate(Date endOfSupportDate) {
        this.endOfSupportDate = endOfSupportDate;
    }
    
    /**
     * @return the lastObsolescenceUpdate
     */
    public Date getLastObsolescenceUpdate() {
        return lastObsolescenceUpdate;
    }
    
    /**
     * @param lastObsolescenceUpdate
     *            the lastObsolescenceUpdate to set
     */
    public void setLastObsolescenceUpdate(Date lastObsolescenceUpdate) {
        this.lastObsolescenceUpdate = lastObsolescenceUpdate;
    }
    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        
        String lManufacturer = "";
        if (isCreateMode()) {
            try {
                ObsolescenceReference lObsoRef = retrieveReference();
                if (lObsoRef != null) {
                    lManufacturer = lObsoRef.getManufacturer();
                }
            } catch (Exception e) {
                log.warning(Constants.OBSO_CONSTITUANT_ERROR + ":"
                        + ExceptionUtil.getMessage(e));
            }
        }
        else {
            lManufacturer = obsoData.getManufacturer();
        }
        return lManufacturer;
    }
    
    /**
     * @return the manufacturerStatusId
     */
    public Long getManufacturerStatusId() {
        return manufacturerStatusId;
    }
    
    /**
     * @param manufacturerStatusId
     *            the manufacturerStatusId to set
     */
    public void setManufacturerStatusId(Long manufacturerStatusId) {
        this.manufacturerStatusId = manufacturerStatusId;
    }
    
    /**
     * @return the mtbf
     */
    public Long getMtbf() {
        return mtbf;
    }
    
    /**
     * @param mtbf
     *            the mtbf to set
     */
    public void setMtbf(Long mtbf) {
        this.mtbf = mtbf;
    }
    
    /**
     * @return the obsolescenceDate
     */
    public Date getObsolescenceDate() {
        return obsolescenceDate;
    }
    
    /**
     * @param obsolescenceDate
     *            the obsolescenceDate to set
     */
    public void setObsolescenceDate(Date obsolescenceDate) {
        this.obsolescenceDate = obsolescenceDate;
    }
    
    /**
     * @return the optionsRadioReferenceType
     */
    public SelectItem[] getOptionsRadioReferenceType() {
        return optionsRadioReferenceType;
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
    public void setPersonInCharge(String personInCharge) {
        this.personInCharge = personInCharge;
    }
    
    /**
     * @return true if the Type Article reference is available
     *         (i.e. when the Type Article shall be associated to a PN)
     */
    public boolean isReferenceTypeArticleAvailable() {
        switch (referenceTypeChosen) {
        case AIRBUSPN_TYPEARTICLE:
        case MANUFACTURERPN_TYPEARTICLE:
            return true;
        case SOFTWARE:
        case TYPEPC:
        default:
            return false;
        }
    }
    
    /**
     * @return the referenceTypeChosen
     */
    public ReferenceType getReferenceTypeChosen() {
        return referenceTypeChosen;
    }
    
    /**
     * @param referenceTypeChosen
     *            the referenceTypeChosen to set
     */
    public void setReferenceTypeChosen(ReferenceType referenceTypeChosen) {
        this.referenceTypeChosen = referenceTypeChosen;
    }
    
    /**
     * @return the selectAction
     */
    public List<SelectItem> getSelectAction() {
        selectAction = avlBean.generateSelectItems(ActionObso.class);
        return selectAction;
    }
    
    /**
     * @return the selectAirbusStatus
     */
    public List<SelectItem> getSelectAirbusStatus() {
        selectAirbusStatus = avlBean.generateSelectItems(AirbusStatus.class);
        return selectAirbusStatus;
    }
    
    /**
     * @return the selectConsultPeriod
     */
    public List<SelectItem> getSelectConsultPeriod() {
        selectConsultPeriod = avlBean.generateSelectItems(ConsultPeriod.class);
        return selectConsultPeriod;
    }
    
    /**
     * @return the selectedReferenceName
     */
    public String getSelectedReferenceName() {
        return selectedReferenceName;
    }
    
    /**
     * @param selectedReferenceName
     *            the selectedReferenceName to set
     */
    public void setSelectedReferenceName(String selectedReferenceName) {
        this.selectedReferenceName = selectedReferenceName;
    }
    
    /**
     * @return the selectedTypeArticleId
     */
    public Long getSelectedTypeArticleId() {
        if (selectedTypeArticle != null) {
            return selectedTypeArticle.getId();
        }
        return null;
    }
    
    /**
     * @param pSelectedTypeArticleId
     *            the selectedTypeArticleId to set
     */
    public void setSelectedTypeArticleId(Long pSelectedTypeArticleId) {
        selectedTypeArticle =
                articleBean.findTypeArticleById(pSelectedTypeArticleId);
    }
    
    /**
     * @return the selectManufacturerStatus
     */
    public List<SelectItem> getSelectManufacturerStatus() {
        selectManufacturerStatus =
                avlBean.generateSelectItems(ManufacturerStatus.class);
        return selectManufacturerStatus;
    }
    
    /**
     * @return the selectStrategy
     */
    public List<SelectItem> getSelectStrategy() {
        selectStrategy = avlBean.generateSelectItems(Strategy.class);
        return selectStrategy;
    }
    
    /**
     * @return the selectTypeArticles
     */
    public List<SelectItem> getSelectTypeArticles() {
        
        List<SelectItem> lSelect = new ArrayList<SelectItem>();
        
        if (!StringUtil.isEmptyOrNull(selectedReferenceName)) {
            
            switch (referenceTypeChosen) {
            case AIRBUSPN_TYPEARTICLE:
                AirbusPN lAirbusPN =
                        articleBean.findAirbusPNByName(selectedReferenceName);
                if (lAirbusPN != null) {
                    List<TypeArticle> lAirbusPNTypeArticles =
                            articleBean.findAllTypeArticleForPN(lAirbusPN);
                    for (TypeArticle lTypeArticle : lAirbusPNTypeArticles) {
                        lSelect.add(new SelectItem(lTypeArticle.getId(),
                                lTypeArticle.getLabel()));
                    }
                }
                break;
            
            case MANUFACTURERPN_TYPEARTICLE:
                ManufacturerPN lManufacturerPN =
                        articleBean
                                .findManufacturerPNByName(selectedReferenceName);
                if (lManufacturerPN != null) {
                    List<TypeArticle> lManufacturerPNTypeArticles =
                            articleBean
                                    .findAllTypeArticleForPN(lManufacturerPN);
                    for (TypeArticle lTypeArticle : lManufacturerPNTypeArticles) {
                        lSelect.add(new SelectItem(lTypeArticle.getId(),
                                lTypeArticle.getLabel()));
                    }
                }
                break;
            
            case SOFTWARE:
            case TYPEPC:
            default:
                break;
            }
        }
        
        return lSelect;
    }
    
    /**
     * @return the strategyId
     */
    public Long getStrategyId() {
        return strategyId;
    }
    
    /**
     * @param strategyId
     *            the strategyId to set
     */
    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }
    
    /**
     * @return the supplier
     */
    public String getSupplier() {
        return supplier;
    }
    
    /**
     * @param supplier
     *            the supplier to set
     */
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createObsolescenceTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoObsolescenceTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateObsolescenceTitle");
    }
}
