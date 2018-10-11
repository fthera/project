/*
 * ------------------------------------------------------------------------
 * Class : ArticleQueryBuilder
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.richfaces.component.SortOrder;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.util.StringUtil;

/**
 * Class generating queries in string form for Articles.
 */
public class ArticleQueryBuilder extends EntityQueryBuilder {
    
    private static final long serialVersionUID = 1L;
    
    /** Key for the article acquisition date criterion */
    public static final String ACQUISITION_DATE = "acquisitionDate";
    /** Key for the article acquisition date minimum value criterion */
    public static final String ACQUISITION_DATE_MIN = "acquisitionDateMin";
    /** Key for the article acquisition date maximum value criterion */
    public static final String ACQUISITION_DATE_MAX = "acquisitionDateMax";
    /** Key for the board active stock control date criterion */
    public static final String ACTIVE_STOCK_CONTROL_DATE =
            "activeStockControlDate";
    /** Key for the PC administrator criterion */
    public static final String ADMIN = "admin";
    /** Key for the PC business allocation criterion */
    public static final String ALLOCATION = "allocation";
    /** Key for the article Airbus PN criterion */
    public static final String AIRBUS_PN = "airbusPN.identifier";
    /** Key for the article Airbus SN criterion */
    public static final String AIRBUS_SN = "airbusSN";
    /** Key for the PC assignment criterion */
    public static final String ASSIGNMENT = "assignment";
    /** Key for the PC availability date criterion */
    public static final String AVAILABILITY_DATE = "availabilityDate";
    /** Key for the PC availability status criterion */
    public static final String AVAILABILITY_STATUS = "availabilityStatus";
    /** Key for the article family criterion */
    public static final String FAMILY = "family";
    /** Key for the article CMS Code criterion */
    public static final String CMS_CODE = "cmsCode";
    /** Key for the article general comment or PC comment criterion */
    public static final String COMMENT = "comment";
    /** Key for the article name of container criterion */
    public static final String CONTAINER_NAME = "container.containerName";
    /** Key for the article type of container criterion */
    public static final String CONTAINER_TYPE = "containerType";
    /** Key for the PC default OS criterion */
    public static final String DEFAULT_OS_COMPLETE_NAME =
            "defaultOS.completeName";
    /** Key for the PC default OS distribution criterion */
    public static final String DEFAULT_OS_DISTRIBUTION =
            "defaultOSDistribution";
    /** Key for the PC default OS kernel criterion */
    public static final String DEFAULT_OS_KERNEL = "defaultOSKernel";
    /** Key for the PC default OS name criterion */
    public static final String DEFAULT_OS_NAME = "defaultOSName";
    /** Key for the article's department in charge criterion */
    public static final String DEPARTMENT_IN_CHARGE = "departmentInCharge";
    /** Key for the article designation criterion */
    public static final String DESIGNATION = "designation";
    /** Key for the domain criterion */
    public static final String DOMAIN = "domain";
    /** Key for the article name of external location criterion */
    public static final String EXTERNAL_LOCATION_NAME =
            "location.externalLocationName";
    /** Key for the article type of external location criterion */
    public static final String EXTERNAL_LOCATION_TYPE = "externalLocationType";
    /** Key for the PC function criterion */
    public static final String FUNCTION = "function";
    /** Key for the PC has communication ports criterion */
    public static final String HAS_COM_PORTS = "hasComPorts";
    /** Key for the PC has default OS criterion */
    public static final String HAS_DEFAULT_OS = "hasDefaultOS";
    /** Key for the article's has documents criterion */
    public static final String HAS_DOCUMENTS = "hasDocuments";
    /** Key for the PC has softwares criterion */
    public static final String HAS_SOFTWARES = "hasSoftwares";
    /** Key for the article history comment criterion */
    public static final String HISTORY_COMMENT = "HComment";
    /** Key for the PC in charge (user) criterion */
    public static final String IN_CHARGE_USER = "inCharge";
    /** Key for the PC in charge (login details) criterion */
    public static final String IN_CHARGE_LOGIN_DETAILS =
            "inCharge.loginDetails";
    /** Key for the PC communication port IP address criterion */
    public static final String IP_ADDRESS = "ipAddress";
    /** Key for the article name of location criterion */
    public static final String LOCATION_NAME = "location.locationName";
    /** Key for the article type of location criterion */
    public static final String LOCATION_TYPE = "locationType";
    /** Key for the PC communication port MAC address criterion */
    public static final String MAC_ADDRESS = "macAddress";
    /** Key for the article Manufacturer PN criterion */
    public static final String MANUFACTURER_PN = "manufacturerPN.identifier";
    /** Key for the article Manufacturer SN criterion */
    public static final String MANUFACTURER_SN = "manufacturerSN";
    /** Key for the PC Name criterion */
    public static final String NAME = "name";
    /** Key for the PC number of screens criterion */
    public static final String NB_SCREENS = "nbScreens";
    /** Key for the PC number of screens minimum value criterion */
    public static final String NB_SCREENS_MIN = "nbScreensMin";
    /** Key for the PC number of screens maximum value criterion */
    public static final String NB_SCREENS_MAX = "nbScreensMax";
    /** Key for the PC communication port network criterion */
    public static final String NETWORK = "network";
    /** Key for the PC operating systems criterion */
    public static final String OPERATING_SYSTEMS = "operatingSystemsNames";
    /** Key for the PC owner criterion */
    public static final String OWNER = "owner";
    /** Key for the PC owner siglum criterion */
    public static final String OWNER_SIGLUM = "ownerSiglum";
    /** Key for the PC platform criterion */
    public static final String PLATFORM = "platform";
    /** Key for the PC communication port name criterion */
    public static final String PORT_NAME = "portName";
    /** Key for the PC Product Type criterion */
    public static final String PRODUCT_TYPE = "productType";
    /** Key for the article Airbus or Manufacturer SN criterion */
    public static final String SN = "sn";
    /** Key for the PC communication port socket criterion */
    public static final String SOCKET = "socket";
    /** Key for the PC installed software distribution criterion */
    public static final String SOFTWARE_DISTRIBUTION = "softDistribution";
    /** Key for the PC installed software kernel criterion */
    public static final String SOFTWARE_KERNEL = "softKernel";
    /** Key for the PC installed software name criterion */
    public static final String SOFTWARE_NAME = "softName";
    /** Key for the article state criterion */
    public static final String STATE = "state";
    /** Key for the article use state criterion */
    public static final String USE_STATE = "useState";
    /** Key for the article type criterion */
    public static final String TYPE = "typeArticle.label";
    /** Key for the PC business usage criterion */
    public static final String USAGE = "usage";
    /** Key for the PC communication port comment criterion */
    public static final String COMMENT_PORT = "commentPort";
    /** Key for the article date comments criterion */
    public static final String DATED_COMMENT = "datedComment";
    /** Key for the PC specificity criterion */
    public static final String PC_SPECIFICITY = "PCSpecificity";
    
    private static String convertParamName(String pField) {
        return pField.replace('.', '_');
    }
    
    /**
     * Constructor
     */
    public ArticleQueryBuilder() {
        
        super(Article.class.getSimpleName());
    }
    
    /**
     * Constructor
     * 
     * @param pEntityClass
     *            the entity class name to retrieve
     * @param pIdentifier
     *            the entity identifier into the query
     */
    private ArticleQueryBuilder(String pEntityClass, String pIdentifier) {
        
        super(pEntityClass, pIdentifier);
    }
    
    @Override
    protected void updateCriteriaResultingValues(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        lCriteria.initResultingValues();
        
        if (pIndex == 0) {
            
            Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
            
            // Build the base query
            if (lInputCriteria.get(FAMILY) != null) {
                
                String lEntityClass = (String) lInputCriteria.get(FAMILY);
                
                if (lEntityClass.equals("Rack")) {
                    // Patch because the setEntityClass does not work with Racks
                    addJoin("Main", "INNER JOIN " + lEntityClass
                            + " lEntity ON lArticle.id = lEntity.id");
                    // if searched articles are Racks, exclude Switches
                    // (since they inherit from Rack)
                    addMandatoryCondition("lArticle.id NOT IN "
                            + "(SELECT switch.id FROM Switch switch)");
                }
                else {
                    setEntityClass(lEntityClass);
                }
            }
        }
        
        // Add the Article conditions
        updateArticleConditions(pIndex);
        
        // Add the Software condition
        updateSoftwareCondition(pIndex);
        
        // Add the Designation condition
        updateDesignationConditions(pIndex);
        
        // Add the PC specific condition
        updatePCCondition(pIndex);
        
        // Add the Comment condition
        updateCommentCondition(pIndex);
        
        // Add the Communication port condition
        updatePortCondition(pIndex);
        
        // Add the Location condition
        updateLocationConditions(pIndex);
        
        // Add the External Location condition
        updateExtLocationConditions(pIndex);
        
        // Add the Container condition
        updateContainerConditions(pIndex);
    }
    
    /**
     * Build a list of conditions to put in a WHERE clause of a query in string
     * form for criteria concerning the Article simple attributes.<br>
     * e.g.: <i>lArticle.attribute LIKE :key</i><br>
     * e.g.: <i>lArticle.attribute = :key</i><br>
     * e.g.: <i>EXISTS (SELECT value FROM lArticle.attributeListValues value
     * LIKE :key)</i>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateArticleConditions(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        List<String> lConditions = lCriteria.getResultingConditions();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        String lSN = (String) lInputCriteria.get(SN);
        if (lSN != null) {
            String lParam = convertParamName(SN) + pIndex;
            String lCondition = "("
                    + getQueryCondition("lArticle.airbusSN", lParam, lSN);
            if (lSN.startsWith("!")) {
                lCondition += " AND ";
            }
            else {
                lCondition += " OR ";
            }
            lCondition +=
                    getQueryCondition("lArticle.manufacturerSN", lParam, lSN)
                    + ")";
            lConditions.add(lCondition);
            if (lSN.startsWith("!")) {
                lSN = lSN.substring(1);
            }
            lParameters.put(lParam, lSN);
        }
        
        String lAirbusSN = (String) lInputCriteria.get(AIRBUS_SN);
        if (lAirbusSN != null) {
            String lParam = convertParamName(AIRBUS_SN) + pIndex;
            String lCondition = getQueryCondition("lArticle.airbusSN",
                    lParam, lAirbusSN);
            lConditions.add(lCondition);
            if (lAirbusSN.startsWith("!")) {
                lAirbusSN = lAirbusSN.substring(1);
            }
            lParameters.put(lParam, lAirbusSN);
        }
        
        String lManSN = (String) lInputCriteria.get(MANUFACTURER_SN);
        if (lManSN != null) {
            String lParam = convertParamName(MANUFACTURER_SN) + pIndex;
            String lCondition = getQueryCondition("lArticle.manufacturerSN",
                    lParam, lManSN);
            lConditions.add(lCondition);
            if (lManSN.startsWith("!")) {
                lManSN = lManSN.substring(1);
            }
            lParameters.put(lParam, lManSN);
        }
        
        String lAirbusPN = (String) lInputCriteria.get(AIRBUS_PN);
        if (lAirbusPN != null) {
            // In order to keep NULL values while filtering, the computed field
            // of the view on articles is used instead of the attribute of the
            // contained entity
            addJoin("ArticleView", "INNER JOIN ArticleView lArticleView ON "
                    + "lArticle.id = lArticleView.id");
            String lParam = convertParamName(AIRBUS_PN) + pIndex;
            String lCondition = getQueryCondition("lArticleView.airbusPN",
                    lParam, lAirbusPN);
            lConditions.add(lCondition);
            if (lAirbusPN.startsWith("!")) {
                lAirbusPN = lAirbusPN.substring(1);
            }
            lParameters.put(lParam, lAirbusPN);
        }
        
        String lManPN = (String) lInputCriteria.get(MANUFACTURER_PN);
        if (lManPN != null) {
            // In order to keep NULL values while filtering, the computed field
            // of the view on articles is used instead of the attribute of the
            // contained entity
            addJoin("ArticleView", "LEFT JOIN ArticleView lArticleView ON "
                    + "lArticle.id = lArticleView.id");
            String lParam = convertParamName(MANUFACTURER_PN) + pIndex;
            String lCondition = getQueryCondition("lArticleView.manufacturerPN",
                    lParam, lManPN);
            lConditions.add(lCondition);
            if (lManPN.startsWith("!")) {
                lManPN = lManPN.substring(1);
            }
            lParameters.put(lParam, lManPN);
        }
        
        String lCMSCode = (String) lInputCriteria.get(CMS_CODE);
        if (lCMSCode != null) {
            String lParam = convertParamName(CMS_CODE) + pIndex;
            String lCondition = getQueryCondition("lArticle.cmsCode",
                    lParam, lCMSCode);
            lConditions.add(lCondition);
            if (lCMSCode.startsWith("!")) {
                lCMSCode = lCMSCode.substring(1);
            }
            lParameters.put(lParam, lCMSCode);
        }
        
        String lType = (String) lInputCriteria.get(TYPE);
        if (lType != null) {
            String lParam = convertParamName(TYPE) + pIndex;
            String lCondition = getQueryCondition("lArticle.typeArticle.label",
                    lParam, lType, "lArticle.typeArticle");
            lConditions.add(lCondition);
            if (lType.startsWith("!")) {
                lType = lType.substring(1);
            }
            lParameters.put(lParam, lType);
        }
        
        Object[] lStates = (Object[]) lInputCriteria.get(STATE);
        if (lStates != null) {
            if (lStates.length == 0) {
                lStates = new Object[] { null };
            }
            String lCondition = "lArticle.state IN :param";
            String lParam = convertParamName(STATE) + pIndex;
            lConditions.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lStates));
        }
        
        Object[] lUseStates = (Object[]) lInputCriteria.get(USE_STATE);
        if (lUseStates != null) {
            if (lUseStates.length == 0) {
                lUseStates = new Object[] { null };
            }
            String lCondition = "lArticle.useState IN :param";
            String lParam = convertParamName(USE_STATE) + pIndex;
            lConditions.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lUseStates));
        }
        
        String lHistoryComment = (String) lInputCriteria.get(HISTORY_COMMENT);
        if (lHistoryComment != null) {
            String lParam = convertParamName(HISTORY_COMMENT) + pIndex;
            StringBuffer lConditionBuffer = new StringBuffer("EXISTS (");
            lConditionBuffer.append("SELECT lAction FROM Action lAction");
            lConditionBuffer
                    .append(" WHERE lAction MEMBER OF lArticle.history.actions");
            lConditionBuffer.append(" AND ");
            lConditionBuffer.append(getQueryCondition("lAction.comment.message",
                    lParam, lHistoryComment, "lAction.comment"));
            lConditionBuffer.append(")");
            
            String lCondition =
                    lConditionBuffer.toString().replaceAll("lAction",
                            getUniqueIdentifier("act"));
            lConditions.add(lCondition);
            if (lHistoryComment.startsWith("!")) {
                lHistoryComment = lHistoryComment.substring(1);
            }
            lHistoryComment = "*" + lHistoryComment + "*";
            lParameters.put(lParam, lHistoryComment);
        }
        
        String lDatedComment = (String) lInputCriteria.get(DATED_COMMENT);
        if (lDatedComment != null) {
            String lParam = convertParamName(DATED_COMMENT) + pIndex;
            String lCondition = "(";
            
            lCondition += "EXISTS (";
            lCondition += "SELECT lComment FROM DatedComment lComment";
            lCondition += " WHERE lComment MEMBER OF lArticle.datedComments";
            lCondition += " AND ";
            lCondition += getQueryCondition("lComment.comment",
                    lParam, lDatedComment, "lComment.comment");
            lCondition += ")";
            
            if (lDatedComment.startsWith("!")) {
                lCondition += "OR NOT EXISTS (";
                lCondition += "SELECT lComment FROM DatedComment lComment";
                lCondition +=
                        " WHERE lComment MEMBER OF lArticle.datedComments)";
            }
            
            lCondition += ")";
            
            lCondition = lCondition.replaceAll("lComment", getUniqueIdentifier("cmt"));
            lConditions.add(lCondition);
            if (lDatedComment.startsWith("!")) {
                lDatedComment = lDatedComment.substring(1);
            }
            lDatedComment = "*" + lDatedComment + "*";
            lParameters.put(lParam, lDatedComment);
        }
        
        Object[] lHasDocuments = (Object[]) lInputCriteria.get(HAS_DOCUMENTS);
        if (lHasDocuments != null) {
            if (lHasDocuments.length == 0) {
                lConditions.add("lArticle.id IS NULL");
            }
            else if (lHasDocuments.length == 1) {
                String lCondition = "(";
                if (!(Boolean) lHasDocuments[0]) {
                    lCondition += "NOT ";
                }
                lCondition += "EXISTS (";
                lCondition += "SELECT lDocument FROM Document lDocument";
                lCondition += " WHERE lDocument MEMBER OF lArticle.documents";
                lCondition += ")";
                lCondition += ")";
                
                lConditions.add(lCondition);
            }
        }
        
        Object lAcqDateMin = lInputCriteria.get(ACQUISITION_DATE_MIN);
        Object lAcqDateMax = lInputCriteria.get(ACQUISITION_DATE_MAX);
        if (lAcqDateMin != null) {
            if (lAcqDateMax != null) {
                // both dates are defined, add BETWEEN condition
                String lCondition =
                        "lArticle.acquisitionDate BETWEEN :paramMin AND :paramMax";
                String lParamMin =
                        convertParamName(ACQUISITION_DATE_MIN) + pIndex;
                String lParamMax =
                        convertParamName(ACQUISITION_DATE_MAX) + pIndex;
                lConditions.add(lCondition.replaceAll("paramMin", lParamMin)
                        .replaceAll("paramMax", lParamMax));
                lParameters.put(lParamMin, lAcqDateMin);
                lParameters.put(lParamMax, lAcqDateMax);
            }
            else {
                // only min date is defined, add >= condition
                String lCondition = "lArticle.acquisitionDate >= :param";
                String lParam = convertParamName(ACQUISITION_DATE_MIN) + pIndex;
                lConditions.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, lAcqDateMin);
            }
        }
        else {
            if (lAcqDateMax != null) {
                // only max date is defined, add <= condition
                String lCondition = "lArticle.acquisitionDate <= :param";
                String lParam = convertParamName(ACQUISITION_DATE_MAX) + pIndex;
                lConditions.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, lAcqDateMax);
            }
        }
    }
    
    /**
     * Build a condition to put in a WHERE clause of a query retrieving Articles
     * satisfying the comment criterion provided in the parameters map.<br>
     * e.g.: <i>(lArticle.id IN (SELECT aId.id FROM Article aId WHERE ...)<br>
     * OR lArticle.id IN (SELECT pcId.id FROM PC pcId WHERE ...))</i><br>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateCommentCondition(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        String lComment = (String) lInputCriteria.get(COMMENT);
        if (lComment != null) {
            
            List<String> lSubQueries = new ArrayList<String>();
            
            String lParam = convertParamName(COMMENT) + pIndex;
            
            // Add the query for Articles comment
            String lSubQueryArticle =
                    "SELECT lArticleSQ.id FROM Article lArticleSQ WHERE ";
            lSubQueryArticle += getQueryCondition(
                    "lArticleSQ.history.generalComment.message",
                    lParam, lComment, "lArticleSQ.history.generalComment");
            lSubQueryArticle =
                    lSubQueryArticle.replaceAll("lArticleSQ",
                            getUniqueIdentifier("art"));
            
            lSubQueries.add(lSubQueryArticle);
            
            // Add the query for PC comment
            String lSubQueryPC = "SELECT lPC.id FROM PC lPC WHERE ";
            lSubQueryPC += getQueryCondition("lPC.comment", lParam, lComment);
            
            lSubQueryPC =
                    lSubQueryPC.replaceAll("lPC", getUniqueIdentifier("pc"));
            
            lSubQueries.add(lSubQueryPC);
            
            // Add '*' to surround the searched text
            if (lComment.startsWith("!")) {
                lComment = lComment.substring(1);
            }
            lComment = "*" + lComment + "*";
            lParameters.put(lParam, lComment);
            
            lCriteria.getResultingConditions().add(
                    createConditionFromSubQueries(lSubQueries));
        }
    }
    
    /**
     * Build conditions to put in a WHERE clause of a query retrieving Articles
     * satisfying the container criteria provided in the parameters map.<br>
     * e.g.: <i>lArticle.id IN (SELECT aId.id FROM Article aId WHERE ...)</i><br>
     * The CONTAINER_TYPE criteria is removed from the parameters map.
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateContainerConditions(int pIndex) {
        
        List<String> lQueries = new ArrayList<String>();
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        Object[] lContainerTypes =
                (Object[]) lInputCriteria.get(CONTAINER_TYPE);
        String lContainerName = (String) lInputCriteria.get(CONTAINER_NAME);
        
        // if types are not specified but the name is filtered
        // filter on all possible container types
        if (lContainerTypes == null
                && !StringUtil.isEmptyOrNull(lContainerName)) {
            // Equivalent to looking for not contained articles
            if (lContainerName.equals("!*")) {
                lContainerTypes = new ContainerType[1];
                lContainerTypes[0] = ContainerType.NotContained;
            }
            else {
                lContainerTypes = ContainerType.valuesContained();
            }
        }
        
        if (lContainerTypes != null) {
            if (lContainerTypes.length == 0) {
                lQueries.add("NULL");
            }
            if (StringUtil.isEmptyOrNull(lContainerName)) {
                lContainerName = "*";
            }
            for (Object lObject : lContainerTypes) {
                ContainerType lContainerType = (ContainerType) lObject;
                if (lContainerType.equals(ContainerType.NotContained)) {
                    lQueries.add(buildContainerNotContainedSubQuery());
                }
                if (lContainerType.equals(ContainerType.Board)) {
                    lQueries.add(buildContainerMotherboardSubQuery(
                            lContainerName, pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.Installation)) {
                    
                    lQueries.addAll(buildContainerInstallationSubQueries(
                            lContainerName, pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.Rack)) {
                    
                    lQueries.addAll(buildContainerRackSubQueries(lContainerName,
                            pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.Switch)) {
                    
                    lQueries.add(buildContainerSwitchSubQuery(lContainerName,
                            pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.Cabinet)) {
                    
                    lQueries.addAll(buildContainerCabinetSubQueries(
                            lContainerName, pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.PC)) {
                    
                    lQueries.addAll(buildContainerPCSubQueries(lContainerName,
                            pIndex, lParameters));
                }
                if (lContainerType.equals(ContainerType.Tool)) {
                    
                    lQueries.add(buildContainerToolSubQuery(lContainerName,
                            pIndex, lParameters));
                }
            }
            
            lCriteria.getResultingConditions()
                    .add(createConditionFromSubQueries(lQueries));
        }
    }
    
    /**
     * Build conditions to put in a WHERE clause of a query retrieving Articles
     * satisfying the designation criterion provided in the parameters map.<br>
     * e.g.: <i>(lArticle.id IN (SELECT rId.id FROM Rack rId WHERE ...)<br>
     * OR lArticle.id IN (SELECT cId.id FROM Cabinet cId WHERE ...))</i>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateDesignationConditions(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        String lDesignation = (String) lInputCriteria.get(DESIGNATION);
        if (lDesignation != null) {
            
            List<String> lSubQueries = new ArrayList<String>();
            
            String lParam = convertParamName(DESIGNATION) + pIndex;
            
            // Add the query for Racks designation
            String lParamRack = getUniqueIdentifier("rack");
            String lSubQueryRack = "SELECT " + lParamRack + ".id FROM Rack "
                    + lParamRack + " WHERE ";
            lSubQueryRack += getQueryCondition(lParamRack + ".designation",
                    lParam, lDesignation);
            lSubQueries.add(lSubQueryRack);
            
            // Add the query for Cabinets designation
            String lParamCab = getUniqueIdentifier("cab");
            String lSubQueryCab = "SELECT " + lParamCab + ".id FROM Cabinet "
                    + lParamCab + " WHERE ";
            lSubQueryCab += getQueryCondition(lParamCab + ".designation",
                    lParam, lDesignation);
            lSubQueries.add(lSubQueryCab);
            
            lCriteria.getResultingConditions().add(
                    createConditionFromSubQueries(lSubQueries));
            if (lDesignation.startsWith("!")) {
                lDesignation = lDesignation.substring(1);
            }
            lParameters.put(lParam, lDesignation);
        }
    }
    
    /**
     * Build conditions to put in a WHERE clause of a query retrieving Articles
     * satisfying the external location criteria provided in the parameters map.<br>
     * e.g.: <i>lArticle.id IN (SELECT aId.id FROM Article aId WHERE ...)</i><br>
     * The EXTERNAL_LOCATION_TYPE criteria is removed from the parameters map,
     * if it is no more used.
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateExtLocationConditions(int pIndex) {
        List<String> lQueries = new ArrayList<String>();
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        Object[] lExternalLocationTypes =
                (Object[]) lInputCriteria.get(EXTERNAL_LOCATION_TYPE);
        String lExternalLocationName =
                (String) lInputCriteria.get(EXTERNAL_LOCATION_NAME);
        
        // if types are not specified but the name is filtered
        // add filter on all possible external location types
        if (lExternalLocationTypes == null
                && !StringUtil.isEmptyOrNull(lExternalLocationName)) {
            // Equivalent to looking for not located articles
            if (lExternalLocationName.equals("!*")) {
                lExternalLocationTypes = new Long[1];
                lExternalLocationTypes[0] = 0l;
            }
            else {
                lQueries.add(buildExtLocationExternalEntitySubQuery(null,
                        lExternalLocationName, pIndex, lParameters));
                lCriteria.getResultingConditions()
                        .add(createConditionFromSubQueries(lQueries));
            }
        }
        
        if (lExternalLocationTypes != null) {
            if (lExternalLocationTypes.length == 0) {
                lQueries.add("NULL");
            }
            for (Object lObject : lExternalLocationTypes) {
                Long lExternalEntityTypeId = (Long) lObject;
                // The "not sent" case
                if (lExternalEntityTypeId == 0) {
                    lQueries.add(buildExtLocationNotSentSubQuery());
                }
                else {
                    lQueries.add(buildExtLocationExternalEntitySubQuery(
                            lExternalEntityTypeId, lExternalLocationName,
                            pIndex, lParameters));
                }
            }
            
            lCriteria.getResultingConditions()
                    .add(createConditionFromSubQueries(lQueries));
        }
    }
    
    /**
     * Build conditions to put in a WHERE clause of a query retrieving Articles
     * satisfying the location criteria provided in the parameters map.<br>
     * e.g.: <i>lArticle.id IN (SELECT aId.id FROM Article aId WHERE ...)</i><br>
     * The LOCATION_TYPE criteria is removed from the parameters map.
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateLocationConditions(int pIndex) {
        List<String> lQueries = new ArrayList<String>();
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        Object[] lLocationTypes = (Object[]) lInputCriteria.get(LOCATION_TYPE);
        String lLocationName = (String) lInputCriteria.get(LOCATION_NAME);
        
        // if types are not specified but the name is filtered
        // filter on all located articles
        if (lLocationTypes == null
                && !StringUtil.isEmptyOrNull(lLocationName)) {
            // Equivalent to looking for not located articles
            if (lLocationName.equals("!*")) {
                lLocationTypes = new Boolean[] { false };
            }
            else {
                lLocationTypes = new Boolean[] { true };
            }
        }
        
        if (lLocationTypes != null) {
            if (lLocationTypes.length == 0) {
                lQueries.add("NULL");
            }
            for (Object lObject : lLocationTypes) {
                Boolean lLocationType = (Boolean) lObject;
                // The "not sent" case
                if (lLocationType) {
                    lQueries.add(buildLocationPlaceSubQuery(lLocationName,
                            pIndex, lParameters));
                }
                else {
                    lQueries.add(buildLocationNotLocatedSubQuery());
                }
            }
            lCriteria.getResultingConditions()
                    .add(createConditionFromSubQueries(lQueries));
        }
    }
    
    /**
     * Build a condition to put in a WHERE clause of a query retrieving Articles
     * of PC class satisfying the PC specific criteria provided in the
     * parameters map.<br>
     * e.g.: <i>lArticle.id IN (SELECT pc.id FROM PC pc WHERE ...)</i>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updatePCCondition(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        List<String> lConditionsPC = new ArrayList<String>();
        
        String lPCId = getUniqueIdentifier("pc");
        ArticleQueryBuilder lQueryBuilderPC =
                new ArticleQueryBuilder("PC", lPCId);
        
        Object[] lProductTypes = (Object[]) lInputCriteria.get(PRODUCT_TYPE);
        if (lProductTypes != null) {
            if (lProductTypes.length == 0) {
                lProductTypes = new Object[] { null };
            }
            String lCondition = "lPC.productType.id IN :param";
            String lParam = convertParamName(PRODUCT_TYPE) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lProductTypes));
        }
        
        Object[] lDepartmentsInCharge =
                (Object[]) lInputCriteria.get(DEPARTMENT_IN_CHARGE);
        if (lDepartmentsInCharge != null) {
            if (lDepartmentsInCharge.length == 0) {
                lConditionsPC.add("lPC.id IS NULL");
            }
            else {
                lQueryBuilderPC.addJoin("DepartmentInCharge", "LEFT JOIN "
                        + lPCId + ".departmentInCharge lDepartmentInCharge");
                String lCondition = "";
                if (Arrays.asList(lDepartmentsInCharge).contains(0L)) {
                    lCondition += "lDepartmentInCharge IS NULL OR ";
                }
                lCondition += "lDepartmentInCharge.id IN :param";
                String lParam = convertParamName(DEPARTMENT_IN_CHARGE) + pIndex;
                lConditionsPC.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, Arrays.asList(lDepartmentsInCharge));
            }
        }
        
        String lName = (String) lInputCriteria.get(NAME);
        if (lName != null) {
            String lParam = convertParamName(NAME) + pIndex;
            String lCondition = getQueryCondition("lPC.name",
                    lParam, lName);
            lConditionsPC.add(lCondition);
            if (lName.startsWith("!")) {
                lName = lName.substring(1);
            }
            lParameters.put(lParam, lName);
        }
        
        String lFunction = (String) lInputCriteria.get(FUNCTION);
        if (lFunction != null) {
            String lParam = convertParamName(FUNCTION) + pIndex;
            String lCondition = getQueryCondition("lPC.function",
                    lParam, lFunction);
            lConditionsPC.add(lCondition);
            if (lFunction.startsWith("!")) {
                lFunction = lFunction.substring(1);
            }
            lParameters.put(lParam, lFunction);
        }
        
        String lInChargeUserDetails =
                (String) lInputCriteria.get(IN_CHARGE_LOGIN_DETAILS);
        if (lInChargeUserDetails != null) {
            String lParam = convertParamName(IN_CHARGE_LOGIN_DETAILS) + pIndex;
            String lAttribute =
                    "CONCAT(lPC.inCharge.lastname,' ',lPC.inCharge.firstname,"
                            + "' (',lPC.inCharge.login,')')";
            String lCondition =
                    getQueryCondition(lAttribute, lParam, lInChargeUserDetails);
            lConditionsPC.add(lCondition);
            if (lInChargeUserDetails.startsWith("!")) {
                lInChargeUserDetails = lInChargeUserDetails.substring(1);
            }
            lParameters.put(lParam, lInChargeUserDetails);
        }
        
        Object lInChargeUser = lInputCriteria.get(IN_CHARGE_USER);
        if (lInChargeUser != null) {
            String lCondition = "lPC.inCharge = :param";
            String lParam = convertParamName(IN_CHARGE_USER) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, lInChargeUser);
        }
        
        String lOwner = (String) lInputCriteria.get(OWNER);
        if (lOwner != null) {
            String lParam = convertParamName(OWNER) + pIndex;
            String lCondition = getQueryCondition("lPC.owner", lParam, lOwner);
            lConditionsPC.add(lCondition);
            if (lOwner.startsWith("!")) {
                lOwner = lOwner.substring(1);
            }
            lParameters.put(lParam, lOwner);
        }
        
        String lOwnerSiglum = (String) lInputCriteria.get(OWNER_SIGLUM);
        if (lOwnerSiglum != null) {
            String lParam = convertParamName(OWNER_SIGLUM) + pIndex;
            String lCondition =
                    getQueryCondition("lPC.ownerSiglum", lParam, lOwnerSiglum);
            lConditionsPC.add(lCondition);
            if (lOwnerSiglum.startsWith("!")) {
                lOwnerSiglum = lOwnerSiglum.substring(1);
            }
            lParameters.put(lParam, lOwnerSiglum);
        }
        
        String lAdmin = (String) lInputCriteria.get(ADMIN);
        if (lAdmin != null) {
            String lParam = convertParamName(ADMIN) + pIndex;
            String lCondition = getQueryCondition("lPC.admin", lParam, lAdmin);
            lConditionsPC.add(lCondition);
            if (lAdmin.startsWith("!")) {
                lAdmin = lAdmin.substring(1);
            }
            lParameters.put(lParam, lAdmin);
        }
        
        Object[] lAllocations = (Object[]) lInputCriteria.get(ALLOCATION);
        if (lAllocations != null) {
            if (lAllocations.length == 0) {
                lAllocations = new Object[] { null };
            }
            String lCondition = "lPC.allocation.id IN :param";
            String lParam = convertParamName(ALLOCATION) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lAllocations));
        }
        
        String lAssignment = (String) lInputCriteria.get(ASSIGNMENT);
        if (lAssignment != null) {
            String lParam = convertParamName(ASSIGNMENT) + pIndex;
            String lCondition =
                    getQueryCondition("lPC.assignment", lParam, lAssignment);
            lConditionsPC.add(lCondition);
            if (lAssignment.startsWith("!")) {
                lAssignment = lAssignment.substring(1);
            }
            lParameters.put(lParam, lAssignment);
        }
        
        Object[] lUsages = (Object[]) lInputCriteria.get(USAGE);
        if (lUsages != null) {
            if (lUsages.length == 0) {
                lUsages = new Object[] { null };
            }
            String lCondition = "lPC.usage.id IN :param";
            String lParam = convertParamName(USAGE) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lUsages));
        }
        
        Object[] lDomains = (Object[]) lInputCriteria.get(DOMAIN);
        if (lDomains != null) {
            if (lDomains.length == 0) {
                lConditionsPC.add("lPC.id IS NULL");
            }
            else {
                lQueryBuilderPC.addJoin("Domain",
                        "LEFT JOIN " + lPCId + ".domain lDomain");
                String lCondition = "";
                if (Arrays.asList(lDomains).contains(0L)) {
                    lCondition += "lDomain IS NULL OR ";
                }
                lCondition += "lDomain.id IN :param";
                String lParam = convertParamName(DOMAIN) + pIndex;
                lConditionsPC.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, Arrays.asList(lDomains));
            }
        }
        
        String lPlatform = (String) lInputCriteria.get(PLATFORM);
        if (lPlatform != null) {
            String lParam = convertParamName(PLATFORM) + pIndex;
            String lCondition =
                    getQueryCondition("lPC.platform", lParam, lPlatform);
            lConditionsPC.add(lCondition);
            if (lPlatform.startsWith("!")) {
                lPlatform = lPlatform.substring(1);
            }
            lParameters.put(lParam, lPlatform);
        }
        
        Object lSpecificity = lInputCriteria.get(PC_SPECIFICITY);
        if (lSpecificity != null) {
            StringBuffer lConditionBuffer = new StringBuffer("EXISTS (");
            lConditionBuffer.append(
                    "SELECT lSpecificity FROM PCSpecificity lSpecificity");
            lConditionBuffer.append(
                    " WHERE lSpecificity MEMBER OF lPC.specificities");
            lConditionBuffer
                    .append(" AND lSpecificity.description LIKE :param)");
            
            String lCondition = lConditionBuffer.toString()
                    .replaceAll("lSpecificity", getUniqueIdentifier("spec"));
            String lParam = convertParamName(PC_SPECIFICITY) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            // Add '*' to surround the searched text
            String lSpecificityCommentStr = "*" + ((String) lSpecificity) + "*";
            lParameters.put(lParam, lSpecificityCommentStr);
        }
        
        Object lNbScreensMinObj = lInputCriteria.get(NB_SCREENS_MIN);
        Integer lNbScreensMin = null;
        if (lNbScreensMinObj instanceof String) {
            try {
                lNbScreensMin = Integer.valueOf((String) lNbScreensMinObj);
                if (lNbScreensMin < 0) {
                    lNbScreensMin = null;
                }
            }
            catch (NumberFormatException e) {
            }
        }
        else if (lNbScreensMinObj instanceof Integer) {
            lNbScreensMin = (Integer) lNbScreensMinObj;
        }
        Object lNbScreensMaxObj = lInputCriteria.get(NB_SCREENS_MAX);
        Integer lNbScreensMax = null;
        if (lNbScreensMaxObj instanceof String) {
            try {
                lNbScreensMax = Integer.valueOf((String) lNbScreensMaxObj);
                if (lNbScreensMax < 0) {
                    lNbScreensMax = null;
                }
            }
            catch (NumberFormatException e) {
            }
        }
        else if (lNbScreensMaxObj instanceof Integer) {
            lNbScreensMax = (Integer) lNbScreensMaxObj;
        }
        if (lNbScreensMin != null) {
            if (lNbScreensMax != null) {
                String lCondition =
                        "lPC.nbScreens BETWEEN :paramMin AND :paramMax";
                String lParamMin = convertParamName(NB_SCREENS_MIN) + pIndex;
                String lParamMax = convertParamName(NB_SCREENS_MAX) + pIndex;
                lConditionsPC.add(lCondition.replaceAll("paramMin", lParamMin)
                        .replaceAll("paramMax", lParamMax));
                lParameters.put(lParamMin, lNbScreensMin);
                lParameters.put(lParamMax, lNbScreensMax);
            }
            else {
                String lCondition = "lPC.nbScreens >= :param";
                String lParam = convertParamName(NB_SCREENS_MIN) + pIndex;
                lConditionsPC.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, lNbScreensMin);
            }
        }
        else if (lNbScreensMax != null) {
            String lCondition = "lPC.nbScreens <= :param";
            String lParam = convertParamName(NB_SCREENS_MAX) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, lNbScreensMax);
        }
        
        Object lNbScreensObj = lInputCriteria.get(NB_SCREENS);
        Integer lNbScreens = null;
        if (lNbScreensObj instanceof String) {
            try {
                lNbScreens = Integer.valueOf((String) lNbScreensObj);
                if (lNbScreens < 0) {
                    lNbScreens = null;
                }
            }
            catch (NumberFormatException e) {
            }
        }
        else if (lNbScreensObj instanceof Integer) {
            lNbScreens = (Integer) lNbScreensObj;
        }
        if (lNbScreens != null) {
            String lCondition = "lPC.nbScreens = :param";
            String lParam = convertParamName(NB_SCREENS) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, lNbScreens);
        }
        
        String lOS = (String) lInputCriteria.get(OPERATING_SYSTEMS);
        if (lOS != null) {
            StringBuffer lConditionBuffer = new StringBuffer();
            lConditionBuffer.append("EXISTS (");
            lConditionBuffer.append("SELECT lSoftware FROM Software lSoftware");
            lConditionBuffer.append(" WHERE lSoftware MEMBER OF lPC.softwares");
            lConditionBuffer.append(" AND lSoftware.operatingSystem = true");
            // complete name part
            lConditionBuffer.append(" AND TRIM(TRAILING '")
                    .append(Software.ATTRIBUTES_SEPARATOR)
                    .append("' FROM CONCAT(lSoftware.name,'")
                    .append(Software.ATTRIBUTES_SEPARATOR)
                    .append("',lSoftware.distribution,'")
                    .append(Software.ATTRIBUTES_SEPARATOR)
                    .append("',lSoftware.kernel)) LIKE :param)");
            
            String lCondition =
                    lConditionBuffer.toString().replaceAll("lSoftware",
                            getUniqueIdentifier("soft"));
            String lParam = convertParamName(OPERATING_SYSTEMS) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            if (lOS.startsWith("!")) {
                lOS = lOS.substring(1);
            }
            lParameters.put(lParam, lOS);
        }
        
        Object[] lHasDefaultOS = (Object[]) lInputCriteria.get(HAS_DEFAULT_OS);
        if (lHasDefaultOS != null) {
            if (lHasDefaultOS.length == 0) {
                lConditionsPC.add("lPC.id IS NULL");
            }
            else if (lHasDefaultOS.length == 1) {
                String lCondition = "lPC.defaultOS IS ";
                if ((Boolean) lHasDefaultOS[0]) {
                    lCondition += "NOT ";
                }
                lCondition += "NULL";
                lConditionsPC.add(lCondition);
            }
        }
        
        String lDefaultOSName = (String) lInputCriteria.get(DEFAULT_OS_NAME);
        if (lDefaultOSName != null) {
            lQueryBuilderPC.addJoin("DefaultOS",
                    "LEFT JOIN " + lPCId + ".defaultOS lDefaultOS");
            String lParam = convertParamName(DEFAULT_OS_NAME) + pIndex;
            String lCondition = getQueryCondition("lDefaultOS.name", lParam,
                    lDefaultOSName);
            if (lDefaultOSName.startsWith("!")) {
                lDefaultOSName = lDefaultOSName.substring(1);
            }
            lConditionsPC.add(lCondition);
            lParameters.put(lParam, lDefaultOSName);
        }
        
        String lDefaultOSDistribution =
                (String) lInputCriteria.get(DEFAULT_OS_DISTRIBUTION);
        if (lDefaultOSDistribution != null) {
            lQueryBuilderPC.addJoin("DefaultOS",
                    "LEFT JOIN " + lPCId + ".defaultOS lDefaultOS");
            String lParam = convertParamName(DEFAULT_OS_DISTRIBUTION) + pIndex;
            String lCondition = getQueryCondition("lDefaultOS.distribution",
                    lParam, lDefaultOSDistribution);
            if (lDefaultOSDistribution.startsWith("!")) {
                lDefaultOSDistribution = lDefaultOSDistribution.substring(1);
            }
            lConditionsPC.add(lCondition);
            lParameters.put(lParam, lDefaultOSDistribution);
        }
        
        String lDefaultOSKernel =
                (String) lInputCriteria.get(DEFAULT_OS_KERNEL);
        if (lDefaultOSKernel != null) {
            lQueryBuilderPC.addJoin("DefaultOS",
                    "LEFT JOIN " + lPCId + ".defaultOS lDefaultOS");
            String lParam = convertParamName(DEFAULT_OS_KERNEL) + pIndex;
            String lCondition = getQueryCondition("lDefaultOS.kernel",
                    lParam, lDefaultOSKernel);
            if (lDefaultOSKernel.startsWith("!")) {
                lDefaultOSKernel = lDefaultOSKernel.substring(1);
            }
            lConditionsPC.add(lCondition);
            lParameters.put(lParam, lDefaultOSKernel);
        }
        
        String lDefaultOSCompleteName =
                (String) lInputCriteria.get(DEFAULT_OS_COMPLETE_NAME);
        if (lDefaultOSCompleteName != null) {
            lQueryBuilderPC.addJoin("DefaultOS",
                    "LEFT JOIN " + lPCId + ".defaultOS lDefaultOS");
            String lParam = convertParamName(DEFAULT_OS_COMPLETE_NAME) + pIndex;
            String lAttribute =
                    "CONCAT(lDefaultOS.name,' ',lDefaultOS.distribution,"
                            + "' ',lDefaultOS.kernel)";
            String lCondition = getQueryCondition(lAttribute, lParam,
                    lDefaultOSCompleteName);
            if (lDefaultOSCompleteName.startsWith("!")) {
                lDefaultOSCompleteName = lDefaultOSCompleteName.substring(1);
            }
            lConditionsPC.add(lCondition);
            lParameters.put(lParam, lDefaultOSCompleteName);
        }
        
        Object[] lAvailabilityStatuses =
                (Object[]) lInputCriteria.get(AVAILABILITY_STATUS);
        if (lAvailabilityStatuses != null) {
            if (lAvailabilityStatuses.length == 0) {
                lAvailabilityStatuses = new Object[] { null };
            }
            String lCondition = "lPC.availabilityStatus IN :param";
            String lParam = convertParamName(AVAILABILITY_STATUS) + pIndex;
            lConditionsPC.add(lCondition.replaceAll("param", lParam));
            lParameters.put(lParam, Arrays.asList(lAvailabilityStatuses));
        }
        
        Object[] lHasComPorts = (Object[]) lInputCriteria.get(HAS_COM_PORTS);
        if (lHasComPorts != null) {
            if (lHasComPorts.length == 0) {
                lConditionsPC.add("lPC.id IS NULL");
            }
            else if (lHasComPorts.length == 1) {
                String lCondition = "lPC.ports IS ";
                if ((Boolean) lHasComPorts[0]) {
                    lCondition += "NOT ";
                }
                lCondition += "EMPTY";
                lConditionsPC.add(lCondition);
            }
        }
        
        Object[] lHasSoftwares = (Object[]) lInputCriteria.get(HAS_SOFTWARES);
        if (lHasSoftwares != null) {
            if (lHasSoftwares.length == 0) {
                lConditionsPC.add("lPC.id IS NULL");
            }
            else if (lHasSoftwares.length == 1) {
                String lCondition = "lPC.softwares IS ";
                if ((Boolean) lHasSoftwares[0]) {
                    lCondition += "NOT ";
                }
                lCondition += "EMPTY";
                lConditionsPC.add(lCondition);
            }
        }
        
        List<String> lConditionsCopy = new ArrayList<String>(lConditionsPC);
        lConditionsPC.clear();
        for (String lCondition : lConditionsCopy) {
            lConditionsPC.add(lCondition.replaceAll("lPC", lPCId));
        }
        
        // Retrieve the sub-query and generate the condition
        if (!lConditionsPC.isEmpty()) {
            lQueryBuilderPC.setField("id");
            
            boolean lCombinationAnd =
                    criteriaList.get(pIndex).isCombinationAnd();
            
            Condition lConditionPC =
                    new Condition(lConditionsPC, lCombinationAnd);
            
            lQueryBuilderPC.setCondition(lConditionPC);
            
            lCriteria.getResultingConditions()
                    .add(createConditionFromSubQuery(lQueryBuilderPC
                            .getQueryList()));
        }
    }
    
    /**
     * Build a condition to put in a WHERE clause of a query retrieving Articles
     * of PC class having communication ports satisfying the criteria provided
     * in the parameters map.<br>
     * e.g.: <i>lArticle.id IN (SELECT pcId.id FROM PC pcId WHERE EXISTS
     * (SELECT portId FROM pcId.ports portId WHERE...))</i><br>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updatePortCondition(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        List<String> lConditionsPorts = new ArrayList<String>();
        
        String lPCId = getUniqueIdentifier("pc");
        
        ArticleQueryBuilder lQueryBuilderPC =
                new ArticleQueryBuilder("PC", lPCId);
        
        String lPortId = getUniqueIdentifier("port");
        
        String lName = (String) lInputCriteria.get(PORT_NAME);
        if (lName != null) {
            String lParam = convertParamName(PORT_NAME) + pIndex;
            String lCondition = getQueryCondition("lPort.name", lParam, lName);
            if (lName.startsWith("!")) {
                lName = lName.substring(1);
            }
            lConditionsPorts.add(lCondition);
            lParameters.put(lParam, lName);
        }
        
        String lIPAddress = (String) lInputCriteria.get(IP_ADDRESS);
        if (lIPAddress != null) {
            String lParam = convertParamName(IP_ADDRESS) + pIndex;
            String lCondition =
                    getQueryCondition("lPort.ipAddress", lParam, lIPAddress);
            if (lIPAddress.startsWith("!")) {
                lIPAddress = lIPAddress.substring(1);
            }
            lConditionsPorts.add(lCondition);
            lParameters.put(lParam, lIPAddress);
        }
        
        String lMACAddress = (String) lInputCriteria.get(MAC_ADDRESS);
        if (lMACAddress != null) {
            String lParam = convertParamName(MAC_ADDRESS) + pIndex;
            String lCondition =
                    getQueryCondition("lPort.macAddress", lParam, lMACAddress);
            if (lMACAddress.startsWith("!")) {
                lMACAddress = lMACAddress.substring(1);
            }
            lConditionsPorts.add(lCondition);
            lParameters.put(lParam, lMACAddress);
        }
        
        Object[] lNetworks = (Object[]) lInputCriteria.get(NETWORK);
        if (lNetworks != null) {
            if (lNetworks.length == 0) {
                lConditionsPorts.add("lPort.id IS NULL");
            }
            else {
                String lCondition = "";
                if (Arrays.asList(lNetworks).contains(0L)) {
                    lCondition += "lNetwork IS NULL OR ";
                }
                lCondition += "lNetwork.id IN :param";
                String lParam = convertParamName(NETWORK) + pIndex;
                lConditionsPorts.add(lCondition.replaceAll("param", lParam));
                lParameters.put(lParam, Arrays.asList(lNetworks));
            }
        }
        
        String lSocket = (String) lInputCriteria.get(SOCKET);
        if (lSocket != null) {
            String lParam = convertParamName(SOCKET) + pIndex;
            String lCondition =
                    getQueryCondition("lPort.socket", lParam, lSocket);
            if (lSocket.startsWith("!")) {
                lSocket = lSocket.substring(1);
            }
            lConditionsPorts.add(lCondition);
            lParameters.put(lParam, lSocket);
        }
        
        String lComment = (String) lInputCriteria.get(COMMENT_PORT);
        if (lComment != null) {
            String lParam = convertParamName(COMMENT_PORT) + pIndex;
            String lCondition =
                    getQueryCondition("lPort.comment", lParam, lComment);
            if (lComment.startsWith("!")) {
                lComment = lComment.substring(1);
            }
            lComment = "*" + lComment + "*";
            lConditionsPorts.add(lCondition);
            lParameters.put(lParam, lComment);
        }
        
        List<String> lConditionsCopy = new ArrayList<String>(lConditionsPorts);
        lConditionsPorts.clear();
        for (String lCondition : lConditionsCopy) {
            lConditionsPorts.add(lCondition.replaceAll("lPort", lPortId));
        }
        
        // Retrieve the sub-query and generate the condition
        if (!lConditionsPorts.isEmpty()) {
            lQueryBuilderPC.setField("id");
            
            boolean lCombinationAnd = lCriteria.isCombinationAnd();
            
            Condition lConditionPort =
                    new Condition(lConditionsPorts, lCombinationAnd);
            
            StringBuffer lConditionPCBuffer =
                    new StringBuffer(
                            "EXISTS (SELECT lPort FROM lPC.ports lPort");
            lConditionPCBuffer.append(" LEFT JOIN lPort.network lNetwork");
            lConditionPCBuffer.append(" WHERE ");
            lConditionPCBuffer.append(lConditionPort.combineConditions());
            lConditionPCBuffer.append(")");
            
            String lConditionPCString =
                    lConditionPCBuffer.toString().replaceAll("lPort", lPortId);
            
            lConditionPCString = lConditionPCString.replaceAll("lPC", lPCId);
            
            Condition lConditionPC = new Condition(lConditionPCString);
            
            lQueryBuilderPC.setCondition(lConditionPC);
            
            lCriteria.getResultingConditions()
                    .add(createConditionFromSubQuery(lQueryBuilderPC
                            .getQueryList()));
        }
    }
    
    /**
     * Build a condition for query in string form for Articles having an
     * installed software satisfying the name, distribution and kernel criteria
     * provided in the parameters map.<br>
     * e.g.: <i>EXISTS (SELECT software FROM lArticle.softwares software WHERE
     * ...)</i>
     * 
     * @param pIndex
     *            the index of the criteria to treat into the list
     */
    private void updateSoftwareCondition(int pIndex) {
        
        CriteriaStructure lCriteria = criteriaList.get(pIndex);
        
        Map<String, Object> lInputCriteria = lCriteria.getInputCriteria();
        Map<String, Object> lParameters = lCriteria.getResultingParameters();
        
        StringBuffer lQuerySb = new StringBuffer("EXISTS (");
        lQuerySb.append("SELECT lSoftware FROM Software lSoftware");
        lQuerySb.append(" WHERE lSoftware MEMBER OF lArticle.softwares");
        lQuerySb.append(" AND ");
        
        List<String> lConditions = new ArrayList<String>();
        
        String lName = (String) lInputCriteria.get(SOFTWARE_NAME);
        if (lName != null) {
            String lParam = convertParamName(SOFTWARE_NAME) + pIndex;
            String lCondition =
                    getQueryCondition("lSoftware.name", lParam, lName);
            if (lName.startsWith("!")) {
                lName = lName.substring(1);
            }
            lConditions.add(lCondition);
            lParameters.put(lParam, lName);
        }
        
        String lDistribution =
                (String) lInputCriteria.get(SOFTWARE_DISTRIBUTION);
        if (lDistribution != null) {
            String lParam = convertParamName(SOFTWARE_DISTRIBUTION) + pIndex;
            String lCondition = getQueryCondition("lSoftware.distribution",
                    lParam, lDistribution);
            if (lDistribution.startsWith("!")) {
                lDistribution = lDistribution.substring(1);
            }
            lConditions.add(lCondition);
            lParameters.put(lParam, lDistribution);
        }
        
        String lKernel = (String) lInputCriteria.get(SOFTWARE_KERNEL);
        if (lKernel != null) {
            String lParam = convertParamName(SOFTWARE_KERNEL) + pIndex;
            String lCondition =
                    getQueryCondition("lSoftware.kernel", lParam, lKernel);
            if (lKernel.startsWith("!")) {
                lKernel = lKernel.substring(1);
            }
            lConditions.add(lCondition);
            lParameters.put(lParam, lKernel);
        }
        
        String lSoftwareId = getUniqueIdentifier("soft");
        
        if (!lConditions.isEmpty()) {
            
            boolean lCombinationAnd =
                    criteriaList.get(pIndex).isCombinationAnd();
            
            Condition lCondition = new Condition(lConditions, lCombinationAnd);
            
            lQuerySb.append(lCondition.combineConditions()).append(")");
            
            criteriaList
                    .get(pIndex)
                    .getResultingConditions()
                    .add(lQuerySb.toString().replaceAll("lSoftware",
                            lSoftwareId));
        }
    }
    
    private String buildContainerNotContainedSubQuery() {
        
        StringBuffer lSb =
                new StringBuffer(
                        "SELECT lArticleSQ.id FROM Article lArticleSQ ");
        
        lSb.append("WHERE lArticleSQ.containerArticle IS NULL ");
        lSb.append("AND lArticleSQ.containerInstallation IS NULL ");
        
        lSb.append("AND NOT EXISTS ("
                + "SELECT lContainerTool FROM Contains_Tool_Article lContainerTool"
                + " WHERE lContainerTool.article = lArticleSQ) ");
        
        lSb.append("AND lArticleSQ.id NOT IN ("
                + "SELECT lBoard.id FROM Board lBoard"
                + " WHERE lBoard.containerOrmRack.id IS NOT NULL) ");
        
        lSb.append("AND lArticleSQ.id NOT IN ("
                + "SELECT lBoardPC.id FROM Board lBoardPC"
                + " WHERE lBoardPC.containerOrmPC.id IS NOT NULL) ");
        
        lSb.append("AND lArticleSQ.id NOT IN ("
                + "SELECT lCabinet.id FROM Cabinet lCabinet"
                + " WHERE lCabinet.containerOrmInstallation.id IS NOT NULL) ");
        
        lSb.append("AND lArticleSQ.id NOT IN ("
                + "SELECT lRack.id FROM Rack lRack"
                + " WHERE lRack.containerOrmCabinet.id IS NOT NULL)");
        
        String lSubQuery =
                lSb.toString().replaceAll("lArticleSQ",
                        getUniqueIdentifier("art"));
        
        lSubQuery =
                lSubQuery.replaceAll("lContainerTool",
                        getUniqueIdentifier("linkTool"));
        
        lSubQuery = lSubQuery.replaceAll("lBoard", getUniqueIdentifier("brd"));
        
        lSubQuery =
                lSubQuery.replaceAll("lBoardPC", getUniqueIdentifier("brdPC"));
        
        lSubQuery =
                lSubQuery.replaceAll("lCabinet", getUniqueIdentifier("cab"));
        
        lSubQuery = lSubQuery.replaceAll("lRack", getUniqueIdentifier("rack"));
        
        return lSubQuery;
    }
    
    private List<String> buildContainerCabinetSubQueries(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        String lValue = pContainerName;
        if (lValue.startsWith("!")) {
            lValue = pContainerName.substring(1);
        }
        
        List<String> lQueries = new ArrayList<String>();
        
        // select only Racks
        StringBuffer lSbRacks =
                new StringBuffer(
                        "SELECT lContainerCab.rack.id FROM Contains_Cabinet_Rack lContainerCab");
        if (pContainerName != null) {
            lSbRacks.append(" WHERE ");
            lSbRacks.append(getQueryCondition("lContainerCab.cabinet.airbusSN",
                    lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbRacks.append(" AND ");
            }
            else {
                lSbRacks.append(" OR ");
            }
            lSbRacks.append(
                    getQueryCondition("lContainerCab.cabinet.manufacturerSN",
                            lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbRacks.append(" AND ");
            }
            else {
                lSbRacks.append(" OR ");
            }
            lSbRacks.append(
                    getQueryCondition("lContainerCab.cabinet.designation",
                            lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryRacks =
                lSbRacks.toString().replaceAll("lContainerCab",
                        getUniqueIdentifier("rel"));
        
        lQueries.add(lSubQueryRacks);
        
        // select all Articles except Racks
        StringBuffer lSbArticles =
                new StringBuffer(
                        "SELECT lArticleSQ.id FROM Article lArticleSQ, Cabinet lCabinet"
                                + " WHERE lArticleSQ.containerArticle = lCabinet");
        if (pContainerName != null) {
            lSbArticles.append(" AND (");
            lSbArticles.append(getQueryCondition("lCabinet.airbusSN",
                            lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbArticles.append(" AND ");
            }
            else {
                lSbArticles.append(" OR ");
            }
            lSbArticles.append(getQueryCondition("lCabinet.manufacturerSN",
                    lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbArticles.append(" AND ");
            }
            else {
                lSbArticles.append(" OR ");
            }
            lSbArticles.append(getQueryCondition("lCabinet.designation",
                            lParam, pContainerName));
            lSbArticles.append(")");
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryArticles =
                lSbArticles.toString().replaceAll("lArticleSQ",
                        getUniqueIdentifier("art"));
        
        lSubQueryArticles =
                lSubQueryArticles.replaceAll("lCabinet",
                        getUniqueIdentifier("cab"));
        
        lQueries.add(lSubQueryArticles);
        
        return lQueries;
    }
    
    private List<String> buildContainerInstallationSubQueries(
            String pContainerName, int pIndex,
            Map<String, Object> pParameters) {
        
        List<String> lQueries = new ArrayList<String>();
        
        String lValue = pContainerName;
        if (lValue.startsWith("!")) {
            lValue = pContainerName.substring(1);
        }
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        // select all Articles except Cabinets
        StringBuffer lSbArticles =
                new StringBuffer("SELECT lArticleSQ.id FROM Article lArticleSQ"
                        + " WHERE lArticleSQ.containerInstallation IS NOT NULL");
        if (pContainerName != null) {
            lSbArticles.append(" AND ");
            lSbArticles.append(
                    getQueryCondition("lArticleSQ.containerInstallation.name",
                            lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryArticles = lSbArticles.toString()
                .replaceAll("lArticleSQ", getUniqueIdentifier("art"));
        
        lQueries.add(lSubQueryArticles);
        
        // select only Cabinets
        StringBuffer lSbCabinets = new StringBuffer(
                "SELECT lContainerInst.cabinet.id FROM Contains_Inst_Cabinet lContainerInst");
        if (pContainerName != null) {
            lSbCabinets.append(" WHERE ");
            lSbCabinets.append(
                    getQueryCondition("lContainerInst.installation.name",
                            lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryCabinets = lSbCabinets.toString()
                .replaceAll("lContainerInst", getUniqueIdentifier("rel"));
        
        lQueries.add(lSubQueryCabinets);
        
        return lQueries;
    }
    
    private String buildContainerMotherboardSubQuery(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        StringBuffer lSb = new StringBuffer(
                "SELECT lBoard.id FROM Board lBoard, Board lMotherboard"
                        + " WHERE lBoard.containerArticle = lMotherboard");
        if (pContainerName != null) {
            lSb.append(" AND (");
            lSb.append(getQueryCondition("lBoard.containerArticle.airbusSN",
                    lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSb.append(" AND ");
            }
            else {
                lSb.append(" OR ");
            }
            lSb.append(getQueryCondition(
                    "lBoard.containerArticle.manufacturerSN", lParam,
                    pContainerName));
            lSb.append(")");
            String lValue = pContainerName;
            if (lValue.startsWith("!")) {
                lValue = lValue.substring(1);
            }
            pParameters.put(lParam, lValue);
        }
        
        String lSubQuery =
                lSb.toString().replaceAll("lBoard", getUniqueIdentifier("brd"));
        
        lSubQuery = lSubQuery.replaceAll("lMotherboard",
                getUniqueIdentifier("mbrd"));
        
        return lSubQuery;
    }
    
    private List<String> buildContainerPCSubQueries(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        String lValue = pContainerName;
        if (lValue.startsWith("!")) {
            lValue = pContainerName.substring(1);
        }
        
        List<String> lQueries = new ArrayList<String>();
        
        // select only Boards
        StringBuffer lSbBoards = new StringBuffer(
                "SELECT lContainerPC.board.id FROM Contains_PC_Board lContainerPC");
        if (pContainerName != null) {
            lSbBoards.append(" WHERE ");
            lSbBoards.append(getQueryCondition("lContainerPC.pc.name",
                    lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryBoards =
                lSbBoards.toString().replaceAll("lContainerPC",
                        getUniqueIdentifier("rel"));
        
        lQueries.add(lSubQueryBoards);
        
        // select all Articles except Boards
        StringBuffer lSbArticles =
                new StringBuffer(
                        "SELECT lArticleSQ.id FROM Article lArticleSQ, PC lPC"
                                + " WHERE lArticleSQ.containerArticle = lPC");
        if (pContainerName != null) {
            lSbArticles.append(" AND ");
            lSbArticles.append(
                    getQueryCondition("lPC.name", lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryArticles =
                lSbArticles.toString().replaceAll("lArticleSQ",
                        getUniqueIdentifier("art"));
        
        lSubQueryArticles =
                lSubQueryArticles.replaceAll("lPC", getUniqueIdentifier("pc"));
        
        lQueries.add(lSubQueryArticles);
        
        return lQueries;
    }
    
    private List<String> buildContainerRackSubQueries(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        String lValue = pContainerName;
        if (lValue.startsWith("!")) {
            lValue = pContainerName.substring(1);
        }
        
        List<String> lQueries = new ArrayList<String>();
        
        // select only Boards
        StringBuffer lSbBoards =
                new StringBuffer(
                        "SELECT lContainerRack.board.id FROM Contains_Rack_Board lContainerRack");
        if (pContainerName != null) {
            lSbBoards.append(" WHERE ");
            lSbBoards.append(getQueryCondition("lContainerRack.rack.airbusSN",
                    lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbBoards.append(" AND ");
            }
            else {
                lSbBoards.append(" OR ");
            }
            lSbBoards.append(
                    getQueryCondition("lContainerRack.rack.manufacturerSN",
                            lParam, pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbBoards.append(" AND ");
            }
            else {
                lSbBoards.append(" OR ");
            }
            lSbBoards.append(getQueryCondition(
                    "lContainerRack.rack.designation", lParam, pContainerName));
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryBoards =
                lSbBoards.toString().replaceAll("lContainerRack",
                        getUniqueIdentifier("rel"));
        
        lQueries.add(lSubQueryBoards);
        
        // select all Articles except Boards
        StringBuffer lSbArticles =
                new StringBuffer(
                        "SELECT lArticleSQ.id FROM Article lArticleSQ, Rack lRack"
                                + " WHERE lArticleSQ.containerArticle = lRack");
        if (pContainerName != null) {
            lSbArticles.append(" AND (");
            lSbArticles.append(getQueryCondition("lRack.airbusSN", lParam,
                    pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbArticles.append(" AND ");
            }
            else {
                lSbArticles.append(" OR ");
            }
            lSbArticles.append(getQueryCondition("lRack.manufacturerSN", lParam,
                    pContainerName));
            if (pContainerName.startsWith("!")) {
                lSbArticles.append(" AND ");
            }
            else {
                lSbArticles.append(" OR ");
            }
            lSbArticles.append(getQueryCondition("lRack.designation", lParam,
                    pContainerName));
            lSbArticles.append(")");
            pParameters.put(lParam, lValue);
        }
        
        String lSubQueryArticles =
                lSbArticles.toString().replaceAll("lArticleSQ",
                        getUniqueIdentifier("art"));
        
        lSubQueryArticles =
                lSubQueryArticles.replaceAll("lRack",
                        getUniqueIdentifier("rack"));
        
        lQueries.add(lSubQueryArticles);
        
        return lQueries;
    }
    
    private String buildContainerSwitchSubQuery(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        StringBuffer lSb =
                new StringBuffer(
                        "SELECT DISTINCT lArticleSQ.id FROM Article lArticleSQ, Switch lSwitch"
                                + " WHERE lArticleSQ.containerArticle = lSwitch");
        if (pContainerName != null) {
            String lValue = pContainerName;
            lSb.append(" AND (");
            lSb.append(getQueryCondition("lSwitch.airbusSN", lParam,
                    pContainerName));
            if (pContainerName.startsWith("!")) {
                lSb.append(" AND ");
            }
            else {
                lSb.append(" OR ");
            }
            lSb.append(getQueryCondition("lSwitch.manufacturerSN", lParam,
                    pContainerName));
            lSb.append(")");
            if (lValue.startsWith("!")) {
                lValue = lValue.substring(1);
            }
            pParameters.put(lParam, pContainerName);
        }
        
        String lSubQuery = lSb.toString().replaceAll("lArticleSQ",
                getUniqueIdentifier("art"));
        
        lSubQuery =
                lSubQuery.replaceAll("lSwitch", getUniqueIdentifier("swtch"));
        
        return lSubQuery;
    }
    
    private String buildContainerToolSubQuery(String pContainerName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParam = convertParamName(CONTAINER_NAME) + pIndex;
        
        // select all Articles
        StringBuffer lSb =
                new StringBuffer(
                        "SELECT lContainerTool.article.id FROM Contains_Tool_Article lContainerTool");
        if (pContainerName != null) {
            lSb.append(" WHERE ");
            lSb.append(getQueryCondition("lContainerTool.tool.name", lParam,
                    pContainerName));
            String lValue = pContainerName;
            if (lValue.startsWith("!")) {
                lValue = lValue.substring(1);
            }
            pParameters.put(lParam, lValue);
        }
        
        String lSubQuery =
                lSb.toString().replaceAll("lContainerTool",
                        getUniqueIdentifier("rel"));
        
        return lSubQuery;
    }
    
    private String buildExtLocationNotSentSubQuery() {
        
        String lSubQuery =
                "SELECT lArticleSQ.id FROM Article lArticleSQ WHERE NOT EXISTS ("
                        + "SELECT lLocation FROM LocationForArticle lLocation"
                        + " WHERE lLocation.article = lArticleSQ"
                        + " AND lLocation.externalEntity IS NOT NULL)";
        
        lSubQuery =
                lSubQuery.replaceAll("lArticleSQ", getUniqueIdentifier("art"));
        
        lSubQuery =
                lSubQuery.replaceAll("lLocation", getUniqueIdentifier("loc"));
        
        return lSubQuery;
    }
    
    private String buildExtLocationExternalEntitySubQuery(
            Long pExternalLocationType, String pExternalLocationName,
            int pIndex, Map<String, Object> pParameters) {
        
        String lParamType = convertParamName(EXTERNAL_LOCATION_TYPE) + "_id"
                + pExternalLocationType + "_" + pIndex;
        String lParamName = convertParamName(EXTERNAL_LOCATION_NAME) + pIndex;
        String lParamLoc = getUniqueIdentifier("loc");
        
        StringBuffer lSb =
                new StringBuffer(
                        "SELECT " + lParamLoc
                                + ".article.id FROM LocationForArticle "
                                + lParamLoc);
        if (pExternalLocationType != null) {
            lSb.append(" WHERE " + lParamLoc
                    + ".externalEntity.externalEntityType.id = :paramType");
            pParameters.put(lParamType, pExternalLocationType);
        }
        else {
            lSb.append(" WHERE " + lParamLoc + ".externalEntity IS NOT NULL");
        }
        if (pExternalLocationName != null) {
            lSb.append(" AND ");
            lSb.append(getQueryCondition(lParamLoc + ".externalEntity.name",
                    lParamName, pExternalLocationName));
            String lValue = (String) pExternalLocationName;
            if (lValue.startsWith("!")) {
                lValue = lValue.substring(1);
            }
            pParameters.put(lParamName, lValue);
        }
        
        String lSubQuery = lSb.toString().replaceAll("paramType", lParamType);
        
        return lSubQuery;
    }
    
    private String buildLocationNotLocatedSubQuery() {
        
        String lSubQuery =
                "SELECT lArticleViewMat.id FROM ArticleViewMat lArticleViewMat"
                        + " WHERE lArticleViewMat.computedLocationPlace IS NULL";
        
        lSubQuery =
                lSubQuery.replaceAll("lArticleViewMat",
                        getUniqueIdentifier("artViewMat"));
        
        return lSubQuery;
    }
    
    private String buildLocationPlaceSubQuery(Object pLocationName, int pIndex,
            Map<String, Object> pParameters) {
        String lValue = (String) pLocationName;
        if (StringUtil.isEmptyOrNull(lValue)) {
            lValue = "*";
        }
        String lParam = convertParamName(LOCATION_NAME) + pIndex;
        
        String lSubQuery =
                "SELECT lArticleViewMat.id FROM ArticleViewMat lArticleViewMat";
        lSubQuery += " WHERE ";
        String lAttribute =
                "CONCAT(lArticleViewMat.computedLocationPlace.building.name, '"
                        + Place.BUILDING_PLACE_SEPARATOR
                        + "', lArticleViewMat.computedLocationPlace.name)";
        lSubQuery += getQueryCondition(lAttribute, lParam, lValue);
        
        if (lValue.startsWith("!")) {
            lValue = lValue.substring(1);
        }
        
        pParameters.put(lParam, lValue);
        
        lSubQuery = lSubQuery.replaceAll("lArticleViewMat",
                        getUniqueIdentifier("artViewMat"));
        
        return lSubQuery;
    }
    
    @Override
    protected Order generateSpecificOrder(String pSortField,
    		SortOrder pOrdering, String pLanguageCode) {
        
        Order lOrder = new Order();
        
        if (pSortField.equals(IN_CHARGE_LOGIN_DETAILS)) {
            // the ORDER BY clause shall respect the User.getLoginDetails
            // method
            lOrder.addSorting("lArticle.inCharge.lastname", pOrdering);
            lOrder.addSorting("lArticle.inCharge.firstname", pOrdering);
            lOrder.addSorting("lArticle.inCharge.login", pOrdering);
            return lOrder;
        }
        else if (pSortField.equals(ALLOCATION) || pSortField.equals(USAGE)
                || pSortField.equals(PRODUCT_TYPE)) {
            // the ORDER BY clause shall respect the
            // AttributeValueList.translate method
            StringBuffer lSortField = new StringBuffer();
            lSortField.append("lArticle.").append(pSortField);
            if (pLanguageCode != null
                    && pLanguageCode.equals(Locale.FRENCH.getISO3Language())) {
                lSortField.append(".frenchValue");
            }
            else {
                lSortField.append(".defaultValue");
            }
            lOrder.addSorting(lSortField.toString(), pOrdering);
            return lOrder;
        }
        else if (pSortField.equals(AIRBUS_PN)
                || pSortField.equals(MANUFACTURER_PN)) {
            // In order to keep NULL values while sorting, the computed field of
            // the view on articles is used instead of the attribute of the
            // contained entity
            addJoin("ArticleView",
                    "INNER JOIN ArticleView lArticleView ON "
                            + "lArticle.id = lArticleView.id");
            if (pSortField.equals(AIRBUS_PN)) {
                lOrder.addSorting("lArticleView.airbusPN", pOrdering);
            }
            else if (pSortField.equals(MANUFACTURER_PN)) {
                lOrder.addSorting("lArticleView.manufacturerPN", pOrdering);
            }
            return lOrder;
        }
        return null;
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
                    lAdditionalCondition =
                            "" + testNull + " IS NULL OR ";
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
}
