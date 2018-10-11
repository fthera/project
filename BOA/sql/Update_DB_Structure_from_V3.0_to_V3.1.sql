--
-- UPDATE BOA DATABASE FROM VERSION 3.0 TO VERSION 3.1
--

-- UPDATE BOA DATABASE FOR BOA-DM-137

-- ADD THE FRENCHVALUE COLUMN TO airbusstatus TABLE AND UPDATE ITS CONTENT

ALTER TABLE airbusstatus
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE airbusstatus
 JOIN airbusstatus_translation ON airbusstatus_translation.AIRBUSSTATUS_ID = airbusstatus.ID
 JOIN translation ON translation.ID = airbusstatus_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM airbusstatus_translation, translation USING airbusstatus_translation
 JOIN translation ON translation.ID = airbusstatus_translation.TRANSLATION_ID
 JOIN airbusstatus ON airbusstatus.ID = airbusstatus_translation.AIRBUSSTATUS_ID
 WHERE airbusstatus.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO actionobso TABLE AND UPDATE ITS CONTENT

ALTER TABLE actionobso
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE actionobso
 JOIN actionobso_translation ON actionobso_translation.ACTIONOBSO_ID = actionobso.ID
 JOIN translation ON translation.ID = actionobso_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM actionobso_translation, translation USING actionobso_translation
 JOIN translation ON translation.ID = actionobso_translation.TRANSLATION_ID
 JOIN actionobso ON actionobso.ID = actionobso_translation.ACTIONOBSO_ID
 WHERE actionobso.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO businessallocationpc TABLE AND UPDATE ITS CONTENT

ALTER TABLE businessallocationpc
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE businessallocationpc
 JOIN businessallocationpc_translation ON businessallocationpc_translation.BUSINESSALLOCATIONPC_ID = businessallocationpc.ID
 JOIN translation ON translation.ID = businessallocationpc_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM businessallocationpc_translation, translation USING businessallocationpc_translation
 JOIN translation ON translation.ID = businessallocationpc_translation.TRANSLATION_ID
 JOIN businessallocationpc ON businessallocationpc.ID = businessallocationpc_translation.BUSINESSALLOCATIONPC_ID
 WHERE businessallocationpc.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO businessusagepc TABLE AND UPDATE ITS CONTENT

ALTER TABLE businessusagepc
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE businessusagepc
 JOIN businessusagepc_translation ON businessusagepc_translation.BUSINESSUSAGEPC_ID = businessusagepc.ID
 JOIN translation ON translation.ID = businessusagepc_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM businessusagepc_translation, translation USING businessusagepc_translation
 JOIN translation ON translation.ID = businessusagepc_translation.TRANSLATION_ID
 JOIN businessusagepc ON businessusagepc.ID = businessusagepc_translation.BUSINESSUSAGEPC_ID
 WHERE businessusagepc.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO consultperiod TABLE AND UPDATE ITS CONTENT

ALTER TABLE consultperiod
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE consultperiod
 JOIN consultperiod_translation ON consultperiod_translation.CONSULTPERIOD_ID = consultperiod.ID
 JOIN translation ON translation.ID = consultperiod_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM consultperiod_translation, translation USING consultperiod_translation
 JOIN translation ON translation.ID = consultperiod_translation.TRANSLATION_ID
 JOIN consultperiod ON consultperiod.ID = consultperiod_translation.CONSULTPERIOD_ID
 WHERE consultperiod.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO externalentitytype TABLE AND UPDATE ITS CONTENT

ALTER TABLE externalentitytype
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE externalentitytype
 JOIN externalentitytype_translation ON externalentitytype_translation.EXTERNALENTITYTYPE_ID = externalentitytype.ID
 JOIN translation ON translation.ID = externalentitytype_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM externalentitytype_translation, translation USING externalentitytype_translation
 JOIN translation ON translation.ID = externalentitytype_translation.TRANSLATION_ID
 JOIN externalentitytype ON externalentitytype.ID = externalentitytype_translation.EXTERNALENTITYTYPE_ID
 WHERE externalentitytype.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO manufacturerstatus TABLE AND UPDATE ITS CONTENT

ALTER TABLE manufacturerstatus
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE manufacturerstatus
 JOIN manufacturerstatus_translation ON manufacturerstatus_translation.MANUFACTURERSTATUS_ID = manufacturerstatus.ID
 JOIN translation ON translation.ID = manufacturerstatus_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM manufacturerstatus_translation, translation USING manufacturerstatus_translation
 JOIN translation ON translation.ID = manufacturerstatus_translation.TRANSLATION_ID
 JOIN manufacturerstatus ON manufacturerstatus.ID = manufacturerstatus_translation.MANUFACTURERSTATUS_ID
 WHERE manufacturerstatus.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO network TABLE AND UPDATE ITS CONTENT

ALTER TABLE network
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE network
 JOIN network_translation ON network_translation.NETWORK_ID = network.ID
 JOIN translation ON translation.ID = network_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM network_translation, translation USING network_translation
 JOIN translation ON translation.ID = network_translation.TRANSLATION_ID
 JOIN network ON network.ID = network_translation.NETWORK_ID
 WHERE network.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO producttypepc TABLE AND UPDATE ITS CONTENT

ALTER TABLE producttypepc
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE producttypepc
 JOIN producttypepc_translation ON producttypepc_translation.PRODUCTTYPEPC_ID = producttypepc.ID
 JOIN translation ON translation.ID = producttypepc_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM producttypepc_translation, translation USING producttypepc_translation
 JOIN translation ON translation.ID = producttypepc_translation.TRANSLATION_ID
 JOIN producttypepc ON producttypepc.ID = producttypepc_translation.PRODUCTTYPEPC_ID
 WHERE producttypepc.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- ADD THE FRENCHVALUE COLUMN TO strategy TABLE AND UPDATE ITS CONTENT

ALTER TABLE strategy
 ADD `FRENCHVALUE` varchar(60) DEFAULT NULL AFTER `DEFAULTVALUE`;

UPDATE strategy
 JOIN strategy_translation ON strategy_translation.STRATEGY_ID = strategy.ID
 JOIN translation ON translation.ID = strategy_translation.TRANSLATION_ID
 SET FRENCHVALUE = translation.VALUE
 WHERE translation.LANGAGECODE = "fra";

DELETE FROM strategy_translation, translation USING strategy_translation
 JOIN translation ON translation.ID = strategy_translation.TRANSLATION_ID
 JOIN strategy ON strategy.ID = strategy_translation.STRATEGY_ID
 WHERE strategy.FRENCHVALUE = translation.VALUE
 AND translation.LANGAGECODE = "fra";

-- DROP NO MORE USED TABLES

DROP TABLE airbusstatus_translation,
 actionobso_translation,
 businessallocationpc_translation,
 businessusagepc_translation,
 consultperiod_translation,
 externalentitytype_translation,
 manufacturerstatus_translation,
 network_translation,
 producttypepc_translation,
 strategy_translation,
 translation;

-- CREATE VIEWS FOR RETRIEVING COMPUTED FIELDS FROM DATABASE

DELIMITER //

CREATE FUNCTION getLocationID(pID bigint, pType varchar (50)) RETURNS bigint
COMMENT 'Return location ID of the provided item id'
BEGIN
-- If the location is not defined, it is computed using the parent if the inherited value is True
-- pID:    the ID of an article, an installation, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of a place or NULL
 DECLARE lType           varchar(50) DEFAULT pType;
 DECLARE lPlaceID        bigint DEFAULT NULL;
 DECLARE lContainerID    bigint DEFAULT NULL;
 DECLARE lInherited      tinyint DEFAULT 0;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- if the provided type is NULL, initialize it
 IF lType IS NULL THEN
  SET lType = getType(pID);
  -- if the computed type is still NULL, return NULL
  IF lType IS NULL THEN
   RETURN NULL;
  END IF;
 END IF;
 SET lPlaceID = getDirectLocationID(pID,lType);
 IF lPlaceID IS NOT NULL THEN
  -- Location is found, return it
  RETURN lPlaceID;
 END IF;
 SET lContainerID = pID;
 locationLoop: LOOP
  -- Location has not been found
  SET lInherited = getInheritedLocation(lContainerID,NULL);
  IF lInherited IS NULL THEN
   -- The inherited value is not defined, by default False
   RETURN NULL;
  ELSE
   IF lInherited = 0 THEN
    -- The location is not inherited
    RETURN NULL;
   ELSE
    -- The location is inherited, search the parent
    SET lContainerID = getContainerID(lContainerID,NULL);
    IF lContainerID IS NULL THEN
     -- No parent is defined, no location can be found
     RETURN NULL;
    ELSE
     -- The parent is defined, retrieve its location
     SET lPlaceID = getDirectLocationID(lContainerID,NULL);
     IF lPlaceID IS NOT NULL THEN
      RETURN lPlaceID;
     END IF;
    END IF;
   END IF;
  END IF;
 END LOOP locationLoop;
 RETURN NULL;
END
//

CREATE FUNCTION getType(pID bigint) RETURNS varchar(50)
COMMENT 'Return the type of the provided item'
BEGIN
-- pID:    the ID of an article, an installation, a place, a building, an external entity, a tool (can be NULL)
-- RETURN: the found type label or NULL
 DECLARE lDtype varchar(50) DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 SELECT DTYPE INTO lDtype FROM article WHERE ID = pID;
 IF lDtype IS NOT NULL THEN
  RETURN lDtype;
 END IF;
 IF EXISTS(SELECT NAME FROM installation WHERE ID = pID) THEN
  RETURN "Installation";
 END IF;
 IF EXISTS(SELECT * FROM place WHERE ID = pID) THEN
  RETURN "Place";
 END IF;
 IF EXISTS(SELECT * FROM building WHERE ID = pID) THEN
  RETURN "Building";
 END IF;
 IF EXISTS(SELECT * FROM externalentity WHERE ID = pID) THEN
  RETURN "ExternalEntity";
 END IF;
 IF EXISTS(SELECT * FROM tool WHERE ID = pID) THEN
  RETURN "Tool";
 END IF;
 RETURN NULL;
END
//

CREATE FUNCTION getDirectLocationID(pID bigint, pType varchar (50)) RETURNS bigint
COMMENT 'Return location ID of the provided item id'
BEGIN
-- pID:    the ID of an article, an installation, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of a place or NULL
 DECLARE lType           varchar(50) DEFAULT pType;
 DECLARE lPlaceID        bigint DEFAULT NULL;
 DECLARE lContainerID    bigint DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- if the provided type is NULL, initialize it
 IF lType IS NULL THEN
  SET lType = getType(pID);
  -- if the computed type is still NULL, return NULL
  IF lType IS NULL THEN
   RETURN NULL;
  END IF;
 END IF;
 -- Common case for articles, when location has not yet been found
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- Location of an Article into a Place
  SELECT PLACE_ID INTO lPlaceID FROM location WHERE ARTICLE_ID = pID;
  RETURN lPlaceID;
 END IF;
 IF lType = "Installation" THEN
  -- Location of an Installation into a Place
  SELECT LABO_PLACE_ID INTO lPlaceID FROM installation WHERE ID = pID;
  RETURN lPlaceID;
 END IF;
 IF lType = "Tool" THEN
  -- Location of a Tool into a Place
  SELECT PLACE_ID INTO lPlaceID FROM location WHERE TOOL_ID = pID;
  RETURN lPlaceID;
 END IF;
 RETURN NULL;
END
//

CREATE FUNCTION getInheritedLocation(pID bigint, pType varchar (50)) RETURNS bigint
COMMENT 'Return inherited value for location of the provided item id'
BEGIN
-- pID:    the ID of an article, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the inherited value or NULL
 DECLARE lType       varchar(50) DEFAULT pType;
 DECLARE lInherited  tinyint DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- if the provided type is NULL, initialize it
 IF lType IS NULL THEN
  SET lType = getType(pID);
  -- if the computed type is still NULL, return NULL
  IF lType IS NULL THEN
   RETURN NULL;
  END IF;
 END IF;
 -- Common case for articles
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- Location of an Article into a Place
  SELECT INHERITED INTO lInherited FROM location WHERE ARTICLE_ID = pID;
  RETURN lInherited;
 END IF;
 IF lType = "Tool" THEN
  -- Location of a Tool into a Place
  SELECT INHERITED INTO lInherited FROM location WHERE TOOL_ID = pID;
  RETURN lInherited;
 END IF;
 RETURN NULL;
END
//

CREATE FUNCTION getContainerID(pID bigint, pType varchar (50)) RETURNS bigint
COMMENT 'Return container ID of the provided item id'
BEGIN
-- pID:    the ID of an article, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of an article, an installation, a tool or NULL
 DECLARE lType           varchar(50) DEFAULT pType;
 DECLARE lInstallationID bigint DEFAULT NULL;
 DECLARE lArticleID      bigint DEFAULT NULL;
 DECLARE lCabinetID      bigint DEFAULT NULL;
 DECLARE lRackID         bigint DEFAULT NULL;
 DECLARE lPCID           bigint DEFAULT NULL;
 DECLARE lToolID         bigint DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- if the provided type is NULL, initialize it
 IF lType IS NULL THEN
  SET lType = getType(pID);
  -- if the computed type is still NULL, return NULL
  IF lType IS NULL THEN
   RETURN NULL;
  END IF;
 END IF;
 IF lType = "Cabinet" THEN
  -- Particular case of parent relation of a Cabinet into an Installation
  SELECT INSTALLATION_ID INTO lInstallationID FROM container WHERE INSTALLATION_ID IS NOT NULL AND CABINET_ID = pID;
  IF lInstallationID IS NOT NULL THEN
   RETURN lInstallationID;
  END IF;
 ELSE
  IF lType = "Rack" THEN
   -- Particular case of parent relation of a Rack into a Cabinet
   SELECT CABINET_ID INTO lCabinetID FROM container WHERE CABINET_ID IS NOT NULL AND RACK_ID = pID;
   IF lCabinetID IS NOT NULL THEN
    RETURN lCabinetID;
   END IF;
  ELSE
   IF lType = "Switch" THEN
    -- Particular case of parent relation of a Switch into a Cabinet
    SELECT CABINET_ID INTO lCabinetID FROM container WHERE CABINET_ID IS NOT NULL AND RACK_ID = pID;
    IF lCabinetID IS NOT NULL THEN
     RETURN lCabinetID;
    END IF;
   ELSE
    IF lType = "Board" THEN
     -- Particular case of parent relation of a Board into a Rack
     SELECT RACK_ID INTO lRackID FROM container WHERE RACK_ID IS NOT NULL AND BOARD_ID = pID;
     IF lRackID IS NOT NULL THEN
      RETURN lRackID;
     END IF;
     -- Particular case of parent relation of a Board into a PC
     SELECT PC_ID INTO lPCID FROM container WHERE PC_ID IS NOT NULL AND BOARD_ID = pID;
     IF lPCID IS NOT NULL THEN
      RETURN lPCID;
     END IF;
    END IF;
   END IF;
  END IF;
 END IF;
 -- Common case for articles, when container has not yet been found
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- Case of parent relation of an Article into an installation or another Article
  SELECT USEDBY_INSTALLATION_ID, USEDBY_ARTICLE_ID
   INTO lInstallationID, lArticleID FROM article WHERE ID = pID;
  IF lInstallationID IS NOT NULL THEN
   RETURN lInstallationID;
  END IF;
  IF lArticleID IS NOT NULL THEN
   RETURN lArticleID;
  END IF;
  -- Case of parent relation of an Article into a Tool
  SELECT TOOL_ID INTO lToolID FROM container WHERE TOOL_ID IS NOT NULL AND ARTICLE_ID = pID;
  IF lToolID IS NOT NULL THEN
   RETURN lToolID;
  END IF;
 END IF;
 IF lType = "Tool" THEN
  -- Particular case of parent relation of a Tool into an Installation
  SELECT INSTALLATION_ID INTO lInstallationID FROM tool WHERE tool.ID = pID;
  RETURN lInstallationID;
 END IF;
 RETURN NULL;
END
//

DELIMITER ;

CREATE VIEW view_article AS
 SELECT article.ID, airbuspn.IDENTIFIER AS AIRBUSPN, manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
  getLocationID(article.ID, article.DTYPE) AS C_PLACE_ID FROM article
 LEFT JOIN airbuspn ON airbuspn.ID = article.AIRBUSPN_ID
 LEFT JOIN manufacturerpn ON manufacturerpn.ID = article.MANUFACTURERPN_ID;


-- UPDATE BOA DATABASE FOR BOA-DM-157

-- UPDATE software TABLE STRUCTURE

ALTER TABLE software
 DROP KEY UK_SOFTWARE_NAME_VERSIONLABEL,
 CHANGE NAME NAME VARCHAR(50) NOT NULL,
 CHANGE VERSIONLABEL DISTRIBUTION VARCHAR(50) NOT NULL,
 ADD KERNEL VARCHAR(50) NOT NULL,
 ADD UNIQUE KEY UK_SOFTWARE_NAME_DISTRIBUTION_KERNEL (NAME,DISTRIBUTION,KERNEL);

-- ADD THE DEFAULT OPERATING SYSTEM COLUMN TO THE pc TABLE AND UPDATE ITS VALUE

ALTER TABLE pc
 ADD DEFAULTOS_SOFTWARE_ID bigint(20) DEFAULT NULL,
 ADD KEY `K_PC_DEFAULTOS_SOFTWARE_ID` (`DEFAULTOS_SOFTWARE_ID`),
 ADD CONSTRAINT `FK_PC_DEFAULTOS_SOFWTARE_ID` FOREIGN KEY (`DEFAULTOS_SOFTWARE_ID`) REFERENCES `software` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE TEMPORARY TABLE software_article_default
   SELECT article.ID AS ARTICLE_ID, software.ID AS SOFTWARE_ID FROM article
    JOIN software_article ON software_article.ARTICLE_ID = article.ID
    JOIN software ON software.ID = software_article.SOFTWARE_ID
    WHERE software.OPERATINGSYSTEM = 1
	AND article.DTYPE = "PC"
    GROUP BY article.ID
    HAVING COUNT(software.ID) = 1;

UPDATE pc
 JOIN software_article_default ON software_article_default.ARTICLE_ID = pc.ID
 SET DEFAULTOS_SOFTWARE_ID = software_article_default.SOFTWARE_ID;


-- UPDATE BOA DATABASE FOR BOA-DM-47

-- CREATE document TABLE

CREATE TABLE `document` (
  `ID` bigint(20) NOT NULL,
  `ARTICLE_ID` bigint(20) DEFAULT NULL,
  `INSTALLATION_ID` bigint(20) DEFAULT NULL,
  `TOOL_ID` bigint(20) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `FILEBLOB` longblob NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `UPLOADDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `K_DOCUMENT_ARTICLE_ID` (`ARTICLE_ID`),
  KEY `K_DOCUMENT_INSTALLATION_ID` (`INSTALLATION_ID`),
  KEY `K_DOCUMENT_TOOL_ID` (`TOOL_ID`),
  CONSTRAINT `FK_DOCUMENT_ARTICLE_ID` FOREIGN KEY (`ARTICLE_ID`) REFERENCES `article` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DOCUMENT_INSTALLATION_ID` FOREIGN KEY (`INSTALLATION_ID`) REFERENCES `installation` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DOCUMENT_TOOL_ID` FOREIGN KEY (`TOOL_ID`) REFERENCES `tool` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- UPDATE BOA DATABASE FOR BOA-DM-158

-- MOVE MANUFACTURER FROM obsolescencedata TABLE TO typearticle TABLE

ALTER TABLE typearticle
 ADD `MANUFACTURER` varchar(50) DEFAULT NULL,
 DROP NBSLOTS;

CREATE TEMPORARY TABLE typearticle_manufacturer
   SELECT typearticle.ID, obsolescencedata.MANUFACTURER, COUNT(DISTINCT obsolescencedata.MANUFACTURER) AS NB_MAN FROM obsolescencedata
    JOIN typearticle ON typearticle.ID = obsolescencedata.TYPEARTICLE_ID
    WHERE obsolescencedata.MANUFACTURER != ""
    GROUP BY typearticle.ID
    HAVING NB_MAN = 1
  UNION
   SELECT typearticle.ID, obsolescencedata.MANUFACTURER, COUNT(DISTINCT obsolescencedata.MANUFACTURER) AS NB_MAN FROM obsolescencedata
    JOIN typearticle ON typearticle.ID = obsolescencedata.TYPEPC_TYPEARTICLE_ID
    WHERE obsolescencedata.MANUFACTURER != ""
    GROUP BY typearticle.ID
    HAVING NB_MAN = 1;

UPDATE typearticle
 SET MANUFACTURER = (SELECT typearticle_manufacturer.MANUFACTURER FROM typearticle_manufacturer WHERE typearticle_manufacturer.ID = typearticle.ID);

UPDATE obsolescencedata
 JOIN typearticle ON typearticle.ID = obsolescencedata.TYPEARTICLE_ID OR typearticle.ID = obsolescencedata.TYPEPC_TYPEARTICLE_ID
 SET obsolescencedata.MANUFACTURER = NULL
 WHERE typearticle.MANUFACTURER = obsolescencedata.MANUFACTURER;

-- MOVE MANUFACTURER FROM obsolescencedata TABLE TO software TABLE

ALTER TABLE software
 ADD `MANUFACTURER` varchar(50) DEFAULT NULL;

CREATE TEMPORARY TABLE software_manufacturer
   SELECT software.ID, obsolescencedata.MANUFACTURER, COUNT(DISTINCT obsolescencedata.MANUFACTURER) AS NB_MAN FROM obsolescencedata
    JOIN software ON software.ID = obsolescencedata.SOFTWARE_ID
    WHERE obsolescencedata.MANUFACTURER != ""
    GROUP BY software.ID
    HAVING NB_MAN = 1;

UPDATE software
 SET MANUFACTURER = (SELECT software_manufacturer.MANUFACTURER FROM software_manufacturer WHERE software_manufacturer.ID = software.ID);

UPDATE obsolescencedata
 JOIN software ON software.ID = obsolescencedata.SOFTWARE_ID
 SET obsolescencedata.MANUFACTURER = NULL
 WHERE software.MANUFACTURER = obsolescencedata.MANUFACTURER;

CREATE VIEW view_obso_manufacturer AS
  SELECT MANUFACTURER, TYPEARTICLE_ID, TYPEPC_TYPEARTICLE_ID, SOFTWARE_ID FROM obsolescencedata WHERE MANUFACTURER IS NOT NULL AND MANUFACTURER != "";

-- => Treat manually remaining MANUFACTURER values from obsolescencedata table before removing
DROP VIEW view_obso_manufacturer;

-- DELETE MANUFACTURER COLUMN FROM obsolescencedata TABLE

ALTER TABLE obsolescencedata
 DROP MANUFACTURER;

-- CREATE domain TABLE

CREATE TABLE `domain` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `VALUE` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_DOMAIN_VALUE` (`VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Fill the domain table
INSERT INTO domain (VALUE) VALUES ('avsim.fr'), ('bdisc.fr');

-- IN pc TABLE, ADD COLUMNS

ALTER TABLE pc
 ADD LASTAUTOUPDATEDATE datetime DEFAULT NULL AFTER AUTOUPDATE,
 ADD LASTAUTOUPDATETYPE varchar(10) DEFAULT NULL AFTER LASTAUTOUPDATEDATE,
 ADD AUTODATAVALIDITYDATE datetime DEFAULT NULL AFTER LASTAUTOUPDATETYPE,
 ADD DOMAIN_ID bigint(20) NULL DEFAULT NULL,
 ADD PLATFORM varchar(20) DEFAULT NULL,
 ADD CPUTYPE varchar(50) DEFAULT NULL,
 ADD CPUMAXSPEED varchar(15) DEFAULT NULL,
 ADD CPUSOCKETS int DEFAULT NULL,
 ADD CPUCOREPERSOCKET int DEFAULT NULL,
 ADD CPULOGICALCPUPERSOCKET int DEFAULT NULL,
 ADD CPUTOTALLOGICALCPUS int DEFAULT NULL,
 ADD CPUHYPERTHREADING varchar(15) DEFAULT NULL,
 ADD TOTALMEMORYSIZE varchar(30) DEFAULT NULL,
 ADD KEY K_PC_DOMAIN_ID (DOMAIN_ID),
 ADD CONSTRAINT FK_PC_DOMAIN_ID FOREIGN KEY (DOMAIN_ID) REFERENCES domain (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- CREATE memoryslot TABLE

CREATE TABLE `memoryslot` (
  `ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `NAME` varchar(15) NOT NULL,
  `USED` tinyint(1) DEFAULT '0',
  `MEMORYSIZE` varchar(30) NULL DEFAULT NULL,
  `MEMORYTYPE` varchar(70) NULL DEFAULT NULL,
  `PC_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_MEMORYSLOT_PC_ID_NAME` (`PC_ID`,`NAME`),
  CONSTRAINT `FK_MEMORYSLOT_PC_ID` FOREIGN KEY (`PC_ID`) REFERENCES `pc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- UPDATE BOA DATABASE FOR BOA-DM-153

-- ADD THE OWNER AND OWNER SIGLUM COLUMNS TO pc TABLE

ALTER TABLE pc
 ADD OWNER varchar(100) DEFAULT NULL AFTER USER_ID,
 ADD OWNERSIGLUM varchar(20) DEFAULT NULL AFTER OWNER;

-- ADD THE OWNER AND OWNER SIGLUM COLUMNS TO demand TABLE

ALTER TABLE demand
 ADD OWNER varchar(100) DEFAULT NULL AFTER USER_ID,
 ADD OWNERSIGLUM varchar(20) DEFAULT NULL AFTER OWNER;

