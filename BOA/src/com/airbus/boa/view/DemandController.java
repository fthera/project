/*
 * ------------------------------------------------------------------------
 * Class : DemandController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.mail.MessagingException;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.SoftwareBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PC.AvailabilityStatus;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypePC;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.demand.DemandStatus;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.user.RightCategoryCRUD;
import com.airbus.boa.entity.user.RightMaskAction;
import com.airbus.boa.entity.user.RightMaskCRUD;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.Container;
import com.airbus.boa.localization.Location;
import com.airbus.boa.localization.LocationManager;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.MessageConstants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MailUtil;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.util.NavigationUtil;
import com.airbus.boa.util.StringUtil;
import com.airbus.boa.view.application.ApplicationController;
import com.airbus.boa.view.application.DBConstants;
import com.airbus.boa.view.item.AbstractItemController;
import com.airbus.boa.view.item.SoftwareController;

/**
 * Controller managing the demand creation, consultation and modification
 */
@ManagedBean(name = DemandController.BEAN_NAME)
@ViewScoped
public class DemandController extends AbstractItemController implements
        Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "demandController";
    
    private final int COMMENT_LENGTH;
    
    private Long demandId;
    
    private Long pcToReplaceId;
    
    private Date replacedPCAvailabilityDate = new Date();
    
    private Demand demand = null;
    
    private Long selectedPCId = null;
    private PC selectedPC = null;

    @EJB
    private DemandBean demandBean;
    
    @EJB
    private PCBean pcBean;
    
    @EJB
    private ArticleBean articleBean;
    
    @EJB
    private SoftwareBean softwareBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    private Software software;
    
    /**
     * Default constructor
     */
    public DemandController() {
        
        DBConstants lDBConstants = findBean(DBConstants.class);
        
        COMMENT_LENGTH = lDBConstants.getDemandCommentsLength();
        
        itemPage = NavigationConstants.DEMAND_MANAGEMENT_PAGE;
        listPage = NavigationConstants.DEMAND_MANAGEMENT_PAGE;
        errorId = NavigationConstants.DEMAND_MGMT_ERROR_ID;
    }
    
    @Override
    public void init() {
        Map<String, String> parameterMap =
                (Map<String, String>) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequestParameterMap();
        if (parameterMap.containsKey("pcToReplaceId")) {
            setPcToReplaceId(new Long(parameterMap.get("pcToReplaceId")));
        }
        super.init();
    }
    
    /**
     * Update the selected PC
     */
    public void doSelectPC() {
        selectedPC = pcBean.findById(selectedPCId);
    }

    /**
     * Set the demand status to Cancelled, set the closure date and remove the
     * link to the allocated PC if any. The demand is updated in database.
     */
    public void doCancelDemand() {
        
        PC lAllocatedPC = demand.getAllocatedPC();
        
        demand.setStatus(DemandStatus.Cancelled);
        demand.setClosureDate(new Date());
        demand.setAllocatedPC(null);
        
        demand = demandBean.merge(demand);
        
        // Update the allocated to demand attribute of the PC
        if (lAllocatedPC != null) {
            PC lAllocatedPCInCache = pcBean.findById(lAllocatedPC.getId());
            lAllocatedPCInCache.setAllocatedToDemand(null);
        }
    }
    
    /**
     * Cancel the allocation and return to the consultation page
     */
    public void doCancelAllocation() {
        goToReadPage(demandId);
    }
    
    /**
     * Set the demand status to Closed and set the closure date of the demand in
     * database. <br>
     * Update the PC to replace, if any, with the Replaced status and the filled
     * availability date in database.
     */
    public void doCloseDemand() {
        
        if (demand.getPCToReplace() != null
                && replacedPCAvailabilityDate == null) {
            
            Utils.addFacesMessage(NavigationConstants.DEMAND_MANAGEMENT_PAGE,
                    MessageBundle.getMessage(Constants.DATE_MANDATORY));
        }
        else {
            demand.setStatus(DemandStatus.Closed);
            demand.setClosureDate(new Date());
            
            demand = demandBean.merge(demand);
            
            PC lPCToReplace = demand.getPCToReplace();
            
            if (lPCToReplace != null) {
                
                lPCToReplace.setAvailabilityStatus(AvailabilityStatus.Replaced);
                lPCToReplace.setAvailabilityDate(replacedPCAvailabilityDate);
                pcBean.merge(lPCToReplace, new ArrayList<Action>());
                
                replacedPCAvailabilityDate = new Date();
            }
        }
    }
    
    /**
     * Set the demand status to Confirm in database.
     */
    public void doConfirmDemand() {
        demand.setStatus(DemandStatus.Confirmed);
        demand = demandBean.merge(demand);
    }
    
    /**
     * Set the demand status to Available in database.<br>
     * Set the demand allocated PC status to InUse in database.
     */
    public void doMakeAvailableDemand() {
        
        demand.setStatus(DemandStatus.Available);
        demand = demandBean.merge(demand);
        
        PC lAllocatedPC = demand.getAllocatedPC();
        
        if (lAllocatedPC != null) {
            
            lAllocatedPC.setAvailabilityStatus(AvailabilityStatus.InUse);
            lAllocatedPC.setAvailabilityDate(null);
            pcBean.merge(lAllocatedPC, new ArrayList<Action>());
        }
    }
    
    public void goToAllocationPage() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", demandId.toString());
        NavigationUtil.goTo(NavigationConstants.DEMAND_ALLOCATION_PAGE, params);
    }
    
    /**
     * Allocate the selected PC to the demand.
     */
    public void doAllocateSelectedPC() {
        
        if (selectedPC == null) {
            Utils.addFacesMessage(NavigationConstants.DEMAND_ALLOC_ERROR_ID,
                    MessageBundle.getMessage(Constants.NO_PC_SELECTED));
        }
        else {
            
            Demand lDemandWithSelectedPC =
                    demandBean.findDemandWithAllocatedPC(selectedPC);
            
            if (lDemandWithSelectedPC != null) {
                
                lDemandWithSelectedPC.setAllocatedPC(null);
                demandBean.merge(lDemandWithSelectedPC);
            }
            
            PC lPreviousAllocatedPC = demand.getAllocatedPC();
            
            demand.setAllocatedPC(selectedPC);
            demandBean.merge(demand);
            
            if (lPreviousAllocatedPC != null) {
                // Update the allocated to demand attribute of the PC previously
                // allocated to the demand
                PC lPreviousAllocatedPCInCache =
                        pcBean.findById(lPreviousAllocatedPC.getId());
                lPreviousAllocatedPCInCache.setAllocatedToDemand(null);
            }
            
            // Update the allocated to demand attribute of selected PC
            PC lSelectedPCInCache = pcBean.findById(selectedPC.getId());
            lSelectedPCInCache.setAllocatedToDemand(demand);
            
            // Navigate to the demand consultation page
            goToReadPage(demandId);
        }
    }
    
    @Override
    protected void createItem() throws Exception {
        
        LocationController lLocationCtrl = findBean(LocationController.class);
        Location lLocation = lLocationCtrl.getLocation();
        LocationManager.validateLocation(lLocation);
        
        if (demand.getSoftwares().size() < 1) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.OS_MANDATORY));
        }
        
        ContainerController lContainerController =
                findBean(ContainerController.class);
        Container lContainer = lContainerController.getContainer();
        
        LogInController lLogInController = findBean(LogInController.class);
        demand.setIssuer(lLogInController.getUserLogged());
        
        // create the demand in database
        demand = demandBean.create(demand, lLocation, lContainer);
        demandId = demand.getId();
        
        //send an email notification to the PC management service
        this.sendNewDemandNotification(demand);
        
        // Update the replacement demand attribute of the PC
        PC lPCToReplace = demand.getPCToReplace();
        if (lPCToReplace != null) {
            PC lPCToReplaceInCache = pcBean.findById(lPCToReplace.getId());
            lPCToReplaceInCache.setReplacementDemand(demand);
        }
    }
    
    private void sendNewDemandNotification(Demand pDemand) {
		//get recipients
    	ApplicationController lAppController =  findBean(ApplicationController.class);
        
    	String lFrom = FacesContext.getCurrentInstance().getExternalContext()
                .getInitParameter("ReminderEmailFrom");
        String lTo = lAppController.getPcdemandemailrecipiant().getValue();
    	
        //email content        
    	StringBuffer lContent = new StringBuffer();
        lContent.append("Dear BOA User,<br /><br />");
        lContent.append("Please be informed that the following PC Demand has been created : ");
        lContent.append("<br />");
        lContent.append("<br />");
        lContent.append("<ul>");

        String lDemandNumber = "<a href=\"" + lAppController.getBOAURL() + 
        		"/demand/DemandManagement.faces?id="+ pDemand.getId() +"&mode=READ\">" +
        		pDemand.getDemandNumber() + "</a>";
        lContent.append("<li>");
        lContent.append("Demand N° : " + lDemandNumber);	
        lContent.append("</li>");
        
        lContent.append("<li>");
        lContent.append("Issuer : " + pDemand.getIssuer().getLoginDetails());
        lContent.append("</li>");
        
        lContent.append("<li>");
        lContent.append("Product Type : "
                + pDemand.getProductTypePC().getLocaleValue());
        lContent.append("</li>");
        
        lContent.append("<li>");
        lContent.append("Type  : " + pDemand.getTypePC().getLabel());
        lContent.append("</li>");
        
        lContent.append("<li>");
        lContent.append("Operating System  : " + pDemand.getSoftwaresString());
        lContent.append("</li>");
        
        lContent.append("<li>");
        String lLocation = pDemand.getLocation().toString();
        lLocation= StringUtil.convertNLtoBR(lLocation);
        lContent.append("Location : " +  lLocation);
        lContent.append("</li>");
        
        if(!pDemand.getComments().isEmpty() && !pDemand.getAdditionalInformation().isEmpty()){
            lContent.append("<li>");
            String lComment = pDemand.getComments() + "\n" + pDemand.getAdditionalInformation();
            lLocation = StringUtil.convertNLtoBR(lComment);
            lContent.append("Additionnals informations : " +  lComment);
            lContent.append("</li>");
        }
        lContent.append("</ul>");
        
        //email footer
        lContent.append("<br /><br />");
        lContent.append("Do not reply to this message, sent by a robot.<br />");
        
        //send email

        try {
			MailUtil.sendMail(lFrom, lTo,
			        "[BOA] New PC Demand", lContent);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void updateItem() throws Exception {
        
        if (demand.getSoftwares().size() < 1) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.OS_MANDATORY));
        }

        LocationController lLocationController =
                findBean(LocationController.class);
        Location lLocation = lLocationController.getLocation();
        
        LocationManager.validateLocation(lLocation);
        
        ContainerController lContainerController =
                findBean(ContainerController.class);
        Container lContainer = lContainerController.getContainer();
        
        demand = demandBean.merge(demand, lLocation, lContainer);
    }
    
    @Override
    protected void deleteItem() throws Exception {
    }
    
    @Override
    protected void initItemWithNew() {
        LogInController lLogInController = findBean(LogInController.class);
        User lUser = lLogInController.getUserLogged();
        
        if (pcToReplaceId != null) {
            PC lPCToReplace = pcBean.findById(pcToReplaceId);
            
            demand = new Demand(lUser, lPCToReplace);
        }
        else {
            demand = new Demand(lUser);
        }
    }
    
    @Override
    protected void initItemFromDatabase() {
        demand = demandBean.findDemandById(demandId);
    }
    
    @Override
    protected void initAttributesFromItem() {
        if (demand != null) {
            // Location reset
            LocationController lLocationCtrl =
                    findBean(LocationController.class);
            lLocationCtrl.setLocation(demand.getLocation());
            lLocationCtrl.setLocatedItem(demand);
            
            // Container reset
            ContainerController lContainerController =
                    findBean(ContainerController.class);
            lContainerController.setContainer(demand.getContainer());
            lContainerController.setContainedItem(demand);
        }
    }

    /**
     * Return true if the page is in consultation, the demand is not ended, the
     * logged user is the demand issuer or a user having enough rights.
     * 
     * @return a boolean indicating if some actions are available for the demand
     *         depending to the logged user
     */
    public boolean isActionsAvailable() {
        
        LogInController lLogInController = findBean(LogInController.class);
        User lLoggedUser = lLogInController.getUserLogged();
        
        if (lLoggedUser == null) {
            return false;
        }
        return isReadOnly()
                && (!demand.isEnded())
                && (lLoggedUser.equals(demand.getIssuer())
                        || lLogInController.isAuthorized(
                                RightCategoryCRUD.DemandCRUD,
                                RightMaskCRUD.CRUD_Update)
                        || lLogInController
                                .isAuthorized(RightMaskAction.DemandConfirm)
                        || lLogInController
                                .isAuthorized(RightMaskAction.DemandAllocate)
                        || lLogInController
                                .isAuthorized(RightMaskAction.DemandMakeAvailable)
                        || lLogInController
                                .isAuthorized(RightMaskAction.DemandClose) || lLogInController
                            .isAuthorized(RightMaskAction.DemandCancel));
    }
    
    /**
     * @return a boolean indicating if the Allocate button is available
     */
    public boolean isAllocationAvailable() {
        
        return demand != null && demand.getStatus() == DemandStatus.Confirmed;
    }
    
    /**
     * @return a boolean indicating if the Cancel button is available
     */
    public boolean isCancellationAvailable() {
        
        if (demand != null) {
            switch (demand.getStatus()) {
            case New:
            case Confirmed:
                return true;
            case Available:
            case Closed:
            case Cancelled:
            default:
                return false;
            }
        }
        return false;
    }
    
    /**
     * @return a boolean indicating if the Close button is available
     */
    public boolean isClosureAvailable() {
        
        return demand != null && demand.getStatus() == DemandStatus.Available;
    }
    
    /**
     * @return a boolean indicating if the Confirm button is available
     */
    public boolean isConfirmationAvailable() {
        
        return demand != null && demand.getStatus() == DemandStatus.New;
    }
    
    /**
     * @return a boolean indicating if the Make available button is available
     */
    public boolean isMakingAvailableAvailable() {
        
        return demand != null && demand.getStatus() == DemandStatus.Confirmed
                && demand.getAllocatedPC() != null;
    }
    
    @Override
    protected void setItemId(Long pId) {
        demandId = pId;
    }
    
    @Override
    protected Long getItemId() {
        return demandId;
    }
    
    /**
     * @return the demand
     */
    public Demand getDemand() {
        return demand;
    }
    
    /**
     * @return the number of available characters into the comment field
     */
    public int getAvailableChars() {
        if (demand.getComments() != null) {
            return COMMENT_LENGTH - demand.getComments().length();
        }
        return COMMENT_LENGTH;
    }
    
    /**
     * @return the pcToReplaceId
     */
    public long getPcToReplaceId() {
        return pcToReplaceId;
    }
    
    /**
     * @param pPcToReplaceId
     *            the pcToReplaceId to set
     */
    public void setPcToReplaceId(long pPcToReplaceId) {
        pcToReplaceId = pPcToReplaceId;
    }
    
    /**
     * @return the types
     */
    public List<SelectItem> getTypes() {
        
        List<TypeArticle> lResult =
                articleBean.findAllTypeArticle(new TypePC());
        
        List<SelectItem> types = new ArrayList<SelectItem>();
        for (TypeArticle lType : lResult) {
            types.add(new SelectItem(lType.getId(), lType.getLabel()));
        }
        return types;
    }
    
    /**
     * @return the replacedPCAvailabilityDate
     */
    public Date getReplacedPCAvailabilityDate() {
        return replacedPCAvailabilityDate;
    }
    
    /**
     * @param pReplacedPCAvailabilityDate
     *            the replacedPCAvailabilityDate to set
     */
    public void setReplacedPCAvailabilityDate(Date pReplacedPCAvailabilityDate) {
        replacedPCAvailabilityDate = pReplacedPCAvailabilityDate;
    }
    
    /**
     * @return the allocationId
     */
    public Long getAllocationId() {
        if (demand.getAllocation() != null) {
            return demand.getAllocation().getId();
        }
        else {
            return null;
        }
    }
    
    /**
     * @param pAllocationId
     *            the allocationId to set
     */
    public void setAllocationId(Long pAllocationId) {
        BusinessAllocationPC lAllocation =
                valueListBean.findAttributeValueListById(
                        BusinessAllocationPC.class, pAllocationId);
        demand.setAllocation(lAllocation);
    }
    
    /**
     * @return List of possible allocations as items for combobox.
     */
    public List<SelectItem> getAllocations() {
        return valueListBean.generateSelectItems(BusinessAllocationPC.class);
    }
    
    /**
     * @return the productTypePC id of the demand
     */
    public Long getDemandProductTypePCId() {
        
        ProductTypePC lProductTypePC = demand.getProductTypePC();
        if (lProductTypePC != null) {
            return lProductTypePC.getId();
        }
        return null;
    }
    
    /**
     * @param pProductTypePCId
     *            the productTypePC id to set in the demand
     */
    public void setDemandProductTypePCId(Long pProductTypePCId) {
        
        ProductTypePC lProductTypePC = null;
        if (pProductTypePCId != null) {
            lProductTypePC =
                    valueListBean.findAttributeValueListById(
                            ProductTypePC.class, pProductTypePCId);
        }
        demand.setProductTypePC(lProductTypePC);
    }
    
    /**
     * @return the product types
     */
    public List<SelectItem> getProductTypes() {
        return valueListBean.generateSelectItems(ProductTypePC.class);
    }
    
    /**
     * @return the typePC id of the demand
     */
    public Long getDemandTypePCId() {
        
        TypePC lTypePC = demand.getTypePC();
        if (lTypePC != null) {
            return lTypePC.getId();
        }
        return null;
    }
    
    /**
     * @param pTypePCId
     *            the typePC id to set in the demand
     */
    public void setDemandTypePCId(Long pTypePCId) {
        
        TypePC lTypePC = null;
        if (pTypePCId != null) {
            lTypePC = (TypePC) articleBean.findTypeArticleById(pTypePCId);
        }
        demand.setTypePC(lTypePC);
    }
    
    /**
     * @return the usageId or null if demand is null
     */
    public Long getUsageId() {
        if (demand.getUsage() != null) {
            return demand.getUsage().getId();
        }
        else {
            return null;
        }
    }
    
    /**
     * @param pUsageId
     *            the usageId to set
     */
    public void setUsageId(Long pUsageId) {
        BusinessUsagePC lUsage = valueListBean
                .findAttributeValueListById(BusinessUsagePC.class, pUsageId);
        demand.setUsage(lUsage);
    }
    
    /**
     * @return List of possible usages as items for combobox.
     */
    public List<SelectItem> getUsages() {
        return valueListBean.generateSelectItems(BusinessUsagePC.class);
    }
    
    /**
     * @return the software
     */
    public Software getSoftware() {
        return software;
    }
    
    /**
     * @param software
     *            the software to set
     */
    public void setSoftware(Software software) {
        this.software = software;
    }
    
    /**
     * Add selected software product to the software products list
     */
    public void addSoftware() {
        
        SoftwareController lSoftwareController =
                findBean(SoftwareController.class);
        
        Software lSoftware = lSoftwareController.getSoftwareToInstall();
        
        if (lSoftware == null) {
            Utils.addFacesMessage(NavigationConstants.INSTALL_SOFT_ERROR_ID,
                    MessageBundle
                            .getMessage(MessageConstants.SOFTWARE_NOT_FOUND));
        }
        else if (!demand.getSoftwares().contains(lSoftware)) {
            // Add software product to the list
            demand.getSoftwares().add(lSoftware);
        }
    }
    
    /**
     * Remove the software product from the software products list
     */
    public void removeSoftware() {
        demand.getSoftwares().remove(software);
    }
    
    /**
     * @return the list of all software being Operating Systems
     */
    public List<Software> getSoftwareOSList() {
        return softwareBean.findAllOperatingSystems();
    }
    
    /**
     * @return the softwares
     */
    public List<Software> getSoftwares() {
        return demand.getSoftwares();
    }
    
    /**
     * @param softwares
     *            the softwares to set
     */
    public void setSoftwares(List<Software> softwares) {
        demand.setSoftwares(softwares);
    }

    public Long getSelectedPCId() {
        return selectedPCId;
    }
    
    public void setSelectedPCId(Long selectedPCId) {
        this.selectedPCId = selectedPCId;
    }

    public PC getSelectedPC() {
        return selectedPC;
    }

    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createDemandTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoDemandTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateDemandTitle");
    }
}
