/**
 * 
 */
package com.trendrr.strest.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;


/**
 * @author Dustin Norlander
 * @created Jun 13, 2011
 * 
 */
@Deprecated
public class StrestChunkAggregator extends HttpChunkAggregator {

	/**
	 * @param maxContentLength
	 */
	public StrestChunkAggregator(int maxContentLength) {
		super(maxContentLength);
	}

	protected Log log = LogFactory.getLog(StrestChunkAggregator.class);
}
