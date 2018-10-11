/*
 * ------------------------------------------------------------------------
 * Class : Command
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history.command;

import java.lang.reflect.InvocationTargetException;

public interface Command {
    
    public void execute() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;
    
    public Object getValue();
    
    public Object getReceiver();
}
