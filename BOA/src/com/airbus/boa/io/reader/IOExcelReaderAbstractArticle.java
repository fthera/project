/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderAbstractArticle
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.TypeController;

/**
 * Manage reading of an article in Excel files
 */
public abstract class IOExcelReaderAbstractArticle extends IOExcelBaseReader {
    
    /** The articleBean to use during process */
    protected ArticleBean articleBean;
    private LocationFactory locationFactory;
    private ContainerFactory containerFactory;
    /** The logged user */
    protected User user;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook from which to read
     * @param columns
     *            the columns into Excel files
     * @param sheetname
     *            the sheet name into the Excel file
     * @param pLocationFactory
     *            the locationFactory to use during process
     * @param pContainerFactory
     *            the containerFactory to use during process
     * @param articleBean
     *            the articleBean to use during process
     * @param valueListBean
     *            the valueListBean to use during process
     * @param user
     *            the user logged in
     */
    public IOExcelReaderAbstractArticle(Workbook workbook, Columns columns,
            String sheetname, LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        
        super(valueListBean, workbook, columns, sheetname);
        
        locationFactory = pLocationFactory;
        containerFactory = pContainerFactory;
        this.articleBean = articleBean;
        this.user = user;
    }
    
    /**
     * Read the current line and update the provided article with the line data.<br>
     * Common fields are read:
     * - Acquisition date, - Airbus SN, - Manufacturer SN, - Type, - Airbus PN,
     * - Manufacturer PN, - CMS Code, - State, - General comment <br>
     * Update also the history of the article with the creation action.
     * 
     * @param article
     *            the article to update
     * @throws ImportException
     *             when an error occurs
     */
    protected void readLine(Article article) throws ImportException {
        String airbusSN, manufacturerSN, typeStr, airbusPNstr,
                manufacturerPNstr, cmsCode, state, useState, generalComment;
        
        Date acquisitionDate =
                readDateField(row, IOConstants.ACQUISITIONDATE_TITLE);
        airbusSN = readField(row, IOConstants.AIRBUS_SN_TITLE);
        manufacturerSN = readField(row, IOConstants.MANUFACTURER_SN_TITLE);
        typeStr = readField(row, IOConstants.TYPE_TITLE);
        airbusPNstr = readField(row, IOConstants.AIRBUS_PN_TITLE);
        manufacturerPNstr = readField(row, IOConstants.MANUFACTURER_PN_TITLE);
        cmsCode = readField(row, IOConstants.CMSCODE_TITLE);
        state = readField(row, IOConstants.STATE_TITLE);
        useState = readField(row, IOConstants.USE_STATE_TITLE);
        
        generalComment = readField(row, IOConstants.GENERAL_COMMENT_TITLE);
        
        article.setAirbusSN(airbusSN);
        article.setManufacturerSN(manufacturerSN);
        
        if (airbusSN == null && manufacturerSN == null) {
            String msg =
                    MessageBundle
                            .getMessage(IOConstants.ASN_OR_MSN_MUST_BE_FILLED);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        if ((airbusSN != null && articleBean.existASN(airbusSN))) {
            String msg =
                    MessageBundle
                            .getMessage(IOConstants.ARTICLE_ALREADY_EXISTS);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        article.setCmsCode(cmsCode);
        article.setAcquisitionDate(acquisitionDate);
        
        Action.createCreationAction(user, article, Constants.CREATION_BY_IMPORT);
        
        if (article.getHistory() != null && generalComment != null
                && !generalComment.isEmpty()) {
            article.getHistory().setGeneralComment(new Comment(generalComment));
        }
        
        article.setState(readState(state, getRownumber()));
        article.setUseState(readUseState(useState, getRownumber()));
        
        article.setManufacturerPN(readManufacturerPN(manufacturerPNstr));
        
        TypeArticle type = readTypeArticle(typeStr, article);
        
        AirbusPN aPN = readAirbusPN(airbusPNstr, type);
        article.setAirbusPN(aPN);
        
    }
    
    /**
     * Set the provided article type to the article
     * 
     * @param typeStr
     *            the article type in string form
     * @param article
     *            the article to update
     * @return the found article type
     * @throws ImportException
     *             when the provided article type string does not correspond to
     *             an existing type<br>
     *             or when the found article type does correspond to the
     *             provided article class
     */
    protected TypeArticle readTypeArticle(String typeStr, Article article)
            throws ImportException {
        TypeArticle type = articleBean.findTypeArticleByName(article, typeStr);
        if (type == null
                || type.getClass() != article.createTypeArticle().getClass()) {
            
            Object[] args = { getRownumber(), "[Type :" + typeStr + "]" };
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.ROW_INCORRECT_FIELD, args);
            throw new ImportException(msg, row.getRowNum(),
                    columns.getIndex(IOConstants.TYPE_TITLE));
        }
        else {
            article.setTypeArticle(type);
        }
        return type;
    }
    
    /**
     * Read the Article state on the provided row
     * 
     * @param stateStr
     *            the Article state in String form
     * @param rowNum
     *            the row number
     * @return the read ArticleState
     * @throws ImportException
     *             when the provided string is not an ArticleState
     */
    protected ArticleState readState(String stateStr, int rowNum)
            throws ImportException {
        
        if (stateStr == null) {
            throw new ImportException(MessageBundle.getMessageResource(
                    IOConstants.ROW_EMPTY_FIELD,
                    new Object[] { rowNum, "State" }), row.getRowNum(),
                    columns.getIndex(IOConstants.STATE_TITLE));
        }
        
        ArticleState lState = ArticleState.getEnumValue(stateStr);
        
        if (lState == null) {
            throw new ImportException(MessageBundle.getMessageResource(
                    IOConstants.ROW_INCORRECT_FIELD, new Object[] { rowNum,
                            "State:" + stateStr }), row.getRowNum(),
                    columns.getIndex(IOConstants.STATE_TITLE));
        }
        return lState;
    }
    
    /**
     * Read the use state on the provided row
     * 
     * @param pUseStateStr
     *            the use state in String form
     * @param pRowNum
     *            the row number
     * @return the read ArticleState
     * @throws ImportException
     *             when the provided string is not an ArticleState
     */
    protected UseState readUseState(String pUseStateStr, int pRowNum)
            throws ImportException {
        
        if (pUseStateStr == null) {
            throw new ImportException(
                    MessageBundle.getMessageResource(
                            IOConstants.ROW_EMPTY_FIELD,
                            new Object[] { pRowNum, "Use State" }),
                    row.getRowNum(),
                    columns.getIndex(IOConstants.USE_STATE_TITLE));
        }
        
        UseState lUseState = UseState.getEnumValue(pUseStateStr);
        
        if (lUseState == null) {
            lUseState = UseState.InUse;
        }
        return lUseState;
    }
    
    private AirbusPN readAirbusPN(String airbusIdentifier, TypeArticle type)
            throws ImportException {
        
        AirbusPN aPN = null;
        if (airbusIdentifier != null) {
            aPN = articleBean.findAirbusPNByName(airbusIdentifier);
            if (aPN != null && type != null) {
                
                if ((type.getListAirbusPN() == null)
                        || (!type.getListAirbusPN().contains(aPN))) {
                    
                    String msg =
                            MessageBundle
                                    .getMessageResource(
                                            IOConstants.AIRBUSPN_TYPEARTICLE_RELATION_INCORRECT,
                                            new Object[] { aPN.getIdentifier(),
                                                    type.getLabel() });
                    throw new ImportException(msg, row.getRowNum(),
                            columns.getIndex(IOConstants.AIRBUS_PN_TITLE));
                }
                
            }
            else {
                // Create the Airbus PN
                aPN = new AirbusPN();
                
                try {
                    TypeController.validatePNIdentifier(airbusIdentifier);
                    aPN.setIdentifier(airbusIdentifier);
                    aPN = articleBean.createAirbusPN(aPN, type);
                }
                catch (Exception e) {
                    
                    String msg =
                            MessageBundle.getMessageResource(
                                    IOConstants.AIRBUSPN_CREATION_ERROR,
                                    new Object[] { aPN.getIdentifier() });
                    
                    log.warning(msg + e.getMessage());
                    throw new ImportException(msg + e.getMessage(),
                            row.getRowNum(),
                            columns.getIndex(IOConstants.AIRBUS_PN_TITLE));
                    
                }
                
            }
            
        }
        return aPN;
    }
    
    private ManufacturerPN readManufacturerPN(String manuIdentifier)
            throws ImportException {
        
        if (StringUtil.isEmptyOrNull(manuIdentifier)) {
            return null;
        }
        
        ManufacturerPN pn =
                articleBean.findManufacturerPNByName(manuIdentifier);
        if (pn == null) {
            pn = new ManufacturerPN();
            pn.setIdentifier(manuIdentifier);
            try {
                pn = articleBean.createPN(pn);
            }
            catch (Exception e) {
                String msg =
                        MessageBundle.getMessageResource(
                                IOConstants.MANUFACTURERPN_CREATION_ERROR,
                                new Object[] { pn.getIdentifier() });
                throw new ImportException(msg + e.getMessage(),
                        row.getRowNum(),
                        getColumnIndex(IOConstants.MANUFACTURER_PN_TITLE));
                
            }
        }
        
        return pn;
        
    }
    
    /**
     * Build the provided article location depending on the provided location
     * type in string form
     * 
     * @param pArticle
     *            the article to locate
     * @param pLocationStr
     *            the location name
     * @param pPrecision
     *            the location details
     * @param pExternalLocationStr
     *            the external location name
     * @param pInheritedStr
     *            the inherited value
     * @param pItemContainer
     *            the located object container
     * @return the built location
     * @throws ImportException
     *             when the location cannot be built
     */
    protected Location readLocation(Article pArticle, String pLocationStr,
            String pPrecision, String pExternalLocationStr,
            String pInheritedStr, Container pItemContainer)
            throws ImportException {
        
        Location lLocation = null;
        
        Boolean lInherited = readBoolean(pInheritedStr);
        if (lInherited == null) {
            lInherited = false;
        }
        
        try {
            lLocation =
                    locationFactory.generateLocation(pLocationStr, pPrecision,
                            pExternalLocationStr, pArticle.getLocatedType(),
                            lInherited, pItemContainer);
            if (!pArticle.getUseState().equals(UseState.Archived)) {
                LocationManager.validateLocation(lLocation);
            }
            
        }
        catch (LocalizationException e) {
            switch (e.getError()) {
            case InheritedLocationNotAvailableForItem:
            case InheritedLocationNotAvailableContainerNotDefined:
                throw new ImportException(e.getMessage(), row.getRowNum(),
                        columns.getIndex(IOConstants.INHERITED_LOCATION_TITLE));
            case LocationNotAvailableForItem:
            case LocationNotFound:
                throw new ImportException(e.getMessage(), row.getRowNum(),
                        columns.getIndex(IOConstants.LOCATION_TITLE));
            case LocationMandatory:
                throw new ImportException(
                        MessageBundle.getMessage(Constants.LOCATION_MANDATORY),
                        row.getRowNum(),
                        columns.getIndex(IOConstants.LOCATION_TITLE));
            case ExternalLocationNotFound:
                throw new ImportException(
                        e.getMessage(),
                        row.getRowNum(),
                        columns.getIndex(IOConstants.EXTERNAL_LOCATION_NAME_TITLE));
            case ContainerNotAvailableForItem:
            case ContainerPrecisionNotValid:
            case ContainerNotFound:
            default:
                throw new ImportException(e.getMessage(), row.getRowNum(), -1);
            }
        }
        
        return lLocation;
    }
    
    /**
     * Build the provided article container depending on the provided container
     * type in string form
     * 
     * @param pArticle
     *            the article to link
     * @param pContainerType
     *            the container type in string form
     * @param pContainerStr
     *            the container name
     * @param pDetails
     *            the container details
     * @return the built container
     * @throws ImportException
     *             when the container cannot be built
     */
    protected Container readContainer(Article pArticle, String pContainerType,
            String pContainerStr, String pDetails) throws ImportException {
        
        if (StringUtil.isEmptyOrNull(pContainerType)
                && StringUtil.isEmptyOrNull(pContainerStr)) {
            return null;
        }
        
        ContainerType lContainerType =
                readContainerType(pContainerType, pArticle, getRownumber());
        
        if (lContainerType != null && pContainerStr == null) {
            throw new ImportException(MessageBundle.getMessageResource(
                    IOConstants.ROW_EMPTY_FIELD, new Object[] { getRownumber(),
                            "Parent" }), row.getRowNum(),
                    columns.getIndex(IOConstants.CONTAINER_TITLE));
        }
        
        Container lContainer = null;
        
        if (lContainerType != null) {
            
            try {
                lContainer =
                        containerFactory.generateContainer(lContainerType,
                                pContainerStr, pDetails,
                                pArticle.getContainedType());
            }
            catch (LocalizationException e) {
                switch (e.getError()) {
                case ContainerNotAvailableForItem:
                case ContainerNotFound:
                    throw new ImportException(e.getMessage(), row.getRowNum(),
                            columns.getIndex(IOConstants.CONTAINER_TITLE));
                case ContainerPrecisionNotValid:
                case InheritedLocationNotAvailableForItem:
                case InheritedLocationNotAvailableContainerNotDefined:
                case LocationNotAvailableForItem:
                case LocationMandatory:
                case LocationNotFound:
                case ExternalLocationNotFound:
                default:
                    throw new ImportException(e.getMessage(), row.getRowNum(),
                            -1);
                }
            }
        }
        
        return lContainer;
    }
    
    private ContainerType readContainerType(String pContainerTypeStr,
            Article pArticle, int pRowNum) throws ImportException {
        
        ContainerType lContainerType = null;
        
        if (pContainerTypeStr != null) {
            
            if (pContainerTypeStr.matches(IOConstants.REGEX_MIGRATION_RACK)) {
                lContainerType = ContainerType.Rack;
            }
            else if (pContainerTypeStr
                    .matches(IOConstants.REGEX_MIGRATION_CABINET)) {
                lContainerType = ContainerType.Cabinet;
            }
            else if (pContainerTypeStr
                    .matches(IOConstants.REGEX_MIGRATION_BOARD)) {
                lContainerType = ContainerType.Board;
            }
            else if (pContainerTypeStr
                    .matches(IOConstants.REGEX_MIGRATION_INSTALLATION)) {
                lContainerType = ContainerType.Installation;
            }
            else if (pContainerTypeStr
                    .matches(IOConstants.REGEX_MIGRATION_TOOL)) {
                lContainerType = ContainerType.Tool;
            }
            else if (pContainerTypeStr.equalsIgnoreCase("PC")) {
                lContainerType = ContainerType.PC;
            }
            else if (pContainerTypeStr.equalsIgnoreCase("SWITCH")) {
                lContainerType = ContainerType.Switch;
            }
            else {
                throw new ImportException(
                        MessageBundle.getMessageResource(
                                IOConstants.ROW_INCORRECT_FIELD, new Object[] {
                                        pRowNum,
                                        "Parent type: " + pContainerTypeStr }),
                        row.getRowNum(),
                        columns.getIndex(IOConstants.CONTAINER_TYPE_TITLE));
            }
            
            ContainerManager lContainerManager = new ContainerManager(pArticle);
            Set<ContainerType> lPossibleContainerTypes = Collections.emptySet();
            
            lPossibleContainerTypes =
                    new HashSet<ContainerType>(Arrays.asList(lContainerManager
                            .getPossibleContainers()));
            
            if (!lPossibleContainerTypes.contains(lContainerType)) {
                throw new ImportException(MessageBundle.getMessageResource(
                        IOConstants.ROW_IMPOSSIBLE_CONTAINER, new Object[] {
                                pRowNum, lContainerType.toString() }),
                        row.getRowNum(),
                        columns.getIndex(IOConstants.CONTAINER_TYPE_TITLE));
            }
            
        }
        return lContainerType;
    }
    
    private Boolean readBoolean(String pBooleanValue) {
        
        if (pBooleanValue == null) {
            return false;
        }
        
        Boolean lResult = null;
        for (String lString : IOConstants.TRUE_STRINGS) {
            if (pBooleanValue.equalsIgnoreCase(lString)) {
                lResult = true;
            }
        }
        for (String lString : IOConstants.FALSE_STRINGS) {
            if (pBooleanValue.equalsIgnoreCase(lString)) {
                lResult = false;
            }
        }
        return lResult;
    }
    
}
