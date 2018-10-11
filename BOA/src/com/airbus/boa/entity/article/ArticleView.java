/*
 * ------------------------------------------------------------------------
 * Class : ArticleView
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.article;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class mapping a view in database for filtering and sorting Article entities
 */
@Entity
@Table(name = "view_article")
public class ArticleView {
    
    /** The Article entity id */
    @Id
    private Long id;
    
    @Column(name = "AIRBUSPN")
    private String airbusPN;
    
    @Column(name = "MANUFACTURERPN")
    private String manufacturerPN;
    
}
