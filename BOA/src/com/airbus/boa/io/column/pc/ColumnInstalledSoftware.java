/*
 * ------------------------------------------------------------------------
 * Class : ColumnInstalledSoftware
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column.pc;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

public class ColumnInstalledSoftware extends BaseColumn {
    
    public ColumnInstalledSoftware() {
        super();
    }
    
    public ColumnInstalledSoftware(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnInstalledSoftware(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnInstalledSoftware();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SOFTWARE_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
