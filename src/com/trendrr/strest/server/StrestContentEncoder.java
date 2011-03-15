/**
 * 
 */
package com.trendrr.strest.server;

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.jboss.netty.util.internal.LinkedTransferQueue;

/**
 * @author Dustin Norlander
 * @created Feb 2, 2011
 * 
 */
public class StrestContentEncoder extends HttpContentCompressor {

	protected Log log = LogFactory.getLog(StrestContentEncoder.class);
	
	private final Queue<String> acceptEncodingQueue = new LinkedTransferQueue<String>();
	String previousEncoding = null;
	
    private volatile EncoderEmbedder<ChannelBuffer> encoder;
	
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Object msg = e.getMessage();
        if (!(msg instanceof HttpMessage)) {
            ctx.sendUpstream(e);
            return;
        }

        HttpMessage m = (HttpMessage) msg;
        String acceptedEncoding = m.getHeader(HttpHeaders.Names.ACCEPT_ENCODING);
        if (acceptedEncoding == null) {
            acceptedEncoding = HttpHeaders.Values.IDENTITY;
        }
        boolean offered = acceptEncodingQueue.offer(acceptedEncoding);
        assert offered;

        ctx.sendUpstream(e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {

        Object msg = e.getMessage();
        if (msg instanceof HttpResponse && ((HttpResponse) msg).getStatus().getCode() == 100) {
            // 100-continue response must be passed through.
            ctx.sendDownstream(e);
        } else  if (msg instanceof HttpMessage) {
            HttpMessage m = (HttpMessage) msg;

            encoder = null;

            // Determine the content encoding.
            String acceptEncoding = acceptEncodingQueue.poll();
            if (acceptEncoding == null) {
               acceptEncoding = this.previousEncoding;
            } else {
            	this.previousEncoding = acceptEncoding;
            }

            if ((encoder = newContentEncoder(acceptEncoding)) != null) {
                // Encode the content and remove or replace the existing headers
                // so that the message looks like a decoded message.
                m.setHeader(
                        HttpHeaders.Names.CONTENT_ENCODING,
                        getTargetContentEncoding(acceptEncoding));

                if (!m.isChunked()) {
                    ChannelBuffer content = m.getContent();
                    // Encode the content.
                    content = ChannelBuffers.wrappedBuffer(
                            encode(content), finishEncode());

                    // Replace the content.
                    m.setContent(content);

                    
//                    if (m.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
//                        m.setHeader(
//                                HttpHeaders.Names.CONTENT_LENGTH,
//                                Integer.toString(content.readableBytes()));
//                    }
                    System.out.println("BLAHRG StrestContentEncoder");
                }
            }
            //ALWAYS SET THE Content_LEngth
            m.setHeader(
                    HttpHeaders.Names.CONTENT_LENGTH,
                    Integer.toString(m.getContent().readableBytes()));
            // Because HttpMessage is a mutable object, we can simply forward the write request.
            ctx.sendDownstream(e);
        } else if (msg instanceof HttpChunk) {
            HttpChunk c = (HttpChunk) msg;
            ChannelBuffer content = c.getContent();

            // Encode the chunk if necessary.
            if (encoder != null) {
                if (!c.isLast()) {
                    content = encode(content);
                    if (content.readable()) {
                        c.setContent(content);
                        ctx.sendDownstream(e);
                    }
                } else {
                    ChannelBuffer lastProduct = finishEncode();

                    // Generate an additional chunk if the decoder produced
                    // the last product on closure,
                    if (lastProduct.readable()) {
                        Channels.write(
                                ctx, Channels.succeededFuture(e.getChannel()), new DefaultHttpChunk(lastProduct), e.getRemoteAddress());
                    }

                    // Emit the last chunk.
                    ctx.sendDownstream(e);
                }
            } else {
                ctx.sendDownstream(e);
            }
        } else {
            ctx.sendDownstream(e);
        }
        
        System.out.println(msg);
    }
    
    private ChannelBuffer encode(ChannelBuffer buf) {
        encoder.offer(buf);
        return ChannelBuffers.wrappedBuffer(encoder.pollAll(new ChannelBuffer[encoder.size()]));
    }

    private ChannelBuffer finishEncode() {
        ChannelBuffer result;
        if (encoder.finish()) {
            result = ChannelBuffers.wrappedBuffer(encoder.pollAll(new ChannelBuffer[encoder.size()]));
        } else {
            result = ChannelBuffers.EMPTY_BUFFER;
        }
        encoder = null;
        return result;
    }
}
