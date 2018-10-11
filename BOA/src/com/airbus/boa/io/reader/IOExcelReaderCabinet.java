/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderCabinet
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnCabinet;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.util.ExceptionUtil;

/**
 * Manage reading of a Cabinet into Excel files
 */
public class IOExcelReaderCabinet extends IOExcelReaderAbstractArticle {
    
    private static String sheetname = IOConstants.CabinetSheetName;
    
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
    public IOExcelReaderCabinet(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        
        super(workbook, columns, sheetname, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        
        if (columns == null) {
            this.columns = new ColumnCabinet();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        Cabinet cabinet = new Cabinet();
        
        readLine(cabinet);
        
        String designation = readField(row, IOConstants.DESIGNATION_TITLE);
        cabinet.setDesignation(designation);
        
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
        String lIdLetterOrPrecision =
                readField(row,
                        IOConstants.IDENTIFICATION_LETTER_OR_PRECISION_TITLE);
        
        Container lContainer =
                readContainer(cabinet, lContainerTypeStr, lContainerStr,
                        lIdLetterOrPrecision);
        
        Location lLocation =
                readLocation(cabinet, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        try {
            cabinet = articleBean.createArticle(cabinet, lLocation, lContainer);
        }
        catch (Exception e) {
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION IMPOSSIBLE : " + msg,
                    row.getRowNum(), -1);
        }
    }
    
}
