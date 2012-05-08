/**
 * 
 */
package com.trendrr.strest.server.v2;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestRouter;


/**
 * @author Dustin Norlander
 * @created May 7, 2012
 * 
 */
public class StrestHttpServer {

	protected static Log log = LogFactory.getLog(StrestHttpServer.class);
	
	private int port = 8000;
	
	private DynMap config = new DynMap();
	protected StrestRouter router = new StrestRouter();
		
	public StrestHttpServer(StrestRouter router, DynMap config) {
		this.config = config;
		this.router = router;
	}
	
	public void start(Executor bossExecutor, Executor workerExecutor) {
		 // Configure the server.
		 ServerBootstrap bootstrap = new ServerBootstrap(
	                new NioServerSocketChannelFactory(
	                		bossExecutor,
	                		workerExecutor));
		   
		 // Set up the event pipeline factory.
		 bootstrap.setPipelineFactory(new StrestJsonServerPipelineFactory(router, null));
		   
		 // Bind and start to accept incoming connections.
		 bootstrap.bind(new InetSocketAddress(port));
		  
		 System.out.println("STREST server json started at port " + port + '.');
	 }
	
}
