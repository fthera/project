/*
 * ------------------------------------------------------------------------
 * Class : CustomExceptionHandler
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.util.HashMap;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import com.airbus.boa.service.NavigationConstants;

/**
 * Custom ExceptionHandler to handle ViewExpiredException exceptions
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {
    private ExceptionHandler wrapped;

    /**
     * Constructor
     * 
     * @param pWrapped
     *            the wrapped handler
     */
    public CustomExceptionHandler(ExceptionHandler pWrapped) {
        this.wrapped = pWrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {
        FacesContext context = FacesContext.getCurrentInstance();
        
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext exceptionContext =
                    (ExceptionQueuedEventContext) event.getSource();
            Throwable t = exceptionContext.getException();
            if (t instanceof ViewExpiredException) {
                HashMap<String, String> params = new HashMap<String, String>();
                String returnPage = context.getExternalContext()
                        .getRequestHeaderMap().get("referer");
                if (returnPage == null) {
                    returnPage = NavigationConstants.MAIN_PAGE;
                }
                params.put("ret", returnPage);
                NavigationUtil.goTo(NavigationConstants.SESSION_EXPIRED_PAGE,
                        params);
                i.remove();
            }
        }

        // At this point, the queue will not contain any ViewExpiredEvents. Therefore, let the parent handle them.
        getWrapped().handle();
    }
}
