/*
 * ------------------------------------------------------------------------
 * Class : MessageConstants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.service;

/**
 * History labels keys constants definition
 */
public class HistoryConstants {
    
    /* Modified fields */
    
    /** When a modification of acquisition date is performed */
    public static final String ACQUISITION_DATE = "AcquisitionDate";
    /** When a modification of active stock control date is performed */
    public static final String ACTIVE_STOCK_CONTROL_DATE =
            "activeStockControlDate";
    /** When a modification of Admin is performed */
    public static final String ADMIN = "admin";
    /** When a modification of Airbus PN is performed */
    public static final String AIRBUS_PN = "AirbusPN";
    /** When a modification of Airbus SN is performed */
    public static final String AIRBUS_SN = "AirbusSN";
    /** When a modification of article state is performed */
    public static final String ARTICLE_STATE = "State";
    /** When a modification of article state is performed */
    public static final String ARTICLE_USE_STATE = "UseState";
    /** When a modification of Assignment is performed */
    public static final String ASSIGNMENT = "assignment";
    /** When a modification of boot loader is performed */
    public static final String BOOT_LOADER = "BootLoader";
    /** When a modification of Business allocation is performed */
    public static final String BUSINESS_ALLOCATION = "businessAlloc";
    /** When a modification of Business usage is performed */
    public static final String BUSINESS_USAGE = "businessUsage";
    /** When a modification of calibration is performed */
    public static final String CALIBRATION = "Calibration";
    /** When a modification of CMS Code is performed */
    public static final String CMS_CODE = "CMSCode";
    /** When a modification of default OS is performed */
    public static final String DEFAULT_OS = "defaultOS";
    /** When a modification of the department in charge is performed */
    public static final String DEPARTMENT_IN_CHARGE = "DepartmentInCharge";
    /** When a modification of designation is performed */
    public static final String DESIGNATION = "Designation";
    /** When a modification of domain is performed */
    public static final String DOMAIN = "Domain";
    /** When a modification of Function is performed */
    public static final String FUNCTION = "function";
    /** When a modification of Person in charge is performed */
    public static final String IN_CHARGE = "inCharge";
    /** When a modification of manufacturer PN is performed */
    public static final String MANUFACTURER_PN = "ManufacturerPN";
    /** When a modification of manufacturer SN is performed */
    public static final String MANUFACTURER_SN = "ManufacturerSN";
    /** When a modification of linked documents is performed */
    public static final String MODIFY_DOCUMENTS = "ModifyDocuments";
    /** When a modification of softwares is performed */
    public static final String MODIFY_SOFTWARE = "ModifySoftware";
    /** When a modification of Number of screens is performed */
    public static final String NB_SCREENS = "PCNbScreens";
    /** When a modification of Owner is performed */
    public static final String OWNER = "Owner";
    /** When a modification of Owner Siglum is performed */
    public static final String OWNER_SIGLUM = "OwnerSiglum";
    /** When a modification of platform is performed */
    public static final String PLATFORM = "Platform";
    /** When a modification of Product type is performed */
    public static final String PRODUCT_TYPE = "ProductType";
    /** When a modification of manufacturer PN rev H is performed */
    public static final String REV_H = "ManufacturerPNRevH";
    /** When a modification of manufacturer PN rev S is performed */
    public static final String REV_S = "ManufacturerPNRevS";
    /** When a modification of article type is performed */
    public static final String TYPE_ARTICLE = "TypeArticle";
    
    /* Actions labels */
    
    /** When a Software product is installed */
    public static final String INSTALL_SOFTWARE = "InstallSoftware";
    /** When a document is linked */
    public static final String LINK_DOCUMENT = "LinkOfDocument";
    /** When a Software product is removed */
    public static final String REMOVE_SOFTWARE = "RemoveSoftware";
    /** When a document is removed */
    public static final String UNLINK_DOCUMENT = "UnlinkOfDocument";
    
    /* Other constants */
    /** When a the parent use state modification is reported on its children */
    public static final String USESTATE_PARENT_UPDATE =
            "Updated following parent update";
    /** For modifications resulting from the update of the useState to Archived  */
    public static final String ARCHIVED_UPDATE =
            "Updated following use state update to Archived";
    
}
