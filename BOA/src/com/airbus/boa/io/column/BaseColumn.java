/*
 * ------------------------------------------------------------------------
 * Class : BaseColumn
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract class to represent columns in Excel export sheets
 */
public abstract class BaseColumn implements Columns {

    /** Map associating the column names and their status */
    protected Map<String, COLUMN_STATUS> defaultColumns =
            new LinkedHashMap<String, COLUMN_STATUS>();
    /** Map associating the column names and their index */
    private Map<String, Integer> map = new HashMap<String, Integer>();
    /** Map associating the column indexes and their name */
    private Map<Integer, String> reverseMap = new HashMap<Integer, String>();
    
    /** Enumeration defining columns statuses */
    public enum COLUMN_STATUS {
        MANDATORY,
        OPTIONAL,
        DEFAULT
    }
    
    /** Default constructor */
    public BaseColumn() {
        initDefaultColumns();
        int i = 0;
        for (String lColumn : defaultColumns.keySet()) {
            put(lColumn, i++);
        }
    }
    
    /**
     * Constructor initializing column indexes.
     * 
     * @param pMapping
     *            the list of columns to match with default columns.
     */
    public BaseColumn(String[] pMapping) {
        
        if (pMapping == null || pMapping.length == 0) {
            throw new IllegalArgumentException("PAS DE COLONNES A LIRE!");
        }
        
        initDefaultColumns();
        for (int i = 0; i < pMapping.length; i++) {
            String lColumnToIdentify = pMapping[i];
            for (String lMatch : defaultColumns.keySet()) {
                if (lMatch.equals(lColumnToIdentify)) {
                    put(lMatch, i);
                }
            }
        }
    }
    
    /**
     * Insert a column name and index in the map and reverseMap maps.
     * 
     * @param pKey
     *            the column name.
     * @param pIndex
     *            the column index.
     */
    protected void put(String pKey, Integer pIndex) {
        map.put(pKey, pIndex);
        reverseMap.put(pIndex, pKey);
    }
    
    /**
     * Return a specific Columns instance.
     */
    public abstract Columns getDefaultColumn();
    
    /**
     * Initialize the defaultColumn map.
     */
    protected abstract void initDefaultColumns();
    
    @Override
    public Integer getIndex(String columnName) {
        if (map.containsKey(columnName)) {
            return map.get(columnName);
        }
        return -1;
    }
    
    @Override
    public String getName(int index) {
        if (reverseMap.containsKey(index)) {
            return reverseMap.get(index);
        }
        return null;
    }
    
    @Override
    public Set<Entry<String, Integer>> getDefinition() {
        return map.entrySet();
    }
    
    @Override
    public Integer getMaxColumnIndex() {
        return Collections.max(map.values());
    }
    
    @Override
    public Integer getMinColumnIndex() {
        return Collections.min(map.values());
    }
    
    @Override
    public boolean isMandatory(String pColumnName) {
        return defaultColumns.get(pColumnName).equals(COLUMN_STATUS.MANDATORY);
    }
    
    @Override
    public boolean isOptional(String pColumnName) {
        return defaultColumns.get(pColumnName).equals(COLUMN_STATUS.OPTIONAL);
    }
}
