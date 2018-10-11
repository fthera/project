/*
 * ------------------------------------------------------------------------
 * Class : ArticleViewMat
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.airbus.boa.entity.location.Place;

/**
 * Class mapping a materialized view in database for filtering and sorting
 * Article entities
 */
@Entity
@Table(name = "mat_view_article")
public class ArticleViewMat {
    
    /** The Article entity id */
    @Id
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "C_PLACE_ID")
    private Place computedLocationPlace;
    
}
