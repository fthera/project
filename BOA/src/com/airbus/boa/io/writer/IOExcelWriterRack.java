/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterRack
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a rack into Excel files
 */
public class IOExcelWriterRack extends IOExcelWriterAbstractArticle {
    
    private static final String sheetname = IOConstants.RackSheetName;
    
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
    public IOExcelWriterRack(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Rack)) {
            throw new ExportException("NOT A RACK OBJECT");
        }
        
        Rack rack = (Rack) object;
        Row row = getRow();
        
        writeField(row, rack.getDesignation(), IOConstants.DESIGNATION_TITLE);
        
        // Location
        writeLocation(row, rack.getLocation());
        
        // Container
        writeContainer(row, rack.getContainer(),
                IOConstants.RACK_POSITION_PRECISION_TITLE);
        
        write(row, rack);
    }
}
