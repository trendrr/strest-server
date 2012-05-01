/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.networking.cheshire.Verb;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public interface StrestRequest {

	
	public void addHeader(StrestHeader.Name header, String value);
	public String getHeader(StrestHeader.Name header);
	
	public void setMethod(StrestHeader.Method method);
	public StrestHeader.Method getMethod();
	
	public void setUri(String uri);
	public String getUri();
	

	public void setStrestProtocolVersion(String version);
	public String getStrestProtocolVersion();
	
	public void setTxnId(String id);
	public String getTxnId();
	
	public void setTxnAccept(StrestHeader.TxnAccept accept);
	public StrestHeader.TxnAccept getTxnAccept();
	
	public void setParams(DynMap params);
	public DynMap getParams();
	
	public void setContent(DynMap content);
	public void setContent(String contentType, String content);
	public Object getContent();
	
	public byte[] toByteArray();
}
