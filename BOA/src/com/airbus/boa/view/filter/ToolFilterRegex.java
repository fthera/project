/*
 * ------------------------------------------------------------------------
 * Class : ToolFilterRegex
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.Location;
import com.airbus.boa.service.Constants;

/**
 * Filter for tools
 */
public class ToolFilterRegex extends FilterRegexSupport<Tool> {
    
    private static final long serialVersionUID = -2566239091908753644L;
    
    private String filterExternalLocationType;
    private String filterContainerType;
    
    @Override
    public Integer countFiltered(List<Tool> listItems) {
        Integer lCount = 0;
        for (Tool lTool : listItems) {
            if (filterMethodContainerType(lTool)
            		&& filterMethodExternalLocationType(lTool)
                    && filterMethodRegex(lTool)) {
                lCount++;
            }
        }
        return lCount;
    }
    
    @Override
    public void resetFilters() {
        super.resetFilters();
        filterExternalLocationType = null;
        filterContainerType = null;
        setResetFilters(true);
    }
    
    /**
     * Apply the external location type filter to the provided Tool
     * 
     * @param pCurrentTool
     *            the tool on which to apply the filter
     * @return a boolean indicating if the filter criteria is satisfied or not
     */
    public boolean filterMethodExternalLocationType(Tool pCurrentTool) {
        
        if (filterExternalLocationType == null
                || filterExternalLocationType.equals("all")
                || pCurrentTool == null) {
            return true;
        }
        
        Location lLocation = pCurrentTool.getLocation();
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
     * Apply the container type filter to the provided Tool
     * 
     * @param pCurrentTool
     *            the tool on which to apply the filter
     * @return a boolean indicating if the filter criteria is satisfied or not
     */
    public boolean filterMethodContainerType(Tool pCurrentTool) {
        
        if (filterContainerType == null || filterContainerType.equals("all")
                || pCurrentTool == null) {
            return true;
        }
        if (filterContainerType.equals("notContained")) {
            return (pCurrentTool.getContainer() == null);
        }
        
        Container lContainer = pCurrentTool.getContainer();
        if (lContainer == null) {
            return false;
        }
        ContainerType lType = lContainer.getType();
        
        return lType.name().equals(filterContainerType);
    }
    
    @Override
    public Boolean filterMethodRegex(Tool current) {
        
        Boolean lResult = true;
        
        String lFilter = getFilterValues().get("name");
        String lString = current.getName();
        lResult = compare(lString, lFilter);
        
        if (lResult) {
            lFilter = getFilterValues().get("designation");
            lString = current.getDesignation();
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("personInCharge");
            lString = current.getPersonInCharge();
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("loanDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (current.getLoanDate() != null) {
                lString = lDateFormat.format(current.getLoanDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("loanDueDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (current.getLoanDueDate() != null) {
                lString = lDateFormat.format(current.getLoanDueDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        Location lLocation = current.getLocation();
        if (lResult) {
            lFilter = getFilterValues().get("locationName");
            if (lLocation != null) {
                lString = lLocation.getLocationName();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("externalLocationName");
            if (lLocation != null && lLocation.isExternalLocated()) {
                lString = lLocation.getExternalLocationName();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        Container lContainer = current.getContainer();
        if (lResult) {
            lFilter = getFilterValues().get("containerName");
            if (lContainer != null) {
                lString = lContainer.getContainerName();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        setResetFilters(getFilterValues().isEmpty());
        
        return lResult;
    }
    
    /**
     * @return the filterContainerType
     */
    public String getFilterContainerType() {
        return filterContainerType;
    }
    
    /**
     * @param pFilterContainerType
     *            the filterContainerType to set
     */
    public void setFilterContainerType(String pFilterContainerType) {
        filterContainerType = pFilterContainerType;
    }
    
    /**
     * @return the filterExternalLocationType
     */
    public String getFilterExternalLocationType() {
        return filterExternalLocationType;
    }
    
    /**
     * @param pFilterExternalLocationType
     *            the filterExternalLocationType to set
     */
    public void
            setFilterExternalLocationType(String pFilterExternalLocationType) {
        filterExternalLocationType = pFilterExternalLocationType;
    }
    
}
