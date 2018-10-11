/*
 * ------------------------------------------------------------------------
 * Class : Downloadable
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ng0057cf
 */
public interface Downloadable {
    
    public void write(OutputStream os) throws IOException;
    
}
