/**
 * 
 */
package com.trendrr.strest.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;


/**
 * 
 * Basically a copy of HttpClientCodec. 
 * 
 * removed the method queue, as strest allows multiple responses for a single request.
 * 
 * 
 * @author Dustin Norlander
 * @created Feb 4, 2011
 * 
 */
public class StrestClientCodec implements ChannelUpstreamHandler,
		ChannelDownstreamHandler {

    private HttpRequestEncoder encoder = new HttpRequestEncoder();
    private HttpResponseDecoder decoder = new HttpResponseDecoder();
    
	protected Log log = LogFactory.getLog(StrestClientCodec.class);

	public StrestClientCodec() {
        this(4096, 8192, 8192);
    }
	
    /**
     * Creates a new instance with the specified decoder options.
     */
    public StrestClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        decoder = new HttpResponseDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        decoder.handleUpstream(ctx, e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        encoder.handleDownstream(ctx, e);
    }
}
