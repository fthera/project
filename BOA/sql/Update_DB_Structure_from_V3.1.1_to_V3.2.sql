--
-- UPDATE BOA DATABASE FROM VERSION 3.1.1 TO VERSION 3.2
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

DELIMITER ;


-- UPDATE BOA DATABASE FOR BOA-DM-144

-- CREATE THE permission TABLE

CREATE TABLE permission (
 ID bigint(20) NOT NULL,
 DTYPE varchar(31) DEFAULT NULL,
 CATEGORY varchar(40) NOT NULL,
 ROLE_ID bigint(20) DEFAULT NULL,
 BITWISECODE tinyint UNSIGNED NOT NULL,
 VERSION bigint(20) DEFAULT NULL,
 PRIMARY KEY (ID),
 UNIQUE KEY UK_PERMISSION_CATEGORY_ROLE (CATEGORY, ROLE_ID),
 CONSTRAINT FK_PERMISSION_ROLE_ID FOREIGN KEY (ROLE_ID) REFERENCES role (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- UPDATE THE role TABLE STRUCTURE

ALTER TABLE role
 DROP VERSION,
 ADD MODIFIABLE tinyint(1) DEFAULT '0',
 ADD DELETABLE tinyint(1) DEFAULT '0',
 ADD DEFAULTVALUE varchar(60) DEFAULT NULL,
 ADD FRENCHVALUE varchar(60) DEFAULT NULL,
 ADD LEVEL int(11) DEFAULT NULL;

-- UPDATE THE role TABLE CONTENT

INSERT INTO role
 VALUES (getNewID(), "None", 1, 1, "None", "Aucun", 10);

UPDATE role
 SET MODIFIABLE = 1, DELETABLE = 1, DEFAULTVALUE = "Regular User", FRENCHVALUE = "Simple Utilisateur", LEVEL = 20
 WHERE LABEL = "Regular_User";

UPDATE role
 SET MODIFIABLE = 1, DELETABLE = 1, DEFAULTVALUE = "Super User", FRENCHVALUE = "Super Utilisateur", LEVEL = 30
 WHERE LABEL = "Super_User";

UPDATE role
 SET MODIFIABLE = 1, DELETABLE = 1, DEFAULTVALUE = "Administrator", FRENCHVALUE = "Administrateur", LEVEL = 40
 WHERE LABEL = "Administrator";

INSERT INTO role
 VALUES (getNewID(), "Super_Administrator", 0, 0, "Super Administrator", "Super Administrateur", 100);

-- ADD COLUMN ROLE_ID TO user TABLE

ALTER TABLE user
 ADD ROLE_ID bigint(20) DEFAULT NULL AFTER FIRSTNAME,
 ADD CONSTRAINT FK_USER_ROLE_ID FOREIGN KEY (ROLE_ID) REFERENCES role (ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- UPDATE THE user TABLE ROLE_ID COLUMN

UPDATE user
 SET user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Administrator")
 WHERE user.ID IN (SELECT user_role.USER_ID FROM user_role JOIN role ON role.ID = user_role.ROLE_ID WHERE role.LABEL = "Administrator");

DELETE FROM user_role USING user_role
 JOIN user ON user.ID = user_role.USER_ID
 JOIN role ON role.ID = user_role.ROLE_ID
 WHERE user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Administrator")
 AND role.LABEL IN ("Administrator", "Super_User", "Regular_User", "Obsolescence_User");

UPDATE user
 SET user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Super User")
 WHERE user.ID IN (SELECT user_role.USER_ID FROM user_role JOIN role ON role.ID = user_role.ROLE_ID WHERE role.LABEL = "Super_User");

DELETE FROM user_role USING user_role
 JOIN user ON user.ID = user_role.USER_ID
 JOIN role ON role.ID = user_role.ROLE_ID
 WHERE user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Super User")
 AND role.LABEL IN ("Super_User", "Regular_User", "Obsolescence_User");

UPDATE user
 SET user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Regular User")
 WHERE user.ID IN (SELECT user_role.USER_ID FROM user_role JOIN role ON role.ID = user_role.ROLE_ID WHERE role.LABEL = "Regular_User");

DELETE FROM user_role USING user_role
 JOIN user ON user.ID = user_role.USER_ID
 JOIN role ON role.ID = user_role.ROLE_ID
 WHERE user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "Regular User")
 AND role.LABEL = "Regular_User";

UPDATE user
 SET user.ROLE_ID = (SELECT role.ID FROM role WHERE DEFAULTVALUE = "None")
 WHERE user.ROLE_ID IS NULL;

-- DELETE TABLE user_role

DROP TABLE
 user_role;

-- DELETE UNUSED VALUES FROM TABLE role AND UPDATE ITS STRUCTURE

DELETE FROM role
 WHERE LABEL = "Obsolescence_User";

ALTER TABLE role
 DROP LABEL;

DELIMITER //

CREATE PROCEDURE fillPermission(pType varchar(31), pCategory varchar(40), pNull tinyint(3), pNone tinyint(3), pRegular tinyint(3), pSuperUser tinyint(3), pAdmin tinyint(3), pSuperAdmin tinyint(3))
COMMENT 'Fill permission table with provided values'
BEGIN
 INSERT INTO permission VALUES
  (getNewID(),pType,pCategory,NULL,pNull,1),
  (getNewID(),pType,pCategory,(SELECT role.ID FROM role WHERE DEFAULTVALUE = "None"),pNone,1),
  (getNewID(),pType,pCategory,(SELECT role.ID FROM role WHERE DEFAULTVALUE = "Regular User"),pRegular,1),
  (getNewID(),pType,pCategory,(SELECT role.ID FROM role WHERE DEFAULTVALUE = "Super User"),pSuperUser,1),
  (getNewID(),pType,pCategory,(SELECT role.ID FROM role WHERE DEFAULTVALUE = "Administrator"),pAdmin,1),
  (getNewID(),pType,pCategory,(SELECT role.ID FROM role WHERE DEFAULTVALUE = "Super Administrator"),pSuperAdmin,1);
END
//

DELIMITER ;

SET @Max = 127;

SET @Create = 1; SET @Read = 2; SET @Update = 4; SET @Delete = 8;
SET @CRUD = @Create + @Read + @Update + @Delete;
SET @CRU = @Create + @Read + @Update;
SET @CR = @Create + @Read;

SET @HierSrch = 1; SET @AdvancedAll = 2;     SET @AllSrch = @HierSrch + @AdvancedAll;
SET @ExpTemp = 1; SET @ExpAll = 2; SET @Import = 4; SET @ExpViews = 8;     SET @AllFile = @ExpTemp + @ExpAll + @Import + @ExpViews;      SET @AllExp = @ExpTemp + @ExpAll + @ExpViews;
SET @Name = 1; SET @Distribution = 2;     SET @AllSoftCr = @Name + @Distribution;
SET @Type = 1; SET @ASN = 2; SET @MSN = 4; SET @AcquDate = 8; SET @CMSCode = 16;     SET @AllArtUpd = @Type + @ASN + @MSN + @AcquDate + @CMSCode;
SET @Confirm = 1; SET @Allocate = 2; SET @MakeAvail = 4; SET @Close = 8; SET @Cancel = 16;     SET @AllDemand = @Confirm + @Allocate + @MakeAvail + @Close + @Cancel;
SET @UsRemMenu = 1; SET @UsRemPage = 2;     SET @AllUsRem = @UsRemMenu + @UsRemPage;
SET @StockConsult = 1;
SET @UpdPrfl = 1; SET @Lastname = 2; SET @Firstname = 4; SET @Mail = 8; SET @Login = 16; SET @Pwd = 32;     SET @ProfilePwd = @UpdPrfl + @Pwd;
SET @SendRmd = 1; SET @AutoUpdt = 2; SET @ImportDocs = 4; SET @PermUpdt = 8;

--                                                                    null |          None |  Regular User |    Super User | Administrator | Super Administrator
CALL fillPermission("PermissionCRUD","ArticleCRUD",                  @Read,          @Read,           @CRU,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","InstallationCRUD",             @Read,          @Read,          @Read,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","ToolCRUD",                     @Read,          @Read,           @CRU,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","SoftwareCRUD",                 @Read,          @Read,            @CR,            @CR,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","TypeCRUD",                         0,              0,              0,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","ReminderCRUD",                 @Read,            @CR,            @CR,          @CRUD,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","AirbusPNCRUD",                     0,          @Read,            @CR,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","ManufacturerPNCRUD",               0,          @Read,            @CR,           @CRU,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","LocationCRUD",                     0,              0,              0,              0,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","ObsolescenceCRUD",             @Read,          @Read,          @Read,          @CRUD,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","UserCRUD",                         0,              0,              0,              0,          @Read,    @Max);
CALL fillPermission("PermissionCRUD","ValuesListCRUD",                   0,              0,              0,              0,          @CRUD,    @Max);
CALL fillPermission("PermissionCRUD","DemandCRUD",                       0,            @CR,            @CR,           @CRU,           @CRU,    @Max);
CALL fillPermission("PermissionAction","SearchActions",           @AllSrch,       @AllSrch,       @AllSrch,       @AllSrch,       @AllSrch,    @Max);
CALL fillPermission("PermissionAction","FileActions", @ExpTemp + @ExpViews,        @AllExp,        @AllExp,       @AllFile,       @AllFile,    @Max);
CALL fillPermission("PermissionAction","SoftwareCreationFields",         0,              0,              0,     @AllSoftCr,     @AllSoftCr,    @Max);
CALL fillPermission("PermissionAction","ArticleUpdateFields",            0,              0,              0,     @AllArtUpd,     @AllArtUpd,    @Max);
CALL fillPermission("PermissionAction","DemandActions",                  0,              0,              0,     @AllDemand,     @AllDemand,    @Max);
CALL fillPermission("PermissionAction","UserRemindersList",     @UsRemPage,      @AllUsRem,      @AllUsRem,      @AllUsRem,      @AllUsRem,    @Max);
CALL fillPermission("PermissionAction","StockActions",                   0,              0,              0,  @StockConsult,  @StockConsult,    @Max);
CALL fillPermission("PermissionAction","UserProfileActionsAndFields",    0,    @ProfilePwd,    @ProfilePwd,    @ProfilePwd,    @ProfilePwd,    @Max);
CALL fillPermission("PermissionAction","HiddenAdministrationActions",    0,              0,              0,              0,              0,    @Max);

DROP PROCEDURE fillPermission;

-- UPDATE BOA DATABASE FOR BOA-DM-179
-- ADD RECIEVEBOAMAIL AND LASTLOGGEDIN COLUMNS TO THE USER TABLE
 
ALTER TABLE user
 ADD RECEIVEBOAMAIL tinyint(1) DEFAULT '1';
ALTER TABLE user
 ADD LASTLOGGEDIN datetime DEFAULT NULL;

-- UPDATE BOA DATABASE FOR BOA-DM-182
-- SET DEFAULTOS FOR EACH PC HAVING ONLY ONE OS

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
 
 -- UPDATE BOA DATABASE FOR BOA-DM-188
-- ADD MASK IN COMMUNICATIONS PORT
ALTER TABLE `communicationport` ADD `MASK` VARCHAR(39) NULL DEFAULT NULL AFTER `NETWORK_ID`;

-- BOA-DM-193 : Comminication port name lenght change : 
ALTER TABLE `communicationport` CHANGE `NAME` `NAME` VARCHAR( 100 ) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL ;

-- BOA-DM-194 : Deleting "TEAM" field in Installations
ALTER TABLE installation DROP TEAM;

-- BOA DM-195 : Adding "USER" field in Installation
ALTER TABLE `installation` ADD `USER` VARCHAR(50) NULL DEFAULT NULL ;

-- BOA DM-199 : Email notification. BOA Parameters entity creation
CREATE TABLE IF NOT EXISTS `boa_parameters` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL,
  `VALUE` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


ALTER TABLE `boa_parameters` ADD PRIMARY KEY (`ID`);

INSERT INTO boa_parameters(id, name, value) VALUES (getNewID(), 'pcdemandemailrecipiant', 'infra_mgt_simu@sogeti.com;jean-marie.clout@airbus.com');

-- Clean database from all __tmpXXXXXXXX ports wronglys created by SYSMON
DELETE bc.* FROM board_communicationport AS bc
 LEFT JOIN communicationport AS c ON bc.COMMUNICATIONPORT_ID = c.ID
 WHERE c.NAME REGEXP '__tmp.*';
DELETE pcc.* FROM pc_communicationport AS pcc
 LEFT JOIN communicationport AS c ON pcc.COMMUNICATIONPORT_ID = c.ID
 WHERE c.NAME REGEXP '__tmp.*';
DELETE c.* FROM communicationport AS c WHERE c.NAME REGEXP '__tmp.*';
DELETE a.* FROM action AS a
 LEFT JOIN comment AS c ON a.COMMENT_ID = c.ID
 WHERE c.MESSAGE REGEXP '.*__tmp.*';
DELETE c.* FROM comment AS c WHERE c.MESSAGE REGEXP '.*__tmp.*';

-- Drop temporary tables, functions and procedures

DROP TABLE temp_available_id;

DROP FUNCTION getNewID;