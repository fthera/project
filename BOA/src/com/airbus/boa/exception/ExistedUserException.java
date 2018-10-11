/*
 * ------------------------------------------------------------------------
 * Class : ExistedUserException
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ExistedUserException extends RuntimeException {
    
    private static final long serialVersionUID = -829000109301155353L;
    
    public ExistedUserException() {
        
    }
    
    public ExistedUserException(String arg0) {
        super(arg0);
        
    }
    
}
