/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderPC
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnPC;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.application.DBConstants;

/**
 * Manage reading of a PC into Excel files
 */
public class IOExcelReaderPC extends IOExcelReaderAbstractArticle {
    
    private static final String SHEET_NAME = IOConstants.PCSheetName;
    
    private PCBean pcBean;
    
    private UserBean userBean;
    
    private SoftwareBean softwareBean;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook from which to read
     * @param columns
     *            the columns into Excel files
     * @param pLocationFactory
     *            the locationFactory to use during process
     * @param pContainerFactory
     *            the containerFactory to use during process
     * @param articleBean
     *            the articleBean to use during process
     * @param pcBean
     *            the pcBean to use during process
     * @param valueListBean
     *            the valueListBean to use during process
     * @param pUserBean
     *            the userBean to use during process
     * @param pSoftwareBean
     *            the softwareBean to use during process
     * @param user
     *            the user logged in
     */
    public IOExcelReaderPC(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            PCBean pcBean, ValueListBean valueListBean, UserBean pUserBean,
            SoftwareBean pSoftwareBean, User user) {
        
        super(workbook, columns, SHEET_NAME, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        
        this.pcBean = pcBean;
        userBean = pUserBean;
        softwareBean = pSoftwareBean;
        if (columns == null) {
            this.columns = new ColumnPC();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        
        // Check the name uniqueness
        String lName = readField(row, IOConstants.NAME_TITLE);
        if (lName == null) {
            String msg =
                    MessageBundle.getMessageResource(
                            Constants.FIELD_MUST_NOT_BE_EMPTY,
                            new Object[] { IOConstants.NAME_TITLE });
            throw new ImportException(msg, getRownumber(),
                    getColumnIndex(IOConstants.NAME_TITLE));
        }
        else if (pcBean.existByName(lName)) {
            
            String msg =
                    MessageBundle
                            .getMessage(Constants.PC_NAME_ALREADY_EXISTING);
            throw new ImportException(msg, getRownumber(), -1);
        }
        
        String lDeparmentInChargeStr =
                readField(row, IOConstants.DEPARTMENTINCHARGE_TITLE);
        checkNotEmptyField(row, lDeparmentInChargeStr,
                IOConstants.DEPARTMENTINCHARGE_TITLE);
        DepartmentInCharge lDeparmentInCharge = readDepartmentInCharge(
                lDeparmentInChargeStr, IOConstants.DEPARTMENTINCHARGE_TITLE);
        
        // Check the Airbus SN uniqueness
        String lAirbusSN = readField(row, IOConstants.AIRBUS_SN_TITLE);
        if (lAirbusSN == null) {
            String msg =
                    MessageBundle.getMessageResource(
                            Constants.FIELD_MUST_NOT_BE_EMPTY,
                            new Object[] { IOConstants.AIRBUS_SN_TITLE });
            throw new ImportException(msg, getRownumber(),
                    getColumnIndex(IOConstants.AIRBUS_SN_TITLE));
        }
        else if (pcBean.exist(lAirbusSN)) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.AIRBUSSN_ALREADY_USED,
                            new Object[] { lAirbusSN });
            throw new ImportException(msg, getRownumber(),
                    getColumnIndex(IOConstants.AIRBUS_SN_TITLE));
        }
        
        DBConstants lDBConstants =
                AbstractController.findBean(DBConstants.class);
        
        final int COMMENT_LENGTH = lDBConstants.getPcCommentLength();
        
        String lCommentStr = readField(row, IOConstants.COMMENT_TITLE);
        if (lCommentStr != null && lCommentStr.length() > COMMENT_LENGTH) {
            lCommentStr = lCommentStr.substring(COMMENT_LENGTH);
        }
        
        String lOwner = readField(row, IOConstants.OWNER_TITLE);
        checkNotEmptyField(row, lOwner, IOConstants.OWNER_TITLE);
        
        String lOwnerSiglum = readField(row, IOConstants.OWNER_SIGLUM_TITLE);
        checkNotEmptyField(row, lOwnerSiglum, IOConstants.OWNER_SIGLUM_TITLE);
        
        String lInChargeStr =
                readField(row, IOConstants.PC_TECHNICAL_CONTACT_TITLE);
        checkNotEmptyField(row, lInChargeStr,
                IOConstants.PC_TECHNICAL_CONTACT_TITLE);
        User lInCharge =
                readUser(row, lInChargeStr,
                        IOConstants.PC_TECHNICAL_CONTACT_TITLE);
        
        String lBusinessUsageStr =
                readField(row, IOConstants.BUSINESS_USAGE_TITLE);
        checkNotEmptyField(row, lBusinessUsageStr,
                IOConstants.BUSINESS_USAGE_TITLE);
        BusinessUsagePC lBusinessUsage =
                readValueList(row, lBusinessUsageStr,
                        IOConstants.BUSINESS_USAGE_TITLE, BusinessUsagePC.class);
        
        String lBusinessAllocationStr =
                readField(row, IOConstants.BUSINESS_ALLOC_TITLE);
        checkNotEmptyField(row, lBusinessAllocationStr,
                IOConstants.BUSINESS_ALLOC_TITLE);
        BusinessAllocationPC lBusinessAllocation =
                readValueList(row, lBusinessAllocationStr,
                        IOConstants.BUSINESS_ALLOC_TITLE,
                        BusinessAllocationPC.class);
        
        String lProductTypeStr = readField(row, IOConstants.PRODUCT_TYPE_TITLE);
        checkNotEmptyField(row, lProductTypeStr, IOConstants.PRODUCT_TYPE_TITLE);
        ProductTypePC lProductTypePC =
                readValueList(row, lProductTypeStr,
                        IOConstants.PRODUCT_TYPE_TITLE, ProductTypePC.class);
        
        String lStateStr = readField(row, IOConstants.STATE_TITLE);
        ArticleState lState = readState(lStateStr, row.getRowNum());
        
        String lUseStateStr = readField(row, IOConstants.USE_STATE_TITLE);
        UseState lUseState = readUseState(lUseStateStr, row.getRowNum());
        
        Double lNbScreensDbl =
                readNumericField(row, IOConstants.NB_SCREENS_TITLE);
        Integer lNbScreens = null;
        if (lNbScreensDbl != null) {
            lNbScreens = lNbScreensDbl.intValue();
        }
        
        String lTypePCStr = readField(row, IOConstants.TYPE_TITLE);
        checkNotEmptyField(row, lTypePCStr, IOConstants.TYPE_TITLE);
        TypeArticle lTypePC = readTypeArticle(lTypePCStr, new PC());
        
        PC.AvailabilityStatus lStatus = null;
        if (UseState.isOnPurchase(lUseState)) {
            lStatus = PC.AvailabilityStatus.New;
        }
        else {
            lStatus = PC.AvailabilityStatus.InUse;
        }
        
        // Create a PC with mandatory fields
        PC lPC = new PC(lAirbusSN, lProductTypePC, lName, lDeparmentInCharge,
                lTypePC, lBusinessAllocation, lBusinessUsage, lInCharge, lOwner,
                lOwnerSiglum, lStatus);
        
        // Set optional fields
        lPC.setAdmin(readField(row, IOConstants.ADMIN_TITLE));
        lPC.setAssignment(readField(row, IOConstants.ASSIGNEMENT_TITLE));
        lPC.setFunction(readField(row, IOConstants.FUNCTION_TITLE));
        lPC.setManufacturerSN(readField(row, IOConstants.MANUFACTURER_SN_TITLE));
        lPC.setPlatform(readField(row, IOConstants.PLATFORM_TITLE));
        
        String lDomainStr = readField(row, IOConstants.DOMAIN_TITLE);
        Domain lDomain = readDomain(lDomainStr, IOConstants.DOMAIN_TITLE);
        lPC.setDomain(lDomain);
        
        String lDefaultOSCompleteName =
                readField(row, IOConstants.DEFAULT_OS_TITLE);
        if (lDefaultOSCompleteName == null) {
            lPC.setDefaultOS(null);
        }
        else {
            Software lDefaultOS =
                    softwareBean.findByCompleteName(lDefaultOSCompleteName);
            if (lDefaultOS != null) {
                if (lDefaultOS.getOperatingSystem()) {
                    lPC.getSoftwares().add(lDefaultOS);
                    lPC.setDefaultOS(lDefaultOS);
                }
                else {
                    String lMsg =
                            MessageBundle.getMessageResource(
                                    IOConstants.SOFTWARE_DEFAULT_NOT_OS,
                                    new Object[] { lDefaultOSCompleteName });
                    throw new ImportException(lMsg, getRownumber(),
                            getColumnIndex(IOConstants.DEFAULT_OS_TITLE));
                }
            }
            else {
                String lMsg =
                        MessageBundle.getMessageResource(
                                IOConstants.SOFTWARE_NOT_FOUND,
                                new Object[] { lDefaultOSCompleteName });
                throw new ImportException(lMsg, getRownumber(),
                        getColumnIndex(IOConstants.DEFAULT_OS_TITLE));
            }
        }
        
        lPC.setNbScreens(lNbScreens);
        
        lPC.setState(lState);
        lPC.setUseState(lUseState);
        
        lPC.setAcquisitionDate(readDateField(row,
                IOConstants.ACQUISITIONDATE_TITLE));
        
        lPC.setComment(lCommentStr);
        
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
        String lContainerDetails =
                readField(row, IOConstants.CONTAINER_DETAILS_TITLE);
        
        Container lContainer =
                readContainer(lPC, lContainerTypeStr, lContainerStr,
                        lContainerDetails);
        
        Location lLocation =
                readLocation(lPC, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        try {
            
            Action.createCreationAction(user, lPC, Constants.CREATION_BY_IMPORT);
            pcBean.create(lPC, lLocation, lContainer);
            
        }
        catch (Exception e) {
            throw new ImportException("CREATION PC IMPOSSIBLE: "
                    + ExceptionUtil.getMessage(e), row.getRowNum(), -1);
        }
    }
    
    private Domain readDomain(String pFieldValue, String pColumnName)
            throws ImportException {
        
        if (StringUtil.isEmptyOrNull(pFieldValue)) {
            return null;
        }
        
        Domain lDomain = valueListBean
                .findAttributeValueListByName(Domain.class, pFieldValue);
        if (lDomain == null) {
            String lMsg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return lDomain;
    }
    
    private User readUser(Row pRow, String pFieldValue, String pColumnName)
            throws ImportException {
        
        User lResult = userBean.findUser(pFieldValue);
        if (lResult == null) {
            String msg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(msg, pRow.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return lResult;
    }

    
    /**
     * Convert a read text to a DepartmentInCharge object.
     * 
     * @param pFieldValue
     *            the read text
     * @param pColumnName
     *            the column name of the cell that was read
     * @return an AttributeValueList object
     */
    private DepartmentInCharge readDepartmentInCharge(String pFieldValue,
            String pColumnName) throws ImportException {
        
        DepartmentInCharge lDepartmentInCharge =
                valueListBean.findAttributeValueListByName(
                        DepartmentInCharge.class, pFieldValue);
        if (lDepartmentInCharge == null) {
            String lMsg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return lDepartmentInCharge;
    }
    
}
