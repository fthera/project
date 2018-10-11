/*
 * ------------------------------------------------------------------------
 * Class : LocationForArticle
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Article;

/**
 * Entity implementation class for relation: location of Articles
 */
@Entity
public class LocationForArticle extends LocationOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @OneToOne
    private Article article;
    
    /**
     * Constructor
     */
    public LocationForArticle() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pArticle
     *            the article
     * @param pInherited
     *            the inherited value
     * @param pPlace
     *            the location
     * @param pExternalEntity
     *            the external location
     * @param pPreciseLocation
     *            the details on location
     */
    public LocationForArticle(Article pArticle, boolean pInherited,
            Place pPlace, ExternalEntity pExternalEntity,
            String pPreciseLocation) {
        
        super(pInherited, pPlace, pExternalEntity, pPreciseLocation);
        article = pArticle;
    }
    
    @Override
    protected String getToStringDetails() {
        return "article=" + article;
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
}
