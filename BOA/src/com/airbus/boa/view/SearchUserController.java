/*
 * ------------------------------------------------------------------------
 * Class : UserController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.poi.ss.SpreadsheetVersion;
import org.richfaces.model.Filter;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ReminderBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.io.ExportExcel;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.filter.UserFilterRegex;

/**
 * Controller managing the users (creation, update, deletion)
 */
@ManagedBean(name = SearchUserController.BEAN_NAME)
@SessionScoped
public class SearchUserController extends AbstractController implements Serializable {
    
    private static final long serialVersionUID = -6080496955362597488L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchUserController";
    
    @EJB
    private UserBean userbean;
    
    @EJB
    private PCBean pcbean;
    
    @EJB
    private DemandBean demandBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ReminderBean reminderBean;
    
    private List<User> userList;
    
    private Long userIdToDelete;
    
    private UserFilterRegex userFilterRegex = new UserFilterRegex();
    
    /**
     * Check if the user can be deleted and then delete it from the database
     */
    public void doDeleteUser() {
        
        User lUser = userbean.findUser(userIdToDelete);
        
        // Check that the selected user is the issuer of no demand
        if (!demandBean.findDemandsByIssuer(lUser).isEmpty()) {
            Utils.addFacesMessage(NavigationConstants.LIST_USER_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.USER_DELETION_DEMAND_ISSUER));
        }
        
        // Check that the selected user is not the person in charge of an
        // installation
        else if (!locationBean.findInstallationsByUser(lUser).isEmpty()) {
            Utils.addFacesMessage(NavigationConstants.LIST_USER_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.USER_INSTALLATION_INCHARGE));
        }
        
        // Check that the selected user is not the person in charge of a PC
        else if (pcbean.existByList(lUser)) {
            Utils.addFacesMessage(NavigationConstants.LIST_USER_ERROR_ID,
                    MessageBundle.getMessage(Constants.USER_PC_INCHARGE));
            
        }
        
        else {
        // Remove reminders
            List<Reminder> lReminders = reminderBean.findRemindersByUser(lUser);
            for (Reminder lReminder : lReminders) {
                reminderBean.remove(lReminder);
            }
            
            lUser = userbean.deleteUser(userIdToDelete);
            userList.remove(lUser);
        }
    }
    
    /**
     * Generate and propose to download the list of displayed users in an Excel
     * file
     */
    public void doExport() {
        
        List<User> results = Collections.emptyList();
        if (null != userList && (!userList.isEmpty())) {
            results = userFilterRegex.getFilteredList(userList);
            
            if (!results.isEmpty()) {
                ExportExcel formExcel =
                        new ExportExcel(SpreadsheetVersion.EXCEL2007);
                
                formExcel.writeListResult(results);
                formExcel.applyStyles();
                
                download(formExcel, "users.xlsx",
                        ExportController.MIMETYPE_XLSX);
            }
        }
    }
    
    /**
     * Navigate to the list of users
     */
    public void doListUsers() {
        userList = userbean.findUsers();
    }
    
    /**
     * Reset filters
     */
    public void doResetFilter() {
        userFilterRegex.resetFilters();
    }
    
    /**
     * Return the filter for installations
     * 
     * @return the requested filter object
     */
    public Filter<?> getUserFilter() {
        return new Filter<User>() {
            
            public boolean accept(User item) {
                return userFilterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * Check that the provided email is valid.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponentToValidate
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the email to validate
     * @throws ValidatorException
     *             when the provided email is not valid
     */
    public void validateEMail(FacesContext pContext,
            UIComponent pComponentToValidate, Object pValue)
            throws ValidatorException {
        String email = (String) pValue;
        
        Pattern pattern = Pattern.compile(Constants.REGEX_USER_EMAIL);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            String msg = MessageBundle.getMessage(Constants.VALIDATE_EMAIL);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }
    
    /**
     * @return the countFiltered
     */
    public Integer getCountFiltered() {
        return userFilterRegex.countFiltered(getUserList());
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return userFilterRegex.getFilterValues();
    }
    
    /**
     * @param filterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> filterValues) {
        userFilterRegex.setFilterValues(filterValues);
    }
    
    /**
     * @return the userIdToDelete
     */
    public Long getUserIdToDelete() {
        return userIdToDelete;
    }
    
    /**
     * @param userIdToDelete
     *            the userIdToDelete to set
     */
    public void setUserIdToDelete(Long userIdToDelete) {
        this.userIdToDelete = userIdToDelete;
    }
    
    /**
     * @return the userList
     */
    public List<User> getUserList() {
        userList = userbean.findUsers();
        return userList;
    }
    
    /**
     * @return the login of the user to delete for display
     */
    public String getUserLoginToDelete() {
        
        User lUser = userbean.findUser(userIdToDelete);
        if (lUser != null) {
            return lUser.getLoginDetails();
        }
        else {
            return null;
        }
    }
    
    /**
     * @return the list of reminders of the user to delete
     */
    public List<Reminder> getUserReminders() {
        
        User lUser = userbean.findUser(userIdToDelete);
        List<Reminder> lReminders = new ArrayList<Reminder>();
        
        if (lUser != null) {
            lReminders = reminderBean.findRemindersByUser(lUser);
        }
        return lReminders;
    }
    
}
