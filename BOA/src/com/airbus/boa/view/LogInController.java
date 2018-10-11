/*
 * ------------------------------------------------------------------------
 * Class : LogInController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.user.PermissionAction;
import com.airbus.boa.entity.user.PermissionCRUD;
import com.airbus.boa.entity.user.PermissionOrm;
import com.airbus.boa.entity.user.RightCategoryAction;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMask;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.view.application.ApplicationController;

/**
 * Controller managing the user log in and log out, rights for actions, ...
 */
@ManagedBean(name = LogInController.BEAN_NAME)
@SessionScoped
public class LogInController extends AbstractController implements Serializable{

	private static final long serialVersionUID = 1L;

	/** The managed bean name */
	public static final String BEAN_NAME = "logInController";

	@EJB
	private UserBean userBean;

	/** User password for log in */
	private String password;
	/** User login for log in */
	private String login;

	private User userLogged;

	private Map<RightCategoryCRUD, PermissionCRUD> permissionsCRUDMap = new HashMap<RightCategoryCRUD, PermissionCRUD>();

	private Map<RightCategoryAction, PermissionAction> permissionsActionMap = new HashMap<RightCategoryAction, PermissionAction>();

    private String reconnectPage = NavigationConstants.MAIN_PAGE;
    
	/**
	 * Default constructor
	 */
	public LogInController() {

	}

	/**
	 * Log out the current logged user and invalidate the session
	 */
    public void doDisconnect() {
		//logout connected user from application
		 ApplicationController lApplicationController =
                findBean(ApplicationController.class);
		 lApplicationController.removeConnectedUser(userLogged);
		
		userLogged = null;
		Utils.getSession().invalidate();
        NavigationUtil.goTo(NavigationConstants.MAIN_PAGE);
	}

	/**
	 * Log in the provided user with its login and password (if authentication
	 * is validated).<br>
	 * Refresh the current page in order to take into account the user rights.
	 */
    public void doLogin() {
		try {
			User lUserLogged = userBean.authenticate(login, password);
			if (lUserLogged != null) {
				lUserLogged.setLastLoggedIn(new Date());
			}
			setUserLogged(userBean.merge(lUserLogged));
			password = null;
			login = null;
			
			//log connected user in application
			 ApplicationController lApplicationController =
                findBean(ApplicationController.class);
			 lApplicationController.addConnectedUser(lUserLogged);
			
		} catch (ValidationException ve) {
            Utils.addFacesMessage(NavigationConstants.LOGIN_ERROR_ID,
                    ve.getMessage());
		}
	}
    
    /**
     * Log in the provided user with its login and password (if authentication
     * is validated).<br>
     * Redirect the user to the page he was before being deconnected.
     */
    public void doReconnect() {
        try {
            User lUserLogged = userBean.authenticate(login, password);
            if (lUserLogged != null) {
                lUserLogged.setLastLoggedIn(new Date());
            }
            setUserLogged(userBean.merge(lUserLogged));
            password = null;
            login = null;
            
            // log connected user in application
            ApplicationController lApplicationController =
                    findBean(ApplicationController.class);
            lApplicationController.addConnectedUser(lUserLogged);
            
            if (userLogged != null) {
                ExternalContext lExternalContext =
                        FacesContext.getCurrentInstance().getExternalContext();
                lExternalContext.redirect(reconnectPage);
            }
        } catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.RECONNECT_ERROR_ID,
                    e.getMessage());
        }
    }

	@PostConstruct
	private void init() {
		initPermissionsMaps();
	}

	private void initPermissionsMaps() {

		permissionsCRUDMap.clear();
		permissionsActionMap.clear();

		List<PermissionOrm> lPermissions;

		if (userLogged != null) {
			lPermissions = userBean.findPermissionsByRole(userLogged.getRole());
		} else {
			lPermissions = userBean.findPermissionsNoRole();
		}

		// Update the maps with the retrieved permissions

		for (PermissionOrm lPermission : lPermissions) {

			if (lPermission instanceof PermissionCRUD) {
				PermissionCRUD lPermissionCRUD = (PermissionCRUD) lPermission;
				permissionsCRUDMap.put(lPermissionCRUD.getCategory(),
						lPermissionCRUD);
			} else if (lPermission instanceof PermissionAction) {
				PermissionAction lPermissionAction = (PermissionAction) lPermission;
				permissionsActionMap.put(lPermissionAction.getCategory(),
						lPermissionAction);
			}
		}
	}

	/**
	 * Determine if the action associated to the mask is authorized on the
	 * category for logged user
	 * 
	 * @param pCategory
	 *            the category on which to apply the action
	 * @param pMask
	 *            the action determined by a mask
	 * @return true if the action is authorized, else false
	 */
	public boolean isAuthorized(RightCategoryCRUD pCategory, RightMaskCRUD pMask) {

		if (pMask == null) {
			return false;
		}

		PermissionCRUD lRolePermission = permissionsCRUDMap.get(pCategory);

		if (lRolePermission == null) {
			return false;
		}

		return RightMask.isAllowed(lRolePermission.getBitwiseCode(),
				pMask.getBitwiseCode());
	}

	/**
	 * Determine if the action associated to the mask is authorized for logged
	 * user
	 * 
	 * @param pMask
	 *            the action determined by a mask
	 * @return true if the action is authorized, else false
	 */
	public boolean isAuthorized(RightMaskAction pMask) {

		if (pMask == null) {
			return false;
		}

		PermissionAction lRolePermission = permissionsActionMap.get(pMask
				.getCategory());

		if (lRolePermission == null) {
			return false;
		}

		return RightMask.isAllowed(lRolePermission.getBitwiseCode(),
				pMask.getBitwiseCode());
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the userLogged
	 */
	public User getUserLogged() {
		return userLogged;
	}

	/**
	 * @param pUserLogged
	 *            the userLogged to set
	 */
	public void setUserLogged(User pUserLogged) {
		userLogged = pUserLogged;
		initPermissionsMaps();
	}
    
    /**
     * Reload the logged user from the database
     */
    public void reloadUserLogged() {
        setUserLogged(userBean.findUser(userLogged.getId()));
    }

    /**
     * @return the reconnectPage
     */
    public String getReconnectPage() {
        return reconnectPage;
    }
    
    /**
     * @param pReconnectPage
     *            the reconnectPage to set
     */
    public void setReconnectPage(String pReconnectPage) {
        reconnectPage = pReconnectPage;
    }
    
    /**
     * @return the userBean
     */
    public UserBean getUserBean() {
        return userBean;
    }
    
}
