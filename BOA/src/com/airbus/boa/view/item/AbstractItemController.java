/*
 * ------------------------------------------------------------------------
 * Class : AbstractItemController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.Utils;

/**
 * Abstract controller to be extended by controllers managing an entity
 * creation, consultation and modification
 */
public abstract class AbstractItemController extends AbstractController
        implements Serializable {
    
    protected enum Mode {
        CREATE,
        READ,
        UPDATE
    }
    
    private static final long serialVersionUID = 1L;
    
    protected Mode mode = Mode.READ;
    
    private String returnPage = "";

    protected String itemPage = "";
    
    protected String listPage = "";
    
    protected String resultPage = "";
    
    protected String errorId = "";

    /**
     * This method is called when the whole bean is constructed and initialized.
     * It is called on page loading.
     */
    @PostConstruct
    protected void init() {
        Map<String, String> parameterMap = (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (parameterMap.containsKey("mode")) {
            String mode = parameterMap.get("mode");
            if (mode.equalsIgnoreCase("CREATE"))
                this.mode = Mode.CREATE;
            else if (mode.equalsIgnoreCase("UPDATE"))
                this.mode = Mode.UPDATE;
            else
                this.mode = Mode.READ;
        }
        if (parameterMap.containsKey("ret")) {
            returnPage = parameterMap.get("ret");
        }
        if (parameterMap.containsKey("id")) {
            setItemId(new Long(parameterMap.get("id")));
            initItemFromDatabase();
        }
        if (this.mode == Mode.CREATE)
            initItemWithNew();
        initAttributesFromItem();
    }
    
    /**
     * Perform creation of the new item in database
     */
    public void doCreate() {
        
        try {
            createItem();
            goToReadPage(getItemId());
        }
        catch (Exception e) {
            Utils.addFacesMessage(errorId, ExceptionUtil.getMessage(e));
            e.printStackTrace();
            log.warning("Exception Create Item : " + e.getMessage());
        }
    }
    
    /**
     * Reset the page with the entity data from database.
     */
    public void doRefresh() {
        switch (mode) {
        case CREATE:
            
            initItemWithNew();
            break;
        
        case READ:
        case UPDATE:
            
            initItemFromDatabase();
            break;
        
        default:
            break;
        }
        
        initAttributesFromItem();
    }
    
    /**
     * Perform update of the selected item in database
     */
    public void doUpdate() {
        
        try {
            updateItem();
            goToReturnPage();
        }
        catch (Exception e) {
            Utils.addFacesMessage(errorId, ExceptionUtil.getMessage(e));
            log.warning("Exception Update Item : " + e.getMessage());
        }
    }
    
    public void doCancelUpdate() {
        goToReturnPage();
    }
    
    public void doDelete() {
        
        try {
            deleteItem();
            NavigationUtil.goTo(NavigationConstants.MAIN_PAGE);
        } catch (Exception e) {
            Utils.addFacesMessage(errorId, ExceptionUtil.getMessage(e));
            log.warning("Exception Delete Item : " + e.getMessage());
        }
    }

    public void goToReadPage(Long pId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", pId.toString());
        params.put("mode", "READ");
        NavigationUtil.goTo(itemPage, params);
    }

    public void goToUpdatePage() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", getItemId().toString());
        params.put("mode", "UPDATE");
        params.put("ret", "ITEM");
        NavigationUtil.goTo(itemPage, params);
    }

    public void goToReturnPage() {
        if (returnPage.equalsIgnoreCase("LIST")) {
            NavigationUtil.goTo(listPage);
        }
        else if (returnPage.equalsIgnoreCase("RESULT")) {
            NavigationUtil.goTo(resultPage);
        }
        else if (returnPage.equalsIgnoreCase("ITEM")) {
            goToReadPage(getItemId());
        }
        else {
            NavigationUtil.goTo(NavigationConstants.MAIN_PAGE);
        }
    }
    
    /**
     * Prepare the page listing the items
     */
    protected abstract void setItemId(Long pId);
    
    /**
     * Prepare the page listing the items
     */
    protected abstract Long getItemId();
    
    /**
     * Persist the item in database
     * 
     * @throws Exception
     *             when an error occurs
     */
    protected abstract void createItem() throws Exception;
    
    /**
     * Update the item in database
     * 
     * @throws Exception
     *             when an error occurs
     */
    public abstract void updateItem() throws Exception;
    
    /**
     * Delete the item from the database
     * 
     * @throws Exception
     *             when an error occurs
     */
    protected abstract void deleteItem() throws Exception;
    
    /**
     * Reset the current entity to a new one
     */
    protected abstract void initItemWithNew();
    
    /**
     * Reset the current entity to the one retrieved from database
     */
    protected abstract void initItemFromDatabase();
    
    /**
     * Initialize the controller attributes from the item
     */
    protected abstract void initAttributesFromItem();
    
    /**
     * @return a boolean indicating if the entity is in creation mode
     */
    public boolean isCreateMode() {
        switch (mode) {
        case CREATE:
            return true;
        case READ:
        case UPDATE:
        default:
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the entity is in read only mode
     */
    public boolean isReadOnly() {
        switch (mode) {
        case READ:
            return true;
        case UPDATE:
        case CREATE:
        default:
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the entity is in update mode
     */
    public boolean isUpdateMode() {
        switch (mode) {
        case UPDATE:
            return true;
        case READ:
        case CREATE:
        default:
            return false;
        }
    }
    
    /**
     * @return the title to be displayed
     */
    public String getTitle() {
        
        switch (mode) {
        case READ:
            return getReadTitle();
        case UPDATE:
            return getUpdateTitle();
        case CREATE:
            return getCreateTitle();
        default:
            return "";
        }
    }
    
    /**
     * @return the page title for creation mode
     */
    protected abstract String getCreateTitle();
    
    /**
     * @return the page title for read only mode
     */
    protected abstract String getReadTitle();
    
    /**
     * @return the page title for modification mode
     */
    protected abstract String getUpdateTitle();
}
