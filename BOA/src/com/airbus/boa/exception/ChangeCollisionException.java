/*
 * ------------------------------------------------------------------------
 * Class : ChangeCollisionException
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

import javax.ejb.ApplicationException;

/*
 * Exception de prise en compte des collisions
 * Elle est interceptée par l'application
 */
@ApplicationException(rollback = true)
public class ChangeCollisionException extends RuntimeException {
    
    private static final long serialVersionUID = 4024899765688055301L;
    
    // TODO ChangeCollision : Quel User ? Est-ce possible à recuperer
    public ChangeCollisionException() {
        
    }
    
    public ChangeCollisionException(String arg0) {
        super(arg0);
        
    }
    
    public ChangeCollisionException(Throwable arg0) {
        super(arg0);
        
    }
    
    public ChangeCollisionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        
    }
    
}
