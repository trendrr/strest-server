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
	ServerBootstrap bootstrap;
	
	protected void init(DynMap config) {
		 // Configure HTTP Ping server.
		Executor ex = Executors.newCachedThreadPool();
        this.bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        ex,
                        ex));

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
        
        bootstrap.bind(new InetSocketAddress(port));
		
        System.out.println("Flash Policy Server Ready and listening on port " + port);
		
	}
	
	/**
	 * creates an instance of the flashbridge server based on the configuration file.
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public static FlashSocketPolicyServer instance(DynMap config) {
		FlashSocketPolicyServer server = new FlashSocketPolicyServer();
		server.init(config);
		return server;
	}
}
