/*
 * ------------------------------------------------------------------------
 * Class : ReminderStatus
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.reminder;

import com.airbus.boa.util.MessageBundle;

/**
 * Enumerate of possible reminder status for sending remind
 */
public enum ReminderStatus {
    
    /**
     * When a remind is to be sent until it is completed.
     */
    UntilCompleted,
    /**
     * When a remind is to be sent only once.
     */
    Once,
    /**
     * When no remind is to be sent.
     */
    NoMoreSent;
    
    @Override
    public String toString() {
        return MessageBundle.getMessage(getMessageBundleKey());
    }
    
    /**
     * @return the Message Bundle key of this state
     */
    private String getMessageBundleKey() {
        return "ReminderStatus" + name();
    }
    
}
