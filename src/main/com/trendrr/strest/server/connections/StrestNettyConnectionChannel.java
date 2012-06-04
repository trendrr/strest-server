/**
 * 
 */
package com.trendrr.strest.server.connections;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;

import com.trendrr.strest.server.v2.models.*;
import com.trendrr.strest.server.v2.models.http.StrestHttpResponse;


/**
 * @author Dustin Norlander
 * @created Jan 12, 2011
 * 
 */
public class StrestNettyConnectionChannel extends StrestConnectionChannel {

	protected Log log = LogFactory.getLog(StrestNettyConnectionChannel.class);
	
	/**
	 * Mapping from netty channel to the subsequent strest channel.  
	 *
	 */
	protected static ConcurrentHashMap<Channel,StrestNettyConnectionChannel> channels = new ConcurrentHashMap<Channel,StrestNettyConnectionChannel>();
	
	/**
	 * gets the StrestNettyConnectionChannel based on the channel, or creates a new association.
	 * @param channel
	 */
	public static StrestNettyConnectionChannel get(Channel channel) {
		StrestNettyConnectionChannel c= channels.get(channel);
		if (c == null) {
			channels.putIfAbsent(channel, new StrestNettyConnectionChannel(channel));
			return channels.get(channel);
		}
		return c;
	}
	
	public static void remove(Channel c) {
		remove(get(c));
	}
	
	public static void remove(StrestNettyConnectionChannel c) {
		channels.remove(c.getChannel());
		c.cleanup();
	}
	
	public static int size() {
		return channels.size();
	}
	
	Channel channel;
	
	public StrestNettyConnectionChannel(Channel channel) {
		this.channel = channel;
	}
	
		
	public Channel getChannel() {
		return this.channel;
	}

	
	@Override
	public synchronized boolean isConnected() {
		if (this.channel != null) {
			return this.channel.isConnected();
		}
		return false;
	}
	
	
	@Override
	protected Object doSendMessage(StrestResponse response) throws Exception {
		if (channel == null || !channel.isOpen()) {
			log.info("channel is closed, user has disconnected");
			return null;
		}

		if (response instanceof StrestHttpResponse) {
			//TODO: we should probably just have an encoder in the pipeline..
			return channel.write(((StrestHttpResponse)response).getResponse());
		} else {
			return channel.write(response);
		}
	}
	

//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(StrestConnectionChannel o) {
//		if (o instanceof StrestNettyConnectionChannel) {
//			return this.channel.compareTo(((StrestNettyConnectionChannel)o).getChannel());
//		}
//		return -1;
//	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.connections.StrestConnectionChannel#getRemoteAddress()
	 */
	@Override
	public String getRemoteAddress() {
		try {
			return this.channel.getRemoteAddress().toString();
		} catch (Exception x) {
			log.error("Caught", x);
		}
		return null;
	}

}
