/**
 * 
 */
package com.trendrr.strest.contrib.sessions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Jun 14, 2011
 * 
 */
public class DefaultSessionPersistence implements SessionPersistence {

	protected Log log = LogFactory.getLog(DefaultSessionPersistence.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.contrib.sessions.SessionPersistence#loadSession(java.lang.String)
	 */
	@Override
	public Map<String, Object> loadSession(String sessionId) {
		log.info("Loading session (not really): " + sessionId);
		return new DynMap();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.contrib.sessions.SessionPersistence#saveSession(java.lang.String, java.util.Map)
	 */
	@Override
	public void saveSession(String sessionId, Map<String, Object> values) {
		log.info("Saving session (not really though) : " + sessionId + " " + values);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.contrib.sessions.SessionPersistence#deleteSession(java.lang.String)
	 */
	@Override
	public void deleteSession(String sessionId) {
		log.info("Deleting session (not really though) : " + sessionId);
		
	}
}
