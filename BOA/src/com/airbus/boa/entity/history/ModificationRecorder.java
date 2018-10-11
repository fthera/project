/*
 * ------------------------------------------------------------------------
 * Class : ModificationRecorder
 * Copyright 2013 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.history;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.service.Constants;

public class ModificationRecorder implements PropertyChangeListener,
        Serializable {
    
    private static final long serialVersionUID = -5332749134416857055L;
    
    private static Logger log = Logger.getLogger(ModificationRecorder.class
            .getName());
    
    private Map<String, Action> changes;
    
    private User currentUser;
    
    public ModificationRecorder() {
        super();
        changes = new HashMap<String, Action>();
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        
        if (arg0 == null) {
            return;
        }
        
        String propertyName = arg0.getPropertyName();
        String oldValue = getStringValue(arg0.getOldValue());
        String newValue = getStringValue(arg0.getNewValue());
        
        log.info(arg0.getPropertyName() + " : [" + oldValue + "] [" + newValue
                + "]");
        
        if (changes.containsKey(propertyName)) {
            // il faut modifier le newValue
            Action action = changes.get(propertyName);
            if (action instanceof FieldModification) {
                
                FieldModification modif = (FieldModification) action;
                
                modif.setAfterValue(newValue);
                modif.setDate(new Date());
                if (modif.getAfterValue() == modif.getBeforeValue()
                        || modif.getAfterValue().equals(modif.getBeforeValue())) {
                    changes.remove(modif);
                }
            }
        }
        else { // il faut ajouter cette nouvelle modification
        
            if (oldValue.equals(newValue)) {
                return;
            }
            
            String login =
                    (currentUser != null) ? currentUser.getLogin() : null;
            String author = Action.formatAuthor(currentUser);
            
            FieldModification newModif =
                    new FieldModification(login, author,
                            Constants.MODIFICATION, null,
                            arg0.getPropertyName(), oldValue, newValue);
            changes.put(propertyName, newModif);
        }
        
    }
    
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        return sdf.format(date);
    }
    
    public boolean containsProperty(String propertyName) {
        return changes.containsKey(propertyName);
    }
    
    public Action removeChange(String propertyName) {
        return changes.remove(propertyName);
        
    }
    
    public void clearChanges() {
        changes.clear();
        
        // il faut rafraichir l'objet...
        
    }
    
    @Override
    public String toString() {
        return "ModificationRecorder [changes=" + changes + "]";
    }

    
    /**
     * Returns a string representation of the given object
     * 
     * @param pObject
     *            any object
     * @return the string representation of the given object, empty string if
     *         the object is null
     */
    private String getStringValue(Object pObject) {
        String lResult;
        if (pObject instanceof Date) {
            lResult = formatDate(((Date) pObject));
        }
        else if (pObject instanceof String) {
            String valeur = ((String) pObject).trim();
            lResult = String.valueOf(valeur);
        }
        else if (pObject instanceof ArticleState) {
            lResult = ((ArticleState) pObject).getStringValue();
        }
        else if (pObject instanceof UseState) {
            lResult = ((UseState) pObject).getStringValue();
        }
        else if (pObject == null) {
            lResult = "";
        }
        else {
            lResult = String.valueOf(pObject);
        }
        return lResult;
    }
    
    public List<Action> getActions() {
        return new ArrayList<Action>(changes.values());
    }
    
    public List<String> getFields() {
        return new ArrayList<String>(changes.keySet());
    }
    
    // @Deprecated
    public Object getProperty(String propertyName) {
        return changes.get(propertyName);
    }
    
    public void setUser(User currentUser) {
        this.currentUser = currentUser;
        
    }
    
}
