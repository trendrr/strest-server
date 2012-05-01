/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestHeader {

	protected static Log log = LogFactory.getLog(StrestHeader.class);
	
	public enum Name {
		
		TXN_ID("Strest-Txn-Id", "txn.id"),
		TXN_ACCEPT("Strest-Txn-Accept", "txn.accept"),
		TXN_STATUS("Strest-Txn-Status", "txn.status"),
		CONTENT_TYPE("Content-Type", "content-type"),
		USER_AGENT("User-Agent", "user-agent");

		protected String http;
		protected String json;
		
		Name(String httpHeader, String jsonHeader) {
			this.http = httpHeader;
			this.json = jsonHeader;
		}
		
		public String getHttpName() {
			return http;
		}
		
		public String getJsonName() {
			return this.json;
		}
		
	}
	
	public enum TxnStatus {
		CONTINUE("continue", "continue"),
		COMPLETED("completed", "completed");
		protected String http;
		protected String json;
		
		public static TxnStatus instance(String str) {
			if (str == null)
				return null;
			return TxnStatus.valueOf(str.toUpperCase());
		}
		
		TxnStatus(String http, String json) {
			this.http = http;
			this.json = json;
		}
		
		public String getHttp() {
			return http;
		}
		
		public String getJson() {
			return this.json;
		}
		
		
	}
	
	
	public enum TxnAccept {
		MULTI("multi", "multi"),
		SINGLE("single", "single");
		protected String http;
		protected String json;
		
		public static TxnAccept instance(String str) {
			if (str == null)
				return null;
			return TxnAccept.valueOf(str.toUpperCase());
		}
		
		TxnAccept(String http, String json) {
			this.http = http;
			this.json = json;
		}
		
		public String getHttp() {
			return http;
		}
		
		public String getJson() {
			return this.json;
		}
	}
	
	public enum Method {
		GET,
		POST,
		PUT,
		DELETE;
		
		public static Method instance(String method) {
			if (method == null)
				return null;
			return Method.valueOf(method.toUpperCase());
		}
		
		public String getHttp() {
			return this.toString();
		}
		
		public String getJson() {
			return this.toString();
		}
		
	}
}
