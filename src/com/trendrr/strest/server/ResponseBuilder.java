/**
 * 
 */
package com.trendrr.strest.server;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestUtil;


/**
 * @author Dustin Norlander
 * @created Jan 26, 2011
 * 
 */
public class ResponseBuilder {

	protected Log log = LogFactory.getLog(ResponseBuilder.class);

	HttpResponse response;
	
	public static void main(String...strings) {
		ResponseBuilder b = new ResponseBuilder();
	}
	
	/**
	 * Creates a response builder 
	 * txn-status is complete by default
	 */
	public ResponseBuilder() {
		response = new DefaultHttpResponse(
				new HttpVersion("STREST", 0, 1, true), HttpResponseStatus.OK);
		this.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE);
	}
	
	public ResponseBuilder(HttpRequest request) {
		response = new DefaultHttpResponse(
				request.getProtocolVersion(), HttpResponseStatus.OK);
		this.txnId(request.getHeader(StrestUtil.HEADERS.TXN_ID));
		
	}
	
	public ResponseBuilder(HttpResponse response) {
		this.response = response;
	}
	
	public ResponseBuilder txnStatus(String status) {
		response.setHeader(StrestUtil.HEADERS.TXN_STATUS, status);
		return this;
	}
	
	/**
	 * sets the txn status to complete
	 * @return
	 */
	public ResponseBuilder txnStatusComplete() {
		response.setHeader(StrestUtil.HEADERS.TXN_STATUS, StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE);
		return this;
	}
	
	/**
	 * sets txn status to continue
	 * @return
	 */
	public ResponseBuilder txnStatusContinue() {
		response.setHeader(StrestUtil.HEADERS.TXN_STATUS, StrestUtil.HEADERS.TXN_STATUS_VALUES.CONTINUE);
		return this;
	}
	
	public ResponseBuilder status(HttpResponseStatus status) {
		this.response.setStatus(status);
		return this;
	}
	
	public ResponseBuilder status(int code, String message) {
		this.response.setStatus(new HttpResponseStatus(code, message));
		return this;
	}
	
	/**
	 *
	 * @param id
	 * @return
	 */
	public ResponseBuilder txnId(String id) {
		if (id != null)
			response.setHeader(StrestUtil.HEADERS.TXN_ID, id);
		return this;
	}
	
	
	public ResponseBuilder header(String header, Object value) {
		response.setHeader(header, value);
		return this;
	}
	
	public ResponseBuilder content(String mimeType, byte[] bytes) {
		response.setContent(ChannelBuffers.copiedBuffer(bytes));
		response.setHeader("Content-Type", mimeType);
//		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		return this;
	}
	
	/**
	 * encodes the text as utf8 and swallows and logs a warning for any character encodeing exceptions
	 * @param mimeType
	 * @param content
	 * @return
	 */
	public ResponseBuilder contentUTF8(String mimeType, String content) {
		try {
			this.content(mimeType, content.getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			log.warn("Swallowed", e);
		}
		return this;
	}
	/**
	 * same as above but sets mimetype to text/plain
	 * @param content
	 * @return
	 */
	public ResponseBuilder contentUTF8(String content) {
		this.contentUTF8("text/plain", content);
		return this;
	}
	
	public ResponseBuilder contentJSON(DynMap mp) {
		return this.contentUTF8("text/json", mp.toJSONString());
	}
	
	public HttpResponse getResponse() {
		return this.response;
	}
	
	public void setResponse(HttpResponse response) {
		this.response = response;
	}
	
	public String getTxnStatus() {
		return response.getHeader(StrestUtil.HEADERS.TXN_STATUS);
	}
	
}
