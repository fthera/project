/*
 * ------------------------------------------------------------------------
 * Class : NetworkInterface
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.network;

import java.io.Serializable;

import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * @author ng0057cf
 */
public class NetworkInterface implements Serializable {
    
    public enum InterfaceType {
        Internal,
        External
    }
    
    private static final long serialVersionUID = 1L;
    
    private InterfaceType interfaceType;
    
    private CommunicationPort port;
    
    private Integer slotNumber;
    
    public NetworkInterface(CommunicationPort port) {
        interfaceType = InterfaceType.Internal;
        slotNumber = null;
        this.port = port;
    }
    
    public NetworkInterface(CommunicationPort port, Integer slotNumber) {
        interfaceType = InterfaceType.External;
        this.slotNumber = slotNumber;
        this.port = port;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return port.getComment();
    }
    
    /**
     * @return the interfaceType
     */
    public InterfaceType getInterfaceType() {
        return interfaceType;
    }
    
    public String getInterfaceTypeText() {
        // Conversion du type d'interface en chaîne de caractères dépendant de
        // la langue choisie
        String msg =
                MessageBundle
                        .getMessage((interfaceType == InterfaceType.Internal) ? Constants.Internal
                                : Constants.External);
        return msg;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return port.getName();
    }
    
    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return port.getIpAddress();
    }
    
    /**
     * @return the fixedIP
     */
    public Boolean getFixedIP() {
        return port.getFixedIP();
    }
    
    /**
     * @return the mask
     */
    public String getMask() {
    	return port.getMask();
    }
    
    /**
     * @return the macAddress
     */
    public String getMacAddress() {
        return port.getMacAddress();
    }
    
    /**
     * @return the network
     */
    public Network getNetwork() {
        return port.getNetwork();
    }
    
    /**
     * @return the socket
     */
    public String getSocket() {
        return port.getSocket();
    }
    
    /**
     * @return the slotNumber
     */
    public Integer getSlotNumber() {
        return slotNumber;
    }
    
    public CommunicationPort getPort() {
        return port;
    }

}
