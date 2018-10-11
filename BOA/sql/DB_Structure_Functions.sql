-- -------------------------------------
-- --      BOA database creation      --
-- -------------------------------------
-- --       Database structure        --
-- --          for BOA V3.1.1         --
-- -------------------------------------
-- --    2- Functions, procedures     --
-- --    and triggers description     --
-- -------------------------------------

-- CREATE OR UPDATE FUNCTIONS AND PROCEDURES IN BOA DATABASE

DELIMITER //

DROP FUNCTION IF EXISTS getType//
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

DROP FUNCTION IF EXISTS getName//
CREATE FUNCTION getName(pID bigint) RETURNS varchar(50)
COMMENT 'Return the computed name of the provided id'
BEGIN
-- pID:    the ID of an article, an installation, a place, a building, a tool, an external entity (can be NULL)
-- RETURN: the computed name or NULL
 DECLARE lType                  varchar(50) DEFAULT NULL;
 DECLARE lName                  varchar(50) DEFAULT NULL;
 DECLARE lArticleAirbusSN       varchar(50) DEFAULT NULL;
 DECLARE lArticleManufacturerSN varchar(50) DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 SET lType = getType(pID);
 IF lType IS NULL THEN
  RETURN NULL;
 END IF;
 IF lType = "Cabinet" THEN
  -- For Cabinet, the prefered name is the designation
  SELECT DESIGNATION INTO lName FROM cabinet WHERE ID = pID;
  IF lName IS NOT NULL THEN
   RETURN lName;
  END IF;
 ELSE
  IF lType = "Rack" THEN
   -- For Rack, the prefered name is the designation
   SELECT DESIGNATION INTO lName FROM rack WHERE ID = pID;
   IF lName IS NOT NULL THEN
    RETURN lName;
   END IF;
  ELSE
   IF lType = "PC" THEN
    -- For PC, the prefered name is the PC name
    SELECT NAME INTO lName FROM pc WHERE ID = pID;
    IF lName IS NOT NULL THEN
     RETURN lName;
    END IF;
   END IF;
  END IF;
 END IF;
 -- Common case of articles, when the provided article name has not yet been found
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- In this case, the prefered name is the Airbus SN,
  -- if not defined, the name will be the manufacturer SN
  SELECT AIRBUSSN, MANUFACTURERSN
   INTO lArticleAirbusSN, lArticleManufacturerSN FROM article WHERE ID = pID;
  IF lArticleAirbusSN IS NOT NULL THEN
   RETURN lArticleAirbusSN;
  ELSE
   RETURN lArticleManufacturerSN;
  END IF;
 END IF;
 IF lType = "Installation" THEN
  SELECT NAME INTO lName FROM installation WHERE ID = pID;
  RETURN lName;
 END IF;
 IF lType = "Place" THEN
  SELECT CONCAT(building.NAME, '-', place.NAME) INTO lName FROM place JOIN building ON building.ID = place.BUILDING_ID WHERE place.ID = pID;
  RETURN lName;
 END IF;
 IF lType = "Building" THEN
  SELECT NAME INTO lName FROM building WHERE ID = pID;
  RETURN lName;
 END IF;
 IF lType = "ExternalEntity" THEN
  SELECT NAME INTO lName FROM externalentity WHERE ID = pID;
  RETURN lName;
 END IF;
 IF lType = "Tool" THEN
  SELECT NAME INTO lName FROM tool WHERE ID = pID;
  RETURN lName;
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getInheritedLocation//
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

DROP FUNCTION IF EXISTS getDirectLocationID//
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

DROP FUNCTION IF EXISTS getLocationID//
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

DROP FUNCTION IF EXISTS getLocationDetails//
CREATE FUNCTION getLocationDetails(pID bigint, pType varchar (50)) RETURNS varchar(50)
COMMENT 'Return details on location of the provided item id'
BEGIN
-- pID:    the ID of an article, an installation, a place, a tool (must be NOT NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the details on location or NULL
 DECLARE lType           varchar(50) DEFAULT pType;
 DECLARE lPlaceID        bigint DEFAULT NULL;
 DECLARE lDetails        varchar(50) DEFAULT NULL;
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
  SELECT PLACE_ID, PRECISELOCATION INTO lPlaceID, lDetails FROM location WHERE ARTICLE_ID = pID;
  IF lPlaceID IS NOT NULL THEN
   RETURN lDetails;
  END IF;
 END IF;
 IF lType = "Tool" THEN
  -- Location of a Tool into a Place
  SELECT PLACE_ID, PRECISELOCATION INTO lPlaceID, lDetails FROM location WHERE TOOL_ID = pID;
  IF lPlaceID IS NOT NULL THEN
   RETURN lDetails;
  END IF;
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getExternalLocationID//
CREATE FUNCTION getExternalLocationID(pID bigint, pType varchar (50)) RETURNS bigint
COMMENT 'Return external location ID of the provided item id'
BEGIN
-- pID:    the ID of an article, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of an external entity or NULL
 DECLARE lType             varchar(50) DEFAULT pType;
 DECLARE lExternalEntityID bigint DEFAULT NULL;
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
  -- External location of an Article into an External entity
  SELECT EXTERNALENTITY_ID INTO lExternalEntityID FROM location WHERE ARTICLE_ID = pID;
  RETURN lExternalEntityID;
 END IF;
 IF lType = "Tool" THEN
  -- External location of a Tool into an External entity
  SELECT EXTERNALENTITY_ID INTO lExternalEntityID FROM location WHERE TOOL_ID = pID;
  RETURN lExternalEntityID;
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getContainerID//
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
  -- Case of parent relation of an Article into an Installation or another Article
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

DROP FUNCTION IF EXISTS getContainerDetails//
CREATE FUNCTION getContainerDetails(pID bigint, pType varchar (50)) RETURNS varchar(50)
COMMENT 'Return details on container of the provided item id'
BEGIN
-- pID:    the ID of an article, a tool (must be NOT NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the details on container or NULL
 DECLARE lType           varchar(50) DEFAULT pType;
 DECLARE lInstallationID bigint DEFAULT NULL;
 DECLARE lCabinetID      bigint DEFAULT NULL;
 DECLARE lRackID         bigint DEFAULT NULL;
 DECLARE lPCID           bigint DEFAULT NULL;
 DECLARE lToolID         bigint DEFAULT NULL;
 DECLARE lDetails        varchar(50) DEFAULT NULL;
 DECLARE lFace           varchar(10) DEFAULT NULL;
 DECLARE lSlot           varchar(20) DEFAULT NULL;
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
  SELECT INSTALLATION_ID, LETTER INTO lInstallationID, lDetails FROM container WHERE INSTALLATION_ID IS NOT NULL AND CABINET_ID = pID;
  IF lInstallationID IS NOT NULL THEN
   RETURN lDetails;
  END IF;
 ELSE
  IF lType = "Rack" THEN
   -- Particular case of parent relation of a Rack into a Cabinet
   SELECT CABINET_ID, RACKPOSITION INTO lCabinetID, lDetails FROM container WHERE CABINET_ID IS NOT NULL AND RACK_ID = pID;
   IF lCabinetID IS NOT NULL THEN
    RETURN lDetails;
   END IF;
  ELSE
   IF lType = "Switch" THEN
    -- Particular case of parent relation of a Switch into a Cabinet
    SELECT CABINET_ID, RACKPOSITION INTO lCabinetID, lDetails FROM container WHERE CABINET_ID IS NOT NULL AND RACK_ID = pID;
    IF lCabinetID IS NOT NULL THEN
     RETURN lDetails;
    END IF;
   ELSE
    IF lType = "Board" THEN
     -- Particular case of parent relation of a Board into a Rack
    SELECT RACK_ID, FACE, SLOTNUMBER INTO lRackID, lFace, lSlot FROM container WHERE RACK_ID IS NOT NULL AND BOARD_ID = pID;
     IF lRackID IS NOT NULL THEN
      IF lFace IS NOT NULL AND lSlot IS NOT NULL THEN
       CASE lFace
        WHEN "Rear" THEN
         RETURN CONCAT(lSlot,"ar");
        WHEN "Front" THEN
         RETURN CONCAT(lSlot,"av");
       END CASE;
      ELSE
       RETURN lSlot;
      END IF;
     END IF;
     -- Particular case of parent relation of a Board into a PC
     SELECT PC_ID, SLOTNUMBER INTO lPCID, lDetails FROM container WHERE PC_ID IS NOT NULL AND BOARD_ID = pID;
     IF lPCID IS NOT NULL THEN
      RETURN lDetails;
     END IF;
    END IF;
   END IF;
  END IF;
 END IF;
 -- Common case for articles, when container has not yet been found
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- Case of parent relation of an Article into a Tool
  SELECT TOOL_ID, COMMENT INTO lToolID, lDetails FROM container WHERE TOOL_ID IS NOT NULL AND ARTICLE_ID = pID;
  IF lToolID IS NOT NULL THEN
   RETURN lDetails;
  END IF;
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getMasterContainerID//
CREATE FUNCTION getMasterContainerID(pID bigint, pType varchar(50)) RETURNS bigint
COMMENT 'Return master container ID of the provided id'
BEGIN
-- pID:    the ID of an article, an installation, a tool (can be NULL)
-- pType:  the type label corresponding to the pID (can be NULL)
-- RETURN: the ID of an article, an installation, a tool or NULL
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
 containerLoop: LOOP
  IF lContainerID IS NULL THEN
   -- no higher container found, return the previous one
   RETURN lMasterContainerID;
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

DROP FUNCTION IF EXISTS getActionLabel//
CREATE FUNCTION getActionLabel(pLabel varchar(50)) RETURNS varchar(50)
COMMENT 'Return the action label corresponding to the stored label'
BEGIN
-- pLabel: the label keyword stored
 CASE pLabel
  WHEN "Creation" THEN
   RETURN "Creation";
  WHEN "Modification" THEN
   RETURN "Modification";
  WHEN "AddPort" THEN
   RETURN "Add of communication port";
  WHEN "ModifyPort" THEN
   RETURN "Modification of communication port";
  WHEN "RemovePort" THEN
   RETURN "Removal of communication port";
  WHEN "InstallSoftware" THEN
   RETURN "Software installation";
  WHEN "RemoveSoftware" THEN
   RETURN "Software removal";
  WHEN "LinkOfDocument" THEN
   RETURN "Link document";
  WHEN "UnlinkOfDocument" THEN
   RETURN "Remove document";
  ELSE
   RETURN pLabel;
 END CASE;
END
//

DROP FUNCTION IF EXISTS getDesignation//
CREATE FUNCTION getDesignation(pID bigint) RETURNS varchar(50)
COMMENT 'Return the designation of the rack or cabinet ID'
BEGIN
-- pID: the id of an article
 DECLARE lType varchar(50) DEFAULT NULL;
 SET lType = getType(pID);
 CASE lType
  WHEN "Cabinet" THEN
   RETURN (SELECT DESIGNATION FROM cabinet WHERE ID = pID);
  WHEN "Rack" THEN
   RETURN (SELECT DESIGNATION FROM rack WHERE ID = pID);
  ELSE
   RETURN NULL;
 END CASE;
END
//

DROP FUNCTION IF EXISTS getBoolean//
CREATE FUNCTION getBoolean(pInt tinyint) RETURNS varchar(5)
COMMENT 'Return Yes or No according to provided value'
BEGIN
-- pInt: 1 or 0 representing a boolean
 CASE pInt
  WHEN 1 THEN
   RETURN "Yes";
  WHEN 0 THEN
   RETURN "No";
  ELSE
   RETURN NULL;
 END CASE;
END
//

DROP FUNCTION IF EXISTS getUserName//
CREATE FUNCTION getUserName(pID bigint) RETURNS varchar(100)
COMMENT 'Return user detailed name: LASTNAME Firstname (login)'
BEGIN
-- pID: the id of a user
 DECLARE lID         bigint DEFAULT NULL;
 DECLARE lLastname   varchar(50) DEFAULT NULL;
 DECLARE lFirstname  varchar(25) DEFAULT NULL;
 DECLARE lLogin      varchar(25) DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- Search the user
 SELECT user.ID, user.LASTNAME, user.FIRSTNAME, user.LOGIN
 INTO lID, lLastname, lFirstname, lLogin FROM user WHERE user.ID = pID;
 IF lID IS NOT NULL THEN
  RETURN CONCAT(lLastname, ' ', lFirstname, ' (', lLogin, ')');
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getSoftwareName//
CREATE FUNCTION getSoftwareName(pID bigint) RETURNS varchar(100)
COMMENT 'Return software name in detailed form: NAME DISTRIBUTION KERNEL'
BEGIN
-- pID: the id of a software
 DECLARE lID            bigint DEFAULT NULL;
 DECLARE lName          varchar(50) DEFAULT NULL;
 DECLARE lDistribution  varchar(50) DEFAULT NULL;
 DECLARE lKernel        varchar(50) DEFAULT NULL;
 IF pID IS NULL THEN
  RETURN NULL;
 END IF;
 -- Search the software
 SELECT software.ID, software.NAME, software.DISTRIBUTION, software.KERNEL
 INTO lID, lName, lDistribution, lKernel FROM software WHERE software.ID = pID;
 IF lID IS NOT NULL THEN
  RETURN CONCAT(lName, ' ', lDistribution, ' ', lKernel);
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getObsoReferenceType//
CREATE FUNCTION getObsoReferenceType(pObsolescenceDataID bigint) RETURNS varchar(40)
COMMENT 'Return the obsolescence reference type'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the reference type label or NULL
 DECLARE lAirbusPNID        bigint DEFAULT NULL;
 DECLARE lManufacturerPNID  bigint DEFAULT NULL;
 DECLARE lTypeArticleID     bigint DEFAULT NULL;
 DECLARE lTypePCID          bigint DEFAULT NULL;
 DECLARE lSoftwareID        bigint DEFAULT NULL;
 SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID, obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lAirbusPNID, lManufacturerPNID, lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 IF lAirbusPNID IS NOT NULL AND lTypeArticleID IS NOT NULL THEN
  RETURN "Airbus PN - Article type";
 ELSE
  IF lManufacturerPNID IS NOT NULL AND lTypeArticleID IS NOT NULL THEN
   RETURN "Manufacturer PN - Article type";
  ELSE
   IF lTypePCID IS NOT NULL THEN
    RETURN "PC type";
   ELSE
    IF lSoftwareID IS NOT NULL THEN
     RETURN "Software";
    ELSE
     RETURN NULL;
    END IF;
   END IF;
  END IF;
 END IF;
END
//

DROP FUNCTION IF EXISTS getObsoReferenceName//
CREATE FUNCTION getObsoReferenceName(pObsolescenceDataID bigint) RETURNS varchar(120)
COMMENT 'Return the obsolescence reference name'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the reference name or NULL
 DECLARE lReferenceType             varchar(40) DEFAULT NULL;
 DECLARE lAirbusPNID                bigint DEFAULT NULL;
 DECLARE lManufacturerPNID          bigint DEFAULT NULL;
 DECLARE lTypeArticleID             bigint DEFAULT NULL;
 DECLARE lTypePCID                  bigint DEFAULT NULL;
 DECLARE lSoftwareID                bigint DEFAULT NULL;
 DECLARE lAirbusPNIdentifier        varchar(40) DEFAULT NULL;
 DECLARE lManufacturerPNIdentifier  varchar(40) DEFAULT NULL;
 DECLARE lTypeArticleLabel          varchar(75) DEFAULT NULL;
 DECLARE lTypePCLabel               varchar(75) DEFAULT NULL;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN "ERROR! Unknown reference type";
 END IF;
 SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID, obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lAirbusPNID, lManufacturerPNID, lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 IF lTypeArticleID IS NOT NULL THEN
  SELECT typearticle.LABEL INTO lTypeArticleLabel FROM typearticle WHERE typearticle.ID = lTypeArticleID;
 END IF;
 CASE lReferenceType
  WHEN "Airbus PN - Article type" THEN
   SELECT airbuspn.IDENTIFIER INTO lAirbusPNIdentifier FROM airbuspn WHERE airbuspn.ID = lAirbusPNID;
   IF lAirbusPNIdentifier IS NOT NULL AND lTypeArticleLabel IS NOT NULL THEN
    RETURN CONCAT(lTypeArticleLabel,' - ',lAirbusPNIdentifier);
   ELSE
    RETURN NULL;
   END IF;
  WHEN "Manufacturer PN - Article type" THEN
   SELECT manufacturerpn.IDENTIFIER INTO lManufacturerPNIdentifier FROM manufacturerpn WHERE manufacturerpn.ID = lManufacturerPNID;
   IF lManufacturerPNIdentifier IS NOT NULL AND lTypeArticleLabel IS NOT NULL THEN
    RETURN CONCAT(lTypeArticleLabel,' - ',lManufacturerPNIdentifier);
   ELSE
    RETURN NULL;
   END IF;
  WHEN "PC type" THEN
   SELECT typearticle.LABEL INTO ltypePCLabel FROM typearticle WHERE typearticle.ID = lTypePCID;
   RETURN lTypePCLabel;
  WHEN "Software" THEN
   RETURN getSoftwareName(lSoftwareID);
  ELSE
   RETURN "ERROR! Unknown reference type";
 END CASE;
END
//

DROP FUNCTION IF EXISTS getObsoTypeArticle//
CREATE FUNCTION getObsoTypeArticle(pObsolescenceDataID bigint) RETURNS varchar(75)
COMMENT 'Return the obsolescence article type'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the article (or PC) type or NULL
 DECLARE lReferenceType     varchar(40) DEFAULT NULL;
 DECLARE lTypeArticleID     bigint DEFAULT NULL;
 DECLARE lTypePCID          bigint DEFAULT NULL;
 DECLARE lTypeArticleLabel  varchar(75) DEFAULT NULL;
 DECLARE lTypePCLabel       varchar(75) DEFAULT NULL;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN "ERROR! Unknown reference type";
 END IF;
 SELECT  obsolescencedata.TYPEARTICLE_ID, obsolescencedata.TYPEPC_TYPEARTICLE_ID
 INTO  lTypeArticleID, lTypePCID FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 IF lReferenceType IN ("Airbus PN - Article type", "Manufacturer PN - Article type") THEN
  IF lTypeArticleID IS NOT NULL THEN
   SELECT typearticle.LABEL INTO lTypeArticleLabel FROM typearticle WHERE typearticle.ID = lTypeArticleID;
  END IF;
  RETURN lTypeArticleLabel;
 ELSE
  IF lReferenceType = "PC type" THEN
   IF lTypePCID IS NOT NULL THEN
    SELECT typearticle.LABEL INTO lTypePCLabel FROM typearticle WHERE typearticle.ID = lTypePCID;
   END IF;
   RETURN lTypePCLabel;
  END IF;
 END IF;
 RETURN NULL;
END
//

DROP FUNCTION IF EXISTS getObsoConstituentName//
CREATE FUNCTION getObsoConstituentName(pObsolescenceDataID bigint) RETURNS varchar(120)
COMMENT 'Return the obsolescence reference name'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the reference name or NULL
 DECLARE lReferenceType             varchar(40) DEFAULT NULL;
 DECLARE lAirbusPNID                bigint DEFAULT NULL;
 DECLARE lManufacturerPNID          bigint DEFAULT NULL;
 DECLARE lAirbusPNIdentifier        varchar(40) DEFAULT NULL;
 DECLARE lManufacturerPNIdentifier  varchar(40) DEFAULT NULL;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NOT NULL THEN
  RETURN getObsoReferenceName(pObsolescenceDataID);
 ELSE
  SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID
  INTO lAirbusPNID, lManufacturerPNID
  FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
  IF lAirbusPNID IS NOT NULL THEN
   SELECT airbuspn.IDENTIFIER INTO lAirbusPNIdentifier FROM airbuspn WHERE airbuspn.ID = lAirbusPNID;
   IF lAirbusPNIdentifier IS NOT NULL THEN
    RETURN CONCAT("ERROR! no Type Article - ", lAirbusPNIdentifier);
   END IF;
  ELSE
   IF lManufacturerPNID IS NOT NULL THEN
    SELECT manufacturerpn.IDENTIFIER INTO lManufacturerPNIdentifier FROM manufacturerpn WHERE manufacturerpn.ID = lManufacturerPNID;
    IF lManufacturerPNIdentifier IS NOT NULL THEN
     RETURN CONCAT("ERROR! no Type Article - ", lManufacturerPNIdentifier);
    END IF;
   END IF;
  END IF;
 END IF;
 RETURN "ERROR! Reference undefined";
END
//

DROP FUNCTION IF EXISTS getObsoManufacturer//
CREATE FUNCTION getObsoManufacturer(pObsolescenceDataID bigint) RETURNS varchar(60)
COMMENT 'Return the manufacturer for obsolescence data'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the manufacturer name or NULL
 DECLARE lReferenceType             varchar(40) DEFAULT NULL;
 DECLARE lTypeArticleID             bigint DEFAULT NULL;
 DECLARE lTypePCID                  bigint DEFAULT NULL;
 DECLARE lSoftwareID                bigint DEFAULT NULL;
 DECLARE lTypeArticleManufacturer   varchar(60) DEFAULT NULL;
 DECLARE lTypePCManufacturer        varchar(60) DEFAULT NULL;
 DECLARE lSoftwareManufacturer      varchar(60) DEFAULT NULL;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN "ERROR! Unknown reference type";
 END IF;
 SELECT obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 IF lTypeArticleID IS NOT NULL THEN
  SELECT typearticle.MANUFACTURER INTO lTypeArticleManufacturer FROM typearticle WHERE typearticle.ID = lTypeArticleID;
 END IF;
 CASE lReferenceType
  WHEN "Airbus PN - Article type" THEN
   RETURN lTypeArticleManufacturer;
  WHEN "Manufacturer PN - Article type" THEN
   RETURN lTypeArticleManufacturer;
  WHEN "PC type" THEN
   SELECT typearticle.MANUFACTURER INTO lTypePCManufacturer FROM typearticle WHERE typearticle.ID = lTypePCID;
   RETURN lTypePCManufacturer;
  WHEN "Software" THEN
   SELECT software.MANUFACTURER INTO lSoftwareManufacturer FROM software WHERE software.ID = lSoftwareID;
   RETURN lSoftwareManufacturer;
  ELSE
   RETURN "ERROR! Unknown reference type";
 END CASE;
END
//

DROP FUNCTION IF EXISTS getObsoNextConsultingDate//
CREATE FUNCTION getObsoNextConsultingDate(pLastDate date, pNbMonths int(11)) RETURNS date
COMMENT 'Return the next consulting date'
BEGIN
-- pLastDate: the last update date
-- pNbMonths: the number of months of the consultation period
-- RETURN:    the next consulting date computing from the provided values or NULL
 IF pLastDate IS NOT NULL AND pNbMonths IS NOT NULL THEN
  RETURN DATE_ADD(pLastDate, INTERVAL pNbMonths MONTH);
 ELSE
  RETURN NULL;
 END IF;
END
//

DROP FUNCTION IF EXISTS isArticleInStock//
CREATE FUNCTION isArticleInStock(pID bigint) RETURNS tinyint(1)
COMMENT 'Return 1 if the article is in stock, else 0'
BEGIN
-- pID: the Article ID to analyse
 DECLARE lLocationID    bigint DEFAULT NULL;
 DECLARE lLocationType  varchar(50) DEFAULT NULL;
 DECLARE lPlaceType     varchar(20) DEFAULT NULL;
 DECLARE lPlaceName     varchar(40) DEFAULT NULL;
 SET lLocationID = getLocationID(pID, NULL);
 IF lLocationID IS NULL THEN
  -- the article is not located
  RETURN 0;
 END IF;
 SET lLocationType = getType(lLocationID);
 IF lLocationType = "Place" THEN
  SELECT place.TYPE, place.NAME INTO lPlaceType, lPlaceName FROM place WHERE place.ID = lLocationID;
  IF lPlaceType = "Storeroom" AND lPlaceName != "REBUT (SCRAP)" THEN
   RETURN 1;
  ELSE
   -- the article is located into a place different from a storeroom or into the REBUT (SCRAP)
   RETURN 0;
  END IF;
 ELSE
  -- the article location is not a place
  RETURN 0;
 END IF;
END
//

DROP FUNCTION IF EXISTS isArticleInUse//
CREATE FUNCTION isArticleInUse(pID bigint) RETURNS tinyint(1)
COMMENT 'Return 1 if the article is in use, else 0'
BEGIN
-- pID: the Article ID to analyse
 DECLARE lMasterContainerID    bigint DEFAULT NULL;
 DECLARE lMasterContainerType  varchar(50) DEFAULT NULL;
 SET lMasterContainerID = getMasterContainerID(pID, NULL);
 IF lMasterContainerID IS NULL THEN
  -- the article is not contained
  RETURN 0;
 END IF;
 SET lMasterContainerType = getType(lMasterContainerID);
 IF lMasterContainerType = "Installation" THEN
  RETURN 1;
 ELSE
  -- the article master container is not an installation
  RETURN 0;
 END IF;
END
//

DROP FUNCTION IF EXISTS getObsoNumberOfArticles//
CREATE FUNCTION getObsoNumberOfArticles(pObsolescenceDataID bigint) RETURNS bigint(20)
COMMENT 'Return the total number of articles for reference'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the total number of articles corresponding to the reference or 0
 DECLARE lReferenceType     varchar(40) DEFAULT NULL;
 DECLARE lAirbusPNID        bigint DEFAULT NULL;
 DECLARE lManufacturerPNID  bigint DEFAULT NULL;
 DECLARE lTypeArticleID     bigint DEFAULT NULL;
 DECLARE lTypePCID          bigint DEFAULT NULL;
 DECLARE lSoftwareID        bigint DEFAULT NULL;
 DECLARE lNbArticles        bigint DEFAULT 0;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN 0;
 END IF;
 SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID, obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lAirbusPNID, lManufacturerPNID, lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 CASE lReferenceType
  WHEN "Airbus PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.AIRBUSPN_ID = lAirbusPNID AND article.TYPEARTICLE_ID = lTypeArticleID;
  WHEN "Manufacturer PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.MANUFACTURERPN_ID = lManufacturerPNID AND article.TYPEARTICLE_ID = lTypeArticleID;
  WHEN "PC type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.DTYPE = "PC" AND article.TYPEARTICLE_ID = lTypePCID;
  WHEN "Software" THEN
   SELECT COUNT(DISTINCT software_article.ARTICLE_ID) INTO lNbArticles FROM software_article
   WHERE software_article.SOFTWARE_ID = lSoftwareID;
  ELSE
   RETURN 0;
 END CASE;
 RETURN lNbArticles;
END
//

DROP FUNCTION IF EXISTS getObsoNumberOfArticlesInStock//
CREATE FUNCTION getObsoNumberOfArticlesInStock(pObsolescenceDataID bigint) RETURNS bigint(20)
COMMENT 'Return the number of articles in stock for reference'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the total number of articles corresponding to the reference or 0
 DECLARE lReferenceType     varchar(40) DEFAULT NULL;
 DECLARE lAirbusPNID        bigint DEFAULT NULL;
 DECLARE lManufacturerPNID  bigint DEFAULT NULL;
 DECLARE lTypeArticleID     bigint DEFAULT NULL;
 DECLARE lTypePCID          bigint DEFAULT NULL;
 DECLARE lSoftwareID        bigint DEFAULT NULL;
 DECLARE lNbArticles        bigint DEFAULT 0;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN 0;
 END IF;
 SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID, obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lAirbusPNID, lManufacturerPNID, lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 CASE lReferenceType
  WHEN "Airbus PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.AIRBUSPN_ID = lAirbusPNID AND article.TYPEARTICLE_ID = lTypeArticleID
   AND article.STATE = "Operational" AND isArticleInStock(article.ID) = 1;
  WHEN "Manufacturer PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.MANUFACTURERPN_ID = lManufacturerPNID AND article.TYPEARTICLE_ID = lTypeArticleID
   AND article.STATE = "Operational" AND isArticleInStock(article.ID) = 1;
  WHEN "PC type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.DTYPE = "PC" AND article.TYPEARTICLE_ID = lTypePCID
   AND article.STATE = "Operational" AND isArticleInStock(article.ID) = 1;
  WHEN "Software" THEN
   SELECT COUNT(DISTINCT software_article.ARTICLE_ID) INTO lNbArticles FROM software_article
   JOIN article ON article.ID = software_article.ARTICLE_ID
   WHERE software_article.SOFTWARE_ID = lSoftwareID
   AND article.STATE = "Operational" AND isArticleInStock(article.ID) = 1;
  ELSE
   RETURN 0;
 END CASE;
 RETURN lNbArticles;
END
//

DROP FUNCTION IF EXISTS getObsoNumberOfArticlesInUse//
CREATE FUNCTION getObsoNumberOfArticlesInUse(pObsolescenceDataID bigint) RETURNS bigint(20)
COMMENT 'Return the number of articles in use for reference'
BEGIN
-- pObsolescenceDataID: the ID of the ObsolescenceData (can be NULL)
-- RETURN:              the total number of articles corresponding to the reference or 0
 DECLARE lReferenceType     varchar(40) DEFAULT NULL;
 DECLARE lAirbusPNID        bigint DEFAULT NULL;
 DECLARE lManufacturerPNID  bigint DEFAULT NULL;
 DECLARE lTypeArticleID     bigint DEFAULT NULL;
 DECLARE lTypePCID          bigint DEFAULT NULL;
 DECLARE lSoftwareID        bigint DEFAULT NULL;
 DECLARE lNbArticles        bigint DEFAULT 0;
 SET lReferenceType = getObsoReferenceType(pObsolescenceDataID);
 IF lReferenceType IS NULL THEN
  RETURN 0;
 END IF;
 SELECT obsolescencedata.AIRBUSPN_ID, obsolescencedata.MANUFACTURERPN_ID, obsolescencedata.TYPEARTICLE_ID,
        obsolescencedata.TYPEPC_TYPEARTICLE_ID, obsolescencedata.SOFTWARE_ID
 INTO lAirbusPNID, lManufacturerPNID, lTypeArticleID, lTypePCID, lSoftwareID
 FROM obsolescencedata WHERE obsolescencedata.ID = pObsolescenceDataID;
 CASE lReferenceType
  WHEN "Airbus PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.AIRBUSPN_ID = lAirbusPNID AND article.TYPEARTICLE_ID = lTypeArticleID
   AND isArticleInUse(article.ID) = 1;
  WHEN "Manufacturer PN - Article type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.MANUFACTURERPN_ID = lManufacturerPNID AND article.TYPEARTICLE_ID = lTypeArticleID
   AND isArticleInUse(article.ID) = 1;
  WHEN "PC type" THEN
   SELECT COUNT(article.ID) INTO lNbArticles FROM article
   WHERE article.DTYPE = "PC" AND article.TYPEARTICLE_ID = lTypePCID
   AND isArticleInUse(article.ID) = 1;
  WHEN "Software" THEN
   SELECT COUNT(DISTINCT software_article.ARTICLE_ID) INTO lNbArticles FROM software_article
   JOIN article ON article.ID = software_article.ARTICLE_ID
   WHERE software_article.SOFTWARE_ID = lSoftwareID
   AND isArticleInUse(article.ID) = 1;
  ELSE
   RETURN 0;
 END CASE;
 RETURN lNbArticles;
END
//

DROP PROCEDURE IF EXISTS initChildItemsList//
CREATE PROCEDURE initChildItemsList(pParentID bigint, pParentType varchar(50))
COMMENT 'Initialize temp_child_items with the child items'
BEGIN
-- Initialize the temp_child_items table with the child items which location
-- depends on the provided parent item location.
-- pParentID:   the ID of an article, an installation, a tool (must be NOT NULL)
-- pParentType: the type label corresponding to the pParentID (can be NULL)
 DECLARE lParentType varchar(50) DEFAULT pParentType;
 -- if the provided type is NULL, initialize it
 IF lParentType IS NULL THEN
  SET lParentType = getType(pParentID);
 END IF;
 IF lParentType = "Installation" THEN
  -- Particular case of parent relation of a Cabinet into an Installation
  INSERT INTO temp_child_items SELECT CABINET_ID, "Cabinet" FROM container WHERE CABINET_ID IS NOT NULL AND INSTALLATION_ID = pParentID
     ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
  -- Case of parent relation of an Article into an Installation
  INSERT INTO temp_child_items SELECT ID, DTYPE FROM article WHERE USEDBY_INSTALLATION_ID = pParentID
     ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
  -- Particular case of parent relation of a Tool into an Installation
  INSERT INTO temp_child_items SELECT tool.ID, "Tool" FROM tool WHERE INSTALLATION_ID = pParentID
     ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
 ELSE
  IF lParentType = "Cabinet" THEN
   -- Particular case of parent relation of a Rack or a Switch into a Cabinet
   INSERT INTO temp_child_items SELECT RACK_ID, "Rack" FROM container WHERE RACK_ID IS NOT NULL AND CABINET_ID = pParentID
      ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
  ELSE
   IF lParentType = "Rack" THEN
    -- Particular case of parent relation of a Board into a Rack
    INSERT INTO temp_child_items SELECT BOARD_ID, "Board" FROM container WHERE BOARD_ID IS NOT NULL AND RACK_ID = pParentID
       ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
   ELSE
    IF lParentType = "PC" THEN
     -- Particular case of parent relation of a Board into a PC
     INSERT INTO temp_child_items SELECT BOARD_ID, "Board" FROM container WHERE BOARD_ID IS NOT NULL AND PC_ID = pParentID
        ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
    ELSE
     IF lParentType = "Tool" THEN
      -- Case of parent relation of an Article into a Tool
      INSERT INTO temp_child_items SELECT ARTICLE_ID, NULL FROM container WHERE ARTICLE_ID IS NOT NULL AND TOOL_ID = pParentID
         ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
     END IF;
    END IF;
   END IF;
  END IF;
 END IF;
 -- Common case for articles
 IF lParentType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  -- Case of parent relation of an Article into another Article
  INSERT INTO temp_child_items SELECT article.ID, DTYPE FROM article WHERE USEDBY_ARTICLE_ID = pParentID
     ON DUPLICATE KEY UPDATE temp_child_items.ID = temp_child_items.ID;
 END IF;
END//

DROP PROCEDURE IF EXISTS updateItemLocation//
CREATE PROCEDURE updateItemLocation(pID bigint, pType varchar(50))
COMMENT 'Update mat_view_article.C_PLACE_ID column for pID'
BEGIN
-- Update the mat_view_article.C_PLACE_ID column with the location of the provided item
-- if it is an Article.
-- pID:   the ID of an article, an installation, a tool (must be NOT NULL)
-- pType: the type label corresponding to the pID (can be NULL)
 DECLARE lType varchar(50) DEFAULT pType;
 -- if the provided type is NULL, initialize it
 IF lType IS NULL THEN
  SET lType = getType(pID);
 END IF;
 -- Update the item location if it is an Article
 IF lType IN ("Cabinet", "Rack", "Switch", "Board", "PC", "Various") THEN
  INSERT INTO mat_view_article (ID, C_PLACE_ID) VALUES (pID, getLocationID(pID, lType))
     ON DUPLICATE KEY UPDATE C_PLACE_ID = getLocationID(pID, lType);
 END IF;
END//

DROP PROCEDURE IF EXISTS updateChildItemsLocation//
CREATE PROCEDURE updateChildItemsLocation()
COMMENT 'Update mat_view_article.C_PLACE_ID column for child items'
BEGIN
-- For each child item of temp_child_items table, update its location if it is inherited,
-- remove it from the temp_child_items table and insert it as a parent to treat
-- into the temp_parent_items table.
 DECLARE lLevel      int DEFAULT 0;
 DECLARE lChildCount int DEFAULT 0;
 DECLARE lChildID    bigint DEFAULT NULL;
 DECLARE lChildType  varchar(50) DEFAULT NULL;
 SELECT MAX(LEVEL) INTO lLevel FROM temp_parent_items;
 IF lLevel IS NULL OR lLevel = 0 THEN
  SET lLevel = 1;
 ELSE
  SET lLevel = lLevel + 1;
 END IF;
 -- Loop on child items to treat
 updateChildLocationLoop: LOOP
  -- Check that at least one child item is to be treated
  SELECT COUNT(ID) INTO lChildCount FROM temp_child_items;
  IF lChildCount <= 0 THEN
   LEAVE updateChildLocationLoop;
  END IF;
  -- Retrieve one child item to treat
  SET lChildID = NULL;
  SELECT ID, TYPE INTO lChildID, lChildType FROM temp_child_items LIMIT 1;
  IF lChildID IS NULL THEN
   LEAVE updateChildLocationLoop;
  END IF;
  -- Check that the child item location is inherited from parent item
  IF getInheritedLocation(lChildID,lChildType) = 1 THEN
   CALL updateItemLocation(lChildID, lChildType);
   -- Store the current child item as parent to treat, at lower level.
   INSERT INTO temp_parent_items (ID, TYPE, LEVEL) VALUES (lChildID, lChildType, lLevel)
      ON DUPLICATE KEY UPDATE LEVEL = lLevel;
  END IF;
  -- Remove the current child item from the child items list
  DELETE FROM temp_child_items WHERE ID = lChildID;
 END LOOP updateChildLocationLoop;
END//

DROP PROCEDURE IF EXISTS updateParentItemsLocation//
CREATE PROCEDURE updateParentItemsLocation()
COMMENT 'Update mat_view_article.C_PLACE_ID column for parent items'
BEGIN
-- For each parent item of temp_parent_items table, update its location,
-- search its child items (added in temp_child_items table),
-- and remove it from the table.
 DECLARE lLevel       int DEFAULT 1;
 DECLARE lParentCount int DEFAULT 0;
 DECLARE lParentID    bigint DEFAULT NULL;
 DECLARE lParentType  varchar(50) DEFAULT NULL;
 -- Loop on parent items to treat
 parentLoop: LOOP
  -- Retrieve the current level to treat
  SET lLevel = 0;
  SELECT MIN(LEVEL) INTO lLevel FROM temp_parent_items;
  IF lLevel IS NULL OR lLevel = 0 THEN
   LEAVE parentLoop;
  END IF;
  -- Check that at least one parent item is to be treated
  SELECT COUNT(ID) INTO lParentCount FROM temp_parent_items WHERE LEVEL = lLevel;
  IF lParentCount <= 0 THEN
   LEAVE parentLoop;
  END IF;
  -- Retrieve one parent item to treat
  SET lParentID = NULL;
  SELECT ID, TYPE INTO lParentID, lParentType FROM temp_parent_items WHERE LEVEL = lLevel LIMIT 1;
  IF lParentID IS NULL THEN
   LEAVE parentLoop;
  END IF;
  CALL initChildItemsList(lParentID, lParentType);
  -- Remove the current parent item from the parent items list
  DELETE FROM temp_parent_items WHERE ID = lParentID;
  CALL updateChildItemsLocation();
 END LOOP parentLoop;
END//

DROP PROCEDURE IF EXISTS updateDownstreamLocation//
CREATE PROCEDURE updateDownstreamLocation(pID bigint, pType varchar (50))
COMMENT 'Update mat_view_article C_PLACE_ID'
COMMENT 'column for pID and downstream items'
BEGIN
-- Update the location of the provided item and of the child items depending on it.
-- pID:   the ID of an article, an installation, a tool (must be NOT NULL)
-- pType: the type label corresponding to the pID (can be NULL)
 CALL updateItemLocation(pID, pType);
 -- Store the item as parent to treat, at highest level.
 INSERT INTO temp_parent_items (ID, TYPE, LEVEL) VALUES (pID, pType, 1)
    ON DUPLICATE KEY UPDATE LEVEL = 1;
 CALL updateParentItemsLocation();
END//

DELIMITER ;

-- CREATE OR UPDATE TRIGGERS IN BOA DATABASE

DELIMITER //

DROP TRIGGER IF EXISTS after_insert_article//
CREATE TRIGGER after_insert_article AFTER INSERT ON article
FOR EACH ROW
BEGIN
 -- update article location (no downstream update since article is just created)
 CALL updateItemLocation(NEW.ID, NEW.DTYPE);
END//

DROP TRIGGER IF EXISTS after_update_article//
CREATE TRIGGER after_update_article AFTER UPDATE ON article
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the updated article
 -- check if USEDBY_INSTALLATION_ID or USEDBY_ARTICLE_ID has changed
 IF IFNULL(NEW.USEDBY_INSTALLATION_ID,0) <> IFNULL(OLD.USEDBY_INSTALLATION_ID,0)
    OR IFNULL(NEW.USEDBY_ARTICLE_ID,0) <> IFNULL(OLD.USEDBY_ARTICLE_ID,0) THEN
  CALL updateDownstreamLocation(NEW.ID, NEW.DTYPE);
 END IF;
END//

DROP TRIGGER IF EXISTS before_delete_article//
CREATE TRIGGER before_delete_article BEFORE DELETE ON article
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the article deletion
 CALL initChildItemsList(OLD.ID, OLD.DTYPE);
END//

DROP TRIGGER IF EXISTS after_delete_article//
CREATE TRIGGER after_delete_article AFTER DELETE ON article
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the article deletion
 CALL updateChildItemsLocation();
END//

DROP TRIGGER IF EXISTS after_insert_tool//
CREATE TRIGGER after_insert_tool AFTER INSERT ON tool
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the inserted tool
 CALL updateDownstreamLocation(NEW.ID, "Tool");
END//

DROP TRIGGER IF EXISTS after_update_tool//
CREATE TRIGGER after_update_tool AFTER UPDATE ON tool
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the updated tool
 -- check if INSTALLATION_ID has changed
 IF IFNULL(NEW.INSTALLATION_ID,0) <> IFNULL(OLD.INSTALLATION_ID,0) THEN
  CALL updateDownstreamLocation(NEW.ID, "Tool");
 END IF;
END//

DROP TRIGGER IF EXISTS before_delete_tool//
CREATE TRIGGER before_delete_tool BEFORE DELETE ON tool
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the tool deletion
 CALL initChildItemsList(OLD.ID, "Tool");
END//

DROP TRIGGER IF EXISTS after_delete_tool//
CREATE TRIGGER after_delete_tool AFTER DELETE ON tool
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the tool deletion
 CALL updateChildItemsLocation();
END//

DROP TRIGGER IF EXISTS after_update_installation//
CREATE TRIGGER after_update_installation AFTER UPDATE ON installation
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the updated installation
 -- check if LABO_PLACE_ID has changed
 IF IFNULL(NEW.LABO_PLACE_ID,0) <> IFNULL(OLD.LABO_PLACE_ID,0) THEN
  CALL updateDownstreamLocation(NEW.ID, "Installation");
 END IF;
END//

DROP TRIGGER IF EXISTS before_delete_installation//
CREATE TRIGGER before_delete_installation BEFORE DELETE ON installation
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the installation deletion
 CALL initChildItemsList(OLD.ID, "Installation");
END//

DROP TRIGGER IF EXISTS after_delete_installation//
CREATE TRIGGER after_delete_installation AFTER DELETE ON installation
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the installation deletion
 CALL updateChildItemsLocation();
END//

DROP TRIGGER IF EXISTS after_insert_location//
CREATE TRIGGER after_insert_location AFTER INSERT ON location
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the concerned item
 -- update for ARTICLE_ID
 IF NEW.ARTICLE_ID IS NOT NULL THEN
  CALL updateDownstreamLocation(NEW.ARTICLE_ID, NULL);
 END IF;
 -- update for TOOL_ID
 IF NEW.TOOL_ID IS NOT NULL THEN
  CALL updateDownstreamLocation(NEW.TOOL_ID, "Tool");
 END IF;
END//

DROP TRIGGER IF EXISTS after_update_location//
CREATE TRIGGER after_update_location AFTER UPDATE ON location
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the concerned item
 -- check if INHERITED or PLACE_ID has changed
 IF IFNULL(NEW.INHERITED,0) <> IFNULL(OLD.INHERITED,0)
    OR IFNULL(NEW.PLACE_ID,0) <> IFNULL(OLD.PLACE_ID,0) THEN
  -- update for ARTICLE_ID
  IF NEW.ARTICLE_ID IS NOT NULL THEN
   CALL updateDownstreamLocation(NEW.ARTICLE_ID, NULL);
  END IF;
  -- update for TOOL_ID
  IF NEW.TOOL_ID IS NOT NULL THEN
   CALL updateDownstreamLocation(NEW.TOOL_ID, "Tool");
  END IF;
 END IF;
END//

DROP TRIGGER IF EXISTS before_delete_location//
CREATE TRIGGER before_delete_location BEFORE DELETE ON location
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the location deletion
 -- update for ARTICLE_ID
 IF OLD.ARTICLE_ID IS NOT NULL THEN
  CALL initChildItemsList(OLD.ARTICLE_ID, NULL);
 END IF;
 -- update for TOOL_ID
 IF OLD.TOOL_ID IS NOT NULL THEN
  CALL initChildItemsList(OLD.TOOL_ID, "Tool");
 END IF;
END//

DROP TRIGGER IF EXISTS after_delete_location//
CREATE TRIGGER after_delete_location AFTER DELETE ON location
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the location deletion
 CALL updateChildItemsLocation();
END//

DROP TRIGGER IF EXISTS after_insert_container//
CREATE TRIGGER after_insert_container AFTER INSERT ON container
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the concerned item
 -- update for CABINET_ID in INSTALLATION_ID
 IF NEW.INSTALLATION_ID IS NOT NULL AND NEW.CABINET_ID IS NOT NULL THEN
  CALL updateDownstreamLocation(NEW.CABINET_ID, "Cabinet");
 ELSE
  -- update for RACK_ID in CABINET_ID
  IF NEW.CABINET_ID IS NOT NULL AND NEW.RACK_ID IS NOT NULL THEN
   CALL updateDownstreamLocation(NEW.RACK_ID, "Rack");
  ELSE
   -- update for BOARD_ID in RACK_ID or in PC_ID
   IF (NEW.RACK_ID IS NOT NULL OR NEW.PC_ID IS NOT NULL) AND NEW.BOARD_ID IS NOT NULL THEN
    CALL updateDownstreamLocation(NEW.BOARD_ID, "Board");
   ELSE
    -- update for ARTICLE_ID in TOOL_ID
    IF NEW.TOOL_ID IS NOT NULL AND NEW.ARTICLE_ID IS NOT NULL THEN
     CALL updateDownstreamLocation(NEW.ARTICLE_ID, NULL);
    END IF;
   END IF;
  END IF;
 END IF;
END//

DROP TRIGGER IF EXISTS after_update_container//
CREATE TRIGGER after_update_container AFTER UPDATE ON container
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the concerned item
 -- check if INSTALLATION_ID has changed
 IF IFNULL(NEW.INSTALLATION_ID,0) <> IFNULL(OLD.INSTALLATION_ID,0) THEN
  -- update for CABINET_ID
  IF NEW.CABINET_ID IS NOT NULL THEN
   CALL updateDownstreamLocation(NEW.CABINET_ID, "Cabinet");
  END IF;
 ELSE
  -- check if CABINET_ID has changed
  IF IFNULL(NEW.CABINET_ID,0) <> IFNULL(OLD.CABINET_ID,0) THEN
   -- update for RACK_ID
   IF NEW.RACK_ID IS NOT NULL THEN
    CALL updateDownstreamLocation(NEW.RACK_ID, "Rack");
   END IF;
  ELSE
   -- check if RACK_ID has changed
   IF IFNULL(NEW.RACK_ID,0) <> IFNULL(OLD.RACK_ID,0) THEN
    -- update for BOARD_ID
    IF NEW.BOARD_ID IS NOT NULL THEN
     CALL updateDownstreamLocation(NEW.BOARD_ID, "Board");
    END IF;
   ELSE
    -- check if PC_ID has changed
    IF IFNULL(NEW.PC_ID,0) <> IFNULL(OLD.PC_ID,0) THEN
     -- update for BOARD_ID
     IF NEW.BOARD_ID IS NOT NULL THEN
      CALL updateDownstreamLocation(NEW.BOARD_ID, "Board");
     END IF;
    ELSE
     -- check if TOOL_ID has changed
     IF IFNULL(NEW.TOOL_ID,0) <> IFNULL(OLD.TOOL_ID,0) THEN
      -- update for ARTICLE_ID
      IF NEW.ARTICLE_ID IS NOT NULL THEN
       CALL updateDownstreamLocation(NEW.ARTICLE_ID, NULL);
      END IF;
     END IF;
    END IF;
   END IF;
  END IF;
 END IF;
END//

DROP TRIGGER IF EXISTS before_delete_container//
CREATE TRIGGER before_delete_container BEFORE DELETE ON container
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the container deletion
 -- update for CABINET_ID in INSTALLATION_ID
 IF OLD.INSTALLATION_ID IS NOT NULL AND OLD.CABINET_ID IS NOT NULL THEN
  CALL initChildItemsList(OLD.CABINET_ID, "Cabinet");
 ELSE
  -- update for RACK_ID in CABINET_ID
  IF OLD.CABINET_ID IS NOT NULL AND OLD.RACK_ID IS NOT NULL THEN
   CALL initChildItemsList(OLD.RACK_ID, "Rack");
  ELSE
   -- update for BOARD_ID in RACK_ID or in PC_ID
   IF (OLD.RACK_ID IS NOT NULL OR OLD.PC_ID IS NOT NULL) AND OLD.BOARD_ID IS NOT NULL THEN
    CALL initChildItemsList(OLD.BOARD_ID, "Board");
   ELSE
    -- update for ARTICLE_ID in TOOL_ID
    IF OLD.TOOL_ID IS NOT NULL AND OLD.ARTICLE_ID IS NOT NULL THEN
     CALL initChildItemsList(OLD.ARTICLE_ID, NULL);
    END IF;
   END IF;
  END IF;
 END IF;
END//

DROP TRIGGER IF EXISTS after_delete_container//
CREATE TRIGGER after_delete_container AFTER DELETE ON container
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the container deletion
 CALL updateChildItemsLocation();
END//

DELIMITER ;
