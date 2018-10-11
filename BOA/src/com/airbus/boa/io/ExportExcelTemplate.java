/*
 * ------------------------------------------------------------------------
 * Class : ExportExcelTemplate
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;

import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.column.ColumnBoard;
import com.airbus.boa.io.column.ColumnBuilding;
import com.airbus.boa.io.column.ColumnCabinet;
import com.airbus.boa.io.column.ColumnEquipment;
import com.airbus.boa.io.column.ColumnInstallation;
import com.airbus.boa.io.column.ColumnObsolescence;
import com.airbus.boa.io.column.ColumnPC;
import com.airbus.boa.io.column.ColumnPort;
import com.airbus.boa.io.column.ColumnRack;
import com.airbus.boa.io.column.ColumnSoftware;
import com.airbus.boa.io.column.ColumnSwitch;
import com.airbus.boa.io.column.ColumnTool;
import com.airbus.boa.io.column.ColumnType;
import com.airbus.boa.io.column.ColumnVarious;
import com.airbus.boa.io.obso.writer.IOExcelWriterObsolescence;
import com.airbus.boa.io.writer.IOExcelWriterBoard;
import com.airbus.boa.io.writer.IOExcelWriterBuilding;
import com.airbus.boa.io.writer.IOExcelWriterCabinet;
import com.airbus.boa.io.writer.IOExcelWriterCommunicationPort;
import com.airbus.boa.io.writer.IOExcelWriterEquipment;
import com.airbus.boa.io.writer.IOExcelWriterInstallation;
import com.airbus.boa.io.writer.IOExcelWriterPC;
import com.airbus.boa.io.writer.IOExcelWriterRack;
import com.airbus.boa.io.writer.IOExcelWriterSoftware;
import com.airbus.boa.io.writer.IOExcelWriterSwitch;
import com.airbus.boa.io.writer.IOExcelWriterTool;
import com.airbus.boa.io.writer.IOExcelWriterType;
import com.airbus.boa.io.writer.IOExcelWriterVarious;
import com.airbus.boa.io.writer.Writer;

/**
 * Class building an export template in Excel file format
 */
public class ExportExcelTemplate extends ExportExcelBase {
    
    /**
     * Constructor
     * 
     * @param pVersion
     *            the Excel file version to export
     */
    public ExportExcelTemplate(SpreadsheetVersion pVersion) {
        
        super(pVersion);
        
        try {
            Writer[] lWriters =
                    {
                            new IOExcelWriterBoard(workbook, new ColumnBoard()),
                            new IOExcelWriterBuilding(workbook,
                                    new ColumnBuilding()),
                            new IOExcelWriterCabinet(workbook,
                                    new ColumnCabinet()),
                            new IOExcelWriterInstallation(workbook,
                                    new ColumnInstallation()),
                            new IOExcelWriterTool(workbook, new ColumnTool()),
                            new IOExcelWriterRack(workbook, new ColumnRack()),
                            new IOExcelWriterSwitch(workbook,
                                    new ColumnSwitch()),
                            new IOExcelWriterType(workbook, new ColumnType()),
                            new IOExcelWriterVarious(workbook,
                                    new ColumnVarious()),
                            new IOExcelWriterPC(workbook, new ColumnPC()),
                            new IOExcelWriterObsolescence(workbook,
                                    new ColumnObsolescence()),
                            new IOExcelWriterSoftware(workbook,
                                    new ColumnSoftware()),
                            new IOExcelWriterCommunicationPort(workbook,
                                    new ColumnPort()),
                            new IOExcelWriterEquipment(workbook,
                                    new ColumnEquipment())
                    
                    };
            for (Writer lWriter : lWriters) {
                lWriter.writeEmptyTemplate();
            }
            
        }
        catch (ExportException e) {
        }
        applyStyles();
    }
    
    @Override
    public void writeList(List<?> pList) {
        // Nothing is done since file is an empty template
    }
    
}
