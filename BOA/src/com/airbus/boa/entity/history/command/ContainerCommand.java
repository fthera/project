/*
 * ------------------------------------------------------------------------
 * Class : ContainerCommand
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history.command;

import java.lang.reflect.InvocationTargetException;

import com.airbus.boa.entity.article.Article;

/**
 * Class managing the container modifications
 */
public class ContainerCommand extends ArticleCommand {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor
     */
    public ContainerCommand() {
        super();
        
    }
    
    /**
     * Constructor
     * 
     * @param pArticle
     *            the article
     * @param pValue
     *            the container
     */
    public ContainerCommand(Article pArticle, Object pValue) {
        super(pArticle, pValue);
        
    }
    
    @Override
    public void execute() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        
    }
    
}
