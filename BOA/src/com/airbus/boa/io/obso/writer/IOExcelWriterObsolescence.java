/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterObsolescence
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.obso.writer;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.obso.ObsolescenceReference;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ExportController;
import com.airbus.boa.view.ValueListController;

/**
 * Manage writing of obsolescence data into Excel files
 */
public class IOExcelWriterObsolescence extends IOExcelWriterBaseWriter {
    
    private static final String sheetname =
            IOConstants.ObsolescenceDataSheetName;
    
    private int indexFirstDynamicColumn;
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook on which to write
     * @param columns
     *            the columns
     * @throws ExportException
     *             when the workbook is null
     */
    public IOExcelWriterObsolescence(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
        indexFirstDynamicColumn = 0;
    }
    
    /**
     * Constructor
     * 
     * @param workbook
     *            the workbook on which to write
     * @param columns
     *            the columns
     * @param indexFirstDynamicColumn
     *            index of the first dynamic column
     * @throws ExportException
     *             when the workbook is null
     */
    public IOExcelWriterObsolescence(Workbook workbook, Columns columns,
            int indexFirstDynamicColumn) throws ExportException {
        super(workbook, columns, sheetname);
        this.indexFirstDynamicColumn = indexFirstDynamicColumn;
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof ObsolescenceData)) {
            throw new ExportException("Not an Obsolescence Data");
        }
        
        ObsolescenceData obso = (ObsolescenceData) object;
        Row row = getRow();
        
        // ATTENTION ICI, JE PRESUME QUE SUPPLIER EST LA DERNIERE COLONNE
        if (indexFirstDynamicColumn > 0) {
            for (int i = indexFirstDynamicColumn; i <= columns
                    .getMaxColumnIndex(); i++) {
                writeCell(row, i, "", Styles.DEFAULT);
            }
        }
        
        writeIntegerField(row, obso.getId(), IOConstants.NUMID_TITLE);
        writeValueListField(row, obso.getAirbusStatus(),
                IOConstants.OBSO_AIRBUS_STATUS_TITLE);
        writeField(row, obso.getCommentOnStrategy(),
                IOConstants.COMMENTS_TITLE, Styles.WRAPTEXT);
        writeValueListField(row, obso.getConsultPeriod(),
                IOConstants.OBSO_CONSULT_PERIOD_TITLE);
        
        writeField(row, obso.getContinuityDate(),
                IOConstants.OBSO_CONTINUITY_DATE_TITLE);
        writeValueListField(row, obso.getCurrentAction(),
                IOConstants.OBSO_CURRENT_ACTION_TITLE);
        writeField(row, obso.getEndOfOrderDate(),
                IOConstants.OBSO_END_OF_ORDER_TITLE);
        writeField(row, obso.getEndOfProductionDate(),
                IOConstants.OBSO_END_OF_PRODUCTION_TITLE);
        writeField(row, obso.getEndOfSupportDate(),
                IOConstants.OBSO_END_OF_SUPPORT_TITLE);
        writeField(row, obso.getLastObsolescenceUpdate(),
                IOConstants.OBSO_LAST_UPDATE_TITLE);
        writeIntegerField(row, obso.getMtbf(), IOConstants.OBSO_MTBF_TITLE);
        writeField(row, obso.getManufacturer(),
                IOConstants.OBSO_MANUFACTURER_TITLE);
        writeValueListField(row, obso.getManufacturerStatus(),
                IOConstants.OBSO_MANUFACTURER_STATUS_TITLE);
        writeField(row, obso.getNextConsultingDate(),
                IOConstants.OBSO_NEXT_CONSULTING_DATE_TITLE);
        writeField(row, obso.getObsolescenceDate(), IOConstants.OBSO_DATE_TITLE);
        writeField(row, obso.getPersonInCharge(),
                IOConstants.OBSO_IN_CHARGE_OF_TITLE);
        writeIntegerField(row, obso.getStock().getQuantityTotal(),
                IOConstants.OBSO_QUANTITY_TOTAL_TITLE);
        writeIntegerField(row, obso.getStock().getQuantityStock(),
                IOConstants.OBSO_QUANTITY_STOCK_TITLE);
        writeIntegerField(row, obso.getStock().getQuantityUse(),
                IOConstants.OBSO_QUANTITY_USE_TITLE);
        writeValueListField(row, obso.getStrategyKept(),
                IOConstants.OBSO_STRATEGY_TITLE);
        writeField(row, obso.getSupplier(), IOConstants.OBSO_SUPPLIER_TITLE);
        
        for (Installation lInstallation : obso.getStock()
                .getRepartitionMapKeys()) {
            writeIntegerField(row,
                    obso.getStock().getRepartitionMap().get(lInstallation),
                    lInstallation.getName());
        }
        
        ObsolescenceReference lReference = obso.getReference();
        String lReferenceFamily =
                MessageBundle.getMessageDefault(lReference.getReferenceType()
                        .toString());
        String lType = null;
        switch (lReference.getReferenceType()) {
        case AIRBUSPN_TYPEARTICLE:
        case MANUFACTURERPN_TYPEARTICLE:
            lType = lReference.getTypeArticle().getLabel();
            break;
        case TYPEPC:
            lType = lReference.getTypePC().getLabel();
            break;
        case SOFTWARE:
            break;
        default:
        }
        
        writeField(row, lReferenceFamily, IOConstants.OBSO_FOR_ITEM_TITLE);
        writeField(row, lType, IOConstants.OBSO_TYPE_ARTICLE_TITLE);
        writeField(row, obso.getConstituantName(),
                IOConstants.OBSO_CONSTITUANT_NAME_TITLE);
    }
    
    @Override
    public void writeFooter() {
        
        CellRangeAddress range = new CellRangeAddress(sheet.getFirstRowNum(), // first
                                                                              // row
                                                                              // (0-based)
                sheet.getLastRowNum(), // last row (0-based)
                columns.getMinColumnIndex(), // first column (0-based)
                columns.getMaxColumnIndex() // last column (0-based)
                );
        
        sheet.setAutoFilter(range);
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        ValueListController lController =
                AbstractController.findBean(ValueListController.class);
        ValueListBean lValueListBean = lController.getValueListBean();
        
        int lColumn;
        List<String> lValueList;
        
        lColumn = this.columns.getIndex(IOConstants.OBSO_FOR_ITEM_TITLE);
        lValueList = new ArrayList<String>();
        lValueList.add(ExportController.OBSO_FOR_ITEM_AIRBUS_PN_ARTICLE_TYPE);
        lValueList.add(
                ExportController.OBSO_FOR_ITEM_MANUFACTURER_PN_ARTICLE_TYPE);
        lValueList.add(ExportController.OBSO_FOR_ITEM_PC_TYPE);
        lValueList.add(ExportController.OBSO_FOR_ITEM_SOFTWARE);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.OBSO_CONSULT_PERIOD_TITLE);
        lValueList = lValueListBean.getAllValues(ConsultPeriod.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns
                .getIndex(IOConstants.OBSO_MANUFACTURER_STATUS_TITLE);
        lValueList = lValueListBean.getAllValues(ManufacturerStatus.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.OBSO_AIRBUS_STATUS_TITLE);
        lValueList = lValueListBean.getAllValues(AirbusStatus.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.OBSO_CURRENT_ACTION_TITLE);
        lValueList = lValueListBean.getAllValues(ActionObso.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
        
        lColumn = this.columns.getIndex(IOConstants.OBSO_STRATEGY_TITLE);
        lValueList = lValueListBean.getAllValues(Strategy.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
    }
    
}
