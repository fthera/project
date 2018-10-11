/*
 * ------------------------------------------------------------------------
 * Class : ObsolescenceBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.obso.ObsolescenceReference;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.stock.Stock;
import com.airbus.boa.view.stock.StockForPN;
import com.airbus.boa.view.stock.StockForSoftware;
import com.airbus.boa.view.stock.StockForType;

/**
 * Bean used for managing obsolescence data
 */
@Stateless
@LocalBean
public class ObsolescenceBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final String INST_COMMENTS = "instComments";
    
    private static Logger log = Logger.getLogger(ObsolescenceBean.class
            .getName());
    
    @PersistenceContext(name = "ObsolescenceService")
    private EntityManager em;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private LocationBean locationBean;
    
    private String installationNameFilter;
    private String instCommentsFilter;
    private String programFilter;
    
    /**
     * Create the obsolescence data in database
     * 
     * @param pObsolescenceData
     *            the obsolescence data to store
     * @return the persisted obsolescence data
     */
    public ObsolescenceData create(ObsolescenceData pObsolescenceData) {
        
        if (pObsolescenceData.getReference() == null) {
            throw new ValidationException(Constants.OBSO_INVALID_REFERENCE);
        }
        
        em.persist(pObsolescenceData);
        
        switch (pObsolescenceData.getReference().getReferenceType()) {
        case AIRBUSPN_TYPEARTICLE:
        case MANUFACTURERPN_TYPEARTICLE:
        case SOFTWARE:
            break;
        case TYPEPC:
            // Update the reference to obsolescence data from the PC Type
            Long lTypePCId =
                    pObsolescenceData.getReference().getTypePC().getId();
            TypePC lTypePCInCache =
                    (TypePC) articleBean.findTypeArticleById(lTypePCId);
            lTypePCInCache.setObsolescenceData(pObsolescenceData);
            break;
        default:
            break;
        }
        return pObsolescenceData;
    }
    
    /**
     * Merge the obsolescence data with the database
     * 
     * @param pObsolescenceData
     *            the obsolescence data
     */
    public void merge(ObsolescenceData pObsolescenceData) {
        em.merge(pObsolescenceData);
    }
    
    /**
     * Synchronize the persistence context to the database
     */
    public void flush() {
        em.flush();
    }
    
    /**
     * Remove the obsolescence data from the database
     * 
     * @param pObsolescenceData
     *            the obsolescence data to remove
     */
    public void delete(ObsolescenceData pObsolescenceData) {
        pObsolescenceData = em.merge(pObsolescenceData);
        
        em.remove(pObsolescenceData);
        
        switch (pObsolescenceData.getReference().getReferenceType()) {
        case AIRBUSPN_TYPEARTICLE:
        case MANUFACTURERPN_TYPEARTICLE:
        case SOFTWARE:
            break;
        case TYPEPC:
            // Update the reference to obsolescence data from the PC Type
            Long lTypePCId =
                    pObsolescenceData.getReference().getTypePC().getId();
            TypePC lTypePCInCache =
                    (TypePC) articleBean.findTypeArticleById(lTypePCId);
            lTypePCInCache.setObsolescenceData(null);
            break;
        default:
            break;
        }
    }
    
    /**
     * Search the list of obsolescence data corresponding to the provided
     * criteria
     * 
     * @param parameters
     *            map containing the criteria
     * @return the list of found obsolescence data
     */
    public List<ObsolescenceData> advanceSearch(Map<String, Object> parameters) {
        
        if (parameters.isEmpty()) {
            return findAllObsoData();
        }
        
        removeEmptyParameter(parameters);
        
        StringBuffer sb = new StringBuffer("Select o from ObsolescenceData o ");
        
        List<String> fields = new ArrayList<String>();
        
        if (parameters.containsKey("supplier")) {
            fields.add(getQueryCondition("o.supplier", "supplier",
                    (String) parameters.get("supplier")));
        }
        
        if (parameters.containsKey("airbusPN")) {
            fields.add(getQueryCondition("o.reference.airbusPN.identifier",
                    "airbusPN", (String) parameters.get("airbusPN")));
        }
        if (parameters.containsKey("manufacturerPN")) {
            fields.add(getQueryCondition(
                    "o.reference.manufacturerPN.identifier", "manufacturerPN",
                    (String) parameters.get("manufacturerPN")));
        }
        if (parameters.containsKey("typePC")) {
            fields.add(getQueryCondition("o.reference.typePC.label", "typePC",
                    (String) parameters.get("typePC")));
        }
        if (parameters.containsKey("software")) {
            fields.add(getQueryCondition(
                    "TRIM(TRAILING '" + Software.ATTRIBUTES_SEPARATOR
                            + "' FROM CONCAT(o.reference.software.name,'"
                            + Software.ATTRIBUTES_SEPARATOR
                            + "',o.reference.software.distribution,'"
                            + Software.ATTRIBUTES_SEPARATOR
                            + "',o.reference.software.kernel))",
                    "software", (String) parameters.get("software")));
        }
        
        if (parameters.containsKey("personInCharge")) {
            fields.add(getQueryCondition("o.personInCharge", "personInCharge",
                    (String) parameters.get("personInCharge")));
        }
        if (parameters.containsKey("comments")) {
            fields.add(getQueryCondition("o.commentOnStrategy", "comments",
                    (String) parameters.get("comments")));
        }
        
        Object[] lActionIds = (Object[]) parameters.get("actionIds");
        if (lActionIds != null) {
            if (lActionIds.length == 0) {
                lActionIds = new Object[] { null };
            }
            fields.add("o.currentAction.id IN :actionIds");
            parameters.put("actionIds", Arrays.asList(lActionIds));
        }
        
        Object[] lStrategyIds = (Object[]) parameters.get("strategyIds");
        if (lStrategyIds != null) {
            if (lStrategyIds.length == 0) {
                lStrategyIds = new Object[] { null };
            }
            fields.add("o.strategyKept.id IN :strategyIds");
            parameters.put("strategyIds", Arrays.asList(lStrategyIds));
        }
        
        Object[] lManufacturerStatusIds =
                (Object[]) parameters.get("manufacturerStatusIds");
        if (lManufacturerStatusIds != null) {
            if (lManufacturerStatusIds.length == 0) {
                lManufacturerStatusIds = new Object[] { null };
            }
            fields.add("o.manufacturerStatus.id IN :manufacturerStatusIds");
            parameters.put("manufacturerStatusIds",
                    Arrays.asList(lManufacturerStatusIds));
        }
        
        Object[] lAirbusStatusIds =
                (Object[]) parameters.get("airbusStatusIds");
        if (lAirbusStatusIds != null) {
            if (lAirbusStatusIds.length == 0) {
                lAirbusStatusIds = new Object[] { null };
            }
            fields.add("o.airbusStatus.id IN :airbusStatusIds");
            parameters.put("airbusStatusIds", Arrays.asList(lAirbusStatusIds));
        }
        
        Object[] lConsultPeriodIds =
                (Object[]) parameters.get("consultPeriodIds");
        if (lConsultPeriodIds != null) {
            if (lConsultPeriodIds.length == 0) {
                lConsultPeriodIds = new Object[] { null };
            }
            fields.add("o.consultPeriod.id IN :consultPeriodIds");
            parameters.put("consultPeriodIds",
                    Arrays.asList(lConsultPeriodIds));
        }
        
        if (parameters.containsKey("endOfOrderDate1")
                || parameters.containsKey("endOfOrderDate2")) {
            fields.add(buildRequest(parameters, " o.endOfOrderDate",
                    "endOfOrderDate1", "endOfOrderDate2"));
        }
        
        if (parameters.containsKey("obsolescenceDate1")
                || parameters.containsKey("obsolescenceDate2")) {
            fields.add(buildRequest(parameters, " o.obsolescenceDate",
                    "obsolescenceDate1", "obsolescenceDate2"));
        }
        
        if (parameters.containsKey("endOfSupportDate1")
                || parameters.containsKey("endOfSupportDate2")) {
            fields.add(buildRequest(parameters, " o.endOfSupportDate",
                    "endOfSupportDate1", "endOfSupportDate2"));
        }
        
        if (parameters.containsKey("endOfProductionDate1")
                || parameters.containsKey("endOfProductionDate2")) {
            fields.add(buildRequest(parameters, " o.endOfProductionDate",
                    "endOfProductionDate1", "endOfProductionDate2"));
        }
        
        if (parameters.containsKey("continuityDate1")
                || parameters.containsKey("continuityDate2")) {
            fields.add(buildRequest(parameters, " o.continuityDate",
                    "continuityDate1", "continuityDate2"));
        }
        
        if (parameters.containsKey("lastObsolescenceUpdate1")
                || parameters.containsKey("lastObsolescenceUpdate2")) {
            fields.add(buildRequest(parameters, " o.lastObsolescenceUpdate",
                    "lastObsolescenceUpdate1", "lastObsolescenceUpdate2"));
        }
        
        if (fields.size() > 0) {
            sb.append(" WHERE ");
            // AJOUT DES LIGNES
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    sb.append(" AND ");
                }
                sb.append(fields.get(i));
            }
        }
        
        // log.warning(sb.toString()) ;
        TypedQuery<ObsolescenceData> query =
                em.createQuery(sb.toString(), ObsolescenceData.class);
        
        applyOneParameterOnQuery(parameters, query, "airbusPN");
        applyOneParameterOnQuery(parameters, query, "manufacturerPN");
        applyOneParameterOnQuery(parameters, query, "typePC");
        applyOneParameterOnQuery(parameters, query, "software");
        applyOneParameterOnQuery(parameters, query, "supplier");
        applyOneParameterOnQuery(parameters, query, "actionIds");
        applyOneParameterOnQuery(parameters, query, "strategyIds");
        applyOneParameterOnQuery(parameters, query, "manufacturerStatusIds");
        applyOneParameterOnQuery(parameters, query, "airbusStatusIds");
        applyOneParameterOnQuery(parameters, query, "consultPeriodIds");
        applyOneParameterOnQuery(parameters, query, "personInCharge");
        applyOneParameterOnQuery(parameters, query, "comments");
        
        applyOneDateParameterOnQuery(parameters, query, "endOfOrderDate1",
                "endOfOrderDate2");
        applyOneDateParameterOnQuery(parameters, query, "obsolescenceDate1",
                "obsolescenceDate2");
        applyOneDateParameterOnQuery(parameters, query, "endOfSupportDate1",
                "endOfSupportDate2");
        applyOneDateParameterOnQuery(parameters, query, "endOfProductionDate1",
                "endOfProductionDate2");
        applyOneDateParameterOnQuery(parameters, query, "continuityDate1",
                "continuityDate2");
        applyOneDateParameterOnQuery(parameters, query,
                "lastObsolescenceUpdate1", "lastObsolescenceUpdate2");
        
        try {
            for (ObsolescenceData obso : query.getResultList()) {
                computeStock(obso);
            }
            
            // Application des critÈres de recherche portant sur des champs
            // calculÈs,
            // ‡ la liste de rÈsultat obtenue par la requÍte SQL.
            return checkAutomaticFields(parameters, query.getResultList());
            
        }
        catch (NoResultException e) {
            log.warning("ADVANCE SEARCH OBSOLESCENCE DATA : NO RESULTS");
            
        }
        
        return Collections.emptyList();
    }
    
    private String getQueryCondition(String attribute, String param,
            String value) {
        return getQueryCondition(attribute, param, value, "");
    }
    
    private String getQueryCondition(String attribute, String param,
            String value, String testNull) {
        String lResult = null;
        if (value != null) {
            if (value.startsWith("!")) {
                String lAdditionalCondition = "";
                if (!testNull.isEmpty()) {
                    lAdditionalCondition = "" + testNull + " IS NULL OR ";
                }
                lResult =
                        "(" + lAdditionalCondition + attribute + " IS NULL OR "
                                + attribute + " NOT LIKE :" + param + ")";
            }
            else {
                lResult = "(" + attribute + " LIKE :" + param + ")";
            }
        }
        return lResult;
    }
    
    private void applyOneDateParameterOnQuery(Map<String, Object> mapParameter,
            Query query, String parameter1, String parameter2) {
        
        if (mapParameter.get(parameter2) != null
                && mapParameter.get(parameter2) == null) {
            
            Date date2 = (Date) mapParameter.get(parameter2);
            // log.warning(" DATE : " + date2 );
            query.setParameter(parameter2, date2, TemporalType.TIMESTAMP);
            
        }
        else if (mapParameter.get(parameter1) != null) {
            
            Date date1 = (Date) mapParameter.get(parameter1);
            Date date2 = (Date) mapParameter.get(parameter2);
            // log.warning("acquisitiondate1 contient : "+ date1);
            // log.warning("acquisitiondate2 contient : "+ date2);
            if (date2 == null) {
                date2 = new Date();
            }
            query.setParameter(parameter1, date1, TemporalType.TIMESTAMP);
            query.setParameter(parameter2, date2, TemporalType.TIMESTAMP);
        }
    }
    
    private void applyOneParameterOnQuery(Map<String, Object> mapParameter,
            Query query, String parameter) {
        
        if (mapParameter.get(parameter) != null) {
            if (mapParameter.get(parameter) instanceof String) {
                // Le param√®tre est dans la liste des param√®tres concern√©s
                // par la
                // recherche
                // On r√©cup√®re la valeur √† rechercher
                String value = (String) mapParameter.get(parameter);
                // Remplacement de '*' par '%'
                String like = StringUtil.parseToSQLRegex(value);
                
                // Mise √† jour de la valeur du param√®tre
                query.setParameter(parameter, like);
            }
            else {
                query.setParameter(parameter, mapParameter.get(parameter));
            }
        }
    }
    
    private String buildRequest(Map<String, Object> parameters,
            String fieldName, String startDate, String endDate) {
        String request = fieldName;
        if (parameters.get(endDate) != null
                && parameters.get(startDate) == null) {
            request += " < :" + endDate + " ";
        }
        else if (parameters.get(startDate) != null) {
            // OPERATEUR BETWEEN
            request += " BETWEEN :" + startDate + " and :" + endDate + " ";
        }
        
        return request;
    }
    
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
     * Search, in the provided list of obsolescence data, the list of
     * obsolescence data corresponding to the provided criteria, only for
     * computed fields
     * 
     * @param parameters
     *            map containing the criteria
     * @param queryResult
     *            the list of obsolescence data into which to search
     * @return the list of found obsolescence data
     */
    private List<ObsolescenceData> checkAutomaticFields(
            Map<String, Object> parameters, List<ObsolescenceData> queryResult) {
        
        List<ObsolescenceData> result = new ArrayList<ObsolescenceData>();
        
        if (!parameters.containsKey("constituantName")
                && !parameters.containsKey("manufacturer")
                && !parameters.containsKey("installation")
                && !parameters.containsKey(INST_COMMENTS)
                && !parameters.containsKey("program")
                && !parameters.containsKey("nextConsultingDate1")
                && !parameters.containsKey("nextConsultingDate2")) {
            return queryResult;
        }
        
        // Filters building
        String constituantNameFilter;
        if (parameters.containsKey("constituantName")) {
            constituantNameFilter =
                    StringUtil.parseToRegex((String) parameters
                            .get("constituantName"));
        }
        else {
            constituantNameFilter = ".*";
        }
        
        String lManufacturerFilter;
        if (parameters.containsKey("manufacturer")) {
            lManufacturerFilter =
                    StringUtil.parseToRegex(
                            (String) parameters.get("manufacturer"))
                            .toUpperCase();
        }
        else {
            lManufacturerFilter = ".*";
        }
        
        if (parameters.containsKey("installation")) {
            installationNameFilter =
                    StringUtil.parseToRegex((String) parameters
                            .get("installation"));
        }
        else {
            installationNameFilter = ".*";
        }
        
        if (parameters.containsKey(INST_COMMENTS)) {
            instCommentsFilter =
                    StringUtil.parseToRegex((String) parameters
                            .get(INST_COMMENTS));
        }
        else {
            instCommentsFilter = ".*";
        }
        
        if (parameters.containsKey("program")) {
            programFilter =
                    StringUtil.parseToRegex((String) parameters.get("program"));
        }
        else {
            programFilter = ".*";
        }
        
        for (ObsolescenceData obso : queryResult) {
            
            String name = obso.getConstituantName();
            if (!name.matches(constituantNameFilter)) {
                continue;
            }
            
            String lManufacturer = obso.getManufacturer();
            if (lManufacturer != null) {
                lManufacturer = lManufacturer.toUpperCase();
            }
            if ((lManufacturer == null && lManufacturerFilter != null)
                    || (lManufacturer != null && !lManufacturer
                            .matches(lManufacturerFilter))) {
                continue;
            }
            
            if (!checkConsultingDate(parameters, obso)) {
                continue;
            }
            
            if (!checkConsultingInstallation(parameters, obso)) {
                continue;
            }
            
            // criteria are satisfied, add the obsolescence data to the list
            result.add(obso);
        }
        
        return result;
    }
    
    /**
     * Fonction de v√©rification du champ "date de consultation"
     */
    private Boolean checkConsultingDate(Map<String, Object> parameters,
            ObsolescenceData obso) {
        if (parameters.containsKey("nextConsultingDate1")
                || parameters.containsKey("nextConsultingDate2")) {
            // Calcul de la date de prochaine consultation
            Date nextConsultingDate = obso.getNextConsultingDate();
            if (nextConsultingDate == null) {
                // Pas de date, constituant non s√©lectionn√©
                return false;
            }
            
            if (parameters.containsKey("nextConsultingDate1")) {
                if (((Date) parameters.get("nextConsultingDate1"))
                        .after(nextConsultingDate)) {
                    // Date incorrecte, constituant non s√©lectionn√©
                    return false;
                }
            }
            if (parameters.containsKey("nextConsultingDate2")) {
                if (((Date) parameters.get("nextConsultingDate2"))
                        .before(nextConsultingDate)) {
                    // Date incorrecte, constituant non s√©lectionn√©
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Fonction de vÈrification des champs d'une installation
     */
    private Boolean checkConsultingInstallation(Map<String, Object> pParameters,
            ObsolescenceData pObso) {
        Boolean lFilterOk = true;
        if (pParameters.containsKey("installation")
                || pParameters.containsKey(INST_COMMENTS)
                || pParameters.containsKey("program")) {
            lFilterOk = false;
            
            // Construction de la liste des installations du composant
            for (Installation installation : pObso.getInstallations()) {
                String lName = installation.getName();
                String lComments = installation.getComments();
                String lProgram =
                        installation.getAircraftProgram().getDefaultValue();
                
                if ((pParameters.containsKey(INST_COMMENTS)
                        && lComments == null)
                        || (pParameters.containsKey("program")
                                && lProgram == null)) {
                    // Champs non renseignÈs, constituant non sÈlectionnÈ
                    continue;
                }
                
                if (lName.matches(installationNameFilter)
                        && (lComments == null
                                || lComments.matches(instCommentsFilter))
                        && (lProgram == null
                                || lProgram.matches(programFilter))) {
                    // Champs correspondant aux critËres, constituant
                    // sÈlectionnÈ
                    lFilterOk = true;
                }
            }
        }
        
        return lFilterOk;
    }
    
    /**
     * Compute the stock for the given Obsolescence data
     * 
     * @param pObsolescenceData
     *            the obsolescence data for which the stock shall be computed
     */
    public void computeStock(ObsolescenceData pObsolescenceData) {
        ObsolescenceReference lRef = pObsolescenceData.getReference();
        Stock lStock = null;
        if (lRef.getAirbusPN() != null) {
            lStock = new StockForPN(articleBean, historyBean, locationBean,
                    lRef.getTypeArticle(), lRef.getAirbusPN());
        }
        else if (lRef.getManufacturerPN() != null) {
            lStock = new StockForPN(articleBean, historyBean, locationBean,
                    lRef.getTypeArticle(), lRef.getManufacturerPN());
        }
        else if (lRef.getTypePC() != null) {
            lStock = new StockForType(articleBean, historyBean, locationBean,
                    lRef.getTypePC());
        }
        else if (lRef.getSoftware() != null) {
            lStock = new StockForSoftware(articleBean, historyBean,
                    locationBean, lRef.getSoftware());
        }
        pObsolescenceData.setStock(lStock);
    }
    
    /**
     * Return the list of obsolescence data for the reference
     * 
     * @param pReference
     *            the reference
     * @return the list of found obsolescence data, or null
     */
    public List<ObsolescenceData> findAllByReference(PN pReference) {
        
        StringBuilder lQuerySB = new StringBuilder("select obso from ");
        lQuerySB.append(ObsolescenceData.class.getSimpleName()).append(" obso");
        
        if (pReference instanceof AirbusPN) {
            lQuerySB.append(" where obso.reference.airbusPN = :reference");
        }
        else if (pReference instanceof ManufacturerPN) {
            lQuerySB.append(" where obso.reference.manufacturerPN = :reference");
        }
        else {
            throw new IllegalArgumentException("reference invalide");
        }
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createQuery(lQuerySB.toString(), ObsolescenceData.class);
        lQuery.setParameter("reference", pReference);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Return the list of obsolescence data for the reference
     * 
     * @param pReference
     *            the reference
     * @return the found obsolescence data, or null
     */
    public List<ObsolescenceData> findAllByReference(TypeArticle pReference) {
        
        StringBuilder lQuerySB = new StringBuilder("select obso from ");
        lQuerySB.append(ObsolescenceData.class.getSimpleName()).append(" obso");
        
        lQuerySB.append(" where obso.reference.typeArticle = :reference");
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createQuery(lQuerySB.toString(), ObsolescenceData.class);
        
        lQuery.setParameter("reference", pReference);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @return a list containing all obsolescence data from database
     */
    public List<ObsolescenceData> findAllObsoData() {
        
        TypedQuery<ObsolescenceData> query =
                em.createNamedQuery(ObsolescenceData.ALL_QUERY,
                        ObsolescenceData.class);
        
        List<ObsolescenceData> obsoList = query.getResultList();
        
        Collections.sort(obsoList, new Comparator<ObsolescenceData>() {
            
            @Override
            public int compare(ObsolescenceData arg0, ObsolescenceData arg1) {
                
                return arg0.getConstituantName().compareTo(
                        arg1.getConstituantName());
            }
        });
        
        return obsoList;
    }
    
    /**
     * @param pSelectedObsoId
     *            the obsolescence data id
     * @return the found obsolescence data, else null
     */
    public ObsolescenceData findById(Long pSelectedObsoId) {
        
        try {
            ObsolescenceData lObsolescenceData =
                    em.find(ObsolescenceData.class, pSelectedObsoId);
            return lObsolescenceData;
        }
        catch (NoResultException e) {
            log.warning("findById Obsolescence NO RESULTS");
            return null;
        }
    }
    
    /**
     * Return the obsolescence data for the reference
     * 
     * @param pReference
     *            the reference
     * @return the found obsolescence data, or null
     */
    public ObsolescenceData findByReference(Software pReference) {
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createNamedQuery(
                        ObsolescenceData.OBSOLESCENCE_DATA_BY_SOFTWARE,
                        ObsolescenceData.class);
        lQuery.setParameter("reference", pReference);
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException("More than one value for "
                    + pReference, e);
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Return the obsolescence data for the reference
     * 
     * @param pReference
     *            the reference
     * @return the found obsolescence data, or null
     */
    public ObsolescenceData findByReference(TypePC pReference) {
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createNamedQuery(
                        ObsolescenceData.OBSOLESCENCE_DATA_BY_TYPEPC,
                        ObsolescenceData.class);
        lQuery.setParameter("reference", pReference);
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException("More than one value for "
                    + pReference, e);
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Return the obsolescence data for the reference
     * 
     * @param pReferenceAirbusPN
     *            the reference Airbus PN
     * @param pReferenceTypeArticle
     *            the reference type article
     * @return the found obsolescence data, or null
     */
    public ObsolescenceData findByReference(AirbusPN pReferenceAirbusPN,
            TypeArticle pReferenceTypeArticle) {
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createNamedQuery(
                        ObsolescenceData.OBSOLESCENCE_DATA_BY_AIRBUSPN_TYPEARTICLE,
                        ObsolescenceData.class);
        lQuery.setParameter("refAirbusPN", pReferenceAirbusPN);
        lQuery.setParameter("refTypeArticle", pReferenceTypeArticle);
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException("More than one value for "
                    + pReferenceAirbusPN + " - " + pReferenceTypeArticle, e);
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Return the obsolescence data for the reference
     * 
     * @param pReferenceManufacturerPN
     *            the reference Manufacturer PN
     * @param pReferenceTypeArticle
     *            the reference type article
     * @return the found obsolescence data, or null
     */
    public ObsolescenceData findByReference(
            ManufacturerPN pReferenceManufacturerPN,
            TypeArticle pReferenceTypeArticle) {
        
        TypedQuery<ObsolescenceData> lQuery =
                em.createNamedQuery(
                        ObsolescenceData.OBSOLESCENCE_DATA_BY_MANUFACTURERPN_TYPEARTICLE,
                        ObsolescenceData.class);
        lQuery.setParameter("refManufacturerPN", pReferenceManufacturerPN);
        lQuery.setParameter("refTypeArticle", pReferenceTypeArticle);
        
        try {
            return lQuery.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException("More than one value for "
                    + pReferenceManufacturerPN + " - " + pReferenceTypeArticle,
                    e);
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @return the articleBean
     */
    public ArticleBean getArticleBean() {
        return articleBean;
    }
    
    /**
     * Check that no article, with same PN and Article Type, has already
     * obsolescence data associated to another PN: <br>
     * - if PN is an Airbus one, check that no article having this PN and the
     * Article type has a manufacturer PN already obsolescence managed, <br>
     * - if PN is a manufacturer one, check that no article having this PN and
     * the Article type has an Airbus PN already obsolescence managed. <br>
     * 
     * @param pPN
     *            the PN of the reference to check
     * @param pTypeArticle
     *            the Article type of the reference to check
     * @return true if the obsolescence data can be created for the PN and the
     *         Article type, else false
     */
    public boolean isObsoCreationValidForExistingPN(PN pPN,
            TypeArticle pTypeArticle) {
        
        StringBuilder lQuerySB =
                new StringBuilder(
                        "select count(obso) from ObsolescenceData obso ");
        
        if (pPN instanceof AirbusPN) {
            lQuerySB.append("WHERE obso.reference.manufacturerPN.id in ");
            lQuerySB.append("(select distinct art.manufacturerPN.id from Article art ");
            lQuerySB.append("where art.airbusPN = :pn and art.manufacturerPN is not null ");
            lQuerySB.append("and art.typeArticle = :typeArticle)");
        }
        else {
            lQuerySB.append("WHERE obso.reference.airbusPN.id in ");
            lQuerySB.append("(select distinct art.airbusPN.id from Article art ");
            lQuerySB.append("where art.manufacturerPN = :pn and art.airbusPN is not null ");
            lQuerySB.append("and art.typeArticle = :typeArticle)");
        }
        
        Query lQuery = em.createQuery(lQuerySB.toString());
        lQuery.setParameter("pn", pPN);
        lQuery.setParameter("typeArticle", pTypeArticle);
        
        Number lCountResult = (Number) lQuery.getSingleResult();
        
        return lCountResult.longValue() == 0L;
    }
    
    /**
     * Check that the association between the PN and the Article Type is
     * correct.
     * 
     * @param pPN
     *            the PN of the reference to check
     * @param pTypeArticle
     *            the Article type of the reference to check
     * @return true if the obsolescence data can be created for the PN and the
     *         Article type, else false
     */
    public boolean isObsoCreationValidForPNAndType(PN pPN,
            TypeArticle pTypeArticle) {
        
        List<TypeArticle> lTypeArticles =
                articleBean.findAllTypeArticleForPN(pPN);
        
        return lTypeArticles.contains(pTypeArticle);
    }
    
    /**
     * Return a boolean indicating if the provided reference is already
     * obsolescence managed.
     * 
     * @param pReference
     *            the reference to check (should not be null)
     * @return true if the reference is already obsolescence managed, else false
     */
    public boolean isReferenceAlreadyManagedIntoObso(
            ObsolescenceReference pReference) {
        
        StringBuffer lQuerySB =
                new StringBuffer(
                        "SELECT COUNT(obso) from ObsolescenceData obso where ");
        Query lQuery;
        
        switch (pReference.getReferenceType()) {
        case AIRBUSPN_TYPEARTICLE:
            lQuerySB.append("obso.reference.airbusPN = :airbusPN");
            lQuerySB.append(" and obso.reference.typeArticle = :typeArticle");
            lQuery = em.createQuery(lQuerySB.toString());
            lQuery.setParameter("airbusPN", pReference.getAirbusPN());
            lQuery.setParameter("typeArticle", pReference.getTypeArticle());
            break;
        case MANUFACTURERPN_TYPEARTICLE:
            lQuerySB.append("obso.reference.manufacturerPN = :manufacturerPN");
            lQuerySB.append(" and obso.reference.typeArticle = :typeArticle");
            lQuery = em.createQuery(lQuerySB.toString());
            lQuery.setParameter("manufacturerPN",
                    pReference.getManufacturerPN());
            lQuery.setParameter("typeArticle", pReference.getTypeArticle());
            break;
        case SOFTWARE:
            lQuerySB.append("obso.reference.software = :software");
            lQuery = em.createQuery(lQuerySB.toString());
            lQuery.setParameter("software", pReference.getSoftware());
            break;
        case TYPEPC:
            lQuerySB.append("obso.reference.typePC = :typePC");
            lQuery = em.createQuery(lQuerySB.toString());
            lQuery.setParameter("typePC", pReference.getTypePC());
            break;
        default:
            throw new IllegalArgumentException("Reference type invalid");
        }
        
        return (((Long) lQuery.getSingleResult()) == 1L);
    }
    
    /**
     * @return the softwareBean
     */
    public SoftwareBean getSoftwareBean() {
        return softwareBean;
    }
    
}
