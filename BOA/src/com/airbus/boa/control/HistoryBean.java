/*
 * ------------------------------------------------------------------------
 * Class : HistoryBean
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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.history.History;

@Stateless
@LocalBean
public class HistoryBean implements Serializable {
    
    private static final long serialVersionUID = 2562030491263801834L;
    
    @PersistenceContext(name = "HistoryService")
    private EntityManager em;
    
    public Comment createComment(Comment comment) {
        em.persist(comment);
        return comment;
    }
    
    // la persistance de History est assuré par Article
    public History createHistory(History history) {
        em.persist(history);
        return history;
    }
    
    public <T> T merge(T entity) {
        return em.merge(entity);
    }
    
    public History addAction(History history, Action action) {
        history.addAction(action);
        return em.merge(history);
    }
    
    public History addGeneralComment(History history, Comment comment) {
        history.setGeneralComment(comment);
        comment = merge(comment);
        history = merge(history);
        return history;
    }
    
    public History findHistoryById(Long id) {
        if (id == null) {
            return null;
        }
        History history = em.find(History.class, id);
        em.refresh(history);
        return history;
    }
    
    /**
     * Retrieve all breakdowns for the given type and PN.
     * Breakdowns are field modifications of the article state from the
     * "Operational" value to the "Out of Order" value.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @return the list of field modifications
     */
    @SuppressWarnings("unchecked")
    public List<FieldModification> findAllBreakdowns(TypeArticle pType,
            PN pPN) {
        Query lQuery = null;
        String lQueryString = "SELECT fm FROM FieldModification fm"
                + " WHERE fm.field=\"State\""
                + " AND fm.beforeValue=\"Operational\" AND fm.afterValue=\"Out of Order\""
                + " AND fm.history.id IN (SELECT art.history.id FROM Article art";
        
        if (pPN == null) {
            lQueryString += " WHERE art.typeArticle.id = :id)";
            lQuery = em.createQuery(lQueryString);
            lQuery.setParameter("id", pType.getId());
        }
        else {
            if (pPN instanceof AirbusPN) {
                lQueryString += " WHERE art.airbusPN.id = :id)";
            }
            else if (pPN instanceof ManufacturerPN) {
                lQueryString += " WHERE art.manufacturerPN.id = :id)";
            }
            lQuery = em.createQuery(lQueryString);
            lQuery.setParameter("id", pPN.getId());
        }
        
        List<FieldModification> result;
        try {
            result = lQuery.getResultList();
        } catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    /**
     * Retrieve all scrappings for the given type and PN.
     * Scrappings are field modifications of the article state from
     * any value to the "Unusable" value.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @return the list of field modifications
     */
    @SuppressWarnings("unchecked")
    public List<FieldModification> findAllScrappings(TypeArticle pType,
            PN pPN) {
        Query lQuery = null;
        String lQueryString = "SELECT fm FROM FieldModification fm"
                + " WHERE fm.field=\"State\" AND fm.afterValue=\"Unusable\""
                + " AND fm.history.id IN (SELECT art.history.id FROM Article art";
        
        if (pPN == null) {
            lQueryString += " WHERE art.typeArticle.id = :id)";
            lQuery = em.createQuery(lQueryString);
            lQuery.setParameter("id", pType.getId());
        }
        else {
            if (pPN instanceof AirbusPN) {
                lQueryString += " WHERE art.airbusPN.id = :id)";
            }
            else if (pPN instanceof ManufacturerPN) {
                lQueryString += " WHERE art.manufacturerPN.id = :id)";
            }
            lQuery = em.createQuery(lQueryString);
            lQuery.setParameter("id", pPN.getId());
        }
        
        List<FieldModification> result;
        try {
            result = lQuery.getResultList();
        } catch (NoResultException e) {
            result = Collections.emptyList();
        }
        return result;
    }
}
