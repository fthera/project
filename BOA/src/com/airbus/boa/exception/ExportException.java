/*
 * ------------------------------------------------------------------------
 * Class : ExportException
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

public class ExportException extends Exception {
    
    private static final long serialVersionUID = -3338341413374265204L;
    
    public ExportException() {
        super();
        
    }
    
    public ExportException(String arg0) {
        super(arg0);
    }
    
    public ExportException(Throwable arg0) {
        super(arg0);
    }
    
    public ExportException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
    
}
