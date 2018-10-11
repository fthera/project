/*
 * ------------------------------------------------------------------------
 * Class : PlaceTypeConverter
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.util.MessageBundle;

/**
 * Converter for PlaceType objects
 */
@FacesConverter("placeTypeConverter")
public class PlaceTypeConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        return PlaceType.valueOf(value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        
        return MessageBundle.getMessage("placeType" + value);
    }
    
}
