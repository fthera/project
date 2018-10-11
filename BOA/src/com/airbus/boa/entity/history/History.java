/*
 * ------------------------------------------------------------------------
 * Class : History
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Entity implementation class for Entity: History
 */
@Entity
@Table(name = "history")
public class History implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            orphanRemoval = true)
    @JoinColumn(name = "GENERAL_COMMENT_ID")
    private Comment generalComment;
    
    @OrderBy("date DESC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,
            orphanRemoval = true)
    @JoinColumn(name = "HISTORY_ID")
    private List<Action> actions = new ArrayList<Action>();
    
    @Version
    private Long version;
    
    /**
     * Add an action
     * 
     * @param action
     *            the action to add
     */
    public void addAction(Action action) {
        actions.add(action);
    }
    
    @Override
    public String toString() {
        return "History ["
                + (id != null ? "id=" + id + ", " : "")
                + (version != null ? "version=" + version + ", " : "")
                + (generalComment != null ? "generalComment=" + generalComment
                        + ", " : "")
                + (actions != null ? "actions=" + actions : "") + "]";
    }
    
    /**
     * @return the actions
     */
    public List<Action> getActions() {
        return actions;
    }
    
    /**
     * @param actions
     *            the actions to set
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
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
     * @return the generalComment
     */
    public Comment getGeneralComment() {
        return generalComment;
    }
    
    /**
     * @param generalComment
     *            the generalComment to set
     */
    public void setGeneralComment(Comment generalComment) {
        this.generalComment = generalComment;
        
    }
    
}
