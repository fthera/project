/*
 * ------------------------------------------------------------------------
 * Class : ImportDocumentsController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.hiddenadministration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.DocumentBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.UpdateBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.DocumentController;
import com.airbus.boa.view.ExportController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.Utils;

/**
 * Controller managing import of Excel file for linking documents and
 * corresponding page
 */
@ManagedBean(name = ImportDocumentsController.BEAN_NAME)
@SessionScoped
public class ImportDocumentsController extends AbstractController implements
        Serializable {
    
    private enum Mode {
        Article,
        Installation,
        Tool
    }
    
    /** The managed bean name */
    public static final String BEAN_NAME = "importDocumentsController";

    private static final String AIRBUS_OR_MANUFACTURER_SN_COLUMN =
            "Airbus or Manufacturer S/N";
    private static final String NAME_COLUMN = "Name";
    private static final String FILE_PATH_COLUMN = "File path";
    
    private static final String ARTICLE_SHEET_NAME = "Article";
    private static final String INSTALLATION_SHEET_NAME = "Installation";
    private static final String TOOL_SHEET_NAME = "Tool";
    
    /** Columns of Article sheet */
    protected static final String[] ARTICLE_COLUMNS = {
            AIRBUS_OR_MANUFACTURER_SN_COLUMN, FILE_PATH_COLUMN };
    
    /** Columns of Installation sheet */
    protected static final String[] INSTALLATION_COLUMNS = { NAME_COLUMN,
            FILE_PATH_COLUMN };
    
    /** Columns of Tool sheet */
    protected static final String[] TOOL_COLUMNS = { NAME_COLUMN,
            FILE_PATH_COLUMN };
    
    /**
     * Class managing the file reading and processing
     */
    public class FileManager implements Serializable, Downloadable {
        
        private static final long serialVersionUID = 1L;
        
        private InputStream inputStream;
        private String filename;
        
        private boolean processed = false;
        
        private Workbook workbook;
        private Sheet sheet;
        
        private Mode mode;
        
        private SpreadsheetVersion versionExcel;
        
        private Row row;
        private int rownumber;
        
        private Map<String, Integer> columnsMap =
                new HashMap<String, Integer>();
        
        private CellStyle errorCellStyle;
        
        /**
         * Constructor
         * 
         * @param pInputStream
         *            the input stream to read and import
         * @param pFilename
         *            the file name
         */
        protected FileManager(InputStream pInputStream, String pFilename) {
            
            inputStream = pInputStream;
            filename = pFilename;
        }
        
        /**
         * Read the whole file
         * 
         * @throws ImportException
         *             when the file cannot be opened
         */
        protected void readWorkbook() throws ImportException {
            
            // if file has already been processed, stop here
            if (processed == true) {
                return;
            }
            
            // Retrieve workbook from file
            
            try {
                if (filename.endsWith("xlsx")) {
                    
                    workbook = new XSSFWorkbook(inputStream);
                    versionExcel = SpreadsheetVersion.EXCEL2007;
                }
                else if (filename.endsWith("xls")) {
                    
                    workbook = new HSSFWorkbook(inputStream);
                    versionExcel = SpreadsheetVersion.EXCEL97;
                }
                else {
                    throw new ImportException("NOT RECOGNIZED EXCEL FILE");
                }
            }
            catch (IOException e) {
                throw new ImportException(e);
            }
            
            createStyle();
            
            for (Mode lMode : Mode.values()) {
                
                mode = lMode;
                
                columnsMap.clear();
                int i = 0;
                
                String lCurrentSheetName = "NoCurrentSheet";
                
                switch (mode) {
                case Article:
                    for (String lColumn : ARTICLE_COLUMNS) {
                        columnsMap.put(lColumn, i++);
                    }
                    lCurrentSheetName = ARTICLE_SHEET_NAME;
                    break;
                case Installation:
                    for (String lColumn : INSTALLATION_COLUMNS) {
                        columnsMap.put(lColumn, i++);
                    }
                    lCurrentSheetName = INSTALLATION_SHEET_NAME;
                    break;
                case Tool:
                    for (String lColumn : TOOL_COLUMNS) {
                        columnsMap.put(lColumn, i++);
                    }
                    lCurrentSheetName = TOOL_SHEET_NAME;
                    break;
                default:
                    break;
                }
                
                // Retrieve and treat the sheet
                
                sheet = workbook.getSheet(lCurrentSheetName);
                if (sheet != null) {
                    readSheet();
                }
            }
            
            processed = true;
        }
        
        /**
         * Read the current sheet
         */
        private void readSheet() {
            
            if (!checkHeader()) {
                // color the line
                signalErrorField(sheet.getRow(sheet.getFirstRowNum()), -1);
                return;
            }
            
            // set current line to first data line
            rownumber = sheet.getFirstRowNum() + 1;
            
            int lLastRow = sheet.getLastRowNum();
            while (rownumber <= lLastRow) {
                
                row = sheet.getRow(rownumber);
                if (row == null) {
                    continue;
                }
                
                Integer lLastCol = Collections.max(columnsMap.values()) + 2;
                try {
                    readLine();
                }
                catch (ImportException ie) {
                    
                    signalErrorField(row, ie.getColumnIndex());
                    writeCellErrorMessage(row, lLastCol, ie.getMessage());
                }
                rownumber++;
            }
        }
        
        private boolean checkHeader() {
            if (sheet == null) {
                return false;
            }
            Row lFirstRow = sheet.getRow(sheet.getFirstRowNum());
            if (lFirstRow == null) {
                return false;
            }
            
            Iterator<Entry<String, Integer>> lColumnIterator =
                    columnsMap.entrySet().iterator();
            while (lColumnIterator.hasNext()) {
                
                Entry<String, Integer> lEntry = lColumnIterator.next();
                if (!lEntry.getKey().equalsIgnoreCase(
                        readCell(lFirstRow, lEntry.getValue()))) {
                    return false;
                }
            }
            return true;
        }
        
        private void readLine() throws ImportException {
            
            String lFilePath = readField(row, FILE_PATH_COLUMN);
            
            if (lFilePath == null) {
                throw new ImportException("File path is null", rownumber,
                        getColumnIndex(FILE_PATH_COLUMN));
            }
            
            String lFileName =
                    lFilePath.substring(lFilePath.lastIndexOf("/") + 1);
            lFileName = lFileName.substring(lFilePath.lastIndexOf("\\") + 1);
            
            File lFile = new File(lFilePath);
            byte[] lData = null;
            
            try {
                lData = DocumentController.convertFileToByteArray(lFile);
            }
            catch (Exception e) {
                throw new ImportException(
                        "An error occurs while reading file: "
                                + ExceptionUtil.getMessage(e), rownumber,
                        getColumnIndex(FILE_PATH_COLUMN));
            }
            
            switch (mode) {
            case Article:
                
                updateArticle(lFileName, lData);
                break;
            
            case Installation:
                
                updateInstallation(lFileName, lData);
                break;
            
            case Tool:
                
                updateTool(lFileName, lData);
                break;
            
            default:
                break;
            }
            
        }
        
        private void updateArticle(String pFileName, byte[] pData)
                throws ImportException {
            
            String lSN = readField(row, AIRBUS_OR_MANUFACTURER_SN_COLUMN);
            
            if (lSN == null) {
                String lMsg =
                        MessageBundle
                                .getMessageResource(
                                        Constants.FIELD_MUST_NOT_BE_EMPTY,
                                        new Object[] { AIRBUS_OR_MANUFACTURER_SN_COLUMN });
                throw new ImportException(lMsg, rownumber,
                        getColumnIndex(AIRBUS_OR_MANUFACTURER_SN_COLUMN));
            }
            
            Article lFoundArticle;
            try {
                lFoundArticle = articleBean.findArticleBySN(lSN);
            }
            catch (Exception e) {
                lFoundArticle = null;
            }
            
            if (lFoundArticle == null) {
                throw new ImportException("Article not found", rownumber,
                        getColumnIndex(AIRBUS_OR_MANUFACTURER_SN_COLUMN));
            }
            
            User lUser = findBean(LogInController.class).getUserLogged();
            
            try {
                Context lCtx = new InitialContext();
                updateBean = (UpdateBean) lCtx.lookup("java:module/UpdateBean");
            }
            catch (NamingException e) {
                throw new ValidationException(e);
            }
            updateBean.setArticle(lFoundArticle);
            updateBean.setCurrentUser(lUser);
            
            // Add document to the article
            
            Document lDocument = new Document(lFoundArticle, pFileName, pData);
            
            List<Document> lDocuments =
                    documentBean.findDocumentsByArticle(lFoundArticle);
            lDocuments.add(lDocument);
            
            updateBean.setDocuments(lDocuments);
            
            String lHistoryComment = "Document linked by import";
            
            updateBean.saveChange(lHistoryComment);
            updateBean.remove();
            updateBean = null;
        }
        
        private void updateInstallation(String pFileName, byte[] pData)
                throws ImportException {
            
            String lInstallationName = readField(row, NAME_COLUMN);
            
            if (lInstallationName == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.FIELD_MUST_NOT_BE_EMPTY,
                                new Object[] { NAME_COLUMN });
                throw new ImportException(lMsg, rownumber,
                        getColumnIndex(NAME_COLUMN));
            }
            
            Installation lFoundInstallation;
            try {
                lFoundInstallation =
                        locationBean.findInstallationByName(lInstallationName);
            }
            catch (Exception e) {
                lFoundInstallation = null;
            }
            
            if (lFoundInstallation == null) {
                throw new ImportException("Installation not found", rownumber,
                        getColumnIndex(NAME_COLUMN));
            }
            
            // The DocumentController must be updated manually since no page is
            // displayed.
            // (This managed bean is ViewScoped and is thus not refreshed if it
            // is not loaded)
            
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(lFoundInstallation);
            
            // Add document to the installation
            
            Document lDocument =
                    new Document(lFoundInstallation, pFileName, pData);
            
            lDocumentController.getDocuments().add(lDocument);
            
            lDocumentController.doPersistChanges(lFoundInstallation);
        }
        
        private void updateTool(String pFileName, byte[] pData)
                throws ImportException {
            
            String lToolName = readField(row, NAME_COLUMN);
            
            if (lToolName == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.FIELD_MUST_NOT_BE_EMPTY,
                                new Object[] { NAME_COLUMN });
                throw new ImportException(lMsg, rownumber,
                        getColumnIndex(NAME_COLUMN));
            }
            
            Tool lFoundTool;
            try {
                lFoundTool = toolBean.findToolByName(lToolName);
            }
            catch (Exception e) {
                lFoundTool = null;
            }
            
            if (lFoundTool == null) {
                throw new ImportException("Tool not found", rownumber,
                        getColumnIndex(NAME_COLUMN));
            }
            
            // The DocumentController must be updated manually since no page is
            // displayed.
            // (This managed bean is ViewScoped and is thus not refreshed if it
            // is not loaded)
            
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(lFoundTool);
            
            // Add document to the tool
            
            Document lDocument = new Document(lFoundTool, pFileName, pData);
            
            lDocumentController.getDocuments().add(lDocument);
            
            lDocumentController.doPersistChanges(lFoundTool);
        }
        
        private String readField(Row pRow, String pColumnName) {
            int lIndex = -1;
            if (columnsMap.containsKey(pColumnName)) {
                lIndex = columnsMap.get(pColumnName);
            }
            return readCell(pRow, lIndex);
        }
        
        private String readCell(Row pRow, int pCellNum) {
            
            Cell lCell = pRow.getCell(pCellNum);
            if (lCell == null) {
                return null;
            }
            
            try {
                String lValue = lCell.getStringCellValue().trim();
                
                if (!lValue.isEmpty()) {
                    return lValue;
                }
            }
            catch (Exception e) {
            }
            return null;
        }
        
        private int getColumnIndex(String pColumnName) {
            if (columnsMap.containsKey(pColumnName)) {
                return columnsMap.get(pColumnName);
            }
            return -1;
        }
        
        private void createStyle() {
            
            errorCellStyle = workbook.createCellStyle();
            errorCellStyle.setBorderRight(BorderStyle.THIN);
            errorCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            errorCellStyle.setBorderBottom(BorderStyle.THIN);
            errorCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            errorCellStyle.setBorderLeft(BorderStyle.THIN);
            errorCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            errorCellStyle.setBorderTop(BorderStyle.THIN);
            errorCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
            errorCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font lFont = workbook.createFont();
            lFont.setBold(true);
            errorCellStyle.setFont(lFont);
        }
        
        private void signalErrorField(Row pRow, int pIndex) {
            
            if (pIndex == -1) {
                // color the whole line
                Integer lMinCol = Collections.min(columnsMap.values());
                Integer lMaxCol = Collections.max(columnsMap.values());
                
                if (lMinCol == null) {
                    lMinCol = 0;
                }
                if (lMaxCol == null) {
                    lMaxCol = 0;
                }
                
                for (int i = lMinCol; i <= lMaxCol; i++) {
                    
                    Cell lCell = pRow.getCell(i);
                    if (lCell != null) {
                        
                        lCell.setCellStyle(errorCellStyle);
                    }
                    else {
                        // create cells with error style
                        writeCellErrorMessage(pRow, i, "");
                    }
                }
            }
            else {
                Cell lCell = pRow.getCell(pIndex);
                if (lCell != null) {
                    lCell.setCellStyle(errorCellStyle);
                }
                else {
                    writeCellErrorMessage(pRow, pIndex, "ERROR HERE");
                }
            }
        }
        
        private void writeCellErrorMessage(Row pRow, int pIndexCol,
                String pMessage) {
            if (pRow == null || pIndexCol < 0) {
                return;
            }
            Cell lCell = pRow.createCell(pIndexCol);
            lCell.setCellStyle(errorCellStyle);
            String lValue = pMessage;
            if (lValue == null) {
                lValue = "";
            }
            lCell.setCellValue(lValue);
        }
        
        @Override
        public void write(OutputStream pOutputStream) throws IOException {
            workbook.write(pOutputStream);
        }
        
        /**
         * @return the processed
         */
        public boolean isProcessed() {
            return processed;
        }
        
        /**
         * @return the versionExcel
         */
        protected SpreadsheetVersion getVersionExcel() {
            return versionExcel;
        }
        
        /**
         * @return the filename
         */
        public String getFilename() {
            return filename;
        }
        
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The ArticleBean to use */
    @EJB
    protected ArticleBean articleBean;
    
    /** The LocationBean to use */
    @EJB
    protected LocationBean locationBean;
    
    /** The ToolBean to use */
    @EJB
    protected ToolBean toolBean;
    
    /** The UpdateBean to use */
    @EJB
    protected UpdateBean updateBean;
    
    /** The DocumentBean to use */
    @EJB
    protected DocumentBean documentBean;
    
    private List<FileManager> importFiles = new ArrayList<FileManager>();
    
    private FileManager selectedImportFile;
    
    /**
     * Propose the download of the reject file
     */
    public void doDownloadRejectFile() {
        
        if (selectedImportFile != null && selectedImportFile.isProcessed()) {
            
            String lContentType = null;
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat("yyyy_MM_dd_HHmm");
            
            StringBuffer lFileName = new StringBuffer("IMPORT_RESULTS_");
            lFileName.append(lDateFormat.format(new Date()));
            
            if (selectedImportFile.getVersionExcel() == SpreadsheetVersion.EXCEL2007) {
                lFileName.append(".xlsx");
                lContentType = ExportController.MIMETYPE_XLSX;
            }
            else {
                lFileName.append(".xls");
                lContentType = ExportController.MIMETYPE_XLS;
            }
            
            download(selectedImportFile, lFileName.toString(), lContentType);
            
        }
    }
    
    /**
     * Listener for uploading files
     * 
     * @param event
     *            the event
     * @throws Exception
     *             when an error occurs
     */
    public void fileUploadListener(FileUploadEvent event) throws Exception {
        UploadedFile lUploadItem = event.getUploadedFile();
        
        FileManager lImportFile =
                new FileManager(lUploadItem.getInputStream(),
                        lUploadItem.getName());
        
        importFiles.add(lImportFile);
    }
    
    /**
     * Read the selected importFile
     */
    public void readFile() {
        if (selectedImportFile != null) {
            try {
                selectedImportFile.readWorkbook();
            }
            catch (ImportException e) {
                Utils.addFacesMessage(NavigationConstants.IMPORT_DOC_ERROR_ID,
                        e.getMessage());
            }
        }
    }
    
    /**
     * @return the importFiles
     */
    public List<FileManager> getImportFiles() {
        return importFiles;
    }
    
    /**
     * Remove the selected importFile
     */
    public void removeImportFile() {
        if (selectedImportFile != null) {
            importFiles.remove(selectedImportFile);
        }
    }
    
    /**
     * @param pSelectedImportFile
     *            the selectedImportFile to set
     */
    public void setSelectedImportFile(FileManager pSelectedImportFile) {
        selectedImportFile = pSelectedImportFile;
    }
    
    /**
     * @return the file description in HTML format to display on page
     */
    public String getFileDescription() {
        
        StringBuffer lDescription =
                new StringBuffer(MessageBundle.getMessageResource(
                        MessageConstants.IMPORT_DOC_FILE_SHEET_DESCRIPTION,
                        new Object[] { "Articles", ARTICLE_SHEET_NAME }));
        lDescription.append("<ul>");
        for (String lColumn : ARTICLE_COLUMNS) {
            lDescription.append("<li>").append(lColumn).append("</li>");
        }
        lDescription.append("</ul>");
        
        lDescription.append(MessageBundle.getMessageResource(
                MessageConstants.IMPORT_DOC_FILE_SHEET_DESCRIPTION,
                new Object[] { "Installations", INSTALLATION_SHEET_NAME }));
        lDescription.append("<ul>");
        for (String lColumn : INSTALLATION_COLUMNS) {
            lDescription.append("<li>").append(lColumn).append("</li>");
        }
        lDescription.append("</ul>");
        
        lDescription.append(MessageBundle.getMessageResource(
                MessageConstants.IMPORT_DOC_FILE_SHEET_DESCRIPTION,
                new Object[] { "Tools", TOOL_SHEET_NAME }));
        lDescription.append("<ul>");
        for (String lColumn : TOOL_COLUMNS) {
            lDescription.append("<li>").append(lColumn).append("</li>");
        }
        lDescription.append("</ul>");
        
        lDescription
                .append(MessageBundle
                        .getMessage(MessageConstants.IMPORT_DOC_FILE_PATH_COLUMN_DESCRIPTION));
        
        return lDescription.toString();
    }
    
}
