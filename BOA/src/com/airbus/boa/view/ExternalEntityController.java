/*
 * ------------------------------------------------------------------------
 * Class : ExternalEntityController
 * Copyright 2014 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.airbus.boa.control.DemandBean;
import com.airbus.boa.control.LocationBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.exception.ExportException;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.io.column.ColumnExternalEntity;
import com.airbus.boa.io.writer.IOExcelWriterExternalEntity;
import com.airbus.boa.service.Constants;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.MessageBundle;

/**
 * Controller managing the external entity creation, modification and deletion
 */
@ManagedBean(name = ExternalEntityController.BEAN_NAME)
@ViewScoped
public class ExternalEntityController extends AbstractController {
    
    private static final long serialVersionUID = 2185523763610365644L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "externalEntityController";
    
    @EJB
    private LocationBean locationBean;
    
    @EJB
    private DemandBean demandBean;
    
    @EJB
    private ValueListBean valueListBean;
    
    private String newExternalEntityName;
    
    private Long newExternalEntityTypeId;
    
    private Long selectedExternalEntityId;
    private List<ExternalEntity> externalEntities;
    
    private ExternalEntity selectedExternalEntity;
    
    // gestion de la mise � jour partiel de la table
    private Set<Integer> rowsToUpdate = new HashSet<Integer>();
    
    // gestion de la modification du fournisseur
    private String updateName;
    private Long updateTypeId;
    
    /**
     * Add the external entity to the database
     */
    public void doAddExternalEntity() {
        
        try {
            if (newExternalEntityName != null
                    && !newExternalEntityName.trim().isEmpty()
                    && newExternalEntityTypeId != null) {
                
                if (locationBean.findExternalEntityByName(newExternalEntityName
                        .trim()) != null) {
                    String lMsg =
                            MessageBundle
                                    .getMessage(Constants.EXTERNALENTITYNAME_ALREADY_USED);
                    Utils.addFacesMessage(
                            NavigationConstants.EXTERNAL_ENTITY_ERROR_ID, lMsg);
                    return;
                }
                
                ExternalEntityType lExternalEntityType =
                        valueListBean.findAttributeValueListById(
                                ExternalEntityType.class,
                                newExternalEntityTypeId);
                
                ExternalEntity lExternalEntity =
                        new ExternalEntity(newExternalEntityName.trim(),
                                lExternalEntityType);
                locationBean.createExternalEntity(lExternalEntity);
            }
        }
        catch (Exception e) {
            Utils.addFacesMessage(NavigationConstants.EXTERNAL_ENTITY_ERROR_ID,
                    ExceptionUtil.getMessage(e));
        }
    }
    
    /**
     * Remove the external entity from the database
     */
    public void doDeleteExternalEntity() {
        try {
            ExternalEntity lExternalEntity =
                    locationBean
                            .findExternalEntityById(selectedExternalEntityId);
            
            // Check that no item is located into the selected external entity
            if (!lExternalEntity.getLocatedItemsDirectly().isEmpty()) {
                Utils.addFacesMessage(
                        NavigationConstants.EXTERNAL_ENTITY_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.EXTERNAL_ENTITY_DELETION_CONTAINS_ITEMS));
            }
            
            // Check that no demand is located into the selected external entity
            else if (!demandBean.findDemandsLocatedIntoExternalEntity(
                    lExternalEntity).isEmpty()) {
                Utils.addFacesMessage(
                        NavigationConstants.EXTERNAL_ENTITY_ERROR_ID,
                        MessageBundle
                                .getMessage(Constants.EXTERNAL_ENTITY_DELETION_CONTAINS_DEMAND));
            }
            
            else {
                locationBean.removeExternalEntity(lExternalEntity);
                setSelectedExternalEntityId(null);
            }
            
        }
        catch (ValidationException ve) {
            Utils.addFacesMessage(NavigationConstants.EXTERNAL_ENTITY_ERROR_ID,
                    ve.getMessage());
        }
    }
    
    public void doExport() {
        try {
            Workbook lWorkbook = new XSSFWorkbook();
            IOExcelWriterExternalEntity lSheet =
                    new IOExcelWriterExternalEntity(lWorkbook,
                            new ColumnExternalEntity());
            lSheet.writeHeader();
            
            for (ExternalEntity lExternalEntity : externalEntities) {
                // Ecriture dans la feuille
                lSheet.writeOne(lExternalEntity);
            }
            // Application du style
            lSheet.applyStyles();
            
            download(lSheet, "externalEntities.xlsx",
                    ExportController.MIMETYPE_XLSX);
        }
        catch (ExportException e) {
        }
    }
    
    /**
     * Update the external entity in database
     */
    public void doUpdateExternalEntity() {
        
        if (selectedExternalEntityId != null) {
            
            try {
                
                ExternalEntity lExternalEntity =
                        locationBean
                                .findExternalEntityById(selectedExternalEntityId);
                
                if (updateName != null && updateName.trim().length() > 0) {
                    
                    rowsToUpdate.clear();
                    rowsToUpdate.add(getExternalEntities().indexOf(
                            lExternalEntity));
                    
                    ExternalEntityType lExternalEntityType =
                            valueListBean.findAttributeValueListById(
                                    ExternalEntityType.class, updateTypeId);

                    lExternalEntity.setName(updateName);
                    lExternalEntity.setExternalEntityType(lExternalEntityType);
                    lExternalEntity = locationBean.merge(lExternalEntity);
                    
                }
                
            }
            catch (Exception e) {
                Utils.addFacesMessage(
                        NavigationConstants.UPDATE_EXTERNAL_ENTITY_ERROR_ID,
                        ExceptionUtil.getMessage(e));
            }
            
        }
    }
    
    /**
     * Validate the external entity name
     * 
     * @param pContext
     *            <i>mandatory for JSF validator</i>
     * @param pComponent
     *            <i>mandatory for JSF validator</i>
     * @param pValue
     *            the name to valid
     * @throws ValidatorException
     *             when the temporary precision is not valid
     */
    public void validateExternalEntityName(FacesContext pContext,
            UIComponent pComponent, Object pValue)
            throws ValidatorException {
        
        String lValeur = (String) pValue;
        lValeur = (lValeur == null) ? "" : lValeur.trim();
        if (lValeur.isEmpty()) {
            String lMsg =
                    MessageBundle
                            .getMessage("javax.faces.component.UIInput.REQUIRED");
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
        try {
            ExternalEntity lExternalEntity =
                    locationBean.findExternalEntityByName(lValeur);
            if (lExternalEntity != null
                    && lExternalEntity.getId() != selectedExternalEntityId) {
                
                String lMsg =
                        MessageBundle
                                .getMessage(Constants.EXTERNALENTITYNAME_ALREADY_USED);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
            }
            
        }
        catch (Exception e) {
            String lMsg = ExceptionUtil.getRootCause(e).getMessage();
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, lMsg, lMsg));
        }
        
    }
    
    /**
     * @return the externalEntities
     */
    public List<ExternalEntity> getExternalEntities() {
        externalEntities = locationBean.findAllExternalEntities();
        return externalEntities;
    }
    
    /**
     * @param pExternalEntities
     *            the externalEntities to set
     */
    public void setExternalEntities(List<ExternalEntity> pExternalEntities) {
        externalEntities = pExternalEntities;
    }
    
    /**
     * @return the list of available external entities types
     */
    public List<SelectItem> getExternalEntityTypes() {
        return valueListBean.generateSelectItems(ExternalEntityType.class);
    }
    
    /**
     * @return the newExternalEntityName
     */
    public String getNewExternalEntityName() {
        return newExternalEntityName;
    }
    
    /**
     * @param pNewExternalEntityName
     *            the newExternalEntityName to set
     */
    public void setNewExternalEntityName(String pNewExternalEntityName) {
        newExternalEntityName = pNewExternalEntityName;
    }
    
    /**
     * @return the newExternalEntityTypeId
     */
    public Long getNewExternalEntityTypeId() {
        return newExternalEntityTypeId;
    }
    
    /**
     * @param pNewExternalEntityTypeId
     *            the newExternalEntityTypeId to set
     */
    public void setNewExternalEntityTypeId(Long pNewExternalEntityTypeId) {
        newExternalEntityTypeId = pNewExternalEntityTypeId;
    }
    
    public Set<Integer> getRowsToUpdate() {
        return rowsToUpdate;
    }
    
    public void setRowsToUpdate(Set<Integer> pRowsToUpdate) {
        rowsToUpdate = pRowsToUpdate;
    }
    
    /**
     * @return the selectedExternalEntity
     */
    public ExternalEntity getSelectedExternalEntity() {
        return selectedExternalEntity;
    }
    
    /**
     * @param pSelectedExternalEntity
     *            the selectedExternalEntity to set
     */
    public void
            setSelectedExternalEntity(ExternalEntity pSelectedExternalEntity) {
        selectedExternalEntity = pSelectedExternalEntity;
    }
    
    /**
     * @return the selectedExternalEntityId
     */
    public Long getSelectedExternalEntityId() {
        return selectedExternalEntityId;
    }
    
    /**
     * @param pSelectedExternalEntityId
     *            the selectedExternalEntityId to set
     */
    public void setSelectedExternalEntityId(Long pSelectedExternalEntityId) {
        selectedExternalEntityId = pSelectedExternalEntityId;
    }
    
    public String getUpdateName() {
        return updateName;
    }
    
    public void setUpdateName(String pUpdateName) {
        updateName = pUpdateName;
    }

    /**
     * @return the updateType
     */
    public Long getUpdateTypeId() {
        return updateTypeId;
    }

    /**
     * @param updateType the updateType to set
     */
    public void setUpdateTypeId(Long updateTypeId) {
        this.updateTypeId = updateTypeId;
    }
    
}
