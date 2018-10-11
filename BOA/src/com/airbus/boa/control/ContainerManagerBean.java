/*
 * ------------------------------------------------------------------------
 * Class : ContainerManagerBean
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.control;

import java.io.Serializable;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.location.Contains_Cabinet_Rack;
import com.airbus.boa.entity.location.Contains_Inst_Cabinet;
import com.airbus.boa.entity.location.Contains_PC_Board;
import com.airbus.boa.entity.location.Contains_Rack_Board;
import com.airbus.boa.entity.location.Contains_Tool_Article;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.exception.ValidationException;

/**
 * Bean used to manage the container of entities
 */
@Stateless
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ContainerManagerBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext(name = "ContainerManagerService")
    private EntityManager em;
    
    /**
     * Link the first article to the second one
     * 
     * @param pArticleUsedBy
     *            the article to link
     * @param pArticleUser
     *            the container article
     * @return the merged article to link
     */
    public Article linkToArticle(Article pArticleUsedBy, Article pArticleUser) {
        
        pArticleUser.getContainedArticles().add(pArticleUsedBy);
        pArticleUsedBy.setContainerArticle(pArticleUser);
        pArticleUser = em.merge(pArticleUser);
        pArticleUsedBy = em.merge(pArticleUsedBy);
        
        return pArticleUsedBy;
    }
    
    /**
     * Unlink the first article from the second one
     * 
     * @param pArticleUsedBy
     *            the article to unlink
     * @param pArticleUser
     *            the container article
     * @return the merged article to unlink
     */
    public Article unlinkFromArticle(Article pArticleUsedBy,
            Article pArticleUser) {
        
        pArticleUsedBy.setContainerArticle(null);
        pArticleUser.getContainedArticles().remove(pArticleUsedBy);
        pArticleUser = em.merge(pArticleUser);
        pArticleUsedBy = em.merge(pArticleUsedBy);
        
        return pArticleUsedBy;
    }
    
    /**
     * Link the PC of the demand to the article (cabinet)
     * 
     * @param pDemand
     *            the demand
     * @param pArticle
     *            the article
     * @return the merged demand
     */
    public Demand linkToArticle(Demand pDemand, Article pArticle) {
        
        pDemand.setContainerArticle(pArticle);
        
        return em.merge(pDemand);
    }
    
    /**
     * Unlink the PC of the demand from its article (cabinet)
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand unlinkFromArticle(Demand pDemand) {
        
        pDemand.setContainerArticle(null);
        
        return em.merge(pDemand);
    }
    
    /**
     * Link the first board to the second one
     * 
     * @param pMezzanine
     *            the board to link
     * @param pMotherboard
     *            the container board
     * @return the merged board to link
     */
    public Board linkToBoard(Board pMezzanine, Board pMotherboard) {
        linkToArticle(pMezzanine, pMotherboard);
        return pMezzanine;
    }
    
    /**
     * Unlink the first board from the second one
     * 
     * @param pMezzanine
     *            the board to unlink
     * @param pMotherboard
     *            the container board
     * @return the merged board to unlink
     */
    public Board unlinkFromBoard(Board pMezzanine, Board pMotherboard) {
        unlinkFromArticle(pMezzanine, pMotherboard);
        return pMezzanine;
    }
    
    /**
     * Link the rack to the cabinet
     * 
     * @param lRack
     *            the rack
     * @param lCabinet
     *            the cabinet
     * @param lRackPosition
     *            the rack position
     * @return the merged rack
     */
    public Rack
            linkToCabinet(Rack lRack, Cabinet lCabinet, String lRackPosition) {
        
        lRack = em.merge(lRack);
        lCabinet = em.merge(lCabinet);
        
        lCabinet.addRack(lRack, lRackPosition);
        em.persist(lRack.getContainerOrmCabinet());
        lRack = em.merge(lRack);
        lCabinet = em.merge(lCabinet);
        return lRack;
    }
    
    /**
     * Unlink the rack from the cabinet
     * 
     * @param pRack
     *            the rack
     * @param pCabinet
     *            the cabinet
     * @return the merged rack
     */
    public Rack unlinkFromCabinet(Rack pRack, Cabinet pCabinet) {
        
        pRack = em.merge(pRack);
        pCabinet = em.merge(pCabinet);
        
        Contains_Cabinet_Rack lRelation = pCabinet.removeRack(pRack);
        pRack = em.merge(pRack);
        pCabinet = em.merge(pCabinet);
        em.remove(em.merge(lRelation));
        return pRack;
    }
    
    /**
     * Link the article to the installation
     * 
     * @param pArticleUsedBy
     *            the article
     * @param pInstallation
     *            the installation
     * @return the merged article
     */
    public Article linkToInstallation(Article pArticleUsedBy,
            Installation pInstallation) {
        
        pInstallation.getContainedArticles().add(pArticleUsedBy);
        pArticleUsedBy.setContainerInstallation(pInstallation);
        pInstallation = em.merge(pInstallation);
        pArticleUsedBy = em.merge(pArticleUsedBy);
        
        return pArticleUsedBy;
    }
    
    /**
     * Unlink the article from the installation
     * 
     * @param pArticleUsedBy
     *            the article
     * @param pInstallation
     *            the installation
     * @return the merged article
     */
    public Article unlinkFromInstallation(Article pArticleUsedBy,
            Installation pInstallation) {
        
        pArticleUsedBy.setContainerInstallation(null);
        pInstallation.getContainedArticles().remove(pArticleUsedBy);
        pArticleUsedBy = em.merge(pArticleUsedBy);
        pInstallation = em.merge(pInstallation);
        
        return pArticleUsedBy;
    }
    
    /**
     * Link the cabinet to the installation
     * 
     * @param pCabinet
     *            the cabinet
     * @param pInstallation
     *            the installation
     * @param pLetter
     *            the letter
     * @return the merged cabinet
     */
    public Cabinet linkToInstallation(Cabinet pCabinet,
            Installation pInstallation, String pLetter) {
        
        Contains_Inst_Cabinet lRelation = new Contains_Inst_Cabinet();
        lRelation.setLetter(pLetter);
        lRelation.setInstallation(pInstallation);
        lRelation.setCabinet(pCabinet);
        em.persist(lRelation);
        pInstallation = em.merge(pInstallation);
        pInstallation.getContainedOrmCabinets().add(lRelation);
        
        pCabinet.setContainerOrmInstallation(lRelation);
        pCabinet = em.merge(pCabinet);
        return pCabinet;
    }
    
    /**
     * Unlink the cabinet from the installation
     * 
     * @param pCabinet
     *            the cabinet
     * @param pInstallation
     *            the installation
     * @return the merged cabinet
     */
    public Cabinet unlinkFromInstallation(Cabinet pCabinet,
            Installation pInstallation) {
        
        Contains_Inst_Cabinet lRelation =
                pCabinet.getContainerOrmInstallation();
        
        pInstallation.getContainedOrmCabinets().remove(lRelation);
        pCabinet.setContainerOrmInstallation(null);
        lRelation.setCabinet(null);
        lRelation.setInstallation(null);
        pCabinet = em.merge(pCabinet);
        pInstallation = em.merge(pInstallation);
        em.remove(em.merge(lRelation));
        return pCabinet;
    }
    
    /**
     * Link the PC of the demand to the installation
     * 
     * @param pDemand
     *            the demand
     * @param pInstallation
     *            the installation
     * @return the merged demand
     */
    public Demand
            linkToInstallation(Demand pDemand, Installation pInstallation) {
        
        pDemand.setContainerInstallation(pInstallation);
        
        return em.merge(pDemand);
    }
    
    /**
     * Unlink the PC of the demand from its installation
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand unlinkFromInstallation(Demand pDemand) {
        
        pDemand.setContainerInstallation(null);
        
        return em.merge(pDemand);
    }
    
    /**
     * Link the tool to the installation
     * 
     * @param pTool
     *            the tool
     * @param pInstallation
     *            the installation
     * @return the merged tool
     */
    public Tool linkToInstallation(Tool pTool, Installation pInstallation) {
        pInstallation.getContainedTools().add(pTool);
        pTool.setContainerInstallation(pInstallation);
        pInstallation = em.merge(pInstallation);
        pTool = em.merge(pTool);
        return pTool;
    }
    
    /**
     * Unlink the tool from the installation
     * 
     * @param pTool
     *            the tool
     * @param pInstallation
     *            the installation
     * @return the merged tool
     */
    public Tool unlinkFromInstallation(Tool pTool, Installation pInstallation) {
        pTool.setContainerInstallation(null);
        pInstallation.getContainedArticles().remove(pTool);
        pTool = em.merge(pTool);
        pInstallation = em.merge(pInstallation);
        return pTool;
    }
    
    /**
     * Link the board to the PC
     * 
     * @param pBoard
     *            the board
     * @param pPC
     *            the PC
     * @param pSlotNumber
     *            the slot number
     * @return the merged board
     */
    public Board linkToPC(Board pBoard, PC pPC, Integer pSlotNumber) {
        if (pPC == null) {
            throw new ValidationException();
        }
        if (pBoard == null) {
            throw new ValidationException();
        }
        
        pPC = em.merge(pPC);
        pBoard = em.merge(pBoard);
        
        Contains_PC_Board lRelation = new Contains_PC_Board();
        lRelation.setBoard(pBoard);
        lRelation.setPc(pPC);
        lRelation.setSlotNumber(pSlotNumber);
        
        em.persist(lRelation);
        pPC.getContainedOrmBoards().add(lRelation);
        pBoard.setContainerOrmPC(lRelation);
        
        pPC = em.merge(pPC);
        pBoard = em.merge(pBoard);
        return pBoard;
    }
    
    /**
     * Unlink the board from the PC
     * 
     * @param pBoard
     *            the board
     * @param pPC
     *            the PC
     * @return the merged board
     */
    public Board unlinkFromPC(Board pBoard, PC pPC) {
        if (pPC == null) {
            throw new ValidationException();
        }
        if (pBoard == null) {
            throw new ValidationException();
        }
        
        Contains_PC_Board lRelation = pBoard.getContainerOrmPC();
        
        pPC.getContainedOrmBoards().remove(lRelation);
        pBoard.setContainerOrmPC(null);
        lRelation.setBoard(null);
        lRelation.setPc(null);
        pPC = em.merge(pPC);
        pBoard = em.merge(pBoard);
        em.remove(em.merge(lRelation));
        return pBoard;
    }
    
    /**
     * Link the board to the rack
     * 
     * @param pBoard
     *            the board
     * @param pRack
     *            the rack
     * @param pSlotNumber
     *            the slot number
     * @param pFace
     *            the face
     * @return merged board
     */
    public Board linkToRack(Board pBoard, Rack pRack, Integer pSlotNumber,
            Contains_Rack_Board.Face pFace) {
        if (pRack == null) {
            throw new ValidationException();
        }
        if (pBoard == null) {
            throw new ValidationException();
        }
        
        pRack = em.merge(pRack);
        pBoard = em.merge(pBoard);
        
        Contains_Rack_Board lRelation = new Contains_Rack_Board();
        lRelation.setBoard(pBoard);
        lRelation.setRack(pRack);
        lRelation.setFace(pFace);
        lRelation.setSlotNumber(pSlotNumber);
        
        em.persist(lRelation);
        pRack.getContainedOrmBoards().add(lRelation);
        pBoard.setContainerOrmRack(lRelation);
        
        pRack = em.merge(pRack);
        pBoard = em.merge(pBoard);
        return pBoard;
    }
    
    /**
     * Unlink the board from the rack
     * 
     * @param pBoard
     *            the board
     * @param pRack
     *            the rack
     * @return the merged board
     */
    public Board unlinkFromRack(Board pBoard, Rack pRack) {
        if (pRack == null) {
            throw new ValidationException();
        }
        if (pBoard == null) {
            throw new ValidationException();
        }
        
        Contains_Rack_Board lRelation = pBoard.getContainerOrmRack();
        
        pRack.getContainedOrmBoards().remove(lRelation);
        pBoard.setContainerOrmRack(null);
        lRelation.setBoard(null);
        lRelation.setRack(null);
        pRack = em.merge(pRack);
        pBoard = em.merge(pBoard);
        em.remove(em.merge(lRelation));
        return pBoard;
    }
    
    /**
     * Link the article to the tool
     * 
     * @param pArticle
     *            the article
     * @param pTool
     *            the tool
     * @param pComment
     *            the relation comment
     * @return the merged article
     */
    public Article linkToTool(Article pArticle, Tool pTool, String pComment) {
        
        Contains_Tool_Article lRelation = new Contains_Tool_Article();
        lRelation.setTool(pTool);
        lRelation.setArticle(pArticle);
        lRelation.setComment(pComment);
        em.persist(lRelation);
        
        pTool = em.merge(pTool);
        pTool.getContainedOrmArticles().add(lRelation);
        
        pArticle.setContainerOrmTool(lRelation);
        pArticle = em.merge(pArticle);
        
        return pArticle;
    }
    
    /**
     * Unlink the article from the tool
     * 
     * @param pArticle
     *            the article
     * @param pTool
     *            the tool
     * @return the merged article
     */
    public Article unlinkFromTool(Article pArticle, Tool pTool) {
        
        Contains_Tool_Article lRelation = pArticle.getContainerOrmTool();
        
        pTool.getContainedOrmArticles().remove(lRelation);
        pArticle.setContainerOrmTool(null);
        lRelation.setArticle(null);
        lRelation.setTool(null);
        pArticle = em.merge(pArticle);
        pTool = em.merge(pTool);
        em.remove(em.merge(lRelation));
        return pArticle;
    }
    
    /**
     * Link the PC of the demand to the tool
     * 
     * @param pDemand
     *            the demand
     * @param pTool
     *            the tool
     * @return the merged demand
     */
    public Demand linkToTool(Demand pDemand, Tool pTool) {
        
        pDemand.setContainerTool(pTool);
        
        return em.merge(pDemand);
    }
    
    /**
     * Unlink the PC of the demand from its tool
     * 
     * @param pDemand
     *            the demand
     * @return the merged demand
     */
    public Demand unlinkFromTool(Demand pDemand) {
        
        pDemand.setContainerTool(null);
        
        return em.merge(pDemand);
    }
    
}
