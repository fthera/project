/*
 * ------------------------------------------------------------------------
 * Class : DatedComment
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.airbus.boa.entity.EntityBase;
import com.airbus.boa.entity.user.User;

/**
 * Entity implementation class for Entity: DatedComment
 */
@Entity
@Table(name = DatedComment.TABLE_NAME)
public class DatedComment implements Serializable, EntityBase {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "dated_comment";
    
    /** The comment column name */
    public static final String COMMENT_COLUMN_NAME = "COMMENT";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ARTICLE_ID")
    private Article article;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @Column(name = "COMMENT")
    private String comment;
    
    @Column(name = "DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the function to id
     */
    public void setId(Long pId) {
        id = pId;
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
    /**
     * @param pArticle
     *            the article to id
     */
    public void setArticle(Article pArticle) {
        article = pArticle;
    }
    
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * @param pUser
     *            the user to id
     */
    public void setUser(User pUser) {
        user = pUser;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param pComment
     *            the comment to id
     */
    public void setComment(String pComment) {
        comment = pComment;
    }
    
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * @param pDate
     *            the date to id
     */
    public void setDate(Date pDate) {
        date = pDate;
    }
}
