/*
 * ------------------------------------------------------------------------
 * Class : ArticleBean
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.ArticleState;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.UseState;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.history.History;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.exception.ChangeCollisionException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.ContainerManager;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.HistoryConstants;
import com.airbus.boa.util.StringUtil;

/**
 * Bean used to manage Articles in database
 */
@Stateless
@LocalBean
public class ArticleBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = Logger.getLogger(ArticleBean.class.getName());
    
    @PersistenceContext(name = "ArticleService")
    private EntityManager em;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private HistoryBean historyBean;
    
    @EJB
    private LocationManagerBean locationManagerBean;
    
    @EJB
    private ContainerManagerBean containerManagerBean;
    
    public AirbusPN createAirbusPN(AirbusPN pn, TypeArticle type) {
        em.persist(pn);
        type.add(pn);
        em.merge(type);
        return pn;
    }
    
    /**
     * Create the article in database with the provided location and container
     * 
     * @param article
     *            the article
     * @param pLocation
     *            the location
     * @param pContainer
     *            the container
     * @return the article
     */
    public <T extends Article> T createArticle(T article, Location pLocation,
            Container pContainer) {
        
        em.persist(article);
        em.flush();
        
        if (pLocation != null) {
            
            LocationManager lLocationManager = new LocationManager(article);
            
            lLocationManager.moveTo(pLocation, locationManagerBean);
        }
        
        if (pContainer != null) {
            
            ContainerManager lContainerManager = new ContainerManager(article);
            
            lContainerManager.linkTo(pContainer, containerManagerBean);
        }
        
        if (article.getSoftwares() != null) {
            // Mise à jour des la liste des équipements des logiciels
            // installés
            // sur l'article (Board, PC).
            for (Software soft : article.getSoftwares()) {
                Software software = softwareBean.findById(soft.getId());
                software.addEquipement(article);
                softwareBean.merge(software);
            }
        }
        return article;
    }
    
    /**
     * Create the provided PN in database
     * 
     * @param pn
     *            the PN to create
     * @return the merged PN
     */
    public <T extends PN> T createPN(T pn) {
        em.persist(pn);
        return pn;
    }
    
    /**
     * Create the provided TypeArticle in database
     * 
     * @param typeArticle
     *            the TypeArticle to persist
     * @return the created TypeArticle
     */
    public <T extends TypeArticle> T createTypeArticle(T typeArticle) {
        em.persist(typeArticle);
        return typeArticle;
    }
    
    /**
     * @param article
     * @param ports
     * @param addPortModifications
     * @return
     */
    public Board merge(Board board, List<CommunicationPort> ports,
            List<Action> addPortModifications) {
        History history = board.getHistory();
        history.getActions().addAll(addPortModifications);
        history = historyBean.merge(history);
        
        // Mise à jour de la nouvelle liste de ports
        // Pas d'appel à la fonction merge de comPortBean, traitement effectué
        // automatiquement grâce au "cascade.all"
        board.setPorts(ports);
        
        board = em.merge(board);
        return board;
        
    }
    
    /**
     * @param selectedPN
     * @param fromTypeArticleId
     * @param destTypeArticleId
     *            doit exister
     */
    public void merge(PN selectedPN, String udpatedIdentifier,
            Long fromTypeArticleId, Long destTypeArticleId) {
        
        TypeArticle fromType = findTypeArticleById(fromTypeArticleId);
        TypeArticle destType = findTypeArticleById(destTypeArticleId);
        
        selectedPN = em.merge(selectedPN);
        if (fromType != null) {
            fromType.getListAirbusPN().remove(selectedPN);
        }
        selectedPN.setIdentifier(udpatedIdentifier);
        destType.getListAirbusPN().add((AirbusPN) selectedPN);
        
        em.merge(fromType);
        em.merge(destType);
        em.merge(selectedPN);
        
    }
    
    /**
     * Merge the provided PN in database
     * 
     * @param pn
     *            the PN
     * @return the merged PN
     */
    public <T extends PN> T merge(T pn) {
        pn = em.merge(pn);
        return pn;
    }
    
    // TODO VERIFIER LE POLYMORPHISME
    public <T extends Article> T merge(T art) {
        T mergedArticle = art;
        try {
            
            art = em.merge(art);
            // detection des collisions
            
        }
        catch (OptimisticLockException e) {
            throw new ChangeCollisionException(e);
        }
        return mergedArticle;
    }
    
    /**
     * Merge the provided article in the database 
     * alongside its container and location
     * 
     * @param pArt
     *            the article
     * @param pLocation
     *            the location
     * @param pContainer
     *            the container
     * @return the merged article
     */
    public <T extends Article> T merge(T pArt, Location pLocation,
            Container pContainer) {
        T mergedArticle = pArt;
        try {
            mergedArticle = em.merge(mergedArticle);
            
            LocationManager lLocationManager =
                    new LocationManager(mergedArticle);
            lLocationManager.moveTo(pLocation, locationManagerBean);
            
            ContainerManager lContainerManager =
                    new ContainerManager(mergedArticle);
            lContainerManager.linkTo(pContainer, containerManagerBean);
        } catch (OptimisticLockException e) {
            throw new ChangeCollisionException(e);
        }
        return mergedArticle;
    }
    
    public <T extends TypeArticle> T mergeTypeArticle(T typeArticle) {
        return em.merge(typeArticle);
    }
    
    public void remove(Article article) {
        article = findArticleById(article.getId());
        em.remove(article);
    }
    
    /**
     * Delete the provided PN from the database
     * 
     * @param pn
     *            the Airbus or Manufacturer PN to delete
     */
    public void remove(PN pn) {
        
        if (pn == null) {
            return;
        }
        
        if (pn instanceof ManufacturerPN) {
            ManufacturerPN mpn = em.find(ManufacturerPN.class, pn.getId());
            
            if (mpn == null) {
                return; // DEJA SUPPRIMER
            }
            
            em.remove(mpn);
        }
        else if (pn instanceof AirbusPN) {
            
            AirbusPN apn = em.find(AirbusPN.class, pn.getId());
            
            if (apn == null) {
                return; // DEJA SUPPRIMER
            }
            
            // decablage du type d'article avec ce PN
            TypeArticle typeArt = findTypeArticleForPN(apn);
            if (typeArt != null) {
                typeArt.getListAirbusPN().remove(apn);
                typeArt = mergeTypeArticle(typeArt);
            }
            em.remove(apn);
        }
    }
    
    /**
     * Remove the type article.
     * 
     * @param typeArticle
     *            the type article to remove
     */
    public void remove(TypeArticle typeArticle) {
        
        TypeArticle lTypeArticle =
                em.find(TypeArticle.class, typeArticle.getId());
        if (lTypeArticle != null) {
            em.remove(lTypeArticle);
        }
    }
    
    /**
     * Search Articles having the TypeArticle corresponding to the provided id
     * 
     * @param id
     *            the typeArticle id
     * @return the number of found Articles
     */
    public Long countArticleWithTypeArticleById(Long id) {
        Query query =
                em.createNamedQuery(Article.COUNT_BY_TYPEARTICLE_ID_QUERY);
        query.setParameter("id", id);
        return (Long) query.getSingleResult();
    }
    
    /*
     * Recherche si ce SN est déjà attribué
     */
    public boolean existASN(String sn) {
        if (sn == null) {
            return false;
        }
        Query query = em.createNamedQuery("ExistAirbusSN");
        query.setParameter("sn", sn);
        boolean exist = false;
        try {
            String result = (String) query.getSingleResult();
            exist = (result != null);
        }
        catch (NonUniqueResultException e) {
            return true;
        }
        catch (NoResultException e) {
            
        }
        
        return exist;
        
    }
    
    /**
     * Search the provided Cabinet designation
     * 
     * @param designation
     *            the designation
     * @return true if the designation is found, else false
     */
    public boolean existCabinetDesignation(String designation) {
        if (designation == null || designation.isEmpty()) {
            return false;
        }
        
        Query query = em.createNamedQuery(Cabinet.DESIGNATION_QUERY);
        query.setParameter("designation", designation);
        
        try {
            return query.getSingleResult() != null;
            
        }
        catch (NoResultException e) {
        }
        
        return false;
    }
    
    public boolean existMSN(String sn) {
        if (sn == null) {
            return false;
        }
        Query query = em.createNamedQuery("ExistManufacturerSN");
        query.setParameter("sn", sn);
        boolean exist = false;
        try {
            String result = (String) query.getSingleResult();
            exist = (result != null);
        }
        catch (NonUniqueResultException e) {
            return true;
        }
        catch (NoResultException e) {
            
        }
        return exist;
    }
    
    /**
     * Search a TypeArticle by its name
     * 
     * @param name
     *            the TypeArticle name
     * @return true if the TypeArticle is found, else false
     */
    public boolean existTypeArticleByName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        Query query = em.createNamedQuery(TypeArticle.COUNT_BY_NAME_QUERY);
        query.setParameter("name", name);
        Number result;
        try {
            result = (Number) query.getSingleResult();
            return (result.longValue() > 0);
        }
        catch (NoResultException e) {
            // count(*) always returns a result
        }
        return false;
    }
    
    public AirbusPN findAirbusPNById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(AirbusPN.class, id);
    }
    
    public AirbusPN findAirbusPNByName(String identifier) {
        if (identifier == null) {
            return null;
        }
        Query query = em.createNamedQuery("AirbusPNByName");
        query.setParameter("name", identifier);
        AirbusPN airbusPN = null;
        try {
            airbusPN = (AirbusPN) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            
        }
        catch (NoResultException e) {
            
        }
        return airbusPN;
    }
    
    @SuppressWarnings("unchecked")
    public List<Article> findAllArticle() {
        Query query = em.createNamedQuery("AllArticle");
        List<Article> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        
        Iterator<Article> it = result.iterator();
        while (it.hasNext()) {
            Article article = it.next();
            if (article.getLocationOrm() != null) {
                article.getLocationOrm().getPlace();
            }
        }
        
        return result;
    }
    
    /**
     * Search Articles having the provided Airbus PN or Manufacturer PN
     * 
     * @param pn
     *            the Airbus PN or Manufacturer PN
     * @return the list of found Articles
     */
    public List<Article> findAllArticleWithPN(PN pn) {
        if (pn == null) {
            throw new NullPointerException("PN NULL");
        }
        
        TypedQuery<Article> lQuery;
        if (pn instanceof AirbusPN) {
            lQuery =
                    em.createNamedQuery(Article.BY_AIRBUSPN_ID_QUERY,
                            Article.class);
        }
        else if (pn instanceof ManufacturerPN) {
            lQuery =
                    em.createNamedQuery(Article.BY_MANUFACTURERPN_ID_QUERY,
                            Article.class);
        }
        else {
            return Collections.emptyList();
        }
        
        lQuery.setParameter("pn", pn.getId());
        try {
            return lQuery.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    private List<ManufacturerPN> findAllManufacturerPNbyTypeArticleId(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        
        TypedQuery<ManufacturerPN> query =
                em.createNamedQuery(
                        Article.MANUFACTURERPN_BY_TYPEARTICLE_ID_QUERY,
                        ManufacturerPN.class);
        query.setParameter("id", id);
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<String> findAllManufacturerPNIdentifiers() {
        
        Query query = em.createNamedQuery("AllManufacturerPNIdentifers");
        List<String> result;
        try {
            result = query.getResultList();
            
        }
        catch (NoResultException e) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    /*
     * recupère la liste triée des PN sur un article avec un typearticle.id
     * en deux temps (liste airbusPNs) et requete JPQL pour les manufacturerPNs
     */
    public List<PN> findAllPNbyTypeArticleId(Long id) {
        
        List<PN> results = new ArrayList<PN>();
        
        TypeArticle typeArt = findTypeArticleById(id);
        
        if (typeArt == null) {
            return results;
        }
        
        typeArt.getListAirbusPN().size();
        
        if (!typeArt.getListAirbusPN().isEmpty()) {
            results.addAll(typeArt.getListAirbusPN());
        }
        
        typeArt.setListManufacturerPN(findAllManufacturerPNbyTypeArticleId(id));
        
        if (!typeArt.getListManufacturerPN().isEmpty()) {
            results.addAll(typeArt.getListManufacturerPN());
        }
        
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<TypeArticle> findAllTypeArticle() {
        Query query = em.createNamedQuery("AllTypeArticle");
        List<TypeArticle> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        
        for (TypeArticle typeArt : result) {
            
            typeArt.setListManufacturerPN(findAllManufacturerPNbyTypeArticleId(typeArt
                    .getId()));
        }
        
        return result;
    }
    
    /**
     * @param typeArt
     *            le type
     * @return La liste des typeArticles de la classe d'instance de typeArt.
     */
    @SuppressWarnings("unchecked")
    public List<TypeArticle> findAllTypeArticle(TypeArticle typeArt) {
        List<TypeArticle> result;
        if (typeArt == null) {
            result = Collections.emptyList();
            return result;
        }
        
        try {
            Query query = em.createNamedQuery(typeArt.getAllQuery());
            result = query.getResultList();
        }
        catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public List<TypeArticle> findAllTypeArticleByName(String label) {
        if (StringUtil.isEmptyOrNull(label)) {
            return null;
        }
        
        Query query = em.createNamedQuery("AllTypeArticleByLabel");
        
        String like = StringUtil.parseToSQLRegex(label);
        log.warning(" : " + like);
        query.setParameter("label", like);
        
        List<TypeArticle> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException e) {
            result = null;
        }
        return result;
    }
    
    /**
     * Search the TypeArticles of Articles having the provided PN (Airbus or
     * Manufacturer)
     * 
     * @param pn
     *            the Airbus PN or Manufacturer PN
     * @return the list of found TypeArticle
     */
    public List<TypeArticle> findAllTypeArticleForPN(PN pn) {
        
        // instanceof gère null => false ;
        TypedQuery<TypeArticle> query = null;
        if (pn instanceof AirbusPN) {
            query =
                    em.createNamedQuery(TypeArticle.BY_AIRBUS_PN_QUERY,
                            TypeArticle.class);
            
        }
        else if (pn instanceof ManufacturerPN) {
            query =
                    em.createNamedQuery(
                            Article.TYPEARTICLE_BY_MANUFACTURERPN_QUERY,
                            TypeArticle.class);
            
        }
        else {
            return null;
        }
        
        query.setParameter("pn", pn);
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            log.warning(e.getMessage());
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private List<TypePC> findAllTypePC() {
        Query query = em.createNamedQuery(TypePC.ALL_QUERY);
        List<TypePC> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    /**
     * @param asn
     * @param msn
     * @param operatorAnd
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Article> findArticleByASNandMSN(String asn, String msn,
            boolean operatorAnd) {
        if (asn == null && msn == null) {
            return Collections.emptyList();
        }
        
        StringBuffer sb = new StringBuffer("SELECT a from Article a WHERE ");
        if (asn != null) {
            sb.append(" a.airbusSN = :asn ");
        }
        if (msn != null) {
            if (asn != null) {
                sb.append((operatorAnd) ? " AND " : " OR ");
            }
            sb.append(" a.manufacturerSN = :msn ");
        }
        
        Query query = em.createQuery(sb.toString());
        
        if (asn != null) {
            query.setParameter("asn", asn);
        }
        if (msn != null) {
            query.setParameter("msn", msn);
        }
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
        
    }
    
    /**
     * Search the Article by its id
     * 
     * @param id
     *            the Article id
     * @return the found Article, else null
     */
    public Article findArticleById(Long id) {
        if (id == null) {
            return null;
        }
        Query query = em.createNamedQuery(Article.BY_ID_QUERY);
        query.setParameter("primaryKey", id);
        try {
            Article article = (Article) query.getSingleResult();
            // Rafraichissement de l'entity manager car des modifications ont
            // été
            // effectuées par l'intermédiaire d'autres entity manager
            // (locationBean, ...)
            em.refresh(article);
            
            return article;
            
        }
        catch (NoResultException nre) {
            return null;
        }
    }
    
    public Article findArticleBySN(String serialNumber) {
        if (serialNumber == null) {
            return null;
        }
        Query query = em.createNamedQuery("ArticleBySN");
        query.setParameter("sn", serialNumber);
        
        Article article = null;
        try {
            article = (Article) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning("Plusieurs résultats pour findArticleBySN("
                    + serialNumber + ")");
            throw new ValidationException(e);
        }
        catch (NoResultException e) {
            throw new ValidationException(e);
        }
        return article;
    }
    
    /**
     * Search articles having the provided PN or PC type (reference)
     * and having an operational state, for articles case
     * 
     * @param reference
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Article> findArticlesFromReference(Object reference) {
        
        List<Article> articles = null;
        
        StringBuffer squery =
                new StringBuffer("Select a from Article a ").append("where ");
        
        if (reference instanceof PN) {
            
            squery.append(
                    (reference instanceof AirbusPN) ? " a.airbusPN = ?1 "
                            : " a.manufacturerPN = ?1 ").append(
                    " and a.state = ?2 ");
            
        }
        else if (reference instanceof TypeArticle) {
            squery.append("  a.typeArticle = ?1 ").append(" and a.state = ?2 "); // PC
        }
        else if (reference instanceof Software) {
            squery.append(" ?1 MEMBER OF a.softwares and a.state=?2");
        }
        
        try {
            Query query = em.createQuery(squery.toString());
            // log.warning(squery.toString()) ;
            
            if (reference != null) {
                query.setParameter(1, reference);
                
                query.setParameter(2, ArticleState.Operational);
                
            }
            articles = query.getResultList();
            
        }
        catch (Exception e) {
            log.warning(e.getMessage());
        }
        
        return articles;
    }
    
    public Board findBoardByASNOrMSN(String identifier) {
        if (identifier == null) {
            return null;
        }
        Query query = em.createNamedQuery("BoardByASNOrMSN");
        query.setParameter("sn", identifier);
        
        Board board = null;
        try {
            board = (Board) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning("Plusieurs résultats pour findBoardByASNOrMSN("
                    + identifier + ")");
        }
        catch (NoResultException e) {
            
        }
        return board;
    }
    
    public Cabinet findCabinetByDesignationOrSN(String desgOrSn) {
        if (desgOrSn == null) {
            return null;
        }
        Query query = em.createNamedQuery("CabinetByDesignationOrSN");
        query.setParameter("designationOrSN", desgOrSn);
        
        Cabinet cabinet = null;
        try {
            cabinet = (Cabinet) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            
        }
        catch (NoResultException e) {
            
        }
        return cabinet;
        
    }
    
    /**
     * Search the list of CMS Code of Articles of the TypeArticle corresponding
     * to the provided id
     * 
     * @param typeArtId
     *            the TypeArticle id
     * @return the list of found CMS Code
     */
    public List<String> findCMSCodeByTypeArticleId(Long typeArtId) {
        TypedQuery<String> query =
                em.createNamedQuery(Article.CMSCODE_BY_TYPEARTICLE_ID_QUERY,
                        String.class);
        query.setParameter("typeArtId", typeArtId);
        List<String> result;
        try {
            result = query.getResultList();
        }
        catch (NoResultException nre) {
            result = Collections.emptyList();
        }
        return result;
    }
    
    public ManufacturerPN findManufacturerPNByName(String identifier) {
        if (identifier == null) {
            return null;
        }
        Query query = em.createNamedQuery("ManufacturerPNByName");
        query.setParameter("name", identifier);
        ManufacturerPN pn = null;
        try {
            pn = (ManufacturerPN) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning("Plusieurs résults pour findManufacturerPNByName("
                    + identifier + ")");
        }
        catch (NoResultException e) {
            
        }
        return pn;
    }
    
    public Rack findRackByDesignationOrSN(String designationOrSN) {
        if (designationOrSN == null) {
            return null;
        }
        Query query = em.createNamedQuery("RackByDesignationOrSN");
        query.setParameter("designationOrSN", designationOrSN);
        
        Rack rack = null;
        try {
            rack = (Rack) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning("Plusieurs résultats pour findRackByDesignationOrSN("
                    + designationOrSN + ")");
        }
        catch (NoResultException e) {
            
        }
        return rack;
    }
    
    public Switch findSwitchByASNOrMSN(String name) {
        if (StringUtil.isEmptyOrNull(name)) {
            return null;
        }
        
        Query query = em.createNamedQuery("SwitchByName");
        query.setParameter("name", name);
        
        Switch aswitch = null;
        try {
            aswitch = (Switch) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning("Plusieurs résultats pour findSwitchByName(" + name
                    + ")");
        }
        catch (NoResultException e) {
            
        }
        return aswitch;
    }
    
    /**
     * Search the TypeArticle by its id
     * 
     * @param id
     *            the TypeArticle id
     * @return the found TypeArticle, else null
     */
    public TypeArticle findTypeArticleById(Long id) {
        if (id == null) {
            return null;
        }
        Query query = em.createNamedQuery(TypeArticle.BY_ID_QUERY);
        query.setParameter("id", id);
        try {
            return (TypeArticle) query.getSingleResult();
        }
        catch (NoResultException nre) {
            return null;
        }
    }
    
    public TypeArticle findTypeArticleByName(Article article, String name) {
        if (article == null) {
            return null;
        }
        Query query =
                em.createNamedQuery(article.createTypeArticle().getByName());
        query.setParameter("name", name);
        TypeArticle result;
        try {
            result = (TypeArticle) query.getSingleResult();
        }
        catch (NoResultException e) {
            log.warning(e.getMessage());
            result = null;
        }
        return result;
    }
    
    public TypeArticle findTypeArticleByName(String label) {
        if (StringUtil.isEmptyOrNull(label)) {
            return null;
        }
        
        Query query = em.createNamedQuery("TypeArticleByLabel");
        query.setParameter("label", label);
        TypeArticle result;
        try {
            result = (TypeArticle) query.getSingleResult();
        }
        catch (NoResultException e) {
            result = null;
        }
        return result;
    }
    
    /**
     * Search the unique TypeArticle of Articles having the provided PN (Airbus
     * or Manufacturer)
     * 
     * @param pn
     *            the Airbus PN or Manufacturer PN
     * @return the found TypeArticle, else null
     */
    public TypeArticle findTypeArticleForPN(PN pn) {
        
        // instanceof gère null => false ;
        Query query = null;
        if (pn instanceof AirbusPN) {
            query = em.createNamedQuery(TypeArticle.BY_AIRBUS_PN_QUERY);
            
        }
        else if (pn instanceof ManufacturerPN) {
            query =
                    em.createNamedQuery(Article.TYPEARTICLE_BY_MANUFACTURERPN_QUERY);
            
        }
        else {
            return null;
        }
        
        query.setParameter("pn", pn);
        try {
            return (TypeArticle) query.getSingleResult();
        }
        catch (NonUniqueResultException e) {
            log.warning(e.getMessage());
        }
        catch (NoResultException e) {
            log.warning(e.getMessage());
        }
        return null;
    }
    
    public TypePC findTypePCByName(String label) {
        if (label == null) {
            return null;
        }
        
        Query query = em.createNamedQuery(TypePC.BY_NAME_QUERY);
        query.setParameter("name", label);
        TypePC result;
        try {
            result = (TypePC) query.getSingleResult();
        }
        catch (NoResultException e) {
            result = null;
        }
        return result;
    }
    
    /**
     * @param suggest
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<PN> suggestionListAirbusPN(String suggest) {
        return (List<PN>) suggestionListPN(suggest, AirbusPN.class);
    }
    
    public <T extends Article> List<T> suggestionListArticle(Class<T> clazz,
            String suggest) {
        
        if (StringUtil.isEmptyOrNull(suggest)) {
            throw new IllegalArgumentException(
                    "suggestionListArticle suggest invalide");
        }
        
        suggest = StringUtil.parseToSQLRegex(suggest);
        if (suggest.indexOf("%") == -1) {
            // il faut integrer toute la sous-chaine comme critère générale de
            // recherche.
            suggest = "%" + suggest + "%";
        }
        
        StringBuffer qs =
                new StringBuffer("SELECT article FROM ").append(
                        clazz.getSimpleName()).append(" article WHERE ");
        
        if (clazz.equals(Rack.class) || clazz.equals(Switch.class)
                || clazz.equals(Cabinet.class)) {
            
            qs.append(" article.designation like :suggest ");
            qs.append(" OR article.airbusSN like :suggest ");
            qs.append(" OR article.manufacturerSN like :suggest ");
            
        }
        else if (clazz.equals(PC.class)) {
            qs.append(" article.name like :suggest ");
            qs.append(" OR article.airbusSN like :suggest ");
            qs.append(" OR article.function like :suggest ");
        }
        else {
            qs.append("  article.airbusSN like :suggest ");
            qs.append(" OR article.manufacturerSN like :suggest ");
            
        }
        
        TypedQuery<T> query = em.createQuery(qs.toString(), clazz);
        query.setParameter("suggest", suggest);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public List<PN> suggestionListManufacturerPN(String suggest) {
        return (List<PN>) suggestionListPN(suggest, ManufacturerPN.class);
    }
    
    private <T extends PN> List<? extends PN> suggestionListPN(String suggest,
            Class<T> clazz) {
        
        if (StringUtil.isEmptyOrNull(suggest)) {
            
            throw new IllegalArgumentException(
                    "suggestionListPN suggest invalide");
        }
        
        suggest = StringUtil.parseToSQLRegex(suggest);
        if (suggest.indexOf("%") == -1) {
            // il faut integrer toute la sous-chaine comme critère générale de
            // recherche.
            suggest = "%" + suggest + "%";
        }
        String queryString =
                "select pn from "
                        + clazz.getSimpleName()
                        + " pn where pn.identifier like :suggest order by pn.identifier asc ";
        TypedQuery<PN> query = em.createQuery(queryString, PN.class);
        query.setParameter("suggest", suggest);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Search the TypeArticle having the name similar to the provided one
     * 
     * @param suggest
     *            the suggested name
     * @return the list of found TypeArticle
     */
    public List<TypeArticle> suggestionListTypeArticle(String suggest) {
        if (StringUtil.isEmptyOrNull(suggest)) {
            // return all article types except the PC types
            List<TypeArticle> results = findAllTypeArticle();
            results.removeAll(findAllTypePC());
            return results;
        }
        
        suggest = StringUtil.parseToSQLRegex(suggest);
        if (suggest.indexOf("%") == -1) {
            // il faut integrer toute la sous-chaine comme critère générale de
            // recherche.
            suggest = "%" + suggest + "%";
        }
        
        TypedQuery<TypeArticle> query =
                em.createNamedQuery(TypeArticle.BY_SUGGEST_NAME_QUERY,
                        TypeArticle.class);
        query.setParameter("suggest", suggest);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Search TypePC having a name similar to the suggested one
     * 
     * @param suggest
     *            the name suggestion
     * @return the list of found TypePC
     */
    public List<TypePC> suggestionListTypePC(String suggest) {
        if (StringUtil.isEmptyOrNull(suggest)) {
            
            throw new IllegalArgumentException(
                    "suggestionListTypePC suggest invalide");
        }
        
        suggest = StringUtil.parseToSQLRegex(suggest);
        if (suggest.indexOf("%") == -1) {
            suggest = "%" + suggest + "%";
        }
        
        TypedQuery<TypePC> query =
                em.createNamedQuery(TypePC.TYPE_PC_BY_SUGGEST_NAME_QUERY,
                        TypePC.class);
        query.setParameter("suggest", suggest);
        
        try {
            return query.getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Compute the repartition map for the given type, PN or software.
     * The map indicate the number of articles located in each installation
     * (identified by its ID).
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the repartion map
     */
    @SuppressWarnings("unchecked")
    public Map<Long, Long> computeReparitionMap(TypeArticle pType,
            PN pPN, Software pSoftware) {
        Query lQuery = null;
        String lSubQueryString =
                "SELECT getMasterContainerID(a.id, NULL)"
                        + " AS containerID, COUNT(a.id) AS cnt FROM article a";
        
        if (pSoftware != null) {
            lSubQueryString +=
                    " JOIN software_article sa ON a.ID = sa.ARTICLE_ID";
            lSubQueryString += " WHERE sa.SOFTWARE_ID = " + pSoftware.getId();
        }
        else if (pPN == null) {
            lSubQueryString += " WHERE a.TYPEARTICLE_ID = " + pType.getId();
        }
        else if (pPN instanceof AirbusPN) {
            lSubQueryString += " WHERE a.AIRBUSPN_ID = " + pPN.getId();
        }
        else if (pPN instanceof ManufacturerPN) {
            lSubQueryString += " WHERE a.MANUFACTURERPN_ID = " + pPN.getId();
        }
        lSubQueryString +=
                " AND a.STATE = \"Operational\" GROUP BY containerID";
        String lQueryString = "SELECT i.id, c.cnt FROM (" + lSubQueryString
                + ") c, installation i" + " WHERE i.id = c.containerID";
        
        lQuery = em.createNativeQuery(lQueryString);
        try {
            List<Object[]> lResults = lQuery.getResultList();
            TreeMap<Long, Long> lRepartitionMap = new TreeMap<Long, Long>();
            for (Object[] lResult : lResults) {
                lRepartitionMap.put((Long) lResult[0], (Long) lResult[1]);
            }
            return lRepartitionMap;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Compute the number of article in use of the given type, PN or having the
     * given software.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the number of articles
     */
    public Long computeInUseQuantity(TypeArticle pType, PN pPN,
            Software pSoftware) {
        String lQueryString = "SELECT count(a) FROM Article a";
        lQueryString += computeWhereClauseForQuantities(pType, pPN, pSoftware);
        lQueryString += " AND a.state = :state";
        lQueryString += " AND a.useState IN :usestates";
        
        Query lQuery = em.createQuery(lQueryString);
        lQuery.setParameter("state", ArticleState.Operational);
        lQuery.setParameter("usestates",
                EnumSet.of(UseState.InUse, UseState.AllocatedSpare));
        try {
            return (Long) lQuery.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }
    
    /**
     * Compute the number of article in stock of the given type, PN or having
     * the given software.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the number of articles
     */
    public Long computeInStockQuantity(TypeArticle pType, PN pPN,
            Software pSoftware) {
        String lQueryString = "SELECT count(a) FROM Article a";
        lQueryString += computeWhereClauseForQuantities(pType, pPN, pSoftware);
        lQueryString += " AND a.state = :state";
        lQueryString += " AND a.useState IN :usestates";
        
        Query lQuery = em.createQuery(lQueryString);
        lQuery.setParameter("state", ArticleState.Operational);
        lQuery.setParameter("usestates", EnumSet.of(UseState.InStock));
        try {
            return (Long) lQuery.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }
    
    /**
     * Compute the number of operational articles not in use and not in stock of
     * the given type, PN or having the given software.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the number of articles
     */
    public Long computeOtherOperationalQuantity(TypeArticle pType, PN pPN,
            Software pSoftware) {
        String lQueryString = "SELECT count(a) FROM Article a";
        lQueryString += computeWhereClauseForQuantities(pType, pPN, pSoftware);
        lQueryString += " AND a.state = :state";
        lQueryString += " AND a.useState IN :usestates";
        
        Query lQuery = em.createQuery(lQueryString);
        lQuery.setParameter("state", ArticleState.Operational);
        lQuery.setParameter("usestates",
                EnumSet.of(UseState.Loaned, UseState.ToBeRemoved));
        try {
            return (Long) lQuery.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }
    
    /**
     * Compute the number of non operational articles of
     * the given type, PN or having the given software.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the map associating to each possible state, the number of article
     */
    @SuppressWarnings("unchecked")
    public Map<String, Long> computeOtherQuantities(TypeArticle pType, PN pPN,
            Software pSoftware) {
        String lQueryString = "SELECT a.state, count(a.state) FROM Article a";
        lQueryString += computeWhereClauseForQuantities(pType, pPN, pSoftware);
        lQueryString += " AND a.state != :state";
        lQueryString += " AND a.useState != :useState";
        lQueryString += " GROUP BY a.state";
        
        Query lQuery = em.createQuery(lQueryString);
        lQuery.setParameter("state", ArticleState.Operational);
        lQuery.setParameter("useState", UseState.Archived);
        try {
            List<Object[]> lResults = lQuery.getResultList();
            TreeMap<String, Long> lStock = new TreeMap<String, Long>();
            for (Object[] lResult : lResults) {
                lStock.put(((ArticleState) lResult[0]).getStringValue(),
                        (Long) lResult[1]);
            }
            return lStock;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Compute the WHERE SQL clause to select articles for the stock
     * computations.
     * 
     * @param pType
     *            the ArticleType
     * @param pPN
     *            the PN
     * @param pSoftware
     *            the software
     * @return the WHERE SQL clause
     */
    private String computeWhereClauseForQuantities(TypeArticle pType, PN pPN, Software pSoftware) {
        String lResult = "";
        
        if (pSoftware != null) {
            lResult += " JOIN a.softwares s WHERE s.id = " + pSoftware.getId();
        }
        else if (pPN == null) {
            lResult += " WHERE a.typeArticle.id = " + pType.getId();
        }
        else if (pPN instanceof AirbusPN) {
            lResult += " WHERE a.airbusPN.id = " + pPN.getId();
        }
        else if (pPN instanceof ManufacturerPN) {
            lResult += " WHERE a.manufacturerPN.id = " + pPN.getId();
        }
        
        return lResult;
    }
    
    /**
     * Update the article's children use states
     * 
     * @param pArticle
     *            the parent article which was updated
     * @param pLogin
     *            login of the author of the modification
     */
    public void updateChildrenUsestate(Article pArticle, String pLogin) {
        for (Article lArt : pArticle.getChildren()) {
            if (pArticle.getUseState() != lArt.getUseState()) {
                History lHistory = lArt.getHistory();
                FieldModification lModif;
                
                // Use State update
                lModif = new FieldModification(pLogin, null,
                        Constants.MODIFICATION,
                        new Comment(HistoryConstants.USESTATE_PARENT_UPDATE),
                        HistoryConstants.ARTICLE_USE_STATE,
                        lArt.getUseState().getStringValue(),
                        pArticle.getUseState().getStringValue());
                lHistory.getActions().add(lModif);
                lArt.setUseState(pArticle.getUseState());
                
                if (lArt.getUseState().equals(UseState.Archived)) {
                    // Functional State update
                    if (!lArt.getState().equals(ArticleState.Unusable)) {
                        lModif = new FieldModification(pLogin, null,
                                Constants.MODIFICATION,
                                new Comment(HistoryConstants.ARCHIVED_UPDATE),
                                HistoryConstants.ARTICLE_STATE,
                                lArt.getState().getStringValue(),
                                ArticleState.Unusable.getStringValue());
                        lHistory.getActions().add(lModif);
                        lArt.setState(ArticleState.Unusable);
                    }
                    
                    // Erase location
                    if (lArt.getLocation() != null) {
                        lModif = new FieldModification(pLogin, null,
                                Constants.MODIFICATION,
                                new Comment(HistoryConstants.ARCHIVED_UPDATE),
                                Constants.Location,
                                lArt.getLocation().toString(), null);
                        lHistory.getActions().add(lModif);
                    }
                    
                    // Erase Parent
                    if (lArt.getContainer() != null) {
                        lModif = new FieldModification(pLogin, null,
                                Constants.MODIFICATION,
                                new Comment(HistoryConstants.ARCHIVED_UPDATE),
                                Constants.Container,
                                lArt.getContainer().toString(), null);
                        lHistory.getActions().add(lModif);
                    }
                    
                    lArt = merge(lArt, null, null);
                }
                else {
                    lArt = merge(lArt);
                }
                
                updateChildrenUsestate(lArt, pLogin);
            }
        }
    }
    
}
