/**
 * 
 */
package com.trendrr.strest.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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
import com.trendrr.oss.SSLContextBuilder;
import com.trendrr.strest.flash.FlashSocketPolicyServer;


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
	
	private List<ServerBootstrap> bootstraps = new ArrayList<ServerBootstrap>();
	private SSLContext sslContext = null;
	
	private DynMap config = new DynMap(); //private local config.
	
	

	public StrestServer() {
		
		
	}
	
//	/**
//	 * creates a server based on a config file.  Config file is assumed to be either 
//	 * json or yaml.  See example_config.yaml for more.
//	 * 
//	 * if more then one filename is passed, the subsequent config files will override any fields in the previous ones.
//	 * this allows you to chain config files
//	 * 
//	 * @param filename
//	 * @return
//	 * @throws Exception
//	 */
//	public static StrestServer instanceFromFile(String filename, String ...filenames) throws Exception {
//		StrestServer server = new StrestServer();
//		initFromFile(server, filename, filenames);
//		return server;
//	}
//	
//	/**
//	 * initializes the server with the passed in filenames.  
//	 * if more then one filename is passed, the subsequent config files will override any fields in the previous ones.
//	 * 
//	 * this allows you to chain config files
//	 * 
//	 * 
//	 * @param server
//	 * @param filename
//	 * @param filenames
//	 * @throws Exception
//	 */
//	public static void initFromFile(StrestServer server, String filename, String ...filenames) throws Exception {
//		DynMap conf = dynMapFromFile(filename);
//		log.warn("Config with : " + conf.toJSONString());
//		for (String f : filenames) {
//			DynMap override = dynMapFromFile(f);
//			log.warn("Overriding config: " + override);
//			conf.extend(override);
//		}
//		log.warn("Initializing with config: " + conf.toJSONString());
//		initialize(server, conf);
//	}
//	
//	private static DynMap dynMapFromFile(String filename) throws Exception {
//		if (filename.endsWith("yaml")) {
//			Yaml yaml = new Yaml();
//		    String document = FileHelper.loadString(filename);
//		    Map map = (Map) yaml.load(document);
//		    return DynMapFactory.instance(map);
//		    
//		} else if (filename.endsWith("xml")) {
//			//TODO (or not..)
//			
//		}
//
//		//assume json
//		return DynMapFactory.instanceFromFile(filename);
//		
//	}
//	/**
//	 * Initialize the server from a config DynMap. 
//	 *
//	 * See example_config.yaml for fields
//	 
//	 * 
//	 */
//	public static StrestServer instance(DynMap config) throws Exception {
//		if (config == null) {
//			throw new Exception("Config is null! unable to create server ");
//		}
//		StrestServer server = new StrestServer();
//		initialize(server, config);
//		return server;
//	}
	
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
	public static void initialize(StrestServer server, DynMap config) throws Exception {
		if (config == null) {
			throw new Exception("Config is null! unable to initialize server ");
		}
		
		server.setPort(config.get(Integer.class, "default.port", 8008));
		server.setMaxWorkerThreads(config.getInteger("threads.worker", 10));
		server.setMaxWorkerThreads(config.getInteger("threads.io",8));
		
		List<String> controllerPackages = config.getList(String.class, "controller_packages");
		if (controllerPackages != null) {
			for (String c : controllerPackages) {
				server.getRouter().addControllerPackage(c);
			}
		}
		
		List<String> filters = config.getList(String.class, "filters");
		if (filters != null) {
			server.getRouter().setFilters("default", filters);
		}
		
		DynMap ssl = config.get(DynMap.class, "ssl");
		
		if (ssl != null) {
			SSLContextBuilder builder = new SSLContextBuilder(false);
			builder.keystoreFilename(ssl.get(String.class, "keystore"));
			builder.keystorePassword(ssl.get(String.class, "keystore_password"));
			builder.certificatePassword(ssl.get(String.class, "certificate_password"));
			server.setSSLContext(builder.toSSLContext());
			server.setSslPort(ssl.get(Integer.class, "port", server.getSslPort()));
		}
		server.config = config;
		server.getRouter().setServer(server);
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
	
	public void start() {
		 // Configure the server(s).
		if (this.port != null) {
			
			ServerBootstrap  bootstrap = new ServerBootstrap(
	                new NioServerSocketChannelFactory(
	                        this.bossExecutor,
	                        this.workerExecutor));
	        // Set up the event pipeline factory.
	        bootstrap.setPipelineFactory(new StrestServerPipelineFactory(router, null));
			bootstrap.bind(new InetSocketAddress(this.port));
			this.bootstraps.add(bootstrap);
			log.warn("Listening on port: " + this.port);
		}
		if (this.sslPort != null && this.sslContext != null) {
			
			ServerBootstrap  bootstrap = new ServerBootstrap(
	                new NioServerSocketChannelFactory(
	                        this.bossExecutor,
	                        this.workerExecutor));
	        // Set up the event pipeline factory.
	        bootstrap.setPipelineFactory(new StrestServerPipelineFactory(router, sslContext));
			bootstrap.bind(new InetSocketAddress(this.sslPort));
			this.bootstraps.add(bootstrap);
			log.warn("SSL listening on port: " + this.sslPort);
		}
		
		//add the flashsocket policy server, if needed
		if (this.config.containsKey("flashsocketpolicy")) {
			FlashSocketPolicyServer f = FlashSocketPolicyServer.instance(config, bossExecutor, workerExecutor);
			this.bootstraps.add(f.getBootstrap());
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
		for (ServerBootstrap bootstrap: this.bootstraps) {
			bootstrap.releaseExternalResources();
		}
	}
}
