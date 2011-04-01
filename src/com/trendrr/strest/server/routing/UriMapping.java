/**
 * 
 */
package com.trendrr.strest.server.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;


/**
 * @author Dustin Norlander
 * @created Jan 14, 2011
 * 
 */
public class UriMapping {

	protected Log log = LogFactory.getLog(UriMapping.class);
	
	final String route;
	final Class cls;
	final List<String> tokens;
	
	private HashMap<Integer, String> wildcards = new HashMap<Integer, String>();

	
	public UriMapping(String route, Class cls) {
		this.route = StringHelper.trim(route, "/");
		this.cls = cls;
		this.tokens = new ArrayList<String>();
		
		int index = 0;
		for (String t : this.route.split("\\/")) {
			if (t.startsWith(":") || t.startsWith("*")) {
				wildcards.put(index, t);
			}
			this.tokens.add(t);
			index++;
		}
	}
	
	public List<String> getTokens() {
		return tokens;
	}

	public String getRoute() {
		return route;
	}
	
	public Class getCls() {
		return cls;
	}
	
	public String toString() {
		return route + " => " + cls;
	}
	
	/**
	 * returns the named wildcard mappings.
	 * 
	 * tokens is the *already* matched uri.
	 * 
	 * 
	 * @param tokens
	 * @return
	 */
	Map<String, String> getWildCardMatches(List<String> tokens) {
		Map<String,String> mp = new HashMap<String,String>();
		if (this.wildcards.isEmpty()) {
			return mp;
		}
		for (Integer ind : this.wildcards.keySet()) {
			String var = this.wildcards.get(ind);
			if (var.startsWith(":")) {
				mp.put(var.substring(1), tokens.get(ind));
			} else if (var.startsWith("*")) {
				if (var.length() > 1) {
					String dir = StringHelper.join(tokens.subList(ind, tokens.size()), "/");
					mp.put(var.substring(1), dir);
				}
				return mp;
			}
		}
		return mp;
	}
}
