/*
 * ------------------------------------------------------------------------
 * Class : CommunicationPortBean
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.airbus.boa.entity.network.CommunicationPort;

/**
 * @author NG0056B0
 */
@Stateless
@LocalBean
public class CommunicationPortBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext(name = "CommunicationPortService")
    private EntityManager em;
    
    public void create(CommunicationPort comPort) {
        
        // A revoir. Création automatique du port via le "cascade.all"
        em.persist(comPort);
        
    }
    
    public CommunicationPort merge(CommunicationPort port) {
        
        // A revoir. Fusion automatique du port via le "cascade.all"
        em.merge(port);
        return port;
    }
    
    public CommunicationPort remove(CommunicationPort port) {
        
        // A revoir. Destruction automatique du port via le "cascade.all"
        port = em.merge(port);
        em.remove(port);
        return port;
    }
    
    @SuppressWarnings("unchecked")
    public List<CommunicationPort> findByName(String name, String ipAddress,
            String macAddress) {
        
        String chaine = "select p from CommunicationPort p  where 1 ";
        if (name != null) {
            chaine += "and p.name = :name ";
        }
        if (ipAddress != null) {
            chaine += "and p.ipAddress = :ipAddress ";
        }
        if (macAddress != null) {
            chaine += "and p.macAddress = :macAddress ";
        }
        
        Query query = em.createQuery(chaine);
        
        if (name != null) {
            query.setParameter("name", name);
        }
        
        if (ipAddress != null) {
            query.setParameter("ipAddress", ipAddress);
        }
        if (macAddress != null) {
            query.setParameter("macAddress", macAddress);
        }
        
        List<CommunicationPort> results = Collections.emptyList();
        results = query.getResultList();
        return results;
    }
    
}
