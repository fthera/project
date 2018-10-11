/*
 * ------------------------------------------------------------------------
 * Class : MessageBundle
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.airbus.boa.service.Constants;

/**
 * Class managing the messages Resource Bundle
 */
public class MessageBundle {
    
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    
    /**
     * Search the message corresponding to the provided key into the provided
     * locale associated bundle and fill it with the provided arguments values. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pLocale
     *            the locale to use for searching the bundle
     * @param pKey
     *            the key of message to retrieve
     * @param pArguments
     *            the values to use for filling the message
     * @return the message corresponding to the key and containing the arguments
     */
    private static String getMessageFormat(Locale pLocale, String pKey,
            Object[] pArguments) {
        
        // Default return value
        String lResourceString = pKey;
        
        try {
            ResourceBundle lBundle =
                    ResourceBundle.getBundle(Constants.BUNDLE_NAME, pLocale);
            lResourceString = lBundle.getString(pKey);
        }
        catch (MissingResourceException e) {
            return pKey;
        }
        
        if (pArguments == null) {
            return lResourceString;
        }
        
        MessageFormat lFormat = new MessageFormat(lResourceString, pLocale);
        return lFormat.format(pArguments);
    }
    
    /**
     * Search the message corresponding to the provided key into the current
     * locale associated bundle and fill it with the provided arguments values. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pKey
     *            the key of message to retrieve
     * @param pArguments
     *            the values to use for filling the message
     * @return the message corresponding to the key and containing the arguments
     */
    public static String getMessageResource(String pKey, Object[] pArguments) {
        
        FacesContext lContext = FacesContext.getCurrentInstance();
        
        Locale lLocale = DEFAULT_LOCALE;
        
        // Try to find the current Locale
        if (lContext != null && lContext.getViewRoot() != null) {
            lLocale = lContext.getViewRoot().getLocale();
        }
        
        return getMessageFormat(lLocale, pKey, pArguments);
    }
    
    /**
     * Search the message corresponding to the provided key into the Default
     * locale associated bundle and fill it with the provided arguments values. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pKey
     *            the key of message to retrieve
     * @param pArguments
     *            the values to use for filling the message
     * @return the message corresponding to the key and containing the arguments
     */
    public static String getMessageResourceDefault(String pKey,
            Object[] pArguments) {
        
        return getMessageFormat(DEFAULT_LOCALE, pKey, pArguments);
    }
    
    /**
     * Search the message corresponding to the provided key into the current
     * locale associated bundle. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pKey
     *            the key of message to retrieve
     * @return the message corresponding to the key
     */
    public static String getMessage(String pKey) {
        
        return getMessageResource(pKey, null);
    }
    
    /**
     * Search the message corresponding to the provided key into the Default
     * locale associated bundle. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pKey
     *            the key of message to retrieve
     * @return the message corresponding to the key
     */
    public static String getMessageDefault(String pKey) {
        
        return getMessageFormat(DEFAULT_LOCALE, pKey, null);
    }
    
    /**
     * Search the message corresponding to the provided key into the French
     * locale associated bundle. <br>
     * If the key is not found in bundle, return it instead of a message.
     * 
     * @param pKey
     *            the key of message to retrieve
     * @return the message corresponding to the key
     */
    public static String getMessageFrench(String pKey) {
        
        return getMessageFormat(Locale.FRENCH, pKey, null);
    }
    
    /**
     * @return a three-letter abbreviation for current locale's language.
     */
    public static String getIso3Langage() {
        
        FacesContext lContext = FacesContext.getCurrentInstance();
        
        Locale lLocale = DEFAULT_LOCALE;
        if (lContext != null && lContext.getViewRoot() != null) {
            lLocale = lContext.getViewRoot().getLocale();
        }
        return lLocale.getISO3Language();
    }
    
    /**
     * Search the messages corresponding to the provided keys into the current
     * locale associated bundle. <br>
     * Return a Map containing as keys the provided keys and as values the
     * message corresponding to the key. <br>
     * When a key is not found in a bundle, the associated value is null.
     * 
     * @param pKeys
     *            the list of keys of messages to retrieve
     * @return a map containing the provided keys and all corresponding found
     *         messages
     */
    public static Map<String, String> getAllMessages(List<String> pKeys) {
        
        Map<String, String> lMap = new HashMap<String, String>();
        
        if (pKeys == null) {
            return lMap;
        }
        
        try {
            for (String lKey : pKeys) {
                try {
                    // Put found message associated to the current key
                    lMap.put(lKey, getMessage(lKey));
                } catch (Exception e) {
                    // Null message is added when key is not found
                    lMap.put(lKey, null);
                }
            }
        } catch (Exception e) {
        }
        return lMap;
    }
    
    /**
     * Search the messages corresponding to the provided keys into the default
     * locale associated bundle. <br>
     * Return a Map containing as keys the provided keys and as values the
     * message corresponding to the key. <br>
     * When a key is not found in a bundle, the associated value is null.
     * 
     * @param pKeys
     *            the list of keys of messages to retrieve
     * @return a map containing the provided keys and all corresponding found
     *         messages
     */
    public static Map<String, String> getAllDefaultMessages(List<String> pKeys) {
        
        Map<String, String> lMap = new HashMap<String, String>();
        
        if (pKeys == null) {
            return lMap;
        }
        
        try {
            for (String lKey : pKeys) {
                try {
                    // Put found message associated to the current key
                    lMap.put(lKey, getMessageDefault(lKey));
                }
                catch (Exception e) {
                    // Null message is added when key is not found
                    lMap.put(lKey, null);
                }
            }
        }
        catch (Exception e) {
        }
        return lMap;
    }
    
    /**
     * Search the messages corresponding to the provided keys into the french
     * locale associated bundle. <br>
     * Return a Map containing as keys the provided keys and as values the
     * message corresponding to the key. <br>
     * When a key is not found in a bundle, the associated value is null.
     * 
     * @param pKeys
     *            the list of keys of messages to retrieve
     * @return a map containing the provided keys and all corresponding found
     *         messages
     */
    public static Map<String, String> getAllFrenchMessages(List<String> pKeys) {
        
        Map<String, String> lMap = new HashMap<String, String>();
        
        if (pKeys == null) {
            return lMap;
        }
        
        try {
            for (String lKey : pKeys) {
                try {
                    // Put found message associated to the current key
                    lMap.put(lKey, getMessageFrench(lKey));
                } catch (Exception e) {
                    // Null message is added when key is not found
                    lMap.put(lKey, null);
                }
            }
        } catch (Exception e) {
        }
        return lMap;
    }
    
}
