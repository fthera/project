/*
 * ------------------------------------------------------------------------
 * Class : Utils
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.sun.faces.component.visit.FullVisitContext;

public class Utils {
    
    private static void addFacesMessage(String componentId,
            FacesMessage.Severity severity, String message, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();
        final String[] componentName = new String[1];
        root.visitTree(new FullVisitContext(context), new VisitCallback() {
            
            @Override
            public VisitResult visit(VisitContext context,
                    UIComponent component) {
                if (component.getId() != null
                        && component.getId().equals(componentId)) {
                    componentName[0] = component.getClientId();
                    return VisitResult.COMPLETE;
                }
                return VisitResult.ACCEPT;
            }
        });
        FacesContext.getCurrentInstance().addMessage(componentName[0],
                new FacesMessage(severity, message, detail));
        
    }
    
    public static void addFacesMessage(String componentId, String message) {
        addFacesMessage(componentId, FacesMessage.SEVERITY_ERROR, message,
                message);
    }
    
    public static void addFacesMessageWithDetail(String componentId,
            String message, String detail) {
        addFacesMessage(componentId, FacesMessage.SEVERITY_ERROR, message,
                detail);
    }
    
    public static void addInfoMessage(String componentId, String message) {
        addFacesMessage(componentId, FacesMessage.SEVERITY_INFO, message,
                message);
    }
    
    public static void addWarningMessage(String componentId, String message) {
        addFacesMessage(componentId, FacesMessage.SEVERITY_WARN, message,
                message);
    }
    
    public static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(true);
    }
    
    private Utils() {
    }
    
}
