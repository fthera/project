/*
 * ------------------------------------------------------------------------
 * Class : TypeArticleConverter
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.SearchController;

/**
 * Converter for TypeArticle objects
 */
@FacesConverter("typeArticleConverter")
public class TypeArticleConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        SearchController controller =
                AbstractController.findBean(SearchController.class);
        return controller.getArticleBean().findTypeArticleByName(arg2);
        
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        TypeArticle typeArticle = (TypeArticle) value;
        return typeArticle.toString();
    }
    
}
