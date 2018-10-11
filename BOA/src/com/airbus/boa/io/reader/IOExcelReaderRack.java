/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderRack
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnRack;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.util.ExceptionUtil;

/**
 * Manage reading of a Rack into Excel files
 */
public class IOExcelReaderRack extends IOExcelReaderAbstractArticle {
    
    private static String sheetname = IOConstants.RackSheetName;
    
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
    public IOExcelReaderRack(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        super(workbook, columns, sheetname, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        
        if (columns == null) {
            this.columns = new ColumnRack();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        Rack rack = new Rack();
        
        readLine(rack);
        
        String designation = readField(row, IOConstants.DESIGNATION_TITLE);
        
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
        String lRackPrecision =
                readField(row, IOConstants.RACK_POSITION_PRECISION_TITLE);
        
        Container lContainer =
                readContainer(rack, lContainerTypeStr, lContainerStr,
                        lRackPrecision);
        
        Location lLocation =
                readLocation(rack, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        rack.setDesignation(designation);
        
        try {
            rack = articleBean.createArticle(rack, lLocation, lContainer);
        }
        catch (Exception e) {
            throw new ImportException("CREATION IMPOSSIBLE : "
                    + ExceptionUtil.getMessage(e), row.getRowNum(), -1);
        }
    }
    
}
