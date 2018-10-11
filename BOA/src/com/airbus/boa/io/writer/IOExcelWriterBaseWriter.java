/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterBaseWriter
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.Location;
import com.airbus.boa.util.MessageBundle;

public abstract class IOExcelWriterBaseWriter implements Writer {
    
    public enum Styles {
        DEFAULT,
        DATE,
        DATE_MMYY,
        DATE_HHMM,
        NB_MONTHS,
        TBD,
        RED,
        WRAPTEXT,
        WRAPTEXT_COLUMN,
        HEADER_DEFAULT,
        HEADER_MANDATORY,
        HEADER_OPTIONAL,
        SEPARATION,
        HEADER_OBSO,
        OBSO_TITLE,
        HEADER_OBSO_UNDERLINED,
        COMMENT
    }
    
    protected Workbook workbook;
    
    protected Columns columns;
    protected Sheet sheet;
    protected int rowNumber;
    
    protected Map<Styles, CellStyle> stylesMap;
    
    /**
     * Default Constructor
     */
    public IOExcelWriterBaseWriter() {
    }
    
    /**
     * Constructor
     * 
     * @param pWorkbook
     *            the Excel workbook on which to write
     * @param pColumns
     *            the PC columns into Excel files
     * @param pSheetname
     *            the Excel sheet name
     * @throws ExportException
     *             when the provided workbook is null
     */
    public IOExcelWriterBaseWriter(Workbook pWorkbook, Columns pColumns,
            String pSheetname) throws ExportException {
        
        this.workbook = pWorkbook;
        if (this.workbook == null) {
            throw new ExportException("WORKBOOK EST NULL");
        }
        stylesMap = createStyles(pWorkbook);
        this.columns = pColumns;
        sheet = pWorkbook.getSheet(pSheetname);
        if (sheet == null) {
            sheet = pWorkbook.createSheet(pSheetname);
            rowNumber = 0;
            if (sheet instanceof SXSSFSheet) {
            	((SXSSFSheet)sheet).trackAllColumnsForAutoSizing();
            }
        }
    }
    
    public void writeField(Row row, Object value, String ioconstants) {
        if (columns != null) {
            int index = columns.getIndex(ioconstants);
            Styles chosenStyle =
                    (value instanceof Date) ? Styles.DATE : Styles.DEFAULT;
            if (value instanceof Date) {
                writeCellDate(row, index, value, chosenStyle);
            }
            else {
                writeCell(row, index, value, chosenStyle);
            }
        }
    }
    
    public void writeValueListField(Row row, AttributeValueList avl,
            String columnName) {
        if (avl == null) {
            writeField(row, null, columnName);
        }
        else {
            writeField(row, avl.getLocaleValue(), columnName);
        }
    }
    
    public void writeErrorField(Row row, Object value, String ioconstants) {
        if (columns != null) {
            int index = columns.getIndex(ioconstants);
            writeCell(row, index, value, Styles.TBD);
        }
    }
    
    public void writeField(Row row, Object value, String ioconstants,
            Styles style) {
        if (columns != null) {
            int index = columns.getIndex(ioconstants);
            if (value instanceof Date) {
                writeCellDate(row, index, value, style);
            }
            else {
                writeCell(row, index, value, style);
            }
        }
    }
    
    public void writeIntegerField(Row row, Object value, String ioconstants) {
        if (columns != null) {
            int index = columns.getIndex(ioconstants);
            writeCellNumeric(row, index, value, Styles.DEFAULT);
        }
    }
    
    /**
     * création des styles utilisateurs
     * 
     * @param wb
     */
    protected Map<Styles, CellStyle> createStyles(Workbook wb) {
        
        Map<Styles, CellStyle> styles = new HashMap<Styles, CellStyle>();
        
        CellStyle style = createBorderedStyle(wb);
        styles.put(Styles.DEFAULT, style);
        
        Font font = wb.createFont();
        font.setBold(true);
        
        style = createBorderedStyle(wb);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.TBD, style);
        
        DataFormat df = wb.createDataFormat();
        
        style = createBorderedStyle(wb);
        style.setDataFormat(df.getFormat("dd/mm/yyyy"));
        styles.put(Styles.DATE, style);
        
        style = createBorderedStyle(wb);
        style.setDataFormat(df.getFormat("mmm-yy"));
        styles.put(Styles.DATE_MMYY, style);
        
        style = createBorderedStyle(wb);
        style.setDataFormat(df.getFormat("dd/mm/yyyy HH:MM"));
        styles.put(Styles.DATE_HHMM, style);
        
        style = createBorderedStyle(wb);
        style.setDataFormat(df.getFormat("0_ \"mois\""));
        styles.put(Styles.NB_MONTHS, style);
        
        style = createBorderedStyle(wb);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put(Styles.HEADER_DEFAULT, style);
        
        style = createBorderedStyle(wb);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put(Styles.HEADER_MANDATORY, style);
        
        style = createBorderedStyle(wb);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put(Styles.HEADER_OPTIONAL, style);
        
        style = createBorderedStyle(wb);
        style.setWrapText(true);
        styles.put(Styles.WRAPTEXT, style);
        
        return styles;
        
    }
    
    protected void writeCell(Row row, int col, Object o, Styles style) {
        if (row == null || col < 0) {
            return;
        }
        Cell cell = row.createCell(col);
        
        String valeur = (o == null) ? "" : o.toString();
        cell.setCellValue(valeur);
        if (style != null) {
            cell.setCellStyle(stylesMap.get(style));
        }
    }
    
    protected void writeCellDate(Row row, int col, Object o, Styles style) {
        if (row == null || col < 0) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellStyle(stylesMap.get(style));
        if (o == null) {
            cell.setCellValue("");
        }
        else {
            cell.setCellValue((Date) o);
        }
    }
    
    /**
     * Write a formula in a cell
     * 
     * @param pRow
     *            the cell's row
     * @param pCol
     *            the cell's column
     * @param pObject
     *            the formula to write
     * @param pStyles
     *            the styles to apply
     */
    protected void writeCellFormula(Row pRow, int pCol, Object pObject,
            Styles pStyles) {
        if (pRow == null || pCol < 0) {
            return;
        }
        Cell cell = pRow.createCell(pCol);
        String valeur = (pObject == null) ? "" : pObject.toString();
        cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(valeur);
        if (pStyles != null) {
            cell.setCellStyle(stylesMap.get(pStyles));
        }
    }
    
    private static CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }
    
    public abstract void write(Object object) throws ExportException;
    
    @Override
    public void writeHeader() {
        writeHeader(0, false);
    }
    
    /**
     * Writes the header line of the sheet.
     * 
     * @param pNumrow
     *            the row number on which the header shall be written
     * @param pIsImportTeamplate
     *            a boolean indicating if the workbook being written is the
     *            import template or an export file
     */
    public void writeHeader(int pNumrow, boolean pIsImportTeamplate) {
        Row firstRow = sheet.createRow(pNumrow);
        Iterator<Entry<String, Integer>> iterator =
                columns.getDefinition().iterator();
        while (iterator.hasNext()) {
            Entry<String, Integer> entry = iterator.next();
            Styles lStyles = Styles.HEADER_DEFAULT;
            if (columns.isMandatory(entry.getKey()) && pIsImportTeamplate) {
                lStyles = Styles.HEADER_MANDATORY;
            }
            else if (columns.isOptional(entry.getKey()) && pIsImportTeamplate) {
                lStyles = Styles.HEADER_OPTIONAL;
            }
            writeCell(firstRow, entry.getValue(), entry.getKey(), lStyles);
            sheet.autoSizeColumn(entry.getValue());
        }
        rowNumber++;
    }
    
    @Override
    public void writeContent(List<?> list) {
        
        try {
            for (Object object : list) {
                write(object);
                rowNumber++;
            }
            
        }
        catch (ExportException e) {
            // creation du fichier de rejet
        }
    }
    
    @Override
    public void write(List<?> list) {
        writeHeader();
        writeContent(list);
        writeFooter();
    }
    
    @Override
    public void writeOne(Object object) throws ExportException {
        write(object);
        rowNumber++;
        
    }
    
    @Override
    public void writeFooter() {
        
    }
    
    @Override
    public void applyStyles() {
        Iterator<Entry<String, Integer>> iterator =
                columns.getDefinition().iterator();
        while (iterator.hasNext()) {
            Entry<String, Integer> entry = iterator.next();
            sheet.autoSizeColumn(entry.getValue());
            
        }
        
    }
    
    @Override
    public void writeEmptyTemplate() {
        writeHeader(0, true);
        applyStyles();
        ExcelUtils.writeTextBoxInfoMessage(workbook, sheet,
                MessageBundle.getMessage(IOConstants.TEMPLATE_LEGEND));
    }
    
    protected void writeCellNumeric(Row row, int col, Object o, Styles style) {
        if (row == null || col < 0) {
            return;
        }
        Cell cell = row.createCell(col);
        
        if (o == null) {
            cell.setCellValue("");
        }
        else {
            cell.setCellType(CellType.NUMERIC);
            if (o instanceof Long) {
                cell.setCellValue((Long) o);
            }
            else if (o instanceof Integer) {
                cell.setCellValue((Integer) o);
            }
            else {
                cell.setCellValue((double) o);
            }
        }
        cell.setCellStyle(stylesMap.get(style));
    }
    
    /**
     * Write the location attributes on the row: type, name and details on
     * location
     * 
     * @param pRow
     *            the current row
     * @param pLocation
     *            the location
     */
    protected void writeLocation(Row pRow, Location pLocation) {
        
        if (pLocation != null) {
            writeField(pRow, pLocation.isInherited(),
                    IOConstants.INHERITED_LOCATION_TITLE);
            writeField(pRow, pLocation.getLocationName(),
                    IOConstants.LOCATION_TITLE);
            writeField(pRow, pLocation.getPrecision(),
                    IOConstants.LOCATION_DETAILS_TITLE);
        }
        else {
            writeField(pRow, "", IOConstants.INHERITED_LOCATION_TITLE);
            writeField(pRow, "", IOConstants.LOCATION_TITLE);
            writeField(pRow, "", IOConstants.LOCATION_DETAILS_TITLE);
        }
        
        // External location
        if (pLocation != null && pLocation.isExternalLocated()) {
            writeField(pRow, pLocation.getExternalLocationName(),
                    IOConstants.EXTERNAL_LOCATION_NAME_TITLE);
        }
        else {
            writeField(pRow, "", IOConstants.EXTERNAL_LOCATION_NAME_TITLE);
        }
    }
    
    /**
     * Write the container attributes on the row: type, name and details on
     * container (in the provided column)
     * 
     * @param pRow
     *            the current row
     * @param pContainer
     *            the container
     * @param pDetailsColumn
     *            the column name for writing details (can be null)
     */
    protected void writeContainer(Row pRow, Container pContainer,
            String pDetailsColumn) {
        
        if (pContainer != null) {
            writeField(pRow, pContainer.getContainerName(),
                    IOConstants.CONTAINER_TITLE);
            writeField(pRow, pContainer.getType(),
                    IOConstants.CONTAINER_TYPE_TITLE);
            if (pDetailsColumn != null) {
                writeField(pRow, pContainer.getPrecision(), pDetailsColumn);
            }
        }
        else {
            writeField(pRow, "", IOConstants.CONTAINER_TITLE);
            writeField(pRow, "", IOConstants.CONTAINER_TYPE_TITLE);
            if (pDetailsColumn != null) {
                writeField(pRow, "", pDetailsColumn);
            }
        }
    }
    
    /**
     * Write the master container attributes on the row: type and name
     * 
     * @param pRow
     *            the current row
     * @param pMasterContainer
     *            the master container
     */
    protected void writeMasterContainer(Row pRow, Container pMasterContainer) {
        
        if (pMasterContainer != null) {
            writeField(pRow, pMasterContainer.getContainerName(),
                    IOConstants.MASTER_CONTAINER_TITLE);
            writeField(pRow, pMasterContainer.getType(),
                    IOConstants.MASTER_CONTAINER_TYPE_TITLE);
        }
        else {
            writeField(pRow, "", IOConstants.MASTER_CONTAINER_TITLE);
            writeField(pRow, "", IOConstants.MASTER_CONTAINER_TYPE_TITLE);
        }
        
    }
    
    @Override
    public int getRowNumber() {
        return rowNumber;
    }
    
    @Override
    public Columns getColumns() {
        return columns;
    }
    
    @Override
    public Object getHeader() {
        return columns;
    }
    
    public Row getRow() {
        return sheet.createRow(rowNumber);
    }
    
}
