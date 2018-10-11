/*
 * ------------------------------------------------------------------------
 * Class : ColumnRack
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Rack columns for Excel files
 */
public class ColumnRack extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnRack() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnRack(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnRack(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnRack();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DESIGNATION_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.AIRBUS_PN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_PN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.CMSCODE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.STATE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.USE_STATE_TITLE,
                COLUMN_STATUS.MANDATORY);
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
        defaultColumns.put(IOConstants.RACK_POSITION_PRECISION_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TYPE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ACQUISITIONDATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.GENERAL_COMMENT_TITLE,
                COLUMN_STATUS.OPTIONAL);
    }
}
