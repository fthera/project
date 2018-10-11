/*
 * ------------------------------------------------------------------------
 * Class : ReportingObsoController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.obso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.richfaces.component.SortOrder;
import org.richfaces.model.Filter;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.io.column.ColumnObsolescence;
import com.airbus.boa.io.obso.ExportExcelObso;
import com.airbus.boa.io.obso.writer.IOExcelObsoGeneratePlatformSheet;
import com.airbus.boa.io.obso.writer.IOExcelObsoGenerateSheet;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ExportController;
import com.airbus.boa.view.Utils;
import com.airbus.boa.view.filter.ObsolescenceDataFilterRegex;

/**
 * @author ng0057cf
 */
@ManagedBean(name = ReportingObsoController.BEAN_NAME)
@SessionScoped
public class ReportingObsoController extends AbstractController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "reportingObsoController";

    @EJB
    private ObsolescenceBean obsoBean;
    
    @EJB
    private LocationBean locationBean;
    
    private ObsolescenceData selectedObsoData;
    
    private List<ObsolescenceData> searchResults =
            new ArrayList<ObsolescenceData>();
    
    /*
     * nom du constituant de la ligne selectionnée.
     */
    private String constituantName;
    
    private List<Installation> selectedPlatforms;
    
    // progressBar
    private boolean renderedProgressPanel = false;
    
    private boolean renderedProgressBar = false;
    
    private boolean generatePlatformButtonDisabled = false;
    
    private int progressCurrentValue;
    
    /**
     * indispensable pour le fonctionement des tris sur les rich:columns
     */
    private Map<String, SortOrder> sortOrders =
            new HashMap<String, SortOrder>();
    
    private ObsolescenceDataFilterRegex obsoFilterRegex =
            new ObsolescenceDataFilterRegex();
    
    private boolean resetFilters;
    
    private Integer countFiltered;
    
    /**
     * <pre>
     * <param-name>PASSED_DATE_COLOR</param-name>
     *   	   <param-name>0_TO_45_DAYS_COLOR</param-name>
     *   	   <param-name>45_TO_90_DAYS_COLOR</param-name>
     * </pre>
     * 
     * @param date
     * @return
     */
    public String colorationDate(Date date) {
        
        final int DAYS_IN_MILLISEC = 24 * 60 * 60 * 1000;
        if (date == null) {
            return null;
        }
        Date today = new Date();
        
        int deltaDays =
                (int) ((date.getTime() - today.getTime()) / DAYS_IN_MILLISEC);
        StringBuffer sb = new StringBuffer("font-weight: bold;color:");
        
        if (today.after(date)) {
            return sb.append(getInitParameter("PASSED_DATE_COLOR")).append(";")
                    .toString();
        }
        else if (deltaDays < 45) {
            return sb.append(getInitParameter("0_TO_45_DAYS_COLOR"))
                    .append(";").toString();
        }
        else if (deltaDays < 90) {
            return sb.append("#000000").append(";background-color:")
                    .append(getInitParameter("45_TO_90_DAYS_COLOR"))
                    .append(";").toString();
        }
        return null;
    }
    
    /**
     * return le style carartère gras et couleur rouge pour un nom de
     * constitutant commencant par "ERR.*!.*
     * 
     * @param obso
     * @return error Style
     */
    public String ColorErrorOnConstituant(ObsolescenceData obso) {
        return (obso.getConstituantName().matches("^ERR.*!.*")) ? "font-weight: bold;color:red;"
                : "";
    }
    
    /**
     * Retrieve the obsolescence data for the given platforms
     * 
     * @param pObsolist
     * @param pSelectedPlatforms
     * @return the list of existing obsolescence data for the given platforms
     */
    private List<ObsolescenceData> compileObsoforPlatforms(
            List<ObsolescenceData> pObsolist,
            List<Installation> pSelectedPlatforms) {
        
        List<ObsolescenceData> results = new ArrayList<ObsolescenceData>();
        
        for (ObsolescenceData lObso : pObsolist) {
            obsoBean.computeStock(lObso);
            for (Installation lInst : pSelectedPlatforms) {
                if (lObso.getStock().getRepartitionMap().containsKey(lInst)
                        && !results.contains(lObso)) {
                    results.add(lObso);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Generate the Excel file containing the displayed obsolescence data and
     * propose its download
     */
    public void doExportObsolescence() {
        
        List<ObsolescenceData> lObsoList =
                obsoFilterRegex.getFilteredList(searchResults);
        Set<Installation> lInstallationSet = new TreeSet<Installation>();
        for (ObsolescenceData lObso : lObsoList) {
            obsoBean.computeStock(lObso);
            lInstallationSet.addAll(lObso.getInstallations());
        }
        Installation[] lInstallationArray = lInstallationSet
                .toArray(new Installation[lInstallationSet.size()]);
        
        String[] lDynamicColumns = new String[lInstallationSet.size()];
        for (int i = 0; i < lInstallationSet.size(); i++) {
            lDynamicColumns[i] = lInstallationArray[i].getName();
        }
        ColumnObsolescence lColumn = new ColumnObsolescence();
        lColumn.setDynamicColumns(lDynamicColumns);
        
        ExportExcelObso excelObso =
                new ExportExcelObso(SpreadsheetVersion.EXCEL2007, lColumn,
                        lColumn.getIndexFirstDynamicColumn());
        excelObso.writeList(lObsoList);
        excelObso.applyStyles();
        
        download(excelObso, "exportObsolescence.xlsx",
                ExportController.MIMETYPE_XLSX);
    }
    
    public void doGenerateConstituantForm() {
        
        if (selectedObsoData != null) {
            
            Workbook workbook = new XSSFWorkbook();
            try {
                IOExcelObsoGenerateSheet constituantFormExcel =
                        new IOExcelObsoGenerateSheet(
                                workbook,
                                StringUtil
                                        .convertStringForSheetName(
                                                selectedObsoData
                                                .getConstituantName()));
                
                String relativePathJpg = getInitParameter("LOGO_RELATIVE_PATH");
                
                String fullPathLogo = getFullPath(relativePathJpg);
                
                if (fullPathLogo != null) {
                    // log.warning("LOGO FULL PATH : " + fullPathLogo ) ;
                    constituantFormExcel.loadLogoImage(fullPathLogo,
                            Workbook.PICTURE_TYPE_JPEG);
                }
                obsoBean.computeStock(selectedObsoData);
                constituantFormExcel.generateObsoForm(selectedObsoData);
                download(constituantFormExcel, "constituant.xlsx",
                        ExportController.MIMETYPE_XLSX);
                
            }
            catch (ExportException e) {
                Utils.addFacesMessage(NavigationConstants.OBSO_LIST_ERROR_ID,
                        e.getMessage());
            }
        }
        
    }
    
    public void doGeneratePlatforms() {
        // log.warning("\nPlateformes selectionnées : \n" +
        // String.valueOf(selectedPlatforms)) ;
        if (selectedPlatforms != null && selectedPlatforms.size() > 0) {
            
            List<ObsolescenceData> restritedToSelectedPlatforms =
                    compileObsoforPlatforms(getSearchResults(),
                            selectedPlatforms);
            Workbook workbook = new XSSFWorkbook();
            
            String relativePathJpg = getInitParameter("LOGO_RELATIVE_PATH");
            
            String fullPathLogo = getFullPath(relativePathJpg);
            
            try {
                IOExcelObsoGeneratePlatformSheet platformSheet = null;
                boolean first = true;
                int logoIndex = -1;
                for (Installation platform : selectedPlatforms) {
                    platformSheet =
                            new IOExcelObsoGeneratePlatformSheet(workbook,
                                    platform, logoIndex);
                    if (first) {
                        logoIndex =
                                platformSheet.loadLogoImage(fullPathLogo,
                                        Workbook.PICTURE_TYPE_JPEG);
                        first = false;
                        platformSheet.setLogoImageIndex(logoIndex);
                    }
                    
                    platformSheet.generatePlatFormSheet(
                            restritedToSelectedPlatforms);
                }
                // la derniere feuille platformSheet envoie l'ordre d'ecriture
                // //NEED REFACTORISATION VERS UN OBJET ENGLOBANT.
                if (platformSheet != null) {
                    // on envoie dans la reponseServlet
                    download(platformSheet, "platforms.xlsx",
                            ExportController.MIMETYPE_XLSX);
                }
                
            }
            catch (ExportException e) {
                Utils.addFacesMessage(
                        NavigationConstants.GEN_PLATFORM_SHEET_ERROR_ID,
                        e.getMessage());
            }
        }
        
    }
    
    public void doListObso() {
        setSearchResults(obsoBean.findAllObsoData());
    }
    
    public void doResetFilters() {
        obsoFilterRegex.resetFilters();
    }
    
    public void doSelectObso(Long pSelectedObsoId) {
        setSelectedObsoData(obsoBean.findById(pSelectedObsoId));
        
    }
    
    /**
     * Return the filter for obsolescence data
     * 
     * @return the requested filter object
     */
    public Filter<?> getObsoFilter() {
        return new Filter<ObsolescenceData>() {
            
            public boolean accept(ObsolescenceData item) {
                return obsoFilterRegex.filterMethodRegex(item);
            }
        };
    }
    
    /**
     * @return the constituantName
     */
    public String getConstituantName() {
        return constituantName;
    }
    
    /**
     * @param constituantName
     *            the constituantName to set
     */
    public void setConstituantName(String constituantName) {
        this.constituantName = constituantName;
    }
    
    // le nombre d'article retournée par les filtres
    public Integer getCountFiltered() {
        countFiltered = obsoFilterRegex.countFiltered(getSearchResults());
        return countFiltered;
    }
    
    public Map<String, String> getFilterValues() {
        return obsoFilterRegex.getFilterValues();
    }
    
    public void setFilterValues(Map<String, String> filterValues) {
        obsoFilterRegex.setFilterValues(filterValues);
    }
    
    public String getGenerateObsoButtonName() {
        
        StringBuffer sb =
                new StringBuffer(
                        MessageBundle.getMessage("generateConstituantSheet"));
        if (!StringUtil.isEmptyOrNull(getConstituantName())) {
            sb.append(" ").append(getConstituantName());
        }
        else {
            
            sb.append(" <constituent>");
        }
        return sb.toString();
        
    }
    
    /**
     * @return the generatePlatformButtonDisabled
     */
    public boolean isGeneratePlatformButtonDisabled() {
        return generatePlatformButtonDisabled;
    }
    
    /**
     * @param generatePlatformButtonDisabled
     *            the generatePlatformButtonDisabled to set
     */
    public void setGeneratePlatformButtonDisabled(
            boolean generatePlatformButtonDisabled) {
        this.generatePlatformButtonDisabled = generatePlatformButtonDisabled;
    }
    
    /**
     * @return the obsoBean
     */
    public ObsolescenceBean getObsoBean() {
        return obsoBean;
    }
    
    /**
     * @return the platforms
     */
    public List<Installation> getPlatforms() {
        return locationBean.findAllInstallation();
    }
    
    /**
     * @return the progressCurrentValue
     */
    public int getProgressCurrentValue() {
        return progressCurrentValue;
    }
    
    /**
     * @param progressCurrentValue
     *            the progressCurrentValue to set
     */
    public void setProgressCurrentValue(int progressCurrentValue) {
        this.progressCurrentValue = progressCurrentValue;
    }
    
    /**
     * @return the renderedProgressBar
     */
    public boolean isRenderedProgressBar() {
        return renderedProgressBar;
    }
    
    /**
     * @param renderedProgressBar
     *            the renderedProgressBar to set
     */
    public void setRenderedProgressBar(boolean renderedProgressBar) {
        this.renderedProgressBar = renderedProgressBar;
    }
    
    /**
     * @return the renderedProgressPanel
     */
    public boolean isRenderedProgressPanel() {
        return renderedProgressPanel;
    }
    
    /**
     * @param renderedProgressPanel
     *            the renderedProgressPanel to set
     */
    public void setRenderedProgressPanel(boolean renderedProgressPanel) {
        this.renderedProgressPanel = renderedProgressPanel;
    }
    
    public boolean isResetFilters() {
        resetFilters = obsoFilterRegex.isResetFilters();
        return resetFilters;
    }
    
    /**
     * @return the searchResults
     */
    public List<ObsolescenceData> getSearchResults() {
        return searchResults;
    }
    
    /**
     * @param searchResults
     *            the searchResults to set
     */
    public void setSearchResults(List<ObsolescenceData> searchResults) {
        this.searchResults = searchResults;
    }
    
    /**
     * @return the selectedObsoData
     */
    public ObsolescenceData getSelectedObsoData() {
        return selectedObsoData;
    }
    
    /**
     * @param selectedObsoData
     *            the selectedObsoData to set
     */
    public void setSelectedObsoData(ObsolescenceData selectedObsoData) {
        // log.warning("setSelectedObsoData value=" + selectedObsoData) ;
        this.selectedObsoData = selectedObsoData;
        setConstituantName(selectedObsoData.getConstituantName());
    }
    
    /**
     * @return the selectedPlatforms
     */
    public List<Installation> getSelectedPlatforms() {
        return selectedPlatforms;
    }
    
    /**
     * @param selectedPlatforms
     *            the selectedPlatforms to set
     */
    public void setSelectedPlatforms(List<Installation> selectedPlatforms) {
        this.selectedPlatforms = selectedPlatforms;
    }
    
    /**
     * @return the sortOrders
     */
    public Map<String, SortOrder> getSortOrders() {
        return sortOrders;
    }
    
    /**
     * @param sortOrders
     *            the sortOrders to set
     */
    public void setSortOrders(Map<String, SortOrder> sortOrders) {
        this.sortOrders = sortOrders;
    }
}
