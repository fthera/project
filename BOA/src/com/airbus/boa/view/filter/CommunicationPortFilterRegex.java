/*
 * ------------------------------------------------------------------------
 * Class : CommunicationPortFilterRegex
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import com.airbus.boa.entity.network.CommunicationPort;

/**
 * Managing of the filter of Communication Ports results list <br>
 * No count is performed: to be used only for suggestion values lists
 */
public class CommunicationPortFilterRegex extends
        FilterRegexSupport<CommunicationPort> {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public Boolean filterMethodRegex(CommunicationPort pCurrentPort) {
        
        Boolean lResult = true;
        
        String lFilter = filterValues.get("portName");
        String lValue = pCurrentPort.getName();
        lResult = compare(lValue, lFilter);
        
        if (lResult) {
            lFilter = filterValues.get("socket");
            lValue = pCurrentPort.getSocket();
            lResult = compare(lValue, lFilter);
        }
        
        if (lResult) {
            lFilter = filterValues.get("ipAddress");
            lValue = pCurrentPort.getIpAddress();
            lResult = compare(lValue, lFilter);
        }
        
        if (lResult) {
            lFilter = filterValues.get("macAddress");
            lValue = pCurrentPort.getMacAddress();
            lResult = compare(lValue, lFilter);
        }
        
        setResetFilters(filterValues.isEmpty());
        
        return lResult;
    }
    
}
