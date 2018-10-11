/*
 * ------------------------------------------------------------------------
 * Class : ImportExcel
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.reader.IOExcelBaseReader;
import com.airbus.boa.io.reader.IOExcelReaderBoard;
import com.airbus.boa.io.reader.IOExcelReaderBuilding;
import com.airbus.boa.io.reader.IOExcelReaderCabinet;
import com.airbus.boa.io.reader.IOExcelReaderCommunicationPort;
import com.airbus.boa.io.reader.IOExcelReaderEquipment;
import com.airbus.boa.io.reader.IOExcelReaderInstallation;
import com.airbus.boa.io.reader.IOExcelReaderObsolescence;
import com.airbus.boa.io.reader.IOExcelReaderPC;
import com.airbus.boa.io.reader.IOExcelReaderRack;
import com.airbus.boa.io.reader.IOExcelReaderSoftware;
import com.airbus.boa.io.reader.IOExcelReaderSwitch;
import com.airbus.boa.io.reader.IOExcelReaderTool;
import com.airbus.boa.io.reader.IOExcelReaderType;
import com.airbus.boa.io.reader.IOExcelReaderVarious;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.LocationFactory;

/**
 * Manage the import of Excel files
 */
public class ImportExcel implements Serializable, Downloadable {
    
    private static final long serialVersionUID = 3994962653195110795L;
    
    private Workbook workbook;
    private LocationBean locationBean;
    private ArticleBean articleBean;
    private LocationFactory locationFactory;
    private ContainerFactory containerFactory;
    private ValueListBean valueListBean;
    private PCBean pcBean;
    private SoftwareBean softwareBean;
    private ObsolescenceBean obsoBean;
    private ToolBean toolBean;
    private UserBean userBean;
    private User user;
    private InputStream inputStream;
    private String filename;
    private boolean processed;
    private boolean successful;
    private boolean sheetFound;
    private SpreadsheetVersion versionExcel;
    
    /**
     * Constructor
     * 
     * @param pInputStream
     *            the input stream to read and import
     * @param pFilename
     *            the file name
     * @param pLocationBean
     *            the LocationBean to use
     * @param pArticleBean
     *            the articleBean to use
     * @param pPCBean
     *            the PCBean to use
     * @param pValueListBean
     *            the ValueListBean to use
     * @param pLocationFactory
     *            the LocationFactory to use
     * @param pContainerFactory
     *            the ContainerFactory to use
     * @param pSoftwareBean
     *            the SoftwareBean to use
     * @param pToolBean
     *            the ToolBean to use
     * @param pObsoBean
     *            the ObsolescenceBean to use
     * @param pUser
     *            the user performing the file import
     * @param pUserBean
     *            the userBean to use
     */
    public ImportExcel(InputStream pInputStream, String pFilename,
            LocationBean pLocationBean, ArticleBean pArticleBean,
            PCBean pPCBean, ValueListBean pValueListBean,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, SoftwareBean pSoftwareBean,
            ToolBean pToolBean, ObsolescenceBean pObsoBean, User pUser,
            UserBean pUserBean) {
        
        // EJB have to be initialized by a JSF ManagedBean
        
        if (pLocationBean == null || pArticleBean == null
                || pValueListBean == null || pPCBean == null
                || pSoftwareBean == null || pObsoBean == null
                || pToolBean == null || pUserBean == null) {
            throw new IllegalArgumentException(
                    "One of the provided Stateless bean is null");
        }
        locationBean = pLocationBean;
        articleBean = pArticleBean;
        pcBean = pPCBean;
        valueListBean = pValueListBean;
        softwareBean = pSoftwareBean;
        locationFactory = pLocationFactory;
        containerFactory = pContainerFactory;
        toolBean = pToolBean;
        obsoBean = pObsoBean;
        userBean = pUserBean;
        user = pUser;
        inputStream = pInputStream;
        filename = pFilename;
        processed = false;
        successful = true;
        sheetFound = false;
    }
    
    /**
     * Open the file corresponding to the filename
     * 
     * @throws ImportException
     *             when the file cannot be opened
     */
    public void open() throws ImportException {
        
        try {
            if (filename.endsWith("xlsx")) {
                
                workbook = new XSSFWorkbook(inputStream);
                setVersionExcel(SpreadsheetVersion.EXCEL2007);
                
            }
            else if (filename.endsWith("xls")) {
                workbook = new HSSFWorkbook(inputStream);
                setVersionExcel(SpreadsheetVersion.EXCEL97);
                
            }
            else {
                throw new ImportException("FICHIER EXCEL NON RECONNU");
            }
            
        }
        catch (IOException e) {
            throw new ImportException(e); // VOIR POUR LES LIGNES
        }
    }
    
    /**
     * Read the whole file
     */
    public void read() throws ImportException{
        
        String lLogin;
        if (user != null) {
            lLogin = user.getLogin();
        }
        else {
            lLogin = null;
        }
        
        if (processed == true) {
            return;
        }

        IOExcelBaseReader[] lReaders =
                {
                        new IOExcelReaderBuilding(workbook, null, locationBean,
                                valueListBean),
                        
                        new IOExcelReaderType(workbook, null, articleBean,
                                valueListBean),
                        
                        new IOExcelReaderSoftware(workbook, null, softwareBean,
                                valueListBean, lLogin),
                        
                        new IOExcelReaderInstallation(workbook, null,
                                locationFactory, locationBean, valueListBean,
                                userBean),
                        new IOExcelReaderTool(workbook, locationFactory,
                                containerFactory, toolBean, valueListBean),
                        
                        new IOExcelReaderCabinet(workbook, null,
                                locationFactory, containerFactory, articleBean,
                                valueListBean, user),
                        new IOExcelReaderRack(workbook, null, locationFactory,
                                containerFactory, articleBean, valueListBean,
                                user),
                        new IOExcelReaderPC(workbook, null, locationFactory,
                                containerFactory, articleBean, pcBean,
                                valueListBean, userBean, softwareBean, user),
                        new IOExcelReaderSwitch(workbook, null,
                                locationFactory, containerFactory, articleBean,
                                valueListBean, user),
                        new IOExcelReaderBoard(workbook, null, locationFactory,
                                containerFactory, articleBean, valueListBean,
                                user),
                        new IOExcelReaderVarious(workbook, null,
                                locationFactory, containerFactory, articleBean,
                                valueListBean, user),
                        
                        new IOExcelReaderObsolescence(workbook, null,
                                valueListBean, obsoBean),
                        new IOExcelReaderCommunicationPort(workbook, null,
                                articleBean, pcBean, valueListBean, lLogin),
                        new IOExcelReaderEquipment(workbook, null, articleBean,
                                pcBean, softwareBean, valueListBean, lLogin) };
        
        for (IOExcelBaseReader lReader : lReaders) {
            lReader.read();
            if (lReader.isSheetFound()) {
                sheetFound = true;
                successful &= lReader.isSuccessful();
            }
        }
        setProcessed(true);
    }
    
    /**
     * Reset all managed beans
     */
    public void dispose() {
        locationBean = null;
        articleBean = null;
        locationFactory = null;
        containerFactory = null;
        obsoBean = null;
        softwareBean = null;
        toolBean = null;
        user = null;
        valueListBean = null;
        pcBean = null;
    }
    
    @Override
    public void write(OutputStream stream) throws IOException {
        workbook.write(stream);
    }
    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * @return a boolean indicating if the parsing was successful
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * @param pSuccessful
     *            a boolean to set the successful attribute
     */
    public void setSuccessful(boolean pSuccessful) {
        this.successful = pSuccessful;
    }
    
    /**
     * @return a boolean indicating if the sheet was found in the Excel file
     */
    public boolean isSheetFound() {
        return sheetFound;
    }
    
    /**
     * @param pSheetFound
     *            a boolean to set the sheetFound attribute
     */
    public void setSheetFound(boolean pSheetFound) {
        this.sheetFound = pSheetFound;
    }
    
    /**
     * @return the processed
     */
    public boolean isProcessed() {
        return processed;
    }
    
    /**
     * @param processed
     *            the processed to set
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    
    /**
     * @return the versionExcel
     */
    public SpreadsheetVersion getVersionExcel() {
        return versionExcel;
    }
    
    /**
     * @param versionExcel
     *            the versionExcel to set
     */
    public void setVersionExcel(SpreadsheetVersion versionExcel) {
        this.versionExcel = versionExcel;
    }
    
}
