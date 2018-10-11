/*
 * ------------------------------------------------------------------------
 * Class : ObsolescenceData
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.obso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;
import com.airbus.boa.view.stock.Stock;

/**
 * Entity implementation class for Entity: ObsolescenceData
 */
@Entity
@Table(name = ObsolescenceData.TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                ObsolescenceReference.APN_COLUMN_NAME,
                ObsolescenceReference.TYPEARTICLE_COLUMN_NAME }),
        @UniqueConstraint(columnNames = {
                ObsolescenceReference.MPN_COLUMN_NAME,
                ObsolescenceReference.TYPEARTICLE_COLUMN_NAME }) })
@NamedQueries({
        @NamedQuery(name = ObsolescenceData.ALL_QUERY,
                query = "SELECT o FROM ObsolescenceData o"
                        + " LEFT JOIN FETCH o.airbusStatus"
                        + " LEFT JOIN FETCH o.manufacturerStatus"
                        + " LEFT JOIN FETCH o.currentAction"
                        + " LEFT JOIN FETCH o.strategyKept"
                        + " LEFT JOIN FETCH o.consultPeriod"),
        @NamedQuery(name = ObsolescenceData.OBSOLESCENCE_DATA_BY_TYPEPC,
                query = "SELECT o FROM ObsolescenceData o"
                        + " WHERE o.reference.typePC = :reference"),
        @NamedQuery(name = ObsolescenceData.OBSOLESCENCE_DATA_BY_SOFTWARE,
                query = "SELECT o FROM ObsolescenceData o"
                        + " WHERE o.reference.software = :reference"),
        @NamedQuery(
                name = ObsolescenceData.OBSOLESCENCE_DATA_BY_AIRBUSPN_TYPEARTICLE,
                query = "SELECT o FROM ObsolescenceData o"
                        + " WHERE o.reference.airbusPN = :refAirbusPN"
                        + " AND o.reference.typeArticle = :refTypeArticle"),
        @NamedQuery(
                name = ObsolescenceData.OBSOLESCENCE_DATA_BY_MANUFACTURERPN_TYPEARTICLE,
                query = "SELECT o FROM ObsolescenceData o"
                        + " WHERE o.reference.manufacturerPN = :refManufacturerPN"
                        + " AND o.reference.typeArticle = :refTypeArticle") })
public class ObsolescenceData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "obsolescencedata";
    /** The comment on strategy column name */
    public static final String COMMENTONSTRATEGY_COLUMN_NAME =
            "COMMENTONSTRATEGY";
    /** The person in charge column name */
    public static final String PERSONINCHARGE_COLUMN_NAME = "PERSONINCHARGE";
    /** The supplier column name */
    public static final String SUPPLIER_COLUMN_NAME = "SUPPLIER";
    
    /** The query retrieving all obsolescence data */
    public static final String ALL_QUERY = "findAllObsoDatas";
    
    /**
     * Name of the named query selecting the ObsolescenceData having the
     * provided reference.<br>
     * This query is parameterized:<br>
     * <b>refTypePC</b> the TypePC
     */
    public static final String OBSOLESCENCE_DATA_BY_TYPEPC = "obsoDataByTypePC";
    
    /**
     * Name of the named query selecting the ObsolescenceData having the
     * provided reference.<br>
     * This query is parameterized:<br>
     * <b>refSoftware</b> the Software
     */
    public static final String OBSOLESCENCE_DATA_BY_SOFTWARE =
            "obsoDataBySoftware";
    
    /**
     * Name of the named query selecting the ObsolescenceData having the
     * provided reference.<br>
     * This query is parameterized:<br>
     * <b>refAirbusPN</b> the AirbusPN<br>
     * <b>refTypeArticle</b> the TypeArticle
     */
    public static final String OBSOLESCENCE_DATA_BY_AIRBUSPN_TYPEARTICLE =
            "obsoDataByAirbusPNTypeArt";
    
    /**
     * Name of the named query selecting the ObsolescenceData having the
     * provided reference.<br>
     * This query is parameterized:<br>
     * <b>refManufacturerPN</b> the ManufacturerPN<br>
     * <b>refTypeArticle</b> the TypeArticle
     */
    public static final String OBSOLESCENCE_DATA_BY_MANUFACTURERPN_TYPEARTICLE =
            "obsoDataByManPNTypeArt";
    
    /**
     * Compute the next consulting date by adding the consult period to the last
     * consulting date
     * 
     * @param date
     *            the last consulting date
     * @param consultPeriod
     *            the consult period
     * @return the next consulting date
     */
    public static Date calculateNextConsultingDate(Date date,
            ConsultPeriod consultPeriod) {
        
        Date nextDate = null;
        
        if (consultPeriod != null && date != null
                && consultPeriod.getMonthNumber() != null) {
            
            Calendar calendar = Calendar.getInstance();
            Integer month = consultPeriod.getMonthNumber();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, month);
            nextDate = calendar.getTime();
        }
        
        return nextDate;
    }
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Embedded
    private ObsolescenceReference reference = new ObsolescenceReference();
    
    @OneToOne(fetch = FetchType.LAZY)
    private AirbusStatus airbusStatus;
    
    @Column
    private String commentOnStrategy;
    
    @Temporal(TemporalType.DATE)
    private Date continuityDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIONOBSO_ID")
    private ActionObso currentAction;
    
    @Temporal(TemporalType.DATE)
    private Date endOfOrderDate;
    
    @Temporal(TemporalType.DATE)
    private Date endOfProductionDate;
    
    @Temporal(TemporalType.DATE)
    private Date endOfSupportDate;
    
    @Temporal(TemporalType.DATE)
    private Date lastObsolescenceUpdate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    @OneToOne(fetch = FetchType.LAZY)
    private ManufacturerStatus manufacturerStatus;
    
    @Column
    private Long mtbf;
    
    @Temporal(TemporalType.DATE)
    private Date obsolescenceDate;
    
    @Column
    private String personInCharge;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRATEGY_ID")
    private Strategy strategyKept;
    
    @Column
    private String supplier;
    
    @OneToOne(fetch = FetchType.LAZY)
    private ConsultPeriod consultPeriod;
    
    @Transient
    private Stock stock;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        if (reference == null) {
            result = prime * result + 0;
        }
        else {
            result = prime * result + reference.hashCode();
        }
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
        ObsolescenceData other = (ObsolescenceData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        }
        else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }
    
    /**
     * Update the creation and the last update date for database
     */
    @PrePersist
    public void onCreate() {
        creationDate = lastUpdate = new Date();
        validate();
    }
    
    /**
     * Update the last update date for database
     */
    @PreUpdate
    public void onUpdate() {
        lastUpdate = new Date();
        validate();
    }
    
    /**
     * Check if this is defined for one reference
     */
    public void validate() {
        if (!reference.isOnlyOneReferenceDefined()) {
            String msg =
                    MessageBundle
                            .getMessage(Constants.OBSO_MULTI_REFERENCE_CREATION_ERROR);
            throw new ValidationException(msg);
        }
    }
    
    /**
     * @return the consultPeriod
     */
    public ConsultPeriod getConsultPeriod() {
        return consultPeriod;
    }
    
    /**
     * @param consultPeriod
     *            the consultPeriod to set
     */
    public void setConsultPeriod(ConsultPeriod consultPeriod) {
        this.consultPeriod = consultPeriod;
    }
    
    /**
     * @return the airbusStatus
     */
    public AirbusStatus getAirbusStatus() {
        return airbusStatus;
    }
    
    /**
     * @param airbusStatus
     *            the airbusStatus to set
     */
    public void setAirbusStatus(AirbusStatus airbusStatus) {
        this.airbusStatus = airbusStatus;
    }
    
    /**
     * @return the commentOnStrategy
     */
    public String getCommentOnStrategy() {
        return commentOnStrategy;
    }
    
    /**
     * @param commentOnStrategy
     *            the commentOnStrategy to set
     */
    public void setCommentOnStrategy(String commentOnStrategy) {
        this.commentOnStrategy = commentOnStrategy;
    }
    
    /**
     * @return the constituantName
     */
    public String getConstituantName() {
        if (reference != null) {
            if (reference.getReferenceType() != null) {
                return reference.getName();
            }
            else if (reference.getAirbusPN() != null) {
                return "ERROR! no Type Article - "
                        + reference.getAirbusPN().toString();
            }
            else if (reference.getManufacturerPN() != null) {
                return "ERROR! no Type Article - "
                        + reference.getManufacturerPN().toString();
            }
        }
        return "ERROR! Reference undefined";
    }
    
    /**
     * @return the continuityDate
     */
    public Date getContinuityDate() {
        return continuityDate;
    }
    
    /**
     * @param continuityDate
     *            the continuityDate to set
     */
    public void setContinuityDate(Date continuityDate) {
        this.continuityDate = continuityDate;
    }
    
    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }
    
    /**
     * @return the currentAction
     */
    public ActionObso getCurrentAction() {
        return currentAction;
    }
    
    /**
     * @param currentAction
     *            the currentAction to set
     */
    public void setCurrentAction(ActionObso currentAction) {
        this.currentAction = currentAction;
    }
    
    /**
     * @return the endOfOrderDate
     */
    public Date getEndOfOrderDate() {
        return endOfOrderDate;
    }
    
    /**
     * @param endOfOrderDate
     *            the endOfOrderDate to set
     */
    public void setEndOfOrderDate(Date endOfOrderDate) {
        this.endOfOrderDate = endOfOrderDate;
    }
    
    /**
     * @return the endOfProductionDate
     */
    public Date getEndOfProductionDate() {
        return endOfProductionDate;
    }
    
    /**
     * @param endOfProductionDate
     *            the endOfProductionDate to set
     */
    public void setEndOfProductionDate(Date endOfProductionDate) {
        this.endOfProductionDate = endOfProductionDate;
    }
    
    /**
     * @return the endOfSupportDate
     */
    public Date getEndOfSupportDate() {
        return endOfSupportDate;
    }
    
    /**
     * @param endOfSupportDate
     *            the endOfSupportDate to set
     */
    public void setEndOfSupportDate(Date endOfSupportDate) {
        this.endOfSupportDate = endOfSupportDate;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @return the lastObsolescenceUpdate
     */
    public Date getLastObsolescenceUpdate() {
        return lastObsolescenceUpdate;
    }
    
    /**
     * @param lastObsolescenceUpdate
     *            the lastObsolescenceUpdate to set
     */
    public void setLastObsolescenceUpdate(Date lastObsolescenceUpdate) {
        this.lastObsolescenceUpdate = lastObsolescenceUpdate;
    }
    
    /**
     * @return the lastUpdate
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        if (reference != null) {
            if (reference.getReferenceType() != null) {
                return reference.getManufacturer();
            }
            else if (reference.getTypeArticle() != null) {
                return reference.getTypeArticle().getManufacturer();
            }
            else if (reference.getTypePC() != null) {
                return reference.getTypePC().getManufacturer();
            }
        }
        return "ERROR! Reference undefined";
    }
    
    /**
     * @return the manufacturerStatus
     */
    public ManufacturerStatus getManufacturerStatus() {
        return manufacturerStatus;
    }
    
    /**
     * @param manufacturerStatus
     *            the manufacturerStatus to set
     */
    public void setManufacturerStatus(ManufacturerStatus manufacturerStatus) {
        this.manufacturerStatus = manufacturerStatus;
    }
    
    /**
     * @return the mtbf
     */
    public Long getMtbf() {
        return mtbf;
    }
    
    /**
     * @param mtbf
     *            the mtbf to set
     */
    public void setMtbf(Long mtbf) {
        this.mtbf = mtbf;
    }
    
    /**
     * @return the nextConsultingDate
     */
    public Date getNextConsultingDate() {
        return calculateNextConsultingDate(lastObsolescenceUpdate,
                consultPeriod);
    }
    
    /**
     * @return the obsolescenceDate
     */
    public Date getObsolescenceDate() {
        return obsolescenceDate;
    }
    
    /**
     * @param obsolescenceDate
     *            the obsolescenceDate to set
     */
    public void setObsolescenceDate(Date obsolescenceDate) {
        this.obsolescenceDate = obsolescenceDate;
    }
    
    /**
     * @return the personInCharge
     */
    public String getPersonInCharge() {
        return personInCharge;
    }
    
    /**
     * @param personInCharge
     *            the personInCharge to set
     */
    public void setPersonInCharge(String personInCharge) {
        this.personInCharge = personInCharge;
    }
    
    /**
     * @return the strategyKept
     */
    public Strategy getStrategyKept() {
        return strategyKept;
    }
    
    /**
     * @param strategyKept
     *            the strategyKept to set
     */
    public void setStrategyKept(Strategy strategyKept) {
        this.strategyKept = strategyKept;
    }
    
    /**
     * @return the supplier
     */
    public String getSupplier() {
        return supplier;
    }
    
    /**
     * @param supplier
     *            the supplier to set
     */
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    /**
     * @return the reference
     */
    public ObsolescenceReference getReference() {
        
        return reference;
    }
    
    /**
     * @param pReference
     *            the reference to set
     */
    public void setReference(ObsolescenceReference pReference) {
        
        reference = pReference;
    }
    
    /**
     * @return the stock
     */
    public Stock getStock() {
        return stock;
    }

    /**
     * @param pStock
     *            the stock to set
     */
    public void setStock(Stock pStock) {
        stock = pStock;
    }

    /**
     * @return the list of installations of the stock
     */
    public List<Installation> getInstallations() {
        return new ArrayList<>(stock.getRepartitionMapKeys());
    }
}
