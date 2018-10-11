/*
 * ------------------------------------------------------------------------
 * Class : DBConstants
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view.application;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.airbus.boa.control.ApplicationBean;
import com.airbus.boa.entity.article.AirbusPN;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.entity.article.DatedComment;
import com.airbus.boa.entity.article.ManufacturerPN;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.PCSpecificity;
import com.airbus.boa.entity.article.PN;
import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Software;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.demand.Demand;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.history.FieldModification;
import com.airbus.boa.entity.linkedelement.Document;
import com.airbus.boa.entity.location.Building;
import com.airbus.boa.entity.location.ContainerOrm;
import com.airbus.boa.entity.location.Contains_Cabinet_Rack;
import com.airbus.boa.entity.location.Contains_Inst_Cabinet;
import com.airbus.boa.entity.location.Contains_Rack_Board;
import com.airbus.boa.entity.location.Contains_Tool_Article;
import com.airbus.boa.entity.location.ExternalEntity;
import com.airbus.boa.entity.location.Installation;
import com.airbus.boa.entity.location.LocationOrm;
import com.airbus.boa.entity.location.Place;
import com.airbus.boa.entity.location.Tool;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.obso.ObsolescenceData;
import com.airbus.boa.entity.reminder.Reminder;
import com.airbus.boa.entity.user.Role;
import com.airbus.boa.entity.user.StockSelection;
import com.airbus.boa.entity.user.User;
import com.airbus.boa.entity.valuelist.AircraftProgram;
import com.airbus.boa.entity.valuelist.AttributeValueList;
import com.airbus.boa.entity.valuelist.BOAParameters;
import com.airbus.boa.entity.valuelist.DepartmentInCharge;
import com.airbus.boa.entity.valuelist.Domain;
import com.airbus.boa.entity.valuelist.ExternalEntityType;
import com.airbus.boa.entity.valuelist.I18nAttributeValueList;
import com.airbus.boa.entity.valuelist.obso.ActionObso;
import com.airbus.boa.entity.valuelist.obso.AirbusStatus;
import com.airbus.boa.entity.valuelist.obso.ConsultPeriod;
import com.airbus.boa.entity.valuelist.obso.ManufacturerStatus;
import com.airbus.boa.entity.valuelist.obso.Strategy;
import com.airbus.boa.entity.valuelist.pc.BusinessAllocationPC;
import com.airbus.boa.entity.valuelist.pc.BusinessUsagePC;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.entity.valuelist.pc.ProductTypePC;

/**
 * Definition of the database columns length
 */
@ManagedBean(name = DBConstants.BEAN_NAME)
@ApplicationScoped
public class DBConstants implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ApplicationBean applicationBean;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "DBConstants";
    
    /* ACTION table */
    
    private int ACTION_AUTHOR_LENGTH;
    private int ACTION_LABEL_LENGTH;
    private int ACTION_LOGIN_LENGTH;
    
    /* ACTIONOBSO table */
    
    private int ACTIONOBSO_DEFAULTVALUE_LENGTH;
    private int ACTIONOBSO_FRENCHVALUE_LENGTH;
    
    /* AIRBUSPN table */
    
    private int AIRBUSPN_IDENTIFIER_LENGTH;
    
    /* AIRBUSSTATUS table */
    
    private int AIRBUSSTATUS_DEFAULTVALUE_LENGTH;
    private int AIRBUSSTATUS_FRENCHVALUE_LENGTH;
    
    /* AIRCRAFTPROGRAM table */
    
    private int AIRCRAFTPROGRAM_DEFAULTVALUE_LENGTH;
    
    /* ARTICLE table */
    
    private int ARTICLE_AIRBUSSN_LENGTH;
    private int ARTICLE_CMSCODE_LENGTH;
    private int ARTICLE_MANUFACTURERSN_LENGTH;
    
    /* BOA_PARAMETER table */
    
    private int BOA_PARAMETER_VALUE_LENGTH;
    
    /* BOARD table */
    
    private int BOARD_BOOTLOADER_LENGTH;
    private int BOARD_IPADDRESS_LENGTH;
    private int BOARD_MACADDRESS_LENGTH;
    private int BOARD_REVH_LENGTH;
    private int BOARD_REVS_LENGTH;
    
    /* BUILDING table */
    
    private int BUILDING_NAME_LENGTH;
    
    /* BUSINESSALLOCATIONPC table */
    
    private int BUSINESSALLOCATIONPC_DEFAULTVALUE_LENGTH;
    private int BUSINESSALLOCATIONPC_FRENCHVALUE_LENGTH;
    
    /* BUSINESSUSAGEPC table */
    
    private int BUSINESSUSAGEPC_DEFAULTVALUE_LENGTH;
    private int BUSINESSUSAGEPC_FRENCHVALUE_LENGTH;
    
    /* CABINET table */
    
    private int CABINET_DESIGNATION_LENGTH;
    
    /* COMMENT table */
    
    private int COMMENT_MESSAGE_LENGTH;
    
    /* COMMUNICATIONPORT table */
    
    private int COMMUNICATIONPORT_IPADDRESS_LENGTH;
    private int COMMUNICATIONPORT_MASK_LENGTH;
    private int COMMUNICATIONPORT_MACADDRESS_LENGTH;
    private int COMMUNICATIONPORT_NAME_LENGTH;
    private int COMMUNICATIONPORT_SOCKET_LENGTH;
    private int COMMUNICATIONPORT_COMMENT_LENGTH;
    
    /* CONSULTPERIOD table */
    
    private int CONSULTPERIOD_DEFAULTVALUE_LENGTH;
    private int CONSULTPERIOD_FRENCHVALUE_LENGTH;
    
    /* CONTAINS_CABINET_RACK table */
    
    private int CONTAINS_CABINET_RACK_RACKPOSITION_LENGTH;
    
    /* CONTAINS_INST_CABINET table */
    
    private int CONTAINS_INST_CABINET_LETTER_LENGTH;
    
    /* CONTAINS_RACK_BOARD table */
    
    private int CONTAINS_RACK_BOARD_FACE_LENGTH;
    
    /* CONTAINS_TOOL_ARTICLE table */
    
    private int CONTAINS_TOOL_ARTICLE_COMMENT_LENGTH;
    
    /* DATED_COMMENT table */
    
    private int DATED_COMMENT_LENGTH;
    
    /* DEMAND table */
    
    private int DEMAND_ADDITIONALINFORMATION_LENGTH;
    private int DEMAND_BUDGET_LENGTH;
    private int DEMAND_COMMENTS_LENGTH;
    private int DEMAND_CONTACT_LENGTH;
    private int DEMAND_DEPARTMENT_LENGTH;
    private int DEMAND_FEATURES_LENGTH;
    private int DEMAND_JUSTIFICATION_LENGTH;
    private int DEMAND_OWNER_LENGTH;
    private int DEMAND_OWNERSIGLUM_LENGTH;
    private int DEMAND_PLUGNUMBER_LENGTH;
    private int DEMAND_PROGRAM_LENGTH;
    private int DEMAND_PROJECT_LENGTH;
    private int DEMAND_REQUESTNUMBER_LENGTH;
    
    /* DEPARTMENTINCHARGE table */
    private int DEPARTMENTINCHARGE_DEFAULTVALUE_LENGTH;
    private int DEPARTMENTINCHARGE_DESCRIPTION_LENGTH;
    
    /* DOCUMENT table */
    
    private int DOCUMENT_NAME_LENGTH;
    
    /* DOMAIN table */
    
    private int DOMAIN_DEFAULTVALUE_LENGTH;
    
    /* EXTERNALENTITY table */
    
    private int EXTERNAL_ENTITY_NAME_LENGTH;
    
    /* EXTERNALENTITYTYPE table */
    
    private int EXTERNALENTITYTYPE_DEFAULTVALUE_LENGTH;
    private int EXTERNALENTITYTYPE_FRENCHVALUE_LENGTH;
    
    /* FIELDMODIFICATION table */
    
    private int FIELDMODIFICATION_AFTERVALUE_LENGTH;
    private int FIELDMODIFICATION_BEFOREVALUE_LENGTH;
    private int FIELDMODIFICATION_FIELD_LENGTH;
    
    /* INSTALLATION table */
    
    private int INSTALLATION_COMMENTS_LENGTH;
    private int INSTALLATION_NAME_LENGTH;
    private int INSTALLATION_BUSINESSSIGLUM_LENGTH;
    private int INSTALLATION_USER_LENGTH;
    
    /* LOCATION table */
    
    private int LOCATION_PRECISELOCATION_LENGTH;
    
    /* MANUFACTURERPN table */
    
    private int MANUFACTURERPN_IDENTIFIER_LENGTH;
    
    /* MANUFACTURERSTATUS table */
    
    private int MANUFACTURERSTATUS_DEFAULTVALUE_LENGTH;
    private int MANUFACTURERSTATUS_FRENCHVALUE_LENGTH;
    
    /* NETWORK table */
    
    private int NETWORK_DEFAULTVALUE_LENGTH;
    private int NETWORK_FRENCHVALUE_LENGTH;
    
    /* OBSOLESCENCEDATA table */
    
    private int OBSOLESCENCEDATA_COMMENTONSTRATEGY_LENGTH;
    private int OBSOLESCENCEDATA_PERSONINCHARGE_LENGTH;
    private int OBSOLESCENCEDATA_SUPPLIER_LENGTH;
    
    /* PC table */
    
    private int PC_ADMIN_LENGTH;
    private int PC_ASSIGNMENT_LENGTH;
    private int PC_COMMENT_LENGTH;
    private int PC_FUNCTION_LENGTH;
    private int PC_NAME_LENGTH;
    private int PC_OWNER_LENGTH;
    private int PC_OWNERSIGLUM_LENGTH;
    private int PC_PLATFORM_LENGTH;
    
    /* PC specificity table */
    private int PC_SPECIFICITY_DESC_LENGTH;
    private int PC_SPECIFICITY_CONTACT_LENGTH;
    
    /* PLACE table */
    
    private int PLACE_NAME_LENGTH;
    private int PLACE_TYPE_LENGTH;
    
    /* PRODUCTTYPEPC table */
    
    private int PRODUCTTYPEPC_DEFAULTVALUE_LENGTH;
    private int PRODUCTTYPEPC_FRENCHVALUE_LENGTH;
    
    /* RACK table */
    
    private int RACK_DESIGNATION_LENGTH;
    
    /* REMINDER table */
    
    private int REMINDER_OBJECT_LENGTH;
    
    /* ROLE table */
    private int ROLE_DEFAULTVALUE_LENGTH;
    private int ROLE_FRENCHVALUE_LENGTH;
    
    /* SOFTWARE table */
    
    private int SOFTWARE_DESCRIPTION_LENGTH;
    private int SOFTWARE_DISTRIBUTION_LENGTH;
    private int SOFTWARE_KERNEL_LENGTH;
    private int SOFTWARE_LICENCE_LENGTH;
    private int SOFTWARE_MANUFACTURER_LENGTH;
    private int SOFTWARE_NAME_LENGTH;
    
    /* STOCK_SELECTION table */
    
    private int STOCK_SELECTION_NAME_LENGTH;
    
    /* STRATEGY table */
    
    private int STRATEGY_DEFAULTVALUE_LENGTH;
    private int STRATEGY_FRENCHVALUE_LENGTH;
    
    /* SWITCH table */
    
    private int SWITCH_IPADDRESS_LENGTH;
    
    /* TOOL table */
    
    private int TOOL_DESIGNATION_LENGTH;
    private int TOOL_NAME_LENGTH;
    private int TOOL_PERSONINCHARGE_LENGTH;
    
    /* TYPEARTICLE table */
    
    private int TYPEARTICLE_LABEL_LENGTH;
    private int TYPEARTICLE_MANUFACTURER_LENGTH;
    
    /* USER table */
    
    private int USER_EMAIL_LENGTH;
    private int USER_FIRSTNAME_LENGTH;
    private int USER_LASTNAME_LENGTH;
    private int USER_LOGIN_LENGTH;
    // The User password is encrypted by MD5 algorithm and there is no need to
    // have more than 20 characters (risk of collision).
    private int USER_PASSWORD_LENGTH = 20;
    
    /* Mapped super classes non existing tables */
    
    /**
     * Maximum length of the identifier columns of the tables mapping the super
     * class PN: AIRBUSPN and MANUFACTURERPN
     */
    private int PN_IDENTIFIER_LENGTH;
    
    /**
     * Default constructor
     */
    @PostConstruct
    public void init() {
        Map<String, Map<String, Integer>> lDBMap =
                applicationBean.findDatabaseColumnsLengths();
        
        /* ------------------ ARTICLES entities ------------------- */
        
        ARTICLE_AIRBUSSN_LENGTH =
                lDBMap.get(Article.TABLE_NAME)
                        .get(Article.AIRBUSSN_COLUMN_NAME);
        ARTICLE_CMSCODE_LENGTH =
                lDBMap.get(Article.TABLE_NAME).get(Article.CMSCODE_COLUMN_NAME);
        ARTICLE_MANUFACTURERSN_LENGTH =
                lDBMap.get(Article.TABLE_NAME).get(
                        Article.MANUFACTURERSN_COLUMN_NAME);
        
        BOARD_BOOTLOADER_LENGTH =
                lDBMap.get(Board.TABLE_NAME).get(Board.BOOTLOADER_COLUMN_NAME);
        BOARD_IPADDRESS_LENGTH =
                lDBMap.get(Board.TABLE_NAME).get(Board.IPADDRESS_COLUMN_NAME);
        BOARD_MACADDRESS_LENGTH =
                lDBMap.get(Board.TABLE_NAME).get(Board.MACADDRESS_COLUMN_NAME);
        BOARD_REVH_LENGTH =
                lDBMap.get(Board.TABLE_NAME).get(Board.REVH_COLUMN_NAME);
        BOARD_REVS_LENGTH =
                lDBMap.get(Board.TABLE_NAME).get(Board.REVS_COLUMN_NAME);
        
        CABINET_DESIGNATION_LENGTH =
                lDBMap.get(Cabinet.TABLE_NAME).get(
                        Cabinet.DESIGNATION_COLUMN_NAME);
        
        RACK_DESIGNATION_LENGTH =
                lDBMap.get(Rack.TABLE_NAME).get(Rack.DESIGNATION_COLUMN_NAME);
        
        PC_ADMIN_LENGTH = lDBMap.get(PC.TABLE_NAME).get(PC.ADMIN_COLUMN_NAME);
        PC_ASSIGNMENT_LENGTH =
                lDBMap.get(PC.TABLE_NAME).get(PC.ASSIGNMENT_COLUMN_NAME);
        PC_COMMENT_LENGTH =
                lDBMap.get(PC.TABLE_NAME).get(PC.COMMENT_COLUMN_NAME);
        PC_FUNCTION_LENGTH =
                lDBMap.get(PC.TABLE_NAME).get(PC.FUNCTION_COLUMN_NAME);
        PC_NAME_LENGTH = lDBMap.get(PC.TABLE_NAME).get(PC.NAME_COLUMN_NAME);
        PC_OWNER_LENGTH = lDBMap.get(PC.TABLE_NAME).get(PC.OWNER_COLUMN_NAME);
        PC_OWNERSIGLUM_LENGTH =
                lDBMap.get(PC.TABLE_NAME).get(PC.OWNERSIGLUM_COLUMN_NAME);
        PC_PLATFORM_LENGTH =
                lDBMap.get(PC.TABLE_NAME).get(PC.PLATFORM_COLUMN_NAME);
        
        SWITCH_IPADDRESS_LENGTH =
                lDBMap.get(Switch.TABLE_NAME).get(Switch.IPADDRESS_COLUMN_NAME);
        
        COMMUNICATIONPORT_IPADDRESS_LENGTH =
                lDBMap.get(CommunicationPort.TABLE_NAME).get(
                        CommunicationPort.IPADDRESS_COLUMN_NAME);
        COMMUNICATIONPORT_MASK_LENGTH =
        		lDBMap.get(CommunicationPort.TABLE_NAME).get(
        				CommunicationPort.MASK_COLUMN_NAME);
        COMMUNICATIONPORT_MACADDRESS_LENGTH =
                lDBMap.get(CommunicationPort.TABLE_NAME).get(
                        CommunicationPort.MACADDRESS_COLUMN_NAME);
        COMMUNICATIONPORT_COMMENT_LENGTH =
                lDBMap.get(CommunicationPort.TABLE_NAME).get(
                        CommunicationPort.COMMENT_COLUMN_NAME);
        COMMUNICATIONPORT_NAME_LENGTH =
                lDBMap.get(CommunicationPort.TABLE_NAME).get(
                        CommunicationPort.NAME_COLUMN_NAME);
        COMMUNICATIONPORT_SOCKET_LENGTH =
                lDBMap.get(CommunicationPort.TABLE_NAME).get(
                        CommunicationPort.SOCKET_COLUMN_NAME);
        
        TYPEARTICLE_LABEL_LENGTH =
                lDBMap.get(TypeArticle.TABLE_NAME).get(
                        TypeArticle.LABEL_COLUMN_NAME);
        TYPEARTICLE_MANUFACTURER_LENGTH =
                lDBMap.get(TypeArticle.TABLE_NAME).get(
                        TypeArticle.MANUFACTURER_COLUMN_NAME);
        
        SOFTWARE_DESCRIPTION_LENGTH =
                lDBMap.get(Software.TABLE_NAME).get(
                        Software.DESCRIPTION_COLUMN_NAME);
        SOFTWARE_DISTRIBUTION_LENGTH =
                lDBMap.get(Software.TABLE_NAME).get(
                        Software.DISTRIBUTION_COLUMN_NAME);
        SOFTWARE_KERNEL_LENGTH =
                lDBMap.get(Software.TABLE_NAME)
                        .get(Software.KERNEL_COLUMN_NAME);
        SOFTWARE_LICENCE_LENGTH =
                lDBMap.get(Software.TABLE_NAME).get(
                        Software.LICENCE_COLUMN_NAME);
        SOFTWARE_MANUFACTURER_LENGTH =
                lDBMap.get(Software.TABLE_NAME).get(
                        Software.MANUFACTURER_COLUMN_NAME);
        SOFTWARE_NAME_LENGTH =
                lDBMap.get(Software.TABLE_NAME).get(Software.NAME_COLUMN_NAME);
        
        /* ------------------ PN entities ------------------- */
        
        AIRBUSPN_IDENTIFIER_LENGTH =
                lDBMap.get(AirbusPN.TABLE_NAME).get(PN.IDENTIFIER_COLUMN_NAME);
        
        MANUFACTURERPN_IDENTIFIER_LENGTH =
                lDBMap.get(ManufacturerPN.TABLE_NAME).get(
                        PN.IDENTIFIER_COLUMN_NAME);
        
        PN_IDENTIFIER_LENGTH =
                Math.max(AIRBUSPN_IDENTIFIER_LENGTH,
                        MANUFACTURERPN_IDENTIFIER_LENGTH);
        
        /* ------------------ LOCATION entities ------------------- */
        
        BUILDING_NAME_LENGTH =
                lDBMap.get(Building.TABLE_NAME).get(Building.NAME_COLUMN_NAME);
        
        INSTALLATION_COMMENTS_LENGTH =
                lDBMap.get(Installation.TABLE_NAME).get(
                        Installation.COMMENTS_COLUMN_NAME);
        INSTALLATION_NAME_LENGTH =
                lDBMap.get(Installation.TABLE_NAME).get(
                        Installation.NAME_COLUMN_NAME);
        INSTALLATION_BUSINESSSIGLUM_LENGTH =
                lDBMap.get(Installation.TABLE_NAME).get(
                        Installation.BUSINESSSIGLUM_COLUMN_NAME);
        INSTALLATION_USER_LENGTH =
        		lDBMap.get(Installation.TABLE_NAME).get(
        				Installation.USER_COLUMN_NAME);
        
        PLACE_NAME_LENGTH =
                lDBMap.get(Place.TABLE_NAME).get(Place.NAME_COLUMN_NAME);
        PLACE_TYPE_LENGTH =
                lDBMap.get(Place.TABLE_NAME).get(Place.TYPE_COLUMN_NAME);
        
        EXTERNAL_ENTITY_NAME_LENGTH =
                lDBMap.get(ExternalEntity.TABLE_NAME).get(
                        ExternalEntity.NAME_COLUMN_NAME);
        
        TOOL_DESIGNATION_LENGTH =
                lDBMap.get(Tool.TABLE_NAME).get(Tool.DESIGNATION_COLUMN_NAME);
        TOOL_NAME_LENGTH =
                lDBMap.get(Tool.TABLE_NAME).get(Tool.NAME_COLUMN_NAME);
        TOOL_PERSONINCHARGE_LENGTH =
                lDBMap.get(Tool.TABLE_NAME)
                        .get(Tool.PERSONINCHARGE_COLUMN_NAME);
        
        CONTAINS_CABINET_RACK_RACKPOSITION_LENGTH =
                lDBMap.get(ContainerOrm.TABLE_NAME).get(
                        Contains_Cabinet_Rack.RACKPOSITION_COLUMN_NAME);
        
        CONTAINS_INST_CABINET_LETTER_LENGTH =
                lDBMap.get(ContainerOrm.TABLE_NAME).get(
                        Contains_Inst_Cabinet.LETTER_COLUMN_NAME);
        
        CONTAINS_RACK_BOARD_FACE_LENGTH =
                lDBMap.get(ContainerOrm.TABLE_NAME).get(
                        Contains_Rack_Board.FACE_COLUMN_NAME);
        
        CONTAINS_TOOL_ARTICLE_COMMENT_LENGTH =
                lDBMap.get(ContainerOrm.TABLE_NAME).get(
                        Contains_Tool_Article.COMMENT_COLUMN_NAME);
        
        LOCATION_PRECISELOCATION_LENGTH =
                lDBMap.get(LocationOrm.TABLE_NAME).get(
                        LocationOrm.PRECISELOCATION_COLUMN_NAME);
        
        /* ------------------ HISTORY entities ------------------- */
        
        ACTION_AUTHOR_LENGTH =
                lDBMap.get(Action.TABLE_NAME).get(Action.AUTHOR_COLUMN_NAME);
        ACTION_LABEL_LENGTH =
                lDBMap.get(Action.TABLE_NAME).get(Action.LABEL_COLUMN_NAME);
        ACTION_LOGIN_LENGTH =
                lDBMap.get(Action.TABLE_NAME).get(Action.LOGIN_COLUMN_NAME);
        
        FIELDMODIFICATION_AFTERVALUE_LENGTH =
                lDBMap.get(FieldModification.TABLE_NAME).get(
                        FieldModification.AFTERVALUE_COLUMN_NAME);
        FIELDMODIFICATION_BEFOREVALUE_LENGTH =
                lDBMap.get(FieldModification.TABLE_NAME).get(
                        FieldModification.BEFOREVALUE_COLUMN_NAME);
        FIELDMODIFICATION_FIELD_LENGTH =
                lDBMap.get(FieldModification.TABLE_NAME).get(
                        FieldModification.FIELD_COLUMN_NAME);
        
        COMMENT_MESSAGE_LENGTH =
                lDBMap.get(Comment.TABLE_NAME).get(Comment.MESSAGE_COLUMN_NAME);
        
        /* ------------------ OBSOLESCENCE entities ------------------- */
        
        OBSOLESCENCEDATA_COMMENTONSTRATEGY_LENGTH =
                lDBMap.get(ObsolescenceData.TABLE_NAME).get(
                        ObsolescenceData.COMMENTONSTRATEGY_COLUMN_NAME);
        OBSOLESCENCEDATA_PERSONINCHARGE_LENGTH =
                lDBMap.get(ObsolescenceData.TABLE_NAME).get(
                        ObsolescenceData.PERSONINCHARGE_COLUMN_NAME);
        OBSOLESCENCEDATA_SUPPLIER_LENGTH =
                lDBMap.get(ObsolescenceData.TABLE_NAME).get(
                        ObsolescenceData.SUPPLIER_COLUMN_NAME);
        
        /* ------------------ USER entities ------------------- */
        
        USER_EMAIL_LENGTH =
                lDBMap.get(User.TABLE_NAME).get(User.EMAIL_COLUMN_NAME);
        USER_FIRSTNAME_LENGTH =
                lDBMap.get(User.TABLE_NAME).get(User.FIRSTNAME_COLUMN_NAME);
        USER_LASTNAME_LENGTH =
                lDBMap.get(User.TABLE_NAME).get(User.LASTNAME_COLUMN_NAME);
        USER_LOGIN_LENGTH =
                lDBMap.get(User.TABLE_NAME).get(User.LOGIN_COLUMN_NAME);
        
        /* ------------------ DEMAND entities ------------------- */
        
        DEMAND_ADDITIONALINFORMATION_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(
                        Demand.ADDITIONALINFORMATION_COLUMN_NAME);
        DEMAND_BUDGET_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.BUDGET_COLUMN_NAME);
        DEMAND_COMMENTS_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.COMMENTS_COLUMN_NAME);
        DEMAND_CONTACT_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.CONTACT_COLUMN_NAME);
        DEMAND_DEPARTMENT_LENGTH =
                lDBMap.get(Demand.TABLE_NAME)
                        .get(Demand.DEPARTMENT_COLUMN_NAME);
        DEMAND_FEATURES_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.FEATURES_COLUMN_NAME);
        DEMAND_JUSTIFICATION_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(
                        Demand.JUSTIFICATION_COLUMN_NAME);
        DEMAND_OWNER_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.OWNER_COLUMN_NAME);
        DEMAND_OWNERSIGLUM_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(
                        Demand.OWNERSIGLUM_COLUMN_NAME);
        DEMAND_PLUGNUMBER_LENGTH =
                lDBMap.get(Demand.TABLE_NAME)
                        .get(Demand.PLUGNUMBER_COLUMN_NAME);
        DEMAND_PROGRAM_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.PROGRAM_COLUMN_NAME);
        DEMAND_PROJECT_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(Demand.PROJECT_COLUMN_NAME);
        DEMAND_REQUESTNUMBER_LENGTH =
                lDBMap.get(Demand.TABLE_NAME).get(
                        Demand.REQUESTNUMBER_COLUMN_NAME);
        
        /* ------------------ REMINDER entities ------------------- */
        
        REMINDER_OBJECT_LENGTH =
                lDBMap.get(Reminder.TABLE_NAME)
                        .get(Reminder.OBJECT_COLUMN_NAME);
        
        /* ------------------ DOCUMENT entities ------------------- */
        
        DOCUMENT_NAME_LENGTH =
                lDBMap.get(Document.TABLE_NAME).get(Document.NAME_COLUMN_NAME);
        
        /* ------------------ BOA PARAMETER entities ------------------- */
        
        BOA_PARAMETER_VALUE_LENGTH = lDBMap.get(BOAParameters.TABLE_NAME)
                .get(BOAParameters.VALUE_COLUMN_NAME);
        
        /* --------------- ATTRIBUTE VALUE LIST entities ---------------- */
        
        AIRCRAFTPROGRAM_DEFAULTVALUE_LENGTH =
                lDBMap.get(AircraftProgram.TABLE_NAME)
                        .get(AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        
        DEPARTMENTINCHARGE_DEFAULTVALUE_LENGTH =
                lDBMap.get(DepartmentInCharge.TABLE_NAME)
                        .get(AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        DEPARTMENTINCHARGE_DESCRIPTION_LENGTH =
                lDBMap.get(DepartmentInCharge.TABLE_NAME)
                        .get(DepartmentInCharge.DESCRIPTION_COLUMN_NAME);
        
        DOMAIN_DEFAULTVALUE_LENGTH = lDBMap.get(Domain.TABLE_NAME)
                .get(AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        
        EXTERNALENTITYTYPE_DEFAULTVALUE_LENGTH =
                lDBMap.get(ExternalEntityType.TABLE_NAME)
                        .get(AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        EXTERNALENTITYTYPE_FRENCHVALUE_LENGTH =
                lDBMap.get(ExternalEntityType.TABLE_NAME)
                        .get(I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        ACTIONOBSO_DEFAULTVALUE_LENGTH =
                lDBMap.get(ActionObso.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        ACTIONOBSO_FRENCHVALUE_LENGTH =
                lDBMap.get(ActionObso.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        AIRBUSSTATUS_DEFAULTVALUE_LENGTH =
                lDBMap.get(AirbusStatus.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        AIRBUSSTATUS_FRENCHVALUE_LENGTH =
                lDBMap.get(AirbusStatus.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        CONSULTPERIOD_DEFAULTVALUE_LENGTH =
                lDBMap.get(ConsultPeriod.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        CONSULTPERIOD_FRENCHVALUE_LENGTH =
                lDBMap.get(ConsultPeriod.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        MANUFACTURERSTATUS_DEFAULTVALUE_LENGTH =
                lDBMap.get(ManufacturerStatus.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        MANUFACTURERSTATUS_FRENCHVALUE_LENGTH =
                lDBMap.get(ManufacturerStatus.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        STRATEGY_DEFAULTVALUE_LENGTH = lDBMap.get(Strategy.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        STRATEGY_FRENCHVALUE_LENGTH = lDBMap.get(Strategy.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        BUSINESSALLOCATIONPC_DEFAULTVALUE_LENGTH =
                lDBMap.get(BusinessAllocationPC.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        BUSINESSALLOCATIONPC_FRENCHVALUE_LENGTH =
                lDBMap.get(BusinessAllocationPC.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        BUSINESSUSAGEPC_DEFAULTVALUE_LENGTH =
                lDBMap.get(BusinessUsagePC.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        BUSINESSUSAGEPC_FRENCHVALUE_LENGTH =
                lDBMap.get(BusinessUsagePC.TABLE_NAME)
                        .get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        NETWORK_DEFAULTVALUE_LENGTH =
                lDBMap.get(Network.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        NETWORK_FRENCHVALUE_LENGTH =
                lDBMap.get(Network.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        PRODUCTTYPEPC_DEFAULTVALUE_LENGTH =
                lDBMap.get(ProductTypePC.TABLE_NAME).get(
                        AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        PRODUCTTYPEPC_FRENCHVALUE_LENGTH =
                lDBMap.get(ProductTypePC.TABLE_NAME).get(
                        I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        ROLE_DEFAULTVALUE_LENGTH = lDBMap.get(Role.TABLE_NAME)
                .get(AttributeValueList.DEFAULTVALUE_COLUMN_NAME);
        ROLE_FRENCHVALUE_LENGTH = lDBMap.get(Role.TABLE_NAME)
                .get(I18nAttributeValueList.FRENCHVALUE_COLUMN_NAME);
        
        /* --------------- ATTRIBUTE VALUE LIST entities ---------------- */
        
        STOCK_SELECTION_NAME_LENGTH = lDBMap.get(StockSelection.TABLE_NAME)
                .get(StockSelection.NAME_COLUMN_NAME);
        
        PC_SPECIFICITY_DESC_LENGTH = lDBMap.get(PCSpecificity.TABLE_NAME)
                .get(PCSpecificity.DESC_COLUMN_NAME);
        PC_SPECIFICITY_CONTACT_LENGTH = lDBMap.get(PCSpecificity.TABLE_NAME)
                .get(PCSpecificity.CONTACT_COLUMN_NAME);
        
        DATED_COMMENT_LENGTH = lDBMap.get(DatedComment.TABLE_NAME)
                .get(DatedComment.COMMENT_COLUMN_NAME);
    }
    
    /**
     * @return the actionAuthorLength
     */
    public int getActionAuthorLength() {
        return ACTION_AUTHOR_LENGTH;
    }
    
    /**
     * @return the actionLabelLength
     */
    public int getActionLabelLength() {
        return ACTION_LABEL_LENGTH;
    }
    
    /**
     * @return the actionLoginLength
     */
    public int getActionLoginLength() {
        return ACTION_LOGIN_LENGTH;
    }
    
    /**
     * @return the actionobsoDefaultvalueLength
     */
    public int getActionobsoDefaultvalueLength() {
        return ACTIONOBSO_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the actionobsoFrenchvalueLength
     */
    public int getActionobsoFrenchvalueLength() {
        return ACTIONOBSO_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the airbuspnIdentifierLength
     */
    public int getAirbuspnIdentifierLength() {
        return AIRBUSPN_IDENTIFIER_LENGTH;
    }
    
    /**
     * @return the airbusstatusFrenchvalueLength
     */
    public int getAirbusstatusFrenchvalueLength() {
        return AIRBUSSTATUS_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the airbusstatusDefaultvalueLength
     */
    public int getAirbusstatusDefaultvalueLength() {
        return AIRBUSSTATUS_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the articleAirbussnLength
     */
    public int getArticleAirbussnLength() {
        return ARTICLE_AIRBUSSN_LENGTH;
    }
    
    /**
     * @return the articleCmscodeLength
     */
    public int getArticleCmscodeLength() {
        return ARTICLE_CMSCODE_LENGTH;
    }
    
    /**
     * @return the articleManufacturersnLength
     */
    public int getArticleManufacturersnLength() {
        return ARTICLE_MANUFACTURERSN_LENGTH;
    }
    
    /**
     * @return the departmentinchargeDefaultvalueLength
     */
    public int getDepartmentinchargeDefaultvalueLength() {
        return DEPARTMENTINCHARGE_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the attributevaluelistFrenchvalueLength
     */
    public int getDepartmentinchargeDescriptionLength() {
        return DEPARTMENTINCHARGE_DESCRIPTION_LENGTH;
    }
    
    /**
     * @return the consultperiodFrenchvalueLength
     */
    public int getBOAParameterValueLength() {
    	return BOA_PARAMETER_VALUE_LENGTH;
    }
    
    /**
     * @return the boardBootloaderLength
     */
    public int getBoardBootloaderLength() {
        return BOARD_BOOTLOADER_LENGTH;
    }
    
    /**
     * @return the boardIpaddressLength
     */
    public int getBoardIpaddressLength() {
        return BOARD_IPADDRESS_LENGTH;
    }
    
    /**
     * @return the boardMacaddressLength
     */
    public int getBoardMacaddressLength() {
        return BOARD_MACADDRESS_LENGTH;
    }
    
    /**
     * @return the boardRevhLength
     */
    public int getBoardRevhLength() {
        return BOARD_REVH_LENGTH;
    }
    
    /**
     * @return the boardRevsLength
     */
    public int getBoardRevsLength() {
        return BOARD_REVS_LENGTH;
    }
    
    /**
     * @return the buildingNameLength
     */
    public int getBuildingNameLength() {
        return BUILDING_NAME_LENGTH;
    }
    
    /**
     * @return the businessallocationpcDefaultvalueLength
     */
    public int getBusinessallocationpcDefaultvalueLength() {
        return BUSINESSALLOCATIONPC_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the businessallocationpcFrenchvalueLength
     */
    public int getBusinessallocationpcFrenchvalueLength() {
        return BUSINESSALLOCATIONPC_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the businessusagepcDefaultvalueLength
     */
    public int getBusinessusagepcDefaultvalueLength() {
        return BUSINESSUSAGEPC_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the businessusagepcFrenchvalueLength
     */
    public int getBusinessusagepcFrenchvalueLength() {
        return BUSINESSUSAGEPC_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the cabinetDesignationLength
     */
    public int getCabinetDesignationLength() {
        return CABINET_DESIGNATION_LENGTH;
    }
    
    /**
     * @return the commentMessageLength
     */
    public int getCommentMessageLength() {
        return COMMENT_MESSAGE_LENGTH;
    }
    
    /**
     * @return the communicationportIpaddressLength
     */
    public int getCommunicationportIpaddressLength() {
        return COMMUNICATIONPORT_IPADDRESS_LENGTH;
    }
    
    /**
     * @return the communicationportIpaddressLength
     */
    public int getCommunicationportMaskLength() {
    	return COMMUNICATIONPORT_MASK_LENGTH;
    }
    
    /**
     * @return the communicationportMacaddressLength
     */
    public int getCommunicationportMacaddressLength() {
        return COMMUNICATIONPORT_MACADDRESS_LENGTH;
    }
    
    /**
     * @return the communicationportNameLength
     */
    public int getCommunicationportNameLength() {
        return COMMUNICATIONPORT_NAME_LENGTH;
    }
    
    /**
     * @return the communicationportSocketLength
     */
    public int getCommunicationportSocketLength() {
        return COMMUNICATIONPORT_SOCKET_LENGTH;
    }
    
    /**
     * @return the communicationportCommentLength
     */
    public int getCommunicationportCommentLength() {
        return COMMUNICATIONPORT_COMMENT_LENGTH;
    }
    
    /**
     * @return the consultperiodDefaultvalueLength
     */
    public int getConsultperiodDefaultvalueLength() {
        return CONSULTPERIOD_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the consultperiodFrenchvalueLength
     */
    public int getConsultperiodFrenchvalueLength() {
        return CONSULTPERIOD_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the containsCabinetRackRackpositionLength
     */
    public int getContainsCabinetRackRackpositionLength() {
        return CONTAINS_CABINET_RACK_RACKPOSITION_LENGTH;
    }
    
    /**
     * @return the containsInstCabinetLetterLength
     */
    public int getContainsInstCabinetLetterLength() {
        return CONTAINS_INST_CABINET_LETTER_LENGTH;
    }
    
    /**
     * @return the containsRackBoardFaceLength
     */
    public int getContainsRackBoardFaceLength() {
        return CONTAINS_RACK_BOARD_FACE_LENGTH;
    }
    
    /**
     * @return the containsToolArticleCommentLength
     */
    public int getContainsToolArticleCommentLength() {
        return CONTAINS_TOOL_ARTICLE_COMMENT_LENGTH;
    }
    
    /**
     * @return the demandAdditionalinformationLength
     */
    public int getDemandAdditionalinformationLength() {
        return DEMAND_ADDITIONALINFORMATION_LENGTH;
    }
    
    /**
     * @return the demandBudgetLength
     */
    public int getDemandBudgetLength() {
        return DEMAND_BUDGET_LENGTH;
    }
    
    /**
     * @return the demandCommentsLength
     */
    public int getDemandCommentsLength() {
        return DEMAND_COMMENTS_LENGTH;
    }
    
    /**
     * @return the demandContactLength
     */
    public int getDemandContactLength() {
        return DEMAND_CONTACT_LENGTH;
    }
    
    /**
     * @return the demandDepartmentLength
     */
    public int getDemandDepartmentLength() {
        return DEMAND_DEPARTMENT_LENGTH;
    }
    
    /**
     * @return the demandFeaturesLength
     */
    public int getDemandFeaturesLength() {
        return DEMAND_FEATURES_LENGTH;
    }
    
    /**
     * @return the demandJustificationLength
     */
    public int getDemandJustificationLength() {
        return DEMAND_JUSTIFICATION_LENGTH;
    }
    
    /**
     * @return the demandOwnerLength
     */
    public int getDemandOwnerLength() {
        return DEMAND_OWNER_LENGTH;
    }
    
    /**
     * @return the demandOwnersiglumLength
     */
    public int getDemandOwnersiglumLength() {
        return DEMAND_OWNERSIGLUM_LENGTH;
    }
    
    /**
     * @return the demandPlugnumberLength
     */
    public int getDemandPlugnumberLength() {
        return DEMAND_PLUGNUMBER_LENGTH;
    }
    
    /**
     * @return the demandProjectLength
     */
    public int getDemandProjectLength() {
        return DEMAND_PROJECT_LENGTH;
    }
    
    /**
     * @return the demandProgramLength
     */
    public int getDemandProgramLength() {
        return DEMAND_PROGRAM_LENGTH;
    }
    
    /**
     * @return the demandRequestnumberLength
     */
    public int getDemandRequestnumberLength() {
        return DEMAND_REQUESTNUMBER_LENGTH;
    }
    
    /**
     * @return the documentNameLength
     */
    public int getDocumentNameLength() {
        return DOCUMENT_NAME_LENGTH;
    }
    
    /**
     * @return the externalentityNameLength
     */
    public int getExternalentityNameLength() {
        return EXTERNAL_ENTITY_NAME_LENGTH;
    }
    
    /**
     * @return the externalentitytypeDefaultvalueLength
     */
    public int getExternalentitytypeDefaultvalueLength() {
        return EXTERNALENTITYTYPE_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the externalentitytypeFrenchvalueLength
     */
    public int getExternalentitytypeFrenchvalueLength() {
        return EXTERNALENTITYTYPE_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the fieldmodificationAftervalueLength
     */
    public int getFieldmodificationAftervalueLength() {
        return FIELDMODIFICATION_AFTERVALUE_LENGTH;
    }
    
    /**
     * @return the fieldmodificationBeforevalueLength
     */
    public int getFieldmodificationBeforevalueLength() {
        return FIELDMODIFICATION_BEFOREVALUE_LENGTH;
    }
    
    /**
     * @return the fieldmodificationFieldLength
     */
    public int getFieldmodificationFieldLength() {
        return FIELDMODIFICATION_FIELD_LENGTH;
    }
    
    /**
     * @return the installationCommentsLength
     */
    public int getInstallationCommentsLength() {
        return INSTALLATION_COMMENTS_LENGTH;
    }
    
    /**
     * @return the installationNameLength
     */
    public int getInstallationNameLength() {
        return INSTALLATION_NAME_LENGTH;
    }

    
    /**
     * @return the installationBusinesssiglumLength
     */
    public int getInstallationBusinesssiglumLength() {
        return INSTALLATION_BUSINESSSIGLUM_LENGTH;
    }
    
    /**
     * @return the installationUserLength
     */
    public int getInstallationUserLength() {
        return INSTALLATION_USER_LENGTH;
    }
    
    /**
     * @return the LocationPreciselocationLength
     */
    public int getLocationPreciselocationLength() {
        return LOCATION_PRECISELOCATION_LENGTH;
    }
    
    /**
     * @return the manufacturerpnIdentifierLength
     */
    public int getManufacturerpnIdentifierLength() {
        return MANUFACTURERPN_IDENTIFIER_LENGTH;
    }
    
    /**
     * @return the manufacturerstatusDefaultvalueLength
     */
    public int getManufacturerstatusDefaultvalueLength() {
        return MANUFACTURERSTATUS_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the manufacturerstatusFrenchvalueLength
     */
    public int getManufacturerstatusFrenchvalueLength() {
        return MANUFACTURERSTATUS_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the networkDefaultvalueLength
     */
    public int getNetworkDefaultvalueLength() {
        return NETWORK_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the networkFrenchvalueLength
     */
    public int getNetworkFrenchvalueLength() {
        return NETWORK_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the obsolescencedataCommentonstrategyLength
     */
    public int getObsolescencedataCommentonstrategyLength() {
        return OBSOLESCENCEDATA_COMMENTONSTRATEGY_LENGTH;
    }
    
    /**
     * @return the obsolescencedataPersoninchargeLength
     */
    public int getObsolescencedataPersoninchargeLength() {
        return OBSOLESCENCEDATA_PERSONINCHARGE_LENGTH;
    }
    
    /**
     * @return the obsolescencedataSupplierLength
     */
    public int getObsolescencedataSupplierLength() {
        return OBSOLESCENCEDATA_SUPPLIER_LENGTH;
    }
    
    /**
     * @return the pcAdminLength
     */
    public int getPcAdminLength() {
        return PC_ADMIN_LENGTH;
    }
    
    /**
     * @return the pcAssignmentLength
     */
    public int getPcAssignmentLength() {
        return PC_ASSIGNMENT_LENGTH;
    }
    
    /**
     * @return the pcCommentLength
     */
    public int getPcCommentLength() {
        return PC_COMMENT_LENGTH;
    }
    
    /**
     * @return the pcFunctionLength
     */
    public int getPcFunctionLength() {
        return PC_FUNCTION_LENGTH;
    }
    
    /**
     * @return the pcNameLength
     */
    public int getPcNameLength() {
        return PC_NAME_LENGTH;
    }
    
    /**
     * @return the pcOwnerLength
     */
    public int getPcOwnerLength() {
        return PC_OWNER_LENGTH;
    }
    
    /**
     * @return the pcOwnersiglumLength
     */
    public int getPcOwnersiglumLength() {
        return PC_OWNERSIGLUM_LENGTH;
    }
    
    /**
     * @return the pcPlatformLength
     */
    public int getPcPlatformLength() {
        return PC_PLATFORM_LENGTH;
    }
    
    /**
     * @return the placeNameLength
     */
    public int getPlaceNameLength() {
        return PLACE_NAME_LENGTH;
    }
    
    /**
     * @return the placeTypeLength
     */
    public int getPlaceTypeLength() {
        return PLACE_TYPE_LENGTH;
    }
    
    /**
     * @return the pnIdentifierLength
     */
    public int getPnIdentifierLength() {
        return PN_IDENTIFIER_LENGTH;
    }
    
    /**
     * @return the producttypepcDefaultvalueLength
     */
    public int getProducttypepcDefaultvalueLength() {
        return PRODUCTTYPEPC_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the producttypepcFrenchvalueLength
     */
    public int getProducttypepcFrenchvalueLength() {
        return PRODUCTTYPEPC_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the rackDesignationLength
     */
    public int getRackDesignationLength() {
        return RACK_DESIGNATION_LENGTH;
    }
    
    /**
     * @return the reminderObjectLength
     */
    public int getReminderObjectLength() {
        return REMINDER_OBJECT_LENGTH;
    }
    
    /**
     * @return the softwareDescriptionLength
     */
    public int getSoftwareDescriptionLength() {
        return SOFTWARE_DESCRIPTION_LENGTH;
    }
    
    /**
     * @return the softwareDistributionLength
     */
    public int getSoftwareDistributionLength() {
        return SOFTWARE_DISTRIBUTION_LENGTH;
    }
    
    /**
     * @return the softwareKernelLength
     */
    public int getSoftwareKernelLength() {
        return SOFTWARE_KERNEL_LENGTH;
    }
    
    /**
     * @return the softwareLicenceLength
     */
    public int getSoftwareLicenceLength() {
        return SOFTWARE_LICENCE_LENGTH;
    }
    
    /**
     * @return the softwareManufacturerLength
     */
    public int getSoftwareManufacturerLength() {
        return SOFTWARE_MANUFACTURER_LENGTH;
    }
    
    /**
     * @return the softwareNameLength
     */
    public int getSoftwareNameLength() {
        return SOFTWARE_NAME_LENGTH;
    }
    
    /**
     * @return the stockSelectionNameLength
     */
    public int getStockSelectionNameLength() {
        return STOCK_SELECTION_NAME_LENGTH;
    }
    
    /**
     * @return the strategyDefaultvalueLength
     */
    public int getStrategyDefaultvalueLength() {
        return STRATEGY_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the strategyFrenchvalueLength
     */
    public int getStrategyFrenchvalueLength() {
        return STRATEGY_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the switchIpaddressLength
     */
    public int getSwitchIpaddressLength() {
        return SWITCH_IPADDRESS_LENGTH;
    }
    
    /**
     * @return the toolDesignationLength
     */
    public int getToolDesignationLength() {
        return TOOL_DESIGNATION_LENGTH;
    }
    
    /**
     * @return the toolNameLength
     */
    public int getToolNameLength() {
        return TOOL_NAME_LENGTH;
    }
    
    /**
     * @return the toolPersoninchargeLength
     */
    public int getToolPersoninchargeLength() {
        return TOOL_PERSONINCHARGE_LENGTH;
    }
    
    /**
     * @return the typearticleLabelLength
     */
    public int getTypearticleLabelLength() {
        return TYPEARTICLE_LABEL_LENGTH;
    }
    
    /**
     * @return the typearticleManufacturerLength
     */
    public int getTypearticleManufacturerLength() {
        return TYPEARTICLE_MANUFACTURER_LENGTH;
    }
    
    /**
     * @return the userEmailLength
     */
    public int getUserEmailLength() {
        return USER_EMAIL_LENGTH;
    }
    
    /**
     * @return the userFirstnameLength
     */
    public int getUserFirstnameLength() {
        return USER_FIRSTNAME_LENGTH;
    }
    
    /**
     * @return the userLastnameLength
     */
    public int getUserLastnameLength() {
        return USER_LASTNAME_LENGTH;
    }
    
    /**
     * @return the userLoginLength
     */
    public int getUserLoginLength() {
        return USER_LOGIN_LENGTH;
    }
    
    /**
     * @return the userPasswordLength
     */
    public int getUserPasswordLength() {
        return USER_PASSWORD_LENGTH;
    }
    
    /**
     * @return the pcSpecificityDescLength
     */
    public int getPcSpecificityDescLength() {
        return PC_SPECIFICITY_DESC_LENGTH;
    }
    
    /**
     * @return the pcSpecificityContactLength
     */
    public int getPcSpecificityContactLength() {
        return PC_SPECIFICITY_CONTACT_LENGTH;
    }
    
    /**
     * @return the pcSpecificityContactLength
     */
    public int getDatedCommentLength() {
        return DATED_COMMENT_LENGTH;
    }
    
    /**
     * @return the roleDefaultvalueLength
     */
    public int getRoleDefaultvalueLength() {
        return ROLE_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the roleFrenchvalueLength
     */
    public int getRoleFrenchvalueLength() {
        return ROLE_FRENCHVALUE_LENGTH;
    }
    
    /**
     * @return the aircraftprogramDefaultvalueLength
     */
    public int getAircraftprogramDefaultvalueLength() {
        return AIRCRAFTPROGRAM_DEFAULTVALUE_LENGTH;
    }
    
    /**
     * @return the domainDefaultvalueLength
     */
    public int getDomainDefaultvalueLength() {
        return DOMAIN_DEFAULTVALUE_LENGTH;
    }
    
}
