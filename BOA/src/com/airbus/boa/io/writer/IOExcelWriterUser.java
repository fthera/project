/*
 * ------------------------------------------------------------------------
 * Class : IOExcelWriterUser
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.writer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.entity.user.User;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.Columns;

public class IOExcelWriterUser extends IOExcelWriterBaseWriter implements Writer {
    
    private static final String sheetname = IOConstants.UserSheetName;
    
    public IOExcelWriterUser(Workbook workbook, Columns chosenColumns)
            throws ExportException {
        super(workbook, chosenColumns, sheetname);
        
    }
    
    @Override
    public void write(Object object) throws ExportException {
        if (!(object instanceof User)) {
            throw new ExportException("n'est pas un objet User");
        }
        
        User user = (User) object;
        Row row = getRow();
        
        writeField(row, user.getLogin(), IOConstants.LOGIN_TITLE);
        writeField(row, user.getLastname(), IOConstants.LASTNAME_TITLE);
        writeField(row, user.getFirstname(), IOConstants.FIRSTNAME_TITLE);
        // writeField(row,user.getPassword(),IOConstants.PASSWORD_TITLE);
        writeField(row, user.getEmail(), IOConstants.EMAIL_TITLE);
        
        if (user.getRole() != null) {
            writeField(row, user.getRole().getLocaleValue(),
                    IOConstants.ROLE_TITLE);
        }
        else {
            writeField(row, "", IOConstants.ROLE_TITLE);
        }
        
    }
    
}
