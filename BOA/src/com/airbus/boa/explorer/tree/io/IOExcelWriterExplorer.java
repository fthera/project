/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterExplorer
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.tree.io;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.explorer.tree.TreeArticle;
import com.airbus.boa.explorer.tree.TreeBuilding;
import com.airbus.boa.explorer.tree.TreeCategory;
import com.airbus.boa.explorer.tree.TreeExternalEntity;
import com.airbus.boa.explorer.tree.TreeInstallation;
import com.airbus.boa.explorer.tree.TreePlace;
import com.airbus.boa.explorer.tree.TreeTool;
import com.airbus.boa.explorer.view.ExplorerController.ChildrenCriterion;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.Location;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage writing of a hierarchical explorer in Excel files
 */
public class IOExcelWriterExplorer extends IOExcelWriterBaseWriter {
    
    private int depthColumn = 0;
    
    private ChildrenCriterion childrenMode;
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the workbook in which to write
     * @param pColumns
     *            the columns
     * @param pChildrenMode
     *            the current children criterion
     * @throws ExportException
     *             when the workbook is null
     */
    public IOExcelWriterExplorer(Workbook pWorkbook, Columns pColumns,
            ChildrenCriterion pChildrenMode) throws ExportException {
        
        super(pWorkbook, pColumns, IOConstants.ExplorerSheetName);
        
        childrenMode = pChildrenMode;
        
        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(true);
        depthColumn = pColumns.getMinColumnIndex();
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (object != null) {
            write(object, 0);
        }
    }
    
    @Override
    public void writeHeader() {
        
        // Insert the export date on first row
        Row lRow = sheet.createRow(0);
        writeCell(lRow, depthColumn,
                MessageBundle.getMessage(IOConstants.EXPORT_DATE),
                Styles.HEADER_DEFAULT);
        writeCellDate(lRow, depthColumn + 1, new Date(), Styles.DATE_HHMM);
        rowNumber++;
        
        super.writeHeader(1, false);
    }
    
    private void write(Object pObject, final int pDepth) throws ExportException {
        
        if (!(pObject instanceof TreeNode)) {
            throw new ExportException("Invalid class - must extend TreeNode<?>");
        }
        
        // Write current object
        
        TreeNode lTreeNode = (TreeNode) pObject;
        
        if (lTreeNode instanceof TreeBuilding) {
            
            writeBuilding(getRow(), ((TreeBuilding) lTreeNode).getName(),
                    pDepth);
        }
        else if (lTreeNode instanceof TreeExternalEntity) {
            
            writeExternalEntity(getRow(),
                    ((TreeExternalEntity) lTreeNode).getName(), pDepth);
        }
        else if (lTreeNode instanceof TreePlace) {
            
            writePlace(getRow(), ((TreePlace) lTreeNode).getPlace(), pDepth);
        }
        else if (lTreeNode instanceof TreeCategory) {
            
            writeCategory(getRow(), ((TreeCategory) lTreeNode).getName(),
                    pDepth);
        }
        else if (lTreeNode instanceof TreeInstallation) {
            
            writeInstallation(getRow(),
                    ((TreeInstallation) lTreeNode).getInstallation(), pDepth);
        }
        else if (lTreeNode instanceof TreeTool) {
            
            writeTool(getRow(), ((TreeTool) lTreeNode).getTool(), pDepth);
        }
        else if (lTreeNode instanceof TreeArticle) {
            
            writeArticle(getRow(), ((TreeArticle) lTreeNode).getArticle(),
                    pDepth);
        }
        
        // Write current object children
        
        // Current object TreeNode is not the root node of the tree
        if (!(lTreeNode instanceof TreeNodeImpl)) {
            // change the line since the object has been previously written
            rowNumber++;
        }
        
        if (!lTreeNode.isLeaf()) {
            
            int lParentRow = rowNumber;
            
            Iterator<Object> lChildrenIterator =
                    lTreeNode.getChildrenKeysIterator();
            
            if (lChildrenIterator != null) {
                while (lChildrenIterator.hasNext()) {
                    TreeNode lEntry = lTreeNode.getChild(
                            lChildrenIterator.next());
                    if (lEntry != null) {
                        write(lEntry, pDepth + 1);
                    }
                }
            }
            
            int lLastChildRow = rowNumber - 1;
            
            // Current object TreeNode is not the root node of the tree
            if (!(lTreeNode instanceof TreeNodeImpl)) {
                sheet.groupRow(lParentRow, lLastChildRow);
            }
        }
    }
    
    private void writeArticle(Row pRow, Article pArticle, final int pDepth) {
        
        writeCell(pRow, pDepth, pArticle.getName(), null);
        writeEmplyLine(pRow);
        
        writeField(pRow,
                MessageBundle.getMessage(pArticle.getClass().getSimpleName()),
                IOConstants.CLASS_ARTICLE_TITLE);
        writeField(pRow, pArticle.getName(), IOConstants.NAME_TITLE);
        writeField(pRow, pArticle.getAirbusSN(), IOConstants.AIRBUS_SN_TITLE);
        writeField(pRow, pArticle.getAirbusPN(), IOConstants.AIRBUS_PN_TITLE);
        writeField(pRow, pArticle.getManufacturerSN(),
                IOConstants.MANUFACTURER_SN_TITLE);
        writeField(pRow, pArticle.getManufacturerPN(),
                IOConstants.MANUFACTURER_PN_TITLE);
        writeField(pRow, pArticle.getTypeArticle(), IOConstants.TYPE_TITLE);
        
        switch (childrenMode) {
        case LOCATION:
            if (pArticle.getLocation() != null) {
                Location lLocation = pArticle.getLocation();
                writeField(pRow, lLocation.getPrecision(),
                        IOConstants.LOCATION_OR_DETAILS_TITLE);
            }
            break;
        case CONTAINER:
            if (pArticle.getContainer() != null) {
                writeField(pRow, Article.computeContainerDetails(pArticle),
                        IOConstants.LOCATION_OR_DETAILS_TITLE);
            }
            break;
        default:
        }
        
        if (pArticle instanceof PC) {
            PC computer = (PC) pArticle;
            writeField(pRow, computer.getComment(),
                    IOConstants.GENERAL_COMMENT_TITLE, Styles.WRAPTEXT);
            writeField(pRow, computer.getFunction(),
                    IOConstants.DESIGNATION_OR_FUNCTION_TITLE);
            
        }
        else {
            
            if (pArticle.getHistory() != null
                    && pArticle.getHistory().getGeneralComment() != null) {
                writeField(pRow, pArticle.getHistory().getGeneralComment(),
                        IOConstants.GENERAL_COMMENT_TITLE, Styles.WRAPTEXT);
            }
            else {
                writeField(pRow, "", IOConstants.GENERAL_COMMENT_TITLE);
            }
        }
        
        if (pArticle instanceof Rack) {
            writeField(pRow, ((Rack) pArticle).getDesignation(),
                    IOConstants.DESIGNATION_OR_FUNCTION_TITLE);
        }
        if (pArticle instanceof Cabinet) {
            writeField(pRow, ((Cabinet) pArticle).getDesignation(),
                    IOConstants.DESIGNATION_OR_FUNCTION_TITLE);
        }
        
        // software products list
        StringBuffer lSoftwareSB = new StringBuffer();
        for (Software lSoftware : pArticle.getSoftwares()) {
            lSoftwareSB.append(lSoftware.getCompleteName()).append("\n");
        }
        writeField(pRow, lSoftwareSB.toString().trim(),
                IOConstants.INSTALLED_SOFTWARE_TITLE, Styles.WRAPTEXT);
        
        // communication ports
        List<CommunicationPort> lCommunicationPorts = null;
        if (pArticle instanceof PC) {
            PC lPC = (PC) pArticle;
            lCommunicationPorts = lPC.getPorts();
        }
        else if (pArticle instanceof Board) {
            Board lBoard = (Board) pArticle;
            lCommunicationPorts = lBoard.getPorts();
        }
        
        if (lCommunicationPorts != null) {
            int lParentRow = rowNumber + 1;
            
            for (CommunicationPort lCommPort : lCommunicationPorts) {
                rowNumber++;
                if (lCommPort != null) {
                    writeCommunicationPort(getRow(), lCommPort);
                }
            }
            
            int lLastChildRow = rowNumber;
            
            sheet.groupRow(lParentRow, lLastChildRow);
        }
        
    }
    
    private void writeCommunicationPort(Row pRow, CommunicationPort pPort) {
        
        writeEmplyLine(pRow);
        
        writeField(pRow, MessageBundle.getMessage(CommunicationPort.class
                .getSimpleName()), IOConstants.CLASS_ARTICLE_TITLE);
        writeField(pRow, pPort.getName(), IOConstants.NAME_TITLE);
        writeField(pRow, pPort.getIpAddress(), IOConstants.IP_ADDRESS_TITLE);
        writeField(pRow, pPort.getFixedIPString(), IOConstants.FIXED_IP_TITLE);
        writeField(pRow, pPort.getMask(), IOConstants.NETWORK_MASK_TITLE);
        writeField(pRow, pPort.getMacAddress(), IOConstants.MAC_ADDRESS_TITLE);
        writeValueListField(pRow, pPort.getNetwork(), IOConstants.NETWORK_TITLE);
        writeField(pRow, pPort.getSocket(), IOConstants.SOCKET_TITLE);
        writeField(pRow, pPort.getComment(), IOConstants.GENERAL_COMMENT_TITLE);
    }
    
    private void writeExternalEntity(Row pRow, String pName, int pDepth) {
        
        writeCell(pRow, pDepth, pName, null);
        writeEmplyLine(pRow);
        
        writeField(pRow, pName, IOConstants.NAME_TITLE);
        writeField(pRow,
                MessageBundle.getMessage(ExternalEntity.class.getSimpleName()),
                IOConstants.CLASS_ARTICLE_TITLE);
    }
    
    private void writeBuilding(Row pRow, String pName, int pDepth) {
        
        writeCell(pRow, pDepth, pName, null);
        writeEmplyLine(pRow);
        
        writeField(pRow, pName, IOConstants.NAME_TITLE);
        writeField(pRow,
                MessageBundle.getMessage(Building.class.getSimpleName()),
                IOConstants.CLASS_ARTICLE_TITLE);
    }
    
    private void writeInstallation(Row pRow, Installation pInstallation,
            int pDepth) {
        
        writeCell(pRow, pDepth, pInstallation.getName(), null);
        writeEmplyLine(pRow);
        
        writeField(pRow, pInstallation.getName(), IOConstants.NAME_TITLE);
        writeField(pRow,
                MessageBundle.getMessage(Installation.class.getSimpleName()),
                IOConstants.CLASS_ARTICLE_TITLE);
        if (pInstallation.getLocation() != null) {
            Location lLocation = pInstallation.getLocation();
            writeField(pRow, lLocation.getLocationName(),
                    IOConstants.LOCATION_OR_DETAILS_TITLE);
        }
    }
    
    private void writePlace(Row pRow, Place pPlace, int pDepth) {
        
        writeCell(pRow, pDepth, pPlace.getName(), null);
        writeEmplyLine(pRow);
        
        writeField(pRow, pPlace.getName(), IOConstants.NAME_TITLE);
        writeField(
                pRow,
                MessageBundle.getMessage("placeType"
                        + pPlace.getType().toString()),
                IOConstants.CLASS_ARTICLE_TITLE);
        
        if (pPlace.getBuilding() != null) {
            writeField(pRow, pPlace.getBuilding().getName(),
                    IOConstants.LOCATION_OR_DETAILS_TITLE);
        }
    }
    
    private void writeCategory(Row pRow, String pName, int pDepth) {
        
        writeCell(pRow, pDepth, pName, null);
        writeEmplyLine(pRow);
        writeField(pRow, pName, IOConstants.NAME_TITLE);
    }
    
    private void writeTool(Row pRow, Tool pTool, final int pDepth) {
        
        writeCell(pRow, pDepth, pTool.getName(), null);
        writeEmplyLine(pRow);
        
        writeField(pRow,
                MessageBundle.getMessage(pTool.getClass().getSimpleName()),
                IOConstants.CLASS_ARTICLE_TITLE);
        writeField(pRow, pTool.getName(), IOConstants.NAME_TITLE);
        
        switch (childrenMode) {
        case LOCATION:
            if (pTool.getLocation() != null) {
                Location lLocation = pTool.getLocation();
                writeField(pRow, lLocation.getPrecision(),
                        IOConstants.LOCATION_OR_DETAILS_TITLE);
            }
            break;
        case CONTAINER:
            if (pTool.getContainer() != null) {
                Container lContainer = pTool.getContainer();
                writeField(pRow, lContainer.getPrecision(),
                        IOConstants.LOCATION_OR_DETAILS_TITLE);
            }
            break;
        default:
        }
        
        writeField(pRow, pTool.getGeneralComment(),
                IOConstants.GENERAL_COMMENT_TITLE, Styles.WRAPTEXT);
        
        writeField(pRow, pTool.getDesignation(),
                IOConstants.DESIGNATION_OR_FUNCTION_TITLE);
    }
    
    @Override
    public void applyStyles() {
        
        super.applyStyles();
        if (depthColumn > 0) {
            for (int i = 0; i < depthColumn; i++) {
                sheet.setColumnWidth(i, 5);
            }
            
            sheet.groupColumn(0, depthColumn - 1);
            sheet.setColumnGroupCollapsed(0, true);
        }
    }
    
    private void writeEmplyLine(Row pRow) {
        
        Iterator<Entry<String, Integer>> lIterator =
                columns.getDefinition().iterator();
        while (lIterator.hasNext()) {
            Entry<String, Integer> lEntry = lIterator.next();
            writeField(pRow, "", lEntry.getKey());
        }
    }
    
}
