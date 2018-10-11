/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderTool
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

import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnTool;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Manage reading of a tool into Excel files
 */
public class IOExcelReaderTool extends IOExcelBaseReader {
    
    private ToolBean toolBean;
    private LocationFactory locationFactory;
    private ContainerFactory containerFactory;
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the workbook to read
     * @param pLocationFactory
     *            the locationFactory to use
     * @param pContainerFactory
     *            the containerFactory to use
     * @param pToolBean
     *            the toolBean to use
     * @param pValueListBean
     *            the ValueListBean to use
     */
    public IOExcelReaderTool(Workbook pWorkbook,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ToolBean pToolBean,
            ValueListBean pValueListBean) {
        
        super(pValueListBean, pWorkbook, new ColumnTool(),
                IOConstants.TOOL_SHEET_NAME);
        
        locationFactory = pLocationFactory;
        containerFactory = pContainerFactory;
        toolBean = pToolBean;
    }
    
    @Override
    public void readLine() throws ImportException {
        
        String lName = readField(row, IOConstants.NAME_TITLE);
        String lDesignation = readField(row, IOConstants.DESIGNATION_TITLE);
        
        // Location
        String lLocationStr = readField(row, IOConstants.LOCATION_TITLE);
        String lLocationDetails =
                readField(row, IOConstants.LOCATION_DETAILS_TITLE);
        String lInheritedStr =
                readField(row, IOConstants.INHERITED_LOCATION_TITLE);
        
        // External location
        String lExternalLocationStr =
                readField(row, IOConstants.EXTERNAL_LOCATION_NAME_TITLE);
        
        // Container
        String lContainerTypeStr =
                readField(row, IOConstants.CONTAINER_TYPE_TITLE);
        String lContainerStr = readField(row, IOConstants.CONTAINER_TITLE);
        
        String lPersonInCharge = readField(row, IOConstants.TECHNICAL_CONTACT_TITLE);
        Date lLoanDate = readDateField(row, IOConstants.LOAN_DATE_TITLE);
        Date lLoanDueDate = readDateField(row, IOConstants.LOAN_DUE_DATE_TITLE);
        String lGeneralComment =
                readField(row, IOConstants.GENERAL_COMMENT_TITLE);
        
        if (lName == null) {
            throw new ImportException("NAME EMPTY", row.getRowNum(),
                    columns.getIndex(IOConstants.NAME_TITLE));
        }
        
        if (lDesignation == null) {
            throw new ImportException("DESIGNATION EMPTY", row.getRowNum(),
                    columns.getIndex(IOConstants.DESIGNATION_TITLE));
        }
        
        Tool lTool = new Tool();
        lTool.setName(lName);
        lTool.setDesignation(lDesignation);
        lTool.setPersonInCharge(lPersonInCharge);
        lTool.setLoanDate(lLoanDate);
        lTool.setLoanDueDate(lLoanDueDate);
        if (lGeneralComment != null) {
            lTool.setGeneralComment(new Comment(lGeneralComment));
        }
        
        Container lContainer =
                readContainer(lTool, lContainerTypeStr, lContainerStr);
        
        Location lLocation =
                readLocation(lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        try {
            lTool =
                    toolBean.create(lTool, null, null, lLocation, lContainer,
                            null);
        }
        catch (Exception e) {
            
            throw new ImportException("CREATION TOOL IMPOSSIBLE: "
                    + ExceptionUtil.getMessage(e), row.getRowNum(), -1);
        }
    }
    
    /**
     * Read the location
     * 
     * @param pLocationStr
     *            the location name
     * @param pDetails
     *            the details on location
     * @param pExternalLocationStr
     *            the external location name
     * @param pInheritedStr
     *            the inherited value
     * @param pItemContainer
     *            the located object container
     * @return the location
     * @throws ImportException
     *             when the location is not correct
     */
    private Location readLocation(String pLocationStr, String pDetails,
            String pExternalLocationStr, String pInheritedStr,
            Container pItemContainer) throws ImportException {
        
        Location lLocation = null;
        
        Boolean lInherited = readBoolean(pInheritedStr);
        if (lInherited == null) {
            lInherited = false;
        }
        
        try {
            lLocation =
                    locationFactory.generateLocation(pLocationStr, pDetails,
                            pExternalLocationStr, LocatedType.Tool, lInherited,
                            pItemContainer);
            
            LocationManager.validateLocation(lLocation);
            
        }
        catch (LocalizationException e) {
            switch (e.getError()) {
            case LocationNotAvailableForItem:
            case LocationNotFound:
                throw new ImportException(e.getMessage(), row.getRowNum(),
                        columns.getIndex(IOConstants.LOCATION_TITLE));
            case LocationMandatory:
                throw new ImportException(
                        MessageBundle.getMessage(Constants.LOCATION_MANDATORY),
                        row.getRowNum(),
                        getColumnIndex(IOConstants.LOCATION_TITLE));
            case InheritedLocationNotAvailableForItem:
            case InheritedLocationNotAvailableContainerNotDefined:
                throw new ImportException(e.getMessage(), row.getRowNum(),
                        columns.getIndex(IOConstants.INHERITED_LOCATION_TITLE));
            case ContainerNotAvailableForItem:
            case ContainerNotFound:
            case ContainerPrecisionNotValid:
            case ExternalLocationNotFound:
            default:
                throw new ImportException(e.getMessage(), row.getRowNum(), -1);
            }
        }
        
        return lLocation;
    }
    
    /**
     * Read the container
     * 
     * @param pTool
     *            the tool
     * @param pContainerTypeStr
     *            the container type in string form
     * @param pContainerStr
     *            the container name
     * @return the container
     * @throws ImportException
     *             when the container is not correct
     */
    private Container readContainer(Tool pTool, String pContainerTypeStr,
            String pContainerStr) throws ImportException {
        
        if (StringUtil.isEmptyOrNull(pContainerTypeStr)
                && StringUtil.isEmptyOrNull(pContainerStr)) {
            return null;
        }
        
        ContainerType lContainerType =
                readContainerType(pContainerTypeStr, pTool, getRownumber());
        
        if (lContainerType != null && pContainerStr == null) {
            
            throw new ImportException(MessageBundle.getMessageResource(
                    IOConstants.ROW_EMPTY_FIELD, new Object[] { getRownumber(),
                            "Parent" }), row.getRowNum(),
                    columns.getIndex(IOConstants.CONTAINER_TITLE));
        }
        
        Container lContainer = null;
        
        try {
            if (lContainerType != null) {
                
                lContainer =
                        containerFactory.generateContainer(lContainerType,
                                pContainerStr, null, ContainedType.Tool);
            }
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            throw new ImportException("Parent KO : " + e.getMessage(),
                    row.getRowNum(),
                    columns.getIndex(IOConstants.CONTAINER_TITLE));
        }
        
        if (lContainer == null) {
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.CONTAINER_NOT_FOUND,
                            new String[] { pContainerStr });
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(IOConstants.CONTAINER_TITLE));
        }
        
        return lContainer;
    }
    
    /**
     * Read the container type and check that it is an authorized one
     * 
     * @param pContainerTypeStr
     *            the container type in a string form
     * @param pTool
     *            the tool
     * @param pRowNum
     *            the row number
     * @return the read containerType
     * @throws ImportException
     *             when the container is not correct
     */
    private ContainerType readContainerType(String pContainerTypeStr,
            Tool pTool, int pRowNum) throws ImportException {
        
        ContainerType lContainerType = null;
        
        if (pContainerTypeStr != null) {
            
            if (pContainerTypeStr
                    .matches(IOConstants.REGEX_MIGRATION_INSTALLATION)) {
                lContainerType = ContainerType.Installation;
            }
            else {
                throw new ImportException(
                        MessageBundle.getMessageResource(
                                IOConstants.ROW_INCORRECT_FIELD, new Object[] {
                                        pRowNum,
                                        "Parent Type: " + pContainerTypeStr }),
                        row.getRowNum(),
                        columns.getIndex(IOConstants.CONTAINER_TYPE_TITLE));
            }
            
            ContainerManager lContainerManager = new ContainerManager(pTool);
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
