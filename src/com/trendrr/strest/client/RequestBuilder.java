/**
 * 
 */
package com.trendrr.strest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestUtil;


/**
 * @author Dustin Norlander
 * @created Jan 26, 2011
 * 
 */
public class RequestBuilder {

	protected Log log = LogFactory.getLog(RequestBuilder.class);

	HttpRequest request;
	
	public static void main(String...strings) {
		RequestBuilder b = new RequestBuilder();
		try {
			b.url("http://www.trendrr.com/api/blah.json?poop=none");
			b.url("www.trendrr.com");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RequestBuilder() {
		request = new DefaultHttpRequest(
				new HttpVersion("STREST", 0, 1, true), HttpMethod.GET, "/");
		this.txnId(StrestUtil.generateTxnId());
		this.txnAccept("multi");
	}
	
	public RequestBuilder(HttpRequest request) {
		this.request = request;
		if (request.getHeader("Strest-Txn-Id") == null) {
			this.txnId(StrestUtil.generateTxnId());
		}
		
	}
	
	/**
	 * sets the host and the uri. 
	 * 
	 * this is assumed to be a properly formed url
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public RequestBuilder url(String url) throws MalformedURLException {
		try {
			URL u = new URL(url);
			String host = u.getHost();
			request.setHeader(HttpHeaders.Names.HOST, host);
			String uri = url.substring(url.indexOf(host) + host.length());
			request.setUri(uri);
		} catch (Exception x) {
			MalformedURLException m = new MalformedURLException("Unable to parse: " + url);
			m.initCause(x);
			throw m;
		}
		return this;
	}
	
	public RequestBuilder uri(String uri) {
		request.setUri(uri);
		return this;
	}
	
	/**
	 * adds params to the uri.
	 * @param params
	 * @return
	 */
	public RequestBuilder params(DynMap params) {
		String encodedParams = params.toURLString();
		if (encodedParams == null || encodedParams.isEmpty()) {
			return this;
		}
		String uri = request.getUri();
		if (!uri.contains("?")) {
			uri = uri + "?";
		} else {
			uri = uri + "&";
		}
		request.setUri(uri + encodedParams);
		return this;
	}
	
	/**
	 * sets a custom transaction Id.  a unique txn id is 
	 * already set, so this call is not manditory.
	 * @param id
	 * @return
	 */
	public RequestBuilder txnId(String id) {
		request.setHeader("Strest-Txn-Id", id);
		return this;
	}
	
	/**
	 * what kind of transactions to accept.
	 * 
	 * 'single' or multi
	 * 
	 * @param val
	 * @return
	 */
	public RequestBuilder txnAccept(String val) {
		request.setHeader("Strest-Txn-Accept", val);
		return this;
	}
	
	public RequestBuilder method(HttpMethod method) {
		request.setMethod(method);
		return this;
	}
	
	public RequestBuilder header(String header, Object value) {
		request.setHeader(header, value);
		return this;
	}
	
	public RequestBuilder content(String mimeType, byte[] bytes) {
		request.setContent(ChannelBuffers.copiedBuffer(bytes));
		request.setHeader("Content-Type", mimeType);
		return this;
	}
	
	public HttpRequest getRequest() {
		return this.request;
	}
	
}
