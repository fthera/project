/*
 * ------------------------------------------------------------------------
 * Class : ColumnEquipment
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

public class ColumnEquipment extends BaseColumn {
    
    public ColumnEquipment() {
        super();
    }
    
    public ColumnEquipment(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnEquipment(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnEquipment();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.SOFTWARE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.FOR_ARTICLE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
    }
    
}
