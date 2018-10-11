/*
 * ------------------------------------------------------------------------
 * Class : AvailabilityStatusConverter
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.article.PC;

/**
 * Converter for AvailabilityStatus enumerate
 */
@FacesConverter("availabilityStatusConverter")
public class AvailabilityStatusConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        if (value == null) {
            return null;
        }
        
        return PC.AvailabilityStatus.getEnumValue(value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        
        return value.toString();
    }
    
}
