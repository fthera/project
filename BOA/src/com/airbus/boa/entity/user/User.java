/*
 * ------------------------------------------------------------------------
 * Class : User
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.entity.user;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.airbus.boa.exception.ValidationException;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.MessageBundle;

/**
 * Entity implementation class for Entity: User
 */
@Entity
@Table(name = User.TABLE_NAME)
@NamedQueries({
        @NamedQuery(name = "findUserByLogin",
                query = "SELECT u FROM User u WHERE u.login = :login"),
        @NamedQuery(name = "AllUsers", query = "SELECT DISTINCT u FROM User u"
                + " ORDER BY u.lastname") })
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The table name */
    public static final String TABLE_NAME = "user";
    /** The email column name */
    public static final String EMAIL_COLUMN_NAME = "EMAIL";
    /** The first name column name */
    public static final String FIRSTNAME_COLUMN_NAME = "FIRSTNAME";
    /** The last name column name */
    public static final String LASTNAME_COLUMN_NAME = "LASTNAME";
    /** The login column name */
    public static final String LOGIN_COLUMN_NAME = "LOGIN";
    /** The password column name */
    public static final String PASSWORD_COLUMN_NAME = "PASSWORD";
    
    /**
     * Compute the encrypted password (MD5)
     * 
     * @param pPassword
     *            the password to encrypt
     * @return the encrypted password
     */
    public static String encryptPassword(String pPassword) {
        try {
            // Apply the MD5 algorithm
            MessageDigest lMD = MessageDigest.getInstance("MD5");
            lMD.update(pPassword.getBytes());
            
            byte[] lBytePassword = lMD.digest();
            
            // Convert the bytes array into an hexadecimal string
            StringBuffer lHexPassword = new StringBuffer();
            for (int i = 0; i < lBytePassword.length; i++) {
                
                String lHex = Integer.toHexString(lBytePassword[i] & 0xFF);
                
                if (lHex.length() == 1) {
                    lHexPassword.append('0');
                }
                lHexPassword.append(lHex);
            }
            return lHexPassword.toString();
            
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = FIRSTNAME_COLUMN_NAME)
    private String firstname;
    
    @Column(name = LASTNAME_COLUMN_NAME)
    private String lastname;
    
    @Column(name = EMAIL_COLUMN_NAME)
    private String email;
    
    @Column(unique = true, name = LOGIN_COLUMN_NAME)
    private String login;
    
    @Column(name = PASSWORD_COLUMN_NAME)
    private String password;
    
    @Column
    private Boolean receiveBOAMail;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoggedIn;
    
    @ManyToOne
    @JoinColumn(name = "ROLE_ID")
    private Role role;
    
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<StockSelection> stockSelections =
            new ArrayList<StockSelection>();
    
    @Version
    private Long version;
    
    /**
     * Default constructor
     */
    public User() {
        super();
        role = null;
        receiveBOAMail = true;
    }
    
    /**
     * Constructor<br>
     * The user has no role and no password.
     * 
     * @param login
     *            the user login
     * @param firstname
     *            the user first name
     * @param lastname
     *            the user last name
     * @param email
     *            the user email
     */
    public User(String login, String firstname, String lastname, String email) {
        super();
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.login = login;
        role = null;
        receiveBOAMail = true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        }
        else if (!login.equals(other.login)) {
            return false;
        }
        return true;
    }
    
    /**
     * Compare the provided password to the user password.<br>
     * If it does not match, a ValidationException is thrown.
     * 
     * @param pPassword
     *            the password to compare (in plain text form, not encrypted)
     */
    public void matchPassword(String pPassword) {
        
        if (pPassword == null || pPassword.isEmpty()) {
            throw new ValidationException(
                    MessageBundle.getMessage(Constants.INVALID_PASSWORD));
        }
        
        if (!password.equals(encryptPassword(pPassword))) {
            throw new ValidationException(
                    MessageBundle
                            .getMessage(Constants.LOGIN_PASSWORD_NOT_MATCH));
        }
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the firstname
     */
    public String getFirstname() {
        return firstname;
    }
    
    /**
     * @param firstname
     *            the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }
    
    /**
     * @param lastname
     *            the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * @return the User name in following form: LASTNAME Firstname (login)
     */
    public String getLoginDetails() {
        String lCombine = lastname + " " + firstname + " " + "(" + login + ")";
        return lCombine;
    }
    
    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * @param pRole
     *            the role to set
     */
    public void setRole(Role pRole) {
        role = pRole;
    }
    
    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }
    
    /**
     * @param login
     *            the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }
    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Encrypt the password and set it
     * 
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return the receiveBOAMail
     */
    public Boolean getReceiveBOAMail() {
        return receiveBOAMail;
    }
    
    /**
     * @param pReceiveBOAMail
     *            the receiveBOAMail to set
     */
    public void setReceiveBOAMail(Boolean pReceiveBOAMail) {
        receiveBOAMail = pReceiveBOAMail;
    }
    
    /**
     * @return the lastLoggedIn date
     */
    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }
    
    /**
     * @param pLastLoggedIn
     *            the lastLoggedIn date to set
     */
    public void setLastLoggedIn(Date pLastLoggedIn) {
    	lastLoggedIn = pLastLoggedIn;
    }
    
    /**
     * @return the stockSelections
     */
    public List<StockSelection> getStockSelections() {
        return stockSelections;
    }

    /**
     * @param pStockSelections
     *            the stockSelections to set
     */
    public void setStockSelection(List<StockSelection> pStockSelections) {
        stockSelections = pStockSelections;
    }
    
}
