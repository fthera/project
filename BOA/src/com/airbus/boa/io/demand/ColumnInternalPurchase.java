/*
 * ------------------------------------------------------------------------
 * Class : ColumnInternalPurchase
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.demand;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

/**
 * Definition of the internal purchase export columns
 */
public class ColumnInternalPurchase extends BaseColumn {
    
    /**
     * Constructor with default columns
     */
    public ColumnInternalPurchase() {
        super();
    }
    
    /**
     * Constructor with a provided columns list
     * 
     * @param pTabString
     *            the list of columns to match with default columns
     */
    public ColumnInternalPurchase(String[] pTabString) {
        super(pTabString);
    }
    
    @Override
    public Columns read(String[] pTabString) {
        return new ColumnInternalPurchase(pTabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnInternalPurchase();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.DEPT_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ISSUER_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BUDGET_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NEW_REPLACE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ASSET_NUMBER_TO_REPLACE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.HARDWARE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.OS_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.FEATURES_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BUSINESS_USAGE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ADDITIONAL_INFORMATION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.JUSTIFICATION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NEED_DATE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.OTP_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PROGRAM_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PROJECT_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.COMMENTS_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BUILDING_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ROOM_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SOCKET_NB_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PRICE_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
