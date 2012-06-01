/**
 * 
 */
package com.trendrr.strest.tests.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Jun 1, 2012
 * 
 */
public class Http {

	protected static Log log = LogFactory.getLog(Http.class);
	
	public static String get(String url) throws IOException {
		return get(url, null);
	}
	
	/**
	 * does a get request
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException 
	 */
	public static String get(String url, DynMap params) throws IOException {
		if (!url.contains("?")) {
			url += "?";
		}
		if (params != null) {
			url += params.toURLString();
		}
		
		URL u = new URL(url);
        URLConnection c = u.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                c.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
        	response.append(inputLine);
        }
        in.close();
		return response.toString();
	}
	
}
