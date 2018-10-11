/*
 * ------------------------------------------------------------------------
 * Class : ArticleFilterRegex
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AvailabilityStatus;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Class managing the regular expressions for searching for articles
 */
public class ArticleFilterRegex extends FilterRegexSupport<Article> {
    
    private static final long serialVersionUID = -7820711013442502749L;
    
    private ArticleState filterState;
    private UseState filterUseState;
    private String filterExternalLocationType;
    private String filterContainerType;
    private Long filterAllocation;
    private Long filterUsage;
    private Long filterProductType;
    private PC.AvailabilityStatus filterAvalabilityStatus;
    
    /**
     * Apply all filter methods on each article and count how many satisfy the
     * filters values
     * 
     * @param listItems
     *            the list of objects to filter
     * @return the number of objects satisfying the filters values
     */
    @Override
    public Integer countFiltered(List<Article> listItems) {
        Integer count = 0;
        for (Article article : listItems) {
            if (filterArticle(article)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Apply all filters on article
     * 
     * @param pArticle
     *            the article to filter
     * @return true if the article satisfies all filters, else false
     */
    private Boolean filterArticle(Article pArticle) {
        
        Boolean lResult =
                filterMethodRegex(pArticle)
                        && filterMethodExternalLocationType(pArticle)
                        && filterMethodContainerType(pArticle)
                        && filterMethodState(pArticle)
                        && filterMethodUseState(pArticle);
        
        if (pArticle instanceof PC) {
            // Specific fields of PC
            PC lPC = (PC) pArticle;
            lResult =
                    lResult && filterMethodAllocation(lPC)
                            && filterMethodUsage(lPC)
                            && filterMethodProductType(lPC)
                            && filterMethodAvailabilityStatus(lPC);
        }
        
        setResetFilters(filterValues.isEmpty() && filterState == null
                && filterExternalLocationType == null && filterUseState == null
                && filterContainerType == null && filterAllocation == null
                && filterUsage == null && filterProductType == null);
        
        return lResult;
    }
    
    @Override
    public Boolean filterMethodRegex(Article currentArticle) {
        
        Boolean result = true;
        
        String filter = filterValues.get("classe");
        String chaine =
                MessageBundle.getMessage(currentArticle.getClass()
                        .getSimpleName());
        result = compare(chaine, filter);
        
        if (result) {
            filter = filterValues.get("airbusSN");
            chaine = currentArticle.getAirbusSN();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("airbusPN");
            chaine =
                    (currentArticle.getAirbusPN() != null) ? currentArticle
                            .getAirbusPN().getIdentifier() : null; // DM0046
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturerSN");
            chaine = currentArticle.getManufacturerSN();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturerPN");
            chaine =
                    (currentArticle.getManufacturerPN() != null) ? currentArticle
                            .getManufacturerPN().getIdentifier() : null; // DM0046
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("type");
            if (currentArticle.getTypeArticle() != null) {
                chaine = currentArticle.getTypeArticle().getLabel();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("cmsCode");
            chaine = currentArticle.getCmsCode();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("acquisitionDate");
            
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            chaine =
                    (currentArticle.getAcquisitionDate() != null) ? sdf
                            .format(currentArticle.getAcquisitionDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("activeStockControlDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            chaine = null;
            if (currentArticle instanceof Board) {
                Board lBoard = (Board) currentArticle;
                if (lBoard.getActiveStockControlDate() != null) {
                    chaine =
                            lDateFormat.format(lBoard
                                    .getActiveStockControlDate());
                }
            }
            result = compare(chaine, filter);
        }
        
        Location lLocation = currentArticle.getLocation();
        if (result) {
            filter = filterValues.get("locationName");
            if (lLocation != null) {
                chaine = lLocation.getLocationName();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("externalLocationName");
            if (lLocation != null && lLocation.isExternalLocated()) {
                chaine = lLocation.getExternalLocationName();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        Container lContainer = currentArticle.getContainer();
        if (result) {
            filter = filterValues.get("containerName");
            if (lContainer != null) {
                chaine = lContainer.getContainerName();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            Container lMasterContainer = currentArticle.getMasterContainer();
            filter = filterValues.get("masterContainerName");
            if (lMasterContainer != null) {
                chaine = lMasterContainer.getContainerName();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("designation");
            if (currentArticle instanceof Rack) {
                chaine = ((Rack) currentArticle).getDesignation();
                
            }
            else if (currentArticle instanceof Cabinet) {
                chaine = ((Cabinet) currentArticle).getDesignation();
                
            }
            else {
                // Article families other than Rack or Cabinet
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("name");
            chaine = currentArticle.getName();
            result = compare(chaine, filter);
        }
        
        if (result && currentArticle instanceof PC) {
            // Fields specific to PC
            result = filterMethodRegexPC((PC) currentArticle);
        }
        
        return result;
    }
    
    private Boolean filterMethodRegexPC(PC currentPC) {
        Boolean result = true;
        String filter;
        String chaine;
        
        if (result) {
            filter = filterValues.get("function");
            chaine = currentPC.getFunction();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("inCharge");
            chaine = currentPC.getInCharge().getLoginDetails();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("admin");
            chaine = currentPC.getAdmin();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("assignment");
            chaine = currentPC.getAssignment();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("endOfSupport");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            ObsolescenceData lObsoData =
                    ((TypePC) currentPC.getTypeArticle()).getObsolescenceData();
            if (lObsoData != null && lObsoData.getEndOfSupportDate() != null) {
                chaine = lDateFormat.format(lObsoData.getEndOfSupportDate());
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("availabilityDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            if (currentPC.getAvailabilityDate() != null) {
                chaine = lDateFormat.format(currentPC.getAvailabilityDate());
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("operatingSystems");
            chaine = currentPC.getOperatingSystemsNames();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("nbScreens");
            if (currentPC.getNbScreens() != null) {
                chaine = currentPC.getNbScreens().toString();
            }
            else {
                // NbScreens is not filled in current PC
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        if (result) {
            // Compare each demand to the filter
            filter = filterValues.get("demands");
            
            if (currentPC.getReplacementDemand() != null) {
                chaine = currentPC.getReplacementDemand().getDemandNumber();
            }
            else {
                chaine = null;
            }
            boolean lResultReplacementDemand = compare(chaine, filter);
            
            if (currentPC.getAllocatedToDemand() != null) {
                chaine = currentPC.getAllocatedToDemand().getDemandNumber();
            }
            else {
                chaine = null;
            }
            boolean lResultAllocatedToDemand = compare(chaine, filter);
            
            result = lResultAllocatedToDemand || lResultReplacementDemand;
        }
        
        return result;
    }
    
    /**
     * Apply the Allocation filter on PC
     * 
     * @param pPC
     *            the PC to filter
     * @return true if the PC satisfies the Allocation filter, else false
     */
    public boolean filterMethodAllocation(PC pPC) {
        
        if (filterAllocation == null || filterAllocation.equals("all")
                || pPC == null) {
            return true;
        }
        
        return (filterAllocation.equals(pPC.getAllocation().getId()));
    }
    
    /**
     * Apply the Usage filter on PC
     * 
     * @param pPC
     *            the PC to filter
     * @return true if the PC satisfies the Usage filter, else false
     */
    public boolean filterMethodUsage(PC pPC) {
        
        if (filterUsage == null || filterUsage.equals("all") || pPC == null) {
            return true;
        }
        
        return (filterUsage.equals(pPC.getUsage().getId()));
    }
    
    /**
     * Apply the Product Type filter on PC
     * 
     * @param pPC
     *            the PC to filter
     * @return true if the PC satisfies the Product Type filter, else false
     */
    public boolean filterMethodProductType(PC pPC) {
        
        if (filterProductType == null || filterProductType.equals("all")
                || pPC == null) {
            return true;
        }
        
        return (filterProductType.equals(pPC.getProductType().getId()));
    }
    
    /**
     * Apply the Availability Status filter on PC
     * 
     * @param pPC
     *            the PC to filter
     * @return true if the PC satisfies the Availability Status filter, else
     *         false
     */
    public boolean filterMethodAvailabilityStatus(PC pPC) {
        if (filterAvalabilityStatus == null
                || filterAvalabilityStatus.equals("all") || pPC == null) {
            return true;
        }
        
        return (filterAvalabilityStatus == pPC.getAvailabilityStatus());
    }
    
    /**
     * Apply the State filter on article
     * 
     * @param current
     *            the article to filter
     * @return true if the article satisfies the State filter, else false
     */
    public boolean filterMethodState(Article current) {
        if (filterState == null || current == null) {
            return true;
        }
        return filterState.equals(current.getState());
    }
    
    /**
     * Apply the UseState filter on article
     * 
     * @param current
     *            the article to filter
     * @return true if the article satisfies the UseState filter, else false
     */
    public boolean filterMethodUseState(Article current) {
        if (filterUseState == null || current == null) {
            return true;
        }
        return filterUseState.equals(current.getUseState());
    }
    
    /**
     * Apply the external location type filter on article
     * 
     * @param currentArticle
     *            the article to filter
     * @return true if the article satisfies the external location type filter,
     *         else false
     */
    public boolean filterMethodExternalLocationType(Article currentArticle) {
        
        if (filterExternalLocationType == null
                || filterExternalLocationType.equals("all")
                || currentArticle == null) {
            return true;
        }
        
        Location lLocation = currentArticle.getLocation();
        if (filterExternalLocationType.equals("notSent")) {
            return (lLocation == null || !lLocation.isExternalLocated());
        }
        
        if (lLocation == null || !lLocation.isExternalLocated()) {
            return false;
        }
        ExternalEntityType lType =
                lLocation.getExternalEntity().getExternalEntityType();
        
        return lType.getId().equals(Long.valueOf(filterExternalLocationType));
    }
    
    /**
     * Apply the container type filter on article
     * 
     * @param currentArticle
     *            the article to filter
     * @return true if the article satisfies the container type filter, else
     *         false
     */
    public boolean filterMethodContainerType(Article currentArticle) {
        
        if (filterContainerType == null || filterContainerType.equals("all")
                || currentArticle == null) {
            return true;
        }
        if (filterContainerType.equals("notContained")) {
            return (currentArticle.getContainer() == null);
        }
        
        Container lContainer = currentArticle.getContainer();
        if (lContainer == null) {
            return false;
        }
        ContainerType lType = lContainer.getType();
        
        return lType.name().equals(filterContainerType);
    }
    
    @Override
    public void resetFilters() {
        
        super.resetFilters();
        filterState = null;
        filterExternalLocationType = null;
        filterContainerType = null;
        filterAllocation = null;
        filterUsage = null;
        filterProductType = null;
        filterAvalabilityStatus = null;
        setResetFilters(true);
    }
    
    /**
     * @return the filterState
     */
    public ArticleState getFilterState() {
        return filterState;
    }
    
    /**
     * @param filterState
     *            the filterState to set
     */
    public void setFilterState(ArticleState filterState) {
        this.filterState = filterState;
    }
    
    /**
     * @return the filterState
     */
    public UseState getFilterUseState() {
        return filterUseState;
    }
    
    /**
     * @param filterState
     *            the filterState to set
     */
    public void setFilterUseState(UseState filterUseState) {
        this.filterUseState = filterUseState;
    }
    
    /**
     * @return the filterExternalLocationType
     */
    public String getFilterExternalLocationType() {
        return filterExternalLocationType;
    }
    
    /**
     * @param filterExternalLocationType
     *            the filterExternalLocationType to set
     */
    public void
            setFilterExternalLocationType(String filterExternalLocationType) {
        this.filterExternalLocationType = filterExternalLocationType;
    }
    
    /**
     * @return the filterContainerType
     */
    public String getFilterContainerType() {
        return filterContainerType;
    }
    
    /**
     * @param filterContainerType
     *            the filterContainerType to set
     */
    public void setFilterContainerType(String filterContainerType) {
        this.filterContainerType = filterContainerType;
    }
    
    /**
     * Apply all filter methods on each article and list the ones which satisfy
     * the filters values
     * 
     * @param listItems
     *            the list of objects to filter
     * @return the list of objects satisfying the filters values
     */
    @Override
    public List<Article> getFilteredList(List<Article> listItems) {
        List<Article> lArticles = new ArrayList<Article>();
        for (Article lArticle : listItems) {
            if (filterArticle(lArticle)) {
                lArticles.add(lArticle);
            }
        }
        return lArticles;
    }
    
    /**
     * @return the filterAllocation
     */
    public Long getFilterAllocation() {
        return filterAllocation;
    }
    
    /**
     * @param filterAllocation
     *            the filterAllocation to set
     */
    public void setFilterAllocation(Long filterAllocation) {
        this.filterAllocation = filterAllocation;
    }
    
    /**
     * @return the filterUsage
     */
    public Long getFilterUsage() {
        return filterUsage;
    }
    
    /**
     * @param filterUsage
     *            the filterUsage to set
     */
    public void setFilterUsage(Long filterUsage) {
        this.filterUsage = filterUsage;
    }
    
    /**
     * @return the filterProductType
     */
    public Long getFilterProductType() {
        return filterProductType;
    }
    
    /**
     * @param filterProductType
     *            the filterProductType to set
     */
    public void setFilterProductType(Long filterProductType) {
        this.filterProductType = filterProductType;
    }
    
    /**
     * @return the filterAvalabilityStatus
     */
    public AvailabilityStatus getFilterAvalabilityStatus() {
        return filterAvalabilityStatus;
    }
    
    /**
     * @param pFilterAvalabilityStatus
     *            the filterAvalabilityStatus to set
     */
    public void setFilterAvalabilityStatus(
            AvailabilityStatus pFilterAvalabilityStatus) {
        
        filterAvalabilityStatus = pFilterAvalabilityStatus;
    }
    
}
