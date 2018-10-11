/*
 * ------------------------------------------------------------------------
 * Class : FilterRegexSupport
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.airbus.boa.util.StringUtil;

/**
 * Support class for regular expression filters
 * 
 * @param <T>
 *            the class of object to filter
 */
public abstract class FilterRegexSupport<T> implements Serializable {
    
    private static final long serialVersionUID = 7460959698530246434L;
    
    private Map<String, Pattern> cacheRegex = new HashMap<String, Pattern>();
    
    /**
     * The Map containing the filters values:<br>
     * - the key corresponds to the name of the field<br>
     * - the value corresponds to the value of the filter for the field
     */
    protected Map<String, String> filterValues = new HashMap<String, String>();
    
    private boolean resetFilters;
    
    /**
     * Apply the filter values (<i>filterValues</i>) to the object
     * 
     * @param current
     *            the object to filter
     * @return true if the object satisfies all the filters values, else false
     */
    public abstract Boolean filterMethodRegex(T current);
    
    /**
     * Compare the provided value with the filter value.<br>
     * If filter contains at least one '*', it is considered as a regular
     * expression. Else, text is compared.<br>
     * Comparison is case and accent insensitive.
     * 
     * @param pCurrentValue
     *            the value to filter
     * @param pFilterValue
     *            the filter (regular expression) to apply
     * @return true if the value satisfies the filter, else false
     */
    protected final Boolean compare(String pCurrentValue, String pFilterValue) {
        
        // Remove accents from the value and the filter
        
        String lCurrentValue;
        if (pCurrentValue != null) {
            lCurrentValue = StringUtil.removeDiacriticalMarks(pCurrentValue);
        }
        else {
            lCurrentValue = null;
        }
        
        String lFilterValue;
        if (pFilterValue != null) {
            lFilterValue = StringUtil.removeDiacriticalMarks(pFilterValue);
        }
        else {
            lFilterValue = null;
        }
        
        if (lFilterValue != null && !lFilterValue.isEmpty()) {
            if (lCurrentValue == null) {
                if (lFilterValue.equals("!*")) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else if (lFilterValue.startsWith("!")) {
                if (lFilterValue.contains("*")) {
                    return !compareWithCacheRegex(lCurrentValue,
                            lFilterValue.substring(1));
                }
                else {
                    // Ignore case
                    return !lCurrentValue
                            .equalsIgnoreCase(lFilterValue.substring(1));
                }
            }
            else {
                if (lFilterValue.contains("*")) {
                    return compareWithCacheRegex(lCurrentValue, lFilterValue);
                }
                else {
                    // Ignore case
                    return lCurrentValue.equalsIgnoreCase(lFilterValue);
                }
            }
        }
        return true;
    }
    
    private final Boolean
            compareWithCacheRegex(String current, String textValue) {
        
        Boolean result = true;
        if (textValue != null && !textValue.isEmpty()) {
            if (current == null) {
                return false;
            }
            if (cacheRegex.containsKey(textValue)) {
                
                Pattern pattern = cacheRegex.get(textValue);
                Matcher m = pattern.matcher(current);
                result = m.matches();
                
            }
            else {
                try {
                    Pattern pattern =
                            Pattern.compile(StringUtil.parseToRegex(textValue),
                                    Pattern.CASE_INSENSITIVE
                                            + Pattern.UNICODE_CASE);
                    cacheRegex.put(textValue, pattern);
                    Matcher m = pattern.matcher(current);
                    result = m.matches();
                }
                catch (PatternSyntaxException e) {
                    // In case of pattern error, comparison is a text comparison
                    result = textValue.equalsIgnoreCase(current);
                }
            }
        }
        return result;
    }
    
    /**
     * Reset filters
     */
    public void resetFilters() {
        filterValues.clear();
        cacheRegex.clear();
        resetFilters = true;
    }
    
    /**
     * @return the filterValues
     */
    public Map<String, String> getFilterValues() {
        return filterValues;
    }
    
    /**
     * @param filterValues
     *            the filterValues to set
     */
    public void setFilterValues(Map<String, String> filterValues) {
        this.filterValues = filterValues;
    }
    
    /**
     * @return the resetFilters
     */
    public boolean isResetFilters() {
        return resetFilters;
    }
    
    /**
     * @param resetFilters
     *            the resetFilters to set
     */
    public void setResetFilters(boolean resetFilters) {
        this.resetFilters = resetFilters;
    }
    
    /**
     * Apply the <i>filterMethodRegex</i> method on each object and count how
     * many satisfy the filters values
     * 
     * @param listItems
     *            the list of objects to filter
     * @return the number of objects satisfying the filters values
     */
    public Integer countFiltered(List<T> listItems) {
        Integer count = 0;
        if (listItems == null) {
            return count;
        }
        for (T item : listItems) {
            if (filterMethodRegex(item)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Apply the <i>filterMethodRegex</i> method on each object and list the
     * ones which satisfy the filters values
     * 
     * @param listItems
     *            the list of objects to filter
     * @return the list of objects satisfying the filters values
     */
    public List<T> getFilteredList(List<T> listItems) {
        List<T> results = new ArrayList<T>();
        for (T item : listItems) {
            if (filterMethodRegex(item)) {
                results.add(item);
            }
        }
        return results;
    }
}
