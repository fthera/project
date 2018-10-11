/*
 * ------------------------------------------------------------------------
 * Class : StreamGobbler
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class allow to manage over-buffered stream. It is used on
 * InputStream and ErrorStream of Runtime processes.
 * 
 * @author ng15d2d@SII
 */
public class StreamGobbler extends Thread {
	
	InputStream is;
	String trace = "";

    /**
     * Class constructor.
     */
    public StreamGobbler(InputStream pIs) {
        this.is = pIs;
	}

    /**
     * Read the stream and store it in the trace attribute.
     */
	@Override
	public void run() {
		try {
            InputStreamReader lISReader = new InputStreamReader(is);
            BufferedReader lBReader = new BufferedReader(lISReader);
			String line = null;
            while ((line = lBReader.readLine()) != null) {
				trace += line + "\n";
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

    /**
     * Retrieve the content of the read stream.
     * 
     * @return the content of the read stream
     */
	public String getTrace() {
		return trace;
	}
}
