/**
 * 
 */
package com.trendrr.strest.server.v2;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.trendrr.strest.server.StrestRouter;


/**
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class StrestJsonServerPipelineFactory extends StrestHttpServerPipelineFactory {

	
	/**
	 * @param router
	 * @param sslContext
	 */
	public StrestJsonServerPipelineFactory(StrestRouter router,
			SSLContext sslContext) {
		super(router, sslContext);
	}

	protected static Log log = LogFactory
			.getLog(StrestJsonServerPipelineFactory.class);
	

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		 // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
        
		pipeline.addLast("decoder", new JsonDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        pipeline.addLast("encoder", new StrestJsonEncoder());
        // Remove the following line if you don't want automatic content compression.
        
        pipeline.addLast("executionHandler", this.handler);
        
        pipeline.addLast("handler", new StrestJsonRequestHandler(router));
        
        return pipeline;
	}
	
}
