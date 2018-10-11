/*
 * ------------------------------------------------------------------------
 * Class : ReminderController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.richfaces.model.Filter;

import com.airbus.boa.control.ReminderBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.reminder.ReminderStatus;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.filter.ReminderFilterRegex;

/**
 * Controller managing the Reminder creation, update and deletion
 */
@ManagedBean(name = ReminderController.BEAN_NAME)
@ViewScoped
public class ReminderController extends AbstractController implements
        Serializable {
    
    private enum ModalMode {
        ADD,
        UPDATE
    }
    
    private enum Mode {
        CONSULTATION_USER,
        CONSULTATION_ARTICLE,
        CONSULTATION_INSTALLATION,
        CONSULTATION_TOOL
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "reminderController";
    
    /** The User id GET parameter name */
    public static final String USER_ID_PARAM_NAME = "pUserId";
    
    @EJB
    private ReminderBean reminderBean;
    
    @EJB
    private UserBean userBean;
    
    private Mode mode = Mode.CONSULTATION_ARTICLE;
    private ModalMode modalMode = ModalMode.ADD;
    
    private List<Reminder> reminders = new ArrayList<Reminder>();
    
    private ReminderFilterRegex filterRegex = new ReminderFilterRegex();
    
    /**
     * The selected reminder (in add/modify/delete/complete actions)
     */
    private Reminder reminder = null;
    
    /** The article for which to display the reminders */
    private Article article = null;
    /** The installation for which to display the reminders */
    private Installation installation = null;
    /** The tool for which to display the reminders */
    private Tool tool = null;
    
    /** The user for which to display the reminders */
    private User user = null;
    
    /** The selected user id in the modal */
    private Long userId = null;
    
    /**
     * Create the current reminder
     */
    public void doAddReminder() {
        User lUser = userBean.findUser(userId);
        reminder.setUser(lUser);
        reminderBean.create(reminder);
        updateReminders();
    }
    
    /**
     * Delete the selected reminder
     */
    public void doDeleteReminder() {
        if (reminder != null) {
            reminderBean.remove(reminder);
        }
        updateReminders();
    }
    
    /**
     * Merge the selected reminder
     * 
     * @param pReminder
     *            the selected Reminder
     */
    public void doMergeReminder(Reminder pReminder) {
        reminderBean.merge(pReminder);
        updateReminders();
    }
    
    /**
     * Reset filters
     */
    public void doResetFilters() {
        filterRegex.resetFilters();
    }
    
    /**
     * Perform the current action of the modal according to the current modal
     * mode (ADD or UPDATE)
     */
    public void doSubmitModal() {
        
        try {
            switch (modalMode) {
            
            case ADD:
                doAddReminder();
                break;
            
            case UPDATE:
                doUpdateReminder();
                break;
            
            default:
                break;
            }
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.REMINDER_ERROR_ID,
                    "An error occurs: " + ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Update the current reminder
     */
    public void doUpdateReminder() {
        User lUser = userBean.findUser(userId);
        reminder.setUser(lUser);
        reminderBean.merge(reminder);
        updateReminders();
    }
    
    /**
     * Return the filter for reminder completed attributes
     * 
     * @return the requested filter object
     */    
    public Filter<?> getCompletedFilter() {
        return new Filter<Reminder>() {
            public boolean accept(Reminder item) {
            	return filterRegex.filterMethodCompleted(item);
            }
        };
    }
    
    /**
     * Return the filter for reminders
     * 
     * @return the requested filter object
     */    
    public Filter<?> getReminderFilter() {
        return new Filter<Reminder>() {
            public boolean accept(Reminder item) {
            	return filterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * Return the filter for reminder statuses
     * 
     * @return the requested filter object
     */    
    public Filter<?> getStatusFilter() {
        return new Filter<Reminder>() {
            public boolean accept(Reminder item) {
            	return filterRegex.filterMethodStatus(item);
            }
        };
    }
    
    /**
     * Initialize attributes
     */
    @PostConstruct
    public void init() {
        
        Map<String, String> lParametersMap =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap();
        // Check if the user id parameter is defined in request
        if (lParametersMap != null
                && lParametersMap.containsKey(USER_ID_PARAM_NAME)) {
            
            String lUserIdString = lParametersMap.get(USER_ID_PARAM_NAME);
            if (lUserIdString != null) {
                try {
                    Long lUserId = Long.valueOf(lUserIdString);
                    if (lUserId != null) {
                        // search the user
                        User lUser = userBean.findUser(lUserId);
                        if (lUser != null) {
                            user = lUser;
                            mode = Mode.CONSULTATION_USER;
                            updateReminders();
                        }
                    }
                }
                catch (NumberFormatException e) {
                    // Nothing to do
                    // The parameter is not correct but optional:
                    // it is not taken into account
                }
            }
        }
    }
    
    public void setMode(Object lItem) {
        if (lItem instanceof Article) {
            article = (Article) lItem;
            mode = Mode.CONSULTATION_ARTICLE;
        }
        else if (lItem instanceof Installation) {
            installation = (Installation) lItem;
            mode = Mode.CONSULTATION_INSTALLATION;
        }
        else if (lItem instanceof Tool) {
            tool = (Tool) lItem;
            mode = Mode.CONSULTATION_TOOL;
        }
        else if (lItem instanceof User) {
            user = (User) lItem;
            mode = Mode.CONSULTATION_USER;
        }
        updateReminders();
    }
    
    /**
     * Prepare the view to create a new reminder.
     */
    public void prepareAdd() {
        // retrieving user
        LogInController lLogInController = findBean(LogInController.class);
        User lUserLogged = lLogInController.getUserLogged();
        
        if (article != null) {
            reminder = new Reminder(article, lUserLogged);
        }
        else if (installation != null) {
            reminder = new Reminder(installation, lUserLogged);
        }
        else if (tool != null) {
            reminder = new Reminder(tool, lUserLogged);
        }
        
        modalMode = ModalMode.ADD;
        
        if (reminder.getUser() != null) {
            userId = reminder.getUser().getId();
        }
    }
    
    /**
     * Update the modal for updating the selected reminder
     * 
     * @param event
     *            event sent when clicking the edit icon of a line
     */
    public void prepareUpdate(ActionEvent event) {
        modalMode = ModalMode.UPDATE;
    }
    
    private void updateReminders() {
        
        switch (mode) {
        case CONSULTATION_ARTICLE:
            reminders = reminderBean.findRemindersByArticle(article);
            break;
        case CONSULTATION_INSTALLATION:
            reminders = reminderBean.findRemindersByInstallation(installation);
            break;
        case CONSULTATION_TOOL:
            reminders = reminderBean.findRemindersByTool(tool);
            break;
        case CONSULTATION_USER:
            reminders = reminderBean.findRemindersByUser(user);
            break;
        default:
            reminders = new ArrayList<Reminder>();
            break;
        }
    }
    
    /**
     * @return the select values for completed attribute filter
     */
    public SelectItem[] getCompletedValues() {
        
        SelectItem[] lSelectItems = new SelectItem[2];
        lSelectItems[0] =
                new SelectItem(Boolean.TRUE,
                        MessageBundle.getMessage(Constants.YES));
        lSelectItems[1] =
                new SelectItem(Boolean.FALSE,
                        MessageBundle.getMessage(Constants.NO));
        
        return lSelectItems;
    }
    
    /**
     * Update the countFilteredReminder with the number of reminders filtered
     * 
     * @return the countFilteredReminder
     */
    public Integer getCountFilteredReminder() {
        return filterRegex.countFiltered(reminders);
    }
    
    /**
     * @return the filterCompleted
     */
    public Boolean getFilterCompleted() {
        return filterRegex.getFilterCompleted();
    }
    
    /**
     * @param pFilterCompleted
     *            the filterCompleted to set
     */
    public void setFilterCompleted(Boolean pFilterCompleted) {
        filterRegex.setFilterCompleted(pFilterCompleted);
    }
    
    /**
     * @return the filterStatus
     */
    public ReminderStatus getFilterStatus() {
        return filterRegex.getFilterStatus();
    }
    
    /**
     * @param filterStatus
     *            the filterStatus to set
     */
    public void setFilterStatus(ReminderStatus filterStatus) {
        filterRegex.setFilterStatus(filterStatus);
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return filterRegex.getFilterValues();
    }
    
    /**
     * @param pFilterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> pFilterValues) {
        filterRegex.setFilterValues(pFilterValues);
    }
    
    /**
     * @return true if the modal mode is ADD
     */
    public boolean isModalModeAdd() {
        return modalMode == ModalMode.ADD;
    }
    
    /**
     * @return true if the mode is the consultation of an item
     */
    public boolean isModeConsultationItem() {
        switch (mode) {
        case CONSULTATION_ARTICLE:
        case CONSULTATION_INSTALLATION:
        case CONSULTATION_TOOL:
            return true;
            
        case CONSULTATION_USER:
        default:
            return false;
        }
    }
    
    /**
     * @return true if the mode is the consultation of a user
     */
    public boolean isModeConsultationUser() {
        switch (mode) {
        case CONSULTATION_USER:
            return true;
            
        case CONSULTATION_ARTICLE:
        case CONSULTATION_INSTALLATION:
        case CONSULTATION_TOOL:
        default:
            return false;
        }
    }
    
    /**
     * @return the reminder
     */
    public Reminder getReminder() {
        return reminder;
    }
    
    /**
     * @param pReminder
     *            the reminder to set
     */
    public void setReminder(Reminder pReminder) {
        reminder = pReminder;
        if (reminder != null && reminder.getUser() != null) {
            userId = reminder.getUser().getId();
        }
        else {
            userId = null;
        }
    }
    
    /**
     * @return the reminders
     */
    public List<Reminder> getReminders() {
        return reminders;
    }
    
    /**
     * @return the available status for selection list
     */
    public SelectItem[] getSelectStatusValues() {
        
        int lLength = ReminderStatus.values().length;
        SelectItem[] lStatusItems = new SelectItem[lLength];
        ReminderStatus[] lValues = ReminderStatus.values();
        for (int i = 0; i < lLength; i++) {
            
            lStatusItems[i] = new SelectItem(lValues[i], lValues[i].toString());
        }
        
        return lStatusItems;
    }
    
    /**
     * @return the inCharges
     */
    public List<SelectItem> getSelectUsers() {
        List<User> lUsers = userBean.findUsers();
        
        List<SelectItem> lSelectUsers = new ArrayList<SelectItem>();
        for (User lUser : lUsers) {
            lSelectUsers.add(new SelectItem(lUser.getId(), lUser
                    .getLoginDetails()));
        }
        return lSelectUsers;
    }
    
    /**
     * @return the userId
     */
    public User getUser() {
        return user;
    }
    
    /**
     * @param pUserId
     *            the userId to set
     */
    public void setUser(User pUser) {
        user = pUser;
    }
    
    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * @param pUserId
     *            the userId to set
     */
    public void setUserId(Long pUserId) {
        userId = pUserId;
    }
    
}
