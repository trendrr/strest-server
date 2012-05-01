/**
 * 
 */
package com.trendrr.strest.server.v2.models;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.server.v2.models.StrestHeader.Method;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created Apr 26, 2012
 * 
 */
public class StrestJsonRequest implements StrestRequest {

	protected static Log log = LogFactory.getLog(StrestJsonRequest.class);

	DynMap map = new DynMap();
	
	public DynMap getMap() {
		return this.map;
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
	 * @see com.trendrr.strest.server.v2.StrestRequest#setMethod(com.trendrr.strest.server.v2.StrestHeader.Method)
	 */
	@Override
	public void setMethod(Method method) {
		this.map.putWithDot("strest.method", method.toString());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getMethod()
	 */
	@Override
	public Method getMethod() {
		return StrestHeader.Method.instance(this.map.getString("strest.method"));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setUri(java.lang.String)
	 */
	@Override
	public void setUri(String uri) {
		this.map.putWithDot("strest.uri", uri);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getUri()
	 */
	@Override
	public String getUri() {
		return this.map.getString("strest.uri");
	}

	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setStrestProtocolVersion(java.lang.String)
	 */
	@Override
	public void setStrestProtocolVersion(String version) {
		this.map.putWithDot("strest.v", version);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getStrestProtocolVersion()
	 */
	@Override
	public String getStrestProtocolVersion() {
		return this.map.getString("strest.v");
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

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setTxnAccept(com.trendrr.strest.server.v2.StrestHeader.TxnAccept)
	 */
	@Override
	public void setTxnAccept(TxnAccept accept) {
		this.addHeader(Name.TXN_ACCEPT, accept.getJson());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getTxnAccept()
	 */
	@Override
	public TxnAccept getTxnAccept() {
		return TxnAccept.instance(this.getHeader(Name.TXN_ACCEPT));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#setParams(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setParams(DynMap params) {
		this.map.putWithDot("strest.params", params);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.StrestRequest#getParams()
	 */
	@Override
	public DynMap getParams() {
		return this.map.getMap("strest.params");
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
	public void setContent(String contentType, String content) {
		this.map.put("content", content);
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
}
