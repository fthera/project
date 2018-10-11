/*
 * ------------------------------------------------------------------------
 * Class : ArticleState
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of possible Article states
 */
public enum ArticleState {
    
    /**
     * When the Article is operational
     */
    Operational,
    /**
     * When the Article is in retrofit
     */
    Retrofit,
    /**
     * When the Article state is unknown
     */
    ToBeTested,
    /**
     * When the Article is out of order
     */
    OutOfOrder,
    /**
     * When the Article is definitely out of order and will no more be used
     */
    Unusable;
    
    /**
     * @param pString
     *            the article state string returned by the <b>getStringValue</b>
     *            method
     * @return the article state if the provided string is available, else null
     */
    public static ArticleState getEnumValue(String pString) {
        
        ArticleState[] lValues = ArticleState.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (ArticleState lState : lValues) {
            lBundleKeys.add(lState.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        for (ArticleState lState : ArticleState.values()) {
            String lMessage = lMap.get(lState.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lState;
            }
        }
        
        lMap = MessageBundle.getAllFrenchMessages(lBundleKeys);
        for (ArticleState lState : ArticleState.values()) {
            String lMessage = lMap.get(lState.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lState;
            }
        }
        return null;
    }
    
    /**
     * @return the Message Bundle key of this state
     */
    private String getMessageBundleKey() {
        return "ArticleState" + name();
    }
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the article state string which can be then converted again to the
     *         article state with the <b>getEnumValue</b> method.
     */
    public String getStringValue() {
        return MessageBundle.getMessageDefault(getMessageBundleKey());
    }
    
    /**
     * Return the list of SelectItem objects corresponding to all possible
     * values for the enumeration
     * 
     * @return the list of all SelectItem objects
     */
    public static List<SelectItem> getSelectItems() {
        List<SelectItem> lResult = new ArrayList<SelectItem>();
        for (ArticleState lState : ArticleState.values()) {
            lResult.add(new SelectItem(lState, lState.toString()));
        }
        return lResult;
    }
    
}
