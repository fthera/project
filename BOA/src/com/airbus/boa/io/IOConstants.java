/*
 * ------------------------------------------------------------------------
 * Class : IOConstants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

public class IOConstants {
    
    /*
     * SHEETS NAMES
     */
    
    public static final String BuildingSheetName = "Building";
    public static final String TypeSheetName = "Type";
    public static final String InstallationSheetName = "Installation";
    /** The tool sheet name */
    public static final String TOOL_SHEET_NAME = "Tool";
    public static final String CabinetSheetName = "Cabinet";
    public static final String RackSheetName = "Rack";
    public static final String SwitchSheetName = "Switch";
    public static final String BoardSheetName = "Board";
    public static final String VariousSheetName = "Various";
    public static final String UserSheetName = "User";
    public static final String HistorySheetName = "History";
    public static final String PNSheetName = "Part Number";
    public static final String PCSheetName = "PC";
    public static final String SoftwareSheetName = "Software";
    public static final String CommunicationPortSheetName =
            "Communication Ports";
    public static final String EquipmentSheetName = "Software Deployment";
    public static final String AttributeValueListSheetName = "Value Lists";
    public static final String ObsolescenceDataSheetName = "Obsolescence Data";
    public static final String InstalledSoftwareSheetName =
            "Installed Software";
    public static final String InstalledBoardSheetName = "Installed Boards";
    public static final String DatedCommentsSheetName = "Dated Comments";
    public static final String PCSpecificitiesSheetName = "PC Specificities";
    public static final String StockSheetName = "Stock";
    public static final String EXTERNAL_ENTITY_SHEET_NAME = "External Entity";
    public static final String ExplorerSheetName = "Explorer";
    /** The purchase sheet name */
    public static final String PURCHASE_SHEET_NAME = "Purchase";
    
    /*
     * COLUMNS TITLES
     */
    
    public static final String TYPE_TITLE = "Type";
    public static final String NAME_TITLE = "Name";
    public static final String DEPARTMENTINCHARGE_TITLE =
            "Department In Charge";
    public static final String PARENT_TITLE = "Parent";
    public static final String NUMID_TITLE = "Id";
    public static final String DESIGNATION_TITLE = "Designation";
    /** The inherited location column title */
    public static final String INHERITED_LOCATION_TITLE = "Inherited Location";
    /** The location name column title */
    public static final String LOCATION_TITLE = "Location";
    /** The external location name column title */
    public static final String EXTERNAL_LOCATION_NAME_TITLE =
            "External Location";
    public static final String FOR_ARTICLE_TITLE = "For Article";
    /** The manufacturer column title */
    public static final String MANUFACTURER_TITLE = "Manufacturer";
    public static final String AIRBUS_SN_TITLE = "Airbus SN";
    public static final String MANUFACTURER_SN_TITLE = "Manufacturer SN";
    public static final String AIRBUS_PN_TITLE = "Airbus PN";
    public static final String MANUFACTURER_PN_TITLE = "Manufacturer PN";
    public static final String CMSCODE_TITLE = "CmsCode";
    public static final String STATE_TITLE = "Functional State";
    public static final String USE_STATE_TITLE = "Use State";
    public static final String IDENTIFICATION_LETTER_OR_PRECISION_TITLE =
            "Identification Letter / Details";
    /** The details on location column title */
    public static final String LOCATION_DETAILS_TITLE = "Details on Location";
    public static final String RACK_POSITION_PRECISION_TITLE =
            "Rack Position / Details";
    public static final String GENERAL_COMMENT_TITLE = "General Comment";
    public static final String REV_H_TITLE = "Rev H";
    public static final String REV_S_TITLE = "Rev S";
    public static final String MREV_H_TITLE = "Rev H Manufacturer";
    public static final String MREV_S_TITLE = "Rev S Manufacturer";
    public static final String BOOT_LOADER_TITLE = "Boot Loader";
    public static final String CALIBRATION_TITLE = "Calibration";
    public static final String SLOTNUMBERPOSITION_OR_PRECISION_TITLE =
            "SlotNumberPosition / Details";
    public static final String ACQUISITIONDATE_TITLE = "Acquisition Date";
    /** The active stock control date column title */
    public static final String ACTIVE_STOCK_CONTROL_DATE_TITLE =
            "Active Stock Control Date";
    
    public static final String AIRCRAFTPROGRAM_TITLE = "Aircraft Program";
    public static final String STARTING_DATE_TITLE = "Starting Date";
    /** The business siglum column title */
    public static final String BUSINESS_SIGLUM_TITLE = "Business Siglum";
    
    public static final String CLASS_ARTICLE_TITLE = "Class";
    public static final String AUTHOR_TITLE = "Author";
    public static final String ACTION_TITLE = "Action";
    public static final String MODIFIED_FIELD_TITLE = "Modified Field";
    public static final String BEFORE_VALUE_TITLE = "Before Value";
    public static final String AFTER_VALUE_TITLE = "After Value";
    public static final String COMMENTS_TITLE = "Comments";
    
    // PC sheet
    public static final String FUNCTION_TITLE = "Function";
    /** The domain column title */
    public static final String DOMAIN_TITLE = "Domain";
    /** The platform column title */
    public static final String PLATFORM_TITLE = "Platform";
    /** The Product Type PC column title */
    public static final String PRODUCT_TYPE_TITLE = "Product Type";
    public static final String TECHNICAL_CONTACT_TITLE = "Technical Contact";
    public static final String PC_TECHNICAL_CONTACT_TITLE =
            "PC Technical Contact";
    public static final String INSTALLATION_USER = "Installation User";
    /** The Owner column title */
    public static final String OWNER_TITLE = "Owner";
    /** The Owner Siglum column title */
    public static final String OWNER_SIGLUM_TITLE = "Owner Siglum";
    public static final String ADMIN_TITLE = "Local administrator";
    /** The default OS column title */
    public static final String DEFAULT_OS_TITLE = "Default Operating System";
    public static final String NB_SCREENS_TITLE = "Number of screens";
    public static final String COMMENT_TITLE = "Comment";
    public static final String BUSINESS_ALLOC_TITLE = "Business allocation";
    public static final String BUSINESS_USAGE_TITLE = "Business usage";
    public static final String ASSIGNEMENT_TITLE = "Assignment";
    
    // feuille ports de communication
    public static final String IP_ADDRESS_TITLE = "IP Address";
    public static final String FIXED_IP_TITLE = "Fixed IP";
    public static final String NETWORK_MASK_TITLE = "Network Mask";
    public static final String MAC_ADDRESS_TITLE = "Mac Address";
    public static final String NETWORK_TITLE = "Network";
    public static final String SOCKET_TITLE = "Plug Number";
    public static final String COMMENT_PORT_TITLE = "Comment";
    
    // Feuille cartes installées sur PC
    public static final String BOARD_AIRBUS_SN_TITLE = "Board "
            + AIRBUS_SN_TITLE;
    public static final String SLOTNUMBERPOSITION_TITLE = "SlotNumber";
    
    // Feuille SWITCH
    public static final String SWITCH_POSITION_PRECISION_TITLE =
            "Switch position / Details";
    
    /*
     * Software sheet
     */
    
    public static final String DISTRIBUTION_TITLE = "Distribution / Version";
    public static final String KERNEL_TITLE = "Kernel";
    /** The operating system column title */
    public static final String OPERATINGSYSTEM_TITLE = "Operating System";
    public static final String DESCRIPTION_TITLE = "Description";
    public static final String LICENCE_TITLE = "Licence number";
    public static final String SOFTWARE_TITLE = "Software";
    
    // feuille User
    public static final String LOGIN_TITLE = "Login";
    public static final String LASTNAME_TITLE = "Lastname";
    public static final String FIRSTNAME_TITLE = "Firstname";
    public static final String EMAIL_TITLE = "Email";
    public static final String ROLE_TITLE = "Role";
    public static final String PASSWORD_TITLE = "Password";
    public static final String DATE_TITLE = "Date";
    
    // Stock
    public static final String PN_TITLE = "Part number";
    public static final String QTYSTOCK_TITLE = "Quantity in stock";
    public static final String QTYUSE_TITLE = "Quantity in use";
    public static final String SPARE_TITLE = "% spare";
    
    // Tool sheet
    
    /** The loan date column title */
    public static final String LOAN_DATE_TITLE = "Loan date";
    /** The loan due date column title */
    public static final String LOAN_DUE_DATE_TITLE = "Loan due date";
    
    // Purchase sheets
    
    /** The issuer column title */
    public static final String ISSUER_TITLE = "Issuer";
    /** The program column title */
    public static final String PROGRAM_TITLE = "Program";
    /** The project column title */
    public static final String PROJECT_TITLE = "Project";
    /** The budget column title */
    public static final String BUDGET_TITLE = "Budget";
    /** The need date column title */
    public static final String NEED_DATE_TITLE = "Need Date";
    /** The demand number column title */
    public static final String DEMAND_NUMBER_TITLE = "Demand N°";
    /** The features column title */
    public static final String FEATURES_TITLE = "Features";
    /** The additional information column title */
    public static final String ADDITIONAL_INFORMATION_TITLE =
            "Additional information";
    /** The justification column title */
    public static final String JUSTIFICATION_TITLE = "Justification";
    /** The operating system column title */
    public static final String OPERATING_SYSTEM_TITLE = "Operating System";
    /** The building column title */
    public static final String BUILDING_TITLE = "Building";
    /** The room column title */
    public static final String ROOM_TITLE = "Room";
    /** The plug number column title */
    public static final String PLUG_NUMBER_TITLE = "Plug number";
    /** The department column title */
    public static final String DEPT_TITLE = "Dept";
    /** The New or Replace column title */
    public static final String NEW_REPLACE_TITLE = "New / Replace";
    /** The Asset Number to replace column title */
    public static final String ASSET_NUMBER_TO_REPLACE =
            "Asset Number to Replace";
    /** The hardware column title */
    public static final String HARDWARE_TITLE = "Hardware";
    /** The OS column title */
    public static final String OS_TITLE = "OS";
    /** The OTP column title */
    public static final String OTP_TITLE = "OTP";
    /** The socket number column title */
    public static final String SOCKET_NB_TITLE = "Socket Nb";
    /** The price column title */
    public static final String PRICE_TITLE = "Price";
    
    // Other labels
    
    /** The Export date title */
    public static final String EXPORT_DATE = "Export date";
    /** The user title */
    public static final String USER_TITLE = "User";
    /** The contact title */
    public static final String CONTACT_TITLE = "Contact";
    
    /*
     * REGEX FOR CONVERTING IMPORTED VALUES
     */
    
    /** Regular expression determining a Building */
    public static final String REGEX_MIGRATION_BUILDING =
            "(?i)BUILDING|BATIMENT";
    /** Regular expression determining a Laboratory */
    public static final String REGEX_MIGRATION_BUILDING_LABO =
            "(?i)LABO|LABORATORY|LABORATOIRE";
    /** Regular expression determining a Storeroom */
    public static final String REGEX_MIGRATION_STOREROOM =
            "(?i)R[éE]SERVE|STOREROOM";
    /** Regular expression determining a Room */
    public static final String REGEX_MIGRATION_ROOM = "(?i)ROOM|SALLE";
    /** Regular expression determining a Place */
    public static final String REGEX_MIGRATION_PLACE =
            "(?i)R[éE]SERVE.*|STOREROOM|LABORATORY|LABORATOIRE|LABO.*|PLACE|EMPLACEMENT|SALLE|ROOM";
    /** Regular expression determining a Rack */
    public static final String REGEX_MIGRATION_RACK = "(?i)RACK";
    /** Regular expression determining a Cabinet */
    public static final String REGEX_MIGRATION_CABINET = "(?i)CABINET|BAIE.*";
    /** Regular expression determining a Board */
    public static final String REGEX_MIGRATION_BOARD = "(?i)BOARD|CARTE.*";
    /** Regular expression determining an Installation */
    public static final String REGEX_MIGRATION_INSTALLATION =
            "(?i)INSTALLATION|SIMU.*";
    /** Regular expression determining a Tool */
    public static final String REGEX_MIGRATION_TOOL = "(?i)TOOL|OUTIL";
    
    public static final String[] TRUE_STRINGS = new String[] { "vrai",
            "enable", "true", "yes", "ok", "oui" };
    public static final String[] FALSE_STRINGS = new String[] { "faux",
            "disable", "false", "no", "ko", "non" };
    
    /*
     * IMPORT / EXPORT MIGRATION MESSAGES
     */
    
    private static final String migration_prefix = "com.airbus.boa.io.";
    
    // Information messages
    public static final String IMPORT_SUCCESSFUL =
            migration_prefix + "IMPORT_SUCCESSFUL";
    public static final String TEMPLATE_LEGEND =
            migration_prefix + "TEMPLATE_LEGEND";
    public static final String TEMPLATE_SN_COMMENT =
            migration_prefix + "TEMPLATE_SN_COMMENT";
    public static final String TEMPLATE_PN_COMMENT =
            migration_prefix + "TEMPLATE_PN_COMMENT";
    public static final String TEMPLATE_LOC_COMMENT =
            migration_prefix + "TEMPLATE_LOC_COMMENT";
    public static final String TEMPLATE_TC_COMMENT =
            migration_prefix + "TEMPLATE_TC_COMMENT";
    
    // IHM tooltips
    public static final String TOOLTIP_INST_NAME = "toolTipInstallationName";
    public static final String TOOLTIP_INST_COMMENT =
            "toolTipInstallationComment";
    public static final String TOOLTIP_INST_BUSINESS_SIGLUM =
            "toolTipInstallationBusinessSiglum";
    public static final String TOOLTIP_INST_PROGRAM =
            "toolTipInstallationProgram";
    public static final String TOOLTIP_INST_INCHARGE =
            "toolTipInstallationInCharge";
    public static final String TOOLTIP_INST_USER = "toolTipInstallationUser";
    
    public static final String TOOLTIP_PC_ASN = "toolTipPCAirbusSN";
    public static final String TOOLTIP_PC_DEPARTMENTINCHARGE =
            "toolTipPCDepartmentInCharge";
    public static final String TOOLTIP_PC_NAME = "toolTipPCName";
    public static final String TOOLTIP_PC_FUNCTION = "toolTipPCFunction";
    public static final String TOOLTIP_PC_INCHARGE = "toolTipPCInCharge";
    public static final String TOOLTIP_PC_OWNER = "toolTipPCOwner";
    public static final String TOOLTIP_PC_OWNERSIGLUM = "toolTipPCOwnerSiglum";
    public static final String TOOLTIP_PC_PLATFORM = "toolTipPCPlatform";
    public static final String TOOLTIP_SOFTWARE_NAME = "softwareNameToolTip";
    public static final String TOOLTIP_SOFTWARE_DISTRIBUTION =
            "softwareDistributionToolTip";
    public static final String TOOLTIP_SOFTWARE_KERNEL =
            "softwareKernelToolTip";
    
    // les titres de colonnes et/ou leur ordre sont erronés pour la feuille
    // {feuillename}
    public static final String INVALID_HEADER = migration_prefix
            + "INVALID_HEADER";
    
    public static final String EXPORT_ERROR = migration_prefix + "EXPORT_ERROR";
    public static final String NOT_IMPORT_FILE = migration_prefix
            + "NOT_IMPORT_FILE_ERROR";
    public static final String ROW_EMPTY_FIELD = migration_prefix
            + "ROW_EMPTY_FIELD";
    public static final String ROW_INCORRECT_FIELD = migration_prefix
            + "ROW_INCORRECT_FIELD"; // rownumber , field
    public static final String SHEET_NO_SHEETNAME = migration_prefix
            + "NO_SHEETNAME";
    public static final String ENTITY_NOT_FOUND = migration_prefix
            + "ENTITY_NOT_FOUND";
    /** When the location is impossible */
    public static final String ROW_IMPOSSIBLE_LOCATION = migration_prefix
            + "ROW_IMPOSSIBLE_LOCATION";
    /** When the container is impossible */
    public static final String ROW_IMPOSSIBLE_CONTAINER = migration_prefix
            + "ROW_IMPOSSIBLE_CONTAINER";
    
    // numero de ligne , nom Exception , message de l'exception
    public static final String ROW_REJECTED = migration_prefix + "ROW_REJECTED"; // 3
                                                                                 // attributs
    
    public static final String SLOT_FACE_FORMAT_INCORRECT = migration_prefix
            + "SLOT_FACE_FORMAT_INCORRECT";
    
    public static final String REGEX_VENUSIA = "(?i).*VENUSIA.*";
    /** The container type column title */
    public static final String CONTAINER_TYPE_TITLE = "Parent type";
    /** The container name column title */
    public static final String CONTAINER_TITLE = "Parent";
    /** The details on container */
    public static final String CONTAINER_DETAILS_TITLE = "Details on parent";
    /** The master container name column title */
    public static final String MASTER_CONTAINER_TITLE = "Master parent";
    /** The master container type column title */
    public static final String MASTER_CONTAINER_TYPE_TITLE =
            "Master parent Type";
    /**
     * The master container business siglum column title (only for
     * installations)
     */
    public static final String CONTAINER_BUSINESS_SIGLUM = "Business Siglum";
    /**
     * The master container business siglum column title (only for
     * installations)
     */
    public static final String CONTAINER_TECHNICAL_CONTACT =
            "Installation Technical Contact";
    
    // MESSAGE D'ERREUR IMPORT
    public static final String AIRBUSPN_TYPEARTICLE_RELATION_INCORRECT =
            migration_prefix + "AIRBUSPN_TYPEARTICLE_RELATION_INCORRECT";
    public static final String AIRBUSPN_CREATION_ERROR = migration_prefix
            + "AIRBUSPN_CREATION_ERROR";
    public static final String MANUFACTURERPN_CREATION_ERROR = migration_prefix
            + "MANUFACTURERPN_CREATION_ERROR";
    public static final String AIRBUSSN_ALREADY_USED = migration_prefix
            + "AIRBUSSN_ALREADY_USED";
    
    /*
     * Message pour les champs invalides
     * nom du champ
     * valeur du champ
     */
    public static final String FIELD_INVALID = migration_prefix
            + "FIELD_INVALID";
    public static final String OBSO_FOR_ITEM_TITLE = "Reference Family";
    public static final String OBSO_LAST_UPDATE_TITLE = "Last Update";
    public static final String OBSO_CONSULT_PERIOD_TITLE = "Consult Period";
    public static final String OBSO_END_OF_ORDER_TITLE = "End of Order";
    public static final String OBSO_END_OF_SUPPORT_TITLE = "End of Support";
    public static final String OBSO_MANUFACTURER_STATUS_TITLE =
            "Manufacturer Status";
    public static final String OBSO_AIRBUS_STATUS_TITLE = "AIRBUS Status";
    public static final String OBSO_CURRENT_ACTION_TITLE = "Current Action";
    public static final String OBSO_STRATEGY_TITLE = "Strategy";
    public static final String OBSO_MTBF_TITLE = "MTBF";
    public static final String OBSO_IN_CHARGE_OF_TITLE = "In charge of";
    public static final String OBSO_END_OF_PRODUCTION_TITLE =
            "End of Production";
    public static final String OBSO_MANUFACTURER_TITLE = "Manufacturer";
    public static final String OBSO_CONTINUITY_DATE_TITLE =
            "Continuity Date (iidefix)";
    public static final String OBSO_DATE_TITLE = "Obsolescence Date";
    public static final String OBSO_TYPE_ARTICLE_TITLE = "Article Type";
    public static final String OBSO_SUPPLIER_TITLE = "Supplier";
    public static final String OBSO_NEXT_CONSULTING_DATE_TITLE =
            "Next consulting Date";
    public static final String OBSO_QUANTITY_TOTAL_TITLE = "Total quantity";
    public static final String OBSO_QUANTITY_STOCK_TITLE = "Stock quantity";
    public static final String OBSO_QUANTITY_USE_TITLE = "In Use quantity";
    public static final String OBSO_CONSTITUANT_NAME_TITLE = "Constituant Name";
    
    public static final String DESIGNATION_OR_FUNCTION_TITLE =
            "Designation / Function";
    public static final String INSTALLED_SOFTWARE_TITLE = "Installed Softwares";
    /** Location or Details on location or on container */
    public static final String LOCATION_OR_DETAILS_TITLE = "Location / Details";
    
    public static final String REFERENCE_MANDATORY_FOR_PN_AND_SOFTWARE =
            migration_prefix + "REFERENCE_MANDATORY_FOR_PN_AND_SOFTWARE";
    public static final String TYPEPC_NOT_FOUND = migration_prefix
            + "TYPEPC_NOT_FOUND";
    /** Error message when Article Type does not exist */
    public static final String TYPEARTICLE_NOT_FOUND = migration_prefix
            + "TYPEARTICLE_NOT_FOUND";
    public static final String SOFTWARE_TO_MANY_MATCH_COMPLETENAME =
            migration_prefix + "SOFTWARE_TO_MANY_MATCH_COMPLETENAME";
    public static final String SOFTWARE_NOT_FOUND = migration_prefix
            + "SOFTWARE_NOT_FOUND";
    /** Error message when Software for Default OS is not an Operating System */
    public static final String SOFTWARE_DEFAULT_NOT_OS = migration_prefix
            + "SOFTWARE_DEFAULT_NOT_OS";
    public static final String IMPORT_ERROR_READ_LINE = migration_prefix
            + "IMPORT_ERROR_READ_LINE";
    public static final String PN_TYPE_NOT_IN_RELATION = migration_prefix
            + "PN_TYPE_NOT_IN_RELATION";
    public static final String ASN_OR_MSN_MUST_BE_FILLED = migration_prefix
            + "ASN_OR_MSN_MUST_BE_FILLED";
    public static final String BOARD_OR_PC_NOT_FOUND = migration_prefix
            + "BOARD_OR_PC_NOT_FOUND";
    public static final String MORE_THAN_ONE_ARTICLE_FOUND = migration_prefix
            + "MORE_THAN_ONE_ARTICLE_FOUND";
    public static final String PORT_ONLY_FOR_PC_AND_BOARD = migration_prefix
            + "PORT_ONLY_FOR_PC_AND_BOARD";
    public static final String PC_PORT_AT_LEAST_ONE_FIELD_3 = migration_prefix
            + "PC_PORT_AT_LEAST_ONE_FIELD_3";
    public static final String PORT_ONLY_FOR_ETHERNET_BOARD = migration_prefix
            + "PORT_ONLY_FOR_ETHERNET_BOARD";
    public static final String ARTICLE_ALREADY_EXISTS = migration_prefix
            + "ARTICLE_ALREADY_EXISTS";
    public static final String TYPEARTICLE_ALREADY_EXISTS = migration_prefix
            + "TYPEARTICLE_ALREADY_EXISTS";
    public static final String SOFTWARE_ALREADY_DEPLOYED_TO_EQUIPMENT =
            migration_prefix + "SOFTWARE_ALREADY_DEPLOYED_TO_EQUIPMENT";
    
}
