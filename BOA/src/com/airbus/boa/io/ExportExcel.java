/*
 * ------------------------------------------------------------------------
 * Class : ExportExcel
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.column.ColumnBoard;
import com.airbus.boa.io.column.ColumnBuilding;
import com.airbus.boa.io.column.ColumnCabinet;
import com.airbus.boa.io.column.ColumnDatedComments;
import com.airbus.boa.io.column.ColumnEquipment;
import com.airbus.boa.io.column.ColumnHistory;
import com.airbus.boa.io.column.ColumnInstallation;
import com.airbus.boa.io.column.ColumnPC;
import com.airbus.boa.io.column.ColumnPort;
import com.airbus.boa.io.column.ColumnRack;
import com.airbus.boa.io.column.ColumnSoftware;
import com.airbus.boa.io.column.ColumnSwitch;
import com.airbus.boa.io.column.ColumnTool;
import com.airbus.boa.io.column.ColumnType;
import com.airbus.boa.io.column.ColumnUser;
import com.airbus.boa.io.column.ColumnVarious;
import com.airbus.boa.io.column.pc.ColumnInstalledBoard;
import com.airbus.boa.io.column.pc.ColumnInstalledSoftware;
import com.airbus.boa.io.column.pc.ColumnPCSpecificities;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.io.writer.IOExcelWriterBoard;
import com.airbus.boa.io.writer.IOExcelWriterBuilding;
import com.airbus.boa.io.writer.IOExcelWriterCabinet;
import com.airbus.boa.io.writer.IOExcelWriterCommunicationPort;
import com.airbus.boa.io.writer.IOExcelWriterDatedComments;
import com.airbus.boa.io.writer.IOExcelWriterEquipment;
import com.airbus.boa.io.writer.IOExcelWriterHistory;
import com.airbus.boa.io.writer.IOExcelWriterInstallation;
import com.airbus.boa.io.writer.IOExcelWriterInstalledBoard;
import com.airbus.boa.io.writer.IOExcelWriterInstalledSoftware;
import com.airbus.boa.io.writer.IOExcelWriterPC;
import com.airbus.boa.io.writer.IOExcelWriterPCSpecificities;
import com.airbus.boa.io.writer.IOExcelWriterRack;
import com.airbus.boa.io.writer.IOExcelWriterSoftware;
import com.airbus.boa.io.writer.IOExcelWriterSwitch;
import com.airbus.boa.io.writer.IOExcelWriterTool;
import com.airbus.boa.io.writer.IOExcelWriterType;
import com.airbus.boa.io.writer.IOExcelWriterUser;
import com.airbus.boa.io.writer.IOExcelWriterVarious;
import com.airbus.boa.io.writer.Writer;

/**
 * Manage the export of Excel files
 */
public class ExportExcel extends ExportExcelBase {
    
    private static final String EQUIPMENT_KEY = "Equipment";
    private static final String INSTALLED_SOFTWARE_KEY = "InstalledSoftware";
    private static final String INSTALLED_BOARD_KEY = "InstalledBoards";
    private static final String TYPE_ARTICLE_KEY = "TypeArticleKey";
    private static final String PLACE_KEY = "PlaceKey";
    private static final String PC_SPECIFICITIES_KEY = "PCSpecificities";
    private static final String DATED_COMMENTS_KEY = "DatedComments";
    
    private Map<String, Writer> mapWriter = new HashMap<String, Writer>();
    
    private Boolean isExportAll = true;
    private boolean historyExported = true;
    
    /**
     * Constructor
     * 
     * @param pVersion
     *            the Excel version of the file to export
     */
    public ExportExcel(SpreadsheetVersion pVersion) {
        super(pVersion);
    }
    
    /**
     * Write the provided list, result of a search
     * 
     * @param pList
     *            the list to write
     */
    public void writeListResult(List<?> pList) {
        
        isExportAll = false;
        writeList(pList);
        isExportAll = true;
    }
    
    @Override
    public void writeList(List<?> pList) {
        
        for (Object lObject : pList) {
            
            // Export of Article History
            if (historyExported && lObject instanceof Article) {
                Article lArticle = (Article) lObject;
                if (lArticle.getHistory().getActions().size() > 0) {
                    
                    writeObject(lArticle.getHistory().getClass()
                            .getSimpleName(), lObject, null);
                }
            }
            
            // Export of the entity
            if (lObject instanceof TypeArticle) {
                
                writeObject(TYPE_ARTICLE_KEY, lObject, null);
            }
            else if (lObject instanceof Building || lObject instanceof Place
                    || lObject instanceof ExternalEntity) {
                
                writeObject(PLACE_KEY, lObject, null);
            }
            else {
                writeObject(lObject.getClass().getSimpleName(), lObject, null);
            }
            
            if (lObject instanceof Article) {
                Article lArticle = (Article) lObject;
                for (DatedComment lDatedComment : lArticle.getDatedComments()) {
                    writeObject(DATED_COMMENTS_KEY, lDatedComment, lObject);
                }
            }
            
            // Export of CommunicationPort
            List<CommunicationPort> lPorts = null;
            if (lObject instanceof PC) {
                lPorts = ((PC) lObject).getPorts();
                
                if (!isExportAll) {
                    // Export of PC only
                    for (Software lSoftware : ((PC) lObject).getSoftwares()) {
                        writeObject(INSTALLED_SOFTWARE_KEY, lSoftware, lObject);
                    }
                    
                    for (Article lChild : ((PC) lObject).getChildren()) {
                        if (lChild instanceof Board) {
                            writeObject(INSTALLED_BOARD_KEY, lChild, lObject);
                        }
                    }
                    
                    for (PCSpecificity lSpecificity : ((PC) lObject)
                            .getSpecificities()) {
                        writeObject(PC_SPECIFICITIES_KEY, lSpecificity,
                                lObject);
                    }
                }
            }
            else if (lObject instanceof Board) {
                lPorts = ((Board) lObject).getPorts();
            }
            
            if (lPorts != null) {
                for (CommunicationPort lPort : lPorts) {
                    writeObject(lPort.getClass().getSimpleName(), lPort,
                            lObject);
                }
            }
            
            // Export of software equipments
            if (lObject instanceof Software) {
                for (Article lEquipment : ((Software) lObject).getEquipments()) {
                    writeObject(EQUIPMENT_KEY, lEquipment, lObject);
                }
            }
        }
    }
    
    /**
     * Search/create the object to export associated writer.
     * Write the object in the Excel file.
     * 
     * @param pKey
     *            writer search key (1 writer per object type)
     * @param pObject
     *            the object to write
     * @param pParent
     *            the object parent, null if not used
     */
    private void writeObject(String pKey, Object pObject, Object pParent) {
        
        try {
            Writer lWriter;
            if (mapWriter.containsKey(pKey)) {
                lWriter = mapWriter.get(pKey);
            }
            else {
                lWriter = getExcelWriter(pKey, pObject);
                mapWriter.put(pKey, lWriter);
                lWriter.writeHeader();
            }
            
            // Initialize parent when needed
            
            if (pKey.equals(CommunicationPort.class.getSimpleName())) {
                // SN information are in the PC or Board containing the port
                ((IOExcelWriterCommunicationPort) lWriter).setParent(pParent);
            }
            else if (pKey.equals(EQUIPMENT_KEY)) {
                // Software complete name is in the installed software
                ((IOExcelWriterEquipment) lWriter).setParent(pParent);
            }
            else if (pKey.equals(INSTALLED_SOFTWARE_KEY)) {
                // SN information are in the PC on which the software is
                // installed
                ((IOExcelWriterInstalledSoftware) lWriter).setParent(pParent);
            }
            else if (pKey.equals(INSTALLED_BOARD_KEY)) {
                // SN information are in the PC on which the board is installed
                ((IOExcelWriterInstalledBoard) lWriter).setParent(pParent);
            }
            
            lWriter.writeOne(pObject);
            
        }
        catch (ExportException e) {
        }
    }
    
    @Override
    public void applyStyles() {
        
        for (Writer lWriter : mapWriter.values()) {
            lWriter.applyStyles();
        }
        
        super.applyStyles();
    }
    
    /**
     * Create a new writer according to the provided object type
     * 
     * @param pKey
     *            key corresponding to the required export
     * @param pObject
     *            the object for which to retrieve the writer
     * @return the created writer, or null if an error occurs
     */
    private IOExcelWriterBaseWriter getExcelWriter(String pKey, Object pObject) {
        
        try {
            
            /*
             * Treat specific cases using the provided key
             */
            
            // The object is the article, only the key allows to know that the
            // history has to be exported
            if (pKey.equals(History.class.getSimpleName())) {
                return new IOExcelWriterHistory(workbook, new ColumnHistory());
            }
            
            // The object (equipment) is an Article and cannot be distinguished
            // using its class
            if (pKey.equals(EQUIPMENT_KEY)) {
                return new IOExcelWriterEquipment(workbook,
                        new ColumnEquipment());
            }
            
            // Only one writer instance is created for all TypeArticle
            if (pKey.equals(TYPE_ARTICLE_KEY)) {
                return new IOExcelWriterType(workbook, new ColumnType());
            }
            
            // Only one writer instance is created for Building, Place,
            // ExternalEntity
            if (pKey.equals(PLACE_KEY)) {
                return new IOExcelWriterBuilding(workbook, new ColumnBuilding());
            }
            
            // Specific case of Board installed on PC
            if (pKey.equals(INSTALLED_BOARD_KEY)) {
                return new IOExcelWriterInstalledBoard(workbook,
                        new ColumnInstalledBoard());
            }
            
            // Specific case of Software installed on PC
            if (pKey.equals(INSTALLED_SOFTWARE_KEY)) {
                return new IOExcelWriterInstalledSoftware(workbook,
                        new ColumnInstalledSoftware());
            }
            
            // Specific case of Board installed on PC
            if (pKey.equals(PC_SPECIFICITIES_KEY)) {
                return new IOExcelWriterPCSpecificities(workbook,
                        new ColumnPCSpecificities());
            }
            
            // Specific case of Software installed on PC
            if (pKey.equals(DATED_COMMENTS_KEY)) {
                return new IOExcelWriterDatedComments(workbook,
                        new ColumnDatedComments());
            }
            
            /*
             * When the provided key corresponds to the object class name,
             * determine the ExcelWriter by checking the object class
             */
            if (pKey.equals(pObject.getClass().getSimpleName())) {
                
                if (pObject instanceof Cabinet) {
                    return new IOExcelWriterCabinet(workbook,
                            new ColumnCabinet());
                }
                
                // To check before Rack since Switch is also a Rack instance
                if (pObject instanceof Switch) {
                    return new IOExcelWriterSwitch(workbook, new ColumnSwitch());
                }
                
                if (pObject instanceof Rack) {
                    return new IOExcelWriterRack(workbook, new ColumnRack());
                }
                
                if (pObject instanceof Various) {
                    return new IOExcelWriterVarious(workbook,
                            new ColumnVarious());
                }
                
                if (pObject instanceof Board) {
                    return new IOExcelWriterBoard(workbook, new ColumnBoard());
                }
                
                if (pObject instanceof User) {
                    String[] lColumnsDefinition =
                            { IOConstants.LOGIN_TITLE,
                                    IOConstants.LASTNAME_TITLE,
                                    IOConstants.FIRSTNAME_TITLE,
                                    IOConstants.EMAIL_TITLE,
                                    IOConstants.ROLE_TITLE };
                    return new IOExcelWriterUser(workbook,
                            new ColumnUser().read(lColumnsDefinition));
                }
                
                if (pObject instanceof Installation) {
                    return new IOExcelWriterInstallation(workbook,
                            new ColumnInstallation());
                }
                
                if (pObject instanceof PC) {
                    return new IOExcelWriterPC(workbook, new ColumnPC());
                }
                
                if (pObject instanceof CommunicationPort) {
                    return new IOExcelWriterCommunicationPort(workbook,
                            new ColumnPort());
                }
                
                if (pObject instanceof Software) {
                    return new IOExcelWriterSoftware(workbook,
                            new ColumnSoftware());
                }
                
                if (pObject instanceof Tool) {
                    return new IOExcelWriterTool(workbook, new ColumnTool());
                }
            }
            
        }
        catch (ExportException e) {
        }
        
        return null;
    }
    
    /**
     * @param pHistoryExported
     *            the historyExported to set
     */
    public void setHistoryExported(boolean pHistoryExported) {
        historyExported = pHistoryExported;
    }
    
}
