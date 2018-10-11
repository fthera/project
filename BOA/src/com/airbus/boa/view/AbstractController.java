/*
 * ------------------------------------------------------------------------
 * Class : AbstractController
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.airbus.boa.io.DownloadSupport;
import com.airbus.boa.io.Downloadable;
import com.airbus.boa.service.Constants;
import com.airbus.boa.util.ExceptionUtil;
import com.airbus.boa.util.StringUtil;

/**
 * Abstract controller to be extended by controllers managing web pages
 */
public abstract class AbstractController implements Serializable,
        DownloadSupport {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The logger to use for logging traces of code execution
     */
    protected static Logger log = Logger.getLogger("Controller");
    
    /**
     * Return a managed bean defined into Java Server Faces.
     * The bean shall have a public static final attribute BEAN_NAME
     * containing its name.
     * 
     * @param <T>
     *            the class of the searched managed bean
     * @param beanClass
     *            managed bean class
     * @return the managed bean if it exists
     */
    public static <T> T findBean(Class<T> beanClass) {
        
        FacesContext context = FacesContext.getCurrentInstance();
        
        Object object = null;
        try {
            String beanName =
                    (String) beanClass.getField("BEAN_NAME").get(null);
            object =
                    context.getApplication().evaluateExpressionGet(context,
                            "#{" + beanName + "}",
                            beanClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return beanClass.cast(object);
    }
    
    /**
     * Create a new UIViewRoot with the current one id, set it to the Faces
     * Context current instance View Root and then render response.
     */
    public static void reset() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot =
                viewHandler.createView(context, context.getViewRoot()
                        .getViewId());
        context.setViewRoot(viewRoot);
        context.renderResponse();
    }
    
    /**
     * @return the Date format for display in String form
     */
    public static String getDateFormat() {
        return Constants.DATE_FORMAT;
    }
    
    /**
     * Return the real path (i.e. full path) of the Faces Context current
     * instance External Context. <br>
     * If the provided relative path is empty or null, then return null. <br>
     * If calling the method retrieving the real path cannot be called, then
     * return null.
     * 
     * @param relativePath
     *            the relative from which to retrieve the full path
     * @return the full path corresponding to the provided relative path. Or
     *         null.
     */
    public final static String getFullPath(String relativePath) {
        
        if (StringUtil.isEmptyOrNull(relativePath)) {
            return null;
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            ExternalContext ec = context.getExternalContext();
            if (ec != null) {
                return ec.getRealPath(relativePath);
            }
        }
        return null;
    }
    
    /**
     * Return the initialization parameter corresponding to the provided name of
     * the Faces Context current instance External Context. <br>
     * If the provided name is null, return null.
     * 
     * @param name
     *            the initialization parameter name
     * @return the initialization parameter value
     */
    public final static String getInitParameter(String name) {
        
        if (name == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String parameter = fc.getExternalContext().getInitParameter(name);
        return parameter;
    }
    
    /**
     * Retrieve the request parameter of type Long corresponding to the provided
     * parameter name.
     * 
     * @param param
     *            the parameter name to retrieve
     * @return the Long value of the parameter value
     */
    protected static Long getParamId(String param) {
        
        ExternalContext ec =
                FacesContext.getCurrentInstance().getExternalContext();
        
        Map<String, String> map = ec.getRequestParameterMap();
        
        String result = map.get(param);
        
        return Long.valueOf(result);
    }
    
    /**
     * @return the HTTP Servlet Request of the Faces Context current instance
     *         External Context
     */
    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }
    
    /**
     * @return the HTTP Servlet Response of the Faces Context current instance
     *         External Context
     */
    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
    }
    
    /**
     * @return the HTTP session of the Faces Context current instance External
     *         Context
     */
    public static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(true);
    }
    
    @Override
    public void download(Downloadable exportFile, final String filename,
            final String contentType) {
        
        HttpServletResponse response = getResponse();
        response.setContentType(contentType);
        response.setHeader("content-disposition", "attachment;filename="
                + filename);
        
        ServletOutputStream sos;
        try {
            sos = response.getOutputStream();
            
            exportFile.write(sos);
            try {
                sos.flush();
            }
            finally {
                sos.close();
            }
        }
        catch (IOException e) {
            Utils.addFacesMessage(null, ExceptionUtil.getMessage(e));
        }
        FacesContext.getCurrentInstance().responseComplete();
    }
    
    /**
     * Propose to download the file
     * 
     * @param pData
     *            the file data
     * @param pFileName
     *            the file name
     */
    public void download(final byte[] pData, final String pFileName) {
        
        HttpServletResponse lResponse = getResponse();
        lResponse.setContentType(FacesContext.getCurrentInstance()
                .getExternalContext().getMimeType(pFileName));
        lResponse.setHeader("content-disposition", "attachment;filename="
                + pFileName);
        
        try {
            OutputStream lOutputStream = lResponse.getOutputStream();
            lOutputStream.write(pData);
            lOutputStream.close();
            try {
                lOutputStream.flush();
            }
            finally {
                lOutputStream.close();
            }
        }
        catch (IOException e) {
            Utils.addFacesMessage(null, ExceptionUtil.getMessage(e));
        }
        FacesContext.getCurrentInstance().responseComplete();
    }
    
    /**
     * @return the Iso3Language of the locale found by getLocale method
     */
    public String getIso3Langage() {
        return getLocale().getISO3Language();
    }
    
    /**
     * Return the locale of the Faces Context current instance View Root, if it
     * exists. <br>
     * Else return null.
     * 
     * @return the found locale, else null
     */
    public Locale getLocale() {
        Locale locale = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        }
        return locale;
    }
    
    /**
     * @return the Iso3Language of the current locale from localeController
     */
    public String getCurrentLocale() {
        
        LocaleController localCtrl = findBean(LocaleController.class);
        return localCtrl.getISO3Langage();
    }
    
}
