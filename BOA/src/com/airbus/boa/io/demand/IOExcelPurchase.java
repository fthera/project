/*
 * ------------------------------------------------------------------------
 * Class : IOExcelPurchase
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.demand;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.localization.Location;
import com.airbus.boa.util.StringUtil;

/**
 * Management of export, in Excel file, of Purchases for demands
 */
public class IOExcelPurchase extends IOExcelWriterBaseWriter implements Downloadable {
    
    /** The type of purchase */
    public enum PurchaseMode {
        /** when the purchase is internal */
        INTERNAL_PURCHASE,
        /** when the purchase is external */
        EXTERNAL_PURCHASE
    }
    
    private static Columns getColumns(PurchaseMode pPurchaseMode)
            throws ExportException {
        switch (pPurchaseMode) {
        case INTERNAL_PURCHASE:
            return new ColumnInternalPurchase();
        case EXTERNAL_PURCHASE:
            return new ColumnExternalPurchase();
        default:
            throw new ExportException("Unknown purchase mode");
        }
    }
    
    private PurchaseMode purchaseMode;
    
    private Date date = new Date();
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the workbook on which to write
     * @param pPurchaseMode
     *            the purchase mode for the generated file
     * @throws ExportException
     *             when an error occurs
     */
    public IOExcelPurchase(Workbook pWorkbook, PurchaseMode pPurchaseMode)
            throws ExportException {
        
        super(pWorkbook, getColumns(pPurchaseMode),
                IOConstants.PURCHASE_SHEET_NAME);
        
        purchaseMode = pPurchaseMode;
    }
    
    /**
     * Write the purchase corresponding to the demands list into the workbook
     * 
     * @param pDemands
     *            the demands
     * @throws ExportException
     *             when an error occurs
     */
    public void writeSheetContent(List<Demand> pDemands) throws ExportException {
        
        writeHeader();
        
        for (Demand lDemand : pDemands) {
            writeOne(lDemand);
        }
        
        writeFooter();
        applyStyles();
    }
    
    @Override
    public void write(OutputStream pOs) throws IOException {
        if (workbook != null) {
            workbook.write(pOs);
        }
    }
    
    @Override
    public void write(Object pObject) throws ExportException {
        
        if (!(pObject instanceof Demand)) {
            throw new ExportException("The provided object is not a demand");
        }
        
        Demand lDemand = (Demand) pObject;
        Row lRow = getRow();
        
        writePurchaseCommon(lRow, lDemand);
        
        switch (purchaseMode) {
        case INTERNAL_PURCHASE:
            writeInternalPurchase(lRow, lDemand);
            break;
        case EXTERNAL_PURCHASE:
            writeExternalPurchase(lRow, lDemand);
            break;
        default:
        }
    }
    
    private void writePurchaseCommon(Row pRow, Demand pDemand) {
        
        writeField(pRow, pDemand.getIssuer().getLoginDetails(),
                IOConstants.ISSUER_TITLE);
        
        writeField(pRow, pDemand.getAdditionalInformation(),
                IOConstants.ADDITIONAL_INFORMATION_TITLE);
        
        writeField(pRow, pDemand.getNeedDate(), IOConstants.NEED_DATE_TITLE);
        
        writeField(pRow, pDemand.getProgram(), IOConstants.PROGRAM_TITLE);
        
        writeField(pRow, pDemand.getComments(), IOConstants.COMMENTS_TITLE);
        
        Location lLocation = pDemand.getLocation();
        if (lLocation != null) {
            writeField(pRow, lLocation.getLocationName(),
                    IOConstants.LOCATION_TITLE);
            writeField(pRow, lLocation.getPlace(), IOConstants.ROOM_TITLE);
            writeField(pRow, lLocation.getPlace().getBuilding(),
                    IOConstants.BUILDING_TITLE);
        }
        else {
            writeField(pRow, "", IOConstants.LOCATION_TITLE);
            writeField(pRow, "TBD", IOConstants.ROOM_TITLE);
            writeField(pRow, "TBD", IOConstants.BUILDING_TITLE);
        }
    }
    
    private void writeInternalPurchase(Row pRow, Demand pDemand) {
        
        // Department: if empty, write "EYYS"
        if (StringUtil.isEmptyOrNull(pDemand.getDepartment())) {
            writeField(pRow, "EYYS", IOConstants.DEPT_TITLE);
        }
        else {
            writeField(pRow, pDemand.getDepartment(), IOConstants.DEPT_TITLE);
        }
        
        // Budget: if empty, write "Business budget"
        if (StringUtil.isEmptyOrNull(pDemand.getBudget())) {
            writeField(pRow, "Business budget", IOConstants.BUDGET_TITLE);
        }
        else {
            writeField(pRow, pDemand.getBudget(), IOConstants.BUDGET_TITLE);
        }
        
        writeField(pRow, pDemand.getJustification(),
                IOConstants.JUSTIFICATION_TITLE);
        
        // New/Replace: if PC to replace empty, write "New", else "Replace"
        // Justification: if PC to replace empty, write "Industrial", else
        // "Industrial Obsolescence" (if justification empty)
        if (pDemand.getPCToReplace() != null) {
            writeField(pRow, "Replace", IOConstants.NEW_REPLACE_TITLE);
            if (StringUtil.isEmptyOrNull(pDemand.getJustification())) {
                writeField(pRow, "Industrial Obsolescence",
                        IOConstants.JUSTIFICATION_TITLE);
            }
            writeField(pRow, pDemand.getPCToReplace().getAirbusSN(),
                    IOConstants.ASSET_NUMBER_TO_REPLACE);
        }
        else {
            writeField(pRow, "New", IOConstants.NEW_REPLACE_TITLE);
            if (StringUtil.isEmptyOrNull(pDemand.getJustification())) {
                writeField(pRow, "Industrial", IOConstants.JUSTIFICATION_TITLE);
            }
            writeField(pRow, "", IOConstants.ASSET_NUMBER_TO_REPLACE);
        }
        
        writeField(pRow, pDemand.getTypePC(), IOConstants.HARDWARE_TITLE);
        
        writeField(pRow, pDemand.getSoftwaresString(), IOConstants.OS_TITLE);
        
        writeField(pRow, pDemand.getFeatures(), IOConstants.FEATURES_TITLE);
        
        writeField(pRow, pDemand.getUsage().getLocaleValue(),
                IOConstants.BUSINESS_USAGE_TITLE);
        
        // OTP: write "TBD"
        writeField(pRow, "TBD", IOConstants.OTP_TITLE);
        
        // Project: write "Other"
        writeField(pRow, "Other", IOConstants.PROJECT_TITLE);
        
        writeField(pRow, pDemand.getPlugNumber(), IOConstants.SOCKET_NB_TITLE);
        
        // Price: left empty
        writeField(pRow, "", IOConstants.PRICE_TITLE);
    }
    
    private void writeExternalPurchase(Row pRow, Demand pDemand) {
        
        writeField(pRow, pDemand.getDemandNumber(),
                IOConstants.DEMAND_NUMBER_TITLE);
        
        writeField(pRow, pDemand.getProject(), IOConstants.PROJECT_TITLE);
        
        writeField(pRow, pDemand.getTypePC(), IOConstants.TYPE_TITLE);
        
        writeField(pRow, pDemand.getProductTypePC().getLocaleValue(),
                IOConstants.PRODUCT_TYPE_TITLE);
        
        writeField(pRow, pDemand.getJustification(),
                IOConstants.JUSTIFICATION_TITLE);
        
        writeField(pRow, pDemand.getFeatures(), IOConstants.FEATURES_TITLE);
        
        writeField(pRow, pDemand.getSoftwaresString(),
                IOConstants.OPERATING_SYSTEM_TITLE);
        
        writeField(pRow, pDemand.getPlugNumber(), IOConstants.PLUG_NUMBER_TITLE);
    }
    
    /**
     * @return the purchaseMode
     */
    public PurchaseMode getPurchaseMode() {
        return purchaseMode;
    }
    
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * @return the computed file name
     */
    public String getFileName() {
        
        SimpleDateFormat lFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
        StringBuffer lFileNameSB = new StringBuffer("Purchase_");
        lFileNameSB.append(lFormat.format(date));
        
        return lFileNameSB.toString();
    }
}
