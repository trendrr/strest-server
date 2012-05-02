/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.v2.models.*;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;
import com.trendrr.strest.server.v2.models.http.StrestHttpResponse;


/**
 * @author Dustin Norlander
 * @created Jan 12, 2011
 * 
 */
public class StrestConnectionChannel implements Comparable<StrestConnectionChannel>{

	protected Log log = LogFactory.getLog(StrestConnectionChannel.class);
	
	Channel channel;
	
	private ConcurrentHashMap<String, StrestConnectionTxn> transactions = new ConcurrentHashMap<String, StrestConnectionTxn>();
	private ConcurrentHashMap<String,Object> connectionStorage = new ConcurrentHashMap<String,Object>();
	private ConcurrentLinkedQueue<DisconnectCallback> disconnectCallbacks = new ConcurrentLinkedQueue<DisconnectCallback>();
	
	public StrestConnectionChannel(Channel channel) {
		this.channel = channel;
	}
	
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
	 * gets the threadsafe connection storage, never null.
	 * @return
	 */
	public Map<String, Object> getStorage() {
		return connectionStorage;
	}

	public synchronized boolean isConnected() {
		if (this.channel != null) {
			return this.channel.isConnected();
		}
		return false;
	}
	
	public synchronized void cleanup() {		
		//first cleanup all the txns
		for (String txnId : this.transactions.keySet()) {
			this.transactions.get(txnId).cleanup();
		}
		this.transactions = null;
		
		//call all the disconnectCallbacks
		while(!this.disconnectCallbacks.isEmpty()) {
			DisconnectCallback cb = this.disconnectCallbacks.poll();
			cb.disconnected(this);
		}
		this.disconnectCallbacks.clear();
//		this.channel = null;
		this.connectionStorage.clear();
	}
	
	public ChannelFuture sendMessage(ResponseBuilder responseBuilder) {
		return this.sendMessage(responseBuilder.getResponse());
	}
	
	public synchronized ChannelFuture sendMessage(StrestResponse response) {
		if (channel == null || !channel.isOpen()) {
			log.info("channel is closed, user has disconnected");
			return null;
		}
        
		// Write the response.
		if (response.getTxnStatus() == TxnStatus.COMPLETED) {
			//remove the txn
			this.txnComplete(response.getTxnId());
		}
		
		if (response instanceof StrestHttpResponse) {
			//TODO: we should probably just have an encoder in the pipeline..
			return channel.write(((StrestHttpResponse)response).getResponse());
		} else {
			return channel.write(response);
		}
	}
	
	public void incoming(StrestRequest request) {
		String txnId = request.getTxnId();
		if (txnId == null) {
			//duh, what do we do now?
			//guess this is a normal HTTP request.
			return;
		}
		if (this.transactions.get(txnId) == null) {
			this.transactions.putIfAbsent(txnId, new StrestConnectionTxn(request));
		}
	}
	
	public StrestConnectionTxn getTxnConnection(String txnId) {
		return this.transactions.get(txnId);
	}
	
	public void txnComplete(String id) {
		if (id == null)
			return;
		StrestConnectionTxn txn = this.transactions.remove(id);
		if (txn == null)
			return;
		txn.cleanup();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StrestConnectionChannel o) {
		return this.channel.compareTo(o.getChannel());
	}
}
