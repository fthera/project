/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterTool
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage writing of a tool into Excel files
 */
public class IOExcelWriterTool extends IOExcelWriterBaseWriter implements Writer {
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the workbook on which to write
     * @param pColumns
     *            the columns
     * @throws ExportException
     *             when the workbook is null
     */
    public IOExcelWriterTool(Workbook pWorkbook, Columns pColumns)
            throws ExportException {
        
        super(pWorkbook, pColumns, IOConstants.TOOL_SHEET_NAME);
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        
        if (!(pObject instanceof Tool)) {
            throw new ExportException("OBJECT IS NOT A TOOL");
        }
        
        Tool lTool = (Tool) pObject;
        Row lRow = getRow();
        
        writeField(lRow, lTool.getDesignation(), IOConstants.DESIGNATION_TITLE);
        
        writeIntegerField(lRow, lTool.getId(), IOConstants.NUMID_TITLE);
        writeField(lRow, lTool.getName(), IOConstants.NAME_TITLE);
        writeField(lRow, lTool.getPersonInCharge(), IOConstants.TECHNICAL_CONTACT_TITLE);
        
        writeField(lRow, lTool.getLoanDate(), IOConstants.LOAN_DATE_TITLE);
        writeField(lRow, lTool.getLoanDueDate(),
                IOConstants.LOAN_DUE_DATE_TITLE);
        
        if (lTool.getGeneralComment() != null) {
            
            writeField(lRow, lTool.getGeneralComment(),
                    IOConstants.GENERAL_COMMENT_TITLE, Styles.WRAPTEXT);
        }
        else {
            writeField(lRow, "", IOConstants.GENERAL_COMMENT_TITLE);
        }
        
        // Location
        writeLocation(lRow, lTool.getLocation());
        
        // Container
        writeContainer(lRow, lTool.getContainer(), null);
        
        // Master container
        writeMasterContainer(lRow, lTool.getMasterContainer());
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        CellAddress lAddress;
        int lColumn;
        
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
    }
}
