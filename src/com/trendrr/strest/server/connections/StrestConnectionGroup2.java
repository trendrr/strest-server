/**
 * 
 */
package com.trendrr.strest.server.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.server.StrestResponseEncoder;
import com.trendrr.strest.server.callbacks.TxnCompleteCallback;


/**
 * @author Dustin Norlander
 * @created Mar 24, 2011
 * 
 */
public class StrestConnectionGroup2 implements TxnCompleteCallback {

	protected Log log = LogFactory.getLog(StrestConnectionGroup2.class);

	private int size = 0; //we keep our own copy of size as the connections.size is slow
	private ConcurrentSkipListSet<StrestConnectionTxn> connections = new ConcurrentSkipListSet<StrestConnectionTxn>();

	/**
	 * Adds a new connection to the group.
	 * @param connection
	 * @param txnId
	 */
	public synchronized void addConnection(StrestConnectionTxn connection) {
		if (!connections.add(connection)) {
			log.warn("Connection already present in the group!: " + connection);
			return;
		}
		size++;
		connection.onTxnComplete(this);
	}
	
	public boolean removeConnection(StrestConnectionTxn connection) {
		if (connections.remove(connection)){
			size--;
			return true;
		}
		return false;
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
	public Collection<ChannelFuture> sendMessage(HttpResponse response) {
		Collection<ChannelFuture> futures = new ArrayList<ChannelFuture>();
		for (StrestConnectionTxn con : this.connections) {
			HttpResponse res = StrestResponseEncoder.copyHeaders(response);
			
			//We don't allow any content encoding.
			//TODO: we should sort the gzip accept ones vs the ungzipped.
			res.setHeader(HttpHeaders.Names.CONTENT_ENCODING, "identity");
			res.setContent(response.getContent()); //all share the same content buffer.
			futures.add(con.sendMessage(res));
		}
		return futures;
	}
	
	public int size() {
		return this.size();
	}
	
	public boolean isEmpty() {
		return this.connections.isEmpty();
	}
	
	public boolean contains(StrestConnectionChannel connection) {
		return this.connections.contains(connection);
	}
	
	
}
