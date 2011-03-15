/**
 * 
 */
package com.trendrr.strest.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.strest.StrestUtil;


/**
 * @author Dustin Norlander
 * @created Jan 25, 2011
 * 
 */
public class RequestHelper {

	protected static Log log = LogFactory.getLog(RequestHelper.class);
	static AtomicLong id = new AtomicLong(0l);
	
	public static void main(String ...strings) {
		
	}
	
	public static HttpRequest create(HttpMethod method, String action) throws URISyntaxException {
		URI uri = new URI(action);
		HttpRequest request = new DefaultHttpRequest(
				new HttpVersion("STREST", 0, 1, true), method, uri.toASCIIString());
        
//        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.setHeader(StrestUtil.HEADERS.TXN_ID, generateTxnId());
        request.setProtocolVersion(new HttpVersion("STREST", 0, 1, true));
        return request;
	}
	
	/**
	 * adds params to the uri
	 * @param params
	 * @return
	 */
	public static HttpRequest addParams(HttpRequest request, DynMap params) {
		String encodedParams = params.toURLString();
		if (encodedParams == null || encodedParams.isEmpty()) {
			return request;
		}
		String uri = request.getUri();
		if (!uri.contains("?")) {
			uri = uri + "?";
		} else {
			uri = uri + "&";
		}
		request.setUri(uri + encodedParams);
		return request;
	}
	
	/**
	 * adds a TxnId if one is not already set.
	 * @param request
	 * @return
	 */
	public static HttpRequest addTxnId(HttpRequest request) {
		if (request.getHeader(StrestUtil.HEADERS.TXN_ID) == null) 
			request.setHeader(StrestUtil.HEADERS.TXN_ID, generateTxnId());
		return request;
	}
	
	public static String generateTxnId() {
		long val = id.incrementAndGet();
		return "s" + Long.toString(val) + "t";
	}
	
	/**
	 * pass in a url encoded string, get back the original
	 * @param string
	 * @return
	 */
	public static String unUrlEncode(String string) {
		if (string == null)
			return null;
		try {
			return URLDecoder.decode(string, "utf-8");
		} catch (Exception x) {
			log.info("Caught", x);
		}
		return null;
		
	}
	
	/**
	 * url encodes the string
	 * @param string
	 * @return
	 */
	public static String urlEncode(String string) {
		if (string == null)
			return null;
		try {
			return URLEncoder.encode(string, "utf-8");
		} catch (Exception x) {
			log.info("Caught", x);
		}
		return null;
		
	}
}
