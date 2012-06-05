/**
 * 
 */
package com.trendrr.strest.tests;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.strest.tests.clients.EchoClient;
import com.trendrr.strest.tests.echoserver.StrestEchoServer;
import com.trendrr.strest.tests.helper.Http;


/**
 * @author Dustin Norlander
 * @created Jun 4, 2012
 * 
 */
public class ProfilingTest {

	protected static Log log = LogFactory.getLog(ProfilingTest.class);
	
	public static void main(String ...strings) throws Exception {
		StrestEchoServer server = new StrestEchoServer();
		
		server.start(false);
	}
}
