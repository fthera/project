/*
 * ------------------------------------------------------------------------
 * Class : ColumnUser
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

public class ColumnUser extends BaseColumn {
    
    public ColumnUser() {
        super();
    }
    
    public ColumnUser(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnUser(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnUser();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.LOGIN_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.LASTNAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.FIRSTNAME_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.EMAIL_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.ROLE_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.PASSWORD_TITLE, COLUMN_STATUS.DEFAULT);
    }
}
