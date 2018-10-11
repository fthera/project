/*
 * ------------------------------------------------------------------------
 * Class : AutoUpdateController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.poi.util.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AutoUpdateType;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.memory.MemorySlot;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.AutoUpdateException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.StreamGobbler;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.application.DBConstants;
import com.airbus.boa.view.item.PCController;
import com.airbus.boa.view.item.SoftwareController;

/**
 * Controller managing PC update using Sysmon tool
 */
@ManagedBean(name = AutoUpdateController.BEAN_NAME)
@RequestScoped
public class AutoUpdateController extends AbstractController {
    
    /**
     * Thread connecting to the PC and executing SYSMON on it
     */
    private class SysmonExecution extends Thread {
        
        private static final String LAUNCH_SYSMON_PATH =
                "com/airbus/boa/resources/sysmon/launch_sysmon.ksh";
        
        private String pcName;
        private String processNb = null;
        private Integer exitValue = null;
        private Process process = null;
        private InterruptedException interruptedException = null;
        private IOException ioException = null;
        
        /**
         * Constructor
         * 
         * @param pPCName
         *            the PC name
         */
        public SysmonExecution(String pPCName) {
            pcName = pPCName;
        }
        
        /**
         * @return the process exit value (may be null)
         * @throws InterruptedException
         *             when the process encountered an exception during
         *             execution
         * @throws IOException
         *             when the process encountered an exception during
         *             execution
         */
        public Integer getExitValue() throws InterruptedException, IOException {
            if (interruptedException != null) {
                throw interruptedException;
            }
            else if (ioException != null) {
                throw ioException;
            }
            return exitValue;
        }
        
        /**
         * @return the process inputStream, or null
         */
        public InputStream getInputStream() {
            if (process != null) {
                return process.getInputStream();
            }
            else {
                return null;
            }
        }
        
        @Override
        public void interrupt() {
            // Destroy the Java process
            if (process != null) {
                process.destroy();
            }
            
            try {
                // Kill the linux process
                Runtime.getRuntime().exec("kill -9 " + processNb);
            }
            catch (IOException e) {
                // Nothing to do
            }
            
            super.interrupt();
        }
        
        @Override
        public void run() {
            
            ClassLoader lClassLoader = getClass().getClassLoader();
            URL lURL = lClassLoader.getResource(LAUNCH_SYSMON_PATH);
            String lScriptFilePath = lURL.getFile();
            // Ensure that the script file is executable
            File lScriptFile = new File(lScriptFilePath);
            lScriptFile.setExecutable(true);
            
            ProcessBuilder lProcessBuilder =
                    new ProcessBuilder(lScriptFilePath, pcName);
            try {
                process = lProcessBuilder.start();
                
                BufferedReader lBufferedReader =
                        new BufferedReader(new InputStreamReader(
                                process.getInputStream()));
                // retrieve the process number
                processNb = lBufferedReader.readLine();
                
                exitValue = process.waitFor();
                
            }
            catch (InterruptedException e) {
                // store the exception for later treatment
                interruptedException = e;
                
            }
            catch (IOException e) {
                // store the exception for later treatment
                ioException = e;
            }
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "autoUpdateController";
    
    /** SYSMON/version */
    @SuppressWarnings("unused")
    private static final String ATT_VERSION = "version";
    
    /** SYSMON/infos element */
    private static final String ELT_INFOS = "infos";
    
    /** SYSMON/infos/sysmon_version */
    private static final String ATT_INFOS_VERSION = "sysmon_version";
    /** SYSMON/infos/date */
    private static final String ATT_INFOS_DATE = "date";
    /** SYSMON/infos/time */
    private static final String ATT_INFOS_TIME = "time";
    /** SYSMON/infos/user */
    @SuppressWarnings("unused")
    private static final String ATT_INFOS_USER = "user";
    /** SYSMON/infos/sudo_user */
    @SuppressWarnings("unused")
    private static final String ATT_INFOS_SUDO = "sudo_user";
    
    /** SYSMON/host element */
    private static final String ELT_HOST = "host";
    
    /** SYSMON/host/hostname */
    private static final String ATT_HOST_HOSTNAME = "hostname";
    /** SYSMON/host/domain */
    private static final String ATT_HOST_DOMAIN = "domain";
    
    /** SYSMON/hardware element */
    private static final String ELT_HARDWARE = "hardware";
    
    /** SYSMON/hardware/cpu element */
    private static final String ELT_HARDWARE_CPU = "cpu";
    
    /** SYSMON/hardware/cpu/type */
    private static final String ATT_HARDWARE_CPU_TYPE = "type";
    /** SYSMON/hardware/cpu/max_speed */
    private static final String ATT_HARDWARE_CPU_MAXSPEED = "max_speed";
    /** SYSMON/hardware/cpu/sockets */
    private static final String ATT_HARDWARE_CPU_SOCKETS = "sockets";
    /** SYSMON/hardware/cpu/core_per_socket */
    private static final String ATT_HARDWARE_CPU_COREPERSOCKET =
            "core_per_socket";
    /** SYSMON/hardware/cpu/logical_cpu_per_socket */
    private static final String ATT_HARDWARE_CPU_LOGICALCPUPERSOCKET =
            "logical_cpu_per_socket";
    /** SYSMON/hardware/cpu/total_logical_cpus */
    private static final String ATT_HARDWARE_CPU_TOTALLOGICALCPUS =
            "total_logical_cpus";
    /**
     * SYSMON/hardware/cpu/hyperthreading
     * Restricted values: disabled, enabled
     */
    private static final String ATT_HARDWARE_CPU_HYPERTHREADING =
            "hyperthreading";
    
    /** SYSMON/hardware/memory element */
    private static final String ELT_HARDWARE_MEMORY = "memory";
    
    /** SYSMON/hardware/memory/slot element */
    private static final String ELT_HARDWARE_MEMORY_SLOT = "slot";
    
    /** SYSMON/hardware/memory/slot/name */
    private static final String ATT_HARDWARE_MEMORY_SLOT_NAME = "name";
    /** SYSMON/hardware/memory/slot/used */
    private static final String ATT_HARDWARE_MEMORY_SLOT_USED = "used";
    /** SYSMON/hardware/memory/slot/memory_size */
    private static final String ATT_HARDWARE_MEMORY_SLOT_MEMORYSIZE =
            "memory_size";
    /** SYSMON/hardware/memory/slot/memory_type */
    private static final String ATT_HARDWARE_MEMORY_SLOT_MEMORYTYPE =
            "memory_type";
    
    /** SYSMON/hardware/memory/total_memory_size */
    private static final String ATT_HARDWARE_MEMORY_TOTALMEMORYSIZE =
            "total_memory_size";
    
    /** SYSMON/hardware/graphics element */
    private static final String ELT_HARDWARE_GRAPHICS = "graphics";
    
    /** SYSMON/hardware/graphics/card element */
    @SuppressWarnings("unused")
    private static final String ELT_HARDWARE_GRAPHICS_CARD = "card";
    
    /** SYSMON/hardware/graphics/card/name */
    @SuppressWarnings("unused")
    private static final String ATT_HARDWARE_GRAPHICS_CARD_NAME = "name";
    
    /** SYSMON/hardware/graphics/monitor element */
    private static final String ELT_HARDWARE_GRAPHICS_MONITOR = "monitor";
    
    /** SYSMON/hardware/graphics/monitor/number */
    private static final String ATT_HARDWARE_GRAPHICS_MONITOR_NUMBER = "number";
    
    /** SYSMON/hardware/devices element */
    @SuppressWarnings("unused")
    private static final String ELT_HARDWARE_DEVICES = "devices";
    
    /** SYSMON/hardware/devices/pci_device element */
    @SuppressWarnings("unused")
    private static final String ELT_HARDWARE_DEVICES_PCIDEVICE = "pci_device";
    
    /** SYSMON/hardware/devices/pci_device/name */
    @SuppressWarnings("unused")
    private static final String ATT_HARDWARE_DEVICES_PCIDEVICE_NAME = "name";
    /** SYSMON/hardware/devices/pci_device/sn */
    @SuppressWarnings("unused")
    private static final String ATT_HARDWARE_DEVICES_PCIDEVICE_SN = "sn";
    
    /** SYSMON/hardware/manufacturer */
    private static final String ATT_HARDWARE_MANUFACTURER = "manufacturer";
    /** SYSMON/hardware/product_name */
    private static final String ATT_HARDWARE_PRODUCTNAME = "product_name";
    /** SYSMON/hardware/manufacturer_sn */
    private static final String ATT_HARDWARE_MANUFACTURERSN = "manufacturer_sn";
    /** SYSMON/hardware/platform */
    private static final String ATT_HARDWARE_PLATFORM = "platform";
    /** SYSMON/hardware/product_type */
    private static final String ATT_HARDWARE_PRODUCTTYPE = "product_type";
    /** SYSMON/hardware/article */
    @SuppressWarnings("unused")
    private static final String ATT_HARDWARE_ARTICLE = "article";
    
    /** SYSMON/operating_system element */
    private static final String ELT_OS = "operating_system";
    
    /** SYSMON/operating_system/installed_kernels element */
    private static final String ELT_OS_INSTALLEDKERNELS = "installed_kernels";
    
    /** SYSMON/operating_system/installed_kernels/kernel element */
    private static final String ELT_OS_INSTALLEDKERNELS_KERNEL = "kernel";
    
    /** SYSMON/operating_system/installed_kernels/kernel/version */
    private static final String ATT_OS_INSTALLEDKERNELS_KERNEL_VERSION =
            "version";
    /** SYSMON/operating_system/installed_kernels/default */
    private static final String ATT_OS_INSTALLEDKERNELS_DEFAULT = "default";
    
    /** SYSMON/operating_system/notable_sysconfig_keys element */
    @SuppressWarnings("unused")
    private static final String ELT_OS_NOTABLESYSCONFIGKEYS =
            "notable_sysconfig_keys";
    
    /** SYSMON/operating_system/notable_sysconfig_keys/sysconfig element */
    @SuppressWarnings("unused")
    private static final String ELT_OS_NOTABLESYSCONFIGKEYS_SYSCONFIG =
            "sysconfig";
    
    /** SYSMON/operating_system/notable_sysconfig_keys/sysconfig/key */
    @SuppressWarnings("unused")
    private static final String ATT_OS_NOTABLESYSCONFIGKEYS_SYSCONFIG_KEY =
            "key";
    /** SYSMON/operating_system/notable_sysconfig_keys/sysconfig/value */
    @SuppressWarnings("unused")
    private static final String ATT_OS_NOTABLESYSCONFIGKEYS_SYSCONFIG_VALUE =
            "value";
    
    /**
     * SYSMON/operating_system/type
     * Restricted values: Linux, Windows
     */
    private static final String ATT_OS_TYPE = "type";
    /** SYSMON/operating_system/version */
    private static final String ATT_OS_VERSION = "version";
    /** SYSMON/operating_system/has_sudo */
    @SuppressWarnings("unused")
    private static final String ATT_OS_HASSUDO = "has_sudo";
    
    /** SYSMON/networks element */
    private static final String ELT_NETWORKS = "networks";
    
    /** SYSMON/networks/interface element */
    private static final String ELT_NETWORKS_INTERFACE = "interface";
    
    /** SYSMON/networks/interface/name */
    private static final String ATT_NETWORKS_INTERFACE_NAME = "name";
    /**
     * SYSMON/networks/interface/network
     * Restricted values: IONEXT, AFDX, General, Local, ADIS
     */
    private static final String ATT_NETWORKS_INTERFACE_NETWORK = "network";
    /** SYSMON/networks/interface/MAC_address */
    private static final String ATT_NETWORKS_INTERFACE_MACADDRESS =
            "MAC_address";
    /** SYSMON/networks/interface/ipv4_address */
    private static final String ATT_NETWORKS_INTERFACE_IPV4ADDRESS =
            "ipv4_address";
    /** SYSMON/networks/interface/dhcp */
    private static final String ATT_NETWORKS_INTERFACE_DHCP = "dhcp";
    /** SYSMON/networks/interface/mask */
    private static final String ATT_NETWORKS_INTERFACE_MASK = "mask";
    /** SYSMON/networks/interface/link_detected */
    @SuppressWarnings("unused")
    private static final String ATT_NETWORKS_INTERFACE_LINKDETECTED =
            "link_detected";
    /** SYSMON/networks/interface/speed */
    @SuppressWarnings("unused")
    private static final String ATT_NETWORKS_INTERFACE_SPEED = "speed";
    
    private static final String ERROR_CONNECTION_PC =
            "Error while connecting to PC";
    private static final String ERROR_EXECUTING_SYSMON =
            "Error while executing SYSMON on PC";
    private static final String ERROR_SYSMON_TIMEOUT =
            "Timeout passed while connecting to PC or executing SYSMON on it";
    private static final String ERROR_SYSMON_FORMAT_VALIDATION =
            "Error while validating the SYSMON results format";
    private static final String ERROR_PARSING_SYSMON_OUTPUT =
            "Error while parsing the SYSMON results";
    private static final String ERROR_SAVING_PC =
            "Error while saving PC in database";
    
    /** The SYSMON tool name for history comment */
    private static final String SYSMON_TOOL_NAME = "SYSMON";
    /** The history comment when an attribute is updated by SYSMON */
    private static final String SYSMON_HISTORY_COMMENT = "automatic update";
    
    private static final String SYSMON_XSD_FILE_PATH =
            "com/airbus/boa/resources/sysmon/sysmon_schema.xsd";
    
    /** The SYSMON virtual user for updating PC */
    private User sysmonVirtualUser;
    
    private User previousLoggedUser = null;
    
    /**
     * Store the currently logged user (if any) and log in as SYSMON virtual
     * user.
     */
    public void logInAsSysmon() {
        
        // Retrieve the logged user
        LogInController lLogInController = findBean(LogInController.class);
        previousLoggedUser = lLogInController.getUserLogged();
        
        // Log the Sysmon virtual user
        lLogInController.setUserLogged(sysmonVirtualUser);
    }
    
    /**
     * Log in as previously logged user (if any)
     */
    public void logInBackAsPreviousLoggedUser() {
        
        LogInController lLogInController = findBean(LogInController.class);
        lLogInController.setUserLogged(previousLoggedUser);
    }
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private PCBean pcBean;
    
    private PCController pcController;
    
    private ComPortController comPortController;
    
    private List<StringBuffer> logs = new ArrayList<StringBuffer>();
    
    @PostConstruct
    private void init() {
        
        pcController = findBean(PCController.class);
        comPortController = findBean(ComPortController.class);
        
        Role lSuperAdminRole =
                valueListBean.findRoleByLevel(Role.SUPER_ADMINISTRATOR_LEVEL);
        
        sysmonVirtualUser = new User("SYSMON", "Tool", "SYSMON", null);
        sysmonVirtualUser.setRole(lSuperAdminRole);
    }
    
    private void logMsg(String pMsg, boolean pIsException) {
        
        StringBuffer lMsg = new StringBuffer();
        if (pIsException) {
            lMsg.append("Error: ");
        }
        else {
            lMsg.append("Info: ");
        }
        lMsg.append(pMsg);
        logs.add(lMsg);
    }
    
    /**
     * Merge the duplicated communication port into the original one
     * 
     * @param pOriginalPort
     *            the original communication port to be updated
     * @param pDuplicatedPort
     *            the duplicated communication port to be deleted
     * @return true if the communication ports have been correctly merged and
     *         the duplicated one can be removed without loss of
     *         information, else false
     */
    private boolean mergeCommunicationPorts(CommunicationPort pOriginalPort,
            CommunicationPort pDuplicatedPort) {
    	
        pOriginalPort.setName(pDuplicatedPort.getName());
        pOriginalPort.setIpAddress(pDuplicatedPort.getIpAddress());
        pOriginalPort.setFixedIP(pDuplicatedPort.getFixedIP());
        pOriginalPort.setMacAddress(pDuplicatedPort.getMacAddress());
        pOriginalPort.setNetwork(pDuplicatedPort.getNetwork());
        pOriginalPort.setMask(pDuplicatedPort.getMask());
        
        return true;
    }
    
    /**
     * Initialize the PCController for updating the provided PC according to
     * information contained in the DOM.
     * Update the PC attributes, communication ports and installed software.
     * Validate the modifications.
     * 
     * @param pPC
     *            the PC to update
     * @param pDom
     *            the DOM containing information
     * @param pSysmonSource
     *            true if the DOM has been retrieved directly from Sysmon
     *            execution, false if it has been retrieved from an imported
     *            file
     * @param pLoggedUser
     *            the logged user (not used when pSysmonSource is false)
     * @throws AutoUpdateException
     *             when a format error occurs
     */
    private void doUpdatePC(PC pPC, Document pDom, boolean pSysmonSource,
            User pLoggedUser) throws AutoUpdateException {
        
        logs.clear();
        
        boolean lSysmonVirtualUserToLogOut = false;
        
        try {
            
            // If PC is updated through Sysmon output and SYSMON virtual user is
            // not already logged, login as Sysmon virtual user
            if (pSysmonSource && !sysmonVirtualUser.equals(pLoggedUser)) {
                logInAsSysmon();
                lSysmonVirtualUserToLogOut = true;
            }
            
            // Initialize PCController for updating the PC
            
            pcController.initPc(pPC.getId());
            
            // The following managed beans must be updated manually since no
            // page is displayed:
            // - LocationController
            // - ContainerController
            // - ReminderController
            // - DocumentController
            // (These managed beans are ViewScoped and are thus not refreshed if
            // they are not loaded)
            LocationController lLocationController =
                    findBean(LocationController.class);
            lLocationController.setLocation(pPC.getLocation());
            lLocationController.setLocatedItem(pPC);
            
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            lContainerController.setContainer(pPC.getContainer());
            lContainerController.setContainedItem(pPC);
            
            ReminderController lReminderController =
                    findBean(ReminderController.class);
            lReminderController.setMode(pPC);
            
            DocumentController lDocumentController =
                    findBean(DocumentController.class);
            lDocumentController.setMode(pPC);
            
            /*
             * Retrieve data from document
             */
            
            // Retrieve the validity date of PC information
            // If an error occurs while retrieving the date, the PC is not
            // updated (AutoUpdateException is thrown) since the date would not
            // be updated in this case
            Date lDate = retrievePCValidityDate(pDom);
            
            pcController.setHistoryComment(retrieveHistoryComment(pDom));
            
            // Update the PCController attributes for updating the PC
            
            prepareUpdatePCAttributes(pDom, !pSysmonSource);
            prepareUpdateComPorts(pDom);
            prepareUpdateSoftwareList(pDom);
            
            // Validate the modifications on dedicated controller
            try {
                pcController.updateItem();
            }
            catch (Exception e) {
                throw new AutoUpdateException(ERROR_SAVING_PC, e);
            }
            
            PC lUpdatedPC = pcBean.findById(pPC.getId());
            
            
            // Update the PC with the values not managed by the modification
            // process (not displayed in HMI and available only in database)
            updatePCHiddenAttributes(lUpdatedPC, pDom);
            
            /*
             * Update the PC with the values
             */
            
            lUpdatedPC.setAutoDataValidityDate(lDate);
            
            try {
                // Update the PC in database with the modifications not managed
                // by the modification process
                pcBean.merge(lUpdatedPC, new ArrayList<Action>());
            }
            catch (Exception e) {
                
                logMsg("Attributes accessible only from database have not been updated because of a database error.",
                        true);
                // Try merging at least the data validity date
                try {
                    // retrieve the PC from database (since it may have been
                    // modified and not well merged just before)
                    lUpdatedPC = pcBean.findById(lUpdatedPC.getId());
                    // update it with sure data (no error should occur when
                    // merging)
                    lUpdatedPC.setAutoDataValidityDate(lDate);
                    // merge it in database
                    pcBean.merge(lUpdatedPC, new ArrayList<Action>());
                    
                }
                catch (Exception ei) {
                    throw new AutoUpdateException(ERROR_SAVING_PC, ei);
                }
            }
        }
        finally {
            
            if (lSysmonVirtualUserToLogOut) {
                // If SYSMON virtual user has been logged in,
                // restore the previous user logged
                logInBackAsPreviousLoggedUser();
            }
        }
        
    }
    
    /**
     * Treat the file containing the SYSMON data for the provided PC and update
     * the PC (through the PCController) with the retrieved information
     * 
     * @param pPC
     *            the PC to update
     * @param InputStream
     *            the input stream containing the SYSMON data
     * @throws AutoUpdateException
     *             when an error occurs while processing commands
     */
    public void doUpdatePCFromFile(PC pPC, InputStream pInputStream)
            throws AutoUpdateException {
        
        byte[] lByteArray;
        InputStream lInputStreamForValidation = null;
        InputStream lInputStreamForParsing = null;
        
        try {
            lByteArray = IOUtils.toByteArray(pInputStream);
        } catch (IOException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
        
        lInputStreamForValidation = new ByteArrayInputStream(lByteArray);
        lInputStreamForParsing = new ByteArrayInputStream(lByteArray);
        
        // Update the PC automatic update information
        PC lPC = pPC;
        
        lPC.setLastAutoUpdate(new Date());
        lPC.setLastAutoUpdateType(AutoUpdateType.File);
        
        try {
            lPC = pcBean.merge(lPC, new ArrayList<Action>());
        }
        catch (Exception e) {
            throw new AutoUpdateException(ERROR_SAVING_PC, e);
        }
        
        // Validate the file format
        
        ClassLoader lClassLoader = getClass().getClassLoader();
        InputStream lXSDInputStream =
                lClassLoader.getResourceAsStream(SYSMON_XSD_FILE_PATH);
        
        SchemaFactory lSchemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        InputSource lXSDInputSource = new InputSource(lXSDInputStream);
        SAXSource lXSDSource = new SAXSource(lXSDInputSource);
        
        try {
            Schema lSchema = lSchemaFactory.newSchema(lXSDSource);
            
            Validator lValidator = lSchema.newValidator();
            
            lValidator.validate(new StreamSource(lInputStreamForValidation));
            
        }
        catch (SAXException e) {
            throw new AutoUpdateException(ERROR_SYSMON_FORMAT_VALIDATION, e);
        }
        catch (IOException e) {
            throw new AutoUpdateException(ERROR_SYSMON_FORMAT_VALIDATION, e);
        }
        
        // Parse the file
        
        DocumentBuilderFactory lDBF = DocumentBuilderFactory.newInstance();
        
        try {
            // Using factory get an instance of document builder
            DocumentBuilder lDB = lDBF.newDocumentBuilder();
            // parse using builder to get DOM representation of the XML file
            Document lDom = lDB.parse(lInputStreamForParsing);
            
            doUpdatePC(pPC, lDom, false, null);
            
        }
        catch (ParserConfigurationException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
        catch (SAXException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
        catch (IOException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
    }
    
    /**
     * Execute the SYSMON command for the provided PC and update the PC
     * (through the PCController) with the retrieved information
     * 
     * @param pPC
     *            the PC to update
     * @param pLoggedUser
     *            the logged user
     * @throws AutoUpdateException
     *             when an error occurs while processing commands or parsing
     */
    public void doUpdatePCWithSysmon(PC pPC, User pLoggedUser)
            throws AutoUpdateException {
        
        // Update the PC automatic update information
        PC lPC = pPC;
        
        lPC.setLastAutoUpdate(new Date());
        lPC.setLastAutoUpdateType(AutoUpdateType.Sysmon);
        
        try {
            lPC = pcBean.merge(lPC, new ArrayList<Action>());
        }
        catch (Exception e) {
            throw new AutoUpdateException(ERROR_SAVING_PC, e);
        }
        
        // check if PC is accessible on network
        
        try {
            Process lCheckConnection =
                    Runtime.getRuntime().exec("ping -c 1 " + pPC.getName());
            
            // Handle the ouput and error streams of the process
            StreamGobbler lErrorGobbler =
                    new StreamGobbler(lCheckConnection.getErrorStream());
            StreamGobbler lOutputGobbler =
                    new StreamGobbler(lCheckConnection.getInputStream());
            lErrorGobbler.start();
            lOutputGobbler.start();
            
            // wait for the end of the process
            lErrorGobbler.join();
            lOutputGobbler.join();
            lCheckConnection.waitFor();
            
            if (lCheckConnection.exitValue() != 0) {
                throw new AutoUpdateException(MessageBundle.getMessageResource(
                        Constants.PC_CONNECTION_FAILURE,
                        new Object[] { pPC.getName() }), null);
            }
        } catch (IOException e) {
            throw new AutoUpdateException(ERROR_CONNECTION_PC, e);
        } catch (InterruptedException e) {
            throw new AutoUpdateException(ERROR_CONNECTION_PC, e);
        }
        
        // launch Sysmon command
        
        String lSysmonTimeoutStr =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getInitParameter("SysmonTimeout");
        int lSysmonTimeout;
        try {
            lSysmonTimeout = Integer.parseInt(lSysmonTimeoutStr);
        }
        catch (NumberFormatException e) {
            lSysmonTimeout = 60;
        }
        
        SysmonExecution lSysmonExecution = new SysmonExecution(pPC.getName());
        try {
            lSysmonExecution.start();
            
            // wait for the process to stop
            lSysmonExecution.join(lSysmonTimeout * 1000);
            
            if (lSysmonExecution.getExitValue() == null) {
                // process has been interrupted by a timeout
                throw new TimeoutException(ERROR_SYSMON_TIMEOUT);
            }
            else if (lSysmonExecution.getExitValue() != 0) {
                // process has terminated but exit value is not successful
                throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, null);
            }
        }
        catch (IOException e) {
            lSysmonExecution.interrupt();
            Thread.currentThread().interrupt();
            throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, e);
        }
        catch (InterruptedException e) {
            lSysmonExecution.interrupt();
            Thread.currentThread().interrupt();
            throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, e);
        }
        catch (TimeoutException e) {
            lSysmonExecution.interrupt();
            Thread.currentThread().interrupt();
            throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, e);
        }
        // at this point, the SYSMON process has been executed with success
        
        InputStream lInputStreamForValidation = null;
        InputStream lInputStreamForParsing = null;
        
        try {
            InputStream lSysmonInputStream = lSysmonExecution.getInputStream();
            
            if (lSysmonInputStream == null) {
                throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, null);
            }
            
            // copy input stream
            
            byte[] lByteArray = IOUtils.toByteArray(lSysmonInputStream);
            
            lInputStreamForValidation = new ByteArrayInputStream(lByteArray);
            lInputStreamForParsing = new ByteArrayInputStream(lByteArray);
            
        }
        catch (IOException e) {
            throw new AutoUpdateException(ERROR_EXECUTING_SYSMON, e);
        }
        
        // Validate the input stream format
        
        ClassLoader lClassLoader = getClass().getClassLoader();
        InputStream lXSDInputStream =
                lClassLoader.getResourceAsStream(SYSMON_XSD_FILE_PATH);
        
        SchemaFactory lSchemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        InputSource lXSDInputSource = new InputSource(lXSDInputStream);
        SAXSource lXSDSource = new SAXSource(lXSDInputSource);
        
        try {
            Schema lSchema = lSchemaFactory.newSchema(lXSDSource);
            
            Validator lValidator = lSchema.newValidator();
            
            lValidator.validate(new StreamSource(lInputStreamForValidation));
            
        }
        catch (SAXException e) {
            throw new AutoUpdateException(ERROR_SYSMON_FORMAT_VALIDATION, e);
        }
        catch (IOException e) {
            throw new AutoUpdateException(ERROR_SYSMON_FORMAT_VALIDATION, e);
        }
        
        // Parse the input stream
        
        DocumentBuilderFactory lDBF = DocumentBuilderFactory.newInstance();
        
        try {
            // Using factory get an instance of document builder
            DocumentBuilder lDB = lDBF.newDocumentBuilder();
            
            // parse using builder to get DOM representation of the XML file
            Document lDom = lDB.parse(lInputStreamForParsing);
            
            doUpdatePC(lPC, lDom, true, pLoggedUser);
            
        }
        catch (ParserConfigurationException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
        catch (SAXException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
        catch (IOException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
    }
    
    /**
     * Update the PCController for updating the provided PC according to
     * information contained in the DOM.<br/>
     * Updated values are the communication ports of the PC.
     * 
     * @param pDom
     *            the DOM containing information
     */
    private void prepareUpdateComPorts(Document pDom) {
        
        /*
         * Retrieve the PC communication ports list
         */
        
        List<CommunicationPort> lPCPorts =
                new ArrayList<CommunicationPort>(comPortController.getPorts());
        
        //New Ports List
        //Adding only Comm port found in SysMon
        List<CommunicationPort> lNewPortList =
                new ArrayList<CommunicationPort>();
                
                
        /*
         * Retrieve data from document
         */
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lNetworksElts = lDocElt.getElementsByTagName(ELT_NETWORKS);
        
        Element lNetworksElt = (Element) lNetworksElts.item(0);
        
        NodeList lInterfacesElts =
                lNetworksElt.getElementsByTagName(ELT_NETWORKS_INTERFACE);
        
        // browse retrieved interfaces
        for (int i = 0; i < lInterfacesElts.getLength(); i++) {
            
            Element lInterfaceElt = (Element) lInterfacesElts.item(i);
            
            String lName =
                    lInterfaceElt.getAttribute(ATT_NETWORKS_INTERFACE_NAME);
            String lIPAddress =
                    lInterfaceElt
                            .getAttribute(ATT_NETWORKS_INTERFACE_IPV4ADDRESS);
            String lDHCP =
                    lInterfaceElt.getAttribute(ATT_NETWORKS_INTERFACE_DHCP);
            String lMACAddress =
                    lInterfaceElt
                            .getAttribute(ATT_NETWORKS_INTERFACE_MACADDRESS);
            String lNetworkString =
                    lInterfaceElt.getAttribute(ATT_NETWORKS_INTERFACE_NETWORK);
            String lMask = lInterfaceElt.getAttribute(ATT_NETWORKS_INTERFACE_MASK);
            
            
            // check that the detected communication port is valid
            if (StringUtil.isEmptyOrNull(lName)
                    && StringUtil.isEmptyOrNull(lMACAddress)
                    && StringUtil.isEmptyOrNull(lIPAddress)) {
                // ignore the communication port
                continue;
            }
            
            /*
             * Treat and convert retrieved values
             */
            Network lNetwork = null;
            if (!StringUtil.isEmptyOrNull(lNetworkString)) {
                lNetwork =
                        valueListBean.findAttributeValueListByName(
                                Network.class, lNetworkString);
            }
            /*
             * Sysmon retrieved port creation.
             */
            CommunicationPort lNewCommunicationPort =
                    new CommunicationPort(lName, lIPAddress, lMACAddress);
            if (lDHCP.equals("no")) {
                lNewCommunicationPort.setFixedIP(true);
            }
            else {
                lNewCommunicationPort.setFixedIP(false);
            }
            if (lNetwork != null) {
                lNewCommunicationPort.setNetwork(lNetwork);
            }
            if (lMask != null) {
                lNewCommunicationPort.setMask(lMask);
            }
                        
            /*
             * Search if the MAC address exists in the current port list
             */
            boolean lFound = false;
            for(CommunicationPort lPort : lPCPorts){
            	if (!StringUtil.isEmptyOrNull(lPort.getMacAddress()) &&
            			lPort.getMacAddress().equals(lMACAddress)) {
                        // Mac addresses are the same
            			//Need to merge the port
                        mergeCommunicationPorts(lPort, lNewCommunicationPort);
                        lNewPortList.add(lPort);
                        lFound = true;
                        break;
                }
            }
            //if not found in current list, search if a port without MAC address match
            if(!lFound){
                for(CommunicationPort lPort : lPCPorts){
                    if (StringUtil.isEmptyOrNull(lPort.getMacAddress())
                            && (lPort.getIpAddress().equals(lIPAddress) || StringUtil
                                    .isEmptyOrNull(lPort.getIpAddress()))
                            && (lPort.getName().equals(lName) || StringUtil
                                    .isEmptyOrNull(lPort.getName()))) {
                        // IP address and name are empty are identical
                        // Need to merge the port
                        mergeCommunicationPorts(lPort, lNewCommunicationPort);
                        lNewPortList.add(lPort);
                        lFound = true;
                        break;
                    }
                }
            }
            //if not found in current list, add it
            if(!lFound){
            	lNewPortList.add(lNewCommunicationPort);
            }
        }
                
        
        /*
         * Update the communication ports list on PC
         */
        comPortController.setPorts(lNewPortList);
    }
    
    /**
     * Update the PCController for updating the provided PC according to
     * information contained in the DOM.<br/>
     * Updated values are the PC attributes.
     * 
     * @param pDom
     *            the DOM containing information
     * @param pCheckPCName
     *            true if the PC name must be checked, false if no check is
     *            needed
     * @throws AutoUpdateException
     *             when a format error occurs
     */
    private void prepareUpdatePCAttributes(Document pDom, boolean pCheckPCName)
            throws AutoUpdateException {
        
        /*
         * Retrieve data from document
         */
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lHostElts = lDocElt.getElementsByTagName(ELT_HOST);
        
        Element lHostElt = (Element) lHostElts.item(0);
        
        String lHostname = lHostElt.getAttribute(ATT_HOST_HOSTNAME);
        
        // Check that current parsed document concerns the provided PC
        if (pCheckPCName) {
            String lCurrentPCName = pcController.getPc().getName();
            if (!lCurrentPCName.equalsIgnoreCase(lHostname)) {
                throw new AutoUpdateException(MessageBundle.getMessageResource(
                        Constants.PARSED_FILE_CONCERNING_ANOTHER_PC,
                        new Object[] { lHostname, lCurrentPCName }), null);
            }
        }
        
        String lDomainStr = lHostElt.getAttribute(ATT_HOST_DOMAIN);
        
        NodeList lHardwareElts = lDocElt.getElementsByTagName(ELT_HARDWARE);
        
        Element lHardwareElt = (Element) lHardwareElts.item(0);
        
        String lManufacturerSN =
                lHardwareElt.getAttribute(ATT_HARDWARE_MANUFACTURERSN);
        String lProductTypePCStr =
                lHardwareElt.getAttribute(ATT_HARDWARE_PRODUCTTYPE);
        String lProductNameStr =
                lHardwareElt.getAttribute(ATT_HARDWARE_PRODUCTNAME);
        String lManufacturer =
                lHardwareElt.getAttribute(ATT_HARDWARE_MANUFACTURER);
        String lPlatform = lHardwareElt.getAttribute(ATT_HARDWARE_PLATFORM);
        
        // Check that the detected type is correct (might no be the case when a
        // PC is being replaced)
        String lCurrentProductNameStr =
                pcController.getPc().getTypeArticle().getLabel();
        if (!lCurrentProductNameStr.equalsIgnoreCase(lProductNameStr)) {
            throw new AutoUpdateException(MessageBundle.getMessageResource(
                    Constants.PARSED_TYPE_MISMATCH, new Object[] {
                            lProductNameStr, lCurrentProductNameStr }),
                    null);
        }
        
        NodeList lGraphicsElts =
                lHardwareElt.getElementsByTagName(ELT_HARDWARE_GRAPHICS);
        
        Element lGraphicsElt = (Element) lGraphicsElts.item(0);
        
        NodeList lMonitorElts =
                lGraphicsElt
                        .getElementsByTagName(ELT_HARDWARE_GRAPHICS_MONITOR);
        
        Element lMonitorElt = (Element) lMonitorElts.item(0);
        
        String lNumberOfScreensStr =
                lMonitorElt.getAttribute(ATT_HARDWARE_GRAPHICS_MONITOR_NUMBER);
        
        /*
         * Treat and convert retrieved values
         */
        
        ProductTypePC lProductTypePC =
                valueListBean.findAttributeValueListByName(ProductTypePC.class,
                        lProductTypePCStr);
        
        TypePC lTypePC = articleBean.findTypePCByName(lProductNameStr);
        
        Domain lDomain = valueListBean
                .findAttributeValueListByName(Domain.class, lDomainStr);
        
        /*
         * Update the PC with the values
         */
        
        // the PC product type is not updated if the retrieved product type does
        // not exist in database
        if (lProductTypePC != null) {
            pcController.setProductTypeId(lProductTypePC.getId());
        }
        else if (!StringUtil.isEmptyOrNull(lProductTypePCStr)) {
            logMsg("Product type was not found: " + lProductTypePCStr, false);
        }
        
        LogInController lLogInController = findBean(LogInController.class);
        
        String lTypeMsgDescription =
                "Label = " + lProductNameStr + ", Manufacturer = "
                        + lManufacturer;
        
        DBConstants lDBConstants = findBean(DBConstants.class);
        
        final int MANUFACTURER_LENGTH =
                lDBConstants.getTypearticleManufacturerLength();
        
        // if the PC type manufacturer is different from the retrieved
        // manufacturer and user is authorized to update the type manufacturer,
        // update the PC type manufacturer value
        if (lTypePC != null && !StringUtil.isEmptyOrNull(lManufacturer)
                && !lManufacturer.equals(lTypePC.getManufacturer())) {
            if (lLogInController.isAuthorized(RightCategoryCRUD.TypeCRUD,
                    RightMaskCRUD.CRUD_Update)) {
                if (lManufacturer.length() <= MANUFACTURER_LENGTH) {
                    lTypePC.setManufacturer(lManufacturer);
                    try {
                        lTypePC = articleBean.mergeTypeArticle(lTypePC);
                    }
                    catch (Exception e) {
                        logMsg("Type was not updated because of a database error: "
                                + lTypeMsgDescription, true);
                        // the type is not updated, retrieve it again from
                        // database
                        lTypePC = articleBean.findTypePCByName(lProductNameStr);
                    }
                }
                else {
                    logMsg("Type was not updated because manufacturer is too long: "
                            + lTypeMsgDescription, false);
                }
            }
            else {
                logMsg("Type was not updated because logged user has not enought rights.",
                        false);
            }
        }
        // the PC type is not updated if the retrieved type does not exist in
        // database
        if (lTypePC != null) {
            pcController.setTypePCId(lTypePC.getId());
        }
        else if (!StringUtil.isEmptyOrNull(lProductNameStr)) {
            logMsg("Type was not found: " + lProductNameStr, false);
        }
        
        // the manufacturer SN is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lManufacturerSN)) {
            pcController.setManufacturerSN(lManufacturerSN);
        }
        
        // the number of screens is not updated if the retrieved value is not an
        // integer
        try {
            if (!StringUtil.isEmptyOrNull(lNumberOfScreensStr)) {
                Integer lNbScreens = Integer.parseInt(lNumberOfScreensStr);
                pcController.setNbScreens(lNbScreens);
            }
        }
        catch (NumberFormatException nfe) {
            // nothing to do
        }
        
        // the domain is not updated if the retrieved value is empty
        if (lDomain != null) {
            pcController.setDomainId(lDomain.getId());
        }
        else if (!StringUtil.isEmptyOrNull(lDomainStr)) {
            logMsg("Domain was not found: " + lDomainStr, false);
        }
        
        // the platform is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lPlatform)) {
            pcController.setPlatform(lPlatform);
        }
    }
    
    /**
     * Update the PCController for updating the provided PC according to
     * information contained in the DOM.<br/>
     * Updated values are the installed software (OS) of the PC.
     * 
     * @param pDom
     *            the DOM containing information
     */
    private void prepareUpdateSoftwareList(Document pDom) {
        
        /*
         * Retrieve the operating systems list of PC
         */
        
        List<Software> lPCSoftwareList = pcController.getSoftwares();
        
        /*
         * Retrieve data from document
         */
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lOperatingSystemElts = lDocElt.getElementsByTagName(ELT_OS);
        
        Element lOperatingSystemElt = (Element) lOperatingSystemElts.item(0);
        
        String lType = lOperatingSystemElt.getAttribute(ATT_OS_TYPE);
        String lVersion = lOperatingSystemElt.getAttribute(ATT_OS_VERSION);
        
        NodeList lInstalledKernelsElts =
                lOperatingSystemElt
                        .getElementsByTagName(ELT_OS_INSTALLEDKERNELS);
        
        Element lInstalledKernelsElt = (Element) lInstalledKernelsElts.item(0);
        
        String lDefaultKernel =
                lInstalledKernelsElt
                        .getAttribute(ATT_OS_INSTALLEDKERNELS_DEFAULT);
        
        NodeList lInstalledKernelsKernelElts =
                lInstalledKernelsElt
                        .getElementsByTagName(ELT_OS_INSTALLEDKERNELS_KERNEL);
        
        DBConstants lDBConstants = findBean(DBConstants.class);
        
        final int NAME_LENGTH = lDBConstants.getSoftwareNameLength();
        final int DISTRIBUTION_LENGTH =
                lDBConstants.getSoftwareDistributionLength();
        final int KERNEL_LENGTH = lDBConstants.getSoftwareKernelLength();
        
        // browse retrieved kernels
        for (int i = 0; i < lInstalledKernelsKernelElts.getLength(); i++) {
            
            Element lKernelElt = (Element) lInstalledKernelsKernelElts.item(i);
            
            String lKernelVersion =
                    lKernelElt
                            .getAttribute(ATT_OS_INSTALLEDKERNELS_KERNEL_VERSION);
            
            /*
             * Add software to PC if necessary
             */
            
            String lSoftwareMsgDescription =
                    "Name = " + lType + ", Distribution = " + lVersion
                            + ", Kernel = " + lKernelVersion;
            
            Software lSoftware =
                    softwareBean.findByCompleteName(Software.getCompleteName(
                            lType, lVersion, lKernelVersion));
            
            // if software does not exist, create it
            if (lSoftware == null) {
                
                // check if software is valid
                if (!StringUtil.isEmptyOrNull(lType)
                        && lType.length() <= NAME_LENGTH
                        && !StringUtil.isEmptyOrNull(lVersion)
                        && lVersion.length() <= DISTRIBUTION_LENGTH
                        && !StringUtil.isEmptyOrNull(lKernelVersion)
                        && lKernelVersion.length() <= KERNEL_LENGTH) {
                    
                    try {
                        SoftwareController.validateName(lType, true,
                                softwareBean);
                        SoftwareController.validateDistribution(lVersion,
                                lType, true, softwareBean);
                        
                        lSoftware =
                                new Software(lType, lVersion, lKernelVersion);
                        lSoftware.setOperatingSystem(true);
                        softwareBean.create(lSoftware,
                                new ArrayList<Article>(), null);
                    }
                    catch (ValidatorException ve) {
                        logMsg("Software was not created because logged user has not enough rights: "
                                + lSoftwareMsgDescription, true);
                    }
                    catch (Exception e) {
                        logMsg("Software was not created because of a database error: "
                                + lSoftwareMsgDescription, true);
                        // the software is not created
                        lSoftware = null;
                    }
                }
                else {
                    logMsg("Software name, distribution and/or kernel is empty or too long: "
                            + lSoftwareMsgDescription, false);
                }
            }
            // if software is not an operating system, set it
            else if (!lSoftware.getOperatingSystem()) {
                
                lSoftware.setOperatingSystem(true);
                softwareBean.merge(lSoftware);
            }
            
            // if software is not installed on PC, add it
            if (lSoftware != null && !lPCSoftwareList.contains(lSoftware)) {
                
                pcController.getSoftwares().add(lSoftware);
            }
            else if (lSoftware == null) {
                logMsg("Software was not found: " + lSoftwareMsgDescription,
                        false);
            }
        }
        
        // set the default operating system
        Software lDefaultOS =
                softwareBean.findByCompleteName(Software.getCompleteName(lType,
                        lVersion, lDefaultKernel));
        if (lDefaultOS != null) {
            pcController.setDefaultOSId(lDefaultOS.getId());
        }
    }
    
    /**
     * Update the provided PC according to information contained in the DOM.<br/>
     * Updated values are the PC attributes not managed via the modification
     * process.
     * 
     * @param pPC
     *            the PC to update
     * @param pDom
     *            the DOM containing information
     */
    private void updatePCHiddenAttributes(PC pPC, Document pDom) {
        
        /*
         * Retrieve data from document
         */
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lHardwareElts = lDocElt.getElementsByTagName(ELT_HARDWARE);
        
        Element lHardwareElt = (Element) lHardwareElts.item(0);
        
        NodeList lCpuElts = lHardwareElt.getElementsByTagName(ELT_HARDWARE_CPU);
        
        Element lCpuElt = (Element) lCpuElts.item(0);
        
        String lCpuType = lCpuElt.getAttribute(ATT_HARDWARE_CPU_TYPE);
        String lCpuMaxSpeed = lCpuElt.getAttribute(ATT_HARDWARE_CPU_MAXSPEED);
        String lCpuSocketsStr = lCpuElt.getAttribute(ATT_HARDWARE_CPU_SOCKETS);
        String lCpuCorePerSocketStr =
                lCpuElt.getAttribute(ATT_HARDWARE_CPU_COREPERSOCKET);
        String lCpuLogicalCpuPerSocketStr =
                lCpuElt.getAttribute(ATT_HARDWARE_CPU_LOGICALCPUPERSOCKET);
        String lCpuTotalLogicalCpusStr =
                lCpuElt.getAttribute(ATT_HARDWARE_CPU_TOTALLOGICALCPUS);
        String lCpuHyperthreading =
                lCpuElt.getAttribute(ATT_HARDWARE_CPU_HYPERTHREADING);
        
        NodeList lMemoryElts =
                lDocElt.getElementsByTagName(ELT_HARDWARE_MEMORY);
        
        Element lMemoryElt = (Element) lMemoryElts.item(0);
        
        String lTotalMemorySize =
                lMemoryElt.getAttribute(ATT_HARDWARE_MEMORY_TOTALMEMORYSIZE);
        
        NodeList lMemorySlotElts =
                lMemoryElt.getElementsByTagName(ELT_HARDWARE_MEMORY_SLOT);
        
        List<MemorySlot> lNewMemorySlots = new ArrayList<MemorySlot>();
        
        for (int i = 0; i < lMemorySlotElts.getLength(); i++) {
            
            Element lMemorySlotElt = (Element) lMemorySlotElts.item(i);
            
            String lName =
                    lMemorySlotElt.getAttribute(ATT_HARDWARE_MEMORY_SLOT_NAME);
            String lUsedStr =
                    lMemorySlotElt.getAttribute(ATT_HARDWARE_MEMORY_SLOT_USED);
            String lSize =
                    lMemorySlotElt
                            .getAttribute(ATT_HARDWARE_MEMORY_SLOT_MEMORYSIZE);
            String lType =
                    lMemorySlotElt
                            .getAttribute(ATT_HARDWARE_MEMORY_SLOT_MEMORYTYPE);
            
            Boolean lUsed = null;
            
            if ("true".equals(lUsedStr)) {
                lUsed = true;
            }
            else if ("false".equals(lUsedStr)) {
                lUsed = false;
            }
            
            // the memory slot is not retrieved if the name is empty or the used
            // is not valid
            if (lUsed != null && !StringUtil.isEmptyOrNull(lName)) {
                MemorySlot lMemorySlot =
                        new MemorySlot(pPC, lName, lUsed, lSize, lType);
                
                lNewMemorySlots.add(lMemorySlot);
            }
        }
        
        /*
         * Update the PC with the values
         */
        
        // the CPU type is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lCpuType)) {
            pPC.setCpuType(lCpuType);
        }
        
        // the CPU max speed is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lCpuMaxSpeed)) {
            pPC.setCpuMaxSpeed(lCpuMaxSpeed);
        }
        
        // the CPU sockets is not updated if the retrieved value is not an
        // integer
        try {
            if (!StringUtil.isEmptyOrNull(lCpuSocketsStr)) {
                Integer lCpuSockets = Integer.parseInt(lCpuSocketsStr);
                pPC.setCpuSockets(lCpuSockets);
            }
        }
        catch (NumberFormatException nfe) {
            // nothing to do
        }
        
        // the CPU core per socket is not updated if the retrieved value is not
        // an integer
        try {
            if (!StringUtil.isEmptyOrNull(lCpuCorePerSocketStr)) {
                Integer lCpuCorePerSocket =
                        Integer.parseInt(lCpuCorePerSocketStr);
                pPC.setCpuCorePerSocket(lCpuCorePerSocket);
            }
        }
        catch (NumberFormatException nfe) {
            // nothing to do
        }
        
        // the CPU logical CPU per socket is not updated if the retrieved value
        // is not an integer
        try {
            if (!StringUtil.isEmptyOrNull(lCpuLogicalCpuPerSocketStr)) {
                Integer lCpuLogicalCpuPerSocket =
                        Integer.parseInt(lCpuLogicalCpuPerSocketStr);
                pPC.setCpuLogicalCpuPerSocket(lCpuLogicalCpuPerSocket);
            }
        }
        catch (NumberFormatException nfe) {
            // nothing to do
        }
        
        // the CPU total logical CPUs is not updated if the retrieved value is
        // not an integer
        try {
            if (!StringUtil.isEmptyOrNull(lCpuTotalLogicalCpusStr)) {
                Integer lCpuTotalLogicalCpu =
                        Integer.parseInt(lCpuTotalLogicalCpusStr);
                pPC.setCpuTotalLogicalCpus(lCpuTotalLogicalCpu);
            }
        }
        catch (NumberFormatException nfe) {
            // nothing to do
        }
        
        // the CPU hyperthreading is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lCpuHyperthreading)) {
            pPC.setCpuHyperthreading(lCpuHyperthreading);
        }
        
        // the total memory size is not updated if the retrieved value is empty
        if (!StringUtil.isEmptyOrNull(lTotalMemorySize)) {
            pPC.setTotalMemorySize(lTotalMemorySize);
        }
        
        // Update the list of memory slots
        
        List<MemorySlot> lPCMemorySlots =
                new ArrayList<MemorySlot>(pPC.getMemorySlots());
        
        for (MemorySlot lPCMemorySlot : lPCMemorySlots) {
            MemorySlot lNewMemorySlot = null;
            for (MemorySlot lSlot : lNewMemorySlots) {
                if (lSlot.getName().equals(lPCMemorySlot.getName())) {
                    lNewMemorySlot = lSlot;
                    break;
                }
            }
            if (lNewMemorySlot == null) {
                // remove the no more found memory slot
                pPC.getMemorySlots().remove(lPCMemorySlot);
            }
            else {
                // update the memory slot
                lPCMemorySlot.setUsed(lNewMemorySlot.getUsed());
                lPCMemorySlot.setMemorySize(lNewMemorySlot.getMemorySize());
                lPCMemorySlot.setMemoryType(lNewMemorySlot.getMemoryType());
            }
        }
        
        for (MemorySlot lNewMemorySlot : lNewMemorySlots) {
            MemorySlot lPCMemorySlot = null;
            for (MemorySlot lSlot : pPC.getMemorySlots()) {
                if (lSlot.getName().equals(lNewMemorySlot.getName())) {
                    lPCMemorySlot = lSlot;
                    break;
                }
            }
            if (lPCMemorySlot == null) {
                // add the new memory slot
                pPC.getMemorySlots().add(lNewMemorySlot);
            }
        }
    }
    
    /**
     * Retrieve the validity date contained in the DOM.
     * 
     * @param pDom
     *            the DOM containing information
     * @return the retrieved date
     * @throws AutoUpdateException
     *             when a format error occurs
     */
    private Date retrievePCValidityDate(Document pDom)
            throws AutoUpdateException {
        
        /*
         * Retrieve data from document
         */
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lInfosElts = lDocElt.getElementsByTagName(ELT_INFOS);
        
        Element lInfosElt = (Element) lInfosElts.item(0);
        
        String lDateStr = lInfosElt.getAttribute(ATT_INFOS_DATE);
        String lTimeStr = lInfosElt.getAttribute(ATT_INFOS_TIME);
        
        /*
         * Treat and convert retrieved values
         */
        
        String lDateTimeStr = lDateStr + " " + lTimeStr;
        
        SimpleDateFormat lSimpleDateFormat =
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        try {
            return lSimpleDateFormat.parse(lDateTimeStr);
        }
        catch (ParseException e) {
            throw new AutoUpdateException(ERROR_PARSING_SYSMON_OUTPUT, e);
        }
    }
    
    /**
     * Compute the history comment according to data contained in the DOM.
     * 
     * @param pDom
     *            the DOM containing information
     * @return the retrieved date
     */
    private String retrieveHistoryComment(Document pDom) {
        
        Element lDocElt = pDom.getDocumentElement();
        
        NodeList lInfosElts = lDocElt.getElementsByTagName(ELT_INFOS);
        
        Element lInfosElt = (Element) lInfosElts.item(0);
        
        String lSysmonVersion = lInfosElt.getAttribute(ATT_INFOS_VERSION);
        
        String lHistoryComment = SYSMON_TOOL_NAME;
        
        if (!StringUtil.isEmptyOrNull(lSysmonVersion)) {
            lHistoryComment = lHistoryComment + " " + lSysmonVersion;
        }
        
        return lHistoryComment + " " + SYSMON_HISTORY_COMMENT;
    }
    
    /**
     * @return the logs
     */
    public List<StringBuffer> getLogs() {
        return logs;
    }
    
    /**
     * @return the SYSMON virtual user
     */
    public User getSysmonVirtualUser() {
        return sysmonVirtualUser;
    }
    
}
