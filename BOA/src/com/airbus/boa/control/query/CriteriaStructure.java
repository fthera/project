/*
 * ------------------------------------------------------------------------
 * Class : CriteriaStructure
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing temporary criteria for building a query
 */
public class CriteriaStructure implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Way to combine the criteria (true for AND, false for OR) */
    private boolean combinationAnd = true;
    
    /**
     * Input criteria for the query to build:<br>
     * - Key: the name of the criterion<br>
     * - Value: the value of the criterion
     */
    private Map<String, Object> inputCriteria = new HashMap<String, Object>();
    
    /** Input conditions */
    private List<String> inputConditions = new ArrayList<String>();
    
    /**
     * Input parameters to apply in input conditions:<br>
     * - Key: the name of the parameter into the input conditions<br>
     * - Value: the value of the parameter to apply (already cast to correct
     * type)
     */
    private Map<String, Object> inputParameters = new HashMap<String, Object>();
    
    /** Resulting conditions, while building the query */
    private List<String> resultingConditions = new ArrayList<String>();
    
    /**
     * Parameters to apply in resulting conditions:<br>
     * - Key: the name of the parameter into the resulting conditions<br>
     * - Value: the value of the parameter to apply (already cast to correct
     * type)
     */
    private Map<String, Object> resultingParameters =
            new HashMap<String, Object>();
    
    /**
     * Constructor
     * 
     * @param pInputCriteria
     *            the input criteria
     * @param pCombinationAnd
     *            the way to combine the criteria
     */
    public CriteriaStructure(final Map<String, Object> pInputCriteria,
            boolean pCombinationAnd) {
        
        init(pInputCriteria, pCombinationAnd, null, null);
    }
    
    /**
     * Constructor
     * 
     * @param pInputCriteria
     *            the input criteria
     * @param pCombinationAnd
     *            the way to combine the criteria
     * @param pInputConditions
     *            the input conditions
     * @param pInputParameters
     *            the input parameters to apply into the input conditions
     */
    public CriteriaStructure(final Map<String, Object> pInputCriteria,
            boolean pCombinationAnd, final List<String> pInputConditions,
            final Map<String, Object> pInputParameters) {
        
        init(pInputCriteria, pCombinationAnd, pInputConditions,
                pInputParameters);
    }
    
    /**
     * Clean this criteria structure by removing empty parameters values
     */
    private void clean() {
        
        for (String lKey : new ArrayList<String>(inputCriteria.keySet())) {
            
            Object lObject = inputCriteria.get(lKey);
            if (lObject == null
                    || (lObject instanceof String && ((String) lObject)
                            .isEmpty())) {
                // Remove the empty criterion
                inputCriteria.remove(lKey);
            }
        }
    }
    
    @Override
    public boolean equals(Object pObj) {
        
        if (!(pObj instanceof CriteriaStructure)) {
            return false;
        }
        
        CriteriaStructure lCriteria = (CriteriaStructure) pObj;
        
        if (combinationAnd != lCriteria.combinationAnd) {
            return false;
        }
        
        if ((inputCriteria == null && lCriteria.inputCriteria != null)
                || (inputCriteria != null && !inputCriteria
                        .equals(lCriteria.inputCriteria))) {
            return false;
        }
        
        if ((inputConditions == null && lCriteria.inputConditions != null)
                || (inputConditions != null && !inputConditions
                        .equals(lCriteria.inputConditions))) {
            return false;
        }
        
        if ((inputParameters == null && lCriteria.inputParameters != null)
                || (inputParameters != null && !inputParameters
                        .equals(lCriteria.inputParameters))) {
            return false;
        }
        
        // resultingParameters field is ignored
        // resultingConditions field is ignored
        
        return true;
    }
    
    @Override
    public int hashCode() {
        
        int lHashCode = 0;
        
        if (combinationAnd) {
            lHashCode = lHashCode + 1;
        }
        
        if (inputCriteria != null) {
            lHashCode = lHashCode + inputCriteria.hashCode();
        }
        
        if (inputConditions != null) {
            lHashCode = lHashCode + inputConditions.hashCode();
        }
        
        if (inputParameters != null) {
            lHashCode = lHashCode + inputParameters.hashCode();
        }
        
        // resultingConditions field is ignored
        // resultingParameters field is ignored
        
        return lHashCode;
    }
    
    private void init(final Map<String, Object> pInputCriteria,
            boolean pCombinationAnd, final List<String> pInputConditions,
            final Map<String, Object> pInputParameters) {
        
        if (pInputCriteria != null) {
            inputCriteria.putAll(pInputCriteria);
        }
        
        combinationAnd = pCombinationAnd;
        
        if (pInputConditions != null) {
            inputConditions.addAll(pInputConditions);
        }
        
        if (pInputParameters != null) {
            inputParameters.putAll(pInputParameters);
        }
        
        clean();
    }
    
    /**
     * Initialize the resulting values with the already formatted input values
     */
    public void initResultingValues() {
        
        resultingConditions.clear();
        resultingConditions.addAll(inputConditions);
        
        resultingParameters.clear();
        resultingParameters.putAll(inputParameters);
    }
    
    /**
     * @return the combinationAnd
     */
    public boolean isCombinationAnd() {
        return combinationAnd;
    }
    
    /**
     * @return the inputCriteria
     */
    public Map<String, Object> getInputCriteria() {
        return inputCriteria;
    }
    
    /**
     * @return the resultingConditions
     */
    public List<String> getResultingConditions() {
        return resultingConditions;
    }
    
    /**
     * @return the resultingParameters
     */
    public Map<String, Object> getResultingParameters() {
        return resultingParameters;
    }
    
}
