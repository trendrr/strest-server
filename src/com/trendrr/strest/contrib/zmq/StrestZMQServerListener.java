/**
 * 
 */
package com.trendrr.strest.contrib.zmq;


import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestServer;
import com.trendrr.strest.server.v2.servers.ServerListenerBase;
import com.trendrr.zmq.server.ZMQChannel;
import com.trendrr.zmq.server.ZMQServer;
import com.trendrr.zmq.server.ZMQServerMessageHandler;


/**
 * @author Dustin Norlander
 * @created May 30, 2012
 * 
 */
public class StrestZMQServerListener extends ServerListenerBase implements ZMQServerMessageHandler {
	
	ZMQServer server;
	/**
	 * @param master
	 * @param config
	 */
	public StrestZMQServerListener(StrestServer master, DynMap config) {
		super(master, config);
	}

	protected static Log log = LogFactory.getLog(StrestZMQServerListener.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#getName()
	 */
	@Override
	public String getName() {
		return "zmq";
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#start(java.util.concurrent.Executor, java.util.concurrent.Executor)
	 */
	@Override
	public void start(Executor bossExecutor, Executor workerExecutor) {
		this.server = new ZMQServer();
		server.listen(this.config.getInteger("port"), this, true);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#stop()
	 */
	@Override
	public void stop() {
		server.close();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.zmq.server.ZMQServerMessageHandler#error(java.lang.Exception)
	 */
	@Override
	public void error(Exception arg0) {
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.zmq.server.ZMQServerMessageHandler#incoming(com.trendrr.zmq.server.ZMQChannel, byte[])
	 */
	@Override
	public void incoming(ZMQChannel channel, byte[] arg1) {
		//create the StrestJsonRequest.
		
		
		
	}
}
