/*
 * ------------------------------------------------------------------------
 * Class : UserBean
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.user.PermissionOrm;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.user.StockSelection;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ExistedUserException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Bean used to manage Users and Permissions
 */
@Stateless
@LocalBean
public class UserBean implements Serializable {
    
    private static final long serialVersionUID = 6456149516921222735L;
    
    @PersistenceContext(name = "UserService")
    private EntityManager em;
    
    @EJB
    private PCBean pcbean;
    
    /**
     * Authenticate the user with the provided login and password.<br>
     * If login is found and password is the correct one, return the
     * authenticated user. Else, throw a ValidationException.
     * 
     * @param login
     *            the login to search
     * @param password
     *            the password to check
     * @return the authenticated user
     * @throws ValidationException
     *             when the log or the password is not correct
     */
    public User authenticate(String login, String password) {
        User user = findUser(login);
        if (user == null) {
            throw new ValidationException(MessageBundle.getMessageResource(
                    Constants.INVALID_LOGIN, new Object[] { login }));
        }
        
        user.matchPassword(password);
        return user;
    }
    
    /**
     * Create the permission in database
     * 
     * @param pPermission
     *            the permission to create
     * @return the created permission
     */
    public PermissionOrm createPermission(PermissionOrm pPermission) {
        
        em.persist(pPermission);
        return pPermission;
    }
    
    /**
     * Create the user in database
     * 
     * @param newUser
     *            the user to create
     * @return the created user
     * @throws ExistedUserException
     *             when the user login already exists
     */
    public User createUser(User newUser) {
        if (findUser(newUser.getLogin()) != null) {
            throw new ExistedUserException(MessageBundle.getMessageResource(
                    Constants.ALREADY_CREATED_USER,
                    new Object[] { newUser.getLogin() }));
        }
        
        em.persist(newUser);
        return newUser;
    }
    
    /**
     * Merge the provided entity in database
     * 
     * @param entity
     *            the entity to merge
     * @return the merged entity
     */
    public <T> T merge(T entity) {
        
        return em.merge(entity);
    }
    
    /**
     * Update the permission in database
     * 
     * @param pPermission
     *            the permission to update
     * @return the merged permission
     */
    public PermissionOrm mergePermission(PermissionOrm pPermission) {
        
        return em.merge(pPermission);
    }
    
    /**
     * Delete the permission from database
     * 
     * @param pPermissionId
     *            the id of the permission to delete
     * @return the deleted permission
     */
    public PermissionOrm deletePermission(Long pPermissionId) {
        if (pPermissionId == null) {
            throw new IllegalArgumentException("permissionId null value");
        }
        PermissionOrm lPermission = em.find(PermissionOrm.class, pPermissionId);
        
        if (lPermission != null) {
            em.remove(lPermission);
        }
        
        return lPermission;
    }
    
    /**
     * Delete the user from database
     * 
     * @param pUserId
     *            the id of the user to delete
     * @return the deleted user
     */
    public User deleteUser(Long pUserId) {
        if (pUserId == null) {
            throw new IllegalArgumentException("userId valeur nulle");
        }
        User lUser = em.find(User.class, pUserId);
        em.remove(lUser);
        return lUser;
    }
    
    /**
     * @return the list of permissions found in database
     */
    public List<PermissionOrm> findAllPermissions() {
        
        TypedQuery<PermissionOrm> lQuery =
                em.createNamedQuery(PermissionOrm.ALL_PERMISSIONS_QUERY,
                        PermissionOrm.class);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * @param pRole
     *            the role
     * @return the list of permissions for provided role found in database
     */
    public List<PermissionOrm> findPermissionsByRole(Role pRole) {
        
        TypedQuery<PermissionOrm> lQuery =
                em.createNamedQuery(PermissionOrm.PERMISSIONS_FOR_ROLE_QUERY,
                        PermissionOrm.class);
        
        lQuery.setParameter("role", pRole);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * @return the list of permissions without role found in database
     */
    public List<PermissionOrm> findPermissionsNoRole() {
        
        TypedQuery<PermissionOrm> lQuery =
                em.createNamedQuery(
                        PermissionOrm.PERMISSIONS_FOR_NO_ROLE_QUERY,
                        PermissionOrm.class);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Search the user corresponding to the provided id
     * 
     * @param userId
     *            the user id to search
     * @return the found user or null
     */
    public User findUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = em.find(User.class, userId);
        return user;
    }
    
    /**
     * Search the user corresponding to the provided login
     * 
     * @param login
     *            the user login to search
     * @return the found user, or null
     */
    public User findUser(String login) {
        Query query = em.createNamedQuery("findUserByLogin");
        query.setParameter("login", login);
        
        User user = null;
        try {
            user = (User) query.getSingleResult();
        }
        catch (NoResultException ne) {
        }
        return user;
    }
    
    /**
     * Retrieve all users from database
     * 
     * @return the list of users
     */
    public List<User> findUsers() {
        TypedQuery<User> query = em.createNamedQuery("AllUsers", User.class);
        List<User> users;
        try {
            users = query.getResultList();
        }
        catch (NoResultException e) {
            users = Collections.emptyList();
        }
        return users;
    }
    
    /**
     * Search the StockSelection with the provided name for the provided user
     * 
     * @param pUser
     *            the user
     * @param pStockSelectionName
     *            the StockSelection name
     * @return the StockSelection object if found, else null
     */
    public StockSelection findStockSelection(User pUser,
            String pStockSelectionName) {
        Query query = em.createQuery("SELECT s FROM StockSelection s"
                + " WHERE s.name = :name AND s.user.id = :userId");
        query.setParameter("userId", pUser.getId());
        query.setParameter("name", pStockSelectionName);
        
        try {
            StockSelection lResult = (StockSelection) query.getSingleResult();
            return lResult;
        } catch (NonUniqueResultException e) {
            return null;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Check id a StockSelection object exist for the given user with the given
     * name
     * 
     * @param pUser
     *            the user
     * @param pStockSelectionName
     *            the StockSelection name
     * @return true if a StockSelection is found, else false
     */
    public boolean existStockSelection(User pUser, String pStockSelectionName) {
        Query query = em.createQuery("SELECT s FROM StockSelection s"
                + " WHERE s.name = :name AND s.user.id = :userId");
        query.setParameter("userId", pUser.getId());
        query.setParameter("name", pStockSelectionName);
        
        boolean exist = false;
        try {
            StockSelection result = (StockSelection) query.getSingleResult();
            exist = (result != null);
        } catch (NonUniqueResultException e) {
            return true;
        } catch (NoResultException e) {
            
        }
        return exist;
    }
    
    /**
     * Save the given StockSelection in the database
     * 
     * @param pStockSelection
     *            the StockSelection to save
     * @return the StockSelection object in sync with the database
     */
    public StockSelection saveStockSelection(StockSelection pStockSelection) {
        Query query = em.createQuery("SELECT s FROM StockSelection s"
                + " WHERE s.name = :name AND s.user.id = :userId");
        query.setParameter("userId", pStockSelection.getUser().getId());
        query.setParameter("name", pStockSelection.getName());
        
        StockSelection lStockSelection = null;
        User lUser = pStockSelection.getUser();
        
        try {
            lStockSelection = (StockSelection) query.getSingleResult();
        } catch (NonUniqueResultException e) {
            
        } catch (NoResultException e) {
            lStockSelection = pStockSelection;
            lUser.getStockSelections().add(pStockSelection);
            em.persist(lStockSelection);
        }
        lStockSelection.setUser(lUser);
        lStockSelection.setSelectedTypes(pStockSelection.getSelectedTypes());
        em.merge(lStockSelection);
        em.merge(lUser);
        
        return pStockSelection;
    }

    
    /**
     * Delete the given stock selection
     * 
     * @param pStockSelection
     *            the StockSelection to delete
     */
    public void deleteStockSelection(StockSelection pStockSelection) {
        StockSelection lStockSelection =
                em.find(StockSelection.class, pStockSelection.getId());
        if (lStockSelection != null) {
            User lUser = lStockSelection.getUser();
            lUser.getStockSelections().remove(lStockSelection);
            em.merge(lUser);
            lStockSelection.setUser(null);
            em.remove(lStockSelection);
        }
    }
    
}
