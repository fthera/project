/*
 * ------------------------------------------------------------------------
 * Class : ColumnTool
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the tool columns for Excel files
 */
public class ColumnTool extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnTool() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param pTabString
     *            the list of columns to match with default columns
     */
    public ColumnTool(String[] pTabString) {
        super(pTabString);
    }
    
    @Override
    public Columns read(String[] pTabString) {
        
        return new ColumnTool(pTabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        
        return new ColumnTool();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.DESIGNATION_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.TECHNICAL_CONTACT_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.LOAN_DATE_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.LOAN_DUE_DATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.INHERITED_LOCATION_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.LOCATION_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.LOCATION_DETAILS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.EXTERNAL_LOCATION_NAME_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.CONTAINER_TYPE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.CONTAINER_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TYPE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.GENERAL_COMMENT_TITLE,
                COLUMN_STATUS.OPTIONAL);
    }
}
