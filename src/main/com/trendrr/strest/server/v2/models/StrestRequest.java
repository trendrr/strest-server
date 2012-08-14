/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.strest.cheshire.Verb;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public interface StrestRequest extends StrestPacketBase{

	public StrestConnectionChannel getConnectionChannel();
	public void setConnectionChannel(StrestConnectionChannel channel);
	
	public void setMethod(StrestHeader.Method method);
	public StrestHeader.Method getMethod();
	
	public void setUri(String uri);
	public String getUri();
	
	public void setTxnAccept(StrestHeader.TxnAccept accept);
	public StrestHeader.TxnAccept getTxnAccept();
	
	public void setParams(DynMap params);
	public DynMap getParams();
}
