/**
 * 
 */
package com.trendrr.strest.tests.clients;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.networking.strest.v2.models.StrestRequest;
import com.trendrr.oss.networking.strest.v2.models.StrestHeader.Method;
import com.trendrr.oss.networking.strest.v2.models.json.StrestJsonRequest;
import com.trendrr.zmq.client.ZMQClient;
import com.trendrr.zmq.client.ZMQClientMessageHandler;


/**
 * @author Dustin Norlander
 * @created Jun 4, 2012
 * 
 */
public class ZMQEchoClient implements ZMQClientMessageHandler {

	protected static Log log = LogFactory.getLog(ZMQEchoClient.class);
	
	ZMQClient client;
	
	final Set<String> sent = Collections.synchronizedSet(new HashSet<String>());
	
	public ZMQEchoClient() {
		client = new ZMQClient("tcp://localhost:8092", this);
	}

	/**
	 * num waiting for response.
	 * @return
	 */
	public int size() {
		return sent.size();
	}
	/* (non-Javadoc)
	 * @see com.trendrr.zmq.client.ZMQClientMessageHandler#error(java.lang.Exception)
	 */
	@Override
	public void error(Exception arg0) {
		log.error("CAught", arg0);
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.zmq.client.ZMQClientMessageHandler#incoming(com.trendrr.zmq.client.ZMQClient, byte[])
	 */
	@Override
	public void incoming(ZMQClient client, byte[] message) {
		try {
			DynMap response = DynMap.instance(new String(message, "utf8"));
//			log.info("GOT RESPONSE: " + response);
			sent.remove(response.getString("content", ""));
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		
	}
	
	public void send(String message) {
		StrestRequest request = new StrestJsonRequest();
		request.setUri("echo");
		request.setMethod(Method.GET);
		DynMap params = new DynMap();
		params.put("echo", message);
		request.setParams(params);
		sent.add(message);
		client.send(request.toByteArray());
	}
	
	public void close() {
		client.close();
	}
	
}
