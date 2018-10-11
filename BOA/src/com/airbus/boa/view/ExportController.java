/*
 * ------------------------------------------------------------------------
 * Class : ExportController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import org.apache.poi.ss.SpreadsheetVersion;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.UserBean;
import com.airbus.boa.entity.obso.ObsolescenceReference.ReferenceType;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.io.ExportExcel;
import com.airbus.boa.io.ExportExcelTemplate;
import com.airbus.boa.util.MessageBundle;

/**
 * Controller managing export of Excel files and corresponding dialog modal
 */
@ManagedBean(name = ExportController.BEAN_NAME)
@RequestScoped
public class ExportController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 2576620194701801121L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "exportController";

    /** Excel file type for .xlsx */
    public static final String MIMETYPE_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
    /** Excel file type for .xls */
    public static final String MIMETYPE_XLS = "application/vnd.ms-excel";
    
    /** Key for the obsolescence list of reference types */
    public static final String OBSO_FOR_ITEM_VALUES_LIST = "OBSO_FOR_ITEM";
    
    /** Value of the PC Type obsolescence reference type */
    public static final String OBSO_FOR_ITEM_PC_TYPE = MessageBundle
            .getMessageDefault(ReferenceType.TYPEPC.toString());
    
    /** Value of the Airbus PN and Article Type obsolescence reference type */
    public static final String OBSO_FOR_ITEM_AIRBUS_PN_ARTICLE_TYPE =
            MessageBundle.getMessageDefault(ReferenceType.AIRBUSPN_TYPEARTICLE
                    .toString());
    
    /**
     * Value of the Manufacturer PN and Article Type obsolescence reference type
     */
    public static final String OBSO_FOR_ITEM_MANUFACTURER_PN_ARTICLE_TYPE =
            MessageBundle
                    .getMessageDefault(ReferenceType.MANUFACTURERPN_TYPEARTICLE
                            .toString());
    
    /** Value of the Software obsolescence reference type */
    public static final String OBSO_FOR_ITEM_SOFTWARE = MessageBundle
            .getMessageDefault(ReferenceType.SOFTWARE.toString());
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private UserBean userBean;
    
    @ManagedProperty(value = "export.xlsx")
    private String filename;
    
    private boolean historyExported = false;
    
    /**
     * Constructor
     */
    public ExportController() {
        super();
    }
    
    /**
     * Generate an empty template Excel file for import and launch download of
     * the generated file
     */
    public void doGenerateExcelTemplate() {
        ExportExcelTemplate lExportExcelTemplate =
                new ExportExcelTemplate(SpreadsheetVersion.EXCEL2007);
        download(lExportExcelTemplate, "ImportTemplate.xlsx", MIMETYPE_XLSX);
    }
    
    /**
     * Export the whole database into an Excel file and launch download of the
     * generated file
     */
    public void doGenerateExport() {
        
        ExportExcel lExportExcel =
                new ExportExcel(SpreadsheetVersion.EXCEL2007);
        
        lExportExcel.setHistoryExported(historyExported);
        
        LogInController lLogInController = findBean(LogInController.class);
        
        lExportExcel.writeList(articleBean.findAllArticle());
        
        lExportExcel.writeList(locationBean.findAllBuilding());
        lExportExcel.writeList(locationBean.findAllPlace());
        lExportExcel.writeList(locationBean.findAllInstallation());
        lExportExcel.writeList(locationBean.findAllExternalEntities());
        
        if (lLogInController.isAuthorized(RightCategoryCRUD.UserCRUD,
                RightMaskCRUD.CRUD_Read)) {
            lExportExcel.writeList(userBean.findUsers());
        }
        
        lExportExcel.writeList(toolBean.findAllTools());
        
        lExportExcel.writeList(articleBean.findAllTypeArticle());
        
        lExportExcel.writeList(softwareBean.findAllSoftware());
        
        lExportExcel.applyStyles();
        
        download(lExportExcel, getFilename(), MIMETYPE_XLSX);
    }
    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * @param filename
     *            the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    /**
     * @return the historyExported
     */
    public boolean isHistoryExported() {
        return historyExported;
    }
    
    /**
     * @param pHistoryExported
     *            the historyExported to set
     */
    public void setHistoryExported(boolean pHistoryExported) {
        historyExported = pHistoryExported;
    }
}
