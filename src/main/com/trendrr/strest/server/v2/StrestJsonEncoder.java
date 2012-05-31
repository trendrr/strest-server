/**
 * 
 */
package com.trendrr.strest.server.v2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.CharsetUtil;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.json.StrestJsonResponse;


/**
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class StrestJsonEncoder extends SimpleChannelHandler {

	protected static Log log = LogFactory.getLog(StrestJsonEncoder.class);
	
	 @Override
	 public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		 Object msg = e.getMessage();
		 log.info("writing: " + msg);
		 if (msg instanceof StrestJsonResponse) {
			 StrestJsonResponse res = (StrestJsonResponse)msg;
			Channels.write(ctx, e.getFuture(), 
					ChannelBuffers.wrappedBuffer(res.getMap().toJSONString().getBytes(CharsetUtil.UTF_8))
			);
			log.info("Writing Response: " + res.getMap().toJSONString());
		 } else if (msg instanceof DynMap) {
			Channels.write(ctx, e.getFuture(), 
					ChannelBuffers.wrappedBuffer(((DynMap)msg).toJSONString().getBytes(CharsetUtil.UTF_8))
			);
			log.info("Writing DynMap: " + ((DynMap)msg).toJSONString());

		 }
	 }
}
