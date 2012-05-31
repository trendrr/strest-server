/**
 * 
 */
package com.trendrr.strest.examples.chat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Mar 24, 2011
 * 
 */
@Strest(
		route = "/chat/message"
		)
public class Message extends StrestController {

	protected Log log = LogFactory.getLog(Message.class);
	
	@Override
	public void handleGET(DynMap params) throws StrestException {
		String from = (String)this.getChannelConnection().getStorage().get("username");
		String to = params.get(String.class, "to");
		String message = params.get(String.class, "message");
		Users.instance().send(to, from, message);
		//this txn will be short lived
	}
}
