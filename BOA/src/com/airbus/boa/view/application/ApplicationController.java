/*
 * ------------------------------------------------------------------------
 * Class : ApplicationController
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.airbus.boa.control.ApplicationBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.BOAParameters;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.AbstractController;

/**
 * BOA application general controller
 */
@ManagedBean(name = ApplicationController.BEAN_NAME)
@ApplicationScoped
public class ApplicationController extends AbstractController {
    
    private static final long serialVersionUID = 5087803475941356616L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "applicationController";

    private int totalActiveSessions = 0;
    private ArrayList<User> connectedUsers = new ArrayList<User>();
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private ApplicationBean applicationBean;
    
    /**
     * BOA Application Parameters
     */
    private BOAParameters pcdemandemailrecipiant;
    
    @PostConstruct
    public void init() {
        doResetBOAParameters();
    }
    
    /**
     * @return the currently used database hostname
     */
    public String getDatabaseHostname() {
        return applicationBean.findDatabaseHostname();
    }
    
    /**
     * @return the currently used database name
     */
    public String getDatabaseName() {
        return applicationBean.findDatabaseName();
    }
    
    /**
     * @return a string indicating that the database is not the production one
     *         if it is the case
     *         else an empty string
     */
    public String getTestsDatabaseString() {
        String lProductionHostname =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getInitParameter("BoaProductionDatabaseHostname");
        if (applicationBean.findDatabaseHostname()
                .equalsIgnoreCase(lProductionHostname)) {
            // The used database is the production one
            return "";
        }
        else {
            return MessageBundle.getMessage("testsDatabase");
        }
    }
    
    /**
     * @return the active session count
     */
	public int getTotalActiveSessions() {
		return Math.max(totalActiveSessions, 0);
	}

	/**
	 * 
	 * @param pTotalActiveSessions the active session count to set
	 */
	public void setTotalActiveSessions(int pTotalActiveSessions) {
		this.totalActiveSessions = pTotalActiveSessions;
	}

	/**
	 * 
	 * @return the connected user list
	 */
	public ArrayList<User> getConnectedUsers() {
		return connectedUsers;
	}

	/**
	 * 
	 * @param pUser the connected user to add
	 */
	public void addConnectedUser(User pUser ) {
		this.connectedUsers.add(pUser);
	}
	/**
	 * 
	 * @param pUser pUser the connected user to remove
	 */
	public void removeConnectedUser(User pUser ) {		
		if(pUser != null){
			connectedUsers.remove(pUser);
		}
	}
	
	public int getUnconnectedUsersCount(){
		return Math.max(0, this.totalActiveSessions - this.connectedUsers.size());
	}
	
	public BOAParameters getPcdemandemailrecipiant() {
		return pcdemandemailrecipiant;
	}

	public void setPcdemandemailrecipiant(BOAParameters pcdemandemailrecipiant) {
		this.pcdemandemailrecipiant = pcdemandemailrecipiant;
	}
	
	/**
	 * @return the BOA String URL 
	 * ie http://hostname:port/ContextPath
	 */
	public String getBOAURL(){
		// Build the BOA URL address
        try {
            HttpServletRequest lRequest =
                    (HttpServletRequest) FacesContext.getCurrentInstance()
                            .getExternalContext().getRequest();
            URL lURL =
                    new URL(lRequest.getScheme(), lRequest.getServerName(),
                            lRequest.getServerPort(), lRequest.getContextPath());
            return lURL.toString();
        }
        catch (MalformedURLException e1) {
            return "";
        }
	}
    
    public void doUpdateBOAParameters() {
        valueListBean.merge(pcdemandemailrecipiant);
    }
    
    public void doResetBOAParameters() {
        pcdemandemailrecipiant =
                valueListBean.findBOAParametersByName("pcdemandemailrecipiant");
    }
}
