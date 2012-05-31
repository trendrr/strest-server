/**
 * 
 */
package com.trendrr.strest.client;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;


/**
 * @author Dustin Norlander
 * @created Jan 31, 2011
 * 
 */
public class SynchronousRequest implements StrestCallback {

	protected Log log = LogFactory.getLog(SynchronousRequest.class);

	Semaphore lock = new Semaphore(1, true);
	HttpResponse response;
	
	public SynchronousRequest() {
		try {
			//take the only semaphore
			lock.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HttpResponse awaitResponse() {
		try {
			//try to aquire a semaphore, none is available so we wait.
			lock.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.response;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.client.StrestCallback#messageRecieved(org.jboss.netty.handler.codec.http.HttpResponse)
	 */
	@Override
	public void messageRecieved(HttpResponse response) {
		// TODO Auto-generated method stub
		this.response = response;
		//release the single semaphore.
		lock.release(1);
		
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.client.StrestCallback#txnComplete()
	 */
	@Override
	public void txnComplete() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.client.StrestCallback#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable x) {
		// TODO Auto-generated method stub

	}
}
