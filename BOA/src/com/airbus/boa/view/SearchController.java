/*
 * ------------------------------------------------------------------------
 * Class : SearchController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.SearchBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.control.query.ArticleQueryBuilder;
import com.airbus.boa.control.query.CriteriaStructure;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleClass;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AvailabilityStatus;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.io.ExportExcel;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.datamodel.ArticleDataModel;
import com.airbus.boa.view.filter.CommunicationPortFilterRegex;
import com.airbus.boa.view.filter.SimuFilterRegex;
import com.airbus.boa.view.filter.SoftwareFilterRegex;
import com.airbus.boa.view.filter.ToolFilterRegex;

/**
 * Controller managing the search of Articles and list of Installations
 */
@ManagedBean(name = SearchController.BEAN_NAME)
@SessionScoped
public class SearchController extends AbstractController implements
        Serializable {
    
    private enum DisplayMode {
        OnlyCabinet,
        OnlyRack,
        OnlySwitch,
        OnlyBoard,
        OnlyVarious,
        OnlyPC,
        AnyArticle
    }
    
    private enum SearchMode {
        CabinetList,
        RackList,
        SwitchList,
        BoardList,
        VariousList,
        PCList,
        AdvancedSearch,
        /**
         * when only displaying results of all families
         * (advanced search of all elements)
         */
        DisplayResultsAll,
        /**
         * when the elements of the list are not articles (installations, tools)
         */
        OtherList
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
         * @param category
         *            the category name of the suggestion data
         * @param value
         *            the value of the suggestion data
         */
        public SuggestionData(String category, String value) {
            this.category = category;
            this.value = value;
        }
        
        @Override
        public int compareTo(SuggestionData o) {
            if (o == null) {
                throw new NullPointerException();
            }
            
            if (category.equals(o.category)) {
                return value.compareTo(o.value);
            }
            return category.compareTo(o.category);
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
         * @param category
         *            the category to set
         */
        public void setCategory(String category) {
            this.category = category;
        }
        
        /**
         * @param value
         *            the value to set
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
    
    private static final long serialVersionUID = 8019442362483040785L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchController";
    
    /** The located name for the location type filter */
    public static final String LOCATED = "located";
    /** The located name for the location type filter */
    public static final String NOT_LOCATED = "notLocated";
    
    /** The string identifying the source of the filtered request */
    public static final String ARTICLE_SEARCH = "search";
    public static final String DATA_MODEL = "dataModel";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private SearchBean searchBean;
    
    private ArticleDataModel articleDataModel;
    
    private String searchResultsMessageCount;
    private String searchResultsMessageSome;
    private String searchResultsMessageNone;
    
    private List<Installation> foundInstallations =
            new ArrayList<Installation>();
    
    private List<Tool> foundTools = new ArrayList<Tool>();
    
    private Map<String, Object> inputTextField = new HashMap<String, Object>();
    private Map<String, List<SelectItem>> filterChoices =
            new HashMap<String, List<SelectItem>>();
    
    private SearchMode searchMode;
    private DisplayMode displayMode;
    
    private boolean displayPCCriteria = false;
    private boolean displayArticleExceptPCCriteria = true;
    
    private Long selectedId;
    
    private String displayResultsTitle;
    
    private SimuFilterRegex simuFilterRegex = new SimuFilterRegex();
    
    private Integer countFilteredSimu;
    
    private ToolFilterRegex toolFilterRegex = new ToolFilterRegex();
    
    private Integer countFilteredTool;
    
    /**
     * Post-construct method to initialize the filter choices
     */
    @PostConstruct
    private void init() {
        doInitListChoices();
        doResetCriteria();
    }
    
    /**
     * Update the display conditions for the page fields and then update the
     * search results
     */
    public void changeFamilyValue() {
        
        String lSelectedFamilyString =
                (String) inputTextField.get(ArticleQueryBuilder.FAMILY);
        
        if (lSelectedFamilyString != null) {
            
            ArticleClass lSelectedFamily =
                    ArticleClass.valueOf(lSelectedFamilyString);
            
            switch (lSelectedFamily) {
            case Board:
            case Cabinet:
            case Rack:
            case Switch:
            case Various:
                
                displayPCCriteria = false;
                removePCCriteria();
                
                displayArticleExceptPCCriteria = true;
                
                break;
            case PC:
                
                displayPCCriteria = true;
                
                displayArticleExceptPCCriteria = false;
                removeArticleExceptPCCriteria();
                
                break;
            default:
                break;
            }
        }
        else {
            displayPCCriteria = false;
            removePCCriteria();
            
            displayArticleExceptPCCriteria = true;
        }
        
        updateSearchResults(null);
    }
    
    /**
     * Initialize the filter choices for all the needed columns
     */
    public void doInitListChoices() {
        filterChoices = new HashMap<String, List<SelectItem>>();
        
        filterChoices.put(ArticleQueryBuilder.STATE,
                ArticleState.getSelectItems());
        
        LogInController lLogInController = findBean(LogInController.class);
        boolean lIsArchivedAuthorized = lLogInController
                .isAuthorized(RightMaskAction.ArchivedCRUDAuthorization);
        filterChoices.put(ArticleQueryBuilder.USE_STATE,
                UseState.getSelectItems(lIsArchivedAuthorized));
        
        filterChoices.put(ArticleQueryBuilder.CONTAINER_TYPE,
                ContainerType.getSelectItems());
        
        List<SelectItem> lLocationTypes = new ArrayList<SelectItem>();
        lLocationTypes.add(new SelectItem(Boolean.TRUE,
                MessageBundle.getMessage(LOCATED)));
        lLocationTypes.add(new SelectItem(Boolean.FALSE,
                MessageBundle.getMessage(NOT_LOCATED)));
        filterChoices.put(ArticleQueryBuilder.LOCATION_TYPE, lLocationTypes);
        
        List<SelectItem> lExternalLocationTypes =
                valueListBean.generateSelectItems(ExternalEntityType.class);
        lExternalLocationTypes
                .add(new SelectItem(0L, MessageBundle.getMessage("notSent")));
        filterChoices.put(ArticleQueryBuilder.EXTERNAL_LOCATION_TYPE,
                lExternalLocationTypes);
        
        filterChoices.put(ArticleQueryBuilder.PRODUCT_TYPE,
                valueListBean.generateSelectItems(ProductTypePC.class));
        
        filterChoices.put(ArticleQueryBuilder.DEPARTMENT_IN_CHARGE,
                valueListBean.generateSelectItems(DepartmentInCharge.class));
        
        List<SelectItem> lDomains =
                valueListBean.generateSelectItems(Domain.class);
        lDomains.add(new SelectItem(0L, MessageBundle.getMessage("unknown")));
        filterChoices.put(ArticleQueryBuilder.DOMAIN, lDomains);
        
        filterChoices.put(ArticleQueryBuilder.ALLOCATION,
                valueListBean.generateSelectItems(BusinessAllocationPC.class));
        
        filterChoices.put(ArticleQueryBuilder.USAGE,
                valueListBean.generateSelectItems(BusinessUsagePC.class));
        
        List<SelectItem> lNetworks = new ArrayList<SelectItem>();
        try {
            ComPortController comPortCtrl = findBean(ComPortController.class);
            lNetworks = comPortCtrl.getNetworksNames();
        } catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.SEARCH_ART_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
        lNetworks.add(new SelectItem(0L, MessageBundle.getMessage("unknown")));
        filterChoices.put(ArticleQueryBuilder.NETWORK, lNetworks);
        
        filterChoices.put(ArticleQueryBuilder.AVAILABILITY_STATUS,
                PC.AvailabilityStatus.getSelectItems());
        
        List<SelectItem> lYesNoList = new ArrayList<SelectItem>();
        lYesNoList.add(new SelectItem(Boolean.TRUE,
                MessageBundle.getMessage(Constants.YES)));
        lYesNoList.add(new SelectItem(Boolean.FALSE,
                MessageBundle.getMessage(Constants.NO)));
        
        filterChoices.put(ArticleQueryBuilder.HAS_COM_PORTS, lYesNoList);
        filterChoices.put(ArticleQueryBuilder.HAS_DOCUMENTS, lYesNoList);
        filterChoices.put(ArticleQueryBuilder.HAS_DEFAULT_OS,
                new ArrayList<>(lYesNoList));
        filterChoices.put(ArticleQueryBuilder.HAS_SOFTWARES,
                new ArrayList<>(lYesNoList));
        
    }
    
    /**
     * Initialize the attributes in order to display the list of articles into
     * the articles list page
     * 
     * @param pInitialFilterMap
     *            the initial filter map
     * @param pCombinationAnd
     *            boolean indicating if criteria shall be combined by an AND
     *            (true) or by an OR (false)
     */
    public void doDisplayAllQuery(Map<String, Object> pInitialFilterMap,
            boolean pCombinationAnd) {
        
        searchMode = SearchMode.DisplayResultsAll;
        
        initArticleDataModel(pInitialFilterMap, null, null, pCombinationAnd);
        
        doDisplayQuery();
    }
    
    /**
     * Prepare the PC results page
     * 
     * @param pInitialFilterMap
     *            the initial filter map
     * @param pAutoConditions
     *            the automatic conditions (can be null)
     * @param pAutoFilterMap
     *            the automatic filter map (can be null)
     */
    public void doDisplayPCQuery(Map<String, Object> pInitialFilterMap,
            List<String> pAutoConditions, Map<String, Object> pAutoFilterMap) {
        
        searchMode = SearchMode.PCList;
        
        initArticleDataModel(pInitialFilterMap, pAutoConditions, pAutoFilterMap);
        
        doDisplayQuery();
    }
    
    /**
     * Prepare the articles results page corresponding to the provided articles
     * list
     */
    private void doDisplayQuery() {
        
        articleDataModel.resetFilters();
        
        String lFamily;
        switch (searchMode) {
        case AdvancedSearch:
            lFamily = "Advanced";
            displayMode = DisplayMode.AnyArticle;
            
            // If a specific family is selected, the list page dedicated to this
            // family will be opened instead of the generic results page
            String lSelectedFamily =
                    (String) inputTextField.get(ArticleQueryBuilder.FAMILY);
            if (lSelectedFamily != null) {
                if (lSelectedFamily.equals("Board")) {
                    displayMode = DisplayMode.OnlyBoard;
                    lFamily = Board.class.getSimpleName();
                }
                else if (lSelectedFamily.equals("Cabinet")) {
                    displayMode = DisplayMode.OnlyCabinet;
                    lFamily = Cabinet.class.getSimpleName();
                }
                else if (lSelectedFamily.equals("PC")) {
                    displayMode = DisplayMode.OnlyPC;
                    lFamily = PC.class.getSimpleName();
                }
                else if (lSelectedFamily.equals("Rack")) {
                    displayMode = DisplayMode.OnlyRack;
                    lFamily = Rack.class.getSimpleName();
                }
                else if (lSelectedFamily.equals("Switch")) {
                    displayMode = DisplayMode.OnlySwitch;
                    lFamily = Switch.class.getSimpleName();
                }
                else if (lSelectedFamily.equals("Various")) {
                    displayMode = DisplayMode.OnlyVarious;
                    lFamily = Various.class.getSimpleName();
                }
            }
            break;
        case DisplayResultsAll:
            displayMode = DisplayMode.AnyArticle;
            lFamily = "Advanced";
            break;
        case BoardList:
            displayMode = DisplayMode.OnlyBoard;
            lFamily = Board.class.getSimpleName();
            break;
        case CabinetList:
            displayMode = DisplayMode.OnlyCabinet;
            lFamily = Cabinet.class.getSimpleName();
            break;
        case PCList:
            displayMode = DisplayMode.OnlyPC;
            lFamily = PC.class.getSimpleName();
            break;
        case RackList:
            displayMode = DisplayMode.OnlyRack;
            lFamily = Rack.class.getSimpleName();
            break;
        case SwitchList:
            displayMode = DisplayMode.OnlySwitch;
            lFamily = Switch.class.getSimpleName();
            break;
        case VariousList:
            displayMode = DisplayMode.OnlyVarious;
            lFamily = Various.class.getSimpleName();
            break;
        case OtherList:
        default:
            displayMode = DisplayMode.AnyArticle;
            lFamily = "Unknown";
            break;
        }
        
        String familyName = MessageBundle.getMessage(lFamily);
        displayResultsTitle =
                MessageBundle.getMessageResource(Constants.DISPLAY_RESULTS,
                        new Object[] { familyName });
    }
    
    /**
     * Export the filtered articles
     */
    public void doExport() {
        List<Article> lArticles = articleDataModel.findEntities(0, -1);
        ExportExcel formExcel = new ExportExcel(SpreadsheetVersion.EXCEL2007);
        formExcel.writeListResult(lArticles);
        formExcel.applyStyles();
        download(formExcel, "articles.xlsx", ExportController.MIMETYPE_XLSX);
    }
    
    /**
     * Export the filtered installations
     */
    public void doExportInstallation() {
        List<Installation> results =
                simuFilterRegex.getFilteredList(foundInstallations);
        ExportExcel formExcel = new ExportExcel(SpreadsheetVersion.EXCEL2007);
        formExcel.writeListResult(results);
        formExcel.applyStyles();
        download(formExcel, "installations.xlsx",
                ExportController.MIMETYPE_XLSX);
        
    }
    
    /**
     * Export the filtered tool
     */
    public void doExportTool() {
        List<Tool> lResults = toolFilterRegex.getFilteredList(foundTools);
        ExportExcel lFormExcel = new ExportExcel(SpreadsheetVersion.EXCEL2007);
        lFormExcel.writeListResult(lResults);
        lFormExcel.applyStyles();
        download(lFormExcel, "tools.xlsx", ExportController.MIMETYPE_XLSX);
    }
    
    /**
     * Prepare the boards list page
     */
    public void doListBoards() {
        searchMode = SearchMode.BoardList;
        displayMode = DisplayMode.OnlyBoard;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, Board.class.getSimpleName());
        
        initArticleDataModel(lFilter, null, null);
        doDisplayQuery();
    }
    
    /**
     * Prepare the cabinets list page
     */
    public void doListCabinets() {
        searchMode = SearchMode.CabinetList;
        displayMode = DisplayMode.OnlyCabinet;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, Cabinet.class.getSimpleName());
        
        initArticleDataModel(lFilter, null, null);
        doDisplayQuery();
    }
    
    /**
     * Prepare the installations list page
     */
    public void doListInstallations() {
        searchMode = SearchMode.OtherList;
        foundInstallations = locationBean.findAllInstallation();
    }
    
    /**
     * Prepare the PC list page
     */
    public void doListPCs() {
        searchMode = SearchMode.PCList;
        displayMode = DisplayMode.OnlyPC;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, PC.class.getSimpleName());
        
        LogInController lLogInController = findBean(LogInController.class);
        boolean lIsArchivedAuthorized = lLogInController
                .isAuthorized(RightMaskAction.ArchivedCRUDAuthorization);
        if (!lIsArchivedAuthorized) {
            lFilter.put(ArticleQueryBuilder.USE_STATE, UseState.values(false));
        }
        
        initArticleDataModel(lFilter, null, null);
        if (lIsArchivedAuthorized) {
            articleDataModel.getSelectionFilterMap()
                    .put(ArticleQueryBuilder.USE_STATE, UseState.values(false));
        }
        doDisplayQuery();
    }
    
    /**
     * Prepare the racks list page
     */
    public void doListRacks() {
        searchMode = SearchMode.RackList;
        displayMode = DisplayMode.OnlyRack;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, Rack.class.getSimpleName());
        
        initArticleDataModel(lFilter, null, null);
        doDisplayQuery();
    }
    
    /**
     * Prepare the switches list page
     */
    public void doListSwitches() {
        searchMode = SearchMode.SwitchList;
        displayMode = DisplayMode.OnlySwitch;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, Switch.class.getSimpleName());
        
        initArticleDataModel(lFilter, null, null);
        doDisplayQuery();
    }
    
    /**
     * Prepare the tools list page
     */
    public void doListTools() {
        searchMode = SearchMode.OtherList;
        foundTools = toolBean.findAllTools();
    }
    
    /**
     * Prepare the various list page
     */
    public void doListVarious() {
        searchMode = SearchMode.VariousList;
        displayMode = DisplayMode.OnlyVarious;
        doInitListChoices();
        
        Map<String, Object> lFilter = new HashMap<String, Object>();
        lFilter.put(ArticleQueryBuilder.FAMILY, Various.class.getSimpleName());
        
        initArticleDataModel(lFilter, null, null);
        doDisplayQuery();
    }
    
    /**
     * Reset search criteria
     */
    public void doResetCriteria() {
        displayPCCriteria = false;
        displayArticleExceptPCCriteria = true;
        inputTextField.clear();
        for (String lKey : filterChoices.keySet()) {
            // For the use state filter, do not select the Archived value
            if (lKey.equals(ArticleQueryBuilder.USE_STATE)) {
                Object[] lOldChoicesValues = getChoicesValues(lKey);
                List<Object> lChoicesList =
                        new ArrayList<>(Arrays.asList(lOldChoicesValues));
                lChoicesList.remove(UseState.Archived);
                Object[] lChoicesValues = new Object[lChoicesList.size()];
                lChoicesValues = lChoicesList.toArray(lChoicesValues);
                inputTextField.put(lKey, lChoicesValues);
            }
            else {
                inputTextField.put(lKey, getChoicesValues(lKey));
            }
        }
        updateSearchResults(null);
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        articleDataModel.resetFilters();
    }
    
    /**
     * Search of Articles (PC have their own search method)
     */
    public void doSearch() {
        searchMode = SearchMode.AdvancedSearch;
        initArticleDataModel(getFilteredInputTextField(), null, null);
        int lNbArticles = articleDataModel.getRowCount();
        if (lNbArticles <= 0) {
            Utils.addFacesMessage(NavigationConstants.SEARCH_ART_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS));
        }
        else if (lNbArticles == 1) {
            List<Article> lArticles = articleDataModel.findEntities(0, 1);
            Article lArticle = lArticles.get(0);
            selectedId = lArticle.getId();
            NavigationUtil.doShowItem(selectedId, articleBean, locationBean,
                    toolBean);
        }
        else {
            doDisplayQuery();
            NavigationUtil.goTo(NavigationConstants.ARTICLE_SEARCH_RESULT_PAGE);
        }
    }
    
    /**
     * Reset installation filters
     */
    public void doSimuResetFilters() {
        simuFilterRegex.resetFilters();
    }
    
    /**
     * Reset tool filters
     */
    public void doToolResetFilters() {
        toolFilterRegex.resetFilters();
    }
    
    /**
     * Initialize the articleDataModel attribute with the provided values.<br>
     * Combination of criteria is done with AND.
     * 
     * @param pInitialFilterMap
     *            the initial filter map
     * @param pAutoConditions
     *            the automatic conditions (can be null)
     * @param pAutoFilterMap
     *            the automatic filter map (can be null)
     */
    private void initArticleDataModel(Map<String, Object> pInitialFilterMap,
            List<String> pAutoConditions, Map<String, Object> pAutoFilterMap) {
        initArticleDataModel(pInitialFilterMap, pAutoConditions,
                pAutoFilterMap, true);
    }
    
    /**
     * Initialize the articleDataModel attribute with the provided values
     * 
     * @param pInitialFilterMap
     *            the initial filter map
     * @param pAutoConditions
     *            the automatic conditions (can be null)
     * @param pAutoFilterMap
     *            the automatic filter map (can be null)
     * @param pCombinationAnd
     *            true if combination shall be AND, false for OR
     */
    private void initArticleDataModel(Map<String, Object> pInitialFilterMap,
            List<String> pAutoConditions, Map<String, Object> pAutoFilterMap,
            boolean pCombinationAnd) {
        
        LocaleController lLocaleController = findBean(LocaleController.class);
        
        articleDataModel =
                new ArticleDataModel(articleBean, searchBean,
                        lLocaleController.getISO3Langage());
        
        articleDataModel.setInitialFilterMap(pInitialFilterMap);
        
        HashMap<String, List<SelectItem>> lFilterChoices =
                new HashMap<String, List<SelectItem>>(filterChoices);
        articleDataModel.setFilterChoices(lFilterChoices);
        
        if (pAutoConditions != null) {
            articleDataModel.setAutoConditions(pAutoConditions);
        }
        
        if (pAutoFilterMap != null) {
            articleDataModel.setAutoFilterMap(pAutoFilterMap);
        }
        
        articleDataModel.setInitialAnd(pCombinationAnd);
    }
    
    /**
     * Refresh the data model in order to refresh the datatable
     */
    public void refreshDataModel() {
    }
    
    /**
     * Remove criteria specific to articles other than PC
     */
    private void removeArticleExceptPCCriteria() {
        
        // Remove Article specific criteria from Identification field set
        inputTextField.remove(ArticleQueryBuilder.AIRBUS_PN);
        inputTextField.remove(ArticleQueryBuilder.MANUFACTURER_PN);
        inputTextField.remove(ArticleQueryBuilder.CMS_CODE);
        inputTextField.remove(ArticleQueryBuilder.DESIGNATION);
    }
    
    /**
     * Remove criteria specific to PC
     */
    private void removePCCriteria() {
        
        // Remove Communication ports criteria
        inputTextField.remove(ArticleQueryBuilder.HAS_COM_PORTS);
        inputTextField.remove(ArticleQueryBuilder.PORT_NAME);
        inputTextField.remove(ArticleQueryBuilder.IP_ADDRESS);
        inputTextField.remove(ArticleQueryBuilder.MAC_ADDRESS);
        inputTextField.remove(ArticleQueryBuilder.NETWORK);
        inputTextField.remove(ArticleQueryBuilder.COMMENT_PORT);
        inputTextField.remove(ArticleQueryBuilder.SOCKET);
        
        // Remove Installed software criteria
        inputTextField.remove(ArticleQueryBuilder.HAS_SOFTWARES);
        inputTextField.remove(ArticleQueryBuilder.SOFTWARE_NAME);
        inputTextField.remove(ArticleQueryBuilder.SOFTWARE_DISTRIBUTION);
        inputTextField.remove(ArticleQueryBuilder.SOFTWARE_KERNEL);
        
        // Remove Default OS criteria
        inputTextField.remove(ArticleQueryBuilder.HAS_DEFAULT_OS);
        inputTextField.remove(ArticleQueryBuilder.DEFAULT_OS_NAME);
        inputTextField.remove(ArticleQueryBuilder.DEFAULT_OS_DISTRIBUTION);
        inputTextField.remove(ArticleQueryBuilder.DEFAULT_OS_KERNEL);
        
        // Remove PC specific criteria from Identification field set
        inputTextField.remove(ArticleQueryBuilder.PRODUCT_TYPE);
        inputTextField.remove(ArticleQueryBuilder.NAME);
        inputTextField.remove(ArticleQueryBuilder.FUNCTION);
        inputTextField.remove(ArticleQueryBuilder.DOMAIN);
        inputTextField.remove(ArticleQueryBuilder.PLATFORM);
        
        // Remove PC specific criteria from Properties field set
        inputTextField.remove(ArticleQueryBuilder.IN_CHARGE_LOGIN_DETAILS);
        inputTextField.remove(ArticleQueryBuilder.OWNER);
        inputTextField.remove(ArticleQueryBuilder.OWNER_SIGLUM);
        inputTextField.remove(ArticleQueryBuilder.ADMIN);
        inputTextField.remove(ArticleQueryBuilder.NB_SCREENS_MIN);
        inputTextField.remove(ArticleQueryBuilder.NB_SCREENS_MAX);
        inputTextField.remove(ArticleQueryBuilder.DATED_COMMENT);
        inputTextField.remove(ArticleQueryBuilder.PC_SPECIFICITY);
        
        // Remove PC specific criteria from Allocation field set
        inputTextField.remove(ArticleQueryBuilder.ALLOCATION);
        inputTextField.remove(ArticleQueryBuilder.USAGE);
        inputTextField.remove(ArticleQueryBuilder.ASSIGNMENT);
    }
    
    /**
     * Return the filter for installations
     * 
     * @return the requested filter object
     */    
    public Filter<?> getInstallationFilter() {
        return new Filter<Installation>() {
            public boolean accept(Installation item) {
            	return simuFilterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * Compute a list of Articles which property corresponding to the parameter
     * name satisfies the suggest value.
     * 
     * @param paramName
     *            the parameter name corresponding to the property on which to
     *            apply the provided criteria
     * @param suggest
     *            the suggest value corresponding to the applied criteria.
     *            When empty or null, it is replaced by "*", i.e. every value.
     * @return the list of found Articles
     */
    private List<? extends Article> suggestionsArticle(String paramName,
            String suggest) {
        
        String lSuggest = new String(suggest);
        
        if (StringUtil.isEmptyOrNull(lSuggest)) {
            lSuggest = "*";
        }
        
        List<? extends Article> results =
                searchBean.suggestionListArticle(paramName, lSuggest,
                        getFilteredInputTextField());
        
        return results;
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
    public List<String> suggestionsArticleAirbusPNs(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.AIRBUS_PN,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing CmsCode values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched CmsCode.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched CmsCode.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsArticleCmsCodes(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.CMS_CODE,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion data containing as Category the name of
     * the containerType and as Value the container name.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched container.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched container
     * @return the list of found suggestion values in SuggestionData form
     */
    public List<SuggestionData> suggestionsArticleContainers(
            FacesContext pContext, UIComponent pComponent, String pPrefix) {
        List<? extends Article> lArticles =
                suggestionsArticle(ArticleQueryBuilder.CONTAINER_NAME,
                        (String) pPrefix);
        
        SortedSet<SuggestionData> lSuggestionData =
                new TreeSet<SuggestionData>();
        
        for (Article lArticle : lArticles) {
            Container lContainer = lArticle.getContainer();
            
            if (lContainer != null) {
                lSuggestionData
                        .add(new SuggestionData(lContainer.getType().toString(),
                                lContainer.getContainerName()));
            }
        }
        
        return new ArrayList<SuggestionData>(lSuggestionData);
    }
    
    /**
     * Compute a list of suggestion values containing Article Designation
     * values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Article Designation.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Article Designation.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsArticleDesignations(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        Map<String, Object> lInputTextFieldCopy =
                new HashMap<String, Object>(inputTextField);
        
        lInputTextFieldCopy.put(ArticleQueryBuilder.FAMILY,
                Cabinet.class.getSimpleName());
        List<String> lDesignations =
                searchBean.suggestionListField(ArticleQueryBuilder.DESIGNATION,
                        (String) pPrefix, lInputTextFieldCopy);
        
        lInputTextFieldCopy.put(ArticleQueryBuilder.FAMILY,
                Rack.class.getSimpleName());
        lDesignations.addAll(searchBean.suggestionListField(
                ArticleQueryBuilder.DESIGNATION, (String) pPrefix,
                lInputTextFieldCopy));
        
        Collections.sort(lDesignations);
        
        return lDesignations;
    }
    
    /**
     * Compute a list of suggestion data containing as Category the name of
     * the externalEntityType and as Value the external entity name.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched external entity.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched external entity
     * @return the list of found suggestion values in SuggestionData form
     */
    public List<SuggestionData> suggestionsArticleExternalLocations(
            FacesContext pContext, UIComponent pComponent, String pPrefix) {
        
        Object[] lExternalEntityTypes = (Object[]) inputTextField
                        .get(ArticleQueryBuilder.EXTERNAL_LOCATION_TYPE);
        if (!Arrays.asList(lExternalEntityTypes).contains(0L)) {
            // since the external entity type does not need an external entity
            // name, the suggestion list is empty
            return Collections.emptyList();
        }
        
        List<? extends Article> lResults =
                suggestionsArticle(ArticleQueryBuilder.EXTERNAL_LOCATION_NAME,
                        (String) pPrefix);
        
        SortedSet<SuggestionData> list = new TreeSet<SuggestionData>();
        
        Iterator<? extends Article> iterator = lResults.iterator();
        while (iterator.hasNext()) {
            Location lLocation = iterator.next().getLocation();
            
            if (lLocation != null) {
                list.add(new SuggestionData(
                        lLocation.getExternalEntity().getExternalEntityType()
                                .getLocaleValue(),
                        lLocation.getExternalLocationName()));
            }
        }
        
        return new ArrayList<SuggestionData>(list);
    }
    
    /**
     * Compute a list of location name.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched location.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched location
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsArticleLocations(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        Object[] lLocationTypes = (Object[]) inputTextField
                .get(ArticleQueryBuilder.LOCATION_TYPE);
        if (!Arrays.asList(lLocationTypes).contains(true)) {
            // since the location type does not need a location name,
            // the suggestion list is empty
            return Collections.emptyList();
        }
        
        List<? extends Article> results =
                suggestionsArticle(ArticleQueryBuilder.LOCATION_NAME,
                        (String) pPrefix);
        
        SortedSet<String> list = new TreeSet<String>();
        
        Iterator<? extends Article> iterator = results.iterator();
        while (iterator.hasNext()) {
            Location lLocation = iterator.next().getLocation();
            if (lLocation != null) {
                list.add(lLocation.getLocationName());
            }
        }
        
        return new ArrayList<String>(list);
    }
    
    /**
     * Compute a list of suggestion values containing Manufacturer PN values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Manufacturer PN.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Manufacturer PN.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsArticleManufacturerPNs(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(
                ArticleQueryBuilder.MANUFACTURER_PN, (String) pPrefix,
                inputTextField);
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
    public List<SuggestionData> suggestionsArticleSNs(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        List<String> lAirbusSNs =
                searchBean.suggestionListField(ArticleQueryBuilder.AIRBUS_SN,
                        (String) pPrefix, inputTextField);
        List<String> lManufacturerSNs =
                searchBean.suggestionListField(
                        ArticleQueryBuilder.MANUFACTURER_SN, (String) pPrefix,
                        inputTextField);
        
        // initialize the returned list
        List<SuggestionData> lSuggestionDataList =
                new ArrayList<SuggestionData>();
        
        // fill the list with the sorted AirbusSN
        for (String lAirbusSN : lAirbusSNs) {
            lSuggestionDataList.add(new SuggestionData(MessageBundle
                    .getMessage("AirbusSN"), lAirbusSN));
        }
        // fill the list with the sorted ManufacturerSN
        for (String lManufacturerSN : lManufacturerSNs) {
            lSuggestionDataList.add(new SuggestionData(MessageBundle
                    .getMessage("ManufacturerSN"), lManufacturerSN));
        }
        
        return lSuggestionDataList;
    }
    
    /**
     * Compute a list of suggestion values containing Article Type values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Article Type.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Article Type.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsArticleTypes(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.TYPE,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing Default OS Distribution
     * values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Distribution.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software
     *            Distribution.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsDefaultOSDistributions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsDefaultOSValue(
                ArticleQueryBuilder.DEFAULT_OS_DISTRIBUTION, (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Default OS Kernel values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Kernel.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software Kernel.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsDefaultOSKernels(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsDefaultOSValue(ArticleQueryBuilder.DEFAULT_OS_KERNEL,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Default OS Name values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Name.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software Name.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsDefaultOSNames(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsDefaultOSValue(ArticleQueryBuilder.DEFAULT_OS_NAME,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of Software products property values (in String form).
     * Software are the ones which property corresponding to the parameter name
     * satisfies the suggest value.
     * 
     * @param pParamName
     *            the parameter name corresponding to the property on which to
     *            apply the provided criteria
     * @param pSuggest
     *            the suggest value corresponding to the applied criteria.
     *            When empty or null, it is replaced by "*", i.e. every value.
     * @return the list of found Software property values (in String form)
     */
    private List<String> suggestionsDefaultOSValue(String pParamName,
            String pSuggest) {
        
        List<? extends Article> lArticles =
                suggestionsArticle(pParamName, pSuggest);
        
        // Initialize filters
        SoftwareFilterRegex lFilter = new SoftwareFilterRegex();
        
        Map<String, String> lFilters = new HashMap<String, String>();
        
        String lSuggest = pSuggest;
        if (lSuggest == null || lSuggest.isEmpty()) {
            lSuggest = "*";
        }
        else if (lSuggest.indexOf('*') == -1) {
            lSuggest = "*" + lSuggest + "*";
        }
        
        // Add the suggest value to the filters
        if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_NAME)) {
            lFilters.put("name", lSuggest);
        }
        else if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_DISTRIBUTION)) {
            lFilters.put("distribution", lSuggest);
        }
        else if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_KERNEL)) {
            lFilters.put("kernel", lSuggest);
        }
        
        // Add the criteria values already entered
        
        String lInputName =
                (String) inputTextField
                        .get(ArticleQueryBuilder.DEFAULT_OS_NAME);
        if (lFilters.get("name") == null && lInputName != null) {
            lFilters.put("name", lInputName);
        }
        
        String lInputDistribution =
                (String) inputTextField
                        .get(ArticleQueryBuilder.DEFAULT_OS_DISTRIBUTION);
        if (lFilters.get("distribution") == null && lInputDistribution != null) {
            lFilters.put("distribution", lInputDistribution);
        }
        
        String lInputKernel =
                (String) inputTextField
                        .get(ArticleQueryBuilder.DEFAULT_OS_KERNEL);
        if (lFilters.get("kernel") == null && lInputKernel != null) {
            lFilters.put("kernel", lInputKernel);
        }
        
        lFilter.setFilterValues(lFilters);
        
        List<Software> lSoftwares = new ArrayList<Software>();
        
        // Initialize the list of Communication ports
        for (Article lCurrentArticle : lArticles) {
            
            if (lCurrentArticle instanceof PC) {
                PC lCurrentPC = (PC) lCurrentArticle;
                Software lDefaultOS = lCurrentPC.getDefaultOS();
                
                if (lDefaultOS != null && lFilter.filterMethodRegex(lDefaultOS)) {
                    lSoftwares.add(lDefaultOS);
                }
            }
        }
        
        // retrieve all values into a SortedSet in order to avoid duplicate
        // values and to sort them
        SortedSet<String> lFilteredValues = new TreeSet<String>();
        
        if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_NAME)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getName() != null) {
                    lFilteredValues.add(lCurrentSoftware.getName());
                }
            }
        }
        else if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_DISTRIBUTION)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getDistribution() != null) {
                    lFilteredValues.add(lCurrentSoftware.getDistribution());
                }
            }
        }
        else if (pParamName.equals(ArticleQueryBuilder.DEFAULT_OS_KERNEL)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getKernel() != null) {
                    lFilteredValues.add(lCurrentSoftware.getKernel());
                }
            }
        }
        
        // if the empty string has been added to the list, remove it
        lFilteredValues.remove("");
        
        List<String> lFinalValues = new ArrayList<String>();
        
        for (String lValue : lFilteredValues) {
            lFinalValues.add(lValue);
        }
        
        return lFinalValues;
    }
    
    /**
     * Compute a list of suggestion values containing PC Admin values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Admin.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Admin.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCAdmins(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.ADMIN,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC Assignment values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Assignment.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Assignment.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCAssignments(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.ASSIGNMENT,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC Function values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Function.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Function.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCFunctions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.FUNCTION,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC InCharge values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC InCharge.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searchedPC InCharge.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCInCharges(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        List<User> lUsers =
                searchBean.suggestionListField(
                        ArticleQueryBuilder.IN_CHARGE_LOGIN_DETAILS,
                        (String) pPrefix, inputTextField, User.class,
                        ArticleQueryBuilder.IN_CHARGE_USER);
        
        List<String> lUsersDetails = new ArrayList<String>();
        
        for (User lUser : lUsers) {
            lUsersDetails.add(lUser.getLoginDetails());
        }
        
        return lUsersDetails;
    }
    
    /**
     * Compute a list of suggestion values containing PC Name values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Name.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Name.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCNames(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.NAME,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC Owner values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Owner.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Owner.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCOwners(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.OWNER,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC Owner Siglum values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Owner Siglum.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Owner Siglum.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCOwnerSiglums(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.OWNER_SIGLUM,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing PC Platform values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched PC Platform.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched PC Platform.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPCPlatforms(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return searchBean.suggestionListField(ArticleQueryBuilder.PLATFORM,
                (String) pPrefix, inputTextField);
    }
    
    /**
     * Compute a list of suggestion values containing Communication port IP
     * address values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Communication port IP address.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Communication port IP
     *            Address.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPortIPAddresses(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsPortValue(ArticleQueryBuilder.IP_ADDRESS,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Communication port MAC
     * address values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Communication port MAC address.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Communication port
     *            MAC Address.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPortMACAddresses(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsPortValue(ArticleQueryBuilder.MAC_ADDRESS,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Communication port Name
     * values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Communication port Name.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Communication port
     *            Name.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPortNames(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsPortValue(ArticleQueryBuilder.PORT_NAME,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Communication port Socket
     * values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Communication port Socket.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Communication port
     *            Socket.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsPortSockets(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsPortValue(ArticleQueryBuilder.SOCKET,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of Communication Ports property values (in String form).
     * Ports are the ones which property corresponding to the parameter name
     * satisfies the suggest value.
     * 
     * @param pParamName
     *            the parameter name corresponding to the property on which to
     *            apply the provided criteria
     * @param pSuggest
     *            the suggest value corresponding to the applied criteria.
     *            When empty or null, it is replaced by "*", i.e. every value.
     * @return the list of found Ports property values (in String form)
     */
    private List<String>
            suggestionsPortValue(String pParamName, String pSuggest) {
        
        List<? extends Article> lArticles =
                suggestionsArticle(pParamName, pSuggest);
        
        // Initialize filters
        CommunicationPortFilterRegex lFilter =
                new CommunicationPortFilterRegex();
        
        Map<String, String> lFilters = new HashMap<String, String>();
        
        String lSuggest = pSuggest;
        if (lSuggest == null || lSuggest.isEmpty()) {
            lSuggest = "*";
        }
        else if (lSuggest.indexOf('*') == -1) {
            lSuggest = "*" + lSuggest + "*";
        }
        lFilters.put(pParamName, lSuggest);
        
        // Add criteria values already entered
        
        String lInputName =
                (String) inputTextField.get(ArticleQueryBuilder.PORT_NAME);
        if (!pParamName.equals(ArticleQueryBuilder.PORT_NAME)
                && lInputName != null) {
            lFilters.put(ArticleQueryBuilder.PORT_NAME, lInputName);
        }
        
        String lInputSocket =
                (String) inputTextField.get(ArticleQueryBuilder.SOCKET);
        if (!pParamName.equals(ArticleQueryBuilder.SOCKET)
                && lInputSocket != null) {
            lFilters.put(ArticleQueryBuilder.SOCKET, lInputSocket);
        }
        
        String lInputIPAddress =
                (String) inputTextField.get(ArticleQueryBuilder.IP_ADDRESS);
        if (!pParamName.equals(ArticleQueryBuilder.IP_ADDRESS)
                && lInputIPAddress != null) {
            lFilters.put(ArticleQueryBuilder.IP_ADDRESS, lInputIPAddress);
        }
        
        String lInputMACAddress =
                (String) inputTextField.get(ArticleQueryBuilder.MAC_ADDRESS);
        if (!pParamName.equals(ArticleQueryBuilder.MAC_ADDRESS)
                && lInputMACAddress != null) {
            lFilters.put(ArticleQueryBuilder.MAC_ADDRESS, lInputMACAddress);
        }
        
        lFilter.setFilterValues(lFilters);
        
        List<CommunicationPort> lCommunicationPorts =
                new ArrayList<CommunicationPort>();
        
        // Initialize the list of Communication ports
        for (Article lCurrentArticle : lArticles) {
            
            if (lCurrentArticle instanceof PC) {
                PC lCurrentPC = (PC) lCurrentArticle;
                
                for (CommunicationPort lCurrentPort : lCurrentPC.getPorts()) {
                    if (lFilter.filterMethodRegex(lCurrentPort)) {
                        lCommunicationPorts.add(lCurrentPort);
                    }
                }
            }
        }
        
        // retrieve all values into a SortedSet in order to avoid duplicate
        // values and to sort them
        SortedSet<String> lFilteredValues = new TreeSet<String>();
        
        if (pParamName.equals(ArticleQueryBuilder.PORT_NAME)) {
            for (CommunicationPort lCurrentPort : lCommunicationPorts) {
                if (lCurrentPort.getName() != null) {
                    lFilteredValues.add(lCurrentPort.getName());
                }
            }
        }
        
        else if (pParamName.equals(ArticleQueryBuilder.SOCKET)) {
            for (CommunicationPort lCurrentPort : lCommunicationPorts) {
                if (lCurrentPort.getSocket() != null) {
                    lFilteredValues.add(lCurrentPort.getSocket());
                }
            }
        }
        
        else if (pParamName.equals(ArticleQueryBuilder.IP_ADDRESS)) {
            for (CommunicationPort lCurrentPort : lCommunicationPorts) {
                if (lCurrentPort.getIpAddress() != null) {
                    lFilteredValues.add(lCurrentPort.getIpAddress());
                }
            }
        }
        
        else if (pParamName.equals(ArticleQueryBuilder.MAC_ADDRESS)) {
            for (CommunicationPort lCurrentPort : lCommunicationPorts) {
                if (lCurrentPort.getMacAddress() != null) {
                    lFilteredValues.add(lCurrentPort.getMacAddress());
                }
            }
        }
        
        // if the empty string has been added to the list, remove it
        lFilteredValues.remove("");
        
        List<String> lFinalValues = new ArrayList<String>();
        
        for (String lValue : lFilteredValues) {
            lFinalValues.add(lValue);
        }
        
        return lFinalValues;
    }
    
    /**
     * Compute a list of suggestion values containing Software Distribution
     * values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Distribution.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software
     *            Distribution.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareDistributions(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsSoftwareValue(
                ArticleQueryBuilder.SOFTWARE_DISTRIBUTION, (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Software Kernel values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Kernel.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software Kernel.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareKernels(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsSoftwareValue(ArticleQueryBuilder.SOFTWARE_KERNEL,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of suggestion values containing Software Name values.
     * Search is based on the provided suggest value (a String)
     * corresponding to the searched Software Name.
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent requesting suggestions
     * @param pPrefix
     *            the string corresponding to the searched Software Name.
     * @return the list of found suggestion values in String form
     */
    public List<String> suggestionsSoftwareNames(FacesContext pContext,
            UIComponent pComponent, String pPrefix) {
        
        return suggestionsSoftwareValue(ArticleQueryBuilder.SOFTWARE_NAME,
                (String) pPrefix);
    }
    
    /**
     * Compute a list of Software products property values (in String form).
     * Software are the ones which property corresponding to the parameter name
     * satisfies the suggest value.
     * 
     * @param pParamName
     *            the parameter name corresponding to the property on which to
     *            apply the provided criteria
     * @param pSuggest
     *            the suggest value corresponding to the applied criteria.
     *            When empty or null, it is replaced by "*", i.e. every value.
     * @return the list of found Software property values (in String form)
     */
    private List<String> suggestionsSoftwareValue(String pParamName,
            String pSuggest) {
        
        List<? extends Article> lArticles =
                suggestionsArticle(pParamName, pSuggest);
        
        // Initialize filters
        SoftwareFilterRegex lFilter = new SoftwareFilterRegex();
        
        Map<String, String> lFilters = new HashMap<String, String>();
        
        String lSuggest = pSuggest;
        if (lSuggest == null || lSuggest.isEmpty()) {
            lSuggest = "*";
        }
        else if (lSuggest.indexOf('*') == -1) {
            lSuggest = "*" + lSuggest + "*";
        }
        
        // Add the suggest value to the filters
        if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_NAME)) {
            lFilters.put("name", lSuggest);
        }
        else if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_DISTRIBUTION)) {
            lFilters.put("distribution", lSuggest);
        }
        else if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_KERNEL)) {
            lFilters.put("kernel", lSuggest);
        }
        
        // Add the criteria values already entered
        
        String lInputName =
                (String) inputTextField.get(ArticleQueryBuilder.SOFTWARE_NAME);
        if (lFilters.get("name") == null && lInputName != null) {
            lFilters.put("name", lInputName);
        }
        
        String lInputDistribution =
                (String) inputTextField
                        .get(ArticleQueryBuilder.SOFTWARE_DISTRIBUTION);
        if (lFilters.get("distribution") == null && lInputDistribution != null) {
            lFilters.put("distribution", lInputDistribution);
        }
        
        String lInputKernel =
                (String) inputTextField
                        .get(ArticleQueryBuilder.SOFTWARE_KERNEL);
        if (lFilters.get("kernel") == null && lInputKernel != null) {
            lFilters.put("kernel", lInputKernel);
        }
        
        lFilter.setFilterValues(lFilters);
        
        List<Software> lSoftwares = new ArrayList<Software>();
        
        // Initialize the list of Communication ports
        for (Article lCurrentArticle : lArticles) {
            
            if (lCurrentArticle instanceof PC) {
                PC lCurrentPC = (PC) lCurrentArticle;
                
                for (Software lCurrentSoftware : lCurrentPC.getSoftwares()) {
                    if (lFilter.filterMethodRegex(lCurrentSoftware)) {
                        lSoftwares.add(lCurrentSoftware);
                    }
                }
            }
        }
        
        // retrieve all values into a SortedSet in order to avoid duplicate
        // values and to sort them
        SortedSet<String> lFilteredValues = new TreeSet<String>();
        
        if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_NAME)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getName() != null) {
                    lFilteredValues.add(lCurrentSoftware.getName());
                }
            }
        }
        else if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_DISTRIBUTION)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getDistribution() != null) {
                    lFilteredValues.add(lCurrentSoftware.getDistribution());
                }
            }
        }
        else if (pParamName.equals(ArticleQueryBuilder.SOFTWARE_KERNEL)) {
            for (Software lCurrentSoftware : lSoftwares) {
                if (lCurrentSoftware.getKernel() != null) {
                    lFilteredValues.add(lCurrentSoftware.getKernel());
                }
            }
        }
        
        // if the empty string has been added to the list, remove it
        lFilteredValues.remove("");
        
        List<String> lFinalValues = new ArrayList<String>();
        
        for (String lValue : lFilteredValues) {
            lFinalValues.add(lValue);
        }
        
        return lFinalValues;
    }
    
    /**
     * Return the filter for tool container types
     * 
     * @return the requested filter object
     */    
    public Filter<?> getToolContainerTypeFilter() {
        return new Filter<Tool>() {
            public boolean accept(Tool item) {
            	return toolFilterRegex.filterMethodContainerType(item);
            }
        };
    }
    
    /**
     * Return the filter for tool external location types
     * 
     * @return the requested filter object
     */    
    public Filter<?> getToolExternalLocationTypeFilter() {
        return new Filter<Tool>() {
            public boolean accept(Tool item) {
            	return toolFilterRegex.filterMethodExternalLocationType(item);
            }
        };
    }
    
    /**
     * Return the filter for tools
     * 
     * @return the requested filter object
     */    
    public Filter<?> getToolFilter() {
        return new Filter<Tool>() {
            public boolean accept(Tool item) {
            	return toolFilterRegex.filterMethodRegex(item);
            }
        };
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
        
        CriteriaStructure lSearchCriteria =
                new CriteriaStructure(getFilteredInputTextField(), true);
        
        Long result = searchBean.searchArticleCount(lSearchCriteria, null);
        
        if (result == 0) {
            searchResultsMessageNone =
                    MessageBundle.getMessage(Constants.QUERY_NO_RESULTS);
            searchResultsMessageCount = "";
            searchResultsMessageSome = "";
        }
        else {
            searchResultsMessageNone = "";
            searchResultsMessageCount = result.toString();
            searchResultsMessageSome =
                    " "
                            + MessageBundle
                                    .getMessage("advancedSearchNbArticleFound");
        }
    }
    
    /**
     * @return the articleBean
     */
    public ArticleBean getArticleBean() {
        return articleBean;
    }
    
    /**
     * @return the articleDataModel
     */
    public ArticleDataModel getArticleDataModel() {
        return articleDataModel;
    }
    
    /**
     * @param pArticleDataModel
     *            the articleDataModel to set
     */
    public void setArticleDataModel(ArticleDataModel pArticleDataModel) {
        articleDataModel = pArticleDataModel;
    }
    
    /**
     * @return the possible availability status list
     */
    public SelectItem[] getAvailabilityStatusList() {
        int lLength = PC.AvailabilityStatus.values().length;
        SelectItem[] lStatus = new SelectItem[lLength];
        PC.AvailabilityStatus[] lValues = AvailabilityStatus.values();
        for (int i = 0; i < lLength; i++) {
            lStatus[i] = new SelectItem(lValues[i], lValues[i].toString());
        }
        
        return lStatus;
    }
    
    /**
     * Compute the classes
     * 
     * @return the classes
     */
    public SelectItem[] getClasses() {
        
        ArticleClass[] lValues = ArticleClass.values();
        
        int lLength = lValues.length;
        
        SelectItem[] lSelectItems = new SelectItem[lLength];
        
        int lIndex = 0;
        for (ArticleClass lValue : lValues) {
            
            lSelectItems[lIndex] =
                    new SelectItem(lValue, MessageBundle.getMessageResource(
                            lValue.toString(), null));
            lIndex++;
        }
        return lSelectItems;
    }
    
    /**
     * Compute the Container name tool tip in order to help the user
     * depending on the selected container type
     * 
     * @return the computed tool tip
     */
    public String getContainerNameToolTip() {
        String lToolTipKeyRoot = "searchToolTipContainerName";
        Object[] lContainerTypes = (Object[]) inputTextField
                .get(ArticleQueryBuilder.CONTAINER_TYPE);
        
        // ContainerName is to be disabled
        if (!isFiltered(ARTICLE_SEARCH, ArticleQueryBuilder.CONTAINER_TYPE)) {
            return MessageBundle.getMessage(lToolTipKeyRoot + "All");
        }
        else if (lContainerTypes.length == 0) {
            return MessageBundle.getMessage(lToolTipKeyRoot + "IgnoredField");
        }
        else{
            String lResult = "";
            for (Object lObject : lContainerTypes) {
                ContainerType lContainerType = (ContainerType) lObject;
                lResult +=
                        MessageBundle.getMessage(lContainerType.toString())
                                + ": ";
                lResult += MessageBundle
                        .getMessage(lToolTipKeyRoot + lContainerType.name());
                lResult += "<br/>";
            }
            return lResult.substring(0, lResult.length() - 5);
        }
    }
    
    /**
     * @return the containerTypes select items
     */
    public SelectItem[] getContainerTypes() {
        
        int lSize = ContainerType.values().length;
        SelectItem[] lContainerTypes = new SelectItem[lSize];
        ContainerType[] lValues = ContainerType.values();
        for (int i = 0; i < lSize; i++) {
            lContainerTypes[i] =
                    new SelectItem(lValues[i], lValues[i].toString());
        }
        return lContainerTypes;
    }
    
    /**
     * Update the countFilteredSimu with the number of installations filtered
     * 
     * @return the countFilteredSimu
     */
    public Integer getCountFilteredSimu() {
        countFilteredSimu = simuFilterRegex.countFiltered(foundInstallations);
        return countFilteredSimu;
    }
    
    /**
     * Update the countFilteredTool with the number of tools filtered
     * 
     * @return the countFilteredTool
     */
    public Integer getCountFilteredTool() {
        countFilteredTool = toolFilterRegex.countFiltered(foundTools);
        return countFilteredTool;
    }
    
    /**
     * @return the displayActiveStockColumn
     */
    public boolean isDisplayActiveStockColumn() {
        switch (displayMode) {
        case OnlyBoard:
            return true;
        case OnlyCabinet:
        case OnlyPC:
        case OnlyRack:
        case OnlySwitch:
        case OnlyVarious:
        case AnyArticle:
        default:
            return false;
        }
    }
    
    /**
     * @return the displayArticleExceptPCCriteria
     */
    public boolean isDisplayArticleExceptPCCriteria() {
        return displayArticleExceptPCCriteria;
    }
    
    /**
     * @return the displayDesignationColumn
     */
    public boolean isDisplayDesignationColumn() {
        switch (displayMode) {
        case OnlyRack:
        case OnlyCabinet:
            return true;
        case OnlyBoard:
        case OnlyPC:
        case OnlySwitch:
        case OnlyVarious:
        case AnyArticle:
        default:
            return false;
        }
    }
    
    /**
     * @return true if the edition is available, else false
     */
    public boolean isDisplayEditionColumn() {
        switch (searchMode) {
        case AdvancedSearch:
        case BoardList:
        case CabinetList:
        case PCList:
        case RackList:
        case SwitchList:
        case VariousList:
        case OtherList:
            return true;
        case DisplayResultsAll:
        default:
            return false;
        }
    }
    
    /**
     * @return the displayFamilyColumn
     */
    public boolean isDisplayFamilyColumn() {
        switch (displayMode) {
        case AnyArticle:
            return true;
        case OnlyBoard:
        case OnlyCabinet:
        case OnlyPC:
        case OnlyRack:
        case OnlySwitch:
        case OnlyVarious:
        default:
            return false;
        }
    }
    
    /**
     * @return the displayNameColumn
     */
    public boolean isDisplayNameColumn() {
        switch (displayMode) {
        case OnlyPC:
        case AnyArticle:
            return true;
        case OnlyBoard:
        case OnlyCabinet:
        case OnlyRack:
        case OnlySwitch:
        case OnlyVarious:
        default:
            return false;
        }
    }
    
    /**
     * @return the displayPCColumns
     */
    public boolean isDisplayPCColumns() {
        switch (displayMode) {
        case OnlyPC:
            return true;
        case OnlyBoard:
        case OnlyCabinet:
        case OnlyRack:
        case OnlySwitch:
        case OnlyVarious:
        case AnyArticle:
        default:
            return false;
        }
    }
    
    /**
     * @return the displayPCCriteria
     */
    public boolean isDisplayPCCriteria() {
        return displayPCCriteria;
    }
    
    /**
     * @return the displayResultsTitle
     */
    public String getDisplayResultsTitle() {
        return displayResultsTitle;
    }
    
    /**
     * @param displayResultsTitle
     *            the displayResultsTitle to set
     */
    public void setDisplayResultsTitle(String displayResultsTitle) {
        this.displayResultsTitle = displayResultsTitle;
    }
    
    /**
     * @return the foundInstallations
     */
    public List<Installation> getFoundInstallations() {
        return foundInstallations;
    }
    
    /**
     * @return the foundTools
     */
    public List<Tool> getFoundTools() {
        return foundTools;
    }
    
    /**
     * @return the inputTextField
     */
    public Map<String, Object> getInputTextField() {
        return inputTextField;
    }
    
    /**
     * @return the filtered inputTextField
     */
    public Map<String, Object> getFilteredInputTextField() {
        Map<String, Object> lFilteredInputTextField =
                new HashMap<String, Object>(inputTextField);
        for (String lKey : filterChoices.keySet()) {
            if (lKey.equals(ArticleQueryBuilder.USE_STATE)) {
                if (getFilteredValues(ARTICLE_SEARCH, lKey).length == UseState
                        .values().length) {
                    lFilteredInputTextField.remove(lKey);
                }
            }
            else if (!isFiltered(ARTICLE_SEARCH, lKey)) {
                lFilteredInputTextField.remove(lKey);
            }
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
     * Initialize the attributes in order to display the list of installations
     * into the installations list page
     * 
     * @param pInstallations
     *            the installations list to display
     */
    public void setInstallationsToDisplay(List<Installation> pInstallations) {
        searchMode = SearchMode.DisplayResultsAll;
        foundInstallations = pInstallations;
    }
    
    /**
     * @return the locationBean
     */
    public LocationBean getLocationBean() {
        return locationBean;
    }
    
    /**
     * @return the types
     */
    public List<String> getPCTypes() {
        
        List<TypeArticle> result = articleBean.findAllTypeArticle(new TypePC());
        
        List<String> lTypes = new ArrayList<String>();
        for (TypeArticle type : result) {
            lTypes.add(type.getLabel());
        }
        return lTypes;
        
    }
    
    /**
     * @return true if the type should be displayed for Article External
     *         Location name list
     */
    public boolean isRenderedSuggestArticleExternalLocationType() {
        Object lExternalLocationType =
                inputTextField.get(ArticleQueryBuilder.EXTERNAL_LOCATION_TYPE);
        return lExternalLocationType == null
                || (lExternalLocationType instanceof String && lExternalLocationType
                        .equals("all"));
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
     * @return the simuFilterValues
     */
    public Map<String, String> getSimuFilterValues() {
        return simuFilterRegex.getFilterValues();
    }
    
    /**
     * @param simuFilterValues
     *            the simuFilterValues to set
     */
    public void setSimuFilterValues(Map<String, String> simuFilterValues) {
        simuFilterRegex.setFilterValues(simuFilterValues);
    }
    
    /**
     * Update the states
     * 
     * @return the states
     */
    public SelectItem[] getStates() {
        int lLength = ArticleState.values().length;
        SelectItem[] lStates = new SelectItem[lLength];
        ArticleState[] lValues = ArticleState.values();
        for (int i = 0; i < lLength; i++) {
            lStates[i] = new SelectItem(lValues[i], lValues[i].toString());
        }
        
        return lStates;
    }
    
    /**
     * @return the use states
     */
    public List<SelectItem> getUseStates() {
        List<SelectItem> lSelectUseState = new ArrayList<SelectItem>();
        
        LogInController lLoginController = findBean(LogInController.class);
        
        for (UseState lState : UseState.values()) {
            if ((lState == UseState.Archived && lLoginController
                    .isAuthorized(RightMaskAction.ArchivedCRUDAuthorization))
                    || lState != UseState.Archived) {
                lSelectUseState.add(new SelectItem(lState, lState.toString()));
            }
        }
        return lSelectUseState;
    }
    
    /**
     * @return the toolContainerTypes select items
     */
    public SelectItem[] getToolContainerTypes() {
        
        SelectItem[] lToolContainerTypes = new SelectItem[1];
        lToolContainerTypes[0] =
                new SelectItem(ContainerType.Installation,
                        ContainerType.Installation.toString());
        return lToolContainerTypes;
    }
    
    /**
     * @return the toolExternalLocationTypes select items
     */
    public List<SelectItem> getToolExternalLocationTypes() {
        return filterChoices.get(ArticleQueryBuilder.EXTERNAL_LOCATION_TYPE);
    }
    
    /**
     * @return the toolFilterContainerType
     */
    public String getToolFilterContainerType() {
        return toolFilterRegex.getFilterContainerType();
    }
    
    /**
     * @param pToolFilterContainerType
     *            the toolFilterContainerType to set
     */
    public void setToolFilterContainerType(String pToolFilterContainerType) {
        toolFilterRegex.setFilterContainerType(pToolFilterContainerType);
    }
    
    /**
     * @return the toolFilterExternalLocationType
     */
    public String getToolFilterExternalLocationType() {
        return toolFilterRegex.getFilterExternalLocationType();
    }
    
    /**
     * @param pToolFilterExternalLocationType
     *            the toolFilterExternalLocationType to set
     */
    public void setToolFilterExternalLocationType(
            String pToolFilterExternalLocationType) {
        toolFilterRegex
                .setFilterExternalLocationType(pToolFilterExternalLocationType);
    }
    
    /**
     * @return the toolFilterValues
     */
    public Map<String, String> getToolFilterValues() {
        return toolFilterRegex.getFilterValues();
    }
    
    /**
     * @param pToolFilterValues
     *            the toolFilterValues to set
     */
    public void setToolFilterValues(Map<String, String> pToolFilterValues) {
        toolFilterRegex.setFilterValues(pToolFilterValues);
    }
    
    /**
     * Initialize the attributes in order to display the list of tools into the
     * tools list page
     * 
     * @param pTools
     *            the tools list to display
     */
    public void setToolsToDisplay(List<Tool> pTools) {
        searchMode = SearchMode.DisplayResultsAll;
        foundTools = pTools;
    }
    
    /**
     * @return the filterChoices
     */
    public Map<String, List<SelectItem>> getFilterChoices() {
        return filterChoices;
    }
    
    /**
     * @param pSource
     *            String representing the component for which we want the filter
     *            choices
     *            (the search form or the data model)
     * @param pField
     *            the field name for which we want the filter choices
     * @return the filter choices for the given source and field
     */
    public List<SelectItem> getFilterChoices(String pSource,
            String pField) {
        if (pSource.equals(ARTICLE_SEARCH)) {
            return filterChoices.get(pField);
        }
        else if (pSource.equals(DATA_MODEL)) {
            return articleDataModel.getFilterChoices().get(pField);
        }
        else {
            return new ArrayList<SelectItem>();
        }
    }
    
    /**
     * @param pField
     *            the field name for which we want the choice values
     * @return the choice values for the given field in the search form
     */
    public Object[] getChoicesValues(String pField) {
        return getChoicesValues(ARTICLE_SEARCH, pField);
    }
    
    /**
     * @param pSource
     *            String representing the component for which we want the choice
     *            values (the search form or the data model)
     * @param pField
     *            the field name for which we want the choice values
     * @return the choice values for the given source and field
     */
    public Object[] getChoicesValues(String pSource, String pField) {
        List<SelectItem> lList = getFilterChoices(pSource, pField);
        int lSize = lList.size();
        Object[] lValues = new Object[lSize];
        for (int i = 0; i < lList.size(); i++) {
            lValues[i] = lList.get(i).getValue();
        }
        return lValues;
    }
    
    /**
     * @param pSource
     *            String representing the component for which we want the
     *            filtered
     *            values (the search form or the data model)
     * @param pField
     *            the field name for which we want the filtered values
     * @return the filtered values for the given source and field
     */
    public Object[] getFilteredValues(String pSource, String pField) {
        if (pSource.equals(ARTICLE_SEARCH)) {
            return (Object[]) inputTextField.get(pField);
        }
        else if (pSource.equals(DATA_MODEL)) {
            return (Object[]) articleDataModel.getSelectionFilterMap()
                    .get(pField);
        }
        else {
            return null;
        }
    }
    
    /**
     * Check if the given field is filtered in the given source
     * 
     * @param pSource
     *            String representing the component for which we the test shall
     *            be done (the search form or the data model)
     * @param pField
     *            the field name for which the test shall be done
     * @return true if the field is filtered, false otherwise
     */
    public boolean isFiltered(String pSource, String pField) {
        Object[] lFilteredValues = getFilteredValues(pSource, pField);
        Object[] lChoicesValues = getChoicesValues(pSource, pField);
        if (lFilteredValues != null
                && lFilteredValues.length < lChoicesValues.length) {
            return true;
        }
        return false;
    }
}
