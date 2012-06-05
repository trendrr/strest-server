/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public interface StrestPacketBase {

	public void addHeader(String header, String value);
	public void addHeader(StrestHeader.Name header, String value);
	public String getHeader(StrestHeader.Name header);
	public String getHeader(String header);
//	public Collection<String> getHeaderNames();
	
	
	public void setProtocol(String protocolName, float version);
	public float getProtocolVersion();
	public String getProtocolName();
	
	public void setTxnId(String id);
	public String getTxnId();
	
	public void setContent(DynMap content);
	public void setContent(String contentType, byte[] bytes);
	public void setContent(String contentType, String utf8Str);
	
	
	public Object getContent();
	
	/**
	 * this packet is done with, clean up anything that needs it.
	 */
	public void cleanup();
	
	public byte[] toByteArray();
}
