/*
 * ------------------------------------------------------------------------
 * Class : Contains_Rack_Board
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;

/**
 * Entity implementation class for relation: Board linked into Rack
 */
@Entity
public class Contains_Rack_Board extends ContainerOrm implements Serializable {
    
    /**
     * Enumerate listing the faces to install a board in a rack
     */
    public enum Face {
        /** When the board is at the front of the rack */
        Front,
        /** When the board is at the rear of the rack */
        Rear
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The face column name */
    public static final String FACE_COLUMN_NAME = "FACE";
    
    /**
     * @param slot
     *            the slot
     * @param face
     *            the face
     * @return the slot and face combined in string form, or null
     */
    public static String convertSlotFaceToString(Integer slot, Face face) {
        String result;
        if (slot == null) {
            return null;
        }
        result = "" + slot;
        if (face != null) {
            if (face == Face.Front) {
                result += "f";
            }
            else {
                result += "r";
            }
        }
        return result;
    }
    
    /**
     * @param ligne
     *            the slot and face combined in string form
     * @return an array containing at index 0 the slot integer and at index 1
     *         the Face enumerate (or null)
     */
    public static Object[] convertStringToSlotFace(String ligne) {
        Object[] result = new Object[] { null, null };
        if (ligne == null
                || !ligne.matches(Constants.REGEX_SLOTNUMBER_AND_POSITION)) {
            throw new ValidationException(
                    "LE FORMAT NUMERO SLOT ET FACE EST INCORRECTE");
        }
        
        // on recupère la première position seulement
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(ligne);
        matcher.find();
        String slotString = null;
        
        try {
            slotString = ligne.substring(matcher.start(), matcher.end());
            Integer slot = Integer.parseInt(slotString);
            result[0] = slot;
        }
        catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new ValidationException(e);
        }
        
        // if the second number exists, it is not interpreted
        // 20-21 av => slotNumber = 20 and Face = Front
        
        Pattern p = Pattern.compile("(?i)av|f|avant|front");
        Matcher matchFace = p.matcher(ligne);
        if (matchFace.find()) {
            result[1] = Face.Front;
        }
        else {
            p = Pattern.compile("(?i)ar|r|arri[èe]re|rear");
            matchFace = p.matcher(ligne);
            if (matchFace.find()) {
                result[1] = Face.Rear;
            }
        }
        
        return result;
    }
    
    @Column
    @Enumerated(EnumType.STRING)
    private Face face;
    
    @Column
    private Integer slotNumber;
    
    @OneToOne
    private Board board;
    
    @ManyToOne
    private Rack rack;
    
    /**
     * @return the board
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * @param board
     *            the board to set
     */
    public void setBoard(Board board) {
        this.board = board;
    }
    
    /**
     * @return the rack
     */
    public Rack getRack() {
        return rack;
    }
    
    /**
     * @param rack
     *            the rack to set
     */
    public void setRack(Rack rack) {
        this.rack = rack;
    }
    
    /**
     * @return the face
     */
    public Face getFace() {
        return face;
    }
    
    /**
     * @param face
     *            the face to set
     */
    public void setFace(Face face) {
        this.face = face;
    }
    
    /**
     * @return the slotNumber
     */
    public Integer getSlotNumber() {
        return slotNumber;
    }
    
    /**
     * @param slotNumber
     *            the slotNumber to set
     */
    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }
    
}
