/*
 * ------------------------------------------------------------------------
 * Class : ArticleDataModel
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.richfaces.model.ArrangeableState;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.SearchBean;
import com.airbus.boa.control.query.ArticleQueryBuilder;
import com.airbus.boa.control.query.CriteriaStructure;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.util.StringUtil;

/**
 * Class defining the data model for articles pagination in datatables
 */
public class ArticleDataModel extends DataModel<Article> {
    
    private ArticleBean articleBean;
    
    private SearchBean searchBean;
    
    /** Map of criteria for searching articles (before displaying the datatable) */
    private Map<String, Object> initialFilterMap;
    
    /**
     * Map of choices for filtering articles (for the checkbox menus)
     */
    private Map<String, List<SelectItem>> filterChoices;
    
    /**
     * True if initial filters have to be combined with an AND, false for
     * combining with an OR
     */
    private boolean initialAnd;
    
    /**
     * Pre-formated conditions to add to the WHERE clause of the query.<br>
     * These conditions are combined with the initial conditions (obtained from
     * initial criteria) with an AND.
     */
    private List<String> autoConditions;
    
    /** Map of criteria for filling the automatic conditions */
    private Map<String, Object> autoFilterMap;
    
    /** Complement for the map of criteria for filtering the datatable */
    private Map<String, Object> selectionFilterMap;
    
    /** Language code for filtering according to the current locale */
    private String languageCode = null;
    
    /**
     * Constructor
     * 
     * @param pArticleBean
     *            the articleBean to use
     * @param pSearchBean
     *            the searchBean to use
     * @param pLanguageCode
     *            the languageCode
     */
    public ArticleDataModel(ArticleBean pArticleBean, SearchBean pSearchBean,
            String pLanguageCode) {
        
        articleBean = pArticleBean;
        searchBean = pSearchBean;
        
        languageCode = pLanguageCode;
        
        initialFilterMap = new HashMap<String, Object>();
        initialAnd = true;
        
        autoConditions = new ArrayList<String>();
        autoFilterMap = new HashMap<String, Object>();
        
        selectionFilterMap = new HashMap<String, Object>();
    }
    
    @Override
    public void arrange(FacesContext context, ArrangeableState state) {
        
        Map<String, Object> lPreviousFilterMap =
                new HashMap<String, Object>(filterMap);
        
        super.arrange(context, state);
        
        for (String lParameter : selectionFilterMap.keySet()) {
            
            Object lSelectionFilter = selectionFilterMap.get(lParameter);
            if (lSelectionFilter instanceof String) {
                String lStringFilter = (String) lSelectionFilter;
                if (!StringUtil.isEmptyOrNull(lStringFilter)) {
                    putFilterInMap(lParameter, lSelectionFilter);
                }
            }
            else if (lSelectionFilter != null) {
                putFilterInMap(lParameter, lSelectionFilter);
            }
        }
        
        if (!filterMap.equals(lPreviousFilterMap)) {
            setFilterInMapChanged();
        }
    }
    
    @Override
    public List<Article> findEntities(int pFirstRow, int pNumberOfRows) {
        
        CriteriaStructure lSearchCriteria =
                new CriteriaStructure(initialFilterMap, initialAnd,
                        autoConditions, autoFilterMap);
        
        CriteriaStructure lFilterCriteria =
                new CriteriaStructure(getFilteredFilterMap(), true);
        
        return searchBean.searchArticle(pFirstRow, pNumberOfRows,
                lSearchCriteria, lFilterCriteria, sortField, ordering,
                languageCode);
    }
    
    /**
     * Reset the filters for filtering the datatable
     */
    public void resetFilters() {
        
        if (initialAnd) {
            selectionFilterMap = new HashMap<String, Object>(initialFilterMap);
        }
        else {
            selectionFilterMap = new HashMap<String, Object>();
        }
        
        for (String lKey : filterChoices.keySet()) {
            if (!selectionFilterMap.containsKey(lKey)) {
                // For the use state filter, do not select the Archived value
                if (lKey.equals(ArticleQueryBuilder.USE_STATE)) {
                    Object[] lOldChoicesValues = getChoicesValues(lKey);
                    List<Object> lChoicesList =
                            new ArrayList<>(Arrays.asList(lOldChoicesValues));
                    lChoicesList.remove(UseState.Archived);
                    Object[] lChoicesValues = new Object[lChoicesList.size()];
                    lChoicesValues = lChoicesList.toArray(lChoicesValues);
                    selectionFilterMap.put(lKey, lChoicesValues);
                }
                else {
                    selectionFilterMap.put(lKey, getChoicesValues(lKey));
                }
            }
        }
        
        arrange(null, null);
    }
    
    /**
     * @param pAutoConditions
     *            the autoConditions to set
     */
    public void setAutoConditions(List<String> pAutoConditions) {
        autoConditions = pAutoConditions;
    }
    
    /**
     * @param pAutoFilterMap
     *            the autoFilterMap to set
     */
    public void setAutoFilterMap(Map<String, Object> pAutoFilterMap) {
        autoFilterMap = pAutoFilterMap;
    }
    
    @Override
    protected String getDefaultSortField() {
        return ArticleQueryBuilder.AIRBUS_SN;
    }
    
    @Override
    protected Article getEntityById(Long pId) {
        
        return articleBean.findArticleById(pId);
    }
    
    @Override
    protected Long getId(Article pEntity) {
        
        return pEntity.getId();
    }
    
    /**
     * @param pInitialAnd
     *            the initialAnd to set
     */
    public void setInitialAnd(boolean pInitialAnd) {
        initialAnd = pInitialAnd;
    }
    
    /**
     * @param pInitialFilterMap
     *            the initialFilterMap to set
     */
    public void setInitialFilterMap(Map<String, Object> pInitialFilterMap) {
        initialFilterMap = pInitialFilterMap;
    }
    
    /**
     * Set the filter choices for this data model.
     * This method shall always be called after the initialFilterMap was set.
     * 
     * @param pFilterChoices
     *            the filterChoices to set
     */
    public void
            setFilterChoices(Map<String, List<SelectItem>> pFilterChoices) {
        filterChoices = new HashMap<String, List<SelectItem>>();
        for (String lKey : pFilterChoices.keySet()) {
            List<SelectItem> lSelectitems = pFilterChoices.get(lKey);
            if (initialFilterMap.containsKey(lKey)){
                List<SelectItem> lNewSelectItems = new ArrayList<SelectItem>();
                Object[] lArrayValues = (Object[]) initialFilterMap.get(lKey);
                List<Object> lListValues = Arrays.asList(lArrayValues);
                for (SelectItem lItem : lSelectitems) {
                    if (lListValues.contains(lItem.getValue())) {
                        lNewSelectItems.add(new SelectItem(lItem.getValue(),
                                lItem.getLabel()));
                    }
                }
                filterChoices.put(lKey, lNewSelectItems);
            }
            else {
                filterChoices.put(lKey,
                        new ArrayList<SelectItem>(lSelectitems));
            }
        }
    }
    
    /**
     * @return the filterChoices
     */
    public Map<String, List<SelectItem>> getFilterChoices() {
        return filterChoices;
    }
    
    @Override
    protected int getNumRecords() {
        
        CriteriaStructure lSearchCriteria =
                new CriteriaStructure(initialFilterMap, initialAnd,
                        autoConditions, autoFilterMap);
        
        CriteriaStructure lFilterCriteria =
                new CriteriaStructure(getFilteredFilterMap(), true);
        
        return searchBean.searchArticleCount(lSearchCriteria, lFilterCriteria)
                .intValue();
    }
    
    /**
     * @return the selectionFilterMap
     */
    public Map<String, Object> getSelectionFilterMap() {
        return selectionFilterMap;
    }
    
    /**
     * @param pSelectionFilterMap
     *            the selectionFilterMap to set
     */
    public void setSelectionFilterMap(Map<String, Object> pSelectionFilterMap) {
        selectionFilterMap = pSelectionFilterMap;
    }
    
    /**
     * @return the filtered filterMap
     */
    public Map<String, Object> getFilteredFilterMap() {
        Map<String, Object> lFilteredFilterMap =
                new HashMap<String, Object>(filterMap);
        for (String lKey : filterChoices.keySet()) {
            if (!isFiltered(lKey)) {
                lFilteredFilterMap.remove(lKey);
            }
        }
        return lFilteredFilterMap;
    }
    
    /**
     * Retrieve the choices values for a given field
     * 
     * @param pField
     *            the field for which the choices values are requested
     * @return the choices values for the given field
     */
    public Object[] getChoicesValues(String pField) {
        List<SelectItem> lList = filterChoices.get(pField);
        int lSize = lList.size();
        Object[] lValues = new Object[lSize];
        for (int i = 0; i < lList.size(); i++) {
            lValues[i] = lList.get(i).getValue();
        }
        return lValues;
    }
    
    /**
     * Retrieve the filtered values for a given field
     * 
     * @param pField
     *            the field for which the filtered values are requested
     * @return the filtered values for the given field
     */
    public Object[] getFilteredValues(String pField) {
        return (Object[]) selectionFilterMap.get(pField);
    }
    
    /**
     * Check if a field is filtered
     * 
     * @param pField
     *            the field to test
     * @return true if the field is filtered, false otherwise
     */
    public boolean isFiltered(String pField) {
        Object[] lFilteredValues = getFilteredValues(pField);
        Object[] lChoicesValues = getChoicesValues(pField);
        if (lFilteredValues != null
                && lFilteredValues.length < lChoicesValues.length) {
            return true;
        }
        return false;
    }
    
}
