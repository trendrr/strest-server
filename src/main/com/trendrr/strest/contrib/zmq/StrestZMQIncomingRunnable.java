/**
 * 
 */
package com.trendrr.strest.contrib.zmq;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestServer;
import com.trendrr.strest.server.v2.models.json.StrestJsonRequest;
import com.trendrr.zmq.server.ZMQChannel;


/**
 * @author Dustin Norlander
 * @created Aug 7, 2012
 * 
 */
public class StrestZMQIncomingRunnable implements Runnable {

	protected static Log log = LogFactory
			.getLog(StrestZMQIncomingRunnable.class);
	StrestServer server = null;
	StrestZMQChannel channel = null;
	byte[] requestBytes = null;
	
	public StrestZMQIncomingRunnable(StrestServer server, StrestZMQChannel channel, byte[] requestBytes) {
		this.server = server;
		this.channel = channel;
		this.requestBytes = requestBytes;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			String json = new String(requestBytes, "utf8");
			StrestJsonRequest request = new StrestJsonRequest(DynMap.instance(json));
			channel.setLastIncoming();
			request.setConnectionChannel(channel);
			this.server.getRouter().incoming(request);
		} catch (UnsupportedEncodingException e) {
			channel.cleanup();//bad message
		}
	}
}
