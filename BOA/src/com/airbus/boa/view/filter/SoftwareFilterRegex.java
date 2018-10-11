/*
 * ------------------------------------------------------------------------
 * Class : SoftwareFilterRegex
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.util.Arrays;
import java.util.List;

import com.airbus.boa.entity.article.Software;

/**
 * Managing of the filter of Software results list
 */
public class SoftwareFilterRegex extends FilterRegexSupport<Software> {
    
    private static final long serialVersionUID = 1L;
    
    private List<Boolean> filterOperatingSystem = Arrays.asList(true, false);
    
    @Override
    public Integer countFiltered(List<Software> listItems) {
        Integer lCount = 0;
        for (Software lSoftware : listItems) {
            if (filterMethodOperatingSystem(lSoftware)
                    && filterMethodRegex(lSoftware)) {
                lCount++;
            }
        }
        return lCount;
    }
    
    @Override
    public Boolean filterMethodRegex(Software currentSoft) {
        
        Boolean result = true;
        
        String filter = filterValues.get("name");
        String chaine = currentSoft.getName();
        result = compare(chaine, filter);
        
        if (result) {
            filter = filterValues.get("distribution");
            chaine = currentSoft.getDistribution();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("kernel");
            chaine = currentSoft.getKernel();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturer");
            chaine = currentSoft.getManufacturer();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("licence");
            chaine = currentSoft.getLicence();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("description");
            chaine = currentSoft.getDescription();
            result = compare(chaine, filter);
        }
        
        setResetFilters(filterValues.isEmpty() && filterOperatingSystem == null);
        
        return result;
    }
    
    /**
     * @param pSoftware
     *            the current software to filter
     * @return true if the software satisfies the operating system criterion,
     *         else false
     */
    public Boolean filterMethodOperatingSystem(Software pSoftware) {
        
        if (pSoftware == null || filterOperatingSystem == null) {
            return true;
        }
        return filterOperatingSystem.contains(pSoftware.getOperatingSystem());
    }
    
    @Override
    public void resetFilters() {
        
        super.resetFilters();
        filterOperatingSystem = Arrays.asList(true, false);
        setResetFilters(true);
    }
    
    /**
     * @return the filterOperatingSystem
     */
    public List<Boolean> getFilterOperatingSystem() {
        return filterOperatingSystem;
    }
    
    /**
     * @param pFilterOperatingSystem
     *            the filterOperatingSystem to set
     */
    public void setFilterOperatingSystem(List<Boolean> pFilterOperatingSystem) {
        filterOperatingSystem = pFilterOperatingSystem;
    }
    
}
