/**
 * 
 */
package com.trendrr.strest.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.yaml.snakeyaml.Yaml;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.SSLContextBuilder;
import com.trendrr.strest.flash.FlashSocketPolicyServer;
import com.trendrr.strest.server.v2.servers.ServerListenerBase;
import com.trendrr.strest.server.v2.servers.StrestHttpServerListener;
import com.trendrr.strest.server.v2.servers.StrestJsonServerListener;


/**
 * @author Dustin Norlander
 * @created Feb 15, 2011
 * 
 */
public class StrestServer {

	protected static Log log = LogFactory.getLog(StrestServer.class);
	
	
	private StrestRouter router = new StrestRouter();
	private Integer port = 8008;
	private Integer sslPort = 8009;


	private Executor bossExecutor = Executors.newCachedThreadPool();
	private Executor workerExecutor = Executors.newCachedThreadPool();
	
	protected HashMap<String, ServerListenerBase> listeners = new HashMap<String, ServerListenerBase>();
	protected HashMap<String, Class<? extends ServerListenerBase>> listenerClasses = new HashMap<String, Class<? extends ServerListenerBase>>();
	
	private SSLContext sslContext = null;
	
	private DynMap config = new DynMap(); //private local config.
	
	

	public StrestServer() {
		//hard coded listener classes..
		this.listenerClasses.put("http", StrestHttpServerListener.class);
		this.listenerClasses.put("json", StrestJsonServerListener.class);
	}

	public void registerListenerClass(String name, Class cls) {
		this.listenerClasses.put(name, cls);
	}
	/**
	 * initializes the passed in server.  Use this if you need to override the 
	 * StrestServer or set a new Router, ect.  
	 * 
	 * In other cases you should use the the StrestServer.instance methods
	 * 
	 * @param server
	 * @param config
	 * @throws Exception
	 */
	public void initialize(DynMap config) throws Exception {
		if (config == null) {
			throw new Exception("Config is null! unable to initialize server ");
		}
		
		this.setMaxWorkerThreads(config.getInteger("threads.worker", 10));
		this.setMaxIOThreads(config.getInteger("threads.io",8));
		
		List<String> controllerPackages = config.getList(String.class, "controller_packages");
		if (controllerPackages != null) {
			for (String c : controllerPackages) {
				this.getRouter().addControllerPackage(c);
			}
		}
		
		List<String> filters = config.getList(String.class, "filters");
		if (filters != null) {
			this.getRouter().setFilters("default", filters);
		}

		this.getRouter().setServer(this);
		
		//now initialize the listeners.
		DynMap listeners = config.getMap("listeners", new DynMap());
		for (String name : listeners.keySet()) {
			DynMap listenerConfig = listeners.getMap(name, new DynMap());
			ServerListenerBase listener = null;
			if (listenerConfig.getString("classname") != null) {
				listener = Reflection.instance(ServerListenerBase.class, listenerConfig.getString("classname"), this, listenerConfig);
			} else {
				Class<? extends ServerListenerBase> cls = this.listenerClasses.get(name);
				if (cls == null) {
					log.warn("No listener class found for " + name + ", skipping");
					continue;
				}
				log.warn("creating: " + cls);
				listener = Reflection.instance(cls, this, listenerConfig);
			}
			if (listener == null) {
				log.warn("No listener class found for " + name + ", skipping");
				continue;
			}
			
			this.listeners.put(name, listener);
		}
		
	}
	
	public StrestRouter getRouter() {
		return router;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer defaultPort) {
		this.port = defaultPort;
	}

	public Integer getSslPort() {
		return sslPort;
	}

	public void setSslPort(Integer sslPort) {
		this.sslPort = sslPort;
	}

	public void setRouter(StrestRouter router) {
		this.router = router;
		this.router.setServer(this);
	}


	public SSLContext getSSLContext() {
		return sslContext;
	}


	/**
	 * sets the ssl context.  If you would like this server to run in SSL mode
	 * provide this.
	 * 
	 * Recommended to create via the SSLContextBuilder provided.
	 * 
	 * @param sslContext
	 */
	public void setSSLContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}


	public Executor getWorkerExecutor() {
		return workerExecutor;
	}


	public void setWorkerExecutor(Executor workerExecutor) {
		this.workerExecutor = workerExecutor;
	}

	/**
	 * will set the workerExecutor to a fixed pool with maxThreads number of threads.
	 * 
	 * -1 or null will set to unlimited.
	 * 
	 * @param maxThreads
	 */
	public void setMaxWorkerThreads(Integer maxThreads) {
		if (maxThreads == null || maxThreads < 1) {
			this.workerExecutor = Executors.newCachedThreadPool();
		} else {
			log.info("Setting max worker threads: " + maxThreads);
			this.workerExecutor = Executors.newFixedThreadPool(maxThreads);
		}
	}
	
	/**
	 * will set the bossExecutor to a fixed pool with maxThreads number of threads.
	 * 
	 * -1 or null will set to unlimited.
	 * 
	 * @param maxThreads
	 */
	public void setMaxIOThreads(Integer maxThreads) {
		if (maxThreads == null || maxThreads < 1) {
			this.bossExecutor = Executors.newCachedThreadPool();
		} else {
			log.info("Setting max IO threads: " + maxThreads);
			this.bossExecutor = Executors.newFixedThreadPool(maxThreads);
		}
	}
	
	/**
	 * starts all the listeners.
	 */
	public void start() {
		if (this.listeners.isEmpty()) {
			log.warn("No listeners configured.  Goodbye");
			System.exit(1);
		}
		for (ServerListenerBase listener : this.listeners.values()) {
			listener.start(bossExecutor, workerExecutor);
		}
    }
	
	/**
	 * The original config file.  Is typically parsed from a yaml file.
	 * @return
	 */
	public DynMap getConfig() {
		return config;
	}
	public void shutdown() {
		for (ServerListenerBase listener : this.listeners.values()) {
			listener.stop();
		}
	}
}
