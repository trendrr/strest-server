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
 * @created Mar 23, 2011
 * 
 */
@Strest(
		route = "/chat/notify/connect"
		)
public class NotifyConnect extends StrestController {

	protected Log log = LogFactory.getLog(NotifyConnect.class);
	

	@Override
	public void handleGET(DynMap params) throws StrestException {
		Users.instance().notifyConnect(this.getTxnConnection());
		this.setSendResponse(false);
	}
} 
