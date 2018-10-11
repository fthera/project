/*
 * ------------------------------------------------------------------------
 * Class : DemandStatus
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.demand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of possible Demand status
 */
public enum DemandStatus {
    
    /**
     * When the demand is new.
     */
    New,
    /**
     * When the demand has been confirmed.
     */
    Confirmed,
    /**
     * When the allocated PC has been made available.
     */
    Available,
    /**
     * When the demand is closed.
     */
    Closed,
    /**
     * When the demand is cancelled.
     */
    Cancelled;
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the Message Bundle key of this state
     */
    private String getMessageBundleKey() {
        return "DemandStatus" + name();
    }
    
    /**
     * @param pString
     *            the demand status string returned by the
     *            <b>getStringValue</b>
     *            method
     * @return the demand status if the provided string is available, else null
     */
    public static DemandStatus getEnumValue(String pString) {
        
        DemandStatus[] lValues = DemandStatus.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (DemandStatus lStatus : lValues) {
            lBundleKeys.add(lStatus.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        for (DemandStatus lStatus : DemandStatus.values()) {
            String lMessage = lMap.get(lStatus.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lStatus;
            }
        }
        
        lMap = MessageBundle.getAllFrenchMessages(lBundleKeys);
        for (DemandStatus lStatus : DemandStatus.values()) {
            String lMessage = lMap.get(lStatus.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lStatus;
            }
        }
        return null;
    }
    
}
