/*
 * ------------------------------------------------------------------------
 * Class : UserFilterRegex
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.filter;

import com.airbus.boa.entity.user.User;

public class UserFilterRegex extends FilterRegexSupport<User> {
    
    private static final long serialVersionUID = 3823229993245509371L;
    
    @Override
    public Boolean filterMethodRegex(User current) {
        
        Boolean result = true;
        String chaine, filter;
        
        if (result) {
            filter = filterValues.get("login");
            chaine = current.getLogin();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("firstname");
            chaine = current.getFirstname();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("lastname");
            chaine = current.getLastname();
            result = compare(chaine, filter);
        }
        
        if (result) {
            filter = filterValues.get("email");
            chaine = current.getEmail();
            result = compare(chaine, filter);
        }
        
        if (result) {
            result = filterRoles(current);
        }
        
        return result;
        
    }
    
    // TODO A IMPLEMENTER
    private boolean filterRoles(User current) {
        boolean result = true;
        
        return result;
    }
    
}
