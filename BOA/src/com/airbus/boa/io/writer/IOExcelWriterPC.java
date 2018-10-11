/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterPC
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ValueListController;

/**
 * Manage writing of a PC into Excel files
 */
public class IOExcelWriterPC extends IOExcelWriterBaseWriter {
    
    private static final String SHEET_NAME = IOConstants.PCSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param columns
     *            the PC columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterPC(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, SHEET_NAME);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        
        if (!(object instanceof PC)) {
            throw new ExportException("NOT A PC OBJECT");
        }
        
        PC lPC = (PC) object;
        final Row lRow = getRow();
        
        writeField(lRow, lPC.getId(), IOConstants.NUMID_TITLE);
        
        writeField(lRow, lPC.getAirbusSN(), IOConstants.AIRBUS_SN_TITLE);
        
        writeField(lRow, lPC.getManufacturerSN(),
                IOConstants.MANUFACTURER_SN_TITLE);
        
        writeField(lRow, lPC.getName(), IOConstants.NAME_TITLE);
        
        if (lPC.getDepartmentInCharge() == null) {
            writeField(lRow, "", IOConstants.DEPARTMENTINCHARGE_TITLE);
        }
        else {
            writeField(lRow, lPC.getDepartmentInCharge().getDefaultValue(),
                    IOConstants.DEPARTMENTINCHARGE_TITLE);
        }
        
        if (lPC.getProductType() == null) {
            writeField(lRow, "", IOConstants.PRODUCT_TYPE_TITLE);
        }
        else {
            writeField(lRow, lPC.getProductType().getLocaleValue(),
                    IOConstants.PRODUCT_TYPE_TITLE);
        }
        
        writeField(lRow, lPC.getTypeArticle().getLabel(),
                IOConstants.TYPE_TITLE);
        
        writeField(lRow, lPC.getFunction(), IOConstants.FUNCTION_TITLE);
        
        if (lPC.getDomain() == null) {
            writeField(lRow, "", IOConstants.DOMAIN_TITLE);
        }
        else {
            writeField(lRow, lPC.getDomain().getDefaultValue(),
                    IOConstants.DOMAIN_TITLE);
        }
        
        writeField(lRow, lPC.getPlatform(), IOConstants.PLATFORM_TITLE);
        
        writeField(lRow, lPC.getInCharge().getLoginDetails(),
                IOConstants.PC_TECHNICAL_CONTACT_TITLE);
        
        writeField(lRow, lPC.getOwner(), IOConstants.OWNER_TITLE);
        
        writeField(lRow, lPC.getOwnerSiglum(), IOConstants.OWNER_SIGLUM_TITLE);
        
        writeField(lRow, lPC.getState().getStringValue(),
                IOConstants.STATE_TITLE);
        
        writeField(lRow, lPC.getUseState().getStringValue(),
                IOConstants.USE_STATE_TITLE);
        
        writeField(lRow, lPC.getAcquisitionDate(),
                IOConstants.ACQUISITIONDATE_TITLE);
        
        writeField(lRow, lPC.getAdmin(), IOConstants.ADMIN_TITLE);
        
        if (lPC.getDefaultOS() == null) {
            writeField(lRow, "", IOConstants.DEFAULT_OS_TITLE);
        }
        else {
            writeField(lRow, lPC.getDefaultOS().getCompleteName(),
                    IOConstants.DEFAULT_OS_TITLE);
        }
        
        writeField(lRow, lPC.getNbScreens(), IOConstants.NB_SCREENS_TITLE);
        
        writeField(lRow, lPC.getComment(), IOConstants.COMMENT_TITLE);
        
        if (lPC.getAllocation() == null) {
            writeField(lRow, "", IOConstants.BUSINESS_ALLOC_TITLE);
        }
        else {
            writeField(lRow, lPC.getAllocation().getLocaleValue(),
                    IOConstants.BUSINESS_ALLOC_TITLE);
        }
        
        if (lPC.getUsage() == null) {
            writeField(lRow, "", IOConstants.BUSINESS_USAGE_TITLE);
        }
        else {
            writeField(lRow, lPC.getUsage().getLocaleValue(),
                    IOConstants.BUSINESS_USAGE_TITLE);
        }
        
        writeField(lRow, lPC.getAssignment(), IOConstants.ASSIGNEMENT_TITLE);
        
        // Location
        writeLocation(lRow, lPC.getLocation());
        
        // Container
        writeContainer(lRow, lPC.getContainer(),
                IOConstants.CONTAINER_DETAILS_TITLE);
        
        // Master container
        writeMasterContainer(lRow, lPC.getMasterContainer());
        
        // Business Siglum, if the master container is an installation
        if ((lPC.getMasterContainer() != null)
                && (lPC.getMasterContainer().getContainerItem() instanceof Installation)) {
            Installation lInstallation =
                    (Installation) lPC.getMasterContainer().getContainerItem();
            writeField(lRow, lInstallation.getBusinessSiglum(),
                    IOConstants.CONTAINER_BUSINESS_SIGLUM);
            writeField(lRow, lInstallation.getUser(),
                    IOConstants.INSTALLATION_USER);
            writeField(lRow, lInstallation.getPersonInCharge()
                    .getLoginDetails(), IOConstants.CONTAINER_TECHNICAL_CONTACT);
        }
        else {
            writeField(lRow, "", IOConstants.CONTAINER_BUSINESS_SIGLUM);
            writeField(lRow, "", IOConstants.INSTALLATION_USER);
            writeField(lRow, "", IOConstants.CONTAINER_TECHNICAL_CONTACT);
        }
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        ValueListController lController =
                AbstractController.findBean(ValueListController.class);
        ValueListBean lValueListBean = lController.getValueListBean();
        
        CellAddress lAddress;
        int lColumn;
        List<String> lValueList;
        
        lColumn = this.columns.getIndex(IOConstants.AIRBUS_SN_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_ASN));
        
        lColumn = this.columns.getIndex(IOConstants.NAME_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_NAME));

        lColumn = this.columns.getIndex(IOConstants.DEPARTMENTINCHARGE_TITLE);
        lAddress = new CellAddress(0, lColumn);
        lValueList = lValueListBean.getAllValues(DepartmentInCharge.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        ExcelUtils.setComment(workbook, sheet, lAddress, MessageBundle
                .getMessage(IOConstants.TOOLTIP_PC_DEPARTMENTINCHARGE));
        
        lColumn = this.columns.getIndex(IOConstants.PRODUCT_TYPE_TITLE);
        lValueList = lValueListBean.getAllValues(ProductTypePC.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.FUNCTION_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_FUNCTION));
        
        lColumn = this.columns.getIndex(IOConstants.DOMAIN_TITLE);
        lValueList = lValueListBean.getAllValues(Domain.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.PLATFORM_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_PLATFORM));
        
        lColumn = this.columns.getIndex(IOConstants.PC_TECHNICAL_CONTACT_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_INCHARGE) + "\n"
                        + MessageBundle
                                .getMessage(IOConstants.TEMPLATE_TC_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.OWNER_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_OWNER));
        
        lColumn = this.columns.getIndex(IOConstants.OWNER_SIGLUM_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_PC_OWNERSIGLUM));
        
        lColumn = this.columns.getIndex(IOConstants.STATE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, ArticleState.values());
        
        lColumn = this.columns.getIndex(IOConstants.USE_STATE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, UseState.values());
        
        lColumn = this.columns.getIndex(IOConstants.BUSINESS_ALLOC_TITLE);
        lValueList = lValueListBean.getAllValues(BusinessAllocationPC.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.BUSINESS_USAGE_TITLE);
        lValueList = lValueListBean.getAllValues(BusinessUsagePC.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.INHERITED_LOCATION_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setColumnValidation(sheet, lColumn,
                new String[] { "yes", "no" });
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_LOC_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.LOCATION_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_LOC_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.CONTAINER_TYPE_TITLE);
        String[] lValues = new String[] { Board.class.getSimpleName(),
                Cabinet.class.getSimpleName(), PC.class.getSimpleName(),
                Rack.class.getSimpleName(), Switch.class.getSimpleName(),
                Various.class.getSimpleName(), Tool.class.getSimpleName(),
                Installation.class.getSimpleName() };
        ExcelUtils.setColumnValidation(sheet, lColumn, lValues);
    }
}
