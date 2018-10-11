/*
 * ------------------------------------------------------------------------
 * Class : ColumnExternalEntity
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the ExternalEntity columns for Excel files
 */
public class ColumnExternalEntity extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnExternalEntity() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param pTabString
     *            the list of columns to match with default columns
     */
    public ColumnExternalEntity(String[] pTabString) {
        super(pTabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnExternalEntity(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnExternalEntity();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.DEFAULT);
    }
    
}
