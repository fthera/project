/*
 * ------------------------------------------------------------------------
 * Class : StringUtil
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static class containing methods for handling String
 */
public class StringUtil {
    
    private StringUtil() {
    }
    
    /**
     * Return a copy of the string able to be interpreted by SQL:<br>
     * - replace all '\' by '\\' (escape the wildcard behavior)<br>
     * - replace all '_' by '\_' (escape the wildcard behavior)<br>
     * - replace all '%' by '\%' (escape the wildcard behavior)<br>
     * - replace all '*' by '%' (keep the wildcard behavior)<br>
     * 
     * @param pString
     *            the string to parse
     * @return the modified string
     */
    public static String parseToSQLRegex(String pString) {
        
        String lModifiedString = pString;
        
        // Escape '\' character
        lModifiedString =
                Pattern.compile("\\\\").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\\\"));
        
        // Escape '_' character
        lModifiedString =
                Pattern.compile("_").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\_"));
        
        // Escape '%' character
        lModifiedString =
                Pattern.compile("%").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\%"));
        
        // Replace one or more '*' character by the SQL wildcard '%'
        lModifiedString = lModifiedString.replaceAll("\\*+", "%");
        
        if (lModifiedString.equals("%")) {
            lModifiedString = "%_%";
        }
        
        return lModifiedString;
    }
    
    /**
     * Return a copy of the string able to be interpreted by the Matcher:<br>
     * - replace all '\' by "\\" (escape the wildcard behavior)
     * - replace all '[' by "\[" (escape the wildcard behavior)
     * - replace all ']' by "\]" (escape the wildcard behavior)
     * - replace all '.' by "\." (escape the wildcard behavior)
     * - replace all '^' by "\^" (escape the wildcard behavior)
     * - replace all '$' by "\$" (escape the wildcard behavior)
     * - replace all '?' by "\?" (escape the wildcard behavior)
     * - replace all '+' by "\+" (escape the wildcard behavior)
     * - replace all '{' by "\{" (escape the wildcard behavior)
     * - replace all '}' by "\}" (escape the wildcard behavior)
     * - replace all '|' by "\|" (escape the wildcard behavior)
     * - replace all '(' by "\(" (escape the wildcard behavior)
     * - replace all ')' by "\)" (escape the wildcard behavior)
     * - replace all '*' by ".*" (keep the wildcard behavior)
     * 
     * @param pString
     *            the string to parse
     * @return the modified string
     */
    public static String parseToRegex(String pString) {
        
        String lModifiedString = pString;
        
        // Escape '\' character
        lModifiedString =
                Pattern.compile("\\\\").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\\\"));
        
        // Escape '[' character
        lModifiedString =
                Pattern.compile("\\[").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\["));
        
        // Escape ']' character
        lModifiedString =
                Pattern.compile("\\]").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\]"));
        
        // Escape '.' character
        lModifiedString =
                Pattern.compile("\\.").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\."));
        
        // Escape '^' character
        lModifiedString =
                Pattern.compile("\\^").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\^"));
        
        // Escape '$' character
        lModifiedString =
                Pattern.compile("\\$").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\$"));
        
        // Escape '?' character
        lModifiedString =
                Pattern.compile("\\?").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\?"));
        
        // Escape '+' character
        lModifiedString =
                Pattern.compile("\\+").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\+"));
        
        // Escape '{' character
        lModifiedString =
                Pattern.compile("\\{").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\{"));
        
        // Escape '}' character
        lModifiedString =
                Pattern.compile("\\}").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\}"));
        
        // Escape '|' character
        lModifiedString =
                Pattern.compile("\\|").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\|"));
        
        // Escape '(' character
        lModifiedString =
                Pattern.compile("\\(").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\("));
        
        // Escape ')' character
        lModifiedString =
                Pattern.compile("\\)").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("\\)"));
        
        // Replace one or more '*' character by the Java Matcher wildcard '.*'
        lModifiedString = lModifiedString.replaceAll("\\*+", ".*");
        
        return lModifiedString;
    }
    
    /**
     * Return a copy of the string able to be interpreted by HTML:<br>
     * - replace all ' ' by '%20'<br>
     * - replace all '&' by '&amp;'<br>
     * 
     * @param pString
     *            the string to parse
     * @return the modified string
     */
    public static String parseToHTML(String pString) {
        
        String lModifiedString = pString;
        
        // Replace ' ' character
        lModifiedString =
                Pattern.compile(" ").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("%20"));
        
        // Replace '&' character
        lModifiedString =
                Pattern.compile("&").matcher(lModifiedString)
                        .replaceAll(Matcher.quoteReplacement("&amp;"));
        
        return lModifiedString;
    }
    
    /**
     * Return a copy of the string, with leading and trailing whitespace
     * omitted. <br>
     * If the string is null or empty, return null.
     * 
     * @param pString
     *            the string to check
     * @return null if the string is null or empty, else a copy of it without
     *         leading and trailing whitespace.
     */
    public static String notEmpty(String pString) {
        if (pString == null || pString.trim().isEmpty()) {
            return null;
        }
        else {
            return pString.trim();
        }
    }
    
    /**
     * Return true if the string is null or empty (or contains only whitespace),
     * else false.
     * 
     * @param pString
     *            the string to check
     * @return a boolean indicating if the string is null or empty
     */
    public static boolean isEmptyOrNull(String pString) {
        return (pString == null || pString.trim().isEmpty());
    }
    
    /**
     * Remove accent into String
     * 
     * @param pString
     *            the string to modify
     * @return a string with letters without accent
     */
    public static String removeDiacriticalMarks(String pString) {
        
        return Normalizer.normalize(pString, Form.NFD).replaceAll(
                "\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    /**
     * Convert the provided name to a name available for an Excel sheet name.<br>
     * All following characters are replaced by '_': '/', '\', '*', '?', '[' and
     * ']'. <br>
     * If the string length is greater than 31, only the 30 last characters are
     * returned.
     * 
     * @param pName
     *            the name to convert
     * @return the name in an available form
     */
    public static String convertStringForSheetName(String pName) {
        
        Pattern lPattern =
                Pattern.compile("[" + Pattern.quote("/\\*?[]") + "]");
        
        Matcher lMatcher = lPattern.matcher(pName);
        String lNewName = lMatcher.replaceAll("_");
        
        if (lNewName.length() > 31) {
            return (lNewName.substring(lNewName.length() - 30,
                    lNewName.length()));
        }
        else {
            return lNewName;
        }
    }
    
    /**
     * Return true if the string contains forbidden characters, else false.
     * 
     * @param pString
     *            the string to check
     * @return a boolean indicating if the string contains forbidden characters
     */
    public static boolean containsForbiddenChar(String pString) {
        
        if (isEmptyOrNull(pString)) {
            return false;
        }
        
        Matcher lMatcher =
                Pattern.compile("[" + Pattern.quote("/\\*?[]!") + "]").matcher(
                        pString);
        return lMatcher.find();
    }
    
    /**
     * Convert a string containing "\n"  by a string containing <br/>
     * @param pString
     * @return
     */
    public static String convertNLtoBR(String pString){
    	return pString.replace("\n", "<br/>");
    }
}
