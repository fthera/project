/*
 * ------------------------------------------------------------------------
 * Class : Various
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.article.type.TypeVarious;
import com.airbus.boa.localization.ContainedType;
import com.airbus.boa.localization.ContainerType;
import com.airbus.boa.localization.LocatedType;

/**
 * Entity implementation class for Entity: Various
 */
@Entity
@Table(name = "various")
public class Various extends Article implements Serializable {
    
    private static final long serialVersionUID = 5621307793201517331L;
    
    @Override
    public TypeArticle createTypeArticle() {
        return new TypeVarious();
    }
    
    @Override
    public ContainerType getContainerType() {
        return null;
    }
    
    @Override
    public ContainedType getContainedType() {
        return ContainedType.Various;
    }
    
    @Override
    public LocatedType getLocatedType() {
        return LocatedType.Various;
    }
    
}
