/*
 * ------------------------------------------------------------------------
 * Class : Board
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeBoard;
import com.airbus.boa.entity.location.Contains_PC_Board;
import com.airbus.boa.entity.location.Contains_Rack_Board;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Entity implementation class for Entity: Board
 */
@Entity
@Table(name = Board.TABLE_NAME)
@NamedQueries({ @NamedQuery(name = "BoardByASNOrMSN",
        query = "SELECT b FROM Board b WHERE b.airbusSN = :sn "
                + "OR b.manufacturerSN = :sn") })
public class Board extends Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "board";
    /** The bootloader column name */
    public static final String BOOTLOADER_COLUMN_NAME = "BOOTLOADER";
    /** The IP address column name */
    public static final String IPADDRESS_COLUMN_NAME = "IPADDRESS";
    /** The Mac address column name */
    public static final String MACADDRESS_COLUMN_NAME = "MACADDRESS";
    /** The revH column name */
    public static final String REVH_COLUMN_NAME = "REVH";
    /** The revS column name */
    public static final String REVS_COLUMN_NAME = "REVS";
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date activeStockControlDate;
    
    @Column
    private String ipAddress;
    
    /**
     * Revision Hardware
     */
    @Column
    private String revH;
    
    /**
     * Revision Software
     */
    @Column
    private String revS;
    
    @Column
    private String macAddress;
    
    @Column
    private String bootLoader;
    
    @Column(name = "CALIBRATION")
    private Boolean calibrated;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL },
            orphanRemoval = true)
    @JoinTable(name = "board_communicationport", joinColumns = @JoinColumn(
            name = "BOARD_ID"), inverseJoinColumns = @JoinColumn(
            name = "COMMUNICATIONPORT_ID"))
    private List<CommunicationPort> ports = new ArrayList<CommunicationPort>();
    
    @OneToOne(mappedBy = "board")
    private Contains_Rack_Board containerOrmRack;
    
    @OneToOne(mappedBy = "board")
    private Contains_PC_Board containerOrmPC;
    
    /**
     * Default constructor
     */
    public Board() {
        super();
        ports.clear();
    }
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypeBoard();
    }
    
    @Override
    public void validate() {
        
        // Il faut au moins un PN Airbus ou un PN Fabricant ou un code CMS
        // //29/02/2012
        if (StringUtil.isEmptyOrNull(cmsCode) && airbusPN == null
                && manufacturerPN == null) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(Constants.PN_OR_CMSCODE_MUST_BE_FILLED));
        }
        
        // validation des champs AirbusPN et CmsCode.
        // ces deux attributs sont en exclusion mutuelle.
        // il ne doivent pas être tous les deux définis
        super.validate();
    }
    
    /**
     * @return the container item if it is a board
     */
    public Board getMotherboard() {
        if (containerArticle != null && containerArticle instanceof Board) {
            return (Board) containerArticle;
        }
        else {
            return null;
        }
    }
    
    /**
     * @return the contained items if they are boards
     */
    public List<Board> getMezzanines() {
        List<Board> mezzanines = new ArrayList<Board>();
        for (Article articleUsed : getContainedArticles()) {
            if (articleUsed instanceof Board) {
                mezzanines.add((Board) articleUsed);
            }
        }
        return mezzanines;
    }
    
    @Override
    public List<? extends Article> getChildren() {
        List<Board> result = getMezzanines();
        Collections.sort(result);
        return result;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * @return the revH
     */
    public String getRevH() {
        return revH;
    }
    
    /**
     * @param revH
     *            the revH to set
     */
    public void setRevH(String revH) {
        this.revH = revH;
    }
    
    /**
     * @return the revS
     */
    public String getRevS() {
        return revS;
    }
    
    /**
     * @param revS
     *            the revS to set
     */
    public void setRevS(String revS) {
        this.revS = revS;
    }
    
    /**
     * @return the calibrated
     */
    public Boolean getCalibrated() {
        return calibrated;
    }
    
    /**
     * @param calibrated
     *            the calibrated to set
     */
    public void setCalibrated(Boolean calibrated) {
        this.calibrated = calibrated;
    }
    
    public String getMacAddress() {
        return macAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    /**
     * @return the bootLoader
     */
    public String getBootLoader() {
        return bootLoader;
    }
    
    /**
     * @param bootLoader
     *            the bootLoader to set
     */
    public void setBootLoader(String bootLoader) {
        this.bootLoader = bootLoader;
    }
    
    /**
     * @return the ports
     */
    public List<CommunicationPort> getPorts() {
        return ports;
    }
    
    /**
     * @param ports
     *            the ports to set
     */
    public void setPorts(List<CommunicationPort> ports) {
        this.ports = ports;
    }
    
    /**
     * @return the container
     */
    public Contains_Rack_Board getContainerOrmRack() {
        return containerOrmRack;
    }
    
    /**
     * @param container
     *            the container to set
     */
    public void setContainerOrmRack(Contains_Rack_Board container) {
        containerOrmRack = container;
    }
    
    /**
     * @return the containerPC
     */
    public Contains_PC_Board getContainerOrmPC() {
        return containerOrmPC;
    }
    
    /**
     * @param containerPC
     *            the containerPC to set
     */
    public void setContainerOrmPC(Contains_PC_Board containerPC) {
        containerOrmPC = containerPC;
    }
    
    /**
     * @return the activeStockControlDate
     */
    public Date getActiveStockControlDate() {
        return activeStockControlDate;
    }
    
    /**
     * @param pActiveStockControlDate
     *            the activeStockControlDate to set
     */
    public void setActiveStockControlDate(Date pActiveStockControlDate) {
        activeStockControlDate = pActiveStockControlDate;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Board;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Board;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Board;
    }
    
}
