/*
 * ------------------------------------------------------------------------
 * Class : DownloadSupport
 * Copyright 2012 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

/**
 * @author ng0057cf
 */
public interface DownloadSupport {
    
    public void download(Downloadable exportFile, final String filename,
            final String contentType);
}
