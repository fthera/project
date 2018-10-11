/*
 * ------------------------------------------------------------------------
 * Class : ValidationException
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

import java.util.logging.Logger;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(ValidationException.class
            .getName());
    
    private String componentId;

    public ValidationException() {
        // log.warning("creation Validation Exception");
    }
    
    public ValidationException(String message) {
        
        super(message);
        log.warning("creation Validation Exception " + message);
    }
    
    public ValidationException(String componentId, String message) {
        
        super(message);
        this.componentId = componentId;
        log.warning("creation Validation Exception " + message);
    }

    public ValidationException(Throwable arg0) {
        super(arg0);
        // log.warning("creation Validation Exception " + arg0.getMessage());
    }
    
    public ValidationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // log.warning("creation Validation Exception " + arg0);
    }
    
    public String getComponentId() {
        return componentId;
    }

}
