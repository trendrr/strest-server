/**
 * 
 */
package com.trendrr.strest.server.callbacks;

import com.trendrr.strest.server.connections.StrestConnectionTxn;


/**
 * Register with the StrestConnectionTxn.
 * 
 * This is called when the txn is complete because either the connection is broken, or the 
 * transaction is complete.
 * 
 * 
 * @author Dustin Norlander
 * @created Mar 24, 2011
 * 
 */
public interface TxnCompleteCallback {
	public void txnComplete(StrestConnectionTxn connection);
}
