/*
 * ------------------------------------------------------------------------
 * Class : UserController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ExistedUserException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.item.AbstractItemController;

/**
 * Controller managing the users (creation, update, deletion)
 */
@ManagedBean(name = UserController.BEAN_NAME)
@ViewScoped
public class UserController extends AbstractItemController implements
        Serializable {
    
    private static final long serialVersionUID = -6080496955362597488L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "userController";
    
    /**
     * component id of rich:message for login inputText field in User Creation
     * Form
     */
    
    @EJB
    private UserBean userbean;
    
    @EJB
    private ValueListBean valueListBean;
    
    /** User to update/create */
    private User user;
    private Long userId;
    
    /** First entered password */
    private String password1 = "";
    /** Confirmation password */
    private String password2 = "";
    
    private Long roleId;
    
    private Long minimumRoleId = null;
    
    /**
     * Constructor
     */
    public UserController() {
        listPage = NavigationConstants.USER_LIST_PAGE;
        resultPage = listPage;
        errorId = NavigationConstants.USER_FORM_ERROR_ID;
    }

    /**
     * Default constructor
     */
    @Override
    protected void init() {
        List<Role> lRoles = valueListBean.findAllValueLists(Role.class);
        Role lMinimumRole = lRoles.get(0);
        if (lMinimumRole != null) {
            minimumRoleId = lMinimumRole.getId();
        }
        
        // Default to creation mode
        mode = Mode.CREATE;
        initItemWithNew();
        initAttributesFromItem();
    }
    
    /**
     * Update the selected user
     */
    @Override
    public void doUpdate() {
        try {
            updateItem();
            NavigationUtil.goTo(NavigationConstants.MAIN_PAGE);
        } catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.USER_FORM_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }
    
    public void doUpdateModal() {
        try {
            updateItem();
        } catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.USER_FORM_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }

    /**
     * Create the new user
     */
    @Override
    public void doCreate() {
        
        try {
            setAttibutesFromController();
            user = userbean.createUser(user);
            NavigationUtil.goTo(NavigationConstants.USER_LIST_PAGE);
        }
        catch (ValidationException e) {
            Utils.addFacesMessage(NavigationConstants.USER_FORM_ERROR_ID,
                    e.getMessage());
        }
        catch (ExistedUserException e) {
            Utils.addFacesMessage(NavigationConstants.LOGIN_FORM_ERROR_ID,
                    e.getMessage());
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.USER_FORM_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }
    
    public void doCancel() {
        NavigationUtil.goTo(NavigationConstants.MAIN_PAGE);
    }
    
    @Override
    protected void createItem() throws Exception {
        return;
    }
    
    @Override
    public void updateItem() throws Exception {
        setAttibutesFromController();
        user = userbean.merge(user);
        LogInController controller = findBean(LogInController.class);
        if (user.getId() == controller.getUserLogged().getId()) {
            controller.setUserLogged(user);
        }
    }
    
    @Override
    protected void deleteItem() throws Exception {
        return;
    }

    @Override
    protected void initItemWithNew() {
        user = new User();
    }
    
    @Override
    protected void initItemFromDatabase() {
        user = userbean.findUser(userId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        if (user != null) {
            roleId =
                    (user.getRole() != null) ? user.getRole().getId()
                            : minimumRoleId;
        }
        else {
            roleId = minimumRoleId;
        }
        password1 = "";
        password2 = "";
    }

    private void setAttibutesFromController() {
        if (!StringUtil.isEmptyOrNull(password1)) {
            user.setPassword(User.encryptPassword(password1));
        }
        user.setRole(valueListBean.findAttributeValueListById(
                Role.class, roleId));
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
     * Check that the provided password is equal to the first entered password.
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponentToValidate
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the password to validate
     * @throws ValidatorException
     *             when the provided password is different from the new user one
     */
    public void validatePassword(FacesContext pContext,
            UIComponent pComponentToValidate, Object pValue)
            throws ValidatorException {
        
        String lConfirmPassword = (String) pValue;
        
        validatePassword(password1, lConfirmPassword);
    }
    
    /**
     * Check that the provided passwords are identical
     * 
     * @param pPassword1
     *            the first password entry
     * @param pPassword2
     *            the second password entry
     */
    public static void validatePassword(String pPassword1, String pPassword2) {
        
        if ((pPassword1 == null && pPassword2 != null)
                || (pPassword1 != null && !pPassword1.equals(pPassword2))) {
            
            String lMsg = MessageBundle.getMessage(Constants.CONFIRM_PASSWORD);
            
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
    }
    
    /**
     * @return the list of available roles
     */
    public List<SelectItem> getAvailableRoles() {
        return valueListBean.generateSelectItems(Role.class);
    }
    
    /**
     * @return the password1
     */
    public String getPassword1() {
        return password1;
    }
    
    /**
     * @param pPassword1
     *            the password1 to set
     */
    public void setPassword1(String pPassword1) {
        password1 = pPassword1;
    }
    
    /**
     * @return the password2
     */
    public String getPassword2() {
        return password2;
    }
    
    /**
     * @param password2
     *            the password2 to set
     */
    public void setPassword2(String password2) {
        this.password2 = password2;
    }
    
    @Override
    protected void setItemId(Long pId) {
        userId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return userId;
    }

    /**
     * @return the selectedUser
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Set the user and force the update mode
     * 
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
        this.userId = user.getId();
        initAttributesFromItem();
        this.mode = Mode.UPDATE;
    }
    
    /**
     * @return the roleId
     */
    public Long getRoleId() {
        return roleId;
    }
    
    /**
     * @param pRoleId
     *            the roleId to set
     */
    public void setRoleId(Long pRoleId) {
        roleId = pRoleId;
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("userCreationTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return "";
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("editUserProfil");
    }
    
}
