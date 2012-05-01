/**
 * 
 */
package com.trendrr.strest.server.v2.models.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.trendrr.strest.server.v2.models.StrestResponse;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public class StrestHttpResponse extends StrestHttpBase implements
		StrestResponse {

	protected static Log log = LogFactory.getLog(StrestHttpResponse.class);

	public StrestHttpResponse(HttpResponse response) {
		this.message = response;
	}
	
	public HttpResponse getResponse() {
		return (HttpResponse)this.message;
	} 
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		this.getResponse().setStatus(new HttpResponseStatus(code, message));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		HttpResponseStatus status = this.getResponse().getStatus();
		if (status == null)
			return 0;
		return status.getCode();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		HttpResponseStatus status = this.getResponse().getStatus();
		if (status == null)
			return "No Message";
		return status.getReasonPhrase();
	}
}
