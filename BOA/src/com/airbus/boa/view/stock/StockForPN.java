/*
 * ------------------------------------------------------------------------
 * Class : StockForPN
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.stock;

/**
 * Class used to compute the stock quantities for a given PN
 * (Airbus PN or Manufacturer PN).
 */
import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.HistoryBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.type.TypeArticle;

public class StockForPN extends Stock {
    
    private TypeArticle type;
    private PN pn;
    
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
     *            the TypeArticle associated with the PN
     * @param pPN
     *            the PN for which the stock is computed
     */
    public StockForPN(ArticleBean pArticleBean, HistoryBean pHistoryBean,
            LocationBean pLocationBean, TypeArticle pType, PN pPN) {
        type = pType;
        pn = pPN;
        setRepartitionMap(pArticleBean.computeReparitionMap(pType, pPN, null),
                pLocationBean);
        setQuantityUse(pArticleBean.computeInUseQuantity(pType, pPN, null));
        setQuantityStock(pArticleBean.computeInStockQuantity(pType, pPN, null));
        setQuantityOtherOperational(
                pArticleBean.computeOtherOperationalQuantity(pType, pPN, null));
        setOtherQuantities(
                pArticleBean.computeOtherQuantities(pType, pPN, null));
        breakdowns = pHistoryBean.findAllBreakdowns(pType, pPN);
        scrappings = pHistoryBean.findAllScrappings(pType, pPN);
    }
    
    /**
     * @return the type
     */
    public TypeArticle getType() {
        return type;
    }
    
    /**
     * @param type
     *            the type to set
     */
    public void setType(TypeArticle type) {
        this.type = type;
    }
    
    /**
     * @return the pn
     */
    public PN getPn() {
        return pn;
    }
    
    /**
     * @param pn
     *            the pn to set
     */
    public void setPn(PN pn) {
        this.pn = pn;
    }
}
