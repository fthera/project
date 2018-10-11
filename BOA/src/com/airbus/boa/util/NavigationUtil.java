/*
 * ------------------------------------------------------------------------
 * Class : NavigationUtil
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ToolBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.Various;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.service.NavigationConstants;

/**
 * Static class for navigation methods
 */
public class NavigationUtil {
    
    /**
     * Navigate to the given url.
     * 
     * @param pUrl
     *            the target url
     *            (complete or relative to the application root path)
     */
    public static void goTo(String pUrl) {
        goTo(pUrl, new HashMap<String, String>());
    }
    
    /**
     * Navigate to the given url, with the given parmaters.
     * 
     * @param pUrl
     *            the target url
     *            (complete or relative to the application root path)
     * @param pParams
     *            a dictionary listing params and their values
     */
    public static void goTo(String pUrl, Map<String, String> pParams) {
        ExternalContext context =
                FacesContext.getCurrentInstance().getExternalContext();
        String rootPath = context.getRequestContextPath();
        if (!pUrl.endsWith(".faces"))
            pUrl += ".faces";
        if (!pUrl.startsWith(rootPath))
            pUrl = rootPath + pUrl;
        
        Map<String, List<String>> parameters =
                new HashMap<String, List<String>>();
        for (String key : pParams.keySet()) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(pParams.get(key));
            parameters.put(key, list);
        }
        
        pUrl = context.encodeRedirectURL(pUrl, parameters);
        
        try {
            context.redirect(pUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Prepare display of the item (Article, Installation, Tool) and navigate to
     * the corresponding consultation page.
     * 
     * @param pItemId
     *            the item id (can be null)
     * @param pArticleBean
     *            the articleBean to use
     * @param pLocationBean
     *            the locationBean to use
     * @param pToolBean
     *            the toolBean to use
     */
    public static void doShowItem(Long pItemId, ArticleBean pArticleBean,
            LocationBean pLocationBean, ToolBean pToolBean) {
        
        if (pItemId != null) {
            String itemPage = null;
            
            // DISPLAY OF AN ARTICLE
            Article lArticle = pArticleBean.findArticleById(pItemId);
            if (lArticle != null) {
                if (lArticle instanceof PC) {
                    itemPage = NavigationConstants.PC_MANAGEMENT_PAGE;
                }
                else if (lArticle instanceof Cabinet) {
                    itemPage = NavigationConstants.CABINET_MANAGEMENT_PAGE;
                }
                else if (lArticle instanceof Switch) {
                    itemPage = NavigationConstants.SWITCH_MANAGEMENT_PAGE;
                }
                else if (lArticle instanceof Rack) {
                    itemPage = NavigationConstants.RACK_MANAGEMENT_PAGE;
                }
                else if (lArticle instanceof Board) {
                    itemPage = NavigationConstants.BOARD_MANAGEMENT_PAGE;
                }
                else if (lArticle instanceof Various) {
                    itemPage = NavigationConstants.VARIOUS_MANAGEMENT_PAGE;
                }
            }
            
            // DISPLAY OF AN INSTALLATION
            Installation lInstallation =
                    pLocationBean.findInstallationById(pItemId);
            if (lInstallation != null) {
                itemPage = NavigationConstants.INSTALLATION_MANAGEMENT_PAGE;
            }
            
            // DISPLAY OF A TOOL
            Tool lTool = pToolBean.findToolById(pItemId);
            if (lTool != null) {
                itemPage = NavigationConstants.TOOL_MANAGEMENT_PAGE;
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", pItemId.toString());
            params.put("mode", "READ");
            goTo(itemPage, params);
        }
    }
    
}
