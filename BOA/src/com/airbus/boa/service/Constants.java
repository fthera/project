/*
 * ------------------------------------------------------------------------
 * Class : Constants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.service;

/**
 * Constants definition
 */
public class Constants {
    
    private static final String err_prefix = "BOA.ERROR.";
    private static final String warn_prefix = "BOA.WARN.";
    private static final String info_prefix = "BOA.INFO.";
    
    public static String REGEX_BOOTLOADER_TYPE =
            "(?i).*VENUSIA.*|.*RIO.*|ALCOR|.?DIONET.*";
    /**
     * Regular expression for Boards having communication ports (Ethernet and
     * RIO or XDIO boards)
     */
    public static String REGEX_ETHERNETBOARD_TYPE =
            "(?i).*RIO.*|.*ETH.*|.*XDIO.*";
    public static String REGEX_CALIBRATION_TYPE = "(?i).*VENUSIA.*";
    
    public static String REGEX_SLOTNUMBER_AND_POSITION =
            "(?i)\\d+(-\\d+)?\\s?(av|f|avant|front|ar|r|arri[eè]re|rear)?";
    public static final String REGEX_RACK_POSITION =
            "[a-z]|[FRfr][a-z]|[A-Z][frFR][a-z]|Z[PEC28][a-z]";
    
    public static final String REGEX_USER_EMAIL = ".+@.+\\.[a-z]+";
    
    // Nom du fichier de ressource
    public static final String BUNDLE_NAME =
            "com.airbus.boa.view.MessageRessources";
    
    /*
     * Class attributes names used by histories
     */
    
    /** When a modification of location is performed */
    public static final String Location = "Location";
    /** When a modification of container is performed */
    public static final String Container = "Parent";
    public static final String IPAddress = "IPAddress";
    public static final String FixedIP = "FixedIP";
    public static final String MACAddress = "MACAddress";
    public static final String Mask = "NetworkMask";
    
    public static final String Name = "name";
    public static final String Comment = "comment";
    public static final String Socket = "socket";
    public static final String Network = "network";
    public static final String AddPort = "AddPort";
    public static final String RemovePort = "RemovePort";
    public static final String ModifyPort = "ModifyPort";
    public static final String ComPortNameToSkip = "_tmp";
    
    public static final String Description = "description";
    public static final String Contact = "contact";
    public static final String AddPCSpecificity = "AddPCSpecificity";
    public static final String ModifyPCSpecificity = "ModifyPCSpecificity";
    public static final String RemovePCSpecificity = "RemovePCSpecificity";
    
    public static final String AddDatedComment = "AddDatedComment";
    public static final String ModifyDatedComment = "ModifyDatedComment";
    public static final String RemoveDatedComment = "RemoveDatedComment";
    
    // attributs pour l'export et l'import de PN
    public static final String CodeBarrePN = "codeBarrePN";
    public static final String RevisionH = "RevH";
    public static final String RevisionS = "RevS";
    public static final String IdentifierPN = "Identifier";
    
    // demands attributes
    /** The demand number */
    public static final String DEMAND_NUMBER = "demandNumber";
    /** The demand status */
    public static final String DEMAND_STATUS = "demandStatus";
    
    // ACTIONS
    /** The history modification line label */
    public static final String MODIFICATION = "Modification";
    /** The history creation line label */
    public static final String CREATION = "Creation";
    /** The history creation line comment for interface creation */
    public static final String CREATION_BY_INTERFACE = "CreationByInterface";
    /** The history creation line comment for import creation */
    public static final String CREATION_BY_IMPORT = "CreationByImport";
    /** Message in History for creation from a demand */
    public static final String CREATION_FROM_DEMAND = "Created from Demand";
    
    // Types d'interface r�seau
    public static final String Internal = "internal";
    public static final String External = "external";
    
    // erreurs pour la classe User
    public static final String ALREADY_CREATED_USER = err_prefix
            + "ALREADY_CREATED_USER";
    public static final String INVALID_LOGIN = err_prefix + "INVALID_LOGIN";
    public static final String REMOVE_ROLES = err_prefix + "REMOVE_ROLES";
    /**
     * Error message when the user cannot be deleted because it is the issuer of
     * at least one demand
     */
    public static final String USER_DELETION_DEMAND_ISSUER = err_prefix
            + "USER_DELETION_DEMAND_ISSUER";
    
    // erreurs pour la classe LocationBean
    public static final String BUILDING_HAS_PLACES = err_prefix
            + "BUILDING_HAS_PLACES";
    public static final String PLACE_ERROR = err_prefix + "PLACE_ERROR";
    public static final String BUILDING_IS_NULL = err_prefix
            + "BUILDING_IS_NULL";
    public static final String PLACE_IS_NULL = err_prefix + "PLACE_IS_NULL";
    public static final String PLACE_HAS_ARTICLES = err_prefix
            + "PLACE_HAS_ARTICLES";
    public static final String PLACE_HAS_TOOLS = err_prefix + "PLACE_HAS_TOOLS";
    public static final String LABO_HAS_TESTBENCHES = err_prefix
            + "LABO_HAS_TESTBENCHES";
    public static final String BUILDING_MISTMATCH = err_prefix
            + "BUILDING_MISTMATCH";
    
    public static final String INVALID_BULDING_NAME = err_prefix
            + "INVALID_BUILDING_NAME";
    public static final String ALREADY_EXISTED_BUILDING = err_prefix
            + "ALREADY_EXISTED_BUILDING";
    
    // name , article's number
    public static final String EXTERNAL_ENTITY_HAS_ARTICLES = err_prefix
            + "EXTERNAL_ENTITY_HAS_ARTICLES";
    public static final String EXTERNAL_ENTITY_HAS_TOOLS = err_prefix
            + "EXTERNAL_ENTITY_HAS_TOOLS";
    
    public static final String PLACE_MUST_BE_LABO_OR_ROOM = err_prefix
            + "PLACE_MUST_BE_LABO_OR_ROOM";
    
    // erreurs pour la classe Board
    public static final String PN_OR_CMSCODE_MUST_BE_FILLED = err_prefix
            + "PN_OR_CMSCODE_MUST_BE_FILLED";
    public static final String A_SN_MUST_BE_FILLED = err_prefix
            + "A_SN_MUST_BE_FILLED";
    
    public static final String SLOTNUMBER_AND_POSITION_INVALID = err_prefix
            + "SLOTNUMBER_AND_POSITION_INVALID";
    
    // erreurs pour la classe Rack
    public static final String RACK_POSITION_INVALID = err_prefix
            + "RACK_POSITION_INVALID";
    
    /** When the identification letter is not valid */
    public static final String IDENTIFICATION_LETTER_INVALID = err_prefix
            + "IDENTIFICATION_LETTER_INVALID";
    
    // erreurs pour la classe PN
    
    // Message de validation
    public static final String SN_ALREADY_USED = err_prefix + "SN_ALREADY_USED";
    public static final String PN_ALREADY_USED = err_prefix + "PN_ALREADY_USED";
    /** Key for error message if autogenerated Airbus SN needs update */
    public static final String AUTOGENERATED_SN_NEED_MODIFICATION = err_prefix
            + "AUTOGENERATED_SN_NEED_MODIFICATION";
    
    // erreurs pour la classe article
    public static final String PNAIRBUS1_CMSCODE_ONE_FIELD_MUST_BE_FILLED =
            err_prefix + "PNAIRBUS_CMSCODE_ONE_FIELD_MUST_BE_FILLED";
    
    // {0} nom de la propri�t�
    public static final String FIELD_MUST_NOT_BE_EMPTY = err_prefix
            + "FIELD_MUST_NOT_BE_EMPTY";
    public static final String INCORRECT_HEADER = err_prefix
            + "INCORRECT_HEADER";
    
    // erreurs pour la classe User
    public static final String INVALID_PASSWORD = err_prefix
            + "INVALID_PASSWORD";
    public static final String LOGIN_PASSWORD_NOT_MATCH = err_prefix
            + "LOGIN_PASSWORD_NOT_MATCH";
    
    public static final String UNCOMPUTABLE = err_prefix + "UNCOMPUTABLE";
    /** When the element is not located */
    public static final String NOT_LOCATED = err_prefix + "NOT_LOCATED";
    /** When the element is not contained */
    public static final String NOT_CONTAINED = err_prefix + "NOT_CONTAINED";
    /** When the location is not found */
    public static final String LOCATION_NOT_FOUND = err_prefix
            + "LOCATION_NOT_FOUND";
    /** When the external location is not found */
    public static final String EXTERNAL_LOCATION_NOT_FOUND = err_prefix
            + "EXTERNAL_LOCATION_NOT_FOUND";
    /** When the container is not found */
    public static final String CONTAINER_NOT_FOUND = err_prefix
            + "CONTAINER_NOT_FOUND";
    /** When an unknown error occurs on localization */
    public static final String LOCALIZATION_UNKNOWN_ERROR = err_prefix
            + "LOCALIZATION_UNKNOWN_ERROR";
    
    public static final String NON_UNIQUE_RESULT = err_prefix
            + "NON_UNIQUE_RESULT";
    
    /** Error message when the designation is not field */
    public static final String DESIGNATION_MANDATORY = err_prefix
            + "DESIGNATION_MANDATORY";
    public static final String TYPE_MANDATORY = err_prefix + "TYPE_MANDATORY";
    public static final String USER_PC_INCHARGE = err_prefix
            + "USER_PC_INCHARGE";
    /**
     * Error message when the user is in charge of an installation and cannot be
     * deleted
     */
    public static final String USER_INSTALLATION_INCHARGE = err_prefix
            + "USER_INSTALLATION_INCHARGE";
    
    /** Error message when the location is not provided */
    public static final String LOCATION_MANDATORY = err_prefix
            + "LOCATION_MANDATORY";
    /** Error message when the location is not valid */
    public static final String LOCATION_NOT_VALID = err_prefix
            + "LOCATION_NOT_VALID";
    
    /** Error message when no OS is provided for a PC demand */
    public static final String OS_MANDATORY = err_prefix + "OS_MANDATORY";
    
    /** Error message when the container is not valid */
    public static final String CONTAINER_NOT_VALID = err_prefix
            + "CONTAINER_NOT_VALID";
    
    public static final String AN_INSTALLATION_NAME_MUST_BE_FILLED = err_prefix
            + "AN_INSTALLATION_NAME_MUST_BE_FILLED";
    
    public static final String INSTALLATION_NAME_ALREADY_USED = err_prefix
            + "INSTALLATION_NAME_ALREADY_USED";
    public static final String CONFIRM_PASSWORD = err_prefix
            + "CONFIRM_PASSWORD";
    public static final String VALIDATE_EMAIL = err_prefix + "VALIDATION_EMAIL";
    
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String SUCCESSFUL_DELETE = "BOA.SUCCESSFULL_DELETE";
    public static final String SN_UNKNOWN = err_prefix + "SN_UNKNOWN";
    public static final String PN_UNKNOWN = err_prefix + "PN_UNKNOWN";
    public static final String INSTALLATION_UNKNOWN = err_prefix
            + "INSTALLATION_UNKNOWN";
    public static final String ALREADY_EXISTED_PLACE = err_prefix
            + "ALREADY_EXISTED_PLACE";
    /**
     * Error message when the installation cannot be deleted because it contains
     * at least one demand
     */
    public static final String INSTALLATION_DELETION_CONTAINS_DEMAND =
            err_prefix + "INSTALLATION_DELETION_CONTAINS_DEMAND";
    /**
     * Error message when the article cannot be deleted because it contains at
     * least one demand
     */
    public static final String ARTICLE_DELETION_CONTAINS_DEMAND = err_prefix
            + "ARTICLE_DELETION_CONTAINS_DEMAND";
    /**
     * Error message when the place cannot be deleted because it contains at
     * least one demand
     */
    public static final String PLACE_DELETION_CONTAINS_DEMAND = err_prefix
            + "PLACE_DELETION_CONTAINS_DEMAND";
    /**
     * Error message when the place cannot be deleted because it contains one or
     * more located items
     */
    public static final String PLACE_DELETION_CONTAINS_ITEMS = err_prefix
            + "PLACE_DELETION_CONTAINS_ITEMS";
    /**
     * Error message when the external entity cannot be deleted because it
     * contains at least one demand
     */
    public static final String EXTERNAL_ENTITY_DELETION_CONTAINS_DEMAND =
            err_prefix + "EXTERNAL_ENTITY_DELETION_CONTAINS_DEMAND";
    /**
     * Error message when the external entity cannot be deleted because it
     * contains one or more located items
     */
    public static final String EXTERNAL_ENTITY_DELETION_CONTAINS_ITEMS =
            err_prefix + "EXTERNAL_ENTITY_DELETION_CONTAINS_ITEMS";
    
    public static final String QUERY_NO_RESULTS = err_prefix
            + "QUERY_NO_RESULTS";
    // argument : nombre d'article trouv�
    public static final String QUERY_NUMBER_RESULTS = info_prefix
            + "QUERY_NUMBER_RESULTS";
    
    public static final String QUERY_SOFTWARE_NO_RESULTS = err_prefix
            + "QUERY_SOFTWARE_NO_RESULTS";
    public static final String QUERY_SOFTWARE_NUMBER_RESULTS = info_prefix
            + "QUERY_SOFTWARE_NUMBER_RESULTS";
    
    public static final String QUERY_OBSO_NO_RESULTS = err_prefix
            + "QUERY_OBSO_NO_RESULTS";
    public static final String QUERY_OBSO_NUMBER_RESULTS = info_prefix
            + "QUERY_OBSO_NUMBER_RESULTS";
    
    public static final String DISPLAY_RESULTS = "displayResultsTitle";
    public static final String INSTALLATION_NAME_MUST_BE_FILLED = err_prefix
            + "INSTALLATION_NAME_MUST_BE_FILLED";
    
    // FENETRE DE CREATION DES TYPES
    public static final String TYPELABEL_INVALID = err_prefix
            + "TYPELABEL_INVALID";
    public static final String TYPELABEL_ALREADY_USED = err_prefix
            + "TYPELABEL_ALREADY_USED";
    
    public static final String PLACENAME_ALREADY_USED = err_prefix
            + "PLACENAME_ALREADY_USED";
    public static final String DESIGNATION_ALREADY_USED = err_prefix
            + "DESIGNATION_ALREADY_USED";
    public static final String DESIGNATION_INVALID = err_prefix
            + "DESIGNATION_INVALID";
    
    public static final String EXTERNALENTITYNAME_ALREADY_USED = err_prefix
            + "EXTERNALENTITYNAME_ALREADY_USED";
    
    // MODIFIABLE VALUE LISTS
    
    /** When the value is not deletable in the list */
    public static final String UNDELETABLE_VALUE_LIST = err_prefix
            + "UNDELETABLE_VALUE_LIST";
    /** When the value is not modifiable in the list */
    public static final String UNMODIFIABLE_VALUE_LIST = err_prefix
            + "UNMODIFIABLE_VALUE_LIST";
    
    public static final String VALUE_LIST_MANDATORY_FIELDS = err_prefix
            + "VALUE_LIST_MANDATORY_FIELDS";
    
    public static final String ALREADY_EXISTED_VALUE_LIST = err_prefix
            + "ALREADY_EXISTED_VALUE_LIST";
    
    /**
     * Error message when the product type PC cannot be deleted because it is
     * used by at least one demand
     */
    public static final String PRODUCTTYPEPC_DELETION_USED_DEMAND = err_prefix
            + "PRODUCTTYPEPC_DELETION_USED_DEMAND";
    
    // OBSOLESCENCE
    public static final String OBSO_MULTI_REFERENCE_CREATION_ERROR = err_prefix
            + "OBSO_MULTI_REFERENCE_CREATION_ERROR";
    
    // PC
    public static final String PC_MANDATORY_FIELD_EMPTY = err_prefix
            + "PC_MANDATORY_FIELD_EMPTY";
    /** Error message when the PC name already exists */
    public static final String PC_NAME_ALREADY_EXISTING = err_prefix
            + "PC_NAME_ALREADY_EXISTING";
    
    public static final String PC_DELETION_ERROR = err_prefix
            + "PC_DELETION_ERROR";
    /**
     * Error message when the PC cannot be deleted because it is used by a
     * demand
     */
    public static final String PC_DELETION_USED_BY_DEMAND = err_prefix
            + "PC_DELETION_USED_BY_DEMAND";
    
    public static final String PC_PORT_AT_LEAST_ONE_FIELD = err_prefix
            + "PC_PORT_AT_LEAST_ONE_FIELD";
    
    public static final String PC_PORT_ALREADY_EXISTS = err_prefix
            + "PC_PORT_ALREADY_EXISTS";
    
    public static final String OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE =
            err_prefix + "OBSO_REFERENCE_ALREADY_MANAGED_INTO_OBSOLESCENCE";
    
    /**
     * Error message when articles with the chosen Airbus PN are already
     * obsolescence managed with their manufacturer PN
     */
    public static final String OBSO_ALREADY_AIRBUSPN_MANAGED_CONFLICT_ERROR =
            err_prefix + "OBSO_ALREADY_AIRBUSPN_MANAGED_CONFLICT_ERROR";
    /**
     * Error message when articles with the chosen manufacturer PN are already
     * obsolescence managed with their Airbus PN
     */
    public static final String OBSO_ALREADY_MANUFACTURERPN_MANAGED_CONFLICT_ERROR =
            err_prefix + "OBSO_ALREADY_MANUFACTURERPN_MANAGED_CONFLICT_ERROR";
    
    public static final String OBSO_INVALID_REFERENCE = err_prefix
            + "OBSO_INVALID_REFERENCE";
    
    public static final String OBSO_CONSTITUANT_ERROR = err_prefix
            + "OBSO_CONSTITUANT_ERROR";
    
    // message de warning signalant que le nombre maximal de resultats pour
    // requete dans une modale est atteint
    // seul les QUERY_MAX_RESULTS premiers r�sultats seront retourn�s.
    // 2 parametres {0}= nombreDeResultatsTrouv�s ; {1}=limite d�finie }
    public static final String QUERY_MAX_MODAL_EXCEEDED = warn_prefix
            + "QUERY_MAX_MODAL_EXCEEDED";
    public static final String QUERY_CRITERIA_MUST_BE_ENTERED = err_prefix
            + "QUERY_CRITERIA_MUST_BE_ENTERED";
    
    // Validation messages for PN and TYPES
    
    /** Error message when PN is invalid */
    public static final String PN_IDENTIFIER_INVALID = err_prefix
            + "PN_IDENTIFIER_INVALID";
    /** Error message when Airbus PN is invalid */
    public static final String AIRBUS_PN_IDENTIFIER_INVALID = err_prefix
            + "AIRBUS_PN_IDENTIFIER_INVALID";
    /** Error message when Airbus PN does not exist */
    public static final String AIRBUS_PN_NOT_FOUND = err_prefix
            + "AIRBUS_PN_NOT_FOUND";
    /** Error message when Manufacturer PN does not exist */
    public static final String MANUFACTURER_PN_NOT_FOUND = err_prefix
            + "MANUFACTURER_PN_NOT_FOUND";
    /** Error message when PC type does not exist */
    public static final String PC_TYPE_NOT_FOUND = err_prefix
            + "PC_TYPE_NOT_FOUND";
    public static final String PN_DELETION_NONE_SELECTED = warn_prefix
            + "PN_DELETION_NONE_SELECTED";
    
    /** Prefix of the Airbus SN for a PC created from a demand */
    public static final String PC_CREATED_FROM_DEMAND_AIRBUS_SN_PREFIX =
            "NON NUM DEMAND ";
    
    /** Prefix of the name for a PC created from a demand */
    public static final String PC_CREATED_FROM_DEMAND_NAME_PREFIX =
            "PC for Demand ";
    
    /** Key for error message if autogenerated Name needs update */
    public static final String AUTOGENERATED_NAME_NEED_MODIFICATION =
            err_prefix + "AUTOGENERATED_NAME_NEED_MODIFICATION";
    
    // un parametre le PN a supprimer
    public static final String PN_DELETION_FORBIDDEN_1 = err_prefix
            + "PN_DELETION_FORBIDDEN_1";
    public static final String PN_DELETION_DEFINED_FOR_MANY_ARTICLES =
            err_prefix + "PN_DELETION_DEFINED_FOR_MANY_ARTICLES";
    public static final String PN_DELETION_DEFINED_FOR_ARTICLE = err_prefix
            + "PN_DELETION_DEFINED_FOR_ARTICLE";
    public static final String TYPEARTICLE_DESTINATION_INVALID = err_prefix
            + "TYPEARTICLE_DESTINATION_INVALID";
    public static final String TYPEARTICLE_DELETION_ERROR_AIRBUSPNS_2 =
            err_prefix + "TYPEARTICLE_DELETION_ERROR_AIRBUSPNS_2";
    
    public static final String TYPEARTICLE_DELETION_ERROR_ARTICLES_2 =
            err_prefix + "TYPEARTICLE_DELETION_ERROR_ARTICLES_2";
    public static final String TYPEPC_DELETION_ERROR_ARTICLES_2 = err_prefix
            + "TYPEPC_DELETION_ERROR_ARTICLES_2";
    /** Error message when the type article is used by at least one demand */
    public static final String TYPEARTICLE_DELETION_USEDBY_DEMAND = err_prefix
            + "TYPEARTICLE_DELETION_USEDBY_DEMAND";
    
    public static final String OBSO_CONFIRMATION_DELETION_FOR_PN_TYPE_SOFTWARE =
            warn_prefix + "OBSO_CONFIRMATION_DELETION_FOR_PN_TYPE_SOFTWARE";
    public static final String TYPEARTICLE_NOT_SELECTED = warn_prefix
            + "TYPEARTICLE_NOT_SELECTED";
    public static final String DELETION_OK = info_prefix + "DELETION_OK";
    public static final String VALUELIST_ERROR = err_prefix + "VALUELIST_ERROR";
    public static final String ASN_OR_MSN_MANDATORY = err_prefix
            + "ASN_OR_MSN_MANDATORY";
    public static final String SN_NON_UNIQUE = err_prefix + "SN_NON_UNIQUE";
    
    /*
     * Tool error messages
     */
    
    /** When the tool name already exists */
    public static final String TOOL_NAME_ALREADY_USED = err_prefix
            + "TOOL_NAME_ALREADY_USED";
    /** When the tool name is not provided */
    public static final String TOOL_NAME_MUST_BE_FILLED = err_prefix
            + "TOOL_NAME_MUST_BE_FILLED";
    /** When the tool name is unknown */
    public static final String TOOL_UNKNOWN = err_prefix + "TOOL_UNKNOWN";
    /**
     * Error message when the tool cannot be deleted because it contains at
     * least one demand
     */
    public static final String TOOL_DELETION_CONTAINS_DEMAND = err_prefix
            + "TOOL_DELETION_CONTAINS_DEMAND";
    
    /*
     * Demand error messages
     */
    
    /** Error message when no PC is selected to be allocated to a demand */
    public static final String NO_PC_SELECTED = err_prefix + "NO_PC_SELECTED";
    /** Error message when the date is mandatory but empty */
    public static final String DATE_MANDATORY = err_prefix + "DATE_MANDATORY";
    
    public static final String DEMAND_NOT_CONFIRMED = err_prefix
            + "DEMAND_NOT_CONFIRMED";
    
    public static final String ALREADY_A_PC_ALLOCATED_TO_DEMAND = err_prefix
            + "ALREADY_A_PC_ALLOCATED_TO_DEMAND";
    public static final String DEMAND_DOES_NOT_EXIST = err_prefix
            + "DEMAND_DOES_NOT_EXIST";
    /** Key for the demands action fieldset title */
    public static final String DEMAND_ACTION_TITLE = "demandActionTitle";
    
    /*
     * Automatic update error messages
     */
    
    /** Error message when no file is selected */
    public static final String NO_SELECTED_FILE = err_prefix
            + "NO_SELECTED_FILE";
    /**
     * Error message when the connection to the PC fails<br>
     * <b>Parameter:</b> (0) the PC name
     */
    public static final String PC_CONNECTION_FAILURE = err_prefix
            + "PC_CONNECTION_FAILURE";
    /**
     * When the parsed file does not concern the current PC<br>
     * <b>Parameters:</b> (0) the PC name found in file, (1) the name of the PC
     * to update
     */
    public static final String PARSED_FILE_CONCERNING_ANOTHER_PC = err_prefix
            + "PARSED_FILE_CONCERNING_ANOTHER_PC";
    /**
     * When the parsed type does not match the current PC type<br>
     * <b>Parameters:</b> (0) the PC type found in file, (1) the PC type
     * in the database
     */
    public static final String PARSED_TYPE_MISMATCH =
            err_prefix + "PARSED_TYPE_MISMATCH";
    
    /*
     * Other error messages
     */
    
    public static final String STOCK_SELECTION_NAME_ALREADY_USED =
            warn_prefix + "STOCK_SELECTION_NAME_ALREADY_USED";
    
    /*
     * Other general constants
     */
    
    /** Yes value */
    public static final String YES = "yes";
    /** No value */
    public static final String NO = "no";
    /** Automatically filled field */
    public static final String AUTO_FILLED = "automaticallyFilled";
    /** Info message when an article is set to archived */
    public static final String UPDATE_TO_ARCHIVED_INFO =
            info_prefix + "UPDATE_TO_ARCHIVED";
    /** Warning message when an article is set to archived */
    public static final String UPDATE_TO_ARCHIVED_WARN =
            warn_prefix + "UPDATE_TO_ARCHIVED";
    
}
