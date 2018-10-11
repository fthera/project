/*
 * ------------------------------------------------------------------------
 * Class : ImportException
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

public class ImportException extends Exception {
    
    private static final long serialVersionUID = 8860363524089836621L;
    
    private int columnIndex;
    private int rowIndex;
    
    public ImportException() {
        super();
        setColumnIndex(-1);
        setRowIndex(-1);
    }
    
    public ImportException(String arg0) {
        super(arg0);
    }
    
    public ImportException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
    
    public ImportException(String arg0, int rownum, int numcolumn) {
        super(arg0);
        
        setRowIndex(rownum);
        setColumnIndex(numcolumn);
    }
    
    public ImportException(Throwable arg0) {
        super(arg0);
    }
    
    public int getColumnIndex() {
        return columnIndex;
    }
    
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }
    
}
