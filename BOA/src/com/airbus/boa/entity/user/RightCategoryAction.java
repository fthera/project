/*
 * ------------------------------------------------------------------------
 * Class : RightCategoryAction
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of available right categories for actions
 */
public enum RightCategoryAction {
    
    /** For search actions */
    SearchActions,
    /** For Article fields update */
    ArticleUpdateFields,
    /** For Software fields creation */
    SoftwareCreationFields,
    
    /** For User Profile fields update */
    UserProfileActionsAndFields,
    /** For User Reminders listing */
    UserRemindersList,
    
    /** For actions on files (import / export) */
    FileActions,
    /** For actions on Demands */
    DemandActions,
    /** For stock consultation */
    StockActions,
    /** For hidden administration actions */
    HiddenAdministrationActions;
    
    /**
     * @param pString
     *            the right category string returned by the
     *            <b>getStringValue</b> method
     * @return the right category if the provided string is available, else null
     */
    public static RightCategoryAction getEnumValue(String pString) {
        
        RightCategoryAction[] lValues = RightCategoryAction.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (RightCategoryAction lRightCategory : lValues) {
            lBundleKeys.add(lRightCategory.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        
        for (RightCategoryAction lRightCategory : RightCategoryAction.values()) {
            
            String lMessage = lMap.get(lRightCategory.getMessageBundleKey());
            
            if (lMessage != null && lMessage.equals(pString)) {
                return lRightCategory;
            }
        }
        return null;
    }
    
    /**
     * @return the Message Bundle key of this right category
     */
    private String getMessageBundleKey() {
        return "RightCategoryAction" + name();
    }
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the right category string which can be then converted again to
     *         the right category with the <b>getEnumValue</b> method.
     */
    public String getStringValue() {
        return MessageBundle.getMessageDefault(getMessageBundleKey());
    }
    
}
