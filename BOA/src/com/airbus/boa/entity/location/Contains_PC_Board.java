/*
 * ------------------------------------------------------------------------
 * Class : Contains_PC_Board
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.location;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.exception.ValidationException;

/**
 * Entity implementation class for relation: Board linked into a PC
 */
@Entity
public class Contains_PC_Board extends ContainerOrm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * @param slot
     *            the slot
     * @return the slot in string form, or null
     */
    public static String convertSlotToString(Integer slot) {
        String result;
        if (slot == null) {
            return null;
        }
        result = "" + slot;
        return result;
    }
    
    /**
     * @param ligne
     *            the slot in string form
     * @return an array containing at index 0 the slot integer and at index 1
     *         null
     */
    public static Object[] convertStringToSlot(String ligne) {
        Object[] result = new Object[] { null, null };
        
        if (ligne == null) {
            throw new ValidationException(
                    "LE FORMAT NUMERO SLOT EST INCORRECTE");
        }
        
        try {
            Integer slot = Integer.parseInt(ligne);
            result[0] = slot;
        }
        catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new ValidationException(e);
        }
        
        return result;
    }
    
    private Integer slotNumber;
    
    @OneToOne
    private Board board;
    
    @ManyToOne
    private PC pc;
    
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
     * @return the pc
     */
    public PC getPc() {
        return pc;
    }
    
    /**
     * @param pc
     *            the pc to set
     */
    public void setPc(PC pc) {
        this.pc = pc;
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
