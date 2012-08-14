/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.trendrr.strest.server;

import static org.jboss.netty.channel.Channels.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class StrestServerPipelineFactory implements ChannelPipelineFactory {
	protected StrestRouter router ;
	
	protected SSLContext sslContext = null;
	
	protected ExecutionHandler handler;
	
	/**
	 * creates a new pipeline factory (non-ssl)
	 * @param router
	 */
	public StrestServerPipelineFactory(StrestRouter router) {
		this(router, null);
		
	}
	
	/**
	 * create a new pipeline factory.  if an sslContext is provided then this
	 * will be an ssl connection.
	 * @param router
	 * @param sslContext
	 */
	public StrestServerPipelineFactory(StrestRouter router, SSLContext sslContext) {
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
        // Uncomment the following line if you don't want to handle HttpChunks.
        pipeline.addLast("aggregator", new StrestChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        pipeline.addLast("deflater", new StrestResponseEncoder());
        
        pipeline.addLast("executionHandler", handler);
        pipeline.addLast("handler", new StrestRequestHandler(router));
        return pipeline;
    }
}
