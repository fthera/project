/*
 * ------------------------------------------------------------------------
 * Class : RightMaskAction
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of available right masks for actions
 */
public enum RightMaskAction {
    
    /*
     * Bitwise masks for Search actions
     */
    
    /** Bitwise mask for Hierarchical search */
    // 0b00000001
    HierarchicalSearch(RightCategoryAction.SearchActions, (byte) 1),
    
    /** Bitwise mask for Advanced search of all items */
    // 0b00000010
    AdvancedSearchAll(RightCategoryAction.SearchActions, (byte) 2),
    
    /*
     * Bitwise masks for Article fields update
     */
    
    /** Bitwise mask for Article update of Type */
    // 0b00000001
    ArticleUpdateType(RightCategoryAction.ArticleUpdateFields, (byte) 1),
    
    /** Bitwise mask for Article update of Airbus SN */
    // 0b00000010
    ArticleUpdateAirbusSN(RightCategoryAction.ArticleUpdateFields, (byte) 2),
    
    /** Bitwise mask for Article update of Manufacturer SN */
    // 0b00000100
    ArticleUpdateManufacturerSN(RightCategoryAction.ArticleUpdateFields,
            (byte) 4),
    
    /** Bitwise mask for Article update of Acquisition date */
    // 0b00001000
    ArticleUpdateAcquisitionDate(RightCategoryAction.ArticleUpdateFields,
            (byte) 8),
    
    /** Bitwise mask for CMS Code */
    // 0b00010000
    ArticleUpdateCMSCode(RightCategoryAction.ArticleUpdateFields, (byte) 16),
    
    /** Bitwise mask for CRUD rights of archived item */
    // 0b00000010
    ArchivedCRUDAuthorization(RightCategoryAction.ArticleUpdateFields, (byte) 32),
    
    /** Bitwise mask for UD rights of dated comments */
    // 0b00000010
    DatedCommentUDAuthorization(RightCategoryAction.ArticleUpdateFields, (byte) 64),
    
    /*
     * Bitwise masks for Software fields creation
     */
    
    /** Bitwise mask for Software Name creation */
    // 0b00000001
    SoftwareCreateName(RightCategoryAction.SoftwareCreationFields, (byte) 1),
    
    /** Bitwise mask for Software Distribution creation */
    // 0b00000010
    SoftwareCreateDistribution(RightCategoryAction.SoftwareCreationFields,
            (byte) 2),
    
    /*
     * Bitwise masks for Files actions
     */
    
    /** Bitwise mask for Export template file */
    // 0b00000001
    ExportTemplate(RightCategoryAction.FileActions, (byte) 1),
    
    /** Bitwise mask for Export all data */
    // 0b00000010
    ExportAll(RightCategoryAction.FileActions, (byte) 2),
    
    /** Bitwise mask for Import files */
    // 0b00000100
    Import(RightCategoryAction.FileActions, (byte) 4),
    
    /** Bitwise mask for Export views */
    // 0b00001000
    ExportViews(RightCategoryAction.FileActions, (byte) 8),
    
    /*
     * Bitwise masks for Demands actions
     */
    
    /** Bitwise mask for Demand confirmation */
    // 0b00000001
    DemandConfirm(RightCategoryAction.DemandActions, (byte) 1),
    
    /** Bitwise mask for Demand allocation */
    // 0b00000010
    DemandAllocate(RightCategoryAction.DemandActions, (byte) 2),
    
    /** Bitwise mask for Demand making available */
    // 0b00000100
    DemandMakeAvailable(RightCategoryAction.DemandActions, (byte) 4),
    
    /** Bitwise mask for Demand closure */
    // 0b00001000
    DemandClose(RightCategoryAction.DemandActions, (byte) 8),
    
    /** Bitwise mask for Demand cancellation */
    // 0b00010000
    DemandCancel(RightCategoryAction.DemandActions, (byte) 16),
    
    /*
     * Bitwise masks for User Profile actions and fields
     */
    
    /** Bitwise mask for User Profile update */
    // 0b00000001
    UserProfileUpdate(RightCategoryAction.UserProfileActionsAndFields, (byte) 1),
    
    /** Bitwise mask for User Profile update of Last name */
    // 0b00000010
    UserProfileUpdateLastname(RightCategoryAction.UserProfileActionsAndFields,
            (byte) 2),
    
    /** Bitwise mask for User Profile update of First name */
    // 0b00000100
    UserProfileUpdateFirstname(RightCategoryAction.UserProfileActionsAndFields,
            (byte) 4),
    
    /** Bitwise mask for User Profile update of Mail */
    // 0b00001000
    UserProfileUpdateMail(RightCategoryAction.UserProfileActionsAndFields,
            (byte) 8),
    
    /** Bitwise mask for User Profile update of Login */
    // 0b00010000
    UserProfileUpdateLogin(RightCategoryAction.UserProfileActionsAndFields,
            (byte) 16),
    
    /** Bitwise mask for User Profile update of Password */
    // 0b00100000
    UserProfileUpdatePassword(RightCategoryAction.UserProfileActionsAndFields,
            (byte) 32),
    
    /*
     * Bitwise masks for User Reminders List
     */
    
    /** Bitwise mask for User Reminders list access from the User menu */
    // 0b00000001
    UserRemindersListMenu(RightCategoryAction.UserRemindersList, (byte) 1),
    
    /** Bitwise mask for User Reminders list page direct access */
    // 0b00000010
    UserRemindersListPage(RightCategoryAction.UserRemindersList, (byte) 2),
    
    /*
     * Bitwise masks for Stock actions
     */
    
    /** Bitwise mask for Stock consultation */
    // 0b00000001
    StockRead(RightCategoryAction.StockActions, (byte) 1),
    
    /*
     * Bitwise masks for Hidden Administration actions
     */
    
    /** Bitwise mask for Sending Reminders */
    // 0b00000001
    HiddenAdministrationSendReminders(
            RightCategoryAction.HiddenAdministrationActions, (byte) 1),
    
    /** Bitwise mask for Automatic Update of PC */
    // 0b00000010
    HiddenAdministrationAutomaticUpdate(
            RightCategoryAction.HiddenAdministrationActions, (byte) 2),
    
    /** Bitwise mask for Importing Documents to link to items */
    // 0b00000100
    HiddenAdministrationImportDocuments(
            RightCategoryAction.HiddenAdministrationActions, (byte) 4),
    
    /** Bitwise mask for Updating Permissions */
    // 0b00001000
    HiddenAdministrationPermissionsUpdate(
            RightCategoryAction.HiddenAdministrationActions, (byte) 8),
    // 0b00010000
    HiddenAdministrationBOAParametersUpdate(
    		RightCategoryAction.HiddenAdministrationActions, (byte) 16);
    
    
    
    /**
     * @param pString
     *            the right mask string returned by the
     *            <b>getStringValue</b> method
     * @return the right mask if the provided string is available, else null
     */
    public static RightMaskAction getEnumValue(String pString) {
        
        RightMaskAction[] lValues = RightMaskAction.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (RightMaskAction lRightMask : lValues) {
            lBundleKeys.add(lRightMask.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        
        for (RightMaskAction lRightMask : RightMaskAction.values()) {
            
            String lMessage = lMap.get(lRightMask.getMessageBundleKey());
            
            if (lMessage != null && lMessage.equals(pString)) {
                return lRightMask;
            }
        }
        return null;
    }
    
    private byte bitwiseCode;
    
    private RightCategoryAction category;
    
    private RightMaskAction(RightCategoryAction pCategory, byte pBitwiseCode) {
        bitwiseCode = pBitwiseCode;
        category = pCategory;
    }
    
    /**
     * @return the Message Bundle key of this right mask
     */
    private String getMessageBundleKey() {
        return "RightMaskAction" + name();
    }
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the right mask string which can be then converted again to
     *         the right mask with the <b>getEnumValue</b> method.
     */
    public String getStringValue() {
        return MessageBundle.getMessageDefault(getMessageBundleKey());
    }
    
    /**
     * @return the bitwiseCode
     */
    public byte getBitwiseCode() {
        return bitwiseCode;
    }
    
    /**
     * @return the category
     */
    public RightCategoryAction getCategory() {
        return category;
    }
    
}
