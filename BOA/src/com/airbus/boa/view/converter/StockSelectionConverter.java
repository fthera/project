/*
 * ------------------------------------------------------------------------
 * Class : StockSelectionConverter
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.LogInController;

/**
 * Converter for Stock selection objects
 */
@FacesConverter("stockSelectionConverter")
public class StockSelectionConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        LogInController lController =
                AbstractController.findBean(LogInController.class);
        return lController.getUserBean()
                .findStockSelection(lController.getUserLogged(), value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        return value.toString();
    }
    
}
