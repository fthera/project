/*
 * ------------------------------------------------------------------------
 * Class : Cabinet
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeCabinet;
import com.airbus.boa.entity.location.Contains_Cabinet_Rack;
import com.airbus.boa.entity.location.Contains_Inst_Cabinet;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;

/**
 * Entity implementation class for entity: Cabinet
 */
@Entity
@Table(name = Cabinet.TABLE_NAME)
@NamedQueries({
        @NamedQuery(
                name = Cabinet.DESIGNATION_QUERY,
                query = "SELECT c.designation FROM Cabinet c WHERE c.designation= :designation"),
        @NamedQuery(
                name = "CabinetByDesignationOrSN",
                query = "SELECT c FROM Cabinet c WHERE c.airbusSN = :designationOrSN"
                        + " OR c.designation = :designationOrSN OR c.manufacturerSN = :designationOrSN") })
public class Cabinet extends Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "cabinet";
    /** The designation column name */
    public static final String DESIGNATION_COLUMN_NAME = "DESIGNATION";
    
    /**
     * Name of the named query selecting the provided designation.<br>
     * This query is parameterized:<br>
     * <b>designation</b> the designation
     */
    public static final String DESIGNATION_QUERY = "CabinetDesignation";
    
    @Column(name = DESIGNATION_COLUMN_NAME, unique = true)
    private String designation;
    
    @OneToOne(mappedBy = "cabinet")
    private Contains_Inst_Cabinet containerOrmInstallation;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cabinet",
            cascade = { CascadeType.ALL })
    private List<Contains_Cabinet_Rack> containedOrmRacks =
            new ArrayList<Contains_Cabinet_Rack>();
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypeCabinet();
    }
    
    /**
     * Create the relation where the cabinet contains the rack with the provided
     * rack position. Update the cabinet and the rack with this relation.
     * 
     * @param rack
     *            the rack to add
     * @param rackPosition
     *            the rack position
     */
    public void addRack(Rack rack, String rackPosition) {
        
        Contains_Cabinet_Rack relation = new Contains_Cabinet_Rack();
        relation.setRackPosition(rackPosition);
        relation.setRack(rack);
        relation.setCabinet(this);
        containedOrmRacks.add(relation);
        rack.setContainerOrmCabinet(relation);
    }
    
    /**
     * Remove the relation where the cabinet contains the rack from the cabinet
     * and the rack. Erase the relation content.
     * 
     * @param rack
     *            the rack to remove
     * @return the reset relation
     */
    public Contains_Cabinet_Rack removeRack(Rack rack) {
        Contains_Cabinet_Rack relation = rack.getContainerOrmCabinet();
        containedOrmRacks.remove(relation);
        rack.setContainerOrmCabinet(null);
        
        relation.setCabinet(null);
        relation.setRack(null);
        return relation;
    }
    
    @Override
    public String getName() {
        // If designation is empty or null, return the common article name
        return (getDesignation() == null || getDesignation().trim().isEmpty()) ? super
                .getName() : getDesignation();
    }
    
    @Override
    public List<? extends Article> getChildren() {
        List<Article> result = new ArrayList<Article>();
        result.addAll(super.getChildren());
        for (Contains_Cabinet_Rack relation : getContainedOrmRacks()) {
            result.add(relation.getRack());
        }
        
        Collections.sort(result);
        return result;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = super.getContainedItems();
        
        for (Contains_Cabinet_Rack lContainedOrm : containedOrmRacks) {
            lContainedItems.add(lContainedOrm.getRack());
        }
        
        return lContainedItems;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Cabinet;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Cabinet;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Cabinet;
    }
    
    /**
     * @return the designation
     */
    public String getDesignation() {
        return designation;
    }
    
    /**
     * @param designation
     *            the designation to set
     */
    public void setDesignation(String designation) {
        if (designation != null && designation.trim().isEmpty()) {
            designation = null;
        }
        this.designation = designation;
    }
    
    /**
     * @return the containedOrmRacks
     */
    public List<Contains_Cabinet_Rack> getContainedOrmRacks() {
        return containedOrmRacks;
    }
    
    /**
     * @param contains
     *            the containedOrmRacks to set
     */
    public void setContainedOrmRacks(List<Contains_Cabinet_Rack> contains) {
        containedOrmRacks = contains;
    }
    
    /**
     * @return the containerOrmInstallation
     */
    public Contains_Inst_Cabinet getContainerOrmInstallation() {
        return containerOrmInstallation;
    }
    
    /**
     * @param container
     *            the containerOrmInstallation to set
     */
    public void setContainerOrmInstallation(Contains_Inst_Cabinet container) {
        containerOrmInstallation = container;
    }
    
}
