/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.trendrr.strest.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;
import com.trendrr.strest.server.v2.models.http.StrestHttpRequest;


/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2368 $, $Date: 2010-10-18 17:19:03 +0900 (Mon, 18 Oct 2010) $
 */
@Deprecated
public class StrestRequestHandler extends SimpleChannelUpstreamHandler {

	protected static Log log = LogFactory.getLog(StrestRequestHandler.class);
	
	protected StrestRouter router;
	
	
	public StrestRequestHandler(StrestRouter r) {
		this.router = r;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
//		log.info("Connect! " + ctx);
	}
	
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	HttpRequest request = (HttpRequest) e.getMessage();
        if (is100ContinueExpected(request)) {
            send100Continue(e);
        }
        
        if (request.isChunked()) {
        	//we don't need no chunked POST's here!
        	//throw an exception here!
        	throw new Exception("Chunking not allowed!");
        } 
        StrestHttpRequest req = new StrestHttpRequest(request);
        Channel channel = e.getChannel();
        StrestNettyConnectionChannel con = StrestNettyConnectionChannel.get(channel);
        req.setConnectionChannel(con);
        router.incoming(req);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
//    	log.info("Disconnect! " + ctx);
    	StrestNettyConnectionChannel.remove(e.getChannel());
    }
    
    
    private void send100Continue(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
    	log.warn("Caught", e.getCause());
        e.getChannel().close();
        StrestNettyConnectionChannel.remove(e.getChannel());
    }
}
