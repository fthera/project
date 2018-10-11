/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterCabinet
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a cabinet into Excel files
 */
public class IOExcelWriterCabinet extends IOExcelWriterAbstractArticle {
    
    private static final String sheetname = IOConstants.CabinetSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param pColumns
     *            the PC columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterCabinet(Workbook workbook, Columns pColumns)
            throws ExportException {
        super(workbook, pColumns, sheetname);
        
    }
    
    @Override
    public void write(Object object) throws ExportException {
        
        if (!(object instanceof Cabinet)) {
            throw new ExportException("PAS UN TYPE d'ARTICLE");
        }
        
        Cabinet cabinet = (Cabinet) object;
        Row row = getRow();
        writeField(row, cabinet.getDesignation(), IOConstants.DESIGNATION_TITLE);
        
        // Location
        writeLocation(row, cabinet.getLocation());
        
        // Container
        writeContainer(row, cabinet.getContainer(),
                IOConstants.IDENTIFICATION_LETTER_OR_PRECISION_TITLE);
        
        write(row, cabinet);
    }
    
}
