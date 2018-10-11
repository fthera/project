/*
 * ------------------------------------------------------------------------
 * Class : MessageConstants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.service;

/**
 * Message keys constants definition
 */
public class MessageConstants {
    
    private static final String err_prefix = "BOA.ERROR.";
    
    /**
     * Description of the File Path column for file imported for linking
     * documents.
     */
    public static final String IMPORT_DOC_FILE_PATH_COLUMN_DESCRIPTION =
            "ImportDocumentsFilePathColumnDescription";
    
    /**
     * Description of a sheet of file imported for linking documents.<br>
     * Parameters are:<br>
     * - the items to which link the documents (Articles, Installations, Tools)<br>
     * - the sheet name
     */
    public static final String IMPORT_DOC_FILE_SHEET_DESCRIPTION =
            "ImportDocumentsFileSheetDescription";
    
    /** Error message when the Software already exists */
    public static final String SOFTWARE_ALREADY_EXISTED = err_prefix
            + "SOFTWARE_ALREADY_EXISTED";
    
    /** Error message when the complete name is invalid */
    public static final String SOFTWARE_COMPLETENAME_INCORRECT = err_prefix
            + "SOFTWARE_COMPLETENAME_INCORRECT";
    
    /** Error message when an error occurs while deleting a Software */
    public static final String SOFTWARE_DELETION_ERROR = err_prefix
            + "SOFTWARE_DELETION_ERROR";
    
    /**
     * Error message when the Software cannot be deleted because it is used by
     * at least one Demand
     */
    public static final String SOFTWARE_DELETION_USED_BY_DEMAND = err_prefix
            + "SOFTWARE_DELETION_USED_BY_DEMAND";
    
    /** Error message when the software distribution is not existing */
    public static final String SOFTWARE_DISTRIBUTION_NOT_EXISTING = err_prefix
            + "SOFTWARE_DISTRIBUTION_NOT_EXISTING";
    
    /** Error message when the Software kernel is empty */
    public static final String SOFTWARE_KERNEL_EMPTY = err_prefix
            + "SOFTWARE_KERNEL_EMPTY";
    
    /** Error message when the software name is not existing */
    public static final String SOFTWARE_NAME_NOT_EXISTING = err_prefix
            + "SOFTWARE_NAME_NOT_EXISTING";
    
    /** Error message when the Software name or distribution is empty */
    public static final String SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY = err_prefix
            + "SOFTWARE_NAME_OR_DISTRIBUTION_EMPTY";
    
    /** Error message when the Software does not exist */
    public static final String SOFTWARE_NOT_FOUND = err_prefix
            + "SOFTWARE_NOT_FOUND";
    
    /** Error message when the Software does not exist */
    public static final String SOFTWARE_ALREADY_INSTALLED =
            err_prefix + "SOFTWARE_ALREADY_INSTALLED";
    
    /** Error message when the person in charge does not exist */
    public static final String UNKNOWN_INCHARGE = err_prefix
            + "UNKNOWN_INCHARGE";

}
