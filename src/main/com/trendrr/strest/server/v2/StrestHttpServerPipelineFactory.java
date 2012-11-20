/**
 * 
 */
package com.trendrr.strest.server.v2;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;

import com.trendrr.strest.server.StrestRouter;


/**
 * @author Dustin Norlander
 * @created Nov 20, 2012
 * 
 */
public class StrestHttpServerPipelineFactory implements ChannelPipelineFactory {

	protected static Log log = LogFactory
			.getLog(StrestHttpServerPipelineFactory.class);
	
	protected StrestRouter router ;
	
	protected SSLContext sslContext = null;
	
	protected ExecutionHandler handler;
	
	/**
	 * creates a new pipeline factory (non-ssl)
	 * @param router
	 */
	public StrestHttpServerPipelineFactory(StrestRouter router) {
		this(router, null);
		
	}
	
	/**
	 * create a new pipeline factory.  if an sslContext is provided then this
	 * will be an ssl connection.
	 * @param router
	 * @param sslContext
	 */
	public StrestHttpServerPipelineFactory(StrestRouter router, SSLContext sslContext) {
		this.router = router;
		this.sslContext = sslContext;
		this.handler = new ExecutionHandler(router.getServer().getWorkerExecutor());
//	             new MemoryAwareThreadPoolExecutor(
//	            		 router.getServer().getConfig().getInteger("threads.worker", 16), 1048576, 1048576)
//	    );
	}
	
    public ChannelPipeline getPipeline() throws Exception {
    	
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        
        if (this.sslContext != null) {
	        SSLEngine engine = this.sslContext.createSSLEngine();
	        engine.setUseClientMode(false);
	        pipeline.addLast("ssl", new SslHandler(engine));
        }
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
//        pipeline.addLast("deflater", new StrestResponseEncoder());
        
        pipeline.addLast("executionHandler", handler);
        pipeline.addLast("handler", new StrestHttpRequestHandler(router));
        return pipeline;
    }
	 
}
