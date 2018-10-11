/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterType
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a Type into Excel files
 */
public class IOExcelWriterType extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.TypeSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param pColumns
     *            the columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterType(Workbook workbook, Columns pColumns)
            throws ExportException {
        
        super(workbook, pColumns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        
        if (!(object instanceof TypeArticle)) {
            throw new ExportException("PAS UN TYPE d'ARTICLE");
        }
        
        TypeArticle typeArticle = (TypeArticle) object;
        Row row = getRow();
        String forArticle = typeArticle.getClass().getSimpleName();
        int index = forArticle.lastIndexOf("Type");
        
        // TODO RAJOUTER L'erreur directement dans la ligne courante .
        if (index == -1) {
            throw new ExportException("NE RECONNAIT PAS LE TYPE");
        }
        forArticle = forArticle.substring(index + 4, forArticle.length());
        
        writeField(row, forArticle, IOConstants.FOR_ARTICLE_TITLE);
        writeField(row, typeArticle.getLabel(), IOConstants.TYPE_TITLE);
        writeField(row, typeArticle.getManufacturer(),
                IOConstants.MANUFACTURER_TITLE);
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        String[] lValues = new String[] { Board.class.getSimpleName(),
                Cabinet.class.getSimpleName(), PC.class.getSimpleName(),
                Rack.class.getSimpleName(), Switch.class.getSimpleName(),
                Various.class.getSimpleName() };
        int lColumn = this.columns.getIndex(IOConstants.FOR_ARTICLE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValues);
    }
}
