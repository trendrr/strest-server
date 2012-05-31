/**
 * 
 */
package com.trendrr.strest.tests.echoserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.strest.StrestServerBuilder;
import com.trendrr.strest.server.StrestServer;


/**
 * @author Dustin Norlander
 * @created May 31, 2012
 * 
 */
public class StrestEchoServer {

	protected static Log log = LogFactory.getLog(StrestEchoServer.class);
	
	
	StrestServer server;
	
	public static void main(String ...args) throws Exception {
		new StrestEchoServer().start();
	}
	
	public void start() throws Exception {
		server = new StrestServerBuilder()
			.addControllerPackage("com.trendrr.strest.tests.echoserver")
			.addListenerHttp(8090)
			.addListenerJson(8091)
			.build();
		server.start();
	}
	
	public void stop() {
		server.shutdown();
	}
	
}
