/*
 * ------------------------------------------------------------------------
 * Class : ColumnPC
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the PC columns for Excel files
 */
public class ColumnPC extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnPC() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param tabString
     *            the list of columns to match with default columns
     */
    public ColumnPC(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnPC(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnPC();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.DEPARTMENTINCHARGE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.PRODUCT_TYPE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.FUNCTION_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.DOMAIN_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.PLATFORM_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.PC_TECHNICAL_CONTACT_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OWNER_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.OWNER_SIGLUM_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.STATE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.USE_STATE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.ACQUISITIONDATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.ADMIN_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.DEFAULT_OS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.NB_SCREENS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.COMMENT_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.BUSINESS_ALLOC_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.BUSINESS_USAGE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.ASSIGNEMENT_TITLE,
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
        defaultColumns.put(IOConstants.CONTAINER_DETAILS_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TYPE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MASTER_CONTAINER_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.INSTALLATION_USER,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.CONTAINER_BUSINESS_SIGLUM,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.CONTAINER_TECHNICAL_CONTACT,
                COLUMN_STATUS.DEFAULT);
    }
    
}
