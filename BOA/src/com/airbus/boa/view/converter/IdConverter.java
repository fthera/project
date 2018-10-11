/*
 * ------------------------------------------------------------------------
 * Class : IdConverter
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter for longs
 */
@FacesConverter("idConverter")
public class IdConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        return Long.parseLong((String) value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        
        return value.toString();
    }
    
}
