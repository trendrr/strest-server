/**
 * 
 */
package com.trendrr.strest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;


/**
 * @author Dustin Norlander
 * @created Jan 16, 2011
 * 
 */
public class StrestHttpException extends StrestException {

	
	protected Log log = LogFactory.getLog(StrestHttpException.class);
	
	public static final StrestHttpException BAD_REQUEST() {
		return new StrestHttpException(400, "Bad Request");
	}
	public static final StrestHttpException UNAUTHORIZED() {
		return new StrestHttpException(401, "Unauthorized");
	}
	public static final StrestHttpException PAYMENT_REQUIRED() {
		return new StrestHttpException(402, "Payment Required");
	}
	public static final StrestHttpException FORBIDDEN() {
		return new StrestHttpException(403, "Forbidden");
	}
	public static final StrestHttpException NOT_FOUND() {
		return new StrestHttpException(404, "Not found");
	}
	
	public static final StrestHttpException METHOD_NOT_ALLOWED() {
		return new StrestHttpException(405, "Method Not Allowed");
	}

	public static final StrestHttpException NOT_ACCEPTABLE() {
		return new StrestHttpException(406, "Not Acceptable");
	}
	
	public static final StrestHttpException CONFLICT() {
		return new StrestHttpException(409, "Conflict");
	}
	
	public static final StrestHttpException INTERNAL_SERVER_ERROR() {
		return new StrestHttpException(500, "Internal Server Error");
	}
	
	public static final StrestHttpException MOVED() {
		return new StrestHttpException(301, "Moved");
	}
	
	
	int code;
	public StrestHttpException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public StrestHttpException(HttpResponseStatus status) {
		this(status.getCode(), status.getReasonPhrase());
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
}
