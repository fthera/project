/*
 * ------------------------------------------------------------------------
 * Class : LocalizationException
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.exception;

/**
 * Exception thrown when the container, location or external location is not
 * available
 */
public class LocalizationException extends Exception {
    
    /** Enumerate of the localization errors */
    public enum LocalizationError {
        
        /** When the container is not available for the item */
        ContainerNotAvailableForItem,
        /** When the container details value is not valid */
        ContainerPrecisionNotValid,
        /** When the container is not found */
        ContainerNotFound,
        
        /** When the location is not available for the item */
        LocationNotAvailableForItem,
        /** When the location is not found */
        LocationNotFound,
        /** When the location is mandatory but not defined */
        LocationMandatory,
        /** When the location cannot be inherited from parent for the item */
        InheritedLocationNotAvailableForItem,
        
        /** When the location cannot be inherited since container not defined */
        InheritedLocationNotAvailableContainerNotDefined,
        
        /** When the external location is not found */
        ExternalLocationNotFound
    }
    
    private static final long serialVersionUID = 1L;
    
    private LocalizationError error;
    
    /**
     * Constructor
     * 
     * @param pError
     *            the error code
     * @param pMsg
     *            the error message
     */
    public LocalizationException(LocalizationError pError, String pMsg) {
        super(pMsg);
        error = pError;
    }
    
    /**
     * @return the error
     */
    public LocalizationError getError() {
        return error;
    }
    
    /**
     * @param pError
     *            the error to set
     */
    public void setError(LocalizationError pError) {
        error = pError;
    }
    
}
