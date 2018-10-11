/*
 * ------------------------------------------------------------------------
 * Class : IOExcelObsoGenerateSheet
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.obso.writer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.io.obso.IOConstantsObso;
import com.airbus.boa.io.writer.IOExcelWriterBaseWriter;
import com.airbus.boa.util.MessageBundle;

/**
 * @author ng0057cf
 */
public class IOExcelObsoGenerateSheet extends IOExcelWriterBaseWriter implements
        Downloadable {
    
    public static final int ROW_MIN = 2;
    
    // ligne et colonne commencant à zero
    public static final int COL_TITLE = 2;
    public static final int COL_HEADER = 2;
    public static final int COL_IDENTIFICATION_NAME = 3;
    
    public static final int COL_DATA_IDENT = COL_IDENTIFICATION_NAME + 1;
    public static final int COL_DATA = COL_IDENTIFICATION_NAME + 2;
    public static final int COL_CONSTITUANTNAME = 6;
    
    public static final int COL_DEPARTMENT = COL_DATA;
    
    public static final int COL_MAX = COL_CONSTITUANTNAME + 2;
    public static final int COL_MIN = COL_HEADER - 1;
    
    private static final float SEPARATION_LINE_HEIGHT_POINTS = 3.75f;
    private ObsolescenceData obso;
    
    private int lastHeaderRow;
    
    private int logoImageIndex = -1; // un index défini est supérieur à 1.
    
    public IOExcelObsoGenerateSheet(Workbook workbook, String sheetname)
            throws ExportException {
        super(workbook, null, sheetname);
        
        stylesMap.putAll(createObsoStyles(workbook));
        rowNumber = ROW_MIN;
        
    }
    
    /**
     * @param workbook
     * @return
     */
    private Map<? extends Styles, ? extends CellStyle> createObsoStyles(
            Workbook workbook) {
        
        Map<Styles, CellStyle> styles = new HashMap<Styles, CellStyle>();
        
        // ligne de separation
        CellStyle style = workbook.createCellStyle();
        style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.SEPARATION, style);
        
        style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        styles.put(Styles.HEADER_OBSO, style);
        
        style = workbook.createCellStyle();
        font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        font.setUnderline(HSSFFont.U_SINGLE);
        style.setFont(font);
        styles.put(Styles.HEADER_OBSO_UNDERLINED, style);
        
        style = workbook.createCellStyle();
        font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        
        style.setFont(font);
        
        styles.put(Styles.OBSO_TITLE, style);
        
        DataFormat df = workbook.createDataFormat();
        style = workbook.createCellStyle();
        
        style.setDataFormat(df.getFormat("dd/mm/yyyy"));
        styles.put(Styles.DATE, style);
        
        style = workbook.createCellStyle();
        style.setDataFormat(df.getFormat("dd/mm/yyyy HH:MM"));
        styles.put(Styles.DATE_HHMM, style);
        
        // REDEFINITION DU STYLE PAR DEFAUT
        style = workbook.createCellStyle();
        styles.put(Styles.DEFAULT, style);
        
        style = workbook.createCellStyle();
        style.setWrapText(true);
        styles.put(Styles.WRAPTEXT, style);
        
        style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setAlignment(HorizontalAlignment.LEFT);
        styles.put(Styles.COMMENT, style);
        
        return styles;
    }
    
    public void writeSeparationLine(Row row, int start, int end) {
        if (row == null) {
            return;
        }
        
        if (start < 0 || end < 0 || start > end) {
            return;
        }
        
        for (int i = start; i <= end; i++) {
            writeCell(row, i, "", Styles.SEPARATION);
        }
        row.setHeightInPoints(SEPARATION_LINE_HEIGHT_POINTS);
    }
    
    public void generateObsoForm(Object object) throws ExportException {
        
        obso = (ObsolescenceData) object;
        writeHeader();
        write(object);
        writeFooter();
        
        // ajustement des colonnes (2 caracteres ) en 1/256th
        sheet.setColumnWidth(COL_MIN, 2 * 256);
        sheet.setColumnWidth(COL_MAX, 15 * 256);
        
        // pour les dates au minimum
        sheet.setColumnWidth(COL_DATA, 12 * 256);
        
        sheet.setColumnWidth(COL_DATA_IDENT, 20 * 256);
        sheet.autoSizeColumn(COL_DATA_IDENT, false);
        
        // pour les nom des références
        sheet.setColumnWidth(COL_IDENTIFICATION_NAME, 15 * 256);
        
        // printing area
        workbook.setPrintArea(workbook.getSheetIndex(sheet), // sheet index
                COL_MIN, // start column
                COL_MAX, // end column
                ROW_MIN, // start row
                sheet.getLastRowNum() // end row
        );
        
        // Cadre
        CellRangeAddress region = new CellRangeAddress(ROW_MIN, // first row
                                                                // (0-based)
                sheet.getLastRowNum(), // last row (0-based)
                COL_MIN, // first column (0-based)
                COL_MAX // last column (0-based)
                );
        
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM.getCode(), region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM.getCode(), region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM.getCode(), region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM.getCode(), region, sheet);
        RegionUtil.setBottomBorderColor(IndexedColors.BLACK.getIndex(), region,
                sheet);
        RegionUtil.setTopBorderColor(IndexedColors.BLACK.getIndex(), region,
                sheet);
        RegionUtil.setLeftBorderColor(IndexedColors.BLACK.getIndex(), region,
                sheet);
        RegionUtil.setRightBorderColor(IndexedColors.BLACK.getIndex(), region,
                sheet);
        
        sheet.setRepeatingRows(
                CellRangeAddress.valueOf(ROW_MIN + ":" + lastHeaderRow));
        
        if (logoImageIndex != -1) {
            writeLogo(3, 4, COL_TITLE, COL_TITLE + 1, logoImageIndex);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.writer.IOExcelBaseWriter#writeFooter()
     */
    @Override
    public void writeFooter() {
        
        Row row = sheet.createRow(rowNumber++);
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.EDITION_DATE_TITLE),
                Styles.HEADER_OBSO_UNDERLINED);
        writeCellDate(row, COL_DATA, new Date(), Styles.DATE);
        
        row = sheet.createRow(rowNumber++);
        
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.writer.IOExcelBaseWriter#writeHeader()
     */
    @Override
    public void writeHeader() {
        Row row = sheet.createRow(rowNumber++);
        row.setHeightInPoints(SEPARATION_LINE_HEIGHT_POINTS);
        
        row = sheet.createRow(rowNumber++);
        
        writeCell(row, COL_DEPARTMENT,
                MessageBundle.getMessage(IOConstantsObso.DEPARTMENT_TITLE),
                Styles.HEADER_OBSO);
        
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), // first row
                                                                    // (0-based)
                row.getRowNum(), // last row (0-based)
                COL_DEPARTMENT, // first column (0-based)
                COL_CONSTITUANTNAME // last column (0-based)
        ));
        
        rowNumber++;
        
        row = sheet.createRow(rowNumber++);
        // ligne de constituant
        writeCell(row, COL_TITLE,
                MessageBundle
                        .getMessage(IOConstantsObso.CONSTITUANT_SHEET_TITLE),
                Styles.OBSO_TITLE);
        writeCell(row, COL_CONSTITUANTNAME, obso.getConstituantName(),
                Styles.DEFAULT);
        
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), // first row
                                                                    // (0-based)
                row.getRowNum(), // last row (0-based)
                COL_CONSTITUANTNAME, // first column (0-based)
                COL_MAX // last column (0-based)
        ));
        
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
        lastHeaderRow = row.getRowNum();
        
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.writer.IOExcelBaseWriter#write(java.lang.Object)
     */
    @Override
    public void write(Object object) throws ExportException {
        
        obso = (ObsolescenceData) object;
        
        // partie information de référence
        
        rowNumber++;
        
        writeReferenceInfo(obso);
        writeDataInfo(obso);
        writeUsageInfo(obso);
        writeComments(obso);
        
    }
    
    private void writeReferenceInfo(ObsolescenceData obso) {
        
        Row row = sheet.createRow(rowNumber++);
        // information générale
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.GENERAL_INFO_TITLE),
                Styles.HEADER_OBSO_UNDERLINED);
        row = sheet.createRow(rowNumber++);
        row.setHeightInPoints(SEPARATION_LINE_HEIGHT_POINTS);
        
        // ecriture des références
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.REFERENCE),
                Styles.DEFAULT);
        writeCell(row, COL_DATA_IDENT, obso.getConstituantName(),
                Styles.DEFAULT);
        
        // fabricant
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.MANUFACTURER),
                Styles.DEFAULT);
        writeCell(row, COL_DATA_IDENT, obso.getManufacturer(), Styles.DEFAULT);
        
        // supplier
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.SUPPLIER),
                Styles.DEFAULT);
        writeCell(row, COL_DATA_IDENT, obso.getSupplier(), Styles.DEFAULT);
        
        rowNumber++;
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
    }
    
    private void writeDataInfo(ObsolescenceData obso) {
        
        Row row = sheet.createRow(rowNumber++);
        // information générale
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.OBSO_INFO_TITLE),
                Styles.HEADER_OBSO_UNDERLINED);
        row = sheet.createRow(rowNumber++);
        row.setHeightInPoints(SEPARATION_LINE_HEIGHT_POINTS);
        
        /*
         * End of Order Date ............. <End of order>
         * End of Support Date ........... <End of support>
         * Manufacturer Status ........... < Manufacturer status >
         * Airbus Status ................. < Airbus status >
         * Current Action ................ <Current action>
         * Strategy ...................... <Strategy>
         * Next Consulting Date .......... <Next consulting date.>
         */
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.END_OF_ORDER),
                Styles.DEFAULT);
        writeCellDate(row, COL_DATA, obso.getEndOfOrderDate(), Styles.DATE);
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.END_OF_SUPPORT),
                Styles.DEFAULT);
        writeCellDate(row, COL_DATA, obso.getEndOfSupportDate(), Styles.DATE);
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.MANUFACTURER_STATUS),
                Styles.DEFAULT);
        if (obso.getManufacturerStatus() != null) {
            writeCell(row, COL_DATA,
                    obso.getManufacturerStatus().getLocaleValue(),
                    Styles.DEFAULT);
        }
        
        row = sheet.createRow(rowNumber++);
        
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.AIRBUS_STATUS),
                Styles.DEFAULT);
        if (obso.getAirbusStatus() != null) {
            writeCell(row, COL_DATA, obso.getAirbusStatus().getLocaleValue(),
                    Styles.DEFAULT);
        }
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.CURRENT_ACTION),
                Styles.DEFAULT);
        if (obso.getCurrentAction() != null) {
            writeCell(row, COL_DATA, obso.getCurrentAction().getLocaleValue(),
                    Styles.DEFAULT);
        }
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.STRATEGY_KEPT),
                Styles.DEFAULT);
        if (obso.getStrategyKept() != null) {
            writeCell(row, COL_DATA, obso.getStrategyKept().getLocaleValue(),
                    Styles.DEFAULT);
        }
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.NEXT_CONSULTING_DATE),
                Styles.DEFAULT);
        writeCellDate(row, COL_DATA, obso.getNextConsultingDate(), Styles.DATE);
        
        rowNumber++;
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
    }
    
    /**
     * Use Information :
     * Number in Use .......... < Number in use>
     * Number in Stock ........ < Number in stock OK >
     * Used in : <instal. 1><number>
     * _________ ...
     * _________ <instal. n><number>
     * 
     * @param obso
     */
    private void writeUsageInfo(ObsolescenceData obso) {
        
        Row row = sheet.createRow(rowNumber++);
        // information utilisation
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.OBSO_USAGE_TITLE),
                Styles.HEADER_OBSO_UNDERLINED);
        row = sheet.createRow(rowNumber++);
        row.setHeightInPoints(SEPARATION_LINE_HEIGHT_POINTS);
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.QUANTITY_USE),
                Styles.DEFAULT);
        writeCellNumeric(row, COL_DATA, obso.getStock().getQuantityUse(), Styles.DEFAULT);
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.QUANTITY_STOCK),
                Styles.DEFAULT);
        writeCellNumeric(row, COL_DATA, obso.getStock().getQuantityStock(), Styles.DEFAULT);
        
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_IDENTIFICATION_NAME,
                MessageBundle.getMessage(IOConstantsObso.USED_IN_TITLE),
                Styles.DEFAULT);
        
        // List d'installation triées ...
        for (Installation lInstallation : obso.getStock()
                .getRepartitionMapKeys()) {
            
            writeCell(row, COL_DATA_IDENT, lInstallation.getName(),
                    Styles.DEFAULT);
            writeCellNumeric(row, COL_DATA,
                    obso.getStock().getRepartitionMap().get(lInstallation),
                    Styles.DEFAULT);
            row = sheet.createRow(rowNumber++);
            
        }
        
        row = sheet.createRow(rowNumber++);
        
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
    }
    
    private void writeComments(ObsolescenceData obso) {
        
        Row row = sheet.createRow(rowNumber++);
        // information utilisation
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.COMMENTS_TITLE),
                Styles.HEADER_OBSO);
        
        row = sheet.createRow(rowNumber++);
        
        writeCell(row, COL_IDENTIFICATION_NAME, obso.getCommentOnStrategy(),
                Styles.WRAPTEXT);
        row.setHeightInPoints(70f);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), // first row
                                                                    // (0-based)
                row.getRowNum(), // last row (0-based)
                COL_IDENTIFICATION_NAME, // first column (0-based)
                COL_CONSTITUANTNAME // last column (0-based)
        ));
        
    }
    
    public int loadLogoImage(String fullpathLogo, int wbPictureType)
            throws ExportException {
        
        try {
            
            InputStream is = new FileInputStream(fullpathLogo);
            byte[] bytes = IOUtils.toByteArray(is);
            logoImageIndex = workbook.addPicture(bytes, wbPictureType);
            
            is.close();
            return logoImageIndex;
        }
        catch (IOException ioe) {
            throw new ExportException("LOGO DECLARE INTROUVABLE");
        }
        
    }
    
    private void writeLogo(int rowStart, int rowEnd, int colStart, int colEnd,
            int imageIndex) {
        
        CreationHelper helper = workbook.getCreationHelper();
        
        // create sheet
        
        // Create the drawing patriarch. This is the top level container for all
        // shapes.
        Drawing drawing = sheet.createDrawingPatriarch();
        
        // add a picture shape
        ClientAnchor anchor = helper.createClientAnchor();
        // set top-left corner of the picture,
        // subsequent call of Picture#resize() will operate relative to it
        anchor.setCol1(colStart);
        anchor.setRow1(rowStart);
        anchor.setCol2(colEnd);
        anchor.setRow2(rowEnd);
        
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE); // INDISPENSABLE
                                                                 // !!!
        Picture pict = drawing.createPicture(anchor, imageIndex);
        
        // auto-size picture relative to its top-left corner
        pict.resize(1.0);
        
    }
    
    @Override
    protected void writeCellNumeric(Row row, int col, Object o, Styles style) {
        if (row == null || col < 0) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue((Long) o);
        cell.setCellStyle(stylesMap.get(style));
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.Downloadable#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream os) throws IOException {
        if (workbook != null) {
            workbook.write(os);
        }
        
    }
    
    /**
     * @return the logoImageIndex
     */
    public int getLogoImageIndex() {
        return logoImageIndex;
    }
    
    /**
     * @param logoImageIndex
     *            the logoImageIndex to set
     */
    public void setLogoImageIndex(int logoImageIndex) {
        this.logoImageIndex = logoImageIndex;
    }
    
}
