/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterSwitch
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a switch into Excel files
 */
public class IOExcelWriterSwitch extends IOExcelWriterAbstractArticle {
    
    private static final String sheetname = IOConstants.SwitchSheetName;
    
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
    public IOExcelWriterSwitch(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Switch)) {
            throw new ExportException("NOT A SWITCH OBJECT");
        }
        
        Switch lSwitch = (Switch) object;
        Row row = getRow();
        
        writeField(row, lSwitch.getIpAddress(), IOConstants.IP_ADDRESS_TITLE);
        
        // Location
        writeLocation(row, lSwitch.getLocation());
        
        // Container
        writeContainer(row, lSwitch.getContainer(),
                IOConstants.SWITCH_POSITION_PRECISION_TITLE);
        
        write(row, lSwitch);
    }
    
}
