/**
 * 
 */
package com.trendrr.strest.examples.chat;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.callbacks.TxnCompleteCallback;
import com.trendrr.strest.server.connections.StrestConnectionGroup;
import com.trendrr.strest.server.connections.StrestConnectionTxn;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;


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
	protected StrestConnectionGroup notifyDisconnect = new StrestConnectionGroup();
	
	//All the connections wanting connect notices
	protected StrestConnectionGroup notifyConnect = new StrestConnectionGroup();
	
	
	
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
		try {
			this.onlineNow.get(username).close();
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}

	public void notifyMessage(StrestConnectionTxn con) throws StrestException {
		String username = (String)con.getChannelStorage().get("username");
		if (this.notifyMessage.putIfAbsent(username, con) != null) {
			//already a user with that name
			throw new StrestHttpException(501, "That username is registered for messages!");
		}
		

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
		try {
			con.sendMessage(new ResponseBuilder(con.getRequest()).txnStatus(TxnStatus.CONTINUE).contentJSON(mp));
		} catch (Exception e) {
			log.error("Caught", e);
		}
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

		String self = (String)con.getChannelStorage().get("username");
		
		//Now that I am registered to recieve connect message, then I
		//tell everyone else that I am online.
		ResponseBuilder response = new ResponseBuilder(con.getRequest());
		response.txnStatus(TxnStatus.CONTINUE);
		response.contentUTF8(self);
		notifyConnect.sendMessage(response.getResponse());
		
		
		this.notifyConnect.addConnection(con);
		//now we send the user the list of all currently connected users
		for (String username : this.onlineNow.keySet()) {
			if (username.equals(self)) {
				continue; //don't need to notify about self
			}
			response = new ResponseBuilder(con.getRequest());
			response.txnStatus(TxnStatus.CONTINUE);
			response.contentUTF8(username);
			try {
				con.sendMessage(response);
			} catch (Exception e) {
				log.error("caught", e);
			}
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
		ResponseBuilder response = new ResponseBuilder(connection.getRequest());
		response.txnStatus(TxnStatus.CONTINUE);
		response.contentUTF8(username);
		notifyDisconnect.sendMessage(response.getResponse());
	}
	
}
