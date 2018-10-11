/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterInstalledSoftware
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

public class IOExcelWriterInstalledSoftware extends IOExcelWriterBaseWriter {
    
    private static final String sheetname =
            IOConstants.InstalledSoftwareSheetName;
    
    private Object parent;
    
    public IOExcelWriterInstalledSoftware(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof Software)) {
            throw new ExportException("NOT A SOFTWARE OBJECT");
        }
        
        Software soft = (Software) object;
        Row row = getRow();
        
        writeField(row, ((PC) parent).getAirbusSN(),
                IOConstants.AIRBUS_SN_TITLE);
        writeField(row, ((PC) parent).getName(), IOConstants.NAME_TITLE);
        
        writeField(row, soft.getCompleteName(), IOConstants.SOFTWARE_TITLE);
    }
    
    public void setParent(Object parent) {
        this.parent = parent;
    }
    
}
