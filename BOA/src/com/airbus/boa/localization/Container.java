/*
 * ------------------------------------------------------------------------
 * Class : Container
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Contains_Rack_Board.Face;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.application.DBConstants;

/**
 * Object representing the container of an element
 */
public class Container implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private enum PrecisionType {
        CabinetInInstallation,
        RackInCabinet,
        BoardInRack,
        BoardInPC,
        ArticleInTool,
        None
    }
    
    private ContainedType containedType;
    
    private ContainerType type;
    
    private ContainerItem containerItem;
    private String containerName;
    private String containerDetailedName;
    
    private String precision;
    
    private Integer slotNumber;
    private Face face;
    
    /**
     * Constructor
     * 
     * @param pContainerItem
     *            the container item (must be not null)
     * @param pContainedType
     *            the type of contained item
     */
    // This constructor shall not be used out of this package
    Container(ContainerItem pContainerItem, ContainedType pContainedType) {
        
        containerItem = pContainerItem;
        type = containerItem.getContainerType();
        
        containedType = pContainedType;
        
        containerName = "";
        containerDetailedName = "";
        
        precision = null;
        slotNumber = null;
        face = null;
        
        switch (type) {
        case Board:
        case Cabinet:
        case PC:
        case Rack:
        case Switch:
            if (containerItem instanceof Article) {
                Article lArticleContainer = (Article) containerItem;
                containerDetailedName =
                        Article.computeDetailedName(lArticleContainer, false);
                containerName = lArticleContainer.getName();
            }
            break;
        case Installation:
            if (containerItem instanceof Installation) {
                Installation lInstallationContainer =
                        (Installation) containerItem;
                containerDetailedName = lInstallationContainer.getName();
                containerName = lInstallationContainer.getName();
            }
            break;
        case Tool:
            if (containerItem instanceof Tool) {
                Tool lToolContainer = (Tool) containerItem;
                containerDetailedName = lToolContainer.getName();
                containerName = lToolContainer.getName();
            }
            break;
        default:
            break;
        }
    }
    
    /**
     * Constructor by copy
     * 
     * @param pContainer
     *            the container to copy into the new one (must be not null)
     */
    public Container(Container pContainer) {
        
        containerItem = pContainer.containerItem;
        type = pContainer.type;
        
        containedType = pContainer.containedType;
        
        containerName = pContainer.containerName;
        containerDetailedName = pContainer.containerDetailedName;
        
        precision = pContainer.precision;
        slotNumber = pContainer.slotNumber;
        face = pContainer.face;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getStringValue());
        builder.append(": ");
        builder.append(containerName);
        if (precision != null && !precision.isEmpty()) {
            builder.append("\n");
            builder.append("Details: ");
            builder.append(precision);
        }
        return builder.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((containerItem == null) ? 0 : containerItem
                                .hashCode());
        result =
                prime * result
                        + ((precision == null) ? 0 : precision.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Container other = (Container) obj;
        if (containerItem == null) {
            if (other.containerItem != null) {
                return false;
            }
        }
        else if (!containerItem.equals(other.containerItem)) {
            return false;
        }
        if (precision == null) {
            if (other.precision != null) {
                return false;
            }
        }
        else if (!precision.equals(other.precision)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        }
        else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
    
    private PrecisionType getPrecisionType() {
        
        switch (type) {
        
        case Installation:
            if (containedType == ContainedType.Cabinet) {
                return PrecisionType.CabinetInInstallation;
            }
            break;
        
        case Cabinet:
            if (containedType == ContainedType.Rack
                    || containedType == ContainedType.Switch) {
                return PrecisionType.RackInCabinet;
            }
            break;
        
        case Rack:
            if (containedType == ContainedType.Board) {
                return PrecisionType.BoardInRack;
            }
            break;
        
        case Switch:
            break;
        
        case Board:
            break;
        
        case PC:
            if (containedType == ContainedType.Board) {
                return PrecisionType.BoardInPC;
            }
            break;
        
        case Tool:
            switch (containedType) {
            case Board:
            case Cabinet:
            case PC:
            case Rack:
            case Switch:
            case Various:
                return PrecisionType.ArticleInTool;
            case Demand:
            case Tool:
            default:
                break;
            }
            break;
        
        default:
            break;
        }
        return PrecisionType.None;
    }
    
    /**
     * Determine if the precision field is available for linking the provided
     * item into the container
     * 
     * @return a boolean indicating if the precision field is available
     */
    public boolean isPrecisionAvailable() {
        
        switch (getPrecisionType()) {
        
        case CabinetInInstallation:
        case RackInCabinet:
        case BoardInRack:
        case BoardInPC:
        case ArticleInTool:
            return true;
            
        case None:
        default:
            return false;
        }
    }
    
    /**
     * Return the label for the precision field for linking the provided
     * item into the container
     * 
     * @return the label for the precision field
     */
    public String getPrecisionLabel() {
        
        switch (getPrecisionType()) {
        
        case CabinetInInstallation:
            return MessageBundle.getMessage("identificationLetter");
            
        case RackInCabinet:
            return MessageBundle.getMessage("position");
            
        case BoardInRack:
            return MessageBundle.getMessage("slotNumberAndPosition");
            
        case BoardInPC:
            return MessageBundle.getMessage("slotNumber");
            
        case ArticleInTool:
            return MessageBundle.getMessage("comment");
            
        case None:
        default:
            return "";
        }
    }
    
    /**
     * Return the maximum length of the precision field for linking the provided
     * item into the container
     * 
     * @param pDBConstants
     *            the DBContants instance to retrieve maximum lengths
     * @return the maximum length of the precision field, or 100
     */
    public int getPrecisionMaxLength(DBConstants pDBConstants) {
        
        switch (getPrecisionType()) {
        
        case CabinetInInstallation:
            return pDBConstants.getContainsInstCabinetLetterLength();
            
        case RackInCabinet:
            return pDBConstants.getContainsCabinetRackRackpositionLength();
            
        case BoardInRack:
            return pDBConstants.getContainsRackBoardFaceLength();
            
        case ArticleInTool:
            return pDBConstants.getContainsToolArticleCommentLength();
            
        case BoardInPC:
        case None:
        default:
            return 100;
        }
    }
    
    /**
     * Return a tool tip for the precision field for linking the provided
     * item into the container
     * 
     * @return the tool tip for the precision field, or empty string
     */
    public String getPrecisionToolTip() {
        
        switch (getPrecisionType()) {
        
        case CabinetInInstallation:
            return MessageBundle
                    .getMessage("containerPrecisionToolTipIdentificationLetter");
            
        case RackInCabinet:
            return MessageBundle
                    .getMessage("containerPrecisionToolTipRackPosition");
            
        case BoardInRack:
            return MessageBundle
                    .getMessage("containerPrecisionToolTipSlotNumberAndPosition");
            
        case BoardInPC:
        case ArticleInTool:
        case None:
        default:
            return "";
        }
    }
    
    /**
     * Validate the precision field for linking the provided item into the
     * container.<br>
     * If the precision is not valid, the error message is returned.
     * 
     * @param pPrecision
     *            the precision to validate
     * @return the error message if the precision is not valid, else an empty
     *         string
     */
    public String getPrecisionValidationError(String pPrecision) {
        
        String lPrecision;
        if (pPrecision != null) {
            lPrecision = pPrecision.trim();
        }
        else {
            lPrecision = "";
        }
        
        if (!lPrecision.isEmpty()) {
            
            switch (getPrecisionType()) {
            
            case CabinetInInstallation:
                if (lPrecision.length() != 1 || !lPrecision.matches("[A-Z]")) {
                    return MessageBundle
                            .getMessage(Constants.IDENTIFICATION_LETTER_INVALID);
                }
                break;
            
            case RackInCabinet:
                Pattern lPattern =
                        Pattern.compile(Constants.REGEX_RACK_POSITION);
                Matcher lMatcher = lPattern.matcher(lPrecision);
                
                if (!(lMatcher.matches() && (lPrecision.length() < 4))) {
                    return MessageBundle
                            .getMessage(Constants.RACK_POSITION_INVALID);
                }
                break;
            
            case BoardInRack:
                if (!lPrecision
                        .matches(Constants.REGEX_SLOTNUMBER_AND_POSITION)) {
                    return MessageBundle
                            .getMessage(Constants.SLOTNUMBER_AND_POSITION_INVALID);
                }
                break;
            
            case BoardInPC:
                if (!lPrecision
                        .matches(Constants.REGEX_SLOTNUMBER_AND_POSITION)) {
                    return MessageBundle
                            .getMessage(Constants.SLOTNUMBER_AND_POSITION_INVALID);
                }
                break;
            
            case ArticleInTool:
            case None:
            default:
                break;
            }
        }
        return "";
    }
    
    /**
     * @return the tool tip corresponding to the container (can be empty)
     */
    public String getToolTip() {
        
        if (containerItem != null && containerItem instanceof Article) {
            Article lContainerArticle = (Article) containerItem;
            return Article.computeToolTip(lContainerArticle);
        }
        else {
            return "";
        }
    }
    
    /**
     * @return the type
     */
    public ContainerType getType() {
        return type;
    }
    
    /**
     * @return the containerName
     */
    public String getContainerName() {
        return containerName;
    }
    
    /**
     * @return the containerDetailedName
     */
    public String getContainerDetailedName() {
        return containerDetailedName;
    }
    
    /**
     * @return the containerItem
     */
    public ContainerItem getContainerItem() {
        return containerItem;
    }
    
    /**
     * @return the precision
     */
    public String getPrecision() {
        return precision;
    }
    
    /**
     * @param pPrecision
     *            the precision to set
     */
    public void setPrecision(String pPrecision) {
        precision = pPrecision;
    }
    
    /**
     * @return the slotNumber
     */
    public Integer getSlotNumber() {
        return slotNumber;
    }
    
    /**
     * @param integer
     *            the slotNumber to set
     */
    public void setSlotNumber(Integer integer) {
        slotNumber = integer;
    }
    
    /**
     * @return the face
     */
    public Face getFace() {
        return face;
    }
    
    /**
     * @param face
     *            the face to set
     */
    public void setFace(Face face) {
        this.face = face;
    }
    
}
