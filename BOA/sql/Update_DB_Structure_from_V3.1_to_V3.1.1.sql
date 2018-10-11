--
-- UPDATE BOA DATABASE FROM VERSION 3.1 TO VERSION 3.1.1
--

-- UPDATE BOA DATABASE FOR BOA-DM-174

-- CREATE THE MATERIALIZED VIEW FOR ARTICLES COMPUTED LOCATION

CREATE TABLE mat_view_article AS
 SELECT article.ID, getLocationID(article.ID, article.DTYPE) AS C_PLACE_ID
 FROM article;

ALTER TABLE mat_view_article
 ADD UNIQUE KEY UK_MATVIEWARTICLE_ID (ID),
 ADD CONSTRAINT FK_MATVIEWARTICLE_ID FOREIGN KEY (ID) REFERENCES article (ID) ON DELETE CASCADE ON UPDATE CASCADE,
 ADD KEY K_MATVIEWARTICLE_C_PLACE_ID (C_PLACE_ID),
 ADD CONSTRAINT FK_MATVIEWARTICLE_C_PLACE_ID FOREIGN KEY (C_PLACE_ID) REFERENCES place (ID) ON DELETE CASCADE ON UPDATE CASCADE;

-- REMOVE THE C_PLACE_ID COLUMN FROM THE view_article VIEW

ALTER VIEW view_article AS
 SELECT article.ID, airbuspn.IDENTIFIER AS AIRBUSPN, manufacturerpn.IDENTIFIER AS MANUFACTURERPN
  FROM article
 LEFT JOIN airbuspn ON airbuspn.ID = article.AIRBUSPN_ID
 LEFT JOIN manufacturerpn ON manufacturerpn.ID = article.MANUFACTURERPN_ID;

-- CREATE THE PROCEDURE UPDATING THE ITEMS WHICH LOCATION DEPENDS ON THE PROVIDED ITEM LOCATION

-- Create tables for algorithm need

CREATE TABLE temp_parent_items (
 ID bigint,
 TYPE varchar(50),
 LEVEL int,
 UNIQUE KEY UK_TEMPPARENTITEMS_ID_TYPE (ID, TYPE)
);

CREATE TABLE temp_child_items (
 ID bigint,
 TYPE varchar(50),
 UNIQUE KEY UK_TEMPCHILDITEMS_ID_TYPE (ID, TYPE)
);

-- Create procedures

DELIMITER //

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

CREATE PROCEDURE updateDownstreamLocation(pID bigint, pType varchar (50))
COMMENT 'Update C_PLACE_ID column for pID and downstream items'
BEGIN
-- Update the location (mat_view_article.C_PLACE_ID) of the provided item
-- and of the child items depending on it.
-- pID:   the ID of an article, an installation, a tool (must be NOT NULL)
-- pType: the type label corresponding to the pID (can be NULL)
 CALL updateItemLocation(pID, pType);
 -- Store the item as parent to treat, at highest level.
 INSERT INTO temp_parent_items (ID, TYPE, LEVEL) VALUES (pID, pType, 1)
    ON DUPLICATE KEY UPDATE LEVEL = 1;
 CALL updateParentItemsLocation();
END//

DELIMITER ;

-- CREATE TRIGGERS FOR UPDATING THE LOCATION AUTOMATICALLY WHEN DATABASE IS UPDATED

DELIMITER //

CREATE TRIGGER after_insert_article AFTER INSERT ON article
FOR EACH ROW
BEGIN
 -- update article location (no downstream update since article is just created)
 CALL updateItemLocation(NEW.ID, NEW.DTYPE);
END//

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

CREATE TRIGGER before_delete_article BEFORE DELETE ON article
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the article deletion
 CALL initChildItemsList(OLD.ID, OLD.DTYPE);
END//

CREATE TRIGGER after_delete_article AFTER DELETE ON article
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the article deletion
 CALL updateChildItemsLocation();
END//

CREATE TRIGGER after_insert_tool AFTER INSERT ON tool
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the inserted tool
 CALL updateDownstreamLocation(NEW.ID, "Tool");
END//

CREATE TRIGGER after_update_tool AFTER UPDATE ON tool
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the updated tool
 -- check if INSTALLATION_ID has changed
 IF IFNULL(NEW.INSTALLATION_ID,0) <> IFNULL(OLD.INSTALLATION_ID,0) THEN
  CALL updateDownstreamLocation(NEW.ID, "Tool");
 END IF;
END//

CREATE TRIGGER before_delete_tool BEFORE DELETE ON tool
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the tool deletion
 CALL initChildItemsList(OLD.ID, "Tool");
END//

CREATE TRIGGER after_delete_tool AFTER DELETE ON tool
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the tool deletion
 CALL updateChildItemsLocation();
END//

CREATE TRIGGER after_update_installation AFTER UPDATE ON installation
FOR EACH ROW
BEGIN
 -- update the hierarchical tree location from the updated installation
 -- check if LABO_PLACE_ID has changed
 IF IFNULL(NEW.LABO_PLACE_ID,0) <> IFNULL(OLD.LABO_PLACE_ID,0) THEN
  CALL updateDownstreamLocation(NEW.ID, "Installation");
 END IF;
END//

CREATE TRIGGER before_delete_installation BEFORE DELETE ON installation
FOR EACH ROW
BEGIN
 -- initialize the list of child items to be treated... after the installation deletion
 CALL initChildItemsList(OLD.ID, "Installation");
END//

CREATE TRIGGER after_delete_installation AFTER DELETE ON installation
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the installation deletion
 CALL updateChildItemsLocation();
END//

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

CREATE TRIGGER after_delete_location AFTER DELETE ON location
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the location deletion
 CALL updateChildItemsLocation();
END//

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

CREATE TRIGGER after_delete_container AFTER DELETE ON container
FOR EACH ROW
BEGIN
 -- update the location of the child items... found before the container deletion
 CALL updateChildItemsLocation();
END//

DELIMITER ;

