/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterDatedComments
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a Dated comments into Excel files
 */
public class IOExcelWriterDatedComments extends IOExcelWriterBaseWriter {
    
    private static final String sheetname =
            IOConstants.DatedCommentsSheetName;
    
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
    public IOExcelWriterDatedComments(Workbook pWorkbook, Columns pColumns)
            throws ExportException {
        super(pWorkbook, pColumns, sheetname);
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        if (!(pObject instanceof DatedComment)) {
            throw new ExportException("NOT A DatedComment OBJECT");
        }
        
        DatedComment lDatedComment = (DatedComment) pObject;
        Row lRow = getRow();
        
        writeField(lRow, lDatedComment.getArticle().getAirbusSN(),
                IOConstants.AIRBUS_SN_TITLE);
        writeField(lRow, lDatedComment.getComment(), IOConstants.COMMENT_TITLE);
        writeField(lRow, lDatedComment.getUser().getLoginDetails(),
                IOConstants.USER_TITLE);
        writeField(lRow, lDatedComment.getDate(), IOConstants.DATE_TITLE);
    }
    
}
