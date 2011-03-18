/**
 * 
 */
package com.trendrr.strest.examples;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.StrestConnection;
import com.trendrr.strest.server.StrestConnectionGroup;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Feb 2, 2011
 * 
 */

/*//
 * 
 * @route /firehose
 * @example /firehose
 * 
 * 
 */
@Strest(
		route = "/firehose"
)
public class FirehoseController extends StrestController implements Runnable{

	protected Log log = LogFactory.getLog(FirehoseController.class);

	/**
	 * This is a special lock that only opens the first time
	 */
	static LazyInit lock = new LazyInit();
	static StrestConnectionGroup connections = new StrestConnectionGroup();
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.StrestController#action(com.trendrr.oss.DynMap)
	 */
	@Override
	public void handleGET(DynMap params) throws Exception {
		if (lock.start()) {
			try {
				//lazily start the thread. (once and only once)
				Thread t = new Thread(this);
				t.setDaemon(true);
				t.start();
			} finally {
				lock.end();
			}
		}
		
		if (!StrestUtil.isTxnMulti(this.request)) {
			throw new StrestHttpException(400, "Strest-Txn-Accept must be multi for this action!");
		}		
		connections.addConnection(this.getConnection(), this.getRequest());
		this.setSendResponse(false); //make sure to not send a default response.
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//Send the firehose messages to all registered connections.	
		long num = 0;
		while (true) {
			//build the response.
			ResponseBuilder res = new ResponseBuilder();
			try {
				res.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE); //lets the client know to expect more messages.
				
				//set the content message.
				res.content("text", ("this is message number: " + num++ + "\r\nThere are " + connections.size() + " concurrent connections").getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				//will never happen :)
			}
			//send it to all connections
			connections.sendMessage(res.getResponse());
			Sleep.millis(500);
		}		
	}
}
