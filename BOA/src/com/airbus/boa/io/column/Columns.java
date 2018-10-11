/*
 * ------------------------------------------------------------------------
 * Class : Columns
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.column;

import java.util.Map.Entry;
import java.util.Set;

public interface Columns {
    
    public Columns read(String[] tabString);
    
    public Integer getIndex(String columnName);
    
    public String getName(int index);
    
    public Set<Entry<String, Integer>> getDefinition();
    
    public boolean isMandatory(String pColumnName);
    
    public boolean isOptional(String pColumnName);
    
    public Integer getMaxColumnIndex();
    
    public Integer getMinColumnIndex();
    
}
