/*
 * ------------------------------------------------------------------------
 * Class : SearchAllController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.control.query.ArticleQueryBuilder;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;

/**
 * Controller managing the search of all items
 */
@ManagedBean(name = SearchAllController.BEAN_NAME)
@SessionScoped
public class SearchAllController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchAllController";
    
    private static final String[] ARTICLE_FIELDS = {
            ArticleQueryBuilder.AIRBUS_SN, ArticleQueryBuilder.MANUFACTURER_SN,
            ArticleQueryBuilder.AIRBUS_PN, ArticleQueryBuilder.MANUFACTURER_PN,
            ArticleQueryBuilder.CMS_CODE, ArticleQueryBuilder.DESIGNATION,
            ArticleQueryBuilder.TYPE, ArticleQueryBuilder.COMMENT,
            // PC
            ArticleQueryBuilder.NAME, ArticleQueryBuilder.FUNCTION,
            ArticleQueryBuilder.IN_CHARGE_LOGIN_DETAILS,
            ArticleQueryBuilder.DATED_COMMENT,
            ArticleQueryBuilder.PC_SPECIFICITY,
            ArticleQueryBuilder.OWNER, ArticleQueryBuilder.OWNER_SIGLUM,
            ArticleQueryBuilder.ADMIN, ArticleQueryBuilder.ASSIGNMENT };
    
    private static final String[] SOFTWARE_FIELDS = { SoftwareBean.NAME,
            SoftwareBean.DISTRIBUTION, SoftwareBean.KERNEL,
            SoftwareBean.MANUFACTURER, SoftwareBean.DESCRIPTION,
            SoftwareBean.LICENCE };
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ToolBean toolBean;
    
    private String inputText;
    
    private List<Installation> foundInstallations =
            new ArrayList<Installation>();
    private List<Article> foundArticles = new ArrayList<Article>();
    private List<Software> foundSoftwares = new ArrayList<Software>();
    private List<Tool> foundTools = new ArrayList<Tool>();
    
    private boolean displayResultsPanel;
    private boolean displayInstallationsPanel;
    private boolean displayArticlesPanel;
    private boolean displaySoftwaresPanel;
    private boolean displayToolsPanel;
    
    /**
     * Display the articles panel
     */
    public void doDisplayArticles() {
        
        displayArticlesPanel = true;
        
        // hide other panels
        displayInstallationsPanel = false;
        displaySoftwaresPanel = false;
        displayToolsPanel = false;
    }
    
    /**
     * Display the installations panel
     */
    public void doDisplayInstallations() {
        
        displayInstallationsPanel = true;
        
        // hide other panels
        displayArticlesPanel = false;
        displaySoftwaresPanel = false;
        displayToolsPanel = false;
    }
    
    /**
     * Display the software products panel
     */
    public void doDisplaySoftwares() {
        
        displaySoftwaresPanel = true;
        
        // hide other panels
        displayInstallationsPanel = false;
        displayArticlesPanel = false;
        displayToolsPanel = false;
    }
    
    /**
     * Display the tools panel
     */
    public void doDisplayTools() {
        
        displayToolsPanel = true;
        
        // hide other panels
        displayInstallationsPanel = false;
        displayArticlesPanel = false;
        displaySoftwaresPanel = false;
    }
    
    /**
     * Reset search criteria
     */
    public void doResetCriteria() {
        init();
    }
    
    /**
     * Search of Articles (PC have their own search method)
     */
    public void doSearch() {
        
        // Search installations
        foundInstallations =
                locationBean.findAllInstallationsByAnyField(inputText);
        
        // Search articles
        Map<String, Object> lFilter = new HashMap<String, Object>();
        for (String lField : ARTICLE_FIELDS) {
            lFilter.put(lField, inputText);
        }
        findBean(SearchController.class).doDisplayAllQuery(lFilter, false);
        
        // Search software products
        foundSoftwares =
                softwareBean.advanceSearch(Arrays.asList(SOFTWARE_FIELDS),
                        inputText);
        
        // Search tools
        foundTools = toolBean.findToolsByAnyField(inputText);
        
        // Define display
        displayResultsPanel = true;
        // No panel containing list of results should be displayed
        displayInstallationsPanel = false;
        displayArticlesPanel = false;
        displaySoftwaresPanel = false;
        displayToolsPanel = false;
    }
    
    /**
     * This method is called when the whole bean is constructed and initialized.
     * It is called on page loading.
     */
    @PostConstruct
    private void init() {
        
        inputText = null;
        
        foundInstallations.clear();
        foundArticles.clear();
        foundSoftwares.clear();
        foundTools.clear();
        
        // No panel should be displayed
        displayResultsPanel = false;
        displayInstallationsPanel = false;
        displayArticlesPanel = false;
        displaySoftwaresPanel = false;
        displayToolsPanel = false;
    }
    
    /**
     * @return the displayArticlesPanel
     */
    public boolean isDisplayArticlesPanel() {
        return displayArticlesPanel;
    }
    
    /**
     * @return the displayInstallationsPanel
     */
    public boolean isDisplayInstallationsPanel() {
        return displayInstallationsPanel;
    }
    
    /**
     * @return the displayResultsPanel
     */
    public boolean isDisplayResultsPanel() {
        return displayResultsPanel;
    }
    
    /**
     * @return the displaySoftwaresPanel
     */
    public boolean isDisplaySoftwaresPanel() {
        return displaySoftwaresPanel;
    }
    
    /**
     * @return the displayToolsPanel
     */
    public boolean isDisplayToolsPanel() {
        return displayToolsPanel;
    }
    
    /**
     * @return the foundArticles
     */
    public List<Article> getFoundArticles() {
        return foundArticles;
    }
    
    /**
     * @return the foundInstallations
     */
    public List<Installation> getFoundInstallations() {
        return foundInstallations;
    }
    
    /**
     * @return the foundSoftwares
     */
    public List<Software> getFoundSoftwares() {
        return foundSoftwares;
    }
    
    /**
     * @return the foundTools
     */
    public List<Tool> getFoundTools() {
        return foundTools;
    }
    
    /**
     * @return the inputText
     */
    public String getInputText() {
        return inputText;
    }
    
    /**
     * @param pInputText
     *            the inputText to set
     */
    public void setInputText(String pInputText) {
        inputText = pInputText;
    }
    
    /**
     * @return the size of foundArticles
     */
    public Integer getNbFoundArticles() {
        return foundArticles.size();
    }
    
    /**
     * @return the size of foundInstallations
     */
    public Integer getNbFoundInstallations() {
        return foundInstallations.size();
    }
    
    /**
     * @return the size of foundSoftwares
     */
    public Integer getNbFoundSoftwares() {
        return foundSoftwares.size();
    }
    
    /**
     * @return the size of foundTools
     */
    public Integer getNbFoundTools() {
        return foundTools.size();
    }
    
}
