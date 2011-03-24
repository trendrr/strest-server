/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.StrestResponseEncoder;
import com.trendrr.strest.server.callbacks.DisconnectCallback;


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
public class StrestConnectionGroup implements DisconnectCallback{

	protected Log log = LogFactory.getLog(StrestConnectionGroup.class);

	//map of connection to list of transactions.
	ConcurrentHashMap<StrestConnectionChannel, ConcurrentSkipListSet<SCon>> connections = new ConcurrentHashMap<StrestConnectionChannel, ConcurrentSkipListSet<SCon>>();
	

	protected class SCon implements Comparable<SCon>{
		StrestConnectionChannel connection;
		HttpRequest requestCopy;
		public SCon(StrestConnectionChannel con, HttpRequest request) {
			//just copy the headers into a new request
			requestCopy = new DefaultHttpRequest(request.getProtocolVersion(), request.getMethod(), request.getUri());
			for (String h : request.getHeaderNames()) {
				requestCopy.addHeader(h, request.getHeader(h));
			}
		}
		@Override
		public int compareTo(SCon o) {
			return this.requestCopy.getHeader(StrestUtil.HEADERS.TXN_ID)
				.compareTo(o.requestCopy.getHeader(StrestUtil.HEADERS.TXN_ID));
		}
	}
	
	/**
	 * Adds a new connection to the group.
	 * @param connection
	 * @param txnId
	 */
	public synchronized void addConnection(StrestConnectionChannel connection, HttpRequest request) {
		connections.putIfAbsent(connection, new ConcurrentSkipListSet<SCon>());
		connections.get(connection).add(new SCon(connection,request));
		connection.onDisconnect(this);
	}
	
	/**
	 * Removes a connection from the group.  Returns a set of all the original requests 
	 * (requests are copies that only hold the headers) that
	 * were associated with this connection in this group, or null if this connection
	 * was not in the group
	 * @param connection
	 */
	public synchronized Set<HttpRequest> removeConnection(StrestConnectionChannel connection) {
		
		Set<SCon> vals = connections.remove(connection);
		if (vals == null || vals.isEmpty()) {
			return null;
		}
		Set<HttpRequest> requests = new HashSet<HttpRequest>();
		for (SCon scon : vals) {
			requests.add(scon.requestCopy);
		}
		return requests;
	}
	
	/**
	 * removes a specific transaction.  the connection is removed as well if there
	 * was only one registered transaction.
	 * 
	 * does nothing if the connection or txn does not belong to this group.
	 * 
	 * @param connection
	 * @param txnId
	 */
	public synchronized void removeTxn(StrestConnectionChannel connection, String txnId) {
		if (!this.connections.containsKey(connection)) {
			return;
		}
		this.connections.get(connection).remove(txnId);
		if (this.connections.get(connection).isEmpty()) {
			this.removeConnection(connection);
		}
	}
	
	public int size() {
		return this.connections.size();
	}
	
	/**
	 * sends a message to all the connections + txn in the group.
	 * @param response
	 * @return
	 */
	public Collection<ChannelFuture> sendMessage(HttpResponse response) {
		Collection<ChannelFuture> futures = new ArrayList<ChannelFuture>();
		for (StrestConnectionChannel con : this.connections.keySet()) {
			Set<SCon> scons = this.connections.get(con);
			if (scons == null || scons.isEmpty()) 
				continue;
			for (SCon scon : scons) {
				HttpResponse res = StrestResponseEncoder.copyHeaders(response);
				
				//We don't allow any content encoding.
				//TODO: we should sort the gzip accept ones vs the ungzipped.
				res.setHeader(HttpHeaders.Names.CONTENT_ENCODING, "none"); //cheap hack, set to something unrecognizable so encoder will skip.
				res.setContent(response.getContent()); //all share the same content buffer.
				res = StrestResponseEncoder.encode(scon.requestCopy, res);
				
				res.removeHeader(HttpHeaders.Names.CONTENT_ENCODING); 
				futures.add(con.sendMessage(res));
			}
		}
		return futures;
	}
	
	public boolean isEmpty() {
		return this.connections.isEmpty();
	}
	
	public boolean contains(StrestConnectionChannel connection) {
		return this.connections.containsKey(connection);
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.DisconnectCallback#disconnected(com.trendrr.strest.server.StrestConnection)
	 */
	@Override
	public void disconnected(StrestConnectionChannel connection) {
		this.removeConnection(connection);
	}
}
