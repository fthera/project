/*
 * ------------------------------------------------------------------------
 * Class : CommunicationPort
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.network;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.airbus.boa.entity.EntityBase;
import com.airbus.boa.entity.valuelist.pc.Network;

/**
 * Entity implementation class for Entity: CommunicationPort
 */
@Entity
@Table(name = CommunicationPort.TABLE_NAME)
public class CommunicationPort implements EntityBase, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "communicationport";
    /** The IP address column name */
    public static final String IPADDRESS_COLUMN_NAME = "IPADDRESS";
    /** The Mac address column name */
    public static final String MACADDRESS_COLUMN_NAME = "MACADDRESS";
    /** The name column name */
    public static final String NAME_COLUMN_NAME = "NAME";
    /** The socket column name */
    public static final String SOCKET_COLUMN_NAME = "SOCKET";
    /** The comment column name */
    public static final String COMMENT_COLUMN_NAME = "COMMENTS";
    /** The mask column name */
    public static final String MASK_COLUMN_NAME = "MASK";
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private String name = "";
    
    @Column
    private String ipAddress;
    
    @Column
    private Boolean fixedIP;
    
    @Column
    private String macAddress;
    
    @OneToOne
    private Network network;
    
    @Column
    private String socket;
    
    @Column(name = COMMENT_COLUMN_NAME)
    private String comment;
    
    @Column
    private String mask;
    
    /**
     * Default constructor
     */
    public CommunicationPort() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param name
     *            the name
     * @param ipAddress
     *            the IP address
     * @param macAddress
     *            the MAC address
     */
    public CommunicationPort(String name, String ipAddress, String macAddress) {
        super();
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result =
                prime * result
                        + ((macAddress == null) ? 0 : macAddress.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((network == null) ? 0 : network.hashCode());
        result = prime * result + ((socket == null) ? 0 : socket.hashCode());
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommunicationPort other = (CommunicationPort) obj;
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        }
        else if (!ipAddress.equals(other.ipAddress)) {
            return false;
        }
        if (fixedIP == null) {
            if (other.fixedIP != null) {
                return false;
            }
        }
        else if (!fixedIP.equals(other.fixedIP)) {
            return false;
        }
        if (macAddress == null) {
            if (other.macAddress != null) {
                return false;
            }
        }
        else if (!macAddress.equals(other.macAddress)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (network == null) {
            if (other.network != null) {
                return false;
            }
        }
        else if (!network.equals(other.network)) {
            return false;
        }
        if (socket == null) {
            if (other.socket != null) {
                return false;
            }
        }
        else if (!socket.equals(other.socket)) {
            return false;
        }

        if (mask == null) {
            if (other.mask != null) {
                return false;
            }
        }
        else if (!mask.equals(other.mask)) {
            return false;
        }
  
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        }
        else if (!comment.equals(other.comment)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        
        String str =
                "[" + name + ",IP:" + ipAddress + ",MAC:" + macAddress + ",";
        if (network != null) {
            str += network.getDefaultValue();
        }
        else {
            str += " ";
        }
        
        str += "," + socket + "," + mask + "," + comment + "]";
        
        return str;
    }
    
    /**
     * @param obj
     *            the object to compare
     * @return true if the provided object is a CommunicationPort with the
     *         ipAddress, macAddress and name equal to this one, else false
     */
    public boolean equalsOnIpMacName(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommunicationPort other = (CommunicationPort) obj;
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        }
        else if (!ipAddress.equals(other.ipAddress)) {
            return false;
        }
        if (macAddress == null) {
            if (other.macAddress != null) {
                return false;
            }
        }
        else if (!macAddress.equals(other.macAddress)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    @Override
    public Long getId() {
        return id;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * @param ipAddress
     *            the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getFixedIPString() {
        if (fixedIP != null) {
            return fixedIP ? "yes" : "no";
        }
        else {
            return "no";
        }
    }
    
    /**
     * @return the fixedIP
     */
    public Boolean getFixedIP() {
        return fixedIP;
    }
    
    /**
     * @param pFixedIP
     *            the fixedIP to set
     */
    public void setFixedIP(Boolean pFixedIP) {
        fixedIP = pFixedIP;
    }
    
    /**
     * @return the macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }
    
    /**
     * @param macAddress
     *            the macAddress to set
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    /**
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }
    
    /**
     * @param network
     *            the network to set
     */
    public void setNetwork(Network network) {
        this.network = network;
    }
    
    /**
     * @return the socket
     */
    public String getSocket() {
        return socket;
    }
    
    /**
     * @param socket
     *            the socket to set
     */
    public void setSocket(String socket) {
        this.socket = socket;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param pComment
     *            the comment to set
     */
    public void setComment(String pComment) {
        comment = pComment;
    }

    /**
     * @return the mask
     */
	public String getMask() {
		return mask;
	}
	
	/**
	 *  
	 * @param mask the mask to set
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}
     
}
