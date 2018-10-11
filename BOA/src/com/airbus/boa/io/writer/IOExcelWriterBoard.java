/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterBoard
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a Board into Excel files
 */
public class IOExcelWriterBoard extends IOExcelWriterAbstractArticle {
    
    private static final String sheetname = IOConstants.BoardSheetName;
    
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
    public IOExcelWriterBoard(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Board)) {
            throw new ExportException("NOT A BOARD OBJECT");
        }
        
        Board board = (Board) object;
        Row row = getRow();
        
        writeField(row, board.getBootLoader(), IOConstants.BOOT_LOADER_TITLE);
        writeField(row, board.getActiveStockControlDate(),
                IOConstants.ACTIVE_STOCK_CONTROL_DATE_TITLE);
        
        if (board.getManufacturerPN() != null) {
            writeField(row, board.getRevH(), IOConstants.REV_H_TITLE);
            writeField(row, board.getRevS(), IOConstants.REV_S_TITLE);
        }
        else {
            writeField(row, "", IOConstants.REV_H_TITLE);
            writeField(row, "", IOConstants.REV_S_TITLE);
        }
        
        if (board.getCalibrated() != null) {
            String yesNo = (board.getCalibrated() ? "oui" : "non");
            writeField(row, yesNo, IOConstants.CALIBRATION_TITLE);
        }
        else {
            writeField(row, "", IOConstants.CALIBRATION_TITLE);
            
        }
        
        // Location
        writeLocation(row, board.getLocation());
        
        // Container
        writeContainer(row, board.getContainer(),
                IOConstants.SLOTNUMBERPOSITION_OR_PRECISION_TITLE);
        
        // Write attributes common with all Articles
        write(row, board);
    }
    
}
