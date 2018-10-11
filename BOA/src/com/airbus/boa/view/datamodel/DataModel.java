/*
 * ------------------------------------------------------------------------
 * Class : DataModel
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.Expression;
import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.richfaces.component.SortOrder;
import org.richfaces.model.Arrangeable;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import com.airbus.boa.entity.EntityBase;

/**
 * Abstract class defining data model used for datatables pagination
 * 
 * @param <T>
 *            the class of entities represented in the datatable
 */
public abstract class DataModel<T extends EntityBase>
        extends ExtendedDataModel<T> implements Arrangeable {

	private boolean detached = false;
    
    private Long currentId;
    private int rowIndex;
    private Integer rowCount = null;
    
    private List<Long> wrappedIds = null;
    private final Map<Long, T> wrappedData = new HashMap<Long, T>();
    
    /** Map of criteria for filtering the datatable */
    protected Map<String, Object> filterMap = new HashMap<String, Object>();
    
    /** Name of the field on which to sort the datatable */
    protected String sortField = getDefaultSortField();
    /** Ordering of the field on which to sort */
    protected SortOrder ordering = SortOrder.ascending;
    
    public void arrange(FacesContext context, ArrangeableState state) {
        
        /* Update sorting */
    	List<SortField> lSortFields = null;
    	List<FilterField> lFilterFields = null;
    	if (state != null){
        	lSortFields = state.getSortFields();
        	lFilterFields = state.getFilterFields();
    	}
        
        // Reset sorting
        sortField = getDefaultSortField();
        ordering = SortOrder.ascending;
        
        // Initialize sorting with provided values
        if (lSortFields != null && !lSortFields.isEmpty()) {
            
        	SortField lSortField = lSortFields.get(0);
            Expression lExpression = lSortField.getSortBy();
            String lExpressionStr = lExpression.getExpressionString();
            
            if (!lExpression.isLiteralText()) {
                
                lExpressionStr =
                        lExpressionStr.replaceAll("[#|$]{1}\\{.*?\\.", "")
                                .replaceAll("\\}", "");
            }
            sortField = lExpressionStr;
            ordering = lSortField.getSortOrder();
        }
        
        /* Update filtering */
        
        // Reset filtering
        Map<String, Object> lPreviousFilterMap =
                new HashMap<String, Object>(filterMap);
        
        filterMap.clear();
        
        // Initialize filtering with provided values
        if (lFilterFields != null && !lFilterFields.isEmpty()) {
            
            for (FilterField lFilterField : lFilterFields) {
                    
                String lValue = (String) lFilterField.getFilterValue();
                
                if (lValue != null && !lValue.isEmpty()) {
                    
                    Expression lExpression =
                    		lFilterField.getFilterExpression();
                    String lExpressionStr =
                            lExpression.getExpressionString();
                    
                    if (!lExpression.isLiteralText()) {
                        
                        lExpressionStr =
                                lExpressionStr.replaceAll(
                                        "[#|$]{1}\\{.*?\\.", "")
                                        .replaceAll("\\}", "");
                    }
                    filterMap.put(lExpressionStr, lValue);
                }
            }
        }
        
        if (!filterMap.equals(lPreviousFilterMap)) {
            setFilterInMapChanged();
        }
    }
    
    @Override
    public void walk(FacesContext pContext, DataVisitor pVisitor, Range pRange,
            Object pArgument) {
        
        if (detached) {
            for (Long lId : wrappedIds) {
                setRowKey(lId);
                pVisitor.process(pContext, lId, pArgument);
            }
        }
        else {
            int lFirstRow = ((SequenceRange) pRange).getFirstRow();
            int lNumberOfRows = ((SequenceRange) pRange).getRows();
            
            wrappedIds = new ArrayList<Long>();
            for (T lEntity : findEntities(lFirstRow, lNumberOfRows)) {
                Long lId = getId(lEntity);
                wrappedIds.add(lId);
                wrappedData.put(lId, lEntity);
                pVisitor.process(pContext, lId, pArgument);
            }
        }
    }
    
    /**
     * Search the entities according to the filter values and ordering
     * 
     * @param pFirstRow
     *            the first row to select
     * @param pNumberOfRows
     *            the number of rows to select
     * @return the list of found entities
     */
    public abstract List<T> findEntities(int pFirstRow, int pNumberOfRows);
    
    /**
     * @return the default sort field
     */
    protected abstract String getDefaultSortField();
    
    /**
     * Search the entity by its id
     * 
     * @param id
     *            the entity id
     * @return the found entity, else null
     */
    protected abstract T getEntityById(Long id);
    
    /**
     * Put the provided value for the provided parameter name
     * 
     * @param pParameterName
     *            the parameter name
     * @param pValue
     *            the value
     */
    protected void putFilterInMap(String pParameterName, Object pValue) {
        
        filterMap.put(pParameterName, pValue);
    }
    
    /**
     * Indicates that the filter has changed (and rows count has to be
     * recomputed)
     */
    protected void setFilterInMapChanged() {
        rowCount = null;
    }
    
    /**
     * @param pEntity
     *            the entity
     * @return the entity id
     */
    protected abstract Long getId(T pEntity);
    
    /**
     * Count the number of entities satisfying the filter values
     * 
     * @return the number of found entities
     */
    protected abstract int getNumRecords();
    
    @Override
    public boolean isRowAvailable() {
        
        if (currentId == null) {
            return false;
        }
        
        if (wrappedIds.contains(currentId)) {
            return true;
        }
        
        if (wrappedData.entrySet().contains(currentId)) {
            return true;
        }
        
        try {
            if (getEntityById(currentId) != null) {
                return true;
            }
        }
        catch (Exception e) {
            
        }
        return false;
    }
    
    @Override
    public int getRowCount() {
        if (rowCount == null) {
            
            rowCount = getNumRecords();
        }
        return rowCount;
    }
    
    @Override
    public T getRowData() {
        
        if (currentId == null) {
            return null;
        }
        
        T lEntity = wrappedData.get(currentId);
        
        if (lEntity == null) {
            lEntity = getEntityById(currentId);
            wrappedData.put(currentId, lEntity);
        }
        return lEntity;
    }
    
    @Override
    public int getRowIndex() {
        return rowIndex;
    }
    
    @Override
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }
    
    @Override
    public Object getRowKey() {
        return currentId;
    }
    
    @Override
    public void setRowKey(Object pId) {
        currentId = (Long) pId;
    }
    
    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setWrappedData(Object pData) {
        throw new UnsupportedOperationException();
    }
    
}
