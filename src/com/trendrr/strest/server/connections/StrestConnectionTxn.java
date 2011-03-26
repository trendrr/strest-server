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
public class StrestConnectionTxn implements Comparable<StrestConnectionTxn>{

	protected Log log = LogFactory.getLog(StrestConnectionTxn.class);
	
	private ConcurrentHashMap<String,Object> storage = new ConcurrentHashMap<String,Object>();
	private StrestConnectionChannel schannel = null;
	private ConcurrentLinkedQueue<TxnCompleteCallback> txnCompleteCallbacks = new ConcurrentLinkedQueue<TxnCompleteCallback>();
	
	//hold a copy of the request (the headers only)
	private HttpRequest request;
	
	
	
	public StrestConnectionTxn(StrestConnectionChannel connection, HttpRequest request) {
		this.schannel = connection;
		this.setRequest(request);
	}
	
	private void setRequest(HttpRequest request) {
		this.request = new DefaultHttpRequest(request.getProtocolVersion(), request.getMethod(), request.getUri());
		for (String h : request.getHeaderNames()) {
			this.request.addHeader(h, request.getHeader(h));
		}
	}
	
	/**
	 * returns a copy of the request that originated this txn.  Does NOT contain the 
	 * content. Only the header information.
	 * @return
	 */
	public HttpRequest getRequest() {
		return this.request;
	}
	
	/**
	 * 
	 * @param responseBuilder
	 * @return
	 */
	public ChannelFuture sendMessage(ResponseBuilder responseBuilder) {
		return this.sendMessage(responseBuilder.getResponse());
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
		return this.schannel.getStorage();
	}
	
	/**
	 * Closes this transaction.
	 * @return
	 */
	public ChannelFuture close() {
		ResponseBuilder response = new ResponseBuilder();
		response.txnStatusComplete();
		return this.sendMessage(response);
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	public ChannelFuture sendMessage(HttpResponse response) {
		//set the txn id
		response.setHeader(StrestUtil.HEADERS.TXN_ID, this.request.getHeader(StrestUtil.HEADERS.TXN_ID));
		/**
		 * do the content encoding here if neeeded.
		 */
		HttpResponse res = StrestResponseEncoder.encode(this.request, response);
		return this.schannel.sendMessage(res);
	}
	
	/**
	 * returns the parent connection
	 * @return
	 */
	public StrestConnectionChannel getStrestConnectionChannel() {
		return this.schannel;
	}

	public String getTxnId() {
		return this.request.getHeader(StrestUtil.HEADERS.TXN_ID);
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
		this.schannel = null;
		this.storage = null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StrestConnectionTxn o) {
		//first check if the parent connections are equal.
		int val = this.getStrestConnectionChannel().compareTo(o.getStrestConnectionChannel());
		if ( val != 0) {
			return val;
		}
		//then compare txn Ids.
		return this.getTxnId().compareTo(o.getTxnId());
	}
}
