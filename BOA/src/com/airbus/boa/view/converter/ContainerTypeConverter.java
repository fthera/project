/*
 * ------------------------------------------------------------------------
 * Class : ContainerTypeConverter
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.localization.ContainerType;

/**
 * Converter for ContainerType enumerate
 */
@FacesConverter("containerTypeConverter")
public class ContainerTypeConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        if (value == null) {
            return null;
        }
        
        return ContainerType.getEnumValue(value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        
        return value.toString();
    }
    
}
