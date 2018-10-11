/*
 * ------------------------------------------------------------------------
 * Class : Comment
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = Comment.TABLE_NAME)
public class Comment implements Serializable {
    
    private static final long serialVersionUID = -4033848319011924888L;
    
    /** The table name */
    public static final String TABLE_NAME = "comment";
    /** The message column name */
    public static final String MESSAGE_COLUMN_NAME = "MESSAGE";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private String message = "";
    
    @Version
    private Long version;
    
    public Comment() {
        super();
        
    }
    
    public Comment(String message) {
        super();
        this.message = removeCarriageReturn(message);
    }
    
    @Override
    public String toString() {
        
        return message;
    }
    
    private String removeCarriageReturn(String message) {
        if (message != null) {
            message.replaceAll("\r", "");
        }
        return message;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = removeCarriageReturn(message);
    }
    
}
