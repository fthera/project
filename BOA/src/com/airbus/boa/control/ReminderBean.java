/*
 * ------------------------------------------------------------------------
 * Class : ReminderBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.exception.ValidationException;

/**
 * Bean used to manage Reminders
 */
@Stateless
@LocalBean
public class ReminderBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(ReminderBean.class.getName());
    
    @PersistenceContext(name = "ReminderService")
    private EntityManager em;
    
    /**
     * Persist the reminder in the database
     * 
     * @param pReminder
     *            the reminder to persist
     * @return the persisted reminder (itself)
     */
    public Reminder create(Reminder pReminder) {
        
        em.persist(pReminder);
        
        return pReminder;
    }
    
    /**
     * Merge the provided reminder with the one in database.
     * 
     * @param pReminder
     *            the reminder to merge
     * @return the merged reminder
     */
    public Reminder merge(Reminder pReminder) {
        try {
            
            return em.merge(pReminder);
            
        }
        catch (OptimisticLockException e) {
            log.throwing(this.getClass().getName(), "merge", e);
            throw new ChangeCollisionException(e);
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            
            throw new ValidationException(e);
        }
    }
    
    /**
     * Remove the reminder from the database
     * 
     * @param pReminder
     *            the reminder to remove
     */
    public void remove(Reminder pReminder) {
        
        Reminder lReminder = em.find(Reminder.class, pReminder.getId());
        
        em.remove(lReminder);
    }
    
    /**
     * Search all reminders
     * 
     * @return the found reminders list
     */
    public List<Reminder> findAllReminders() {
        TypedQuery<Reminder> lQuery =
                em.createNamedQuery(Reminder.ALL_REMINDERS_QUERY,
                        Reminder.class);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search the reminder by its id
     * 
     * @param pId
     *            the reminder id to search
     * @return the found reminder, else null
     */
    public Reminder findReminderById(Long pId) {
        if (pId == null) {
            return null;
        }
        return em.find(Reminder.class, pId);
    }
    
    /**
     * Search reminders related to an article
     * 
     * @param pArticle
     *            the searched article
     * @return the found reminders list
     */
    public List<Reminder> findRemindersByArticle(Article pArticle) {
        TypedQuery<Reminder> lQuery =
                em.createNamedQuery(Reminder.REMINDERS_USING_ARTICLE_QUERY,
                        Reminder.class);
        lQuery.setParameter("article", pArticle);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search reminders related to an installation
     * 
     * @param pInstallation
     *            the searched installation
     * @return the found reminders list
     */
    public List<Reminder>
            findRemindersByInstallation(Installation pInstallation) {
        TypedQuery<Reminder> lQuery =
                em.createNamedQuery(
                        Reminder.REMINDERS_USING_INSTALLATION_QUERY,
                        Reminder.class);
        lQuery.setParameter("installation", pInstallation);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search reminders related to a tool
     * 
     * @param pTool
     *            the searched tool
     * @return the found reminders list
     */
    public List<Reminder> findRemindersByTool(Tool pTool) {
        TypedQuery<Reminder> lQuery =
                em.createNamedQuery(Reminder.REMINDERS_USING_TOOL_QUERY,
                        Reminder.class);
        lQuery.setParameter("tool", pTool);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search reminders by their person in charge
     * 
     * @param pUsedBy
     *            the searched person in charge
     * @return the found reminders list
     */
    public List<Reminder> findRemindersByUser(User pUsedBy) {
        
        TypedQuery<Reminder> lQuery =
                em.createNamedQuery(Reminder.REMINDERS_BY_USER_QUERY,
                        Reminder.class);
        lQuery.setParameter("user", pUsedBy);
        
        return lQuery.getResultList();
    }
    
}
