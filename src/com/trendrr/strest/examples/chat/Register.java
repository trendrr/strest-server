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
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;
import com.trendrr.strest.server.callbacks.DisconnectCallback;
import com.trendrr.strest.server.connections.StrestConnectionChannel;


/**
 * @author Dustin Norlander
 * @created Mar 23, 2011
 * 
 */
@Strest(
		route = "/chat/register"
)
public class Register extends StrestController {

	protected Log log = LogFactory.getLog(Register.class);
	
	@Override
	public void handleGET(DynMap params) throws StrestException {
		String username = params.get(String.class, "username");
		if (username == null) {
			throw new StrestHttpException(501, "username is mandatory!");
		}
		Users.instance().register(username, this.getTxnConnection());
		this.response.setHeader(StrestUtil.HEADERS.TXN_STATUS, StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE);
	}


}
