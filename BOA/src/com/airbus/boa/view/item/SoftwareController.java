/*
 * ------------------------------------------------------------------------
 * Class : SoftwareController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.richfaces.model.Filter;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.SearchBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.ComparatorNameArticle;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.SearchSoftwareController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.filter.ArticleFilterRegex;

/**
 * Software controller managing creation, deletion, consultation and update
 */
@ManagedBean(name = SoftwareController.BEAN_NAME)
@ViewScoped
public class SoftwareController extends AbstractItemController implements
        Serializable {
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "softwareController";
    
    /**
     * Check if the provided distribution value is correct.<br>
     * When software is an operating system:
     * If logged user has enough rights, distribution value is free. Else it
     * must already exist.
     * When software is not an operating system, distribution (version) value is
     * free.
     * 
     * @param pDistribution
     *            the distribution to valid
     * @param pName
     *            the name
     * @param pOperatingSystem
     *            the operating system
     * @param pSoftwareBean
     *            the softwareBean to use
     * @throws ValidatorException
     *             when the distribution is not valid
     */
    public static void validateDistribution(String pDistribution, String pName,
            Boolean pOperatingSystem, SoftwareBean pSoftwareBean)
            throws ValidatorException {
        
        LogInController lLogInController = findBean(LogInController.class);
        
        boolean lOperatingSystem;
        if (pOperatingSystem != null) {
            lOperatingSystem = pOperatingSystem;
        }
        else {
            lOperatingSystem = false;
        }
        
        if (lOperatingSystem
                && !lLogInController
                        .isAuthorized(RightMaskAction.SoftwareCreateDistribution)) {
            
            List<String> lDistributions;
            if (pName != null) {
                lDistributions =
                        pSoftwareBean.findAllDistributions(lOperatingSystem,
                                pName);
            }
            else {
                lDistributions = new ArrayList<String>();
            }
            
            if (!lDistributions.contains(pDistribution)) {
                String lMsg =
                        MessageBundle
                                .getMessage(MessageConstants.SOFTWARE_DISTRIBUTION_NOT_EXISTING);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    /**
     * Check if the provided name value is correct.<br>
     * If logged user has enough rights, name value is free. Else it must
     * already exist.
     * 
     * @param pName
     *            the name to valid
     * @param pOperatingSystem
     *            the operating system
     * @param pSoftwareBean
     *            the softwareBean to use
     * @throws ValidatorException
     *             when the name is not valid
     */
    public static void validateName(String pName, Boolean pOperatingSystem,
            SoftwareBean pSoftwareBean) throws ValidatorException {
        
        LogInController lLogInController = findBean(LogInController.class);
        
        if (!lLogInController.isAuthorized(RightMaskAction.SoftwareCreateName)) {
            
            List<String> lNames;
            if (pOperatingSystem != null) {
                lNames = pSoftwareBean.findAllNames(pOperatingSystem);
            }
            else {
                lNames = pSoftwareBean.findAllNames();
            }
            
            if (!lNames.contains(pName)) {
                String lMsg =
                        MessageBundle
                                .getMessage(MessageConstants.SOFTWARE_NAME_NOT_EXISTING);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
        }
    }
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private SearchBean searchBean;
    
    @EJB
    private DemandBean demandBean;
    
    private ArticleFilterRegex equipmentFilterRegex = new ArticleFilterRegex();
    
    private String name;
    
    private String distribution;
    
    private String kernel;
    
    private Boolean operatingSystem;
    
    private String manufacturer;
    
    private String description;
    
    private String licence;
    
    private DataModel<Article> articleList = new ListDataModel<Article>();
    private List<Article> equipments = new ArrayList<Article>();
    
    private Software software;
    private Long softwareId;
    
    private Long selectedEquipmentId;
    private Article selectedEquipment;
    
    private Integer countFilteredEquipment;
    
    // MANAGEMENT OF SEARCH OF EQUIPMENTS TO ADD
    
    private String sn;
    private String apn;
    private String mpn;
    private String family;
    private String type;
    
    private Map<String, Object> findCriteria = new HashMap<String, Object>();
    
    private List<Article> equipmentsFound = new ArrayList<Article>();
    private List<Article> equipmentsSelected = new ArrayList<Article>();
    
    private Set<Integer> rowsToUpdate = new HashSet<Integer>();
    
    private Integer currentRow;
    
    public SoftwareController() {
        itemPage = NavigationConstants.SOFTWARE_MANAGEMENT_PAGE;
        listPage = NavigationConstants.SOFTWARE_LIST_PAGE;
        resultPage = NavigationConstants.SOFTWARE_SEARCH_RESULT_PAGE;
        errorId = NavigationConstants.SOFT_MGMT_ERROR_ID;
    }

    @Override
    protected void createItem() throws Exception {
        
        String who = getUserConnectedLogin();
        
        Software lSoftware = new Software();
        
        updateItemFromAttributes(lSoftware);
        
        if (softwareBean.exist(name, distribution, kernel)
                || softwareBean.findCountByCompleteName(lSoftware
                        .getCompleteName()) > 0) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_ALREADY_EXISTED));
        }
        
        softwareBean.create(lSoftware, equipments, who);
        softwareId = lSoftware.getId();
    }
    
    /**
     * Remove the software product from the database
     */
    @Override
    protected void deleteItem() {
        // Check that no demand references the selected Software
        if (!demandBean.findDemandsUsingSoftware(software).isEmpty()) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_DELETION_USED_BY_DEMAND));
        }
        
        if (hasObsoData()) {
            ObsolescenceData obso = obsoBean.findByReference(software);
            obsoBean.delete(obso);
        }
        softwareBean.remove(software, getUserConnectedLogin());
        
        SearchSoftwareController controller =
                findBean(SearchSoftwareController.class);
        controller.updateSearchResults(null);
    }

    /**
     * Search the list of equipments corresponding to the entered criteria
     */
    public void doFind() {
        
        List<Article> results = new ArrayList<Article>();
        
        removeEmptyParameter(findCriteria);
        
        // search is performed only if fields are not empty
        if (findCriteria.isEmpty()) {
            equipmentsFound.clear();
            String msg =
                    MessageBundle
                            .getMessage(Constants.QUERY_CRITERIA_MUST_BE_ENTERED);
            Utils.addFacesMessage(NavigationConstants.CHOOSE_EQUIPMENT_ERROR_ID,
                    msg);
            return;
        }
        
        if (findCriteria.containsKey("family")) {
            
            if (findCriteria.keySet().size() > 1) {
                results.addAll(searchBean.searchArticle(findCriteria));
            }
            else {
                String msg =
                        MessageBundle
                                .getMessage(Constants.QUERY_CRITERIA_MUST_BE_ENTERED);
                Utils.addWarningMessage(
                        NavigationConstants.CHOOSE_EQUIPMENT_ERROR_ID,
                        msg);
                return;
            }
            
        }
        else {
            // family is not defined here
            results = searchBean.searchArticle(findCriteria);
        }
        
        results = removeForbiddenClassForEquipment(results);
        
        // Elements have to be sorted by name.
        // Since the name is computed field for Article, use of sort method.
        Collections.sort(results, new ComparatorNameArticle());
        
        int maxLimit = getQUERY_MAX_RESULTS();
        int quantity = results.size();
        
        if ((results.size() > maxLimit) && (maxLimit > 0)) {
            setEquipmentsFound(results.subList(0, maxLimit));
            
            String msg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_MAX_MODAL_EXCEEDED, new Object[] {
                                    quantity, maxLimit });
            Utils.addWarningMessage(
                    NavigationConstants.CHOOSE_EQUIPMENT_ERROR_ID, msg);
        }
        else {
            setEquipmentsFound(results);
        }
    }
    
    /**
     * Add all selected equipments to the equipments list
     */
    public void doModalChoose() {
        
        if (equipmentsSelected != null && !equipmentsSelected.isEmpty()) {
            for (Article equipment : equipmentsSelected) {
                if (!getEquipments().contains(equipment)) {
                    getEquipments().add(equipment);
                }
            }
        }
    }
    
    @Override
    protected void initAttributesFromItem() {
        
        if (software != null) {
            name = software.getName();
            distribution = software.getDistribution();
            kernel = software.getKernel();
            operatingSystem = software.getOperatingSystem();
            description = software.getDescription();
            licence = software.getLicence();
            manufacturer = software.getManufacturer();
            if (software.getEquipments().isEmpty()) {
                
                equipments = new ArrayList<Article>();
            }
            else {
                equipments = new ArrayList<Article>(software.getEquipments());
            }
            
            // Reset of the search modal fields
            resetFindField();
            
            selectedEquipment = null;
            selectedEquipmentId = null;
            rowsToUpdate.clear();
            countFilteredEquipment = 0;
            equipmentFilterRegex.resetFilters();
            
            articleList.setWrappedData(getEquipments());
        }
    }
    
    /**
     * Prepare the software installation modal
     */
    public void doPrepareInstall() {
        name = null;
        distribution = null;
        kernel = null;
    }
    
    /**
     * Remove the selected equipment from the equipments list
     */
    public void doRemoveEquipment() {
        equipments.remove(selectedEquipment);
    }
    
    @Override
    protected void initItemWithNew() {
        
        software = new Software();
    }
    
    /**
     * Reset the filters
     */
    public void doResetFilters() {
        equipmentFilterRegex.resetFilters();
    }
    
    @Override
    public void updateItem() throws Exception {
        
        String login = getUserConnectedLogin();
        
        // check that if the name has been modified, it does not already
        // exist in database
        if (!(software.getName().equals(name))
                || !(software.getDistribution().equals(distribution))
                || !(software.getKernel().equals(kernel))) {
            
            if (softwareBean.exist(name, distribution, kernel)
                    || softwareBean.findCountByCompleteName(Software
                            .getCompleteName(name, distribution, kernel)) > 0) {
                throw new ValidationException(
                        MessageBundle
                                .getMessage(MessageConstants.SOFTWARE_ALREADY_EXISTED));
            }
        }
        
        updateItemFromAttributes(software);
        
        softwareBean.merge(software, getEquipments(), login);
    }
    
    /**
     * Navigate to the obsolescence data consultation page if they are found
     */
    public void doViewObsolescence() {
        if (software != null) {
            ObsolescenceData obso = obsoBean.findByReference(software);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", obso.getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }
    
    /**
     * Return the filter for equipments
     * 
     * @return the requested filter object
     */    
    public Filter<?> getEquipmentFilter() {
        return new Filter<Article>() {
            public boolean accept(Article item) {
            	return equipmentFilterRegex.filterMethodRegex(item);
            }
        };
    }
    
    @Override
    protected void initItemFromDatabase() {
        
        software = softwareBean.findById(softwareId);
    }
    
    /**
     * Update the operating system with the new value
     * 
     * @param pEvent
     *            the event sent while the operating system value is changed
     */
    public void operatingSystemChanged(ValueChangeEvent pEvent) {
        
        if (pEvent != null) {
            Boolean lOperatingSystem = (Boolean) pEvent.getNewValue();
            
            if (lOperatingSystem != null) {
                operatingSystem = lOperatingSystem;
                
                if (!operatingSystem) {
                    kernel = "";
                }
            }
        }
    }
    
    private void removeEmptyParameter(Map<String, Object> mapParameter) {
        for (String key : new ArrayList<String>(mapParameter.keySet())) {
            Object object = mapParameter.get(key);
            if (object == null
                    || (object instanceof String && StringUtil
                            .isEmptyOrNull((String) object))) {
                mapParameter.remove(key);
            }
        }
    }
    
    private List<Article>
            removeForbiddenClassForEquipment(List<Article> results) {
        List<Article> authorizedEquipments = new ArrayList<Article>(results);
        for (Article article : authorizedEquipments) {
            if (!isEquipmentDeployableOnSoftware(article)) {
                results.remove(article);
            }
        }
        return results;
    }
    
    /**
     * Reset the attributes allowing to search for equipments to add
     */
    public void resetFindField() {
        findCriteria.clear();
        equipmentsSelected.clear();
        equipmentsFound.clear();
    }
    
    /**
     * Check if the entered distribution value is correct.<br>
     * When software is an operating system:
     * If logged user has enough rights, distribution value is free. Else it
     * must already exist.
     * When software is not an operating system, distribution (version) value is
     * free.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the distribution to valid
     * @throws ValidatorException
     *             when the distribution is not valid
     */
    public void validateDistribution(FacesContext pContext,
            UIComponent pComponent, Object pValue)
            throws ValidatorException {
        
        validateDistribution((String) pValue, name, operatingSystem,
                softwareBean);
    };
    
    /**
     * Check if the entered name value is correct.<br>
     * If logged user has enough rights, name value is free. Else it must
     * already exist.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the name to valid
     * @throws ValidatorException
     *             when the name is not valid
     */
    public void validateName(FacesContext pContext, UIComponent pComponent,
            Object pValue)
            throws ValidatorException {
        
        validateName((String) pValue, operatingSystem, softwareBean);
    }
    
    private void updateItemFromAttributes(Software pSoftware)
            throws ValidationException {
        
        if (StringUtil.isEmptyOrNull(name)
                || StringUtil.isEmptyOrNull(distribution)) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY));
        }
        if (pSoftware != null) {
            
            pSoftware.setName(name);
            pSoftware.setDistribution(distribution);
            pSoftware.setKernel(kernel);
            pSoftware.setOperatingSystem(operatingSystem);
            pSoftware.setManufacturer(manufacturer);
            pSoftware.setDescription(description);
            pSoftware.setLicence(licence);
        }
    }
    
    /**
     * @return an array of select items corresponding to the families
     */
    public SelectItem[] getAllFamilies() {
        
        SearchSoftwareController controller =
                findBean(SearchSoftwareController.class);
        return controller.getAllFamilies();
    }
    
    /**
     * @return the apn
     */
    public String getApn() {
        return apn;
    }
    
    /**
     * @param apn
     *            the apn to set
     */
    public void setApn(String apn) {
        this.apn = apn;
    }
    
    /**
     * @return the articleList
     */
    public DataModel<Article> getArticleList() {
        return articleList;
    }
    
    /**
     * @param articleList
     *            the articleList to set
     */
    public void setArticleList(DataModel<Article> articleList) {
        this.articleList = articleList;
    }
    
    /**
     * @return the countFilteredEquipment
     */
    public Integer getCountFilteredEquipment() {
        countFilteredEquipment = 0;
        
        countFilteredEquipment =
                equipmentFilterRegex.countFiltered(getEquipments());
        return countFilteredEquipment;
    }
    
    /**
     * @return the currentRow
     */
    public Integer getCurrentRow() {
        return currentRow;
    }
    
    /**
     * @param currentRow
     *            the currentRow to set
     */
    public void setCurrentRow(Integer currentRow) {
        this.currentRow = currentRow;
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
     * @return the distribution
     */
    public String getDistribution() {
        return distribution;
    }
    
    /**
     * @param pDistribution
     *            the distribution to set
     */
    public void setDistribution(String pDistribution) {
        distribution = pDistribution;
    }
    
    private boolean isEquipmentDeployableOnSoftware(Article article) {
        boolean authorized = false;
        if (article == null) {
            return false;
        }
        for (Class<?> clazz : Software.DEPLOYABLE_EQUIPMENT_CLASS) {
            
            if (article.getClass().equals(clazz)) {
                return true;
            }
        }
        return authorized;
    }
    
    /**
     * @return the equipments
     */
    public List<Article> getEquipments() {
        return equipments;
    }
    
    /**
     * @param equipments
     *            the equipments to set
     */
    public void setEquipments(List<Article> equipments) {
        this.equipments = equipments;
        articleList.setWrappedData(getEquipments());
    }
    
    /**
     * @return the equipmentsFound
     */
    public List<Article> getEquipmentsFound() {
        return equipmentsFound;
    }
    
    /**
     * @param equipmentFound
     *            the equipmentsFound to set
     */
    public void setEquipmentsFound(List<Article> equipmentFound) {
        equipmentsFound = equipmentFound;
    }
    
    /**
     * @return the equipmentsSelected
     */
    public List<Article> getEquipmentsSelected() {
        return equipmentsSelected;
    }
    
    /**
     * @param equipmentsSelected
     *            the equipmentsSelected to set
     */
    public void setEquipmentsSelected(List<Article> equipmentsSelected) {
        this.equipmentsSelected = equipmentsSelected;
    }
    
    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }
    
    /**
     * @param family
     *            the family to set
     */
    public void setFamily(String family) {
        this.family = family;
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return equipmentFilterRegex.getFilterValues();
    }
    
    /**
     * @param filterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> filterValues) {
        equipmentFilterRegex.setFilterValues(filterValues);
    }
    
    /**
     * @return the findCriteria
     */
    public Map<String, Object> getFindCriteria() {
        return findCriteria;
    }
    
    /**
     * @param findCriteria
     *            the findCriteria to set
     */
    public void setFindCriteria(Map<String, Object> findCriteria) {
        this.findCriteria = findCriteria;
    }
    
    /**
     * @return the kernel
     */
    public String getKernel() {
        return kernel;
    }
    
    /**
     * @param pKernel
     *            the kernel to set
     */
    public void setKernel(String pKernel) {
        kernel = pKernel;
    }
    
    /**
     * @return the licence
     */
    public String getLicence() {
        return licence;
    }
    
    /**
     * @param licence
     *            the licence to set
     */
    public void setLicence(String licence) {
        this.licence = licence;
    }
    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }
    
    /**
     * @param pManufacturer
     *            the manufacturer to set
     */
    public void setManufacturer(String pManufacturer) {
        manufacturer = pManufacturer;
    }
    
    /**
     * @return the mpn
     */
    public String getMpn() {
        return mpn;
    }
    
    /**
     * @param mpn
     *            the mpn to set
     */
    public void setMpn(String mpn) {
        this.mpn = mpn;
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
     * @return a boolean indicating if the selected software product has
     *         obsolescence data
     */
    public Boolean hasObsoData() {
        if (software == null) {
            return false;
        }
        return (obsoBean.findByReference(software)) != null;
    }
    
    /**
     * @return the confirmation message for deleting obsolescence data
     */
    public String getObsolescenceDataConfirmationToDelete() {
        StringBuffer sb = new StringBuffer(MessageBundle.getMessage("delete"));
        
        if (software != null) {
            
            ObsolescenceData obsoData = obsoBean.findByReference(software);
            if (obsoData != null) {
                sb.append(".<br />");
                sb.append(MessageBundle
                        .getMessageResource(
                                Constants.OBSO_CONFIRMATION_DELETION_FOR_PN_TYPE_SOFTWARE,
                                new Object[] {
                                        obsoData.getConstituantName() }));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * @return the operatingSystem
     */
    public Boolean getOperatingSystem() {
        return operatingSystem;
    }
    
    /**
     * @param operatingSystem
     *            the operatingSystem to set
     */
    public void setOperatingSystem(Boolean operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    
    private int getQUERY_MAX_RESULTS() {
        String limit = getInitParameter("QUERY_MAX_RESULTS");
        if (limit == null) {
            return 0;
        }
        try {
            return Integer.valueOf(limit);
        }
        catch (NumberFormatException e) {
            log.warning("Context variable not well defined : QUERY_MAX_RESULTS must be a positive integer");
            return 0;
        }
    }
    
    /**
     * @return the rowsToUpdate
     */
    public Set<Integer> getRowsToUpdate() {
        return rowsToUpdate;
    }
    
    /**
     * @param rowsToUpdate
     *            the rowsToUpdate to set
     */
    public void setRowsToUpdate(Set<Integer> rowsToUpdate) {
        this.rowsToUpdate = rowsToUpdate;
    }
    
    /**
     * @return the list of existing distributions according to the stored
     *         operatingSystem and name values
     */
    public List<String> getSelectDistributions() {
        if (operatingSystem != null && name != null) {
            return softwareBean.findAllDistributions(operatingSystem, name);
        }
        else {
            return new ArrayList<String>();
        }
    }
    
    /**
     * @param pOSonly
     *            true if only Operating Systems are selected, false if all
     *            Softwares are selected
     * @return the list of found distributions
     */
    public List<String> getSelectDistributions(boolean pOSonly) {
        
        if (name != null) {
            if (pOSonly) {
                return softwareBean.findAllDistributions(true, name);
            }
            else {
                return softwareBean.findAllDistributions(name);
            }
        }
        else {
            return new ArrayList<String>();
        }
    }
    
    /**
     * @return the software
     */
    public Software getSoftware() {
        return software;
    }
    
    /**
     * @param pSoftware
     *            the software to set
     */
    public void setSoftware(Software pSoftware) {
        software = pSoftware;
        softwareId = software.getId();
    }
    
    /**
     * @param pSoftwareId
     *            the softwareId to set
     */
    @Override
    protected void setItemId(Long pId) {
        softwareId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return softwareId;
    }
    
    /**
     * @return the selectedEquipment
     */
    public Article getSelectedEquipment() {
        return selectedEquipment;
    }
    
    /**
     * @param selectedEquipment
     *            the selectedEquipment to set
     */
    public void setSelectedEquipment(Article selectedEquipment) {
        this.selectedEquipment = selectedEquipment;
    }
    
    /**
     * @return the selectedEquipmentId
     */
    public Long getSelectedEquipmentId() {
        return selectedEquipmentId;
    }
    
    /**
     * @param selectedEquipmentId
     *            the selectedEquipmentId to set
     */
    public void setSelectedEquipmentId(Long selectedEquipmentId) {
        this.selectedEquipmentId = selectedEquipmentId;
    }
    
    /**
     * @return the list of existing kernels according to the stored name and
     *         distribution values
     */
    public List<String> getSelectKernels() {
        
        if (name != null && distribution != null) {
            List<String> lKernels =
                    softwareBean.findAllKernels(name, distribution);
            if (lKernels != null && lKernels.contains("")) {
                lKernels.remove("");
            }
            return lKernels;
        }
        else {
            return new ArrayList<String>();
        }
    }
    
    /**
     * @return the list of existing names according to the stored
     *         operatingSystem value
     */
    public List<String> getSelectNames() {
        
        if (operatingSystem != null) {
            return softwareBean.findAllNames(operatingSystem);
        }
        else {
            return softwareBean.findAllNames();
        }
    }
    
    /**
     * @param pOSonly
     *            true if only Operating Systems are selected, false if all
     *            Softwares are selected
     * @return the list of found names
     */
    public List<String> getSelectNames(boolean pOSonly) {
        
        if (pOSonly) {
            return softwareBean.findAllNames(true);
        }
        else {
            return softwareBean.findAllNames();
        }
    }
    
    /**
     * @return the sn
     */
    public String getSn() {
        return sn;
    }
    
    /**
     * @param sn
     *            the sn to set
     */
    public void setSn(String sn) {
        this.sn = sn;
    }
    
    /**
     * @return the found Software to install, or null
     */
    public Software getSoftwareToInstall() {
        
        return softwareBean.findByCompleteName(Software.getCompleteName(name,
                distribution, kernel));
    }
    
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    private String getUserConnectedLogin() {
        LogInController lLogInController = findBean(LogInController.class);
        User user = lLogInController.getUserLogged();
        String who = (user != null) ? user.getLogin() : null;
        return who;
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createSoftwareTitle");
    }
    
    @Override
    protected String getReadTitle() {
        if (software != null)
            return MessageBundle.getMessageResource("infoSoftwareTitle",
                    new Object[] { software.getCompleteName() });
        else
            return MessageBundle.getMessage("infoSoftwareTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateSoftwareTitle");
    }
    
}
