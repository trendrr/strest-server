/**
 * 
 */
package com.trendrr.strest.examples.chat;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.callbacks.TxnCompleteCallback;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.connections.StrestConnectionGroup;
import com.trendrr.strest.server.connections.StrestConnectionGroup2;
import com.trendrr.strest.server.connections.StrestConnectionTxn;


/**
 * @author Dustin Norlander
 * @created Mar 23, 2011
 * 
 */
public class Users implements TxnCompleteCallback {

	private static Users instance = new Users();
	public static Users instance() {
		return instance;
	}
	
	
	protected Log log = LogFactory.getLog(Users.class);
	
	//All the users online now
	protected ConcurrentHashMap<String, StrestConnectionTxn> onlineNow = new ConcurrentHashMap<String, StrestConnectionTxn>();
	
	//users who are recieving messages
	protected ConcurrentHashMap<String, StrestConnectionTxn> notifyMessage = new ConcurrentHashMap<String, StrestConnectionTxn>();
	
	//all the connections wanting disconnect notices.
	protected StrestConnectionGroup2 notifyDisconnect = new StrestConnectionGroup2();
	
	//All the connections wanting connect notices
	protected StrestConnectionGroup2 notifyConnect = new StrestConnectionGroup2();
	
	
	
	public void register(String username, StrestConnectionTxn con) throws StrestException {
		
		if (onlineNow.putIfAbsent(username, con) != null) {
			//already a user with that name
			throw new StrestHttpException(501, "That username is taken");
		}
		
		//store the username in the connection local storage
		con.getChannelStorage().put("username", username);
		
		//We register a callback for when the connection is disconnected, so we 
		//can deregister.
		con.onTxnComplete(this);
	}
	
	/**
	 * deregisters the user.
	 * @param con
	 */
	public void deregister(StrestConnectionTxn con) {
		String username = (String)con.getChannelStorage().get("username");
		this.onlineNow.get(username).close();
	}

	public void notifyMessage(StrestConnectionTxn con) throws StrestException {
		String username = (String)con.getChannelStorage().get("username");
		if (this.notifyMessage.putIfAbsent(username, con) != null) {
			//already a user with that name
			throw new StrestHttpException(501, "That username is registered for messages!");
		}
		
		//We don't make the user public until they have registered for message notification
		//now send the username to all who want connect notifications.
		ResponseBuilder response = new ResponseBuilder();
		response.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE);
		response.contentUTF8(username);
		notifyConnect.sendMessage(response.getResponse());
	}
	
	public void send(String to, String from, String message) throws StrestException {
		StrestConnectionTxn con = this.notifyMessage.get(to);
		if (con == null) {
			throw new StrestHttpException(501, "Unknown user");
		}
		DynMap mp = new DynMap();
		mp.put("to", to);
		mp.put("from", from);
		mp.put("message", message);
		con.sendMessage(new ResponseBuilder().txnStatusContinue().contentJSON(mp));
	}
	
	/**
	 * notifies when a user disconnects
	 * @param con
	 * @param request
	 */
	public void notifyDisconnect(StrestConnectionTxn con) {
		this.notifyDisconnect.addConnection(con);
	}
	
	/**
	 * registers a callback for when users connect.  
	 * connection is sent the list of currently online users as soon as they register for this callback.
	 * 
	 * @param con
	 * @param request
	 */
	public void notifyConnect(StrestConnectionTxn con) {
		this.notifyConnect.addConnection(con);

		String self = (String)con.getChannelStorage().get("username");
		//now we send the user the list of all currently connected users
		for (String username : this.onlineNow.keySet()) {
			if (username.equals(self)) {
				continue; //don't need to notify about self
			}
			ResponseBuilder response = new ResponseBuilder();
			response.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE);
			response.contentUTF8(username);
			con.sendMessage(response);
		}
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.callbacks.TxnCompleteCallback#txnComplete(com.trendrr.strest.server.connections.StrestConnectionTxn)
	 */
	@Override
	public void txnComplete(StrestConnectionTxn connection) {
		//we need to remove this user from the lineup.
		String username = (String)connection.getChannelStorage().get("username");
		if (username == null)
			return;
		this.onlineNow.remove(username);
		this.notifyMessage.remove(username);
		//now send the username to all who want connect notifications.
		ResponseBuilder response = new ResponseBuilder();
		response.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE);
		response.contentUTF8(username);
		notifyDisconnect.sendMessage(response.getResponse());
	}
	
}
