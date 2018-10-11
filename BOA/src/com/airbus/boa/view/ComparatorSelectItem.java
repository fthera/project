/*
 * ------------------------------------------------------------------------
 * Class : ComparatorSelectItem
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.Comparator;

import javax.faces.model.SelectItem;

/**
 * @author ng0057cf
 */
public class ComparatorSelectItem implements Comparator<SelectItem> {
    
    /*
     * (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(SelectItem arg0, SelectItem arg1) {
        
        if (arg0.getLabel() == null && arg1.getLabel() == null) {
            return 0;
        }
        
        String label0 = arg0.getLabel();
        if (label0 == null) {
            return -1;
        }
        
        String label1 = arg1.getLabel();
        if (label1 == null) {
            return 1;
        }
        
        return label0.compareTo(label1);
        
    }
    
}
