/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.StrestResponseEncoder;
import com.trendrr.strest.server.callbacks.ConnectionGroupEmptyCallback;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.callbacks.TxnCompleteCallback;
import com.trendrr.strest.server.v2.models.StrestResponse;


/**
 * 
 * Allows us to write identical requests to multiple connections, using 
 * the connections individual transactions.
 * 
 * Connections are automatically removed when they are disconnected.
 * 
 * @author Dustin Norlander
 * @created Mar 16, 2011
 * 
 */
public class StrestConnectionGroup implements TxnCompleteCallback {

	protected Log log = LogFactory.getLog(StrestConnectionGroup.class);

	private int size = 0; //we keep our own copy of size as the connections.size is slow
	private ConcurrentSkipListSet<StrestConnectionTxn> connections = new ConcurrentSkipListSet<StrestConnectionTxn>();

	private ConcurrentLinkedQueue<ConnectionGroupEmptyCallback> emptyCallbacks = new ConcurrentLinkedQueue<ConnectionGroupEmptyCallback>();
	
	private AtomicBoolean closed = new AtomicBoolean(false);
	
	/**
	 * Adds a new connection to the group.
	 * @param connection
	 * @param txnId
	 */
	public synchronized void addConnection(StrestConnectionTxn connection) {
		if (closed.get()) {
			//TODO: probably should throw an exception
			log.warn("This connection group is closed! ");
			return;
		}
		
		if (!connections.add(connection)) {
			log.warn("Connection already present in the group!: " + connection);
			return;
		}
		size++;
		connection.onTxnComplete(this);
	}
	
	public boolean removeConnection(StrestConnectionTxn connection) {
		try {
			if (connections.remove(connection)){
				size--;
				return true;
			}
		} finally {
			this.onEmptyCallback();
		}
		return false;
	}
	
	private void onEmptyCallback() {
		if (this.isEmpty()) {
			ConnectionGroupEmptyCallback cb = emptyCallbacks.poll();
			while(cb != null) {
				cb.connectionGroupEmpty(this);
				cb = emptyCallbacks.poll();
			}
		}
	}
	
	/**
	 * register a callback for when this connection group is empty. 
	 * 
	 * Note this will never be called if this group never has any members.
	 * 
	 * @param callback
	 */
	public void onEmpty(ConnectionGroupEmptyCallback callback) {
		this.emptyCallbacks.add(callback);
	}
	
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.callbacks.TxnCompleteCallback#txnComplete(com.trendrr.strest.server.connections.StrestConnectionTxn)
	 */
	@Override
	public void txnComplete(StrestConnectionTxn connection) {
		this.removeConnection(connection);
	}
	
	
	/**
	 * sends a message to all the connections + txn in the group.
	 * @param response
	 * @return
	 */
	public Collection<ChannelFuture> sendMessage(StrestResponse response) {
		Collection<ChannelFuture> futures = new ArrayList<ChannelFuture>();
		for (StrestConnectionTxn con : this.connections) {
			
			//TODO: do this in an efficient way!
			log.warn("connection group is disabled, TODO!");
//			
//			HttpResponse res = StrestResponseEncoder.copyHeaders(response);
//			
//			//We don't allow any content encoding.
//			//TODO: we should sort the gzip accept ones vs the ungzipped.
//			res.setHeader(HttpHeaders.Names.CONTENT_ENCODING, "identity");
//			res.setContent(response.getContent()); //all share the same content buffer.
//			futures.add(con.sendMessage(res));
		}
		return futures;
	}
	
	public Collection<ChannelFuture> sendMessage(ResponseBuilder response) {
		return this.sendMessage(response.getResponse());
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return this.connections.isEmpty();
	}
	
	public boolean contains(StrestNettyConnectionChannel connection) {
		return this.connections.contains(connection);
	}
	
	/**
	 * closes this connection group and ends the transaction for *all* participants
	 * @throws Exception 
	 */
	public void close() {
		this.closed.set(true);
		StrestConnectionTxn con = this.connections.pollFirst();
		while (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				log.error("Caught", e);
			}
			con = this.connections.pollFirst();
		}
	}
}
