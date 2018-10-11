/*
 * ------------------------------------------------------------------------
 * Class : ExcelUtils
 * Copyright 2017 by AIRBUS France
 * ------------------------------------------------------------------------
 */
package com.airbus.boa.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFTextbox;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.TextAlign;
import org.apache.poi.xssf.usermodel.TextAutofit;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFTextBox;
import org.apache.poi.xssf.usermodel.XSSFTextParagraph;

import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.view.AbstractController;

/**
 * Class containing various utility methods to write text boxes and comments in
 * and excel file.
 */
public class ExcelUtils {
    
    /**
     * Create an information text box in the sheet
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pMessage
     *            the message to write in the text box
     */
    public static void writeTextBoxInfoMessage(Workbook pWorkbook, Sheet pSheet,
            String pMessage) {
        Font lFont = pWorkbook.createFont();
        lFont.setColor(IndexedColors.BLACK.getIndex());
        writeTextBoxMessage(pWorkbook, pSheet, pMessage, lFont);
    }
    
    /**
     * Create a success text box in the sheet
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pMessage
     *            the message to write in the text box
     */
    public static void writeTextBoxSuccessMessage(Workbook pWorkbook,
            Sheet pSheet, String pMessage) {
        Font lFont = pWorkbook.createFont();
        lFont.setBold(true);
        lFont.setColor(IndexedColors.GREEN.getIndex());
        writeTextBoxMessage(pWorkbook, pSheet, pMessage, lFont);
    }
    
    /**
     * Create an error text box in the sheet
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pMessage
     *            the message to write in the text box
     */
    public static void writeTextBoxErrorMessage(Workbook pWorkbook,
            Sheet pSheet, String pMessage) {
        Font lFont = pWorkbook.createFont();
        lFont.setBold(true);
        lFont.setColor(IndexedColors.RED.getIndex());
        writeTextBoxMessage(pWorkbook, pSheet, pMessage, lFont);
    }
    
    /**
     * Create a text box in the sheet
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pMessage
     *            the message to write in the text box
     * @param pFont
     *            the font to use to write the message
     */
    public static void writeTextBoxMessage(Workbook pWorkbook, Sheet pSheet,
            String pMessage, Font pFont) {
        
        if (pSheet instanceof HSSFSheet) {
            HSSFPatriarch lPatriarch =
                    (HSSFPatriarch) pSheet.createDrawingPatriarch();
            int lDelta = 100;
            HSSFTextbox lTextbox = lPatriarch
                    .createTextbox(new HSSFClientAnchor(lDelta, lDelta, lDelta,
                            lDelta, (short) 1, 2, (short) 6, 6));
            HSSFRichTextString lRichTextStr = new HSSFRichTextString(pMessage);
            lRichTextStr.applyFont(pFont);
            lTextbox.setString(lRichTextStr);
            lTextbox.setHorizontalAlignment(
                    HSSFTextbox.HORIZONTAL_ALIGNMENT_CENTERED);
            lTextbox.setVerticalAlignment(
                    HSSFTextbox.VERTICAL_ALIGNMENT_CENTER);
            lTextbox.setLineWidth(2);
        }
        else {
            XSSFDrawing lDrawing =
                    (XSSFDrawing) pSheet.createDrawingPatriarch();
            int lDelta = Units.pixelToEMU(10);
            XSSFTextBox lTextbox =
                    lDrawing.createTextbox(new XSSFClientAnchor(lDelta, lDelta,
                            lDelta, lDelta, (short) 1, 2, (short) 6, 6));
            XSSFRichTextString lRichTextStr = new XSSFRichTextString(pMessage);
            lRichTextStr.applyFont(pFont);
            lTextbox.clearText();
            XSSFTextParagraph lParagraph =
                    lTextbox.addNewTextParagraph(lRichTextStr);
            lParagraph.setTextAlign(TextAlign.CENTER);
            lTextbox.setTextAutofit(TextAutofit.SHAPE);
            lTextbox.setVerticalAlignment(VerticalAlignment.CENTER);
            lTextbox.setFillColor(255, 255, 255);
            lTextbox.setLineStyleColor(0, 0, 0);
            lTextbox.setLineWidth(2);
        }
    }
    
    /**
     * Set comments on the given sheet
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pComments
     *            a map associating comments to cells
     */
    public static void setComments(Workbook pWorkbook, Sheet pSheet,
            Map<CellAddress, ? extends Comment> pComments) {
        for (CellAddress lAddress : pComments.keySet()) {
            setComment(pWorkbook, pSheet, lAddress,
                    pComments.get(lAddress).getString());
        }
    }
    
    /**
     * Set comment on the given cell
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pAddress
     *            the address of the cell
     * @param pComment
     *            the comment to write
     */
    public static void setComment(Workbook pWorkbook, Sheet pSheet,
            CellAddress pAddress, String pComment) {
        RichTextString lRichTextStr;
        if (pSheet instanceof HSSFSheet) {
            lRichTextStr = new HSSFRichTextString(pComment);
        }
        else {
            lRichTextStr = new XSSFRichTextString(pComment);
        }
        setComment(pWorkbook, pSheet, pAddress, lRichTextStr);
    }
    
    /**
     * Set comment on the given cell
     * 
     * @param pWorkbook
     *            the workbook containing the sheet to complete
     * @param pSheet
     *            the sheet to complete
     * @param pAddress
     *            the address of the cell
     * @param pRichTextStr
     *            the comment to write as a RichTextString
     */
    public static void setComment(Workbook pWorkbook, Sheet pSheet,
            CellAddress pAddress, RichTextString pRichTextStr) {
        CreationHelper lFactory = pWorkbook.getCreationHelper();
        Drawing lDrawing = pSheet.createDrawingPatriarch();
        
        ClientAnchor lAnchor = lFactory.createClientAnchor();
        lAnchor.setCol1(pAddress.getColumn());
        lAnchor.setCol2(pAddress.getColumn() + 4);
        lAnchor.setRow1(pAddress.getRow());
        lAnchor.setRow2(pAddress.getRow() + 5);
        
        Comment lComment = lDrawing.createCellComment(lAnchor);
        lComment.setString(pRichTextStr);
        pSheet.getRow(pAddress.getRow()).getCell(pAddress.getColumn())
                .setCellComment(lComment);
    }
    
    /**
     * Set data validation for the given column
     * 
     * @param pSheet
     *            the sheet to complete
     * @param pColumnIndex
     *            the index of the column
     * @param pValueList
     *            the list of authorized values
     */
    public static void setColumnValidation(Sheet pSheet, int pColumnIndex,
            List<String> pValueList) {
        DataValidationHelper lDVHelper = pSheet.getDataValidationHelper();
        int lNumberRowValidation =
                getNUMBER_ROW_DATA_VALIDATION_IMPORT_TEMPLATE();
        CellRangeAddressList lAddressList = new CellRangeAddressList(1,
                lNumberRowValidation, pColumnIndex, pColumnIndex);
        String[] lValues = new String[pValueList.size()];
        lValues = pValueList.toArray(lValues);
        DataValidationConstraint lDVConstraint =
                lDVHelper.createExplicitListConstraint(lValues);
        DataValidation lValidation =
                lDVHelper.createValidation(lDVConstraint, lAddressList);
        lValidation.setEmptyCellAllowed(true);
        lValidation.setShowErrorBox(true);
        lValidation.setShowPromptBox(true);
        lValidation.setSuppressDropDownArrow(true);
        pSheet.addValidationData(lValidation);
    }
    
    /**
     * Set data validation for the given column
     * 
     * @param pSheet
     *            the sheet to complete
     * @param pColumnIndex
     *            the index of the column
     * @param pValues
     *            an array of objects representing the authorized values
     */
    public static void setColumnValidation(Sheet pSheet, int pColumnIndex,
            Object[] pValues) {
        List<String> lValueList = new ArrayList<String>();
        for (Object lObject : pValues) {
            if (lObject instanceof AttributeValueList) {
                lValueList
                        .add(((AttributeValueList) lObject).getDefaultValue());
            }
            else {
                lValueList.add(lObject.toString());
            }
        }
        ExcelUtils.setColumnValidation(pSheet, pColumnIndex, lValueList);
    }
    
    /**
     * Retrieve the number of line that will have data validation
     * in the excel import template (defined in the web.xml file)
     * 
     * @return the number of lines
     */
    private static int getNUMBER_ROW_DATA_VALIDATION_IMPORT_TEMPLATE() {
        
        String lLimit = AbstractController
                .getInitParameter("NUMBER_ROW_DATA_VALIDATION_IMPORT_TEMPLATE");
        if (lLimit == null) {
            return 0;
        }
        try {
            return Integer.valueOf(lLimit);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
