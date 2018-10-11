/*
 * ------------------------------------------------------------------------
 * Class : StockForSoftware
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.stock;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.UseState;

/**
 * Class used to compute the stock quantities for a given Software
 */
public class StockForSoftware extends Stock {
    
    private Software software;
    
    /**
     * Class constructor
     * 
     * @param pArticleBean
     *            the article bean, to retrieve relevant information from the
     *            database
     * @param pHistoryBean
     *            the history bean, to retrieve relevant information from the
     *            database
     * @param pLocationBean
     *            the location bean, to retrieve relevant information from the
     *            database
     * @param pSoftware
     *            the Software for which the stock is computed
     */
    public StockForSoftware(ArticleBean pArticleBean, HistoryBean pHistoryBean,
            LocationBean pLocationBean, Software pSoftware) {
        software = pSoftware;
        setRepartitionMap(
                pArticleBean.computeReparitionMap(null, null, pSoftware),
                pLocationBean);
        setQuantityUse(
                pArticleBean.computeInUseQuantity(null, null, pSoftware));
        setQuantityStock(
                pArticleBean.computeInStockQuantity(null, null, pSoftware));
        setQuantityOtherOperational(pArticleBean
                .computeOtherOperationalQuantity(null, null, pSoftware));
        setOtherQuantities(
                pArticleBean.computeOtherQuantities(null, null, pSoftware));
    }
    
    /**
     * @return the software
     */
    public Software getSoftware() {
        return software;
    }
    
    /**
     * @param pSoftware
     *            the software to set
     */
    public void setSoftware(Software pSoftware) {
        this.software = pSoftware;
    }
    
    /**
     * @return the details of the quantityOther
     */
    @Override
    public String getOtherDescription() {
        return UseState.ToBeRemoved + " " + ArticleState.Operational + ": "
                + quantityOtherOperational + "<br/>\n" + ArticleState.OutOfOrder
                + ": " + quantityOutOfOrder + "<br/>\n" + ArticleState.Retrofit
                + ": " + quantityRetrofit + "<br/>\n" + ArticleState.ToBeTested
                + ": " + quantityToBeTested + "<br/>\n" + ArticleState.Unusable
                + ": " + quantityUnusable + "<br/>\n";
    }
}
