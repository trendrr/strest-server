/**
 * 
 */
package com.trendrr.strest.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * @author dustin
 *
 */
public interface StrestCallback {
	
	public void messageRecieved(HttpResponse response);
	
	public void txnComplete();
	
	public void error(Throwable x);
	
}
