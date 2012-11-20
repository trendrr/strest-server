/**
 * 
 */
package com.trendrr.strest.server.v2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.json.StrestJsonResponse;


/**
 * @author Dustin Norlander
 * @created Nov 20, 2012
 * 
 */
public class StrestWebSocketEncoder extends OneToOneEncoder {

	protected static Log log = LogFactory.getLog(StrestWebSocketEncoder.class);
	
	
	@Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		
		String json = null;
		if (msg instanceof StrestJsonResponse) {
			 StrestJsonResponse res = (StrestJsonResponse)msg;
			 json = res.getMap().toJSONString();
		 } else if (msg instanceof DynMap) {
			json = ((DynMap)msg).toJSONString();
		 } else {
			 return msg; //do nothing.
		 }
		System.out.println(json);
		return new TextWebSocketFrame(json);
	}
}
