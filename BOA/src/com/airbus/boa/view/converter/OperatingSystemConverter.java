/*
 * ------------------------------------------------------------------------
 * Class : OperatingSystemConverter
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Converter for Software Operating System boolean
 */
@FacesConverter("operatingSystemConverter")
public class OperatingSystemConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String pOperatingSystem) {
        
        if (pOperatingSystem != null) {
            if (pOperatingSystem
                    .equals(MessageBundle.getMessage(Constants.YES))) {
                return true;
            }
            if (pOperatingSystem
                    .equals(MessageBundle.getMessage(Constants.NO))) {
                return false;
            }
            else {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object pOperatingSystem) {
        
        if (pOperatingSystem != null) {
            if (pOperatingSystem.equals(true)) {
                return MessageBundle.getMessage(Constants.YES);
            }
            else {
                return MessageBundle.getMessage(Constants.NO);
            }
        }
        return null;
    }
    
    /**
     * @param pOperatingSystem
     *            the operating system Boolean to convert in String form
     * @return the String value corresponding to the provided value, or null
     */
    public static String getAsString(Object pOperatingSystem) {
        
        if (pOperatingSystem != null) {
            if (pOperatingSystem.equals(true)) {
                return MessageBundle.getMessage(Constants.YES);
            }
            else {
                return MessageBundle.getMessage(Constants.NO);
            }
        }
        return null;
    }
    
}
