/*
 * ------------------------------------------------------------------------
 * Class : UseState
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of possible Article use states
 */
public enum UseState {
    
    /**
     * When the Article is in used
     */
    InUse,
    /**
     * When the Article is used as a spare
     */
    AllocatedSpare,
    /**
     * When the Article is in stock
     */
    InStock,
    /**
     * When the Article is in stock
     */
    Loaned,
    /**
     * When the Article is going to be removed
     */
    ToBeRemoved,
    /**
     * When the Article is no longer in the park
     */
    Archived,
    /**
     * When the Article has been internally purchased and has not been received
     */
    OnInternalPurchase,
    /**
     * When the Article has been externally purchased and has not been received
     */
    OnExternalPurchase;
    
    /**
     * @param pString
     *            the use state string returned by the <b>getStringValue</b>
     *            method
     * @return the use state if the provided string is available, else null
     */
    public static UseState getEnumValue(String pString) {
        
        UseState[] lValues = UseState.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (UseState lState : lValues) {
            lBundleKeys.add(lState.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        for (UseState lState : UseState.values()) {
            String lMessage = lMap.get(lState.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lState;
            }
        }
        
        lMap = MessageBundle.getAllFrenchMessages(lBundleKeys);
        for (UseState lState : UseState.values()) {
            String lMessage = lMap.get(lState.getMessageBundleKey());
            if (lMessage != null && lMessage.equals(pString)) {
                return lState;
            }
        }
        return null;
    }
    
    /**
     * Returns if the state passed as parameter is one of the on purchase one.
     * 
     * @param pUseState
     *            State to be tested
     * @return True if state is either OnExternalPurchase or OnInternalPurchase
     *         and false otherwise.
     */
    public static boolean isOnPurchase(UseState pUseState) {
        if (pUseState != null) {
            switch (pUseState) {
            case OnExternalPurchase:
            case OnInternalPurchase:
                return true;
            default:
                return false;
            }
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns all possible article states with or without the Archived state.
     * 
     * @param isArchivedVisible
     *            boolean indicating if Archived should be in the returned array
     * @return An array of all possible article states.
     */
    public static UseState[] values(boolean isArchivedVisible) {
        if (isArchivedVisible) {
            return UseState.values();
        }
        else {
            ArrayList<UseState> lValues = new ArrayList<UseState>();
            for (UseState lUseState : UseState.values()) {
                if (lUseState != UseState.Archived) {
                    lValues.add(lUseState);
                }
            }
            return lValues.toArray(new UseState[lValues.size()]);
        }
    }
    
    /**
     * Returns true if the state passed as parameter is one of the on purchase
     * one.
     * 
     * @param pUseState
     *            State to be tested
     * @return True if state is either OnExternalPurchase or OnInternalPurchase
     *         and false otherwise.
     */
    public static boolean isApplicable(UseState pUseState,
            Article pArticle) {
        if (pUseState != null) {
            switch (pUseState) {
            case OnExternalPurchase:
            case OnInternalPurchase:
                return pArticle instanceof PC;
            default:
                return true;
            }
        }
        else {
            return false;
        }
    }
    
    /**
     * @return the Message Bundle key of this state
     */
    private String getMessageBundleKey() {
        return "UseState" + name();
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
    public static List<SelectItem>
            getSelectItems(boolean pIsArchivedAuthorized) {
        List<SelectItem> lResult = new ArrayList<SelectItem>();
        for (UseState lState : UseState.values()) {
            if (lState != UseState.Archived || pIsArchivedAuthorized) {
                lResult.add(new SelectItem(lState, lState.toString()));
            }
        }
        return lResult;
    }
}
