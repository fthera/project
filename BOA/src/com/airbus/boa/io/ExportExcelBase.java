/*
 * ------------------------------------------------------------------------
 * Class : ExportExcelBase
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Manage the creation of export Excel files
 */
public abstract class ExportExcelBase implements Downloadable {
    
    /**
     * The workbook on which to write
     */
    protected Workbook workbook;
    
    /**
     * Constructor
     * 
     * @param version
     *            the Excel version of the file to export
     */
    public ExportExcelBase(SpreadsheetVersion version) {
        
        if (version == SpreadsheetVersion.EXCEL2007) {
            workbook = new SXSSFWorkbook(100);
        }
        else {
            workbook = new HSSFWorkbook();
        }
        
        if (workbook.getNumberOfSheets() > 0) {
            workbook.removeSheetAt(0);
        }
    }
    
    @Override
    public void write(OutputStream os) throws IOException {
        workbook.setActiveSheet(0);
        workbook.setSelectedTab(0);
        workbook.write(os);
    }
    
    /**
     * Reorder the sheets into the workbook
     */
    public void applyStyles() {
        
        Integer lPosition = 0;
        
        String[] lCorrectSheetOrder =
                { IOConstants.AttributeValueListSheetName,
                
                IOConstants.BuildingSheetName,
                        IOConstants.InstallationSheetName,
                        IOConstants.TOOL_SHEET_NAME,
                        
                        IOConstants.TypeSheetName,
                        
                        IOConstants.CabinetSheetName,
                        IOConstants.RackSheetName, IOConstants.SwitchSheetName,
                        IOConstants.PCSheetName, IOConstants.BoardSheetName,
                        IOConstants.VariousSheetName,
                        
                        IOConstants.CommunicationPortSheetName,
                        IOConstants.InstalledSoftwareSheetName,
                        IOConstants.InstalledBoardSheetName,
                        
                        IOConstants.SoftwareSheetName,
                        IOConstants.EquipmentSheetName,
                        
                        IOConstants.ObsolescenceDataSheetName,
                        IOConstants.UserSheetName, IOConstants.HistorySheetName };
        
        for (String lCurrentSheetName : lCorrectSheetOrder) {
            lPosition = reorderIfExist(lCurrentSheetName, lPosition);
        }
    }
    
    /**
     * If it exists into the workbook, set the sheet order to the provided
     * position.
     * 
     * @param pSheetName
     *            the name of the sheet to reorder
     * @param pPosition
     *            the position of the sheet
     * @return the position for the next sheet
     */
    private Integer reorderIfExist(String pSheetName, final Integer pPosition) {
        
        if (workbook.getSheet(pSheetName) != null) {
            
            workbook.setSheetOrder(pSheetName, pPosition);
            return pPosition + 1;
        }
        return pPosition;
    }
    
    /**
     * Write a list in the workbook
     * 
     * @param pList
     *            the list to write
     */
    public abstract void writeList(List<?> pList);
    
    /**
     * @return the workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }
    
}
