/*
 * ------------------------------------------------------------------------
 * Class : ColumnHistory
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

public class ColumnHistory extends BaseColumn {
    
    public ColumnHistory() {
        super();
    }
    
    public ColumnHistory(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        
        return new ColumnHistory(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        
        return new ColumnHistory();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.CLASS_ARTICLE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DESIGNATION_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.TYPE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.LOGIN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ACTION_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.MODIFIED_FIELD_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.BEFORE_VALUE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.AFTER_VALUE_TITLE,
                COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.DATE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.COMMENTS_TITLE, COLUMN_STATUS.DEFAULT);
    }
    
}
