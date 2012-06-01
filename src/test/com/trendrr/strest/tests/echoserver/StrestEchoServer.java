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
public class StrestEchoServer implements Runnable {

	protected static Log log = LogFactory.getLog(StrestEchoServer.class);
	
	
	StrestServer server;
	
	public static void main(String ...args) throws Exception {
		new StrestEchoServer().start(false);
	}
	
	/**
	 * if threaded will return immediately and server starts in a separate thread.
	 * @param threaded
	 * @throws Exception
	 */
	public void start(boolean threaded) throws Exception {
		
		server = new StrestServerBuilder()
			.addControllerPackage("com.trendrr.strest.tests.echoserver")
			.addListenerHttp(8090)
			.addListenerJson(8091)
			.build();
		if (threaded) {
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		} else {
			this.run();
		}
	}
	
	public void stop() {
		server.shutdown();
	}

	public StrestServer getServer() {
		return this.server;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		server.start();
	}
	
}
