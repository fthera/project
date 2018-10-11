/*
 * ------------------------------------------------------------------------
 * Class : Reminder
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.reminder;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;

/**
 * Entity implementation class for Entity: Reminder
 */
@Entity
@Table(name = Reminder.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Reminder.ALL_REMINDERS_QUERY,
                query = "SELECT r FROM Reminder r ORDER BY r.targetDate"),
        @NamedQuery(
                name = Reminder.REMINDERS_BY_USER_QUERY,
                query = "SELECT r FROM Reminder r WHERE r.user = :user ORDER BY r.targetDate"),
        @NamedQuery(
                name = Reminder.REMINDERS_USING_ARTICLE_QUERY,
                query = "SELECT r FROM Reminder r WHERE r.article = :article ORDER BY r.targetDate"),
        @NamedQuery(
                name = Reminder.REMINDERS_USING_INSTALLATION_QUERY,
                query = "SELECT r FROM Reminder r WHERE r.installation = :installation ORDER BY r.targetDate"),
        @NamedQuery(
                name = Reminder.REMINDERS_USING_TOOL_QUERY,
                query = "SELECT r FROM Reminder r WHERE r.tool = :tool ORDER BY r.targetDate") })
public class Reminder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "reminder";
    /** The Object column name */
    public static final String OBJECT_COLUMN_NAME = "OBJECT";
    
    /**
     * Name of the named query selecting all Reminders
     */
    public static final String ALL_REMINDERS_QUERY = "allReminders";
    
    /**
     * Name of the named query selecting reminders with the provided user in
     * charge. <br>
     * This query is parameterized:<br>
     * <b>user</b> the user in charge to search
     */
    public static final String REMINDERS_BY_USER_QUERY = "remindersByUser";
    
    /**
     * Name of the named query selecting reminders using the provided Article<br>
     * This query is parameterized:<br>
     * <b>article</b> the article to search
     */
    public static final String REMINDERS_USING_ARTICLE_QUERY =
            "remindersUsingArticle";
    
    /**
     * Name of the named query selecting reminders using the provided
     * Installation<br>
     * This query is parameterized:<br>
     * <b>installation</b> the installation to search
     */
    public static final String REMINDERS_USING_INSTALLATION_QUERY =
            "remindersUsingInstallation";
    
    /**
     * Name of the named query selecting reminders using the provided Tool<br>
     * This query is parameterized:<br>
     * <b>tool</b> the tool to search
     */
    public static final String REMINDERS_USING_TOOL_QUERY =
            "remindersUsingTool";
    
    private static final ReminderStatus DEFAULT_REMINDER_STATUS =
            ReminderStatus.UntilCompleted;
    
    /** the default value for nbDaysNotification */
    private static int DEFAULT_NBDAYSNOTIFICATION = 5;
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date targetDate;
    
    @Column
    private int nbDaysNotification;
    
    @ManyToOne
    @JoinColumn(name = "ARTICLE_ID")
    private Article article = null;
    
    @ManyToOne
    @JoinColumn(name = "INSTALLATION_ID")
    private Installation installation = null;
    
    @ManyToOne
    @JoinColumn(name = "TOOL_ID")
    private Tool tool = null;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user = null;
    
    @Column(name = OBJECT_COLUMN_NAME)
    private String object;
    
    @Column
    private boolean completed;
    
    @Column
    @Enumerated(EnumType.STRING)
    private ReminderStatus reminderStatus = DEFAULT_REMINDER_STATUS;
    
    @Version
    private Long version;
    
    /**
     * Default constructor
     */
    public Reminder() {
        targetDate = new Date();
        nbDaysNotification = DEFAULT_NBDAYSNOTIFICATION;
    }
    
    /**
     * Constructor
     * 
     * @param pArticle
     *            the related article
     * @param pUser
     *            the user in charge
     */
    public Reminder(Article pArticle, User pUser) {
        
        article = pArticle;
        user = pUser;
        targetDate = new Date();
        nbDaysNotification = DEFAULT_NBDAYSNOTIFICATION;
    }
    
    /**
     * Constructor
     * 
     * @param pInstallation
     *            the related installation
     * @param pUser
     *            the user in charge
     */
    public Reminder(Installation pInstallation, User pUser) {
        
        installation = pInstallation;
        user = pUser;
        targetDate = new Date();
        nbDaysNotification = DEFAULT_NBDAYSNOTIFICATION;
    }
    
    /**
     * Constructor
     * 
     * @param pTool
     *            the related tool
     * @param pUser
     *            the user in charge
     */
    public Reminder(Tool pTool, User pUser) {
        
        tool = pTool;
        user = pUser;
        targetDate = new Date();
        nbDaysNotification = DEFAULT_NBDAYSNOTIFICATION;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        if (id != null) {
            result = result + id.hashCode();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        Reminder lOther = (Reminder) pObj;
        if (id == null) {
            if (lOther.id != null) {
                return false;
            }
        }
        else {
            if (!id.equals(lOther.id)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Reminder " + id;
    }
    
    /**
     * @return true if the planned action has to be reminded, else false
     */
    public boolean isToBeReminded() {
        
        if (!completed) {
            switch (reminderStatus) {
            case UntilCompleted:
            case Once:
                
                Date lToday = new Date();
                
                Calendar lCalendar = Calendar.getInstance();
                lCalendar.setTime(targetDate);
                lCalendar.add(Calendar.DATE, -nbDaysNotification);
                
                if (!lToday.before(lCalendar.getTime())) {
                    return true;
                }
                break;
            
            case NoMoreSent:
                return false;
                
            default:
                break;
            }
        }
        
        return false;
    }
    
    /**
     * Store, if necessary, the fact that a remind has been sent
     */
    public void notifyRemind() {
        
        switch (reminderStatus) {
        case UntilCompleted:
            break;
        
        case Once:
            reminderStatus = ReminderStatus.NoMoreSent;
            break;
        
        case NoMoreSent:
            break;
        
        default:
            break;
        }
    }
    
    /**
     * @return the associated item
     */
    public Item getItem() {
        
        if (article != null) {
            return article;
        }
        else if (installation != null) {
            return installation;
        }
        else if (tool != null) {
            return tool;
        }
        return null;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the id to set
     */
    public void setId(Long pId) {
        id = pId;
    }
    
    /**
     * @return the targetDate
     */
    public Date getTargetDate() {
        return targetDate;
    }
    
    /**
     * @param pTargetDate
     *            the targetDate to set
     */
    public void setTargetDate(Date pTargetDate) {
        targetDate = pTargetDate;
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
    
    /**
     * @return the installation
     */
    public Installation getInstallation() {
        return installation;
    }
    
    /**
     * @param pInstallation
     *            the installation to set
     */
    public void setInstallation(Installation pInstallation) {
        installation = pInstallation;
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
     * @return the object
     */
    public String getObject() {
        return object;
    }
    
    /**
     * @return the object escaped
     */
    public String getObjectEscaped() {
        return object.replace("\n", "\\n");
    }
    
    /**
     * @param pObject
     *            the object to set
     */
    public void setObject(String pObject) {
        object = pObject;
    }
    
    /**
     * @return the completed
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * @param pCompleted
     *            the completed to set
     */
    public void setCompleted(boolean pCompleted) {
        completed = pCompleted;
    }
    
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * @param pUser
     *            the user to set
     */
    public void setUser(User pUser) {
        user = pUser;
    }
    
    /**
     * @return the nbDaysNotification
     */
    public int getNbDaysNotification() {
        return nbDaysNotification;
    }
    
    /**
     * @param pNbDaysNotification
     *            the nbDaysNotification to set
     */
    public void setNbDaysNotification(int pNbDaysNotification) {
        nbDaysNotification = pNbDaysNotification;
    }
    
    /**
     * @return the reminderStatus
     */
    public ReminderStatus getReminderStatus() {
        return reminderStatus;
    }
    
    /**
     * @param pReminderStatus
     *            the reminderStatus to set
     */
    public void setReminderStatus(ReminderStatus pReminderStatus) {
        reminderStatus = pReminderStatus;
    }
    
}
