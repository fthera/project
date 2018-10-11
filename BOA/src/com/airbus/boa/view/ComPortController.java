/*
 * ------------------------------------------------------------------------
 * Class : ComPortController
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StringUtil;

/**
 * Controller managing the communication ports creation, modification and
 * deletion
 */
@ManagedBean(name = ComPortController.BEAN_NAME)
@ViewScoped
public class ComPortController extends AbstractController {
    
    private enum Mode {
        CREATE,
        UPDATE
    }
    
    private Mode mode = Mode.CREATE;
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "comPortController";

    @EJB
    private ValueListBean valueListBean;
    
    /* Fields used for the port creation/update */
    
    private CommunicationPort port;
    private String portName;
    private String ipAddress;
    private Boolean fixedIP;
    private String macAddress;
    private Long networkId;
    private String socket;
    private String comment;
    private String mask;
    
    /** Index of selected port for modification */
    private int indexSelection = -1;
    
    /** List of ports, provided by PCController */
    private List<CommunicationPort> ports = new ArrayList<CommunicationPort>();
    
    /** List of ports, linked to ports, displayed in the table */
    private DataModel<CommunicationPort> dataPortList =
            new ListDataModel<CommunicationPort>();
    
    /** Maximum number of managed ports */
    private final int nbMaxPorts = 10;
    
    /** List of user modifications to validate */
    private Map<String, Action> changes = new HashMap<String, Action>();
    
    // managing of partial update of the datatable
    private Set<Integer> keys = new HashSet<Integer>();
    
    /**
     * Create a field modification for the communication modification and add it
     * into the current modifications list
     */
    private void AddPortChangeInList(String property, String newValue,
            String oldValue, String pPortName, String login) {
        if (StringUtil.isEmptyOrNull(newValue)
                && StringUtil.isEmptyOrNull(oldValue)) {
            return;
        }
        
        if (newValue == null || oldValue == null || !newValue.equals(oldValue)) {
            // creation of comment associated to the action
            String msg =
                    MessageBundle.getMessageDefault(Constants.Name) + ": "
                            + pPortName;
            Comment modificationComment = new Comment(msg);
            // creation of an action associated to the modified value
            FieldModification newModif =
                    new FieldModification(login, null, Constants.ModifyPort,
                            modificationComment, property, oldValue, newValue);
            // addition to the modifications list
            changes.put(property + " " + pPortName, newModif);
        }
    }
    
    /**
     * Create a new port and add it to the list
     */
    public void doAdd() {
        
        try {
            // At least one field must be filled
            if (StringUtil.isEmptyOrNull(portName)
                    && StringUtil.isEmptyOrNull(ipAddress)
                    && StringUtil.isEmptyOrNull(macAddress)) {
                Utils.addFacesMessage(NavigationConstants.PORT_MODAL_ERROR_ID,
                        MessageBundle.getMessage(
                                Constants.PC_PORT_AT_LEAST_ONE_FIELD));
            }
            else if (ports == null) {
                throw new IllegalArgumentException("ports NULL");
            }
            else {
                setPortAttributes();
                
                if (!existingPort(port)) {
                    // add in the list if not yet into it
                    ports.add(port);
                }
                else {
                    Utils.addFacesMessage(
                            NavigationConstants.PORT_MODAL_ERROR_ID,
                            MessageBundle.getMessage(
                                    Constants.PC_PORT_ALREADY_EXISTS));
                }
            }
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.PORT_MODAL_ERROR_ID,
                    "ERREUR AJOUT PORT : " + ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Remove the selected port in table from the list
     */
    public void doDelete() {
        try {
            ports.remove(getDataPortList().getRowIndex());
        }
        catch (Exception e) {
            Utils.addFacesMessage(null, ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Reset the port creation fields
     */
    public void doReset() {
        initAttributeFromItem();
    }
    
    /**
     * Update the modified port in the list
     */
    public void doUpdate() {
        
        if (StringUtil.isEmptyOrNull(portName)
                && StringUtil.isEmptyOrNull(ipAddress)
                && StringUtil.isEmptyOrNull(macAddress)) {
            Utils.addFacesMessage(NavigationConstants.PORT_MODAL_ERROR_ID,
                    MessageBundle
                            .getMessage(Constants.PC_PORT_AT_LEAST_ONE_FIELD));
        }
        else {
            setPortAttributes();
            
            try {
                
                ports.set(indexSelection, port);
                
                getKeys().clear();
                getKeys().add(indexSelection);
                
            } catch (ValidationException e) {
                Utils.addFacesMessage(NavigationConstants.PORT_MODAL_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
        }
    }
    
    private boolean existingPort(CommunicationPort searchedPort) {
        
        for (CommunicationPort port : ports) {
            if (port.getName().equals(searchedPort.getName())
                    && port.getIpAddress().equals(searchedPort.getIpAddress())
                    && port.getMacAddress()
                            .equals(searchedPort.getMacAddress())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Initialize the list of ports
     */
    @PostConstruct
    public void init() {
        initData();
    }
    
    /**
     * Initialize the list of ports with the ports attribute
     */
    private void initData() {
        
        dataPortList.setWrappedData(ports);
    }
    
    /**
     * Initialize the fields of the port update modal with the selected port
     * values
     * 
     * @param pEvent
     *            <i>mandatory for ActionListener</i>
     */
    public void prepareUpdate(ActionEvent pEvent) {
        // Store index of the port to update
        indexSelection = getDataPortList().getRowIndex();
        port = getDataPortList().getRowData();
        mode = Mode.UPDATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the fields of the port add modal
     * 
     * @param pEvent
     *            <i>mandatory for ActionListener</i>
     */
    public void prepareAdd(ActionEvent pEvent) {
        port = new CommunicationPort();
        mode = Mode.CREATE;
        initAttributeFromItem();
    }
    
    /**
     * Initialize the controller fields from the given port
     */
    public void initAttributeFromItem() {
        if (port != null) {
            portName = port.getName();
            ipAddress = port.getIpAddress();
            fixedIP = port.getFixedIP();
            macAddress = port.getMacAddress();
            socket = port.getSocket();
            comment = port.getComment();
            mask = port.getMask();
            if (port.getNetwork() != null) {
                networkId = port.getNetwork().getId();
            }
            else {
                networkId = null;
            }
        }
    }
    
    /**
     * Set the port attributes with the controller field values
     */
    public void setPortAttributes() {
        port.setName(portName);
        port.setIpAddress(ipAddress);
        port.setFixedIP(fixedIP);
        port.setMacAddress(macAddress);
        port.setSocket(socket);
        port.setComment(comment);
        port.setMask(mask);
        port.setNetwork(getNetworkValue(networkId));
        port.setSocket(socket);
    }
    
    /**
     * Compare the list of ports currently in the database and new ports list to
     * create actions associated to the performed modifications
     * 
     * @param pOldPorts
     *            the ports list existing in database
     * @param pLogin
     *            the user login
     * @return a map containing the ports name as keys and the actions as values
     */
    public Map<String, Action> GetChangesList(List<CommunicationPort> pOldPorts,
            String pLogin) {
        // Reset the modifications list
        changes.clear();
        
        // Browse the previous ports list
        for (CommunicationPort lOldPort : pOldPorts) {
            // Search port into the new list
            Boolean lFound = false;
            for (CommunicationPort lPort : ports) {
                if (lOldPort.getId().equals(lPort.getId())) {
                    // Update the modifications list
                	
                	//if the old and new port both stats with _tmp, do not trace comments.
                    if (!lPort.getName().startsWith(Constants.ComPortNameToSkip)
                            && !lOldPort.getName()
                                    .startsWith(Constants.ComPortNameToSkip)) {
                        AddPortChangeInList(Constants.Name, lPort.getName(),
                                lOldPort.getName(), lPort.getName(), pLogin);
                	}
                    AddPortChangeInList(Constants.IPAddress,
                            lPort.getIpAddress(), lOldPort.getIpAddress(),
                            lPort.getName(), pLogin);
                    AddPortChangeInList(Constants.FixedIP,
                            lPort.getFixedIPString(),
                            lOldPort.getFixedIPString(), lPort.getName(),
                            pLogin);
                    AddPortChangeInList(Constants.Mask,
                            lPort.getMask(), lOldPort.getMask(),
                            lPort.getName(), pLogin);
                    AddPortChangeInList(Constants.MACAddress,
                            lPort.getMacAddress(), lOldPort.getMacAddress(),
                            lPort.getName(), pLogin);
                    AddPortChangeInList(Constants.Socket, lPort.getSocket(),
                            lOldPort.getSocket(), lPort.getName(), pLogin);
                    AddPortChangeInList(Constants.Comment, lPort.getComment(),
                            lOldPort.getComment(), lPort.getName(), pLogin);
                    String oldNetworkName =
                            (lOldPort.getNetwork() == null) ? "" : lOldPort
                                    .getNetwork().getDefaultValue();
                    String networkName =
                            (lPort.getNetwork() == null) ? "" : lPort
                                    .getNetwork().getDefaultValue();
                    AddPortChangeInList(Constants.Network, networkName,
                            oldNetworkName, lPort.getName(), pLogin);
                    
                    lFound = true;
                    break;
                }
            }
            if (!lFound) {
                // The port has been deleted
                // Update the modifications list
                // Name of the deleted port as comment associated to the action
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Name) + ": "
                                + lOldPort.getName();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null,
                        Constants.RemovePort, lModificationComment);
                // Add to the current modifications list
                changes.put(lOldPort.getName(), lNewModif);
            }
        }
        
        // List of ports modified by user
        for (CommunicationPort lPort : ports) {
            if (lPort.getId() == null) {
                // Added port
                // Update the modifications list
                // Name of the added port as comment associated to the action
                String lMsg =
                        MessageBundle.getMessageDefault(Constants.Name) + ": "
                                + lPort.getName();
                Comment lModificationComment = new Comment(lMsg);
                Action lNewModif = new Action(pLogin, null, Constants.AddPort,
                        lModificationComment);
                // Add to the current modifications list
                changes.put(lPort.getName(), lNewModif);
            }
        }
        
        return changes;
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
     * @return the dataPortList
     */
    public DataModel<CommunicationPort> getDataPortList() {
        return dataPortList;
    }
    
    /**
     * @param dataPortList
     *            the dataPortList to set
     */
    public void setDataPortList(DataModel<CommunicationPort> dataPortList) {
        this.dataPortList = dataPortList;
    }
    
    /**
     * @return the indexSelection
     */
    public int getIndexSelection() {
        return indexSelection;
    }
    
    /**
     * @param indexSelection
     *            the indexSelection to set
     */
    public void setIndexSelection(int indexSelection) {
        this.indexSelection = indexSelection;
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
     * @return the keys
     */
    public Set<Integer> getKeys() {
        return keys;
    }
    
    /**
     * @param keys
     *            the keys to set
     */
    public void setKeys(Set<Integer> keys) {
        this.keys = keys;
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
     * @return the nbMaxPorts
     */
    public int getNbMaxPorts() {
        return nbMaxPorts;
    }
    
    /**
     * @return the networkId
     */
    public Long getNetworkId() {
        return networkId;
    }
    
    /**
     * @param networkId
     *            the networkId to set
     */
    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }
    
    /**
     * Build the list of existing networks names, depending on the locale
     * 
     * @return the networksNames
     */
    public List<SelectItem> getNetworksNames() {
        return valueListBean.generateSelectItems(Network.class);
    }
    
    /**
     * Retrieve the Network corresponding to the provided Id in database
     * 
     * @param pNetworkId
     *            the id of the network to find
     * @return the found Network, else null
     */
    private Network getNetworkValue(Long pNetworkId) {
        Network result = null;
        if (pNetworkId != null) {
            result =
                    valueListBean.findAttributeValueListById(Network.class,
                            pNetworkId);
        }
        return result;
    }
    
    /**
     * @return the portName
     */
    public String getPortName() {
        return portName;
    }
    
    /**
     * @param portName
     *            the portName to set
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }
    
    /**
     * @return the ports
     */
    public List<CommunicationPort> getPorts() {
        return ports;
    }
    
    /**
     * Initialize the list of ports to display and reset the new port fields.
     * 
     * @param ports
     *            the ports to set
     */
    public void setPorts(List<CommunicationPort> ports) {
        this.ports = ports;
        initData();
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
     * @return the mask
     */
    public String getMask() {
		return mask;
	}

    /**
     * @param mask the mask to set.
     */
	public void setMask(String mask) {
		this.mask = mask;
    }
    
    /**
     * @return a boolean indicating if the port is in creation mode
     */
    public boolean isCreateMode() {
        switch (mode) {
        case CREATE:
            return true;
        case UPDATE:
        default:
            return false;
        }
    }
    
    /**
     * @return a boolean indicating if the port is in update mode
     */
    public boolean isUpdateMode() {
        switch (mode) {
        case UPDATE:
            return true;
        case CREATE:
        default:
            return false;
        }
    }
}
