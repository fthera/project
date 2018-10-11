/*
 * ------------------------------------------------------------------------
 * Class : IOExcelBaseReader
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.Reader;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

public abstract class IOExcelBaseReader implements Reader {
    
    public enum STYLES_ERROR {
        WARNING,
        ERROR,
        OK,
        ERROR_DATE
    }
    
    /** Enumeration to identify the parsing status */
    public enum PARSING_STATUS {
        UNKNOWN,
        OK,
        MISSING_SHEET,
        INCORRECT_HEADER,
        DATA_ERROR
    }
    
    protected static Logger log = Logger.getLogger("Reader");
    
    /**
     * Returns a default cell style for the workbook, with the given foreground
     * color.
     * 
     * @param pWorkbook
     *            the workbook for which the style is created
     * @param pIndexedColors
     *            the foreground color to apply
     * @return a CellStyle with required foreground color
     */
    protected static CellStyle SignalErrorStyle(Workbook pWorkbook,
            IndexedColors pIndexedColors) {
        Font lFont = pWorkbook.createFont();
        lFont.setBold(true);
        CellStyle lStyle = createBorderedStyle(pWorkbook, BorderStyle.THIN);
        lStyle.setFont(lFont);
        lStyle.setFillForegroundColor(pIndexedColors.getIndex());
        lStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return lStyle;
    }
    
    /**
     * Returns a date cell style for the workbook, with the given foreground
     * color.
     * 
     * @param pWorkbook
     *            the workbook for which the style is created
     * @param pIndexedColors
     *            the foreground color to apply
     * @return a CellStyle for dates with required foreground color
     */
    protected static CellStyle SignalErrorDateStyle(Workbook pWorkbook,
            IndexedColors pIndexedColors) {
        Font lFont = pWorkbook.createFont();
        lFont.setBold(true);
        CellStyle lStyle = createBorderedStyle(pWorkbook, BorderStyle.THIN);
        DataFormat lDF = pWorkbook.createDataFormat();  
        lStyle.setDataFormat(lDF.getFormat("dd/mm/yy"));
        lStyle.setFont(lFont);
        lStyle.setFillForegroundColor(pIndexedColors.getIndex());
        lStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return lStyle;
    }
    
    /**
     * Returns a date cell style for the workbook, with the given border style.
     * 
     * @param pWorkbook
     *            the workbook for which the style is created
     * @param pBorderStyle
     *            the border style to apply
     * @return a CellStyle with required border style
     */
    private static CellStyle createBorderedStyle(Workbook pWorkbook,
            BorderStyle pBorderStyle) {
        CellStyle lStyle = pWorkbook.createCellStyle();
        lStyle.setBorderRight(pBorderStyle);
        lStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        lStyle.setBorderBottom(pBorderStyle);
        lStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        lStyle.setBorderLeft(pBorderStyle);
        lStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        lStyle.setBorderTop(pBorderStyle);
        lStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return lStyle;
    }
    
    private Workbook workbook;
    protected Columns columns;
    private String sheetname;
    private Sheet sheet;
    
    protected PARSING_STATUS status;
    protected ValueListBean valueListBean;
    
    private int rownumber;
    
    private Map<STYLES_ERROR, CellStyle> styles;
    
    protected Row row;
    
    /**
     * Constructor.
     * 
     * @param pValueListBean
     *            a ValueListBean object
     * @param pWorkbook
     *            the workbook to read
     * @param pColumns
     *            the column definition of the sheet to read
     * @param pSheetname
     *            the sheet to read
     */
    public IOExcelBaseReader(ValueListBean pValueListBean, Workbook pWorkbook,
            Columns pColumns, String pSheetname) {
        this.workbook = pWorkbook;
        this.columns = pColumns;
        this.sheetname = pSheetname;
        sheet = this.workbook.getSheet(sheetname);
        this.valueListBean = pValueListBean;
        createStyles(workbook);
        status = PARSING_STATUS.UNKNOWN;
        rownumber = 0;
    }
    
    @Override
    public void read() {
        
        // Check if the sheet exists
        if (sheet == null) {
            status = PARSING_STATUS.MISSING_SHEET;
            return;
        }
        
        // Check the sheet header
        if (!checkHeader()) {
            status = PARSING_STATUS.INCORRECT_HEADER;
            ExcelUtils.writeTextBoxErrorMessage(workbook, sheet,
                    MessageBundle.getMessage(IOConstants.INVALID_HEADER));
            return;
        }
        
        // positionnement sur la première ligne de donnée
        rownumber = sheet.getFirstRowNum() + 1;
        
        int lLastRow = sheet.getLastRowNum();
        for (; rownumber <= lLastRow; rownumber++) {
            row = sheet.getRow(rownumber);
            if (row != null) {
                try {
                    readLine();
                } catch (ImportException ie) {
                    SignalErrorField(row, ie.getColumnIndex());
                    Integer lLast = columns.getMaxColumnIndex() + 2;
                    writeCellErrorMessage(row, lLast, ie.getMessage());
                    status = PARSING_STATUS.DATA_ERROR;
                } catch (Exception e) {
                    
                    StringBuffer lSB =
                            new StringBuffer(MessageBundle.getMessageResource(
                                    IOConstants.IMPORT_ERROR_READ_LINE,
                                    new Object[] { sheetname,
                                            getRownumber() }));
                    String lExceptionMessage = ExceptionUtil.getMessage(e);
                    lSB.append(lExceptionMessage);
                    e.printStackTrace();
                    Integer lLast = columns.getMaxColumnIndex() + 2;
                    // colorer la ligne
                    SignalErrorField(row, -1);
                    writeCellErrorMessage(row, lLast, lExceptionMessage);
                    status = PARSING_STATUS.DATA_ERROR;
                }
            }
        }
        // IF there was no erro, set the status to OK
        if (status.equals(PARSING_STATUS.UNKNOWN)) {
            status = PARSING_STATUS.OK;
            ExcelUtils.writeTextBoxSuccessMessage(workbook, sheet,
                    MessageBundle.getMessage(IOConstants.IMPORT_SUCCESSFUL));
        }
    }
    
    @Override
    public boolean checkHeader() {
        if (sheet == null) {
            return false;
        }
        boolean lHeaderOk = true;
        Row lFirstRow = sheet.getRow(sheet.getFirstRowNum());
        if (lFirstRow == null) {
            return false;
        }
        
        Iterator<Entry<String, Integer>> lColumnIterator =
                columns.getDefinition().iterator();
        while (lColumnIterator.hasNext() && lHeaderOk) {
            
            Entry<String, Integer> lEntry = lColumnIterator.next();
            if (!lEntry.getKey()
                    .equalsIgnoreCase(readCell(lFirstRow, lEntry.getValue()))) {
                lHeaderOk = false;
            }
        }
        return lHeaderOk;
    }

    
    /**
     * Read a specific text cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the cell number in the row (the column to read)
     * @return the read text as a String
     */
    protected String readCell(Row pRow, int pCellnum) {
        Cell lCell = pRow.getCell(pCellnum);
        if (lCell == null) {
            return null;
        }
        
        String lValue;
        try {
            lValue = lCell.getStringCellValue().trim();
            
            return (lValue.isEmpty()) ? null : lValue;
        }
        catch (Exception e) {
            try {
                lValue = new DataFormatter().formatCellValue(lCell);
                log.warning("Second attempt : " + lValue);
                return lValue;
            }
            catch (Exception fe) {
                
            }
        }
        return null;
    }
    
    /**
     * Read a specific date cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the cell number in the row (the column to read)
     * @return the read Date
     */
    protected Date readCellDate(Row pRow, int pCellnum) {
        Cell lCell = pRow.getCell(pCellnum);
        if (lCell == null) {
            return null;
        }
        Date lDate = null;
        ;
        try {
            lDate = lCell.getDateCellValue();
        }
        catch (Exception e) {
            log.warning("Not a Date value");
        }
        return lDate;
    }
    
    /**
     * Read a specific text cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the name of the column to read
     * @return the read text as a String
     */
    public String readField(Row pRow, String lColumnName) {
        int lIndex = columns.getIndex(lColumnName);
        return readCell(pRow, lIndex);
    }
    
    /**
     * Read a specific date cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the name of the column to read
     * @return the read Date
     */
    public Date readDateField(Row pRow, String lColumnName) {
        int lIndex = columns.getIndex(lColumnName);
        return readCellDate(pRow, lIndex);
    }
    
    /**
     * Read a specific numeric cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the name of the column to read
     * @return the read numeric as a Double
     */
    public Double readNumericField(Row pRow, String lColumnName) {
        int lIndex = columns.getIndex(lColumnName);
        return readCellNumeric(pRow, lIndex);
    }
    
    /**
     * Read a specific numeric cell.
     * 
     * @param pRow
     *            the row to read
     * @param pCellnum
     *            the cell number in the row (the column to read)
     * @return the read numeric as a Double
     */
    @SuppressWarnings("deprecation")
    private Double readCellNumeric(Row pRow, int pIndex) {
        Cell lCell = pRow.getCell(pIndex);
        if (lCell == null) {
            return null;
        }
        Double lResult = null;
        switch (lCell.getCellTypeEnum()) {
        case NUMERIC:
            lResult = lCell.getNumericCellValue();
            break;
        default:
        }
        return lResult;
        
    }

    
    /**
     * Create various styles for the workbook.
     * 
     * @param pWorkbook
     *            the workbook for which the style are created
     */
    protected void createStyles(Workbook pWorkbook) {
        
        styles = new HashMap<STYLES_ERROR, CellStyle>();
        
        CellStyle lStyle = SignalErrorStyle(pWorkbook, IndexedColors.RED);
        styles.put(STYLES_ERROR.ERROR, lStyle);
        
        lStyle = SignalErrorStyle(pWorkbook, IndexedColors.ORANGE);
        styles.put(STYLES_ERROR.WARNING, lStyle);
        
        lStyle = SignalErrorStyle(pWorkbook, IndexedColors.GREEN);
        styles.put(STYLES_ERROR.OK, lStyle);
        
        lStyle = SignalErrorDateStyle(pWorkbook, IndexedColors.RED);
        styles.put(STYLES_ERROR.ERROR_DATE, lStyle);
        
    }
    
    @SuppressWarnings("deprecation")
    public void SignalErrorField(Row pRow, int pIndex) {
        CellStyle lStyle = styles.get(STYLES_ERROR.ERROR);
        CellStyle lStyleDate = styles.get(STYLES_ERROR.ERROR_DATE);
        if (pIndex == -1) {
            // / colorer toute la ligne
            Integer lMinCol = columns.getMinColumnIndex();
            Integer lMaxCol = columns.getMaxColumnIndex();
            
            for (int i = (lMinCol == null) ? 0
                    : lMinCol; i <= ((lMaxCol == null) ? 0 : lMaxCol); i++) {
                
                Cell lCell = pRow.getCell(i);
                if (lCell != null) {
                    
                    switch (lCell.getCellTypeEnum()) {
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(lCell)) {
                            lCell.setCellStyle(lStyleDate);
                        }
                        else {
                            lCell.setCellStyle(lStyle);
                        }
                        break;
                    default:
                        lCell.setCellStyle(lStyle);
                    }
                    
                }
                else {
                    // on crée dans la continuité des cellules avec le style
                    // error.
                    writeCellErrorMessage(pRow, i, "");
                }
            }
        }
        else {
            Cell lCell = pRow.getCell(pIndex);
            if (lCell != null) {
                lCell.setCellStyle(lStyle);
            }
            else {
                writeCellErrorMessage(pRow, pIndex, "ERREUR ICI");
            }
        }
    }
    
    /**
     * Write an error message in a cell.
     * 
     * @param pRow
     *            the row of the cell to write
     * @param pCol
     *            the column of the cell to write
     * @param pObject
     *            an Object to write in the cell
     */
    public void writeCellErrorMessage(Row pRow, int pCol, Object pObject) {
        CellStyle style = styles.get(STYLES_ERROR.ERROR);
        if (pRow == null || pCol < 0) {
            return;
        }
        Cell lCell = pRow.createCell(pCol);
        lCell.setCellStyle(style);
        String lValue = (pObject == null) ? "" : pObject.toString();
        lCell.setCellValue(lValue);
    }
    
    /**
     * Convert a read text to an AttributeValueList object.
     * 
     * @param pRow
     *            the row of the cell that was read
     * @param pFieldValue
     *            the read text
     * @param pColumnName
     *            the column name of the cell that was read
     * @param pClass
     *            the class of the AttributeValueList to retrieve
     * @return an AttributeValueList object
     */
    protected <T extends AttributeValueList> T readValueList(Row pRow,
            String pFieldValue, String pColumnName, Class<T> pClass)
            throws ImportException {
        
        AttributeValueList lValueList =
                valueListBean.findAttributeValueListByName(pClass, pFieldValue);
        if (lValueList == null) {
            
            String lMsg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { pColumnName, pFieldValue });
            throw new ImportException(lMsg, pRow.getRowNum(),
                    columns.getIndex(pColumnName));
        }
        
        return pClass.cast(lValueList);
    }
    
    /**
     * Check that the read cell was not empty.
     * 
     * @param pRow
     *            the row of the cell to check
     * @param pFieldValue
     *            the value of the cell
     * @param pColumnName
     *            the column name of the cell to check
     */
    protected void checkNotEmptyField(Row pRow, String pFieldValue,
            String pColumnName) throws ImportException {
        if (pFieldValue == null) {
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.FIELD_MUST_NOT_BE_EMPTY,
                            new Object[] { pColumnName });
            throw new ImportException(lMsg, pRow.getRowNum(),
                    columns.getIndex(pColumnName));
        }
    }
    
    /**
     * @return the current row number
     */
    public int getRownumber() {
        return rownumber + 1; // numero de ligne à l'ecran.
    }
    
    @Override
    public int getColumnIndex(String pColumnName) {
        return columns.getIndex(pColumnName);
    }
    
    @Override
    public String getColumnName(int pIndex) {
        return columns.getName(pIndex);
    }
    
    @Override
    public Object getHeader() {
        return columns;
    }
    
    /**
     * @return a boolean indicating if the sheet was found in the workbook
     */
    public boolean isSheetFound() {
        return status != PARSING_STATUS.MISSING_SHEET;
    }
    
    /**
     * @return a boolean indicating if the parsing was successful
     */
    public boolean isSuccessful() {
        return status == PARSING_STATUS.OK;
    }
    
    /**
     * @return the parsing status
     */
    public PARSING_STATUS getParsingStatus() {
        return status;
    }
    
    /**
     * @return the number of line of the sheet
     */
    public int getLineCountToProcess() {
        return sheet.getLastRowNum();
    }
}
