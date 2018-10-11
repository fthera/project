/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderEquipment
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnEquipment;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * @author ng0057cf
 */
public class IOExcelReaderEquipment extends IOExcelBaseReader {
    
    private static final String sheetname = IOConstants.EquipmentSheetName;
    
    private ArticleBean articleBean;
    private SoftwareBean softwareBean;
    
    private String login;
    
    public IOExcelReaderEquipment(Workbook workbook, Columns columns,
            ArticleBean articleBean, PCBean pcBean, SoftwareBean softwareBean,
            ValueListBean valueListBean, String login) {
        super(valueListBean, workbook, columns, sheetname);
        
        if (columns == null) {
            this.columns = new ColumnEquipment();
        }
        this.articleBean = articleBean;
        this.softwareBean = softwareBean;
        this.login = login;
    }
    
    @Override
    public void readLine() throws ImportException {
        
        String lCompleteName = readField(row, IOConstants.SOFTWARE_TITLE);
        String forArticle = readField(row, IOConstants.FOR_ARTICLE_TITLE);
        String asn = readField(row, IOConstants.AIRBUS_SN_TITLE);
        String msn = readField(row, IOConstants.MANUFACTURER_SN_TITLE);
        
        checkNotEmptyField(row, lCompleteName, IOConstants.SOFTWARE_TITLE);
        checkNotEmptyField(row, forArticle, IOConstants.FOR_ARTICLE_TITLE);
        
        // Check Article identification
        if (asn == null && msn == null) {
            
            String msg =
                    MessageBundle
                            .getMessage(IOConstants.ASN_OR_MSN_MUST_BE_FILLED);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        List<Article> articles =
                articleBean.findArticleByASNandMSN(asn, msn, true);
        if (articles.isEmpty()) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.BOARD_OR_PC_NOT_FOUND, new Object[] {
                                    asn, msn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        if (articles.size() > 1) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.MORE_THAN_ONE_ARTICLE_FOUND,
                            new Object[] { asn, msn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        Article article = articles.get(0);
        
        if (!(article instanceof PC || article instanceof Board)) {
            
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.PORT_ONLY_FOR_PC_AND_BOARD,
                            new Object[] { asn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        // found article family shall be the same as the provided one
        if (!forArticle.equals(article.getClass().getSimpleName())) {
            String msg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { IOConstants.FOR_ARTICLE_TITLE,
                                    forArticle });
            throw new ImportException(msg, row.getRowNum(),
                    getColumnIndex(IOConstants.FOR_ARTICLE_TITLE));
        }
        
        // Check Software identification
        Software software = null;
        
        software = softwareBean.findByCompleteName(lCompleteName);
        
        if (softwareBean.findCountByCompleteName(lCompleteName) > 1) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.SOFTWARE_TO_MANY_MATCH_COMPLETENAME,
                            new Object[] { lCompleteName });
            throw new ImportException(msg, row.getRowNum(),
                    getColumnIndex(IOConstants.SOFTWARE_TITLE));
        }
        
        if (software == null) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.SOFTWARE_NOT_FOUND,
                            new Object[] { lCompleteName });
            throw new ImportException(msg, row.getRowNum(),
                    getColumnIndex(IOConstants.SOFTWARE_TITLE));
        }
        
        // check if software is already installed
        if (software.getEquipments().contains(article)) {
            
            // software is already installed,
            // check if it is not the default OS of the article (if it is a PC)
            if (!((article instanceof PC) && software.equals(((PC) article)
                    .getDefaultOS()))) {
                
                String msg =
                        MessageBundle
                                .getMessageResource(
                                        IOConstants.SOFTWARE_ALREADY_DEPLOYED_TO_EQUIPMENT,
                                        new Object[] {
                                                lCompleteName,
                                                article.getName(),
                                                article.getClass()
                                                        .getSimpleName() });
                throw new ImportException(msg, row.getRowNum(), -1);
            }
            // if software is already installed because it is the default OS of
            // the article (PC), ignore the line but do not throw any exception
            // since it may have been installed during this import process when
            // creating the PC
        }
        else {
            try {
                
                List<Article> equipments = new ArrayList<Article>();
                equipments.addAll(software.getEquipments());
                equipments.add(article);
                
                softwareBean.merge(software, equipments, login);
            }
            catch (Exception e) {
                String msg =
                        "DEPLOYEMENT IMPOSSIBLE :"
                                + ExceptionUtil.getMessage(e);
                log.warning(msg);
                throw new ImportException(msg, row.getRowNum(), -1);
                
            }
        }
        
    }
    
}
