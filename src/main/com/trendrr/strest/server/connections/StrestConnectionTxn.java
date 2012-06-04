/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.StrestResponseEncoder;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.callbacks.TxnCompleteCallback;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;
import com.trendrr.strest.server.v2.models.StrestResponse;


/**
 * 
 * A connection that represents a single transaction. 
 * 
 * sends to this will automatically add the appropriate txn id to the response. 
 * Is considered disconnected when either the parent channel is closed, or the txn is complete.
 * 
 * @author Dustin Norlander
 * @created Mar 24, 2011
 * 
 */
public class StrestConnectionTxn /* implements Comparable<StrestConnectionTxn> */{

	protected Log log = LogFactory.getLog(StrestConnectionTxn.class);
	
	private ConcurrentHashMap<String,Object> storage = new ConcurrentHashMap<String,Object>();
	private ConcurrentLinkedQueue<TxnCompleteCallback> txnCompleteCallbacks = new ConcurrentLinkedQueue<TxnCompleteCallback>();
	
	private StrestRequest request;
	
	
	
	public StrestConnectionTxn(StrestRequest request) {
		this.request = request;
	}
	
	/**
	 * returns the request that originated this txn.
	 * @return
	 */
	public StrestRequest getRequest() {
		return this.request;
	}
	
	/**
	 * returns a threadsafe map for use as txn storage.
	 * @return
	 */
	public Map<String,Object> getStorage() {
		return this.storage;
	}
	
	/**
	 * Gets threadsafe storage shared by the channel (so shared by all transactions for this user)
	 * @return
	 */
	public Map<String,Object> getChannelStorage() {
		return this.request.getConnectionChannel().getStorage();
	}
	
	/**
	 * Closes this transaction.
	 * @return
	 */
	public Object close() throws Exception{
		ResponseBuilder response = new ResponseBuilder(this.request);
		response.txnStatus(TxnStatus.COMPLETED);
		return this.sendMessage(response);
	}
	
	public Object sendMessage(ResponseBuilder response) throws Exception{
		return this.sendMessage(response.getResponse());
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	public Object sendMessage(StrestResponse response) throws Exception{
		//set the txn id
		response.setTxnId(this.request.getTxnId());
		return request.getConnectionChannel().sendMessage(response);	
	}
	
	/**
	 * returns the parent connection
	 * @return
	 */
	public StrestConnectionChannel getStrestConnectionChannel() {
		return this.request.getConnectionChannel();
	}

	public String getTxnId() {
		return this.request.getTxnId();
	}
	
	/**
	 * registers a callback for when the connection is disconnected OR when the txn is complete..
	 * @param callback
	 */
	public void onTxnComplete(TxnCompleteCallback callback) {
		this.txnCompleteCallbacks.add(callback);
	}
	
	/**
	 * called when the txn is over or disconnected.
	 */
	public void cleanup() {
		for (TxnCompleteCallback cb : this.txnCompleteCallbacks) {
			cb.txnComplete(this);
		}
		this.request = null;
		this.storage = null;
	}
	
//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(StrestConnectionTxn o) {
//		//first check if the parent connections are equal.
//		int val = this.getStrestConnectionChannel().compareTo(o.getStrestConnectionChannel());
//		if ( val != 0) {
//			return val;
//		}
//		//then compare txn Ids.
//		return this.getTxnId().compareTo(o.getTxnId());
//	}
}
