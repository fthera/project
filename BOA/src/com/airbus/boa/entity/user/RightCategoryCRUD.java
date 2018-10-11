/*
 * ------------------------------------------------------------------------
 * Class : RightCategoryCRUD
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of available right categories for CRUD actions
 */
public enum RightCategoryCRUD {
    
    /** For Articles CRUD */
    ArticleCRUD,
    /** For Installations CRUD */
    InstallationCRUD,
    /** For Tools CRUD */
    ToolCRUD,
    /** For Software CRUD */
    SoftwareCRUD,
    /** For Reminders CRUD */
    ReminderCRUD,
    /** For Obsolescence data CRUD */
    ObsolescenceCRUD,
    /** For Demands CRUD */
    DemandCRUD,
    /** For Airbus PN CRUD */
    AirbusPNCRUD,
    /** For Manufacturer PN CRUD */
    ManufacturerPNCRUD,
    /** For Types CRUD */
    TypeCRUD,
    /** For Buildings, Places and External Entities CRUD */
    LocationCRUD,
    /** For Attributes Values Lists CRUD */
    ValuesListCRUD,
    /** For Users CRUD */
    UserCRUD;
    
    /**
     * @param pString
     *            the right category string returned by the
     *            <b>getStringValue</b> method
     * @return the right category if the provided string is available, else null
     */
    public static RightCategoryCRUD getEnumValue(String pString) {
        
        RightCategoryCRUD[] lValues = RightCategoryCRUD.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (RightCategoryCRUD lRightCategory : lValues) {
            lBundleKeys.add(lRightCategory.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        
        for (RightCategoryCRUD lRightCategory : RightCategoryCRUD.values()) {
            
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
        return "RightCategoryCRUD" + name();
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
