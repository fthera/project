/*
 * ----------------------------------------------------------------------------
 * Class : ExplorerController
 * Copyright 2016 by AIRBUS France
 * ----------------------------------------------------------------------------
 */

package com.airbus.boa.explorer.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.richfaces.component.UITree;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import com.airbus.boa.control.ObsolescenceBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.explorer.tree.TreeArticle;
import com.airbus.boa.explorer.tree.TreeBuilding;
import com.airbus.boa.explorer.tree.TreeExternalEntity;
import com.airbus.boa.explorer.tree.TreeInstallation;
import com.airbus.boa.explorer.tree.TreeNodeBase;
import com.airbus.boa.explorer.tree.TreePlace;
import com.airbus.boa.explorer.tree.TreeTool;
import com.airbus.boa.explorer.tree.io.ColumnExplorer;
import com.airbus.boa.explorer.tree.io.IOExcelWriterExplorer;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.ExportController;

/**
 * Controller managing the tree view part of some pages
 */
@ManagedBean(name = ExplorerController.BEAN_NAME)
@ViewScoped
public class ExplorerController extends AbstractController {
    
    /**
     * Enumerate determining in which way children of tree node have to be
     * retrieved
     */
    public enum ChildrenCriterion {
        /** Children are retrieved using their location */
        LOCATION,
        /** Children are retrieved using their container */
        CONTAINER
    }
    
    private static final long serialVersionUID = 671660147065168032L;
    
    /** Controller name */
    public static final String BEAN_NAME = "explorerController";
    
    @EJB
    private ObsolescenceBean obsoBean;
    
    /**
     * The root node of the tree (associated data is not significant, can
     * contain one or more child(children)
     */
    protected TreeNode rootNode;
    
    /** The current search mode */
    protected ChildrenCriterion searchMode;
    
    private Long itemId;
    
    /** The tree used in GUI */
    protected UITree treeBinding;
    
    /**
     * Initialize the rootNode with no child
     */
    public void initRootNode() {
        rootNode = new TreeNodeImpl();
    }
    
    /**
     * Initialize attributes
     */
    @PostConstruct
    protected void init() {
        initRootNode();
    }
    
    /**
     * Initialize the rootNode with the given object
     * The provided object should be an instance of one of the following
     * classes: <br>
     * - Building <br>
     * - Article <br>
     * - Place <br>
     * - Installation <br>
     * - Tool <br>
     * - ExternalEntity <br>
     * Else no child tree node is added.
     */
    public void setRootNode(Object pObject) {
        initRootNode();
        addTreeNode(pObject);
    }
    
    /**
     * Add a tree node based on the provided object to the root node. <br>
     * <b>The rootNode must have been initialized.</b><br>
     * The provided object should be an instance of one of the following
     * classes: <br>
     * - Building <br>
     * - Article <br>
     * - Place <br>
     * - Installation <br>
     * - Tool <br>
     * - ExternalEntity <br>
     * Else no child tree node is added.
     * 
     * @param pObject
     *            the object for which to add a child node
     */
    public void addTreeNode(Object pObject) {
        
        if (pObject instanceof Building) {
            
            searchMode = ChildrenCriterion.LOCATION;
            rootNode.addChild(((Building) pObject).getId(), new TreeBuilding(
                    (Building) pObject, searchMode));
        }
        else if (pObject instanceof Article) {
            
            searchMode = ChildrenCriterion.CONTAINER;
            rootNode.addChild(((Article) pObject).getId(), new TreeArticle(
                    (Article) pObject, null, searchMode));
        }
        else if (pObject instanceof Place) {
            
            searchMode = ChildrenCriterion.LOCATION;
            rootNode.addChild(((Place) pObject).getId(), new TreePlace(
                    (Place) pObject, null, searchMode));
        }
        else if (pObject instanceof Installation) {
            
            searchMode = ChildrenCriterion.CONTAINER;
            rootNode.addChild(((Installation) pObject).getId(),
                    new TreeInstallation((Installation) pObject, null,
                            searchMode));
        }
        else if (pObject instanceof Tool) {
            
            searchMode = ChildrenCriterion.CONTAINER;
            rootNode.addChild(((Tool) pObject).getId(), new TreeTool(
                    (Tool) pObject, null, searchMode));
        }
        else if (pObject instanceof ExternalEntity) {
            
            searchMode = ChildrenCriterion.LOCATION;
            rootNode.addChild(((ExternalEntity) pObject).getId(),
                    new TreeExternalEntity((ExternalEntity) pObject, null,
                            searchMode));
        }
    }
    
    /**
     * Expand all tree nodes
     */
    public void expandAll() {
        Iterator<Object> iterator = rootNode.getChildrenKeysIterator();
        while (iterator.hasNext()) {
            TreeNodeBase node =
                    (TreeNodeBase) rootNode.getChild(iterator.next());
            node.expandAll();
        }
    }
    
    /**
     * Collapse all tree nodes
     */
    public void collapseAll() {
        Iterator<Object> iterator = rootNode.getChildrenKeysIterator();
        while (iterator.hasNext()) {
            TreeNodeBase node =
                    (TreeNodeBase) rootNode.getChild(iterator.next());
            node.collapseAll();
        }
    }
    
    /**
     * Perform the export of the current explorer into an Excel file. <br>
     * Does nothing if no root node is defined.
     * 
     * @throws ExportException
     *             when an error occurs during export
     */
    public void doExportExplorer() throws ExportException {
        
        if (rootNode != null) {
            
            final Workbook lWORKBOOK = new SXSSFWorkbook(100);
            
            int lMaxDepth = getMaxDepth();
            
            IOExcelWriterExplorer lTreeSheet =
                    new IOExcelWriterExplorer(lWORKBOOK, new ColumnExplorer(
                            lMaxDepth), searchMode);
            lTreeSheet.writeHeader();
            lTreeSheet.write(rootNode);
            lTreeSheet.writeFooter();
            lTreeSheet.applyStyles();
            
            Downloadable lExcelFile = new Downloadable() {
                
                @Override
                public void write(OutputStream os) throws IOException {
                    lWORKBOOK.write(os);
                }
            };
            
            download(lExcelFile, "explorer.xlsx",
                    ExportController.MIMETYPE_XLSX);
        }
    }
    
    /**
     * @return the rootNode
     */
    public TreeNode getRootNode() {
        
        return rootNode;
    }
    
    /**
     * @return the itemId
     */
    public Long getItemId() {
        return itemId;
    }
    
    /**
     * @param itemId
     *            the itemId to set
     */
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    /**
     * @return the treeBinding
     */
    public UITree getTreeBinding() {
        return treeBinding;
    }
    
    /**
     * @param treeBinding
     *            the treeBinding to set
     */
    public void setTreeBinding(UITree treeBinding) {
        this.treeBinding = treeBinding;
    }
    
    /**
     * @return this tree maximum depth
     */
    private int getMaxDepth() {
        int lMaxDepth = 1;
        Iterator<Object> iterator = rootNode.getChildrenKeysIterator();
        while (iterator.hasNext()) {
            TreeNodeBase node =
                    (TreeNodeBase) rootNode.getChild(iterator.next());
            int lDepth = 1 + node.getDepth();
            if (lDepth > lMaxDepth) {
                lMaxDepth = lDepth;
            }
        }
        return lMaxDepth;
    }
    
    /**
     * @return true if the legend for inherited items is to be displayed, else
     *         false
     */
    public boolean isShowLegendInherited() {
        if (searchMode != null) {
            switch (searchMode) {
            case LOCATION:
                return true;
                
            case CONTAINER:
            default:
                return false;
            }
        }
        else {
            return false;
        }
    }
}
