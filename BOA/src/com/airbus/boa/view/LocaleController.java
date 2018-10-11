/*
 * ------------------------------------------------------------------------
 * Class : LocaleController
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * Controller managing the locale
 */
@ManagedBean(name = LocaleController.BEAN_NAME)
@SessionScoped
public class LocaleController extends AbstractController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "localeCtrl";

    /** User session locale */
    private Locale locale;
    
    /**
     * Constructor
     */
    public LocaleController() {
        FacesContext context = FacesContext.getCurrentInstance();
        locale = context.getApplication().getDefaultLocale();
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
    }
    
    /**
     * Change the current locale to English
     */
    public void doChangeLocaleEN() {
        locale = Locale.ENGLISH;
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(Locale.ENGLISH);
    }
    
    /**
     * Change the current locale to French
     */
    public void doChangeLocaleFR() {
        locale = Locale.FRENCH;
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(Locale.FRENCH);
    }
    
    /**
     * @return the locale ISO3Language
     */
    public String getISO3Langage() {
        return locale.getISO3Language();
    }
    
    /**
     * @return the locale
     */
    @Override
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
