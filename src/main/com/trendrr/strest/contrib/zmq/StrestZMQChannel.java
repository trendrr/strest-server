/**
 * 
 */
package com.trendrr.strest.contrib.zmq;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Timeframe;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.json.StrestJsonResponse;
import com.trendrr.zmq.server.ZMQChannel;


/**
 * @author Dustin Norlander
 * @created Jun 4, 2012
 * 
 */
public class StrestZMQChannel extends StrestConnectionChannel{

	protected static Log log = LogFactory.getLog(StrestZMQChannel.class);

	protected ZMQChannel channel;
	
	protected Date lastMessage; //last time we recieved a message from the client.  
	
	protected int timeoutSeconds; //number of seconds we consider this connection to be dead if no incoming message recieved.
	protected  StrestZMQChannel(ZMQChannel channel) {
		super();
		this.channel = channel;
	}
	
	/**
	 * Mapping from zmq id to the subsequent strest channel.  
	 *
	 */
	protected static ConcurrentHashMap<ByteBuffer,StrestZMQChannel> channels = new ConcurrentHashMap<ByteBuffer,StrestZMQChannel>();
	
	/**
	 * gets the StrestNettyConnectionChannel based on the channel, or creates a new association.
	 * @param channel
	 */
	public static StrestZMQChannel get(ZMQChannel channel) {
		ByteBuffer id = ByteBuffer.wrap(channel.getId());
		StrestZMQChannel c= channels.get(id);
		if (c == null) {
			try {
				System.out.println("couldn't find key: "  + new String(channel.getId(), "utf8"));
			} catch (Exception x) {
				x.printStackTrace();
			}
			channels.putIfAbsent(id, new StrestZMQChannel(channel));
			return channels.get(id);
		}
		return c;
	}
	
	/**
	 * runs the connection reaper.  removes any connections that have timedout.
	 * @return
	 */
	public static void reaper() {
		log.warn("***  Running ZMQ Connection Reaper ****");
		for (StrestZMQChannel c : channels.values()) {
			if (!c.isConnected()) {
				log.warn("*** REAPING: " + c);
				c.cleanup();
			}
		}
		log.warn("***  DONE ZMQ Connection Reaper ****");
	}
	
	public ZMQChannel getChannel() {
		return this.channel;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.connections.StrestConnectionChannel#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return (this.getLastIncoming().after(Timeframe.SECONDS.add(new Date(), -this.timeoutSeconds)));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.connections.StrestConnectionChannel#doSendMessage(com.trendrr.strest.server.v2.models.StrestResponse)
	 */
	@Override
	protected Object doSendMessage(StrestResponse response) throws Exception {
		if (response instanceof StrestJsonResponse) {
			channel.send(((StrestJsonResponse)response).toByteArray());
		} else {
			throw new TrendrrException("Invalid response type: " + response.getClass());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.connections.StrestConnectionChannel#getRemoteAddress()
	 */
	@Override
	public String getRemoteAddress() {
		return null;
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		channels.remove(this.channel.getId());
		//TODO: no way to disconnect a client from the server?
	}
	
	/**
	 * a message has been recieved, this resets our counter.
	 */
	public synchronized void setLastIncoming() {
		this.lastMessage = new Date();
	}
	
	public synchronized Date getLastIncoming() {
		return this.lastMessage;
	}
}
