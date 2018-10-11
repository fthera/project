/*
 * ------------------------------------------------------------------------
 * Class : SupportURLController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.application;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.airbus.boa.entity.user.User;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.LogInController;

/**
 * Support URL management
 */
@ManagedBean(name = SupportURLController.BEAN_NAME)
@SessionScoped
public class SupportURLController extends AbstractController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "supportURLController";
    
    /** URL of the support form */
    private static final String ADDRESS =
            "http://avsim.fr.eu.airbus.corp:8870/SIMU_FRAMEWORK/INFO-SUPPORT/formulaire_support.html";
    
    /* Constants defining the fields names into the support form */
    
    private static final String PRODUCT_NAME = "Product Name";
    private static final String PRODUCT_NUMBER = "Product Number";
    
    @SuppressWarnings("unused")
    private static final String MSG_TITLE = "MsgTitle";
    @SuppressWarnings("unused")
    private static final String MESSAGE = "SR Description";
    
    private static final String MAIL = "mail";
    
    private static final String USER = "User";
    private static final String USER_ACCOUNT = "SR User Account";
    
    /* List of parameters */
    private Map<String, String> parameters = new Hashtable<String, String>();
    
    private void addCurrentUserParameters() {
        
        LogInController lLogInController = findBean(LogInController.class);
        User lUserLogged = lLogInController.getUserLogged();
        // User identification
        addUserParameters(lUserLogged);
    }
    
    /**
     * Add the user parameters
     * 
     * @param pUser
     *            the user for which to generate the support URL (can be null)
     */
    private void addUserParameters(User pUser) {
        
        // User identification
        if (pUser != null) {
            parameters.put(USER,
                    pUser.getFirstname() + " " + pUser.getLastname());
            parameters.put(USER_ACCOUNT, pUser.getLogin());
            parameters.put(MAIL, pUser.getEmail());
        }
    }
    
    /**
     * Build the complete URL based on ADDRESS and parameters attributes
     * 
     * @return the complete URL
     */
    private String buildURL() {
        String url = ADDRESS;
        
        Set<String> keys = parameters.keySet();
        String value;
        boolean first = true;
        
        for (String key : keys) {
            
            value = parameters.get(key);
            
            if (!value.equals("")) {
                if (first) {
                    url += "?";
                    first = false;
                }
                else {
                    url += "&";
                }
                url += key + "=" + value;
            }
        }
        
        return url;
    }
    
    /**
     * Initialize the parameters map with default and retrieved values
     */
    private void initParameters() {
        parameters.clear();
        
        // Product identification
        parameters.put(PRODUCT_NAME, "BOA");
        parameters.put(PRODUCT_NUMBER, FacesContext.getCurrentInstance()
                .getExternalContext().getInitParameter("BoaVersion"));
    }
    
    /**
     * Generate the support URL for the logged user if any, else for unknown
     * user
     * 
     * @return the generated support URL
     */
    public String getSupportURL() {
        initParameters();
        addCurrentUserParameters();
        return buildURL();
    }
    
    /**
     * Generate the support URL for the provided user if any, else for unknown
     * user
     * 
     * @param pUser
     *            the user for which to generate the support URL (can be null)
     * @return the generated support URL
     */
    public String getSupportURLFormatHTML(User pUser) {
        initParameters();
        addUserParameters(pUser);
        String lURL = buildURL();
        return StringUtil.parseToHTML(lURL);
    }
    
}
