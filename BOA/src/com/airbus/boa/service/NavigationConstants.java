/*
 * ------------------------------------------------------------------------
 * Class : NavigationConstants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.service;

/**
 * Define constants for navigation pages
 */
public class NavigationConstants {
    
    /**
     * The parameter to add to the navigation string in order to perform a
     * redirection. <br>
     * The navigation string must be an address (and not a navigation rule).
     */
    public static final String REDIRECT_NAVIGATION_SUFFIX =
            "?faces-redirect=true";
    
    /*
     * Common pages
     */
    
    /** The Main page */
    public static final String MAIN_PAGE = "/main";
    
    /*
     * Installation pages
     */
    
    /** Installation creation, consultation and modification page */
    public static final String INSTALLATION_MANAGEMENT_PAGE =
            "/itemManagement/InstallationManagement";
    
    /** Installations list page */
    public static final String INSTALLATION_LIST_PAGE =
            "/itemList/InstallationList";

    /*
     * Articles pages
     */
    
    /** Cabinet creation, consultation and modification page */
    public static final String CABINET_MANAGEMENT_PAGE =
            "/itemManagement/CabinetManagement";
    
    /** Cabinets list page */
    public static final String CABINET_LIST_PAGE = "/itemList/CabinetList";
    
    /** Cabinet creation, consultation and modification page */
    public static final String RACK_MANAGEMENT_PAGE =
            "/itemManagement/RackManagement";
    
    /** Cabinets list page */
    public static final String RACK_LIST_PAGE = "/itemList/RackList";
    
    /** Cabinet creation, consultation and modification page */
    public static final String SWITCH_MANAGEMENT_PAGE =
            "/itemManagement/SwitchManagement";
    
    /** Cabinets list page */
    public static final String SWITCH_LIST_PAGE = "/itemList/SwitchList";
    
    /** Cabinet creation, consultation and modification page */
    public static final String BOARD_MANAGEMENT_PAGE =
            "/itemManagement/BoardManagement";
    
    /** Cabinets list page */
    public static final String BOARD_LIST_PAGE = "/itemList/BoardList";
    
    /** Cabinet creation, consultation and modification page */
    public static final String VARIOUS_MANAGEMENT_PAGE =
            "/itemManagement/VariousManagement";
    
    /** Cabinets list page */
    public static final String VARIOUS_LIST_PAGE = "/itemList/VariousList";
    
    /*
     * PC pages
     */
    
    /** PC consultation page */
    public static final String PC_MANAGEMENT_PAGE =
            "/itemManagement/PCManagement";
    /** Tools list page */
    public static final String PC_LIST_PAGE = "/itemList/PCList";
    
    /*
     * Software pages
     */
    
    /** Software creation, consultation and modification page */
    public static final String SOFTWARE_MANAGEMENT_PAGE =
            "/itemManagement/SoftwareManagement";
    
    /** Software list page */
    public static final String SOFTWARE_LIST_PAGE = "/itemList/SoftwareList";
    
    /*
     * Tool pages
     */
    
    /** Tool creation, consultation and modification page */
    public static final String TOOL_MANAGEMENT_PAGE =
            "/itemManagement/ToolManagement";
    /** Tools list page */
    public static final String TOOL_LIST_PAGE = "/itemList/ToolList";
    
    /*
     * Obsolescence pages
     */
    
    /** Obsolescence creation, consultation and modification page */
    public static final String OBSO_MANAGEMENT_PAGE =
            "/obso/ObsolescenceManagement";
    /** Obsolescence list page */
    public static final String OBSO_LIST_PAGE = "/obso/ObsolescenceList";
    /** Obsolescence search results page */
    public static final String OBSO_RESULT_PAGE = "/obso/ResultObso";
    
    /*
     * PC demands pages
     */
    
    /** Demand creation, consultation and modification page */
    public static final String DEMAND_MANAGEMENT_PAGE =
            "/demand/DemandManagement";
    /** Demand allocation page */
    public static final String DEMAND_ALLOCATION_PAGE =
            "/demand/DemandAllocation";
    
    /*
     * Search pages
     */
    
    /** Article advanced search results page */
    public static final String ARTICLE_SEARCH_RESULT_PAGE =
            "/search/ArticleResults";
    
    /** Article advanced search results page */
    public static final String SOFTWARE_SEARCH_RESULT_PAGE =
            "/search/SoftwareResults";

    /*
     * User pages
     */
    
    /** Users list page */
    public static final String USER_LIST_PAGE = "/user/ListUser";
    
    /*
     * Error pages
     */
    
    /** Users list page */
    public static final String SESSION_EXPIRED_PAGE =
            "/error/viewExpirationException";
    
    /*
     * Error component ids
     */
    public static final String USER_FORM_ERROR_ID = "userFormError";
    public static final String LIST_USER_ERROR_ID = "listUserError";
    public static final String LOGIN_FORM_ERROR_ID = "loginError";
    public static final String IMPORT_DOC_ERROR_ID = "importDocError";
    public static final String INST_MGMT_ERROR_ID =
            "installationManagementError";
    public static final String ART_MGMT_ERROR_ID = "articleManagementError";
    public static final String PC_MGMT_ERROR_ID = "PCManagementError";
    public static final String TOOL_MGMT_ERROR_ID = "toolManagementError";
    public static final String SOFT_MGMT_ERROR_ID = "softwareManagementError";
    public static final String OBSO_MGMT_ERROR_ID = "obsoManagementError";
    public static final String DEMAND_MGMT_ERROR_ID = "demandManagementError";
    public static final String OBSO_LIST_ERROR_ID = "obsoListError";
    public static final String SEARCH_OBSO_ERROR_ID = "searchObsoError";
    public static final String SEARCH_ART_ERROR_ID = "searchArticlesError";
    public static final String SEARCH_SOFT_ERROR_ID = "searchSoftwaresError";
    public static final String INSTALL_SOFT_ERROR_ID = "installSoftwareError";
    public static final String UPLOAD_SYSMON_ERROR_ID =
            "uploadSysmonFileModalError";
    public static final String CHOOSE_EQUIPMENT_ERROR_ID =
            "chooseEquipmentError";
    public static final String CHOOSE_COMPONENT_ERROR_ID =
            "chooseToolComponentError";
    public static final String GEN_PLATFORM_SHEET_ERROR_ID =
            "generatePlatformError";
    public static final String UPDATE_PLACE_ERROR_ID = "updatePlaceError";
    public static final String BUILDING_ERROR_ID = "buildingError";
    public static final String UPDATE_EXTERNAL_ENTITY_ERROR_ID =
            "updateExternalEntityError";
    public static final String EXTERNAL_ENTITY_ERROR_ID = "externalEntityError";
    public static final String PORT_MODAL_ERROR_ID = "portModalError";
    public static final String CHANGE_CONTAINER_ERROR_ID =
            "changeContainerError";
    public static final String CHANGE_LOCATION_ERROR_ID = "changeLocationError";
    public static final String CREATE_PN_ERROR_ID = "createPNError";
    public static final String DEL_ART_ERROR_ID = "deleteArticleError";
    public static final String DEL_INST_ERROR_ID = "deleteInstallationError";
    public static final String DEL_TOOL_ERROR_ID = "deleteToolError";
    public static final String DEL_MANUPN_ERROR_ID =
            "deleteManufacturerPNError";
    public static final String DEMAND_ALLOC_ERROR_ID = "demandAllocationError";
    public static final String DEMAND_LIST_ERROR_ID = "demandListError";
    public static final String DOC_LIST_ERROR_ID = "documentListError";
    public static final String DOC_UPLOAD_ERROR_ID = "uploadFileModalError";
    public static final String IMPOR_EXCEL_ERROR_ID = "importExcelError";
    public static final String LOGIN_ERROR_ID = "loginError";
    public static final String RECONNECT_ERROR_ID = "reconnectError";
    public static final String REMINDER_ERROR_ID = "reminderError";
    public static final String ADD_NEW_TYPE_ERROR_ID = "addNewTypeError";
    public static final String TYPE_LIST_ERROR_ID = "typeListError";
    public static final String UPDATE_TYPE_ERROR_ID = "updateTypeError";
    public static final String UPDATE_PN_ERROR_ID = "updatePNError";
    public static final String VALUE_LIST_ERROR_ID = "valueListError";
    public static final String VALUE_LIST_MODAL_ERROR_ID =
            "valueListModalError";
    public static final String UPDATE_VALUE_ERROR_ID = "updateValueError";
    public static final String SAVE_STOCK_SELECTION_ERROR_ID =
            "saveSelectionError";
    
    /*
     * Other ids
     */
    public static final String STOCK_EVOL_TAB_ID = "stockEvolution";
}
