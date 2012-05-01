/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonResponse implements StrestResponse {

	protected static Log log = LogFactory.getLog(StrestJsonResponse.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#addHeader(com.trendrr.strest.server.v2.models.StrestHeader.Name, java.lang.String)
	 */
	@Override
	public void addHeader(Name header, String value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getHeader(com.trendrr.strest.server.v2.models.StrestHeader.Name)
	 */
	@Override
	public String getHeader(Name header) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStrestProtocolVersion(java.lang.String)
	 */
	@Override
	public void setStrestProtocolVersion(String version) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStrestProtocolVersion()
	 */
	@Override
	public String getStrestProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setTxnId(java.lang.String)
	 */
	@Override
	public void setTxnId(String id) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getTxnId()
	 */
	@Override
	public String getTxnId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setContent(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setContent(DynMap content) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setContent(java.lang.String, java.lang.String)
	 */
	@Override
	public void setContent(String contentType, String content) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getContent()
	 */
	@Override
	public Object getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
