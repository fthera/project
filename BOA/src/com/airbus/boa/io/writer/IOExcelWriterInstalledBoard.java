/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterInstalledBoard
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

public class IOExcelWriterInstalledBoard extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.InstalledBoardSheetName;
    
    private Object parent;
    
    public IOExcelWriterInstalledBoard(Workbook workbook, Columns columns)
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
        
        writeField(row, ((PC) parent).getAirbusSN(),
                IOConstants.AIRBUS_SN_TITLE);
        writeField(row, ((PC) parent).getName(), IOConstants.NAME_TITLE);
        
        writeField(row, board.getAirbusSN(), IOConstants.BOARD_AIRBUS_SN_TITLE);
        writeField(row, board.getManufacturerSN(),
                IOConstants.MANUFACTURER_SN_TITLE);
        writeField(row, board.getTypeArticle(), IOConstants.TYPE_TITLE);
        writeField(row, board.getContainerOrmPC().getSlotNumber(),
                IOConstants.SLOTNUMBERPOSITION_TITLE);
        
    }
    
    public void setParent(Object parent) {
        this.parent = parent;
    }
    
}
