/*
 * ------------------------------------------------------------------------
 * Class : ImportExcelController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.poi.ss.SpreadsheetVersion;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

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
import com.airbus.boa.io.ImportExcel;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.service.NavigationConstants;

/**
 * Controller managing import of Excel files and corresponding page
 */
@ManagedBean(name = ImportExcelController.BEAN_NAME)
@SessionScoped
public class ImportExcelController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "importExcelController";

    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private LocationFactory locationFactory;
    
    @EJB
    private ContainerFactory containerFactory;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    private List<ImportExcel> importFiles = new ArrayList<ImportExcel>();
    
    private ImportExcel selectedImportFile;
    
    /**
     * Launch the download of the reject file
     */
    public void doDownloadRejectFile() {
        if (selectedImportFile != null && selectedImportFile.isProcessed()) {
            
            SimpleDateFormat lSdf = new SimpleDateFormat("yyyy_MM_dd_HHmm");
            StringBuffer lSbFilename = new StringBuffer("IMPORT_RESULTS_");
            lSbFilename.append(lSdf.format(new Date()));
            lSbFilename
                    .append((selectedImportFile.getVersionExcel() == SpreadsheetVersion.EXCEL2007) ? ".xlsx"
                            : ".xls");
            
            download(
                    selectedImportFile,
                    lSbFilename.toString(),
                    ((selectedImportFile.getVersionExcel() == SpreadsheetVersion.EXCEL2007) ? ExportController.MIMETYPE_XLSX
                            : ExportController.MIMETYPE_XLS));
            
        }
    }
    
    /**
     * Create an ImportExcel object when a file is added in the a component
     * 
     * @param pEvent
     *            the event sent when a file is added
     */
    public void fileUploadListener(FileUploadEvent pEvent) throws Exception {
        UploadedFile lItem = pEvent.getUploadedFile();
            
        log.log(Level.WARNING, lItem.toString());
        log.log(Level.WARNING, lItem.getName());
        log.log(Level.WARNING, lItem.getContentType());
        log.log(Level.WARNING, Long.valueOf(lItem.getSize())
                .toString());
        
        LogInController lLogInController = findBean(LogInController.class);
        User lCurrentUser = null;
        if (lLogInController != null) {
            lCurrentUser = lLogInController.getUserLogged();
        }
        ImportExcel lImportFile =
                new ImportExcel(lItem.getInputStream(), lItem.getName(),
                        locationBean, articleBean, pcBean, valueListBean,
                        locationFactory, containerFactory, softwareBean,
                        toolBean, obsoBean, lCurrentUser, userBean);
        
        importFiles.add(lImportFile);
    }
    
    /**
     * Read the selected imported file
     */
    public void readFile() {
        if (selectedImportFile != null) {
            try {
                selectedImportFile.open();
                selectedImportFile.read();
                selectedImportFile.dispose();
            }
            catch (ImportException e) {
                Utils.addFacesMessage(NavigationConstants.IMPOR_EXCEL_ERROR_ID,
                        e.getMessage());
            }
            
        }
        
    }
    
    /**
     * @return the list of imported files
     */
    public List<ImportExcel> getImportFiles() {
        return importFiles;
    }
    
    /**
     * Remove the selected file from the imported file list
     */
    public void removeImportFile() {
        if (selectedImportFile != null) {
            importFiles.remove(selectedImportFile);
        }
    }
    
    /**
     * @return the selectedImportFile
     */
    public ImportExcel getSelectedImportFile() {
        return selectedImportFile;
    }
    
    /**
     * @param pSelectedImportFile
     *            the selectedImportFile to set
     */
    public void setSelectedImportFile(ImportExcel pSelectedImportFile) {
        this.selectedImportFile = pSelectedImportFile;
    }
    
}
