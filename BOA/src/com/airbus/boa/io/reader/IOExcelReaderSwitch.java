/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderSwitch
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnSwitch;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.util.ExceptionUtil;

/**
 * Manage reading of a Switch into Excel files
 */
public class IOExcelReaderSwitch extends IOExcelReaderAbstractArticle {
    
    private static String sheetname = IOConstants.SwitchSheetName;
    
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
    public IOExcelReaderSwitch(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        
        super(workbook, columns, sheetname, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        
        if (columns == null) {
            this.columns = new ColumnSwitch();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        Switch lSwitch = new Switch();
        
        readLine(lSwitch);
        
        String ipaddress;
        ipaddress = readField(row, IOConstants.IP_ADDRESS_TITLE);
        lSwitch.setIpAddress(ipaddress);
        
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
        String lSwitchPrecision =
                readField(row, IOConstants.SWITCH_POSITION_PRECISION_TITLE);
        
        Container lContainer =
                readContainer(lSwitch, lContainerTypeStr, lContainerStr,
                        lSwitchPrecision);
        
        Location lLocation =
                readLocation(lSwitch, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        try {
            lSwitch = articleBean.createArticle(lSwitch, lLocation, lContainer);
        }
        catch (Exception e) {
            throw new ImportException("CREATION IMPOSSIBLE : "
                    + ExceptionUtil.getMessage(e), row.getRowNum(), -1);
        }
    }
    
}
