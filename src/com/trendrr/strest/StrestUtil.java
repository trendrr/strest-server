/**
 * 
 */
package com.trendrr.strest;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;


/**
 * @author Dustin Norlander
 * @created Jan 13, 2011
 * 
 */
public class StrestUtil {

	protected static Log log = LogFactory.getLog(StrestUtil.class);
	
	private static AtomicLong txn = new AtomicLong(0l);
	public static String generateTxnId() {
		return "str" + Long.toHexString(txn.incrementAndGet()) + "st";
	}
	public static final HttpVersion STREST_VERSION = new HttpVersion("STREST", 0, 1, true);
	
	public static final class HEADERS {
		public static final String TXN_ID = "Strest-Txn-Id";
		public static final String TXN_STATUS = "Strest-Txn-Status";
		public static final String TXN_ACCEPT = "Strest-Txn-Accept";	
		
		public static final class TXN_STATUS_VALUES {
			public static final String COMPLETE = "complete";
			public static final String CONTINUE = "continue";
		}
		public static final class TXN_ACCEPT_VALUES {
			public static final String SINGLE = "single";	
			public static final String MULTI = "multi";	
		}
	}
	
	/**
	 * easy check to make sure client can handle multiple returns.
	 * @param request
	 * @return
	 */
	public static boolean isTxnMulti(HttpRequest request) {
		String val = request.getHeader(HEADERS.TXN_ACCEPT);
		if (val == null || !val.equals(HEADERS.TXN_ACCEPT_VALUES.MULTI)) {
			return false;
		}
		return true;
	}
	
	public static String txnId(HttpMessage message) {
		return 	message.getHeader(StrestUtil.HEADERS.TXN_ID);
	}
	
	public static boolean isStrest(HttpRequest request) {
		return "STREST".equalsIgnoreCase(request.getProtocolVersion().getProtocolName());
	}
}
