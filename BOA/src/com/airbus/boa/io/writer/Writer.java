/*
 * ------------------------------------------------------------------------
 * Class : Writer
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.List;

import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.column.Columns;

public interface Writer {
    
    public void writeHeader();
    
    public void write(List<?> list);
    
    public void writeContent(List<?> list);
    
    public void writeFooter();
    
    public void writeEmptyTemplate();
    
    public void writeOne(Object object) throws ExportException;
    
    public void applyStyles();
    
    public Columns getColumns();
    
    public Object getHeader();
    
    public int getRowNumber();
    
}
