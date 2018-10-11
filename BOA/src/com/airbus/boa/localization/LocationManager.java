/*
 * ------------------------------------------------------------------------
 * Class : LocationManager
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.airbus.boa.control.LocationManagerBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.LocationOrm;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.LocalizationException.LocalizationError;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Location manager for located entities
 */
public class LocationManager implements Serializable {
    
    private enum LocationKind {
        
        ArticleStoredInPlace,
        DemandStoredInPlace,
        InstallationInLabo,
        ToolInPlace,
        
        NotLocated;
    }
    
    private enum ExternalLocationKind {
        
        ArticleSentToExternalEntity,
        DemandSentToExternalEntity,
        ToolSentToExternalEntity,
        
        NotSent;
    }
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(LocationManager.class
            .getSimpleName());
    
    /**
     * Check if the location is valid (location is mandatory for all items)
     * 
     * @param pLocation
     *            the location to check
     * @throws LocalizationException
     *             when the location is not valid
     */
    public static void validateLocation(Location pLocation)
            throws LocalizationException {
        
        if (pLocation == null
                || !(pLocation.isLocated() || pLocation.isInherited())) {
            throw new LocalizationException(
                    LocalizationError.LocationMandatory,
                    MessageBundle.getMessage(Constants.LOCATION_MANDATORY));
        }
    }
    
    /**
     * Generate the location based on the provided values
     * 
     * @param pPlace
     *            the location place
     * @param pPrecision
     *            the location precision string
     * @param pExternalEntity
     *            the external location external entity
     * @param pInherited
     *            boolean indicating if the location is inherited from parent
     * @param pLocatedType
     *            the located object type
     * @param pItemContainer
     *            the located object container
     * @return the created location or null
     * @throws LocalizationException
     *             when the location is not available for provided item type
     */
    public static Location generateLocation(Place pPlace, String pPrecision,
            ExternalEntity pExternalEntity, boolean pInherited,
            LocatedType pLocatedType, Container pItemContainer)
            throws LocalizationException {
        
        if (pInherited) {
            if (!isInheritedLocationAvailable(pLocatedType)) {
                throw new LocalizationException(
                        LocalizationError.InheritedLocationNotAvailableForItem,
                        MessageBundle.getMessage(Constants.LOCATION_NOT_VALID));
            }
            else if (pItemContainer == null) {
                throw new LocalizationException(
                        LocalizationError.InheritedLocationNotAvailableContainerNotDefined,
                        MessageBundle.getMessage(Constants.LOCATION_NOT_VALID));
            }
        }
        else {
            if (pPlace == null) {
                return null;
            }
            if (!isLocationAvailable(pPlace, pLocatedType)) {
                String lMsg;
                if (pLocatedType == LocatedType.Installation) {
                    lMsg =
                            MessageBundle.getMessageResource(
                                    Constants.PLACE_MUST_BE_LABO_OR_ROOM,
                                    new Object[] { pPlace.getName() });
                }
                else {
                    lMsg =
                            MessageBundle
                                    .getMessage(Constants.LOCATION_NOT_VALID);
                }
                throw new LocalizationException(
                        LocalizationError.LocationNotAvailableForItem, lMsg);
            }
        }
        
        // Retrieve the location
        Place lPlace = pPlace;
        
        // If location is inherited from parent, reinitialize the place
        if (pInherited) {
            lPlace = getParentLocation(pItemContainer);
        }
        
        // Build the location
        Location lLocation =
                new Location(pInherited, lPlace, pExternalEntity, pLocatedType);
        lLocation.setPrecision(pPrecision);
        return lLocation;
    }
    
    /**
     * Generate the location based on the provided values. The inherited
     * attribute of location is set to false.
     * 
     * @param pPlace
     *            the location place
     * @param pPrecision
     *            the location precision string
     * @param pExternalEntity
     *            the external location external entity
     * @param pLocatedType
     *            the located object type
     * @return the created location or null
     * @throws LocalizationException
     *             when the location is not available for provided item type
     */
    public static Location generateLocation(Place pPlace, String pPrecision,
            ExternalEntity pExternalEntity, LocatedType pLocatedType)
            throws LocalizationException {
        
        return generateLocation(pPlace, pPrecision, pExternalEntity, false,
                pLocatedType, null);
    }
    
    /**
     * If the container is defined, there are 3 cases depending on the
     * pUpdateLocationAction value:
     * - 0: the location is inherited from the container
     * - 1: the location is copied from the container
     * - 2: the current location is left unchanged
     * If the container is not defined, there are 2 cases;
     * - if the location was inherited, it is copied.
     * - else nothing is done.
     * 
     * @param pLocation
     *            the location to update
     * @param pContainer
     *            the located item container
     * @param pUpdateLocationAction
     *            integer indicating the action to do on the location
     * @param pLocatedType
     *            the type of the located item
     * @return the updated location or a new one
     * @throws LocalizationException
     *             when the location is not available for provided item type
     */
    public static Location updateLocationWithContainer(Location pLocation,
            Container pContainer, int pUpdateLocationAction,
            LocatedType pLocatedType) throws LocalizationException {
        
        Location lLocation = pLocation;
        
        if (pContainer != null) {
            if (pUpdateLocationAction == 0) {
                if (lLocation == null) {
                    lLocation = generateLocation(getParentLocation(pContainer),
                            null, null, true, pLocatedType, pContainer);
                }
                else {
                    lLocation.setPlace(getParentLocation(pContainer));
                    lLocation.setInherited(true);
                }
            }
            else if (pUpdateLocationAction == 1) {
                if (lLocation == null) {
                    lLocation = generateLocation(getParentLocation(pContainer),
                            null, null, false, pLocatedType, pContainer);
                }
                else {
                    lLocation.setPlace(getParentLocation(pContainer));
                    lLocation.setInherited(false);
                }
            }
        }
        else if (lLocation != null && lLocation.isInherited()) {
            lLocation.setInherited(false);
        }
        
        return lLocation;
    }
    
    /**
     * Update the location item of the location. If the location is not defined,
     * a new one may be created.
     * 
     * @param pLocation
     *            the location to update
     * @param pPlace
     *            the location item
     * @param pLocatedType
     *            the type of the located item
     * @return the update location or a new one
     * @throws LocalizationException
     *             when the location is not valid
     */
    public static Location updateLocationItem(Location pLocation, Place pPlace,
            LocatedType pLocatedType) throws LocalizationException {
        
        if (pLocation != null) {
            pLocation.setPlace(pPlace);
            return pLocation;
        }
        else {
            if (pPlace != null) {
                Location lLocation =
                        generateLocation(pPlace, null, null, pLocatedType);
                return lLocation;
            }
            else {
                return null;
            }
        }
    }
    
    /**
     * Update the location inherited attribute and, if it is true, update the
     * location with the parent location.
     * 
     * @param pLocation
     *            the location to update
     * @param pInherited
     *            the inherited value
     * @param pContainer
     *            the located item container
     * @param pLocatedType
     *            the type of the located item
     * @return the updated location or a new one
     * @throws LocalizationException
     *             when the location is not available for provided item type
     */
    public static Location updateInherited(Location pLocation,
            boolean pInherited, Container pContainer, LocatedType pLocatedType)
            throws LocalizationException {
        
        if (pLocation != null) {
            // Update inherited attribute
            pLocation.setInherited(pInherited);
            if (pLocation.isInherited()) {
                // Update the location with the container location
                if (pContainer != null) {
                    pLocation.setPlace(getParentLocation(pContainer));
                }
                else {
                    pLocation.setPlace(null);
                }
            }
            return pLocation;
        }
        else {
            Location lLocation =
                    generateLocation(null, null, null, pInherited,
                            pLocatedType, pContainer);
            return lLocation;
        }
    }
    
    /**
     * Update the external location item of the location. If the location is not
     * defined, a new one may be created.
     * 
     * @param pLocation
     *            the location to update
     * @param pExternalEntity
     *            the external location item
     * @param pLocatedType
     *            the type of the located item
     * @return the update location or a new one
     * @throws LocalizationException
     *             when the location is not valid
     */
    public static Location updateExternalLocationItem(Location pLocation,
            ExternalEntity pExternalEntity, LocatedType pLocatedType)
            throws LocalizationException {
        
        if (pLocation != null) {
            pLocation.setExternalEntity(pExternalEntity);
            return pLocation;
        }
        else {
            if (pExternalEntity != null) {
                Location lLocation =
                        generateLocation(null, null, pExternalEntity,
                                pLocatedType);
                return lLocation;
            }
            else {
                return null;
            }
        }
    }
    
    private static Place getParentLocation(Container pContainer) {
        
        if (pContainer != null) {
            if (pContainer.getContainerItem() instanceof LocatedItem) {
                LocationManager lParentLocationManager =
                        new LocationManager(
                                (LocatedItem) pContainer.getContainerItem());
                Location lParentLocation = lParentLocationManager.getLocation();
                if (lParentLocation != null) {
                    return lParentLocation.getPlace();
                }
            }
        }
        return null;
    }
    
    /**
     * @param pDirectlyLocatedItems
     *            the list of located items directly in the location item
     * @return the list of located items through container inheritance
     */
    public static List<LocatedItem> getInheritedLocatedItems(
            List<LocatedItem> pDirectlyLocatedItems) {
        
        List<LocatedItem> lInheritedLocatedItems = new ArrayList<LocatedItem>();
        
        for (LocatedItem lLocatedItem : pDirectlyLocatedItems) {
            
            if (lLocatedItem instanceof ContainerItem) {
                // Located item is also a container item, retrieve the contained
                // items inheriting from it
                ContainerItem lContainerItem = (ContainerItem) lLocatedItem;
                
                for (ContainedItem lContainedItem : lContainerItem
                        .getContainedItemsInheriting()) {
                    // Contained item is also a located item, add it to the list
                    if (lContainedItem instanceof LocatedItem) {
                        lInheritedLocatedItems
                                .add((LocatedItem) lContainedItem);
                    }
                }
            }
        }
        return lInheritedLocatedItems;
    }
    
    /**
     * Return true if the location can be replaced by the parent location when
     * changing parent. Return false when the location is inherited from parent.
     * 
     * @param pLocation
     *            the location to check
     * @return true if the location is defined by its own (not inherited)
     */
    public static Boolean isLocationReplaceAvailable(Location pLocation) {
        
        if (pLocation == null) {
            return true;
        }
        else {
            return !pLocation.isInherited();
        }
    }
    
    /**
     * @param pLocatedType
     *            the located item type
     * @return true if the external location is available, else false
     */
    public static boolean isExternalLocationAvailable(LocatedType pLocatedType) {
        
        switch (pLocatedType) {
        
        case Installation:
            return false;
            
        case Cabinet:
        case Rack:
        case Switch:
        case Board:
        case PC:
        case Demand:
        case Various:
        case Tool:
            return true;
            
        default:
            return false;
        }
    }
    
    /**
     * @param pPlace
     *            the place to check
     * @param pLocatedType
     *            the located object type
     * @return true if an item of the located type can be located into the
     *         place, else false
     */
    public static boolean isLocationAvailable(Place pPlace,
            LocatedType pLocatedType) {
        
        switch (pLocatedType) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
        case Tool:
        case Demand:
            return true;
            
        case Installation:
            switch (pPlace.getType()) {
            case Laboratory:
            case Room:
                return true;
                
            case Storeroom:
            default:
                return false;
            }
            
        default:
            return false;
        }
    }
    
    /**
     * @param pLocatedType
     *            the located object type
     * @return true if an item of the located type can be located into the
     *         location of the parent, else false
     */
    public static boolean
            isInheritedLocationAvailable(LocatedType pLocatedType) {
        switch (pLocatedType) {
        
        case Installation:
            return false;
            
        case Cabinet:
        case Rack:
        case Switch:
        case Board:
        case PC:
        case Demand:
        case Various:
        case Tool:
            return true;
            
        default:
            return false;
        }
    }
    
    private LocatedType locatedType;
    
    /** Managed entity when it is an Article */
    private Article article;
    /** Managed entity when it is an Installation */
    private Installation installation;
    /** Managed entity when it is a Tool */
    private Tool tool;
    /** Managed entity when it is a demand */
    private Demand demand;
    
    /**
     * Constructor.<br>
     * Initialize the location manager depending on the provided entity which
     * can be: <br>
     * - an Article <br>
     * - an Installation <br>
     * - a Tool <br>
     * - a Demand
     * 
     * @param pLocatedItem
     *            the entity for which to initialize the location manager (must
     *            be not null)
     */
    public LocationManager(LocatedItem pLocatedItem) {
        
        article = null;
        installation = null;
        tool = null;
        demand = null;
        locatedType = pLocatedItem.getLocatedType();
        
        switch (locatedType) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
            article = (Article) pLocatedItem;
            break;
        case Demand:
            demand = (Demand) pLocatedItem;
            break;
        case Installation:
            installation = (Installation) pLocatedItem;
            break;
        case Tool:
            tool = (Tool) pLocatedItem;
            break;
        default:
            break;
        }
    }
    
    /**
     * Move the managed entity to the provided location (and external location)
     * 
     * @param pLocation
     *            the location to which move the managed entity
     * @param pLocationManagerBean
     *            the LocationManagerBean to use
     */
    public void moveTo(Location pLocation,
            LocationManagerBean pLocationManagerBean) {
        
        Location lCurrentLocation = getLocation();
        
        if ((pLocation == null && lCurrentLocation == null)
                || (pLocation != null && pLocation.equals(lCurrentLocation))) {
            // The new location is equal to the current one
            return;
        }
        
        if (lCurrentLocation == null) {
            // The new location is not null, move to this new location
            moveToLocation(pLocation, pLocationManagerBean);
            moveToExternalLocation(pLocation, pLocationManagerBean);
        }
        else {
            // Current location is not null
            
            if (!lCurrentLocation.equalsLocation(pLocation)) {
                // Current location is different from the new location
                moveToLocation(pLocation, pLocationManagerBean);
            }
            
            if (!lCurrentLocation.equalsExternalLocation(pLocation)) {
                // Current external location is different from the new external
                // location
                moveToExternalLocation(pLocation, pLocationManagerBean);
            }
        }
    }
    
    /**
     * Move the managed entity to the provided location
     * 
     * @param pLocation
     *            the location to which move the managed entity
     * @param pLocationManagerBean
     *            the locationManagerBean to use
     */
    private void moveToLocation(Location pLocation,
            LocationManagerBean pLocationManagerBean) {
        
        removeFromLocation(pLocationManagerBean);
        
        boolean lMoved = false;
        
        if (pLocation == null
                || !(pLocation.isLocated() || pLocation.isInherited())) {
            return;
        }
        
        Place lPlace = pLocation.getPlace();
        String lPrecision = pLocation.getPrecision();
        boolean lInherited = pLocation.isInherited();
        
        switch (getAuthorizedLocationKind()) {
        
        case ArticleStoredInPlace:
            
            if (lInherited) {
                if (isPossibleInheritedLocation()) {
                    article =
                            pLocationManagerBean.moveToInheritedLocation(
                                    article, lPrecision);
                    lMoved = true;
                }
            }
            else {
                if (isLocationAvailable(lPlace, article.getLocatedType())) {
                    article =
                            pLocationManagerBean.moveToPlace(article, lPlace,
                                    lPrecision);
                    lMoved = true;
                }
            }
            break;
        
        case DemandStoredInPlace:
            
            if (lInherited) {
                if (isPossibleInheritedLocation()) {
                    demand =
                            pLocationManagerBean.moveToInheritedLocation(
                                    demand, lPrecision);
                    lMoved = true;
                }
            }
            else {
                if (isLocationAvailable(lPlace, LocatedType.Demand)) {
                    demand =
                            pLocationManagerBean.moveToPlace(demand, lPlace,
                                    lPrecision);
                    lMoved = true;
                }
            }
            break;
        
        case InstallationInLabo:
            
            if (isLocationAvailable(lPlace, LocatedType.Installation)) {
                pLocationManagerBean.moveToPlace(installation, lPlace);
                lMoved = true;
            }
            break;
        
        case ToolInPlace:
            
            if (lInherited) {
                if (isPossibleInheritedLocation()) {
                    tool =
                            pLocationManagerBean.moveToInheritedLocation(tool,
                                    lPrecision);
                    lMoved = true;
                }
            }
            else {
                if (isLocationAvailable(lPlace, LocatedType.Tool)) {
                    tool =
                            pLocationManagerBean.moveToPlace(tool, lPlace,
                                    lPrecision);
                    lMoved = true;
                }
            }
            break;
        
        case NotLocated:
        default:
            break;
        }
        
        if (!lMoved) {
            log.warning("LocationManager.moveTo: Illegal location");
        }
    }
    
    /**
     * Move the managed entity to the provided external location
     * 
     * @param pLocation
     *            the location to which move the managed entity
     * @param pLocationManagerBean
     *            the locationManagerBean to use
     */
    private void moveToExternalLocation(Location pLocation,
            LocationManagerBean pLocationManagerBean) {
        
        removeFromExternalLocation(pLocationManagerBean);
        
        boolean lMoved = false;
        
        if (pLocation == null || !pLocation.isExternalLocated()) {
            return;
        }
        
        List<ExternalLocationKind> lAuthorizedLocationKinds =
                getAuthorizedExternalLocationKinds();
        
        Iterator<ExternalLocationKind> lLocationKindIterator =
                lAuthorizedLocationKinds.iterator();
        
        ExternalEntity lExternalEntity = pLocation.getExternalEntity();
        
        while ((!lMoved) && lLocationKindIterator.hasNext()) {
            
            switch (lLocationKindIterator.next()) {
            
            case ArticleSentToExternalEntity:
                
                article =
                        pLocationManagerBean.moveToExternalEntity(article,
                                lExternalEntity);
                lMoved = true;
                break;
            
            case DemandSentToExternalEntity:
                
                demand =
                        pLocationManagerBean.moveToExternalEntity(demand,
                                lExternalEntity);
                lMoved = true;
                break;
            
            case ToolSentToExternalEntity:
                
                tool =
                        pLocationManagerBean.moveToExternalEntity(tool,
                                lExternalEntity);
                lMoved = true;
                break;
            
            case NotSent:
            default:
                break;
            }
        }
        
        if (!lMoved) {
            log.warning("LocationManager.moveTo: Illegal external location");
        }
    }
    
    /**
     * Remove the managed entity from any location and external location
     * 
     * @param pLocationManagerBean
     *            the locationManagerBean to use
     */
    public void removeFrom(LocationManagerBean pLocationManagerBean) {
        
        removeFromLocation(pLocationManagerBean);
        removeFromExternalLocation(pLocationManagerBean);
        
        // Remove the empty location line from database
        switch (locatedType) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
            article = pLocationManagerBean.removeFromAll(article);
            break;
        case Installation:
            // Location of installation is already removed
            break;
        case Tool:
            tool = pLocationManagerBean.removeFromAll(tool);
            break;
        case Demand:
            demand = pLocationManagerBean.removeFromAll(demand);
            break;
        default:
            break;
        }
    }
    
    private void removeFromLocation(LocationManagerBean pLocationManagerBean) {
        
        Location lLocation = getLocation();
        if (lLocation == null
                || !(lLocation.isInherited() || lLocation.isLocated())) {
            // The entity is already not located
            return;
        }
        
        boolean lInherited = lLocation.isInherited();
        
        switch (getLocationKind()) {
        
        case ArticleStoredInPlace:
            
            if (lInherited) {
                article =
                        pLocationManagerBean
                                .removeFromInheritedLocation(article);
            }
            else if (lLocation.getPlace() != null) {
                article =
                        pLocationManagerBean.removeFromPlace(article,
                                lLocation.getPlace());
            }
            break;
        
        case DemandStoredInPlace:
            
            if (lInherited) {
                demand =
                        pLocationManagerBean
                                .removeFromInheritedLocation(demand);
            }
            else {
                demand = pLocationManagerBean.removeFromPlace(demand);
            }
            break;
        
        case InstallationInLabo:
            
            pLocationManagerBean.removeFromPlace(installation,
                    lLocation.getPlace());
            break;
        
        case ToolInPlace:
            
            if (lInherited) {
                tool = pLocationManagerBean.removeFromInheritedLocation(tool);
            }
            else if (lLocation.getPlace() != null) {
                tool =
                        pLocationManagerBean.removeFromPlace(tool,
                                lLocation.getPlace());
            }
            break;
        
        case NotLocated:
        default:
            break;
        }
    }
    
    /**
     * Return the managed entity from any external location
     * 
     * @param pLocationManagerBean
     *            the locationManagerBean to use
     */
    private void removeFromExternalLocation(
            LocationManagerBean pLocationManagerBean) {
        
        Location lLocation = getLocation();
        
        if (lLocation == null || !lLocation.isExternalLocated()) {
            // The entity is already not located
            return;
        }
        
        ExternalEntity lExternalEntity = lLocation.getExternalEntity();
        
        switch (getExternalLocationKind()) {
        
        case ArticleSentToExternalEntity:
            
            if (lExternalEntity != null) {
                article =
                        pLocationManagerBean.removeFromExternalEntity(article,
                                lExternalEntity);
            }
            break;
        
        case DemandSentToExternalEntity:
            
            demand = pLocationManagerBean.removeFromExternalEntity(demand);
            break;
        
        case ToolSentToExternalEntity:
            
            if (lExternalEntity != null) {
                tool =
                        pLocationManagerBean.removeFromExternalEntity(tool,
                                lExternalEntity);
            }
            break;
        
        case NotSent:
        default:
            break;
        }
    }
    
    /**
     * @return true if the external location is available, else false
     */
    public Boolean isPossibleExternalLocation() {
        
        if (locatedType == null) {
            return false;
        }
        
        return isExternalLocationAvailable(locatedType);
    }
    
    /**
     * @return true if the location can be inherited from parent, else false
     */
    public Boolean isPossibleInheritedLocation() {
        
        if (locatedType == null) {
            return false;
        }
        
        return isInheritedLocationAvailable(locatedType);
    }
    
    /**
     * Determine the authorized location kind for managed entity.
     * 
     * @return a list of location kinds
     */
    private LocationKind getAuthorizedLocationKind() {
        
        if (locatedType == null) {
            return null;
        }
        
        switch (locatedType) {
        
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
            // All Articles can be located in a Place
            return LocationKind.ArticleStoredInPlace;
            
        case Demand:
            // Location of the PC of a demand in a Place
            return LocationKind.DemandStoredInPlace;
            
        case Tool:
            // Location of a Tool in a Place
            return LocationKind.ToolInPlace;
            
        case Installation:
            // Location of an Installation in a Laboratory or a Room
            return LocationKind.InstallationInLabo;
            
        default:
            return null;
        }
    }
    
    /**
     * Determine the list of authorized external locations kind for managed
     * entity.
     * 
     * @return a list of external location kinds
     */
    private List<ExternalLocationKind> getAuthorizedExternalLocationKinds() {
        
        List<ExternalLocationKind> lLocKinds =
                new ArrayList<ExternalLocationKind>();
        
        if (isPossibleExternalLocation()) {
            
            switch (locatedType) {
            
            case Board:
            case Cabinet:
            case PC:
            case Rack:
            case Switch:
            case Various:
                // All Articles can be sent to an external entity
                lLocKinds.add(ExternalLocationKind.ArticleSentToExternalEntity);
                break;
            
            case Demand:
                // Sent of the PC of a demand to an external entity
                lLocKinds.add(ExternalLocationKind.DemandSentToExternalEntity);
                break;
            
            case Tool:
                // Specific sent of a Tool to an external entity
                lLocKinds.add(ExternalLocationKind.ToolSentToExternalEntity);
                break;
            
            case Installation:
            default:
                break;
            }
        }
        return lLocKinds;
    }
    
    /**
     * Determine the managed entity kind of location.
     * 
     * @return the location kind
     */
    private LocationKind getLocationKind() {
        
        switch (getAuthorizedLocationKind()) {
        
        case ArticleStoredInPlace:
            
            if (article.getLocationOrm() != null) {
                return LocationKind.ArticleStoredInPlace;
            }
            break;
        
        case DemandStoredInPlace:
            
            if (demand.getLocationOrm() != null) {
                return LocationKind.DemandStoredInPlace;
            }
            break;
        
        case InstallationInLabo:
            
            if (installation.getLocationPlace() != null) {
                return LocationKind.InstallationInLabo;
            }
            break;
        
        case ToolInPlace:
            
            if (tool.getLocationOrm() != null) {
                return LocationKind.ToolInPlace;
            }
            break;
        
        case NotLocated:
        default:
            break;
        }
        
        return LocationKind.NotLocated;
    }
    
    /**
     * Determine the managed entity kind of external location.
     * 
     * @return the location kind
     */
    private ExternalLocationKind getExternalLocationKind() {
        
        // Browse all authorized external location kinds
        for (ExternalLocationKind lExternalLocationKind : getAuthorizedExternalLocationKinds()) {
            
            switch (lExternalLocationKind) {
            
            case ArticleSentToExternalEntity:
                
                if (article.getLocationOrm() != null
                        && article.getLocationOrm().getExternalEntity() != null) {
                    return ExternalLocationKind.ArticleSentToExternalEntity;
                }
                break;
            
            case DemandSentToExternalEntity:
                
                if (demand.getLocationOrm() != null
                        && demand.getLocationOrm().getExternalEntity() != null) {
                    return ExternalLocationKind.DemandSentToExternalEntity;
                }
                break;
            
            case ToolSentToExternalEntity:
                
                if (tool.getLocationOrm() != null
                        && tool.getLocationOrm().getExternalEntity() != null) {
                    return ExternalLocationKind.ToolSentToExternalEntity;
                }
                break;
            
            case NotSent:
            default:
                break;
            }
        }
        
        return ExternalLocationKind.NotSent;
    }
    
    /**
     * Compute the managed entity location. <br>
     * If it is an available location, return it. <br>
     * If it is a non available location, throw an exception. <br>
     * If no location is found, return null
     * 
     * @return the managed entity location (may be null)
     */
    public Location getLocation() {
        
        // Retrieve the location
        LocationOrm lLocationOrm = null;
        Container lContainer = null;
        LocatedType lLocatedType = null;
        switch (getLocationKind()) {
        
        case ArticleStoredInPlace:
            
            lLocationOrm = article.getLocationOrm();
            lContainer = article.getContainer();
            lLocatedType = article.getLocatedType();
            break;
        
        case DemandStoredInPlace:
            
            lLocationOrm = demand.getLocationOrm();
            lContainer = demand.getContainer();
            lLocatedType = demand.getLocatedType();
            break;
        
        case InstallationInLabo:
            
            // An installation cannot be located in an external entity
            return new Location(false, installation.getLocationPlace(), null,
                    LocatedType.Installation);
            
        case ToolInPlace:
            
            lLocationOrm = tool.getLocationOrm();
            lContainer = tool.getContainer();
            lLocatedType = tool.getLocatedType();
            break;
        
        case NotLocated:
            break;
        
        default:
            throw new IllegalArgumentException("Not managed location kind");
        }
        
        Place lPlace = null;
        String lPrecision = null;
        boolean lInherited = false;
        if (lLocationOrm != null) {
            lPlace = lLocationOrm.getPlace();
            lPrecision = lLocationOrm.getPreciseLocation();
            lInherited = lLocationOrm.isInherited();
        }
        
        // Retrieve the external location
        ExternalEntity lExternalEntity = null;
        switch (getExternalLocationKind()) {
        
        case ArticleSentToExternalEntity:
            
            lExternalEntity = article.getLocationOrm().getExternalEntity();
            break;
        
        case DemandSentToExternalEntity:
            
            lExternalEntity = demand.getLocationOrm().getExternalEntity();
            break;
        
        case ToolSentToExternalEntity:
            
            lExternalEntity = tool.getLocationOrm().getExternalEntity();
            break;
        
        case NotSent:
            
            break;
        
        default:
            throw new IllegalArgumentException(
                    "Not managed external location kind");
        }
        
        // If location is inherited from parent, reinitialize the place
        if (lInherited) {
            lPlace = getParentLocation(lContainer);
        }
        
        // Build the location
        if (lInherited || lPlace != null || lExternalEntity != null) {
            Location lLocation =
                    new Location(lInherited, lPlace, lExternalEntity,
                            lLocatedType);
            lLocation.setPrecision(lPrecision);
            return lLocation;
        }
        else {
            return null;
        }
    }
    
}
