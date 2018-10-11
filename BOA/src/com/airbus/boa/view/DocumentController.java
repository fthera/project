/*
 * ------------------------------------------------------------------------
 * Class : DocumentController
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import com.airbus.boa.control.DocumentBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

/**
 * Controller managing the Document creation and deletion
 */
@ManagedBean(name = DocumentController.BEAN_NAME)
@ViewScoped
public class DocumentController extends AbstractController implements
        Serializable {
    
    private enum Mode {
        CONSULTATION_ARTICLE,
        CONSULTATION_INSTALLATION,
        CONSULTATION_TOOL
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "documentController";
    
    /**
     * Read and convert the provided file into an array of bytes
     * 
     * @param pFile
     *            the file
     * @return the bytes array
     * @throws IOException
     *             when an error occurs
     */
    public static byte[] convertFileToByteArray(File pFile) throws IOException {
        
        byte[] lData = new byte[(int) pFile.length()];
        
        FileInputStream lFileInputStream = new FileInputStream(pFile);
        lFileInputStream.read(lData);
        lFileInputStream.close();
        
        return lData;
    }
    
    @EJB
    private DocumentBean documentBean;
    
    private Mode mode = Mode.CONSULTATION_ARTICLE;
    
    private List<Document> documents = new ArrayList<Document>();
    
    /**
     * The selected document (in delete/download actions)
     */
    private Document document = null;
    
    /** The article for which to display the documents */
    private Article article = null;
    /** The installation for which to display the documents */
    private Installation installation = null;
    /** The tool for which to display the documents */
    private Tool tool = null;
    
    private UploadedFile uploadItem = null;
    
    /**
     * Add the uploaded file to the documents list
     */
    public void addUploadedFile() {
        
        if (uploadItem != null) {
            
            String lName = uploadItem.getName();
            lName = lName.substring(lName.lastIndexOf("\\") + 1);
            
            byte[] lData = uploadItem.getData();
            
            Document lDocument = null;
            
            if (article != null) {
                lDocument = new Document(article, lName, lData);
            }
            else if (installation != null) {
                lDocument = new Document(installation, lName, lData);
            }
            else if (tool != null) {
                lDocument = new Document(tool, lName, lData);
            }
            
            if (lDocument != null) {
                documents.add(lDocument);
            }
        }
        else {
            Utils.addFacesMessage(NavigationConstants.DOC_UPLOAD_ERROR_ID,
                    MessageBundle.getMessage(Constants.NO_SELECTED_FILE));
        }
    }
    
    /**
     * Download the selected document
     */
    public void doDownloadDocument() {
        if (document != null) {
            download(document.getData(), document.getName());
        }
    }
    
    /**
     * Persist added documents and delete removed documents in database for the
     * provided article
     * 
     * @param pArticle
     *            the persisted article
     */
    public void doPersistChanges(Article pArticle) {
        
        List<Document> lNewDocuments = new ArrayList<Document>(documents);
        updateDocuments();
        
        // Add new documents
        for (Document lDocument : lNewDocuments) {
            if (!documents.contains(lDocument)) {
                lDocument.setArticle(pArticle);
                documentBean.create(lDocument);
            }
        }
        
        // Remove old documents
        for (Document lDocument : documents) {
            if (!lNewDocuments.contains(lDocument)) {
                documentBean.remove(lDocument);
            }
        }
        
        updateDocuments();
    }
    
    /**
     * Persist added documents and delete removed documents in database for the
     * provided installation
     * 
     * @param pInstallation
     *            the persisted installation
     */
    public void doPersistChanges(Installation pInstallation) {
        
        List<Document> lNewDocuments = new ArrayList<Document>(documents);
        updateDocuments();
        
        // Add new documents
        for (Document lDocument : lNewDocuments) {
            if (!documents.contains(lDocument)) {
                lDocument.setInstallation(pInstallation);
                documentBean.create(lDocument);
            }
        }
        
        // Remove old documents
        for (Document lDocument : documents) {
            if (!lNewDocuments.contains(lDocument)) {
                documentBean.remove(lDocument);
            }
        }
        
        updateDocuments();
    }
    
    /**
     * Persist added documents and delete removed documents in database for the
     * provided tool
     * 
     * @param pTool
     *            the persisted tool
     */
    public void doPersistChanges(Tool pTool) {
        
        List<Document> lNewDocuments = new ArrayList<Document>(documents);
        updateDocuments();
        
        // Add new documents
        for (Document lDocument : lNewDocuments) {
            if (!documents.contains(lDocument)) {
                lDocument.setTool(pTool);
                documentBean.create(lDocument);
            }
        }
        
        // Remove old documents
        for (Document lDocument : documents) {
            if (!lNewDocuments.contains(lDocument)) {
                documentBean.remove(lDocument);
            }
        }
        
        updateDocuments();
    }
    
    /**
     * Listener allowing uploading a file
     * 
     * @param pEvent
     *            the upload event
     * @throws Exception
     *             when an error occurs
     */
    public void fileUploadListener(FileUploadEvent pEvent) throws Exception {
        UploadedFile lItem = pEvent.getUploadedFile();
        uploadItem = lItem;
    }
    
    /**
     * Initialize attributes article and mode
     */
    public void setMode(Object lItem) {
        if (lItem instanceof Article) {
            article = (Article) lItem;
            mode = Mode.CONSULTATION_ARTICLE;
        }
        else if (lItem instanceof Installation) {
            installation = (Installation) lItem;
            mode = Mode.CONSULTATION_INSTALLATION;
        }
        else if (lItem instanceof Tool) {
            tool = (Tool) lItem;
            mode = Mode.CONSULTATION_TOOL;
        }
        updateDocuments();
    }
    
    /**
     * Remove the document from the documents list
     */
    public void removeDocument() {
        if (document != null) {
            
            documents.remove(document);
        }
        else {
            Utils.addFacesMessage(NavigationConstants.DOC_LIST_ERROR_ID,
                    MessageBundle.getMessage(Constants.NO_SELECTED_FILE));
        }
    }
    
    private void updateDocuments() {
        
        switch (mode) {
        case CONSULTATION_ARTICLE:
            documents = documentBean.findDocumentsByArticle(article);
            break;
        case CONSULTATION_INSTALLATION:
            documents = documentBean.findDocumentsByInstallation(installation);
            break;
        case CONSULTATION_TOOL:
            documents = documentBean.findDocumentsByTool(tool);
            break;
        default:
            documents = new ArrayList<Document>();
            break;
        }
    }
    
    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * @param pDocument
     *            the document to set
     */
    public void setDocument(Document pDocument) {
        document = pDocument;
    }
    
    /**
     * @return the documents
     */
    public List<Document> getDocuments() {
        return documents;
    }
    
}
