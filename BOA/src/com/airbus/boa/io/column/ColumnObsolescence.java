/*
 * ------------------------------------------------------------------------
 * Class : ColumnObsolescence
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Obsolescence columns for Excel files
 */
public class ColumnObsolescence extends BaseColumn {
    
    /** Index of the first dynamic column (for installations) */
    private int indexFirstDynamicColumn;
    
    /**
     * Constructor with default columns
     */
    public ColumnObsolescence() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnObsolescence(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnObsolescence(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnObsolescence();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.OBSO_FOR_ITEM_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OBSO_TYPE_ARTICLE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OBSO_CONSTITUANT_NAME_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OBSO_LAST_UPDATE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OBSO_CONSULT_PERIOD_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_END_OF_ORDER_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_END_OF_SUPPORT_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_CONTINUITY_DATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_DATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_END_OF_PRODUCTION_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_MANUFACTURER_STATUS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_AIRBUS_STATUS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_CURRENT_ACTION_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_STRATEGY_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.COMMENTS_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_MTBF_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_IN_CHARGE_OF_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OBSO_SUPPLIER_TITLE,
                COLUMN_STATUS.OPTIONAL);
        indexFirstDynamicColumn = defaultColumns.size();
    }

    
    /**
     * @return the index of the firts dynamic column
     */
    public int getIndexFirstDynamicColumn() {
        return indexFirstDynamicColumn;
    }

    
    /**
     * Set the dynamic columns.
     * 
     * @param pDynamicColumns
     *            Array of column names (installation names).
     */
    public void setDynamicColumns(String[] pDynamicColumns) {
        int i = indexFirstDynamicColumn;
        for (String lName : pDynamicColumns) {
            defaultColumns.put(lName, COLUMN_STATUS.DEFAULT);
            put(lName, i++);
        }
    }
    
}
