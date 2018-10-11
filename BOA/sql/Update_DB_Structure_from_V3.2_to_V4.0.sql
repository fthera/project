--
-- UPDATE BOA DATABASE FROM VERSION 3.2 TO VERSION 4.0
--

-- Create temporary tables, functions and procedures for updating the database
DROP TABLE IF EXISTS temp_available_id;
CREATE TABLE temp_available_id (
 ID bigint(20) NOT NULL,
 PRIMARY KEY (ID)
);

DELIMITER //


DROP FUNCTION IF EXISTS getNewID//
CREATE FUNCTION getNewID() RETURNS bigint
COMMENT 'Return an ID not used'
BEGIN
-- RETURN: an available ID
 DECLARE lNewID     bigint DEFAULT NULL;
 DECLARE lCurrentID bigint DEFAULT 1;
 DECLARE lSequence  bigint;
 -- Retrieve one available ID
 SELECT ID INTO lNewID FROM temp_available_id LIMIT 1;
 IF lNewID IS NOT NULL THEN
  DELETE FROM temp_available_id WHERE ID = lNewID;
  RETURN lNewID;
 END IF;
 -- If no available ID has been returned, fill the table with available ID
 SELECT SEQ_COUNT INTO lSequence FROM SEQUENCE WHERE SEQ_NAME = "SEQ_GEN";
 FillTableLoop: LOOP
  INSERT INTO temp_available_id (ID) VALUES (lCurrentID);
  SET lCurrentID = lCurrentID + 1;
  IF lCurrentID >= lSequence THEN
   LEAVE FillTableLoop;
  END IF;
 END LOOP FillTableLoop;
 DELETE FROM temp_available_id WHERE ID IN (SELECT action.ID FROM action);
 DELETE FROM temp_available_id WHERE ID IN (SELECT actionobso.ID FROM actionobso);
 DELETE FROM temp_available_id WHERE ID IN (SELECT airbuspn.ID FROM airbuspn);
 DELETE FROM temp_available_id WHERE ID IN (SELECT airbusstatus.ID FROM airbusstatus);
 DELETE FROM temp_available_id WHERE ID IN (SELECT article.ID FROM article);
 DELETE FROM temp_available_id WHERE ID IN (SELECT building.ID FROM building);
 DELETE FROM temp_available_id WHERE ID IN (SELECT businessallocationpc.ID FROM businessallocationpc);
 DELETE FROM temp_available_id WHERE ID IN (SELECT businessusagepc.ID FROM businessusagepc);
 DELETE FROM temp_available_id WHERE ID IN (SELECT comment.ID FROM comment);
 DELETE FROM temp_available_id WHERE ID IN (SELECT communicationport.ID FROM communicationport);
 DELETE FROM temp_available_id WHERE ID IN (SELECT consultperiod.ID FROM consultperiod);
 DELETE FROM temp_available_id WHERE ID IN (SELECT container.ID FROM container);
 DELETE FROM temp_available_id WHERE ID IN (SELECT document.ID FROM document);
 DELETE FROM temp_available_id WHERE ID IN (SELECT externalentity.ID FROM externalentity);
 DELETE FROM temp_available_id WHERE ID IN (SELECT externalentitytype.ID FROM externalentitytype);
 DELETE FROM temp_available_id WHERE ID IN (SELECT history.ID FROM history);
 DELETE FROM temp_available_id WHERE ID IN (SELECT installation.ID FROM installation);
 DELETE FROM temp_available_id WHERE ID IN (SELECT location.ID FROM location);
 DELETE FROM temp_available_id WHERE ID IN (SELECT manufacturerpn.ID FROM manufacturerpn);
 DELETE FROM temp_available_id WHERE ID IN (SELECT manufacturerstatus.ID FROM manufacturerstatus);
 DELETE FROM temp_available_id WHERE ID IN (SELECT memoryslot.ID FROM memoryslot);
 DELETE FROM temp_available_id WHERE ID IN (SELECT network.ID FROM network);
 DELETE FROM temp_available_id WHERE ID IN (SELECT obsolescencedata.ID FROM obsolescencedata);
 DELETE FROM temp_available_id WHERE ID IN (SELECT place.ID FROM place);
 DELETE FROM temp_available_id WHERE ID IN (SELECT producttypepc.ID FROM producttypepc);
 DELETE FROM temp_available_id WHERE ID IN (SELECT reminder.ID FROM reminder);
 DELETE FROM temp_available_id WHERE ID IN (SELECT software.ID FROM software);
 DELETE FROM temp_available_id WHERE ID IN (SELECT strategy.ID FROM strategy);
 DELETE FROM temp_available_id WHERE ID IN (SELECT tool.ID FROM tool);
 DELETE FROM temp_available_id WHERE ID IN (SELECT typearticle.ID FROM typearticle);
 DELETE FROM temp_available_id WHERE ID IN (SELECT user.ID FROM user);
 DELETE FROM temp_available_id WHERE ID IN (SELECT role.ID FROM role);
 -- Retrieve one available ID
 SELECT ID INTO lNewID FROM temp_available_id LIMIT 1;
 IF lNewID IS NOT NULL THEN
  DELETE FROM temp_available_id WHERE ID = lNewID;
  RETURN lNewID;
 END IF;
 -- If no available ID has been returned, update the SEQUENCE table
 UPDATE SEQUENCE SET SEQ_COUNT = SEQ_COUNT + 50 WHERE SEQ_NAME = "SEQ_GEN";
 SELECT SEQ_COUNT INTO lNewID FROM SEQUENCE WHERE SEQ_NAME = "SEQ_GEN";
 RETURN lNewID;
END
//

DROP FUNCTION IF EXISTS insertHistoryLine//
CREATE FUNCTION insertHistoryLine(pHistoryID bigint, pLogin varchar(15), pLabel varchar(30), pField varchar(35), pBeforeValue varchar(140), pAfterValue varchar(140), pComment varchar(450)) RETURNS bigint
COMMENT 'Insert a line in history'
BEGIN
-- Insert lines in comment, action, fieldmodification...
-- pHistoryID:   the ID of the history to update
-- pLogin:       the login of the user performing the action
-- pLabel:       the label of the action
-- pField:       the modified field, if any (can be NULL)
-- pBeforeValue: the value of the field before the modification (can be NULL)
-- pAfterValue:  the value of the field after the modification (can be NULL)
-- pComment:     the comment of the history line (can be NULL)
 DECLARE lCommentID bigint DEFAULT NULL;
 DECLARE lActionID  bigint DEFAULT NULL;
 DECLARE lDtype     varchar(31) DEFAULT "Action";
 -- Check if values are different
 IF IFNULL(pBeforeValue,"") != IFNULL(pAfterValue,"") THEN
  -- Insert the comment line if needed
  IF pComment IS NOT NULL THEN
   SET lCommentID = getNewID();
   INSERT INTO comment (ID, MESSAGE, VERSION) VALUES (lCommentID, pComment, 1);
  END IF;
  -- Insert the action line
  SET lActionID = getNewID();
  IF pField IS NOT NULL THEN
   SET lDtype = "FieldModification";
  END IF;
  INSERT INTO action (ID, DTYPE, AUTHOR, LABEL, LOGIN, DATE, VERSION, COMMENT_ID, HISTORY_ID)
     VALUES (lActionID, lDtype, NULL, pLabel, pLogin, NOW(), 1, lCommentID, pHistoryID);
  IF pField IS NOT NULL THEN
   INSERT INTO fieldmodification (ID, FIELD, BEFOREVALUE, AFTERVALUE)
      VALUES (lActionID, pField, pBeforeValue, pAfterValue);
  END IF;
 END IF;
 RETURN NULL;
END
//

DELIMITER ;


-- UPDATE BOA DATABASE FOR BOA-DM-213
-- ADD COLUMN USESTATE TO user TABLE
-- UPDATE USESTATE VALUES FOR TABLE article
ALTER TABLE article
 ADD USESTATE varchar(20) DEFAULT "InUse" AFTER STATE;

UPDATE article a
 SET a.USESTATE = "Archived"
 WHERE getMasterContainerID(a.ID, NULL) IN (SELECT i.ID FROM installation i WHERE i.NAME LIKE "REMOVED");
 
UPDATE article a
 SET a.USESTATE = "ToBeRemoved"
 WHERE getMasterContainerID(a.ID, NULL) IN (SELECT i.ID FROM installation i WHERE i.NAME LIKE "TO_BE_REMOVED");
 
UPDATE article a
 SET a.USESTATE = "InStock"
 WHERE getMasterContainerID(a.ID, NULL) IN (SELECT i.ID FROM installation i WHERE i.NAME LIKE "STOCK");
 
UPDATE article a
 SET a.USESTATE = "InStock"
 WHERE getMasterContainerID(a.ID, NULL) IS NULL AND a.DTYPE != "PC" AND a.DTYPE != "Various";

-- UPDATE BOA DATABASE FOR BOA-DM-212
-- UPDATE STATE VALUES FOR TABLE article
-- UPDATE fielmodification AND mat_view_article TABLES ACCORDINGLY
-- REMOVE "REBUT (SCRAP)" LOCATION
UPDATE article
 SET USESTATE = "OnInternalPurchase", STATE = "Operational"
 WHERE STATE = "OnInternalPurchase";

UPDATE article
 SET USESTATE = "OnExternalPurchase", STATE = "Operational"
 WHERE STATE = "OnExternalPurchase";

UPDATE article
 SET STATE = "ToBeTested"
 WHERE STATE = "Unknown";
 
SELECT insertHistoryLine(a.HISTORY_ID, "ng15d2d", "Modification", "State", a.STATE, "Unusable", "")
 FROM article a
 JOIN mat_view_article mva ON a.ID = mva.ID
 JOIN place p on mva.C_PLACE_ID = p.ID
 WHERE p.NAME LIKE "REBUT (SCRAP)"; 

UPDATE article a
 JOIN mat_view_article mva ON a.ID = mva.ID
 JOIN place p on mva.C_PLACE_ID = p.ID
 SET a.STATE = "Unusable"
 WHERE p.NAME LIKE "REBUT (SCRAP)"; 

-- UPDATE BOA DATABASE FOR BOA-DM-214
-- ADD TABLES stock_selection AND stock_selection_types
CREATE TABLE IF NOT EXISTS `stock_selection` (
  `ID` bigint(20) NOT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `NAME` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `K_STOCK_SELECTION_USER_ID` (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `stock_selection`
  ADD CONSTRAINT `FK_STOCK_SELECTION_USER_ID` FOREIGN KEY (`USER_ID`) REFERENCES `user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;


CREATE TABLE IF NOT EXISTS `stock_selection_types` (
  `STOCKSELECTION_ID` bigint(20) NOT NULL,
  `TYPEARTICLE_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`STOCKSELECTION_ID`,`TYPEARTICLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `stock_selection_types`
  ADD CONSTRAINT `FK_STOCK_SELECTION_TYPES_STOCKSELECTION_ID` FOREIGN KEY (`STOCKSELECTION_ID`) REFERENCES `stock_selection` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FK_STOCK_SELECTION_TYPES_TYPEARTICLE_ID` FOREIGN KEY (`TYPEARTICLE_ID`) REFERENCES `typearticle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- UPDATE BOA DATABASE FOR BOA-DM-201
-- ADD COLUMN FIXEDIP TO communicationport TABLE
ALTER TABLE communicationport
 ADD FIXEDIP tinyint(1) DEFAULT NULL AFTER IPADDRESS;


-- UPDATE BOA DATABASE FOR BOA-DM-200
-- ADD TABLE dated_comment
CREATE TABLE IF NOT EXISTS `dated_comment` (
  `ID` bigint(20) NOT NULL,
  `ARTICLE_ID` bigint(20) NOT NULL,
  `USER_ID` bigint(20) NOT NULL,
  `COMMENT` varchar(200) NOT NULL,
  `DATE` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `dated_comment`
  ADD CONSTRAINT `FK_DATED_COMMMENT_PC_ID` FOREIGN KEY (`ARTICLE_ID`) REFERENCES `article` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FK_DATED_COMMMENT_USER_ID` FOREIGN KEY (`USER_ID`) REFERENCES `user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- UPDATE BOA DATABASE FOR BOA-DM-203
-- ADD TABLE pc_specificity
CREATE TABLE IF NOT EXISTS `pc_specificity` (
  `ID` bigint(20) NOT NULL,
  `PC_ID` bigint(20) NOT NULL,
  `DESCRIPTION` varchar(200) NOT NULL,
  `CONTACT` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `pc_specificity`
  ADD CONSTRAINT `FK_PC_SPECIFICITY_PC_ID` FOREIGN KEY (`PC_ID`) REFERENCES `pc` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- Minor corrections to clean the database
UPDATE communicationport SET NAME = "" WHERE NAME IS NULL;
UPDATE comment SET MESSAGE = "" WHERE MESSAGE IS NULL;
UPDATE pc SET PLATFORM = NULL WHERE PLATFORM != "x86" AND PLATFORM != "x86_64";


-- Drop temporary tables, functions and procedures
DROP TABLE temp_available_id;
DROP FUNCTION getNewID;
DROP FUNCTION insertHistoryLine;