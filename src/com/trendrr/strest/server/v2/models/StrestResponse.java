/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import com.trendrr.oss.DynMap;

/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public interface StrestResponse {

	
	public void addHeader(StrestHeader.Name header, String value);
	public String getHeader(StrestHeader.Name header);
	
	
	public void setStatus(int code, String message);
	public int getStatusCode();
	public String getStatusMessage();
	

	public void setStrestProtocolVersion(String version);
	public String getStrestProtocolVersion();
	
	public void setTxnId(String id);
	public String getTxnId();
	
	public void setContent(DynMap content);
	public void setContent(String contentType, String content);
	public Object getContent();
	
	public byte[] toByteArray();
	
}
