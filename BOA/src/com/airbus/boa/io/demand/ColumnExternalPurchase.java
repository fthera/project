/*
 * ------------------------------------------------------------------------
 * Class : ColumnExternalPurchase
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.demand;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

/**
 * Definition of the external purchase export columns
 */
public class ColumnExternalPurchase extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnExternalPurchase() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param pTabString
     *            the list of columns to match with default columns
     */
    public ColumnExternalPurchase(String[] pTabString) {
        super(pTabString);
    }
    
    @Override
    public Columns read(String[] pTabString) {
        return new ColumnExternalPurchase(pTabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnExternalPurchase();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.DEMAND_NUMBER_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ISSUER_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PROGRAM_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PROJECT_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NEED_DATE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PRODUCT_TYPE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.FEATURES_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ADDITIONAL_INFORMATION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.JUSTIFICATION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.OPERATING_SYSTEM_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.LOCATION_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BUILDING_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ROOM_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PLUG_NUMBER_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.COMMENTS_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
