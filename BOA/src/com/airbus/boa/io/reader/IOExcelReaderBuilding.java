/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderBuilding
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Place.PlaceType;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnBuilding;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage reading of a Building, Place or External Entity into Excel files
 */
public class IOExcelReaderBuilding extends IOExcelBaseReader {
    
    private static String sheetname = IOConstants.BuildingSheetName;
    private LocationBean locationBean;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook to read
     * @param columns
     *            the columns to read
     * @param locationBean
     *            the locationBean to use
     * @param valueListBean
     *            the ValueListBean to use
     */
    public IOExcelReaderBuilding(Workbook workbook, Columns columns,
            LocationBean locationBean, ValueListBean valueListBean) {
        super(valueListBean, workbook, columns, sheetname);
        if (columns == null) {
            this.columns = new ColumnBuilding();
        }
        
        this.locationBean = locationBean;
    }
    
    @Override
    public void readLine() throws ImportException {
        
        String type = readField(row, IOConstants.TYPE_TITLE);
        String name = readField(row, IOConstants.NAME_TITLE);
        String parent = readField(row, IOConstants.PARENT_TITLE);
        
        if (type == null || name == null) {
            
            throw new ImportException("TYPE OU NAME INCORRECT",
                    row.getRowNum(), columns.getIndex(IOConstants.TYPE_TITLE));
        }
        
        if (type.matches(IOConstants.REGEX_MIGRATION_BUILDING)) {
            
            readBuilding(name);
            
        }
        else if (readPlaceType(type) != null) {
            
            readPlace(type, name, parent);
            
        }
        else {
            
            readExternalEntity(type, name);
        }
    }
    
    private void readPlace(String type, String name, String parent)
            throws ImportException {
        
        PlaceType placetype = readPlaceType(type);
        if (placetype == null) {
            throw new ImportException("UNKNOWN TYPE :" + type, row.getRowNum(),
                    columns.getIndex(IOConstants.TYPE_TITLE));
        }
        
        Place place = new Place(name, placetype);
        
        if (parent == null) {
            throw new ImportException("PARENT IS MANDATORY", row.getRowNum(),
                    columns.getIndex(IOConstants.PARENT_TITLE));
        }
        
        Building building = locationBean.findBuildingByName(parent);
        if (building == null) {
            
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.ENTITY_NOT_FOUND,
                            new Object[] { row.getRowNum() + 1, "Building",
                                    parent });
            throw new ImportException(msg, row.getRowNum(),
                    columns.getIndex(IOConstants.PARENT_TITLE));
            
        }
        // Check that place does not already exist
        if (locationBean.findPlaceByNameAndBuilding(name, building) != null) {
            throw new ImportException(
                    MessageBundle.getMessage(Constants.PLACENAME_ALREADY_USED),
                    row.getRowNum(), -1);
        }
        
        try {
            place = locationBean.createPlace(place, building);
        }
        catch (Exception e) {
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION PLACE IMPOSSIBLE : " + msg,
                    row.getRowNum(), -1);
        }
    }
    
    private void readExternalEntity(String pType, String name)
            throws ImportException {
        
        checkNotEmptyField(row, pType, IOConstants.TYPE_TITLE);
        ExternalEntityType lExternalEntityType =
                readValueList(row, pType, IOConstants.TYPE_TITLE,
                        ExternalEntityType.class);
        
        ExternalEntity lExternalEntity =
                new ExternalEntity(name, lExternalEntityType);
        
        try {
            lExternalEntity =
                    locationBean.createExternalEntity(lExternalEntity);
        }
        catch (Exception e) {
            throw new ImportException("CREATION EXTERNAL ENTITY IMPOSSIBLE:",
                    row.getRowNum(), -1);
        }
    }
    
    private void readBuilding(String name) throws ImportException {
        Building building = new Building();
        building.setName(name);
        try {
            building = locationBean.createBuilding(building);
        }
        catch (Exception e) {
            throw new ImportException("CREATION BUILDING IMPOSSIBLE : "
                    + ExceptionUtil.getMessage(e), row.getRowNum(), -1);
        }
    }
    
    private PlaceType readPlaceType(String value) {
        if (value == null) {
            return null;
        }
        if (value.trim().matches(IOConstants.REGEX_MIGRATION_BUILDING_LABO)) {
            return PlaceType.Laboratory;
        }
        if (value.trim().matches(IOConstants.REGEX_MIGRATION_STOREROOM)) {
            return PlaceType.Storeroom;
        }
        
        if (value.trim().matches(IOConstants.REGEX_MIGRATION_ROOM)) {
            return PlaceType.Room;
        }
        
        return null;
        
    }
    
}
