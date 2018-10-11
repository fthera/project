/*
 * ------------------------------------------------------------------------
 * Class : SearchSoftwareController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.apache.poi.ss.SpreadsheetVersion;
import org.richfaces.model.Filter;

import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.io.ExportExcel;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.filter.ArticleFilterRegex;
import com.airbus.boa.view.filter.SoftwareFilterRegex;

/**
 * Controller managing the search of Software
 */
@ManagedBean(name = SearchSoftwareController.BEAN_NAME)
@SessionScoped
public class SearchSoftwareController extends AbstractController implements
        Serializable {
    
    private enum SearchMode {
        SoftwareList,
        AdvancedSearch,
        /**
         * when only displaying results (advanced search of all elements)
         */
        DisplayResultsAll
    }
    
    /**
     * Contains two attributes, named category and value, corresponding to
     * columns to be displayed when the list of suggestion values should
     * provide a category associated to the value in order to improve easy of
     * use.
     */
    public class SuggestionData implements Comparable<SuggestionData> {
        
        private String category;
        private String value;
        
        /**
         * Constructor
         */
        public SuggestionData() {
            category = "";
            value = "";
        }
        
        /**
         * Constructor
         * 
         * @param pCategory
         *            the category name of the suggestion data
         * @param pValue
         *            the value of the suggestion data
         */
        public SuggestionData(String pCategory, String pValue) {
            category = pCategory;
            value = pValue;
        }
        
        @Override
        public int compareTo(SuggestionData pObject) {
            if (pObject == null) {
                throw new NullPointerException();
            }
            
            if (category.equals(pObject.category)) {
                return value.compareTo(pObject.value);
            }
            return category.compareTo(pObject.category);
        }
        
        /**
         * @return the category
         */
        public String getCategory() {
            return category;
        }
        
        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * @param pCategory
         *            the category to set
         */
        public void setCategory(String pCategory) {
            category = pCategory;
        }
        
        /**
         * @param pValue
         *            the value to set
         */
        public void setValue(String pValue) {
            value = pValue;
        }
    }
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchSoftwareController";

    private SearchMode searchMode;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    private List<Software> foundSoftwares;
    private String searchResultsMessageCount;
    private String searchResultsMessageSome;
    private String searchResultsMessageNone;
    
    private Map<String, Object> inputTextField = new HashMap<String, Object>();
    
    private SoftwareFilterRegex softwareFilterRegex = new SoftwareFilterRegex();
    
    private Long selectedId;
    
    private SelectItem[] allFamilies;
    
    /**
     * Post-construct method to initialize the filter values in the
     * inputTextField map
     */
    @PostConstruct
    private void init() {
        inputTextField.put("operatingSystem", new Boolean[] { true, false });
        String[] lAllFamilies =
                new String[Software.DEPLOYABLE_EQUIPMENT_CLASS.length];
        for (int i = 0; i < lAllFamilies.length; i++) {
            lAllFamilies[i] =
                    Software.DEPLOYABLE_EQUIPMENT_CLASS[i].getSimpleName();
        }
        inputTextField.put("family", lAllFamilies);
    }
    
    /**
     * Export the filtered software products
     */
    public void doExport() {
        
        List<Software> results;
        results = softwareFilterRegex.getFilteredList(foundSoftwares);
        
        ExportExcel formExcel = new ExportExcel(SpreadsheetVersion.EXCEL2007);
        formExcel.writeList(results);
        formExcel.applyStyles();
        
        download(formExcel, "software.xlsx", ExportController.MIMETYPE_XLSX);
    }
    
    /**
     * Prepare the software list page
     * @throws IOException
     */
    public void doListSoftwares() {
        searchMode = SearchMode.SoftwareList;
        foundSoftwares = softwareBean.findAllSoftware();
    }
    
    /**
     * Reset search criteria
     */
    public void doResetCriteria() {
        inputTextField.clear();
        inputTextField.put("operatingSystem", new Boolean[] { true, false });
        String[] lAllFamilies =
                new String[Software.DEPLOYABLE_EQUIPMENT_CLASS.length];
        for (int i = 0; i < lAllFamilies.length; i++) {
            lAllFamilies[i] =
                    Software.DEPLOYABLE_EQUIPMENT_CLASS[i].getSimpleName();
        }
        inputTextField.put("family", lAllFamilies);
        updateSearchResults(null);
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        softwareFilterRegex.resetFilters();
    }
    
    public void prepareSearch() {
        searchMode = SearchMode.AdvancedSearch;
        updateSearchResults(null);
    }
    
    /**
     * Search of software products
     */
    public void doSearch() {
        
        searchMode = SearchMode.AdvancedSearch;
        foundSoftwares =
                softwareBean.advanceSearch(getFilteredInputTextField());
        if (foundSoftwares.isEmpty()) {
            
            Utils.addFacesMessage(NavigationConstants.SEARCH_SOFT_ERROR_ID,
                    MessageBundle
                    .getMessage(Constants.QUERY_SOFTWARE_NO_RESULTS));
        }
        else if (foundSoftwares.size() == 1) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", foundSoftwares.get(0).getId().toString());
            params.put("mode", "READ");
            params.put("ret", NavigationConstants.SOFTWARE_LIST_PAGE);
            NavigationUtil.goTo(NavigationConstants.SOFTWARE_MANAGEMENT_PAGE,
                    params);
        }
        else {
            NavigationUtil
                    .goTo(NavigationConstants.SOFTWARE_SEARCH_RESULT_PAGE);
        }
        String msg =
                MessageBundle.getMessageResource(
                        Constants.QUERY_SOFTWARE_NUMBER_RESULTS,
                        new Object[] { foundSoftwares.size() });
        log.info(msg);
    }

	/**
	 * Return the filter for software operating systems
	 * 
	 * @return the requested filter object
	 */
	public Filter<?> getOperatingSystemFilter() {
		return new Filter<Software>() {
			public boolean accept(Software item) {
                return softwareFilterRegex.filterMethodOperatingSystem(item);
			}
		};
	}

	/**
	 * Return the filter for softwares
	 * 
	 * @return the requested filter object
	 */
	public Filter<?> getSoftwareFilter() {
		return new Filter<Software>() {
			public boolean accept(Software item) {
				return softwareFilterRegex.filterMethodRegex(item);
			}
		};
	}
    
    /**
     * Prepare the obsolescence data consultation page
     */
    public void showSelectedObsoData() {
        if (getSelectedId() != null) {
            ObsolescenceData obso =
                    obsoBean.findByReference(softwareBean
                            .findById(getSelectedId()));
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", obso.getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
    }
    
    /**
     * Compute a list of suggestion values containing Airbus PN values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Airbus PN.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Airbus PN.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsEquipmentAirbusPNs(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("apn", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing manufacturer PN values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched manufacturer PN.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched manufacturer PN.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsEquipmentManufacturerPNs(
            FacesContext pContext, UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("mpn", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion data containing as Category the name of
     * SN origin ("AirbusSN" or "ManufacturerSN") and as Value the SN.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched SN.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched SN
     * @return the list of found suggestion values in SuggestionData form
     */
    public List<SuggestionData> suggestionsEquipmentSNs(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        List<Software> lSoftwareResults =
                softwareBean.suggestionListSoftware("sn", (String) pPrefix,
                        getFilteredInputTextField());
        
        List<Article> lArticles = new ArrayList<Article>();
        for (Software lSoftware : lSoftwareResults) {
            lArticles.addAll(lSoftware.getEquipments());
        }
        
        String lSuggestValue = (String) pPrefix;
        
        if (lSuggestValue.indexOf('*') == -1) {
            lSuggestValue = "*" + lSuggestValue + "*";
        }
        
        // initialize filters in order to return only the attribute which
        // satisfies the suggest value
        
        ArticleFilterRegex lFilterAirbus = new ArticleFilterRegex();
        ArticleFilterRegex lFilterManufacturer = new ArticleFilterRegex();
        
        Map<String, String> lFiltersAirbus = new HashMap<String, String>();
        Map<String, String> lFiltersManufacturer =
                new HashMap<String, String>();
        
        lFiltersAirbus.put("airbusSN", lSuggestValue);
        lFiltersManufacturer.put("manufacturerSN", lSuggestValue);
        
        lFilterAirbus.setFilterValues(lFiltersAirbus);
        lFilterManufacturer.setFilterValues(lFiltersManufacturer);
        
        // retrieve all AirbusSN and ManufacturerSN values into a SortedSet
        // in order to avoid duplicate values and to sort them
        SortedSet<String> listAirbusSN = new TreeSet<String>();
        SortedSet<String> listManufacturerSN = new TreeSet<String>();
        for (int i = 0; i < lArticles.size(); i++) {
            Article currentArticle = lArticles.get(i);
            // if currentArticle AirbusSN is not null and satisfies the suggest
            // value, add it to the returned values
            if (currentArticle.getAirbusSN() != null
                    && lFilterAirbus.filterMethodRegex(currentArticle)) {
                listAirbusSN.add(currentArticle.getAirbusSN());
            }
            // if currentArticle ManufacturerSN is not null and satisfies the
            // suggest value, add it to the returned values
            if (currentArticle.getManufacturerSN() != null
                    && lFilterManufacturer.filterMethodRegex(currentArticle)) {
                listManufacturerSN.add(currentArticle.getManufacturerSN());
            }
        }
        
        // if the empty string has been added to the lists, remove it
        listAirbusSN.remove("");
        listManufacturerSN.remove("");
        
        // initialize the returned list
        List<SuggestionData> lFinalList = new ArrayList<SuggestionData>();
        
        // fill the list with the sorted AirbusSN
        Iterator<String> iterator = listAirbusSN.iterator();
        while (iterator.hasNext()) {
            lFinalList.add(new SuggestionData(MessageBundle
                    .getMessage("AirbusSN"), iterator.next()));
        }
        // fill the list with the sorted ManufacturerSN
        iterator = listManufacturerSN.iterator();
        while (iterator.hasNext()) {
            lFinalList.add(new SuggestionData(MessageBundle
                    .getMessage("ManufacturerSN"), iterator.next()));
        }
        
        return lFinalList;
    }
    
    /**
     * Compute a list of suggestion values containing type values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched type.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched type.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsEquipmentTypes(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("type", (String) pPrefix);
    }
    
    /**
     * Compute a list of Softwares property values (in String form).
     * Softwares are the ones which property corresponding to the parameter name
     * satisfies the suggest value.
     * 
     * @param pParamName
     *            the parameter name corresponding to the property on which to
     *            apply
     *            the provided criteria
     * @param pSuggest
     *            the suggest value corresponding to the applied criteria.
     *            When empty or null, it is replaced by "*", i.e. every value.
     * @return the list of found Softwares property values (in String form)
     */
    private List<String> suggestionSoftwareValue(String pParamName,
            String pSuggest) {
        
        String lSuggest = pSuggest;
        if (StringUtil.isEmptyOrNull(lSuggest)) {
            lSuggest = "*";
        }
        
        List<Software> results =
                softwareBean.suggestionListSoftware(pParamName, lSuggest,
                        getFilteredInputTextField());
        
        // retrieve all values into a set in order to avoid duplicate values
        SortedSet<String> list = new TreeSet<String>();
        for (int i = 0; i < results.size(); i++) {
            
            if (pParamName.equals("name")) {
                list.add(results.get(i).getName());
            }
            else if (pParamName.equals("distribution")) {
                list.add(results.get(i).getDistribution());
            }
            else if (pParamName.equals("kernel")) {
                list.add(results.get(i).getKernel());
            }
            else if (pParamName.equals("manufacturer")) {
                list.add(results.get(i).getManufacturer());
            }
            else if (pParamName.equals("licence")) {
                list.add(results.get(i).getLicence());
            }
            else if (pParamName.equals("description")) {
                list.add(results.get(i).getDescription());
            }
            else if (pParamName.equals("type")) {
                for (Article lArticle : results.get(i).getEquipments()) {
                    list.add(lArticle.getTypeArticle().getLabel());
                }
            }
            
            else if (pParamName.equals("apn")) {
                for (Article lArticle : results.get(i).getEquipments()) {
                    list.add(lArticle.getAirbusPN().getIdentifier());
                }
            }
            
            else if (pParamName.equals("mpn")) {
                for (Article lArticle : results.get(i).getEquipments()) {
                    list.add(lArticle.getManufacturerPN().getIdentifier());
                }
            }
            
        }
        
        // if the empty string has been added to the list, remove it
        list.remove("");
        
        return new ArrayList<String>(list);
    }
    
    /**
     * Compute a list of suggestion values containing description values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched description.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched description.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareDescriptions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("description", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing distribution values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched distribution.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched distribution.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareDistributions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("distribution", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing kernel values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched kernel.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched kernel.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareKernels(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("kernel", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing license values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched license.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched license.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareLicences(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("licence", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Software manufacturers.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software manufacturer.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software
     *            manufacturer.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareManufacturers(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("manufacturer", (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Software names.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software name.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string in Object form corresponding to the searched
     *            Software name.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareNames(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        return suggestionSoftwareValue("name", (String) pPrefix);
    }
    
    /**
     * Executes the advance research and then updates the
     * searchResultsMessageSome and searchResultsMessageNone
     * with the number of found results.
     * 
     * @param pEvent
     *            the event sent when an ajax update is requested
     */
    public void updateSearchResults(AjaxBehaviorEvent pEvent) {
        
        switch (searchMode) {
        case DisplayResultsAll:
        case AdvancedSearch:
            foundSoftwares =
                    softwareBean.advanceSearch(getFilteredInputTextField());
            break;
        case SoftwareList:
            foundSoftwares = softwareBean.findAllSoftware();
            break;
        default:
            break;
        }
        
        if (foundSoftwares.isEmpty()) {
            searchResultsMessageNone =
                    MessageBundle
                            .getMessage(Constants.QUERY_SOFTWARE_NO_RESULTS);
            searchResultsMessageCount = "";
            searchResultsMessageSome = "";
        }
        else {
            searchResultsMessageNone = "";
            searchResultsMessageCount =
                    ((Integer) foundSoftwares.size()).toString();
            searchResultsMessageSome =
                    " "
                            + MessageBundle
                                    .getMessage("advancedSearchNbSoftwareFound");
        }
    }
    
    /**
     * @return the allFamilies
     */
    public SelectItem[] getAllFamilies() {
        allFamilies =
                new SelectItem[Software.DEPLOYABLE_EQUIPMENT_CLASS.length];
        for (int i = 0; i < Software.DEPLOYABLE_EQUIPMENT_CLASS.length; i++) {
            String className =
                    Software.DEPLOYABLE_EQUIPMENT_CLASS[i].getSimpleName();
            allFamilies[i] =
                    new SelectItem(className,
                            MessageBundle.getMessage(className));
        }
        return allFamilies;
    }
    
    /**
     * @param allFamilies
     *            the allFamilies to set
     */
    public void setAllFamilies(SelectItem[] allFamilies) {
        this.allFamilies = allFamilies;
    }
    
    /**
     * @return the countFiltered
     */
    public int getCountFiltered() {
        return softwareFilterRegex.countFiltered(foundSoftwares);
    }
    
    /**
     * @return true if the edition is available, else false
     */
    public boolean isDisplayEditionColumn() {
        
        switch (searchMode) {
        case AdvancedSearch:
        case SoftwareList:
            return true;
        case DisplayResultsAll:
        default:
            return false;
        }
    }
    
    /**
     * @return the filterOperatingSystem
     */
    public List<Boolean> getFilterOperatingSystem() {
        return softwareFilterRegex.getFilterOperatingSystem();
    }
    
    /**
     * @param pFilterOperatingSystem
     *            the filterOperatingSystem to set
     */
    public void setFilterOperatingSystem(List<Boolean> pFilterOperatingSystem) {
        softwareFilterRegex.setFilterOperatingSystem(pFilterOperatingSystem);
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return softwareFilterRegex.getFilterValues();
    }
    
    /**
     * @param filterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> filterValues) {
        softwareFilterRegex.setFilterValues(filterValues);
    }
    
    /**
     * @return the foundSoftwares
     */
    public List<Software> getFoundSoftwares() {
        return foundSoftwares;
    }
    
    /**
     * @return the inputTextField
     */
    public Map<String, Object> getInputTextField() {
        return inputTextField;
    }
    
    /**
     * @return the filter inputTextField
     */
    public Map<String, Object> getFilteredInputTextField() {
        Map<String, Object> lFilteredInputTextField =
                new HashMap<String, Object>(inputTextField);
        if (!getSearchOsFiltered()) {
            lFilteredInputTextField.remove("operatingSystem");
        }
        if (!getFamilyFiltered()) {
            lFilteredInputTextField.remove("family");
        }
        return lFilteredInputTextField;
    }
    
    /**
     * @param inputTextField
     *            the inputTextField to set
     */
    public void setInputTextField(Map<String, Object> inputTextField) {
        this.inputTextField = inputTextField;
    }
    
    /**
     * Determine if the provided software has associated obsolescence data
     * 
     * @param software
     *            the software
     * @return true if the software has associated obsolescence data, else false
     */
    public Boolean hasObsoData(Software software) {
        return (obsoBean.findByReference(software)) != null;
    }
    
    /**
     * @return the select values for operating system
     */
    public SelectItem[] getOperatingSystems() {
        
        SelectItem[] lOperatingSystems = new SelectItem[2];
        lOperatingSystems[0] =
                new SelectItem(Boolean.TRUE,
                        MessageBundle.getMessage(Constants.YES));
        lOperatingSystems[1] =
                new SelectItem(Boolean.FALSE,
                        MessageBundle.getMessage(Constants.NO));
        
        return lOperatingSystems;
    }
    
    /**
     * @return the searchResultsMessageCount
     */
    public String getSearchResultsMessageCount() {
        return searchResultsMessageCount;
    }
    
    /**
     * @return the searchResultsMessageNone
     */
    public String getSearchResultsMessageNone() {
        return searchResultsMessageNone;
    }
    
    /**
     * @return the searchResultsMessageSome
     */
    public String getSearchResultsMessageSome() {
        return searchResultsMessageSome;
    }
    
    /**
     * @return the selectedId
     */
    public Long getSelectedId() {
        return selectedId;
    }
    
    /**
     * @param selectedId
     *            the selectedId to set
     */
    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }
    
    /**
     * Initialize the attributes in order to display the list of software
     * products into the software list page
     * 
     * @param pSoftwares
     *            the software products list to display
     */
    public void setSoftwaresToDisplay(List<Software> pSoftwares) {
        searchMode = SearchMode.DisplayResultsAll;
        foundSoftwares = pSoftwares;
    }
    
    /**
     * Check if the OS column is filtered
     * 
     * @return true if the column is filtered, false otherwise
     */
    public boolean getOsFiltered() {
        if (softwareFilterRegex.getFilterOperatingSystem().size() < 2){
            return true;
        }
        return false;
    }
    
    /**
     * Check if the OS search field is filtered
     * 
     * @return true if the field is filtered, false otherwise
     */
    public boolean getSearchOsFiltered() {
        Object[] lParamList = (Object[]) inputTextField.get("operatingSystem");
        if (lParamList != null && lParamList.length < 2) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if the Family search field is filtered
     * 
     * @return true if the field is filtered, false otherwise
     */
    public boolean getFamilyFiltered() {
        Object[] lParamList = (Object[]) inputTextField.get("family");
        if (lParamList != null
                && lParamList.length < Software.DEPLOYABLE_EQUIPMENT_CLASS.length) {
            return true;
        }
        return false;
    }
    
}
