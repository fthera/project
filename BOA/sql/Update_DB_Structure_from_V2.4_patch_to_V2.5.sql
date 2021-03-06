--
-- UPDATE BOA DATABASE FROM VERSION 2.4 PATCH TO VERSION 2.5
--

-- UPDATE BOA DATABASE FOR BOA-DM-117

-- RENAME TABLES TO LOWER CASE

RENAME TABLE ACTION TO action;
RENAME TABLE ACTIONOBSO TO actionobso;
RENAME TABLE ACTIONOBSO_TRANSLATION TO actionobso_translation;
RENAME TABLE AIRBUSPN TO airbuspn;
RENAME TABLE AIRBUSSTATUS TO airbusstatus;
RENAME TABLE AIRBUSSTATUS_TRANSLATION TO airbusstatus_translation;
RENAME TABLE ARTICLE TO article;
RENAME TABLE BOARD TO board;
RENAME TABLE BOARD_COMMUNICATIONPORT TO board_communicationport;
RENAME TABLE BUILDING TO building;
RENAME TABLE BUSINESSALLOCATIONPC TO businessallocationpc;
RENAME TABLE BUSINESSALLOCATIONPC_TRANSLATION TO businessallocationpc_translation;
RENAME TABLE BUSINESSUSAGEPC TO businessusagepc;
RENAME TABLE BUSINESSUSAGEPC_TRANSLATION TO businessusagepc_translation;
RENAME TABLE CABINET TO cabinet;
RENAME TABLE COMMENT TO comment;
RENAME TABLE COMMUNICATIONPORT TO communicationport;
RENAME TABLE CONSULTPERIOD TO consultperiod;
RENAME TABLE CONSULTPERIOD_TRANSLATION TO consultperiod_translation;
RENAME TABLE CONTAINS_CABINET_RACK TO contains_cabinet_rack;
RENAME TABLE CONTAINS_INST_CABINET TO contains_inst_cabinet;
RENAME TABLE CONTAINS_PC_BOARD TO contains_pc_board;
RENAME TABLE CONTAINS_PLACE_TOOL TO contains_place_tool;
RENAME TABLE CONTAINS_RACK_BOARD TO contains_rack_board;
RENAME TABLE CONTAINS_TOOL_ARTICLE TO contains_tool_article;
RENAME TABLE FIELDMODIFICATION TO fieldmodification;
RENAME TABLE HISTORY TO history;
RENAME TABLE INSTALLATION TO installation;
RENAME TABLE MANUFACTURERPN TO manufacturerpn;
RENAME TABLE MANUFACTURERSTATUS TO manufacturerstatus;
RENAME TABLE MANUFACTURERSTATUS_TRANSLATION TO manufacturerstatus_translation;
RENAME TABLE NESTEDSET TO nestedset;
RENAME TABLE NETWORK TO network;
RENAME TABLE NETWORK_TRANSLATION TO network_translation;
RENAME TABLE OBSOLESCENCEDATA TO obsolescencedata;
RENAME TABLE PC TO pc;
RENAME TABLE PC_COMMUNICATIONPORT TO pc_communicationport;
RENAME TABLE PLACE TO place;
RENAME TABLE PLACE_STORERELATION TO place_storerelation;
RENAME TABLE PRODUCTTYPEPC TO producttypepc;
RENAME TABLE PRODUCTTYPEPC_TRANSLATION TO producttypepc_translation;
RENAME TABLE RACK TO rack;
RENAME TABLE ROLE TO role;
RENAME TABLE SERVICE TO service;
RENAME TABLE SOFTWARE TO software;
RENAME TABLE SOFTWARE_ARTICLE TO software_article;
RENAME TABLE STORERELATION TO storerelation;
RENAME TABLE STRATEGY TO strategy;
RENAME TABLE STRATEGY_TRANSLATION TO strategy_translation;
RENAME TABLE SUPPLIER TO supplier;
RENAME TABLE SWITCH TO switch;
RENAME TABLE TOOL TO tool;
RENAME TABLE TRANSLATION TO translation;
RENAME TABLE TYPEARTICLE TO typearticle;
RENAME TABLE TYPEARTICLE_AIRBUSPN TO typearticle_airbuspn;
RENAME TABLE USER TO user;
RENAME TABLE USER_ROLE TO user_role;
RENAME TABLE VARIOUS TO various;


-- UPDATE BOA DATABASE FOR BOA-DM-88

-- ADD AVAILABILITYSTATUS AND AVAILABILITYDATE in PC

ALTER TABLE `pc`
 ADD `AVAILABILITYSTATUS` VARCHAR( 20 ) NULL DEFAULT NULL,
 ADD `AVAILABILITYDATE` DATE NULL DEFAULT NULL;

-- Update fields
UPDATE pc
 NATURAL JOIN article
 SET pc.AVAILABILITYSTATUS = 'InUse'
 WHERE article.STATE != 'OnPurchase';
UPDATE pc
 NATURAL JOIN article
 SET pc.AVAILABILITYSTATUS = 'New'
 WHERE article.STATE = 'OnPurchase';

-- ADD demand TABLE

CREATE TABLE `demand` (
  `ID` bigint(20) NOT NULL,
  `REQUESTNUMBER` varchar(20) DEFAULT NULL,
  `LASTUPDATE` datetime DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `CREATIONDATE` datetime DEFAULT NULL,
  `TOREPLACE_PC_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `NEEDDATE` date DEFAULT NULL,
  `STATUS` varchar(20) DEFAULT NULL,
  `ALLOCATED_PC_ID` bigint(20) DEFAULT NULL,
  `CLOSUREDATE` date DEFAULT NULL,
  `DEPARTMENT` varchar(5) DEFAULT NULL,
  `PROGRAM` varchar(50) DEFAULT NULL,
  `PROJECT` varchar(50) DEFAULT NULL,
  `BUDGET` varchar(50) DEFAULT NULL,
  `JUSTIFICATION` varchar(100) DEFAULT NULL,
  `CONTACT` varchar(50) DEFAULT NULL,
  `COMMENTS` varchar(250) DEFAULT NULL,
  `TYPEPC_TYPEARTICLE_ID` bigint(20) DEFAULT NULL,
  `PRODUCTTYPEPC_ID` bigint(20) DEFAULT NULL,
  `PLUGNUMBER` varchar(10) DEFAULT NULL,
  `OS_SOFTWARE_ID` bigint(20) DEFAULT NULL,
  `FEATURES` varchar(50) DEFAULT NULL,
  `ADDITIONALINFORMATION` varchar(250) DEFAULT NULL,
  `USEDBY_INSTALLATION_ID` bigint(20) DEFAULT NULL,
  `SENTTO_SUPPLIER_ID` bigint(20) DEFAULT NULL,
  `USEDBY_ARTICLE_ID` bigint(20) DEFAULT NULL,
  `USEDBY_TOOL_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_DEMAND_TOREPLACE_PC_ID` (`TOREPLACE_PC_ID`),
  UNIQUE KEY `UK_DEMAND_ALLOCATED_PC_ID` (`ALLOCATED_PC_ID`),
  KEY `K_DEMAND_USER_ID`(`USER_ID`),
  KEY `K_DEMAND_TYPEPC_TYPEARTICLE_ID`(`TYPEPC_TYPEARTICLE_ID`),
  KEY `K_DEMAND_PRODUCTTYPEPC_ID`(`PRODUCTTYPEPC_ID`),
  KEY `K_DEMAND_OS_SOFTWARE_ID`(`OS_SOFTWARE_ID`),
  CONSTRAINT `FK_DEMAND_TOREPLACE_PC_ID` FOREIGN KEY (`TOREPLACE_PC_ID`) REFERENCES `pc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_USER_ID` FOREIGN KEY (`USER_ID`) REFERENCES `user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_ALLOCATED_PC_ID` FOREIGN KEY (`ALLOCATED_PC_ID`) REFERENCES `pc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_TYPEPC_TYPEARTICLE_ID` FOREIGN KEY (`TYPEPC_TYPEARTICLE_ID`) REFERENCES `typearticle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_PRODUCTTYPEPC_ID` FOREIGN KEY (`PRODUCTTYPEPC_ID`) REFERENCES `producttypepc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_OS_SOFTWARE_ID` FOREIGN KEY (`OS_SOFTWARE_ID`) REFERENCES `software` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_USEDBY_INSTALLATION_ID` FOREIGN KEY (`USEDBY_INSTALLATION_ID`) REFERENCES `installation` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_SENTTO_SUPPLIER_ID` FOREIGN KEY (`SENTTO_SUPPLIER_ID`) REFERENCES `supplier` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_USEDBY_ARTICLE_ID` FOREIGN KEY (`USEDBY_ARTICLE_ID`) REFERENCES `article` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DEMAND_USEDBY_TOOL_ID` FOREIGN KEY (`USEDBY_TOOL_ID`) REFERENCES `tool` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ADD A LINE IN SEQUENCE TABLE FOR demand TABLE ID
INSERT INTO `SEQUENCE` (`SEQ_NAME`, `SEQ_COUNT`)
VALUES ('DEMAND_SEQ', 0);

-- ADD contains_place_demand TABLE

CREATE TABLE IF NOT EXISTS `contains_place_demand` (
  `ID` bigint(20) NOT NULL,
  `PRECISELOCATION` varchar(50) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `PLACE_ID` bigint(20) DEFAULT NULL,
  `DEMAND_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `K_CONTAINS_PLACE_DEMAND_PLACE_ID` (`PLACE_ID`),
  KEY `K_CONTAINS_PLACE_DEMAND_DEMAND_ID` (`DEMAND_ID`),
  CONSTRAINT `FK_CONTAINS_PLACE_DEMAND_PLACE_ID` FOREIGN KEY (`PLACE_ID`) REFERENCES `place` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CONTAINS_PLACE_DEMAND_DEMAND_ID` FOREIGN KEY (`DEMAND_ID`) REFERENCES `demand` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- IN pc TABLE, ADD THE USER_ID COLUMN REFERENCING A USER FOR REPLACING THE INCHARGE COLUMN (TEXT FIELD)

ALTER TABLE pc
 ADD USER_ID BIGINT NULL DEFAULT NULL AFTER INCHARGE,
 ADD INDEX K_PC_USER_ID (USER_ID),
 ADD CONSTRAINT FK_PC_USER_ID FOREIGN KEY (USER_ID) REFERENCES user (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Update the USER_ID when the login is present in INCHARGE column
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE LOGIN LIKE pc.INCHARGE) WHERE USER_ID IS NULL;

-- Update the USER_ID when the firstname (initial or complete) and lastname are present in INCHARGE column
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1),". ",LASTNAME) LIKE pc.INCHARGE) WHERE USER_ID IS NULL;
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1),".",LASTNAME) LIKE pc.INCHARGE) WHERE USER_ID IS NULL;
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LEFT(FIRSTNAME,1)," ",LASTNAME) LIKE pc.INCHARGE) WHERE USER_ID IS NULL;
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(FIRSTNAME," ",LASTNAME) LIKE pc.INCHARGE) WHERE USER_ID IS NULL;
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE CONCAT(LASTNAME," ",FIRSTNAME) LIKE pc.INCHARGE) WHERE USER_ID IS NULL;

-- Update the USER_ID when the lastname is present in INCHARGE column
UPDATE pc SET USER_ID = (SELECT user.ID FROM user WHERE LASTNAME LIKE pc.INCHARGE) WHERE USER_ID IS NULL;

-- Clean the INCHARGE column if USER_ID is filled
UPDATE pc SET INCHARGE = NULL WHERE USER_ID IS NOT NULL;

-- => Treat manually the fill of the USER_ID column

-- Remove temporarily the foreign key constraint
ALTER TABLE pc
 DROP FOREIGN KEY FK_PC_USER_ID;
-- REMOVE THE INCHARGE COLUMN AND CHANGE THE NEW COLUMN TO NOT NULL IN pc TABLE
-- Restore the foreign key constraint on the USER_ID column
ALTER TABLE pc
 DROP COLUMN INCHARGE,
 CHANGE USER_ID USER_ID BIGINT NOT NULL,
 ADD CONSTRAINT FK_PC_USER_ID FOREIGN KEY (USER_ID) REFERENCES user (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- UPDATE BOA DATABASE FOR BOA-DM-120

-- ADD TEAM COLUMN TO installation TABLE

ALTER TABLE installation
 ADD TEAM VARCHAR(10) NULL DEFAULT NULL AFTER AIRCRAFTPROGRAM;
