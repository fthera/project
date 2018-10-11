/*
 * ------------------------------------------------------------------------
 * Class : SearchObsoController
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.obso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.Utils;

@ManagedBean(name = SearchObsoController.BEAN_NAME)
@SessionScoped
public class SearchObsoController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = -1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "searchObsoController";

    @EJB
    ObsolescenceBean obsoBean;
    
    @EJB
    LocationBean locationBean;
    
    private List<ObsolescenceData> searchResults;
    
    private Map<String, Object> inputTextField = new HashMap<String, Object>();
    private Map<String, List<SelectItem>> filterChoices =
            new HashMap<String, List<SelectItem>>();
    
    /**
     * Initialize the filter choices for all the needed columns
     */
    @PostConstruct
    public void init() {
        ObsolescenceController lController =
                findBean(ObsolescenceController.class);
        filterChoices.put("actionIds", lController.getSelectAction());
        filterChoices.put("strategyIds", lController.getSelectStrategy());
        filterChoices.put("manufacturerStatusIds",
                lController.getSelectManufacturerStatus());
        filterChoices.put("airbusStatusIds",
                lController.getSelectAirbusStatus());
        filterChoices.put("consultPeriodIds",
                lController.getSelectConsultPeriod());
        doResetCriteria();
    }
    
    public void doResetCriteria() {
        inputTextField.clear();
        for (String lKey : filterChoices.keySet()) {
            inputTextField.put(lKey, getChoicesValues(lKey));
        }
    }
    
    public void doSearch() {
        setSearchResults(obsoBean.advanceSearch(getFilteredInputTextField()));
        if (getSearchResults().isEmpty()) {
            Utils.addFacesMessage(NavigationConstants.SEARCH_OBSO_ERROR_ID,
                    MessageBundle.getMessage(Constants.QUERY_OBSO_NO_RESULTS));
        }
        else if (getSearchResults().size() == 1) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", getSearchResults().get(0).getId().toString());
            params.put("mode", "READ");
            NavigationUtil.goTo(NavigationConstants.OBSO_MANAGEMENT_PAGE,
                    params);
        }
        else {
            String msg =
                    MessageBundle.getMessageResource(
                            Constants.QUERY_OBSO_NUMBER_RESULTS,
                            new Object[] { getSearchResults().size() });
            log.info(msg);
            
            ReportingObsoController controller =
                    findBean(ReportingObsoController.class);
            controller.setSearchResults(getSearchResults());
            NavigationUtil.goTo(NavigationConstants.OBSO_RESULT_PAGE);
        }
    }
    
    public Map<String, Object> getInputTextField() {
        return inputTextField;
    }
    
    public void setInputTextField(Map<String, Object> inputTextField) {
        this.inputTextField = inputTextField;
    }
    
    /**
     * @return the filtered inputTextField
     */
    public Map<String, Object> getFilteredInputTextField() {
        Map<String, Object> lFilteredInputTextField =
                new HashMap<String, Object>(inputTextField);
        for (String lKey : filterChoices.keySet()) {
            if (!isFiltered(lKey)) {
                lFilteredInputTextField.remove(lKey);
            }
        }
        return lFilteredInputTextField;
    }
    
    public List<SelectItem> getListInstallations() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        for (Installation installation : locationBean.findAllInstallation()) {
            result.add(new SelectItem(installation.getId(), installation
                    .getName()));
        }
        return result;
    }
    
    public List<ObsolescenceData> getSearchResults() {
        return searchResults;
        
    }
    
    public void setSearchResults(List<ObsolescenceData> searchResult) {
        searchResults = searchResult;
    }
    
    /**
     * @param pField
     *            the field name for which we want the filter choices
     * @return the filter choices for the given source and field
     */
    public List<SelectItem> getFilterChoices(String pField) {
        return filterChoices.get(pField);
    }
    
    /**
     * @param pField
     *            the field name for which we want the choice values
     * @return the choice values for the given source and field
     */
    public Object[] getChoicesValues(String pField) {
        List<SelectItem> lList = getFilterChoices(pField);
        int lSize = lList.size();
        Object[] lValues = new Object[lSize];
        for (int i = 0; i < lList.size(); i++) {
            lValues[i] = lList.get(i).getValue();
        }
        return lValues;
    }
    
    /**
     * Check if the given field is filtered.
     * 
     * @param pField
     *            the field name for which the test shall be done
     * @return true if the field is filtered, false otherwise
     */
    public boolean isFiltered(String pField) {
        Object[] lFilteredValues = (Object[]) inputTextField.get(pField);
        Object[] lChoicesValues = getChoicesValues(pField);
        if (lFilteredValues != null
                && lFilteredValues.length < lChoicesValues.length) {
            return true;
        }
        return false;
    }
    
}
