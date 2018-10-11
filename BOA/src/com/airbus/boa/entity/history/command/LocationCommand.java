/*
 * ------------------------------------------------------------------------
 * Class : LocationCommand
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history.command;

import java.lang.reflect.InvocationTargetException;

import com.airbus.boa.entity.article.Article;

/**
 * Class managing the location modifications
 */
public class LocationCommand extends ArticleCommand {
    
    private static final long serialVersionUID = -6998125520634969930L;
    
    /**
     * Constructor
     */
    public LocationCommand() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param pArticle
     *            the article
     * @param pValue
     *            the location
     */
    public LocationCommand(Article pArticle, Object pValue) {
        super(pArticle, pValue);
        
    }
    
    @Override
    public void execute() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        
    }
    
}
