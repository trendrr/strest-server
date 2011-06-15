/**
 * 
 */
package com.trendrr.strest.contrib.sessions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jun 14, 2011
 * 
 */
public interface SessionPersistence {

	public Map<String,Object> loadSession(String sessionId);
	public void saveSession(String sessionId, Map<String,Object> values);
	public void deleteSession(String sessionId);
}
