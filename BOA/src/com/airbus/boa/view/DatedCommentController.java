/*
 * ------------------------------------------------------------------------
 * Class : DatedCommentController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Controller managing the dated comments creation, modification and
 * deletion
 */
@ManagedBean(name = DatedCommentController.BEAN_NAME)
@ViewScoped
public class DatedCommentController extends AbstractController {
    
    private enum Mode {
        CREATE,
        UPDATE
    }
    
    private Mode mode = Mode.CREATE;
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "DatedCommentController";

    @EJB
    private ValueListBean valueListBean;
    
    /* Fields used for the dated comment creation/update */
    
    private DatedComment datedComment;
    private String comment;
    private Article article;
    
    /** List of dated comments, provided by PCController */
    private List<DatedComment> datedComments = new ArrayList<DatedComment>();
    
    /**
     * List of dated comments, provided by PCController, that will remained
     * unchanged
     */
    private List<DatedComment> oldDatedComments = new ArrayList<DatedComment>();
    
    /** List of user modifications to validate */
    private List<Action> changes = new ArrayList<Action>();
    
    /**
     * Create a field modification for the dated comment modification and add it
     * into the current modifications list
     * 
     * @param pProperty
     *            the changed property
     * @param pNewValue
     *            the new value
     * @param pOldValue
     *            the old value
     * @param pLogin
     *            the user login
     */
    private void AddChangeInList(String pProperty, String pNewValue,
            String pOldValue, String pLogin) {
        if (StringUtil.isEmptyOrNull(pNewValue)
                && StringUtil.isEmptyOrNull(pOldValue)) {
            return;
        }
        
        if (pNewValue == null || pOldValue == null
                || !pNewValue.equals(pOldValue)) {
            // creation of comment associated to the action
            String msg = "";
            Comment modificationComment = new Comment(msg);
            // creation of an action associated to the modified value
            FieldModification newModif =
                    new FieldModification(pLogin, null,
                            Constants.ModifyDatedComment,
                            modificationComment, pProperty, pOldValue,
                            pNewValue);
            // addition to the modifications list
            changes.add(newModif);
        }
    }
    
    /**
     * Create a new dated comment and add it to the list
     */
    public void doAdd() {
        setDatedCommentAttributes();
        datedComments.add(0, datedComment);
    }
    
    /**
     * Remove the selected dated comment in table from the list
     */
    public void doDelete() {
        datedComments.remove(datedComment);
        datedComment = null;
    }
    
    /**
     * Reset the dated comment creation/modification fields
     */
    public void doReset() {
        initAttributeFromItem();
    }
    
    /**
     * Update the modified dated comment in the list
     */
    public void doUpdate() {
        setDatedCommentAttributes();
    }
    
    /**
     * Initialize the fields of the dated comment update modal with the selected
     * dated comment values
     */
    public void prepareUpdate() {
        mode = Mode.UPDATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the fields of the dated comment add modal
     */
    public void prepareAdd() {
        datedComment = new DatedComment();
        datedComment.setArticle(article);
        datedComment.setDate(new Date());
        LogInController lLoginController = findBean(LogInController.class);
        datedComment.setUser(lLoginController.getUserLogged());
        mode = Mode.CREATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the controller fields from the given dated comment
     */
    public void initAttributeFromItem() {
        if (datedComment != null) {
            comment = datedComment.getComment();
        }
    }
    
    /**
     * Set the dated comment attributes with the controller field values
     */
    public void setDatedCommentAttributes() {
        datedComment.setComment(comment);
    }
    
    /**
     * Compare the list of dated comments currently in the database and new
     * dated comments list to
     * create actions associated to the performed modifications
     * 
     * @param pOldDatedComments
     *            the dated comments list existing in database
     * @param pLogin
     *            the user login
     * @return a map containing the dated comments description as keys and the
     *         actions as values
     */
    public List<Action> GetChangesList(
            List<DatedComment> pOldDatedComments, String pLogin) {
        // Reset the modifications list
        changes.clear();
        
        // Browse the previous dated comments list
        for (DatedComment lOldDatedComment : pOldDatedComments) {
            // Search dated comment into the new list
            Boolean lFound = false;
            for (DatedComment lDatedComment : datedComments) {
                if (lOldDatedComment.getId().equals(lDatedComment.getId())) {
                    // Update the modifications list
                    AddChangeInList(Constants.Description,
                            lDatedComment.getComment(),
                            lOldDatedComment.getComment(), pLogin);
                    lFound = true;
                    break;
                }
            }
            if (!lFound) {
                // The dated comment has been deleted
                // Update the modifications list
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Description)
                                + ": " + lOldDatedComment.getComment();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null,
                        Constants.RemoveDatedComment, lModificationComment);
                // Add to the current modifications list
                changes.add(lNewModif);
            }
        }
        
        // List of dated comments modified by user
        for (DatedComment lDatedComment : datedComments) {
            if (lDatedComment.getId() == null) {
                // Added dated comment
                // Update the modifications list
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Description)
                                + ": " + lDatedComment.getComment();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null,
                        Constants.AddDatedComment, lModificationComment);
                // Add to the current modifications list
                changes.add(lNewModif);
            }
        }
        
        return changes;
    }
    
    /**
     * Check if the given dated comment is modifiable
     * (i.e was created during this edition session for regular users)
     * 
     * @param pDatedComment
     *            the dated comment to check
     * @return true if the dated comment is modifiable, false otherwise
     */
    public boolean isModificationAuthorized(DatedComment pDatedComment) {
        LogInController lLogInController = findBean(LogInController.class);
        if (lLogInController
                .isAuthorized(RightMaskAction.DatedCommentUDAuthorization)) {
            return true;
        }
        else {
            return !oldDatedComments.contains(pDatedComment);
        }
    }
    
    /**
     * @return a boolean indicating if the dated comment is in creation mode
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
     * @return a boolean indicating if the dated comment is in update mode
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
     * @return the datedComment
     */
    public DatedComment getDatedComment() {
        return datedComment;
    }
    
    /**
     * @param pDatedComment
     *            the datedComment to set
     */
    public void setDatedComment(DatedComment pDatedComment) {
        datedComment = pDatedComment;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param pComment
     *            the comment to set
     */
    public void setComment(String pComment) {
        comment = pComment;
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
     * @return the datedComments
     */
    public List<DatedComment> getDatedComments() {
        return datedComments;
    }
    
    /**
     * @param pDatedComments
     *            the datedComments to set
     */
    public void setDatedComments(List<DatedComment> pDatedComments) {
        datedComments = pDatedComments;
        oldDatedComments = new ArrayList<>(pDatedComments);
    }
}
