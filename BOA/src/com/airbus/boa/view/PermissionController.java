/*
 * ------------------------------------------------------------------------
 * Class : PermissionController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.user.PermissionAction;
import com.airbus.boa.entity.user.PermissionCRUD;
import com.airbus.boa.entity.user.PermissionOrm;
import com.airbus.boa.entity.user.RightCategoryAction;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMask;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.Role;

/**
 * Controller managing the permissions for roles to access to functionalities
 */
@ManagedBean(name = PermissionController.BEAN_NAME)
@RequestScoped
public class PermissionController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "permissionController";
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    private Map<RightCategoryCRUD, Map<Role, PermissionCRUD>> permissionsCRUDMap =
            new HashMap<RightCategoryCRUD, Map<Role, PermissionCRUD>>();
    
    private Map<RightCategoryCRUD, Map<Role, Map<RightMaskCRUD, Boolean>>> booleansCRUDMap =
            new HashMap<RightCategoryCRUD, Map<Role, Map<RightMaskCRUD, Boolean>>>();
    
    private Map<RightCategoryAction, Map<Role, PermissionAction>> permissionsActionMap =
            new HashMap<RightCategoryAction, Map<Role, PermissionAction>>();
    
    private Map<RightCategoryAction, Map<Role, Map<RightMaskAction, Boolean>>> booleansActionMap =
            new HashMap<RightCategoryAction, Map<Role, Map<RightMaskAction, Boolean>>>();
    
    private Role superAdminRole = null;
    
    private List<RightCategoryCRUD> categoriesCRUD =
            new ArrayList<RightCategoryCRUD>();
    private List<RightMaskAction> masksAction =
            new ArrayList<RightMaskAction>();
    private List<RightMaskCRUD> masksCRUD = new ArrayList<RightMaskCRUD>();
    private List<Role> roles = new ArrayList<Role>();
    
    /**
     * Default constructor
     */
    public PermissionController() {
        
    }
    
    /**
     * Refresh the page
     */
    public void doRefresh() {
        reset();
    }
    
    /**
     * Update the permissions according to the set values.<br>
     * The permissions for Super Administrator are forced to all granted.
     */
    public void doUpdatePermissions() {
        
        for (RightCategoryCRUD lCategory : booleansCRUDMap.keySet()) {
            
            Map<Role, Map<RightMaskCRUD, Boolean>> lCategoryMap =
                    booleansCRUDMap.get(lCategory);
            
            for (Role lRole : lCategoryMap.keySet()) {
                
                Map<RightMaskCRUD, Boolean> lRoleMap = lCategoryMap.get(lRole);
                
                // Initialize the bitwise code
                
                Byte lBitwiseCode;
                if (lRole.equals(superAdminRole)) {
                    // force all permissions for Super Administrator
                    lBitwiseCode = RightMask.ALL;
                }
                else {
                    // initialize permissions with none
                    lBitwiseCode = RightMask.NONE;
                    
                    // update permissions with provided ones
                    for (RightMaskCRUD lCode : lRoleMap.keySet()) {
                        if (lRoleMap.get(lCode)) {
                            lBitwiseCode =
                                    RightMask.addRight(lBitwiseCode,
                                            lCode.getBitwiseCode());
                        }
                    }
                }
                
                // Retrieve the corresponding permissions
                
                PermissionCRUD lPermission = null;
                if (permissionsCRUDMap.containsKey(lCategory)) {
                    if (permissionsCRUDMap.get(lCategory).containsKey(lRole)) {
                        lPermission =
                                permissionsCRUDMap.get(lCategory).get(lRole);
                    }
                }
                
                if (lPermission != null) {
                    // Update the permission
                    
                    lPermission.setBitwiseCode(lBitwiseCode);
                    
                    userBean.mergePermission(lPermission);
                }
                else {
                    // Create the permission
                    
                    // replace the virtual Anonymous Role by null for
                    // storing in database
                    if (lRole.equals(Role.ANONYMOUS)) {
                        lRole = null;
                    }
                    
                    lPermission =
                            new PermissionCRUD(lCategory, lRole, lBitwiseCode);
                    
                    userBean.createPermission(lPermission);
                }
            }
        }
        
        for (RightCategoryAction lCategory : booleansActionMap.keySet()) {
            
            Map<Role, Map<RightMaskAction, Boolean>> lCategoryMap =
                    booleansActionMap.get(lCategory);
            
            for (Role lRole : lCategoryMap.keySet()) {
                
                Map<RightMaskAction, Boolean> lRoleMap =
                        lCategoryMap.get(lRole);
                
                // Initialize the bitwise code
                
                Byte lBitwiseCode;
                if (lRole.equals(superAdminRole)) {
                    // force all permissions for Super Administrator
                    lBitwiseCode = RightMask.ALL;
                }
                else {
                    // initialize permissions with none
                    lBitwiseCode = RightMask.NONE;
                    
                    // update permissions with provided ones
                    for (RightMaskAction lCode : lRoleMap.keySet()) {
                        if (lRoleMap.get(lCode)) {
                            lBitwiseCode =
                                    RightMask.addRight(lBitwiseCode,
                                            lCode.getBitwiseCode());
                        }
                    }
                }
                
                // Retrieve the corresponding permission
                
                PermissionAction lPermission = null;
                if (permissionsActionMap.containsKey(lCategory)) {
                    if (permissionsActionMap.get(lCategory).containsKey(lRole)) {
                        lPermission =
                                permissionsActionMap.get(lCategory).get(lRole);
                    }
                }
                
                if (lPermission != null) {
                    // Update the permission
                    
                    lPermission.setBitwiseCode(lBitwiseCode);
                    
                    userBean.mergePermission(lPermission);
                }
                else {
                    // Create the permission
                    
                    // replace the virtual Anonymous Role by null for
                    // storing in database
                    if (lRole.equals(Role.ANONYMOUS)) {
                        lRole = null;
                    }
                    
                    lPermission =
                            new PermissionAction(lCategory, lRole, lBitwiseCode);
                    
                    userBean.createPermission(lPermission);
                }
            }
        }
        
        initPermissionsMaps();
    }
    
    private void initPermissionsMaps() {
        
        List<Role> lRoles =
                valueListBean.findAllValueLists(Role.class);
        
        // Add the Anonymous Role (when no user is logged)
        lRoles.add(Role.ANONYMOUS);
        
        // Initialize the booleans maps with all values equal to false
        
        for (RightCategoryCRUD lCategory : RightCategoryCRUD.values()) {
            
            Map<Role, Map<RightMaskCRUD, Boolean>> lCategoryMap =
                    new HashMap<Role, Map<RightMaskCRUD, Boolean>>();
            
            booleansCRUDMap.put(lCategory, lCategoryMap);
            
            for (Role lRole : lRoles) {
                
                Map<RightMaskCRUD, Boolean> lRoleMap =
                        new HashMap<RightMaskCRUD, Boolean>();
                
                lCategoryMap.put(lRole, lRoleMap);
                
                for (RightMaskCRUD lMask : RightMaskCRUD.values()) {
                    lRoleMap.put(lMask, false);
                }
            }
        }
        
        for (RightCategoryAction lCategory : RightCategoryAction.values()) {
            
            Map<Role, Map<RightMaskAction, Boolean>> lCategoryMap =
                    new HashMap<Role, Map<RightMaskAction, Boolean>>();
            
            booleansActionMap.put(lCategory, lCategoryMap);
            
            for (Role lRole : lRoles) {
                
                Map<RightMaskAction, Boolean> lRoleMap =
                        new HashMap<RightMaskAction, Boolean>();
                
                lCategoryMap.put(lRole, lRoleMap);
                
                for (RightMaskAction lMask : RightMaskAction.values()) {
                    // check that the mask corresponds to the current category
                    if (lCategory.equals(lMask.getCategory())) {
                        lRoleMap.put(lMask, false);
                    }
                }
            }
        }
        
        // Update the maps with the retrieved permissions
        
        for (PermissionOrm lPermission : userBean.findAllPermissions()) {
            
            if (lPermission instanceof PermissionCRUD) {
                PermissionCRUD lPermissionCRUD = (PermissionCRUD) lPermission;
                
                RightCategoryCRUD lCategory = lPermissionCRUD.getCategory();
                Role lRole = lPermissionCRUD.getRole();
                byte lBitwiseCode = lPermissionCRUD.getBitwiseCode();
                
                if (lRole == null) {
                    lRole = Role.ANONYMOUS;
                }
                
                if (!permissionsCRUDMap.containsKey(lCategory)) {
                    permissionsCRUDMap.put(lCategory,
                            new HashMap<Role, PermissionCRUD>());
                }
                
                permissionsCRUDMap.get(lCategory).put(lRole, lPermissionCRUD);
                
                // since boolean permissions map has been initialized with all
                // possible values, no check is necessary
                
                for (RightMaskCRUD lMask : RightMaskCRUD.values()) {
                    // replace the default previously inserted value
                    booleansCRUDMap
                            .get(lCategory)
                            .get(lRole)
                            .put(lMask,
                                    RightMask.isAllowed(lBitwiseCode,
                                            lMask.getBitwiseCode()));
                }
            }
            else if (lPermission instanceof PermissionAction) {
                PermissionAction lPermissionAction =
                        (PermissionAction) lPermission;
                
                RightCategoryAction lCategory = lPermissionAction.getCategory();
                Role lRole = lPermissionAction.getRole();
                byte lBitwiseCode = lPermissionAction.getBitwiseCode();
                
                if (lRole == null) {
                    lRole = Role.ANONYMOUS;
                }
                
                if (!permissionsActionMap.containsKey(lCategory)) {
                    permissionsActionMap.put(lCategory,
                            new HashMap<Role, PermissionAction>());
                }
                
                permissionsActionMap.get(lCategory).put(lRole,
                        lPermissionAction);
                
                // since boolean permissions map has been initialized with all
                // possible values, no check is necessary
                
                for (RightMaskAction lMask : RightMaskAction.values()) {
                    // check that the mask corresponds to the current category
                    if (lCategory.equals(lMask.getCategory())) {
                        // replace the default previously inserted value
                        booleansActionMap
                                .get(lCategory)
                                .get(lRole)
                                .put(lMask,
                                        RightMask.isAllowed(lBitwiseCode,
                                                lMask.getBitwiseCode()));
                    }
                }
            }
        }
    }
    
    @PostConstruct
    private void init() {
        
        superAdminRole =
                valueListBean.findRoleByLevel(Role.SUPER_ADMINISTRATOR_LEVEL);
        
        categoriesCRUD.clear();
        for (RightCategoryCRUD lCategory : RightCategoryCRUD.values()) {
            categoriesCRUD.add(lCategory);
        }
        
        masksAction.clear();
        for (RightMaskAction lMask : RightMaskAction.values()) {
            masksAction.add(lMask);
        }
        
        masksCRUD.clear();
        for (RightMaskCRUD lMask : RightMaskCRUD.values()) {
            masksCRUD.add(lMask);
        }
        
        roles.clear();
        List<Role> lRoles = valueListBean.findAllValueLists(Role.class);
        roles.addAll(lRoles);
        
        initPermissionsMaps();
    }
    
    /**
     * @return the virtual Anonymous Role
     */
    public Role getAnonymous() {
        return Role.ANONYMOUS;
    }
    
    /**
     * @return the list of CRUD categories
     */
    public List<RightCategoryCRUD> getCategoriesCRUD() {
        
        return categoriesCRUD;
    }
    
    /**
     * @return the list of masks for Actions
     */
    public List<RightMaskAction> getMasksAction() {
        
        return masksAction;
    }
    
    /**
     * @return the list of masks for CRUD actions
     */
    public List<RightMaskCRUD> getMasksCRUD() {
        
        return masksCRUD;
    }
    
    /**
     * @return the Map of booleans representing the permissions for Actions
     */
    public Map<RightCategoryAction, Map<Role, Map<RightMaskAction, Boolean>>>
            getBooleansActionMap() {
        return booleansActionMap;
    }
    
    /**
     * @return the Map of booleans representing the permissions for CRUD actions
     */
    public Map<RightCategoryCRUD, Map<Role, Map<RightMaskCRUD, Boolean>>>
            getBooleansCRUDMap() {
        return booleansCRUDMap;
    }
    
    /**
     * @return the list of Roles for which the permissions can be modified
     */
    public List<Role> getRoles() {
        
        return roles;
    }
    
    /**
     * @return the superAdminRole
     */
    public Role getSuperAdminRole() {
        return superAdminRole;
    }
    
}
