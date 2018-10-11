/*
 * ------------------------------------------------------------------------
 * Class : ViewExportController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.application;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.airbus.boa.service.Constants;
import com.airbus.boa.view.AbstractController;

/**
 * Controller managing the export in CSV format of the database views
 */
@ManagedBean(name = ViewExportController.BEAN_NAME)
@ViewScoped
public class ViewExportController extends AbstractController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "viewExportController";
    
    private static final String SEPARATOR = ";";
    private static final String CRLF = "\r\n";
    
    private String viewDatabaseName;
    
    private static final String VIEWS_PREFIX = "view_";
    
    @PersistenceContext(name = "ViewExportService")
    private EntityManager em;
    
    private List<String> views = new ArrayList<String>();
    
    private String selectedView = null;
    
    /**
     * Constructor
     */
    public ViewExportController() {
        
        viewDatabaseName =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getInitParameter("BoaViewDatabaseName");
    }
    
    /**
     * Retrieve the list of views of database (only views named like "view_*"
     * are kept)
     */
    @PostConstruct
    public void init() {
        
        StringBuffer lQuerySB = new StringBuffer();
        lQuerySB.append("SELECT table_name");
        lQuerySB.append(" FROM information_schema.tables");
        lQuerySB.append(" WHERE table_schema = '").append(viewDatabaseName)
                .append("'");
        lQuerySB.append(" ORDER BY table_name");
        
        Query lQuery = em.createNativeQuery(lQuerySB.toString());
        @SuppressWarnings("unchecked")
        List<Object> lResults = lQuery.getResultList();
        
        for (Object lResult : lResults) {
            
            if (lResult instanceof String) {
                
                String lTableName = (String) lResult;
                
                // Filter on views
                if (lTableName.startsWith(VIEWS_PREFIX)) {
                    views.add(lTableName);
                }
            }
        }
    }
    
    /**
     * Generate the CSV file using the selected view content.<br>
     * <b><i>selectedView</i></b> must be initialized before calling
     */
    public void doDownloadView() {
        
        if (selectedView != null) {
        
            StringBuffer lViewContent = new StringBuffer();
            
            // Read columns names
            
            StringBuffer lQueryColumnsSB = new StringBuffer();
            lQueryColumnsSB.append("SELECT column_name");
            lQueryColumnsSB.append(" FROM information_schema.columns");
            lQueryColumnsSB.append(" WHERE table_schema = '")
                    .append(viewDatabaseName).append("'");
            lQueryColumnsSB.append(" AND table_name = '").append(selectedView)
                    .append("'");
            lQueryColumnsSB.append(" ORDER BY ordinal_position ASC");
            
            Query lQueryColumns =
                    em.createNativeQuery(lQueryColumnsSB.toString());
            @SuppressWarnings("unchecked")
            List<Object> lColumns = lQueryColumns.getResultList();
            
            int lNbCol = lColumns.size();
            
            for (int i = 0; i < lNbCol; i++) {
                
                Object lColumn = lColumns.get(i);
                
                if (lColumn instanceof String) {
                    
                    String lColumnName = (String) lColumn;
                    
                    lViewContent.append('"').append(lColumnName).append('"');
                    
                    if (i < lNbCol - 1) {
                        lViewContent.append(SEPARATOR);
                    }
                }
            }
            lViewContent.append(CRLF);
            
            // Read lines
            
            StringBuffer lQuerySB = new StringBuffer();
            
            lQuerySB.append("SELECT * FROM ");
            lQuerySB.append(viewDatabaseName).append(".").append(selectedView);
            
            Query lQuery = em.createNativeQuery(lQuerySB.toString());
            @SuppressWarnings("unchecked")
            List<Object> lResults = lQuery.getResultList();
            
            for (Object lResult : lResults) {
                
                if (lResult instanceof Object[]) {
                    
                    Object[] lResultArray = (Object[]) lResult;
                    
                    for (int i = 0; i < lNbCol; i++) {
                        
                        Object lValue = lResultArray[i];
                        
                        if (lValue instanceof String) {
                            
                            String lValueStr = (String) lValue;
                            lValueStr = lValueStr.replaceAll("\"", "\"\"");
                            lViewContent.append('"').append(lValueStr)
                                    .append('"');
                        }
                        else if (lValue instanceof Number) {
                            
                            lViewContent.append('"').append(lValue)
                                    .append('"');
                        }
                        else if (lValue instanceof Timestamp) {
                            
                            Timestamp lValueTime = (Timestamp) lValue;
                            
                            SimpleDateFormat lDateFormat =
                                    new SimpleDateFormat(Constants.DATE_FORMAT);
                            lDateFormat.setTimeZone(TimeZone
                                    .getTimeZone("Europe/Paris"));
                            
                            lViewContent.append('"')
                                    .append(lDateFormat.format(lValueTime))
                                    .append('"');
                        }
                        else if (lValue instanceof Boolean) {
                            
                            Boolean lValueBoolean = (Boolean) lValue;
                            lViewContent.append(lValueBoolean);
                        }
                        
                        if (i < lNbCol - 1) {
                            lViewContent.append(SEPARATOR);
                        }
                    }
                    lViewContent.append(CRLF);
                }
            }
            
            download(lViewContent.toString().getBytes(), selectedView + ".csv");
        }
    }
    
    /**
     * @return the views
     */
    public List<String> getViews() {
        return views;
    }
    
    /**
     * @param pSelectedView
     *            the selectedView to set
     */
    public void setSelectedView(String pSelectedView) {
        selectedView = pSelectedView;
    }
    
}
