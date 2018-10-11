/*
 * ------------------------------------------------------------------------
 * Class : ColumnBuilding
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

public class ColumnBuilding extends BaseColumn {
    
    public ColumnBuilding() {
        super();
    }
    
    public ColumnBuilding(String[] tabString) {
        super(tabString);
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
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.PARENT_TITLE, COLUMN_STATUS.OPTIONAL);
    }
}
