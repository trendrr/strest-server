/**
 * 
 */
package com.trendrr.strest.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Called when a specific connection is disconnected.
 * 
 * use connection.onDisconnect to register the callback.
 * 
 * @author Dustin Norlander
 * @created Feb 16, 2011
 * 
 */
public interface DisconnectCallback {

	public void disconnected(StrestConnection connection);
}
