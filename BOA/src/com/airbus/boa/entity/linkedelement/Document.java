/*
 * ------------------------------------------------------------------------
 * Class : Document
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.linkedelement;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.airbus.boa.entity.Item;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;

/**
 * Entity implementation class for Entity: Document
 */
@Entity
@Table(name = Document.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = Document.ALL_DOCUMENTS_QUERY,
                query = "SELECT d FROM Document d ORDER BY d.uploadDate"),
        @NamedQuery(
                name = Document.DOCUMENTS_USING_ARTICLE_QUERY,
                query = "SELECT d FROM Document d WHERE d.article = :article ORDER BY d.uploadDate"),
        @NamedQuery(
                name = Document.DOCUMENTS_USING_INSTALLATION_QUERY,
                query = "SELECT d FROM Document d WHERE d.installation = :installation ORDER BY d.uploadDate"),
        @NamedQuery(
                name = Document.DOCUMENTS_USING_TOOL_QUERY,
                query = "SELECT d FROM Document d WHERE d.tool = :tool ORDER BY d.uploadDate") })
public class Document implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "document";
    /** The Object column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    
    /**
     * Name of the named query selecting all Documents
     */
    public static final String ALL_DOCUMENTS_QUERY = "allDocuments";
    
    /**
     * Name of the named query selecting Documents using the provided Article<br>
     * This query is parameterized:<br>
     * <b>article</b> the article to search
     */
    public static final String DOCUMENTS_USING_ARTICLE_QUERY =
            "documentsUsingArticle";
    
    /**
     * Name of the named query selecting Documents using the provided
     * Installation<br>
     * This query is parameterized:<br>
     * <b>installation</b> the installation to search
     */
    public static final String DOCUMENTS_USING_INSTALLATION_QUERY =
            "documentsUsingInstallation";
    
    /**
     * Name of the named query selecting Documents using the provided Tool<br>
     * This query is parameterized:<br>
     * <b>tool</b> the tool to search
     */
    public static final String DOCUMENTS_USING_TOOL_QUERY =
            "documentsUsingTool";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ARTICLE_ID")
    private Article article = null;
    
    @ManyToOne
    @JoinColumn(name = "INSTALLATION_ID")
    private Installation installation = null;
    
    @ManyToOne
    @JoinColumn(name = "TOOL_ID")
    private Tool tool = null;
    
    @Column(name = NAME_COLUMN_NAME)
    private String name;
    
    @Basic(fetch = FetchType.EAGER)
    @Lob
    @Column(name = "FILEBLOB")
    private byte[] data;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;
    
    /**
     * Default constructor
     */
    public Document() {
        
    }
    
    /**
     * Constructor
     * 
     * @param pArticle
     *            the related article
     * @param pName
     *            the document name
     * @param pData
     *            the document data
     */
    public Document(Article pArticle, String pName, byte[] pData) {
        
        article = pArticle;
        name = pName;
        data = pData;
    }
    
    /**
     * Constructor
     * 
     * @param pInstallation
     *            the related installation
     * @param pName
     *            the document name
     * @param pData
     *            the document data
     */
    public Document(Installation pInstallation, String pName, byte[] pData) {
        
        installation = pInstallation;
        name = pName;
        data = pData;
    }
    
    /**
     * Constructor
     * 
     * @param pTool
     *            the related tool
     * @param pName
     *            the document name
     * @param pData
     *            the document data
     */
    public Document(Tool pTool, String pName, byte[] pData) {
        
        tool = pTool;
        name = pName;
        data = pData;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        Document lOther = (Document) pObj;
        if (id == null) {
            if (lOther.id != null) {
                return false;
            }
        }
        else {
            if (!id.equals(lOther.id)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        if (id != null) {
            result = result + id.hashCode();
        }
        return result;
    }
    
    /**
     * Method automatically called before creating a document in database
     */
    @PrePersist
    public void onCreate() {
        uploadDate = new Date();
    }
    
    @Override
    public String toString() {
        return "Document " + id;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param pId
     *            the id to set
     */
    public void setId(Long pId) {
        id = pId;
    }
    
    /**
     * @return the associated item
     */
    public Item getItem() {
        
        if (article != null) {
            return article;
        }
        else if (installation != null) {
            return installation;
        }
        else if (tool != null) {
            return tool;
        }
        return null;
    }
    
    /**
     * @return the article
     */
    public Article getArticle() {
        return article;
    }
    
    /**
     * @param pArticle
     *            the article to set
     */
    public void setArticle(Article pArticle) {
        article = pArticle;
    }
    
    /**
     * @return the installation
     */
    public Installation getInstallation() {
        return installation;
    }
    
    /**
     * @param pInstallation
     *            the installation to set
     */
    public void setInstallation(Installation pInstallation) {
        installation = pInstallation;
    }
    
    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }
    
    /**
     * @param pTool
     *            the tool to set
     */
    public void setTool(Tool pTool) {
        tool = pTool;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }
    
    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }
    
    /**
     * @param pData
     *            the data to set
     */
    public void setData(byte[] pData) {
        data = pData;
    }
    
    /**
     * @return the uploadDate
     */
    public Date getUploadDate() {
        return uploadDate;
    }
    
}
