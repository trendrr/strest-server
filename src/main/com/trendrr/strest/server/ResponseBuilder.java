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
import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.v2.models.StrestHeader;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.http.StrestHttpRequest;
import com.trendrr.strest.server.v2.models.http.StrestHttpResponse;
import com.trendrr.strest.server.v2.models.json.StrestJsonRequest;
import com.trendrr.strest.server.v2.models.json.StrestJsonResponse;


/**
 * @author Dustin Norlander
 * @created Jan 26, 2011
 * 
 */
public class ResponseBuilder {

	protected static Log log = LogFactory.getLog(ResponseBuilder.class);

	StrestResponse response;
	
	public static void main(String...strings) {
//		ResponseBuilder b = new ResponseBuilder();
	}
	
	
	public static ResponseBuilder instance(StrestRequest request) {
		return new ResponseBuilder(request);
	}
	public static ResponseBuilder instance(StrestResponse response) {
		return new ResponseBuilder(response);
	}
	
	public ResponseBuilder(StrestResponse response) {
		this.response = response;
	}
	/**
	 * creates a new response builder based on the txn id of the request.
	 * @param request
	 */
	public ResponseBuilder(StrestRequest request) {
		if (request instanceof StrestJsonRequest) {
			this.response = new StrestJsonResponse();
		} else if (request instanceof StrestHttpRequest) {
			this.response = new StrestHttpResponse();
		}
		response.setProtocol(request.getProtocolName(), request.getProtocolVersion());
		response.setTxnId(request.getTxnId());
	}
	
	public ResponseBuilder txnStatus(TxnStatus status) {
		response.setTxnStatus(status);
		return this;
	}
	
//	/**
//	 * Sets the status to 302, and the Location to the url.  This is a standard (sort of) redirect. 
//	 * 
//	 * This could be problematic for STREST clients, it is up to them to implement
//	 * redirects (or not). 
//	 * 
//	 * @param url
//	 * @return
//	 */
//	public ResponseBuilder redirect(String url) {
//		this.response.setStatus(HttpResponseStatus.FOUND);
//		this.response.setHeader("Location", url);
//		return this;
//	}
	
//	/**
//	 * Sets the status of the header.
//	 * 
//	 * @param status
//	 * @return
//	 */
//	public ResponseBuilder status(HttpResponseStatus status) {
//		this.response.setStatus(status);
//		return this;
//	}
	
	/**
	 * Sets the status of the header.
	 * 
	 * @param status
	 * @return
	 */
	public ResponseBuilder status(int code, String message) {
		this.response.setStatus(code, message);
		return this;
	}
	
	/**
	 * Set the Strest-Txn-Id header.
	 * @param id
	 * @return
	 */
	public ResponseBuilder txnId(String id) {
		if (id != null)
			response.setTxnId(id);
		return this;
	}
	
	
	public ResponseBuilder header(String header, String value) {
		response.addHeader(header, value);
		return this;
	}
	
	public ResponseBuilder content(String mimeType, byte[] bytes) {
		response.setContent(mimeType, bytes);
		return this;
	}
	
	/**
	 * encodes the text as utf8 and swallows and logs a warning for any character encoding exceptions
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
		this.contentUTF8(ContentTypes.TEXT, content);
		return this;
	}
	
	public ResponseBuilder contentJSON(DynMap mp) {
		return this.contentUTF8(ContentTypes.JSON, mp.toJSONString());
	}
	
	public StrestResponse getResponse() {
		return this.response;
	}
	
	public void setResponse(StrestResponse response) {
		this.response = response;
	}	
}
