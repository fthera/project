/*
 * ------------------------------------------------------------------------
 * Class : SoftwareBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.HistoryConstants;
import com.airbus.boa.util.CollectionUtil;
import com.airbus.boa.util.StringUtil;

/**
 * Bean used to manage Software in database
 */
@Stateless
@LocalBean
public class SoftwareBean implements Serializable {
    
    private static final long serialVersionUID = 155212L;
    
    /** Key for the Software description criterion */
    public static final String DESCRIPTION = "description";
    /** Key for the Software distribution criterion */
    public static final String DISTRIBUTION = "distribution";
    /** Key for the Software kernel criterion */
    public static final String KERNEL = "kernel";
    /** Key for the Software license criterion */
    public static final String LICENCE = "licence";
    /** Key for the Software manufacturer criterion */
    public static final String MANUFACTURER = "manufacturer";
    /** Key for the Software name criterion */
    public static final String NAME = "name";
    /** Key for the Software operating system criterion */
    public static final String OPERATING_SYSTEM = "operatingSystem";
    
    /** Key for the Software installed on equipment having Airbus PN criterion */
    public static final String APN = "apn";
    /** Key for the Software installed on equipment of family criterion */
    public static final String FAMILY = "family";
    /**
     * Key for the Software installed on equipment having manufacturer PN
     * criterion
     */
    public static final String MPN = "mpn";
    /**
     * Key for the Software installed on equipment having Airbus or manufacturer
     * SN criterion
     */
    public static final String SN = "sn";
    /** Key for the Software installed on equipment having type criterion */
    public static final String TYPE = "type";
    
    private static Logger log = Logger.getLogger(SoftwareBean.class.getName());
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private PCBean pcBean;
    
    @PersistenceContext(name = "SoftwareService")
    private EntityManager em;
    
    /**
     * Create the software in database
     * 
     * @param software
     *            the software to create
     * @param equipments
     *            the articles on which software is installed
     * @param login
     *            the user login
     */
    public void
            create(Software software, List<Article> equipments, String login) {
        if (log.isLoggable(Level.WARNING)) {
            log.warning("Creation du logiciel");
        }
        
        em.persist(software);
        em.flush();
        
        List<Article> added = new ArrayList<Article>();
        List<Article> removed = new ArrayList<Article>();
        CollectionUtil.retrieveAddedAndRemoved(software.getEquipments(),
                equipments, added, removed);
        
        if (log.isLoggable(Level.INFO)) {
            log.info("AJOUTS EQUIPEMENT :" + String.valueOf(added));
            log.info("RETRAITS EQUIPEMENT : " + String.valueOf(removed));
        }
        software.setEquipments(equipments);
        software = em.merge(software);
        
        for (Article equipment : added) {
            
            equipment =
                    updateArticleHistory(software, login,
                            HistoryConstants.INSTALL_SOFTWARE, equipment);
        }
    }
    
    /**
     * Merge the provided software in database
     * 
     * @param software
     *            the software to merge
     * @return the merged software
     */
    public Software merge(Software software) {
        return em.merge(software);
    }
    
    /**
     * Merge the provided software in database
     * 
     * @param software
     *            the software to merge
     * @param equipments
     *            the articles on which software is installed
     * @param login
     *            the user login
     * @return the merged software
     */
    public Software merge(Software software, List<Article> equipments,
            String login) {
        
        List<Article> added = new ArrayList<Article>();
        List<Article> removed = new ArrayList<Article>();
        CollectionUtil.retrieveAddedAndRemoved(software.getEquipments(),
                equipments, added, removed);
        
        software.setEquipments(equipments);
        software = em.merge(software);
        
        for (Article equipment : added) {
            equipment.getSoftwares().add(software);
            equipment =
                    updateArticleHistory(software, login,
                            HistoryConstants.INSTALL_SOFTWARE, equipment);
        }
        
        for (Article equipment : removed) {
            equipment.getSoftwares().remove(software);
            equipment =
                    updateArticleHistory(software, login,
                            HistoryConstants.REMOVE_SOFTWARE, equipment);
        }
        
        return software;
        
    }
    
    private <T extends Article> T mergeArticle(T art) {
        T mergedArticle = art;
        try {
            
            mergedArticle = em.merge(art);
            // detection des collisions
            
        }
        catch (OptimisticLockException e) {
            throw new ChangeCollisionException(e);
        }
        return mergedArticle;
    }
    
    /**
     * Remove the software from the database
     * 
     * @param software
     *            the software to remove
     * @param login
     *            the user login
     */
    public void remove(Software software, final String login) {
        
        software = em.find(Software.class, software.getId());
        
        for (Article equipment : software.getEquipments()) {
            equipment =
                    updateArticleHistory(software, login,
                            HistoryConstants.REMOVE_SOFTWARE, equipment);
        }
        
        software.setEquipments(new ArrayList<Article>());
        em.remove(software);
        em.flush();
    }
    
    private Article updateArticleHistory(final Software software,
            final String login, final String installRemoveConstant,
            Article equipment) {
        
        History history = equipment.getHistory();
        
        // Add to history the installation or removal of software
        
        String lCompleteName = software.getCompleteName();
        String lAfter = null;
        if (installRemoveConstant.equals(HistoryConstants.INSTALL_SOFTWARE)) {
            lAfter = lCompleteName;
        }
        String lBefore = null;
        if (installRemoveConstant.equals(HistoryConstants.REMOVE_SOFTWARE)) {
            lBefore = lCompleteName;
        }
        
        FieldModification newModif =
                new FieldModification(login, null, installRemoveConstant, null,
                        null, lBefore, lAfter);
        
        history.getActions().add(newModif);
        history = historyBean.merge(history);
        equipment = mergeArticle(equipment);
        
        // Erase/Update the default OS if necessary
        
        if (equipment instanceof PC) {
            
            PC lPC = (PC) equipment;
            List<Software> lListOS = lPC.getOperatingSystems();
            
            Software lOldDefaultOS = lPC.getDefaultOS();
            Software lNewDefaultOS = lOldDefaultOS;
            if (software.getOperatingSystem()) {
                if (installRemoveConstant
                        .equals(HistoryConstants.REMOVE_SOFTWARE)) {
                    lListOS.remove(software);
                    if (software.equals(lOldDefaultOS)) {
                        lPC.setDefaultOS(null);
                    }
                }
                if (installRemoveConstant
                        .equals(HistoryConstants.INSTALL_SOFTWARE)) {
                    lListOS.add(software);
                }
            }
            if (lListOS.size() == 1) {
                lNewDefaultOS = lListOS.get(0);
                lPC.setDefaultOS(lNewDefaultOS);
            }
                
            List<Action> lActions = new ArrayList<Action>();
            
            String lOldDefaultOSName = "";
            String lNewDefaultOSName = "";
            if (lOldDefaultOS != null) {
                lOldDefaultOSName = lOldDefaultOS.getCompleteName();
            }
            if (lNewDefaultOS != null) {
                lNewDefaultOSName = lNewDefaultOS.getCompleteName();
            }
            
            if (!lOldDefaultOSName.equals(lNewDefaultOSName)) {
                lActions.add(new FieldModification(login, null,
                        Constants.MODIFICATION, null,
                        HistoryConstants.DEFAULT_OS, lOldDefaultOSName,
                        lNewDefaultOSName));
            }
            
            equipment = pcBean.merge(lPC, lActions);
        }
        
        return equipment;
    }
    
    /**
     * Search in database the list of Software products satisfying the provided
     * criterion value for one of the provided criteria fields.
     * (At least one criterion must be satisfied.)
     * 
     * @param pParameters
     *            the list of criteria fields to be applied. This parameter is
     *            only read and not modified.
     * @param pCriterion
     *            the criterion value
     * @return the list of Software products satisfying the provided parameters
     */
    public List<Software> advanceSearch(List<String> pParameters,
            String pCriterion) {
        
        Map<String, Object> lParameters = new HashMap<String, Object>();
        
        Set<Software> lSoftwaresSet = new HashSet<Software>();
        
        for (String lField : pParameters) {
            
            lParameters.put(lField, pCriterion);
            
            lSoftwaresSet.addAll(advanceSearch(lParameters));
            
            lParameters.clear();
        }
        
        return new ArrayList<Software>(lSoftwaresSet);
    }
    
    /**
     * Search in database the list of Softwares satisfying the provided
     * criteria.
     * 
     * @param parameters
     *            the Map of criteria to be applied.
     *            This parameter is only read and not modified.
     * @return the list of Softwares satisfying the provided parameters
     */
    public List<Software> advanceSearch(Map<String, Object> parameters) {
        
        Map<String, Object> localParameters =
                new HashMap<String, Object>(parameters);
        
        if (localParameters.isEmpty()) {
            return findAllSoftware();
        }
        
        removeEmptyParameter(localParameters);
        
        StringBuffer queryString = getQueryString(localParameters);
        
        TypedQuery<Software> query = getQuery(queryString, localParameters);
        
        try {
            return query.getResultList();
            
        }
        catch (NoResultException e) {
            log.warning("ADVANCE SEARCH SOFTWARE : NO RESULTS");
        }
        return Collections.emptyList();
    }
    
    /**
     * Fill the parameter value, if it exists into the parameters map,
     * into the provided query.
     * The parameter name shall be the same into the parameterized query and
     * into
     * the parameters map.
     * 
     * @param mapParameter
     *            the parameters map containing the parameter value.
     *            This parameter is only read and not modified.
     * @param query
     *            the parameterized query on which the parameter is to be set
     * @param parameter
     *            the parameter name
     */
    private void applyOneParameterOnQuery(Map<String, Object> mapParameter,
            Query query, String parameter) {
        
        if (mapParameter.get(parameter) != null) {
            /*---------- Treat some specific cases first ----------*/
            
            if (parameter.equals(OPERATING_SYSTEM)) {
                Object[] lParamList =
                        (Object[]) mapParameter.get(OPERATING_SYSTEM);
                if (lParamList.length == 0) {
                    lParamList = new Object[] { null };
                }
                query.setParameter(OPERATING_SYSTEM, Arrays.asList(lParamList));
            }
            else {
                /*---------- Treat "generic" cases ------------*/
                
                // since the parameter exists into the map, retrieve its value
                String lValue = (String) mapParameter.get(parameter);
                if (lValue.startsWith("!")) {
                    lValue = lValue.substring(1);
                }
                // replace '*' by '%'
                String like = StringUtil.parseToSQLRegex(lValue);
                // set the parameter value into the query
                try {
                    query.setParameter(parameter, like);
                }
                catch (IllegalArgumentException e) {
                    log.warning("Parameter "
                            + parameter
                            + " has not been set to query since it was not found.");
                }
                
            }
        }
    }
    
    /**
     * Clean the map of parameters by removing the null or empty values
     * 
     * @param mapParameter
     *            the parameters map
     */
    private void removeEmptyParameter(Map<String, Object> mapParameter) {
        for (String key : new ArrayList<String>(mapParameter.keySet())) {
            Object object = mapParameter.get(key);
            if (object == null
                    || (object instanceof String && ((String) object).isEmpty())) {
                mapParameter.remove(key);
            }
        }
    }
    
    /**
     * Return the predicate concerning the Equipments on which
     * the Software is installed.
     * Parameters values are not read since the query parameters are not filled
     * (except for the "family" parameter which concerns a table in database).
     * 
     * @param parameters
     *            the map of parameters to include into the query.
     *            May be modified.
     * @return the built predicate
     */
    private String
            getPredicateStringInstalledOn(Map<String, Object> parameters) {
        
        List<String> fields = new ArrayList<String>();
        
        if (parameters.containsKey(SN)) {
            fields.add("("
                    + getQueryCondition("a.airbusSN", SN,
                            (String) parameters.get(SN))
                    + " OR " + getQueryCondition("a.manufacturerSN", SN,
                            (String) parameters.get(SN))
                    + ")");
        }
        
        if (parameters.containsKey(MPN)) {
            fields.add(getQueryCondition("mpn.identifier", MPN,
                    (String) parameters.get(MPN)));
        }
        
        if (parameters.containsKey(APN)) {
            fields.add(getQueryCondition("apn.identifier", APN,
                    (String) parameters.get(APN)));
        }
        
        if (parameters.containsKey(TYPE)) {
            fields.add(getQueryCondition("a.typeArticle.label", TYPE,
                    (String) parameters.get(TYPE)));
        }
        
        StringBuffer sbOtherConditions = new StringBuffer("");
        for (int i = 0; i < fields.size(); i++) {
            sbOtherConditions.append(" AND ");
            sbOtherConditions.append(fields.get(i));
        }
        
        StringBuffer sbMainConditions = new StringBuffer("");
        if (!parameters.containsKey(FAMILY)) {
            sbMainConditions.append("EXISTS (SELECT a FROM Article a");
            sbMainConditions.append(" LEFT JOIN a.manufacturerPN mpn");
            sbMainConditions.append(" LEFT JOIN a.airbusPN apn");
            sbMainConditions.append(" WHERE s MEMBER OF a.softwares");
            sbMainConditions.append(sbOtherConditions + ")");
        }
        else {
            String[] lFamilyList = (String[]) parameters.get(FAMILY);
            if (lFamilyList.length == 0) {
                sbMainConditions.append("NOT EXISTS (SELECT a FROM Article a");
                sbMainConditions.append(" LEFT JOIN a.manufacturerPN mpn");
                sbMainConditions.append(" LEFT JOIN a.airbusPN apn");
                sbMainConditions.append(" WHERE s MEMBER OF a.softwares)");
            }
            else {
                for (int i = 0; i < lFamilyList.length; i++) {
                    if (i != 0) {
                        sbMainConditions.append(" OR ");
                    }
                    sbMainConditions.append(
                            "EXISTS (SELECT a FROM " + lFamilyList[i] + " a");
                    sbMainConditions.append(" LEFT JOIN a.manufacturerPN mpn");
                    sbMainConditions.append(" LEFT JOIN a.airbusPN apn");
                    sbMainConditions.append(" WHERE s MEMBER OF a.softwares");
                    sbMainConditions.append(sbOtherConditions + ")");
                }
            }
        }
        
        if (parameters.containsKey(FAMILY) || fields.size() > 0) {
            parameters.remove(FAMILY);
            return sbMainConditions.toString();
        }
        else {
            return null;
        }
        
    }
    
    /**
     * Create the query by setting the provided parameters values
     * into the provided query in string form.
     * 
     * @param queryString
     *            the parameterized query with empty fields
     * @param parameters
     *            the map of parameters which values are to be set into the
     *            query.
     *            This parameter is only read and not modified.
     * @return the created query (filled with parameters values)
     */
    private TypedQuery<Software> getQuery(StringBuffer queryString,
            Map<String, Object> parameters) {
        
        TypedQuery<Software> query =
                em.createQuery(queryString.toString(), Software.class);
        
        for (String key : parameters.keySet()) {
            applyOneParameterOnQuery(parameters, query, key);
        }
        
        return query;
    }
    
    /**
     * Return the query based on provided parameters in string form.
     * If the map of parameters is not empty, the returned query is
     * parameterized,
     * else the returned query is not parameterized.
     * 
     * @param parameters
     *            the map of parameters to include into the query.
     *            May be modified.
     * @return the built query
     */
    private StringBuffer getQueryString(Map<String, Object> parameters) {
        
        StringBuffer sb = new StringBuffer("SELECT s FROM Software s ");
        
        List<String> fields = new ArrayList<String>();
        if (parameters.containsKey(NAME)) {
            fields.add(getQueryCondition("s.name", NAME,
                    (String) parameters.get(NAME)));
        }
        
        if (parameters.containsKey(DISTRIBUTION)) {
            fields.add(getQueryCondition("s.distribution", DISTRIBUTION,
                    (String) parameters.get(DISTRIBUTION)));
        }
        
        if (parameters.containsKey(KERNEL)) {
            fields.add(getQueryCondition("s.kernel", KERNEL,
                    (String) parameters.get(KERNEL)));
        }
        
        if (parameters.containsKey(MANUFACTURER)) {
            fields.add(getQueryCondition("s.manufacturer", MANUFACTURER,
                    (String) parameters.get(MANUFACTURER)));
        }
        
        if (parameters.containsKey(DESCRIPTION)) {
            fields.add(getQueryCondition("s.description", DESCRIPTION,
                    (String) parameters.get(DESCRIPTION)));
        }
        
        if (parameters.containsKey(LICENCE)) {
            fields.add(getQueryCondition("s.licence", LICENCE,
                    (String) parameters.get(LICENCE)));
        }
        
        if (parameters.containsKey(OPERATING_SYSTEM)) {
            fields.add("s.operatingSystem IN :" + OPERATING_SYSTEM);
        }
        
        String subQuery = getPredicateStringInstalledOn(parameters);
        if (subQuery != null) {
            fields.add(subQuery);
        }
        
        for (int i = 0; i < fields.size(); i++) {
            if (i == 0) {
                sb.append(" WHERE ");
            }
            else {
                sb.append(" AND ");
            }
            sb.append(fields.get(i));
        }
        
        return sb;
    }

    
    /**
     * Return a query like condition on the given attribute, with the given
     * parameter and value.
     * Depending on the content of the value, the condition will be LIKE or NOT
     * LIKE,
     * and will test or not for NULL values.
     * 
     * @param pAttribute
     *            the attribute to be tested.
     * @param pParam
     *            the parameter to use.
     * @param pValue
     *            the value to test.
     * @return the built query condition as a string
     */
    private String getQueryCondition(String pAttribute, String pParam,
            String pValue) {
        String lResult = null;
        if (pValue != null) {
            if (pValue.startsWith("!")) {
                String lAdditionalCondition = "";
                if (pAttribute.indexOf('.') != pAttribute.lastIndexOf('.')) {
                    String lSecondAttribute =
                            pAttribute.substring(0,
                                    pAttribute.lastIndexOf('.'));
                    lAdditionalCondition =
                            "" + lSecondAttribute + " IS NULL OR ";
                }
                lResult =
                        "(" + lAdditionalCondition + pAttribute + " IS NULL OR "
                                + pAttribute + " NOT LIKE :" + pParam + ")";
            }
            else {
                lResult = "(" + pAttribute + " LIKE :" + pParam + ")";
            }
        }
        return lResult;
    }
    
    /**
     * Check if a software with the provided name, distribution and kernel
     * exists
     * 
     * @param pName
     *            the software name
     * @param pDistribution
     *            the software distribution
     * @param pKernel
     *            the software kernel
     * @return true if the software exists, else false
     */
    public boolean exist(String pName, String pDistribution, String pKernel) {
        
        if (pName == null || pDistribution == null) {
            throw new IllegalArgumentException();
        }
        
        String lKernel = pKernel;
        if (lKernel == null) {
            lKernel = "";
        }
        
        long result = 0;
        Query query =
                em.createNamedQuery(Software.EXIST_SOFTWARE_BY_NAME_DISTRIBUTION_KERNEL_QUERY);
        
        query.setParameter("name", pName);
        query.setParameter("distribution", pDistribution);
        query.setParameter("kernel", lKernel);
        
        result = (Long) query.getSingleResult();
        return (result > 0);
    }
    
    /**
     * @param pName
     *            the name of the software for which to search distributions
     * @return the list of distributions of all Softwares having the provided
     *         name
     */
    public List<String> findAllDistributions(String pName) {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(Software.FIND_DISTRIBUTIONS_BY_NAME_QUERY,
                        String.class);
        
        lQuery.setParameter("name", pName);
        
        return lQuery.getResultList();
    }
    
    /**
     * @param pOperatingSystem
     *            indicates if operating systems are to be retrieved (true) or
     *            simple softwares (false)
     * @param pName
     *            the name of the software for which to search distributions
     * @return the list of distributions of all Softwares being an Operating
     *         System or not and having the provided name
     */
    public List<String> findAllDistributions(boolean pOperatingSystem,
            String pName) {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(
                        Software.FIND_DISTRIBUTIONS_BY_OS_NAME_QUERY,
                        String.class);
        
        lQuery.setParameter("operatingSystem", pOperatingSystem);
        lQuery.setParameter("name", pName);
        
        return lQuery.getResultList();
    }
    
    /**
     * @param pName
     *            the name of the software for which to search kernels
     * @param pDistribution
     *            the distribution of the software for which to search kernels
     * @return the list of kernels of all Softwares having the provided name and
     *         distribution
     */
    public List<String> findAllKernels(String pName, String pDistribution) {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(Software.FIND_KERNELS_BY_NAME_DISTRI_QUERY,
                        String.class);
        
        lQuery.setParameter("name", pName);
        lQuery.setParameter("distribution", pDistribution);
        
        return lQuery.getResultList();
    }
    
    /**
     * @return the list of names of all Softwares
     */
    public List<String> findAllNames() {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(Software.FIND_NAMES_QUERY, String.class);
        
        return lQuery.getResultList();
    }
    
    /**
     * @param pOperatingSystem
     *            indicates if operating systems are to be retrieved (true) or
     *            simple softwares (false)
     * @return the list of names of all Softwares being an Operating System or
     *         not
     */
    public List<String> findAllNames(boolean pOperatingSystem) {
        
        TypedQuery<String> lQuery =
                em.createNamedQuery(Software.FIND_NAMES_BY_OS_QUERY,
                        String.class);
        
        lQuery.setParameter("operatingSystem", pOperatingSystem);
        
        return lQuery.getResultList();
    }
    
    /**
     * @return the list of all Software being operating systems
     */
    public List<Software> findAllOperatingSystems() {
        
        TypedQuery<Software> lQuery =
                em.createNamedQuery(Software.ALL_OS, Software.class);
        
        List<Software> lOperatingSystems = Collections.emptyList();
        
        lOperatingSystems = lQuery.getResultList();
        
        return lOperatingSystems;
    }
    
    /**
     * @return the list of all software
     */
    public List<Software> findAllSoftware() {
        TypedQuery<Software> query =
                em.createNamedQuery(Software.ALL_QUERY, Software.class);
        
        List<Software> softwares = Collections.emptyList();
        
        softwares = query.getResultList();
        
        return softwares;
    }
    
    /**
     * Search the software by its id
     * 
     * @param id
     *            the software id to search
     * @return the found software
     */
    public Software findById(Long id) {
        Software soft = em.find(Software.class, id);
        // soft.getEquipments();
        return soft;
        
    }
    
    /**
     * Return the software of which the complete name is equal to the provided
     * one
     * 
     * @param pCompleteName
     *            the searched complete name
     *            "&ltname&gt &ltdistribution&gt[ &ltkernel&gt]"
     * @return the list of found software
     */
    public Software findByCompleteName(String pCompleteName) {
        
        if (StringUtil.isEmptyOrNull(pCompleteName)) {
            return null;
        }
        
        TypedQuery<Software> query =
                em.createNamedQuery(Software.FIND_BY_COMPLETE_NAME_QUERY,
                        Software.class);
        query.setParameter("completeName", pCompleteName);
        
        try {
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            log.warning("More than one software found with complete name: "
                    + pCompleteName);
            return null;
        }
    }
    
    /**
     * Return the number of software of which the complete name is similar to
     * the provided one
     * 
     * @param pCompleteName
     *            the searched complete name
     *            "&ltname&gt &ltdistribution&gt[ &ltkernel&gt]"
     * @return the number of found software
     */
    public int findCountByCompleteName(String pCompleteName) {
        
        if (StringUtil.isEmptyOrNull(pCompleteName)) {
            return 0;
        }
        
        TypedQuery<Software> query =
                em.createNamedQuery(Software.FIND_BY_COMPLETE_NAME_QUERY,
                        Software.class);
        query.setParameter("completeName", pCompleteName);
        List<Software> results = new ArrayList<Software>();
        
        try {
            results = query.getResultList();
        }
        catch (NoResultException e) {
            return 0;
        }
        return results.size();
    }
    
    /**
     * Return a list of software of which the complete name is similar to the
     * provided one
     * 
     * @param pCompleteName
     *            the searched complete name
     *            "&ltname&gt &ltdistribution&gt[ &ltkernel&gt]"
     * @return the list of found software
     */
    public List<Software> suggestionListSoftware(String pCompleteName) {
        
        if (StringUtil.isEmptyOrNull(pCompleteName)) {
            throw new IllegalArgumentException("invalid complete name");
        }
        
        pCompleteName = StringUtil.parseToSQLRegex(pCompleteName);
        if (pCompleteName.indexOf("%") == -1) {
            pCompleteName = "%" + pCompleteName + "%";
        }
        
        TypedQuery<Software> query =
                em.createNamedQuery(
                        Software.SUGGESTION_BY_SUBS_COMPLETE_NAME_QUERY,
                        Software.class);
        query.setParameter("completeName", pCompleteName);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Return the list of Softwares satisfying the provided criteria,
     * without taking into account the suggested parameter criteria if it
     * exists,
     * and the suggested parameter value.
     * 
     * @param suggestName
     *            the suggested parameter name
     * @param suggestValue
     *            the suggested parameter value (it can contain '*')
     * @param parameters
     *            the parameters map (it can contain all possible parameters).
     *            This parameter is only read and not modified.
     * @return the list of Softwares satisfying the criteria and the suggested
     *         value
     */
    public List<Software> suggestionListSoftware(String suggestName,
            String suggestValue, Map<String, Object> parameters) {
        // create a local copy of the provided parameters
        Map<String, Object> localParameters =
                new HashMap<String, Object>(parameters);
        
        removeEmptyParameter(localParameters);
        localParameters.put(suggestName, suggestValue);
        
        // prepare the parameterized query string with parameter fields not
        // filled
        StringBuffer queryString = getQueryString(localParameters);
        
        // retrieve the parameterized query filled with the provided parameters
        // values
        // except the suggest one
        TypedQuery<Software> query = getQuery(queryString, localParameters);
        
        // add the suggest parameter to the local copy of parameters
        if (suggestValue.indexOf('*') == -1 && !suggestName.startsWith("!")) {
            suggestValue = "*" + suggestValue + "*";
        }
        
        // fill the suggest parameter into the query
        applyOneParameterOnQuery(localParameters, query, suggestName);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            log.warning("SUGGESTION LIST SOFTWARE " + suggestName
                    + ": NO RESULT");
            return Collections.emptyList();
        }
    }
    
}
