/*
 * ------------------------------------------------------------------------
 * Class : ContainerManager
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.airbus.boa.control.ContainerManagerBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.Contains_Cabinet_Rack;
import com.airbus.boa.entity.location.Contains_Inst_Cabinet;
import com.airbus.boa.entity.location.Contains_PC_Board;
import com.airbus.boa.entity.location.Contains_Rack_Board;
import com.airbus.boa.entity.location.Contains_Rack_Board.Face;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.LocalizationException.LocalizationError;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Container manager for contained entities
 */
public class ContainerManager implements Serializable {
    
    private enum ContainerKind {
        
        ArticleInTool,
        ArticleUsedByCabinet,
        ArticleUsedByInstallation,
        ArticleUsedByPC,
        ArticleUsedByRack,
        ArticleUsedBySwitch,
        BoardInMotherboard,
        BoardInPC,
        BoardInRack,
        CabinetInInstallation,
        DemandInInstallation,
        DemandInTool,
        DemandUsedByCabinet,
        RackInCabinet,
        ToolInInstallation,
        
        NotContained,
        ErrorArticleUsedByIncorrectType;
    }
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(ContainerManager.class
            .getSimpleName());
    
    /**
     * Generate the container based on the provided values
     * 
     * @param pContainedType
     *            the contained object type
     * @param pContainerItem
     *            the container object
     * @param pPrecision
     *            the container precision string (can be null)
     * @return the created container or null
     * @throws LocalizationException
     *             when the precision is not valid
     */
    public static Container generateContainer(ContainedType pContainedType,
            ContainerItem pContainerItem, String pPrecision)
            throws LocalizationException {
        
        if (pContainedType == null || pContainerItem == null) {
            return null;
        }
        
        if (!isContainerAvailable(pContainerItem, pContainedType)) {
            String lMsg =
                    MessageBundle.getMessage(Constants.CONTAINER_NOT_VALID);
            throw new LocalizationException(
                    LocalizationError.ContainerNotAvailableForItem, lMsg);
        }
        
        Container lContainer = new Container(pContainerItem, pContainedType);
        
        String lMsg = lContainer.getPrecisionValidationError(pPrecision);
        if (lMsg.isEmpty()) {
            lContainer.setPrecision(pPrecision);
        }
        else {
            throw new LocalizationException(
                    LocalizationError.ContainerPrecisionNotValid, lMsg);
        }
        return lContainer;
    }
    
    /**
     * @param pContainedType
     *            the contained object type
     * @return the possible containers
     */
    private static ContainerType[] getPossibleContainers(
            ContainedType pContainedType) {
        
        switch (pContainedType) {
        
        case Cabinet:
            return new ContainerType[] { ContainerType.Installation };
            
        case Rack:
            return new ContainerType[] { ContainerType.Installation,
                    ContainerType.Cabinet, ContainerType.Tool };
            
        case Switch:
            return new ContainerType[] { ContainerType.Installation,
                    ContainerType.Cabinet, ContainerType.Rack,
                    ContainerType.Tool };
            
        case Board:
            return new ContainerType[] { ContainerType.Rack,
                    ContainerType.Switch, ContainerType.PC,
                    ContainerType.Board, ContainerType.Tool };
            
        case PC:
        case Demand:
            return new ContainerType[] { ContainerType.Installation,
                    ContainerType.Cabinet, ContainerType.Tool };
            
        case Various:
            return new ContainerType[] { ContainerType.Installation,
                    ContainerType.Cabinet, ContainerType.Rack,
                    ContainerType.PC, ContainerType.Tool };
            
        case Tool:
            return new ContainerType[] { ContainerType.Installation };
            
        default:
            return new ContainerType[] {};
        }
    }
    
    /**
     * Determine the master container of the provided container. <br>
     * When no master container is found, return the container.
     * 
     * @param pContainer
     *            the container
     * @return the master container or the provided container
     */
    public static Container getMasterContainer(Container pContainer) {
        
        Container lCurrentContainer = pContainer;
        
        if (lCurrentContainer == null) {
            return null;
        }
        
        Container lParentContainer = null;
        
        while (lCurrentContainer != null) {
            
            lParentContainer = lCurrentContainer;
            if (lCurrentContainer.getContainerItem() instanceof ContainedItem) {
                ContainerManager lContainerManager =
                        new ContainerManager(
                                (ContainedItem) lCurrentContainer
                                        .getContainerItem());
                lCurrentContainer = lContainerManager.getContainer();
            }
            else {
                lCurrentContainer = null;
            }
        }
        return lParentContainer;
    }
    
    /**
     * @param pContainedItems
     *            the contained items
     * @return the list of contained items inheriting their location from parent
     *         (retrieved from the provided list and their children)
     */
    public static List<ContainedItem> getInheritingContainedItem(
            List<ContainedItem> pContainedItems) {
        
        Set<ContainedItem> lContainedItems = new HashSet<ContainedItem>();
        
        for (ContainedItem lContainedItem : pContainedItems) {
            
            if (lContainedItem instanceof LocatedItem) {
                
                // ContainedItem is also a located item
                LocatedItem lLocatedItem = (LocatedItem) lContainedItem;
                Location lLocation = lLocatedItem.getLocation();
                
                if (lLocation != null && lLocation.isInherited()) {
                    
                    // Add current contained item
                    lContainedItems.add(lContainedItem);
                    
                    if (lContainedItem instanceof ContainerItem) {
                        // if contained item is also a container item, add its
                        // contained items inheriting its location
                        ContainerItem lContainerItem =
                                (ContainerItem) lContainedItem;
                        lContainedItems.addAll(lContainerItem
                                .getContainedItemsInheriting());
                    }
                }
            }
        }
        
        return new ArrayList<ContainedItem>(lContainedItems);
    }
    
    /**
     * @param pContainer
     *            the container object
     * @param pContainedType
     *            the contained object type
     * @return true if an item of the contained type can be linked into the
     *         article, else false
     */
    public static boolean isContainerAvailable(ContainerItem pContainer,
            ContainedType pContainedType) {
        
        for (ContainerType lContainerType : getPossibleContainers(pContainedType)) {
            
            if (lContainerType.equals(pContainer.getContainerType())) {
                return true;
            }
        }
        return false;
    }
    
    private ContainedType containedType;
    
    /** Managed entity when it is an Article */
    private Article article;
    /** Managed entity when it is a Tool */
    private Tool tool;
    /** Managed entity when it is a demand */
    private Demand demand;
    
    /**
     * Constructor.<br>
     * Initialize the container manager depending on the provided entity which
     * can be: <br>
     * - an Article <br>
     * - a Tool <br>
     * - a demand
     * 
     * @param pContainedItem
     *            the entity for which to create a container manager (must be
     *            not null)
     */
    public ContainerManager(ContainedItem pContainedItem) {
        
        article = null;
        tool = null;
        demand = null;
        containedType = pContainedItem.getContainedType();
        
        switch (containedType) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
        case Various:
            article = (Article) pContainedItem;
            break;
        case Tool:
            tool = (Tool) pContainedItem;
            break;
        case Demand:
            demand = (Demand) pContainedItem;
            break;
        default:
            break;
        }
    }
    
    /**
     * Link the managed entity to the provided container
     * 
     * @param pContainer
     *            the container to which move the managed entity
     * @param pContainerManagerBean
     *            the ContainerManagerBean to use
     */
    public void linkTo(Container pContainer,
            ContainerManagerBean pContainerManagerBean) {
        
        Container lCurrentContainer = getContainer();
        
        if ((pContainer == null && lCurrentContainer == null)
                || (pContainer != null && pContainer.equals(lCurrentContainer))) {
            // The new container is equal to the current one
            return;
        }
        
        unlink(pContainerManagerBean);
        
        boolean lMoved = false;
        
        if (pContainer == null) {
            return;
        }
        
        List<ContainerKind> lAuthorizedContainerKinds =
                getAuthorizedContainerKinds();
        
        Iterator<ContainerKind> lContainerKindIterator =
                lAuthorizedContainerKinds.iterator();
        
        while ((!lMoved) && lContainerKindIterator.hasNext()) {
            
            switch (lContainerKindIterator.next()) {
            
            case ArticleInTool:
                
                if (pContainer.getType() == ContainerType.Tool) {
                    article =
                            pContainerManagerBean.linkToTool(article,
                                    (Tool) pContainer.getContainerItem(),
                                    pContainer.getPrecision());
                    lMoved = true;
                }
                break;
            
            case ArticleUsedByCabinet:
                
                if (pContainer.getType() == ContainerType.Cabinet) {
                    article =
                            pContainerManagerBean.linkToArticle(article,
                                    (Article) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case ArticleUsedByRack:
                
                if (pContainer.getType() == ContainerType.Rack) {
                    article =
                            pContainerManagerBean.linkToArticle(article,
                                    (Article) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case ArticleUsedBySwitch:
                
                if (pContainer.getType() == ContainerType.Switch) {
                    article =
                            pContainerManagerBean.linkToArticle(article,
                                    (Article) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case ArticleUsedByPC:
                
                if (pContainer.getType() == ContainerType.PC) {
                    article =
                            pContainerManagerBean.linkToArticle(article,
                                    (Article) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case ArticleUsedByInstallation:
                
                if (pContainer.getType() == ContainerType.Installation) {
                    article =
                            pContainerManagerBean.linkToInstallation(article,
                                    (Installation) pContainer
                                            .getContainerItem());
                    lMoved = true;
                }
                break;
            
            case BoardInMotherboard:
                
                if (pContainer.getType() == ContainerType.Board) {
                    
                    article =
                            pContainerManagerBean.linkToBoard((Board) article,
                                    (Board) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case BoardInPC:
                
                if (pContainer.getType() == ContainerType.PC) {
                    
                    Object[] result;
                    try {
                        result =
                                Contains_PC_Board
                                        .convertStringToSlot(pContainer
                                                .getPrecision());
                    }
                    catch (ValidationException e) {
                        result = new Object[1];
                        log.warning("FORMAT SLOTNUMBER INCORRECT : "
                                + pContainer.getPrecision());
                    }
                    article =
                            pContainerManagerBean.linkToPC((Board) article,
                                    (PC) pContainer.getContainerItem(),
                                    (Integer) result[0]);
                    lMoved = true;
                }
                break;
            
            case BoardInRack:
                
                if (pContainer.getType() == ContainerType.Rack) {
                    
                    Object[] result;
                    try {
                        result =
                                Contains_Rack_Board
                                        .convertStringToSlotFace(pContainer
                                                .getPrecision());
                    }
                    catch (ValidationException e) {
                        result = new Object[2];
                        log.warning("FORMAT SLOTNUMBER ET FACE INCORRECT : "
                                + pContainer.getPrecision());
                    }
                    article =
                            pContainerManagerBean.linkToRack((Board) article,
                                    (Rack) pContainer.getContainerItem(),
                                    (Integer) result[0], (Face) result[1]);
                    lMoved = true;
                }
                break;
            
            case CabinetInInstallation:
                
                if (pContainer.getType() == ContainerType.Installation) {
                    article =
                            pContainerManagerBean.linkToInstallation(
                                    (Cabinet) article,
                                    (Installation) pContainer
                                            .getContainerItem(), pContainer
                                            .getPrecision());
                    lMoved = true;
                }
                break;
            
            case DemandInInstallation:
                
                if (pContainer.getType() == ContainerType.Installation) {
                    demand =
                            pContainerManagerBean.linkToInstallation(demand,
                                    (Installation) pContainer
                                            .getContainerItem());
                    lMoved = true;
                }
                break;
            
            case DemandInTool:
                
                if (pContainer.getType() == ContainerType.Tool) {
                    demand =
                            pContainerManagerBean.linkToTool(demand,
                                    (Tool) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case DemandUsedByCabinet:
                
                if (pContainer.getType() == ContainerType.Cabinet) {
                    demand =
                            pContainerManagerBean.linkToArticle(demand,
                                    (Cabinet) pContainer.getContainerItem());
                    lMoved = true;
                }
                break;
            
            case RackInCabinet:
                
                if (pContainer.getType() == ContainerType.Cabinet) {
                    article =
                            pContainerManagerBean.linkToCabinet((Rack) article,
                                    (Cabinet) pContainer.getContainerItem(),
                                    pContainer.getPrecision());
                    lMoved = true;
                }
                break;
            
            case ToolInInstallation:
                
                if (pContainer.getType() == ContainerType.Installation) {
                    tool =
                            pContainerManagerBean.linkToInstallation(tool,
                                    (Installation) pContainer
                                            .getContainerItem());
                    lMoved = true;
                }
                break;
            
            case NotContained:
            case ErrorArticleUsedByIncorrectType:
            default:
                break;
            }
        }
        
        if (!lMoved) {
            log.warning("ContainerManager.linkTo: Illegal container");
        }
    }
    
    /**
     * Unlink the managed entity from any container
     * 
     * @param pContainerManagerBean
     *            the ContainerManagerBean to use
     */
    public void unlink(ContainerManagerBean pContainerManagerBean) {
        
        Container lContainer = getContainer();
        boolean lRemoved = false;
        
        if (lContainer == null) {
            // The entity is already not contained
            return;
        }
        
        switch (getContainerKind()) {
        
        case ArticleInTool:
            
            article =
                    pContainerManagerBean.unlinkFromTool(article,
                            (Tool) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case ArticleUsedByCabinet:
        case ArticleUsedByRack:
        case ArticleUsedBySwitch:
        case ArticleUsedByPC:
            
            article =
                    pContainerManagerBean.unlinkFromArticle(article,
                            (Article) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case ArticleUsedByInstallation:
            
            article =
                    pContainerManagerBean.unlinkFromInstallation(article,
                            (Installation) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case BoardInMotherboard:
            
            article =
                    pContainerManagerBean.unlinkFromBoard((Board) article,
                            (Board) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case BoardInPC:
            
            article =
                    pContainerManagerBean.unlinkFromPC((Board) article,
                            (PC) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case BoardInRack:
            
            article =
                    pContainerManagerBean.unlinkFromRack((Board) article,
                            (Rack) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case CabinetInInstallation:
            
            article =
                    pContainerManagerBean.unlinkFromInstallation(
                            (Cabinet) article,
                            (Installation) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case DemandInInstallation:
            
            demand = pContainerManagerBean.unlinkFromInstallation(demand);
            lRemoved = true;
            break;
        
        case DemandInTool:
            
            demand = pContainerManagerBean.unlinkFromTool(demand);
            lRemoved = true;
            break;
        
        case DemandUsedByCabinet:
            
            demand = pContainerManagerBean.unlinkFromArticle(demand);
            lRemoved = true;
            break;
        
        case RackInCabinet:
            
            article =
                    pContainerManagerBean.unlinkFromCabinet((Rack) article,
                            (Cabinet) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case ToolInInstallation:
            
            tool =
                    pContainerManagerBean.unlinkFromInstallation(tool,
                            (Installation) lContainer.getContainerItem());
            lRemoved = true;
            break;
        
        case NotContained:
        case ErrorArticleUsedByIncorrectType:
        default:
            break;
        }
        
        if (!lRemoved) {
            log.warning("ContainerManager.remove: Illegal container");
        }
    }
    
    /**
     * @return the possible containers
     */
    public ContainerType[] getPossibleContainers() {
        
        if (containedType == null) {
            return new ContainerType[] {};
        }
        
        return getPossibleContainers(containedType);
    }
    
    /**
     * Determine the list of authorized containers kind for managed entity.
     * 
     * @return a list of container kinds
     */
    private List<ContainerKind> getAuthorizedContainerKinds() {
        
        List<ContainerKind> lContKinds = new ArrayList<ContainerKind>();
        
        // Browse all possible containers
        for (ContainerType lContainerType : getPossibleContainers()) {
            
            switch (lContainerType) {
            
            case Board:
                
                // Only an other Board can be contained in a Board
                if (containedType == ContainedType.Board) {
                    
                    lContKinds.add(ContainerKind.BoardInMotherboard);
                }
                break;
            
            case Cabinet:
                
                switch (containedType) {
                
                case Rack:
                case Switch:
                    // Specific link of a Rack (or a Switch since
                    // inherits from Rack) in a Cabinet
                    lContKinds.add(ContainerKind.RackInCabinet);
                    break;
                
                case PC:
                case Various:
                    // Common link of an Article in a Cabinet
                    lContKinds.add(ContainerKind.ArticleUsedByCabinet);
                    break;
                
                case Demand:
                    // Link of the PC of a demand in a Cabinet
                    lContKinds.add(ContainerKind.DemandUsedByCabinet);
                    break;
                
                case Board:
                case Cabinet:
                case Tool:
                default:
                    break;
                }
                break;
            
            case Installation:
                
                switch (containedType) {
                
                case Cabinet:
                    // Specific link of a Cabinet in an Installation
                    lContKinds.add(ContainerKind.CabinetInInstallation);
                    break;
                
                case Board:
                case PC:
                case Rack:
                case Switch:
                case Various:
                    // Common link of an Article in an Installation
                    lContKinds.add(ContainerKind.ArticleUsedByInstallation);
                    break;
                
                case Tool:
                    // Specific link of a Tool in an Installation
                    lContKinds.add(ContainerKind.ToolInInstallation);
                    break;
                
                case Demand:
                    // Link of the PC of a Demand in an installation
                    lContKinds.add(ContainerKind.DemandInInstallation);
                    break;
                
                default:
                    break;
                }
                break;
            
            case PC:
                
                switch (containedType) {
                
                case Board:
                    // Specific link of a Board in a PC
                    lContKinds.add(ContainerKind.BoardInPC);
                    break;
                
                case Various:
                    // Common link of an Article in a PC
                    lContKinds.add(ContainerKind.ArticleUsedByPC);
                    break;
                
                case Cabinet:
                case Rack:
                case Switch:
                case PC:
                case Tool:
                case Demand:
                default:
                    break;
                }
                break;
            
            case Rack:
                
                switch (containedType) {
                
                case Board:
                    // Specific link of a Board in a Rack
                    lContKinds.add(ContainerKind.BoardInRack);
                    break;
                
                case Switch:
                case PC:
                case Various:
                    // Common link of an Article in a Rack
                    lContKinds.add(ContainerKind.ArticleUsedByRack);
                    break;
                
                case Cabinet:
                case Rack:
                case Tool:
                case Demand:
                default:
                    break;
                }
                break;
            
            case Switch:
                
                // Only a Board can be contained by a Switch
                if (containedType == ContainedType.Board) {
                    lContKinds.add(ContainerKind.ArticleUsedBySwitch);
                }
                break;
            
            case Tool:
                
                switch (containedType) {
                case Board:
                case Rack:
                case Switch:
                case PC:
                case Various:
                    // Common link of an Article in a Tool
                    lContKinds.add(ContainerKind.ArticleInTool);
                    break;
                
                case Demand:
                    // Link of the PC of a demand in a Tool
                    lContKinds.add(ContainerKind.DemandInTool);
                    break;
                
                case Cabinet:
                case Tool:
                default:
                    break;
                }
                break;
            
            default:
                break;
            }
        }
        return lContKinds;
    }
    
    /**
     * Determine the managed entity kind of container.
     * 
     * @return the container kind
     */
    private ContainerKind getContainerKind() {
        
        boolean lUsedByRelationToFind = false;
        
        // Browse all authorized container kinds
        for (ContainerKind lContainerKind : getAuthorizedContainerKinds()) {
            
            switch (lContainerKind) {
            
            case ArticleInTool:
                
                if (article.getContainerOrmTool() != null) {
                    return ContainerKind.ArticleInTool;
                }
                break;
            
            case ArticleUsedByCabinet:
                
                if (article.getContainerArticle() != null) {
                    
                    if (article.getContainerArticle() instanceof Cabinet) {
                        return ContainerKind.ArticleUsedByCabinet;
                    }
                    
                    // The usedBy relation is to be used
                    lUsedByRelationToFind = true;
                }
                break;
            
            case ArticleUsedByInstallation:
                
                if (article.getContainerInstallation() != null) {
                    return ContainerKind.ArticleUsedByInstallation;
                }
                break;
            
            case ArticleUsedByPC:
                
                if (article.getContainerArticle() != null) {
                    
                    if (article.getContainerArticle() instanceof PC) {
                        return ContainerKind.ArticleUsedByPC;
                    }
                    
                    // The usedBy relation is to be used
                    lUsedByRelationToFind = true;
                }
                break;
            
            case ArticleUsedByRack:
                
                if (article.getContainerArticle() != null) {
                    
                    if (article.getContainerArticle() instanceof Rack) {
                        return ContainerKind.ArticleUsedByRack;
                    }
                    
                    // The usedBy relation is to be used
                    lUsedByRelationToFind = true;
                }
                break;
            
            case ArticleUsedBySwitch:
                
                if (article.getContainerArticle() != null) {
                    
                    if (article.getContainerArticle() instanceof Switch) {
                        return ContainerKind.ArticleUsedBySwitch;
                    }
                    
                    // The usedBy relation is to be used
                    lUsedByRelationToFind = true;
                }
                break;
            
            case BoardInMotherboard:
                
                if (((Board) article).getMotherboard() != null) {
                    return ContainerKind.BoardInMotherboard;
                }
                break;
            
            case BoardInPC:
                
                if (((Board) article).getContainerOrmPC() != null) {
                    return ContainerKind.BoardInPC;
                }
                break;
            
            case BoardInRack:
                
                if (((Board) article).getContainerOrmRack() != null) {
                    return ContainerKind.BoardInRack;
                }
                break;
            
            case CabinetInInstallation:
                
                if (((Cabinet) article).getContainerOrmInstallation() != null) {
                    return ContainerKind.CabinetInInstallation;
                }
                break;
            
            case DemandInInstallation:
                
                if (demand.getContainerInstallation() != null) {
                    return ContainerKind.DemandInInstallation;
                }
                break;
            
            case DemandInTool:
                
                if (demand.getContainerTool() != null) {
                    return ContainerKind.DemandInTool;
                }
                break;
            
            case DemandUsedByCabinet:
                
                if (demand.getContainerArticle() != null) {
                    return ContainerKind.DemandUsedByCabinet;
                }
                break;
            
            case RackInCabinet:
                
                if (((Rack) article).getContainerOrmCabinet() != null) {
                    return ContainerKind.RackInCabinet;
                }
                break;
            
            case ToolInInstallation:
                
                if (tool.getContainerInstallation() != null) {
                    return ContainerKind.ToolInInstallation;
                }
                break;
            
            case NotContained:
            case ErrorArticleUsedByIncorrectType:
            default:
                break;
            }
        }
        
        // No container kind has been found
        
        if (lUsedByRelationToFind) {
            /*
             * The usedBy relation, which is not null for the article,
             * should be used for the article link.
             */
            return ContainerKind.ErrorArticleUsedByIncorrectType;
        }
        
        return ContainerKind.NotContained;
    }
    
    /**
     * Compute the managed entity container. <br>
     * If it is an available container, return it. <br>
     * If it is a non available container, throw an exception. <br>
     * If no container is found, return null
     * 
     * @return the managed entity container (may be null)
     */
    public Container getContainer() {
        
        switch (getContainerKind()) {
        
        case ArticleInTool:
            
            Tool lTool = article.getContainerOrmTool().getTool();
            Container lArticleToolContainer =
                    new Container(lTool, article.getContainedType());
            lArticleToolContainer.setPrecision(article.getContainerOrmTool()
                    .getComment());
            return lArticleToolContainer;
            
        case ArticleUsedByInstallation:
            
            Installation lUsedByInstallation =
                    article.getContainerInstallation();
            return new Container(lUsedByInstallation,
                    article.getContainedType());
            
        case ArticleUsedByCabinet:
        case ArticleUsedByPC:
        case ArticleUsedByRack:
        case ArticleUsedBySwitch:
            
            Article lUsedByArticle = article.getContainerArticle();
            return new Container(lUsedByArticle, article.getContainedType());
            
        case BoardInMotherboard:
            
            Board lMotherboard = ((Board) article).getMotherboard();
            return new Container(lMotherboard, ContainedType.Board);
            
        case BoardInPC:
            
            Contains_PC_Board lContainerPC =
                    ((Board) article).getContainerOrmPC();
            PC lPC = lContainerPC.getPc();
            Container lPCContainer = new Container(lPC, ContainedType.Board);
            
            String lPCPrecision =
                    Contains_PC_Board.convertSlotToString(lContainerPC
                            .getSlotNumber());
            lPCContainer.setPrecision(lPCPrecision);
            lPCContainer.setSlotNumber(lContainerPC.getSlotNumber());
            return lPCContainer;
            
        case BoardInRack:
            
            Contains_Rack_Board lContainerRack =
                    ((Board) article).getContainerOrmRack();
            Rack lRack = lContainerRack.getRack();
            Container lRackContainer =
                    new Container(lRack, ContainedType.Board);
            
            String lRackPrecision =
                    Contains_Rack_Board.convertSlotFaceToString(
                            lContainerRack.getSlotNumber(),
                            lContainerRack.getFace());
            lRackContainer.setPrecision(lRackPrecision);
            
            lRackContainer.setFace(lContainerRack.getFace());
            lRackContainer.setSlotNumber(lContainerRack.getSlotNumber());
            return lRackContainer;
            
        case CabinetInInstallation:
            
            Installation lInstallation =
                    ((Cabinet) article).getContainerOrmInstallation()
                            .getInstallation();
            
            Contains_Inst_Cabinet lContainerInstallation =
                    ((Cabinet) article).getContainerOrmInstallation();
            
            Container lInstallationContainer =
                    new Container(lInstallation, ContainedType.Cabinet);
            lInstallationContainer.setPrecision(lContainerInstallation
                    .getLetter());
            return lInstallationContainer;
            
        case DemandInInstallation:
            
            Installation lDemandInstallation =
                    demand.getContainerInstallation();
            Container lDemandInstallationContainer =
                    new Container(lDemandInstallation, ContainedType.Demand);
            return lDemandInstallationContainer;
            
        case DemandInTool:
            
            Tool lDemandTool = demand.getContainerTool();
            Container lDemandToolContainer =
                    new Container(lDemandTool, ContainedType.Demand);
            return lDemandToolContainer;
            
        case DemandUsedByCabinet:
            
            Article lDemandUsedByCabinet = demand.getContainerArticle();
            return new Container(lDemandUsedByCabinet, ContainedType.Demand);
            
        case RackInCabinet:
            
            Contains_Cabinet_Rack lContainerCabinet =
                    ((Rack) article).getContainerOrmCabinet();
            
            Cabinet lCabinet = lContainerCabinet.getCabinet();
            Container lCabinetContainer =
                    new Container(lCabinet, ContainedType.Rack);
            lCabinetContainer.setPrecision(lContainerCabinet.getRackPosition());
            return lCabinetContainer;
            
        case ToolInInstallation:
            
            Installation lToolInstallation = tool.getContainerInstallation();
            return new Container(lToolInstallation, ContainedType.Tool);
            
        case NotContained:
            return null;
            
        case ErrorArticleUsedByIncorrectType:
            throw new IllegalArgumentException(
                    "Article usedBy relation is incorrect (id: "
                            + article.getId() + ")");
            
        default:
            throw new IllegalArgumentException("Not managed container kind");
        }
    }
    
    /**
     * Determine the master container. <br>
     * When no master container is found, return the container.
     * 
     * @return the master container or the provided container
     */
    public Container getMasterContainer() {
        
        return getMasterContainer(getContainer());
    }
    
}
