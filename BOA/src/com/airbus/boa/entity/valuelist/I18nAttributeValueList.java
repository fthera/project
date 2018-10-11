/*
 * ------------------------------------------------------------------------
 * Class : I18nAttributeValueList
 * Copyright 2017 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.valuelist;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation super class for entity: I18nAttributeValueList
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class I18nAttributeValueList extends AttributeValueList {
    
    private static final long serialVersionUID = 1L;
    
    /** The French value column name */
    public static final String FRENCHVALUE_COLUMN_NAME = "FRENCHVALUE";
    
    /** Value in French language */
    @Column
    protected String frenchValue;

    /**
     * Get the value of the attribute in the current language
     * 
     * @return the default value if the current language is English,<br>
     *         the French value if the current language is French,<br>
     *         the default value surrounded by "??" if the current language is
     *         not recognized
     */
    @Override
    public String getLocaleValue() {
        
        String result = "??" + defaultValue + "??";
        
        String langageCode = MessageBundle.getIso3Langage();
        
        if (langageCode != null) {
            if (langageCode.equals(Locale.ENGLISH.getISO3Language())) {
                result = defaultValue;
            }
            else if (langageCode.equals(Locale.FRENCH.getISO3Language())) {
                result = frenchValue;
            }
        }
        
        return result;
    }
    
    /**
     * @return the frenchValue
     */
    public String getFrenchValue() {
        return frenchValue;
    }
    
    /**
     * @param pFrenchValue
     *            the frenchValue to set
     */
    public void setFrenchValue(String pFrenchValue) {
        frenchValue = pFrenchValue;
    }
    
}
