/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderBoard
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.Date;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnBoard;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.util.ExceptionUtil;

/**
 * Manage reading of a Board into Excel files
 */
public class IOExcelReaderBoard extends IOExcelReaderAbstractArticle {
    
    private static String sheetname = IOConstants.BoardSheetName;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the Excel workbook from which to read
     * @param columns
     *            the columns into Excel files
     * @param pLocationFactory
     *            the locationFactory to use during process
     * @param pContainerFactory
     *            the containerFactory to use during process
     * @param articleBean
     *            the articleBean to use during process
     * @param valueListBean
     *            the valueListBean to use during process
     * @param user
     *            the user logged in
     */
    public IOExcelReaderBoard(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        
        super(workbook, columns, sheetname, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        if (columns == null) {
            this.columns = new ColumnBoard();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        Board board = new Board();
        readLine(board);
        
        String ipaddress, macaddress, bootLoader, calibrationStr;
        
        ipaddress = readField(row, IOConstants.IP_ADDRESS_TITLE);
        macaddress = readField(row, IOConstants.MAC_ADDRESS_TITLE);
        bootLoader = readField(row, IOConstants.BOOT_LOADER_TITLE);
        calibrationStr = readField(row, IOConstants.CALIBRATION_TITLE);
        
        board.setBootLoader(bootLoader);
        board.setIpAddress(ipaddress);
        board.setMacAddress(macaddress);
        
        String revH, revS;
        
        revH = readField(row, IOConstants.REV_H_TITLE);
        revS = readField(row, IOConstants.REV_S_TITLE);
        board.setRevH(revH);
        board.setRevS(revS);
        
        Date lActiveStockControlDate =
                readDateField(row, IOConstants.ACTIVE_STOCK_CONTROL_DATE_TITLE);
        board.setActiveStockControlDate(lActiveStockControlDate);
        
        // Check if type is Venusia
        String typeStr = readField(row, IOConstants.TYPE_TITLE);
        if (typeStr != null && typeStr.matches(IOConstants.REGEX_VENUSIA)) {
            board.setCalibrated(readCalibration(calibrationStr));
        }
        
        // Location
        String lLocationStr = readField(row, IOConstants.LOCATION_TITLE);
        String lLocationDetails =
                readField(row, IOConstants.LOCATION_DETAILS_TITLE);
        String lInheritedStr =
                readField(row, IOConstants.INHERITED_LOCATION_TITLE);
        
        // External location
        String lExternalLocationStr =
                readField(row, IOConstants.EXTERNAL_LOCATION_NAME_TITLE);
        
        // Container
        String lContainerTypeStr =
                readField(row, IOConstants.CONTAINER_TYPE_TITLE);
        String lContainerStr = readField(row, IOConstants.CONTAINER_TITLE);
        String lSlotNumberOrPrecisionStr =
                readField(row,
                        IOConstants.SLOTNUMBERPOSITION_OR_PRECISION_TITLE);
        
        Container lContainer =
                readContainer(board, lContainerTypeStr, lContainerStr,
                        lSlotNumberOrPrecisionStr);
        
        Location lLocation =
                readLocation(board, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        // Article creation
        try {
            board = articleBean.createArticle(board, lLocation, lContainer);
        }
        catch (Exception e) {
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION IMPOSSIBLE : " + msg,
                    row.getRowNum(), -1);
        }
        
    }
    
    private Boolean readCalibration(String calibrationStr) {
        if (calibrationStr == null) {
            return null;
        }
        
        Boolean result = null;
        for (String vrai : IOConstants.TRUE_STRINGS) {
            if (calibrationStr.equalsIgnoreCase(vrai)) {
                result = true;
            }
        }
        for (String faux : IOConstants.FALSE_STRINGS) {
            if (calibrationStr.equalsIgnoreCase(faux)) {
                result = false;
            }
        }
        return result;
    }
    
}
