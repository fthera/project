/*
 * ------------------------------------------------------------------------
 * Class : Contains_Tool_Article
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Article;

/**
 * Entity implementation class for relation: Tool contains Articles
 */
@Entity
public class Contains_Tool_Article extends ContainerOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The comment column name */
    public static final String COMMENT_COLUMN_NAME = "COMMENT";
    
    @Column
    private String comment;
    
    @ManyToOne
    private Tool tool;
    
    @OneToOne
    private Article article;
    
    /**
     * Constructor
     */
    public Contains_Tool_Article() {
    }
    
    /**
     * Constructor
     * 
     * @param pTool
     *            the tool containing the article
     * @param pArticle
     *            the article contained by the tool
     * @param pComment
     *            the relation comment
     */
    public Contains_Tool_Article(Tool pTool, Article pArticle, String pComment) {
        
        tool = pTool;
        article = pArticle;
        comment = pComment;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param pComment
     *            the comment to set
     */
    public void setComment(String pComment) {
        comment = pComment;
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }
    
    /**
     * @param pTool
     *            the tool to set
     */
    public void setTool(Tool pTool) {
        tool = pTool;
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
    /**
     * @param pArticle
     *            the article to set
     */
    public void setArticle(Article pArticle) {
        article = pArticle;
    }
    
}
