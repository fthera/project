/*
 * ------------------------------------------------------------------------
 * Class : Order
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.richfaces.component.SortOrder;

/**
 * Class defining the order of a query (ORDER BY clause)
 */
public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** List of fields for ordering */
    private List<String> orders = new ArrayList<String>();
    
    /**
     * Default constructor
     */
    public Order() {
        
    }
    
    /**
     * Constructor: initialize the order with the provided values as the first
     * sorting
     * 
     * @param pSortColumn
     *            the name of the sort column
     * @param pOrdering
     *            the ordering
     */
    public Order(String pSortColumn, SortOrder pOrdering) {
        
        addSorting(pSortColumn, pOrdering);
    }
    
    /**
     * Add an order
     * 
     * @param pOrder
     *            the order to add
     */
    public void addOrder(Order pOrder) {
        
        orders.add(pOrder.combineOrders());
    }
    
    /**
     * Add a sorting
     * 
     * @param pSortColumn
     *            the name of the sort column
     * @param pOrdering
     *            the ordering
     */
    public void addSorting(String pSortColumn, SortOrder pOrdering) {
        
        boolean lSort = true;
        
        if (pSortColumn != null) {
            
            String lOrderingStr = null;
            
            if (pOrdering != null) {
                switch (pOrdering) {
                case ascending:
                    lOrderingStr = " ASC";
                    break;
                case descending:
                    lOrderingStr = " DESC";
                    break;
                case unsorted:
                default:
                    lSort = false;
                }
            }
            else {
                lSort = false;
            }
            
            if (lSort) {
                orders.add(pSortColumn + lOrderingStr);
            }
        }
    }
    
    /**
     * Combine the orders
     * 
     * @return the resulting order
     */
    public String combineOrders() {
        
        if (orders.isEmpty()) {
            return null;
        }
        
        if (orders.size() == 1) {
            return orders.get(0);
        }
        
        StringBuffer lCombinedOrder = new StringBuffer();
        
        boolean lIsFirstAdded = false;
        for (String lOrder : orders) {
            
            if (lIsFirstAdded) {
                lCombinedOrder.append(", ");
            }
            else {
                lIsFirstAdded = true;
            }
            lCombinedOrder.append(lOrder);
        }
        
        return lCombinedOrder.toString();
    }
    
    /**
     * @return true if the list of conditions is empty
     */
    public boolean isEmpty() {
        
        return orders.isEmpty();
    }
    
}
