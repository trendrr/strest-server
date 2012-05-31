/**
 * 
 */
package com.trendrr.strest.server.v2.servers;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestRouter;
import com.trendrr.strest.server.StrestServer;
import com.trendrr.strest.server.v2.StrestJsonServerPipelineFactory;


/**
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class StrestJsonServerListener extends ServerListenerBase{

	/**
	 * @param master
	 * @param config
	 */
	public StrestJsonServerListener(StrestServer master, DynMap config) {
		super(master, config);
	}

	protected static Log log = LogFactory.getLog(StrestJsonServerListener.class);
	
	protected ServerBootstrap bootstrap;
	
	
	
	
	public void start(Executor bossExecutor, Executor workerExecutor) {
		 // Configure the server.
		 this.bootstrap = new ServerBootstrap(
	                new NioServerSocketChannelFactory(
	                		bossExecutor,
	                		workerExecutor));
		   
		 // Set up the event pipeline factory.
		 bootstrap.setPipelineFactory(new StrestJsonServerPipelineFactory(this.master.getRouter(), null));
		 int port = this.config.getInteger("port", 8009);
		 // Bind and start to accept incoming connections.
		 bootstrap.bind(new InetSocketAddress(port));
		  
		 System.out.println("STREST server json started at port " + port + '.');
	 }

	
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#getName()
	 */
	@Override
	public String getName() {
		return "strest-json";
	}



	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#stop()
	 */
	@Override
	public void stop() {
		this.bootstrap.releaseExternalResources();
		
	}
	
	
}
