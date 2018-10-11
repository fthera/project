/*
 * ------------------------------------------------------------------------
 * Class : StockForType
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.stock;

import java.util.ArrayList;
import java.util.List;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.entity.article.type.TypeArticle;

/**
 * Class used to compute the stock quantities for a given TypeArticle
 */
public class StockForType extends Stock {
    
    private TypeArticle type;
    private List<StockForPN> stockPNs = new ArrayList<StockForPN>();

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
     * @param pType
     *            the TypeArticle for which the stock is computed
     */
    public StockForType(ArticleBean pArticleBean, HistoryBean pHistoryBean,
            LocationBean pLocationBean, TypeArticle pType) {
        type = pType;
        setRepartitionMap(pArticleBean.computeReparitionMap(pType, null, null),
                pLocationBean);
        setQuantityUse(pArticleBean.computeInUseQuantity(pType, null, null));
        setQuantityStock(
                pArticleBean.computeInStockQuantity(pType, null, null));
        setQuantityOtherOperational(pArticleBean
                .computeOtherOperationalQuantity(pType, null, null));
        setOtherQuantities(
                pArticleBean.computeOtherQuantities(pType, null, null));
        breakdowns = pHistoryBean.findAllBreakdowns(pType, null);
        scrappings = pHistoryBean.findAllScrappings(pType, null);
    }
    
    /**
     * @return the type
     */
    public TypeArticle getType() {
        return type;
    }
    
    /**
     * @param pType
     *            the type to set
     */
    public void setType(TypeArticle pType) {
        this.type = pType;
    }
    
    /**
     * @return the stockPNs
     */
    public List<StockForPN> getStockPNs() {
        return stockPNs;
    }
    
    /**
     * @param pStockPNs
     *            the stockPNs to set
     */
    public void setStockPNs(List<StockForPN> pStockPNs) {
        this.stockPNs = pStockPNs;
    }
}
