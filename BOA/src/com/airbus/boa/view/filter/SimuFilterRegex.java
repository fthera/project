/*
 * ------------------------------------------------------------------------
 * Class : SimuFilterRegex
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.localization.Location;
import com.airbus.boa.service.Constants;

/**
 * Class managing the regular expressions for searching for installations
 */
public class SimuFilterRegex extends FilterRegexSupport<Installation> {
    
    private static final long serialVersionUID = -2566239091908753644L;
    
    @Override
    public Boolean filterMethodRegex(Installation current) {
        Boolean result = true;
        
        String filter = getFilterValues().get("name");
        String chaine = current.getName();
        result = compare(chaine, filter);
        
        if (result) {
            filter = getFilterValues().get("comments");
            chaine = current.getComments();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = getFilterValues().get("businessSiglum");
            chaine = current.getBusinessSiglum();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = getFilterValues().get("aircraftProgram");
            chaine =
                    (current.getAircraftProgram() != null) ? current
                            .getAircraftProgram().getDefaultValue() : null;
            result = compare(chaine, filter);
            
        }
        
        if (result) {
            filter = getFilterValues().get("personInCharge");
            chaine =
                    (current.getPersonInCharge() != null) ? current
                            .getPersonInCharge().getLoginDetails() : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("startingDate");
            
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            chaine =
                    (current.getStartingDate() != null) ? sdf.format(current
                            .getStartingDate()) : null;
            result = compare(chaine, filter);
        }
        
        Location lLocation = current.getLocation();
        if (result) {
            filter = getFilterValues().get("locationName");
            if (lLocation != null) {
                chaine = lLocation.getLocationName();
            }
            else {
                chaine = null;
            }
            result = compare(chaine, filter);
        }
        
        setResetFilters(getFilterValues().isEmpty());
        
        return result;
    }
    
}
