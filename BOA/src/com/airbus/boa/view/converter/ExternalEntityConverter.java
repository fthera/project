/*
 * ------------------------------------------------------------------------
 * Class : ExternalEntityConverter
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.view.SearchController;

/**
 * Converter for ExternalEntity class
 */
@FacesConverter("externalEntityConverter")
public class ExternalEntityConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        
        FacesContext lContext = FacesContext.getCurrentInstance();
        SearchController lSearchController =
                lContext.getApplication().evaluateExpressionGet(lContext,
                        "#{searchController}", SearchController.class);
        return lSearchController.getLocationBean().findExternalEntityByName(
                arg2);
    }
    
    @Override
    public String
            getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        return ((ExternalEntity) value).getName();
    }
    
}