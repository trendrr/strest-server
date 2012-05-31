/**
 * 
 */
package com.trendrr.strest.flash;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.trendrr.oss.DynMap;


/**
 * 
 * This server handles falsh socket policy requests.  This allows you to use the flash bridge as a websocket connection for
 * browsers that do not support websockets. 
 * 
 * see: http://www.lightsphere.com/dev/articles/flash_socket_policy.html
 * 
 * Please note that on linux this must be run as root.  
 * or you can forward via IPTABLES:
 * $ iptables -t nat -A PREROUTING -p tcp --dport 843 -j REDIRECT --to-port 8430
 * 
 * 
 * @author Dustin Norlander
 * @created Apr 14, 2011
 * 
 */
public class FlashSocketPolicyServer {

	protected Log log = LogFactory.getLog(FlashSocketPolicyServer.class);
	
	public static void main(String ...strings) {
		FlashSocketPolicyServer.instance(new DynMap());
		
	}
	private ServerBootstrap bootstrap;
	
	public ServerBootstrap getBootstrap() {
		return this.bootstrap;
	}
	
	protected void init(DynMap config, Executor bossExecutor, Executor workerExecutor) {
		 // Configure HTTP Ping server.
		
        this.bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                		bossExecutor,
                		workerExecutor));

        final DynMap cfg = config;
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				// Create a default pipeline implementation.
		        ChannelPipeline pipeline = pipeline();

		   
		        pipeline.addLast("handler", new SocketPolicyHandler(cfg));
		        return pipeline;
			}
		});

        // Bind and start to accept incoming connections.
        int port = config.getInteger("flashsocketpolicy.port", 843);
        try {
        	bootstrap.bind(new InetSocketAddress(port));
        } catch (org.jboss.netty.channel.ChannelException x) {
        	if (x.getCause() instanceof java.net.SocketException) {
        		if (x.getCause().getMessage().equalsIgnoreCase("Permission denied")) {
        			log.warn("!!! Unable to connect the Flash Policy server                 !!!");
        			log.warn("*** Often ports below 1024 are only available to admins       ***");
        			log.warn("*** trying running as sudo or enable internal port forwarding ***");
        			log.warn("!!! Skipping Flash Policy Server                              !!!");
        			return;
        		}
        	}
        	throw x;
        }
        System.out.println("Flash Policy Server Ready and listening on port " + port);
		
	}
	
	/**
	 * creates an instance of the flashbridge server based on the configuration file.
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public static FlashSocketPolicyServer instance(DynMap config, Executor bossExecutor, Executor workerExecutor) {
		FlashSocketPolicyServer server = new FlashSocketPolicyServer();
		if (bossExecutor == null ) {
			bossExecutor = Executors.newCachedThreadPool();
		} 
		if (workerExecutor == null) {
			workerExecutor = Executors.newCachedThreadPool();
		}
		server.init(config, bossExecutor, workerExecutor);
		return server;
	}
	
	public static FlashSocketPolicyServer instance(DynMap config) {
		return instance(config, null, null);
	}
}
