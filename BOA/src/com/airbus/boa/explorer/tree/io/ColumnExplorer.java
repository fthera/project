/*
 * ------------------------------------------------------------------------
 * Class : ColumnExplorer
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree.io;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.ColumnBuilding;
import com.airbus.boa.io.column.Columns;

/**
 * Definition of the explorer export file columns
 */
public class ColumnExplorer extends BaseColumn {
    
    /**
     * Constructor
     */
    public ColumnExplorer() {
        super();
        
    }
    
    /**
     * Constructor
     * 
     * @param maxDepth
     *            the explorer tree depth (for shifting columns to the right)
     */
    public ColumnExplorer(int maxDepth) {
        
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be positive");
        }
        initDefaultColumns();
        
        int i = maxDepth;
        for (String column : defaultColumns.keySet()) {
            put(column, i++);
        }
    }
    
    /**
     * Constructor
     * 
     * @param tabString
     */
    public ColumnExplorer(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnExplorer(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnBuilding();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.CLASS_ARTICLE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.LOCATION_OR_DETAILS_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DESIGNATION_OR_FUNCTION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.AIRBUS_PN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MANUFACTURER_PN_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.INSTALLED_SOFTWARE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.IP_ADDRESS_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.FIXED_IP_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NETWORK_MASK_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MAC_ADDRESS_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NETWORK_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SOCKET_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.GENERAL_COMMENT_TITLE,
                COLUMN_STATUS.DEFAULT);
    }
}
