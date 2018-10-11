/*
 * ------------------------------------------------------------------------
 * Class : Action
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity defining an Action in database
 */
@Entity
@Table(name = Action.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public class Action implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "action";
    /** The author column name */
    public static final String AUTHOR_COLUMN_NAME = "AUTHOR";
    /** The label column name */
    public static final String LABEL_COLUMN_NAME = "LABEL";
    /** The login column name */
    public static final String LOGIN_COLUMN_NAME = "LOGIN";
    
    /**
     * Build a string containing the provided user last name and first name
     * initial. <br>
     * 
     * <pre>
     * &ltLast name&gt &ltFirst name initial in upper case&gt.
     * </pre>
     * 
     * @param pUser
     *            the action author
     * @return the built string
     */
    public static String formatAuthor(User pUser) {
        
        String lResult;
        if (pUser == null || pUser.getLastname() == null) {
            return null;
        }
        
        StringBuffer sb = new StringBuffer(pUser.getLastname());
        
        if (pUser.getFirstname() != null && !pUser.getFirstname().isEmpty()) {
            sb.append(" ").append(pUser.getFirstname().toUpperCase().charAt(0));
            sb.append(".");
        }
        
        lResult = sb.toString();
        return lResult;
    }
    
    /**
     * Add the Creation action to the provided article history. <br>
     * If the article history is null or already contains some actions, do not
     * add any action.
     * 
     * @param pUser
     *            the user creating the article
     * @param pArticle
     *            the article being created
     * @param pCreationType
     *            a comment indicating the creation type
     */
    public static void createCreationAction(User pUser, Article pArticle,
            String pCreationType) {
        
        if (pArticle.getHistory() != null) {
            
            List<Action> lActions = pArticle.getHistory().getActions();
            if (lActions != null && lActions.size() == 0) {
                
                String lMsg = MessageBundle.getMessageDefault(pCreationType);
                String lLogin = (pUser != null) ? pUser.getLogin() : null;
                String lAuthor = Action.formatAuthor(pUser);
                Action lFirstAction =
                        new Action(lLogin, lAuthor, Constants.CREATION,
                                new Comment(lMsg));
                
                pArticle.getHistory().addAction(lFirstAction);
            }
        }
    }
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private String label;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Comment comment;
    
    @Column
    private String login;
    
    @Column
    private String author;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    @Version
    private Long version;
    
    @ManyToOne
    private History history;
    
    /**
     * Constructor
     */
    public Action() {
        super();
        date = new Date();
    }
    
    /**
     * Constructor <br>
     * The date is initialized to current date.
     * 
     * @param login
     *            the author login
     * @param author
     *            the author name
     * @param label
     *            the action label
     * @param comment
     *            the author comment
     */
    public Action(String login, String author, String label, Comment comment) {
        this.login = login;
        this.author = author;
        this.label = label;
        this.comment = comment;
        
        date = new Date();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Action other = (Action) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        }
        else if (!date.equals(other.date)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        }
        else if (!label.equals(other.label)) {
            return false;
        }
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        }
        else if (!login.equals(other.login)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Action [" + (id != null ? "id=" + id + ", " : "")
                + (login != null ? "login=" + login + ", " : "")
                + (author != null ? "author=" + author + ", " : "")
                + (label != null ? "label=" + label + ", " : "")
                + (date != null ? "date=" + date + ", " : "")
                + (version != null ? "version=" + version + ", " : "")
                + (comment != null ? "comment=" + comment : "") + "]";
    }
    
    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }
    
    /**
     * @param login
     *            the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }
    
    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * @param author
     *            the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * @return the comment
     */
    public Comment getComment() {
        return comment;
    }
    
    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(Comment comment) {
        this.comment = comment;
        
    }
    
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * @param date
     *            the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }
    
    /**
     * @return the history
     */
    public History getHistory() {
        return history;
    }
    
    /**
     * @param pHistory
     *            the history to set
     */
    public void setHistory(History pHistory) {
        history = pHistory;
    }
    
}
