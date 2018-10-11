/*
 * ------------------------------------------------------------------------
 * Class : ApplicationBean
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Bean used to manage generic queries in the database
 */
@Stateless
@LocalBean
public class ApplicationBean implements Serializable {
    
    private static final long serialVersionUID = 7950543468834069697L;
    
    @PersistenceContext(name = "ApplicationService")
    private EntityManager em;
    
    /**
     * Retrieve the database columns lengths, in the following form: <br>
     * A map representing the database tables, having: <br>
     * - as Key the table name <br>
     * - as Value a map representing the table columns <br>
     * The maps representing the tables columns have: <br>
     * - as Key the column name <br>
     * - as Value the column length
     * 
     * @return the database columns lengths, in a map form
     */
    public Map<String, Map<String, Integer>> findDatabaseColumnsLengths() {
        
        String lDBName = findDatabaseName();
        
        StringBuffer lQuerySB = new StringBuffer();
        lQuerySB.append(
                "SELECT table_name, column_name, character_maximum_length");
        lQuerySB.append(" FROM information_schema.columns");
        lQuerySB.append(" WHERE table_schema = '" + lDBName + "'");
        lQuerySB.append(" AND data_type LIKE 'varchar'");
        lQuerySB.append(" ORDER BY table_name, column_name");
        
        Query lQuery = em.createNativeQuery(lQuerySB.toString());
        @SuppressWarnings("unchecked")
        List<Object[]> lResults = lQuery.getResultList();
        
        Map<String, Map<String, Integer>> lDatabaseLengths =
                new HashMap<String, Map<String, Integer>>();
        
        for (Object[] lResult : lResults) {
            
            String lTableName = (String) lResult[0];
            String lColumnName = (String) lResult[1];
            Integer lColumnLength = ((Number) lResult[2]).intValue();
            
            Map<String, Integer> lTableLengths =
                    lDatabaseLengths.get(lTableName);
            if (lTableLengths == null) {
                lTableLengths = new HashMap<String, Integer>();
                lDatabaseLengths.put(lTableName, lTableLengths);
            }
            
            lTableLengths.put(lColumnName, lColumnLength);
        }
        
        return lDatabaseLengths;
    }
    
    /**
     * @return the currently used database hostname
     */
    public String findDatabaseHostname() {
        Query lQuery = em.createNativeQuery("SHOW VARIABLES LIKE 'hostname'");
        @SuppressWarnings("unchecked")
        List<Object[]> lResults = lQuery.getResultList();
        if (lResults.isEmpty()) {
            return "No hostname found";
        }
        else {
            Object[] lResult = lResults.get(0);
            return (String) lResult[1];
        }
    }
    
    /**
     * @return the currently used database name
     */
    public String findDatabaseName() {
        Query lQuery = em.createNativeQuery("SELECT DATABASE()");
        return (String) lQuery.getSingleResult();
    }
    
}
