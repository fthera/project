/*
 * ------------------------------------------------------------------------
 * Class : ExportExcelObso
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.obso;

import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;

import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExportExcelBase;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.io.obso.writer.IOExcelWriterObsolescence;
import com.airbus.boa.io.writer.Writer;

/**
 * @author ng0057cf
 */
public class ExportExcelObso extends ExportExcelBase {
    
    private Writer writer;
    
    /**
     * @param version
     */
    public ExportExcelObso(SpreadsheetVersion version, Columns columns,
            int firstIndexInstallationColumns) {
        super(version);
        
        try {
            writer =
                    new IOExcelWriterObsolescence(workbook, columns,
                            firstIndexInstallationColumns);
            
        }
        catch (ExportException e) {
            e.printStackTrace();
        }
        
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.ExportExcelBase#writeList(java.util.List)
     */
    @Override
    public void writeList(List<?> list) {
        writer.write(list);
        writer.applyStyles();
    }
    
}
