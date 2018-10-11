/*
 * ------------------------------------------------------------------------
 * Class : AbstractQueryBuilder
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class generating queries in string form.
 */
public abstract class AbstractQueryBuilder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Return the default entity identifier for the provided entity class.<br>
     * The entity identifier into the query is by default
     * <b>l&ltpEntityClass&gt</b>.
     * 
     * @param pEntityClass
     *            the entity class name
     * @return the computed default entity identifier
     */
    private static final String getEntityDefaultIdentifier(String pEntityClass) {
        return "l" + pEntityClass;
    }
    
    /** The class name of the retrieved entities */
    private String entityClass;
    
    /**
     * The identifier (alias) of the entity into the query
     * (e.g.: used in conditions)
     */
    private String identifier;
    
    /** The field of the entity to retrieve */
    private String field = null;
    
    /** The WHERE clause condition */
    private Condition condition = null;
    
    /** The map containing the join conditions */
    private HashMap<String, String> joinConditions = null;
    
    /** The ORDER BY clause content */
    private Order order = null;
    
    /**
     * Constructor<br>
     * The entity identifier into the query is by default
     * <b>l&ltpEntityClass&gt</b>.
     * 
     * @param pEntityClass
     *            the entity class name to retrieve
     */
    public AbstractQueryBuilder(String pEntityClass) {
        
        entityClass = pEntityClass;
        identifier = getEntityDefaultIdentifier(pEntityClass);
    }
    
    /**
     * Constructor
     * 
     * @param pEntityClass
     *            the entity class name to retrieve
     * @param pIdentifier
     *            the entity identifier into the query
     */
    public AbstractQueryBuilder(String pEntityClass, String pIdentifier) {
        
        entityClass = pEntityClass;
        identifier = pIdentifier;
    }
    
    private StringBuffer buildQuery(boolean lCount) {
        
        StringBuffer lQuery = new StringBuffer();
        
        lQuery.append("SELECT ");
        
        if (lCount) {
            lQuery.append("COUNT(");
        }
        
        if (field != null) {
            lQuery.append("DISTINCT ");
        }
        
        lQuery.append(getSelectClauseContent());
        
        if (lCount) {
            lQuery.append(")");
        }
        
        // update the WHERE clause condition and eventually the entityClass
        updateWhereClauseCondition();
        
        lQuery.append(" FROM ").append(entityClass).append(" ")
                .append(identifier);
        
        if (joinConditions != null) {
            for (String lKey : joinConditions.keySet()) {
                String lJoinCondition = joinConditions.get(lKey);
                lQuery.append(" " + lJoinCondition);
            }
        }
        
        if (condition != null && !condition.isEmpty()) {
            lQuery.append(" WHERE ").append(condition.combineConditions());
        }
        
        if ((!lCount) && order != null && !order.isEmpty()) {
            lQuery.append(" ORDER BY ").append(order.combineOrders());
        }
        
        return lQuery;
    }
    
    /**
     * @return the identifier
     */
    public final String getIdentifier() {
        return identifier;
    }
    
    /**
     * @param pEntityClass
     *            the entityClass to set
     */
    public final void setEntityClass(String pEntityClass) {
        entityClass = pEntityClass;
    }
    
    /**
     * @param pField
     *            the field to set
     */
    public final void setField(String pField) {
        field = pField;
    }
    
    /**
     * Compute and return the SELECT clause content.<br>
     * The returned value is used to fill the following part of the query:<br>
     * <b>SELECT </b>[COUNT(][DISTINCT ]<b>&ltreturnValue&gt</b>[)]<br>
     * e.g.: <i>identifier</i><br>
     * e.g.: <i>identifier.field</i>
     * 
     * @return the SELECT clause content<br>
     */
    protected String getSelectClauseContent() {
        
        StringBuffer lSelect = new StringBuffer(identifier);
        
        if (field != null) {
            lSelect.append(".").append(field);
        }
        
        return lSelect.toString();
    }
    
    /**
     * Add a manual join<br>
     * The join entity identifier into the condition must be
     * <b>l&ltpEntityClass&gt</b>.
     * 
     * @param pJoinEntityClass
     *            the joinEntityClass to add
     * @param pJoinType
     *            the type of join
     * @param pCondition
     *            the condition to add
     */
    public final void addJoin(String pKey, String pJoinCondition) {
        if (joinConditions == null) {
            joinConditions = new HashMap<String, String>();
        }
        if (!joinConditions.containsKey(pKey)) {
            joinConditions.put(pKey, pJoinCondition);
        }
    }
    
    /**
     * If not already initialized, initialize the WHERE clause condition.<br>
     * This method may update the entity class.
     */
    protected abstract void updateWhereClauseCondition();
    
    /**
     * @return true if the condition is initialized, else false
     */
    protected final boolean isConditionInitialized() {
        return condition != null;
    }
    
    /**
     * @param pCondition
     *            the condition to set
     */
    public final void setCondition(Condition pCondition) {
        condition = pCondition;
    }
    
    /**
     * @param pOrder
     *            the order to set
     */
    public final void addOrder(Order pOrder) {
        if (order == null) {
            order = new Order();
        }
        order.addOrder(pOrder);
    }
    
    /**
     * Build the query retrieving the number of results (rows), using the
     * attributes values
     * 
     * @return the query in string form
     */
    public final String getQueryCount() {
        
        return buildQuery(true).toString();
    }
    
    /**
     * Build the query retrieving the list of results, using the attributes
     * values
     * 
     * @return the query in string form
     */
    public final String getQueryList() {
        
        return buildQuery(false).toString();
    }
    
}
