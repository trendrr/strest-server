/**
 * 
 */
package com.trendrr.strest.flash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Apr 14, 2011
 * 
 */
public class SocketPolicyHandler extends SimpleChannelUpstreamHandler {

	protected Log log = LogFactory.getLog(SocketPolicyHandler.class);
	DynMap config = new DynMap();
	public SocketPolicyHandler(DynMap config) {
		this.config = config;
	}
	
	 @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		 //just ignore incoming message, assume it is 
		 //<policy-file-request/>
		 
		 String response = "<cross-domain-policy>" +
		 		"<allow-access-from domain=\"*\" to-ports=\"*\" />" +
		 		"</cross-domain-policy>";
		 ChannelFuture future = e.getChannel().write(ChannelBuffers.wrappedBuffer(response.getBytes("utf8")));
		 //disconnect..
		 future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
