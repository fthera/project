/*
 * ------------------------------------------------------------------------
 * Class : ColumnType
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Type columns for Excel files
 */
public class ColumnType extends BaseColumn {
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnType(String[] tabString) {
        super(tabString);
    }
    
    /**
     * Constructor with default columns
     */
    public ColumnType() {
        super();
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnType(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        
        return new ColumnType();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.FOR_ARTICLE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_TITLE,
                COLUMN_STATUS.OPTIONAL);
    }
}
