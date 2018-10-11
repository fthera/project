/*
 * ------------------------------------------------------------------------
 * Class : IOExcelObsoGeneratePlatformSheet
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
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
import com.airbus.boa.util.StringUtil;

/**
 * @author ng0057cf
 */
public class IOExcelObsoGeneratePlatformSheet extends IOExcelWriterBaseWriter
        implements Downloadable {
    
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
    
    public static final int COL_H_CONSTITUANT_LIST = COL_HEADER;
    public static final int COL_H_QUANTITY = COL_HEADER + 2;
    public static final int COL_H_MANU_STATUS = COL_H_QUANTITY + 1;
    public static final int COL_H_AIRBUS_STATUS = COL_H_QUANTITY + 2;
    
    /**
     * The name is over 31 characters
     * The name contains any of the following characters: / \ * ? [ ]
     * cette fonction convertit ces caractères interdits dans la limite de 31
     * caractères
     * 
     * @param name
     * @return
     */
    public static String convertStringForSheetName(String name) {
        Pattern p = Pattern.compile("[" + Pattern.quote("/\\*?[]") + "]");
        
        Matcher matcher = p.matcher(name);
        String tmp = matcher.replaceAll("_");
        
        return (tmp.length() > 31) ? (tmp.substring(31)) : tmp;
        
    }
    
    private int lastHeaderRow;
    private Installation platform;
    
    private int logoImageIndex = -1;
    
    public IOExcelObsoGeneratePlatformSheet(Workbook workbook,
            Installation platform, int logoIndex) throws ExportException {
        super(workbook, null, StringUtil.convertStringForSheetName(platform
                .getName()));
        
        stylesMap.putAll(createObsoStyles(workbook));
        rowNumber = ROW_MIN;
        this.platform = platform;
        logoImageIndex = logoIndex;
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
    
    public void generatePlatFormSheet(List<ObsolescenceData> obsoDataList)
            throws ExportException {
        
        writeHeader();
        writeConstituants(obsoDataList);
        writeEmptyFieldComments();
        writeFooter();
        
        sheetLayout();
        
    }
    
    /**
	 * 
	 */
    private void sheetLayout() {
        // ajustement des colonnes (2 caracteres ) en 1/256th
        sheet.setColumnWidth(COL_MIN, 2 * 256);
        sheet.setColumnWidth(COL_MAX, 5 * 256);
        
        sheet.setColumnWidth(COL_HEADER, 5 * 256);
        
        sheet.autoSizeColumn(COL_H_MANU_STATUS);
        sheet.autoSizeColumn(COL_H_AIRBUS_STATUS);
        sheet.autoSizeColumn(COL_H_QUANTITY);
        sheet.autoSizeColumn(COL_IDENTIFICATION_NAME);
        
        // printing area
        workbook.setPrintArea(workbook.getSheetIndex(sheet), // sheet index
                COL_MIN, // start column
                COL_MAX, // end column
                ROW_MIN, // start row
                sheet.getLastRowNum() // end row
        );
        
        generateOuterBorder();
        
        sheet.setRepeatingRows(
                CellRangeAddress.valueOf(ROW_MIN + ":" + lastHeaderRow));
        
        if (logoImageIndex != -1) {
            writeLogo(3, 4, COL_TITLE, COL_TITLE + 1, logoImageIndex);
        }
    }
    
    /**
     * génération de la bordure noire exterieure de la fiche suivant
     * ROW_MIN, COL_MIN, COL_MAX
     */
    private void generateOuterBorder() {
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
                Styles.HEADER_OBSO);
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
        
        // NOM DE LA PLATEFORME
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_TITLE,
                MessageBundle.getMessage(IOConstantsObso.PLATFORM_SHEET_TITLE),
                Styles.OBSO_TITLE);
        writeCell(row, COL_CONSTITUANTNAME, platform.getName(), Styles.DEFAULT);
        
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), // first row
                                                                    // (0-based)
                row.getRowNum(), // last row (0-based)
                COL_CONSTITUANTNAME, // first column (0-based)
                COL_MAX // last column (0-based)
        ));
        
        // NOM DU RESPONSABLE
        row = sheet.createRow(rowNumber++);
        writeCell(row, COL_CONSTITUANTNAME, platform.getPersonInCharge()
                .getLoginDetails(), Styles.DEFAULT);
        
        // ligne de separation
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
        // deux lignes vides
        rowNumber += 2;
        
        // ligne de séparation
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
        lastHeaderRow = row.getRowNum();
        
    }
    
    public void writeConstituants(List<ObsolescenceData> obsoList)
            throws ExportException {
        
        writeHeaderLineConstituant();
        writeComponentListInfo(obsoList);
        
    }
    
    private void writeHeaderLineConstituant() {
        
        Row row = sheet.createRow(rowNumber++);
        // liste des constituants
        writeCell(row, COL_HEADER,
                MessageBundle
                        .getMessage(IOConstantsObso.CONSTITUANT_LIST_TITLE),
                Styles.HEADER_OBSO_UNDERLINED);
        
        // quantité
        writeCell(row, COL_H_QUANTITY,
                MessageBundle.getMessage(IOConstantsObso.QUANTITY),
                Styles.HEADER_OBSO_UNDERLINED);
        
        // fabricant status
        writeCell(row, COL_H_MANU_STATUS,
                MessageBundle.getMessage(IOConstantsObso.MANUFACTURER_STATUS),
                Styles.HEADER_OBSO_UNDERLINED);
        
        // Airbus Status
        writeCell(row, COL_H_AIRBUS_STATUS,
                MessageBundle.getMessage(IOConstantsObso.AIRBUS_STATUS),
                Styles.HEADER_OBSO_UNDERLINED);
        
    }
    
    private void writeComponentListInfo(List<ObsolescenceData> obsolist) {
        
        Row row = sheet.createRow(rowNumber++);
        
        int countLine = 0;
        for (ObsolescenceData obso : obsolist) {
            
            // ecriture d'une ligne
            if (obso.getStock().getRepartitionMap().containsKey(platform)) {
                
                countLine++;
                // liste des constituants
                writeCell(row, COL_IDENTIFICATION_NAME,
                        obso.getConstituantName(), Styles.DEFAULT);
                
                // quantité
                writeCellNumeric(row, COL_H_QUANTITY,
                        obso.getStock().getRepartitionMap().get(platform),
                        Styles.DEFAULT);
                
                // fabricant status
                writeCell(
                        row,
                        COL_H_MANU_STATUS,
                        (obso.getManufacturerStatus() == null) ? "" : obso
                                .getManufacturerStatus().getLocaleValue(),
                        Styles.DEFAULT);
                
                // Airbus Status
                writeCell(
                        row,
                        COL_H_AIRBUS_STATUS,
                        (obso.getAirbusStatus() == null) ? "" : obso
                                .getAirbusStatus().getLocaleValue(),
                        Styles.DEFAULT);
                
                row = sheet.createRow(rowNumber++);
                
            }
            
        }
        
        // Mininum de lignes = 10 pour harmoniser l'affichage sur une page.
        if (countLine < 10) {
            rowNumber += (10 - countLine);
        }
        
        rowNumber++;
        row = sheet.createRow(rowNumber++);
        writeSeparationLine(row, COL_MIN, COL_MAX);
        
    }
    
    private void writeEmptyFieldComments() {
        
        Row row = sheet.createRow(rowNumber++);
        // information utilisation
        writeCell(row, COL_HEADER,
                MessageBundle.getMessage(IOConstantsObso.COMMENTS_TITLE),
                Styles.HEADER_OBSO);
        
        row = sheet.createRow(rowNumber++);
        
        writeCell(row, COL_IDENTIFICATION_NAME, "", Styles.WRAPTEXT);
        row.setHeightInPoints(70f);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), // first row
                                                                    // (0-based)
                row.getRowNum(), // last row (0-based)
                COL_IDENTIFICATION_NAME, // first column (0-based)
                COL_CONSTITUANTNAME // last column (0-based)
        ));
        
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.writer.IOExcelBaseWriter#write(java.lang.Object)
     */
    @Override
    public void write(Object object) throws ExportException {
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
        
        pict.resize(1.0);
        
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
