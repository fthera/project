/*
 * ------------------------------------------------------------------------
 * Class : Condition
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class defining the condition(s) of a query (WHERE clause)
 */
public class Condition implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** List of conditions to combine (see combinationAnd) */
    private List<String> conditions = new ArrayList<String>();
    
    /** Way to combine the conditions (true => AND, false => OR) */
    private boolean combinationAnd = true;
    
    /**
     * Default constructor<br>
     * The way to combine conditions is by default AND
     */
    public Condition() {
        
    }
    
    /**
     * Constructor<br>
     * The way to combine conditions is by default AND
     * 
     * @param pCondition
     *            the condition
     */
    public Condition(String pCondition) {
        
        addCondition(pCondition);
    }
    
    /**
     * Constructor
     * 
     * @param pConditions
     *            the list of conditions
     * @param pCombinationAnd
     *            the way to combine the conditions
     */
    public Condition(List<String> pConditions, boolean pCombinationAnd) {
        
        addConditions(pConditions);
        combinationAnd = pCombinationAnd;
    }
    
    /**
     * Combine the conditions
     * 
     * @return the resulting condition
     */
    public String combineConditions() {
        
        if (conditions.isEmpty()) {
            return null;
        }
        
        if (conditions.size() == 1) {
            return conditions.get(0);
        }
        
        StringBuffer lCombinedCondition = new StringBuffer("(");
        
        String lCombinationStr;
        if (combinationAnd) {
            lCombinationStr = " AND ";
        }
        else {
            lCombinationStr = " OR ";
        }
        
        boolean lIsFirstAdded = false;
        for (String lCondition : conditions) {
            
            if (lIsFirstAdded) {
                lCombinedCondition.append(lCombinationStr);
            }
            else {
                lIsFirstAdded = true;
            }
            lCombinedCondition.append(lCondition);
        }
        
        lCombinedCondition.append(")");
        
        return lCombinedCondition.toString();
    }
    
    /**
     * @param pCondition
     *            the condition to add
     */
    public void addCondition(String pCondition) {
        if (pCondition != null && !pCondition.isEmpty()) {
            conditions.add(pCondition);
        }
    }
    
    /**
     * @param pConditions
     *            the list of conditions to add
     */
    public void addConditions(List<String> pConditions) {
        if (pConditions != null && !pConditions.isEmpty()) {
            for (String lCondition : pConditions) {
                addCondition(lCondition);
            }
        }
    }
    
    /**
     * @param pCombinationAnd
     *            the combinationAnd to set
     */
    public void setCombinationAnd(boolean pCombinationAnd) {
        combinationAnd = pCombinationAnd;
    }
    
    /**
     * @return true if the list of conditions is empty
     */
    public boolean isEmpty() {
        
        return conditions.isEmpty();
    }
    
}
