/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderObsolescence
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.obso.ObsolescenceReference;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnObsolescence;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.ExportController;

/**
 * Manage reading of obsolescence data into Excel files
 */
public class IOExcelReaderObsolescence extends IOExcelBaseReader {
    
    private final static String sheetname =
            IOConstants.ObsolescenceDataSheetName;
    private ObsolescenceBean obsoBean;
    private ArticleBean articleBean;
    private SoftwareBean softwareBean;
    
    /**
     * Constructor
     * 
     * @param valueListBean
     *            the valueListBean to use
     * @param workbook
     *            the workbook to read
     * @param columns
     *            the columns list
     * @param obsoBean
     *            the obsoBean to use
     */
    public IOExcelReaderObsolescence(Workbook workbook, Columns columns,
            ValueListBean valueListBean, ObsolescenceBean obsoBean) {
        super(valueListBean, workbook, columns, sheetname);
        
        if (columns == null) {
            this.columns = new ColumnObsolescence();
        }
        this.obsoBean = obsoBean;
        articleBean = obsoBean.getArticleBean();
        softwareBean = obsoBean.getSoftwareBean();
        
    }
    
    @Override
    public void readLine() throws ImportException {
        
        ObsolescenceData obso = new ObsolescenceData();
        
        String airbusStatus, manufacturerStatus, currentAction, strategy, consultPeriod;
        
        // ECRITURE DES CHAMPS OPTIONNELS D'OBSOLESCENCE POUR LES LISTES
        // MODIFIABLES
        airbusStatus = readField(row, IOConstants.OBSO_AIRBUS_STATUS_TITLE);
        if (airbusStatus != null) {
            obso.setAirbusStatus(readValueList(row, airbusStatus,
                    IOConstants.OBSO_AIRBUS_STATUS_TITLE, AirbusStatus.class));
        }
        
        manufacturerStatus =
                readField(row, IOConstants.OBSO_MANUFACTURER_STATUS_TITLE);
        if (manufacturerStatus != null) {
            obso.setManufacturerStatus(readValueList(row, manufacturerStatus,
                    IOConstants.OBSO_MANUFACTURER_STATUS_TITLE,
                    ManufacturerStatus.class));
        }
        
        currentAction = readField(row, IOConstants.OBSO_CURRENT_ACTION_TITLE);
        if (currentAction != null) {
            obso.setCurrentAction(readValueList(row, currentAction,
                    IOConstants.OBSO_CURRENT_ACTION_TITLE, ActionObso.class));
        }
        
        strategy = readField(row, IOConstants.OBSO_STRATEGY_TITLE);
        if (strategy != null) {
            obso.setStrategyKept(readValueList(row, strategy,
                    IOConstants.OBSO_STRATEGY_TITLE, Strategy.class));
        }
        
        consultPeriod = readField(row, IOConstants.OBSO_CONSULT_PERIOD_TITLE);
        if (consultPeriod != null) {
            obso.setConsultPeriod(readValueList(row, consultPeriod,
                    IOConstants.OBSO_CONSULT_PERIOD_TITLE, ConsultPeriod.class));
        }
        
        obso.setSupplier(readField(row, IOConstants.OBSO_SUPPLIER_TITLE));
        obso.setPersonInCharge(readField(row,
                IOConstants.OBSO_IN_CHARGE_OF_TITLE));
        obso.setCommentOnStrategy(readField(row, IOConstants.COMMENTS_TITLE));
        
        obso.setContinuityDate(readDateField(row,
                IOConstants.OBSO_CONTINUITY_DATE_TITLE));
        obso.setEndOfOrderDate(readDateField(row,
                IOConstants.OBSO_END_OF_ORDER_TITLE));
        obso.setEndOfProductionDate(readDateField(row,
                IOConstants.OBSO_END_OF_PRODUCTION_TITLE));
        obso.setEndOfSupportDate(readDateField(row,
                IOConstants.OBSO_END_OF_SUPPORT_TITLE));
        obso.setObsolescenceDate(readDateField(row, IOConstants.OBSO_DATE_TITLE));
        obso.setLastObsolescenceUpdate(readDateField(row,
                IOConstants.OBSO_LAST_UPDATE_TITLE));
        
        Double value = readNumericField(row, IOConstants.OBSO_MTBF_TITLE);
        Long mtbf = (value != null) ? value.longValue() : null;
        obso.setMtbf(mtbf);
        
        ObsolescenceReference lReference = readReference(row);
        obso.setReference(lReference);
        
        try {
            obso = obsoBean.create(obso);
        }
        catch (Exception e) {
            
            String msg = ExceptionUtil.getMessage(e);
            throw new ImportException("CREATION OBSO IMPOSSIBLE: " + msg,
                    row.getRowNum(), -1);
        }
    }
    
    private ObsolescenceReference readReference(Row pRow)
            throws ImportException {
        
        String forItem, type, referenceStr;
        int rowNum = pRow.getRowNum();
        
        forItem = readField(pRow, IOConstants.OBSO_FOR_ITEM_TITLE);
        type = readField(pRow, IOConstants.OBSO_TYPE_ARTICLE_TITLE);
        referenceStr = readField(pRow, IOConstants.OBSO_CONSTITUANT_NAME_TITLE);
        
        // reference column is mandatory for Airbus or Manufacturer PN, and
        // Software
        if (referenceStr == null
                && !ExportController.OBSO_FOR_ITEM_PC_TYPE.equals(forItem)) {
            
            String msg =
                    MessageBundle
                            .getMessage(IOConstants.REFERENCE_MANDATORY_FOR_PN_AND_SOFTWARE);
            throw new ImportException(msg, rowNum, -1);
        }
        
        ObsolescenceReference lObsolescenceReference;
        
        if (ExportController.OBSO_FOR_ITEM_AIRBUS_PN_ARTICLE_TYPE
                .equals(forItem)) {
            
            AirbusPN lAirbusPN = articleBean.findAirbusPNByName(referenceStr);
            
            if (lAirbusPN == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.AIRBUS_PN_NOT_FOUND,
                                new Object[] { referenceStr });
                throw new ImportException(lMsg, rowNum,
                        getColumnIndex(
                                IOConstants.OBSO_CONSTITUANT_NAME_TITLE));
            }
            
            TypeArticle lAirbusTypeArticle =
                    articleBean.findTypeArticleByName(type);
            
            if (lAirbusTypeArticle == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                IOConstants.TYPEARTICLE_NOT_FOUND,
                                new Object[] { type });
                throw new ImportException(lMsg, rowNum,
                        getColumnIndex(IOConstants.OBSO_TYPE_ARTICLE_TITLE));
            }
            
            lObsolescenceReference =
                    new ObsolescenceReference(lAirbusPN, lAirbusTypeArticle);
            
            checkPN(rowNum, lObsolescenceReference);
            
            return lObsolescenceReference;
            
        }
        else if (ExportController.OBSO_FOR_ITEM_MANUFACTURER_PN_ARTICLE_TYPE
                .equals(forItem)) {
            
            ManufacturerPN lManufacturerPN =
                    articleBean.findManufacturerPNByName(referenceStr);
            
            if (lManufacturerPN == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.MANUFACTURER_PN_NOT_FOUND,
                                new Object[] { referenceStr });
                throw new ImportException(lMsg, rowNum,
                        getColumnIndex(
                                IOConstants.OBSO_CONSTITUANT_NAME_TITLE));
            }
            
            TypeArticle lManufacturerTypeArticle =
                    articleBean.findTypeArticleByName(type);
            
            if (lManufacturerTypeArticle == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                IOConstants.TYPEARTICLE_NOT_FOUND,
                                new Object[] { type });
                throw new ImportException(lMsg, rowNum,
                        getColumnIndex(IOConstants.OBSO_TYPE_ARTICLE_TITLE));
            }
            
            lObsolescenceReference =
                    new ObsolescenceReference(lManufacturerPN,
                            lManufacturerTypeArticle);
            
            checkPN(rowNum, lObsolescenceReference);
            
            return lObsolescenceReference;
        }
        
        else if (ExportController.OBSO_FOR_ITEM_PC_TYPE.equals(forItem)) {
            
            TypePC lTypePC = articleBean.findTypePCByName(type);
            
            if (lTypePC == null) {
                String msg =
                        MessageBundle.getMessageResource(
                                IOConstants.TYPEPC_NOT_FOUND,
                                new Object[] { type });
                throw new ImportException(msg, rowNum,
                        getColumnIndex(IOConstants.OBSO_TYPE_ARTICLE_TITLE));
            }
            
            lObsolescenceReference = new ObsolescenceReference(lTypePC);
            
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lObsolescenceReference)) {
                
                String msg =
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE);
                throw new ImportException(msg, rowNum, -1);
            }
            
            return lObsolescenceReference;
        }
        else if (ExportController.OBSO_FOR_ITEM_SOFTWARE.equals(forItem)) {
            
            Software software = softwareBean.findByCompleteName(referenceStr);
            
            if (software == null) {
                
                String msg =
                        MessageBundle.getMessageResource(
                                IOConstants.SOFTWARE_NOT_FOUND,
                                new Object[] { referenceStr });
                throw new ImportException(msg, rowNum,
                        getColumnIndex(
                                IOConstants.OBSO_CONSTITUANT_NAME_TITLE));
            }
            
            lObsolescenceReference = new ObsolescenceReference(software);
            
            if (obsoBean
                    .isReferenceAlreadyManagedIntoObso(lObsolescenceReference)) {
                
                String msg =
                        MessageBundle
                                .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE);
                throw new ImportException(msg, rowNum, -1);
            }
            return lObsolescenceReference;
        }
        
        return null;
    }
    
    private void checkPN(int pRowNum,
            ObsolescenceReference pObsolescenceReference)
            throws ImportException {
        
        PN lPN;
        TypeArticle lTypeArticle;
        
        switch (pObsolescenceReference.getReferenceType()) {
        case AIRBUSPN_TYPEARTICLE:
            lPN = pObsolescenceReference.getAirbusPN();
            lTypeArticle = pObsolescenceReference.getTypeArticle();
            break;
        case MANUFACTURERPN_TYPEARTICLE:
            lPN = pObsolescenceReference.getManufacturerPN();
            lTypeArticle = pObsolescenceReference.getTypeArticle();
            break;
        case SOFTWARE:
        case TYPEPC:
        default:
            // this case shall not happen
            throw new ImportException();
        }
        
        if (obsoBean.isReferenceAlreadyManagedIntoObso(pObsolescenceReference)) {
            String msg =
                    MessageBundle
                            .getMessage(Constants.OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE);
            throw new ImportException(msg, pRowNum, -1);
        }
        
        if (!obsoBean.isObsoCreationValidForExistingPN(lPN, lTypeArticle)) {
            if (lPN instanceof AirbusPN) {
                String msg =
                        MessageBundle
                                .getMessage(Constants.OBSO_ALREADY_AIRBUSPN_MANAGED_CONFLICT_ERROR);
                throw new ImportException(msg, pRowNum, -1);
            }
            else if (lPN instanceof ManufacturerPN) {
                String lMsg =
                        MessageBundle
                                .getMessage(Constants.OBSO_ALREADY_MANUFACTURERPN_MANAGED_CONFLICT_ERROR);
                throw new ImportException(lMsg, pRowNum, -1);
            }
            else {
                throw new ImportException("INVALID PN: conflict", pRowNum, -1);
            }
        }
        
        if (!obsoBean.isObsoCreationValidForPNAndType(lPN, lTypeArticle)) {
            String lMsg =
                    MessageBundle.getMessageResource(
                            IOConstants.PN_TYPE_NOT_IN_RELATION,
                            new Object[] { lPN.getIdentifier(),
                                    lTypeArticle.getLabel() });
            throw new ImportException(lMsg, pRowNum, -1);
        }
    }
    
}
