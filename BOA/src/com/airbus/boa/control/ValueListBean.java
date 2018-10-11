/*
 * ------------------------------------------------------------------------
 * Class : ValueListBean
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.user.PermissionOrm;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.entity.valuelist.BOAParameters;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.ComparatorSelectItem;

/**
 * Bean used for managing AttributeValueList
 */
@Stateless
@LocalBean
public class ValueListBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** List of classes inheriting from AttributeValueList abstract class */
    public static final Class<?>[] VALUE_LIST_CLASSES = {
            BusinessUsagePC.class, BusinessAllocationPC.class,
            ProductTypePC.class, Network.class, ManufacturerStatus.class,
            AirbusStatus.class, ActionObso.class, Strategy.class,
            ConsultPeriod.class, ExternalEntityType.class, Role.class,
            AircraftProgram.class, Domain.class, DepartmentInCharge.class };
    
    private static Logger log = Logger.getLogger(ValueListBean.class.getName());
    
    @PersistenceContext(name = "ValueListService")
    private EntityManager em;
    
    @EJB
    UserBean userBean;
    
    /**
     * Persist the attribute value in database
     * 
     * @param valueList
     *            the attribute value to persist
     * @return the persisted attribute value
     */
    public <T extends AttributeValueList> T create(T valueList) {
        
        em.persist(valueList);
        log.warning("valueList persisted: " + valueList);
        return valueList;
    }
    
    /**
     * Update the attribute value in database
     * 
     * @param selection
     *            the attribute value to update
     * @return the updated attribute value
     */
    public <T extends AttributeValueList> T merge(T selection) {
        
        if (selection != null) {
            if (!selection.isModifiable()) {
                throw new ValidationException(
                        MessageBundle
                                .getMessage(Constants.UNMODIFIABLE_VALUE_LIST));
            }
        }
        try {
            selection = em.merge(selection);
        }
        catch (EJBException e) {
            log.warning(e.getMessage());
            throw new ValidationException(e);
        }
        
        return selection;
    }
    
    /**
     * Update the BOA parameter value in database
     * 
     * @param selection
     *            the BOA parameter value to update
     * @return the updated BOA parameter value
     */
    public BOAParameters merge(BOAParameters selection) {
        
        try {
            selection = em.merge(selection);
        } catch (EJBException e) {
            log.warning(e.getMessage());
            throw new ValidationException(e);
        }
        
        return selection;
    }
    
    /**
     * Search all BOA parameters.
     * 
     * @return the list of BOA parameters
     */
    public List<BOAParameters> findAllBOAParameters() {
        TypedQuery<BOAParameters> lQuery = em.createNamedQuery(
                BOAParameters.BY_NAME_QUERY, BOAParameters.class);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search the BOA parameter by its name.
     * 
     * @param pName
     *            the name of the BOA parametes
     * @return the found BOA parameter, else null
     */
    public BOAParameters findBOAParametersByName(String pName) {
        TypedQuery<BOAParameters> lQuery = em.createNamedQuery(
                BOAParameters.BY_NAME_QUERY, BOAParameters.class);
        lQuery.setParameter("name", pName);
        
        return lQuery.getSingleResult();
    }
    
    /**
     * Remove the attribute value from the database
     * 
     * @param valueList
     *            the attribute value to remove
     */
    public <T extends AttributeValueList> void remove(T valueList) {
        if (valueList != null) {
            if (!valueList.isDeletable()) {
                throw new ValidationException(
                        MessageBundle
                                .getMessage(Constants.UNDELETABLE_VALUE_LIST));
            }
        }
        try {
            log.warning("valuelist deleted: " + valueList);
            
            AttributeValueList avl =
                    em.find(valueList.getClass(), valueList.getId());
            
            if (avl instanceof Role) {
                // Remove associated permissions
                Role lRole = (Role) avl;
                
                List<PermissionOrm> lPermissions =
                        userBean.findPermissionsByRole(lRole);
                
                for (PermissionOrm lPermission : lPermissions) {
                    userBean.deletePermission(lPermission.getId());
                }
            }
            
            em.remove(avl);
            
        }
        catch (EJBException e) {
            throw new ValidationException(e.getMessage());
        }
    }
    
    /**
     * Search all attributes values for the given class.<br>
     * The query results shall be of a class extending the AttributeValueList
     * abstract class.
     * 
     * @param pClass
     *            the AttributeValueList subclass for which values are retrieved
     * @return the result of the query in list form
     */
    @SuppressWarnings("unchecked")
    public <T extends AttributeValueList> List<T>
            findAllValueLists(Class<T> pClass) {
        
        List<T> LResult;
        try {
            Query lQuery =
                    em.createNamedQuery(pClass.newInstance().getAllQuery());
            LResult = lQuery.getResultList();
        }
        catch (NoResultException
                | InstantiationException
                | IllegalAccessException nre) {
            LResult = Collections.emptyList();
        }
        return LResult;
    }
    
    /**
     * Search the attribute value by its id.
     * 
     * @param c
     *            the class of the attribute
     * @param id
     *            the id of the attribute
     * @return the found attribute value
     * @throws ValidationException
     *             when the id is not found
     */
    public <T extends AttributeValueList> T findAttributeValueListById(
            Class<T> c, Long id) {
        
        if (id == null) {
            return null;
        }
        try {
            return em.find(c, id);
        }
        catch (EJBException e) {
            log.warning(e.getMessage());
            throw new ValidationException(e);
        }
    }
    
    /**
     * Search the attribute value by its default value.
     * 
     * @param clazz
     *            the class of the attribute
     * @param defaultValue
     *            the default value of the attribute
     * @return the found attribute value, else null
     */
    public <T extends AttributeValueList> T findAttributeValueListByName(
            Class<T> clazz, String defaultValue) {
        
        try {
            
            Query query =
                    em.createNamedQuery(clazz.newInstance().getByNameQuery());
            query.setParameter("defaultValue", defaultValue);
            
            return clazz.cast(query.getSingleResult());
            
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (NoResultException e) {
            // log.warning(e.getMessage()) ;
        }
        catch (NonUniqueResultException e) {
            log.warning(e.getMessage());
            throw new ValidationException(e);
        }
        
        return null;
    }
    
    /**
     * Search the Role having the provided level
     * 
     * @param pLevel
     *            the searched level
     * @return the found Role, or null if no Role found or several Roles found
     */
    public Role findRoleByLevel(Integer pLevel) {
        
        try {
            TypedQuery<Role> lQuery =
                    em.createNamedQuery(Role.BY_LEVEL_QUERY, Role.class);
            
            lQuery.setParameter("level", pLevel);
            
            return lQuery.getSingleResult();
            
        }
        catch (NoResultException nre) {
            return null;
        }
        catch (NonUniqueResultException nure) {
            return null;
        }
    }
    
    /**
     * Generate a list of SelectItem objects containing all the values present
     * in the database for a given AttributeValueList subclass.
     * 
     * @param pClass
     *            the class for which the list shall be generated
     */
    public <T extends AttributeValueList> List<SelectItem>
            generateSelectItems(Class<T> pClass) {
        
        List<T> lDatas = Collections.emptyList();
        List<SelectItem> lItems = new ArrayList<SelectItem>();
        lDatas = findAllValueLists(pClass);
        
        for (AttributeValueList lAvl : lDatas) {
            SelectItem lItem =
                    new SelectItem(lAvl.getId(), lAvl.getLocaleValue());
            lItems.add(lItem);
        }
        
        Collections.sort(lItems, new ComparatorSelectItem());
        
        return lItems;
    }
    
    /**
     * Generate a list of the values present
     * in the database for a given AttributeValueList subclass.
     * 
     * @param pClass
     *            the class for which the list shall be generated
     */
    public <T extends AttributeValueList> List<String>
            getAllValues(Class<T> pClass) {
        
        List<T> lDatas = Collections.emptyList();
        List<String> lValues = new ArrayList<String>();
        lDatas = findAllValueLists(pClass);
        
        for (AttributeValueList lAvl : lDatas) {
            lValues.add(lAvl.getDefaultValue());
        }
        
        Collections.sort(lValues);
        
        return lValues;
    }
}
