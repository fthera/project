/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderInstallation
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.Date;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnInstallation;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage reading of an Installation into Excel files
 */
public class IOExcelReaderInstallation extends IOExcelBaseReader {
    
    private static String sheetname = IOConstants.InstallationSheetName;
    private LocationBean locationBean;
    private UserBean userBean;
    private LocationFactory locationFactory;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook to read
     * @param columns
     *            the columns to read
     * @param pLocationFactory
     *            the locationFactory to use
     * @param locationBean
     *            the locationBean to use
     * @param valueListBean
     *            the ValueListBean to use
     * @param pUserBean
     *            the userBean to use
     */
    public IOExcelReaderInstallation(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory, LocationBean locationBean,
            ValueListBean valueListBean, UserBean pUserBean) {
        
        super(valueListBean, workbook, columns, sheetname);
        if (columns == null) {
            this.columns = new ColumnInstallation();
        }
        
        locationFactory = pLocationFactory;
        this.locationBean = locationBean;
        userBean = pUserBean;
    }
    
    @Override
    public void readLine() throws ImportException {
        
        String name = readField(row, IOConstants.NAME_TITLE);
        String lComments = readField(row, IOConstants.COMMENTS_TITLE);
        String lLocationStr = readField(row, IOConstants.LOCATION_TITLE);
        String lBusinessSiglum =
                readField(row, IOConstants.BUSINESS_SIGLUM_TITLE);
        String lAircraftProgramStr =
                readField(row, IOConstants.AIRCRAFTPROGRAM_TITLE);
        Date startingDate = readDateField(row, IOConstants.STARTING_DATE_TITLE);
        String lUser = readField(row, IOConstants.INSTALLATION_USER);
        
        // Business siglum is mandatory
        checkNotEmptyField(row, lBusinessSiglum,
                IOConstants.BUSINESS_SIGLUM_TITLE);
        // Aircraft program is mandatory
        checkNotEmptyField(row, lAircraftProgramStr,
                IOConstants.AIRCRAFTPROGRAM_TITLE);
        
        AircraftProgram lAircraftProgram =
                readAircraftProgram(lAircraftProgramStr,
                        IOConstants.AIRCRAFTPROGRAM_TITLE);
        
        // Person in charge is mandatory
        String lPersonInChargeStr = readField(row, IOConstants.TECHNICAL_CONTACT_TITLE);
        checkNotEmptyField(row, lPersonInChargeStr, IOConstants.TECHNICAL_CONTACT_TITLE);
        User lPersonInCharge =
                readUser(lPersonInChargeStr, IOConstants.TECHNICAL_CONTACT_TITLE);
        
        if (name == null) {
            throw new ImportException("NAME VIDE", row.getRowNum(),
                    columns.getIndex(IOConstants.NAME_TITLE));
        }
        
        Installation installation = new Installation();
        installation.setName(name);
        installation.setComments(lComments);
        installation.setBusinessSiglum(lBusinessSiglum);
        installation.setAircraftProgram(lAircraftProgram);
        installation.setPersonInCharge(lPersonInCharge);
        installation.setStartingDate(startingDate);
        installation.setUser(lUser);
        
        Location lLocation = readInstallationLocation(lLocationStr);
        
        try {
            installation =
                    locationBean.createInstallation(installation, lLocation);
        }
        catch (Exception e) {
            
            String msg = ExceptionUtil.getMessage(e);
            
            throw new ImportException("CREATION INSTALLATION IMPOSSIBLE: "
                    + msg, row.getRowNum(), -1);
            
        }
        
    }
    
    private Location readInstallationLocation(final String pLocationStr)
            throws ImportException {
        
        Location lLocation = null;
        
        try {
            lLocation =
                    locationFactory.generateLocation(pLocationStr, null, null,
                            LocatedType.Installation, false, null);
            
            LocationManager.validateLocation(lLocation);
            
        }
        catch (LocalizationException e) {
            switch (e.getError()) {
            case LocationNotAvailableForItem:
            case LocationNotFound:
                throw new ImportException(e.getMessage(), row.getRowNum(),
                        columns.getIndex(IOConstants.LOCATION_TITLE));
            case LocationMandatory:
                throw new ImportException(
                        MessageBundle.getMessage(Constants.LOCATION_MANDATORY),
                        row.getRowNum(),
                        getColumnIndex(IOConstants.LOCATION_TITLE));
            case ContainerNotAvailableForItem:
            case ContainerNotFound:
            case ContainerPrecisionNotValid:
            case ExternalLocationNotFound:
            case InheritedLocationNotAvailableForItem:
            case InheritedLocationNotAvailableContainerNotDefined:
            default:
                throw new ImportException(e.getMessage(), row.getRowNum(), -1);
            }
        }
        
        return lLocation;
    }
    
    private User readUser(String pFieldValue, String pColumnName)
            throws ImportException {
        
        User lUser = userBean.findUser(pFieldValue);
        if (lUser == null) {
            String lMsg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return lUser;
    }
    
    private AircraftProgram readAircraftProgram(String pFieldValue,
            String pColumnName) throws ImportException {
        
        AircraftProgram lAircraftProgram =
                valueListBean.findAttributeValueListByName(
                        AircraftProgram.class, pFieldValue);
        if (lAircraftProgram == null) {
            String lMsg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return lAircraftProgram;
    }
    
}
