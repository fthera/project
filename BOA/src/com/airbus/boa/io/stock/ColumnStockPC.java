/*
 * ------------------------------------------------------------------------
 * Class : ColumnStockPC
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.stock;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

public class ColumnStockPC extends BaseColumn {
    
    public ColumnStockPC() {
        super();
    }
    
    public ColumnStockPC(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnStockPC(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnStockPC();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.QTYSTOCK_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.QTYUSE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SPARE_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
