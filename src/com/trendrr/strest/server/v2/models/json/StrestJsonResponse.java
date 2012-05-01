/**
 * 
 */
package com.trendrr.strest.server.v2.models.json;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonResponse extends StrestJsonBase implements StrestResponse {

	protected static Log log = LogFactory.getLog(StrestJsonResponse.class);

	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		this.map.putWithDot("status.code", code);
		this.map.putWithDot("status.message", message);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		return this.map.getInteger("status.code");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return this.map.getString("status.message");
	}
	
	
}
