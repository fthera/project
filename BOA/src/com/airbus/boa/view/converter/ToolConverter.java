/*
 * ------------------------------------------------------------------------
 * Class : ToolConverter
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.SearchController;

/**
 * Converter for Article class
 */
@FacesConverter("toolConverter")
public class ToolConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        
        SearchController lController =
                AbstractController.findBean(SearchController.class);
        return lController.getLocationBean().findToolById(Long.valueOf(arg2));
    }
    
    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        return ((Tool) arg2).getId().toString();
    }
    
}
