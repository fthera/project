/*
 * ------------------------------------------------------------------------
 * Class : ArticleConverter
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.SearchController;


/**
 * Converter for Article objects
 */
@FacesConverter("articleConverter")
public class ArticleConverter implements Converter {
    
    /*
     * (non-Javadoc)
     * @see
     * javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext
     * , javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        
        SearchController controller =
                AbstractController.findBean(SearchController.class);
        return controller.getArticleBean().findArticleById(Long.valueOf(arg2));
    }
    
    /*
     * (non-Javadoc)
     * @see
     * javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext
     * , javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        return ((Article) arg2).getId().toString();
    }
    
}
