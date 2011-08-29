/**
 * 
 */
package com.trendrr.strest.contrib.sessions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * The session persistence implementation.
 * 
 * A single instance will be used, so take care that it is threadsafe when needed.
 * 
 * @author Dustin Norlander
 * @created Jun 14, 2011
 * 
 */
public interface SessionPersistence {

	/**
	 * called exactly once.  used for any initialization that needs to happen.
	 * This is called the first time the persistence class is used (lazyinit).  
	 * 
	 * @param sessionsConfig contains everything from the sessions part of the server config file.
	 */
	public void init(DynMap sessionsConfig);
	public Map<String,Object> loadSession(String sessionId);
	public void saveSession(String sessionId, Map<String,Object> values);
	public void deleteSession(String sessionId);
}
