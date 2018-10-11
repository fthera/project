/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterExternalEntity
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

public class IOExcelWriterExternalEntity extends IOExcelWriterBaseWriter implements
        Downloadable {
    
    private static final String SHEET_NAME =
            IOConstants.EXTERNAL_ENTITY_SHEET_NAME;
    
    public IOExcelWriterExternalEntity(Workbook pWorkbook, Columns pColumns)
            throws ExportException {
        super(pWorkbook, pColumns, SHEET_NAME);
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        Row lRow = getRow();
        
        writeField(lRow, ((ExternalEntity) pObject).getName(),
                IOConstants.NAME_TITLE);
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.Downloadable#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream pOs) throws IOException {
        if (workbook != null) {
            workbook.write(pOs);
        }
    }
    
}
