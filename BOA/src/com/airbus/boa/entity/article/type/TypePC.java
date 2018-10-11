/*
 * ------------------------------------------------------------------------
 * Class : TypePC
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article.type;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import com.airbus.boa.entity.obso.ObsolescenceData;

/**
 * Entity implementation class for Entity: TypePC
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TypePC.ALL_QUERY,
                query = "SELECT t FROM TypePC t ORDER BY t.label"),
        @NamedQuery(name = TypePC.BY_NAME_QUERY,
                query = "SELECT t FROM TypePC t WHERE t.label = :name"),
        @NamedQuery(
                name = TypePC.TYPE_PC_BY_SUGGEST_NAME_QUERY,
                query = "SELECT t FROM TypePC t WHERE t.label LIKE :suggest ORDER BY t.label") })
public class TypePC extends TypeArticle implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the named query selecting all TypePC
     */
    public static final String ALL_QUERY = "AllTypePC";
    
    /**
     * Name of the named query retrieving the type having the provided name. <br>
     * This query is parameterized:<br>
     * <b>name</b> the name to search
     */
    public static final String BY_NAME_QUERY = "TypePCByName";
    
    /**
     * Name of the named query selecting the TypePC having the name similar to
     * the provided one.<br>
     * This query is parameterized:<br>
     * <b>suggest</b> the name suggestion
     */
    public static final String TYPE_PC_BY_SUGGEST_NAME_QUERY =
            "TypePCBySuggestName";
    
    @OneToOne(mappedBy = "reference.typePC", fetch = FetchType.LAZY)
    private ObsolescenceData obsolescenceData;
    
    /**
     * Default constructor
     */
    public TypePC() {
        super();
    }
    
    @Override
    public String getAllQuery() {
        return TypePC.ALL_QUERY;
    }
    
    @Override
    public String getByName() {
        return TypePC.BY_NAME_QUERY;
    }
    
    /**
     * @return the obsolescenceData
     */
    public ObsolescenceData getObsolescenceData() {
        return obsolescenceData;
    }
    
    /**
     * @param pObsolescenceData
     *            the obsolescenceData to set
     */
    public void setObsolescenceData(ObsolescenceData pObsolescenceData) {
        obsolescenceData = pObsolescenceData;
    }
    
}
