/*
 * ------------------------------------------------------------------------
 * Class : TypeController
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.richfaces.model.Filter;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeBoard;
import com.airbus.boa.entity.article.type.TypeCabinet;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.article.type.TypeRack;
import com.airbus.boa.entity.article.type.TypeSwitch;
import com.airbus.boa.entity.article.type.TypeVarious;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.obso.ObsolescenceReference;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.filter.TypeFilterRegex;

/**
 * Controller managing the type and PN creation, consultation, modification and
 * deletion
 */
@ManagedBean(name = TypeController.BEAN_NAME)
@SessionScoped
public class TypeController extends AbstractController {
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "typeController";
    
    private static final Class<?>[] allType = { TypeCabinet.class,
            TypeRack.class, TypeBoard.class, TypeVarious.class, TypePC.class,
            TypeSwitch.class };
    
    /**
     * Apply the following rules issued from AM2701 and AM2702: <br>
     * - The AIRBUS PN must contain 15 characters, <br>
     * - The authorized characters are the numerals from "0" to "9", the
     * characters (letters) in upper case from "A" to "Y" except "O", "I", "Q"
     * and "Z", the dash "-" (minus).
     * 
     * @param pIdentifier
     *            the Airbus PN identifier to valid
     * @throws ValidatorException
     *             when the provided Airbus PN is not valid
     */
    public static void validatePNIdentifier(String pIdentifier)
            throws ValidatorException {
        
        if (!Pattern.matches("([0-9]|[A-Y&&[^OIQZ]]|-){15}", pIdentifier)) {
            String lMsg = MessageBundle
                    .getMessage(Constants.AIRBUS_PN_IDENTIFIER_INVALID);
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private DemandBean demandBean;
    
    private List<TypeArticle> types = Collections.emptyList();
    private TypeFilterRegex filterRegex = new TypeFilterRegex();
    
    private List<PN> allPNs = new ArrayList<PN>();
    
    private String selectedTypeTitle;
    
    private String newTypeName;
    private String newTypeManufacturer;
    
    private String newTypeClass;
    
    /**
     * gestion du partial Update richface pour le tableau des types
     */
    private Set<Integer> keys = new HashSet<Integer>();
    
    private int currentRow;
    
    private TypeArticle selectedType;
    private String identifier;
    private Boolean createObso = false;
    
    /*
     * label du type vers lequel l'airbusPN sera déplacé.
     */
    private String movedToTypeLabel;
    
    private String manufacturerPN;
    
    private PN selectedPN;
    private String updateIdentifier;
    
    /**
     * Default constructor
     */
    public TypeController() {
        super();
    }
    
    @PostConstruct
    public void init() {
        types = articleBean.findAllTypeArticle();
    }
    
    /**
     * Create the article type in database
     */
    public void doCreateType() {
        if (newTypeClass == null || newTypeName == null
                || newTypeName.trim().isEmpty()
                || newTypeClass.trim().isEmpty()) {
            String msg = "Creation impossible";
            log.warning(msg);
            Utils.addFacesMessage(NavigationConstants.ADD_NEW_TYPE_ERROR_ID,
                    msg);
            return;
        }
        TypeArticle typeArticle;
        try {
            Class<?> clazz = Class.forName(newTypeClass);
            typeArticle = (TypeArticle) clazz.newInstance();
            
        }
        catch (Exception e) { // IllegalAccessException et
                              // //InstanciationException
            String msg = "Creation impossible";
            log.warning(msg);
            Utils.addFacesMessage(NavigationConstants.ADD_NEW_TYPE_ERROR_ID,
                    msg);
            return;
        }
        
        try {
            typeArticle.setLabel(newTypeName);
            typeArticle.setManufacturer(newTypeManufacturer);
            typeArticle = articleBean.createTypeArticle(typeArticle);
            
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.ADD_NEW_TYPE_ERROR_ID,
                    "Creation impossible : " + ExceptionUtil.getMessage(e));
            return;
        }
        
        boolean lCreateOboso = createObso && isPCTypeSelected();
        
        types = articleBean.findAllTypeArticle();
        
        setNewTypeClass(null);
        setNewTypeName(null);
        setSelectedType(null);
        newTypeManufacturer = null;
        
        if (lCreateOboso) {
            ObsolescenceReference lObsoReference =
                    new ObsolescenceReference(typeArticle);
            ObsolescenceData lObsoData = new ObsolescenceData();
            lObsoData.setReference(lObsoReference);
            lObsoData = obsoBean.create(lObsoData);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", lObsoData.getId().toString());
            params.put("mode", "UPDATE");
            params.put("ret", "ITEM");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }
    
    /**
     * Create the Airbus PN into the database, and link it to its corresponding
     * type
     */
    public void doCreatePN() {
        
        try {
            AirbusPN airbusPN = new AirbusPN();
            airbusPN.setIdentifier(identifier);
            
            airbusPN = articleBean.createPN(airbusPN);
            
            selectedType.add(airbusPN);
            selectedType = articleBean.mergeTypeArticle(selectedType);
            
            int lIndex = types.indexOf(selectedType);
            types.set(lIndex, selectedType);
            allPNs = articleBean.findAllPNbyTypeArticleId(selectedType.getId());
            
            if (createObso) {
                ObsolescenceReference lObsoReference =
                        new ObsolescenceReference(airbusPN, selectedType);
                ObsolescenceData lObsoData = new ObsolescenceData();
                lObsoData.setReference(lObsoReference);
                lObsoData = obsoBean.create(lObsoData);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("id", lObsoData.getId().toString());
                params.put("mode", "UPDATE");
                params.put("ret", "ITEM");
                NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                        params);
            }
            
        } catch (ValidationException e) {
            Utils.addFacesMessage(NavigationConstants.CREATE_PN_ERROR_ID,
                    e.getMessage());
        }
    }
    
    /**
     * @return the delete PN confirmation message
     */
    public String getDeletePNConfirmationMessage() {
        return MessageBundle.getMessage("delete") + " "
                + ((selectedPN != null) ? selectedPN.getIdentifier() : "")
                + "?";
    }
    
    /**
     * Remove the selected PN from the database and all obsolescence data
     * referencing this PN
     */
    public void doDeletePN() {
        if (getSelectedPN() == null) {
            Utils.addWarningMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.PN_DELETION_NONE_SELECTED));
        }
        else if (isDeletionAllowed(getSelectedPN())) {
            List<ObsolescenceData> lObsoDataList =
                    obsoBean.findAllByReference(getSelectedPN());
            
            try {
                if (lObsoDataList != null && !lObsoDataList.isEmpty()) {
                    // remove all obsolescence data referencing the selected PN
                    for (ObsolescenceData lObsolescenceData : lObsoDataList) {
                        obsoBean.delete(lObsolescenceData);
                    }
                }
                
                articleBean.remove(getSelectedPN());
                
                selectedType =
                        articleBean.findTypeArticleById(selectedType.getId());
                int lIndex = types.indexOf(selectedType);
                types.set(lIndex, selectedType);
                allPNs = articleBean
                        .findAllPNbyTypeArticleId(selectedType.getId());
                
                Utils.addInfoMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                        MessageBundle.getMessage(Constants.DELETION_OK));

            }
            catch (Exception e) {
                Utils.addFacesMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }
    
    /**
     * @return the delete type confirmation message
     */
    public String getDeleteTypeConfirmationMessage() {
        return MessageBundle.getMessage("delete") + " "
                + ((selectedType != null) ? selectedType.getLabel() : "") + "?";
    }
    
    /**
     * Remove the selected type from the database
     */
    public void doDeleteType() {
        if (getSelectedType() == null) {
            Utils.addFacesMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                    MessageBundle
                    .getMessage(Constants.TYPEARTICLE_NOT_SELECTED));
        }
        else {
            
            // Number of articles still of this type
            Long countDefinedArticleWithType =
                    articleBean
                            .countArticleWithTypeArticleById(getSelectedType()
                                    .getId());
            
            // Number of linked Airbus PN
            if (getSelectedType().getListAirbusPN().size() > 0) {
                Utils.addFacesMessage(
                        NavigationConstants.TYPE_LIST_ERROR_ID,
                        MessageBundle
                                .getMessageResource(
                                        Constants.TYPEARTICLE_DELETION_ERROR_AIRBUSPNS_2,
                                        new Object[] {
                                                getSelectedType().getLabel(),
                                                getSelectedType()
                                                        .getListAirbusPN()
                                                        .size() }));
            }
            else if (countDefinedArticleWithType > 0) {
                
                String keyMessage =
                        (getSelectedType() instanceof TypePC) ? Constants.TYPEPC_DELETION_ERROR_ARTICLES_2
                                : Constants.TYPEARTICLE_DELETION_ERROR_ARTICLES_2;
                String label = getSelectedType().getLabel();
                String msg =
                        MessageBundle.getMessageResource(keyMessage,
                                new Object[] { label,
                                        countDefinedArticleWithType });
                Utils.addFacesMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                        msg);
            }
            
            // Check that the type is not linked to a demand
            else if ((getSelectedType() instanceof TypePC)
                    && !demandBean.findDemandsUsingType(
                            (TypePC) getSelectedType()).isEmpty()) {
                Utils.addFacesMessage(
                        NavigationConstants.TYPE_LIST_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.TYPEARTICLE_DELETION_USEDBY_DEMAND));
            }
            else {
                
                // Article or PC type may be linked to obsolescence references:
                // in this case, delete them
                if (getSelectedType() instanceof TypePC) {
                    ObsolescenceData lObsoData =
                            obsoBean.findByReference((TypePC) getSelectedType());
                    if (lObsoData != null) {
                        obsoBean.delete(lObsoData);
                        obsoBean.flush();
                    }
                }
                else {
                    List<ObsolescenceData> lObsoDataList =
                            obsoBean.findAllByReference(getSelectedType());
                    if (lObsoDataList != null && !lObsoDataList.isEmpty()) {
                        for (ObsolescenceData lObsoData : lObsoDataList) {
                            obsoBean.delete(lObsoData);
                        }
                        obsoBean.flush();
                    }
                }
                
                articleBean.remove(getSelectedType());
                
                setSelectedType(null);
                types = articleBean.findAllTypeArticle();
                getAllPNs().clear();
                setSelectedTypeTitle("");
            }
        }
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        filterRegex.resetFilters();
        selectedType = null;
        selectedPN = null;
        selectedTypeTitle = null;
        allPNs = new ArrayList<PN>();
    }
    
    /**
     * Update the attributes with the selected type
     */
    public void doSelectType(Long selectedTypeId) {
        if (selectedTypeId != null) {
            
            TypeArticle myType =
                    articleBean.findTypeArticleById(selectedTypeId);
            setSelectedType(myType);
            allPNs = articleBean.findAllPNbyTypeArticleId(selectedTypeId);
            
            selectedTypeTitle = (myType != null) ? myType.getLabel() : "";
        }
    }
    
    /**
     * Update the selected PN in database
     */
    public void doUpdatePN() {
        
        if (getSelectedPN() != null) {
            if (StringUtil.isEmptyOrNull(getMovedToTypeLabel())) {
                String msg =
                        MessageBundle
                                .getMessage(Constants.TYPEARTICLE_DESTINATION_INVALID);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, msg, msg));
            }
            
            try {
                // une methode
                validateUpdateIdentifier(null, null, updateIdentifier);
                
                // recuperation du type d'article s'il est modifié
                if (getSelectedPN() instanceof AirbusPN) {
                    
                    // NOTA BENE : le type d'article devrait être aussi
                    // normalement
                    // getSelectedType()
                    TypeArticle fromTypeArticle =
                            articleBean.findTypeArticleForPN(getSelectedPN());
                    String fromTypeLabel = null;
                    Long fromTypeArticleId = null;
                    if (fromTypeArticle != null
                            && fromTypeArticle.getLabel() != null) {
                        
                        fromTypeLabel = fromTypeArticle.getLabel();
                        fromTypeArticleId = fromTypeArticle.getId();
                    }
                    
                    if (!(movedToTypeLabel.equals(fromTypeLabel))) {
                        // le pn doit être déplacé.
                        TypeArticle destTypeArticle =
                                articleBean
                                        .findTypeArticleByName(movedToTypeLabel);
                        
                        if (destTypeArticle == null) {
                            String msg =
                                    MessageBundle
                                            .getMessage(Constants.TYPEARTICLE_DESTINATION_INVALID);
                            throw new ValidatorException(new FacesMessage(
                                    FacesMessage.SEVERITY_ERROR, msg, msg));
                        }
                        else {
                            
                            Long destTypeArticleId = destTypeArticle.getId();
                            articleBean.merge(getSelectedPN(),
                                    updateIdentifier, fromTypeArticleId,
                                    destTypeArticleId);
                            
                            int lIndex = types.indexOf(fromTypeArticle);
                            fromTypeArticle = articleBean.findTypeArticleById(
                                    fromTypeArticle.getId());
                            types.set(lIndex, fromTypeArticle);
                            lIndex = types.indexOf(destTypeArticle);
                            destTypeArticle = articleBean.findTypeArticleById(
                                    destTypeArticle.getId());
                            types.set(lIndex, destTypeArticle);
                            
                        }

                    }
                    else { // le PN n'a pas été déplacé, on modifie son
                           // identifiant si nécessaire
                        getSelectedPN().setIdentifier(updateIdentifier);
                        articleBean.merge(getSelectedPN());
                    }
                    
                }
                else { // ManufacturerPN
                    getSelectedPN().setIdentifier(updateIdentifier);
                    articleBean.merge(getSelectedPN());
                }
                
                allPNs = articleBean
                        .findAllPNbyTypeArticleId(selectedType.getId());
                // partial update
                getKeys().clear();
                getKeys().add(getCurrentRow());

            } catch (ValidatorException e) {
                Utils.addFacesMessage(NavigationConstants.UPDATE_PN_ERROR_ID,
                        e.getMessage());
            } catch (Exception e) {
                String msg = ExceptionUtil.getMessage(e);
                Utils.addFacesMessage(NavigationConstants.UPDATE_PN_ERROR_ID,
                        String.valueOf(e) + ":" + msg);
                log.warning(msg);
            }
        }
    }
    
    /**
     * Update the selected type in database
     */
    public void doUpdateType() {
        if (selectedType != null) {
            try {
                
                TypeArticle typeArticle = getSelectedType();
                if (newTypeName == null || newTypeName.isEmpty()) {
                    Utils.addFacesMessage(
                            NavigationConstants.UPDATE_TYPE_ERROR_ID,
                            MessageBundle
                                    .getMessage(Constants.TYPELABEL_INVALID));
                }
                else if (!newTypeName.equals(typeArticle.getLabel())
                        || (newTypeManufacturer == null && typeArticle
                                .getManufacturer() != null)
                        || (newTypeManufacturer != null && !newTypeManufacturer
                                .equals(typeArticle.getManufacturer()))) {
                    
                    Integer index = types.indexOf(typeArticle);
                    typeArticle = types.get(index);
                    
                    getKeys().clear();
                    
                    typeArticle.setLabel(newTypeName);
                    typeArticle.setManufacturer(newTypeManufacturer);
                    typeArticle = articleBean.mergeTypeArticle(typeArticle);
                    
                    types.set(index, typeArticle);
                    
                    if (filterRegex.filterMethodRegex(typeArticle)) {
                        getKeys().add(getCurrentRow());
                    }
                    else {
                        types = articleBean.findAllTypeArticle();
                    }
                    
                    setSelectedTypeTitle(typeArticle.getLabel());
                    setNewTypeName(null);
                    
                }
            }
            catch (Exception e) {
                Utils.addFacesMessage(NavigationConstants.UPDATE_TYPE_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }

    /**
     * Return the filter for article types
     * 
     * @return the requested filter object
     */
    public Filter<?> getTypeArticleFilter() {
        return new Filter<TypeArticle>() {
            
            public boolean accept(TypeArticle item) {
                return filterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * Return a suggestions list depending on the provided value
     * 
     * @param value
     *            the string from which obtain the suggestions list
     * @return the suggestions list
     */
    public List<TypeArticle> suggestionTypeArticle(String suggestion) {
        return articleBean.suggestionListTypeArticle(suggestion);
    }
    
    /**
     * Check if the provided PN exists
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the PN to valid
     * @throws ValidatorException
     *             when the PN does not exist
     */
    public void validateExistPN(FacesContext context, UIComponent component,
            Object value)
            throws ValidatorException {
        
        String partNumber = (String) value;
        PN pn = articleBean.findManufacturerPNByName(partNumber);
        setSelectedPN(pn);
        
        if (partNumber == null || partNumber.trim().equals("") || pn == null) {
            
            setSelectedPN(null);
            String msg = MessageBundle.getMessage(Constants.PN_UNKNOWN);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }
    
    /**
     * Check if the provided name value is correct
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the name to valid
     * @throws ValidatorException
     *             when the name is not valid
     */
    public void validateNewTypeName(FacesContext context,
            UIComponent component, Object value)
            throws ValidatorException {
        
        String valeur = (String) value;
        valeur = (valeur == null) ? "" : valeur.trim();
        if ("".equals(valeur)) {
            String msg = MessageBundle.getMessage(Constants.TYPELABEL_INVALID);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
        
        // la validation ne doit pas si le nom est celui de l'article
        // actuellement selectionné
        TypeArticle typeArticle = getSelectedType();
        if (typeArticle != null && typeArticle.getLabel().equals(valeur)) {
            return;
        }
        
        if (articleBean.existTypeArticleByName(valeur)) {
            String msg =
                    MessageBundle.getMessage(Constants.TYPELABEL_ALREADY_USED);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }
    
    /**
     * Check if the provided PN, if updated, does not already exist. <br>
     * If the updated PN is an Airbus PN, apply the creation validation.
     * 
     * @param context
     *            <i>mandatory for JSF validator</i>
     * @param component
     *            <i>mandatory for JSF validator</i>
     * @param value
     *            the PN to valid
     * @throws ValidatorException
     *             when the provided PN is not valid
     */
    public void validateUpdateIdentifier(FacesContext context,
            UIComponent component, Object value)
            throws ValidatorException {
        
        String lPN = (String) value;
        lPN = (lPN == null) ? "" : lPN.trim();
        if (!PN.isValidIdentifier(lPN)) {
            String msg =
                    MessageBundle.getMessage(Constants.PN_IDENTIFIER_INVALID);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
        
        // If the updated PN has is equal to the current PN, no validation is to
        // be applied
        if (getSelectedPN() != null
                && getSelectedPN().getIdentifier().equals(lPN)) {
            return;
        }
        
        if (getSelectedPN() instanceof ManufacturerPN
                && articleBean.findManufacturerPNByName(lPN) != null) {
            String lMsg = MessageBundle.getMessage(Constants.PN_ALREADY_USED);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        else if (getSelectedPN() instanceof AirbusPN) {
            
            if (articleBean.findAirbusPNByName(lPN) != null) {
                String lMsg =
                        MessageBundle.getMessage(Constants.PN_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
            
            validatePNIdentifier(lPN);
        }
    }
    
    /**
     * Check if the provided Airbus PN does not already exist. <br>
     * Apply the rules issued from AM2701 and AM2702.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the AirbusPN to valid
     * @throws ValidatorException
     *             when the provided Airbus PN is not valid
     */
    public void validatePNIdentifier(FacesContext pContext,
            UIComponent pComponent, Object pValue) throws ValidatorException {
        
        String lIdentifier = (String) pValue;
        lIdentifier = lIdentifier.trim();
        AirbusPN lAirbusPN = articleBean.findAirbusPNByName(lIdentifier);
        
        if (lAirbusPN != null) {
            String lMsg = MessageBundle.getMessage(Constants.PN_ALREADY_USED);
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        validatePNIdentifier(lIdentifier);
    }
    
    /**
     * @return the allPNs
     */
    public List<PN> getAllPNs() {
        return allPNs;
    }
    
    /**
     * Update the countFiltered with the number of types filtered
     * 
     * @return the countFiltered
     */
    public Integer getCountFiltered() {
        return filterRegex.countFiltered(types);
    }
    
    /**
     * @return the currentRow
     */
    public int getCurrentRow() {
        return currentRow;
    }
    
    /**
     * @param currentRow
     *            the currentRow to set
     */
    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
    
    /**
     * Check if PN deletion is available
     * 
     * @param pn
     *            the PN to delete
     * @return true if PN deletion is available, else false
     */
    private boolean isDeletionAllowed(PN pn) {
        
        List<Article> articles = articleBean.findAllArticleWithPN(pn);
        
        if (articles.size() == 0) {
            return true;
        }
        
        StringBuffer sbMsg =
                new StringBuffer(MessageBundle.getMessageResource(
                        Constants.PN_DELETION_FORBIDDEN_1,
                        new Object[] { pn.getIdentifier() }));
        sbMsg.append("\n");
        if (articles.size() > 3) {
            sbMsg.append(MessageBundle
                    .getMessage(Constants.PN_DELETION_DEFINED_FOR_MANY_ARTICLES));
        }
        else {
            sbMsg.append(MessageBundle
                    .getMessage(Constants.PN_DELETION_DEFINED_FOR_ARTICLE));
            for (Article article : articles) {
                sbMsg.append(article.toString()).append('\n');
            }
        }
        
        Utils.addFacesMessage(NavigationConstants.TYPE_LIST_ERROR_ID,
                sbMsg.toString());
        
        return false;
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return filterRegex.getFilterValues();
    }
    
    /**
     * @param filterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> filterValues) {
        filterRegex.setFilterValues(filterValues);
    }
    
    /**
     * @return the keys
     */
    public Set<Integer> getKeys() {
        return keys;
    }
    
    /**
     * @param keys
     *            the keys to set
     */
    public void setKeys(Set<Integer> keys) {
        this.keys = keys;
    }
    
    /**
     * @return the listTypeFamily
     */
    public List<SelectItem> getListTypeFamily() {
        List<SelectItem> lListTypeFamily = new ArrayList<SelectItem>();
        for (Class<?> itemValue : allType) {
            lListTypeFamily.add(new SelectItem(itemValue.getName(),
                    MessageBundle.getMessage(itemValue.getSimpleName())));
        }
        return lListTypeFamily;
    }
    
    /**
     * @return true if the selected type is PC, false otherwise
     */
    public boolean isPCTypeSelected() {
        if (newTypeClass == null) {
            return false;
        }
        return newTypeClass.equals(TypePC.class.getName());
    }
    
    /**
     * @return the manufacturerPN
     */
    public String getManufacturerPN() {
        return manufacturerPN;
    }
    
    /**
     * @param manufacturerPN
     *            the manufacturerPN to set
     */
    public void setManufacturerPN(String manufacturerPN) {
        this.manufacturerPN = manufacturerPN;
    }
    
    /**
     * @return the movedToTypeLabel
     */
    public String getMovedToTypeLabel() {
        return movedToTypeLabel;
    }
    
    /**
     * @param movedToTypeLabel
     *            the movedToTypeLabel to set
     */
    public void setMovedToTypeLabel(String movedToTypeLabel) {
        this.movedToTypeLabel = movedToTypeLabel;
    }
    
    /**
     * @return the newTypeClass
     */
    public String getNewTypeClass() {
        return newTypeClass;
    }
    
    /**
     * @param newTypeClass
     *            the newTypeClass to set
     */
    public void setNewTypeClass(String newTypeClass) {
        this.newTypeClass = newTypeClass;
    }
    
    /**
     * @return the newTypeManufacturer
     */
    public String getNewTypeManufacturer() {
        return newTypeManufacturer;
    }
    
    /**
     * @param pNewTypeManufacturer
     *            the newTypeManufacturer to set
     */
    public void setNewTypeManufacturer(String pNewTypeManufacturer) {
        newTypeManufacturer = pNewTypeManufacturer;
    }
    
    /**
     * @return the newTypeName
     */
    public String getNewTypeName() {
        return newTypeName;
    }
    
    /**
     * @param newTypeName
     *            the newTypeName
     */
    public void setNewTypeName(String newTypeName) {
        this.newTypeName = newTypeName;
    }
    
    /**
     * @return true if the selected PN has obsolescence data, else false
     */
    public Boolean hasObsoData(PN pPN) {
        if (pPN != null) {
            List<ObsolescenceData> lObsoDataList =
                    obsoBean.findAllByReference(pPN);
            if (lObsoDataList != null && !lObsoDataList.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if the selected PN has obsolescence data, else false
     */
    public Boolean pcHasObsoData(TypeArticle pType) {
        if (pType != null && pType instanceof TypePC) {
            ObsolescenceData lObsoData =
                    obsoBean.findByReference((TypePC) pType);
            if (lObsoData != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Build the message displayed for user confirmation for deletion.
     * 
     * @param reference
     *            the reference for which the confirmation message is to be
     *            built
     * @return the confirmation message
     */
    public String getObsolescenceDataConfirmationToDelete(Object reference) {
        
        StringBuffer sb = new StringBuffer(MessageBundle.getMessage("delete"));
        
        List<ObsolescenceData> lObsoDataList = null;
        
        if (reference instanceof PN) {
            lObsoDataList = obsoBean.findAllByReference((PN) reference);
        }
        else if (reference instanceof TypeArticle) {
            lObsoDataList =
                    obsoBean.findAllByReference((TypeArticle) reference);
        }
        if (lObsoDataList != null) {
            for (ObsolescenceData lObsoData : lObsoDataList) {
                sb.append(".<br />");
                sb.append(MessageBundle
                        .getMessageResource(
                                Constants.OBSO_CONFIRMATION_DELETION_FOR_PN_TYPE_SOFTWARE,
                                new Object[] {
                                        lObsoData.getConstituantName() }));
            }
        }
        return sb.toString();
    }
    
    /**
     * @return the selectedPN
     */
    public PN getSelectedPN() {
        return selectedPN;
    }
    
    /**
     * @param selectedPN
     *            the selectedPN to set
     */
    public void setSelectedPN(PN selectedPN) {
        this.selectedPN = selectedPN;
    }
    
    /**
     * @return the selectedType
     */
    public TypeArticle getSelectedType() {
        return selectedType;
    }
    
    /**
     * @param selectedType
     *            the selectedType to set
     */
    public void setSelectedType(TypeArticle selectedType) {
        this.selectedType = selectedType;
    }
    
    /**
     * @return the selectedTypeTitle
     */
    public String getSelectedTypeTitle() {
        return selectedTypeTitle;
    }
    
    /**
     * @param selectedTypeTitle
     *            the selectedTypeTitle to set
     */
    public void setSelectedTypeTitle(String selectedTypeTitle) {
        this.selectedTypeTitle = selectedTypeTitle;
    }
    
    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the createObso
     */
    public Boolean getCreateObso() {
        return createObso;
    }
    
    /**
     * @param createObso
     *            the createObso to set
     */
    public void setCreateObso(Boolean pCreateObso) {
        createObso = pCreateObso;
    }
    
    /**
     * @return the types
     */
    public List<TypeArticle> getTypes() {
        return types;
    }
    
    /**
     * @return the updateIdentifier
     */
    public String getUpdateIdentifier() {
        return updateIdentifier;
    }
    
    /**
     * @param updateIdentifier
     *            the updateIdentifier to set
     */
    public void setUpdateIdentifier(String updateIdentifier) {
        this.updateIdentifier = updateIdentifier;
    }
    
    /**
     * Show the obsolescence data for the given TypeArticle
     * 
     * @param pType
     *            TypeArticle for which the obsolescence data shall be displayed
     */
    public void showPCObsoData(TypeArticle pType) {
        if (pType != null && pType instanceof TypePC) {
            ObsolescenceData obso =
                    obsoBean.findByReference((TypePC) pType);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", obso.getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }

    /**
     * Show the obsolescence data for the given PN
     * 
     * @param pPN
     *            PN for which the obsolescence data shall be displayed
     */
    public void showPNObsoData(PN pPN) {
        if (pPN != null) {
            ObsolescenceData obso = null;
            if (pPN instanceof AirbusPN) {
                obso = obsoBean.findByReference((AirbusPN) pPN,
                        selectedType);
            }
            else if (pPN instanceof ManufacturerPN) {
                obso = obsoBean.findByReference((ManufacturerPN) pPN,
                        selectedType);
            }
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", obso.getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }
    
}
