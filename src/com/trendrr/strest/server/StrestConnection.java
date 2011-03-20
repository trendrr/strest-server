/**
 * 
 */
package com.trendrr.strest.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Jan 12, 2011
 * 
 */
public class StrestConnection {

	protected Log log = LogFactory.getLog(StrestConnection.class);
	
	Channel channel;
	private ConcurrentHashMap<String, ConcurrentHashMap<String,Object>> transactionStorage = new ConcurrentHashMap<String,ConcurrentHashMap<String,Object>>();
	private ConcurrentHashMap<String,Object> connectionStorage = new ConcurrentHashMap<String,Object>();
	private ConcurrentLinkedQueue<DisconnectCallback> disconnectCallbacks = new ConcurrentLinkedQueue<DisconnectCallback>();
	
	/**
	 * registers a callback for when the connection is disconnected..
	 * @param callback
	 */
	public void onDisconnect(DisconnectCallback callback) {
		this.disconnectCallbacks.add(callback);
	}
	
	public Channel getChannel() {
		return this.channel;
	}
	
	/**
	 * gets the transaction storage, or null
	 * @param txnId
	 * @return
	 */
	public Map<String, Object> getTransactionStorage(String txnId) {
		if (txnId == null)
			return null;
		return transactionStorage.get(txnId);
	}

	/**
	 * gets the connection storage, never null.
	 * @return
	 */
	public Map<String, Object> getConnectionStorage() {
		return connectionStorage;
	}

	public synchronized boolean isConnected() {
		return this.channel != null;
	}
	
	synchronized void finished() {		
		for (DisconnectCallback cb : this.disconnectCallbacks) {
			cb.disconnected(this);
		}
		this.channel = null;
		this.transactionStorage.clear();
		this.transactionStorage = null;
		this.connectionStorage.clear();
		this.connectionStorage = null;
	}
	
	public synchronized ChannelFuture sendMessage(HttpResponse response) {
		 // Write the response.
        return channel.write(response);
	}
	
	void transactionStarted(String id) {
		if (id == null)
			return;
		this.transactionStorage.putIfAbsent(id, new ConcurrentHashMap<String,Object>());
	}
	
	void transactionComplete(String id) {
		if (id == null)
			return;
		
		this.transactionStorage.remove(id);
	}
	
	
	
	
}
