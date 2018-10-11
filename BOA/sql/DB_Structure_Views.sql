-- -------------------------------------
-- --      BOA database creation      --
-- -------------------------------------
-- --       Database structure        --
-- --          for BOA V3.1.1         --
-- -------------------------------------
-- --      3- Views description       --
-- -------------------------------------

-- CREATE OR UPDATE VIEWS IN BOA DATABASE

CREATE OR REPLACE VIEW view_article AS
 SELECT article.ID, airbuspn.IDENTIFIER AS AIRBUSPN, manufacturerpn.IDENTIFIER AS MANUFACTURERPN
  FROM article
 LEFT JOIN airbuspn ON airbuspn.ID = article.AIRBUSPN_ID
 LEFT JOIN manufacturerpn ON manufacturerpn.ID = article.MANUFACTURERPN_ID;
