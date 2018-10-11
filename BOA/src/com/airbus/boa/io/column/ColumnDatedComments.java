/*
 * ------------------------------------------------------------------------
 * Class : ColumnDatedComments
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Dated comments columns for Excel files
 */
public class ColumnDatedComments extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnDatedComments() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnDatedComments(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnDatedComments(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnDatedComments();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.COMMENT_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.USER_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DATE_TITLE, COLUMN_STATUS.DEFAULT);
    }
    
}
