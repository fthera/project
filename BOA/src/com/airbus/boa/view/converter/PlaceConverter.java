/*
 * ------------------------------------------------------------------------
 * Class : PlaceConverter
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.location.Place;
import com.airbus.boa.view.SearchController;

/**
 * Converter for Place class
 */
@FacesConverter("placeConverter")
public class PlaceConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        
        FacesContext lContext = FacesContext.getCurrentInstance();
        SearchController lSearchController =
                lContext.getApplication().evaluateExpressionGet(lContext,
                        "#{searchController}", SearchController.class);
        return lSearchController.getLocationBean()
                .findPlaceByCompleteName(arg2);
    }
    
    @Override
    public String
            getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        return ((Place) value).getCompleteName();
    }
    
}