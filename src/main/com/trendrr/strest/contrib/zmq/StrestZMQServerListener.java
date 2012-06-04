/**
 * 
 */
package com.trendrr.strest.contrib.zmq;


import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestServer;
import com.trendrr.strest.server.v2.models.json.StrestJsonRequest;
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
	Timer connectionReaper = new Timer(true);
	
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
		int port= this.config.getInteger("port");
		server.listen(port, this, true);
		this.connectionReaper.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					StrestZMQChannel.reaper();
				} catch (Exception x) {
					log.error("Caught", x);
				}
			}
		}, 60*1000, 60*1000);
		System.out.println("ZMQ server started at port " + port + '.');
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.servers.ServerListenerBase#stop()
	 */
	@Override
	public void stop() {
		server.close();
		this.connectionReaper.cancel();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.zmq.server.ZMQServerMessageHandler#error(java.lang.Exception)
	 */
	@Override
	public void error(Exception e) {
		log.error("Caught", e);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.zmq.server.ZMQServerMessageHandler#incoming(com.trendrr.zmq.server.ZMQChannel, byte[])
	 */
	@Override
	public void incoming(ZMQChannel channel, byte[] bytes) {
		//create the StrestJsonRequest.
		StrestZMQChannel c = StrestZMQChannel.get(channel);
		try {
			String json = new String(bytes, "utf8");
			StrestJsonRequest request = new StrestJsonRequest(DynMap.instance(json));
			c.setLastIncoming();
			request.setConnectionChannel(c);
			this.master.getRouter().incoming(request);
		} catch (UnsupportedEncodingException e) {
			this.error(e);
			c.cleanup();//bad message
		}
	}
}
