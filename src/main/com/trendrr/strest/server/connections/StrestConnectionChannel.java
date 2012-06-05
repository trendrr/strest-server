/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.netty.channel.ChannelFuture;

import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;



/**
 * @author Dustin Norlander
 * @created May 31, 2012
 * 
 */
public abstract class StrestConnectionChannel {

	public abstract boolean isConnected();
	
	private ConcurrentHashMap<String, StrestConnectionTxn> transactions = new ConcurrentHashMap<String, StrestConnectionTxn>();
	private ConcurrentHashMap<String,Object> connectionStorage = new ConcurrentHashMap<String,Object>();
	private ConcurrentLinkedQueue<DisconnectCallback> disconnectCallbacks = new ConcurrentLinkedQueue<DisconnectCallback>();
	
	
	/**
	 * send a message. 
	 * @param response
	 * @return 
	 * @throws Exception
	 */
	protected abstract Object doSendMessage(StrestResponse response) throws Exception;
	
	/**
	 * return the address of the connected client (or null if unavailable).
	 * @return
	 */
	public abstract String getRemoteAddress();
	
	public Object sendMessage(ResponseBuilder responseBuilder) throws Exception{
		return this.sendMessage(responseBuilder.getResponse());
	}
	
	public Object sendMessage(StrestResponse response) throws Exception {
		// Write the response.
		if (response.getTxnStatus() == TxnStatus.COMPLETED) {
			//remove the txn
			this.txnComplete(response.getTxnId());
		}
		return this.doSendMessage(response);
	}
	
	/**
	 * registers a callback for when the connection is disconnected..
	 * @param callback
	 */
	public void onDisconnect(DisconnectCallback callback) {
		this.disconnectCallbacks.add(callback);
	}
	
	/**
	 * gets the threadsafe connection storage, never null.
	 * @return
	 */
	public Map<String, Object> getStorage() {
		return connectionStorage;
	}
	
	public synchronized void cleanup() {		
		//first cleanup all the txns
		for (String txnId : this.transactions.keySet()) {
			this.transactions.get(txnId).cleanup();
		}
		
		this.transactions.clear();
		
		//call all the disconnectCallbacks
		while(!this.disconnectCallbacks.isEmpty()) {
			DisconnectCallback cb = this.disconnectCallbacks.poll();
			cb.disconnected(this);
		}
		this.disconnectCallbacks.clear();
//		this.channel = null;
		this.connectionStorage.clear();
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
	
	
}
