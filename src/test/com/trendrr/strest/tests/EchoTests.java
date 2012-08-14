/**
 * 
 */
package com.trendrr.strest.tests;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.strest.tests.clients.EchoClient;
import com.trendrr.strest.tests.echoserver.StrestEchoServer;
import com.trendrr.strest.tests.helper.Http;


/**
 * @author Dustin Norlander
 * @created Jun 1, 2012
 * 
 */
public class EchoTests {

	protected static Log log = LogFactory.getLog(EchoTests.class);
	protected static StrestEchoServer server;
	protected static boolean startServer = true;
	
	@BeforeClass
    public static void setUpClass() throws Exception {
		if (startServer) {
			server = new StrestEchoServer();
			
			server.start(true);
			Sleep.seconds(3);
			//warm up.
			System.out.println("******* WARMING UP THE SERVER ***********");
			for (int i=0; i < 10; i++) {
				String response = Http.get("http://localhost:8090/echo?echo=warmup"+ i);
			}
			System.out.println("******* DONE WARMING UP THE SERVER ***********");  
		}
    }
	@AfterClass
    public static void tearDownClass() throws Exception {
		if (server != null) {
			server.stop();
		}
    }
	
	
//	@Test
	public void speedTest() throws Exception {
		
		Date start = new Date();
		int num = 1000;
		for (int i=0; i < num; i++) {
			String response = Http.get("http://localhost:8090/echo?echo=request"+ i);
		}
		
		System.out.println("COMPLETED: " + num + " in " + (new Date().getTime()-start.getTime()));
	}
	
	@Test
	public void strestSpeedTest() throws Exception {
//		Completed 1000000 in 128114 millis
		
		EchoClient client = new EchoClient();
		client.send("warmup message");
		
		Date start = new Date();
		int num = 1000000;
		for (int i=0; i < num; i++) {
			client.send("message" + i);
			if (i % 10000 == 0) {
				System.out.println("Completed " + i + " in " + (new Date().getTime()-start.getTime()) + " millis");
			}
		}
		
		while(client.size() > 0) {
			Sleep.millis(10); //busy waiting, baaad
		}
		System.out.println("Completed " + num + " in " + (new Date().getTime()-start.getTime()) + " millis");
		client.close();
	}

	/**
	 * make sure connections don't leak.
	 * @throws Exception
	 */
	@Test
	public void connectionSizeTest() throws Exception {
		
		System.out.println("CONNECTIONS: " + server.getServer().getRouter().getNumConnections());
		int num = 100;
		for (int i=0; i < num; i++) {
			String response = Http.get("http://localhost:8090/echo?echo=request"+ i);
		}
		//a few 404 for good measure
		for (int i=0; i < 4; i++) {
			try {String response = Http.get("http://localhost:8090/random404"+ i); }
			catch (Exception x) {/*swallow 404 exception*/}
		}
		Sleep.seconds(2);
		Assert.assertEquals(0, server.getServer().getRouter().getNumConnections());
	}

}
