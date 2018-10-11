/*
 * ------------------------------------------------------------------------
 * Class : EntityQueryBuilder
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.richfaces.component.SortOrder;

import com.airbus.boa.util.StringUtil;

/**
 * Class generating queries in string form for entities, based on filtering and
 * sorting information.
 */
public abstract class EntityQueryBuilder extends AbstractQueryBuilder implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(EntityQueryBuilder.class
            .getName());
    
    private int lastIdent = 0;
    
    /** List of criteria (the index 0 is the main one) */
    protected List<CriteriaStructure> criteriaList =
            new ArrayList<CriteriaStructure>();
    
    /**
     * Mandatory condition to be combined with an AND with the condition
     * obtained from the criteria list
     */
    private Condition mandatoryCondition = null;
    
    /**
     * Constructor<br>
     * The entity identifier into the query is by default
     * <b>l&ltpEntityClass&gt</b>.
     * 
     * @param pEntityClass
     *            the entity class name to retrieve
     */
    public EntityQueryBuilder(String pEntityClass) {
        
        super(pEntityClass);
    }
    
    /**
     * Constructor
     * 
     * @param pEntityClass
     *            the entity class name to retrieve
     * @param pIdentifier
     *            the entity identifier into the query
     */
    public EntityQueryBuilder(String pEntityClass, String pIdentifier) {
        
        super(pEntityClass, pIdentifier);
    }
    
    /**
     * Update the criteria structure of the provided index by computing the
     * resulting values
     * 
     * @param pIndex
     *            the index of the criteria structure in the list
     */
    protected abstract void updateCriteriaResultingValues(int pIndex);
    
    @Override
    protected void updateWhereClauseCondition() {
        
        if (!isConditionInitialized()) {
            
            Condition lMainCondition = new Condition();
            
            for (int i = 0; i < criteriaList.size(); i++) {
                
                updateCriteriaResultingValues(i);
                
                CriteriaStructure lCurrentCriteria = criteriaList.get(i);
                
                // Generate the condition for current criteria
                Condition lCondition =
                        new Condition(
                                lCurrentCriteria.getResultingConditions(),
                                lCurrentCriteria.isCombinationAnd());
                
                // Add generated conditions to the main condition
                lMainCondition.addCondition(lCondition.combineConditions());
            }
            
            // Add the mandatory condition
            if (mandatoryCondition != null) {
                lMainCondition.addCondition(mandatoryCondition
                        .combineConditions());
            }
            
            // Set the condition
            setCondition(lMainCondition);
        }
    }
    
    /**
     * Create the condition from the provided sub queries
     * (with an OR combination).
     * 
     * @param pSubQueries
     *            the sub queries to combine
     * @return the condition
     */
    protected String createConditionFromSubQueries(List<String> pSubQueries) {
        
        if (pSubQueries.size() == 1) {
            return createConditionFromSubQuery(pSubQueries.get(0));
        }
        
        Condition lCondition = new Condition();
        lCondition.setCombinationAnd(false);
        
        for (String lSubQuery : pSubQueries) {
            lCondition.addCondition(getIdentifier() + ".id IN (" + lSubQuery
                    + ")");
        }
        
        return lCondition.combineConditions();
    }
    
    /**
     * Create the condition from the provided sub query.
     * 
     * @param pSubQuery
     *            the sub query to combine
     * @return the condition
     */
    protected String createConditionFromSubQuery(String pSubQuery) {
        
        return getIdentifier() + ".id IN (" + pSubQuery + ")";
    }
    
    /**
     * @param pCriteriaList
     *            the criteriaList to set
     */
    public void setCriteriaList(List<CriteriaStructure> pCriteriaList) {
        
        if (pCriteriaList != null) {
            criteriaList = pCriteriaList;
        }
        else {
            criteriaList = new ArrayList<CriteriaStructure>();
        }
    }
    
    /**
     * @param pCondition
     *            the mandatory condition to add
     */
    protected void addMandatoryCondition(String pCondition) {
        if (mandatoryCondition == null) {
            mandatoryCondition = new Condition(pCondition);
        }
        else {
            mandatoryCondition.addCondition(pCondition);
        }
    }
    
    /**
     * Add the sorting if the sort field is not null (order is by default
     * ascending)
     * 
     * @param pSortField
     *            the sortField to set
     * @param pOrdering
     *            the ordering to set
     * @param pLanguageCode
     *            the languageCode to set
     */
    public void addSorting(String pSortField, SortOrder pOrdering,
            String pLanguageCode) {
        
        if (pSortField != null && pOrdering != null) {
            
            Order lOrder = null;
            
            /*---------- Treat some specific cases first ----------*/
            lOrder =
                    generateSpecificOrder(pSortField, pOrdering, pLanguageCode);
            
            if (lOrder == null) {
                /*---------- Treat "generic" cases ------------*/
                lOrder =
                        new Order(getIdentifier() + "." + pSortField, pOrdering);
            }
            
            addOrder(lOrder);
        }
    }
    
    /**
     * Generate and return the order corresponding to the provided values if
     * they concern specific parameters. If sortField is not specific, then
     * return null.
     * 
     * @param pSortField
     *            the field on which to sort (not formatted attribute name:
     *            e.g.: entityAttribute.attribute)
     * @param pOrdering
     *            the ordering
     * @param pLanguageCode
     *            the language code to use for sorting (may be null)
     * @return the generated order or null
     */
    protected abstract Order generateSpecificOrder(String pSortField,
    		SortOrder pOrdering, String pLanguageCode);
    
    /**
     * @param pPrefix
     *            the prefix of the identifier
     * @return a unique identifier for queries
     */
    protected String getUniqueIdentifier(String pPrefix) {
        lastIdent++;
        if (pPrefix != null && !pPrefix.isEmpty()) {
            return pPrefix + lastIdent;
        }
        else {
            return "id" + lastIdent;
        }
    }
    
    /**
     * Fill the parameter value, if it is defined into the map, into
     * the provided query. The parameter name shall be the same into the
     * parameterized query and into the parameters map.
     * 
     * @param pQuery
     *            the parameterized query on which the parameter is to be set
     * @param pParametersMap
     *            the map containing the parameters
     * @param pParameter
     *            the parameter name
     */
    protected void applyOneParameterOnQuery(Query pQuery,
            Map<String, Object> pParametersMap, String pParameter) {
        
        Object lValue = pParametersMap.get(pParameter);
        
        if (lValue != null) {
            
            if (lValue instanceof String) {
                
                // replace '*' by '%'
                lValue = StringUtil.parseToSQLRegex((String) lValue);
            }
            
            try {
                // set the parameter value into the query
                if (lValue instanceof Date) {
                    pQuery.setParameter(pParameter, (Date) lValue,
                            TemporalType.TIMESTAMP);
                }
                else {
                    pQuery.setParameter(pParameter, lValue);
                }
                
            }
            catch (IllegalArgumentException e) {
                log.warning(e.getMessage());
            }
        }
    }
    
    /**
     * Fill the parameters values into the provided query.
     * 
     * @param pQuery
     *            the parameterized query on which the parameter is to be set
     */
    public <T> void applyParametersOnQuery(TypedQuery<T> pQuery) {
        
        for (int i = 0; i < criteriaList.size(); i++) {
            
            Map<String, Object> lMap =
                    criteriaList.get(i).getResultingParameters();
            
            for (String lKey : lMap.keySet()) {
                applyOneParameterOnQuery(pQuery, lMap, lKey);
            }
        }
    }
    
}
