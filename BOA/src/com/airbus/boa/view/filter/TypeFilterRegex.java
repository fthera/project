/*
 * ------------------------------------------------------------------------
 * Class : TypeFilterRegex
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import java.util.List;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.util.MessageBundle;

/**
 * Filter for types
 */
public class TypeFilterRegex extends FilterRegexSupport<TypeArticle> {
    
    private static final long serialVersionUID = -452137857255572601L;
    
    @Override
    public Boolean filterMethodRegex(TypeArticle current) {
        Boolean result = true;
        String filter, chaine;
        
        if (result) {
            filter = filterValues.get("classe");
            chaine =
                    MessageBundle
                            .getMessage(current.getClass().getSimpleName());
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("label");
            chaine = current.getLabel();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturer");
            chaine = current.getManufacturer();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("airbusPNCount");
            int nombre = current.getListAirbusPN().size();
            
            result = compareNumericWithOperator(nombre, filter);
        }
        
        if (result) {
            filter = filterValues.get("manufacturerPNCount");
            int nombre = current.getListManufacturerPN().size();
            
            result = compareNumericWithOperator(nombre, filter);
        }
        
        return result;
    }
    
    private Boolean compareNumericWithOperator(int number, String filter) {
        if (filter != null && !filter.trim().isEmpty()) {
            /*
             * if (filter.matches("^\\s?(>|>=|<|<=)?\\s?\\d+")){
             * // utilisation des operateurs
             * }else {
             */
            try {
                int numberFilter = Integer.parseInt(filter);
                return (number == numberFilter);
            }
            catch (NumberFormatException e) {
                return false;
            }
            /* } */
        }
        return true;
        
    }
    
    @Override
    public List<TypeArticle> getFilteredList(List<TypeArticle> listItems) {
        return super.getFilteredList(listItems);
    }
    
}
