/**
 * 
 */
package com.trendrr.strest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class StrestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3720409055017281521L;

	protected Log log = LogFactory.getLog(StrestException.class);
	
	protected String message = null;
	
	protected Exception cause = null;
	
	public StrestException(String message, Exception cause) {
		this.message = message;
		this.cause = cause;
		if (message == null && cause != null) {
			this.message = cause.getMessage();
		} 
	}
	
	public StrestException() {
		this(null, null);
	}
	
	public StrestException(String message) {
		this(message, null);
	}
	
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Exception getCause() {
		return this.cause;
	}

	public void setCause(Exception cause) {
		this.cause = cause;
	}

	public StrestException(Exception cause) {
		this(null, cause);
	}
}
