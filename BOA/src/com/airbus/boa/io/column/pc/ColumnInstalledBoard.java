/*
 * ------------------------------------------------------------------------
 * Class : ColumnInstalledBoard
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column.pc;

import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.BaseColumn;
import com.airbus.boa.io.column.Columns;

public class ColumnInstalledBoard extends BaseColumn {
    
    public ColumnInstalledBoard() {
        super();
    }
    
    public ColumnInstalledBoard(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnInstalledBoard(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnInstalledBoard();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BOARD_AIRBUS_SN_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.SLOTNUMBERPOSITION_TITLE,
                COLUMN_STATUS.DEFAULT);
    }
}
