/*
 * ------------------------------------------------------------------------
 * Class : ColumnStock
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.stock;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

public class ColumnStock extends BaseColumn {
    
    public ColumnStock() {
        super();
    }
    
    public ColumnStock(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnStock(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnStock();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.QTYSTOCK_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.QTYUSE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SPARE_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
