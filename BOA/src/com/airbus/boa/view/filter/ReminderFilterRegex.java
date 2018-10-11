/*
 * ------------------------------------------------------------------------
 * Class : ReminderFilterRegex
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.reminder.ReminderStatus;
import com.airbus.boa.service.Constants;

/**
 * Filter for reminders
 */
public class ReminderFilterRegex extends FilterRegexSupport<Reminder> {
    
    private static final long serialVersionUID = 1L;
    
    private ReminderStatus filterStatus = null;
    private Boolean filterCompleted = null;
    
    @Override
    public Integer countFiltered(List<Reminder> listItems) {
        
        Integer lCount = 0;
        for (Reminder lReminder : listItems) {
            if (filterMethodRegex(lReminder) && filterMethodStatus(lReminder)
                    && filterMethodCompleted(lReminder)) {
                lCount++;
            }
        }
        return lCount;
    }
    
    @Override
    public void resetFilters() {
        super.resetFilters();
        filterStatus = null;
        filterCompleted = null;
        setResetFilters(true);
    }
    
    /**
     * Apply the status filter to the provided Reminder
     * 
     * @param pCurrentReminder
     *            the reminder on which to apply the filter
     * @return a boolean indicating if the filter criteria is satisfied or not
     */
    public boolean filterMethodStatus(Reminder pCurrentReminder) {
        
        if (filterStatus == null || pCurrentReminder == null) {
            return true;
        }
        
        ReminderStatus lStatus = pCurrentReminder.getReminderStatus();
        if (lStatus == null) {
            return false;
        }
        
        return lStatus.equals(filterStatus);
    }
    
    /**
     * @param pReminder
     *            the current reminder to filter
     * @return true if the reminder satisfies the completed criterion, else
     *         false
     */
    public Boolean filterMethodCompleted(Reminder pReminder) {
        
        if (pReminder == null || filterCompleted == null) {
            return true;
        }
        return filterCompleted.equals(pReminder.isCompleted());
    }
    
    @Override
    public Boolean filterMethodRegex(Reminder pCurrent) {
        
        Boolean lResult = true;
        
        String lFilter = getFilterValues().get("itemName");
        String lString = pCurrent.getItem().getName();
        lResult = compare(lString, lFilter);
        
        if (lResult) {
            lFilter = getFilterValues().get("user");
            if (pCurrent.getUser() != null) {
                lString = pCurrent.getUser().getLoginDetails();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("object");
            lString = pCurrent.getObject();
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("targetDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (pCurrent.getTargetDate() != null) {
                lString = lDateFormat.format(pCurrent.getTargetDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        setResetFilters(filterValues.isEmpty() && filterStatus == null
                && filterCompleted == null);
        
        return lResult;
    }
    
    /**
     * @return the filterStatus
     */
    public ReminderStatus getFilterStatus() {
        return filterStatus;
    }
    
    /**
     * @param pFilterStatus
     *            the filterStatus to set
     */
    public void setFilterStatus(ReminderStatus pFilterStatus) {
        filterStatus = pFilterStatus;
    }
    
    /**
     * @return the filterCompleted
     */
    public Boolean getFilterCompleted() {
        return filterCompleted;
    }
    
    /**
     * @param pFilterCompleted
     *            the filterCompleted to set
     */
    public void setFilterCompleted(Boolean pFilterCompleted) {
        filterCompleted = pFilterCompleted;
    }
    
}
