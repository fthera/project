/*
 * ------------------------------------------------------------------------
 * Class : ColumnPort
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import com.airbus.boa.io.IOConstants;

public class ColumnPort extends BaseColumn {
    
    public ColumnPort() {
        super();
    }
    
    public ColumnPort(String[] tabString) {
        super(tabString);
    }
    
    @Override
    public Columns read(String[] tabString) {
        return new ColumnPort(tabString);
    }
    
    @Override
    public Columns getDefaultColumn() {
        return new ColumnPort();
    }
    
    @Override
    protected void initDefaultColumns() {
        defaultColumns.put(IOConstants.NUMID_TITLE, COLUMN_STATUS.DEFAULT);
        defaultColumns.put(IOConstants.FOR_ARTICLE_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.AIRBUS_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.MANUFACTURER_SN_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.NAME_TITLE, COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.IP_ADDRESS_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.FIXED_IP_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.NETWORK_MASK_TITLE,
                COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.MAC_ADDRESS_TITLE,
                COLUMN_STATUS.MANDATORY);
        defaultColumns.put(IOConstants.NETWORK_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.SOCKET_TITLE, COLUMN_STATUS.OPTIONAL);
        defaultColumns.put(IOConstants.COMMENT_PORT_TITLE,
                COLUMN_STATUS.OPTIONAL);
    }
    
}
