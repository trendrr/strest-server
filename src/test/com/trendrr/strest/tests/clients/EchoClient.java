/**
 * 
 */
package com.trendrr.strest.tests.clients;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.networking.strest.v2.StrestClient;
import com.trendrr.oss.networking.strest.v2.StrestRequestCallback;
import com.trendrr.oss.networking.strest.v2.models.StrestRequest;
import com.trendrr.oss.networking.strest.v2.models.StrestResponse;
import com.trendrr.oss.networking.strest.v2.models.StrestHeader.Method;
import com.trendrr.oss.networking.strest.v2.models.json.StrestJsonRequest;


/**
 * @author Dustin Norlander
 * @created Jun 1, 2012
 * 
 */
public class EchoClient {

	protected static Log log = LogFactory.getLog(EchoClient.class);
	
	StrestClient client = new StrestClient("localhost", 8091);
	final Set<String> sent = Collections.synchronizedSet(new HashSet<String>());
//	AtomicInteger received = new AtomicInteger(0);
	StrestRequestCallback callback = new StrestRequestCallback() {
		
		@Override
		public void txnComplete(String txnId) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void response(StrestResponse response) {
//			log.info("GOT RESPONSE: " + response);
			if (response.getContent() != null) {
				sent.remove(response.getContent().toString());
			}
		}
		
		@Override
		public void error(Throwable x) {
			log.error("Caught", x);
		}
	};
	public EchoClient() throws IOException {
		client.setMaxQueuedWrites(10000);
		client.setWaitOnMaxQueuedWrites(true);
		client.connect();
		
	}
	
	public void close() {
		client.close();
	}
	public int size() {
		return this.sent.size();
	}
	
	public void send(String message) {
		StrestRequest request = new StrestJsonRequest();
		request.setUri("echo");
		request.setMethod(Method.GET);
		DynMap params = new DynMap();
		params.put("echo", message);
		request.setParams(params);
		sent.add(message);
		client.sendRequest(request, callback);
	}
}
