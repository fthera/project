/*
 * ------------------------------------------------------------------------
 * Class : IOExcelReaderCommunicationPort
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import com.airbus.boa.control.ArticleBean;
import com.airbus.boa.control.PCBean;
import com.airbus.boa.control.ValueListBean;
import com.airbus.boa.entity.article.Article;
import com.airbus.boa.entity.article.Board;
import com.airbus.boa.entity.article.PC;
import com.airbus.boa.entity.article.type.TypeArticle;
import com.airbus.boa.entity.history.Action;
import com.airbus.boa.entity.history.Comment;
import com.airbus.boa.entity.network.CommunicationPort;
import com.airbus.boa.entity.valuelist.pc.Network;
import com.airbus.boa.exception.ImportException;
import com.airbus.boa.io.IOConstants;
import com.airbus.boa.io.column.ColumnPort;
import com.airbus.boa.io.column.Columns;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * @author ng0057cf
 */
public class IOExcelReaderCommunicationPort extends IOExcelBaseReader {
    
    private static final String sheetname =
            IOConstants.CommunicationPortSheetName;
    
    private ArticleBean articleBean;
    private PCBean pcBean;
    
    private String login;
    
    /**
     * @param valueListBean
     * @param workbook
     * @param columns
     * @param sheetname
     */
    public IOExcelReaderCommunicationPort(Workbook workbook, Columns columns,
            ArticleBean articleBean, PCBean pcBean,
            ValueListBean valueListBean, String login) {
        super(valueListBean, workbook, columns, sheetname);
        
        if (columns == null) {
            this.columns = new ColumnPort();
        }
        this.articleBean = articleBean;
        this.pcBean = pcBean;
        this.login = login;
    }
    
    /*
     * (non-Javadoc)
     * @see com.airbus.boa.io.Reader#readLine()
     */
    @Override
    public void readLine() throws ImportException {
        
        String forArticle, asn, msn, name, ipAddress, fixedIP, networkMask,
                macAddress, networkStr, socket;
        
        forArticle = readField(row, IOConstants.FOR_ARTICLE_TITLE);
        asn = readField(row, IOConstants.AIRBUS_SN_TITLE);
        msn = readField(row, IOConstants.MANUFACTURER_SN_TITLE);
        name = readField(row, IOConstants.NAME_TITLE);
        ipAddress = readField(row, IOConstants.IP_ADDRESS_TITLE);
        fixedIP = readField(row, IOConstants.FIXED_IP_TITLE);
        networkMask = readField(row, IOConstants.NETWORK_MASK_TITLE);
        macAddress = readField(row, IOConstants.MAC_ADDRESS_TITLE);
        String comment = readField(row, IOConstants.COMMENT_PORT_TITLE);
        
        networkStr = readField(row, IOConstants.NETWORK_TITLE);
        
        Network network = null;
        if (networkStr != null) {
            network =
                    readValueList(row, networkStr, IOConstants.NETWORK_TITLE,
                            Network.class);
        }
        
        socket = readField(row, IOConstants.SOCKET_TITLE);
        
        // LE CHAMP FOR ARTICLE DOIT ETRE RENSEIGNE
        checkNotEmptyField(row, forArticle, IOConstants.FOR_ARTICLE_TITLE);
        
        // LE MSN OU LE SN DOIT ETRE RENSEIGNE
        if (asn == null && msn == null) {
            
            String msg =
                    MessageBundle
                            .getMessage(IOConstants.ASN_OR_MSN_MUST_BE_FILLED);
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        // IL NE DOIT Y AVOIR Q'UN SEUL ARTICLE TROUVE
        List<Article> articles =
                articleBean.findArticleByASNandMSN(asn, msn, true);
        if (articles.isEmpty()) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.BOARD_OR_PC_NOT_FOUND, new Object[] {
                                    asn, msn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        // Erreur s'il y a plus d'un article retourné.
        if (articles.size() > 1) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.MORE_THAN_ONE_ARTICLE_FOUND,
                            new Object[] { asn, msn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        Article article = articles.get(0);
        
        // la classe définie doit correspondre avec celle retournée par la
        // recherche.
        if (!forArticle.equals(article.getClass().getSimpleName())) {
            String msg =
                    MessageBundle.getMessageResource(IOConstants.FIELD_INVALID,
                            new Object[] { IOConstants.FOR_ARTICLE_TITLE,
                                    forArticle });
            throw new ImportException(msg, row.getRowNum(),
                    getColumnIndex(IOConstants.FOR_ARTICLE_TITLE));
        }
        
        if (!(article instanceof PC || article instanceof Board)) {
            
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.PORT_ONLY_FOR_PC_AND_BOARD,
                            new Object[] { asn });
            throw new ImportException(msg, row.getRowNum(), -1);
        }
        
        // VERIFICATION QUE LA CARTE EST UNE CARTE ETHERNET
        if (article instanceof Board) {
            
            TypeArticle typeArt = article.getTypeArticle();
            
            if ((typeArt == null)
                    || (typeArt.getLabel() == null)
                    || (!typeArt.getLabel().matches(
                            Constants.REGEX_ETHERNETBOARD_TYPE))) {
                
                String msg =
                        MessageBundle.getMessageResource(
                                IOConstants.PORT_ONLY_FOR_ETHERNET_BOARD,
                                new Object[] { asn, msn });
                throw new ImportException(msg, row.getRowNum(), -1);
            }
        }
        
        // VERIFICATION DE LA PRESENCE D'UN IDENTIFIANT
        if ((name == null) && (ipAddress == null) && (macAddress == null)) {
            String msg =
                    MessageBundle.getMessageResource(
                            IOConstants.PC_PORT_AT_LEAST_ONE_FIELD_3,
                            new Object[] { name, ipAddress, macAddress });
            throw new ImportException(msg, row.getRowNum(), -1); // toute la
                                                                 // ligne
        }
        
        CommunicationPort comPort =
                new CommunicationPort(name, ipAddress, macAddress);
        comPort.setNetwork(network);
        comPort.setMask(networkMask);
        comPort.setSocket(socket);
        comPort.setComment(comment);
        comPort.setFixedIP(fixedIP != null && fixedIP.equals("yes"));
        
        List<CommunicationPort> ports = Collections.emptyList();
        ports =
                (article instanceof PC) ? ((PC) article).getPorts()
                        : ((Board) article).getPorts();
        
        for (CommunicationPort availablePort : ports) {
            
            if (comPort.equalsOnIpMacName(availablePort)) {
                
                String msg =
                        MessageBundle
                                .getMessage(Constants.PC_PORT_ALREADY_EXISTS);
                throw new ImportException(msg, row.getRowNum(), -1); // toute la
                                                                     // ligne
            }
        }
        
        ports.add(comPort);
        
        if (article instanceof Board) {
            Board board = (Board) article;
            board =
                    articleBean.merge(board, ports,
                            getAddPortModifications(comPort, login));
            
        }
        else { // PC
        
            PC pc = (PC) article;
            pc =
                    pcBean.merge(pc, ports,
                            getAddPortModifications(comPort, login));
            
        }
        
    }
    
    private List<Action> getAddPortModifications(CommunicationPort port,
            String pLogin) {
        String msg =
                MessageBundle.getMessageDefault(Constants.Name) + ": "
                        + port.getName();
        Comment modificationComment = new Comment(msg);
        Action newModif =
                new Action(pLogin, null, Constants.AddPort, modificationComment);
        
        List<Action> modifications = new ArrayList<Action>();
        modifications.add(newModif);
        
        return modifications;
    }
    
}
