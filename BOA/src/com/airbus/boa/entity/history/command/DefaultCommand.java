/*
 * ------------------------------------------------------------------------
 * Class : DefaultCommand
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history.command;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.airbus.boa.entity.article.Article;

public class DefaultCommand implements Command, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    protected Object receiver;
    protected Method method;
    protected Object[] args;
    
    public DefaultCommand(Object receiver, Method method, Object[] args) {
        super();
        this.receiver = receiver;
        this.method = method;
        this.args = args;
    }
    
    public DefaultCommand(Article article, String methodName, Object[] args) {
        super();
        receiver = article;
    }
    
    @Override
    public void execute() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        
        method.invoke(receiver, args);
        
    }
    
    @Override
    public Object getReceiver() {
        return receiver;
    }
    
    @Override
    public Object getValue() {
        return args;
    }
    
}
