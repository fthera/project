/*
 * ------------------------------------------------------------------------
 * Class : DocumentBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;

/**
 * Bean used to manage Documents
 */
@Stateless
@LocalBean
public class DocumentBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext(name = "DocumentService")
    private EntityManager em;
    
    /**
     * Persist the document in the database
     * 
     * @param pDocument
     *            the document to persist
     * @return the persisted document (itself)
     */
    public Document create(Document pDocument) {
        
        em.persist(pDocument);
        
        return pDocument;
    }
    
    /**
     * Remove the document from the database
     * 
     * @param pDocument
     *            the document to remove
     */
    public void remove(Document pDocument) {
        
        Document lDocument = em.find(Document.class, pDocument.getId());
        
        em.remove(lDocument);
    }
    
    /**
     * Search all documents
     * 
     * @return the found documents list
     */
    public List<Document> findAllDocuments() {
        TypedQuery<Document> lQuery =
                em.createNamedQuery(Document.ALL_DOCUMENTS_QUERY,
                        Document.class);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search the document by its id
     * 
     * @param pId
     *            the document id to search
     * @return the found document, else null
     */
    public Document findDocumentById(Long pId) {
        if (pId == null) {
            return null;
        }
        return em.find(Document.class, pId);
    }
    
    /**
     * Search documents related to an article
     * 
     * @param pArticle
     *            the searched article
     * @return the found documents list
     */
    public List<Document> findDocumentsByArticle(Article pArticle) {
        TypedQuery<Document> lQuery =
                em.createNamedQuery(Document.DOCUMENTS_USING_ARTICLE_QUERY,
                        Document.class);
        lQuery.setParameter("article", pArticle);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search documents related to an installation
     * 
     * @param pInstallation
     *            the searched installation
     * @return the found documents list
     */
    public List<Document>
            findDocumentsByInstallation(Installation pInstallation) {
        TypedQuery<Document> lQuery =
                em.createNamedQuery(
                        Document.DOCUMENTS_USING_INSTALLATION_QUERY,
                        Document.class);
        lQuery.setParameter("installation", pInstallation);
        
        return lQuery.getResultList();
    }
    
    /**
     * Search documents related to a tool
     * 
     * @param pTool
     *            the searched tool
     * @return the found documents list
     */
    public List<Document> findDocumentsByTool(Tool pTool) {
        TypedQuery<Document> lQuery =
                em.createNamedQuery(Document.DOCUMENTS_USING_TOOL_QUERY,
                        Document.class);
        lQuery.setParameter("tool", pTool);
        
        return lQuery.getResultList();
    }
    
}
