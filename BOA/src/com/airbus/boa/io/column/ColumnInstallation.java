/*
 * ------------------------------------------------------------------------
 * Class : ColumnInstallation
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

/**
 * Manage the Installation columns for Excel files
 */
public class ColumnInstallation extends BaseColumn {
    
    /**
     * Constructor with defaults columns
     */
    public ColumnInstallation() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param mapping
     *            the list of columns to match with default columns
     */
    public ColumnInstallation(String[] mapping) {
        super(mapping);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnBuilding(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnBuilding();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.COMMENTS_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.BUSINESS_SIGLUM_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.AIRCRAFTPROGRAM_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.TECHNICAL_CONTACT_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.INSTALLATION_USER,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.STARTING_DATE_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.LOCATION_TITLE, COLUMN_STATUS.MANDATORY);
    }
    
}
