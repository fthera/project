/*
 * ------------------------------------------------------------------------
 * Class : ContainerType
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate listing the BOA existing containers
 */
public enum ContainerType {
    
    /** When the entity is contained by a cabinet */
    Cabinet,
    /** When the entity is contained by a rack */
    Rack,
    /** When the entity is contained by a switch */
    Switch,
    /** When the entity is contained by motherboard */
    Board,
    /** When the entity is contained by a PC */
    PC,
    /** When the entity is contained by a tool */
    Tool,
    /** When the entity is contained by an installation */
    Installation,
    /** When the entity is not contained */
    NotContained;
    
    /**
     * @return the Message Bundle key of this state
     */
    private String getMessageBundleKey() {
        return "ContainerType" + name();
    }
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * Equivalent to the toString() method but using the default locale instead
     * of the current one
     * 
     * @return the value string in default locale
     */
    public String getStringValue() {
        return MessageBundle.getMessageDefault(getMessageBundleKey());
    }

    
    /**
     * @param pString
     *            the container type string returned by the
     *            <b>getStringValue</b>
     *            method
     * @return the container type if the provided string is available, else null
     */
    public static ContainerType getEnumValue(String pString) {
        
        ContainerType[] lValues = ContainerType.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (ContainerType lType : lValues) {
            lBundleKeys.add(lType.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        for (ContainerType lType : ContainerType.values()) {
            String lMessage = lMap.get(lType.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lType;
            }
        }
        
        lMap = MessageBundle.getAllFrenchMessages(lBundleKeys);
        for (ContainerType lType : ContainerType.values()) {
            String lMessage = lMap.get(lType.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lType;
            }
        }
        return null;
    }
    
    /**
     * Return an array of all values but the NotContained one
     * 
     * @return the array of all contained values
     */
    public static ContainerType[] valuesContained() {
        ArrayList<ContainerType> lValues = new ArrayList<ContainerType>();
        for (ContainerType lContainer : ContainerType.values()) {
            if (lContainer != ContainerType.NotContained) {
                lValues.add(lContainer);
            }
        }
        return lValues.toArray(new ContainerType[lValues.size()]);
    }
    
    /**
     * Return the list of SelectItem objects corresponding to all possible
     * values for the enumeration
     * 
     * @return the list of all SelectItem objects
     */
    public static List<SelectItem> getSelectItems() {
        List<SelectItem> lResult = new ArrayList<SelectItem>();
        for (ContainerType lType : ContainerType.values()) {
            lResult.add(new SelectItem(lType, lType.toString()));
        }
        return lResult;
    }
}