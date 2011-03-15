/**
 * 
 */
package com.trendrr.strest.server.routing;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jan 14, 2011
 * 
 */
public class MatchedRoute {

	protected Log log = LogFactory.getLog(MatchedRoute.class);
	
	UriMapping mapping;
	Map<String,String> params = new HashMap<String,String>();
	
	public UriMapping getMapping() {
		return mapping;
	}
	public void setMapping(UriMapping mapping) {
		this.mapping = mapping;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	
	public String toString() {
		return mapping.toString() + "\n" + params;
	}
	
}
