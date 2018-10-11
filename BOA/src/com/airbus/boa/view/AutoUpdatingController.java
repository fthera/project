/*
 * ------------------------------------------------------------------------
 * Class : AutoUpdatingController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import com.airbus.boa.control.PCBean;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.exception.AutoUpdateException;

/**
 * Controller managing the updating of all PC using SYSMON tool
 */
@ManagedBean(name = AutoUpdatingController.BEAN_NAME)
@RequestScoped
public class AutoUpdatingController extends AbstractController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "autoUpdatingController";
    
    @EJB
    private PCBean pcBean;
    private BufferedWriter logWriter = null;
    
    /**
     * Store the provided messages in the log with information about the PC
     * 
     * @param pPC
     *            the concerned PC
     * @param pResultsLogs
     *            the list of results of automatic update messages to log
     * @param pErrorMessage
     *            the error message to log
     * @throws IOException
     *             when an error occurs while writing the file
     */
    private void logPCUpdate(PC pPC, List<StringBuffer> pResultsLogs,
            StringBuffer pErrorMessage) throws IOException {
        
        StringBuffer lLog = new StringBuffer();
        
        lLog.append("\r\n");
        lLog.append(pPC.getName() + "\r\n\r\n");
        if (pResultsLogs != null) {
            for (StringBuffer lMessage : pResultsLogs) {
                lLog.append(lMessage).append("\r\n");
            }
        }
        if (pErrorMessage != null) {
            lLog.append(pErrorMessage);
        }
        else {
            lLog.append("Automatic update OK");
        }
        lLog.append("\r\n");
        lLog.append("_______________________________________________\r\n");
        
        logWriter.write(lLog.toString());
    }
    
    /**
     * Update all PCs having the tag allowing automatic update, or all PCs if
     * the pGlobal parameter is true.
     * 
     * @param pGlobal
     *            boolean indicating if all the PCs shall be updated (true) or
     *            only
     *            those for which the update is specifically asked (false).
     */
    public void perform(String pGlobal) {
        
        try {
            String lLogFilePath =
                    FacesContext.getCurrentInstance().getExternalContext()
                            .getInitParameter("AutoUpdatingLogFile");
            
            File lLogFile = new File(lLogFilePath);
            
            if (!lLogFile.exists()) {
                lLogFile.createNewFile();
            }
            
            FileWriter lFileWriter = new FileWriter(lLogFile.getAbsoluteFile());
            logWriter = new BufferedWriter(lFileWriter);
            
            StringBuffer lLog = new StringBuffer();
            
            DateFormat lDateFormat =
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            
            lLog.append(
                    "______________________________________________________\r\n\r\n");
            lLog.append("   Automatic updating results - "
                    + lDateFormat.format(new Date()) + "   \r\n");
            lLog.append(
                    "______________________________________________________\r\n");
            
            // Retrieve all PC having Automatic update activated
            List<PC> lPCList;
            if (pGlobal != null && pGlobal.equalsIgnoreCase("true")) {
            	lPCList = pcBean.findAllPC();
            }
            else {
            	lPCList = pcBean.findPCAutoUpdate();
            }
            
            if (lPCList.isEmpty()) {
                lLog.append("\r\nNo PC to update!\r\n");
            }
            
            logWriter.write(lLog.toString());
            
            AutoUpdateController lAutoUpdateController =
                    findBean(AutoUpdateController.class);
            
            // Log the Sysmon virtual user
            lAutoUpdateController.logInAsSysmon();
            
            for (PC lPC : lPCList) {
                try {
                    // prepare the PC modifications on dedicated controller
                    lAutoUpdateController.doUpdatePCWithSysmon(lPC,
                            lAutoUpdateController.getSysmonVirtualUser());
                    
                    // The PC automatic update information have been updated in
                    // database through the previous treatment which is at this
                    // point successful
                    
                    logPCUpdate(lPC, lAutoUpdateController.getLogs(), null);
                    
                }
                catch (AutoUpdateException e) {
                    
                    StringBuffer lStringBuffer = new StringBuffer();
                    lStringBuffer.append("An error occurred: " + e.getMessage()
                            + " [" + e.getClass().getSimpleName() + "]");
                    
                    logPCUpdate(lPC, null, lStringBuffer);
                }
                logWriter.flush();
            }
            
            // Restore the previous user logged, if any
            lAutoUpdateController.logInBackAsPreviousLoggedUser();
            
        }
        catch (IOException ioe) {
            // nothing to do
        }
        finally {
            
            try {
                logWriter.close();
            }
            catch (IOException e) {
                // nothing to do
            }
        }
    }
    
    /**
     * Retrieve the content of the log file from the last SYSMON execution
     * 
     * @return the content of the log file as a String
     */
    public String getLog() {
        String lResult = "";
        
        String lLogFilePath = FacesContext.getCurrentInstance()
                .getExternalContext().getInitParameter("AutoUpdatingLogFile");
        File lLogFile = new File(lLogFilePath);
        
        if (lLogFile.exists()) {
            try {
                FileReader lFileReader =
                        new FileReader(lLogFile.getAbsoluteFile());
                BufferedReader lBufferedReader =
                        new BufferedReader(lFileReader);
                String lCurrentLine;
                while ((lCurrentLine = lBufferedReader.readLine()) != null) {
                    lResult += lCurrentLine + "\n";
                }
                lBufferedReader.close();
                lFileReader.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
        
        return lResult;
    }
    
}
