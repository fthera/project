/*
 * ------------------------------------------------------------------------
 * Class : AutoUpdateException
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

/**
 * Exception thrown when an automatic update error occurs
 */
public class AutoUpdateException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor
     * 
     * @param pMsg
     *            the error message
     * @param pCause
     *            the cause
     */
    public AutoUpdateException(String pMsg, Throwable pCause) {
        super(pMsg, pCause);
    }
    
    @Override
    public String getMessage() {
        
        if (getCause() != null) {
            return super.getMessage() + ": " + getCause().getMessage();
        }
        return super.getMessage();
    }
}
