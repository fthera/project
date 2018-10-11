/*
 * ------------------------------------------------------------------------
 * Class : ColumnPCSpecificities
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column.pc;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

/**
 * Manage the PC specificities columns for Excel files
 */
public class ColumnPCSpecificities extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnPCSpecificities() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnPCSpecificities(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnPCSpecificities(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnPCSpecificities();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DESCRIPTION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.CONTACT_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
