/*
 * ------------------------------------------------------------------------
 * Class : SearchBean
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.richfaces.component.SortOrder;

import com.airbus.boa.control.query.ArticleQueryBuilder;
import com.airbus.boa.control.query.CriteriaStructure;
import com.airbus.boa.entity.article.Article;

/**
 * Bean used for articles search (including PC)
 */
@Stateless
@LocalBean
public class SearchBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(SearchBean.class.getName());
    
    @PersistenceContext(name = "SearchService")
    private EntityManager em;
    
    /**
     * Create the query by setting the parameters values into the query
     * retrieved from the query builder.
     * 
     * @param pSearchCriteria
     *            the search criteria structure
     * @param pFilterCriteria
     *            the filter criteria structure
     * @param pSortField
     *            the field used to sort results
     * @param pOrdering
     *            the ordering to apply to the sort field
     * @param pLanguageCode
     *            the language code used by some sorts (can be null)
     * @param pClass
     *            the class of the typed query to retrieve:<br>
     *            - Long.class for a query counting the found articles<br>
     *            - Article.class for a query retrieving the found articles<br>
     *            - another class for a query retrieving one field of the found
     *            articles
     * @param pRetrievedField
     *            the name of the field to retrieve from the Article (null for
     *            retrieving all fields)
     * @return the created query (filled with parameters values)
     */
    private <T> TypedQuery<T> getQuery(CriteriaStructure pSearchCriteria,
            CriteriaStructure pFilterCriteria, String pSortField,
            SortOrder pOrdering, String pLanguageCode, Class<T> pClass,
            String pRetrievedField) {
        
        ArticleQueryBuilder lQueryBuilder = new ArticleQueryBuilder();
        
        /* Initialize the criteria list */
        
        List<CriteriaStructure> lCriteriaList =
                new ArrayList<CriteriaStructure>();
        
        // add the search map
        if (pSearchCriteria != null) {
            lCriteriaList.add(pSearchCriteria);
        }
        
        // add the filter map
        if (pFilterCriteria != null) {
            lCriteriaList.add(pFilterCriteria);
        }
        
        lQueryBuilder.setCriteriaList(lCriteriaList);
        
        /* Initialize the sorting */
        
        lQueryBuilder.addSorting(pSortField, pOrdering, pLanguageCode);
        
        /* Initialize the retrieved data */
        
        lQueryBuilder.setField(pRetrievedField);
        
        /* Compute the query */
        
        String lQueryString;
        
        if (pClass.equals(Article.class)) {
            lQueryString = lQueryBuilder.getQueryList();
        }
        else if (pClass.equals(Long.class)) {
            lQueryString = lQueryBuilder.getQueryCount();
        }
        else {
            // Only one field is retrieved
            lQueryString = lQueryBuilder.getQueryList();
        }
        
        log.info(lQueryString);
        
        TypedQuery<T> lQuery = em.createQuery(lQueryString, pClass);
        
        lQueryBuilder.applyParametersOnQuery(lQuery);
        
        return lQuery;
    }
    
    /**
     * Search in database the list of Articles satisfying the provided criteria.
     * 
     * @param pFirstRow
     *            the first row to retrieve (taken into account only if >= 0 and
     *            pNumberOfRows taken into account)
     * @param pNumberOfRows
     *            the maximum number of rows to retrieve (taken into account
     *            only if >= 1 and pFirstRow taken into account)
     * @param pSearchCriteria
     *            the search criteria structure
     * @param pFilterCriteria
     *            the filter criteria structure
     * @param pSortField
     *            the field used to sort results
     * @param pOrdering
     *            the ordering to apply to the sort field
     * @param pLanguageCode
     *            the language code used by some sorts (can be null)
     * @return the list of Articles satisfying the provided parameters
     */
    public List<Article> searchArticle(int pFirstRow, int pNumberOfRows,
            CriteriaStructure pSearchCriteria,
            CriteriaStructure pFilterCriteria, String pSortField,
            SortOrder pOrdering, String pLanguageCode) {
        
        TypedQuery<Article> lQuery = getQuery(
        // search criteria and combination
                pSearchCriteria,
                // filter criteria
                pFilterCriteria,
                // sorting
                pSortField, pOrdering, pLanguageCode,
                // class and field
                Article.class, null);
        
        if (pFirstRow >= 0 && pNumberOfRows >= 1) {
            lQuery.setFirstResult(pFirstRow);
            lQuery.setMaxResults(pNumberOfRows);
        }
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return new ArrayList<Article>();
        }
    }
    
    /**
     * Search in database the list of Articles satisfying the provided criteria.
     * (The criteria must be all satisfied.)
     * 
     * @param pMapCriteria
     *            the map of criteria to be applied
     * @return the list of Articles satisfying the provided parameters
     */
    public List<Article> searchArticle(final Map<String, Object> pMapCriteria) {
        
        TypedQuery<Article> lQuery = getQuery(
        // search criteria and combination
                new CriteriaStructure(pMapCriteria, true),
                // filter criteria
                null,
                // sorting
                null, null, null,
                // class and field
                Article.class, null);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return new ArrayList<Article>();
        }
    }
    
    /**
     * Count in database the number of Articles satisfying the provided
     * criteria.
     * 
     * @param pSearchCriteria
     *            the search criteria structure
     * @param pFilterCriteria
     *            the filter criteria structure
     * @return the number of Articles satisfying the provided parameters
     */
    public Long searchArticleCount(CriteriaStructure pSearchCriteria,
            CriteriaStructure pFilterCriteria) {
        
        TypedQuery<Long> lQuery = getQuery(
        // search criteria and combination
                pSearchCriteria,
                // filter criteria
                pFilterCriteria,
                // sorting
                null, null, null,
                // class and field
                Long.class, "id");
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NoResultException e) {
            return new Long(0);
        }
    }
    
    /**
     * Return the list of Articles satisfying the provided criteria, without
     * taking into account the suggested parameter criteria if it exists, and
     * the suggested parameter value.
     * (The criteria must be all satisfied.)
     * 
     * @param pSuggestName
     *            the suggested parameter name
     * @param pSuggestValue
     *            the suggested parameter value (it can contain '*')
     * @param pParameters
     *            the parameters map (it can contain all possible parameters)
     * @return the list of Articles satisfying the criteria and the suggested
     *         value
     */
    public List<? extends Article> suggestionListArticle(String pSuggestName,
            String pSuggestValue, final Map<String, Object> pParameters) {
        
        Map<String, Object> lParametersCopy =
                new HashMap<String, Object>(pParameters);
        
        // add the suggest parameter to the local copy of parameters
        String lSuggestValue = new String(pSuggestValue);
        if (lSuggestValue.indexOf('*') == -1) {
            lSuggestValue = "*" + lSuggestValue + "*";
        }
        
        lParametersCopy.put(pSuggestName, lSuggestValue);
        
        TypedQuery<Article> lQuery = getQuery(
        // search criteria and combination
                new CriteriaStructure(lParametersCopy, true),
                // filter criteria
                null,
                // sorting
                null, null, null,
                // class and field
                Article.class, null);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return new ArrayList<Article>();
        }
    }
    
    /**
     * Return the list of fields from Articles satisfying the provided criteria,
     * taking into account the suggested parameter value instead of the existing
     * one in criteria for the suggest parameter criterion.
     * (The criteria must be all satisfied.)
     * 
     * @param pSuggestParameterName
     *            the suggested parameter name
     * @param pSuggestValue
     *            the suggested parameter value (it can contain '*')
     * @param pParameters
     *            the parameters map (it can contain all possible parameters)
     * @return the list of Articles satisfying the criteria and the suggested
     *         value
     */
    public List<String> suggestionListField(String pSuggestParameterName,
            String pSuggestValue, final Map<String, Object> pParameters) {
        
        return suggestionListField(pSuggestParameterName, pSuggestValue,
                pParameters, String.class, pSuggestParameterName);
    }
    
    /**
     * Return the list of fields from Articles satisfying the provided criteria,
     * taking into account the suggested parameter value instead of the existing
     * one in criteria for the suggest parameter criterion.
     * (The criteria must be all satisfied.)
     * 
     * @param pSuggestParameterName
     *            the suggested parameter name
     * @param pSuggestValue
     *            the suggested parameter value (it can contain '*')
     * @param pParameters
     *            the parameters map (it can contain all possible parameters)
     * @param pClass
     *            the class of retrieved field
     * @param pRetrievedField
     *            the field of the Article to retrieve
     * @return the list of Articles satisfying the criteria and the suggested
     *         value
     */
    public <T> List<T> suggestionListField(String pSuggestParameterName,
            String pSuggestValue, final Map<String, Object> pParameters,
            Class<T> pClass, String pRetrievedField) {
        
        Map<String, Object> lParametersCopy =
                new HashMap<String, Object>(pParameters);
        
        // add the suggest parameter to the local copy of parameters
        String lSuggestValue = pSuggestValue;
        if (lSuggestValue == null || lSuggestValue.isEmpty()) {
            lSuggestValue = "*";
        }
        else if (lSuggestValue.indexOf('*') == -1) {
            lSuggestValue = "*" + lSuggestValue + "*";
        }
        
        lParametersCopy.put(pSuggestParameterName, lSuggestValue);
        
        TypedQuery<T> lQuery = getQuery(
        // search criteria and combination
                new CriteriaStructure(lParametersCopy, true),
                // filter criteria
                null,
                // sorting
                pSuggestParameterName, SortOrder.ascending, null,
                // class and field
                pClass, pRetrievedField);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return new ArrayList<T>();
        }
    }
    
}
