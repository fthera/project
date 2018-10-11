/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderType
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnType;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Manage reading of an ArticleType into Excel files
 */
public class IOExcelReaderType extends IOExcelBaseReader {
    
    private static String sheetname = IOConstants.TypeSheetName;
    private ArticleBean articleBean;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook to read
     * @param columns
     *            the columns to read
     * @param articlebean
     *            the articleBean to use
     * @param valueListBean
     *            the ValueListBean to use
     */
    public IOExcelReaderType(Workbook workbook, Columns columns,
            ArticleBean articlebean, ValueListBean valueListBean) {
        super(valueListBean, workbook, columns, sheetname);
        if (columns == null) {
            this.columns = new ColumnType();
        }
        
        articleBean = articlebean;
        
    }
    
    @Override
    public void readLine() throws ImportException {
        String forArticle, type;
        
        forArticle = readField(row, IOConstants.FOR_ARTICLE_TITLE);
        type = readField(row, IOConstants.TYPE_TITLE);
        String lManufacturer = readField(row, IOConstants.MANUFACTURER_TITLE);
        
        if (type == null || forArticle == null) {
            throw new ImportException("TYPE OU FORARTICLE VIDE",
                    row.getRowNum(), columns.getIndex(IOConstants.TYPE_TITLE));
        }
        
        // Check if article type already exists
        TypeArticle typeArt = articleBean.findTypeArticleByName(type);
        if (typeArt != null) {
            
            if (typeArt.getClass().getSimpleName().equals("Type" + forArticle)) {
                throw new ImportException(MessageBundle.getMessageResource(
                        IOConstants.TYPEARTICLE_ALREADY_EXISTS,
                        new Object[] { type }), row.getRowNum(), -1);
            }
            else {
                throw new ImportException(
                        MessageBundle
                                .getMessage(Constants.TYPELABEL_ALREADY_USED),
                        row.getRowNum(), -1);
            }
        }
        
        // determine type class
        Class<?> clazz = null;
        TypeArticle typeArticle = null;
        try {
            clazz =
                    Class.forName("com.airbus.boa.entity.article.type.Type"
                            + forArticle);
            typeArticle = (TypeArticle) clazz.newInstance();
        }
        catch (Exception e) {
            // catch of InstantiationException, IllegalAccessException,
            // ClassNotFoundException
            Object[] args = { getRownumber(), forArticle };
            log.warning(ExceptionUtil.getMessage(e));
            throw new ImportException(MessageBundle.getMessageResource(
                    IOConstants.ROW_INCORRECT_FIELD, args), row.getRowNum(),
                    columns.getIndex(IOConstants.FOR_ARTICLE_TITLE));
        }
        
        typeArticle.setLabel(type);
        typeArticle.setManufacturer(lManufacturer);
        
        try {
            articleBean.createTypeArticle(typeArticle);
        }
        catch (Exception e) {
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION IMPOSSIBLE : " + msg,
                    row.getRowNum(), -1);
            
        }
        
    }
    
}
