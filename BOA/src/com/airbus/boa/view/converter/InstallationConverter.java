/*
 * ------------------------------------------------------------------------
 * Class : InstallationConverter
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.view.SearchController;

/**
 * Converter for Installation objects
 */
@FacesConverter("installationConverter")
public class InstallationConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        
        FacesContext context = FacesContext.getCurrentInstance();
        SearchController controller =
                context.getApplication().evaluateExpressionGet(context,
                        "#{searchController}", SearchController.class);
        return controller.getLocationBean().findInstallationByName(arg2);
        
    }
    
    @Override
    public String
            getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        return ((Installation) value).getName();
    }
    
}