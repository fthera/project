-- -------------------------------------
-- --   BOA_VIEW database creation    --
-- -------------------------------------
-- --       Based on BOA V3.2         --
-- --       database structure        --
-- -------------------------------------

-- ------------------------
-- --   Views similar    --
-- --   to BOA exports   --
-- ------------------------

-- Building view
-- Type | Name | Parent
CREATE OR REPLACE VIEW view_BUILDING AS
(SELECT 'Building' AS TYPE, BOA.building.NAME AS NAME, NULL AS PARENT
FROM BOA.building)
 UNION
(SELECT BOA.place.TYPE, BOA.place.NAME, BOA.building.NAME AS PARENT
FROM BOA.place
LEFT JOIN BOA.building ON BOA.building.ID = BOA.place.BUILDING_ID)
 UNION
(SELECT BOA.externalentitytype.DEFAULTVALUE, BOA.externalentity.NAME, NULL
FROM BOA.externalentity
LEFT JOIN BOA.externalentitytype ON BOA.externalentitytype.ID = BOA.externalentity.EXTERNALENTITYTYPE_ID)
ORDER BY TYPE, NAME;

-- Installation view
-- ID | Name | Comments | Business Siglum | Aircraft Program | In charge | Starting Date | Location | User
CREATE OR REPLACE VIEW view_INSTALLATION AS
SELECT BOA.installation.ID, BOA.installation.NAME, BOA.installation.COMMENTS,
       BOA.installation.BUSINESSSIGLUM, BOA.aircraftprogram.DEFAULTVALUE AS AIRCRAFTPROGRAM,
       getUserName(BOA.installation.USER_ID) AS INCHARGE, BOA.installation.STARTINGDATE,
       getName(getLocationID(BOA.installation.ID,"Installation")) AS LOCATION,
       BOA.installation.USER
FROM BOA.installation
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
ORDER BY NAME;

-- Tool view
-- ID | Name | Designation | In charge | Loan date | Loan due date
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Details on parent | Master parent Type | Master parent
--    | General Comment
CREATE OR REPLACE VIEW view_TOOL AS
SELECT BOA.tool.ID, BOA.tool.NAME, BOA.tool.DESIGNATION, BOA.tool.PERSONINCHARGE AS INCHARGE,
       BOA.tool.LOANDATE, BOA.tool.LOANDUEDATE,
       getBoolean(getInheritedLocation(BOA.tool.ID,"Tool")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.tool.ID,"Tool")) AS LOCATION,
       getLocationDetails(BOA.tool.ID,"Tool") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.tool.ID,"Tool")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.tool.ID,"Tool")) AS PARENTTYPE,
       getName(getContainerID(BOA.tool.ID,"Tool")) AS PARENT,
       getContainerDetails(BOA.tool.ID,"Tool") AS DETAILSONPARENT,
       getType(getMasterContainerID(BOA.tool.ID,"Tool")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.tool.ID,"Tool")) AS MASTERPARENT,
       BOA.comment.MESSAGE AS GENERALCOMMENT
FROM BOA.tool
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.tool.COMMENT_ID
ORDER BY NAME;

-- Type view
-- For Article | Type | Manufacturer
-- For Article = PC, Rack, Various, Board, Cabinet, Switch
CREATE OR REPLACE VIEW view_TYPE AS
SELECT SUBSTRING(BOA.typearticle.DTYPE FROM 5) AS FORARTICLE, BOA.typearticle.LABEL AS TYPE, BOA.typearticle.MANUFACTURER
FROM BOA.typearticle
ORDER BY FORARTICLE, TYPE;

-- Cabinet view
-- ID | Designation | Airbus SN | Manufacturer SN | Type | Airbus PN | Manufacturer PN | CmsCode | State
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Identification Letter / Details
--    | Acquisition Date | General Comment
CREATE OR REPLACE VIEW view_CABINET AS
SELECT BOA.cabinet.ID, BOA.cabinet.DESIGNATION,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.typearticle.LABEL AS TYPE,
       BOA.airbuspn.IDENTIFIER AS AIRBUSPN,
       BOA.manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
       BOA.article.CMSCODE,
       BOA.article.STATE,
       getBoolean(getInheritedLocation(BOA.article.ID,"Cabinet")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"Cabinet")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"Cabinet") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"Cabinet")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"Cabinet")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"Cabinet")) AS PARENT,
       getContainerDetails(article.ID,"Cabinet") AS IDENTIFICATIONLETTER_DETAILSONPARENT,
       article.ACQUISITIONDATE AS ACQUISITIONDATE,
       comment.MESSAGE AS GENERALCOMMENT
FROM BOA.cabinet
JOIN BOA.article ON BOA.article.ID = BOA.cabinet.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.airbuspn ON BOA.airbuspn.ID = BOA.article.AIRBUSPN_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.history ON BOA.history.ID = BOA.article.HISTORY_ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.history.GENERAL_COMMENT_ID
ORDER BY DESIGNATION, AIRBUSSN, MANUFACTURERSN;

-- Rack view
-- ID | Designation | Airbus SN | Manufacturer SN | Type | Airbus PN | Manufacturer PN | CmsCode | State
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Rack Position / Details | Master parent Type | Master parent
--    | Acquisition Date | General Comment
CREATE OR REPLACE VIEW view_RACK AS
SELECT BOA.rack.ID, BOA.rack.DESIGNATION,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.typearticle.LABEL AS TYPE,
       BOA.airbuspn.IDENTIFIER AS AIRBUSPN,
       BOA.manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
       BOA.article.CMSCODE,
       BOA.article.STATE,
       getBoolean(getInheritedLocation(BOA.article.ID,"Rack")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"Rack")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"Rack") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"Rack")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"Rack")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"Rack")) AS PARENT,
       getContainerDetails(BOA.article.ID,"Rack") AS RACKPOSITION_DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"Rack")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"Rack")) AS MASTERPARENT,
       article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.comment.MESSAGE AS GENERALCOMMENT
FROM BOA.rack
JOIN BOA.article ON BOA.article.ID = BOA.rack.ID AND BOA.article.DTYPE = "Rack"
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.airbuspn ON BOA.airbuspn.ID = BOA.article.AIRBUSPN_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.history ON BOA.history.ID = BOA.article.HISTORY_ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.history.GENERAL_COMMENT_ID
ORDER BY DESIGNATION, AIRBUSSN, MANUFACTURERSN;

-- Switch view
-- ID | Airbus SN | Manufacturer SN | Type | Airbus PN | Manufacturer PN | CmsCode | State | IP Address
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Switch Position / Details | Master parent Type | Master parent
--    | Acquisition Date | General Comment
CREATE OR REPLACE VIEW view_SWITCH AS
SELECT BOA.switch.ID,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.typearticle.LABEL AS TYPE,
       BOA.airbuspn.IDENTIFIER AS AIRBUSPN,
       BOA.manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
       BOA.article.CMSCODE,
       BOA.article.STATE,
       BOA.switch.IPADDRESS,
       getBoolean(getInheritedLocation(BOA.article.ID,"Switch")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"Switch")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"Switch") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"Switch")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"Switch")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"Switch")) AS PARENT,
       getContainerDetails(BOA.article.ID,"Switch") AS SWITCHPOSITION_DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"Switch")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"Switch")) AS MASTERPARENT,
       BOA.article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.comment.MESSAGE AS GENERALCOMMENT
FROM BOA.switch
JOIN BOA.article ON BOA.article.ID = BOA.switch.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.airbuspn ON BOA.airbuspn.ID = BOA.article.AIRBUSPN_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.history ON BOA.history.ID = BOA.article.HISTORY_ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.history.GENERAL_COMMENT_ID
ORDER BY AIRBUSSN, MANUFACTURERSN;

-- PC view
-- ID | Airbus SN | Manufacturer SN | Name | Product type | Type | Manufacturer | Function | In charge | Owner | Owner Siglum | State | Acquisition Date
--    | Local administrator | Number of screens | Comment | Business allocation | Business usage | Assignment | Default OS | Domain | Platform
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Details on parent | Master parent Type | Master parent
--    | Last Automatic Update Date | Last Automatic Update Type | Automatic Data Validity Date
CREATE OR REPLACE VIEW view_PC AS
SELECT BOA.pc.ID,
       BOA.departmentincharge.DEFAULTVALUE AS DEPARTMENTINCHARGE,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.pc.NAME,
       BOA.producttypepc.DEFAULTVALUE AS PRODUCTTYPE,
       BOA.typearticle.LABEL AS TYPE, BOA.typearticle.MANUFACTURER,
       BOA.pc.FUNCTION, getUserName(BOA.user.ID) AS INCHARGE,
       BOA.pc.OWNER, BOA.pc.OWNERSIGLUM,
       BOA.article.STATE,
       BOA.article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.pc.ADMIN AS LOCALADMINISTRATOR, BOA.pc.NBSCREENS AS NUMBEROFSCREENS, BOA.pc.COMMENT,
       BOA.businessallocationpc.DEFAULTVALUE AS BUSINESSALLOCATION,
       BOA.businessusagepc.DEFAULTVALUE AS BUSINESSUSAGE,
       BOA.pc.ASSIGNMENT,
       getSoftwareName(BOA.pc.DEFAULTOS_SOFTWARE_ID) AS DEFAULT_OS,
       BOA.domain.DEFAULTVALUE AS DOMAIN, BOA.pc.PLATFORM,
       getBoolean(getInheritedLocation(BOA.article.ID,"PC")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"PC")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"PC") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"PC")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"PC")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"PC")) AS PARENT,
       getContainerDetails(BOA.article.ID,"PC") AS DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"PC")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"PC")) AS MASTERPARENT,
       BOA.pc.LASTAUTOUPDATEDATE AS LAST_AUTO_UPDATE_DATE,
       BOA.pc.LASTAUTOUPDATETYPE AS LAST_AUTO_UPDATE_TYPE,
       BOA.pc.AUTODATAVALIDITYDATE AS AUTO_DATA_VALIDITY_DATE
FROM BOA.pc
JOIN BOA.article ON BOA.article.ID = BOA.pc.ID
LEFT JOIN BOA.departmentincharge ON BOA.departmentincharge.ID = BOA.pc.DEPARTMENTINCHARGE_ID
LEFT JOIN BOA.producttypepc ON BOA.producttypepc.ID = BOA.pc.PRODUCTTYPEPC_ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.businessallocationpc ON BOA.businessallocationpc.ID = BOA.pc.BUSINESSALLOCATIONPC_ID
LEFT JOIN BOA.businessusagepc ON BOA.businessusagepc.ID = BOA.pc.BUSINESSUSAGEPC_ID
LEFT JOIN BOA.user ON BOA.user.ID = BOA.pc.USER_ID
LEFT JOIN BOA.domain ON BOA.domain.ID = BOA.pc.DOMAIN_ID
ORDER BY NAME;

-- PC view with complete information
-- ID | Airbus SN | Manufacturer SN | Name | Product type | Type | Manufacturer | Function | In charge | Owner | Owner Siglum | State | Acquisition Date
--    | Local administrator | Number of screens | Comment | Business allocation | Business usage | Assignment | Default OS | Domain | Platform
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Details on parent | Master parent Type | Master parent
--    | Last Automatic Update Date | Last Automatic Update Type | Automatic Data Validity Date
--    | CPU Type | CPU Max Speed | CPU Sockets | CPU Core Per Socket | CPU Logical CPU Per Socket | CPU Total Logical CPUs | CPU Hyperthreading
--    | Total Memory Size | Memory Slot Name | Memory Slot Used | Memory Slot Size | Memory Slot Type
CREATE OR REPLACE VIEW view_PC_COMPLETE AS
SELECT BOA.pc.ID,
       BOA.departmentincharge.DEFAULTVALUE AS DEPARTMENTINCHARGE,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.pc.NAME,
       BOA.producttypepc.DEFAULTVALUE AS PRODUCTTYPE,
       BOA.typearticle.LABEL AS TYPE, BOA.typearticle.MANUFACTURER,
       BOA.pc.FUNCTION, getUserName(BOA.user.ID) AS INCHARGE,
       BOA.pc.OWNER, BOA.pc.OWNERSIGLUM,
       BOA.article.STATE,
       BOA.article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.pc.ADMIN AS LOCALADMINISTRATOR, BOA.pc.NBSCREENS AS NUMBEROFSCREENS, BOA.pc.COMMENT,
       BOA.businessallocationpc.DEFAULTVALUE AS BUSINESSALLOCATION,
       BOA.businessusagepc.DEFAULTVALUE AS BUSINESSUSAGE,
       BOA.pc.ASSIGNMENT,
       getSoftwareName(BOA.pc.DEFAULTOS_SOFTWARE_ID) AS DEFAULT_OS,
       BOA.domain.DEFAULTVALUE AS DOMAIN, BOA.pc.PLATFORM,
       getBoolean(getInheritedLocation(BOA.article.ID,"PC")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"PC")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"PC") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"PC")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"PC")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"PC")) AS PARENT,
       getContainerDetails(BOA.article.ID,"PC") AS DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"PC")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"PC")) AS MASTERPARENT,
       BOA.pc.LASTAUTOUPDATEDATE AS LAST_AUTO_UPDATE_DATE,
       BOA.pc.LASTAUTOUPDATETYPE AS LAST_AUTO_UPDATE_TYPE,
       BOA.pc.AUTODATAVALIDITYDATE AS AUTO_DATA_VALIDITY_DATE,
       BOA.pc.CPUTYPE AS CPU_TYPE,
       BOA.pc.CPUMAXSPEED AS CPU_MAX_SPEED,
       BOA.pc.CPUSOCKETS AS CPU_SOCKETS,
       BOA.pc.CPUCOREPERSOCKET AS CPU_CORE_PER_SOCKET,
       BOA.pc.CPULOGICALCPUPERSOCKET AS CPU_LOGICAL_CPU_PER_SOCKET,
       BOA.pc.CPUTOTALLOGICALCPUS AS CPU_TOTAL_LOGICAL_CPUS,
       BOA.pc.CPUHYPERTHREADING AS CPU_HYPERTHREADING,
       BOA.pc.TOTALMEMORYSIZE AS TOTAL_MEMORY_SIZE,
       BOA.memoryslot.NAME AS MEMORY_SLOT_NAME,
       getBoolean(BOA.memoryslot.USED) AS MEMORY_SLOT_USED,
       BOA.memoryslot.MEMORYSIZE AS MEMORY_SLOT_SIZE,
       BOA.memoryslot.MEMORYTYPE AS MEMORY_SLOT_TYPE
FROM BOA.pc
JOIN BOA.article ON BOA.article.ID = BOA.pc.ID
LEFT JOIN BOA.departmentincharge ON BOA.departmentincharge.ID = BOA.pc.DEPARTMENTINCHARGE_ID
LEFT JOIN BOA.producttypepc ON BOA.producttypepc.ID = BOA.pc.PRODUCTTYPEPC_ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.businessallocationpc ON BOA.businessallocationpc.ID = BOA.pc.BUSINESSALLOCATIONPC_ID
LEFT JOIN BOA.businessusagepc ON BOA.businessusagepc.ID = BOA.pc.BUSINESSUSAGEPC_ID
LEFT JOIN BOA.user ON BOA.user.ID = BOA.pc.USER_ID
LEFT JOIN BOA.domain ON BOA.domain.ID = BOA.pc.DOMAIN_ID
LEFT JOIN BOA.memoryslot ON BOA.memoryslot.PC_ID = BOA.pc.ID
ORDER BY NAME;

-- Board view
-- ID | Airbus SN | Manufacturer SN | Type | Airbus PN | Manufacturer PN | Rev H | Rev S | CmsCode
--    | State | Boot Loader | Calibration | Active Stock Control Date
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | SlotNumberPosition / Details | Master parent Type | Master parent
--    | Acquisition Date | General Comment
CREATE OR REPLACE VIEW view_BOARD AS
SELECT BOA.board.ID,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.typearticle.LABEL AS TYPE,
       BOA.airbuspn.IDENTIFIER AS AIRBUSPN,
       BOA.manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
       BOA.board.REVH, BOA.board.REVS,
       BOA.article.CMSCODE,
       BOA.article.STATE,
       BOA.board.BOOTLOADER, BOA.board.CALIBRATION,
       BOA.board.ACTIVESTOCKCONTROLDATE,
       getBoolean(getInheritedLocation(BOA.article.ID,"Board")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"Board")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"Board") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"Board")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"Board")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"Board")) AS PARENT,
       getContainerDetails(BOA.article.ID,"Board") AS SLOTNUMBERPOSITION_DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"Board")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"Board")) AS MASTERPARENT,
       BOA.article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.comment.MESSAGE AS GENERALCOMMENT
FROM BOA.board
JOIN BOA.article ON BOA.article.ID = BOA.board.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.airbuspn ON BOA.airbuspn.ID = BOA.article.AIRBUSPN_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.history ON BOA.history.ID = BOA.article.HISTORY_ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.history.GENERAL_COMMENT_ID
ORDER BY AIRBUSSN, MANUFACTURERSN;

-- Various view
-- ID | Airbus SN | Manufacturer SN | Type | Airbus PN | Manufacturer PN | CmsCode | State
--    | Inherited Location | Location | Details on Location | External Location
--    | Parent type | Parent | Details on parent | Master parent Type | Master parent
--    | Acquisition Date | General Comment
CREATE OR REPLACE VIEW view_VARIOUS AS
SELECT BOA.various.ID,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.typearticle.LABEL AS TYPE,
       BOA.airbuspn.IDENTIFIER AS AIRBUSPN,
       BOA.manufacturerpn.IDENTIFIER AS MANUFACTURERPN,
       BOA.article.CMSCODE,
       BOA.article.STATE,
       getBoolean(getInheritedLocation(BOA.article.ID,"Various")) AS INHERITEDLOCATION,
       getName(getLocationID(BOA.article.ID,"Various")) AS LOCATION,
       getLocationDetails(BOA.article.ID,"Various") AS DETAILSONLOCATION,
       getName(getExternalLocationID(BOA.article.ID,"Various")) AS EXTERNALLOCATION,
       getType(getContainerID(BOA.article.ID,"Various")) AS PARENTTYPE,
       getName(getContainerID(BOA.article.ID,"Various")) AS PARENT,
       getContainerDetails(BOA.article.ID,"Various") AS DETAILSONPARENT,
       getType(getMasterContainerID(BOA.article.ID,"Various")) AS MASTERPARENTTYPE,
       getName(getMasterContainerID(BOA.article.ID,"Various")) AS MASTERPARENT,
       BOA.article.ACQUISITIONDATE AS ACQUISITIONDATE,
       BOA.comment.MESSAGE AS GENERALCOMMENT
FROM BOA.various
JOIN BOA.article ON BOA.article.ID = BOA.various.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.airbuspn ON BOA.airbuspn.ID = BOA.article.AIRBUSPN_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.history ON BOA.history.ID = BOA.article.HISTORY_ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.history.GENERAL_COMMENT_ID
ORDER BY AIRBUSSN, MANUFACTURERSN;

-- Communication Ports view
-- For Article | Airbus SN | Manufacturer SN | Name | IP Address | Mac Address | Network | Plug Number | Comment
CREATE OR REPLACE VIEW view_COMMUNICATIONPORT AS
SELECT getType(BOA.article.ID) AS FORARTICLE,
       BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       BOA.communicationport.NAME, BOA.communicationport.IPADDRESS,
       BOA.communicationport.MASK, BOA.communicationport.MACADDRESS,
       BOA.network.DEFAULTVALUE AS NETWORK,
       BOA.communicationport.SOCKET AS PLUGNUMBER, BOA.communicationport.COMMENTS AS COMMENT
FROM BOA.communicationport
LEFT JOIN BOA.board_communicationport ON BOA.board_communicationport.COMMUNICATIONPORT_ID = BOA.communicationport.ID
LEFT JOIN BOA.board ON BOA.board.ID = BOA.board_communicationport.BOARD_ID
LEFT JOIN BOA.pc_communicationport ON BOA.pc_communicationport.COMMUNICATIONPORT_ID = BOA.communicationport.ID
LEFT JOIN BOA.pc ON BOA.pc.ID = BOA.pc_communicationport.PC_ID
LEFT JOIN BOA.article ON (BOA.article.ID = BOA.board.ID OR BOA.article.ID = BOA.pc.ID)
LEFT JOIN BOA.network ON BOA.network.ID = BOA.communicationport.NETWORK_ID
ORDER BY FORARTICLE, AIRBUSSN, MANUFACTURERSN, NAME;

-- Software view
-- ID | Name | Distribution/Version | Kernel | Operating System | Manufacturer | Description | Licence number
CREATE OR REPLACE VIEW view_SOFTWARE AS
SELECT BOA.software.ID, BOA.software.NAME, BOA.software.DISTRIBUTION AS DISTRIBUTION_VERSION, BOA.software.KERNEL,
       getBoolean(BOA.software.OPERATINGSYSTEM) AS OPERATINGSYSTEM, BOA.software.MANUFACTURER,
       BOA.software.DESCRIPTION, BOA.software.LICENCE AS LICENCENUMBER
FROM BOA.software
ORDER BY NAME, DISTRIBUTION, VERSION;

-- Software Deployment
-- Software | For Article | Airbus SN | Manufacturer SN
CREATE OR REPLACE VIEW view_SOFTWAREDEPLOYMENT AS
SELECT getSoftwareName(BOA.software.ID) AS SOFTWARE,
       BOA.article.DTYPE AS FORARTICLE, BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN
FROM BOA.software_article
JOIN BOA.software ON BOA.software.ID = BOA.software_article.SOFTWARE_ID
JOIN BOA.article ON BOA.article.ID = BOA.software_article.ARTICLE_ID
ORDER BY SOFTWARE, AIRBUSSN, MANUFACTURERSN;

-- User view
-- Login | Lastname | Firstname | Email | Role
CREATE OR REPLACE VIEW view_USER AS
SELECT BOA.user.LOGIN, BOA.user.LASTNAME, BOA.user.FIRSTNAME, BOA.user.EMAIL,
       BOA.role.DEFAULTVALUE AS ROLE, BOA.user.LASTLOGGEDIN
FROM BOA.user
LEFT JOIN BOA.role ON BOA.role.ID = BOA.user.ROLE_ID
GROUP BY LOGIN
ORDER BY LASTNAME, FIRSTNAME;

-- History view
-- Class | Airbus SN | Manufacturer SN | Designation | Type | Login | Action | Modified Field | Before Value | After Value
--       | Date | Comments
CREATE OR REPLACE VIEW view_HISTORY AS
SELECT BOA.article.DTYPE AS CLASS, BOA.article.AIRBUSSN, BOA.article.MANUFACTURERSN,
       getDesignation(BOA.article.ID) AS DESIGNATION,
       BOA.typearticle.LABEL AS TYPE,
       BOA.action.LOGIN, getActionLabel(BOA.action.LABEL) AS ACTION,
       BOA.fieldmodification.FIELD AS MODIFIEDFIELD, BOA.fieldmodification.BEFOREVALUE, BOA.fieldmodification.AFTERVALUE,
       BOA.action.DATE,
       BOA.comment.MESSAGE AS COMMENTS
FROM BOA.history
JOIN BOA.article ON BOA.article.HISTORY_ID = BOA.history.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.action ON BOA.action.HISTORY_ID = BOA.history.ID
LEFT JOIN BOA.fieldmodification ON BOA.fieldmodification.ID = BOA.action.ID
LEFT JOIN BOA.comment ON BOA.comment.ID = BOA.action.COMMENT_ID
ORDER BY CLASS, AIRBUSSN, MANUFACTURERSN, DATE;

-- Obsolescence view
-- ID | Reference Family | Article Type | Reference | Constituent Name
--    | Last Update | Consult Period | Next consulting Date | End of Order | End of Support | Continuity Date (iidefix) | Obsolescence Date | End of Production
--    | Manufacturer Status | AIRBUS Status | Current Action | Strategy | Comments
--    | Total quantity | Stock quantity | In Use quantity
--    | MTBF | In charge of | Manufacturer | Supplier
CREATE OR REPLACE VIEW view_OBSOLESCENCEDATA AS
SELECT BOA.obsolescencedata.ID,
       getObsoReferenceType(BOA.obsolescencedata.ID) AS REFERENCEFAMILY,
       getObsoTypeArticle(BOA.obsolescencedata.ID) AS ARTICLETYPE,
       getObsoReferenceName(BOA.obsolescencedata.ID) AS REFERENCE,
       getObsoConstituentName(BOA.obsolescencedata.ID) AS CONSTITUENTNAME,
       BOA.obsolescencedata.LASTOBSOLESCENCEUPDATE AS LASTUPDATE,
       BOA.consultperiod.DEFAULTVALUE AS CONSULTPERIOD,
       getObsoNextConsultingDate(BOA.obsolescencedata.LASTOBSOLESCENCEUPDATE, BOA.consultperiod.MONTHNUMBER) AS NEXTCONSULTINGDATE,
       BOA.obsolescencedata.ENDOFORDERDATE AS ENDOFORDER,
       BOA.obsolescencedata.ENDOFSUPPORTDATE AS ENDOFSUPPORT,
       BOA.obsolescencedata.CONTINUITYDATE,
       BOA.obsolescencedata.OBSOLESCENCEDATE,
       BOA.obsolescencedata.ENDOFPRODUCTIONDATE AS ENDOFPRODUCTION,
       BOA.manufacturerstatus.DEFAULTVALUE AS MANUFACTURERSTATUS,
       BOA.airbusstatus.DEFAULTVALUE AS AIRBUSSTATUS,
       BOA.actionobso.DEFAULTVALUE AS CURRENTACTION,
       BOA.strategy.DEFAULTVALUE AS STRATEGY,
       BOA.obsolescencedata.COMMENTONSTRATEGY AS COMMENTS,
       getObsoNumberOfArticles(BOA.obsolescencedata.ID) AS TOTALQUANTITY,
       getObsoNumberOfArticlesInStock(BOA.obsolescencedata.ID) AS STOCKQUANTITY,
       getObsoNumberOfArticlesInUse(BOA.obsolescencedata.ID) AS INUSEQUANTITY,
       BOA.obsolescencedata.MTBF,
       BOA.obsolescencedata.PERSONINCHARGE AS INCHARGEOF,
       getObsoManufacturer(BOA.obsolescencedata.ID) AS MANUFACTURER, BOA.obsolescencedata.SUPPLIER
FROM BOA.obsolescencedata
LEFT JOIN BOA.consultperiod ON BOA.consultperiod.ID = BOA.obsolescencedata.CONSULTPERIOD_ID
LEFT JOIN BOA.manufacturerstatus ON BOA.manufacturerstatus.ID = BOA.obsolescencedata.MANUFACTURERSTATUS_ID
LEFT JOIN BOA.airbusstatus ON BOA.airbusstatus.ID = BOA.obsolescencedata.AIRBUSSTATUS_ID
LEFT JOIN BOA.actionobso ON BOA.actionobso.ID = BOA.obsolescencedata.ACTIONOBSO_ID
LEFT JOIN BOA.strategy ON BOA.strategy.ID = BOA.obsolescencedata.STRATEGY_ID
ORDER BY CONSTITUENTNAME;

-- ------------------------
-- --    Customised      --
-- --      views         --
-- ------------------------

-- List of Installations with PC and OS
-- Plateforme | Programme | Machine | utilisation | Logiciel | Distribution/Version_Logiciel | Kernel_Logiciel
CREATE OR REPLACE VIEW view_INSTALLATION_PC_OS AS
SELECT DISTINCT BOA.installation.NAME AS Plateforme, BOA.aircraftprogram.DEFAULTVALUE AS Programme,
 view_PC.NAME AS Machine, view_PC.FUNCTION AS Utilisation,
 BOA.software.NAME AS Logiciel, BOA.software.DISTRIBUTION AS Distribution_Version_Logiciel, BOA.software.KERNEL AS Kernel_Logiciel
FROM BOA.installation
LEFT JOIN view_PC ON view_PC.MASTERPARENTTYPE = "Installation" AND view_PC.MASTERPARENT = BOA.installation.NAME
LEFT JOIN BOA.software_article ON BOA.software_article.ARTICLE_ID = view_PC.ID
LEFT JOIN BOA.software ON BOA.software.ID = BOA.software_article.SOFTWARE_ID
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
WHERE BOA.software.OPERATINGSYSTEM = 1 OR BOA.software.ID IS NULL
ORDER BY Plateforme;

-- List of Installations (with details) with PC and OS
-- Plateforme | Programme | Comments | Person_In_Charge | Machine | Utilisation | Logiciel | Distribution/Version_Logiciel | Kernel_Logiciel
CREATE OR REPLACE VIEW view_INSTALLATION_DETAILS_PC_OS AS
SELECT DISTINCT BOA.installation.NAME AS Plateforme, BOA.aircraftprogram.DEFAULTVALUE AS Programme,
 BOA.installation.COMMENTS AS Comments, getUserName(BOA.installation.USER_ID) AS Person_In_Charge,
 view_PC.NAME AS Machine, view_PC.FUNCTION AS Utilisation,
 BOA.software.NAME AS Logiciel, BOA.software.DISTRIBUTION AS Distribution_Version_Logiciel, BOA.software.KERNEL AS Kernel_Logiciel
FROM BOA.installation
LEFT JOIN view_PC ON view_PC.MASTERPARENTTYPE = "Installation" AND view_PC.MASTERPARENT = BOA.installation.NAME
LEFT JOIN BOA.software_article ON BOA.software_article.ARTICLE_ID = view_PC.ID
LEFT JOIN BOA.software ON BOA.software.ID = BOA.software_article.SOFTWARE_ID
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
WHERE BOA.software.OPERATINGSYSTEM = 1 OR BOA.software.ID IS NULL
ORDER BY Plateforme;

-- OCASIME view
-- #TYPE_ARTICLE | MACHINE | TYPE | OS | BARCODE | ROOM | SOCKET | MAC@ | IP@
--    | PLATFORM_NAME | PLATFORM_DESIGNATION | PROGRAM | SCREENS | USERS | COMMENT
CREATE OR REPLACE VIEW view_OCASIME AS
(SELECT "PC" AS TYPE_ARTICLE,
BOA.pc.NAME AS MACHINE,
BOA.typearticle.LABEL AS TYPE,
getSoftwareName(BOA.software.ID) AS OS,
BOA.article.AIRBUSSN AS BARCODE,
getName(getLocationID(BOA.pc.ID,"PC")) AS ROOM,
BOA.communicationport.SOCKET,
BOA.communicationport.MACADDRESS, BOA.communicationport.IPADDRESS,
BOA.installation.NAME AS PLATFORM_NAME,
BOA.installation.COMMENTS AS PLATFORM_DESIGNATION,
BOA.aircraftprogram.DEFAULTVALUE AS PROGRAM,
BOA.pc.NBSCREENS AS SCREENS,
getUserName(BOA.user.ID) AS USERS,
BOA.pc.COMMENT -- added for some information to display
FROM BOA.pc
JOIN BOA.article ON BOA.article.ID = BOA.pc.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.pc_communicationport ON BOA.pc_communicationport.PC_ID = BOA.pc.ID
LEFT JOIN BOA.communicationport ON BOA.communicationport.ID = BOA.pc_communicationport.COMMUNICATIONPORT_ID
LEFT JOIN BOA.installation ON BOA.installation.ID = BOA.article.USEDBY_INSTALLATION_ID
LEFT JOIN BOA.software ON BOA.software.ID = BOA.pc.DEFAULTOS_SOFTWARE_ID
LEFT JOIN BOA.user ON BOA.user.ID = BOA.pc.USER_ID
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
WHERE BOA.pc.NAME LIKE "%ocasi%"
)
 UNION
(SELECT "Board" AS TYPE_ARTICLE,
BOA.pc.NAME AS MACHINE,
BOA.typearticle.LABEL AS TYPE,
BOA.manufacturerpn.IDENTIFIER AS OS,
BOA.article.AIRBUSSN AS BARCODE,
getName(getLocationID(BOA.article.ID, "Board")) AS ROOM,
BOA.communicationport.SOCKET,
BOA.communicationport.MACADDRESS, BOA.communicationport.IPADDRESS,
BOA.installation.NAME AS PLATFORM_NAME,
BOA.installation.COMMENTS AS PLATFORM_DESIGNATION,
BOA.aircraftprogram.DEFAULTVALUE AS PROGRAM,
NULL AS SCREENS, -- not available for Various
NULL AS USERS, -- not available for Various
NULL AS COMMENT
FROM BOA.board
JOIN BOA.article USING (ID)
LEFT JOIN BOA.container ON BOA.container.BOARD_ID = BOA.board.ID AND BOA.container.DTYPE = "Contains_PC_Board"
LEFT JOIN BOA.pc ON BOA.pc.ID = BOA.container.PC_ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.installation ON BOA.installation.ID = BOA.article.USEDBY_INSTALLATION_ID
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
LEFT JOIN BOA.board_communicationport ON BOA.board_communicationport.BOARD_ID = BOA.board.ID
LEFT JOIN BOA.communicationport ON BOA.communicationport.ID = BOA.board_communicationport.COMMUNICATIONPORT_ID
WHERE BOA.pc.NAME LIKE "%ocasi%"
)
 UNION
(SELECT "Various" AS TYPE_ARTICLE,
NULL AS MACHINE,
BOA.typearticle.LABEL AS TYPE,
BOA.manufacturerpn.IDENTIFIER AS OS,
BOA.article.AIRBUSSN AS BARCODE,
getName(getLocationID(BOA.article.ID, "Various")) AS ROOM,
NULL AS SOCKET, -- not available for Various
NULL AS MACADDRESS, NULL AS IPADDRESS, -- not available for Various
BOA.installation.NAME AS PLATFORM_NAME,
BOA.installation.COMMENTS AS PLATFORM_DESIGNATION,
BOA.aircraftprogram.DEFAULTVALUE AS PROGRAM,
NULL AS SCREENS, -- not available for Various
NULL AS USERS, -- not available for Various
NULL AS COMMENT
FROM BOA.various
JOIN BOA.article USING (ID)
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.manufacturerpn ON BOA.manufacturerpn.ID = BOA.article.MANUFACTURERPN_ID
LEFT JOIN BOA.installation ON BOA.installation.ID = BOA.article.USEDBY_INSTALLATION_ID
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
WHERE BOA.installation.COMMENTS LIKE "%OCASI%"
);

-- PC_EYYS_INSTALLATION_EV view
-- #PC_NAME | TYPE | AIRBUS_SN | PC_IN_CHARGE | PC_OWNER | PC_OWNER_SIGLUM | ROOM | PLATFORM_NAME | PLATFORM_DESIGNATION
--    | PROGRAM | PLATFORM_IN_CHARGE | PLATFORM_BUSINESS_SIGLUM | SCREENS | USERS | COMMENT
CREATE OR REPLACE VIEW view_PC_EYYS_INSTALLATION_EV AS
(SELECT BOA.pc.NAME AS PC_NAME,
BOA.typearticle.LABEL AS TYPE,
BOA.article.AIRBUSSN AS AIRBUS_SN,
getUserName(BOA.pc.USER_ID) AS PC_IN_CHARGE,
BOA.pc.OWNER AS PC_OWNER,
BOA.pc.OWNERSIGLUM AS PC_OWNER_SIGLUM,
getName(getLocationID(BOA.pc.ID,"PC")) AS ROOM,
BOA.installation.NAME AS PLATFORM_NAME,
BOA.installation.COMMENTS AS PLATFORM_DESIGNATION,
BOA.aircraftprogram.DEFAULTVALUE AS PROGRAM,
getUserName(BOA.installation.USER_ID) AS PLATFORM_IN_CHARGE,
BOA.installation.BUSINESSSIGLUM AS PLATFORM_BUSINESS_SIGLUM
FROM BOA.pc
LEFT JOIN BOA.article ON BOA.article.ID = BOA.pc.ID
LEFT JOIN BOA.typearticle ON BOA.typearticle.ID = BOA.article.TYPEARTICLE_ID
LEFT JOIN BOA.installation ON BOA.installation.ID = getMasterContainerID(BOA.article.ID, "PC")
LEFT JOIN BOA.aircraftprogram ON BOA.aircraftprogram.ID = BOA.installation.AIRCRAFTPROGRAM_ID
WHERE BOA.pc.OWNERSIGLUM LIKE "EYYS%" AND BOA.installation.BUSINESSSIGLUM LIKE "EV%"
);