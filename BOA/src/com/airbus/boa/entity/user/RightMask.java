/*
 * ------------------------------------------------------------------------
 * Class : RightMask
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

/**
 * Enumerate of available right masks for CRUD actions
 */
public class RightMask {
    
    /** Bitwise code for no granted right */
    // 0b00000000
    public static final byte NONE = (byte) 0;
    
    /** Bitwise code for granting all rights */
    // 0b01111111
    // Covers all possible values of mask (even if not defined...)
    public static final byte ALL = (byte) 127;
    
    /**
     * @param pRights
     *            the bitwise code for granted rights
     * @param pRightToCheck
     *            the bitwise code of the mask to apply (rights to check)
     * @return true if the mask rights are all granted, else false
     */
    public static boolean isAllowed(byte pRights, byte pRightToCheck) {
        return (pRights & pRightToCheck) == pRightToCheck;
    }
    
    /**
     * @param pRights
     *            the bitwise code of the current granted rights
     * @param pRightToAdd
     *            the bitwise code of the right to add
     * @return the updated granted rights
     */
    public static byte addRight(byte pRights, byte pRightToAdd) {
        return (byte) (pRights | pRightToAdd);
    }
    
}
