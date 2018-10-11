/*
 * ------------------------------------------------------------------------
 * Class : IOExcelFormStock
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.stock;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.view.stock.StockForType;

/**
 * Manage writing of a stock computations into Excel files
 */
public class IOExcelStockForm extends IOExcelWriterBaseWriter implements Downloadable {
    
    public static final String TEMPLATE_WORKBOOK_PATH =
            "/resources/Stock_Export_Template.xlsx";
    
    private static final String TEMPLATE_SHEETNAME = "TEMPLATE";
    private static final String TEMPLATE_COMMENTS_SHEETNAME =
            "TEMPLATE_COMMENTS";
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the Excel workbook on which to write
     * @param pSheetname
     *            the Excel sheet name on which to write
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelStockForm(String pSheetname, Workbook pWorkbook)
            throws ExportException {
        
        this.workbook = pWorkbook;
        if (this.workbook == null) {
            throw new ExportException("WORKBOOK EST NULL");
        }
        
        stylesMap = createStyles(workbook);
        sheet = workbook.getSheet(pSheetname);
        if (sheet == null) {
            sheet = workbook
                    .cloneSheet(workbook.getSheetIndex(TEMPLATE_SHEETNAME));
            workbook.setSheetName(workbook.getSheetIndex(sheet), pSheetname);
            rowNumber = 0;
            if (sheet instanceof XSSFSheet) {
                // Needed to fix comment writing (Bug Apache POI 55814)
                XSSFSheet lTemplateSheet = (XSSFSheet) workbook
                        .getSheet(TEMPLATE_COMMENTS_SHEETNAME);
                Map<CellAddress, ? extends Comment> lComments =
                        lTemplateSheet.getCellComments();
                ExcelUtils.setComments(workbook, sheet, lComments);
            }
            
        }
    }
    
    /**
     * Write a stock sheet
     * 
     * @param pObject
     *            the stock to write to complete
     */
    public void write(Object pObject) throws ExportException {
        if (!(pObject instanceof StockForType)) {
            throw new ExportException("NOT A STOCK OBJECT");
        }
        
        StockForType lStock = (StockForType) pObject;
        
        // Group installations by aircraft program
        Map<Installation, Long> lRepartitionMap = lStock.getRepartitionMap();
        Map<String, ArrayList<Installation>> lACProgramRepartionMap =
                new TreeMap<String, ArrayList<Installation>>();
        for (Installation lInst : lRepartitionMap.keySet()) {
            String lACProgram = lInst.getAircraftProgram().getDefaultValue();
            if (!lACProgramRepartionMap.containsKey(lACProgram)) {
                lACProgramRepartionMap.put(lACProgram,
                        new ArrayList<Installation>());
            }
            lACProgramRepartionMap.get(lACProgram).add(lInst);
        }
        
        if (lRepartitionMap.isEmpty()){
            return;
        }
        
        // Write header
        sheet.getRow(0).getCell(6).setCellValue(new Date());
        sheet.getRow(1).getCell(4).setCellValue(lStock.getType().getLabel());
        
        // Insert installations rows
        sheet.shiftRows(3, sheet.getLastRowNum(), lRepartitionMap.size());
        
        // Fill installations rows
        int lRowNumber = 3;
        sheet.createRow(lRowNumber);
        for (String lACProgram : lACProgramRepartionMap.keySet()) {
            writeCell(sheet.getRow(lRowNumber), 0, lACProgram,
                    Styles.DEFAULT);
            setBorders(lRowNumber, 0, BorderStyle.MEDIUM, "TRBL");
            setBackground(lRowNumber, 0, IndexedColors.RED.getIndex());
            setFont(lRowNumber, 0, true, IndexedColors.WHITE.getIndex());
            setAlignement(lRowNumber, 0, HorizontalAlignment.CENTER,
                    VerticalAlignment.CENTER);
            
            boolean lFirst = true;
            for (Installation lInst : lACProgramRepartionMap.get(lACProgram)) {
                String lDefaultPos = "";
                if (lFirst) {
                    lDefaultPos += "T";
                }
                writeCell(sheet.getRow(lRowNumber), 1, lInst.getName(),
                        Styles.DEFAULT);
                setBorders(lRowNumber, 1, BorderStyle.MEDIUM,
                        lDefaultPos + "RL");
                setBackground(lRowNumber, 1,
                        IndexedColors.LIGHT_ORANGE.getIndex());
                setFont(lRowNumber, 1, true, null);
                
                writeCellDate(sheet.getRow(lRowNumber), 2,
                        lInst.getStartingDate(),
                        Styles.DATE_MMYY);
                setBorders(lRowNumber, 2, BorderStyle.MEDIUM,
                        lDefaultPos + "L");
                setAlignement(lRowNumber, 2, HorizontalAlignment.CENTER,
                        VerticalAlignment.CENTER);
                
                writeCellFormula(sheet.getRow(lRowNumber), 3,
                        "(DAYS360(C" + (lRowNumber + 1) + ",TODAY()))/30",
                        Styles.NB_MONTHS);
                setBorders(lRowNumber, 3, BorderStyle.MEDIUM,
                        lDefaultPos + "R");
                setAlignement(lRowNumber, 3, HorizontalAlignment.CENTER,
                        VerticalAlignment.CENTER);
                
                if (lRepartitionMap.containsKey(lInst)) {
                    writeCellNumeric(sheet.getRow(lRowNumber), 4,
                            lRepartitionMap.get(lInst), Styles.DEFAULT);
                }
                else {
                    writeCellNumeric(sheet.getRow(lRowNumber), 4, 0,
                            Styles.DEFAULT);
                }
                setBorders(lRowNumber, 4, BorderStyle.MEDIUM,
                        lDefaultPos + "RL");
                setAlignement(lRowNumber, 4, HorizontalAlignment.CENTER,
                        VerticalAlignment.CENTER);
                setBackground(lRowNumber, 4,
                        IndexedColors.LIGHT_YELLOW.getIndex());
                
                writeCell(sheet.getRow(lRowNumber), 5, "", Styles.DEFAULT);
                setBorders(lRowNumber, 5, BorderStyle.MEDIUM,
                        lDefaultPos + "RL");
                
                int lCol = 6;
                for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
                    writeCellFormula(sheet.getRow(lRowNumber), lCol,
                            "IF((DAYS360($C" + (lRowNumber + 1)
                                    + " ,TODAY()))/30+$" + lChar
                                    + "2-$E$3>0,0,IF(" + lChar + "$2 + $D"
                                    + (lRowNumber + 1) + "<0,0,$E"
                                    + (lRowNumber + 1) + "))",
                            Styles.DEFAULT);
                    setBorders(lRowNumber, lCol, BorderStyle.MEDIUM,
                            lDefaultPos);
                    lCol++;
                }
                setBorders(lRowNumber, lCol - 1, BorderStyle.MEDIUM, "R");
                
                lRowNumber++;
                if (lRowNumber < lRepartitionMap.size() + 3) {
                    sheet.createRow(lRowNumber);
                }
                lFirst = false;
            }
            
            // Merge aircraft program cells and apply border and styles
            if (lACProgramRepartionMap.get(lACProgram).size() > 1) {
                for (int i = 1; i < lACProgramRepartionMap.get(lACProgram)
                        .size(); i++) {
                    sheet.getRow(lRowNumber - i).createCell(0);
                    setBorders(lRowNumber - i, 0, BorderStyle.MEDIUM,
                            "TRBL");
                }
                CellRangeAddress lRegion = new CellRangeAddress(
                        lRowNumber
                                - lACProgramRepartionMap.get(lACProgram).size(),
                        lRowNumber - 1, 0, 0);
                sheet.addMergedRegion(lRegion);
            }
        }
        
        // Set the border for the final line of the installations part
        for (int i = 1; i < 24; i++) {
            setBorders(lRowNumber - 1, i, BorderStyle.MEDIUM, "B");
        }
        
        // Fill total line
        lRowNumber += 7;
        int lTotalRowNumber = lRowNumber;
        sheet.getRow(lRowNumber).getCell(5)
                .setCellFormula("SUM(E4:E" + (lRowNumber - 1) + ")");
        int lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol).setCellFormula(
                    "SUM(" + lChar + "4:" + lChar + (lRowNumber - 1) + ")");
            lCol++;
        }
        
        // Fill stock diminution line
        lRowNumber += 9;
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol).setCellFormula("SUM(" + lChar
                    + (lRowNumber - 1) + ":" + lChar + lRowNumber + ")");
            lCol++;
        }
        
        // Fill spare number line
        lRowNumber += 13;
        sheet.getRow(lRowNumber).getCell(5)
                .setCellValue(lStock.getQuantityStock());
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol).setCellFormula("F"
                    + (lRowNumber + 1) + "-" + lChar + (lTotalRowNumber + 3)
                    + "-" + lChar + (lTotalRowNumber + 5) + "-" + lChar
                    + (lTotalRowNumber + 10) + "+" + lChar + (lRowNumber - 1));
            lCol++;
        }
        
        // Fill number in use line
        lRowNumber++;
        sheet.getRow(lRowNumber).getCell(5)
                .setCellFormula("F" + (lTotalRowNumber + 1));
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol)
                    .setCellFormula("F" + (lRowNumber + 1) + "+" + lChar
                            + (lTotalRowNumber + 3) + "-SUM(" + lChar
                            + (lTotalRowNumber + 12) + ":" + lChar
                            + (lTotalRowNumber + 18) + ")");
            lCol++;
        }
        
        // Fill total line (in use + spare)
        lRowNumber++;
        sheet.getRow(lRowNumber).getCell(5)
                .setCellFormula("F" + (lRowNumber - 1) + "+F" + lRowNumber);
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol).setCellFormula(
                    "" + lChar + (lRowNumber - 1) + "+" + lChar + lRowNumber);
            lCol++;
        }
        
        // Fill lines for the graph
        lRowNumber += 6;
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol)
                    .setCellFormula("" + lChar + (lRowNumber - 6));
            lCol++;
        }
        
        lRowNumber++;
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol)
                    .setCellFormula("(" + lChar + (lRowNumber - 7) + "*$C$"
                            + (lRowNumber - 4) + ")+" + lChar
                            + (lRowNumber - 7));
            lCol++;
        }
        
        lRowNumber += 4;
        lCol = 6;
        for (char lChar : "GHIJKLMNOPQRSTUVWX".toCharArray()) {
            sheet.getRow(lRowNumber).getCell(lCol)
                    .setCellFormula("" + lChar + (lRowNumber - 10));
            lCol++;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.Downloadable#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream pOS) throws IOException {
        if (workbook != null) {
            workbook.setForceFormulaRecalculation(true);
            workbook.removeSheetAt(workbook.getSheetIndex(TEMPLATE_SHEETNAME));
            workbook.removeSheetAt(
                    workbook.getSheetIndex(TEMPLATE_COMMENTS_SHEETNAME));
            workbook.write(pOS);
        }
        
    }
    
    /**
     * Set a cell border
     * 
     * @param pRow
     *            the cell's row
     * @param pCol
     *            the cell's column
     * @param pStyle
     *            the border style to apply
     * @param pPos
     *            a string representing the borders to set, concatenation of the
     *            following letters : T=Top, R=Right, B=Bottom, L=Left
     */
    private void setBorders(int pRow, int pCol, BorderStyle pStyle,
            String pPos) {
        CellStyle lStyle = workbook.createCellStyle();
        lStyle.cloneStyleFrom(sheet.getRow(pRow).getCell(pCol).getCellStyle());
        if (pPos.contains("T")) {
            lStyle.setBorderTop(pStyle);
        }
        if (pPos.contains("R")) {
            lStyle.setBorderRight(pStyle);
        }
        if (pPos.contains("B")) {
            lStyle.setBorderBottom(pStyle);
        }
        if (pPos.contains("L")) {
            lStyle.setBorderLeft(pStyle);
        }
        sheet.getRow(pRow).getCell(pCol).setCellStyle(lStyle);
    }
    
    /**
     * Set a cell background color
     * 
     * @param pRow
     *            the cell's row
     * @param pCol
     *            the cell's column
     * @param pBackground
     *            the color to apply
     */
    private void setBackground(int pRow, int pCol, Short pBackground) {
        CellStyle lStyle = workbook.createCellStyle();
        lStyle.cloneStyleFrom(sheet.getRow(pRow).getCell(pCol).getCellStyle());
        if (pBackground != null) {
            lStyle.setFillForegroundColor(pBackground);
            lStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        sheet.getRow(pRow).getCell(pCol).setCellStyle(lStyle);
    }
    
    /**
     * Set a cell font
     * 
     * @param pRow
     *            the cell's row
     * @param pCol
     *            the cell's column
     * @param pBold
     *            boolean indicating of the to apply font is bold
     * @param pColor
     *            the font color to apply
     */
    private void setFont(int pRow, int pCol, boolean pBold, Short pColor) {
        CellStyle lStyle = workbook.createCellStyle();
        lStyle.cloneStyleFrom(sheet.getRow(pRow).getCell(pCol).getCellStyle());
        Font lFont = workbook.createFont();
        lFont.setFontName("Arial");
        lFont.setFontHeightInPoints((short) 10);
        lFont.setBold(pBold);
        if (pColor != null) {
            lFont.setColor(pColor);
        }
        lStyle.setFont(lFont);
        sheet.getRow(pRow).getCell(pCol).setCellStyle(lStyle);
    }
    
    /**
     * Set a cell text alignment
     * 
     * @param pRow
     *            the cell's row
     * @param pCol
     *            the cell's column
     * @param pHAlign
     *            the horizontal alignment to apply
     * @param pVAlign
     *            the vertical alignment to apply
     */
    private void setAlignement(int pRow, int pCol, HorizontalAlignment pHAlign,
            VerticalAlignment pVAlign) {
        CellStyle lStyle = workbook.createCellStyle();
        lStyle.cloneStyleFrom(sheet.getRow(pRow).getCell(pCol).getCellStyle());
        if (pHAlign != null) {
            lStyle.setAlignment(pHAlign);
        }
        if (pVAlign != null) {
            lStyle.setVerticalAlignment(pVAlign);
        }
        sheet.getRow(pRow).getCell(pCol).setCellStyle(lStyle);
    }
}
