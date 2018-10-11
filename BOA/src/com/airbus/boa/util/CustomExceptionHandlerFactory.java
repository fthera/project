/*
 * ------------------------------------------------------------------------
 * Class : CustomExceptionHandlerFactory
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * Custom ExceptionHandlerFactory to use the CustomExceptionHandler
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {
    
    private ExceptionHandlerFactory parent;
    
    /**
     * Constructor
     * 
     *  @param pParent
     *          the parent factory
     */
    public CustomExceptionHandlerFactory(ExceptionHandlerFactory pParent) {
        this.parent = pParent;
    }
    
    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler handler =
                new CustomExceptionHandler(parent.getExceptionHandler());
        return handler;
    }
}
