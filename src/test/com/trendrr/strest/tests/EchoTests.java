/**
 * 
 */
package com.trendrr.strest.tests;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.strest.tests.echoserver.EchoClient;
import com.trendrr.strest.tests.echoserver.StrestEchoServer;
import com.trendrr.strest.tests.helper.Http;


/**
 * @author Dustin Norlander
 * @created Jun 1, 2012
 * 
 */
public class EchoTests {

	protected static Log log = LogFactory.getLog(EchoTests.class);
	
//	@Test
	public void speedTest() throws Exception {
		StrestEchoServer server = this.server();
		
		Date start = new Date();
		int num = 1000;
		for (int i=0; i < num; i++) {
			String response = Http.get("http://localhost:8090/echo?echo=request"+ i);
		}
		
		System.out.println("COMPLETED: " + num + " in " + (new Date().getTime()-start.getTime()));
		
		server.stop();
	}
	
	@Test
	public void strestTest() throws Exception {
		StrestEchoServer server = this.server();
		
		EchoClient client = new EchoClient();
		for (int i=0; i < 10; i++) {
			client.send("message" + i);
		}
		Sleep.seconds(5);
	}

	/**
	 * make sure connections don't leak.
	 * @throws Exception
	 */
//	@Test
	public void connectionSizeTest() throws Exception {
		StrestEchoServer server = this.server();
		
		int num = 100;
		for (int i=0; i < num; i++) {
			String response = Http.get("http://localhost:8090/echo?echo=request"+ i);
		}
		//a few 404 for good measure
		for (int i=0; i < 4; i++) {
			try {String response = Http.get("http://localhost:8090/random404"+ i); }
			catch (Exception x) {/*swallow 404 exception*/}
		}
		
		Assert.assertEquals(0, server.getServer().getRouter().getNumConnections());
		server.stop();
	}
	
	
	public StrestEchoServer server() throws Exception {
		StrestEchoServer server = new StrestEchoServer();
		
		server.start(true);
		Sleep.seconds(3);
		//warm up.
		System.out.println("******* WARMING UP THE SERVER ***********");
		for (int i=0; i < 10; i++) {
			String response = Http.get("http://localhost:8090/echo?echo=warmup"+ i);
		}
		System.out.println("******* DONE WARMING UP THE SERVER ***********");
				
		return server;
	}
}
