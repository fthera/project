/*
 * ------------------------------------------------------------------------
 * Class : RightMaskCRUD
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of available right masks for CRUD actions
 */
public enum RightMaskCRUD {
    
    /** Bitwise mask for Create CRUD action */
    // 0b00000001
    CRUD_Create((byte) 1),
    
    /** Bitwise mask for Read CRUD action */
    // 0b00000010
    CRUD_Read((byte) 2),
    
    /** Bitwise mask for Update CRUD action */
    // 0b00000100
    CRUD_Update((byte) 4),
    
    /** Bitwise mask for Delete CRUD action */
    // 0b00001000
    CRUD_Delete((byte) 8);
    
    /**
     * @param pString
     *            the right mask string returned by the
     *            <b>getStringValue</b> method
     * @return the right mask if the provided string is available, else null
     */
    public static RightMaskCRUD getEnumValue(String pString) {
        
        RightMaskCRUD[] lValues = RightMaskCRUD.values();
        List<String> lBundleKeys = new ArrayList<String>();
        
        for (RightMaskCRUD lRightMask : lValues) {
            lBundleKeys.add(lRightMask.getMessageBundleKey());
        }
        
        Map<String, String> lMap =
                MessageBundle.getAllDefaultMessages(lBundleKeys);
        
        for (RightMaskCRUD lRightMask : RightMaskCRUD.values()) {
            
            String lMessage = lMap.get(lRightMask.getMessageBundleKey());
            
            if (lMessage != null && lMessage.equals(pString)) {
                return lRightMask;
            }
        }
        return null;
    }
    
    private byte bitwiseCode;
    
    private RightMaskCRUD(byte pBitwiseCode) {
        bitwiseCode = pBitwiseCode;
    }
    
    /**
     * @return the Message Bundle key of this right mask
     */
    private String getMessageBundleKey() {
        return "RightMaskCRUD" + name();
    }
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the right mask string which can be then converted again to
     *         the right mask with the <b>getEnumValue</b> method.
     */
    public String getStringValue() {
        return MessageBundle.getMessageDefault(getMessageBundleKey());
    }
    
    /**
     * @return the bitwiseCode
     */
    public byte getBitwiseCode() {
        return bitwiseCode;
    }
    
}
