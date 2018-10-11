/*
 * ------------------------------------------------------------------------
 * Class : ArticleCommand
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history.command;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import com.airbus.boa.entity.article.Article;

public class ArticleCommand implements Command, Serializable {
    
    private static final long serialVersionUID = 728431816442558871L;
    
    protected Article article;
    protected Object value;
    
    public ArticleCommand() {
    }
    
    public ArticleCommand(Article article, Object value) {
        super();
        this.article = article;
        this.value = value;
    }
    
    @Override
    public void execute() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        
    }
    
    @Override
    public Object getReceiver() {
        
        return article;
    }
    
    @Override
    public Object getValue() {
        return value;
    }
    
}
