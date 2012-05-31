/**
 * 
 */
package com.trendrr.strest;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;
import com.trendrr.strest.server.v2.models.StrestPacketBase;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.http.StrestHttpResponse;
import com.trendrr.strest.server.v2.models.json.StrestJsonResponse;


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
	
	public static void validateRequest(StrestRequest packet) throws StrestException {
		if (packet.getProtocolName() == null) {
			throw StrestHttpException.BAD_REQUEST("Invalid packet, protocol is required");
		}
		if (packet.getProtocolVersion() == 0) {
			throw StrestHttpException.BAD_REQUEST("Invalid packet, bad protocol version");
		}
		if (packet.getMethod() == null) {
			throw StrestHttpException.BAD_REQUEST("Invalid packet, method is required");
		}
		if (packet.getUri() == null) {
			throw StrestHttpException.BAD_REQUEST("Invalid packet, uri is required");
		}
		
	}
	
	/**
	 * easy check to make sure client can handle multiple returns.
	 * @param request
	 * @return
	 */
	public static boolean isTxnMulti(StrestRequest request) {
		TxnAccept val = request.getTxnAccept();
		if (val == null || val == TxnAccept.SINGLE) {
			return false;
		}
		return true;
	}
	
//	public static String txnId(HttpMessage message) {
//		return 	message.getHeader(StrestUtil.HEADERS.TXN_ID);
//	}
	
	public static boolean isStrest(StrestRequest request) {
		return "STREST".equalsIgnoreCase(request.getProtocolName());
	}
	
	public static String toString(StrestResponse response) {
		
		if (response instanceof StrestHttpResponse) {
			
			StringBuilder str = new StringBuilder();
			str.append(response.getProtocolVersion());
			str.append(" ");
			str.append(response.getStatusCode());
			str.append(" ");
			str.append(response.getStatusMessage());
			
			str.append("\n");
			for (String hdr : ((StrestHttpResponse)response).getResponse().getHeaderNames()) {
				str.append(hdr);
				str.append(" : ");
				str.append(response.getHeader(hdr));
				str.append("\n");
			}
			
			str.append("\n\r\n\r");
			str.append(response.getContent().toString());
	
			return str.toString();
		} else if (response instanceof StrestJsonResponse) {
			return ((StrestJsonResponse)response).getMap().toJSONString();
		}
		return response.toString();
	}
}
