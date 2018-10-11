/*
 * ------------------------------------------------------------------------
 * Class : StockController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.stock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.richfaces.component.AbstractDataTable;
import org.richfaces.event.ItemChangeEvent;
import org.richfaces.model.Filter;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.user.StockSelection;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.stock.IOExcelStockForm;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ExportController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.filter.TypeFilterRegex;

/**
 * General stocks controller
 */
@ManagedBean(name = StockController.BEAN_NAME)
@SessionScoped
public class StockController extends AbstractController {
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "stockController";

    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private UserBean userBean;
    
    /*
     * Input fields
     */
    private String inputType;
    private String inputAPN;
    private String inputMPN;
    
    /*
     * Display filters
     */
    private TypeFilterRegex filterRegex = new TypeFilterRegex();
    
    private String activeTab = NavigationConstants.STOCK_EVOL_TAB_ID;
    
    private List<StockForType> stocks = new ArrayList<StockForType>();
    private List<StockForPN> stockPNs = new ArrayList<StockForPN>();
    private Stock selectedStock;
    private String selectedTypeTitle;
    private Collection<Object> selectedTypes;
    private Collection<Object> selectedPNs;
    
    private List<TypeArticle> typesFound = new ArrayList<TypeArticle>();
    private List<TypeArticle> typesSelected = new ArrayList<TypeArticle>();
    
    private Date stockEvolutionFrom;
    private Date stockEvolutionTo;
    
    private String stockSelectionName;
    private StockSelection stockSelection;
    private List<StockSelection> stockSelections =
            new ArrayList<StockSelection>();
    
    /**
     * Default constructor, initializing the current date
     */
    public StockController() {
        stockEvolutionTo = new Date();
    }
    
    /**
     * Compute variables quantityInStock and quantityInUse, for an article type
     * 
     * @param type
     *            article type
     */
    public void computeStock(TypeArticle pType) {
        StockForType lStock =
                new StockForType(articleBean, historyBean, locationBean, pType);
        
        if (!(pType instanceof TypePC)) {
            // Article different from PC
            // Search all PN (Airbus/Manufacturer) associated to this type
            List<PN> lPNs = articleBean.findAllPNbyTypeArticleId(pType.getId());
            List<StockForPN> lStockPNs = new ArrayList<StockForPN>();
            for (PN lPN : lPNs) { // Store quantities for each PN
                StockForPN lStockPN =
                        new StockForPN(articleBean, historyBean, locationBean,
                                pType, lPN);
                lStockPNs.add(lStockPN);
            }
            lStock.setStockPNs(lStockPNs);
        }
        
        // Store results
        stocks.add(lStock);
    }
    
    /**
     * Prepare the bean to save a stock selection
     */
    public void doPrepareSaveSelection() {
        validateStockSelectionName(stockSelectionName);
    }
    
    /**
     * Save the created stock selection in the database
     */
    public void doSaveSelection() {
        LogInController lLogInController = findBean(LogInController.class);
        User lLoggedUser = lLogInController.getUserLogged();
        
        StockSelection lStockSelection = new StockSelection();
        lStockSelection.setName(stockSelectionName);
        lStockSelection.setSelectedTypes(typesSelected);
        lStockSelection.setUser(lLoggedUser);
        
        stockSelection =
                userBean.saveStockSelection(lStockSelection);
        lLogInController.reloadUserLogged();
        stockSelections = lLoggedUser.getStockSelections();
    }
    
    /**
     * Update the stockSelection according to the selected one
     * 
     * @param pEvent
     *            the event sent while the selected name in the list is changed
     */
    public void stockSelectionChanged(ValueChangeEvent pEvent) {
        if (pEvent.getNewValue() != null) {
            stockSelection = (StockSelection) pEvent.getNewValue();
        }
    }
    
    /**
     * Prepare the bean to load a stock selection
     */
    public void doPrepareLoadSelection() {
        LogInController lLogInController = findBean(LogInController.class);
        User lLoggedUser = lLogInController.getUserLogged();
        stockSelections = lLoggedUser.getStockSelections();
    }
    
    /**
     * Load the selected stock selection from the database
     */
    public void doLoadSelection() {
        if (stockSelection != null) {
            stockSelectionName = stockSelection.getName();
            typesSelected = stockSelection.getSelectedTypes();
            
            Set<TypeArticle> lSet = new HashSet<TypeArticle>();
            lSet.addAll(typesFound);
            lSet.addAll(typesSelected);
            typesFound = new ArrayList<>(lSet);
        }
    }
    
    /**
     * Delete the selected stock selection from the database
     */
    public void doDeleteSelection() {
        if (stockSelection != null) {
            userBean.deleteStockSelection(stockSelection);
            stockSelection = null;
            
            LogInController lLogInController = findBean(LogInController.class);
            lLogInController.reloadUserLogged();
            User lLoggedUser = lLogInController.getUserLogged();
            stockSelections = lLoggedUser.getStockSelections();
        }
    }
    
    /**
     * Search a type in the database according to the given input search
     * criteria
     */
    public void doSearch() {
        
        Set<TypeArticle> lSet = new HashSet<TypeArticle>();
        
        if (inputType != null && !inputType.isEmpty()) {
            // Recherche des types correspondant au critère de recherche de
            // l'utilisateur
            List<TypeArticle> listTypes =
                    articleBean.findAllTypeArticleByName(inputType);
            lSet.addAll(listTypes);
        }
        
        if (inputAPN != null && !inputAPN.isEmpty()) {
            // Recherche des PN Airbus correspondant au critère de recherche de
            // l'utilisateur
            List<PN> listAPN = articleBean.suggestionListAirbusPN(inputAPN);
            if (listAPN != null) {
                for (PN pn : listAPN) {
                    // Recherche des types associés au PN (normalement 1 seul)
                    List<TypeArticle> listTypeArticlePN =
                            articleBean.findAllTypeArticleForPN(pn);
                    lSet.addAll(listTypeArticlePN);
                }
            }
        }
        if (inputMPN != null && !inputMPN.isEmpty()) {
            // Recherche des PN Fabricant correspondant au critère de recherche
            // de l'utilisateur
            List<PN> listMPN =
                    articleBean.suggestionListManufacturerPN(inputMPN);
            if (listMPN != null) {
                for (PN pn : listMPN) {
                    // Recherche des types associés au PN
                    List<TypeArticle> listTypeArticlePN =
                            articleBean.findAllTypeArticleForPN(pn);
                    lSet.addAll(listTypeArticlePN);
                }
            }
        }
        
        lSet.addAll(typesSelected);
        
        typesFound = new ArrayList<TypeArticle>(lSet);
        
    }
    
    /**
     * Compute the stock for the selected types
     */
    public void doComputeStock() {
        // Liste des stocks pour tous les types
        stocks = new ArrayList<StockForType>();
        stockPNs = new ArrayList<StockForPN>();
        selectedStock = null;
        selectedTypeTitle = "";
        selectedTypes = new HashSet<>();
        selectedPNs = new HashSet<>();
        
        for (TypeArticle type : typesSelected) {
            // Calcul du stock pour le type et chacun de ses PN
            computeStock(type);
        }
    }
    
    /**
     * Export the stock computation in an Excel file
     */
    public void doExport() throws ExportException, IOException {
        
        if (!stocks.isEmpty()) {
            List<StockForType> results = new ArrayList<StockForType>();
            
            // parcours des types, résultats de recherche
            for (StockForType stock : stocks) {
                if (filterRegex.filterMethodRegex(stock.getType())) {
                    // Type conservé après le filtre,
                    // ajout à la la liste des types à exporter
                    results.add(stock);
                }
            }
            
            // Une feuille, donc un writer, par type d'article
            Map<String, IOExcelStockForm> mapWriter =
                    new HashMap<String, IOExcelStockForm>();
            InputStream lInputStream = FacesContext.getCurrentInstance()
                    .getExternalContext().getResourceAsStream(
                            IOExcelStockForm.TEMPLATE_WORKBOOK_PATH);
            Workbook workbook = new XSSFWorkbook(lInputStream);
            IOExcelStockForm stockSheet = null;
            
            for (StockForType lStock : results) {
                String lKey = StringUtil
                        .convertStringForSheetName(lStock.getType().getLabel());
                stockSheet =
                        new IOExcelStockForm(lKey, workbook);
                mapWriter.put(lKey, stockSheet);
                // Ecriture dans la feuille
                stockSheet.write(lStock);
            }
                
                // Application du style à chaque feuille
            // for (IOExcelStockForm sheet : mapWriter.values()) {
            // sheet.applyStyles();
            // }
                
            download(stockSheet, "stocks.xlsx", ExportController.MIMETYPE_XLSX);
        }
    }
    
    /**
     * Reset the type search criteria
     */
    public void doReset() {
        inputType = null;
        inputAPN = null;
        inputMPN = null;
        typesFound = new ArrayList<>(typesSelected);
    }
    
    /**
     * Display the PN list and the stock computations of the selected type
     * 
     * @param pEvent
     *            the event sent while the selected type in the list is changed
     */
    public void doSelectStock(AjaxBehaviorEvent pEvent) {
        AbstractDataTable dataTable = (AbstractDataTable) pEvent.getComponent();
        selectedStock = (Stock) dataTable.getRowData();
        if (selectedStock instanceof StockForType) {
            selectedTypeTitle =
                    ((StockForType) selectedStock).getType().getLabel();
            stockPNs = ((StockForType) selectedStock).getStockPNs();
            selectedPNs = new HashSet<>();
            selectedTypes.clear();
            selectedTypes.add(dataTable.getRowKey());
        }
        else {
            selectedPNs.clear();
            selectedPNs.add(dataTable.getRowKey());
        }
    }
    
    /**
     * Checks the validity of the given selection name
     * 
     * @param pContext
     *            FacesContext for the request we are processing
     * @param pComponent
     *            UIComponent we are checking for correctness
     * @param pValue
     *            the value to validate
     */
    public void validateStockSelectionName(FacesContext pContext,
            UIComponent pComponent, Object pValue) throws ValidatorException {
        
        String lName = (String) pValue;
        lName = (lName == null) ? "" : lName.trim();
        
        validateStockSelectionName(lName);
    }
    
    /**
     * Checks the validity of the given selection name
     * 
     * @param pName
     *            the name to validate
     */
    public void validateStockSelectionName(String pName)
            throws ValidatorException {
        LogInController lLogInController = findBean(LogInController.class);
        User lLoggedUser = lLogInController.getUserLogged();
        
        if (userBean.existStockSelection(lLoggedUser, pName)) {
            Utils.addWarningMessage(
                    NavigationConstants.SAVE_STOCK_SELECTION_ERROR_ID,
                    MessageBundle.getMessage(
                            Constants.STOCK_SELECTION_NAME_ALREADY_USED));
        }
    }
    
    /**
     * Return the filter for stock management
     * 
     * @return the requested filter object
     */
    public Filter<?> getStockFilter() {
        return new Filter<StockForType>() {
            
            public boolean accept(StockForType item) {
                return filterRegex.filterMethodRegex(item.getType());
            }
        };
    }
    
    /**
     * Return the filtered values
     * 
     * @return the map associating a column and its filtered value
     */
    public Map<String, String> getFilterValues() {
        return filterRegex.getFilterValues();
    }
    
    /**
     * Set the filtered values
     * 
     * @param pFilterValues
     *            a map associating a column and its filtered value
     */
    public void setFilterValues(Map<String, String> pFilterValues) {
        filterRegex.setFilterValues(pFilterValues);
    }
    
    /**
     * @return the inputAPN
     */
    public String getInputAPN() {
        return inputAPN;
    }
    
    /**
     * @param pInputAPN
     *            the inputAPN to set
     */
    public void setInputAPN(String pInputAPN) {
        this.inputAPN = pInputAPN;
    }
    
    /**
     * @return the inputMPN
     */
    public String getInputMPN() {
        return inputMPN;
    }
    
    /**
     * @param pInputMPN
     *            the inputMPN to set
     */
    public void setInputMPN(String pInputMPN) {
        this.inputMPN = pInputMPN;
    }
    
    /**
     * @return the inputType
     */
    public String getInputType() {
        return inputType;
    }
    
    /**
     * @param pInputType
     *            the inputType to set
     */
    public void setInputType(String pInputType) {
        this.inputType = pInputType;
    }
    
    /**
     * @return the selectedTypeTitle
     */
    public String getSelectedTypeTitle() {
        return selectedTypeTitle;
    }
    
    /**
     * @param pSelectedTypeTitle
     *            the selectedTypeTitle to set
     */
    public void setSelectedTypeTitle(String pSelectedTypeTitle) {
        this.selectedTypeTitle = pSelectedTypeTitle;
    }
    
    /**
     * @return the list of computed stocks for the selected article types
     */
    public List<StockForType> getStocks() {
        return stocks;
    }
    
    /**
     * @return the list of computed stocks for the selected PNs
     */
    public List<StockForPN> getStockPNs() {
        return stockPNs;
    }

    /**
     * @return the list of article types found by the search
     */
    public List<TypeArticle> getTypesFound() {
        return typesFound;
    }

    /**
     * @param pTypesFound
     *            the list of found type articles to set
     */
    public void setTypesFound(List<TypeArticle> pTypesFound) {
        typesFound = pTypesFound;
    }

    /**
     * @return the list of selected article types from the search
     */
    public List<TypeArticle> getTypesSelected() {
        return typesSelected;
    }

    /**
     * @param pTypesFound
     *            the list of selected article types from the search to set
     */
    public void setTypesSelected(List<TypeArticle> pTypesSelected) {
        typesSelected = pTypesSelected;
    }
    
    /**
     * @return the list of selected stock
     */
    public Stock getSelectedStock() {
        return selectedStock;
    }

    /**
     * @return the stock evolution origin date
     */
    public Date getStockEvolutionFrom() {
        return stockEvolutionFrom;
    }

    /**
     * @param pStockEvolutionFrom
     *            the stock evolution origin date to set
     */
    public void setStockEvolutionFrom(Date pStockEvolutionFrom) {
        stockEvolutionFrom = pStockEvolutionFrom;
    }

    /**
     * @return the stock evolution target date
     */
    public Date getStockEvolutionTo() {
        return stockEvolutionTo;
    }

    /**
     * @param pStockEvolutionTo
     *            the stock evolution target date to set
     */
    public void setStockEvolutionTo(Date pStockEvolutionTo) {
        stockEvolutionTo = pStockEvolutionTo;
    }

    /**
     * @return the active tab
     */
    public String getActiveTab() {
        return activeTab;
    }

    /**
     * @param pActiveTab
     *            the active tab to set
     */
    public void setActiveTab(String pActiveTab) {
        activeTab = pActiveTab;
    }
    
    /**
     * Tab change listener
     * 
     * @param pEvent
     *            the event sent while the active tab is changed
     */
    public void tabChangeListener(ItemChangeEvent pEvent) {
        activeTab = pEvent.getNewItemName();
    }
    
    /**
     * @return the list of selected article types in the stock section of the
     *         page
     */
    public Collection<Object> getSelectedTypes() {
        return selectedTypes;
    }
    
    /**
     * @param pTypesFound
     *            the list of selected article types in the stock section of the
     *            page to set
     */
    public void setSelectedTypes(Collection<Object> pSelectedTypes) {
        selectedTypes = pSelectedTypes;
    }
    
    /**
     * @return the list of selected PNs in the stock section of the
     *         page
     */
    public Collection<Object> getSelectedPNs() {
        return selectedPNs;
    }
    
    /**
     * @param pTypesFound
     *            the list of selected PNs in the stock section of the
     *            page to set
     */
    public void setSelectedPNs(Collection<Object> pSelectedPNs) {
        selectedPNs = pSelectedPNs;
    }
    
    /**
     * @return the number of breakdowns in the selected period, for the selected
     *         stock
     */
    public Integer getBreakdownsNumber() {
        return selectedStock.getBreakdownsNumber(stockEvolutionFrom,
                stockEvolutionTo);
    }
    
    /**
     * @return the number of scrappings in the selected period, for the selected
     *         stock
     */
    public Integer getScrapingsNumber() {
        return selectedStock.getScrapingsNumber(stockEvolutionFrom,
                stockEvolutionTo);
    }

    /**
     * @return the stockSelectionName
     */
    public String getStockSelectionName() {
        return stockSelectionName;
    }

    /**
     * @param pStockSelectionName
     *            the stockSelectionName to set
     */
    public void setStockSelectionName(String pStockSelectionName) {
        stockSelectionName = pStockSelectionName;
    }

    /**
     * @return the stockSelection
     */
    public StockSelection getStockSelection() {
        return stockSelection;
    }

    /**
     * @param pStockSelection
     *            the stockSelection to set
     */
    public void setStockSelection(StockSelection pStockSelection) {
        stockSelection = pStockSelection;
    }
    
    /**
     * @return the stockSelections
     */
    public List<StockSelection> getStockSelections() {
        return stockSelections;
    }
}
