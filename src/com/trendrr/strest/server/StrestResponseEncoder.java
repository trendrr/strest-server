/**
 * 
 */
package com.trendrr.strest.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.StrestUtil;


/**
 * @author Dustin Norlander
 * @created Mar 17, 2011
 * 
 */
public class StrestResponseEncoder extends SimpleChannelHandler {

	protected Log log = LogFactory.getLog(StrestResponseEncoder.class);

	/**
	 * Minimum number of bytes before we will gzip content.
	 */
	public static int GZIP_THRESHOLD = 500; //
	
	 @Override
	 public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		 System.out.println("response encoder " + e);
		 Object msg = e.getMessage();
		 if (msg instanceof HttpResponse && ((HttpResponse) msg).getStatus().getCode() == 100) {
			 // 100-continue response must be passed through.
			 ctx.sendDownstream(e);
		 } else  if (msg instanceof HttpMessage) {
			 HttpMessage m = (HttpMessage) msg;
			 //make sure content length is set.
			 if (!m.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
				 if (m.getContent() != null && m.getContent().readableBytes() > 0) {
					 m.setHeader(HttpHeaders.Names.CONTENT_LENGTH, m.getContent().readableBytes());
				 } else {
					 m.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);
				 } 
			 }
		 }
		 ctx.sendDownstream(e);
	 }
	
	/**
	 * Copies a response headers to a new response.  does NOT copy the content!
	 * @param request
	 */
	public static HttpResponse copyHeaders(HttpResponse response) {
		HttpResponse copy = new DefaultHttpResponse(response.getProtocolVersion(), response.getStatus());
		for (String h : response.getHeaderNames()) {
			copy.addHeader(h, response.getHeader(h));
		}
		return copy;
	}
	
	/**
	 * encodes the response adding correct STREST headers when needed
	 * and gzipping when appropriate.
	 * 
	 *  
	 * @param response
	 * @return
	 */
	public static HttpResponse encode(HttpRequest request, HttpResponse response) {
		String accept = request.getHeader(HttpHeaders.Names.ACCEPT_ENCODING);
		long bytes = 0l;
		if (response.getContent() != null) {
			bytes = response.getContent().readableBytes();
		}
		compress(response, accept);
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes);
		if (!response.containsHeader(StrestUtil.HEADERS.TXN_ID)) {
			response.setHeader(StrestUtil.HEADERS.TXN_ID, request.getHeader(StrestUtil.HEADERS.TXN_ID));
		}
		return response;
	}
	
	/**
	 * GZIPs the response content when accept is gzip.  only when content > GZIP_THRESHOLD
	 * 
	 * Will make no change if the content is already encoded, or if accept is not gzip
	 * 
	 * @param response
	 * @param accept
	 */
	public static void compress(HttpResponse response, String accept) {
		if (accept == null)
			return; //no acceptable encodings
		if (response.containsHeader(HttpHeaders.Names.CONTENT_ENCODING))
			return; //content is already encoded
		if (response.getContent() == null || response.getContent().readableBytes() == 0)
			return; //no content bytes

		if (accept.indexOf("gzip") >= 0
					&& response.getContent().readableBytes() > GZIP_THRESHOLD) {
			//actually gzip it.	
			response.setContent(ChannelBuffers.wrappedBuffer(gzip(response.getContent().array())));
			response.setHeader(HttpHeaders.Names.CONTENT_ENCODING, accept);
		} 	
	}
	
	
	public static byte[] gzip(byte[] bytes) {
		ByteArrayOutputStream stream = null;
        try {
            stream = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(stream);
            gos.write(bytes, 0, bytes.length);
            gos.finish();
            return stream.toByteArray();
        }
        catch(IOException e) {
            throw new IllegalStateException("Exception occurred attempting to encode a payload body", e);
        }
        finally {
            try{
                if(stream != null) stream.close();
            }
            catch(IOException e) {/*ignore*/}
        }
	}
}
