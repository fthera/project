/*
 * ------------------------------------------------------------------------
 * Class : ComparatorNameArticle
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.service;

import java.util.Comparator;

import com.airbus.boa.entity.article.Article;

/**
 * @author ng0057cf
 */
public class ComparatorNameArticle implements Comparator<Article> {
    
    /*
     * (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Article arg0, Article arg1) {
        if (arg0.getName() == null && arg1.getName() == null) {
            return 0;
        }
        
        String name0 = arg0.getName();
        if (name0 == null) {
            return -1;
        }
        
        String name1 = arg1.getName();
        if (name1 == null) {
            return 1;
        }
        return name0.compareTo(name1);
    }
    
}
