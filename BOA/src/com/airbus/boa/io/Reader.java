/*
 * ------------------------------------------------------------------------
 * Class : Reader
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import com.airbus.boa.exception.ImportException;

public interface Reader {
    
    public void readLine() throws ImportException;
    
    public boolean checkHeader();
    
    public void read() throws ImportException;
    
    public Object getHeader();
    
    public int getColumnIndex(String columnName);
    
    public String getColumnName(int index);
    
}
