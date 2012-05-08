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
import com.trendrr.strest.server.StrestServer;
import com.trendrr.strest.server.StrestServerPipelineFactory;


/**
 * @author Dustin Norlander
 * @created May 7, 2012
 * 
 */
public class StrestHttpServerListener extends ServerListenerBase {

	/**
	 * @param master
	 * @param config
	 */
	public StrestHttpServerListener(StrestServer master, DynMap config) {
		super(master, config);
	}

	protected static Log log = LogFactory
			.getLog(StrestHttpServerListener.class);

	protected ServerBootstrap bootstrap;
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#getName()
	 */
	@Override
	public String getName() {
		return "http";
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#start(java.util.concurrent.Executor, java.util.concurrent.Executor)
	 */
	@Override
	public void start(Executor bossExecutor, Executor workerExecutor) {
		this.bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        bossExecutor,
                        workerExecutor));
        // Set up the event pipeline factory.
		 int port = this.config.getInteger("port", 8010);
		 
        bootstrap.setPipelineFactory(new StrestServerPipelineFactory(this.master.getRouter(), null));
		bootstrap.bind(new InetSocketAddress(port));
		System.out.println("HTTP server started at port " + port + '.');

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#stop()
	 */
	@Override
	public void stop() {
		this.bootstrap.releaseExternalResources();
		
	}
}
