/**
 * 
 */
package com.trendrr.strest.server.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;


/**
 * @author Dustin Norlander
 * @created Mar 18, 2011
 * 
 */
public class WebsocketDecoder extends OneToOneDecoder {

	protected Log log = LogFactory.getLog(WebsocketDecoder.class);

	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel arg1,
			Object m) throws Exception {
		if (!(m instanceof DefaultWebSocketFrame)) {
            return m;
        }
//        
        DefaultWebSocketFrame wsf = (DefaultWebSocketFrame)m;
        
        if (wsf.isBinary()) {
	    	return wsf.getBinaryData();
	    } else if (wsf.isText()) {
	    	return ChannelBuffers.wrappedBuffer(wsf.getTextData().getBytes("utf8"));
	    }
        
		return m;
	}
	
//	@Override
//    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
//		
//		
//	}
//	 @Override
//	    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
//	            throws Exception {
//		 
//	        Object m = e.getMessage();
//	        if (!(m instanceof DefaultWebSocketFrame)) {
//	            ctx.sendUpstream(e);
//	            return;
//	        }
//	        
//	        DefaultWebSocketFrame wsf = (DefaultWebSocketFrame)m;
//	        if (!(e instanceof UpstreamMessageEvent)) {
//	        	ctx.sendUpstream(e);
//	        	return;
//	        }
//	        
//	        UpstreamMessageEvent ume = (UpstreamMessageEvent)e;
//	        ume.
//	        System.out.println(e.getClass());
//	        
//	        
////	        if (wsf.isBinary()) {
////	        	ctx.sendUpstream(arg0)
////	        	ctx.sendUpstream(wsf.getBinaryData());
////	        }
////	        wsf.getBinaryData()
////	        wsf.
////	        ChannelBuffer input = (ChannelBuffer) m;
////	        if (!input.readable()) {
////	            return;
////	        }
////
////	        ChannelBuffer cumulation = cumulation(ctx);
////	        cumulation.discardReadBytes();
////	        cumulation.writeBytes(input);
////	        callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());
//	    }
}
