/**
 * 
 */
package com.trendrr.strest.server.websocket;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;


/**
 * @author Dustin Norlander
 * @created Mar 18, 2011
 * 
 */
public class WebsocketEncoder extends org.jboss.netty.handler.codec.oneone.OneToOneEncoder {

	protected Log log = LogFactory.getLog(WebsocketEncoder.class);

	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.oneone.OneToOneEncoder#encode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object encode(ChannelHandlerContext arg0, Channel arg1,
			Object m) throws Exception {
		if (m instanceof ChannelBuffer) {
			ChannelBuffer cbuf = (ChannelBuffer)m;
			WebSocketFrame frame = new DefaultWebSocketFrame(cbuf.toString(Charset.forName("utf8")));
			return frame;
		}
		return m;
	}
}
