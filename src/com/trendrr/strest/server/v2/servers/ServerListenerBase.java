/**
 * 
 */
package com.trendrr.strest.server.v2.servers;

import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestRouter;
import com.trendrr.strest.server.StrestServer;


/**
 * @author Dustin Norlander
 * @created May 7, 2012
 * 
 */
public abstract class ServerListenerBase {

	protected static Log log = LogFactory.getLog(ServerListenerBase.class);
	
	protected DynMap config = new DynMap();
	protected StrestServer master;
	
	
	
	
	public ServerListenerBase(StrestServer master, DynMap config) {
		this.config = config;
		this.master = master;
	}
	
	public abstract String getName();
	
	public abstract void start(Executor bossExecutor, Executor workerExecutor);
	
	public abstract void stop();
}
