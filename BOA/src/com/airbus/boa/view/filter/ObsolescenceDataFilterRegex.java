/*
 * ------------------------------------------------------------------------
 * Class : ObsolescenceDataFilterRegex
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.context.FacesContext;

import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.service.Constants;

/**
 * @author ng0057cf
 */
public class ObsolescenceDataFilterRegex extends
        FilterRegexSupport<ObsolescenceData> {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    
    @Override
    public Boolean filterMethodRegex(ObsolescenceData obso) {
        
        Boolean result = true;
        
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        
        String filter = filterValues.get("constituantName");
        String chaine = obso.getConstituantName();
        result = compare(chaine, filter);
        
        if (result) {
            filter = filterValues.get("lastObsolescenceUpdate");
            
            chaine =
                    (obso.getLastObsolescenceUpdate() != null) ? sdf
                            .format(obso.getLastObsolescenceUpdate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("personInCharge");
            chaine = obso.getPersonInCharge();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("comments");
            chaine = obso.getCommentOnStrategy();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturer");
            chaine = obso.getManufacturer();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("supplier");
            chaine = obso.getSupplier();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("consultPeriod");
            
            chaine = (obso.getConsultPeriod() != null)
                    ? obso.getConsultPeriod().getLocaleValue() : "";
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("currentAction");
            
            chaine = (obso.getCurrentAction() != null)
                    ? obso.getCurrentAction().getLocaleValue() : "";
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("airbusStatus");
            
            chaine = (obso.getAirbusStatus() != null)
                    ? obso.getAirbusStatus().getLocaleValue() : "";
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturerStatus");
            
            chaine = (obso.getManufacturerStatus() != null)
                    ? obso.getManufacturerStatus().getLocaleValue() : "";
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("strategy");
            
            chaine = (obso.getStrategyKept() != null)
                    ? obso.getStrategyKept().getLocaleValue() : "";
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("continuityDate");
            
            chaine =
                    (obso.getContinuityDate() != null) ? sdf.format(obso
                            .getContinuityDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("endOfOrderDate");
            
            chaine =
                    (obso.getEndOfOrderDate() != null) ? sdf.format(obso
                            .getEndOfOrderDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("endOfSupportDate");
            
            chaine =
                    (obso.getEndOfSupportDate() != null) ? sdf.format(obso
                            .getEndOfSupportDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("endOfProductionDate");
            
            chaine =
                    (obso.getEndOfProductionDate() != null) ? sdf.format(obso
                            .getEndOfProductionDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("nextConsultingDate");
            
            chaine =
                    (obso.getNextConsultingDate() != null) ? sdf.format(obso
                            .getNextConsultingDate()) : null;
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("obsolescenceDate");
            
            chaine =
                    (obso.getObsolescenceDate() != null) ? sdf.format(obso
                            .getObsolescenceDate()) : null;
            result = compare(chaine, filter);
        }
        
        // il faut la liste de toutes les installations (colonnes dynamiques)
        // pour travailler avec installationsCount
        
        setResetFilters(filterValues.isEmpty());
        
        return result;
    }
    
    public Locale getLocale() {
        Locale locale = Locale.FRANCE;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        }
        return locale;
    }
    
    public String getIso3Langage() {
        return getLocale().getISO3Language();
    }
    
}
