/*
 * ------------------------------------------------------------------------
 * Class : Stock
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.stock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.util.MessageBundle;

/**
 * Base class used to compute the stock quantities
 */
public class Stock {
    
    protected long quantityOutOfOrder = 0;
    protected long quantityRetrofit = 0;
    protected long quantityToBeTested = 0;
    protected long quantityUnusable = 0;
    protected long quantityUse = 0;
    protected long quantityStock = 0;
    protected long quantityOtherOperational = 0;
    
    protected Map<Installation, Long> repartitionMap = null;
    
    protected List<FieldModification> breakdowns =
            new ArrayList<FieldModification>();
    protected List<FieldModification> scrappings =
            new ArrayList<FieldModification>();

    /**
     * @param pStock
     *            a map describing the stock
     */
    public void setOtherQuantities(Map<String, Long> pStock) {
        for (String key : pStock.keySet()) {
            ArticleState state = ArticleState.getEnumValue(key);
            switch (state) {
            case Operational:
                break;
            case OutOfOrder:
                quantityOutOfOrder = pStock.get(key);
                break;
            case Retrofit:
                quantityRetrofit = pStock.get(key);
                break;
            case ToBeTested:
                quantityToBeTested = pStock.get(key);
                break;
            case Unusable:
                quantityUnusable = pStock.get(key);
                break;
            }
        }
    }
    
    /**
     * @param pQuantityUse
     *            the quantity in use
     */
    public void setQuantityUse(long pQuantityUse) {
        quantityUse = pQuantityUse;
    }
    
    /**
     * @param pQuantityUse
     *            the quantity in stock
     */
    public void setQuantityStock(long pQuantityStock) {
        quantityStock = pQuantityStock;
    }
    
    /**
     * @param pQuantityOtherOperational
     *            the quantity of operational articles not in use or in stock
     */
    public void setQuantityOtherOperational(long pQuantityOtherOperational) {
        quantityOtherOperational = pQuantityOtherOperational;
    }
    
    /**
     * @return the quantityStock
     */
    public long getQuantityStock() {
        return quantityStock;
    }
    
    /**
     * @return the quantityUse
     */
    public long getQuantityUse() {
        return quantityUse;
    }
    
    /**
     * @return the quantityOtherOperational
     */
    public long getQuantityOtherOperational() {
        return quantityOtherOperational;
    }
    
    /**
     * @return the spare
     */
    public String getSpare() {
        if (quantityUse == 0) {
            return "NA";
        }
        else {
            return String.format("%.2f",
                    (((double) quantityStock) * 100.0 / (double) quantityUse));
        }
    }

    /**
     * @return the quantityOther
     */
    public long getQuantityOther() {
        return quantityOtherOperational + quantityOutOfOrder + quantityRetrofit
                + quantityToBeTested + quantityUnusable;
    }

    /**
     * @return the quantityTotal
     */
    public long getQuantityTotal() {
        return quantityOutOfOrder + quantityRetrofit + quantityToBeTested
                + quantityUnusable + quantityStock + quantityUse
                + quantityOtherOperational;
    }
    
    /**
     * @return the details of the quantityOther
     */
    public String getOtherDescription() {
        String lDescription = "";
        if (quantityOtherOperational > 0) {
            lDescription +=
                    ArticleState.Operational + " (" + UseState.Loaned + "/"
                            + UseState.ToBeRemoved + "): "
                    + quantityOtherOperational + "<br/>";
        }
        if (quantityOutOfOrder > 0) {
            lDescription += ArticleState.OutOfOrder + ": " + quantityOutOfOrder
                    + "<br/>";
        }
        if (quantityRetrofit > 0) {
            lDescription +=
                    ArticleState.Retrofit + ": " + quantityRetrofit + "<br/>";
        }
        if (quantityToBeTested > 0) {
            lDescription += ArticleState.ToBeTested + ": " + quantityToBeTested
                    + "<br/>";
        }
        if (quantityUnusable > 0) {
            lDescription +=
                    ArticleState.Unusable + ": " + quantityUnusable + "<br/>";
        }
        return lDescription;
    }
    
    /**
     * @return the repartitionMap keys
     */
    public Set<Installation> getRepartitionMapKeys() {
        return repartitionMap.keySet();
    }
    
    /**
     * @return the repartitionMap
     */
    public Map<Installation, Long> getRepartitionMap() {
        return repartitionMap;
    }
    
    /**
     * @param pRepartitionMap
     *            a map associating installations id with a quantity
     * @param pLocationBean
     *            the location bean, to retrieve installations
     */
    public void setRepartitionMap(Map<Long, Long> pRepartitionMap,
            LocationBean pLocationBean) {
        repartitionMap = new TreeMap<Installation, Long>();
        for (Long key : pRepartitionMap.keySet()) {
            repartitionMap.put(pLocationBean.findInstallationById(key),
                    pRepartitionMap.get(key));
        }
    }

    /**
     * @return the breakdowns
     */
    public List<FieldModification> getBreakdowns() {
        return breakdowns;
    }

    /**
     * @param pBreakdowns
     *            the breakdown list to set
     */
    public void setBreakdowns(List<FieldModification> pBreakdowns) {
        breakdowns = new ArrayList<FieldModification>(pBreakdowns);
    }

    /**
     * @return the scrappings
     */
    public List<FieldModification> getScrappings() {
        return scrappings;
    }

    /**
     * @param pScrappings
     *            the scrapping list to set
     */
    public void setScrappings(List<FieldModification> pScrappings) {
        scrappings = new ArrayList<FieldModification>(pScrappings);
    }
    
    /**
     * @return the LineChartModel shwoing the stock evolutions
     */
    public LineChartModel getStockEvolutionModel() {
        LineChartModel stockEvolutionModel = new LineChartModel();
        
        Date lTodayDate = new Date();
        SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy");
        
        // Series initialization
        ChartSeries lBreakdownsSeries = new ChartSeries();
        lBreakdownsSeries.setLabel(MessageBundle.getMessage("Breakdowns"));
        TreeMap<String, Integer> lBreakdownsMap =
                new TreeMap<String, Integer>();
        ChartSeries lScrappingsSeries = new ChartSeries();
        lScrappingsSeries.setLabel(MessageBundle.getMessage("Scrappings"));
        TreeMap<String, Integer> lScrappingsMap =
                new TreeMap<String, Integer>();
        
        // Initialize X axis values (years)
        List<Date> lDates = new ArrayList<Date>();
        for (FieldModification lFM : breakdowns) {
            lDates.add(lFM.getDate());
        }
        for (FieldModification lFM : scrappings) {
            lDates.add(lFM.getDate());
        }
        lDates.add(lTodayDate);
        Calendar lCalendar = Calendar.getInstance();
        lCalendar.setTime(Collections.min(lDates));
        while (lCalendar.getTime().compareTo(lTodayDate) <= 0) {
            String lYear = lDateFormat.format(lCalendar.getTime());
            lBreakdownsMap.put(lYear, 0);
            lScrappingsMap.put(lYear, 0);
            lCalendar.add(Calendar.YEAR, 1);
        }
        lCalendar.setTime(lTodayDate);
        String lCurrentYear = lDateFormat.format(lCalendar.getTime());
        lBreakdownsMap.put(lCurrentYear, 0);
        lScrappingsMap.put(lCurrentYear, 0);
        
        // Populates maps and computes breakdowns and scrappings nummbers
        // during the selected period
        for (FieldModification lFM : breakdowns) {
            Date lDate = lFM.getDate();
            String lYear = lDateFormat.format(lDate);
            lBreakdownsMap.put(lYear, lBreakdownsMap.get(lYear) + 1);
        }
        for (FieldModification lFM : scrappings) {
            Date lDate = lFM.getDate();
            String lYear = lDateFormat.format(lDate);
            lScrappingsMap.put(lYear, lScrappingsMap.get(lYear) + 1);
        }
        
        // Set the Y axis values
        for (Map.Entry<String, Integer> lEntry : lBreakdownsMap.entrySet()) {
            lBreakdownsSeries.set(lEntry.getKey(), lEntry.getValue());
        }
        for (Map.Entry<String, Integer> lEntry : lScrappingsMap.entrySet()) {
            lScrappingsSeries.set(lEntry.getKey(), lEntry.getValue());
        }
        
        // Finalize the graph
        stockEvolutionModel.addSeries(lBreakdownsSeries);
        stockEvolutionModel.addSeries(lScrappingsSeries);
        stockEvolutionModel
                .setTitle(MessageBundle.getMessage("stockEvolution"));
        stockEvolutionModel.setLegendPosition("e");
        stockEvolutionModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
        stockEvolutionModel.setDatatipFormat("%2$d");
        stockEvolutionModel.getAxes().put(AxisType.X,
                new CategoryAxis(MessageBundle.getMessage("years")));
        Axis yAxis = stockEvolutionModel.getAxis(AxisType.Y);
        yAxis.setLabel(MessageBundle.getMessage("number"));
        yAxis.setMin(0);
        int max = 9;
        if (!lBreakdownsMap.isEmpty()) {
            max = Math.max(max, Collections.max(lBreakdownsMap.values()));
        }
        if (!lScrappingsMap.isEmpty()) {
            max = Math.max(max, Collections.max(lScrappingsMap.values()));
        }
        yAxis.setMax(max + 1);
        
        return stockEvolutionModel;
    }
    
    /**
     * @param pFrom
     *            origin date
     * @param pTo
     *            target date
     * @return the number of breakdown between the given dates
     */
    public Integer getBreakdownsNumber(Date pFrom, Date pTo) {
        Integer lBreakdownsNumber = 0;
        for (FieldModification lFM : breakdowns) {
            Date lDate = lFM.getDate();
            if ((pFrom != null && lDate.compareTo(pFrom) > 0)
                    || pFrom == null) {
                if ((pTo != null && lDate.compareTo(pTo) < 0) || pTo == null) {
                    lBreakdownsNumber++;
                }
            }
        }
        return lBreakdownsNumber;
    }
    
    /**
     * @param pFrom
     *            origin date
     * @param pTo
     *            target date
     * @return the number of scrappings between the given dates
     */
    public Integer getScrapingsNumber(Date pFrom, Date pTo) {
        Integer lScrapingsNumber = 0;
        for (FieldModification lFM : scrappings) {
            Date lDate = lFM.getDate();
            if ((pFrom != null && lDate.compareTo(pFrom) > 0)
                    || pFrom == null) {
                if ((pTo != null && lDate.compareTo(pTo) < 0) || pTo == null) {
                    lScrapingsNumber++;
                }
            }
        }
        return lScrapingsNumber;
    }
}
