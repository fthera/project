/*
 * ------------------------------------------------------------------------
 * Class : ReminderSendingController
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import com.airbus.boa.control.ReminderBean;
import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.util.MailUtil;
import com.airbus.boa.view.application.SupportURLController;

/**
 * Controller managing the Reminder sending
 */
@ManagedBean(name = ReminderSendingController.BEAN_NAME)
@RequestScoped
public class ReminderSendingController extends AbstractController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "reminderSendingController";
    
    @EJB
    private ReminderBean reminderBean;
    
    private StringBuffer logBuffer = null;
    
    private String from = "";
    
    /**
     * Map containing the reminders to remind:<br>
     * it is a map containing as key the user and as value a map containing
     * itself as key the item (owning the reminder) and as value the list of
     * reminders.<br>
     * Only reminders to be reminded are put in the map.
     */
    private Map<User, Map<Item, List<Reminder>>> remindersMap =
            new HashMap<User, Map<Item, List<Reminder>>>();
    
    /**
     * Initialize the reminders map
     */
    private void buildRemindersMap() {
        
        List<Reminder> lAllReminders = reminderBean.findAllReminders();
        
        for (Reminder lReminder : lAllReminders) {
            // Check if reminder is to be reminded
            if (lReminder.isToBeReminded()) {
                
                User lUser = lReminder.getUser();
                Item lItem = lReminder.getItem();
                // Retrieve the user map containing the list of reminders for
                // each item
                Map<Item, List<Reminder>> lMapItem = remindersMap.get(lUser);
                if (lMapItem == null) {
                    // Create the map if it does not exist
                    lMapItem = new HashMap<Item, List<Reminder>>();
                    remindersMap.put(lUser, lMapItem);
                }
                // Retrieve the item list of reminders
                List<Reminder> lReminders = lMapItem.get(lItem);
                if (lReminders == null) {
                    // Create the list if it does not exist
                    lReminders = new ArrayList<Reminder>();
                    lMapItem.put(lItem, lReminders);
                }
                // Add the reminder to the list
                lReminders.add(lReminder);
            }
        }
    }
    
    /**
     * Build the e-mail content for the provided user
     * 
     * @param pUser
     *            the user
     * @param pRemindedReminders
     *            the list of reminded reminders (updated by this function, must
     *            be initialized)
     * @return the content of the mail
     */
    private StringBuffer buildUserMailContent(User pUser,
            List<Reminder> pRemindedReminders) {
        
        SimpleDateFormat lDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        // Build the reminders list page link
        String lRemindersListLink = null;
        try {
            HttpServletRequest lRequest =
                    (HttpServletRequest) FacesContext.getCurrentInstance()
                            .getExternalContext().getRequest();
            URL lURL =
                    new URL(lRequest.getScheme(), lRequest.getServerName(),
                            lRequest.getServerPort(), lRequest.getContextPath());
            lRemindersListLink =
                    lURL.toString() + "/user/ListUserReminders.faces";
        }
        catch (MalformedURLException e1) {
            // Nothing to do
            // The link will not be added to the mail
        }
        
        StringBuffer lContent = new StringBuffer();
        
        /*
         * Mail header
         */
        
        lContent.append("Dear BOA User,<br /><br />");
        lContent.append("Please be informed about the following planned actions assigned to you:<br />");
        
        /*
         * Mail content (list of reminders)
         */
        
        lContent.append("<ul>"); // open items part
        
        Map<Item, List<Reminder>> lMapItem = remindersMap.get(pUser);
        
        // Browse items
        for (Item lItem : lMapItem.keySet()) {
            
            lContent.append("<li>" + lItem.getName() + "</li>");
            
            lContent.append("<ul>"); // open reminders sub-part
            
            List<Reminder> lReminders = lMapItem.get(lItem);
            // Browse reminders
            for (Reminder lReminder : lReminders) {
                lContent.append("<li><b>"
                        + lDateFormat.format(lReminder.getTargetDate())
                        + "</b> : " + lReminder.getObject() + "</li>");
                
                pRemindedReminders.add(lReminder);
            }
            lContent.append("</ul>"); // close reminders sub-part
        }
        lContent.append("</ul>"); // close items part
        
        /*
         * Mail footer
         */
        
        lContent.append("You can consult your planned actions (reminders) through ");
        if (lRemindersListLink != null) {
            // Add link to open the reminders list page
            lContent.append("<a href=" + lRemindersListLink + "?"
                    + ReminderController.USER_ID_PARAM_NAME + "="
                    + pUser.getId() + ">BOA</a>");
        }
        else {
            // Add only text since link is not available
            lContent.append("BOA");
        }
        lContent.append(" application (login: ");
        lContent.append(pUser.getLogin());
        lContent.append(").<br /><br />");
        
        lContent.append("Do not reply to this message, sent by a robot.<br />");
        
        SupportURLController lSupportURLController =
                findBean(SupportURLController.class);
        String lSupportURL =
                lSupportURLController.getSupportURLFormatHTML(pUser);
        lContent.append("If you need assistance you may ");
        if (lSupportURL != null) {
            // Add link to support
            lContent.append("<a href=" + lSupportURL + ">contact support</a>");
        }
        else {
            // Add only text since link is not available
            lContent.append("contact support (through BOA)");
        }
        lContent.append(".<br />");
        
        lContent.append("<br />Regards,<br />The support team.<br />");
        
        return lContent;
    }
    
    /**
     * Store the provided message in HTML format in the right log with
     * information about the user
     * 
     * @param pUser
     *            the concerned user
     * @param pErrorMessage
     *            the error message to log
     */
    private void logDisplay(User pUser, StringBuffer pErrorMessage) {
        
        logBuffer.append("\r\n");
        logBuffer.append(pUser.getLoginDetails() + " => " + pUser.getEmail()
                + "\r\n\r\n");
        if (pErrorMessage != null) {
            logBuffer.append(pErrorMessage);
        }
        else {
            logBuffer.append("Sending OK");
        }
        logBuffer.append("\r\n");
        logBuffer.append("_______________________________________________\r\n");
    }
    
    /**
     * Browse all reminders and send e-mail to users having at least one
     * reminder to be reminded
     */
    @PostConstruct
    private void perform() {
        
        logBuffer = new StringBuffer();
        
        logBuffer.append("_______________________________________________\r\n");
        logBuffer.append("\r\n");
        logBuffer.append("           Sending reminders results           \r\n");
        logBuffer.append("_______________________________________________\r\n");
        
        from =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getInitParameter("ReminderEmailFrom");
        
        buildRemindersMap();
        
        if (remindersMap.isEmpty()) {
            logBuffer.append("\r\nNo reminder to send!\r\n");
        }
        
        for (User lUser : remindersMap.keySet()) {
            
            try {
                List<Reminder> lRemindedReminders = sendUserMail(lUser);
                
                // Update reminders according to the sent mail
                for (Reminder lReminder : lRemindedReminders) {
                    
                    lReminder.notifyRemind();
                    reminderBean.merge(lReminder);
                }
                
            }
            catch (MessagingException e) {
                
                // Initialize the error description
                StringBuffer lErrorDescription =
                        new StringBuffer(
                                "An error occurred while sending e-mail: "
                                        + e.getMessage());
                
                // Add message to the error log for display on HMI
                logDisplay(lUser, lErrorDescription);
                
                // Send mail to support
                String lSupportMail =
                        FacesContext.getCurrentInstance().getExternalContext()
                                .getInitParameter("SupportEmail");
                
                // Complete the error description
                lErrorDescription.append(" to <b>" + lUser.getEmail()
                        + "</b> (" + lUser.getLoginDetails() + ")");
                
                StringBuffer lMessageContent =
                        new StringBuffer(lErrorDescription + "<br/><br/><br/>");
                lMessageContent.append("<b>Message:</b><br/>");
                lMessageContent.append(e.getMessage() + "<br/><br/><br/>");
                lMessageContent.append("<b>StackTrace:</b><br/>");
                Writer lWriter = new StringWriter();
                PrintWriter lPrintWriter = new PrintWriter(lWriter);
                e.printStackTrace(lPrintWriter);
                lMessageContent.append(lWriter.toString());
                try {
                    MailUtil.sendMail(from, lSupportMail,
                            "[BOA] Sending reminders mails error",
                            lMessageContent);
                }
                catch (MessagingException me) {
                    // Nothing more to do
                }
            }
        }
    }
    
    /**
     * Send an e-mail containing the reminder(s) to the provided user
     * 
     * @param pUser
     *            the user
     * @return the list of reminder(s) contained in the mail
     * @throws MessagingException
     *             when an error occurs while sending the mail
     */
    private List<Reminder> sendUserMail(User pUser) throws MessagingException {
        
        List<Reminder> lRemindedReminders = new ArrayList<Reminder>();
        
        StringBuffer lContent = buildUserMailContent(pUser, lRemindedReminders);
        
        MailUtil.sendMail(from, pUser.getEmail(),
                "[BOA] Reminder for planned action(s)", lContent);
        
        logDisplay(pUser, null);
        
        return lRemindedReminders;
    }
    
    /**
     * @return the logBuffer
     */
    public StringBuffer getLogBuffer() {
        return logBuffer;
    }
    
}
