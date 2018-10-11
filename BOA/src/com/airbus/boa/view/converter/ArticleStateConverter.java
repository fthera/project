/*
 * ------------------------------------------------------------------------
 * Class : ArticleStateConverter
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.article.ArticleState;

/**
 * Converter for ArticleState enumerate
 */
@FacesConverter("stateConverter")
public class ArticleStateConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        if (value == null) {
            return null;
        }
        
        return ArticleState.getEnumValue(value);
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        
        return value.toString();
    }
    
}
