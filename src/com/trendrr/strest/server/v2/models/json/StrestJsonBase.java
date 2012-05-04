/**
 * 
 */
package com.trendrr.strest.server.v2.models.json;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.StrestHeader;
import com.trendrr.strest.server.v2.models.StrestPacketBase;
import com.trendrr.strest.server.v2.models.StrestHeader.Method;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public abstract class StrestJsonBase implements StrestPacketBase {

	protected static Log log = LogFactory.getLog(StrestJsonBase.class);

	DynMap map = new DynMap();
	
	public StrestJsonBase(DynMap map) {
		this.map = map;
	}
	
	public StrestJsonBase() {
		
	}
	/**
	 * gets the DynMap this packet is based on.
	 * @return
	 */
	public DynMap getMap() {
		return this.map;
	}
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestPacketBase#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String header, String value) {
		this.map.putWithDot("strest." + header.toLowerCase(), value);
		
	}
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#addHeader(com.trendrr.strest.server.v2.StrestHeaders, java.lang.String)
	 */
	@Override
	public void addHeader(StrestHeader.Name header, String value) {
		this.map.putWithDot("strest." + header.getJsonName(), value);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getHeader(com.trendrr.strest.server.v2.StrestHeaders)
	 */
	@Override
	public String getHeader(StrestHeader.Name header) {
		return this.map.getString("strest." + header.getJsonName());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getHeader(com.trendrr.strest.server.v2.StrestHeaders)
	 */
	@Override
	public String getHeader(String header) {
		return this.map.getString("strest." + header.toLowerCase());
	}
		
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setStrestProtocolVersion(java.lang.String)
	 */
	@Override
	public void setProtocol(String protocol, float version) {
		this.map.putWithDot("strest.v", version);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getStrestProtocolVersion()
	 */
	@Override
	public float getProtocolVersion() {
		return this.map.getDouble("strest.v").floatValue();
	}

	public String getProtocolName() {
		return "strest";
	}
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setTxnId(java.lang.String)
	 */
	@Override
	public void setTxnId(String id) {
		this.addHeader(StrestHeader.Name.TXN_ID, id);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getTxnId()
	 */
	@Override
	public String getTxnId() {
		return this.getHeader(Name.TXN_ID);
	}

	/**
	 * 
	 * This actually merges the content into this packet, instead of putting into a 'content' field
	 * This is done for backward compatibilty with cheshire requests. 
	 */
	@Override
	public void setContent(DynMap content) {
		this.map.putAll(content);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setContent(java.lang.String, java.lang.String)
	 */
	@Override
	public void setContent(String contentType, byte[] content) {
		log.warn("Setting byte content. no good, need a better way -Dustin");
		this.map.put("content", content); //TODO: this ain't right
		this.addHeader(Name.CONTENT_TYPE, contentType);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getContent()
	 */
	@Override
	public Object getContent() {
		
		return this.map.get("content");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		try {
			return this.map.toJSONString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		this.map = null;
	}
}
