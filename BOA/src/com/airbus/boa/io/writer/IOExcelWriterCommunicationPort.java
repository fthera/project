/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterCommunicationPort
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.ExcelUtils;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ValueListController;

public class IOExcelWriterCommunicationPort extends IOExcelWriterBaseWriter {
    
    private static final String sheetname =
            IOConstants.CommunicationPortSheetName;
    
    private Object parent;
    
    public IOExcelWriterCommunicationPort(Workbook workbook, Columns columns)
            throws ExportException {
        super(workbook, columns, sheetname);
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof CommunicationPort)) {
            throw new ExportException("NOT A COMMUNICATION PORT OBJECT");
        }
        
        CommunicationPort port = (CommunicationPort) object;
        Row row = getRow();
        
        writeField(row, port.getId(), IOConstants.NUMID_TITLE);
        writeField(row, parent.getClass().getSimpleName(),
                IOConstants.FOR_ARTICLE_TITLE);
        if (parent instanceof PC) {
            writeField(row, ((PC) parent).getAirbusSN(),
                    IOConstants.AIRBUS_SN_TITLE);
            writeField(row, ((PC) parent).getManufacturerSN(),
                    IOConstants.MANUFACTURER_SN_TITLE);
        }
        else if (parent instanceof Board) {
            writeField(row, ((Board) parent).getAirbusSN(),
                    IOConstants.AIRBUS_SN_TITLE);
            writeField(row, ((Board) parent).getManufacturerSN(),
                    IOConstants.MANUFACTURER_SN_TITLE);
        }
        else {
            writeField(row, "", IOConstants.AIRBUS_SN_TITLE);
            writeField(row, "", IOConstants.MANUFACTURER_SN_TITLE);
        }
        
        writeField(row, port.getName(), IOConstants.NAME_TITLE);
        writeField(row, port.getIpAddress(), IOConstants.IP_ADDRESS_TITLE);
        writeField(row, port.getFixedIPString(), IOConstants.FIXED_IP_TITLE);
        writeField(row, port.getMask(), IOConstants.NETWORK_MASK_TITLE);
        writeField(row, port.getMacAddress(), IOConstants.MAC_ADDRESS_TITLE);
        
        writeValueListField(row, port.getNetwork(), IOConstants.NETWORK_TITLE);
        
        writeField(row, port.getSocket(), IOConstants.SOCKET_TITLE);
        
        writeField(row, port.getComment(), IOConstants.COMMENT_PORT_TITLE);
    }
    
    public void setParent(Object parent) {
        this.parent = parent;
    }
    
    @Override
    public void writeEmptyTemplate() {
        super.writeEmptyTemplate();
        
        ValueListController lController =
                AbstractController.findBean(ValueListController.class);
        ValueListBean lValueListBean = lController.getValueListBean();
        
        int lColumn;
        List<String> lValueList;
        
        lColumn = this.columns.getIndex(IOConstants.FOR_ARTICLE_TITLE);
        String[] lValues = new String[] { Board.class.getSimpleName(),
                PC.class.getSimpleName() };
        ExcelUtils.setColumnValidation(sheet, lColumn, lValues);
        
        lColumn = this.columns.getIndex(IOConstants.NETWORK_TITLE);
        lValueList = lValueListBean.getAllValues(Network.class);
        ExcelUtils.setColumnValidation(sheet, lColumn, lValueList);
    }
}
