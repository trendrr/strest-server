/**
 * 
 */
package com.trendrr.strest.tests;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.strest.tests.clients.EchoClient;
import com.trendrr.strest.tests.clients.ZMQEchoClient;
import com.trendrr.strest.tests.echoserver.StrestEchoServer;
import com.trendrr.strest.tests.helper.Http;


/**
 * @author Dustin Norlander
 * @created Jun 4, 2012
 * 
 */
public class ZMQTests {

	protected static Log log = LogFactory.getLog(ZMQTests.class);
	
	@BeforeClass
    public static void setUpClass() throws Exception {
		EchoTests.setUpClass();
    }
	@AfterClass
    public static void tearDownClass() throws Exception {
		EchoTests.tearDownClass();
    }
	
	@Test
	public void strestTest() throws Exception {
		//warm up.
		ZMQEchoClient client = new ZMQEchoClient();
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
}
