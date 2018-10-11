/*
 * ------------------------------------------------------------------------
 * Class : PCSpecificityController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Controller managing the pc specificities creation, modification and
 * deletion
 */
@ManagedBean(name = PCSpecificityController.BEAN_NAME)
@ViewScoped
public class PCSpecificityController extends AbstractController {
    
    private enum Mode {
        CREATE,
        UPDATE
    }
    
    private Mode mode = Mode.CREATE;
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "PCSpecificityController";

    @EJB
    private ValueListBean valueListBean;
    
    /* Fields used for the specificity creation/update */
    
    private PCSpecificity specificity;
    private String description;
    private String contact;
    private PC pc;
    
    /** List of specificities, provided by PCController */
    private List<PCSpecificity> specificities = new ArrayList<PCSpecificity>();
    
    /** List of user modifications to validate */
    private List<Action> changes = new ArrayList<Action>();
    
    /**
     * Create a field modification for the specificity modification and add it
     * into the current modifications list
     * 
     * @param pProperty
     *            the changed property
     * @param pNewValue
     *            the new value
     * @param pOldValue
     *            the old value
     * @param pDescription
     *            the description of the change
     * @param pLogin
     *            the user login
     */
    private void AddChangeInList(String pProperty, String pNewValue,
            String pOldValue, String pDescription, String pLogin) {
        if (StringUtil.isEmptyOrNull(pNewValue)
                && StringUtil.isEmptyOrNull(pOldValue)) {
            return;
        }
        
        if (pNewValue == null || pOldValue == null
                || !pNewValue.equals(pOldValue)) {
            // creation of comment associated to the action
            String lMsg = MessageBundle.getMessageDefault(Constants.Description)
                    + ": " + pDescription;
            Comment lModificationComment = new Comment(lMsg);
            // creation of an action associated to the modified value
            FieldModification newModif =
                    new FieldModification(pLogin, null,
                            Constants.ModifyPCSpecificity,
                            lModificationComment, pProperty, pOldValue,
                            pNewValue);
            // addition to the modifications list
            changes.add(newModif);
        }
    }
    
    /**
     * Create a new specificity and add it to the list
     */
    public void doAdd() {
        setSpecificityAttributes();
        specificities.add(specificity);
    }
    
    /**
     * Remove the selected specificity in the table from the list
     */
    public void doDelete() {
        specificities.remove(specificity);
        specificity = null;
    }
    
    /**
     * Reset the specificity modification/creation fields
     */
    public void doReset() {
        initAttributeFromItem();
    }
    
    /**
     * Update the modified specificity in the list
     */
    public void doUpdate() {
        setSpecificityAttributes();
    }
    
    /**
     * Initialize the fields of the specificity update modal with the selected
     * specificity values
     */
    public void prepareUpdate() {
        mode = Mode.UPDATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the fields of the specificity add modal
     */
    public void prepareAdd() {
        specificity = new PCSpecificity();
        specificity.setPc(pc);
        mode = Mode.CREATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the controller fields from the given specificity
     */
    public void initAttributeFromItem() {
        if (specificity != null) {
            description = specificity.getDescription();
            contact = specificity.getContact();
        }
    }
    
    /**
     * Set the specificity attributes with the controller field values
     */
    public void setSpecificityAttributes() {
        specificity.setDescription(description);
        specificity.setContact(contact);
    }
    
    /**
     * Compare the list of specificities currently in the database and new
     * specificity list to create actions associated to the performed
     * modifications
     * 
     * @param pOldSpecificities
     *            the specificities list existing in database
     * @param pLogin
     *            the user login
     * @return a map containing the specificities name as keys and the actions
     *         as values
     */
    public List<Action> GetChangesList(
            List<PCSpecificity> pOldSpecificities,
            String pLogin) {
        // Reset the modifications list
        changes.clear();
        
        // Browse the previous specificity list
        for (PCSpecificity lOldSpecificity : pOldSpecificities) {
            // Search specificity into the new list
            Boolean lFound = false;
            for (PCSpecificity lSpecificity : specificities) {
                if (lOldSpecificity.getId().equals(lSpecificity.getId())) {
                    // Update the modifications list
                    AddChangeInList(Constants.Description,
                            lSpecificity.getDescription(),
                            lOldSpecificity.getDescription(),
                            lSpecificity.getDescription(), pLogin);
                    AddChangeInList(Constants.Contact,
                            lSpecificity.getContact(),
                            lOldSpecificity.getContact(),
                            lSpecificity.getDescription(), pLogin);
                    lFound = true;
                    break;
                }
            }
            if (!lFound) {
                // The specificity has been deleted
                // Update the modifications list
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Description)
                                + ": " + lOldSpecificity.getDescription();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null,
                        Constants.RemovePCSpecificity, lModificationComment);
                // Add to the current modifications list
                changes.add(lNewModif);
            }
        }
        
        // List of specificities modified by user
        for (PCSpecificity lSpecificity : specificities) {
            if (lSpecificity.getId() == null) {
                // Added specificity
                // Update the modifications list
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Description)
                                + ": " + lSpecificity.getDescription();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null,
                        Constants.AddPCSpecificity, lModificationComment);
                // Add to the current modifications list
                changes.add(lNewModif);
            }
        }
        
        return changes;
    }
    
    /**
     * @return a boolean indicating if the specificity is in creation mode
     */
    public boolean isCreateMode() {
        switch (mode) {
        case CREATE:
            return true;
        case UPDATE:
        default:
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the specificity is in update mode
     */
    public boolean isUpdateMode() {
        switch (mode) {
        case UPDATE:
            return true;
        case CREATE:
        default:
            return false;
        }
    }
    
    /**
     * @return the specificity
     */
    public PCSpecificity getSpecificity() {
        return specificity;
    }
    
    /**
     * @param pSpecificity
     *            the specificity to set
     */
    public void setSpecificity(PCSpecificity pSpecificity) {
        specificity = pSpecificity;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }
    
    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }
    
    /**
     * @param pContact
     *            the contact to set
     */
    public void setContact(String pContact) {
        contact = pContact;
    }
    
    /**
     * @return the pc
     */
    public PC getPc() {
        return pc;
    }
    
    /**
     * @param pPc
     *            the pc to set
     */
    public void setPc(PC pPc) {
        pc = pPc;
    }
    
    /**
     * @return the specificities
     */
    public List<PCSpecificity> getSpecificities() {
        return specificities;
    }
    
    /**
     * @param pSpecificities
     *            the specificities to set
     */
    public void setSpecificities(List<PCSpecificity> pSpecificities) {
        specificities = pSpecificities;
    }
}
