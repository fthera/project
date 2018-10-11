/*
 * ------------------------------------------------------------------------
 * Class : DemandFilterRegex
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.demand.DemandStatus;
import com.airbus.boa.localization.Location;
import com.airbus.boa.service.Constants;

/**
 * Filter for demands
 */
public class DemandFilterRegex extends FilterRegexSupport<Demand> {
    
    private static final long serialVersionUID = 1L;
    
    private List<DemandStatus> filterStatus;
    
    private List<Long> filterProductType;
    
    @Override
    public Integer countFiltered(List<Demand> listItems) {
        
        Integer lCount = 0;
        for (Demand lDemand : listItems) {
            if (filterMethodRegex(lDemand) && filterMethodStatus(lDemand)
                    && filterMethodProductType(lDemand)) {
                lCount++;
            }
        }
        return lCount;
    }
    
    @Override
    public void resetFilters() {
        super.resetFilters();
        setResetFilters(true);
    }
    
    /**
     * Apply the status filter to the provided Demand
     * 
     * @param pCurrentDemand
     *            the demand on which to apply the filter
     * @return a boolean indicating if the filter criteria is satisfied or not
     */
    public boolean filterMethodStatus(Demand pCurrentDemand) {
        
        if (filterStatus == null || pCurrentDemand == null) {
            return true;
        }
        
        return filterStatus.contains(pCurrentDemand.getStatus());
    }
    
    /**
     * Apply the productType filter to the provided Demand
     * 
     * @param pCurrentDemand
     *            the demand on which to apply the filter
     * @return a boolean indicating if the filter criteria is satisfied or not
     */
    public boolean filterMethodProductType(Demand pCurrentDemand) {
        
        if (filterProductType == null || pCurrentDemand == null) {
            return true;
        }
        
        return (filterProductType
                .contains(pCurrentDemand.getProductTypePC()
                .getId()));
    }
    
    @Override
    public Boolean filterMethodRegex(Demand pCurrent) {
        
        Boolean lResult = true;
        
        String lFilter = getFilterValues().get("demandNumber");
        String lString = pCurrent.getDemandNumber();
        lResult = compare(lString, lFilter);
        
        if (lResult) {
            lFilter = getFilterValues().get("typePC");
            if (pCurrent.getTypePC() != null) {
                lString = pCurrent.getTypePC().getLabel();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("typePCToReplace");
            if (pCurrent.getPCToReplace() != null
                    && pCurrent.getPCToReplace().getTypeArticle() != null) {
                lString = pCurrent.getPCToReplace().getTypeArticle().getLabel();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("operatingSystem");
            lString = pCurrent.getSoftwaresString();
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("issuer");
            if (pCurrent.getIssuer() != null) {
                lString = pCurrent.getIssuer().getLoginDetails();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("pcToReplace");
            if (pCurrent.getPCToReplace() != null) {
                lString = pCurrent.getPCToReplace().getAirbusSN();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("allocatedPC");
            if (pCurrent.getAllocatedPC() != null) {
                lString = pCurrent.getAllocatedPC().getAirbusSN();
            }
            else {
                lString = null;
            }
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("needDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (pCurrent.getNeedDate() != null) {
                lString = lDateFormat.format(pCurrent.getNeedDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("availabilityDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (pCurrent.getAllocatedPC() != null
                    && pCurrent.getAllocatedPC().getAvailabilityDate() != null) {
                lString =
                        lDateFormat.format(pCurrent.getAllocatedPC()
                                .getAvailabilityDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        if (lResult) {
            lFilter = getFilterValues().get("closureDate");
            
            SimpleDateFormat lDateFormat =
                    new SimpleDateFormat(Constants.DATE_FORMAT);
            lDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            
            if (pCurrent.getClosureDate() != null) {
                lString = lDateFormat.format(pCurrent.getClosureDate());
            }
            else {
                lString = null;
            }
            
            lResult = compare(lString, lFilter);
        }
        
        Location lLocation = pCurrent.getLocation();
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
        
        setResetFilters(filterValues.isEmpty());
        
        setResetFilters(filterValues.isEmpty() && filterStatus == null
                && filterProductType == null);
        
        return lResult;
    }
    
    /**
     * @return the filterStatus
     */
    public List<DemandStatus> getFilterStatus() {
        return filterStatus;
    }
    
    /**
     * @param pFilterStatus
     *            the filterStatus to set
     */
    public void setFilterStatus(List<DemandStatus> pFilterStatus) {
        filterStatus = pFilterStatus;
    }
    
    /**
     * @return the filterProductType
     */
    public List<Long> getFilterProductType() {
        return filterProductType;
    }
    
    /**
     * @param filterProductType
     *            the filterProductType to set
     */
    public void setFilterProductType(List<Long> filterProductType) {
        this.filterProductType = filterProductType;
    }
    
}
