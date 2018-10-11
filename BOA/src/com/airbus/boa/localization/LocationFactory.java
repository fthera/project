/*
 * ------------------------------------------------------------------------
 * Class : LocationFactory
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.LocationManagerBean;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.LocalizationException.LocalizationError;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Factory managing the location
 */
@Stateless
public class LocationFactory implements Serializable {
    
    private static final long serialVersionUID = -2833244631632144077L;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    /**
     * Constructor
     */
    public LocationFactory() {
    }
    
    /**
     * Generate the location based on the provided values
     * 
     * @param pLocationCompleteName
     *            the location complete name
     * @param pPrecision
     *            the location precision string
     * @param pExternalLocationName
     *            the external location name
     * @param pLocatedType
     *            the located object type
     * @param pInherited
     *            boolean indicating if the location is inherited from the
     *            parent
     * @param pItemContainer
     *            the located object container
     * @return the created location or null
     * @throws LocalizationException
     *             when the location is not valid
     */
    public Location generateLocation(String pLocationCompleteName,
            String pPrecision, String pExternalLocationName,
            LocatedType pLocatedType, boolean pInherited,
            Container pItemContainer) throws LocalizationException {
        
        Place lPlace = null;
        if (!StringUtil.isEmptyOrNull(pLocationCompleteName)) {
            lPlace =
                    locationBean.findPlaceByCompleteName(pLocationCompleteName);
            
            if (lPlace == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.LOCATION_NOT_FOUND,
                                new String[] { pLocationCompleteName });
                throw new LocalizationException(
                        LocalizationError.LocationNotFound, lMsg);
            }
        }
        
        ExternalEntity lExternalEntity = null;
        if (!StringUtil.isEmptyOrNull(pExternalLocationName)) {
            lExternalEntity =
                    locationBean
                            .findExternalEntityByName(pExternalLocationName);
            
            if (lExternalEntity == null) {
                String lMsg =
                        MessageBundle.getMessageResource(
                                Constants.EXTERNAL_LOCATION_NOT_FOUND,
                                new String[] { pExternalLocationName });
                throw new LocalizationException(
                        LocalizationError.ExternalLocationNotFound, lMsg);
            }
        }
        
        return LocationManager.generateLocation(lPlace, pPrecision,
                lExternalEntity, pInherited, pLocatedType, pItemContainer);
    }
    
    /**
     * Generate the location based on the provided values
     * 
     * @param pLocationId
     *            the location id
     * @param pPrecision
     *            the location precision string
     * @param pExternalLocationId
     *            the external location id (can be null)
     * @param pLocatedType
     *            the located object type
     * @param pInherited
     *            boolean indicating if the location is inherited from the
     *            parent
     * @param pItemContainer
     *            the located object container
     * @return the created location or null
     * @throws LocalizationException
     *             when the location is not valid
     */
    public Location generateLocation(Long pLocationId, String pPrecision,
            Long pExternalLocationId, LocatedType pLocatedType,
            boolean pInherited, Container pItemContainer)
            throws LocalizationException {
        
        Place lPlace = null;
        if (pLocationId != null) {
            lPlace = locationBean.findPlaceById(pLocationId);
        }
        
        ExternalEntity lExternalEntity = null;
        if (pExternalLocationId != null) {
            lExternalEntity =
                    locationBean.findExternalEntityById(pExternalLocationId);
        }
        
        return LocationManager.generateLocation(lPlace, pPrecision,
                lExternalEntity, pInherited, pLocatedType, pItemContainer);
    }
    
}
