/*
 * ------------------------------------------------------------------------
 * Class : ToolBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.location.Contains_Tool_Article;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.exception.LocalizationException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerFactory;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationFactory;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.CollectionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Bean used for managing Tools
 */
@Stateless
@LocalBean
public class ToolBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(ToolBean.class.getName());
    
    @EJB
    private ContainerFactory containerFactory;
    
    @EJB
    private LocationFactory locationFactory;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    @PersistenceContext(name = "ToolService")
    private EntityManager em;
    
    /**
     * Persist the provided tool in database. <br>
     * Update the linked articles history.
     * 
     * @param pTool
     *            the tool to create
     * @param pComponents
     *            the tool components
     * @param pComponentsComments
     *            the tool components comments
     * @param pLocation
     *            the tool location
     * @param pContainer
     *            the tool container
     * @param pLogin
     *            the user login
     * @return the created tool
     */
    public Tool create(Tool pTool, List<Article> pComponents,
            Map<Long, String> pComponentsComments, Location pLocation,
            Container pContainer, String pLogin) {
        
        try {
            em.persist(pTool);
            em.flush();
            
            LocationManager lToolLocationManager = new LocationManager(pTool);
            
            lToolLocationManager.moveTo(pLocation, locationManagerBean);
            
            ContainerManager lToolContainerManager =
                    new ContainerManager(pTool);
            
            lToolContainerManager.linkTo(pContainer, containerManagerBean);
            
            if (pComponents != null) {
                // Link the articles to the tool
                for (Article lArticle : pComponents) {
                    
                    String lPrecision = null;
                    if (pComponentsComments != null) {
                        lPrecision = pComponentsComments.get(lArticle.getId());
                    }
                    addArticleInTool(lArticle, pTool, lPrecision, pLogin);
                }
            }
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            
            throw new ValidationException(e);
        }
        
        return pTool;
    }
    
    /**
     * Merge the provided tool with the one in database. <br>
     * Update the newly linked or unlinked articles history.
     * 
     * @param pTool
     *            the tool to merge
     * @param pComponents
     *            the tool to merge new components
     * @param pComponentsComments
     *            the tool to merge new components comments
     * @param pLocation
     *            the new tool location
     * @param pContainer
     *            the new tool container
     * @param pLogin
     *            the user login
     * @return the merged tool
     */
    public Tool merge(Tool pTool, List<Article> pComponents,
            Map<Long, String> pComponentsComments, Location pLocation,
            Container pContainer, String pLogin) {
        try {
            
            List<Article> lOldArticles = pTool.getChildren();
            
            // Update the tool in database
            pTool = em.merge(pTool);
            
            LocationManager lManager = new LocationManager(pTool);
            lManager.moveTo(pLocation, locationManagerBean);
            
            ContainerManager lContainerManager = new ContainerManager(pTool);
            lContainerManager.linkTo(pContainer, containerManagerBean);
            
            /*
             * Update of the articles list
             */
            
            List<Article> lAddedArticles = new ArrayList<Article>();
            List<Article> lRemovedArticles = new ArrayList<Article>();
            
            CollectionUtil.retrieveAddedAndRemoved(lOldArticles, pComponents,
                    lAddedArticles, lRemovedArticles);
            
            // Update the components comments
            for (Article lArticle : pComponents) {
                
                Long lArticleId = lArticle.getId();
                
                Article lManagedArticle =
                        articleBean.findArticleById(lArticleId);
                
                // the article was already in tool
                if (!lAddedArticles.contains(lManagedArticle)) {
                    
                    Contains_Tool_Article lRelation =
                            lManagedArticle.getContainerOrmTool();
                    
                    if (lRelation != null) {
                        
                        String lComment = pComponentsComments.get(lArticleId);
                        
                        String lPreviousComment = lRelation.getComment();
                        
                        // comment was null or has changed
                        if (lPreviousComment == null
                                || !lPreviousComment.equals(lComment)) {
                            
                            lRelation.setComment(lComment);
                            em.merge(lRelation);
                        }
                    }
                }
            }
            
            // Link the added articles to the tool
            for (Article lAddedArticle : lAddedArticles) {
                
                String lPrecision =
                        pComponentsComments.get(lAddedArticle.getId());
                addArticleInTool(lAddedArticle, pTool, lPrecision, pLogin);
            }
            
            // Unlink the removed articles from the container
            for (Article lRemovedArticle : lRemovedArticles) {
                
                removeArticleFromTool(lRemovedArticle, pLogin);
            }
            
            return pTool;
            
        }
        catch (OptimisticLockException e) {
            log.throwing(this.getClass().getName(), "merge", e);
            throw new ChangeCollisionException(e);
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            
            throw new ValidationException(e);
        }
    }
    
    /**
     * Remove the provided tool from database
     * 
     * @param pTool
     *            the tool to delete
     */
    public void remove(Tool pTool) {
        
        pTool = findToolById(pTool.getId());
        
        // Remove the tool from its location
        if (pTool.getLocation() != null) {
            LocationManager lManager = new LocationManager(pTool);
            lManager.removeFrom(locationManagerBean);
        }
        
        // unlink the tool from its container
        if (pTool.getContainer() != null) {
            ContainerManager lContainerManager = new ContainerManager(pTool);
            lContainerManager.unlink(containerManagerBean);
        }
        
        // children must have been removed from the tool before
        
        em.remove(pTool);
    }
    
    /**
     * Link the article to the tool (with the comment) and change the location
     * to inherited
     * 
     * @param pArticle
     *            the article
     * @param pTool
     *            the tool
     * @param pComment
     *            the comment
     * @param pLogin
     *            the login of the user
     * @throws LocalizationException
     *             when the location is not valid
     */
    private void addArticleInTool(Article pArticle, Tool pTool,
            String pComment, String pLogin) throws LocalizationException {
        
        Article lManagedArticle = articleBean.findArticleById(pArticle.getId());
        
        Container lCurrentContainer = lManagedArticle.getContainer();
        Location lCurrentLocation = lManagedArticle.getLocation();
        
        // Article container is set to the tool with the comment
        Container lNewContainer =
                ContainerManager.generateContainer(pArticle.getContainedType(),
                        pTool, pComment);
        
        String lAfterContainerStr = lNewContainer.toString();
        
        String lBeforeContainerStr = null;
        if (lCurrentContainer != null) {
            lBeforeContainerStr = lCurrentContainer.toString();
        }
        
        FieldModification lFieldModification =
                new FieldModification(pLogin, null, Constants.MODIFICATION,
                        null, Constants.Container, lBeforeContainerStr,
                        lAfterContainerStr);
        
        lManagedArticle.getHistory().getActions().add(lFieldModification);
        
        ContainerManager lContainerManager =
                new ContainerManager(lManagedArticle);
        
        lContainerManager.linkTo(lNewContainer, containerManagerBean);
        
        // Article location is set to inherited
        Location lNewLocation = pArticle.getLocation();
        
        lNewLocation =
                LocationManager.updateInherited(lNewLocation, true,
                        lNewContainer, pArticle.getLocatedType());
        
        if ((lCurrentLocation != null && !lCurrentLocation.equals(lNewLocation))
                || (lCurrentLocation == null && lNewLocation != null)) {
            
            String lAfterLocationStr = null;
            if (lNewLocation != null) {
                lAfterLocationStr = lNewLocation.toString();
            }
            
            String lBeforeLocationStr = null;
            if (lCurrentLocation != null) {
                lBeforeLocationStr = lCurrentLocation.toString();
            }
            
            FieldModification lFieldModificationLoc =
                    new FieldModification(pLogin, null, Constants.MODIFICATION,
                            null, Constants.Location, lBeforeLocationStr,
                            lAfterLocationStr);
            
            lManagedArticle.getHistory().getActions()
                    .add(lFieldModificationLoc);
            
            LocationManager lLocationManager =
                    new LocationManager(lManagedArticle);
            
            lLocationManager.moveTo(lNewLocation, locationManagerBean);
        }
    }
    
    /**
     * Unlink the article from the tool and change the location to not inherited
     * 
     * @param pArticle
     *            the article
     * @param pLogin
     *            the login of the user
     * @throws LocalizationException
     *             when the location is not valid
     */
    private void removeArticleFromTool(Article pArticle, String pLogin)
            throws LocalizationException {
        
        Article lManagedArticle = articleBean.findArticleById(pArticle.getId());
        
        Container lCurrentContainer = lManagedArticle.getContainer();
        Location lCurrentLocation = lManagedArticle.getLocation();
        
        // Article container is set to null
        String lBeforeContainerStr = null;
        if (lCurrentContainer != null) {
            lBeforeContainerStr = lCurrentContainer.toString();
        }
        
        FieldModification lFieldModification =
                new FieldModification(pLogin, null, Constants.MODIFICATION,
                        null, Constants.Container, lBeforeContainerStr, null);
        
        lManagedArticle.getHistory().getActions().add(lFieldModification);
        
        ContainerManager lContainerManager =
                new ContainerManager(lManagedArticle);
        
        lContainerManager.unlink(containerManagerBean);
        
        // Article location is set to not inherited
        Location lNewLocation = null;
        String lBeforeLocationStr = null;
        String lAfterLocationStr = null;
        if (lCurrentLocation != null) {
            lBeforeLocationStr = lCurrentLocation.toString();
            
            lNewLocation = new Location(lCurrentLocation);
            lNewLocation =
                    LocationManager.updateInherited(lNewLocation, false,
                            lCurrentContainer, pArticle.getLocatedType());
            
            if (lNewLocation != null) {
                lAfterLocationStr = lNewLocation.toString();
            }
        }
        
        if ((lCurrentLocation != null && !lCurrentLocation.equals(lNewLocation))
                || (lCurrentLocation == null && lNewLocation != null)) {
            
            FieldModification lFieldModificationLoc =
                    new FieldModification(pLogin, null, Constants.MODIFICATION,
                            null, Constants.Location, lBeforeLocationStr,
                            lAfterLocationStr);
            
            lManagedArticle.getHistory().getActions()
                    .add(lFieldModificationLoc);
            
            LocationManager lLocationManager =
                    new LocationManager(lManagedArticle);
            
            lLocationManager.moveTo(lNewLocation, locationManagerBean);
        }
    }
    
    /**
     * Check if the provided exists for a tool in database
     * 
     * @param pValue
     *            the tool name to search
     * @return a boolean indicating if the provided tool name exists in database
     */
    public boolean existToolName(String pValue) {
        if (pValue == null || pValue.trim().isEmpty()) {
            return false;
        }
        Query query = em.createNamedQuery(Tool.EXIST_TOOL_NAME_QUERY);
        query.setParameter("name", pValue);
        try {
            query.getSingleResult();
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
        catch (NonUniqueResultException e) {
            return true;
        }
    }
    
    /**
     * Return all tools from database
     * 
     * @return a list of tools from database
     */
    public List<Tool> findAllTools() {
        TypedQuery<Tool> lQuery =
                em.createNamedQuery(Tool.ALL_TOOLS_QUERY, Tool.class);
        List<Tool> lTools = lQuery.getResultList();
        return lTools;
    }
    
    /**
     * Search the tool by its id
     * 
     * @param pId
     *            the tool id to search
     * @return the found tool, else null
     */
    public Tool findToolById(Long pId) {
        if (pId == null) {
            return null;
        }
        Tool lTool = em.find(Tool.class, pId);
        if (lTool != null)
            em.refresh(lTool);
        return lTool;
    }
    
    /**
     * Search the tool by its name
     * 
     * @param pName
     *            the tool name to search
     * @return the found tool, else null
     */
    public Tool findToolByName(String pName) {
        
        Query lQuery = em.createNamedQuery(Tool.TOOL_BY_NAME_QUERY);
        lQuery.setParameter("name", pName);
        Tool lTool;
        try {
            lTool = (Tool) lQuery.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (NonUniqueResultException e) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.NON_UNIQUE_RESULT));
        }
        return lTool;
    }
    
    /**
     * Find tools from database with at least one text field corresponding to
     * the provided criterion
     * 
     * @param pCriterion
     *            the criterion to search (can contain '*' as wildcard)
     * @return the list of found tools
     */
    public List<Tool> findToolsByAnyField(String pCriterion) {
        
        TypedQuery<Tool> lQuery =
                em.createNamedQuery(Tool.TOOLS_BY_ANY_FIELD_QUERY, Tool.class);
        
        String lCriterionSql = StringUtil.parseToSQLRegex(pCriterion);
        
        lQuery.setParameter("criterion", lCriterionSql);
        
        List<Tool> lTools = lQuery.getResultList();
        return lTools;
    }
    
    /**
     * Find tools from database with a name like the provided one
     * 
     * @param pname
     *            the name to search (can contain '*' as wildcard)
     * @return the list of found tools
     */
    public List<Tool> findToolsByName(String pname) {
        
        TypedQuery<Tool> lQuery =
                em.createNamedQuery(Tool.TOOLS_BY_NAME_QUERY, Tool.class);
        
        String lCriterionSql = StringUtil.parseToSQLRegex(pname);
        
        lQuery.setParameter("name", lCriterionSql);
        
        List<Tool> lTools = lQuery.getResultList();
        return lTools;
    }
    
    /**
     * Return the tools satisfying the provided suggested value
     * 
     * @param pSuggest
     *            the search criteria
     * @return a list of tools
     */
    public List<Tool> suggestionListTool(String pSuggest) {
        
        if (StringUtil.isEmptyOrNull(pSuggest)) {
            throw new IllegalArgumentException(
                    "suggestionListTool suggest invalid");
        }
        
        pSuggest = StringUtil.parseToSQLRegex(pSuggest);
        if (pSuggest.indexOf("%") == -1) {
            // search the provided value as a substring
            pSuggest = "%" + pSuggest + "%";
        }
        
        TypedQuery<Tool> lQuery =
                em.createNamedQuery(Tool.TOOLS_BY_NAME_QUERY, Tool.class);
        lQuery.setParameter("name", pSuggest);
        
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
}
