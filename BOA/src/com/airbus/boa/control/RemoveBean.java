/*
 * ------------------------------------------------------------------------
 * Class : RemoveBean
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.LocationManager;

/**
 * Bean managing the removal of articles or installation
 */
@Stateless
@LocalBean
public class RemoveBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    /**
     * Remove the article from database
     * 
     * @param articleToRemove
     *            the article
     */
    public void removeArticle(Article articleToRemove) {
        
        for (Software soft : articleToRemove.getSoftwares()) {
            Software softwareToUpdate = softwareBean.findById(soft.getId());
            softwareToUpdate.removeEquipement(articleToRemove);
            softwareBean.merge(softwareToUpdate);
        }
        
        if (articleToRemove.getLocation() != null) {
            
            LocationManager lManager = new LocationManager(articleToRemove);
            lManager.removeFrom(locationManagerBean);
        }
        
        if (articleToRemove.getContainer() != null) {
            
            ContainerManager lContainerManager =
                    new ContainerManager(articleToRemove);
            lContainerManager.unlink(containerManagerBean);
        }
        
        articleBean.remove(articleToRemove);
    }
    
    /**
     * remove the article from database
     * 
     * @param serialNumber
     *            the article Airbus SN or Manufacturer SN
     */
    public void removeArticle(String serialNumber) {
        Article article = articleBean.findArticleBySN(serialNumber);
        removeArticle(article);
    }
    
    /**
     * Remove the installation from database
     * 
     * @param installation
     *            the installation
     */
    public void removeInstallation(Installation installation) {
        if (installation.getLocation() != null) {
            LocationManager lManager = new LocationManager(installation);
            lManager.removeFrom(locationManagerBean);
        }
        
        locationBean.removeInstallation(installation.getId());
    }
    
    /**
     * Remove the installation from database
     * 
     * @param name
     *            the installation name
     */
    public void removeInstallation(String name) {
        Installation simu = locationBean.findInstallationByName(name);
        removeInstallation(simu);
    }
    
}
