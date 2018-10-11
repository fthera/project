/*
 * ------------------------------------------------------------------------
 * Class : ColumnSoftware
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Software columns for Excel files
 */
public class ColumnSoftware extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnSoftware() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnSoftware(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnSoftware(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnSoftware();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.DISTRIBUTION_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.KERNEL_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.OPERATINGSYSTEM_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.MANUFACTURER_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.DESCRIPTION_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.LICENCE_TITLE, COLUMN_STATUS.OPTIONAL);
    }
    
}
