/*
 * ------------------------------------------------------------------------
 * Class : MailUtil
 * Copyright 2015 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

/**
 * Class managing sending of e-mails
 */
public class MailUtil {
    
    /**
     * Send an e-mail with the provided elements
     * 
     * @param pFrom
     *            the From mail address
     * @param pTo
     *            the To mail address
     * @param pSubject
     *            the subject of the e-mail
     * @param pHtmlContent
     *            the HTML content of the e-mail
     * @throws MessagingException
     *             when an error occurs during sending or preparing the e-mail
     */
    public static void sendMail(String pFrom, String pTo, String pSubject,
            StringBuffer pHtmlContent) throws MessagingException {
        
        String lAddress =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getInitParameter("ReminderURLMailer");
        try {
            URL lURL = new URL(lAddress);
            
            HttpURLConnection lConnnection =
                    (HttpURLConnection) lURL.openConnection();
            String lURLParameters =
                    "content_type=html"
                            + "&from="
                            + pFrom
                            + "&to="
                            + pTo
                            + "&subject="
                            + URLEncoder.encode(pSubject, "UTF-8")
                            + "&body="
                            + URLEncoder.encode(pHtmlContent.toString(),
                                    "UTF-8");
            lConnnection.setRequestMethod("POST");
            lConnnection.setDoOutput(true);
            DataOutputStream lOutput =
                    new DataOutputStream(lConnnection.getOutputStream());
            lOutput.writeBytes(lURLParameters);
            lOutput.flush();
            lOutput.close();
            
            BufferedReader lInput =
                    new BufferedReader(new InputStreamReader(
                            lConnnection.getInputStream()));
            String lInputLine;
            StringBuffer lResponse = new StringBuffer();
            
            while ((lInputLine = lInput.readLine()) != null) {
                lResponse.append(lInputLine);
            }
            lInput.close();
            
            if (("e-mail not sent :( ...").equals(lResponse)) {
                throw new MessagingException("Unable to send email");
            }
            
        }
        catch (MalformedURLException e) {
            throw new MessagingException(
                    "Unable to send email (MalformedURLException)");
        }
        catch (IOException e) {
            throw new MessagingException("Unable to send email (IOException)");
        }
    }
    
}
