/*
 * ------------------------------------------------------------------------
 * Class : ContainerFactory
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.localization;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.LocalizationException.LocalizationError;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Factory managing the container
 */
@Stateless
public class ContainerFactory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private ToolBean toolBean;
    
    @EJB
    private LocationBean locationBean;
    
    /**
     * Constructor
     */
    public ContainerFactory() {
    }
    
    /**
     * Generate the container based on the provided values
     * 
     * @param pContainerType
     *            the container type
     * @param pName
     *            the container name
     * @param pPrecision
     *            the container precision string
     * @param pContainedType
     *            the type of item to be contained
     * @return the created container or null
     * @throws LocalizationException
     *             when the container is not valid
     */
    public Container generateContainer(ContainerType pContainerType,
            String pName, String pPrecision, ContainedType pContainedType)
            throws LocalizationException {
        
        if (pContainerType == null || pName == null) {
            return null;
        }
        
        Article lArticle = null;
        Container lContainer = null;
        boolean lIsGenerated = false;
        
        switch (pContainerType) {
        case Installation:
            Installation lInstallation =
                    locationBean.findInstallationByName(pName);
            if (lInstallation != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lInstallation, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        case Board:
            lArticle = articleBean.findBoardByASNOrMSN(pName);
            if (lArticle != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lArticle, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        case Cabinet:
            
            lArticle = articleBean.findCabinetByDesignationOrSN(pName);
            if (lArticle != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lArticle, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        case Rack:
            lArticle = articleBean.findRackByDesignationOrSN(pName);
            if (lArticle != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lArticle, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        case Switch:
            lArticle = articleBean.findSwitchByASNOrMSN(pName);
            if (lArticle != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lArticle, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        case PC:
            PC lPc = pcBean.findPCbyName(pName);
            if (lPc != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType, lPc,
                                pPrecision);
                lIsGenerated = true;
            }
            else {
                
                lPc = pcBean.findPCbySN(pName);
                if (lPc != null) {
                    lContainer =
                            ContainerManager.generateContainer(pContainedType,
                                    lPc, pPrecision);
                    lIsGenerated = true;
                }
            }
            break;
        
        case Tool:
            Tool lTool = toolBean.findToolByName(pName);
            if (lTool != null) {
                lContainer =
                        ContainerManager.generateContainer(pContainedType,
                                lTool, pPrecision);
                lIsGenerated = true;
            }
            break;
        
        default:
            return null;
        }
        
        if (!lIsGenerated) {
            String lMsg =
                    MessageBundle.getMessageResource(
                            Constants.CONTAINER_NOT_FOUND,
                            new String[] { pName });
            throw new LocalizationException(
                    LocalizationError.ContainerNotFound, lMsg);
        }
        
        return lContainer;
    }
    
    /**
     * Generate the container based on the provided values
     * 
     * @param pContainerType
     *            the container type
     * @param pContainerId
     *            the container id
     * @param pPrecision
     *            the container precision string (can be null)
     * @param pContainedType
     *            the type of item to be contained
     * @return the created container or null
     * @throws LocalizationException
     *             when the container is not valid
     */
    public Container generateContainer(ContainerType pContainerType,
            Long pContainerId, String pPrecision, ContainedType pContainedType)
            throws LocalizationException {
        
        if (pContainerType == null || pContainerId == null) {
            return null;
        }
        
        switch (pContainerType) {
        case Installation:
            Installation lInstallation =
                    locationBean.findInstallationById(pContainerId);
            if (lInstallation != null) {
                return ContainerManager.generateContainer(pContainedType,
                        lInstallation, pPrecision);
            }
            return null;
            
        case Board:
        case Cabinet:
        case Rack:
        case Switch:
        case PC:
            Article lArticle = articleBean.findArticleById(pContainerId);
            if (lArticle != null) {
                return ContainerManager.generateContainer(pContainedType,
                        lArticle, pPrecision);
            }
            return null;
            
        case Tool:
            Tool lTool = toolBean.findToolById(pContainerId);
            if (lTool != null) {
                return ContainerManager.generateContainer(pContainedType,
                        lTool, pPrecision);
            }
            return null;
            
        default:
            return null;
        }
    }
    
}
