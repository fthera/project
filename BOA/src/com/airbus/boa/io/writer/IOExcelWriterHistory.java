/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterHistory
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage writing of History into Excel files
 */
public class IOExcelWriterHistory extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.HistorySheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook on which to write
     * @param columns
     *            the columns
     * @throws ExportException
     *             when the workbook is null
     */
    public IOExcelWriterHistory(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Article)) {
            throw new ExportException("PAS UN ARTICLE");
            
        }
        Article article = (Article) object;
        Iterator<Action> iteratorAction =
                article.getHistory().getActions().iterator();
        while (iteratorAction.hasNext()) {
            Action action = iteratorAction.next();
            Row row = getRow();
            writeField(row, article.getClass().getSimpleName(),
                    IOConstants.CLASS_ARTICLE_TITLE);
            writeField(row, article.getAirbusSN(), IOConstants.AIRBUS_SN_TITLE);
            writeField(row, article.getManufacturerSN(),
                    IOConstants.MANUFACTURER_SN_TITLE);
            writeField(row, article.getTypeArticle().getLabel(),
                    IOConstants.TYPE_TITLE);
            
            String designation = null;
            if (article instanceof Rack) {
                designation = ((Rack) article).getDesignation();
            }
            else if (article instanceof Cabinet) {
                designation = ((Cabinet) article).getDesignation();
            }
            writeField(row, designation, IOConstants.DESIGNATION_TITLE);
            writeField(row, action.getLogin(), IOConstants.LOGIN_TITLE);
            writeField(row, action.getAuthor(), IOConstants.AUTHOR_TITLE);
            writeField(row, MessageBundle.getMessage(action.getLabel()),
                    IOConstants.ACTION_TITLE);
            writeField(row, action.getDate(), IOConstants.DATE_TITLE,
                    Styles.DATE_HHMM);
            writeField(row, action.getComment(), IOConstants.COMMENTS_TITLE);
            
            if (action instanceof FieldModification) {
                writeFieldModification(row, (FieldModification) action);
            }
            else {
                writeField(row, null, IOConstants.BEFORE_VALUE_TITLE);
                writeField(row, null, IOConstants.AFTER_VALUE_TITLE);
                writeField(row, null, IOConstants.MODIFIED_FIELD_TITLE);
            }
            if (iteratorAction.hasNext()) {
                rowNumber++;
            }
        }
        
    }
    
    private void writeFieldModification(Row row, FieldModification fieldmodif) {
        writeField(row, fieldmodif.getBeforeValue(),
                IOConstants.BEFORE_VALUE_TITLE);
        writeField(row, fieldmodif.getAfterValue(),
                IOConstants.AFTER_VALUE_TITLE);
        if (fieldmodif.getField() != null) {
            writeField(row, MessageBundle.getMessage(fieldmodif.getField()),
                    IOConstants.MODIFIED_FIELD_TITLE);
        }
        else {
            writeField(row, "", IOConstants.MODIFIED_FIELD_TITLE);
        }
    }
    
}
