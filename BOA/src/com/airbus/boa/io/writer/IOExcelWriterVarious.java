/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterVarious
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Various;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a various into Excel files
 */
public class IOExcelWriterVarious extends IOExcelWriterAbstractArticle {
    
    private static final String sheetname = IOConstants.VariousSheetName;
    
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
    public IOExcelWriterVarious(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Various)) {
            throw new ExportException("NOT A VARIOUS");
        }
        Row row = getRow();
        
        Various various = (Various) object;
        write(row, various);
        
        // Location
        writeLocation(row, various.getLocation());
        
        // Container
        writeContainer(row, various.getContainer(),
                IOConstants.CONTAINER_DETAILS_TITLE);
    }
    
}
