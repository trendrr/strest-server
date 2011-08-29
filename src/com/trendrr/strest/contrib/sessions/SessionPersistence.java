/**
 * 
 */
package com.trendrr.strest.contrib.sessions;

import java.util.Date;
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
	/**
	 * load a session
	 * 
	 * Null is treated as an empty session.
	 * 
	 * @param sessionId
	 * @return
	 */
	public Map<String,Object> loadSession(String sessionId);
	
	/**
	 * Saves a session.
	 * 
	 * By default the expires time will be rolling, i.e. each request will push back the expiration time.
	 * @param sessionId
	 * @param values
	 * @param expires
	 */
	public void saveSession(String sessionId, Map<String,Object> values, Date expires);
	
	/**
	 * deletes a session.
	 * @param sessionId
	 */
	public void deleteSession(String sessionId);
}
