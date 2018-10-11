/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterPCSpecificities
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a PC specificities into Excel files
 */
public class IOExcelWriterPCSpecificities extends IOExcelWriterBaseWriter {
    
    private static final String sheetname =
            IOConstants.PCSpecificitiesSheetName;
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the Excel workbook on which to write
     * @param pColumns
     *            the PC columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterPCSpecificities(Workbook pWorkbook, Columns pColumns)
            throws ExportException {
        super(pWorkbook, pColumns, sheetname);
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        if (!(pObject instanceof PCSpecificity)) {
            throw new ExportException("NOT A PCSpecificity OBJECT");
        }
        
        PCSpecificity lPCSpecificity = (PCSpecificity) pObject;
        Row lRow = getRow();
        
        writeField(lRow, lPCSpecificity.getPc().getAirbusSN(),
                IOConstants.AIRBUS_SN_TITLE);
        writeField(lRow, lPCSpecificity.getPc().getName(),
                IOConstants.NAME_TITLE);
        writeField(lRow, lPCSpecificity.getDescription(),
                IOConstants.DESCRIPTION_TITLE);
        writeField(lRow, lPCSpecificity.getContact(),
                IOConstants.CONTACT_TITLE);
    }
    
}
