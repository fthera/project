/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterBuilding
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

/**
 * Manage writing of a Building, Place and ExternalEntity into Excel files
 */
public class IOExcelWriterBuilding extends IOExcelWriterBaseWriter {
    
    private static final String sheetname = IOConstants.BuildingSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook on which to write
     * @param columns
     *            the PC columns into Excel files
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterBuilding(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        Row row = getRow();
        
        if (object instanceof Building) {
            writeBuilding(row, (Building) object);
        }
        else if (object instanceof ExternalEntity) {
            writeExternalEntity(row, (ExternalEntity) object);
        }
        else if (object instanceof Place) {
            writePlace(row, (Place) object);
        }
        
    }
    
    private void writePlace(Row row, Place place) {
        writeField(row, place.getName(), IOConstants.NAME_TITLE);
        writeField(row, place.getType().toString(), IOConstants.TYPE_TITLE);
        
        if (place.getBuilding() == null) {
            writeErrorField(row, "VIDE", IOConstants.PARENT_TITLE);
        }
        writeField(row, place.getBuilding().getName(), IOConstants.PARENT_TITLE);
        
    }
    
    private void writeExternalEntity(Row row, ExternalEntity pExternalEntity) {
        writeField(row, pExternalEntity.getName(), IOConstants.NAME_TITLE);
        writeField(row, pExternalEntity.getExternalEntityType()
                .getDefaultValue(), IOConstants.TYPE_TITLE);
        writeField(row, "NA", IOConstants.PARENT_TITLE);
    }
    
    private void writeBuilding(Row row, Building building) {
        writeField(row, building.getName(), IOConstants.NAME_TITLE);
        writeField(row, building.getClass().getSimpleName(),
                IOConstants.TYPE_TITLE);
        writeField(row, "NA", IOConstants.PARENT_TITLE);
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        int lColumn = this.columns.getIndex(IOConstants.TYPE_TITLE);
        ExcelUtils.setColumnValidation(sheet, lColumn, PlaceType.values());
    }
}
