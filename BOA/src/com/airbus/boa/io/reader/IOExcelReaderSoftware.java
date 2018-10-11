/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderSoftware
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnSoftware;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.application.DBConstants;

/**
 * Manage reading of a Software into Excel files
 */
public class IOExcelReaderSoftware extends IOExcelBaseReader {
    
    private SoftwareBean softwareBean;
    private String login;
    private static String sheetname = IOConstants.SoftwareSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook from which to read
     * @param columns
     *            the columns into Excel files
     * @param softwareBean
     *            the softwareBean to use during process
     * @param valueListBean
     *            the valueListBean to use during process
     * @param login
     *            the login of the user logged in
     */
    public IOExcelReaderSoftware(Workbook workbook, Columns columns,
            SoftwareBean softwareBean, ValueListBean valueListBean, String login) {
        super(valueListBean, workbook, columns, sheetname);
        
        this.softwareBean = softwareBean;
        this.login = login;
        
        if (columns == null) {
            this.columns = new ColumnSoftware();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        
        String name = readField(row, IOConstants.NAME_TITLE);
        String lDistribution = readField(row, IOConstants.DISTRIBUTION_TITLE);
        String lKernel = readField(row, IOConstants.KERNEL_TITLE);
        String lManufacturer = readField(row, IOConstants.MANUFACTURER_TITLE);
        String description = readField(row, IOConstants.DESCRIPTION_TITLE);
        String lOperatingSystemStr =
                readField(row, IOConstants.OPERATINGSYSTEM_TITLE);
        String licence = readField(row, IOConstants.LICENCE_TITLE);
        
        if (StringUtil.isEmptyOrNull(name)) {
            
            String msg =
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY);
            throw new ImportException(msg, row.getRowNum(),
                    columns.getIndex(IOConstants.NAME_TITLE));
        }
        
        if (StringUtil.isEmptyOrNull(lDistribution)) {
            
            String msg =
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY);
            throw new ImportException(msg, row.getRowNum(),
                    columns.getIndex(IOConstants.DISTRIBUTION_TITLE));
        }
        
        Boolean lOperatingSystem = readOperatingSystem(lOperatingSystemStr);
        
        if (lOperatingSystem && StringUtil.isEmptyOrNull(lKernel)) {
            
            String lMsg =
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_KERNEL_EMPTY);
            throw new ImportException(lMsg, row.getRowNum(),
                    columns.getIndex(IOConstants.KERNEL_TITLE));
        }
        
        if (softwareBean.exist(name, lDistribution, lKernel)) {
            String msg =
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_ALREADY_EXISTED);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        if (softwareBean.findCountByCompleteName(Software.getCompleteName(name,
                lDistribution, lKernel)) > 0) {
            String msg =
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_ALREADY_EXISTED);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        DBConstants lDBConstants =
                AbstractController.findBean(DBConstants.class);
        
        final int MANUFACTURER_LENGTH =
                lDBConstants.getSoftwareManufacturerLength();
        final int LICENCE_LENGTH = lDBConstants.getSoftwareLicenceLength();
        final int DESCRIPTION_LENGTH =
                lDBConstants.getSoftwareDescriptionLength();
        
        if (lManufacturer != null
                && lManufacturer.length() > MANUFACTURER_LENGTH) {
            lManufacturer = lManufacturer.substring(MANUFACTURER_LENGTH);
        }
        
        if (licence != null && licence.length() > LICENCE_LENGTH) {
            licence = licence.substring(LICENCE_LENGTH);
        }
        
        if (description != null && description.length() > DESCRIPTION_LENGTH) {
            description = description.substring(DESCRIPTION_LENGTH);
        }
        
        Software software = new Software(name, lDistribution, lKernel);
        software.setManufacturer(lManufacturer);
        software.setLicence(licence);
        software.setDescription(description);
        software.setOperatingSystem(lOperatingSystem);
        
        try {
            softwareBean.create(software, new ArrayList<Article>(), login);
            
        }
        catch (Exception e) {
            
            throw new ImportException("CREATION SOFTWARE IMPOSSIBLE: "
                    + ExceptionUtil.getMessage(e), getRownumber(), -1);
        }
    }
    
    private Boolean readOperatingSystem(String pOperatingSystem) {
        
        if (pOperatingSystem == null) {
            return false;
        }
        
        Boolean lResult = null;
        for (String lString : IOConstants.TRUE_STRINGS) {
            if (pOperatingSystem.equalsIgnoreCase(lString)) {
                lResult = true;
            }
        }
        for (String lString : IOConstants.FALSE_STRINGS) {
            if (pOperatingSystem.equalsIgnoreCase(lString)) {
                lResult = false;
            }
        }
        return lResult;
    }
    
}
