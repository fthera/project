--
-- UPDATE BOA DATABASE FROM VERSION 2.5 TO VERSION 2.6
--

-- UPDATE BOA DATABASE FOR BOA-DM-121

-- CHANGE MAXIMUM SIZE OF EMAIL TO 100 CHARACTERS

ALTER TABLE user
 CHANGE EMAIL EMAIL VARCHAR(100) NULL DEFAULT NULL;


-- UPDATE BOA DATABASE FOR BOA-DM-94

-- CHANGE THE DEFAULT VALUES OF ATTRIBUTE VALUE LIST FROM FRENCH TO ENGLISH

-- Add a temporary column to the tables for retrieving the English value

ALTER TABLE actionobso
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE consultperiod
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE airbusstatus
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE manufacturerstatus
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE strategy
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE businessallocationpc
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE businessusagepc
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE producttypepc
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;
ALTER TABLE network
 ADD ENGLISHVALUE varchar(60) DEFAULT NULL;

-- Copy the English value from the translation table into the temporary column

UPDATE actionobso
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN actionobso_translation ON actionobso_translation.TRANSLATION_ID = translation.ID
                     WHERE actionobso.ID = actionobso_translation.ACTIONOBSO_ID);
UPDATE consultperiod
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN consultperiod_translation ON consultperiod_translation.TRANSLATION_ID = translation.ID
                     WHERE consultperiod.ID = consultperiod_translation.CONSULTPERIOD_ID);
UPDATE airbusstatus
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN airbusstatus_translation ON airbusstatus_translation.TRANSLATION_ID = translation.ID
                     WHERE airbusstatus.ID = airbusstatus_translation.AIRBUSSTATUS_ID);
UPDATE manufacturerstatus
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN manufacturerstatus_translation ON manufacturerstatus_translation.TRANSLATION_ID = translation.ID
                     WHERE manufacturerstatus.ID = manufacturerstatus_translation.MANUFACTURERSTATUS_ID);
UPDATE strategy
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN strategy_translation ON strategy_translation.TRANSLATION_ID = translation.ID
                     WHERE strategy.ID = strategy_translation.STRATEGY_ID);
UPDATE businessallocationpc
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN businessallocationpc_translation ON businessallocationpc_translation.TRANSLATION_ID = translation.ID
                     WHERE businessallocationpc.ID = businessallocationpc_translation.BUSINESSALLOCATIONPC_ID);
UPDATE businessusagepc
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN businessusagepc_translation ON businessusagepc_translation.TRANSLATION_ID = translation.ID
                     WHERE businessusagepc.ID = businessusagepc_translation.BUSINESSUSAGEPC_ID);
UPDATE producttypepc
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN producttypepc_translation ON producttypepc_translation.TRANSLATION_ID = translation.ID
                     WHERE producttypepc.ID = producttypepc_translation.PRODUCTTYPEPC_ID);
UPDATE network
 SET ENGLISHVALUE = (SELECT translation.VALUE FROM translation
                     JOIN network_translation ON network_translation.TRANSLATION_ID = translation.ID
                     WHERE network.ID = network_translation.NETWORK_ID);

-- Replace the English value into the translation table by the French value

UPDATE translation
 SET VALUE = (SELECT actionobso.DEFAULTVALUE FROM actionobso
              JOIN actionobso_translation ON actionobso_translation.ACTIONOBSO_ID = actionobso.ID
              WHERE translation.ID = actionobso_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM actionobso_translation);
UPDATE translation
 SET VALUE = (SELECT consultperiod.DEFAULTVALUE FROM consultperiod
              JOIN consultperiod_translation ON consultperiod_translation.CONSULTPERIOD_ID = consultperiod.ID
              WHERE translation.ID = consultperiod_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM consultperiod_translation);
UPDATE translation
 SET VALUE = (SELECT airbusstatus.DEFAULTVALUE FROM airbusstatus
              JOIN airbusstatus_translation ON airbusstatus_translation.AIRBUSSTATUS_ID = airbusstatus.ID
              WHERE translation.ID = airbusstatus_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM airbusstatus_translation);
UPDATE translation
 SET VALUE = (SELECT manufacturerstatus.DEFAULTVALUE FROM manufacturerstatus
              JOIN manufacturerstatus_translation ON manufacturerstatus_translation.MANUFACTURERSTATUS_ID = manufacturerstatus.ID
              WHERE translation.ID = manufacturerstatus_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM manufacturerstatus_translation);
UPDATE translation
 SET VALUE = (SELECT strategy.DEFAULTVALUE FROM strategy
              JOIN strategy_translation ON strategy_translation.STRATEGY_ID = strategy.ID
              WHERE translation.ID = strategy_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM strategy_translation);
UPDATE translation
 SET VALUE = (SELECT businessallocationpc.DEFAULTVALUE FROM businessallocationpc
              JOIN businessallocationpc_translation ON businessallocationpc_translation.BUSINESSALLOCATIONPC_ID = businessallocationpc.ID
              WHERE translation.ID = businessallocationpc_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM businessallocationpc_translation);
UPDATE translation
 SET VALUE = (SELECT businessusagepc.DEFAULTVALUE FROM businessusagepc
              JOIN businessusagepc_translation ON businessusagepc_translation.BUSINESSUSAGEPC_ID = businessusagepc.ID
              WHERE translation.ID = businessusagepc_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM businessusagepc_translation);
UPDATE translation
 SET VALUE = (SELECT producttypepc.DEFAULTVALUE FROM producttypepc
              JOIN producttypepc_translation ON producttypepc_translation.PRODUCTTYPEPC_ID = producttypepc.ID
              WHERE translation.ID = producttypepc_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM producttypepc_translation);
UPDATE translation
 SET VALUE = (SELECT network.DEFAULTVALUE FROM network
              JOIN network_translation ON network_translation.NETWORK_ID = network.ID
              WHERE translation.ID = network_translation.TRANSLATION_ID),
     LANGAGECODE = "fra"
 WHERE translation.ID IN (SELECT TRANSLATION_ID FROM network_translation);

-- Copy the English value into the default value column

UPDATE actionobso
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE consultperiod
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE airbusstatus
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE manufacturerstatus
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE strategy
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE businessallocationpc
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE businessusagepc
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE producttypepc
 SET DEFAULTVALUE = ENGLISHVALUE;
UPDATE network
 SET DEFAULTVALUE = ENGLISHVALUE;

-- Remove the temporary column from the tables

ALTER TABLE actionobso
 DROP ENGLISHVALUE;
ALTER TABLE consultperiod
 DROP ENGLISHVALUE;
ALTER TABLE airbusstatus
 DROP ENGLISHVALUE;
ALTER TABLE manufacturerstatus
 DROP ENGLISHVALUE;
ALTER TABLE strategy
 DROP ENGLISHVALUE;
ALTER TABLE businessallocationpc
 DROP ENGLISHVALUE;
ALTER TABLE businessusagepc
 DROP ENGLISHVALUE;
ALTER TABLE producttypepc
 DROP ENGLISHVALUE;
ALTER TABLE network
 DROP ENGLISHVALUE;


-- UPDATE BOA DATABASE FOR BOA-DM-118

-- ADD THE ACTIVESTOCKCONTROLDATE TO THE board TABLE

ALTER TABLE board
 ADD ACTIVESTOCKCONTROLDATE datetime DEFAULT NULL;


-- UPDATE BOA DATABASE FOR BOA-DM-126

-- IN installation TABLE, ADD THE USER_ID COLUMN REFERENCING A USER FOR REPLACING THE PERSONINCHARGE COLUMN (TEXT FIELD)

ALTER TABLE installation
 ADD USER_ID BIGINT NULL DEFAULT NULL AFTER PERSONINCHARGE,
 ADD INDEX K_INSTALLATION_USER_ID (USER_ID),
 ADD CONSTRAINT FK_INSTALLATION_USER_ID FOREIGN KEY (USER_ID) REFERENCES user (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Update the USER_ID when the login is present in PERSONINCHARGE column
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE LOGIN LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;

-- Update the USER_ID when the firstname (initial or complete) and lastname are present in PERSONINCHARGE column
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(FIRSTNAME," ",LASTNAME) LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LASTNAME," ",FIRSTNAME) LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1),". ",LASTNAME) LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1),".",LASTNAME) LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1)," ",LASTNAME) LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;

-- Update the USER_ID when the lastname is present in PERSONINCHARGE column
UPDATE installation SET USER_ID = (SELECT user.ID FROM user WHERE LASTNAME LIKE installation.PERSONINCHARGE) WHERE USER_ID IS NULL;

-- Clean the PERSONINCHARGE column if USER_ID is filled
UPDATE installation SET PERSONINCHARGE = NULL WHERE USER_ID IS NOT NULL;

-- => Treat manually the fill of the USER_ID column

-- Remove temporarily the foreign key constraint
ALTER TABLE installation
 DROP FOREIGN KEY FK_INSTALLATION_USER_ID;
-- REMOVE THE PERSONINCHARGE COLUMN AND CHANGE THE NEW COLUMN TO NOT NULL IN installation TABLE
-- Restore the foreign key constraint on the USER_ID column
ALTER TABLE installation
 DROP COLUMN PERSONINCHARGE,
 CHANGE USER_ID USER_ID BIGINT NOT NULL,
 ADD CONSTRAINT FK_INSTALLATION_USER_ID FOREIGN KEY (USER_ID) REFERENCES user (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- UPDATE BOA DATABASE FOR BOA-DM-127

-- ADD BUSINESSSIGLUM COLUMN TO installation TABLE

ALTER TABLE installation
 ADD BUSINESSSIGLUM varchar(30) NOT NULL;

-- CREATE aircraftprogram TABLE

CREATE TABLE `aircraftprogram` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `VALUE` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_AIRCRAFTPROGRAM_VALUE` (`VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- IN installation TABLE, ADD THE AIRCRAFTPROGRAM_ID COLUMN

ALTER TABLE installation
 ADD AIRCRAFTPROGRAM_ID BIGINT NULL DEFAULT NULL AFTER AIRCRAFTPROGRAM,
 ADD INDEX K_INSTALLATION_AIRCRAFTPROGRAM_ID (AIRCRAFTPROGRAM_ID),
 ADD CONSTRAINT FK_INSTALLATION_AIRCRAFTPROGRAM_ID FOREIGN KEY (AIRCRAFTPROGRAM_ID) REFERENCES aircraftprogram (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Fill the aircraftprogram table with 'MULTI' and all existing values in installation.AIRCRAFTPROGRAM column
INSERT INTO aircraftprogram (VALUE) VALUES ('MULTI');
INSERT INTO aircraftprogram (VALUE) SELECT DISTINCT TRIM(installation.AIRCRAFTPROGRAM) FROM installation WHERE TRIM(installation.AIRCRAFTPROGRAM) NOT LIKE "";

-- Update installation table AIRCRAFTPROGRAM_ID column with the corresponding ID from aircraftprogram table
UPDATE installation
 SET AIRCRAFTPROGRAM_ID = (SELECT aircraftprogram.ID FROM aircraftprogram WHERE aircraftprogram.VALUE = TRIM(installation.AIRCRAFTPROGRAM));

-- Clean the AIRCRAFTPROGRAM column if AIRCRAFTPROGRAM_ID is filled
UPDATE installation SET AIRCRAFTPROGRAM = NULL WHERE AIRCRAFTPROGRAM_ID IS NOT NULL;

-- => Treat manually the fill of the AIRCRAFTPROGRAM_ID column

-- Remove temporarily the forign key constraint
ALTER TABLE installation
 DROP FOREIGN KEY FK_INSTALLATION_AIRCRAFTPROGRAM_ID;
-- REMOVE THE AIRCRAFTPROGRAM COLUMN AND CHANGE THE NEW COLUMN TO NOT NULL IN installation TABLE
-- Restore the foreign ky constraint on the AIRCRAFTPROGRAM_ID column
ALTER TABLE installation
 DROP COLUMN AIRCRAFTPROGRAM,
 CHANGE AIRCRAFTPROGRAM_ID AIRCRAFTPROGRAM_ID BIGINT NOT NULL,
 ADD CONSTRAINT FK_INSTALLATION_AIRCRAFTPROGRAM_ID FOREIGN KEY (AIRCRAFTPROGRAM_ID) REFERENCES aircraftprogram (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- UPDATE BOA DATABASE FOR BOA-DM-130

-- DROP USELESS TABLES

DROP TABLE nestedset;
DROP TABLE place_storerelation;

ALTER TABLE article
  DROP FOREIGN KEY `FK_ARTICLE_LENTTO_SERVICE_ID`,
  DROP INDEX `K_ARTICLE_LENTTO_SERVICE_ID`,
  DROP `LENTTO_SERVICE_ID`;
DROP TABLE service;

-- MERGE storerelation, contains_place_demand AND contains_place_tool TABLES IN NEW location TABLE

CREATE TABLE `location` (
  `ID` bigint(20) NOT NULL,
  `DTYPE` varchar(31) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `INHERITED` tinyint(1) DEFAULT '0',
  `PLACE_ID` bigint(20) DEFAULT NULL,
  `ARTICLE_ID` bigint(20) DEFAULT NULL,
  `DEMAND_ID` bigint(20) DEFAULT NULL,
  `TOOL_ID` bigint(20) DEFAULT NULL,
  `PRECISELOCATION` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `K_LOCATION_PLACE_ID` (`PLACE_ID`),
  UNIQUE KEY `UK_LOCATION_ARTICLE_ID` (`ARTICLE_ID`),
  UNIQUE KEY `UK_LOCATION_DEMAND_ID` (`DEMAND_ID`),
  UNIQUE KEY `UK_LOCATION_TOOL_ID` (`TOOL_ID`),
  CONSTRAINT `FK_LOCATION_ARTICLE_ID` FOREIGN KEY (`ARTICLE_ID`) REFERENCES `article` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LOCATION_PLACE_ID` FOREIGN KEY (`PLACE_ID`) REFERENCES `place` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LOCATION_DEMAND_ID` FOREIGN KEY (`DEMAND_ID`) REFERENCES `demand` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LOCATION_TOOL_ID` FOREIGN KEY (`TOOL_ID`) REFERENCES `tool` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `location` (ID, DTYPE, VERSION, INHERITED, PLACE_ID, PRECISELOCATION, ARTICLE_ID)
  SELECT storerelation.ID, "LocationForArticle", storerelation.VERSION, 0,
         storerelation.PLACE_ID, storerelation.PRECISELOCATION, storerelation.ARTICLE_ID
  FROM storerelation;
DROP TABLE storerelation;

INSERT INTO `location` (ID, DTYPE, VERSION, INHERITED, PLACE_ID, PRECISELOCATION, TOOL_ID)
  SELECT contains_place_tool.ID, "LocationForTool", contains_place_tool.VERSION, 0,
         contains_place_tool.PLACE_ID, contains_place_tool.PRECISELOCATION, contains_place_tool.TOOL_ID
  FROM contains_place_tool;
DROP TABLE contains_place_tool;

INSERT INTO `location` (ID, DTYPE, VERSION, INHERITED, PLACE_ID, PRECISELOCATION, DEMAND_ID)
  SELECT contains_place_demand.ID, "LocationForDemand", contains_place_demand.VERSION, 0,
         contains_place_demand.PLACE_ID, contains_place_demand.PRECISELOCATION, contains_place_demand.DEMAND_ID
  FROM contains_place_demand;
DROP TABLE contains_place_demand;

-- MERGE contains_inst_cabinet, contains_cabinet_rack, contains_rack_board, contains_pc_board AND contains_tool_article TABLES IN NEW container TABLE

CREATE TABLE `container` (
  `ID` bigint(20) NOT NULL,
  `DTYPE` varchar(31) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `INSTALLATION_ID` bigint(20) DEFAULT NULL,
  `CABINET_ID` bigint(20) DEFAULT NULL,
  `RACK_ID` bigint(20) DEFAULT NULL,
  `PC_ID` bigint(20) DEFAULT NULL,
  `BOARD_ID` bigint(20) DEFAULT NULL,
  `TOOL_ID` bigint(20) DEFAULT NULL,
  `ARTICLE_ID` bigint(20) DEFAULT NULL,
  `LETTER` varchar(3) DEFAULT NULL,
  `RACKPOSITION` varchar(6) DEFAULT NULL,
  `SLOTNUMBER` int(11) DEFAULT NULL,
  `FACE` varchar(10) DEFAULT NULL,
  `COMMENT` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `K_CONTAINER_INSTALLATION_ID` (`INSTALLATION_ID`),
  KEY `K_CONTAINER_CABINET_ID` (`CABINET_ID`),
  KEY `K_CONTAINER_RACK_ID` (`RACK_ID`),
  KEY `K_CONTAINER_PC_ID` (`PC_ID`),
  KEY `K_CONTAINER_BOARD_ID` (`BOARD_ID`),
  KEY `K_CONTAINER_TOOL_ID` (`TOOL_ID`),
  KEY `K_CONTAINER_ARTICLE_ID` (`ARTICLE_ID`),
  CONSTRAINT `FK_CONTAINER_INSTALLATION_ID` FOREIGN KEY (`INSTALLATION_ID`) REFERENCES `installation` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_CABINET_ID` FOREIGN KEY (`CABINET_ID`) REFERENCES `cabinet` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_RACK_ID` FOREIGN KEY (`RACK_ID`) REFERENCES `rack` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_PC_ID` FOREIGN KEY (`PC_ID`) REFERENCES `pc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_BOARD_ID` FOREIGN KEY (`BOARD_ID`) REFERENCES `board` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_TOOL_ID` FOREIGN KEY (`TOOL_ID`) REFERENCES `tool` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINER_ARTICLE_ID` FOREIGN KEY (`ARTICLE_ID`) REFERENCES `article` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `container` (ID, DTYPE, VERSION, INSTALLATION_ID, LETTER, CABINET_ID)
  SELECT contains_inst_cabinet.ID, "Contains_Inst_Cabinet", contains_inst_cabinet.VERSION,
         contains_inst_cabinet.INSTALLATION_ID, contains_inst_cabinet.LETTER, contains_inst_cabinet.CABINET_ID
  FROM contains_inst_cabinet;
DROP TABLE contains_inst_cabinet;

INSERT INTO `container` (ID, DTYPE, VERSION, CABINET_ID, RACKPOSITION, RACK_ID)
  SELECT contains_cabinet_rack.ID, "Contains_Cabinet_Rack", contains_cabinet_rack.VERSION,
         contains_cabinet_rack.CABINET_ID, contains_cabinet_rack.RACKPOSITION, contains_cabinet_rack.RACK_ID
  FROM contains_cabinet_rack;
DROP TABLE contains_cabinet_rack;

INSERT INTO `container` (ID, DTYPE, VERSION, RACK_ID, FACE, SLOTNUMBER, BOARD_ID)
  SELECT contains_rack_board.ID, "Contains_Rack_Board", contains_rack_board.VERSION,
         contains_rack_board.RACK_ID, contains_rack_board.FACE, contains_rack_board.SLOTNUMBER, contains_rack_board.BOARD_ID
  FROM contains_rack_board;
DROP TABLE contains_rack_board;

INSERT INTO `container` (ID, DTYPE, VERSION, PC_ID, SLOTNUMBER, BOARD_ID)
  SELECT contains_pc_board.ID, "Contains_PC_Board", contains_pc_board.VERSION,
         contains_pc_board.PC_ID, contains_pc_board.SLOTNUMBER, contains_pc_board.BOARD_ID
  FROM contains_pc_board;
DROP TABLE contains_pc_board;

INSERT INTO `container` (ID, DTYPE, VERSION, TOOL_ID, COMMENT, ARTICLE_ID)
  SELECT contains_tool_article.ID, "Contains_Tool_Article", contains_tool_article.VERSION,
         contains_tool_article.TOOL_ID, contains_tool_article.COMMENT, contains_tool_article.ARTICLE_ID
  FROM contains_tool_article;
DROP TABLE contains_tool_article;

-- CREATE FUNCTION GENERATING IDS
delimiter //
CREATE FUNCTION getNewId() RETURNS bigint(20)
COMMENT 'A not used id, using the SEQUENCE table'
BEGIN
 DECLARE lId bigint(20);
 SELECT SEQ_COUNT INTO lId FROM SEQUENCE WHERE SEQ_NAME = "SEQ_GEN";
 SET lId = lId + 1;
 UPDATE SEQUENCE SET SEQ_COUNT = lId WHERE SEQ_NAME = "SEQ_GEN";
 RETURN lId;
END
//
delimiter ;

-- CREATE externalentitytype AND externalentitytype_translation TABLES

CREATE TABLE `externalentitytype` (
  `ID` bigint(20) NOT NULL,
  `MODIFIABLE` tinyint(1) DEFAULT '0',
  `DELETABLE` tinyint(1) DEFAULT '0',
  `DEFAULTVALUE` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `externalentitytype_translation` (
  `EXTERNALENTITYTYPE_ID` bigint(20) NOT NULL,
  `TRANSLATION_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`EXTERNALENTITYTYPE_ID`,`TRANSLATION_ID`),
  KEY `K_EXTERNALENTITYTYPE_TRANSLATION_TRANSLATION_ID` (`TRANSLATION_ID`),
  CONSTRAINT `FK_EXTERNALENTITYTYPE_TRANSLATION_EXTERNALENTITYTYPE_ID` FOREIGN KEY (`EXTERNALENTITYTYPE_ID`) REFERENCES `externalentitytype` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EXTERNALENTITYTYPE_TRANSLATION_TRANSLATION_ID` FOREIGN KEY (`TRANSLATION_ID`) REFERENCES `translation` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `externalentitytype` (ID, MODIFIABLE, DELETABLE, DEFAULTVALUE)
  VALUES (getNewid(), 1, 1, "Supplier"),
         (getNewId(), 1, 1, "Service");

INSERT INTO `translation` (ID, VALUE, LANGAGECODE)
  VALUES (getNewId(), "Fournisseur", "fra"),
         (getNewId(), "Service", "fra");

INSERT INTO `externalentitytype_translation` (EXTERNALENTITYTYPE_ID, TRANSLATION_ID)
  VALUES ((SELECT externalentitytype.ID FROM externalentitytype WHERE DEFAULTVALUE = "Supplier"), (SELECT translation.ID FROM translation WHERE VALUE = "Fournisseur")),
         ((SELECT externalentitytype.ID FROM externalentitytype WHERE DEFAULTVALUE = "Service"), (SELECT translation.ID FROM translation WHERE VALUE = "Service"));

-- MERGE supplier TABLE AND place TABLE LINES CORRESPONDING TO SERVICES INTO NEW externalentity TABLE

CREATE TABLE `externalentity` (
  `ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `NAME` varchar(30) NOT NULL,
  `EXTERNALENTITYTYPE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_EXTERNALENTITY_NAME` (`NAME`),
  KEY `K_EXTERNALENTITY_EXTERNALENTITYTYPE_ID` (`EXTERNALENTITYTYPE_ID`),
  CONSTRAINT `FK_EXTERNALENTITY_EXTERNALENTITYTYPE_ID` FOREIGN KEY (`EXTERNALENTITYTYPE_ID`) REFERENCES `externalentitytype` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `externalentity` (ID, VERSION, NAME, EXTERNALENTITYTYPE_ID)
  SELECT supplier.ID, supplier.VERSION, supplier.NAME, (SELECT externalentitytype.ID FROM externalentitytype WHERE DEFAULTVALUE = "Supplier")
  FROM supplier;

INSERT INTO `externalentity` (ID, VERSION, NAME, EXTERNALENTITYTYPE_ID)
  SELECT place.ID, place.VERSION, place.NAME, (SELECT externalentitytype.ID FROM externalentitytype WHERE DEFAULTVALUE = "Service")
  FROM place
  WHERE place.TYPE = "Service";

ALTER TABLE `location`
  ADD `EXTERNALENTITY_ID` bigint(20) DEFAULT NULL AFTER PLACE_ID,
  ADD KEY `K_LOCATION_EXTERNALENTITY_ID` (`EXTERNALENTITY_ID`),
  ADD CONSTRAINT `FK_LOCATION_EXTERNALENTITY_ID` FOREIGN KEY (`EXTERNALENTITY_ID`) REFERENCES `externalentity` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

INSERT INTO `location` (ID, VERSION, DTYPE, EXTERNALENTITY_ID, ARTICLE_ID)
  SELECT getNewId(), 1, "LocationForArticle", article.SENTTO_SUPPLIER_ID, article.ID
  FROM article
  WHERE article.SENTTO_SUPPLIER_ID IS NOT NULL;

ALTER TABLE `article`
  DROP FOREIGN KEY FK_ARTICLE_SENTTO_SUPPLIER_ID,
  DROP KEY K_ARTICLE_SENTTO_SUPPLIER_ID,
  DROP SENTTO_SUPPLIER_ID;

INSERT INTO `location` (ID, VERSION, DTYPE, EXTERNALENTITY_ID, DEMAND_ID)
  SELECT getNewId(), 1, "LocationForDemand", demand.SENTTO_SUPPLIER_ID, demand.ID
  FROM demand
  WHERE demand.SENTTO_SUPPLIER_ID IS NOT NULL;

ALTER TABLE `demand`
  DROP FOREIGN KEY FK_DEMAND_SENTTO_SUPPLIER_ID,
  DROP KEY FK_DEMAND_SENTTO_SUPPLIER_ID,
  DROP SENTTO_SUPPLIER_ID,
  DROP KEY `FK_DEMAND_USEDBY_INSTALLATION_ID`,
  DROP KEY `FK_DEMAND_USEDBY_ARTICLE_ID`,
  DROP KEY `FK_DEMAND_USEDBY_TOOL_ID`,
  ADD KEY `K_DEMAND_USEDBY_INSTALLATION_ID` (`USEDBY_INSTALLATION_ID`),
  ADD KEY `K_DEMAND_USEDBY_ARTICLE_ID` (`USEDBY_ARTICLE_ID`),
  ADD KEY `K_DEMAND_USEDBY_TOOL_ID` (`USEDBY_TOOL_ID`);

UPDATE `location`
  SET EXTERNALENTITY_ID = PLACE_ID
  WHERE PLACE_ID IN (SELECT ID FROM place WHERE place.TYPE = "Service")
  AND EXTERNALENTITY_ID IS NULL;

UPDATE `location`
  SET PLACE_ID = NULL
  WHERE PLACE_ID = EXTERNALENTITY_ID;

DROP TABLE supplier;

DELETE FROM `place` WHERE place.TYPE = "Service";

-- CREATE FUNCTIONS COMPUTING THE CONTAINER OF AN ITEM

delimiter //
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
delimiter ;

delimiter //
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
 CASE lType
  WHEN "Cabinet" THEN
   -- Particular case of parent relation of a Cabinet into an Installation
   SELECT INSTALLATION_ID INTO lInstallationID FROM container WHERE DTYPE = "Contains_Inst_Cabinet" AND CABINET_ID = pID;
   IF lInstallationID IS NOT NULL THEN
    RETURN lInstallationID;
   END IF;
  WHEN "Rack" THEN
   -- Particular case of parent relation of a Rack into a Cabinet
   SELECT CABINET_ID INTO lCabinetID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID = pID;
   IF lCabinetID IS NOT NULL THEN
    RETURN lCabinetID;
   END IF;
  WHEN "Switch" THEN
   -- Particular case of parent relation of a Switch into a Cabinet
   SELECT CABINET_ID INTO lCabinetID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID = pID;
   IF lCabinetID IS NOT NULL THEN
    RETURN lCabinetID;
   END IF;
  WHEN "Board" THEN
   -- Particular case of parent relation of a Board into a Rack
   SELECT RACK_ID INTO lRackID FROM container WHERE DTYPE = "Contains_Rack_Board" AND BOARD_ID = pID;
   IF lRackID IS NOT NULL THEN
    RETURN lRackID;
   END IF;
   -- Particular case of parent relation of a Board into a PC
   SELECT PC_ID INTO lPCID FROM container WHERE DTYPE = "Contains_PC_Board" AND BOARD_ID = pID;
   IF lPCID IS NOT NULL THEN
    RETURN lPCID;
   END IF;
  ELSE
   BEGIN
    -- Nothing to do
   END;
 END CASE;
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
  SELECT TOOL_ID INTO lToolID FROM container WHERE DTYPE = "Contains_Tool_Article" AND ARTICLE_ID = pID;
  IF lToolID IS NOT NULL THEN
   RETURN lToolID;
  END IF;
 END IF;
 RETURN NULL;
END
//
delimiter ;

delimiter //
CREATE FUNCTION getMasterContainerInstallationID(pID bigint, pType varchar(50)) RETURNS bigint
COMMENT 'Return master container ID of the provided id'
BEGIN
-- pID:    the ID of an article, an installation, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of an installation or NULL
 DECLARE lType                varchar(50) DEFAULT pType;
 DECLARE lContainerID         bigint DEFAULT NULL;
 DECLARE lMasterContainerID   bigint DEFAULT NULL;
 DECLARE lMasterContainerType varchar(50) DEFAULT NULL;
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
 SET lContainerID = getContainerID(pID, lType);
 IF lContainerID IS NOT NULL THEN
  IF getType(lContainerID) = "Installation" THEN
   RETURN lContainerID;
  END IF;
 ELSE
  RETURN NULL;
 END IF;
 containerLoop: LOOP
  IF lContainerID IS NULL THEN
   -- no higher container found
   RETURN NULL;
  ELSE
   -- store the found container
   SET lMasterContainerID = lContainerID;
   SET lMasterContainerType = getType(lMasterContainerID);
   -- master container type is an installation, stop search
   IF lMasterContainerType = "Installation" THEN
    RETURN lMasterContainerID;
   END IF;
   -- search the higher container of current master container
   SET lContainerID = getContainerID(lMasterContainerID, lMasterContainerType);
  END IF;
 END LOOP containerLoop;
END
//
delimiter ;

-- DROP COLUMN DEDICATEDTO_INSTALLATION_ID IN article TABLE

-- Change the dedicated to installation to the parent of the article (if no parent is defined)
-- Specific case of Cabinet
INSERT INTO `container` (ID, VERSION, DTYPE, INSTALLATION_ID, CABINET_ID)
  SELECT getNewId(), 1, "Contains_Inst_Cabinet", article.DEDICATEDTO_INSTALLATION_ID, article.ID
  FROM article
  WHERE article.DTYPE = "Cabinet"
  AND article.DEDICATEDTO_INSTALLATION_ID IS NOT NULL
  AND article.USEDBY_INSTALLATION_ID IS NULL
  AND article.USEDBY_ARTICLE_ID IS NULL
  AND article.ID NOT IN
     (SELECT CABINET_ID FROM container WHERE DTYPE = "Contains_Inst_Cabinet" AND CABINET_ID IS NOT NULL
      UNION
	  SELECT RACK_ID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID IS NOT NULL
      UNION
	  SELECT BOARD_ID FROM container WHERE (DTYPE = "Contains_Rack_Board" OR DTYPE = "Contains_PC_Board") AND BOARD_ID IS NOT NULL
      UNION
	  SELECT ARTICLE_ID FROM container WHERE DTYPE = "Contains_Tool_Article" AND ARTICLE_ID IS NOT NULL);
-- Case of other articles except Board
UPDATE `article`
 SET USEDBY_INSTALLATION_ID = DEDICATEDTO_INSTALLATION_ID
 WHERE article.DTYPE != "Cabinet" AND article.DTYPE != "Board"
  AND article.DEDICATEDTO_INSTALLATION_ID IS NOT NULL
  AND article.USEDBY_INSTALLATION_ID IS NULL
  AND article.USEDBY_ARTICLE_ID IS NULL
  AND article.ID NOT IN
     (SELECT CABINET_ID FROM container WHERE DTYPE = "Contains_Inst_Cabinet" AND CABINET_ID IS NOT NULL
      UNION
	  SELECT RACK_ID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID IS NOT NULL
      UNION
	  SELECT BOARD_ID FROM container WHERE (DTYPE = "Contains_Rack_Board" OR DTYPE = "Contains_PC_Board") AND BOARD_ID IS NOT NULL
      UNION
	  SELECT ARTICLE_ID FROM container WHERE DTYPE = "Contains_Tool_Article" AND ARTICLE_ID IS NOT NULL);


-- Erase dedicated to installation when it is identical to the direct container of the article
UPDATE `article`
 SET DEDICATEDTO_INSTALLATION_ID = NULL
 WHERE DEDICATEDTO_INSTALLATION_ID = USEDBY_INSTALLATION_ID;

UPDATE `article`
 JOIN container ON container.CABINET_ID = article.ID
 SET article.DEDICATEDTO_INSTALLATION_ID = NULL
 WHERE article.DEDICATEDTO_INSTALLATION_ID = container.INSTALLATION_ID;

-- Erase dedicated to installation when it is identical to the master container of the article
UPDATE `article`
 SET DEDICATEDTO_INSTALLATION_ID = NULL
 WHERE DEDICATEDTO_INSTALLATION_ID = getMasterContainerInstallationID(ID,NULL);

-- => Treat manually the remaining "dedicated to" values

DROP FUNCTION IF EXISTS getType;
DROP FUNCTION IF EXISTS getContainerID;
DROP FUNCTION IF EXISTS getMasterContainerInstallationID;

ALTER TABLE `article`
  DROP FOREIGN KEY `FK_ARTICLE_DEDICATEDTO_INSTALLATION_ID`,
  DROP INDEX `K_ARTICLE_DEDICATEDTO_INSTALLATION_ID`,
  DROP `DEDICATEDTO_INSTALLATION_ID`;

-- DROP COLUMN BUILDING_ID IN instalation TABLE

ALTER TABLE `installation`
  DROP FOREIGN KEY `FK_INSTALLATION_BUILDING_ID`,
  DROP INDEX `K_INSTALLATION_BUILDING_ID`,
  DROP `BUILDING_ID`;

-- UPDATE LOCATIONS OF ITEMS (SET INHERITED VALUE)

INSERT INTO location (ID, VERSION, DTYPE, INHERITED, ARTICLE_ID)
  SELECT getNewId(), 1, "LocationForArticle", 1, article.ID
  FROM article
  WHERE article.ID NOT IN (SELECT location.ARTICLE_ID FROM location WHERE location.ARTICLE_ID IS NOT NULL)
  AND (article.USEDBY_INSTALLATION_ID IS NOT NULL
    OR article.USEDBY_ARTICLE_ID IS NOT NULL
    OR article.ID IN
     (SELECT CABINET_ID FROM container WHERE DTYPE = "Contains_Inst_Cabinet" AND CABINET_ID IS NOT NULL
       UNION
	  SELECT RACK_ID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID IS NOT NULL
       UNION
	  SELECT BOARD_ID FROM container WHERE (DTYPE = "Contains_Rack_Board" OR DTYPE = "Contains_PC_Board") AND BOARD_ID IS NOT NULL
       UNION
	  SELECT ARTICLE_ID FROM container WHERE DTYPE = "Contains_Tool_Article" AND ARTICLE_ID IS NOT NULL));

UPDATE location
  JOIN article ON article.ID = location.ARTICLE_ID
  SET INHERITED = 1
  WHERE location.PLACE_ID IS NULL
  AND (article.USEDBY_INSTALLATION_ID IS NOT NULL
    OR article.USEDBY_ARTICLE_ID IS NOT NULL
    OR article.ID IN
     (SELECT CABINET_ID FROM container WHERE DTYPE = "Contains_Inst_Cabinet" AND CABINET_ID IS NOT NULL
       UNION
	  SELECT RACK_ID FROM container WHERE DTYPE = "Contains_Cabinet_Rack" AND RACK_ID IS NOT NULL
       UNION
	  SELECT BOARD_ID FROM container WHERE (DTYPE = "Contains_Rack_Board" OR DTYPE = "Contains_PC_Board") AND BOARD_ID IS NOT NULL
       UNION
	  SELECT ARTICLE_ID FROM container WHERE DTYPE = "Contains_Tool_Article" AND ARTICLE_ID IS NOT NULL));

INSERT INTO location (ID, VERSION, DTYPE, INHERITED, TOOL_ID)
  SELECT getNewId(), 1, "LocationForTool", 1, tool.ID
  FROM tool
  WHERE tool.ID NOT IN (SELECT location.TOOL_ID FROM location WHERE location.TOOL_ID IS NOT NULL)
  AND tool.INSTALLATION_ID IS NOT NULL;

UPDATE location
  JOIN tool ON tool.ID = location.TOOL_ID
  SET INHERITED = 1
  WHERE location.PLACE_ID IS NULL
  AND tool.INSTALLATION_ID IS NOT NULL;

INSERT INTO location (ID, VERSION, DTYPE, INHERITED, DEMAND_ID)
  SELECT getNewId(), 1, "LocationForDemand", 1, demand.ID
  FROM demand
  WHERE demand.ID NOT IN (SELECT location.DEMAND_ID FROM location WHERE location.DEMAND_ID IS NOT NULL)
  AND (demand.USEDBY_INSTALLATION_ID IS NOT NULL
      OR demand.USEDBY_TOOL_ID IS NOT NULL
      OR demand.USEDBY_ARTICLE_ID IS NOT NULL);

UPDATE location
  JOIN demand ON demand.ID = location.DEMAND_ID
  SET INHERITED = 1
  WHERE location.PLACE_ID IS NULL
  AND (demand.USEDBY_INSTALLATION_ID IS NOT NULL
      OR demand.USEDBY_TOOL_ID IS NOT NULL
      OR demand.USEDBY_ARTICLE_ID IS NOT NULL);

-- => Update manually the SEQUENCE table with the SEQ_GEN corresponding value rounded to 50 (higher)

DROP FUNCTION IF EXISTS getNewId;


-- UPDATE BOA DATABASE FOR BOA-DM-133

-- CHANGE MAXIMUM SIZE OF PLUGNUMBER TO 15 CHARACTERS
ALTER TABLE demand
 CHANGE PLUGNUMBER PLUGNUMBER VARCHAR(15) NULL DEFAULT NULL;
 
-- CHANGE MAXIMUM SIZE OF DEPARTMENT TO 10 CHARACTERS
ALTER TABLE demand
 CHANGE DEPARTMENT DEPARTMENT VARCHAR(10) NULL DEFAULT NULL;
 
-- CREATE TABLE software_demand.
CREATE TABLE `software_demand` (
  `SOFTWARE_ID` bigint(20) NOT NULL,
  `DEMAND_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`SOFTWARE_ID`,`DEMAND_ID`),
  KEY `K_SOFTWARE_DEMAND_DEMAND_ID` (`DEMAND_ID`),
  CONSTRAINT `FK_SOFTWARE_DEMAND_SOFTWARE_ID` FOREIGN KEY (`SOFTWARE_ID`) REFERENCES `software` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_SOFTWARE_DEMAND_DEMAND_ID` FOREIGN KEY (`DEMAND_ID`) REFERENCES `demand` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- FILL TABLE software_demand.
INSERT INTO `software_demand` (DEMAND_ID, SOFTWARE_ID)
  SELECT ID, OS_SOFTWARE_ID FROM `demand` WHERE OS_SOFTWARE_ID IS NOT NULL;
  
-- DROP COLUMN OS_SOFTWARE_ID in demand TABLE
ALTER TABLE `demand`
  DROP FOREIGN KEY `FK_DEMAND_OS_SOFTWARE_ID`,
  DROP KEY `K_DEMAND_OS_SOFTWARE_ID`,
  DROP `OS_SOFTWARE_ID`;

-- ADD columns for the BusinessAllocation and BusinessUsage in the demand TABLE
ALTER TABLE `demand`
  ADD `BUSINESSUSAGEPC_ID` bigint(20) DEFAULT NULL,
  ADD `BUSINESSALLOCATIONPC_ID` bigint(20) DEFAULT NULL,
  ADD KEY `K_DEMAND_BUSINESSALLOCATIONPC_ID` (`BUSINESSALLOCATIONPC_ID`),
  ADD KEY `K_DEMAND_BUSINESSUSAGEPC_ID` (`BUSINESSUSAGEPC_ID`),
  ADD CONSTRAINT `FK_DEMAND_BUSINESSUSAGEPC_ID` FOREIGN KEY (`BUSINESSUSAGEPC_ID`) REFERENCES `businessusagepc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FK_DEMAND_BUSINESSALLOCATIONPC_ID` FOREIGN KEY (`BUSINESSALLOCATIONPC_ID`) REFERENCES `businessallocationpc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
  
-- Update all article onpurchase state to OnInternalPurchase
UPDATE `article`
  SET STATE = 'OnInternalPurchase'
  WHERE STATE = 'OnPurchase';
