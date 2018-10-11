/*
 * ------------------------------------------------------------------------
 * Class : ObsolescenceReference
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.obso;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.type.TypeArticle;

/**
 * Entity implementation class for embedded Entity: ObsolescenceReference
 */
@Embeddable
public class ObsolescenceReference implements Serializable,
        Comparable<ObsolescenceReference> {
    
    /**
     * Enumerate of the possible reference types
     */
    public static enum ReferenceType {
        /** The reference is a couple Airbus PN - article type */
        AIRBUSPN_TYPEARTICLE,
        /** The reference is a couple manufacturer PN - article type */
        MANUFACTURERPN_TYPEARTICLE,
        /** The reference is a PC type */
        TYPEPC,
        /** The reference is a software */
        SOFTWARE;
        
        @Override
        public String toString() {
            return "ObsolescenceReferenceType." + super.toString();
        }
    };
    
    private static final long serialVersionUID = 1L;
    
    /** The Airbus PN column name */
    public static final String APN_COLUMN_NAME = "AIRBUSPN_ID";
    /** The Manufacturer PN column name */
    public static final String MPN_COLUMN_NAME = "MANUFACTURERPN_ID";
    /** The type article column name */
    public static final String TYPEARTICLE_COLUMN_NAME = "TYPEARTICLE_ID";
    /** The type PC column name */
    public static final String TYPEPC_TYPEARTICLE_COLUMN_NAME =
            "TYPEPC_TYPEARTICLE_ID";
    /** The software column name */
    public static final String SOFTWARE_COLUMN_NAME = "SOFTWARE_ID";
    
    @ManyToOne
    @JoinColumn(name = APN_COLUMN_NAME)
    private AirbusPN airbusPN;
    
    @ManyToOne
    @JoinColumn(name = MPN_COLUMN_NAME)
    private ManufacturerPN manufacturerPN;
    
    @ManyToOne
    @JoinColumn(name = TYPEARTICLE_COLUMN_NAME)
    private TypeArticle typeArticle;
    
    @OneToOne
    @JoinColumn(name = SOFTWARE_COLUMN_NAME, unique = true)
    private Software software;
    
    @OneToOne
    @JoinColumn(name = TYPEPC_TYPEARTICLE_COLUMN_NAME, unique = true)
    private TypeArticle typePC;
    
    @Transient
    private ReferenceType referenceType;
    
    /**
     * Default constructor
     */
    public ObsolescenceReference() {
        reset();
    }
    
    /**
     * Constructor
     * 
     * @param pAirbusPN
     *            the airbusPN to set
     * @param pTypeArticle
     *            the typeArticle to set
     */
    public ObsolescenceReference(AirbusPN pAirbusPN, TypeArticle pTypeArticle) {
        
        setAirbusPN(pAirbusPN, pTypeArticle);
    }
    
    /**
     * Constructor
     * 
     * @param pManufacturerPN
     *            the manufacturerPN to set
     * @param pTypeArticle
     *            the typeArticle to set
     */
    public ObsolescenceReference(ManufacturerPN pManufacturerPN,
            TypeArticle pTypeArticle) {
        
        setManufacturerPN(pManufacturerPN, pTypeArticle);
    }
    
    /**
     * Constructor
     * 
     * @param pSoftware
     *            the software to set
     */
    public ObsolescenceReference(Software pSoftware) {
        
        setSoftware(pSoftware);
    }
    
    /**
     * Constructor
     * 
     * @param pTypePC
     *            the typePC to set
     */
    public ObsolescenceReference(TypeArticle pTypePC) {
        
        setTypePC(pTypePC);
    }
    
    @Override
    public int hashCode() {
        final int lPrime = 31;
        int lResult = 1;
        lResult =
                lPrime * lResult
                        + ((airbusPN == null) ? 0 : airbusPN.hashCode());
        lResult =
                lPrime
                        * lResult
                        + ((manufacturerPN == null) ? 0 : manufacturerPN
                                .hashCode());
        lResult =
                lPrime * lResult
                        + ((typeArticle == null) ? 0 : typeArticle.hashCode());
        lResult = lPrime * lResult + ((typePC == null) ? 0 : typePC.hashCode());
        lResult =
                lPrime * lResult
                        + ((software == null) ? 0 : software.hashCode());
        return lResult;
    }
    
    @Override
    public boolean equals(Object pObject) {
        if (this == pObject) {
            return true;
        }
        if (pObject == null) {
            return false;
        }
        if (getClass() != pObject.getClass()) {
            return false;
        }
        ObsolescenceReference lOther = (ObsolescenceReference) pObject;
        if (airbusPN == null) {
            if (lOther.airbusPN != null) {
                return false;
            }
        }
        else if (!airbusPN.equals(lOther.airbusPN)) {
            return false;
        }
        if (typePC == null) {
            if (lOther.typePC != null) {
                return false;
            }
        }
        else if (!typePC.equals(lOther.typePC)) {
            return false;
        }
        if (manufacturerPN == null) {
            if (lOther.manufacturerPN != null) {
                return false;
            }
        }
        else if (!manufacturerPN.equals(lOther.manufacturerPN)) {
            return false;
        }
        if (software == null) {
            if (lOther.software != null) {
                return false;
            }
        }
        else if (!software.equals(lOther.software)) {
            return false;
        }
        if (typeArticle == null) {
            if (lOther.typeArticle != null) {
                return false;
            }
        }
        else if (!typeArticle.equals(lOther.typeArticle)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        
        computeReferenceType();
        
        if (referenceType == null) {
            return "";
        }
        
        switch (referenceType) {
        case AIRBUSPN_TYPEARTICLE:
            return typeArticle.toString() + " - " + airbusPN.toString();
        case MANUFACTURERPN_TYPEARTICLE:
            return typeArticle.toString() + " - " + manufacturerPN.toString();
        case TYPEPC:
            return typePC.toString();
        case SOFTWARE:
            return software.toString();
        default:
            return "ERROR! Unknown reference type";
        }
    }
    
    @Override
    public int compareTo(ObsolescenceReference pRef) {
        
        if (pRef == null) {
            throw new NullPointerException();
        }
        
        computeReferenceType();
        pRef.computeReferenceType();
        
        if (referenceType == null) {
            return -1;
        }
        
        if (referenceType.equals(pRef.referenceType)) {
            return toString().compareTo(pRef.toString());
        }
        return referenceType.compareTo(pRef.referenceType);
    }
    
    /**
     * Update the reference type according to the other attributes
     */
    public void computeReferenceType() {
        
        if (airbusPN != null && typeArticle != null) {
            referenceType = ReferenceType.AIRBUSPN_TYPEARTICLE;
        }
        else if (manufacturerPN != null && typeArticle != null) {
            referenceType = ReferenceType.MANUFACTURERPN_TYPEARTICLE;
        }
        else if (typePC != null) {
            referenceType = ReferenceType.TYPEPC;
        }
        else if (software != null) {
            referenceType = ReferenceType.SOFTWARE;
        }
        else {
            referenceType = null;
        }
    }
    
    /**
     * Return a boolean indicating if this is defined for exactly one reference
     * 
     * @return true if this is defined for exactly one reference, else false
     */
    public boolean isOnlyOneReferenceDefined() {
        
        int lCount = 0;
        
        if (typePC != null) {
            lCount++;
        }
        if (airbusPN != null && typeArticle != null) {
            lCount++;
        }
        if (manufacturerPN != null && typeArticle != null) {
            lCount++;
        }
        if (software != null) {
            lCount++;
        }
        
        return (lCount == 1);
    }
    
    /**
     * Reset all attributes
     */
    private void reset() {
        airbusPN = null;
        manufacturerPN = null;
        typeArticle = null;
        typePC = null;
        software = null;
        referenceType = null;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return toString();
    }
    
    /**
     * @return the airbusPN
     */
    public AirbusPN getAirbusPN() {
        return airbusPN;
    }
    
    /**
     * @param pAirbusPN
     *            the airbusPN to set
     * @param pTypeArticle
     *            the typeArticle to set
     */
    public void setAirbusPN(AirbusPN pAirbusPN, TypeArticle pTypeArticle) {
        
        reset();
        if (pAirbusPN != null && pTypeArticle != null) {
            airbusPN = pAirbusPN;
            typeArticle = pTypeArticle;
            referenceType = ReferenceType.AIRBUSPN_TYPEARTICLE;
        }
    }
    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        
        computeReferenceType();
        
        if (referenceType == null) {
            return "";
        }
        
        switch (referenceType) {
        case AIRBUSPN_TYPEARTICLE:
        case MANUFACTURERPN_TYPEARTICLE:
            return typeArticle.getManufacturer();
        case TYPEPC:
            return typePC.getManufacturer();
        case SOFTWARE:
            return software.getManufacturer();
        default:
            return "ERROR! Unknown reference type";
        }
    }
    
    /**
     * @return the manufacturerPN
     */
    public ManufacturerPN getManufacturerPN() {
        return manufacturerPN;
    }
    
    /**
     * @param pManufacturerPN
     *            the manufacturerPN to set
     * @param pTypeArticle
     *            the typeArticle to set
     */
    public void setManufacturerPN(ManufacturerPN pManufacturerPN,
            TypeArticle pTypeArticle) {
        
        reset();
        if (pManufacturerPN != null && pTypeArticle != null) {
            manufacturerPN = pManufacturerPN;
            typeArticle = pTypeArticle;
            referenceType = ReferenceType.MANUFACTURERPN_TYPEARTICLE;
        }
    }
    
    /**
     * @return the typeArticle
     */
    public TypeArticle getTypeArticle() {
        return typeArticle;
    }
    
    /**
     * @return the software
     */
    public Software getSoftware() {
        return software;
    }
    
    /**
     * @param pSoftware
     *            the software to set
     */
    public void setSoftware(Software pSoftware) {
        
        reset();
        if (pSoftware != null) {
            software = pSoftware;
            referenceType = ReferenceType.SOFTWARE;
        }
    }
    
    /**
     * @return the typePC
     */
    public TypeArticle getTypePC() {
        return typePC;
    }
    
    /**
     * @param pTypePC
     *            the typePC to set
     */
    public void setTypePC(TypeArticle pTypePC) {
        
        reset();
        if (pTypePC != null) {
            typePC = pTypePC;
            referenceType = ReferenceType.TYPEPC;
        }
    }
    
    /**
     * @return the referenceType
     */
    public ReferenceType getReferenceType() {
        
        computeReferenceType();
        return referenceType;
    }
    
}
