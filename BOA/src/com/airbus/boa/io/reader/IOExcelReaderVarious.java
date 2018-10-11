/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderVarious
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnVarious;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.util.ExceptionUtil;

/**
 * Manage reading of a Various article into Excel files
 */
public class IOExcelReaderVarious extends IOExcelReaderAbstractArticle {
    
    private static String sheetname = IOConstants.VariousSheetName;
    
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
    public IOExcelReaderVarious(Workbook workbook, Columns columns,
            LocationFactory pLocationFactory,
            ContainerFactory pContainerFactory, ArticleBean articleBean,
            ValueListBean valueListBean, User user) {
        
        super(workbook, columns, sheetname, pLocationFactory,
                pContainerFactory, articleBean, valueListBean, user);
        
        if (columns == null) {
            this.columns = new ColumnVarious();
        }
    }
    
    @Override
    public void readLine() throws ImportException {
        Various various = new Various();
        
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
        String lContainerDetails =
                readField(row, IOConstants.CONTAINER_DETAILS_TITLE);
        
        Container lContainer =
                readContainer(various, lContainerTypeStr, lContainerStr,
                        lContainerDetails);
        
        Location lLocation =
                readLocation(various, lLocationStr, lLocationDetails,
                        lExternalLocationStr, lInheritedStr, lContainer);
        
        readLine(various);
        
        try {
            various = articleBean.createArticle(various, lLocation, lContainer);
        }
        catch (Exception e) {
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION VARIOUS IMPOSSIBLE : " + msg,
                    row.getRowNum(), -1);
        }
    }
    
}
