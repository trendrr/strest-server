/**
 * 
 */
package com.trendrr.strest.server.v2.models.json;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;
import com.trendrr.strest.server.v2.models.StrestHeader;
import com.trendrr.strest.server.v2.models.StrestResponse;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonResponse extends StrestJsonBase implements StrestResponse {

	/**
	 * @param map
	 */
	public StrestJsonResponse(DynMap map) {
		super(map);
	}

	public StrestJsonResponse() {
		super();
	}
	
	protected static Log log = LogFactory.getLog(StrestJsonResponse.class);

	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int code, String message) {
		this.map.putWithDot("status.code", code);
		this.map.putWithDot("status.message", message);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode() {
		return this.map.getInteger("status.code", 200);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return this.map.getString("status.message", "OK");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#setTxnStatus(com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus)
	 */
	@Override
	public void setTxnStatus(TxnStatus status) {
		this.addHeader(StrestHeader.Name.TXN_STATUS, status.getJson());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getTxnStatus()
	 */
	@Override
	public TxnStatus getTxnStatus() {
		return TxnStatus.instance(this.getHeader(StrestHeader.Name.TXN_STATUS));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestResponse#getContentBytes()
	 */
	@Override
	public byte[] getContentBytes() {
		//gets the json packet minus the headers.
		DynMap mp = new DynMap();
		mp.putAll(this.getMap());
		mp.remove("strest");
		
		try {
			return mp.toJSONString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
	
	
}
