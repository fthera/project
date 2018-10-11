/*
 * ------------------------------------------------------------------------
 * Class : ExceptionUtil
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import com.airbus.boa.exception.ValidationException;

public class ExceptionUtil {
    
    private ExceptionUtil() {
        super();
    }
    
    public static Throwable getRootCause(Throwable t) {
        
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
    
    public static String getMessage(Throwable t) {
        String msg =
                (t.getMessage() != null) ? t.getMessage() : getRootCause(t)
                        .getMessage();
        return msg;
    }
    
    public static String getComponentId(Throwable t) {
        String componentId = null;
        if (t instanceof ValidationException)
            componentId = ((ValidationException) t).getComponentId();
        return componentId;
    }

}
