/*
 * ------------------------------------------------------------------------
 * Class : Contains_Inst_Cabinet
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Cabinet;

/**
 * Entity implementation class for relation: Cabinet linked into Installation
 */
@Entity
public class Contains_Inst_Cabinet extends ContainerOrm implements Serializable {
    
    private static final long serialVersionUID = 2237525712161168417L;
    
    /** The letter column name */
    public static final String LETTER_COLUMN_NAME = "LETTER";
    
    @Column
    private String letter;
    
    @OneToOne
    private Cabinet cabinet;
    
    @ManyToOne
    private Installation installation;
    
    /**
     * @return the letter
     */
    public String getLetter() {
        return letter;
    }
    
    /**
     * @param letter
     *            the letter to set
     */
    public void setLetter(String letter) {
        this.letter = letter;
    }
    
    /**
     * @return the cabinet
     */
    public Cabinet getCabinet() {
        return cabinet;
    }
    
    /**
     * @param cabinet
     *            the cabinet to set
     */
    public void setCabinet(Cabinet cabinet) {
        this.cabinet = cabinet;
    }
    
    /**
     * @return the installation
     */
    public Installation getInstallation() {
        return installation;
    }
    
    /**
     * @param installation
     *            the installation to set
     */
    public void setInstallation(Installation installation) {
        this.installation = installation;
    }
    
}
