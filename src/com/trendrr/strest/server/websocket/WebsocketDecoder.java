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
        DefaultWebSocketFrame wsf = (DefaultWebSocketFrame)m;
        
        if (wsf.isBinary()) {
	    	return wsf.getBinaryData();
	    } else if (wsf.isText()) {
	    	return ChannelBuffers.wrappedBuffer(wsf.getTextData().getBytes("utf8"));
	    }
        
		return m;
	}
}
