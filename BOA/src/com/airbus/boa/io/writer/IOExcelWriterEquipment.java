/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterEquipment
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

public class IOExcelWriterEquipment extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.EquipmentSheetName;
    
    private Object parent;
    
    public IOExcelWriterEquipment(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Article) || !(parent instanceof Software)) {
            throw new ExportException("NOT AN EQUIPMENT OBJECT");
        }
        
        Article article = (Article) object;
        Row row = getRow();
        
        writeField(row, article.getClass().getSimpleName(),
                IOConstants.FOR_ARTICLE_TITLE);
        if (article instanceof PC) {
            writeField(row, ((PC) article).getAirbusSN(),
                    IOConstants.AIRBUS_SN_TITLE);
            writeField(row, "", IOConstants.MANUFACTURER_SN_TITLE);
        }
        else {
            writeField(row, article.getAirbusSN(), IOConstants.AIRBUS_SN_TITLE);
            writeField(row, article.getManufacturerSN(),
                    IOConstants.MANUFACTURER_SN_TITLE);
        }
        
        writeField(row, ((Software) parent).getCompleteName(),
                IOConstants.SOFTWARE_TITLE);
    }
    
    public void setParent(Object parent) {
        this.parent = parent;
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        int lColumn = this.columns.getIndex(IOConstants.FOR_ARTICLE_TITLE);
        String[] lValues = new String[] { Board.class.getSimpleName(),
                PC.class.getSimpleName() };
        ExcelUtils.setColumnValidation(sheet, lColumn, lValues);
    }
    
}
