/**
 * 
 */
package com.trendrr.strest.server.v2;

import static org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.StrestRouter;
import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;
import com.trendrr.strest.server.v2.models.http.StrestHttpRequest;
import com.trendrr.strest.server.v2.models.json.StrestJsonRequest;


/**
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class StrestJsonRequestHandler extends SimpleChannelUpstreamHandler {

	protected static Log log = LogFactory
			.getLog(StrestJsonRequestHandler.class);
	StrestRouter router;
	
	public StrestJsonRequestHandler(StrestRouter router) {
		this.router = router;
	}
	
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	DynMap request = (DynMap) e.getMessage(); 
        StrestJsonRequest req = new StrestJsonRequest(request);
        req.setConnectionChannel(StrestNettyConnectionChannel.get(e.getChannel()));
        router.incoming(req);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
//    	log.info("Disconnect! " + ctx);    	
    	StrestNettyConnectionChannel.remove(e.getChannel());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
    	log.warn("Caught", e.getCause());
        e.getChannel().close();
        StrestNettyConnectionChannel.remove(e.getChannel());
    }
    
    
    
}
