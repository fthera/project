/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterAbstractArticle
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage writing of an Article into Excel files
 */
public abstract class IOExcelWriterAbstractArticle extends IOExcelWriterBaseWriter {
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param columns
     *            the PC columns into Excel files
     * @param sheetname
     *            the sheet name on which to write
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterAbstractArticle(Workbook workbook, Columns columns,
            String sheetname) throws ExportException {
        super(workbook, columns, sheetname);
        
    }
    
    /**
     * Write the article common attributes of provided article
     * 
     * @param row
     *            the current row
     * @param article
     *            the article
     */
    public void write(Row row, Article article) {
        writeIntegerField(row, article.getId(), IOConstants.NUMID_TITLE);
        writeField(row, article.getAirbusSN(), IOConstants.AIRBUS_SN_TITLE);
        writeField(row, article.getManufacturerSN(),
                IOConstants.MANUFACTURER_SN_TITLE);
        writeField(row, article.getTypeArticle(), IOConstants.TYPE_TITLE);
        writeField(row, article.getAirbusPN(), IOConstants.AIRBUS_PN_TITLE);
        writeField(row, article.getManufacturerPN(),
                IOConstants.MANUFACTURER_PN_TITLE);
        writeField(row, article.getCmsCode(), IOConstants.CMSCODE_TITLE);
        writeField(row, article.getState().getStringValue(),
                IOConstants.STATE_TITLE);
        writeField(row, article.getUseState().getStringValue(),
                IOConstants.USE_STATE_TITLE);
        writeField(row, article.getAcquisitionDate(),
                IOConstants.ACQUISITIONDATE_TITLE);
        if (article.getHistory() != null
                && article.getHistory().getGeneralComment() != null) {
            
            writeField(row, article.getHistory().getGeneralComment(),
                    IOConstants.GENERAL_COMMENT_TITLE, Styles.WRAPTEXT);
        }
        else {
            writeField(row, "", IOConstants.GENERAL_COMMENT_TITLE);
        }
        
        writeMasterContainer(row, article.getMasterContainer());
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        CellAddress lAddress;
        int lColumn;
        
        lColumn = this.columns.getIndex(IOConstants.STATE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, ArticleState.values());
        
        lColumn = this.columns.getIndex(IOConstants.USE_STATE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, UseState.values());
        
        lColumn = this.columns.getIndex(IOConstants.AIRBUS_SN_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_SN_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.MANUFACTURER_SN_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_SN_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.AIRBUS_PN_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_PN_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.MANUFACTURER_PN_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_PN_COMMENT));
        
        lColumn = this.columns.getIndex(IOConstants.CMSCODE_TITLE);
        lAddress = new CellAddress(0, lColumn);
        ExcelUtils.setComment(workbook, sheet, lAddress,
                MessageBundle.getMessage(IOConstants.TEMPLATE_PN_COMMENT));
        
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
        
        lColumn = this.columns.getIndex(IOConstants.CONTAINER_TYPE_TITLE);
        String[] lValues = new String[] { Board.class.getSimpleName(),
                Cabinet.class.getSimpleName(), PC.class.getSimpleName(),
                Rack.class.getSimpleName(), Switch.class.getSimpleName(),
                Various.class.getSimpleName(), Tool.class.getSimpleName(),
                Installation.class.getSimpleName() };
        ExcelUtils.setColumnValidation(sheet, lColumn, lValues);
    }
}
