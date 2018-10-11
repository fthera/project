/*
 * ------------------------------------------------------------------------
 * Class : FieldModification
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: FieldModification
 */
@Entity
@Table(name = FieldModification.TABLE_NAME)
public class FieldModification extends Action implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "fieldmodification";
    /** The after value column name */
    public static final String AFTERVALUE_COLUMN_NAME = "AFTERVALUE";
    /** The before value column name */
    public static final String BEFOREVALUE_COLUMN_NAME = "BEFOREVALUE";
    /** The field column name */
    public static final String FIELD_COLUMN_NAME = "FIELD";
    
    @Column
    private String field;
    
    @Column
    private String beforeValue;
    
    @Column
    private String afterValue;
    
    public FieldModification() {
        super();
    }
    
    public FieldModification(String field, String beforeValue, String afterValue) {
        super();
        this.field = field;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }
    
    public FieldModification(String login, String author, String label,
            Comment comment, String field, String beforeValue, String afterValue) {
        super(login, author, label, comment);
        this.field = field;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }
    
    @Override
    public String toString() {
        return "FieldModification ["
                + (super.toString() != null ? "toString()=" + super.toString()
                        + ", " : "")
                + (afterValue != null ? "afterValue=" + afterValue + ", " : "")
                + (beforeValue != null ? "beforeValue=" + beforeValue + ", "
                        : "") + (field != null ? "field=" + field : "") + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result =
                prime * result
                        + ((afterValue == null) ? 0 : afterValue.hashCode());
        result =
                prime * result
                        + ((beforeValue == null) ? 0 : beforeValue.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FieldModification other = (FieldModification) obj;
        if (afterValue == null) {
            if (other.afterValue != null) {
                return false;
            }
        }
        else if (!afterValue.equals(other.afterValue)) {
            return false;
        }
        if (beforeValue == null) {
            if (other.beforeValue != null) {
                return false;
            }
        }
        else if (!beforeValue.equals(other.beforeValue)) {
            return false;
        }
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        }
        else if (!field.equals(other.field)) {
            return false;
        }
        return true;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getBeforeValue() {
        return beforeValue;
    }
    
    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }
    
    public String getAfterValue() {
        return afterValue;
    }
    
    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }
    
}
