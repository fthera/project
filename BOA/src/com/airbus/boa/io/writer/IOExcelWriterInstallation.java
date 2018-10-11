/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterInstallation
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Location;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ValueListController;

/**
 * Manage writing of an Installation into Excel files
 */
public class IOExcelWriterInstallation extends IOExcelWriterBaseWriter implements
        Writer {
    
    private static final String SHEET_NAME = IOConstants.InstallationSheetName;
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the Excel workbook on which to write
     * @param pChosenColumns
     *            the PC columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterInstallation(Workbook pWorkbook, Columns pChosenColumns)
            throws ExportException {
        super(pWorkbook, pChosenColumns, SHEET_NAME);
        
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        if (!(pObject instanceof Installation)) {
            throw new ExportException("PAS UNE INSTALLATION");
        }
        
        Installation lInstallation = (Installation) pObject;
        Row lRow = getRow();
        writeField(lRow, lInstallation.getId(), IOConstants.NUMID_TITLE);
        writeField(lRow, lInstallation.getName(), IOConstants.NAME_TITLE);
        
        writeField(lRow, lInstallation.getComments(),
                IOConstants.COMMENTS_TITLE);
        writeField(lRow, lInstallation.getBusinessSiglum(),
                IOConstants.BUSINESS_SIGLUM_TITLE);
        writeField(lRow, lInstallation.getAircraftProgram().getDefaultValue(),
                IOConstants.AIRCRAFTPROGRAM_TITLE);
        writeField(lRow, lInstallation.getPersonInCharge().getLoginDetails(),
                IOConstants.TECHNICAL_CONTACT_TITLE);
        writeField(lRow, lInstallation.getUser(),
        		IOConstants.INSTALLATION_USER);
        writeField(lRow, lInstallation.getStartingDate(),
                IOConstants.STARTING_DATE_TITLE);
        
        Location lLocation = lInstallation.getLocation();
        
        if (lLocation != null) {
            writeField(lRow, lInstallation.getLocation().getLocationName(),
                    IOConstants.LOCATION_TITLE);
        }
        else {
            writeField(lRow, "", IOConstants.LOCATION_TITLE);
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
        
        lColumn = this.columns.getIndex(IOConstants.NAME_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_INST_NAME));
        
        lColumn = this.columns.getIndex(IOConstants.COMMENTS_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_INST_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.BUSINESS_SIGLUM_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress, MessageBundle
                .getMessage(IOConstants.TOOLTIP_INST_BUSINESS_SIGLUM));
        
        lColumn = this.columns.getIndex(IOConstants.PRODUCT_TYPE_TITLE);
        
        lColumn = this.columns.getIndex(IOConstants.AIRCRAFTPROGRAM_TITLE);
        lAddress = new CellAddress(0, lColumn);
        lValueList = lValueListBean.getAllValues(AircraftProgram.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_INST_PROGRAM));
        
        lColumn = this.columns.getIndex(IOConstants.TECHNICAL_CONTACT_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_INST_INCHARGE)
                        + "\n" + MessageBundle
                                .getMessage(IOConstants.TEMPLATE_TC_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.INSTALLATION_USER);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_INST_USER));
    }
}
