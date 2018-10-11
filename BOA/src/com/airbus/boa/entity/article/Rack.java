/*
 * ------------------------------------------------------------------------
 * Class : Rack
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeRack;
import com.airbus.boa.entity.location.Contains_Cabinet_Rack;
import com.airbus.boa.entity.location.Contains_Rack_Board;
import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.localization.ContainedItem;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation class for Entity: Rack
 */
@Entity
@Table(name = Rack.TABLE_NAME)
@NamedQueries({ @NamedQuery(
        name = "RackByDesignationOrSN",
        query = "SELECT r FROM Rack r WHERE r.airbusSN = :designationOrSN"
                + " OR r.designation = :designationOrSN OR r.manufacturerSN = :designationOrSN") })
public class Rack extends Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "rack";
    /** The designation column name */
    public static final String DESIGNATION_COLUMN_NAME = "DESIGNATION";
    
    @OneToMany(mappedBy = "rack")
    private List<Contains_Rack_Board> containedOrmBoards =
            new ArrayList<Contains_Rack_Board>();
    
    @Column(name = DESIGNATION_COLUMN_NAME)
    private String designation;
    
    @OneToOne(mappedBy = "rack")
    private Contains_Cabinet_Rack containerOrmCabinet;
    
    /**
     * Constructor
     */
    public Rack() {
        super();
    }
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypeRack();
    }
    
    /**
     * @return the withDesignation
     */
    public boolean isWithDesignation() {
        
        return true;
    }
    
    @Override
    public void validate() {
        // validation de Article puis Designation ne peut etre vide.
        
        validateWithDesignation();
        super.validate();
    }
    
    private void validateWithDesignation() {
        if (isWithDesignation()) {
            if (designation == null) {
                throw new ValidationException(MessageBundle.getMessageResource(
                        Constants.FIELD_MUST_NOT_BE_EMPTY,
                        new Object[] { "Designation" }));
            }
        }
    }
    
    @Override
    public String getName() {
        
        // retourné dans l'ordre designation,airbusSN ou manufacturer SN
        return (designation == null) ? super.getName() : designation;
    }
    
    @Override
    public List<? extends Article> getChildren() {
        List<Article> result = new ArrayList<Article>();
        
        result.addAll(super.getContainedArticles());
        for (Contains_Rack_Board relation : getContainedOrmBoards()) {
            result.add(relation.getBoard());
        }
        Collections.sort(result, new Comparator<Article>() {
            
            @Override
            public int compare(Article arg0, Article arg1) {
                String first =
                        (arg0.getAirbusSN() != null) ? arg0.getAirbusSN()
                                : arg0.getManufacturerSN();
                String second =
                        (arg1.getAirbusSN() != null) ? arg1.getAirbusSN()
                                : arg1.getManufacturerSN();
                return first.compareTo(second);
                
            }
        });
        return result;
    }
    
    @Override
    public List<ContainedItem> getContainedItems() {
        
        List<ContainedItem> lContainedItems = super.getContainedItems();
        
        for (Contains_Rack_Board lContainerOrm : containedOrmBoards) {
            lContainedItems.add(lContainerOrm.getBoard());
        }
        
        return lContainedItems;
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.Rack;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Rack;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Rack;
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
        this.designation = designation;
    }
    
    /**
     * @return the containedOrmBoards
     */
    public List<Contains_Rack_Board> getContainedOrmBoards() {
        return containedOrmBoards;
    }
    
    /**
     * @param contains
     *            the containedOrmBoards to set
     */
    public void setContainedOrmBoards(List<Contains_Rack_Board> contains) {
        containedOrmBoards = contains;
    }
    
    /**
     * @return the containerOrmCabinet
     */
    public Contains_Cabinet_Rack getContainerOrmCabinet() {
        return containerOrmCabinet;
    }
    
    /**
     * @param container
     *            the containerOrmCabinet to set
     */
    public void setContainerOrmCabinet(Contains_Cabinet_Rack container) {
        containerOrmCabinet = container;
    }
    
}
