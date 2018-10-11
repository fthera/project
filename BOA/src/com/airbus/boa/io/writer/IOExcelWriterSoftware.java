/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterSoftware
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import com.airbus.boa.entity.article.Software;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.converter.OperatingSystemConverter;

/**
 * Manage writing of a Software into Excel files
 */
public class IOExcelWriterSoftware extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.SoftwareSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param columns
     *            the columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterSoftware(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Software)) {
            throw new ExportException("NOT A SOFTWARE OBJECT");
        }
        
        Software software = (Software) object;
        Row row = getRow();
        
        writeField(row, software.getId(), IOConstants.NUMID_TITLE);
        writeField(row, software.getName(), IOConstants.NAME_TITLE);
        writeField(row, software.getDistribution(),
                IOConstants.DISTRIBUTION_TITLE);
        writeField(row, software.getKernel(), IOConstants.KERNEL_TITLE);
        writeField(row, software.getManufacturer(),
                IOConstants.MANUFACTURER_TITLE);
        writeField(row, OperatingSystemConverter.getAsString(software
                .getOperatingSystem()), IOConstants.OPERATINGSYSTEM_TITLE);
        writeField(row, software.getDescription(),
                IOConstants.DESCRIPTION_TITLE);
        writeField(row, software.getLicence(), IOConstants.LICENCE_TITLE);
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        CellAddress lAddress;
        int lColumn;
        
        lColumn = this.columns.getIndex(IOConstants.NAME_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_SOFTWARE_NAME));
        
        lColumn = this.columns.getIndex(IOConstants.DISTRIBUTION_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress, MessageBundle
                .getMessage(IOConstants.TOOLTIP_SOFTWARE_DISTRIBUTION));
        
        lColumn = this.columns.getIndex(IOConstants.KERNEL_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TOOLTIP_SOFTWARE_KERNEL));
    }
    
}
